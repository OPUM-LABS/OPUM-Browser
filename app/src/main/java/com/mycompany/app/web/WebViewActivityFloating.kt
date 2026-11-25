@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import android.view.View

import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.FloatSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.snsJs2
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.snsJs1
import com.mycompany.app.main.MainUtil.stripHitomiTitle
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.comicSiteKind
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.applyDesktopMode
import com.mycompany.app.main.MainUtil.defaultUserAgent
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.freeMemory
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.main.MainUtil.absoluteUrl
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.downMenuCount
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
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebNestView
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.wview.WebUpView
import com.mycompany.app.ui.browser.ScrollNavi
import com.mycompany.app.view.MyGesNoti
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefFloat
import android.os.Handler
import java.lang.Exception
import java.util.ArrayList

/*
 * The browser activity's floating functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.hideAllButtons() {
    val webDownView = this.imageButton
    if (webDownView != null && !webDownView.hideBlocked) {
        webDownView.hide(false)
    }
    val videoButton = this.videoButton
    if (videoButton != null && !videoButton.hideBlocked) {
        videoButton.hide(false)
    }
    val webFltView = this.floatingState.userButton1
    if (webFltView != null && !webFltView.isHideBlocked()) {
        this.floatingState.userButton1!!.hide(false)
    }
    val userButton2 = this.floatingState.userButton2
    if (userButton2 != null && !userButton2.isHideBlocked()) {
        this.floatingState.userButton2!!.hide(false)
    }
    val userButton3 = this.floatingState.userButton3
    if (userButton3 != null && !userButton3.isHideBlocked()) {
        this.floatingState.userButton3!!.hide(false)
    }
    val rssButton = this.floatingState.rssButton
    if (rssButton != null) {
        val isKeptShown = rssButton.isKeptShown()
        if (!isKeptShown) {
            rssButton.hide(false)
        }
    }
    val translateButton = this.floatingState.translateButton
    if (translateButton != null && !translateButton.isHideBlocked()) {
        this.floatingState.translateButton!!.hide(false)
    }
    val zoomButton = this.floatingState.zoomButton
    if (zoomButton == null || zoomButton.isHideBlocked()) {
        return
    }
    this.floatingState.zoomButton!!.hide(false)
}

fun WebViewActivity.addRssButton() {
    val handler: Handler? = if (this.floatingState.rssButton != null) null else this.mainHandler
    if (this.floatingState.rssButton != null || handler == null) {
        return
    }
    handler.post(WvaAddRssButton(this))
}

fun WebViewActivity.addLockButton() {
    val arrayList: ArrayList<Any?>? = if (this.floatingState.lockButton != null || this.mainHandler == null) null else this.dialogsState.siteLockList
    if (this.floatingState.lockButton != null || this.mainHandler == null || arrayList == null || arrayList.isEmpty()) {
        return
    }
    this.mainHandler!!.post(WvaAddLockButton(this))
}

fun WebViewActivity.showUpButton() {
    val webUpView = this.upButton
    if (webUpView != null) {
        if (webUpView.getVisibility() != View.VISIBLE) {
            if (this.pageState.barOffset != 0) {
                return
            } else {
                this.upButton!!.setAlpha(1.0f)
            }
        }
        val upButton = this.upButton
        upButton!!.scheduleHide()
        upButton.setVisibility(View.VISIBLE)
    }
}

fun WebViewActivity.resetHintTouch() {
    val webMoveText = this.moveHint
    if (webMoveText != null) {
        webMoveText.touching = false
    }
    val webAreaText = this.linkTitlePill
    if (webAreaText != null) {
        webAreaText.touching = false
    }
    val webAreaMenu = this.videoState.areaMenu
    if (webAreaMenu != null) {
        webAreaMenu.touching = false
    }
    val webAreaView = this.dialogsState.areaToolbar
    if (webAreaView != null) {
        webAreaView.touched = false
    }
    val mySnackbar = this.dialogsState.tabUndoSnackbar
    if (mySnackbar != null) {
        mySnackbar.hintShown = false
    }
    val popupBlockSnackbar = this.dialogsState.popupBlockSnackbar
    if (popupBlockSnackbar != null) {
        popupBlockSnackbar.hintShown = false
    }
    val appBlockSnackbar = this.dialogsState.appBlockSnackbar
    if (appBlockSnackbar != null) {
        appBlockSnackbar.hintShown = false
    }
    val snackbar = this.snackbar
    if (snackbar != null) {
        snackbar.hintShown = false
    }
    val webFltView = this.floatingState.lockButton
    if (webFltView != null) {
        webFltView.touching = false
    }
    val webTtsView = this.speechState.ttsButton
    if (webTtsView != null) {
        webTtsView.touching = false
    }
    val webUpView = this.upButton
    if (webUpView != null) {
        webUpView.touching = false
    }
    val webDownView = this.imageButton
    if (webDownView != null) {
        webDownView.touching = false
    }
    val videoButton = this.videoButton
    if (videoButton != null) {
        videoButton.touching = false
    }
    val userButton1 = this.floatingState.userButton1
    if (userButton1 != null) {
        userButton1.touching = false
    }
    val userButton2 = this.floatingState.userButton2
    if (userButton2 != null) {
        userButton2.touching = false
    }
    val userButton3 = this.floatingState.userButton3
    if (userButton3 != null) {
        userButton3.touching = false
    }
    val rssButton = this.floatingState.rssButton
    if (rssButton != null) {
        rssButton.touching = false
    }
    val translateButton = this.floatingState.translateButton
    if (translateButton != null) {
        translateButton.touching = false
    }
    val zoomButton = this.floatingState.zoomButton
    if (zoomButton != null) {
        zoomButton.touching = false
    }
    val dialogViewRead = this.dialogsState.viewReadDialog
    if (dialogViewRead != null) {
        dialogViewRead.hintTouch = false
    }
}

fun WebViewActivity.addScrollNavis() {
    if (this.webCoord != null) {
        try {
            if (this.leftScrollNavi == null) {
                val myScrollNavi = ScrollNavi(this)
                this.leftScrollNavi = myScrollNavi
                myScrollNavi.setup(this.tabsState.layoutRtl, true)
                this.leftScrollNavi!!.setVisibility(View.INVISIBLE)
                val dp64Px = MainApp.dp64
                val layoutParams = com.mycompany.app.ui.browser.OverlayParams(dp64Px, dp64Px)
                layoutParams.gravity = 19
                this.floatingState.overlays!!.addCoordView(this.leftScrollNavi, layoutParams)
            }
            if (this.floatingState.rightScrollNavi == null) {
                val myScrollNavi = ScrollNavi(this)
                this.floatingState.rightScrollNavi = myScrollNavi
                myScrollNavi.setup(this.tabsState.layoutRtl, false)
                this.floatingState.rightScrollNavi!!.setVisibility(View.INVISIBLE)
                val dp64Px = MainApp.dp64
                val layoutParams = com.mycompany.app.ui.browser.OverlayParams(dp64Px, dp64Px)
                layoutParams.gravity = 21
                this.floatingState.overlays!!.addCoordView(this.floatingState.rightScrollNavi, layoutParams)
            }
        } catch (unused: Exception) {
        }
    }
}

fun WebViewActivity.setRssLoading(loading: kotlin.Boolean) {
    val webFltView = this.floatingState.rssButton
    if (webFltView == null) {
        return
    }
    webFltView.setLoad(loading)
    if (loading && ZtwoSettings.newsUse.value && rssButtonAllowed()) {
        this.floatingState.rssButton!!.setHideBlocked(true)
    } else {
        this.floatingState.rssButton!!.setHideBlocked(false)
    }
}

fun WebViewActivity.placeUpButton() {
    var anchorGravity: Int = 0
    var rightMargin: Int = 0
    if (this.webCoord != null) {
        val upPos = ZoneSettings.showUpPos.value
        var leftMargin = 0
        if (upPos == 1) {
            anchorGravity = 51
            leftMargin = MainApp.dp4
            rightMargin = 0
        } else if (upPos == 2) {
            anchorGravity = 49
            rightMargin = 0
        } else {
            if (upPos != 3) {
                return
            }
            rightMargin = MainApp.dp4
            anchorGravity = 53
        }
        val webUpView = this.upButton
        if (webUpView != null) {
            val layoutParams = this.floatingState.overlays?.params(webUpView)
            if (layoutParams != null) {
                if (layoutParams.anchorGravity == anchorGravity && layoutParams.gravity == anchorGravity && layoutParams.leftMargin == leftMargin && layoutParams.rightMargin == rightMargin) {
                    return
                }
                layoutParams.anchorGravity = anchorGravity
                layoutParams.gravity = anchorGravity
                layoutParams.leftMargin = leftMargin
                layoutParams.rightMargin = rightMargin
                this.floatingState.overlays?.relayout(webUpView)
                return
            }
            return
        }
        val dp48Px = MainApp.dp48
        // Anchored to the bottom bar, in the Compose overlay stack
        val layoutParams = com.mycompany.app.ui.browser.OverlayParams(dp48Px, dp48Px)
        layoutParams.anchor = com.mycompany.app.ui.browser.OverlayParams.BOTTOM_BAR
        layoutParams.bottomMargin = MainApp.dp8
        layoutParams.anchorGravity = anchorGravity
        layoutParams.gravity = anchorGravity
        layoutParams.leftMargin = leftMargin
        layoutParams.rightMargin = rightMargin
        try {
            val upButton = WebUpView(this)
            this.upButton = upButton
            upButton.setVisibility(View.GONE)
            this.upButton!!.setBgColors(this.onStartPage)
            val upButtonNow = this.upButton
            val wvaUpButtonListener = WvaUpButtonListener(this)
            upButtonNow!!.longPressEnabled = true
            upButtonNow.listener = wvaUpButtonListener
            this.webCoord!!.setMoveListener(WvaUpButtonPosition(this))
            this.floatingState.overlays!!.addCoordView(this.upButton, layoutParams)
        } catch (unused: Exception) {
        }
    }
}

fun WebViewActivity.holdQuickMenuReopen() {
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    this.quickMenuJustClosed = true
    handler.postDelayed(WvaHoldQuickMenuReopenTask(this), 800L)
}

fun WebViewActivity.addZoomButton() {
    if (this.floatingState.zoomButton != null || this.dialogsState.webBody == null) {
        return
    }
    try {
        val webFltView = WebFltView(this, 4)
        this.floatingState.zoomButton = webFltView
        webFltView.refreshColors()
        this.floatingState.zoomButton!!.setVisibility(View.GONE)
        this.floatingState.zoomButton!!.setFltListener(WvaZoomButtonListener(this))
        val myWebBody = this.dialogsState.webBody
        val zoomButton = this.floatingState.zoomButton
        val width = MainApp.dp48
        this.floatingState.overlays!!.addView(zoomButton, width, width)
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.refreshButtonIcons(code: Int) {
    val themeColor = if (!PdfSettings.topToBottom.value) pageThemeColor() else 0
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        myBarView.updateMoreIcon(code, MainUtil.barTint(themeColor, this.onStartPage))
    }
    val bottomBarView = this.barsState.bottomBarView
    if (bottomBarView != null) {
        bottomBarView.updateMoreIcon(code, MainUtil.barTint(0, this.onStartPage))
    }
    val webFltView = this.floatingState.userButton1
    if (webFltView != null) {
        webFltView.refreshIconFor(code, true)
    }
    val userButton2 = this.floatingState.userButton2
    if (userButton2 != null) {
        userButton2.refreshIconFor(code, true)
    }
    val userButton3 = this.floatingState.userButton3
    if (userButton3 != null) {
        userButton3.refreshIconFor(code, true)
    }
}

fun WebViewActivity.showUserButtons(visible: kotlin.Boolean) {
    if (!visible || this.quickState.editingAddress || this.keyboardOpen || this.videoFrame != null || SyncSettings.splash.value) {
        val webFltView = this.floatingState.userButton1
        if (webFltView != null) {
            webFltView.hide(false)
        }
        val userButton2 = this.floatingState.userButton2
        if (userButton2 != null) {
            userButton2.hide(false)
        }
        val userButton3 = this.floatingState.userButton3
        if (userButton3 != null) {
            userButton3.hide(false)
            return
        }
        return
    }
    val userButton1 = this.floatingState.userButton1
    if (userButton1 != null) {
        userButton1.show(false)
    }
    val userButton2 = this.floatingState.userButton2
    if (userButton2 != null) {
        userButton2.show(false)
    }
    val webFltView = this.floatingState.userButton3
    if (webFltView != null) {
        webFltView.show(false)
    }
}

fun WebViewActivity.updateRssButton(visible: kotlin.Boolean) {
    if (this.floatingState.rssButton == null || this.currentPage == null) {
        return
    }
    if ((visible == true) && ZtwoSettings.newsUse.value && rssButtonAllowed()) {
        this.floatingState.rssButton!!.show(false)
    } else {
        this.floatingState.rssButton!!.hide(false)
    }
}

fun WebViewActivity.applyDarkMode(navBarColor: Int, darkPage: kotlin.Boolean, savedDarkWeb: kotlin.Boolean): kotlin.Boolean {
    if (this.currentPage == null) {
        return false
    }
    if (darkPage != MainUtil.isDarkPage(this.onStartPage)) {
        refreshThemeColors()
    } else if (navBarColor != navBarColorNow()) {
        applyBodyColors(pageThemeColor(), navBarColorNow())
    }
    if (savedDarkWeb == AppState.darkWeb) {
        return false
    }
    val webNestFrame: WebNestFrame? = if (!this.nextPageAnim && !this.addTabAnim) this.tabsState.currentFrame else null
    if (!this.nextPageAnim && !this.addTabAnim && webNestFrame != null) {
        webNestFrame.setDarkMode(this.onStartPage)
    }
    this.currentPage!!.setDarkWeb(AppState.darkWeb)
    val webFltView = this.floatingState.lockButton
    if (webFltView != null) {
        webFltView.refreshAlpha(true)
    }
    val webTtsView = this.speechState.ttsButton
    webTtsView?.refreshColors()
    val userButton1 = this.floatingState.userButton1
    if (userButton1 != null) {
        userButton1.refreshTheme()
    }
    val userButton2 = this.floatingState.userButton2
    if (userButton2 != null) {
        userButton2.refreshTheme()
    }
    val userButton3 = this.floatingState.userButton3
    if (userButton3 != null) {
        userButton3.refreshTheme()
    }
    val rssButton = this.floatingState.rssButton
    if (rssButton != null) {
        rssButton.refreshAlpha(true)
    }
    val translateButton = this.floatingState.translateButton
    if (translateButton != null) {
        translateButton.refreshAlpha(true)
    }
    val zoomButton = this.floatingState.zoomButton
    if (zoomButton != null) {
        zoomButton.refreshAlpha(true)
    }
    return true
}

fun WebViewActivity.dismissHints(): kotlin.Boolean {
    if ((this.miscState.touchDownEdge != 0 && BrowserSettings.showNavi.value) || this.quickState.editingAddress) {
        return true
    }
    val webMoveText = this.moveHint
    if (webMoveText != null && webMoveText.shown) {
        if (webMoveText.touching) {
            return true
        }
        webMoveText.hideSoon()
    }
    val webAreaText = this.linkTitlePill
    if (webAreaText != null && webAreaText.shown) {
        if (webAreaText.touching) {
            return true
        }
        webAreaText.hideSoon()
    }
    val webAreaMenu = this.videoState.areaMenu
    if (webAreaMenu != null && webAreaMenu.shown) {
        if (webAreaMenu.touching) {
            return true
        }
        webAreaMenu.hideSoon()
    }
    val webAreaView = this.dialogsState.areaToolbar
    if (webAreaView != null && webAreaView.touched) {
        return true
    }
    val mySnackbar = this.dialogsState.tabUndoSnackbar
    if (mySnackbar != null && mySnackbar.hintShown) {
        return true
    }
    val popupBlockSnackbar = this.dialogsState.popupBlockSnackbar
    if (popupBlockSnackbar != null && popupBlockSnackbar.hintShown) {
        return true
    }
    val appBlockSnackbar = this.dialogsState.appBlockSnackbar
    if (appBlockSnackbar != null && appBlockSnackbar.hintShown) {
        return true
    }
    val snackbar = this.snackbar
    val webNestView: WebNestView? = if ((snackbar != null && snackbar.hintShown)) null else this.currentPage
    if ((snackbar != null && snackbar.hintShown) || webNestView == null || webNestView.quickPanelOpen() || this.videoState.quickSchEdit != null) {
        return true
    }
    val webFltView = this.floatingState.lockButton
    if (webFltView != null && webFltView.touching) {
        return true
    }
    val webTtsView = this.speechState.ttsButton
    if (webTtsView != null && webTtsView.touching) {
        return true
    }
    val webUpView = this.upButton
    if ((webUpView != null && webUpView.touching) || this.floatingState.upButtonHidden) {
        return true
    }
    val webDownView = this.imageButton
    if (webDownView != null && webDownView.touching) {
        return true
    }
    val videoButton = this.videoButton
    if (videoButton != null && videoButton.touching) {
        return true
    }
    val userButton1 = this.floatingState.userButton1
    if (userButton1 != null && userButton1.touching) {
        return true
    }
    val userButton2 = this.floatingState.userButton2
    if (userButton2 != null && userButton2.touching) {
        return true
    }
    val userButton3 = this.floatingState.userButton3
    if (userButton3 != null && userButton3.touching) {
        return true
    }
    val rssButton = this.floatingState.rssButton
    if (rssButton != null && rssButton.touching) {
        return true
    }
    val translateButton = this.floatingState.translateButton
    if (translateButton != null && translateButton.touching) {
        return true
    }
    val zoomButton = this.floatingState.zoomButton
    if (zoomButton != null && zoomButton.touching) {
        return true
    }
    val dialogViewRead = this.dialogsState.viewReadDialog
    return dialogViewRead != null && dialogViewRead.hintTouch
}

fun WebViewActivity.updateScrollBar() {
    val myScrollBar = this.scrollBar
    if (myScrollBar != null) {
        if (ZoneSettings.scrollPos.value != 0) {
            myScrollBar.setVisibility(View.INVISIBLE)
        } else {
            myScrollBar.setVisibility(View.GONE)
        }
        val webNestView = this.currentPage
        if (webNestView != null) {
            webNestView.setScrollPos(ZoneSettings.scrollPos.value)
        }
        val scrollBarSide = this.scrollBarSide
        val scrollPos = ZoneSettings.scrollPos.value
        if (scrollBarSide != scrollPos) {
            this.scrollBarSide = scrollPos
            val layoutParams = if (scrollPos == 0) null else this.floatingState.overlays?.params(this.scrollBar)
            if (scrollPos == 0 || layoutParams == null) {
                return
            }
            if (this.scrollBarSide == 1) {
                layoutParams.gravity = 3
                this.scrollBar!!.setPosLeft(true)
            } else {
                layoutParams.gravity = 5
                this.scrollBar!!.setPosLeft(false)
            }
            this.floatingState.overlays?.relayout(this.scrollBar)
        }
    }
}

fun WebViewActivity.updateTranslateButton(visible: kotlin.Boolean) {
    if (this.floatingState.translateButton == null || this.currentPage == null || this.dialogsState.webBody == null) {
        return
    }
    if ((visible == true)) {
        if ((if (!isTranslateActive()) false else AlbumSettings.transPage.value) && !isInputBusy() && !SyncSettings.splash.value) {
            this.floatingState.translateButton!!.show(false)
            return
        }
    }
    this.floatingState.translateButton!!.hide(false)
}

fun WebViewActivity.onRefreshButton() {
    if (this.currentPage != null) {
        reloadPage()
        if (TextUtils.isEmpty(this.currentPage!!.getUrl())) {
            updatePageProgress(100)
        }
        val handler: Handler? = if (!this.onStartPage || !ZtwoSettings.newsUse.value || this.floatingState.rssButton == null) null else this.mainHandler
        if (!this.onStartPage || !ZtwoSettings.newsUse.value || this.floatingState.rssButton == null || handler == null) {
            return
        }
        handler.post(WvaNewsFloatRefresh(this))
    }
}

fun WebViewActivity.updateZoomButton(visible: kotlin.Boolean) {
    if (this.floatingState.zoomButton == null) {
        return
    }
    if (!visible || !ZtriSettings.zoomIcon.value || isInputBusy() || SyncSettings.splash.value) {
        this.floatingState.zoomButton!!.hide(false)
    } else {
        this.floatingState.zoomButton!!.show(false)
    }
}

fun WebViewActivity.closeTabBarItemMenu() {
    if (this.tabBarItemMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.tabBarItemMenu!!.dismissNow()
        this.tabBarItemMenu = null
    }
}

fun WebViewActivity.closeFloatingPopupMenu() {
    if (this.floatingState.popupMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.floatingState.popupMenu!!.dismissNow()
        this.floatingState.popupMenu = null
    }
}

fun WebViewActivity.resetScrollNavis(keep: kotlin.Boolean, animate: kotlin.Boolean) {
    if (this.floatingState.gestureMode.toInt() != 0 || this.currentPage == null) {
        return
    }
    resetGestureState(true, false)
    if (!keep) {
        val myScrollNavi = this.leftScrollNavi
        if (myScrollNavi != null) {
            myScrollNavi.hide()
        }
        val rightScrollNavi = this.floatingState.rightScrollNavi
        if (rightScrollNavi != null) {
            rightScrollNavi.hide()
            return
        }
        return
    }
    val layoutRtl = this.tabsState.layoutRtl
    val leftScrollNavi = this.leftScrollNavi
    if (layoutRtl) {
        if ((animate == true)) {
            if (leftScrollNavi != null) {
                leftScrollNavi.hide()
            }
            val myScrollNavi = this.floatingState.rightScrollNavi
            if (myScrollNavi != null) {
                myScrollNavi.hide()
                return
            }
            return
        }
        if (leftScrollNavi != null) {
            leftScrollNavi.hide()
        }
        val myScrollNavi = this.floatingState.rightScrollNavi
        if (myScrollNavi != null) {
            myScrollNavi.hide()
            return
        }
        return
    }
    if ((animate == true)) {
        if (leftScrollNavi != null) {
            leftScrollNavi.hide()
        }
        val myScrollNavi = this.floatingState.rightScrollNavi
        if (myScrollNavi != null) {
            myScrollNavi.hide()
            return
        }
        return
    }
    if (leftScrollNavi != null) {
        leftScrollNavi.hide()
    }
    val myScrollNavi = this.floatingState.rightScrollNavi
    if (myScrollNavi != null) {
        myScrollNavi.hide()
    }
}

fun WebViewActivity.hasFloatingButtons(): kotlin.Boolean {
    val mode = if (this.floatingState.floatingShown || this.keyboardOpen) 0 else this.floatingState.gestureZone
    if (this.floatingState.floatingShown || this.keyboardOpen || mode.toInt() == 1 || mode.toInt() == 2) {
        return false
    }
    return if (this.imageButton == null && this.videoButton == null && this.floatingState.userButton1 == null && this.floatingState.userButton2 == null && this.floatingState.userButton3 == null && this.floatingState.rssButton == null && this.floatingState.translateButton == null && this.floatingState.zoomButton == null) false else true
}

fun WebViewActivity.addUserButton(slot: Int) {
    val index: Int
    val webFltView: WebFltView?
    if (slot == 7) {
        index = FloatSettings.flt2Icon.value
    } else {
        index = if (slot == 8) FloatSettings.flt3Icon.value else FloatSettings.flt1Icon.value
    }
    if (index >= 0 && index < com.mycompany.app.main.MainConst.mainMenuNames.size && index != 0) {
        if (slot == 7) {
            webFltView = this.floatingState.userButton2
        } else {
            webFltView = if (slot == 8) this.floatingState.userButton3 else this.floatingState.userButton1
        }
        if (webFltView != null || this.dialogsState.webBody == null) {
            return
        }
        try {
            val newButton = WebFltView(this, slot)
            newButton.refreshColors()
            newButton.setFltListener(WvaFloatGroupListener(this))
            val myWebBody = this.dialogsState.webBody
            val width = MainApp.dp48
            this.floatingState.overlays!!.addView(newButton, width, width)
            if (slot == 7) {
                this.floatingState.userButton2 = newButton
                return
            } else if (slot == 8) {
                this.floatingState.userButton3 = newButton
                return
            } else {
                this.floatingState.userButton1 = newButton
                return
            }
        } catch (unused: Exception) {
            return
        }
    }
    if (slot == 7) {
        val userButton2 = this.floatingState.userButton2
        if (userButton2 != null) {
            userButton2.release()
            val myWebBody = this.dialogsState.webBody
            if (myWebBody != null) {
                this.floatingState.overlays!!.removeView(this.floatingState.userButton2)
            }
            this.floatingState.userButton2 = null
            return
        }
        return
    }
    if (slot == 8) {
        val userButton3 = this.floatingState.userButton3
        if (userButton3 != null) {
            userButton3.release()
            val myWebBody = this.dialogsState.webBody
            if (myWebBody != null) {
                this.floatingState.overlays!!.removeView(this.floatingState.userButton3)
            }
            this.floatingState.userButton3 = null
            return
        }
        return
    }
    val userButton1 = this.floatingState.userButton1
    if (userButton1 != null) {
        userButton1.release()
        val myWebBody = this.dialogsState.webBody
        if (myWebBody != null) {
            this.floatingState.overlays!!.removeView(this.floatingState.userButton1)
        }
        this.floatingState.userButton1 = null
    }
}

fun WebViewActivity.addGestureHint() {
    if (this.floatingState.gestureHint != null || this.webCoord == null) {
        return
    }
    val myGesNoti = MyGesNoti(this)
    this.floatingState.gestureHint = myGesNoti
    myGesNoti.setVisibility(View.GONE)
    val dp64Px = MainApp.dp64
    val layoutParams = com.mycompany.app.ui.browser.OverlayParams(dp64Px, dp64Px)
    layoutParams.gravity = 17
    this.floatingState.overlays!!.addCoordView(this.floatingState.gestureHint, layoutParams)
}
