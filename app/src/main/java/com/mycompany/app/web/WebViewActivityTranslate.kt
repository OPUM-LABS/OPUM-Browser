@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.parseFloatSafe
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.dialog.DialogTransMsg
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.web.WebViewActivity.PrevPageListener
import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.dialog.DialogSetTrans
import android.view.View
import java.lang.Exception

/*
 * The browser activity's translate functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.tintHandModeBar(color: Int) {
    val myWebBody = this.dialogsState.webBody
    if (myWebBody != null) {
        val onStartPage = this.onStartPage
        if (myWebBody.handNotiIcon != null) {
            myWebBody.updateHandIcon(this, onStartPage, color)
        }
        val bar = this.dialogsState.handModeBar ?: return
        val tint = MainUtil.barTint(color, this.onStartPage)
        bar.tint(tint != 0, BarFace.pressColorFor(tint, false))
    }
}

fun WebViewActivity.updateKeyboardState() {
    if (this.translateState.keyboardHeight == 0) {
        this.keyboardOpen = false
        this.keyboardInset = 0
        this.keyboardBottomMargin = 0
        return
    }
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return
    }
    this.keyboardOpen = true
    val height = myWebBody.getHeight() - this.dialogsState.webBody!!.getPaddingTop()
    val keyboardHeight = this.translateState.keyboardHeight
    this.keyboardInset = height - keyboardHeight
    this.keyboardBottomMargin = keyboardHeight - this.dialogsState.webBody!!.getPaddingBottom()
}

fun WebViewActivity.showSetTransDialog() {
    if (this.currentPage == null || this.isClosing) {
        return
    }
    if (this.translateState.transLangMenuListenerBusy || !isAnyDialogOpen()) {
        closeSetTransDialog()
        this.lastTransLang = this.transLangCode
        val dialogSetTrans = DialogSetTrans(this, currentPageUrl(false), WvaShowSetTransDialogListener(this))
        this.setTransDialog = dialogSetTrans
        dialogSetTrans.setOnDismissListener(WvaSetTransDialogDismiss(this))
    }
}

fun WebViewActivity.showTransMsgDialog() {
    if (this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeTransMsgDialog()
    val dialogTransMsg = DialogTransMsg(this, WvaShowTransMsgDialogListener(this))
    this.transMsgDialog = dialogTransMsg
    dialogTransMsg.setOnCancelListener(TransMsgDialogCancelled(this))
    this.transMsgDialog!!.setOnDismissListener(TransMsgDialogDismissed(this))
}

fun WebViewActivity.closeSetTransDialog() {
    val dialogSetTrans = this.setTransDialog
    if (dialogSetTrans != null) {
        dialogSetTrans.dismiss()
        this.setTransDialog = null
    }
    this.translateState.transLangMenuListenerBusy = false
}

fun WebViewActivity.closeTransMsgDialog() {
    val dialogTransMsg = this.transMsgDialog
    if (dialogTransMsg != null) {
        dialogTransMsg.dismiss()
        this.transMsgDialog = null
    }
}

fun WebViewActivity.showTransControl(view: View?) {
    if (this.isClosing || isAnyDialogOpen() || view == null || this.dialogsState.webBody == null) {
        return
    }
    closeTransControl()
    if (this.translateState.pendingTransControl == null) {
        this.translateState.pendingTransControl = WebTransControl(this)
        val handler = this.mainHandler
        if (handler != null) {
            handler.post(WvaShowTransControl(this, view = view))
        }
    }
}

fun WebViewActivity.goBackPage(prevPageListener: PrevPageListener?) {
    if (this.tabsState.goBackPageBusy) {
        return
    }
    this.tabsState.goBackPageBusy = true
    if (closeReaderFirst()) {
        prevPageListener?.onDone()
        this.tabsState.goBackPageBusy = false
        return
    }
    val webNestFrame = this.tabsState.currentFrame
    if (webNestFrame != null && webNestFrame.backAnim == null && this.tabsState.tabSwipeState.toInt() == 0 && this.tabsState.pageSwipeState.toInt() == 0) {
        this.tabsState.tabUndoAllowed = BrowserSettings.tabRestore.value
        startBackImpl(false, null, prevPageListener)
    } else {
        if (prevPageListener != null) {
            prevPageListener.onDone()
        }
        this.tabsState.goBackPageBusy = false
    }
}

fun WebViewActivity.closeTransControl() {
    val frameLayout = this.transControlFrame
    val webTransControl = this.transControl
    this.transControlFrame = null
    this.transControl = null
    if (frameLayout != null) {
        try {
            val myWebBody = this.dialogsState.webBody
            if (myWebBody != null) {
                this.floatingState.overlays!!.removeView(frameLayout)
            }
            if (webTransControl != null) {
                webTransControl.release()
            }
        } catch (unused: Exception) {
        }
    } else if (webTransControl != null) {
        webTransControl.release()
    }
    val webFltView: WebFltView? = if (!AlbumSettings.transPage.value) null else this.floatingState.translateButton
    if (!AlbumSettings.transPage.value || webFltView == null) {
        return
    }
    webFltView.setHideBlocked(false)
}

fun WebViewActivity.hideTransControl() {
    val webTransControl = this.transControl
    if (webTransControl != null) {
        webTransControl.animateShow(false)
    }
}
