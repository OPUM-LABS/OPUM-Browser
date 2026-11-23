@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import android.view.View

import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import android.os.Handler
import java.util.ArrayList

/*
 * The browser activity's find functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.applyFindBarMargin() {
    if (this.quickState.editingAddress || this.onStartPage) {
        return
    }
    val keyboardBottomMargin = this.keyboardBottomMargin
    val myWebCoord = this.webCoord
    val layoutParams: RelativeLayout.LayoutParams? = if (myWebCoord != null) (myWebCoord.getLayoutParams() as RelativeLayout.LayoutParams?) else null
    if (myWebCoord != null && layoutParams != null && layoutParams.bottomMargin != keyboardBottomMargin) {
        layoutParams.bottomMargin = keyboardBottomMargin
        this.webCoord!!.requestLayout()
    }
    if (this.keyboardBottomMargin != 0) {
        val myFindView = this.findView
        if (myFindView != null) {
            val myEditPure = myFindView.editField
            if (if (myEditPure == null) false else myEditPure.isFocused()) {
                return
            }
        }
        var height = this.keyboardInset
        val findView = this.findView
        if (findView != null && !findView.tintBar) {
            height -= findView.getHeight()
        }
        val handler: Handler? = if (this.findState.touchDownY + MainApp.dp40 < height) null else this.mainHandler
        if (this.findState.touchDownY + MainApp.dp40 < height || handler == null) {
            return
        }
        handler.postDelayed(WvaFindScrollToTouch(this), 200L)
    }
}

fun WebViewActivity.updateBottomBarForKeyboard() {
    val myBarFrame = this.bottomBarFrame
    if (myBarFrame != null) {
        if (!this.keyboardOpen) {
            if (this.tabsState.barsDisabledMode.toInt() == 0) {
                myBarFrame.setVisibility(View.VISIBLE)
            }
        } else if (this.quickState.editingAddress) {
            myBarFrame.setVisibility(View.GONE)
        } else if (this.findView == null) {
            myBarFrame.setVisibility(View.GONE)
        } else if (isSingleBarMode()) {
            this.bottomBarFrame!!.setVisibility(View.GONE)
        }
    }
}

fun WebViewActivity.openInNextPage(url: String?, refererOrHtml: String?, isHtml: kotlin.Boolean) {
    if (this.tabsState.currentFrame == null || this.findState.openInNextPageBusy) {
        return
    }
    this.findState.openInNextPageBusy = true
    this.dialogsState.hasRealPage = false
    if (this.videoFrame != null) {
        exitVideoFrame()
    }
    stopWebClean()
    hidePageOverlays(true)
    hideKeyboard()
    if (!isHtml && openTextLink(url)) {
        finishProgress()
        destroyPendingPage()
        this.findState.openInNextPageBusy = false
        return
    }
    exitFind(false)
    settleSwipes()
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaOpenInNextPageTask(this, isHtml = (isHtml) == true, url = url, refererOrHtml = refererOrHtml))
}

fun WebViewActivity.restorePageBottomMargin() {
    if (!BrowserSettings.fixBot.value || this.quickState.editingAddress || this.onStartPage || this.findView != null || isAnyDialogOpen()) {
        return
    }
    if (this.keyboardOpen) {
        this.barsState.findBarMargin = 0
        setPageBottomMargin(0)
        return
    }
    val findBarMargin = this.barsState.findBarMargin
    if (findBarMargin != 0) {
        setPageBottomMargin(findBarMargin)
        this.barsState.findBarMargin = 0
    }
}

fun WebViewActivity.selectTab(currentTabIndex: Int, select: kotlin.Boolean) {
    val arrayList: ArrayList<WebTabItem?>? = if (this.tabsState.currentFrame == null || this.currentPage == null) null else this.tabsState.tabItems
    if (this.tabsState.currentFrame == null || this.currentPage == null || arrayList == null || currentTabIndex < 0 || currentTabIndex >= arrayList.size || this.findState.selectTabBusy) {
        return
    }
    this.findState.selectTabBusy = true
    finishProgress()
    exitFind(true)
    val changed = this.currentTabIndex != currentTabIndex
    this.currentTabIndex = currentTabIndex
    clampTabIndex()
    if (!changed) {
        val myWebCoord = this.webCoord
        if (myWebCoord != null) {
            myWebCoord.post(WvaSelectTabReloadTask(this, select = (select) == true))
            return
        }
        return
    }
    if (this.keyboardOpen && !this.barsState.addressEditAfterAdd) {
        hideKeyboard()
    }
    this.currentPage!!.setWebViewClient(com.mycompany.app.main.MainUtil.quietWebClient)
    this.currentPage!!.setWebChromeClient(null)
    this.currentPage!!.setDownloadListener(null)
    stopWebClean()
    hidePageOverlays(true)
    val myWebCoord = this.webCoord
    if (myWebCoord != null) {
        myWebCoord.post(WvaSelectTabTask(this))
    }
}

fun WebViewActivity.exitFind(hideKeyboard: kotlin.Boolean) {
    if (this.findView == null) {
        return
    }
    if ((hideKeyboard == true) && this.keyboardOpen) {
        closeKeyboard(this.dialogsState.appContext, this.webCoord)
    }
    closeFindBar()
}

fun WebViewActivity.reloadPage() {
    if (this.currentPage != null) {
        this.transRequested = false
        stopWebClean()
        hidePageOverlays(true)
        stopTts()
        if (this.currentPage!!.prepareReload() || TextUtils.isEmpty(this.dialogsState.pageUrl)) {
            return
        }
        this.currentPage!!.openUrl(this.dialogsState.pageUrl, null)
    }
}

fun WebViewActivity.closeFindIfEmpty() {
    val myFindView = this.findView
    if (myFindView == null || !TextUtils.isEmpty(myFindView.findText)) {
        return
    }
    closeFindBar()
}
