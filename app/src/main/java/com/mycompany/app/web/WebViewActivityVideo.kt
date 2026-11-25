@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.resetTextSizePref
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.applyScreenRotation
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.youTubeAutoplayJs
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
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.newMediaUri
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
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.lighterColor
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.needsDesktopUa
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.isRootPath
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.main.MainUtil.navBarInset
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
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
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebNestView
import android.widget.RelativeLayout
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.main.MainConst
import android.os.Build
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.web.WebVideoImage
import com.mycompany.app.dialog.DialogVideoList
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.quick.QuickSchEdit
import com.mycompany.app.view.MySnackbar.SnackItem
import com.mycompany.app.pref.PrefZtri
import android.os.Handler
import android.view.Window
import java.util.ArrayList
import com.mycompany.app.web.WebViewActivity.Companion.Zn

/*
 * The browser activity's video functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.resetBusyFlags() {
    this.videoState.blockedTouchDownTime = 0L
    this.quickState.quickSearchImeAnimationBusy = false
    this.miscState.startPageSetupBusy = false
    this.dialogsState.applySettingsChangeBusy = false
    this.tabsState.goBackPageBusy = false
    this.tabsState.startGoForwardBusy = false
    this.findState.openInNextPageBusy = false
    this.videoState.addTabImplBusy = false
    this.tabsState.createBackTabBusy = false
    this.tabsState.reloadTabViewsBusy = false
    this.dialogsState.applyTabLimitBusy = false
    this.pageState.rebuildTabsBusy = false
    this.tabsState.switchToTabBusy = false
    this.tabsState.exitAppBusy = false
    this.tabsState.finishTabAnimBusy = false
    this.findState.selectTabBusy = false
    this.pageState.chromeClientBusy = false
    this.dialogsState.showDeleteTabsDialogBusy = false
    this.dialogsState.switchSecretModeBusy = false
    this.videoState.webQuickListenerBusy = false
    this.miscState.videoRateInjectBusy = false
    this.dialogsState.barUpdateBusy = false
    this.videoState.pageFrameBusy = false
    this.pageState.pageFrameListenerBusy = false
    this.dialogsState.setSystemBarsHiddenBusy = false
    this.dialogsState.applySystemBarsBusy = false
    this.videoState.videoEnterBusy = false
    this.videoState.videoExitBusy = false
    this.dialogsState.unusedBusy = false
    this.dialogsState.unusedBusy2 = false
    val webVideoFrame = this.videoFrame
    if (webVideoFrame == null) {
        return
    }
    webVideoFrame.scriptLoaded()
}

fun WebViewActivity.closeDownBlobDialog() {
    val dialogDownBlob = this.dialogsState.downBlobDialog
    if (dialogDownBlob != null) {
        dialogDownBlob.dismiss()
        this.dialogsState.downBlobDialog = null
        if (this.videoFrame != null) {
            this.videoState.videoDialogOpen = false
            applyScreenRotation(this, false)
        }
    }
}

fun WebViewActivity.closeDownUrlDialog() {
    val dialogDownUrl = this.videoState.downUrlDialog
    if (dialogDownUrl != null) {
        dialogDownUrl.dismiss()
        this.videoState.downUrlDialog = null
        if (this.videoFrame != null) {
            this.videoState.videoDialogOpen = false
            applyScreenRotation(this, false)
        }
    }
}

fun WebViewActivity.handleLinkLongPress() {
    var handler: Handler?
    if (this.dialogsState.longPressBusy || this.videoFrame != null) {
        return
    }
    var delayMs: Long = 400
    if (!this.videoState.webViewActivityBusy || !this.videoState.longPressInspectBusy) {
        if (!TextUtils.isEmpty(this.videoState.longPressSrcUrl)) {
        }
        handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.postDelayed(WvaLongPressFollowUp(this), delayMs)
    }
    val longPressLinkUrl = this.videoState.longPressLinkUrl
    val srcUrl = if (!TextUtils.isEmpty(this.videoState.longPressSrcUrl)) this.videoState.longPressSrcUrl else this.quickState.longPressImage
    if (TextUtils.isEmpty(longPressLinkUrl) && TextUtils.isEmpty(srcUrl)) {
        val longPressTitle = this.videoState.longPressTitle
        if (TextUtils.isEmpty(longPressTitle)) {
            val menuHandler: Handler? = if (!BrowserSettings.areaBlock.value || this.videoState.longPressPending) null else this.mainHandler
            if (!BrowserSettings.areaBlock.value || this.videoState.longPressPending || menuHandler == null) {
                return
            }
            menuHandler.postDelayed(WvaOfferAreaMenu(this), 400L)
            return
        }
        if (this.videoState.longPressPending) {
            return
        }
        val mainHandler = this.mainHandler
        if (mainHandler != null) {
            mainHandler.postDelayed(WvaOfferLinkTitle(this, linkTitleText = longPressTitle), 400L)
            return
        }
        return
    }
    delayMs = 0
    handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.postDelayed(WvaLongPressFollowUp(this), delayMs)
}

fun WebViewActivity.closeDownGuideDialog() {
    val dialogConfirm = this.downGuideDialog
    if (dialogConfirm != null) {
        dialogConfirm.dismiss()
        this.downGuideDialog = null
        if (this.videoFrame != null) {
            this.videoState.videoDialogOpen = false
            applyScreenRotation(this, false)
        }
    }
}

fun WebViewActivity.onAdsDialogResult(jsChanged: kotlin.Boolean, listChanged: kotlin.Boolean, userChanged: kotlin.Boolean, refreshPage: kotlin.Boolean) {
    this.dialogsState.adsDialogUrl = null
    if ((jsChanged == true) && !refreshPage && BrowserSettings.adsBlock.value) {
        runInBackground(WvaOnAdsDialogResultTask(this))
    }
    if ((refreshPage == true)) {
        reloadPage()
    }
    if ((listChanged == true)) {
        runInBackground(WvaWebCleanRun(this, userChanged))
    }
}

/** [lock] true stops the bars from following the page scroll (was the AppBarLayout child's scroll flags 0 / 21). */
fun WebViewActivity.lockBarScroll(lock: kotlin.Boolean) {
    val bars = this.barScroll ?: return
    bars.enabled = !lock
}

