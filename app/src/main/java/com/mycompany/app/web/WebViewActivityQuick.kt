@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.PathSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.isImagePath
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.hasPermission
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.isPhnGif
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.evalConfirmJs
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookRecent.Companion.loadItems
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.main.MainUtil.checkTransJs
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.parseColorText
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.pref.PrefPath.Companion.savePaths
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
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
import com.mycompany.app.main.MainUtil.readTextFile
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.screenBrightnessPercent
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebNestView
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.quick.QuickView
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.main.MainConst
import android.os.Build
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.pref.PrefAlbum
import android.view.GestureDetector
import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.dialog.DialogQuickEdit
import com.mycompany.app.help.KeyHelper
import com.mycompany.app.quick.QuickSearch
import androidx.core.view.WindowInsetsAnimationCompat
import com.mycompany.app.pref.PrefZtri
import androidx.core.view.ViewCompat
import com.mycompany.app.pref.PrefPath
import com.google.android.gms.security.ProviderInstaller
import android.graphics.Bitmap
import android.view.View
import android.view.Window
import java.util.ArrayList

/*
 * The browser activity's quick functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.finishStartup() {
    val found: kotlin.Boolean
    if (this.webCoord != null) {
        if (!this.quickState.tabBarRefreshed) {
            this.quickState.tabBarRefreshed = true
            refreshTabBar(2, true)
        }
        val window: Window? = if (!MainConst.isApi30 && this.keyHelper == null && this.dialogsState.appContext != null) getWindow() else null
        if (!MainConst.isApi30 && this.keyHelper == null && this.dialogsState.appContext != null && window != null) {
            window.setSoftInputMode(16)
            this.keyHelper =
                KeyHelper(this.dialogsState.appContext, this.webCoord, BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, WvaKeyHelperListener(this))
        }
        if (!this.quickState.imeAnimationSet) {
            this.quickState.imeAnimationSet = true
            val viewZ = decorViewCached()
            if (viewZ != null) {
                val wvaQuickSearchImeAnimation: WvaQuickSearchImeAnimation = WvaQuickSearchImeAnimation(this)
                ViewCompat.setWindowInsetsAnimationCallback(viewZ, wvaQuickSearchImeAnimation)
            }
        }
        startWebClean()
        if (!this.quickState.sdkVersionChecked) {
            this.quickState.sdkVersionChecked = true
            var isFolder = false
            if (PathSettings.sdkVer.value < Build.VERSION.SDK_INT) {
                PathSettings.editLocal { set(PathSettings.sdkVer, Build.VERSION.SDK_INT) }
                found = true
            } else {
                found = false
            }
            if (PathSettings.checkPath.value) {
                PathSettings.editLocal { set(PathSettings.checkPath, false) }
                if (TextUtils.isEmpty(PathSettings.pathDown.value) && TextUtils.isEmpty(PathSettings.pathAlbum.value) && TextUtils.isEmpty(
                        PathSettings.docUri.value
                    )
                ) {
                    isFolder = true
                } else {
                    PathSettings.editLocal { set(PathSettings.pathDown, "") }
                    PathSettings.editLocal { set(PathSettings.pathAlbum, "") }
                    PathSettings.editLocal { set(PathSettings.docUri, "") }
                    PrefPath.savePaths(this.dialogsState.appContext)
                    if (!this.isClosing && !isAnyDialogOpen()) {
                        val dialogConfirm = this.dialogsState.pathsResetDialog
                        if (dialogConfirm != null) {
                            dialogConfirm.dismiss()
                            this.dialogsState.pathsResetDialog = null
                        }
                        val pathsResetDialog = DialogConfirm(
                            this,
                            null as String?,
                            null as String?,
                            PathsResetDialogButtons(this)
                        )
                        this.dialogsState.pathsResetDialog = pathsResetDialog
                        pathsResetDialog.setOnDismissListener(PathsResetDialogDismissed(this))
                    }
                }
            } else {
                isFolder = found
            }
            if (isFolder) {
                PrefPath.savePaths(this.dialogsState.appContext)
            }
        }
        if (!this.quickState.providerInstalled) {
            this.quickState.providerInstalled = true
            ProviderInstaller.installIfNeededAsync(this.dialogsState.appContext!!, WvaProviderInstallListener())
        }
        if (this.tabsState.gestureDetector == null) {
            this.tabsState.gestureDetector = GestureDetector(this, WvaGestureZoneListener(this))
        }
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
    }
}

fun WebViewActivity.showQuickDeleteDialog(quickItem: QuickItem?, position: Int) {
    if (this.isClosing) {
        return
    }
    if (this.quickState.quickSubView == null && isAnyDialogOpen()) {
        return
    }
    closeQuickDeleteDialog()
    if (position == 0) {
        return
    }
    val dialogDeleteItem = DialogDeleteItem(this, WvaQuickDeleteListener(this, showQuickDeleteDialogQuickItem = quickItem, position = position))
    this.quickDeleteDialog = dialogDeleteItem
    dialogDeleteItem.setOnDismissListener(WvaQuickDeleteDialogDismiss(this))
}

fun WebViewActivity.showQuickEditDialog(quickItem: QuickItem?) {
    var arrayList: ArrayList<com.mycompany.app.quick.QuickAdapter.QuickSubItem?>? = null
    val isFolder: kotlin.Boolean
    val editable: kotlin.Boolean
    val path: String?
    val label: String?
    val favicon: Bitmap?
    val color: Int
    if (this.isClosing) {
        return
    }
    if (this.quickState.quickSubView == null && isAnyDialogOpen()) {
        return
    }
    closeQuickEditDialog()
    if (quickItem != null) {
        val isFolderNow = quickItem.isFolder
        val pathNow = quickItem.path
        val labelNow = quickItem.label
        val colorNow = quickItem.color
        if (isFolderNow) {
            editable = true
            isFolder = isFolderNow
            label = labelNow
            color = colorNow
            favicon = null
            path = pathNow
            arrayList = quickItem.subItems
        } else {
            editable = true
            isFolder = isFolderNow
            label = labelNow
            color = colorNow
            arrayList = null
            path = pathNow
            favicon = null
        }
    } else {
        val currentUrl = currentPageUrl(true)
        if (TextUtils.isEmpty(currentUrl)) {
            return
        }
        isFolder = false
        editable = false
        arrayList = null
        path = currentUrl
        label = pageTitleOf(this.currentPage, currentUrl)
        favicon = this.currentPage!!.getFavicon()
        color = 0
    }
    val dialogQuickEdit =
        DialogQuickEdit(this, isFolder, path, label, favicon, color, editable, arrayList, WvaQuickAdded(this), quickItem?.letterOnly == true)
    this.dialogsState.quickEditDialog = dialogQuickEdit
    dialogQuickEdit.setOnDismissListener(QuickEditDialogDismissed(this))
}

fun WebViewActivity.addQuickSearch() {
    if (this.quickSearch != null || this.webCoord == null) {
        return
    }
    val quickSearch = QuickSearch(this)
    quickSearch.dataActivity = this
    quickSearch.recentAll = true
    this.quickSearch = quickSearch
    quickSearch.setVisibility(View.GONE)
    val quickSearchNow = this.quickSearch
    val onStartPage = this.onStartPage
    val reversed = !isSingleBarMode()
    val wvaQuickSearchEditListener = WvaQuickSearchEditListener(this)
    quickSearchNow!!.ownerActivity = this
    quickSearchNow.dataActivity = this
    quickSearchNow.searchListener = wvaQuickSearchEditListener
    val columns = QuickSearch.columnsFor(quickSearchNow.isLandscapeNow())
    quickSearchNow.darkState = onStartPage
    quickSearchNow.darkModeState = isDarkMode()
    quickSearchNow.darkHomeState = BrowserSettings.darkHome.value
    quickSearchNow.darkUiState = AppState.darkUi
    quickSearchNow.reversed = reversed
    quickSearchNow.columns = columns
    quickSearchNow.quickSmallState = PdfSettings.quickSmall.value
    quickSearchNow.quickPlusState = ZtriSettings.quickPlus.value
    quickSearchNow.clipShownState = quickSearchNow.clipShown
    this.floatingState.overlays!!.addCoordView(this.quickSearch, com.mycompany.app.ui.browser.OverlayParams(-1, -1))
    this.webCoord!!.post(WvaQuickSearchRefresh(this))
}

fun WebViewActivity.statusBarInset(): Int {
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return 0
    }
    if (MainConst.isApi30) {
        return myWebBody.rectTop
    }
    if (BrowserSettings.showStatus.value) {
        return statusBarInsetPx(this.dialogsState.appContext, getWindow(), decorViewCached())
    }
    return 0
}

fun WebViewActivity.closeNewsMenuDialog() {
    val dialogNewsMenu = this.newsMenuDialog
    if (dialogNewsMenu != null) {
        if (!this.newsMenuOpen) {
            this.newsMenuOpen = dialogNewsMenu.startClose()
        }
        this.newsMenuDialog = null
    }
    val webFltView = this.floatingState.rssButton
    if (webFltView == null || this.quickState.quickNews != null) {
        return
    }
    webFltView.setHideBlocked(false)
}

fun WebViewActivity.showQuickAddDialog(url: String?, quickItem: QuickItem?, fromHtml: kotlin.Boolean) {
    var url = url
    val label: String?
    val found: kotlin.Boolean
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen() || this.pageState.readDialogOpen || this.quickState.quickAddBusy) {
        return
    }
    val fromHtmlFlag = false
    if (quickItem != null) {
        url = quickItem.path
        if (TextUtils.isEmpty(url)) {
            url = quickItem.link
        }
        label = quickItem.label
        found = true
    } else {
        label = pageTitleOf(this.currentPage, url)
        found = false
    }
    val dialogViewRead = this.dialogsState.viewReadDialog
    if (dialogViewRead == null || !dialogViewRead.isSamePage(url, found, fromHtml)) {
        val viewReadDialog = this.dialogsState.viewReadDialog
        this.dialogsState.viewReadDialog = null
        if (viewReadDialog != null) {
            viewReadDialog.closeReader(null)
        }
        this.pageState.readDialogOpen = false
        this.quickState.quickAddBusy = true
        stopTts()
        finishActionMode()
        val webNestView: WebNestView? = if (!fromHtml) this.currentPage else null
        if (!fromHtml && webNestView != null) {
            webNestView.pauseOnce()
        }
        this.lastTransLang = this.transLangCode
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaShowViewReadDialog(this, url = url, title = label, showQuickAddDialogQuickItem = quickItem, fromHtml = (fromHtml) == true, transOn = fromHtmlFlag))
    }
}

fun WebViewActivity.rssButtonAllowed(): kotlin.Boolean {
    var webNestView: WebNestView? = null
    return !SyncSettings.splash.value && (this.currentPage.also {
        webNestView = it
    }) != null && this.onStartPage && !webNestView!!.quickPanelOpen() && this.newsSearchDialog == null && this.videoState.downUrlDialog == null && !this.quickState.editingAddress && !this.keyboardOpen && this.videoState.quickSchEdit == null && this.quickState.quickSubView == null && this.videoFrame == null &&
        // news lives on its own page (2026-11-19): the button shows there only, not over the shortcuts
        webNestView.quickPanel?.itemAdapter?.let { it.newsPage && !it.rssPage } == true
}

fun WebViewActivity.refreshThemeColors() {
    if (this.currentPage != null) {
        val themeColor = pageThemeColor()
        val themeLight = pageThemeLight()
        this.currentPage!!.setDarkUi(AppState.darkUi)
        applyThemeColors(themeColor, themeLight, navBarColorNow())
        val webAreaView = this.dialogsState.areaToolbar
        if (webAreaView != null) {
            webAreaView.applyDark()
        }
        val quickControl = this.quickControl
        if (quickControl != null) {
            quickControl.setColor(this.onStartPage)
        }
        val webSearchAdapter = this.searchAdapter
        if (webSearchAdapter != null) {
            webSearchAdapter.notifyDataSetChanged()
        }
    }
}

fun WebViewActivity.closeQuickSubView() {
    val quickSubView = this.quickState.quickSubView
    this.quickState.quickSubView = null
    if (quickSubView != null) {
        if (this.tabsState.barsDisabledMode.toInt() == 3) {
            setBarsDisabled(3, false)
        }
        if (quickSubView.closing) {
            val webNestView = this.currentPage
            val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
            if (webNestView != null && quickView != null) {
                quickView.notifyShown(true)
            }
            val quickSearch = this.quickSearch
            if (quickSearch != null) {
                quickSearch.loadItems()
            }
        }
        quickSubView.ghostShown = false
        quickSubView.endDrag()
        quickSubView.checkAll = null
        quickSubView.deleteButton = null
        val myRecyclerView = quickSubView.subGrid
        if (myRecyclerView != null) {
            myRecyclerView.release()
            quickSubView.subGrid = null
        }
        val quickAdapter = quickSubView.subAdapter
        if (quickAdapter != null) {
            quickAdapter.release()
            quickSubView.subAdapter = null
        }
        val myCoverView = quickSubView.cover
        if (myCoverView != null) {
            myCoverView.release()
            quickSubView.cover = null
        }
        quickSubView.appContext = null
        quickSubView.subListener = null
        quickSubView.folderPath = null
        quickSubView.headerBar = null
        quickSubView.countText = null
        quickSubView.edgeView = null
        quickSubView.cornerTop1 = null
        quickSubView.cornerTop2 = null
        quickSubView.cornerBottomLeft = null
        quickSubView.cornerBottomRight = null
        quickSubView.actionBar = null
        quickSubView.editButton = null
        val myWebBody = this.dialogsState.webBody
        if (myWebBody != null) {
            this.floatingState.overlays!!.removeView(quickSubView)
        }
    }
}

fun WebViewActivity.stopNews() {
    if (this.currentPage != null) {
        val quickNews = this.quickState.quickNews
        if (quickNews != null) {
            quickNews.release()
            this.quickState.quickNews = null
        }
        this.quickState.newsListType = 0
        val quickView = this.currentPage!!.quickPanel
        if (quickView != null) {
            quickView.loadNews(true)
        }
    }
}

fun WebViewActivity.closeTransCtrlMenu() {
    if (this.dialogsState.transCtrlMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.dialogsState.transCtrlMenu!!.dismissNow()
        this.dialogsState.transCtrlMenu = null
    }
}

fun WebViewActivity.closeAddressMenu() {
    if (this.addressMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.addressMenu!!.dismissNow()
        this.addressMenu = null
    }
}

fun WebViewActivity.onTranslateButton(transText: String?) {
    val transState = this.dialogsState.transState
    if (transState.toInt() == 1) {
        showToast(this, AppR.string.wait_retry)
        return
    }
    if (transState.toInt() == 2) {
        closeSetTransDialog()
        closeTransControl()
        showTransMsgDialog()
        return
    }
    hideTransControl()
    if (this.dialogsState.transState.toInt() != 3) {
        this.dialogsState.transState = 0.toByte()
        this.transActive = true
        this.miscState.transMsgPending = true
        this.transText = null
        this.transRequested = false
        if (AlbumSettings.transMode.value == 0) {
            this.autoTransPending = false
        }
        updateTransIcon(false)
        if (TextUtils.isEmpty(transText)) {
            this.transText = AlbumSettings.transPick.value
        } else {
            this.transText = transText
        }
        checkTransJs(this.currentPage, "onTransUser")
    } else if (!applyTransLang()) {
        evalConfirmJs(this.currentPage, "confirm")
    }
    closeSetTransDialog()
}
