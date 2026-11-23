@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import android.view.View

import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.db.book.DbBookTmem.Companion.get
import com.mycompany.app.db.book.DbBookTrans.Companion.get
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.view.MyBarFrame
import android.os.Handler

/*
 * The browser activity's find callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaKeyboardRelayout(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class UpdateBottomBarForKeyboardTask : Runnable {

        internal inner class UpdateBarsStep : Runnable {
            override fun run() {
                val wvaKeyboardRelayout = this@WvaKeyboardRelayout
                val webViewActivity = wa
                if (webViewActivity.keyboardOpen) {
                    return
                }
                webViewActivity.updateBottomBarForKeyboard()
                val activity = wa
                val myFindView = activity.findView
                if (myFindView == null || myFindView.tintBar) {
                    return
                }
                activity.closeFindIfEmpty()
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.updateKeyboardState()
            webViewActivity.layoutBars()
            webViewActivity.applyFindBarMargin()
            webViewActivity.restorePageBottomMargin()
            val handler: Handler? = if (webViewActivity.keyboardOpen) null else webViewActivity.mainHandler
            if (webViewActivity.keyboardOpen || handler == null) {
                return
            }
            handler.post(UpdateBarsStep())
        }
    }

    override fun run() {
        val handMode = TtsSettings.handMode.value
        val webViewActivity = wa
        if (handMode) {
            webViewActivity.applyTopInset(!webViewActivity.keyboardOpen)
        }
        if (webViewActivity.keyboardOpen) {
            webViewActivity.updateBottomBarForKeyboard()
            if (!webViewActivity.quickState.editingAddress && !webViewActivity.dialogsState.keyboardRelayout) {
                webViewActivity.dialogsState.keyboardRelayout = true
                webViewActivity.checkSavePassword()
            }
            webViewActivity.hideFloatingButtons()
        } else {
            val myFindView = webViewActivity.findView
            if (myFindView != null && myFindView.tintBar) {
                webViewActivity.closeFindIfEmpty()
            }
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(UpdateBottomBarForKeyboardTask())
    }
}

class WvaFindScrollToTouch(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        var height = webViewActivity.keyboardInset
        val myFindView = webViewActivity.findView
        if (myFindView != null && !myFindView.tintBar) {
            height -= myFindView.getHeight()
        }
        webViewActivity.currentPage!!.scrollTo(
            0,
            webViewActivity.currentPage!!.getScrollY() - (height - ((webViewActivity.findState.touchDownY + MainApp.dp48).toInt()))
        )
    }
}

class WvaShowBottomBarFrame(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        var myWebCoord: MyWebCoord? = null
        var myBarFrame: MyBarFrame? = null
        val webViewActivity = wa
        if (!webViewActivity.quickState.editingAddress && webViewActivity.findView == null && webViewActivity.tabsState.barsDisabledMode.toInt() == 0) {
            if ((!webViewActivity.keyboardOpen || (webViewActivity.webCoord.also {
                    myWebCoord = it
                }) == null || myWebCoord!!.getPaddingBottom() == 0) && (webViewActivity.bottomBarFrame.also {
                    myBarFrame = it
                }) != null) {
                myBarFrame!!.setVisibility(View.VISIBLE)
            }
        }
    }
}

class WvaFindViewRefresh(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.findView == null) {
            return
        }
        webViewActivity.findView!!.build(webViewActivity.pageThemeColor(), webViewActivity.pageThemeLight(), webViewActivity.onStartPage)
    }
}