fun WebViewActivity.showVideoListDialog(type: Int) {
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeVideoListDialog()
    if (this.videoFrame != null) {
        this.videoState.videoDialogOpen = true
        AppState.darkUi = true
    }
    val dialogVideoList = DialogVideoList(this, this.currentPage, this.dialogsState.pageUrl, type, WvaVideoListListener(this))
    this.videoListDialog = dialogVideoList
    dialogVideoList.setOnDismissListener(VideoListDialogDismissed(this))
    if (this.videoFrame != null) {
        this.videoListDialog!!.noDim = true
    }
}

fun WebViewActivity.navBarColorNow(): Int {
    if (this.videoFrame != null) {
        return 0
    }
    return editorTextColor()
}

fun WebViewActivity.attachPageListeners(webNestFrame: WebNestFrame?, webNestView: WebNestView?, index: Int) {
    if (index == 2 || webNestView == null) {
        return
    }
    if (index == 0) {
        webNestView.setWebViewClient(WvaLocalWebViewClient(this))
        webNestView.setWebChromeClient(WvaLocalChromeClient(this))
    }
    webNestView.setDownloadListener(PageDownloadListener(this))
    webNestView.setListener(WvaPageViewListener(this))
    webNestView.setQuickListener(WvaWebQuickListener(this))
    attachAnimListener(webNestView)
    if (webNestFrame != null) {
        webNestFrame.setListener(WvaPageFrameListener(this))
    }
    registerForContextMenu(webNestView)
}

fun WebViewActivity.requestOpenTypeDialog(longExtra: Long, intExtra: Int, path: String?, mime: String?, type: Int) {
    if (this.videoFrame != null || isReaderOpen()) {
        return
    }
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.post(WvaShowOpenTypeDialog(this, openTypeId = longExtra, openKind = intExtra, withoutFile = ((type) != 0), path = path, mime = mime))
}

