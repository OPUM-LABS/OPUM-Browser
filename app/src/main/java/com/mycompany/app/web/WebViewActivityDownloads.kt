@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.quick.QuickView
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.dialog.DialogVideoMenu
import com.mycompany.app.pref.PrefZtri
import android.view.View
import com.mycompany.app.web.WebViewActivity.Companion.hasCustomZones

/*
 * The browser activity's downloads functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.closeDownFontDialog() {
    val dialogDownFont = this.dialogsState.downFontDialog
    if (dialogDownFont != null) {
        dialogDownFont.dismiss()
        this.dialogsState.downFontDialog = null
    }
}

fun WebViewActivity.addFloatingButtons() {
    if (this.webCoord != null) {
        if (ZoneSettings.downPos.value == 3 && this.imageButton == null) {
            placeDownIcons()
        }
        if (ZoneSettings.youPos.value == 3 && this.videoButton == null) {
            placeYouIcons()
        }
        if (isZoneGestureOff()) {
            addScrollNavis()
        }
        if (ZoneSettings.showUpPos.value != 0) {
            placeUpButton()
        }
        if (ZtriSettings.zoomIcon.value) {
            addZoomButton()
        }
        addUserButton(6)
        addUserButton(7)
        addUserButton(8)
        if (ZtwoSettings.newsUse.value && ZtriSettings.newsIcon.value && !QuickView.isCustomNewsUrl()) {
            addRssButton()
        }
        if (hasCustomZones()) {
            addGestureHint()
        }
    }
}

fun WebViewActivity.showVideoMenu(view: View?, isLong: kotlin.Boolean) {
    if (this.isClosing || isAnyDialogOpen() || this.downloadsState.videoMenuBusy) {
        return
    }
    closeVideoMenuDialog()
    if (view == null) {
        return
    }
    this.downloadsState.videoMenuBusy = false
    if ((isLong == true)) {
        val webDownView = this.videoButton
        if (webDownView != null) {
            webDownView.setHideBlocked(true)
        }
    } else {
        val webDownView = this.imageButton
        if (webDownView != null) {
            webDownView.setHideBlocked(true)
        }
    }
    this.videoMenuDialog = DialogVideoMenu(this, this.dialogsState.webBody, view, isLong, this.tabsState.layoutRtl, WvaVideoMenuListener(this, isLong = (isLong) == true))
}

fun WebViewActivity.isFreshPage(): kotlin.Boolean {
    val count: Int
    val webNestView = this.currentPage
    return webNestView != null && ((webNestView.pageState.also {
        count = it
    }) == 0 || count == 1) && !webNestView.canGoBack()
}

fun WebViewActivity.closeVideoMenuDialog() {
    val dialogVideoMenu = this.videoMenuDialog
    if (dialogVideoMenu != null) {
        if (!this.downloadsState.videoMenuBusy) {
            this.downloadsState.videoMenuBusy = dialogVideoMenu.startClose()
        }
        this.videoMenuDialog = null
    }
    val webDownView = this.imageButton
    if (webDownView != null) {
        webDownView.setHideBlocked(false)
    }
    val videoButton = this.videoButton
    if (videoButton != null) {
        videoButton.setHideBlocked(false)
    }
}

fun WebViewActivity.clearDownCheckFlag() {
    if (BrowserSettings.checkDown.value) {
        BrowserSettings.editLocal { set(BrowserSettings.checkDown, false) }
        putBoolNow(14, this.dialogsState.appContext, "mCheckDown", false)
    }
}

fun WebViewActivity.updateVideoButton(enabled: kotlin.Boolean) {
    if (!enabled || !this.dialogsState.isVideoPage || isInputBusy() || SyncSettings.splash.value) {
        val webDownView = this.videoButton
        if (webDownView != null) {
            webDownView.hide(false)
        }
        showYouIcons(false)
        return
    }
    val webDownView = this.videoButton
    if (webDownView != null) {
        if (ZoneSettings.youPos.value == 3) {
            webDownView.showNow()
        } else {
            webDownView.hide(false)
        }
    }
    showYouIcons(true)
}

fun WebViewActivity.hideFloatingButtons() {
    hidePageOverlays(true)
    updateTransFloatButton(false)
    updateUpButton(false)
    if (this.imageButton != null && ZoneSettings.downPos.value == 3) {
        updateImageButton(false)
    }
    if (this.videoButton != null && ZoneSettings.youPos.value == 3) {
        updateVideoButton(false)
    }
    showUserButtons(false)
    updateRssButton(false)
    updateTranslateButton(false)
    updateZoomButton(false)
}
