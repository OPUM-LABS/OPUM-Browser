@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.ui.state.AppState
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import ch.opumlabs.opumbrowser.R as AppR
import android.animation.Animator
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.wildcardHost
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.isHmgUrl
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.main.MainUtil.imageUrlFallback
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.siteNameOf
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.setUserAgent
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.applyDesktopMode
import com.mycompany.app.main.MainUtil.backupBitmapCopy
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.main.MainUtil.resetTextSizePref
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isSecretLocked
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.translateUrlFor
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.insetsController
import com.mycompany.app.db.book.DbBookTab.Companion.updateParents
import com.mycompany.app.db.book.DbBookTab.Companion.deleteTabs
import com.mycompany.app.main.MainUtil.toggleSystemBars
import com.mycompany.app.main.MainUtil.applySystemBarsMode
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.hasZtwoFlag
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openKeyboard
import com.mycompany.app.db.book.DbBookRecent.Companion.clearSecret
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.openIntentUri
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.openInExternalApp
import com.mycompany.app.main.MainUtil.clipboardText
import com.mycompany.app.pref.PrefZtwo.Companion.saveSearch
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.openWithChooser
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.transLangFromCookie
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.newMediaUri
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.main.MainUtil.clearCookies
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.main.MainUtil.openPlayStore
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
import com.mycompany.app.main.MainUtil.isAudioPlaying
import com.mycompany.app.main.MainUtil.findMediaJs
import com.mycompany.app.db.book.DbBookRecent.Companion.clearAll
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.db.book.DbBookTmem.Companion.get
import com.mycompany.app.db.book.DbBookTrans.Companion.get
import com.mycompany.app.main.MainUtil.absoluteUrl
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.injectVideoObserver
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.windowStatusColor
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.db.book.DbBookHistory.Companion.clearAll
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.db.book.DbBookTab.Companion.clearAll
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.durationText
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import com.mycompany.app.web.WebNestView
import com.mycompany.app.main.MainActivity
import android.webkit.WebViewClient
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import android.content.Intent
import android.animation.ValueAnimator
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.web.WebNestView.WebBundleListener
import com.mycompany.app.web.WebNestView.WebStyleListener
import com.mycompany.app.main.MainWebDestroy
import com.mycompany.app.web.WebNestView.WebAnimListener
import com.mycompany.app.web.WebNestView.WebBackListener
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.db.DbUtil
import android.animation.ValueAnimator.AnimatorUpdateListener
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.main.MainConst
import android.os.Build
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.web.WebGridDialog.WebImgListener
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.dialog.DialogConfirm.DialogConfListener
import android.content.DialogInterface
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.view.MyFadeFrame
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.MyFadeListener
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.db.book.DbBookTab
import android.database.sqlite.SQLiteDatabase
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.web.WebViewActivity.TabLimitListener
import com.mycompany.app.pull.MyPullView
import com.mycompany.app.main.MainFilterSvc
import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.web.WebViewActivity.TabViewListener
import com.mycompany.app.main.MainScriptSvc
import android.speech.tts.TextToSpeech
import com.mycompany.app.web.WebViewActivity.PrevPageListener
import com.mycompany.app.dialog.DialogLoadImg
import com.mycompany.app.view.MyEngineMenu
import com.mycompany.app.dialog.DialogVideoMenu
import com.mycompany.app.dialog.DialogVideoList
import com.mycompany.app.dialog.DialogSetDesk
import com.mycompany.app.dialog.DialogPreview
import com.mycompany.app.help.KeyHelper
import com.mycompany.app.dialog.DialogWebView
import com.mycompany.app.dialog.DialogSetFull
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.dialog.DialogSetAdblock
import com.mycompany.app.wview.WebAreaMenu
import androidx.viewpager2.widget.ViewPager2
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogSetPrivacy
import com.mycompany.app.view.MyDialogBottom
import com.mycompany.app.dialog.DialogPrintPage
import android.view.ViewTreeObserver
import com.mycompany.app.help.KeyHelper.KeyHelperListener
import android.webkit.DownloadListener
import com.mycompany.app.main.MainActivity.MainInsetListener
import com.mycompany.app.wview.WebFltView.FltViewListener
import com.mycompany.app.dialog.DialogVideoMenu.VideoMenuListener
import com.mycompany.app.dialog.DialogVideoList.VkvListListener
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.main.MainWebDestroy.WebBusyListener
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.dialog.DialogConfirm.DialogConfBtnListener
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.view.MyEngineMenu.MyEngineListener
import com.mycompany.app.view.MyDialogBottom.UserShowListener
import com.mycompany.app.view.GlideApp
import com.mycompany.app.dialog.DialogSetDown.SetDownListener
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import com.mycompany.app.dialog.DialogWebView.DialogWebListener
import com.mycompany.app.dialog.DialogSetAdblock.DialogAdsListener
import com.mycompany.app.wview.WebAreaMenu.WebAreaMenuListener
import com.mycompany.app.db.book.DbBookPms
import android.webkit.WebViewDatabase
import android.text.style.ClickableSpan
import com.mycompany.app.dialog.DialogLoadImg.LoadImgListener
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.dialog.DialogSetPrivacy.TabDeletedListener
import com.mycompany.app.view.MyWebBody.HandListener
import android.speech.tts.TextToSpeech.OnInitListener
import com.mycompany.app.pull.MyPullView.OnRefreshListener
import android.database.Cursor
import android.os.Handler
import android.view.View
import java.io.File
import java.lang.Exception
import java.util.ArrayList
import java.util.Locale
import com.mycompany.app.web.WebViewActivity.Companion.checkedPageUrl