fun WebViewActivity.closeSetDownDialog() {
    val dialogSetDown = this.setDownDialog
    if (dialogSetDown != null) {
        dialogSetDown.dismiss()
        this.setDownDialog = null
        if (this.videoFrame != null) {
            this.videoState.videoDialogOpen = false
            applyScreenRotation(this, false)
        }
    }
}

fun WebViewActivity.refreshPageState() {
    val urlChanged = this.pageState.urlChanged
    val hostChanged = this.pageState.hostChanged
    val videoHostEntered = this.pageState.videoHostEntered
    if (this.currentPage == null) {
        this.dialogsState.lastLoadUrl = null
        return
    }
    refreshStartPage()
    updateUpButton(true)
    if (this.currentPage != null) {
        updateDownBadge()
        if (this.videoFrame == null || this.dialogsState.videoMode.toInt() != 4) {
            val otherDownUrl: kotlin.Boolean = WebNestView.Companion.isOtherDownUrl(this.currentPage!!.downUrl, this.dialogsState.pageUrl)
            val webNestView = this.currentPage
            if (otherDownUrl) {
                webNestView!!.resetMedia(null)
                this.videoDownFound = false
                val webVideoFrame = this.videoFrame
                if (webVideoFrame != null) {
                    webVideoFrame.setVideoDown(false)
                }
            } else if (!TextUtils.isEmpty(webNestView!!.downVideo)) {
                this.videoDownFound = true
                val webVideoFrame = this.videoFrame
                if (webVideoFrame != null) {
                    webVideoFrame.setVideoDown(true)
                }
            }
            if (otherDownUrl) {
                updateImageButton(false)
            } else {
                updateImageButton(true)
            }
        } else {
            val isRootPath = !MainUtil.isRootPath(this.dialogsState.pageUrl)
            this.videoDownFound = isRootPath
            this.videoFrame!!.setVideoDown(isRootPath)
        }
        if (this.videoState.freshLoad) {
            this.videoState.freshLoad = false
        } else {
            val myWebCoord = this.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaRefreshPageStateTask(this))
            }
        }
    }
    updateImageButton(true)
    showUserButtons(true)
    if (!ZtwoSettings.quickDown.value) {
        updateRssButton(true)
    }
    updateZoomButton(true)
    if (this.onStartPage) {
        finishProgress()
    } else {
        offerScriptInstall(this.dialogsState.pageUrl)
    }
    if (!urlChanged && !hostChanged && this.tabBar == null) {
        this.dialogsState.lastLoadUrl = null
        return
    }
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return
    }
    myWebBody.post(WvaRefreshPageBarsTask(this, urlChanged = urlChanged, hostChanged = hostChanged, videoHostEntered = videoHostEntered))
}

fun WebViewActivity.showPopupBlockedSnackbar(pkg: String?, pendingUrl: String?) {
    var pendingUrl = pendingUrl
    val webVideoFrame = this.videoFrame
    if ((webVideoFrame != null && webVideoFrame.isReloadPending()) || TextUtils.isEmpty(pkg) || "about:blank" == pkg) {
        return
    }
    if (TextUtils.isEmpty(pendingUrl)) {
        pendingUrl = this.dialogsState.pageUrl
    }
    val snackItem = SnackItem()
    snackItem.pkg = pkg
    snackItem.pendingUrl = pendingUrl
    updateUpButton(false)
    closeAllSnackbars()
    addBlockCount(1, true)
    val mySnackbar = MySnackbar(this)
    this.dialogsState.popupBlockSnackbar = mySnackbar
    mySnackbar.setAppBarView(this.barScroll)
    this.dialogsState.popupBlockSnackbar!!.snackItem = snackItem
    this.dialogsState.popupBlockSnackbar!!.setActivity(this)
    val popupBlockSnackbar = this.dialogsState.popupBlockSnackbar
    val myWebCoord = this.webCoord
    val popBlocked = AppR.string.pop_blocked
    val secondActive = ZoneSettings.popNoti.value
    val wvaShowPopupMenuDialog = WvaShowPopupMenuDialog(this)
    popupBlockSnackbar!!.snackType = 4.toByte()
    popupBlockSnackbar.siteUrl = pkg
    popupBlockSnackbar.secondActive = secondActive
    popupBlockSnackbar.show(myWebCoord, popBlocked, 4, 3, wvaShowPopupMenuDialog)
}

fun WebViewActivity.showQuickSchEdit(engine: Int, openFlag: kotlin.Boolean) {
    if (this.webCoord != null) {
        lockBarScroll(openFlag)
        val quickSchEdit = this.videoState.quickSchEdit
        if ((openFlag == true)) {
            if (quickSchEdit != null) {
                return
            }
            val newQuickSchEdit = QuickSchEdit(this, this.dialogsState.appContext, engine, WvaQuickViewListener(this))
            this.videoState.quickSchEdit = newQuickSchEdit
            this.floatingState.overlays!!.addCoordView(newQuickSchEdit, com.mycompany.app.ui.browser.OverlayParams(-1, -1))
            return
        }
        if (quickSchEdit != null) {
            clearHandler(quickSchEdit.mainHandler)
            quickSchEdit.mainHandler = null
            val quickView = quickSchEdit.shownView
            if (quickView != null) {
                quickView.releaseView()
                quickSchEdit.shownView = null
            }
            val quickControl = quickSchEdit.shownControl
            if (quickControl != null) {
                quickControl.release()
                quickSchEdit.shownControl = null
            }
            quickSchEdit.hostActivity = null
            quickSchEdit.appContext = null
            this.floatingState.overlays?.removeView(this.videoState.quickSchEdit)
            this.videoState.quickSchEdit = null
        }
    }
}

fun WebViewActivity.applySystemBarsBehavior(showStatusBar: kotlin.Boolean) {
    val window: Window? = if (MainConst.isApi30 || this.dialogsState.webBody == null) null else getWindow()
    if (MainConst.isApi30 || this.dialogsState.webBody == null || window == null) {
        return
    }
    if (this.videoFrame != null) {
        setDecorFits(window, false)
    } else {
        setDecorFits(window, showStatusBar)
    }
}

fun WebViewActivity.updateSystemBarPadding(showStatusBar: kotlin.Boolean, showNavBar: kotlin.Boolean) {
    val window: Window? = if (MainConst.isApi30 || this.dialogsState.webBody == null) null else getWindow()
    if (MainConst.isApi30 || this.dialogsState.webBody == null || window == null) {
        return
    }
    val navPadding = if ((showStatusBar == true) || !showNavBar || this.videoFrame != null || isLandscapeScreen()) 0 else navBarInset(this.dialogsState.appContext, window, decorViewCached())
    if (this.dialogsState.webBody!!.getPaddingBottom() != navPadding) {
        this.dialogsState.webBody!!.setPadding(0, 0, 0, navPadding)
    }
}

fun WebViewActivity.closeVideoListDialog() {
    val dialogVideoList = this.videoListDialog
    if (dialogVideoList != null) {
        dialogVideoList.dismiss()
        this.videoListDialog = null
        if (this.videoFrame == null || this.videoState.downUrlDialog != null) {
            return
        }
        this.videoState.videoDialogOpen = false
        applyScreenRotation(this, false)
    }
}

fun WebViewActivity.addAreaToolbar(openFlag: kotlin.Boolean) {
    if (this.isClosing || this.dialogsState.areaToolbar != null || this.dialogsState.webBody == null || this.videoFrame != null || this.videoState.areaToolbarBusy) {
        return
    }
    this.videoState.areaToolbarBusy = true
    this.areaToolbarOn = (openFlag) == true
    val myWebCoord = this.webCoord
    if (myWebCoord != null) {
        myWebCoord.post(WvaAddAreaToolbar(this))
    }
}

fun WebViewActivity.setUpVideoFrame() {
    if (this.videoFrame == null) {
        this.videoState.videoEnterBusy = false
        return
    }
    val videoMode = this.dialogsState.videoMode
    var downFound = true
    if (videoMode.toInt() == 4) {
        downFound = true xor MainUtil.isRootPath(this.dialogsState.pageUrl)
        this.videoDownFound = downFound
    } else if (!this.videoDownFound || videoMode.toInt() == 2) {
        downFound = false
    }
    this.videoFrame!!.setupFrame(pageTitleOf(this.currentPage, this.dialogsState.pageUrl), downFound, WvaVideoFrameListener(this))
    this.videoState.videoEnterBusy = false
}