/*
 * The browser activity's misc callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaFinishTabAddOnBack(@JvmField val wa: WebViewActivity) : WebViewActivity.PrevPageListener {
    override fun onDone() {
        wa.finishTabAdd()
    }
}

class WvaStartAddressEdit(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.setAddressEditing(true)
    }
}

class WvaKeyHelperListener(@JvmField val wa: WebViewActivity) : KeyHelperListener {

    internal inner class ApplyKeyboardInsetStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.keyboardOpen && webViewActivity.isAnyDialogOpen()) {
                return
            }
            if (TtsSettings.handMode.value) {
                webViewActivity.applyTopInset(!webViewActivity.keyboardOpen)
            }
            webViewActivity.updateBottomBarForKeyboard()
            if (webViewActivity.keyboardOpen) {
                if (!webViewActivity.quickState.editingAddress && !webViewActivity.dialogsState.keyboardRelayout) {
                    webViewActivity.dialogsState.keyboardRelayout = true
                    webViewActivity.checkSavePassword()
                }
                webViewActivity.hideFloatingButtons()
            } else {
                webViewActivity.closeFindIfEmpty()
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaKeyboardClosedTask(webViewActivity))
        }
    }

    override fun onKeyboard() {
        // Nothing to do on API 30+ (minSdk 32): the old pre-30 keyboard relayout is gone.
    }

    override fun onKeyboardShown(shown: Boolean) {
        val webViewActivity = wa
        webViewActivity.keyboardOpen = shown
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ApplyKeyboardInsetStep())
    }
}

class WvaBackTabClient() : WebViewClient() {
    override fun onRenderProcessGone(
        webView: WebView?,
        renderProcessGoneDetail: RenderProcessGoneDetail?
    ): Boolean {
        onPageRenderGone(webView, renderProcessGoneDetail)
        return true
    }
}

class WvaWebBackListener(@JvmField val wa: WebViewActivity) : WebBackListener {

    internal inner class InjectScriptsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.injectUserScripts(true)
            if (!webViewActivity.dialogsState.isVideoPage || webViewActivity.dialogsState.videoRateOff) {
                webViewActivity.resumeWebDestroy()
                return
            }
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(WvaLocalWebViewClientTask(webViewActivity))
        }
    }

    override fun onBack() {
        val userScript = PdfSettings.userScript.value
        val webViewActivity = wa
        if (userScript || (webViewActivity.dialogsState.isVideoPage && !webViewActivity.dialogsState.videoRateOff)) {
            webViewActivity.runInBackground(InjectScriptsStep())
        } else {
            webViewActivity.resumeWebDestroy()
        }
    }
}

class WvaApplyTabLimit(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.applyTabLimit(WvaTabLimitListener(webViewActivity))
    }
}

class WvaKeyboardClosedTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.restorePageBottomMargin()
        val sdkInt = if (!BrowserSettings.showNavi.value) Build.VERSION.SDK_INT else 0
        if (!BrowserSettings.showNavi.value && sdkInt >= 30) {
            val window = webViewActivity.getWindow()
            val viewZ = webViewActivity.decorViewCached()
            var windowInsetsControllerCompatB = webViewActivity.insetsControllerCompat()
            val keyboardOpen = webViewActivity.keyboardOpen
            if (sdkInt >= 30 && window != null && (windowInsetsControllerCompatB != null || (insetsController(
                    window,
                    viewZ
                ).also { windowInsetsControllerCompatB = it }) != null)
            ) {
                if (keyboardOpen) {
                    windowInsetsControllerCompatB!!.show(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                } else {
                    windowInsetsControllerCompatB!!.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                }
            }
        }
        if (webViewActivity.keyboardOpen) {
            return
        }
    }
}

class WvaSelectTabReloadTask(@JvmField val wa: WebViewActivity, @JvmField val select: Boolean = false) : Runnable {

    internal inner class TabShownListener : WebViewActivity.TabViewListener {
        override fun onTabView() {
            val webViewActivity = wa
            if (this@WvaSelectTabReloadTask.select) {
                webViewActivity.refreshTabBar(1, false)
            } else {
                webViewActivity.refreshTabBar(2, false)
            }
            webViewActivity.refreshAfterPageChange()
        }
    }

    override fun run() {
        val tabShownListener: TabShownListener = TabShownListener()
        val webViewActivity = wa
        webViewActivity.reloadTabViews(tabShownListener)
        webViewActivity.findState.selectTabBusy = false
    }
}

class WvaRebuildTabsTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.pageState.rebuildTabsBusy = false
    }
}

class WvaApplyTabLimitTask(@JvmField val wa: WebViewActivity, @JvmField val closedTabs: ArrayList<Any?>? = null, @JvmField val reparentedTabs: ArrayList<Any?>? = null) : Runnable {

    internal inner class SaveClosedTabsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val arrayList = this@WvaApplyTabLimitTask.closedTabs
            val reparentedTabs = this@WvaApplyTabLimitTask.reparentedTabs
            if ((arrayList != null && !arrayList.isEmpty()) || (reparentedTabs != null && !reparentedTabs.isEmpty())) {
                DbBookTab.updateParents(webViewActivity.dialogsState.appContext, (reparentedTabs as ArrayList<WebTabItem?>?))
                DbBookTab.deleteTabs(webViewActivity.dialogsState.appContext, (arrayList as ArrayList<WebTabItem?>?))
            }
            webViewActivity.clampTabIndex()
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaTabLimitDone(webViewActivity))
        }
    }

    override fun run() {
        wa.runInBackground(SaveClosedTabsStep())
    }
}

class WvaTabLimitDone(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val tabLimitListener = webViewActivity.miscState.tabLimitListener
        webViewActivity.miscState.tabLimitListener = null
        if (tabLimitListener != null) {
            tabLimitListener.onLimitApplied(true)
        }
        webViewActivity.dialogsState.applyTabLimitBusy = false
    }
}

class WvaReloadTabViewsTask(@JvmField val wa: WebViewActivity, @JvmField val tabItems: ArrayList<Any?>? = null) : Runnable {

    internal inner class UpdateParentsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val arrayList = this@WvaReloadTabViewsTask.tabItems
            DbBookTab.updateParents(webViewActivity.dialogsState.appContext, (arrayList as ArrayList<WebTabItem?>?))
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaReloadAttachTask(webViewActivity))
        }
    }

    override fun run() {
        wa.runInBackground(UpdateParentsStep())
    }
}

class WvaMainInsetListener(@JvmField val wa: WebViewActivity) : MainInsetListener {
    override fun onInsetChanged(bottom: Int) {
        val webViewActivity = wa
        val runnable = webViewActivity.insetTask
        val keyboardOpen = bottom > 0
        if (keyboardOpen) {
            if (webViewActivity.isAnyDialogOpen()) {
                return
            }
        } else if (!webViewActivity.keyboardOpen) {
            return
        }
        webViewActivity.translateState.keyboardHeight = bottom
        webViewActivity.keyboardOpen = keyboardOpen
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.removeCallbacks(runnable)
        webViewActivity.mainHandler!!.post(runnable)
    }

    override fun onImeHidden() {
        val webViewActivity = wa
        if (webViewActivity.quickState.editingAddress || webViewActivity.keyboardOpen) {
            webViewActivity.updateKeyboardState()
            webViewActivity.layoutBars()
            webViewActivity.applyFindBarMargin()
        }
    }
}

class WvaSwipeToPrevTab(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class SwipePrevStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.animateTabSwipe(4, true)
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.dragPrevTab(0.5f, 4)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(SwipePrevStep())
    }
}

class WvaSwipeToNextTab(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class SwipeNextStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.animateTabSwipe(4, false)
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.dragNextTab(webViewActivity.pageAreaWidth() - 0.5f, false)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(SwipeNextStep())
    }
}

class WvaFinishPageResumeTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val prevPageListener = webViewActivity.miscState.prevPageListener
        webViewActivity.miscState.prevPageListener = null
        webViewActivity.resumeCurrentPage()
        webViewActivity.refreshAfterPageChange()
        if (prevPageListener != null) {
            prevPageListener.onDone()
        }
        webViewActivity.tabsState.goBackPageBusy = false
    }
}

class WvaCloseTabReloadViewsTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.reloadTabViews(TabRemovedBackListener(webViewActivity))
        webViewActivity.tabsState.goBackPageBusy = false
    }
}

class WvaApplySystemBarsAfterExpand(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.applySystemBars()
    }
}

class WvaApplySystemBarsModeTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class WatchWindowHeightStep : Runnable {

        internal inner class WatchHeightStep : Runnable {
            override fun run() {
                val wvaApplySystemBarsModeTask = this@WvaApplySystemBarsModeTask
                val webViewActivity = wa
                webViewActivity.watchWindowHeight()
                wa.dialogsState.applySystemBarsBusy = false
            }
        }

        override fun run() {
            val webViewActivity = wa
            toggleSystemBars(
                webViewActivity.getWindow(),
                webViewActivity.decorViewCached(),
                webViewActivity.insetsControllerCompat(),
                webViewActivity.dialogsState.showStatusBar,
                webViewActivity.videoState.showNavBar
            )
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WatchHeightStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        applySystemBarsMode(
            webViewActivity.getWindow(),
            webViewActivity.decorViewCached(),
            webViewActivity.insetsControllerCompat(),
            webViewActivity.dialogsState.showStatusBar,
            webViewActivity.videoState.showNavBar
        )
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WatchWindowHeightStep())
    }
}

class WvaUpdateSystemBarPadding(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val showStatusBar = BrowserSettings.showStatus.value
        val showNavBar = BrowserSettings.showNavi.value
        wa.updateSystemBarPadding(showStatusBar, showNavBar)
    }
}

class WvaPlaceDownIconsTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.needsDownIcon()) {
            webViewActivity.placeDownIcons()
        }
    }
}

class WvaVideoMenuListener(@JvmField val wa: WebViewActivity, @JvmField val isLong: Boolean = false) : VideoMenuListener {
    override fun onClose() {
        val webViewActivity = wa
        webViewActivity.downloadsState.videoMenuBusy = false
        webViewActivity.closeVideoMenuDialog()
    }

    override fun onAction(action: Int) {
        val webViewActivity = wa
        webViewActivity.closeVideoMenuDialog()
        if (this@WvaVideoMenuListener.isLong) {
            webViewActivity.runVideoAction(action)
        } else {
            webViewActivity.runDownAction(action)
        }
    }
}

class WvaVkvListListener(@JvmField val wa: WebViewActivity) : VkvListListener {
    override fun onList(arrayList: ArrayList<com.mycompany.app.main.MainDownAdapter.DownListItem?>?) {
        wa.dialogsState.vkvListListenerList = arrayList
    }
}

class WvaRefreshPageStateTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ApplySnsFixesStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.applySnsFixes()
        }
    }

    override fun run() {
        wa.runInBackground(ApplySnsFixesStep())
    }
}

class WvaRunVideoActionListener(@JvmField val wa: WebViewActivity) : DialogApplyListener {

    internal inner class RetryVideoActionStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val runnable = webViewActivity.videoActionRetry
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.removeCallbacks(runnable)
            if (webViewActivity.miscState.touchBusy || webViewActivity.isBarUpdatePending()) {
                webViewActivity.webCoord!!.postDelayed(runnable, 800L)
            } else {
                webViewActivity.webCoord!!.post(runnable)
            }
        }
    }

    override fun onApply() {
        val myWebCoord = wa.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.postDelayed(RetryVideoActionStep(), 500L)
    }
}

class PageDownloadListener(@JvmField val wa: WebViewActivity) : DownloadListener {
    override fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        wa.onPageDownload(url, contentDisposition, mimeType, contentLength, true)
    }
}

class WvaWebAnimListener(@JvmField val wa: WebViewActivity) : WebAnimListener {
    override fun onAnimEnd(direction: Int, forward: Boolean) {
        wa.finishPageChange(direction, forward, null)
    }
}

class WvaWebBusyListener(@JvmField val wa: WebViewActivity) : WebBusyListener {
    override fun isBusy(): Boolean {
        val webViewActivity = wa
        if (webViewActivity.miscState.touchBusy || webViewActivity.keyboardOpen || webViewActivity.keyboardHiding) {
            return true
        }
        return webViewActivity.isBarUpdatePending()
    }
}

class WvaSyncJsBridgeTask(@JvmField val wa: WebViewActivity, @JvmField val view: android.webkit.WebView? = null, @JvmField val url: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webView = this@WvaSyncJsBridgeTask.view
        val url = this@WvaSyncJsBridgeTask.url
        if (webView == null) {
            return
        }
        if (TextUtils.isEmpty(url) || !url!!.startsWith("http://")) {
            webView.addJavascriptInterface(WvaWebAppInterface(webViewActivity), "android")
        } else {
            webView.removeJavascriptInterface("android")
        }
    }
}

class WvaPauseSecureFlag(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.finishActionMode()
        if (!webViewActivity.isPaused || webViewActivity.isClosing) {
            return
        }
        webViewActivity.updateSecureFlag(true)
    }
}

class WvaLoadFilterList(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class FilterServiceDone : SetDeskListener {
        override fun onChanged(deskMode: Boolean) {
            val webViewActivity = wa
            val mainFilterSvc = webViewActivity.miscState.filterService
            if (mainFilterSvc != null) {
                mainFilterSvc.release()
                webViewActivity.miscState.filterService = null
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.webBody != null) {
            val context = webViewActivity.dialogsState.appContext
            val dbBookFilter = DbBookFilter.cached
            var found = false
            if (context != null) {
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        DbBookFilter.get(context).getWritableDatabase(),
                        "DbBookFilter_table",
                        arrayOf<String?>("_path"),
                        "_use=?",
                        arrayOf<String?>("1"),
                        null
                    )
                    if (cursorG != null && cursorG.moveToFirst()) {
                        val columnIndex = cursorG.getColumnIndex("_path")
                        do {
                            if (!TextUtils.isEmpty(cursorG.getString(columnIndex))) {
                                found = true
                                break
                            }
                        } while (cursorG.moveToNext())
                    }
                } catch (unused: Exception) {
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            if (found) {
                webViewActivity.miscState.filterService =
                    MainFilterSvc(webViewActivity.dialogsState.appContext, FilterServiceDone())
            }
        }
    }
}

class WvaLoadScriptUrls(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ScriptServiceDone : SetDeskListener {
        override fun onChanged(deskMode: Boolean) {
            val webViewActivity = wa
            val mainScriptSvc = webViewActivity.miscState.scriptService
            if (mainScriptSvc != null) {
                mainScriptSvc.release()
                webViewActivity.miscState.scriptService = null
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.webBody != null) {
            val context = webViewActivity.dialogsState.appContext
            val dbBookScript = DbBookScript.cached
            var found = false
            if (context != null) {
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        DbBookScript.get(context).getWritableDatabase(),
                        "DbBookScript_table",
                        arrayOf<String?>("_downurl"),
                        "_enabled=?",
                        arrayOf<String?>("1"),
                        null
                    )
                    if (cursorG != null && cursorG.moveToFirst()) {
                        val columnIndex = cursorG.getColumnIndex("_downurl")
                        do {
                            if (!TextUtils.isEmpty(cursorG.getString(columnIndex))) {
                                found = true
                                break
                            }
                        } while (cursorG.moveToNext())
                    }
                } catch (unused: Exception) {
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            if (found) {
                webViewActivity.miscState.scriptService =
                    MainScriptSvc(webViewActivity.dialogsState.appContext, ScriptServiceDone())
            }
        }
    }
}

class WvaResumeScriptCacheTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.loadScriptCache()
        if (webViewActivity.dialogsState.webBody == null || webViewActivity.miscState.scriptService != null || webViewActivity.miscState.scriptCacheTime == 0L || !PdfSettings.userScript.value || PdfSettings.scriptTime.value == 0L || PdfSettings.scriptDay.value == 0) {
            return
        }
        val now = System.currentTimeMillis()
        if (now >= ((PdfSettings.scriptDay.value.toLong()) * 86400000) + PdfSettings.scriptTime.value) {
            PdfSettings.editLocal { set(PdfSettings.scriptTime, now) }
            val prefPdfR = PrefPdf.get(webViewActivity.dialogsState.appContext, false)
            prefPdfR!!.putLong(PdfSettings.scriptTime.value, "mScriptTime")
            prefPdfR.saveAsync()
            webViewActivity.runInBackground(WvaLoadScriptUrls(webViewActivity))
        }
    }
}

class WvaEditUrlListener(@JvmField val wa: WebViewActivity) : EditUrlListener {

    internal inner class LoadScriptCacheStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.loadScriptCache()
        }
    }

    override fun onSaved(id: Long, path: String?, extra: String?) {
        wa.runInBackground(LoadScriptCacheStep())
    }
}

class WvaAddressSubmit(@JvmField val wa: WebViewActivity, @JvmField val input: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val input = this@WvaAddressSubmit.input
        var isScript = false
        val trimmedInput: String? = if (input != null) input.trim { it <= ' ' } else null
        if (input != null && trimmedInput != null && trimmedInput.length >= 12) {
            isScript = trimmedInput.startsWith("javascript:")
        }
        if (isScript) {
            webViewActivity.runInBackground(WvaRunJavascriptUrl(webViewActivity, bookListListenerText = input))
            return
        }
        val normalizedUrl = normalizeUrl(webViewActivity.dialogsState.appContext, input)
        if (hasZtwoFlag(4)) {
            webViewActivity.addTabPlain(null, normalizedUrl, true, null)
        } else {
            webViewActivity.openInCurrentPage(normalizedUrl, null)
        }
    }
}

class WvaTextZoomNudge2(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.refreshPageState()
    }
}

class WvaFocusResumeDownloads(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.focusPending = false
        val mainAppO: MainApp? = if (webViewActivity.hasWindowFocusNow) null else MainApp.from(webViewActivity.getApplicationContext())
        val mainDownSvc: MainDownSvc? = if (webViewActivity.hasWindowFocusNow || mainAppO == null || !mainAppO.downBinding) null else mainAppO.downService
        if (webViewActivity.hasWindowFocusNow || mainAppO == null || !mainAppO.downBinding || mainDownSvc == null) {
            return
        }
        mainDownSvc.flushFinished(false)
    }
}

class WvaRefreshFileUrlTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.updateFileUrl(webViewActivity.dialogsState.pageUrl)
        webViewActivity.dialogsState.lastLoadUrl = null
    }
}

class WvaWebStyleListener(@JvmField val wa: WebViewActivity) : WebStyleListener {
    override fun onStyleApplied() {
        // Style applied: refresh, and on the known comic sites start the image-list helper.
        val act = wa
        act.checkPageLanguage(true)
        val host = act.dialogsState.pageHost
        if (TextUtils.isEmpty(host)) {
            return
        }
        if (host!!.startsWith("manatoki")) {
            if (host.endsWith(".net")) {
                act.miscState.webStyleListenerBusy = false
                act.runInBackground(WvaComicDetectScript(act))
            }
        } else if (host.startsWith("newtoki")) {
            if (host.endsWith(".com")) {
                act.miscState.webStyleListenerBusy = false
                act.runInBackground(WvaComicDetectScript(act))
            }
        } else if (host.endsWith("hitomi.la") && MainUtil.isHmgUrl(act.dialogsState.pageUrl)) {
            act.miscState.webStyleListenerBusy = true
            act.runInBackground(WvaComicDetectScript(act))
        }
    }
}

class WvaRetryVideoRateTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val rateRetryPending = webViewActivity.videoState.rateRetryPending
        val runnable = webViewActivity.videoRateRetry
        if (!webViewActivity.checkVideoRate()) {
            webViewActivity.miscState.rateScriptUrl = null
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.removeCallbacks(runnable)
                if (webViewActivity.miscState.touchBusy || webViewActivity.isBarUpdatePending()) {
                    webViewActivity.webCoord!!.postDelayed(runnable, 800L)
                } else {
                    webViewActivity.webCoord!!.post(runnable)
                }
            }
        }
        if (rateRetryPending) {
            webViewActivity.resumeWebDestroy()
        }
    }
}

class WvaVideoRateInject(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ClearRateInjectBusyStep : Runnable {
        override fun run() {
            wa.miscState.videoRateInjectBusy = false
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.miscState.videoRateInjectBusy) {
            return
        }
        webViewActivity.miscState.videoRateInjectBusy = true
        webViewActivity.injectVideoRateScript(true, (ZtwoSettings.youSpeedRate.value).compareTo(1.0f) != 0)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ClearRateInjectBusyStep())
    }
}

class WvaVideoRateInjectForce(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ClearForcedRateInjectBusyStep : Runnable {
        override fun run() {
            wa.miscState.videoRateInjectBusy = false
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.miscState.videoRateInjectBusy) {
            return
        }
        webViewActivity.miscState.videoRateInjectBusy = true
        webViewActivity.injectVideoRateScript(false, true)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ClearForcedRateInjectBusyStep())
    }
}

class WvaMenuReopenRelease(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.menuJustClosed = false
    }
}

class WvaApplyPageBarColor(@JvmField val wa: WebViewActivity, @JvmField val barColorSource: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.applyPageBarColor(webViewActivity.dialogsState.pageBarColor, this@WvaApplyPageBarColor.barColorSource)
    }
}

class WvaAddressEditUnlock(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.pageState.addressEditBusy = false
    }
}

class WvaHideKeyboardTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.keyboardHiding = false
    }
}

class WvaHoldQuickMenuReopenTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.quickMenuJustClosed = false
    }
}

class WvaUpdateProgress(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.updatePageProgress(webViewActivity.dialogsState.pageProgress)
    }
}

class WvaApplyPageColors(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.updateWindowBgColor(webViewActivity.pageThemeColor(), webViewActivity.pageThemeLight())
    }
}

class FacebookVideoScan(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.findPageVideos("face_dummy", null, null, null)
    }
}

class InstagramVideoScan(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.findPageVideos("insta_dummy", null, null, null)
    }
}

class TwitterVideoScan(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.findPageVideos("twit_dummy", null, null, null)
    }
}

class VkVideoScan(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.findPageVideos("vkv_dummy", null, null, null)
    }
}

class WvaUrlOverrideTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val url: String?
) : Runnable {
    override fun run() {
        val webViewActivity = this.webViewActivity
        webViewActivity.javaClass
        val url = this.url
        if (TextUtils.isEmpty(url)) {
            return
        }
        webViewActivity.finishProgress()
        if (url!!.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:")) {
            val opened = openIntentUri(webViewActivity, url)
            webViewActivity.dialogsState.leavingByIntent = opened
            if (opened && webViewActivity.isFreshPage()) {
                webViewActivity.miscState.overrideUrl = url
                return
            }
            return
        }
        if (url.startsWith("intent://search.app.goo.gl") && url.contains("googleapp://lens")) {
            val opened = openIntentUri(webViewActivity, url)
            webViewActivity.dialogsState.leavingByIntent = opened
            if (opened) {
                if (webViewActivity.isFreshPage()) {
                    webViewActivity.miscState.overrideUrl = url
                    return
                }
                return
            }
        }
        if (url.startsWith("cid:frame-") && url.endsWith("@mhtml.blink")) {
            return
        }
        var fallbackUrl = if (url.contains("yahoo.co.jp")) intentToUrl(url, true) else null
        if (PdfSettings.appBlock.value == 1 && TextUtils.isEmpty(fallbackUrl)) {
            fallbackUrl = intentToUrl(url, false)
        }
        if (!TextUtils.isEmpty(fallbackUrl)) {
            if (webViewActivity.isFreshPage()) {
                webViewActivity.goBackPage(WvaUrlOverrideNewTabTask(webViewActivity, fallbackUrl!!))
                return
            } else {
                webViewActivity.addTabInGroup(null, fallbackUrl, true, null)
                return
            }
        }
        if (PdfSettings.appBlock.value != 0) {
            if (webViewActivity.isFreshPage()) {
                webViewActivity.miscState.overrideUrl = url
            }
            webViewActivity.showAppBlockedSnackbar(url, intentToUrl(url, false))
            return
        }
        val opened = openIntentUri(webViewActivity, url)
        webViewActivity.dialogsState.leavingByIntent = opened
        if (opened) {
            if (webViewActivity.isFreshPage()) {
                webViewActivity.miscState.overrideUrl = url
            }
        } else if (webViewActivity.isFreshPage()) {
            webViewActivity.goBackPage(WvaUrlOverrideNewTabTask(webViewActivity, url))
        } else {
            webViewActivity.addTabInGroup(null, url, true, null)
        }
    }
}

class WvaPopupBlockedOnBack(@JvmField val outer: WebViewActivity,
                                       @JvmField val url: String?
) : WebViewActivity.PrevPageListener {
    override fun onDone() {
        this.outer.showPopupBlockedSnackbar(this.url, null)
    }
}

class WvaUrlOverrideNewTabTask(@JvmField val outer: WebViewActivity,
                                       @JvmField val url: String
) : WebViewActivity.PrevPageListener {
    override fun onDone() {
        this.outer.addTabInGroup(null, this.url, true, null)
    }
}

class PopupDownloadListener(@JvmField val wa: WebViewActivity) : DownloadListener {
    override fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        wa.onPageDownload(url, contentDisposition, mimeType, contentLength, false)
    }
}

class WvaOnPopupPageResultTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val popupResultUrl = webViewActivity.miscState.popupResultUrl
        webViewActivity.miscState.popupResultUrl = null
        webViewActivity.destroyPopupPage()
        if (PdfSettings.appBlock.value != 0) {
            webViewActivity.showAppBlockedSnackbar(popupResultUrl, null)
            return
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaPopupOpenIntentTask(webViewActivity, url = popupResultUrl))
    }
}

class WvaPopupInGroupTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val popupResultUrl = webViewActivity.miscState.popupResultUrl
        webViewActivity.miscState.popupResultUrl = null
        webViewActivity.destroyPopupPage()
        val appBlock = PdfSettings.appBlock.value
        if (appBlock == 2) {
            webViewActivity.showAppBlockedSnackbar(popupResultUrl, popupResultUrl)
            return
        }
        if (appBlock == 1) {
            webViewActivity.addTabInGroup(null, popupResultUrl, true, null)
            return
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaPopupExternalAppTask(webViewActivity, url = popupResultUrl))
    }
}

class WvaPopupOpenTask(@JvmField val wa: WebViewActivity, @JvmField val kind: Int = 0, @JvmField val popupLoaded: Boolean = false, @JvmField val url: String? = null, @JvmField val referer: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaPopupOpenTask.url
        val referer = this@WvaPopupOpenTask.referer
        val kind = this@WvaPopupOpenTask.kind
        val popupLoaded = this@WvaPopupOpenTask.popupLoaded
        if (kind == 3) {
            if ((ZtwoSettings.tabOpen.value and 2) == 2) {
                webViewActivity.addTabInGroup(null, url, true, WvaTabAddFailed(webViewActivity))
                return
            } else {
                webViewActivity.openInNextPage(url, webViewActivity.dialogsState.pageUrl, false)
                return
            }
        }
        if (!popupLoaded) {
            webViewActivity.finishProgress()
            webViewActivity.addTabInGroup(null, url, true, WvaTabAddFailed(webViewActivity))
        } else {
            if (TextUtils.isEmpty(url)) {
                webViewActivity.destroyPendingPage()
                return
            }
            webViewActivity.runInBackground(WvaPopupUrlTask(webViewActivity, url = url, referer = referer))
        }
    }
}

class WvaTabAddFailed(@JvmField val wa: WebViewActivity) : WebViewActivity.TabAddListener {
    override fun onTabAdded(isAdded: Boolean) {
        if (isAdded) {
            return
        }
        wa.destroyPendingPage()
    }
}

class WvaTabAddFailedReset(@JvmField val wa: WebViewActivity) : WebViewActivity.TabAddListener {
    override fun onTabAdded(isAdded: Boolean) {
        val webViewActivity = wa
        webViewActivity.videoState.pendingTabUrl = null
        if (isAdded) {
            return
        }
        webViewActivity.destroyPendingPage()
    }
}

class WvaPopupOpenIntentTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaPopupOpenIntentTask.url
        webViewActivity.dialogsState.leavingByIntent = openIntentUri(webViewActivity, url)
    }
}

class WvaCreateViews(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class CreateViewsStep : Runnable {

        internal inner class SetUpEditorBarStep : Runnable {

            internal inner class ApplySystemBarsStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    val showStatus = BrowserSettings.showStatus.value
                    val showNavi = BrowserSettings.showNavi.value
                    webViewActivity.setSystemBarsHidden(showStatus, showNavi, false)
                    val setupLock = wa.setupLock
                    if (setupLock == null) {
                        return
                    }
                    synchronized(setupLock) {
                        val activity = wa
                        activity.dialogsState.viewsCreated = true
                        if (activity.dialogsState.setupDone) {
                            activity.startPageSetup()
                        }
                    }
                }
            }

            override fun run() {
                val wvaCreateViews = this@WvaCreateViews
                val webViewActivity = wa
                webViewActivity.setUpEditorBar()
                val handler = wa.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ApplySystemBarsStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.createEditorBar()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(SetUpEditorBarStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.addQuickSearch()
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(CreateViewsStep())
    }
}

class WvaPopupExternalAppTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null) : Runnable {

    internal inner class OpenPopupTabStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val popupTabUrl = webViewActivity.miscState.popupTabUrl
            webViewActivity.miscState.popupTabUrl = null
            webViewActivity.addTabInGroup(null, popupTabUrl, true, null)
        }
    }

    override fun run() {
        val webViewActivity = wa
        val url = this@WvaPopupExternalAppTask.url
        val openedExternal = openInExternalApp(webViewActivity, url)
        webViewActivity.dialogsState.leavingByIntent = openedExternal
        if (openedExternal) {
            return
        }
        webViewActivity.miscState.popupTabUrl = url
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(OpenPopupTabStep())
    }
}

class WvaOnNewIntentTask(@JvmField val wa: WebViewActivity, @JvmField val intentUrl: String? = null) : Runnable {

    internal inner class HandleIntentStep(
                                         @JvmField val checked: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            val intentUrl = this@WvaOnNewIntentTask.intentUrl
            if (this.checked) {
                webViewActivity.showPopupBlockedSnackbar(intentUrl, null)
                return
            }
            webViewActivity.addTabFromIntent = true
            webViewActivity.addTabInGroup(null, intentUrl, false, null)
            webViewActivity.addTabFromIntent = false
        }
    }

    override fun run() {
        val webViewActivity = wa
        // filter engine: the lists' $popup rules, blocked addresses, ad-like third-party targets
        val popupBlocked = com.mycompany.app.web.filter.UboFilter.checkPopup(this@WvaOnNewIntentTask.intentUrl, webViewActivity.dialogsState.pageUrl) == true
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(HandleIntentStep(popupBlocked))
    }
}

class WvaAddressMenuListener(@JvmField val wa: WebViewActivity) : MyPopupListener {
    override fun onClose() {
        wa.closeAddressMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        wa.onAddressMenuItem(id)
        return true
    }
}

class WvaEngineMenuListener(@JvmField val wa: WebViewActivity) : MyEngineListener {
    override fun onClose() {
        wa.closeEngineMenu()
    }

    override fun onPick(engine: Int, color: Int, url: String?) {
        val webViewActivity = wa
        if (webViewActivity.engineIcon == null || ZtwoSettings.searchType.value == engine) {
            return
        }
        ZtwoSettings.editLocal { set(ZtwoSettings.searchType, engine) }
        ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, url) }
        ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, color) }
        PrefZtwo.saveSearch(webViewActivity.dialogsState.appContext)
        webViewActivity.engineIcon!!.updateEngineIcon()
    }
}

class WvaEngineMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.engineMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class TabMenuDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeTabMenuDialog()
    }
}

class WvaUpdateFileUrlTask(@JvmField val wa: WebViewActivity, @JvmField val addressChanged: Boolean = false, @JvmField val tintChanged: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val addressChanged = this@WvaUpdateFileUrlTask.addressChanged
        val tintChanged = this@WvaUpdateFileUrlTask.tintChanged
        if (addressChanged) {
            webViewActivity.setBarAddress(webViewActivity.barAddressText())
        }
        if (tintChanged) {
            webViewActivity.tintBars(webViewActivity.dialogsState.barsTinted)
        }
    }
}

class WvaLongPressFollowUpTask(@JvmField val wa: WebViewActivity, @JvmField val linkFlag: Boolean = false, @JvmField val fromFollowUp: Boolean = false, @JvmField val linkUrl: String? = null, @JvmField val imageUrl: String? = null, @JvmField val title: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val linkUrl = this@WvaLongPressFollowUpTask.linkUrl
        val imageUrl = this@WvaLongPressFollowUpTask.imageUrl
        val linkFlag = this@WvaLongPressFollowUpTask.linkFlag
        val title = this@WvaLongPressFollowUpTask.title
        val fromFollowUp = this@WvaLongPressFollowUpTask.fromFollowUp
        webViewActivity.showUrlLinkDialog(linkUrl, imageUrl, linkFlag, title, fromFollowUp)
        webViewActivity.videoState.longPressFollowUpBusy = false
    }
}

class UrlLinkDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeUrlLinkDialog()
        webViewActivity.quickState.longPressVideo = null
    }
}

class DownGuideDialogListener(@JvmField val wa: WebViewActivity) : DialogConfListener {
    override fun onResult(confirmed: Boolean) {
        val webViewActivity = wa
        if (confirmed) {
            ReadSettings.edit(webViewActivity.dialogsState.appContext) { set(ReadSettings.guideDown, false) }
        }
        webViewActivity.closeDownGuideDialog()
    }
}

class DownGuideDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeDownGuideDialog()
        webViewActivity.runDownAction(0)
    }
}

class WvaOnPageDownloadListener(@JvmField val wa: WebViewActivity) : WebViewActivity.PrevPageListener {
    override fun onDone() {
        val webViewActivity = wa
        val pendingDownloadUrl = webViewActivity.miscState.pendingDownloadUrl
        val pendingDownloadDisposition = webViewActivity.miscState.pendingDownloadDisposition
        val pendingDownloadMime = webViewActivity.miscState.pendingDownloadMime
        val pendingDownloadLength = webViewActivity.miscState.pendingDownloadLength
        val pendingDownloadUseCurrent = webViewActivity.miscState.pendingDownloadUseCurrent
        webViewActivity.miscState.pendingDownloadUrl = pendingDownloadUrl
        webViewActivity.miscState.pendingDownloadDisposition = pendingDownloadDisposition
        webViewActivity.miscState.pendingDownloadMime = pendingDownloadMime
        webViewActivity.miscState.pendingDownloadLength = pendingDownloadLength
        webViewActivity.miscState.pendingDownloadUseCurrent = pendingDownloadUseCurrent
        webViewActivity.startUrlDownload(pendingDownloadUrl, pendingDownloadDisposition, pendingDownloadMime, pendingDownloadLength)
    }
}

class WvaShowDownloadDialogTask(@JvmField val wa: WebViewActivity, @JvmField val tabsOpenDownloadDialogTaskText: String? = null, @JvmField val url: String? = null, @JvmField val miscOpenDownloadDialogTaskText: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaShowDownloadDialogTask.url
        val tabsOpenDownloadDialogTaskText = this@WvaShowDownloadDialogTask.tabsOpenDownloadDialogTaskText
        val miscOpenDownloadDialogTaskText = this@WvaShowDownloadDialogTask.miscOpenDownloadDialogTaskText
        webViewActivity.dialogsState.leavingByIntent = MainUtil.openWithChooser(
            webViewActivity,
            AlbumSettings.dappPkg.value,
            AlbumSettings.dappCls.value,
            url,
            webViewActivity.dialogsState.pageUrl,
            tabsOpenDownloadDialogTaskText,
            miscOpenDownloadDialogTaskText
        )
    }
}

class DownUrlDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeDownUrlDialog()
        webViewActivity.updateImageButton(true)
        webViewActivity.dialogsState.downloadPageUrl = null
        if (ZtwoSettings.quickDown.value) {
            return
        }
        webViewActivity.updateRssButton(true)
    }
}

class WvaSetDownListener(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val name: String? = null, @JvmField val dirName: String? = null) : SetDownListener {
    override fun onPick(label: String?, packageName: String?, className: String?) {
        val webViewActivity = wa
        val url = this@WvaSetDownListener.url
        val name = this@WvaSetDownListener.name
        val dirName = this@WvaSetDownListener.dirName
        webViewActivity.dialogsState.leavingByIntent =
            openWithChooser(webViewActivity, packageName, className, url, webViewActivity.dialogsState.pageUrl, name, dirName)
    }
}

class SetDownDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeSetDownDialog()
    }
}

class DownBlobDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeDownBlobDialog()
    }
}

class CaptureModeSheetDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeCaptureModeSheet()
    }
}

class WvaNoOpClick() : View.OnClickListener {
    override fun onClick(view: View?) {
    }
}

class WvaInitGlide(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.miscState.glide == null) {
            webViewActivity.miscState.glide = GlideApp.with(webViewActivity)
        }
    }
}

class TransMsgDialogCancelled(@JvmField val wa: WebViewActivity) : DialogInterface.OnCancelListener {
    override fun onCancel(dialogInterface: DialogInterface?) {
        wa.closeTransMsgDialog()
    }
}

class TransMsgDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeTransMsgDialog()
    }
}

class WvaStartPageSetupTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val tabBar = BrowserSettings.tabBar.value
        val webViewActivity = wa
        if (tabBar == 0) {
            webViewActivity.createPageViews()
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaSetupCreatePagesTask(webViewActivity))
                return
            }
            return
        }
        webViewActivity.addTabBar(webViewActivity.dialogsState.themeColor, webViewActivity.miscState.themeLight)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaSetupBindTabBar(webViewActivity))
    }
}

class WvaTransCodeResult(@JvmField val wa: WebViewActivity) : ValueCallback<String> {
    override fun onReceiveValue(cookies: String?) {
        val cookieLang = transLangFromCookie(cookies)
        if (TextUtils.isEmpty(cookieLang) || cookieLang == AlbumSettings.transCode.value) {
            return
        }
        AlbumSettings.edit(wa.dialogsState.appContext) { set(AlbumSettings.transCode, cookieLang) }
    }
}

class WvaAddTransFloat(@JvmField val wa: WebViewActivity, @JvmField val updateTransFloatFlag: Boolean = false) : Runnable {
    override fun run() {
        var myWebCoord: MyWebCoord? = null
        val webViewActivity = wa
        val updateTransFloatFlag = this@WvaAddTransFloat.updateTransFloatFlag
        if (if (webViewActivity.isTranslateActive() && webViewActivity.dialogsState.transState.toInt() == 3) webViewActivity.dialogsState.transReady else false) {
            if (webViewActivity.transFloatButton == null && webViewActivity.webCoord != null) {
                val panelWidth = Math.round(dpToPx(webViewActivity.dialogsState.appContext, 130.0f))
                val panelMargin = Math.round(dpToPx(webViewActivity.dialogsState.appContext, 24.0f))
                val anchorGravity = if (ZoneSettings.showUpPos.value == 3) 51 else 53
                // Anchored to the bottom bar, in the Compose overlay stack
                val layoutParams = com.mycompany.app.ui.browser.OverlayParams(panelWidth, panelMargin)
                layoutParams.anchor = com.mycompany.app.ui.browser.OverlayParams.BOTTOM_BAR
                layoutParams.anchorGravity = anchorGravity
                layoutParams.gravity = anchorGravity
                try {
                    val view = View(webViewActivity)
                    webViewActivity.transFloatButton = view
                    if (AppState.darkUi) {
                        view.setBackgroundResource(AppR.drawable.trans_logo_short_back_white)
                    } else {
                        view.setBackgroundResource(AppR.drawable.trans_logo_short_back_color)
                    }
                    wa.floatingState.overlays!!.addCoordView(webViewActivity.transFloatButton, layoutParams)
                } catch (unused: Exception) {
                }
            }
            webViewActivity.updateTransFloatButton(true)
        } else if (!updateTransFloatFlag) {
            webViewActivity.updateTransFloatButton(false)
        } else if (webViewActivity.transFloatButton != null && (webViewActivity.webCoord.also {
                myWebCoord = it
            }) != null) {
            val transMode = AlbumSettings.transMode.value
            if (transMode == 1 || transMode == 2) {
                webViewActivity.updateTransFloatButton(false)
            } else {
                myWebCoord!!.post(WvaAddTransFloat2(webViewActivity))
            }
        }
        val webCoord = webViewActivity.webCoord
        if (webCoord != null) {
            webCoord.post(WvaTransControlRefresh(webViewActivity))
        }
    }
}

class WvaPostTransIconUpdateTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.updateTransIcon(false)
    }
}

class WvaCheckTransTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.updateTransIcon(false)
        if (!sameObject(webViewActivity.transCheckedUrl, webViewActivity.dialogsState.pageUrl)) {
            webViewActivity.closeTransControl()
            webViewActivity.detectSiteFix()
        } else if (webViewActivity.miscState.transMsgPending) {
            webViewActivity.miscState.transMsgPending = false
            webViewActivity.closeTransControl()
            webViewActivity.showTransMsgDialog()
        }
    }
}

class WvaTransFloatListener(@JvmField val wa: WebViewActivity) : FltViewListener {
    override fun onTap(view: View?, button: Int) {
        wa.showTransControl(view)
    }

    override fun onGone() {
        wa.hideMoveHint()
    }

    override fun onLongPress(view: View?) {
        wa.showMoveHint(view)
    }
}

class WvaAddTransFloat2(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val view = webViewActivity.transFloatButton
        if (view != null) {
            webViewActivity.floatingState.overlays?.removeView(view)
            webViewActivity.transFloatButton = null
        }
    }
}

class WvaHideTransControlClick(@JvmField val wa: WebViewActivity) : View.OnClickListener {
    override fun onClick(view: View?) {
        wa.hideTransControl()
    }
}

class SetUrlDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeSetUrlDialog()
    }
}

class PopupMenuDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closePopupMenuDialog()
    }
}

class WvaOpenInOtherAppPost(@JvmField val wa: WebViewActivity, @JvmField val showAppBlockedSnackbarListenerText: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val showAppBlockedSnackbarListenerText = this@WvaOpenInOtherAppPost.showAppBlockedSnackbarListenerText
        val opened = openIntentUri(webViewActivity, showAppBlockedSnackbarListenerText)
        webViewActivity.dialogsState.leavingByIntent = opened
        if (opened) {
            return
        }
        webViewActivity.dialogsState.openInOtherAppPostText = showAppBlockedSnackbarListenerText
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(OpenInOtherAppTask(webViewActivity))
    }
}

class WvaDialogWebListener(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null) : DialogWebListener {
    override fun onAction(action: Int, url: String?, label: String?) {
        val webViewActivity = wa
        webViewActivity.closeWebViewDialog()
        webViewActivity.onUrlLinkAction(action, url, label)
    }

    override fun onClosed() {
        val webViewActivity = wa
        webViewActivity.closeWebViewDialog()
        webViewActivity.showAllowPopupDialog()
    }

    override fun onDownload(url: String?, userAgent: String?, mimeType: String?, size: Long) {
        wa.onPageDownload(url, userAgent, mimeType, size, false)
    }

    override fun onNewWindow(webNestView: WebNestView?, url: String?) {
        val webViewActivity = wa
        webViewActivity.closeWebViewDialog()
        if (webNestView == null) {
            webViewActivity.addTabInGroup(null, url, true, null)
            return
        }
        val isShortcutPage = "file:///android_asset/shortcut.html" == url
        webNestView.startPage = isShortcutPage
        val webNestFrame = WebNestFrame(webViewActivity)
        webNestFrame.setDarkBars(isShortcutPage)
        webNestFrame.addPage(webNestView, 0, 0)
        webViewActivity.addTabInGroup(webNestFrame, url, true, null)
    }

    override fun onShown() {
        val webViewActivity = wa
        webViewActivity.closeWebViewDialog()
        webViewActivity.openLoadDialogFor(this@WvaDialogWebListener.url, true)
    }

    override fun onHidden() {
        val webViewActivity = wa
        webViewActivity.closeWebViewDialog()
        webViewActivity.openEmgGallery(this@WvaDialogWebListener.url, true)
    }
}

class WebViewDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeWebViewDialog()
    }
}

class WvaAdsDialogListener(@JvmField val wa: WebViewActivity) : DialogAdsListener {
    override fun onApply(
        changed: Boolean,
        jsChanged: Boolean,
        listChanged: Boolean,
        userChanged: Boolean,
        refreshPage: Boolean,
        pageUrl: String?
    ) {
        wa.onAdsDialogResult(jsChanged, listChanged, userChanged, refreshPage)
    }
}

class WvaPlaceMoveHint(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webMoveText = webViewActivity.moveHint
        if (webMoveText != null) {
            webMoveText.place(webViewActivity.dialogsState.webBody, webViewActivity.miscState.moveHintX, webViewActivity.miscState.moveHintY)
            webViewActivity.moveHint!!.setVisibility(View.VISIBLE)
        }
        webViewActivity.videoState.moveHintBusy = false
    }
}

class WvaPlaceLinkTitlePill(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webAreaText = webViewActivity.linkTitlePill
        if (webAreaText != null) {
            webAreaText.place(webViewActivity.dialogsState.webBody, webViewActivity.videoState.linkPillX, webViewActivity.videoState.linkPillY)
            webViewActivity.linkTitlePill!!.setVisibility(View.VISIBLE)
        }
        webViewActivity.videoState.linkPillBusy = false
    }
}

class WvaAreaMenuListener(@JvmField val wa: WebViewActivity) : WebAreaMenuListener {
    override fun onClear() {
        val webViewActivity = wa
        webViewActivity.tabsState.touchDownX = webViewActivity.miscState.areaMenuX.toFloat()
        webViewActivity.findState.touchDownY = webViewActivity.miscState.areaMenuY.toFloat()
        webViewActivity.addAreaToolbar(true)
    }

    override fun onSettings() {
        val webViewActivity = wa
        val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.CleanSettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 6)
        intent.putExtra("EXTRA_PATH", checkedUrl)
        webViewActivity.launchExternalForResult(intent, RequestCode.CLEAN_SETTINGS)
    }
}

class WvaPlaceAreaMenu(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webAreaMenu = webViewActivity.videoState.areaMenu
        if (webAreaMenu != null) {
            webAreaMenu.place(webViewActivity.dialogsState.webBody, webViewActivity.miscState.areaMenuX, webViewActivity.miscState.areaMenuY)
            webViewActivity.videoState.areaMenu!!.setVisibility(View.VISIBLE)
        }
        webViewActivity.videoState.areaMenuBusy = false
    }
}

class WvaPlaceAreaToolbar(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val touchX: Int
        val touchY: Int
        val webViewActivity = wa
        val webAreaView = webViewActivity.dialogsState.areaToolbar
        if (webAreaView != null) {
            if (webViewActivity.areaToolbarOn) {
                touchX = webViewActivity.tabsState.touchDownX.toInt()
                touchY = webViewActivity.findState.touchDownY.toInt()
            } else {
                touchX = -1234
                touchY = -1234
            }
            webAreaView.placeIn(webViewActivity.dialogsState.webBody, touchX, touchY)
            webViewActivity.dialogsState.areaToolbar!!.setVisibility(View.VISIBLE)
        }
        webViewActivity.dialogsState.areaSelectBusy = false
        if (webViewActivity.dialogsState.areaToolbar != null) {
            webViewActivity.dialogsState.areaSelectBusy = true
            webViewActivity.runInBackground(WvaStartAreaSelect(webViewActivity))
        }
        webViewActivity.videoState.areaToolbarBusy = false
    }
}

class WvaWebQuickListenerTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.videoState.webQuickListenerBusy = false
    }
}

class QuickEditDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeQuickEditDialog()
    }
}

class WvaShowDeleteTabsDialogTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.dialogsState.showDeleteTabsDialogBusy = false
    }
}

class DeleteItemDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeDeleteItemDialog()
    }
}

class JsMessageDialogApply(@JvmField val wa: WebViewActivity) : DialogApplyListener {
    override fun onApply() {
        val webViewActivity = wa
        val jsResult = webViewActivity.tabsState.jsResult
        if (jsResult != null) {
            jsResult.confirm()
            webViewActivity.tabsState.jsResult = null
        }
        webViewActivity.closeJsMessageDialog()
    }
}

class JsMessageDialogCancelled(@JvmField val wa: WebViewActivity) : DialogInterface.OnCancelListener {
    override fun onCancel(dialogInterface: DialogInterface?) {
        wa.closeJsMessageDialog()
    }
}

class JsMessageDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeJsMessageDialog()
    }
}

class WvaPermissionRequestDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closePermissionDialog()
        webViewActivity.pageState.permissionRequest = null
        webViewActivity.pageState.permissionOrigin = null
        webViewActivity.pageState.geoCallback = null
    }
}

class WvaSavePermissionTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        var args: Array<String?>? = null
        var writableDatabase: SQLiteDatabase? = null
        val webViewActivity = wa
        val permissionHost = webViewActivity.pageState.permissionHost
        val sitePermissionAllow = webViewActivity.pageState.sitePermissionAllow
        val permissionKind = webViewActivity.pageState.permissionKind
        webViewActivity.pageState.permissionHost = null
        // "Allow this time" in DialogPermission: nothing is remembered for the site
        if (webViewActivity.pageState.permissionOnce) {
            webViewActivity.pageState.permissionOnce = false
            return
        }
        val context = webViewActivity.dialogsState.appContext
        val dbBookPms = DbBookPms.cached
        val hostSaved = if (context == null || TextUtils.isEmpty(permissionHost)) 0 else DbUtil.existsWhere(
                (DbBookPms.get(context).getWritableDatabase().also { writableDatabase = it }),
                "DbBookPms_table",
                null,
                "_path=?",
                (arrayOf<String?>(permissionHost!!).also { args = it })
            )
        if (context == null || TextUtils.isEmpty(permissionHost) || hostSaved == 0) {
            return
        }
        val contentValuesG = android.content.ContentValues().also { cv -> cv.put("_path", permissionHost) }
        contentValuesG.put("_time", System.currentTimeMillis())
        contentValuesG.put("_allow", sitePermissionAllow)
        contentValuesG.put("_block", permissionKind)
        if (hostSaved == 1) {
            updateWhere(writableDatabase, "DbBookPms_table", contentValuesG, "_path=?", args)
        } else {
            insert(writableDatabase, "DbBookPms_table", contentValuesG)
        }
    }
}

class WvaCheckSiteLock(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.checkSiteLock(true)
    }
}

class WvaSetupBindTabBar(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.bindTabBar(webViewActivity.dialogsState.themeColor, webViewActivity.miscState.themeLight)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaSetupCreatePages(webViewActivity))
    }
}

class WvaRestartBrowserTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.restartBrowser()
    }
}

class WvaRestartBundleListener(@JvmField val wa: WebViewActivity) : WebBundleListener {

    internal inner class RestartBrowserStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.restartBrowser()
        }
    }

    override fun onBundle() {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(RestartBrowserStep())
    }
}

class WvaRestartClearAuth(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ClearAuthStep : Runnable {

        internal inner class ClearCookiesStep : Runnable {
            override fun run() {
                val wvaRestartClearAuth = this@WvaRestartClearAuth
                clearCookies(wa.dialogsState.appContext)
                val webViewActivity = wa
                webViewActivity.runInBackground(WvaRestartWithUrl(webViewActivity))
            }
        }

        override fun run() {
            val webViewActivity = wa
            try {
                WebViewDatabase.getInstance(webViewActivity.dialogsState.appContext).clearHttpAuthUsernamePassword()
            } catch (unused: Exception) {
            } catch (unused: NoSuchMethodError) {
            }
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ClearCookiesStep())
        }
    }

    override fun run() {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(ClearAuthStep())
    }
}

class MenuDialogsDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeMenuDialogs()
        if (webViewActivity.mainMenuActionDone) {
            webViewActivity.showMainMenu(webViewActivity.mainMenuAnchor)
        }
    }
}

class WvaTabAddDone(@JvmField val wa: WebViewActivity) : WebViewActivity.TabAddListener {
    override fun onTabAdded(isAdded: Boolean) {
        wa.onTabAdded(isAdded)
    }
}

class MiniTabOverviewDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeMiniTabOverview()
        val miniMode = ZoneSettings.tabMiniMode.value
        val fastList = miniMode && ZoneSettings.tabListType.value == 0
        if (miniMode == webViewActivity.tabOverviewAnim && fastList == webViewActivity.tabOverviewFast) {
            return
        }
        if (miniMode) {
            webViewActivity.showMiniTabOverview()
        } else {
            webViewActivity.showTabOverview()
        }
    }
}

class TabBarRefresh(@JvmField val wa: WebViewActivity) : WebViewActivity.TabViewListener {
    override fun onTabView() {
        wa.refreshTabBar(2, true)
    }
}

class LoadImgDialogListener(@JvmField val wa: WebViewActivity) : LoadImgListener {
    override fun openNewUrl(url: String?) {
        val webViewActivity = wa
        webViewActivity.removeLoadView()
        webViewActivity.closeLoadImgDialog()
        webViewActivity.addTabInGroup(null, url, true, null)
    }

    override fun onImages(arrayList: ArrayList<String?>?, title: String?): Boolean {
        return false
    }

    override fun onClose(isDone: Boolean) {
        val webViewActivity = wa
        webViewActivity.removeLoadView()
        webViewActivity.closeLoadImgDialog()
        webViewActivity.miscState.dataCachesDirty = true
    }
}

class LoadImgDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.loadDialogKind = 0.toByte()
        webViewActivity.removeLoadView()
        webViewActivity.closeLoadImgDialog()
    }
}

class WvaLoadEmgListener(@JvmField val wa: WebViewActivity) : LoadImgListener {
    override fun openNewUrl(url: String?) {
        val webViewActivity = wa
        val webEmgLoad = webViewActivity.emgLoad
        if (webEmgLoad != null) {
            webEmgLoad.release()
            webViewActivity.emgLoad = null
        }
        webViewActivity.closeLoadEmgDialog()
        webViewActivity.addTabInGroup(null, url, true, null)
    }

    override fun onImages(arrayList: ArrayList<String?>?, title: String?): Boolean {
        return false
    }

    override fun onClose(isDone: Boolean) {
        val webViewActivity = wa
        val webEmgLoad = webViewActivity.emgLoad
        if (webEmgLoad != null) {
            webEmgLoad.release()
            webViewActivity.emgLoad = null
        }
        webViewActivity.closeLoadEmgDialog()
        webViewActivity.miscState.dataCachesDirty = true
    }
}

class WvaEmgLoadDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.loadDialogKind = 0.toByte()
        val webEmgLoad = webViewActivity.emgLoad
        if (webEmgLoad != null) {
            webEmgLoad.release()
            webViewActivity.emgLoad = null
        }
        webViewActivity.closeLoadEmgDialog()
    }
}

class WvaLoadHmgListener(@JvmField val wa: WebViewActivity) : LoadImgListener {
    override fun openNewUrl(url: String?) {
        val webViewActivity = wa
        val webHmgLoad = webViewActivity.miscState.hmgLoad
        if (webHmgLoad != null) {
            webHmgLoad.release()
            webViewActivity.miscState.hmgLoad = null
        }
        webViewActivity.closeLoadHmgDialog()
        webViewActivity.addTabInGroup(null, url, true, null)
    }

    override fun onImages(arrayList: ArrayList<String?>?, title: String?): Boolean {
        return false
    }

    override fun onClose(isDone: Boolean) {
        val webViewActivity = wa
        val webHmgLoad = webViewActivity.miscState.hmgLoad
        if (webHmgLoad != null) {
            webHmgLoad.release()
            webViewActivity.miscState.hmgLoad = null
        }
        webViewActivity.closeLoadHmgDialog()
        webViewActivity.miscState.dataCachesDirty = true
    }
}

class WvaHmgLoadDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.loadDialogKind = 0.toByte()
        val webHmgLoad = webViewActivity.miscState.hmgLoad
        if (webHmgLoad != null) {
            webHmgLoad.release()
            webViewActivity.miscState.hmgLoad = null
        }
        webViewActivity.closeLoadHmgDialog()
    }
}

class WvaOpenEmgGalleryDismiss() : WebImgListener

class WvaGallerySiteImageListener() : WebImgListener

class VideoListDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {

    internal inner class UpdateImageButtonStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.updateImageButton(true)
        }
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeVideoListDialog()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(UpdateImageButtonStep())
    }
}

class WvaOpenMediaTask(@JvmField val wa: WebViewActivity, @JvmField val videoListListenerFlag: Boolean = false, @JvmField val showDownBlobDialogValue: Int = 0, @JvmField val showDownBlobDialogText: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val showDownBlobDialogText = this@WvaOpenMediaTask.showDownBlobDialogText
        val videoListListenerFlag = this@WvaOpenMediaTask.videoListListenerFlag
        val showDownBlobDialogValue = this@WvaOpenMediaTask.showDownBlobDialogValue
        if (showDownBlobDialogValue == 0) {
            webViewActivity.openMediaUrl(showDownBlobDialogText, null)
        } else {
            webViewActivity.playVideoUrl(showDownBlobDialogText, videoListListenerFlag, showDownBlobDialogValue)
        }
        webViewActivity.dialogsState.downBlobBusy = false
    }
}

class DownFontDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeDownFontDialog()
    }
}

class PreviewDialogListener(@JvmField val wa: WebViewActivity) : PreviewListener {
    override fun copyUrl(url: String?) {
        copyTextToast(wa, AppR.string.copied_clipboard, "Copied URL", url)
    }

    override fun openFile(url: String?, size: Long, isVideo: Boolean) {
        val webViewActivity = wa
        val previewDialogUrl = webViewActivity.dialogsState.previewDialogUrl
        webViewActivity.closePreviewDialog()
        webViewActivity.closeAllDialogs()
        if (isVideo) {
            webViewActivity.requestImagePreview(url, null, false)
        } else {
            webViewActivity.playVideoInPlayer(url, previewDialogUrl, false)
        }
    }

    override fun download(url: String?, mimeType: String?) {
        val webViewActivity = wa
        webViewActivity.closePreviewDialog()
        webViewActivity.closeAllDialogs()
        webViewActivity.requestSetDownDialog(url, null, mimeType)
    }

    override fun share(url: String?) {
        val webViewActivity = wa
        webViewActivity.closePreviewDialog()
        webViewActivity.closeAllDialogs()
        webViewActivity.openIntentUrl(url, null)
    }

    override fun openWith(url: String?, mimeType: String?) {
        val webViewActivity = wa
        if (webViewActivity.miscState.previewDialogListenerBusy) {
            return
        }
        webViewActivity.miscState.previewDialogListenerBusy = true
        webViewActivity.closePreviewDialog()
        if (!webViewActivity.previewFromLink) {
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaDownUrlDialogApplyPending(webViewActivity))
                return
            }
            return
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaPreviewOpenMedia(webViewActivity, url = url, mime = mimeType))
    }
}

class PreviewDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closePreviewDialog()
        webViewActivity.previewFromLink = false
    }
}

class WvaPreviewOpenMedia(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val mime: String? = null) : Runnable {

    internal inner class OpenMediaStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val url = this@WvaPreviewOpenMedia.url
            val mime = this@WvaPreviewOpenMedia.mime
            webViewActivity.openMediaUrl(url, mime)
            webViewActivity.miscState.previewDialogListenerBusy = false
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.closeVideoListDialog()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenMediaStep())
    }
}

class PreImageDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val handedOff = (dialogInterface as? com.mycompany.app.dialog.DialogPreImage)?.handedOff == true
        wa.closePreImageDialog()
        // the preview replaced the link menu: it shows again only when an action continues there
        if (handedOff) wa.urlLinkDialog?.window?.decorView?.visibility = android.view.View.VISIBLE else wa.closeUrlLinkDialog()
    }
}

class WvaBarsSettingApply(@JvmField val wa: WebViewActivity) : DialogApplyListener {
    override fun onApply() {
        val showStatus = BrowserSettings.showStatus.value
        val webViewActivity = wa
        if (showStatus != webViewActivity.dialogsState.showStatusBar || BrowserSettings.showNavi.value != webViewActivity.videoState.showNavBar) {
            if (!MainConst.isApi30 && (BrowserSettings.showStatus.value || BrowserSettings.showNavi.value)) {
                webViewActivity.miscState.barsSettingApplyBusy = true
            }
            webViewActivity.setSystemBarsHidden(BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, false)
        }
        if (BrowserSettings.fixTop.value == webViewActivity.savedFixTop && BrowserSettings.fixBot.value == webViewActivity.savedFixBot) {
            return
        }
        webViewActivity.applyChangedPrefs(webViewActivity.pageThemeColor(), webViewActivity.pageThemeLight())
    }
}

class WvaDarkConfirmButtons(@JvmField val wa: WebViewActivity) : DialogConfBtnListener {
    override fun onConfirm(confirmed: Boolean) {
        val webViewActivity = wa
        if (confirmed) {
            ZtriSettings.edit(webViewActivity.dialogsState.appContext) { set(ZtriSettings.guideDark, false) }
        }
        webViewActivity.closeDarkConfirmDialog()
    }

    override fun onButton() {
        val webViewActivity = wa
        webViewActivity.closeDarkConfirmDialog()
        openPlayStore(webViewActivity, "com.google.android.webview")
    }

    override fun onCancel() {
        wa.closeDarkConfirmDialog()
    }
}

class WvaShowDarkConfirmDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        wa.closeDarkConfirmDialog()
    }
}

class WvaTtsSettingApply(@JvmField val wa: WebViewActivity) : DialogApplyListener {

    internal inner class ApplyTtsSettingsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.applyTtsSettings()
        }
    }

    override fun onApply() {
        val ttsMode = TtsSettings.ttsMode.value
        val webViewActivity = wa
        if (ttsMode) {
            webViewActivity.runInBackground(ApplyTtsSettingsStep())
        } else {
            webViewActivity.shutdownTts()
        }
    }
}

class WvaWebCleanRun(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val reload: Boolean
) : Runnable {
    override fun run() {
        val webViewActivity = this.webViewActivity
        val webClean = webViewActivity.miscState.webClean
        if (webClean == null) {
            return
        }
        webClean.load(webViewActivity.dialogsState.appContext, this.reload, false)
    }
}

class WvaResumeSettingsTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ApplySettingsChangeStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.applySettingsChange(false)
        }
    }

    override fun run() {
        if (!AppState.nativeLoaded) {
            try {
                System.loadLibrary("myutil")
                AppState.nativeLoaded = true
            } catch (unused: Exception) {
            } catch (unused: UnsatisfiedLinkError) {
            }
        }
        val webViewActivity = wa
        val context = webViewActivity.dialogsState.appContext
        val resources = webViewActivity.getResources()
        MainApp.loadEarlySettings(context)
        MainApp.loadMediaSettings(context)
        MainApp.loadLateSettings(context, resources)
        AppState.settingsLoaded = true
        val webCoord = webViewActivity.webCoord
        if (webCoord != null) {
            webCoord.post(ApplySettingsChangeStep())
            if (com.mycompany.app.ui.viewer.ImagePreviewActivity.backgroundChanged) {
                com.mycompany.app.ui.viewer.ImagePreviewActivity.backgroundChanged = false
                val copyPath = backupDirPath2(webViewActivity.dialogsState.appContext, SyncSettings.secretMode.value, false)
                val myWebCoord: MyWebCoord? = if (!TextUtils.isEmpty(copyPath) && File(copyPath!!).exists()) webViewActivity.webCoord else null
                if (!TextUtils.isEmpty(copyPath) && File(copyPath!!).exists() && myWebCoord != null) {
                    myWebCoord.post(WvaApplyThemeTask(webViewActivity))
                }
            }
            if (BrowserSettings.checkDown.value) {
                webViewActivity.checkPendingDownloads()
            }
        }
    }
}

class WvaTabDeletedListener(@JvmField val wa: WebViewActivity) : TabDeletedListener {
    override fun onDeleted() {
        wa.rebuildTabs(false)
    }
}

class WvaPauseBackPlay(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class SetBackgroundPlayStep(
                                         @JvmField val checked: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.setBackgroundPlay(this.checked)
        }
    }

    override fun run() {
        val webViewActivity = wa
        val audioPlaying = MainUtil.isAudioPlaying(webViewActivity.dialogsState.appContext)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(SetBackgroundPlayStep(audioPlaying))
    }
}

class WvaHandListener(@JvmField val wa: WebViewActivity) : HandListener {
    override fun onHand() {
        val webViewActivity = wa
        if (webViewActivity.isPortraitSingleWindow()) {
            if (TtsSettings.handNoti.value) {
                TtsSettings.edit(webViewActivity.dialogsState.appContext) { set(TtsSettings.handNoti, false) }
            }
            if (TtsSettings.handMode.value) {
                webViewActivity.exitHandMode()
            }
        }
    }
}

class WvaHandModeEnterAnim(@JvmField val wa: WebViewActivity) : AnimatorUpdateListener {
    override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        val webViewActivity = wa
        if (webViewActivity.miscState.handEnterAnim == null || webViewActivity.webCoord == null) {
            return
        }
        val animatedValue = (valueAnimator.getAnimatedValue() as Int)
        if (webViewActivity.miscState.handEnterAnim == null || webViewActivity.webCoord == null) {
            return
        }
        webViewActivity.miscState.handEnterOffset = animatedValue
        if (webViewActivity.miscState.handEnterAnimBusy) {
            return
        }
        webViewActivity.miscState.handEnterAnimBusy = true
        MainApp.postToMain(webViewActivity.dialogsState.appContext, webViewActivity.`in`)
    }
}

class WvaHandModeEnterAnimListener(@JvmField val wa: WebViewActivity) : Animator.AnimatorListener {

    internal inner class HandEnterEndStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.miscState.handEnterAnim == null) {
                return
            }
            MainApp.postToMain(webViewActivity.dialogsState.appContext, WvaHandModeEnterEnd(webViewActivity))
        }
    }

    override fun onAnimationCancel(animator: Animator) {
        val webViewActivity = wa
        if (webViewActivity.miscState.handEnterAnim == null) {
            return
        }
        webViewActivity.miscState.handEnterAnim = null
        webViewActivity.applyTopInset(TtsSettings.handMode.value)
    }

    override fun onAnimationEnd(animator: Animator) {
        val webViewActivity = wa
        val handler: Handler? = if (webViewActivity.miscState.handEnterAnim == null) null else webViewActivity.mainHandler
        if (webViewActivity.miscState.handEnterAnim == null || handler == null) {
            return
        }
        handler.post(HandEnterEndStep())
    }

    override fun onAnimationRepeat(animator: Animator) {
    }

    override fun onAnimationStart(animator: Animator) {
    }
}

class WvaSetupCreatePages(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.createPageViews()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaSetupCreatePagesTask(webViewActivity))
    }
}

class WvaHandModeEnterApply(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.miscState.handEnterAnimBusy = false
        if (webViewActivity.miscState.handEnterAnim != null) {
            val handEnterOffset = webViewActivity.miscState.handEnterOffset
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.setTranslationY(handEnterOffset.toFloat())
        }
    }
}

class WvaHandModeEnterEnd(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.miscState.handEnterAnim != null) {
            webViewActivity.miscState.handEnterAnim = null
            webViewActivity.applyTopInset(TtsSettings.handMode.value)
            if (ZtriSettings.handIcon.value) {
                webViewActivity.updatePullButton(true)
            }
        }
    }
}

class WvaHandModeExitAnim(@JvmField val wa: WebViewActivity) : AnimatorUpdateListener {
    override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        val webViewActivity = wa
        if (webViewActivity.miscState.handExitAnim == null || webViewActivity.webCoord == null) {
            return
        }
        val animatedValue = (valueAnimator.getAnimatedValue() as Int)
        if (webViewActivity.miscState.handExitAnim == null || webViewActivity.webCoord == null) {
            return
        }
        webViewActivity.miscState.handModeOffset = animatedValue
        if (webViewActivity.miscState.handExitAnimBusy) {
            return
        }
        webViewActivity.miscState.handExitAnimBusy = true
        MainApp.postToMain(webViewActivity.dialogsState.appContext, webViewActivity.handExitTask)
    }
}

class WvaHandModeExitAnimListener(@JvmField val wa: WebViewActivity) : Animator.AnimatorListener {

    internal inner class HandExitEndStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.miscState.handExitAnim == null) {
                return
            }
            MainApp.postToMain(webViewActivity.dialogsState.appContext, WvaHandModeExitEnd(webViewActivity))
        }
    }

    override fun onAnimationCancel(animator: Animator) {
        val webViewActivity = wa
        if (webViewActivity.miscState.handExitAnim == null) {
            return
        }
        webViewActivity.miscState.handExitAnim = null
        webViewActivity.applyTopInset(TtsSettings.handMode.value)
    }

    override fun onAnimationEnd(animator: Animator) {
        val webViewActivity = wa
        val handler: Handler? = if (webViewActivity.miscState.handExitAnim == null) null else webViewActivity.mainHandler
        if (webViewActivity.miscState.handExitAnim == null || handler == null) {
            return
        }
        handler.post(HandExitEndStep())
    }

    override fun onAnimationRepeat(animator: Animator) {
    }

    override fun onAnimationStart(animator: Animator) {
    }
}

class WvaHandModeExitApply(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.miscState.handExitAnimBusy = false
        if (webViewActivity.miscState.handExitAnim != null) {
            val handModeOffset = webViewActivity.miscState.handModeOffset
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.setTranslationY(handModeOffset.toFloat())
        }
    }
}

class WvaHandModeExitEnd(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.miscState.handExitAnim != null) {
            webViewActivity.miscState.handExitAnim = null
            webViewActivity.applyTopInset(TtsSettings.handMode.value)
            webViewActivity.removeHandModeBar()
            if (ZtriSettings.handIcon.value) {
                webViewActivity.updatePullButton(true)
            }
        }
    }
}

class WvaTtsInitListener(@JvmField val wa: WebViewActivity) : OnInitListener {
    override fun onInit(status: Int) {
        if (status == -1) {
            wa.shutdownTts()
        }
    }
}

class WvaApplyRecentLimitTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val recentLimit = ZtwoSettings.recentLimit.value
        val webViewActivity = wa
        val context = webViewActivity.dialogsState.appContext
        if (recentLimit == 0) {
            DbBookRecent.clearAll(context)
            return
        }
        trimOld(context, false)
        val recentSecret = ZtwoSettings.recentSecret.value
        val appContext = webViewActivity.dialogsState.appContext
        if (recentSecret) {
            trimOld(appContext, true)
        } else {
            clearSecret(appContext, true)
        }
    }
}

class WvaZoomButtonListener(@JvmField val wa: WebViewActivity) : FltViewListener {
    override fun onTap(view: View?, button: Int) {
        wa.applyTextZoom()
    }

    override fun onGone() {
        wa.hideMoveHint()
    }

    override fun onLongPress(view: View?) {
        wa.showMoveHint(view)
    }
}

class WvaTabLimitApplied(@JvmField val wa: WebViewActivity) : WebViewActivity.TabLimitListener {

    internal inner class RefreshAfterLimitStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.exitFind(true)
            webViewActivity.refreshTabBar(2, true)
        }
    }

    override fun onLimitApplied(isChanged: Boolean) {
        if (isChanged) {
            val webViewActivity = wa
            webViewActivity.updateTabCountIcons()
            webViewActivity.layoutTabFrames()
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(RefreshAfterLimitStep())
        }
    }
}

class WvaInvalidateActionMode(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val actionMode = wa.dialogsState.actionMode
        if (actionMode == null) {
            return
        }
        try {
            actionMode.invalidateContentRect()
        } catch (unused: Exception) {
        }
    }
}

class WvaConfigThemeChanged(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.onThemeChanged(true)
        val myWebCoord: MyWebCoord? = if (!PdfSettings.topToBottom.value) null else webViewActivity.webCoord
        if (!PdfSettings.topToBottom.value || myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaOnConfigurationChangedTask(webViewActivity))
    }
}

class WvaOnConfigurationChangedTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.updateSplitLand(webViewActivity.isLandscapeScreen())
        webViewActivity.refreshBarFrames()
    }
}

class WvaExitCleanup2(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ExitCleanupStep : Runnable {

        internal inner class ExitClearCookiesStep : Runnable {
            override fun run() {
                val wvaExitCleanup = this@WvaExitCleanup2
                clearCookies(wa.dialogsState.appContext)
                val webViewActivity = wa
                val deleteBit = BrowserSettings.exitDelete.value and 16
                val handler = webViewActivity.mainHandler
                if (deleteBit != 16) {
                    if (handler != null) {
                        handler.post(WvaClearOnExit(webViewActivity))
                    }
                } else {
                    if (handler == null) {
                        return
                    }
                    handler.post(WvaExitClearPage(webViewActivity))
                }
            }
        }

        override fun run() {
            val webViewActivity = wa
            try {
                WebViewDatabase.getInstance(webViewActivity.dialogsState.appContext).clearHttpAuthUsernamePassword()
            } catch (unused: Exception) {
            } catch (unused: NoSuchMethodError) {
            }
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ExitClearCookiesStep())
        }
    }

    override fun run() {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(ExitCleanupStep())
    }
}

class SendToBackgroundTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.sendAppToBackground()
    }
}

class BundleSavedToBackground(@JvmField val wa: WebViewActivity) : WebBundleListener {

    internal inner class SendToBackgroundStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.sendAppToBackground()
        }
    }

    override fun onBundle() {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(SendToBackgroundStep())
    }
}

class WvaSendAppToBackgroundTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.finishWithCleanup()
    }
}

class WvaRestartAppTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.restartApp()
    }
}

class WvaRestartAppBundleListener(@JvmField val wa: WebViewActivity) : WebBundleListener {

    internal inner class RestartAppStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.restartApp()
        }
    }

    override fun onBundle() {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(RestartAppStep())
    }
}

class WvaCheckSiteLockLater(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        wa.checkSiteLock(false)
    }
}

class WvaSetWindowBackgroundTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class SetWindowBackgroundStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val windowBgLandscape = webViewActivity.miscState.windowBgLandscape
            val bitmap = webViewActivity.miscState.windowBgBitmap
            webViewActivity.miscState.windowBgBitmap = null
            webViewActivity.setWindowBackground(bitmap, windowBgLandscape)
        }
    }

    override fun run() {
        val webViewActivity = wa
        val landscape = webViewActivity.isLandscapeScreen()
        webViewActivity.miscState.windowBgLandscape = landscape
        webViewActivity.miscState.windowBgBitmap = backupBitmapCopy(webViewActivity.dialogsState.appContext, landscape)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(SetWindowBackgroundStep())
    }
}

class WvaWindowBackgroundCheck(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.dialogsState.postWindowBackgroundUpdateBusy = false
        webViewActivity.checkWindowBackground()
    }
}

class EngineMenuClick(@JvmField val wa: WebViewActivity) : View.OnClickListener {
    override fun onClick(view: View?) {
        val webViewActivity = wa
        if (webViewActivity.isAnyDialogOpen() || webViewActivity.engineMenu != null) {
            return
        }
        webViewActivity.closeEngineMenu()
        if (view != null) {
            val myEngineMenu = MyEngineMenu(
                webViewActivity,
                webViewActivity.dialogsState.webBody,
                view,
                MainUtil.isDarkPage(webViewActivity.onStartPage),
                WvaEngineMenuListener(webViewActivity)
            )
            webViewActivity.engineMenu = myEngineMenu
            webViewActivity.popupMenu = myEngineMenu
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaEngineMenuClosed(webViewActivity))
        }
    }
}

class WvaFinishStartupTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.addFloatingButtons()
        webViewActivity.finishStartup()
        webViewActivity.miscState.startPageSetupBusy = false
    }
}

class WvaEditorBarColors(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.setupStarted) {
            webViewActivity.applyThemeColors(webViewActivity.dialogsState.themeColor, webViewActivity.miscState.themeLight, webViewActivity.navBarColorNow())
        }
        if (webViewActivity.onStartPage) {
            webViewActivity.refreshStartPage()
        }
    }
}

class WvaExitFindOnApply(@JvmField val wa: WebViewActivity) : DialogApplyListener {
    override fun onApply() {
        wa.exitFind(true)
    }
}

class WvaOnRefreshListener(@JvmField val wa: WebViewActivity) : OnRefreshListener {
    override fun onRefresh() {
        wa.onRefreshButton()
    }
}

class WvaNoOpUserShow(@JvmField val wa: WebViewActivity) : UserShowListener {
    override fun onShow() {
        val webViewActivity = wa
    }
}

class WvaHistoryDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeHistoryDialog()
    }
}

class TabOverviewDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeTabOverview()
        val miniMode = ZoneSettings.tabMiniMode.value
        val fastList = miniMode && ZoneSettings.tabListType.value == 0
        if (miniMode != webViewActivity.tabOverviewAnim || fastList != webViewActivity.tabOverviewFast) {
            if (miniMode) {
                webViewActivity.showMiniTabOverview()
            } else {
                webViewActivity.showTabOverview()
            }
        }
    }
}