fun WebViewActivity.isInputBusy(): kotlin.Boolean {
    val webNestView = this.currentPage
    if (webNestView != null) {
        return this.quickState.editingAddress || this.keyboardOpen || webNestView.coversPage() || this.videoState.quickSchEdit != null || this.videoFrame != null
    }
    return false
}

fun WebViewActivity.findPageVideos(src: String?, page: String?, poster: String?, arrayList: ArrayList<Any?>?) {
    var found = false
    if (arrayList != null) {
        val size = arrayList.size
        var index = 0
        while (index < size) {
            val any: Any? = arrayList.get(index)
            index++
            val videoUrl = any as String?
            val webNestView = this.currentPage
            if (webNestView == null) {
                return
            }
            if (webNestView.addPageVideo(this.dialogsState.pageUrl, videoUrl, page, poster)) {
                found = true
            }
        }
    } else {
        val webNestView: WebNestView? = if (src == null) null else this.currentPage
        if (src == null || webNestView == null) {
            return
        }
        if (webNestView.addPageVideo(this.dialogsState.pageUrl, src, page, poster)) {
            found = true
        }
    }
    if (found) {
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaVideoDownFound(this))
        return
    }
    this.videoDownFound = true
    val webVideoFrame = this.videoFrame
    if (webVideoFrame != null) {
        webVideoFrame.updateDownIcon()
    }
}

fun WebViewActivity.showDownGuideDialog() {
    if (!ReadSettings.guideDown.value || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeDownGuideDialog()
    if (this.videoFrame != null) {
        this.videoState.videoDialogOpen = true
        AppState.darkUi = true
    }
    val dialogConfirm = DialogConfirm(this, DownGuideDialogListener(this))
    this.downGuideDialog = dialogConfirm
    dialogConfirm.setOnDismissListener(DownGuideDialogDismissed(this))
    if (this.videoFrame != null) {
        this.downGuideDialog!!.noDim = true
    }
}

fun WebViewActivity.exitVideoFrame() {
    val webVideoFrame = this.videoFrame
    this.videoFrame = null
    if (webVideoFrame != null) {
        this.videoState.videoExitBusy = true
        webVideoFrame.exitCustomView()
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaExitVideoFrameTask(this, exitedVideoFrame = webVideoFrame, exitedVideoType = webVideoFrame.videoType, videoWasImage = webVideoFrame is WebVideoImage))
    }
}

fun WebViewActivity.applyTopInset(showBars: kotlin.Boolean) {
    var myWebBody: MyWebBody? = null
    if (this.webCoord != null) {
        val valueAnimator = this.miscState.handEnterAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
        }
        val handExitAnim = this.miscState.handExitAnim
        if (handExitAnim != null) {
            handExitAnim.cancel()
        }
        val topInset = if (!showBars || this.quickState.editingAddress || this.keyboardOpen || this.videoFrame != null || !isPortraitSingleWindow() || (this.dialogsState.webBody.also {
                myWebBody = it
            }) == null) 0 else myWebBody!!.handTopPos(this)
        Zn = topInset
        val isApi = MainConst.isApi30
        val webBody = this.dialogsState.webBody
        if (isApi) {
            pageThemeColor()
            webBody!!.insetTop = topInset
            webBody.topColor = webBody.topBarColor
        } else if (topInset == 0) {
            webBody!!.setOldTheme(0)
        } else {
            webBody!!.setOldTheme(pageThemeColor())
        }
        this.webCoord!!.setTranslationY(0.0f)
        val layoutParams = this.webCoord!!.getLayoutParams() as RelativeLayout.LayoutParams?
        if (layoutParams == null || layoutParams.topMargin == topInset) {
            return
        }
        layoutParams.topMargin = topInset
        this.webCoord!!.requestLayout()
    }
}
