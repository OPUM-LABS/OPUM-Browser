@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.web.WebNestFrame.BackAnimUpdate
import com.mycompany.app.web.WebNestFrame.DrawShadeAnimListener
import com.mycompany.app.web.WebTabBarSubView.SetOpenTask
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.nativeJsN
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.snsJs2
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.snsUrl
import com.mycompany.app.main.MainUtil.snsJs1
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.extractUrl
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.fileNumberSuffix
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.setAlgorithmicDark
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.main.MainUtil.offlineUrlFor
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.parseFloatSafe
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.db.book.DbBookTab.Companion.insertTab
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.loadDrawable
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
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.main.MainUtil.pageTitleForFile
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.setIcon
import com.mycompany.app.main.MainUtil.applyBarAppearance
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.compress.Compress.Companion.isVideoExt
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.nextPageUrl
import com.mycompany.app.main.MainUtil.isPhnGif
import com.mycompany.app.main.MainUtil.phTitle
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.freeMemory
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
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
import com.mycompany.app.data.book.DataBookTmem.Companion.instance
import com.mycompany.app.db.book.DbBookTmem.Companion.get
import com.mycompany.app.data.book.DataBookTrans.Companion.instance
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
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.iconResFor
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.main.MainUtil.navBarInset
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
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
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import android.view.ViewGroup
import com.mycompany.app.web.WebNestView
import android.webkit.WebViewClient
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import android.webkit.WebChromeClient
import android.animation.ValueAnimator
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.quick.QuickView
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.db.DbUtil
import android.view.MotionEvent
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefAlbum
import android.content.DialogInterface
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import android.view.GestureDetector
import com.mycompany.app.view.MyPopupMenu
import android.view.GestureDetector.SimpleOnGestureListener
import com.mycompany.app.view.PopMenuItem
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.db.book.DbBookTab

import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.web.WebViewActivity.TabLimitListener
import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.web.WebViewActivity.TabViewListener
import com.mycompany.app.web.WebTabBarSubView
import com.mycompany.app.dialog.DialogSetFull
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.ui.browser.BarScroll
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.view.MyBarView
import android.webkit.DownloadListener
import com.mycompany.app.view.MySnackbar.SnackbarListener
import com.mycompany.app.view.MySnackbar.SnackItem
import com.mycompany.app.quick.TabDragListener
import com.mycompany.app.web.WebTabBarSubView.TabBarSubListener
import android.content.ContentValues
import com.mycompany.app.dialog.DialogDeleteItem.DelItemListener
import com.mycompany.app.dialog.ListTabListener
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.view.MyBarView.BarListener
import android.graphics.Bitmap
import android.view.View
import com.mycompany.app.db.book.DbTabThumb
import ch.opumlabs.opumbrowser.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import kotlin.math.abs
import com.mycompany.app.web.WebViewActivity.Companion.Xn
import com.mycompany.app.web.WebViewActivity.Companion.pauseMedia
import com.mycompany.app.web.WebViewActivity.Companion.pauseWebView
import com.mycompany.app.web.WebViewActivity.Companion.applyDarkSettings
import com.mycompany.app.web.WebViewActivity.Companion.newTabUrl
import com.mycompany.app.web.WebViewActivity.Companion.SHOW_ADDED_TAB_MS

/*
 * The browser activity's tabs callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaAddTabAnimateTask(@JvmField val wa: WebViewActivity, @JvmField val isTop: Boolean = false, @JvmField val animateFrame: Boolean = false, @JvmField val addTabImplTask7AddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val reusedPending: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val isTop = this@WvaAddTabAnimateTask.isTop
        val animateFrame = this@WvaAddTabAnimateTask.animateFrame
        val tabAddListener = this@WvaAddTabAnimateTask.addTabImplTask7AddListener
        val reusedPending = this@WvaAddTabAnimateTask.reusedPending
        val webNestFrame: WebNestFrame? = if ((animateFrame || reusedPending)) webViewActivity.tabsState.currentFrame else null
        if ((animateFrame || reusedPending) && webNestFrame != null) {
            webNestFrame.startAddAnim()
        }
        TabFrameLog.step(webViewActivity, "step100 shown", webViewActivity.tabsState.currentFrame, "anim=${animateFrame || reusedPending}")
        TabFrameLog.checkAfterAdd(webViewActivity) { webViewActivity.tabsState.currentFrame }
        // The new frame waits hidden (setAddNeed) until its page first paints (onPageCommitVisible / onPageFinished
        // call finishTabFrameAnim, which slides it in). On a slow or stalled page that never came, so the previous tab stayed on
        // screen and took the touches although the new tab was current. Show it after SHOW_ADDED_TAB_MS anyway.
        val added = webViewActivity.tabsState.currentFrame
        if (!(animateFrame || reusedPending) && added != null) {
            added.postDelayed({
                if (!webViewActivity.isFinishing && webViewActivity.tabsState.currentFrame === added && added.touchBlocked && added.addAnim == null &&
                    added.visibility != View.VISIBLE
                ) {
                    TabFrameLog.step(webViewActivity, "show added tab (page not painted yet)", added)
                    webViewActivity.finishTabFrameAnim()
                }
            }, SHOW_ADDED_TAB_MS)
        }
        webViewActivity.updateTabCountIcons()
        webViewActivity.refreshTabBar(1, true)
        webViewActivity.tabsState.goBackAnimBusy = false
        webViewActivity.tabsState.isTop = isTop
        webViewActivity.tabsState.animateFrame = animateFrame
        webViewActivity.miscState.tabAddListener = tabAddListener
        webViewActivity.tabsState.reusedPending = reusedPending
        if (reusedPending && webViewActivity.pageState.popupPending) {
            webViewActivity.goBackPage(WvaFinishTabAddOnBack(webViewActivity))
        } else {
            webViewActivity.finishTabAdd()
        }
    }
}

class WvaOpenInNextPageTask(@JvmField val wa: WebViewActivity, @JvmField val isHtml: Boolean = false, @JvmField val url: String? = null, @JvmField val refererOrHtml: String? = null) : Runnable {
    override fun run() {
        val pageColor: Int
        val pageLight: Int
        val webViewActivity = wa
        val url = this@WvaOpenInNextPageTask.url
        val refererOrHtml = this@WvaOpenInNextPageTask.refererOrHtml
        val isHtml = this@WvaOpenInNextPageTask.isHtml
        val webNestFrame = webViewActivity.tabsState.currentFrame
        var deskMode = false
        if (webNestFrame == null) {
            webViewActivity.destroyPendingPage()
            webViewActivity.findState.openInNextPageBusy = false
            return
        }
        val pageIndex = webNestFrame.pageIndex + 1
        val childPos = webViewActivity.tabsState.currentFrame!!.childIndexFor(pageIndex)
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            deskMode = webNestView.deskMode
            pageColor = webViewActivity.pageThemeColor()
            pageLight = webViewActivity.pageThemeLight()
            val quickView: QuickView? = if (webViewActivity.onStartPage) webViewActivity.currentPage!!.quickPanel else null
            if (webViewActivity.onStartPage && quickView != null) {
                quickView.editing = true
            }
            webViewActivity.currentPage!!.setWebViewClient(MainUtil.quietWebClient)
            webViewActivity.currentPage!!.setWebChromeClient(null)
            webViewActivity.currentPage!!.setDownloadListener(null)
            webViewActivity.nextPageAnim = true
            webViewActivity.animFromStartPage = webViewActivity.onStartPage
        } else {
            pageColor = 0
            pageLight = 0
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaNextPagePauseTask(webViewActivity, url = url, refererOrHtml = refererOrHtml, isHtml = isHtml, pageIndex = pageIndex, childIndex = childPos, deskMode = deskMode, themeColor = pageColor, themeLight = pageLight))
    }
}

class WvaNextPageTintTask(@JvmField val wa: WebViewActivity, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val themeColor = this@WvaNextPageTintTask.themeColor
        val themeLight = this@WvaNextPageTintTask.themeLight
        val webTabBarAdapter = webViewActivity.tabBar
        val arrayList: ArrayList<WebTabItem?>? = if (webTabBarAdapter != null) webViewActivity.tabsState.tabItems else null
        if (webTabBarAdapter != null && arrayList != null) {
            webTabBarAdapter.applyTint(arrayList, webViewActivity.currentTabIndex, webViewActivity.onStartPage, themeColor, themeLight, true)
            webViewActivity.tintSideButton(themeColor, themeLight)
        }
        val webNestFrame: WebNestFrame? = if (!webViewActivity.nextPageAnim) webViewActivity.tabsState.currentFrame else null
        if (!webViewActivity.nextPageAnim && webNestFrame != null) {
            webNestFrame.postBlankTime()
        }
        webViewActivity.tabsState.goBackAnimBusy = false
        webViewActivity.findState.openInNextPageBusy = false
    }
}

class WvaCreateBackTabTask(@JvmField val wa: WebViewActivity, @JvmField val backTabPage: com.mycompany.app.web.WebNestView? = null, @JvmField val backTabFrame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val backUrl: String? = null) : Runnable {

    internal inner class CreateBackTabStep : Runnable {

        internal inner class BackTabDarkStep : Runnable {

            internal inner class BackTabListenersStep : Runnable {

                internal inner class ShowBackTabStep : Runnable {
                    override fun run() {
                        val webViewActivity = wa
                        val webNestView = this@WvaCreateBackTabTask.backTabPage
                        val webNestFrame = this@WvaCreateBackTabTask.backTabFrame
                        val backUrl = this@WvaCreateBackTabTask.backUrl
                        val inGroup = webViewActivity.isInTabGroup(webViewActivity.currentTabIndex)
                        val autoGroup = TtsSettings.autoGroup.value
                        val insertedIndex = webViewActivity.insertTabItem(
                            webViewActivity.nextTabInsertIndex(inGroup || autoGroup),
                            backUrl,
                            webNestFrame,
                            inGroup,
                            autoGroup
                        )
                        webViewActivity.touchTabUsage(insertedIndex)
                        val myWebCoord = webViewActivity.webCoord
                        if (myWebCoord == null) {
                            return
                        }
                        myWebCoord.post(WvaAttachBackTabTask(webViewActivity, createBackTabTaskPage = webNestView, nestFrame = webNestFrame, backUrl = backUrl, ownerIndex = insertedIndex))
                    }
                }

                override fun run() {
                    val createBackTabStep = this@CreateBackTabStep
                    val webViewActivity = wa
                    webViewActivity.attachPageListeners(this@WvaCreateBackTabTask.backTabFrame, this@WvaCreateBackTabTask.backTabPage, 1)
                    wa.runInBackground(ShowBackTabStep())
                }
            }

            override fun run() {
                val wvaCreateBackTabTask = this@WvaCreateBackTabTask
                WebViewActivity.applyDarkSettings(this@WvaCreateBackTabTask.backTabPage)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(BackTabListenersStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.addJsBridge(this@WvaCreateBackTabTask.backTabPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(BackTabDarkStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.applyWebSettings(this@WvaCreateBackTabTask.backTabPage, 1)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(CreateBackTabStep())
    }
}

class WvaTabLimitListener(@JvmField val wa: WebViewActivity) : WebViewActivity.TabLimitListener {

    internal inner class ReleaseBackTabStep : Runnable {
        override fun run() {
            val parent: Any?
            val webViewActivity = wa
            val webNestView = webViewActivity.tabsState.nestView
            val backTabUrl = webViewActivity.tabsState.backTabUrl
            webViewActivity.tabsState.nestView = null
            webViewActivity.tabsState.backTabUrl = null
            if (webViewActivity.tabsState.currentFrame == null || webNestView == null) {
                webViewActivity.tabsState.createBackTabBusy = false
                return
            }
            webNestView.openUrl(backTabUrl, webViewActivity.dialogsState.pageUrl)
            val webNestFrame = webViewActivity.tabsState.currentFrame
            if (webNestFrame!!.backAnim == null) {
                webNestFrame.cancelAddAnim()
                if (webNestFrame.getWidth() != 0 || ((webNestFrame.getParent()
                        .also { parent = it }) != null && (parent as View).getWidth() != 0)
                ) {
                    webNestFrame.ensureShadePaints()
                    val distance = MainApp.dp48 shl 1
                    webNestFrame.backAnimValue = 0
                    webNestFrame.backAnimRunning = false
                    val valueAnimatorOfInt = ValueAnimator.ofInt(0, distance)
                    webNestFrame.backAnim = valueAnimatorOfInt
                    valueAnimatorOfInt.setDuration(600L)
                    webNestFrame.backAnim!!.addUpdateListener(webNestFrame.BackAnimUpdate())
                    webNestFrame.backAnim!!.addListener(webNestFrame.DrawShadeAnimListener())
                    webNestFrame.backAnim!!.start()
                }
            }
            webViewActivity.updateTabCountIcons()
            webViewActivity.refreshTabBar(0, true)
            webViewActivity.tabsState.goBackAnimBusy = false
            webViewActivity.tabsState.createBackTabBusy = false
        }
    }

    override fun onLimitApplied(isChanged: Boolean) {
        val webViewActivity = wa
        webViewActivity.layoutTabFrames()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ReleaseBackTabStep())
    }
}

class WvaWebChromeClient(@JvmField val wa: WebViewActivity) : WebChromeClient() {

    internal inner class RedrawTabStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val redrawTabIndex = webViewActivity.tabsState.redrawTabIndex
            val webTabBarAdapter = webViewActivity.tabBar
            if (webTabBarAdapter != null) {
                webTabBarAdapter.redrawAt(webTabBarAdapter.barPosOf(redrawTabIndex))
            }
        }
    }

    override fun onReceivedIcon(webView: WebView?, bitmap: Bitmap?) {
        if (webView == null || webView !is WebNestView) {
            return
        }
        val backIndex = (webView).backIndex
        wa.onPageIcon(webView, bitmap, backIndex, true)
    }

    override fun onReceivedTitle(webView: WebView?, title: String?) {
        if (webView == null || webView !is WebNestView) {
            return
        }
        val webNestView = webView
        val backIndex = webNestView.backIndex
        val webViewActivity = wa
        val arrayList = webViewActivity.tabsState.tabItems
        if (arrayList == null || backIndex < 0 || backIndex >= arrayList.size) {
            return
        }
        val url = webView.getUrl()
        if (TextUtils.isEmpty(url)) {
            return
        }
        val pageTitle = webViewActivity.pageTitleOf(webView, url)
        val webTabItemD = webViewActivity.tabItemAt(backIndex)
        if (webTabItemD != null) {
            webTabItemD.tabUrl = url
            webTabItemD.tabTitle = pageTitle
            webTabItemD.deskMode = webNestView.deskMode
            webViewActivity.loadTabItemAsync(webTabItemD)
            if (webViewActivity.tabBar != null) {
                webViewActivity.tabsState.redrawTabIndex = backIndex
                val myWebBody = webViewActivity.dialogsState.webBody
                if (myWebBody == null) {
                    return
                } else {
                    myWebBody.post(RedrawTabStep())
                }
            }
        }
        if (BrowserSettings.historyTime.value != 0) {
            if ((SyncSettings.secretMode.value && SecretSettings.secretHist.value == 0L) || "file:///android_asset/shortcut.html" == url) {
                return
            }
            webViewActivity.saveBookIcon(url, pageTitle, null, true, false)
        }
    }
}

class WvaFinishTabAnimTask(@JvmField val wa: WebViewActivity, @JvmField val finishTabAnimTabItem: WebTabItem? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaFinishTabAnimTask.finishTabAnimTabItem
        if (webTabItem == null || webViewActivity.tabsState.currentFrame == null) {
            webViewActivity.tabsState.finishTabAnimBusy = false
            return
        }
        WebViewActivity.pauseMedia(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaFinishTabAnimPauseWebTask(webViewActivity, finishTabAnimTaskTabItem = webTabItem))
    }
}

class WvaFinishTabAnimPauseWebTask(@JvmField val wa: WebViewActivity, @JvmField val finishTabAnimTaskTabItem: WebTabItem? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaFinishTabAnimPauseWebTask.finishTabAnimTaskTabItem
        if (webTabItem == null || webViewActivity.tabsState.currentFrame == null) {
            webViewActivity.tabsState.finishTabAnimBusy = false
            return
        }
        WebViewActivity.pauseWebView(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaFinishTabAnimDestroyFrameTask(webViewActivity, finishTabAnimTask2TabItem = webTabItem))
    }
}

class WvaFinishTabAnimDestroyFrameTask(@JvmField val wa: WebViewActivity, @JvmField val finishTabAnimTask2TabItem: WebTabItem? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaFinishTabAnimDestroyFrameTask.finishTabAnimTask2TabItem
        var frame: WebNestFrame? = null
        val webNestFrame: WebNestFrame? = if (webTabItem == null) null else webViewActivity.tabsState.currentFrame
        if (webTabItem == null || webNestFrame == null) {
            webViewActivity.tabsState.finishTabAnimBusy = false
            return
        }
        detachFromParent(webNestFrame)
        val tabRestore = BrowserSettings.tabRestore.value
        val currentFrame = webViewActivity.tabsState.currentFrame
        if (tabRestore) {
            currentFrame!!.backItem = webTabItem
            frame = currentFrame
        } else {
            currentFrame!!.destroyFrame(null)
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaFinishTabAnimRemoveTask(webViewActivity, animFrame = frame))
    }
}

class WvaFinishTabAnimRemoveTask(@JvmField val wa: WebViewActivity, @JvmField val animFrame: com.mycompany.app.web.WebNestFrame? = null) : Runnable {

    internal inner class RemoveAnimatedTabStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestFrame = this@WvaFinishTabAnimRemoveTask.animFrame
            webViewActivity.removeTabAt(webViewActivity.currentTabIndex)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaFinishTabAnimUndoTask(webViewActivity, animFrame = webNestFrame))
        }
    }

    override fun run() {
        wa.runInBackground(RemoveAnimatedTabStep())
    }
}

class WvaFinishTabAnimUndoTask(@JvmField val wa: WebViewActivity, @JvmField val animFrame: com.mycompany.app.web.WebNestFrame? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestFrame = this@WvaFinishTabAnimUndoTask.animFrame
        val arrayList = webViewActivity.tabsState.tabItems
        if (arrayList == null || arrayList.size == 0) {
            webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), false, null)
            if (webNestFrame != null) {
                webViewActivity.offerTabUndo(webNestFrame)
            }
        } else {
            webViewActivity.tabsState.animFrame = webNestFrame
            webViewActivity.reloadTabViews(TabRemovedListener(webViewActivity))
        }
        webViewActivity.refreshAfterPageChange()
        webViewActivity.tabsState.finishTabAnimBusy = false
    }
}

class TabRemovedListener(@JvmField val wa: WebViewActivity) : WebViewActivity.TabViewListener {
    override fun onTabView() {
        val webViewActivity = wa
        val webNestFrame = webViewActivity.tabsState.animFrame
        webViewActivity.tabsState.animFrame = null
        if (webNestFrame != null) {
            webViewActivity.offerTabUndo(webNestFrame)
        }
        webViewActivity.refreshTabBar(1, true)
    }
}

class WvaSwitchToTabTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class SwitchTabStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val switchIndex = webViewActivity.tabsState.switchIndex
            val webNestFrame = webViewActivity.tabsState.switchFrame
            webViewActivity.tabsState.switchFrame = null
            webViewActivity.removeTabAt(switchIndex)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaSwitchTabFrameTask(webViewActivity, switchIndex = switchIndex, switchFrame = webNestFrame))
        }
    }

    override fun run() {
        wa.runInBackground(SwitchTabStep())
    }
}

class WvaSwitchTabFrameTask(@JvmField val wa: WebViewActivity, @JvmField val switchIndex: Int = 0, @JvmField val switchFrame: com.mycompany.app.web.WebNestFrame? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val switchIndex = this@WvaSwitchTabFrameTask.switchIndex
        val webNestFrame = this@WvaSwitchTabFrameTask.switchFrame
        val arrayList = webViewActivity.tabsState.tabItems
        if (arrayList == null || arrayList.size == 0) {
            webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), false, null)
            if (webNestFrame != null) {
                webViewActivity.offerTabUndo(webNestFrame)
            }
            webViewActivity.refreshTabBarItems(switchIndex)
        } else {
            webViewActivity.reloadTabViews(WvaTabUndoReload(webViewActivity, switchFrame = webNestFrame, switchIndex = switchIndex))
        }
        webViewActivity.tabsState.switchToTabBusy = false
    }
}

class WvaTabUndoReload(@JvmField val wa: WebViewActivity, @JvmField val switchFrame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val switchIndex: Int = 0) : WebViewActivity.TabViewListener {
    override fun onTabView() {
        val webViewActivity = wa
        val switchIndex = this@WvaTabUndoReload.switchIndex
        val webNestFrame = this@WvaTabUndoReload.switchFrame
        if (webNestFrame != null) {
            webViewActivity.offerTabUndo(webNestFrame)
        }
        webViewActivity.refreshTabBarItems(switchIndex)
    }
}

class WvaTabDeleteListenerTask(@JvmField val wa: WebViewActivity, @JvmField val tabDeleteListenerTabItem: WebTabItem? = null) : Runnable {

    internal inner class DeleteTabStep : Runnable {

        internal inner class TabBarRefreshedListener : WebViewActivity.TabViewListener {

            internal inner class CloseDeleteItemStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    webViewActivity.closeDeleteItemDialog()
                }
            }

            override fun onTabView() {
                val wvaTabDeleteListenerTask = this@WvaTabDeleteListenerTask
                val webViewActivity = wa
                webViewActivity.refreshTabBar(2, true)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CloseDeleteItemStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            val tabViewListener = TabBarRefreshedListener()
            webViewActivity.reloadTabViews(tabViewListener)
        }
    }

    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaTabDeleteListenerTask.tabDeleteListenerTabItem
        if (webTabItem != null) {
            DbBookTab.clearOpen(webViewActivity.dialogsState.appContext, SyncSettings.secretMode.value)
            webTabItem.uid = System.currentTimeMillis()
            webTabItem.parentId = -1L
            webTabItem.position = 0
            webTabItem.groupId = 0L
            webTabItem.groupName = null
            webTabItem.groupColor = 0
            insertTab(webViewActivity.dialogsState.appContext, webTabItem, SyncSettings.secretMode.value)
            val arrayList: ArrayList<WebTabItem?> = ArrayList()
            webViewActivity.tabsState.tabItems = arrayList
            arrayList.add(webTabItem)
            webViewActivity.currentTabIndex = 0
            webViewActivity.clampTabIndex()
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(DeleteTabStep())
        }
    }
}

class WvaReloadAttachTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ReloadViewsStep : Runnable {

        internal inner class ReloadDownloadListener : DownloadListener {
            override fun onDownloadStart(
                url: String?,
                userAgent: String?,
                contentDisposition: String?,
                mimeType: String?,
                contentLength: Long
            ) {
                val webViewActivity = wa
                webViewActivity.onPageDownload(url, contentDisposition, mimeType, contentLength, true)
            }
        }

        internal inner class AttachListenersStep : Runnable {

            internal inner class ReloadDarkStep : Runnable {

                internal inner class ReloadListenersStep : Runnable {
                    override fun run() {
                        val attachListenersStep = this@AttachListenersStep
                        val webViewActivity = wa
                        webViewActivity.attachPageListeners(webViewActivity.tabsState.currentFrame, webViewActivity.currentPage, 0)
                        val activity = wa
                        val myWebCoord = activity.webCoord
                        if (myWebCoord == null) {
                            return
                        }
                        myWebCoord.post(WvaReloadLayoutTask(activity))
                    }
                }

                override fun run() {
                    val reloadViewsStep = this@ReloadViewsStep
                    WebViewActivity.applyDarkSettings(wa.currentPage)
                    val myWebCoord = wa.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(ReloadListenersStep())
                }
            }

            override fun run() {
                val wvaReloadAttachTask = this@WvaReloadAttachTask
                val webViewActivity = wa
                webViewActivity.addJsBridge(webViewActivity.currentPage)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(ReloadDarkStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            val webNestView: WebNestView? = if (webViewActivity.tabsState.currentFrame == null) null else webViewActivity.currentPage
            if (webViewActivity.tabsState.currentFrame == null || webNestView == null) {
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(WvaReloadLayoutTask(webViewActivity))
                return
            }
            if (!webNestView.hasSetting) {
                webViewActivity.pageUrlForTab()
                webViewActivity.applyWebSettings(webViewActivity.currentPage, 0)
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.post(AttachListenersStep())
                    return
                }
                return
            }
            webNestView.setWebViewClient(WvaLocalWebViewClient(webViewActivity))
            webViewActivity.currentPage!!.setWebChromeClient(WvaLocalChromeClient(webViewActivity))
            webViewActivity.currentPage!!.setDownloadListener(ReloadDownloadListener())
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaReloadLayoutTask(webViewActivity))
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        val webTabItemD: WebTabItem? = if (webViewActivity.tabsState.currentFrame != null && webViewActivity.currentPage != null) webViewActivity.tabItemAt(
                webViewActivity.currentTabIndex
            ) else null
        if (webViewActivity.tabsState.currentFrame != null && webViewActivity.currentPage != null && webTabItemD != null) {
            var webNestFrameM = webTabItemD.webFrame
            if (webNestFrameM == null) {
                webNestFrameM =
                    webViewActivity.createTabFrame(webTabItemD.uid, webTabItemD.tabUrl, webTabItemD.deskMode, true)
                webTabItemD.webFrame = webNestFrameM
            }
            val pageCreate = webNestFrameM.pageCreate
            if (pageCreate != null) {
                webViewActivity.tabsState.currentFrame = webTabItemD.webFrame
                webViewActivity.currentPage = pageCreate
            }
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ReloadViewsStep())
    }
}

class WvaReloadLayoutTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class FinishReloadStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val tabViewListener = webViewActivity.tabsState.tabViewListener
            webViewActivity.tabsState.tabViewListener = null
            val webNestFrame = webViewActivity.tabsState.currentFrame
            if (webNestFrame != null && webViewActivity.currentPage != null) {
                webNestFrame.clearBackdrop()
                if ((webNestFrame.swipeOffset).compareTo(0.0f) != 0 || (webNestFrame.pageX).compareTo(0.0f) != 0
                ) {
                    webNestFrame.swipeOffset = 0.0f
                    webNestFrame.pageX = 0.0f
                    webNestFrame.invalidate()
                }
                webViewActivity.tabsState.currentFrame!!.syncAddPage(false)
                webViewActivity.tabsState.currentFrame!!.setVisibility(View.VISIBLE)
                webViewActivity.loadPageUrl(webViewActivity.pageUrlForTab(), false, true)
                webViewActivity.checkPageLanguage(true)
                webViewActivity.updateTabCountIcons()
                webViewActivity.tabsState.goBackAnimBusy = false
            }
            if (tabViewListener != null) {
                tabViewListener.onTabView()
            }
            webViewActivity.tabsState.reloadTabViewsBusy = false
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.tabsState.currentFrame != null && webViewActivity.currentPage != null) {
            webViewActivity.layoutTabFrames()
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(FinishReloadStep())
    }
}

class WvaTabUndoSnackbar(@JvmField val wa: WebViewActivity, @JvmField val undoFrame: com.mycompany.app.web.WebNestFrame? = null) : Runnable {

    internal inner class UndoSnackbarListener : SnackbarListener {

        internal inner class RestoreClosedTabStep : Runnable {
            override fun run() {
                val webViewActivity = wa
                val webNestFrame = webViewActivity.tabsState.tabUndoSnackbarFrame
                webViewActivity.tabsState.tabUndoSnackbarFrame = null
                val pageCreate: WebNestView? = if (webNestFrame == null || !webNestFrame.alive) null else webNestFrame.pageCreate
                if (webNestFrame == null || !webNestFrame.alive || pageCreate == null) {
                    return
                }
                webNestFrame.resetOffsets()
                webNestFrame.syncAddPage(true)
                webNestFrame.setVisibility(View.VISIBLE)
                val backItem = webNestFrame.backItem
                if (backItem == null || backItem.groupId == 0L) {
                    webViewActivity.addTabPlain(webNestFrame, pageCreate.getUrl(), true, null)
                } else {
                    webViewActivity.addTabInGroup(webNestFrame, pageCreate.getUrl(), true, null)
                }
                val webTabBarSubView = webViewActivity.tabBarSubView
                if (webTabBarSubView != null) {
                    webTabBarSubView.post(WvaTabUndoSnackbarTask(webViewActivity))
                }
            }
        }

        override fun onSecond() {
        }

        override fun onDismissed() {
            val webTabBarSubView = wa.tabBarSubView
            if (webTabBarSubView != null) {
                webTabBarSubView.closedItem = null
            }
        }

        override fun onFirst() {
            val webViewActivity = wa
            val mySnackbar = webViewActivity.dialogsState.tabUndoSnackbar
            val snackItem: SnackItem? = if (mySnackbar == null) null else mySnackbar.snackItem
            if (mySnackbar == null || snackItem == null) {
                return
            }
            webViewActivity.tabsState.tabUndoSnackbarFrame = snackItem.frame
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(RestoreClosedTabStep())
        }

        override fun onDismiss() {
            wa.dialogsState.tabUndoSnackbar = null
        }
    }

    override fun run() {
        val viewGroup: ViewGroup?
        val webViewActivity = wa
        val webNestFrame = this@WvaTabUndoSnackbar.undoFrame
        if (webNestFrame == null) {
            return
        }
        val snackItem = SnackItem()
        snackItem.frame = webNestFrame
        var attached = false
        webViewActivity.updateUpButton(false)
        webViewActivity.closeAllSnackbars()
        val webTabBarSubView = webViewActivity.tabBarSubView
        if (webTabBarSubView == null) {
            viewGroup = webViewActivity.webCoord
        } else {
            val myTabFrame = webTabBarSubView.popupFrame
            if (if (myTabFrame == null) false else !myTabFrame.isClosed) {
                attached = true
                viewGroup = webTabBarSubView
            } else {
                viewGroup = webViewActivity.webCoord
            }
        }
        val snackbarParent = viewGroup
        val mySnackbar = MySnackbar(webViewActivity)
        webViewActivity.dialogsState.tabUndoSnackbar = mySnackbar
        mySnackbar.setAppBarView(webViewActivity.barScroll)
        if (attached) {
            webViewActivity.dialogsState.tabUndoSnackbar!!.setElevation(MainApp.dp16.toFloat())
        }
        webViewActivity.dialogsState.tabUndoSnackbar!!.snackItem = snackItem
        val tabUndoSnackbar = webViewActivity.dialogsState.tabUndoSnackbar
        val undelete = AppR.string.undelete
        val undoSnackbarListener: UndoSnackbarListener = UndoSnackbarListener()
        tabUndoSnackbar!!.snackType = 2.toByte()
        tabUndoSnackbar.show(snackbarParent, 0, undelete, 0, undoSnackbarListener)
    }
}

class WvaTabUndoSnackbarTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webTabBarSubView = wa.tabBarSubView
        val list: MutableList<WebTabItem?>? = if (webTabBarSubView == null) null else webTabBarSubView.groupTabs
        val webTabItem: WebTabItem? = if (webTabBarSubView == null || list == null) null else webTabBarSubView.closedItem
        if (webTabBarSubView == null || list == null || webTabItem == null) {
            return
        }
        try {
            val closedPos = webTabBarSubView.closedPos
            webTabBarSubView.groupSelected = closedPos
            list.add(closedPos, webTabItem)
            webTabBarSubView.renumberTabs()
            webTabBarSubView.tabAdapter!!.applyTint(
                webTabBarSubView.groupTabs,
                webTabBarSubView.groupSelected,
                webTabBarSubView.onStartPage,
                webTabBarSubView.tintColor,
                webTabBarSubView.tintColor2,
                true
            )
            webTabBarSubView.tabBar!!.postDelayed(webTabBarSubView.ScrollToGroupTask(), 300L)
        } catch (unused: Exception) {
        }
    }
}

class WvaPreviewPrevTabTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class RevealPrevTabStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestFrame = webViewActivity.swipeTabFrame
            if (webNestFrame == null) {
                return
            }
            webViewActivity.revealPrevTab(webNestFrame.tabY, true)
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.revealPrevTab(0.0f, false)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(RevealPrevTabStep())
    }
}

class WvaFinishPageChangeTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class RestoreDeskModeStep : Runnable {

        internal inner class RestorePageItemStep : Runnable {

            internal inner class ReloadRestoredPageStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    val restoreDeskMode = webViewActivity.barsState.restoreDeskMode
                    val restoredPageUrl = webViewActivity.dialogsState.restoredPageUrl
                    webViewActivity.dialogsState.restoredPageUrl = null
                    val webNestView: WebNestView? = if (webViewActivity.tabsState.currentFrame == null) null else webViewActivity.currentPage
                    if (webViewActivity.tabsState.currentFrame == null || webNestView == null) {
                        webViewActivity.tabsState.pendingDeskMode = restoreDeskMode
                        webViewActivity.dialogsState.pendingPageUrl = restoredPageUrl
                        val myWebCoord = webViewActivity.webCoord
                        if (myWebCoord == null) {
                            return
                        }
                        myWebCoord.post(WvaFinishPageResumeTask(webViewActivity))
                        return
                    }
                    if (!webNestView.hasSetting) {
                        webViewActivity.applyWebSettings(webNestView, 0)
                        val myWebCoord = webViewActivity.webCoord
                        if (myWebCoord != null) {
                            myWebCoord.post(WvaFinishPageDeskTask(webViewActivity, deskMode = restoreDeskMode, pendingUrl = restoredPageUrl))
                            return
                        }
                        return
                    }
                    webNestView.setWebViewClient(WvaLocalWebViewClient(webViewActivity))
                    webViewActivity.currentPage!!.setWebChromeClient(WvaLocalChromeClient(webViewActivity))
                    webViewActivity.currentPage!!.setDownloadListener(PageDownloadListener(webViewActivity))
                    webViewActivity.tabsState.pendingDeskMode = restoreDeskMode
                    webViewActivity.dialogsState.pendingPageUrl = restoredPageUrl
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord != null) {
                        myWebCoord.post(WvaFinishPageResumeTask(webViewActivity))
                    }
                }
            }

            override fun run() {
                val wvaFinishPageChangeTask = this@WvaFinishPageChangeTask
                val webViewActivity = wa
                webViewActivity.restorePageItem()
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(ReloadRestoredPageStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            WebViewActivity.pauseWebView(webViewActivity.currentPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(RestorePageItemStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        WebViewActivity.pauseMedia(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(RestoreDeskModeStep())
    }
}

class WvaFinishPageDeskTask(@JvmField val wa: WebViewActivity, @JvmField val deskMode: Boolean = false, @JvmField val pendingUrl: String? = null) : Runnable {

    internal inner class ApplyDeskModeStep : Runnable {

        internal inner class DeskListenersStep : Runnable {

            internal inner class LoadDeskPageStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    val deskMode = this@WvaFinishPageDeskTask.deskMode
                    val pendingUrl = this@WvaFinishPageDeskTask.pendingUrl
                    webViewActivity.tabsState.pendingDeskMode = deskMode
                    webViewActivity.dialogsState.pendingPageUrl = pendingUrl
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(WvaFinishPageResumeTask(webViewActivity))
                }
            }

            override fun run() {
                val wvaFinishPageDeskTask = this@WvaFinishPageDeskTask
                val webViewActivity = wa
                webViewActivity.attachPageListeners(webViewActivity.tabsState.currentFrame, webViewActivity.currentPage, 0)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(LoadDeskPageStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            WebViewActivity.applyDarkSettings(webViewActivity.currentPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(DeskListenersStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.addJsBridge(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ApplyDeskModeStep())
    }
}

class WvaGoBackTask(@JvmField val wa: WebViewActivity, @JvmField val fromEdgeGesture: Boolean = false, @JvmField val canGoBack: Boolean = false, @JvmField val fromKey: Boolean = false, @JvmField val prevBackListener: com.mycompany.app.web.WebViewActivity.PrevBackListener? = null, @JvmField val startBackImplBackListener: com.mycompany.app.web.WebViewActivity.PrevPageListener? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val fromEdgeGesture = this@WvaGoBackTask.fromEdgeGesture
        val canGoBack = this@WvaGoBackTask.canGoBack
        val fromKey = this@WvaGoBackTask.fromKey
        val prevBackListener = this@WvaGoBackTask.prevBackListener
        val prevPageListener = this@WvaGoBackTask.startBackImplBackListener
        val webNestFrame = webViewActivity.tabsState.currentFrame
        val webNestView: WebNestView? = if (webNestFrame == null) null else webViewActivity.currentPage
        if (webNestFrame == null || webNestView == null) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            if (prevBackListener != null) {
                prevBackListener.onBack(false)
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        if (ZtwoSettings.addPage.value && !canGoBack) {
            val list = webNestFrame.pageItems
            if (list != null && list.size >= 2 && webNestFrame.currentPageIndex > 0) {
                webViewActivity.tabsState.goBackAnimBusy = false
                webViewActivity.finishPageChange(4, true, prevPageListener)
                if (prevBackListener != null) {
                    prevBackListener.onBack(true)
                    return
                }
                return
            }
        } else if (canGoBack) {
            webViewActivity.tabsState.goBackAnimBusy = false
            webNestView.goBack()
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            if (prevBackListener != null) {
                prevBackListener.onBack(true)
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        if (MainSettings.lastNoti.value && !webViewActivity.tabsState.goBackAnimBusy) {
            if (MainSettings.doubleBack.value) {
                val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
                if (if (webTabItemD == null) false else webTabItemD.reloadPending) {
                    webViewActivity.dialogsState.exitOnBackArmed = true
                    if (prevPageListener != null) {
                        prevPageListener.onDone()
                    }
                    if (prevBackListener != null) {
                        prevBackListener.onBack(false)
                    }
                    webViewActivity.tabsState.goBackPageBusy = false
                    return
                }
                if (webViewActivity.otherTabItemAt(webViewActivity.currentTabIndex) == null) {
                    if (prevPageListener != null) {
                        prevPageListener.onDone()
                    }
                    if (prevBackListener != null) {
                        prevBackListener.onBack(false)
                    }
                    webViewActivity.tabsState.goBackPageBusy = false
                    return
                }
            }
            webViewActivity.tabsState.goBackAnimBusy = true
            showToast(webViewActivity, AppR.string.last_page_noti)
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            if (prevBackListener != null) {
                prevBackListener.onBack(true)
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
        if (if (webTabItemD == null) false else webTabItemD.reloadPending) {
            webViewActivity.dialogsState.exitOnBackArmed = true
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            if (prevBackListener != null) {
                prevBackListener.onBack(false)
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        if (TtsSettings.lastExit.value) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            if (prevBackListener != null) {
                prevBackListener.onBack(false)
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        val webTabItemW = webViewActivity.otherTabItemAt(webViewActivity.currentTabIndex)
        if (webTabItemW == null) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            if (prevBackListener != null) {
                prevBackListener.onBack(false)
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        if (webViewActivity.currentPage == null) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            webViewActivity.tabsState.goBackPageBusy = false
        } else if (!fromEdgeGesture || webViewActivity.hasPrevTab()) {
            webViewActivity.finishProgress()
            webViewActivity.exitFind(true)
            if (!fromKey || !AlbumSettings.tabAnim.value) {
                webViewActivity.closeTabPage(webTabItemW, prevPageListener)
            } else if (webViewActivity.tabsState.tabSwipeState.toInt() != 0) {
                if (prevPageListener != null) {
                    prevPageListener.onDone()
                }
                webViewActivity.tabsState.goBackPageBusy = false
            } else {
                webViewActivity.dragPrevTab(
                    if (webViewActivity.tabsState.layoutRtl) webViewActivity.currentPage!!.getWidth() - 0.5f else 0.5f,
                    2
                )
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.post(WvaGoBackPageTask(webViewActivity, goBackTaskTabItem = webTabItemW, goBackTaskBackListener = prevPageListener))
                }
            }
        } else {
            if (MainSettings.lastNoti.value) {
                showToast(webViewActivity, AppR.string.last_page_noti)
            }
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            webViewActivity.tabsState.goBackPageBusy = false
        }
        if (prevBackListener != null) {
            prevBackListener.onBack(true)
        }
    }
}

class WvaGoBackPageTask(@JvmField val wa: WebViewActivity, @JvmField val goBackTaskTabItem: WebTabItem? = null, @JvmField val goBackTaskBackListener: com.mycompany.app.web.WebViewActivity.PrevPageListener? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaGoBackPageTask.goBackTaskTabItem
        val prevPageListener = this@WvaGoBackPageTask.goBackTaskBackListener
        webViewActivity.animateTabSwipe(2, true)
        if (webViewActivity.tabsState.tabSwipeState.toInt() == 0) {
            webViewActivity.closeTabPage(webTabItem, prevPageListener)
            return
        }
        val webNestView = webViewActivity.currentPage
        webNestView!!.windowClosed = true
        evalJs(webNestView, "window.close();", false)
        if (prevPageListener != null) {
            prevPageListener.onDone()
        }
        webViewActivity.tabsState.goBackPageBusy = false
    }
}

class WvaCloseTabPageTask(@JvmField val wa: WebViewActivity, @JvmField val closeTabPageTabItem: WebTabItem? = null, @JvmField val closeTabPageBackListener: com.mycompany.app.web.WebViewActivity.PrevPageListener? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaCloseTabPageTask.closeTabPageTabItem
        val prevPageListener = this@WvaCloseTabPageTask.closeTabPageBackListener
        val webNestView: WebNestView? = if (webTabItem == null) null else webViewActivity.currentPage
        if (webTabItem == null || webNestView == null) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        webNestView.windowClosed = true
        evalJs(webNestView, "window.close();", false)
        webViewActivity.stopWebClean()
        webViewActivity.hidePageOverlays(true)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCloseTabPauseMediaTask(webViewActivity, closeTabPageTaskTabItem = webTabItem, closeTabPageTaskBackListener = prevPageListener))
    }
}

class WvaCloseTabPauseMediaTask(@JvmField val wa: WebViewActivity, @JvmField val closeTabPageTaskTabItem: WebTabItem? = null, @JvmField val closeTabPageTaskBackListener: com.mycompany.app.web.WebViewActivity.PrevPageListener? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaCloseTabPauseMediaTask.closeTabPageTaskTabItem
        val prevPageListener = this@WvaCloseTabPauseMediaTask.closeTabPageTaskBackListener
        if (webTabItem == null || webViewActivity.tabsState.currentFrame == null) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        WebViewActivity.pauseMedia(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCloseTabPauseWebTask(webViewActivity, closeTabPageTask2TabItem = webTabItem, closeTabPageTask2BackListener = prevPageListener))
    }
}

class WvaCloseTabPauseWebTask(@JvmField val wa: WebViewActivity, @JvmField val closeTabPageTask2TabItem: WebTabItem? = null, @JvmField val closeTabPageTask2BackListener: com.mycompany.app.web.WebViewActivity.PrevPageListener? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaCloseTabPauseWebTask.closeTabPageTask2TabItem
        val prevPageListener = this@WvaCloseTabPauseWebTask.closeTabPageTask2BackListener
        if (webTabItem == null || webViewActivity.tabsState.currentFrame == null) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        WebViewActivity.pauseWebView(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCloseTabDestroyFrameTask(webViewActivity, closeTabPageTask3TabItem = webTabItem, closeTabPageTask3BackListener = prevPageListener))
    }
}

class WvaCloseTabDestroyFrameTask(@JvmField val wa: WebViewActivity, @JvmField val closeTabPageTask3TabItem: WebTabItem? = null, @JvmField val closeTabPageTask3BackListener: com.mycompany.app.web.WebViewActivity.PrevPageListener? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaCloseTabDestroyFrameTask.closeTabPageTask3TabItem
        val prevPageListener = this@WvaCloseTabDestroyFrameTask.closeTabPageTask3BackListener
        var frame: WebNestFrame? = null
        val webNestFrame: WebNestFrame? = if (webTabItem == null) null else webViewActivity.tabsState.currentFrame
        if (webTabItem == null || webNestFrame == null) {
            if (prevPageListener != null) {
                prevPageListener.onDone()
            }
            webViewActivity.tabsState.goBackPageBusy = false
            return
        }
        detachFromParent(webNestFrame)
        val tabRestore = BrowserSettings.tabRestore.value
        val currentFrame = webViewActivity.tabsState.currentFrame
        if (tabRestore) {
            currentFrame!!.backItem = webTabItem
            frame = currentFrame
        } else {
            currentFrame!!.destroyFrame(null)
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCloseTabRemoveTask(webViewActivity, closeTabPageTask4BackListener = prevPageListener, closingFrame = frame))
    }
}

class WvaCloseTabRemoveTask(@JvmField val wa: WebViewActivity, @JvmField val closeTabPageTask4BackListener: com.mycompany.app.web.WebViewActivity.PrevPageListener? = null, @JvmField val closingFrame: com.mycompany.app.web.WebNestFrame? = null) : Runnable {

    internal inner class RemoveClosedTabStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val prevPageListener = this@WvaCloseTabRemoveTask.closeTabPageTask4BackListener
            val webNestFrame = this@WvaCloseTabRemoveTask.closingFrame
            webViewActivity.removeTabAt(webViewActivity.currentTabIndex)
            webViewActivity.tabsState.closingBackListener = prevPageListener
            webViewActivity.tabsState.closingFrame = webNestFrame
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaCloseTabReloadViewsTask(webViewActivity))
        }
    }

    override fun run() {
        wa.runInBackground(RemoveClosedTabStep())
    }
}

class TabRemovedBackListener(@JvmField val wa: WebViewActivity) : WebViewActivity.TabViewListener {
    override fun onTabView() {
        val webViewActivity = wa
        val prevPageListener = webViewActivity.tabsState.closingBackListener
        val webNestFrame = webViewActivity.tabsState.closingFrame
        webViewActivity.tabsState.closingBackListener = null
        webViewActivity.tabsState.closingFrame = null
        if (webNestFrame != null) {
            webViewActivity.offerTabUndo(webNestFrame)
        }
        webViewActivity.refreshTabBar(1, true)
        if (prevPageListener != null) {
            prevPageListener.onDone()
        }
    }
}

class WvaGoForwardFinish(@JvmField val wa: WebViewActivity, @JvmField val goForwardStartFlag: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val goForwardStartFlag = this@WvaGoForwardFinish.goForwardStartFlag
        val webNestFrame = webViewActivity.tabsState.currentFrame
        val webNestView: WebNestView? = if (webNestFrame == null) null else webViewActivity.currentPage
        if (webNestFrame == null || webNestView == null) {
            webViewActivity.tabsState.startGoForwardBusy = false
            return
        }
        if (ZtwoSettings.addPage.value && !goForwardStartFlag) {
            val list = webNestFrame.pageItems
            val size = if (list != null) list.size else 0
            if (list != null && size >= 2 && webNestFrame.currentPageIndex < size - 1) {
                webViewActivity.tabsState.goBackAnimBusy = false
                webViewActivity.finishPageChange(4, false, null)
            }
        } else if (goForwardStartFlag) {
            webViewActivity.tabsState.goBackAnimBusy = false
            webNestView.goForward()
        }
        webViewActivity.tabsState.startGoForwardBusy = false
    }
}

class WvaTabBarListener(@JvmField val wa: WebViewActivity) : WebTabBar.Listener {

    internal inner class ClampTabIndexListener : WebViewActivity.TabViewListener {
        override fun onTabView() {
            val webViewActivity = wa
            webViewActivity.clampTabIndex()
        }
    }

    override fun onListChanged() {
        val webViewActivity = wa
        val webTabBarAdapter = webViewActivity.tabBar
        if (webTabBarAdapter != null) {
            try {
                webTabBarAdapter.applyList(webViewActivity.currentTabIndex, webViewActivity.tabsState.tabItems)
            } catch (unused: Exception) {
            }
        }
    }

    override fun onCloseTab(position: Int, view: View?, isGroup: Boolean) {
        val webViewActivity = wa
        if (!isGroup) {
            if (webViewActivity.tabsState.currentFrame == null || webViewActivity.isBusy() || webViewActivity.tabsState.tabBarBusy) {
                return
            }
            if (position == webViewActivity.currentTabIndex) {
                webViewActivity.runMenuItem(null, 38)
                return
            } else {
                webViewActivity.switchToTab(position)
                return
            }
        }
        if (webViewActivity.tabBarItemMenu == null) {
            webViewActivity.closeTabBarItemMenu()
            if (view != null) {
                val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                arrayList.add(PopMenuItem(0, AppR.string.edit))
                arrayList.add(PopMenuItem(1, AppR.string.delete))
                val myPopupMenu = MyPopupMenu(
                    webViewActivity,
                    webViewActivity.dialogsState.webBody,
                    view,
                    arrayList,
                    MainUtil.isDarkPage(webViewActivity.onStartPage),
                    WvaTabBarItemMenuListener(webViewActivity, position)
                )
                webViewActivity.tabBarItemMenu = myPopupMenu
                webViewActivity.popupMenu = myPopupMenu
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.post(WvaTabBarItemMenuClosed(webViewActivity))
                }
            }
        }
    }

    override fun onTabLongPress(
        view: View?,
        position: Int,
        dragFrom: Int,
        isGroup: Boolean
    ) {
        val webViewActivity = wa
        webViewActivity.showTabMenu(view, -1, position, isGroup)
        webViewActivity.tabsState.tabMenuDragFrom = dragFrom
        webViewActivity.tabDragStart = dragFrom
    }

    override fun onTabClick(x: Int, position: Int, width: Int, isGroup: Boolean) {
        val webViewActivity = wa
        if (!isGroup) {
            webViewActivity.selectTab(position, true)
            return
        }
        if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
            return
        }
        webViewActivity.removeTabBarSubView()
        if (webViewActivity.tabBar != null) {
            val themeColor = webViewActivity.pageThemeColor()
            val themeLight = webViewActivity.pageThemeLight()
            val webTabBarSubView = WebTabBarSubView(webViewActivity)
            webTabBarSubView.hostContext = webViewActivity
            webViewActivity.tabBarSubView = webTabBarSubView
            webTabBarSubView.open(
                webViewActivity.dialogsState.webBody,
                x,
                webViewActivity.tabsState.tabItems,
                webViewActivity.currentTabIndex,
                position,
                width,
                webViewActivity.onStartPage,
                themeColor,
                themeLight,
                WvaTabBarSubListener(webViewActivity)
            )
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(WvaShowTabBarSubView(webViewActivity))
        }
    }

    override fun onTabsMerged(position: Int, arrayList: ArrayList<WebTabItem?>?) {
        val webViewActivity = wa
        if (arrayList == null || arrayList.isEmpty()) {
            webViewActivity.rebuildTabs(false)
        } else {
            webViewActivity.tabsState.tabItems = arrayList
            webViewActivity.currentTabIndex = position
            webViewActivity.reloadTabViews(ClampTabIndexListener())
        }
    }
}

class WvaTabDragListener(@JvmField val wa: WebViewActivity) : TabDragListener {

    internal inner class FinishDragStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.tabsState.tabDragMoved) {
                webViewActivity.tabsState.tabDragMoved = false
                val webTabBarAdapter = webViewActivity.tabBar
                if (webTabBarAdapter == null) {
                    return
                }
                if (webViewActivity.tabsState.tabMenuDragFrom != webViewActivity.tabDragStart) {
                    webTabBarAdapter.rebuild(true)
                } else {
                    webTabBarAdapter.redrawAll()
                }
            }
        }
    }

    override fun onDragState(state: Int) {
        var myWebCoord: MyWebCoord? = null
        val webViewActivity = wa
        if (state == 1) {
            webViewActivity.tabsState.tabDragMoved = false
            webViewActivity.tabsState.tabDragging = true
        } else if (state == 2) {
            webViewActivity.tabsState.tabDragMoved = true
            webViewActivity.tabsState.tabDragging = false
        } else if (state == 0 && webViewActivity.tabsState.tabDragMoved && (webViewActivity.webCoord.also {
                myWebCoord = it
            }) != null) {
            myWebCoord!!.post(FinishDragStep())
        }
    }

    override fun onMove(from: Int, to: Int): Boolean {
        val webViewActivity = wa
        val webTabBarAdapter = webViewActivity.tabBar
        if (webTabBarAdapter == null) {
            return false
        }
        webViewActivity.tabDragStart = to
        return webTabBarAdapter.moveItem(from, to)
    }

    override fun onMerge(from: Int, to: Int) {
        var size: Int = 0
        var to = to
        var webTabItemB: WebTabItem? = null
        var groupHead: WebTabItem? = null
        val arrayList: ArrayList<WebTabItem?>?
        val groupMembers: ArrayList<WebTabItem?>?
        val webTabItemE: WebTabItem?
        val groupSize: Int
        var groupIndex: Int
        var webTabItem: WebTabItem
        var shownItem: WebTabItem?
        val webViewActivity = wa
        val webTabBarAdapter = webViewActivity.tabBar
        if (webTabBarAdapter != null) {
            val myRecyclerView = webViewActivity.tabBarView
            val tabItems = webTabBarAdapter.tabItems
            if (tabItems != null && from >= 0 && to >= 0 && from < (tabItems.size.also {
                    size = it
                }) && to < size && (webTabBarAdapter.itemAt(from)
                    .also { webTabItemB = it }) != null && (webTabBarAdapter.itemAt(to)
                    .also { groupHead = it }) != null
            ) {
                val now = System.currentTimeMillis()
                val groupTabs = groupHead!!.groupTabs
                val tabItemsNow = webTabBarAdapter.tabItems
                if (groupTabs != null) {
                    tabItemsNow!!.remove(webTabItemB)
                    val firstChild = groupHead.groupTabs!!.get(0)
                    if (firstChild == null) {
                        webTabBarAdapter.rebuild(false)
                        if (myRecyclerView != null) {
                            myRecyclerView.post(webTabBarAdapter.NotifyAllTabsTask())
                        }
                    } else {
                        val groupTabsNow = webTabItemB!!.groupTabs
                        if (groupTabsNow != null) {
                            val groupCount = groupTabsNow.size
                            var index = 0
                            while (index < groupCount) {
                                val entry: Any? = groupTabsNow.get(index)
                                index++
                                val tab = entry as WebTabItem
                                val shownAt = webTabBarAdapter.shownAt(tab.position)
                                val shownFirst: WebTabItem? = if (shownAt != null) webTabBarAdapter.shownAt(firstChild.position) else null
                                if (shownAt != null && shownFirst != null) {
                                    shownAt.groupId = shownFirst.groupId
                                    shownAt.groupName = shownFirst.groupName
                                    shownAt.groupColor = shownFirst.groupColor
                                    tab.inGroup = true
                                    groupHead.groupTabs!!.add(tab)
                                }
                            }
                        } else {
                            val shownAt = webTabBarAdapter.shownAt(webTabItemB.position)
                            val shownFirst: WebTabItem? = if (shownAt != null) webTabBarAdapter.shownAt(firstChild.position) else null
                            if (shownAt != null && shownFirst != null) {
                                shownAt.groupId = shownFirst.groupId
                                shownAt.groupName = shownFirst.groupName
                                shownAt.groupColor = shownFirst.groupColor
                                webTabItemB.inGroup = true
                                groupHead.groupTabs!!.add(webTabItemB)
                            }
                        }
                        webTabBarAdapter.rebuild(false)
                        if (myRecyclerView != null) {
                            myRecyclerView.post(webTabBarAdapter.NotifyAllTabsTask())
                        }
                    }
                } else {
                    tabItemsNow!!.remove(groupHead)
                    webTabBarAdapter.tabItems!!.remove(webTabItemB)
                    val shownAt = webTabBarAdapter.shownAt(groupHead.position)
                    if (shownAt != null) {
                        shownAt.groupId = now
                        groupHead.inGroup = true
                        val groupTabsNow = webTabItemB!!.groupTabs
                        val firstTab: WebTabItem? = if (groupTabsNow == null) null else (groupTabsNow.get(0))
                        if (groupTabsNow == null || firstTab == null) {
                            arrayList = ArrayList()
                            arrayList.add(groupHead)
                            groupMembers = webTabItemB.groupTabs
                            if (groupMembers != null) {
                                groupSize = groupMembers.size
                                groupIndex = 0
                                while (groupIndex < groupSize) {
                                    val otherEntry: Any? = groupMembers.get(groupIndex)
                                    groupIndex++
                                    webTabItem = otherEntry as WebTabItem
                                    shownItem = webTabBarAdapter.shownAt(webTabItem.position)
                                    if (shownItem != null) {
                                        shownItem.groupId = now
                                        webTabItem.inGroup = true
                                        arrayList.add(webTabItem)
                                    }
                                }
                            } else {
                                webTabItemE = webTabBarAdapter.shownAt(webTabItemB.position)
                                if (webTabItemE != null) {
                                    webTabItemE.groupId = now
                                    webTabItemB.inGroup = true
                                    arrayList.add(webTabItemB)
                                }
                            }
                            if (from < to) {
                                to--
                            }
                            val copy = WebTabItem()
                            copy.groupTabs = arrayList
                            webTabBarAdapter.tabItems!!.add(to, copy)
                            webTabBarAdapter.rebuild(false)
                            if (myRecyclerView != null) {
                                myRecyclerView.post(webTabBarAdapter.NotifyAllTabsTask())
                            }
                        } else {
                            val shownFirstTab = webTabBarAdapter.shownAt(firstTab.position)
                            if (shownFirstTab != null) {
                                shownAt.groupName = shownFirstTab.groupName
                                shownAt.groupColor = shownFirstTab.groupColor
                                arrayList = ArrayList()
                                arrayList.add(groupHead)
                                groupMembers = webTabItemB.groupTabs
                                if (groupMembers != null) {
                                    groupSize = groupMembers.size
                                    groupIndex = 0
                                    while (groupIndex < groupSize) {
                                        val nextEntry: Any? = groupMembers.get(groupIndex)
                                        groupIndex++
                                        webTabItem = nextEntry as WebTabItem
                                        shownItem = webTabBarAdapter.shownAt(webTabItem.position)
                                        if (shownItem != null) {
                                            shownItem.groupId = now
                                            webTabItem.inGroup = true
                                            arrayList.add(webTabItem)
                                        }
                                    }
                                } else {
                                    webTabItemE = webTabBarAdapter.shownAt(webTabItemB.position)
                                    if (webTabItemE != null) {
                                        webTabItemE.groupId = now
                                        webTabItemB.inGroup = true
                                        arrayList.add(webTabItemB)
                                    }
                                }
                                if (from < to) {
                                    to--
                                }
                                val otherCopy = WebTabItem()
                                otherCopy.groupTabs = arrayList
                                webTabBarAdapter.tabItems!!.add(to, otherCopy)
                                webTabBarAdapter.rebuild(false)
                                if (myRecyclerView != null) {
                                    myRecyclerView.post(webTabBarAdapter.NotifyAllTabsTask())
                                }
                            }
                        }
                    }
                }
            }
        }
        webViewActivity.tabsState.tabDragMoved = false
    }

    override fun onSwiped(position: Int) {
        val webViewActivity = wa
        if (webViewActivity.tabsState.tabDragging) {
            webViewActivity.tabsState.tabDragging = false
            val webTabBarAdapter = webViewActivity.tabBar
            if (webTabBarAdapter != null) {
                val touchTabIndex = webViewActivity.tabsState.touchTabIndex
                var tabIndex = -1
                if (touchTabIndex != -1) {
                    webViewActivity.onTabBarItemClick(touchTabIndex, position)
                    return
                }
                val webTabItemB = webTabBarAdapter.itemAt(position)
                if (webTabItemB != null) {
                    tabIndex =
                        if (webTabItemB.groupTabs != null) WebTabBar.groupColor(webTabItemB) + 10000 else webTabItemB.position
                }
                webViewActivity.onTabBarItemClick(tabIndex, position)
            }
        }
    }

    override fun onSwipePos(state: Int) {
        wa.tabsState.dragTabIndex = state
    }
}

class WvaTabBarScrollToCurrent(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabBarAdapter = webViewActivity.tabBar
        val barPos = if (webTabBarAdapter == null) 0 else webTabBarAdapter.barPosOf(webViewActivity.currentTabIndex)
        if (webTabBarAdapter == null || barPos == -1) {
            return
        }
        webViewActivity.tabBarView!!.scrollTo(barPos)
    }
}

class WvaTabAddClick(@JvmField val wa: WebViewActivity) : View.OnClickListener {

    internal inner class TabAddedListener : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            val webViewActivity = wa
            webViewActivity.onTabAdded(isAdded)
        }
    }

    override fun onClick(view: View?) {
        val webViewActivity = wa
        if (webViewActivity.tabsState.currentFrame == null || webViewActivity.isBusy()) {
            return
        }
        webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), true, TabAddedListener())
    }
}

class WvaTabBarScrollTo(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val position: Int, modeParam: Int
) : Runnable {
    val mode: Byte

    init {
        this.mode = modeParam.toByte()
    }

    override fun run() {
        val webViewActivity = this.webViewActivity
        val webTabBarAdapter = webViewActivity.tabBar
        val barPos = if (webTabBarAdapter == null) 0 else webTabBarAdapter.barPosOf(this.position)
        if (webTabBarAdapter == null || barPos == -1) {
            return
        }
        if (this.mode.toInt() != 1) {
            webViewActivity.tabBarView!!.scrollTo(barPos)
            return
        }
        val arrayList = webViewActivity.tabsState.tabItems
        val size = if (arrayList != null) arrayList.size else 0
        val tabBarScrollIndex = webViewActivity.dialogsState.tabBarScrollIndex
        if ((tabBarScrollIndex == 0 && webViewActivity.currentTabIndex == size - 1) || (webViewActivity.currentTabIndex == 0 && tabBarScrollIndex == size - 1)) {
            webViewActivity.tabBarView!!.scrollTo(barPos)
        } else {
            webViewActivity.tabBarView!!.smoothScrollTo(barPos)
        }
    }
}

class WvaTabBarChangeListener(@JvmField val wa: WebViewActivity) : WebTabBar.ChangeListener {

    internal inner class RefreshTabBarStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.refreshTabBar(0, true)
            webViewActivity.tabsState.tabBarBusy = false
        }
    }

    override fun onChanged() {
        val myRecyclerView = wa.tabBarView
        if (myRecyclerView == null) {
            return
        }
        myRecyclerView.postDelayed(RefreshTabBarStep(), 300L)
    }
}

class WvaTabBarItemRefresh(@JvmField val wa: WebViewActivity, @JvmField val targetIndex: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val targetIndex = this@WvaTabBarItemRefresh.targetIndex
        val webTabBarAdapter = webViewActivity.tabBar
        if (webTabBarAdapter == null) {
            return
        }
        if (targetIndex != -1) {
            webTabBarAdapter.notifyChanged(targetIndex)
        } else {
            webTabBarAdapter.notifyAllTabs()
        }
    }
}

class WvaTabBarSubListener(@JvmField val wa: WebViewActivity) : TabBarSubListener {
    override fun activity(): WebViewActivity {
        return wa
    }

    internal inner class SubTabAddedListener : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            val webViewActivity = wa
            webViewActivity.onTabAdded(isAdded)
        }
    }

    internal inner class CloseSubViewTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.closeTabBarSubView()
        }
    }

    fun onTabPicked(currentTabIndex: Int, refresh: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.tabsState.currentFrame == null || webViewActivity.isBusy()) {
            return
        }
        webViewActivity.currentTabIndex = currentTabIndex
        val webTabBarAdapter: WebTabBar? = if (refresh) webViewActivity.tabBar else null
        if (refresh && webTabBarAdapter != null) {
            webTabBarAdapter.selectedPos = webTabBarAdapter.barPosOf(currentTabIndex)
            webTabBarAdapter.shownIndex = currentTabIndex
        }
        webViewActivity.closeTabBarSubView()
        webViewActivity.openInNewTab(WebViewActivity.newTabUrl(), SubTabAddedListener())
    }

    fun onTabChecked(index: Int, last: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.tabsState.currentFrame == null || webViewActivity.isBusy() || webViewActivity.tabsState.tabBarBusy) {
            return
        }
        if (last) {
            webViewActivity.closeTabBarSubView()
        }
        if (index == webViewActivity.currentTabIndex) {
            webViewActivity.runMenuItem(null, 38)
        } else {
            webViewActivity.switchToTab(index)
        }
    }
}

class WvaShowTabBarSubView(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val myWebBody = webViewActivity.dialogsState.webBody
        val webTabBarSubView: WebTabBarSubView? = if (myWebBody == null) null else webViewActivity.tabBarSubView
        if (myWebBody == null || webTabBarSubView == null) {
            return
        }
        wa.floatingState.overlays!!.addView(webTabBarSubView, -1, -1)
        val tabBarSubView = webViewActivity.tabBarSubView
        if (tabBarSubView!!.popupFrame != null) {
            tabBarSubView.post(tabBarSubView.SetOpenTask())
        }
    }
}

class WvaPageFrameLoadTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class FrameShownStep : Runnable {

        internal inner class FrameListenersStep : Runnable {

            internal inner class LoadFramePageStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    if (webViewActivity.currentPage == null) {
                        webViewActivity.videoState.pageFrameBusy = false
                        return
                    }
                    webViewActivity.loadPageUrl(webViewActivity.dialogsState.pageUrl, false, true)
                    webViewActivity.tabsState.goBackAnimBusy = false
                    if (TextUtils.isEmpty(webViewActivity.dialogsState.pageUrl)) {
                        webViewActivity.videoState.pageFrameBusy = false
                        return
                    }
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(WvaScrollToPendingText(webViewActivity))
                }
            }

            override fun run() {
                val wvaPageFrameLoadTask = this@WvaPageFrameLoadTask
                val webViewActivity = wa
                webViewActivity.attachPageListeners(webViewActivity.tabsState.currentFrame, webViewActivity.currentPage, 0)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(LoadFramePageStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            WebViewActivity.applyDarkSettings(webViewActivity.currentPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(FrameListenersStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.addJsBridge(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(FrameShownStep())
    }
}

class WvaRefreshPageBarsTask(@JvmField val wa: WebViewActivity, @JvmField val urlChanged: Boolean = false, @JvmField val hostChanged: Boolean = false, @JvmField val videoHostEntered: Boolean = false) : Runnable {
    override fun run() {
        var appBarLayout: BarScroll? = null
        val webViewActivity = wa
        val urlChanged = this@WvaRefreshPageBarsTask.urlChanged
        val hostChanged = this@WvaRefreshPageBarsTask.hostChanged
        if (this@WvaRefreshPageBarsTask.videoHostEntered) {
            val barScroll = webViewActivity.barScroll
            if (barScroll != null) {
                barScroll.setExpanded(false)
            }
        } else if (hostChanged && (webViewActivity.barScroll.also { appBarLayout = it }) != null) {
            appBarLayout!!.setExpanded(true)
        }
        if (!urlChanged && webViewActivity.tabBar == null) {
            webViewActivity.dialogsState.lastLoadUrl = null
            return
        }
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody == null) {
            return
        }
        myWebBody.post(WvaRefreshTabBarTask(webViewActivity, urlChanged = urlChanged))
    }
}

class WvaRefreshTabBarTask(@JvmField val wa: WebViewActivity, @JvmField val urlChanged: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val urlChanged = this@WvaRefreshTabBarTask.urlChanged
        val webTabBarAdapter = webViewActivity.tabBar
        if (webTabBarAdapter != null) {
            webTabBarAdapter.redrawAt(webTabBarAdapter.barPosOf(webViewActivity.currentTabIndex))
        }
        if (urlChanged) {
            webViewActivity.runInBackground(WvaRefreshFileUrlTask(webViewActivity))
        } else {
            webViewActivity.dialogsState.lastLoadUrl = null
        }
    }
}

class WvaStoreTabThumb(@JvmField val wa: WebViewActivity, @JvmField val tabId: Long = 0, @JvmField val cacheKey: String? = null, @JvmField val bitmap: android.graphics.Bitmap? = null) : Runnable {

    internal inner class SaveThumbStep : Runnable {
        /** Stores the captured tab snapshot (lossy WebP, was PNG 100) and hands it to an open tab overview. */
        override fun run() {
            val activity = wa
            val tabId = this@WvaStoreTabThumb.tabId
            val cacheKey = this@WvaStoreTabThumb.cacheKey
            val bitmap = this@WvaStoreTabThumb.bitmap
            val context = activity.dialogsState.appContext
            if (context != null && tabId > 0 && isBitmapOk(bitmap)) {
                val png: ByteArray? = TabSnapshot.encode(bitmap!!)
                if (png != null && png.size != 0) {
                    val args = arrayOf<String?>(tabId.toString())
                    val db = DbTabThumb.get(context).getWritableDatabase()
                    val state = existsWhere(db, "DbTabThumb_table", null, "_uid=?", args)
                    if (state != 0) {
                        val values = ContentValues()
                        values.put("_secret", com.mycompany.app.pref.store.Workspaces.dataNumber())
                        values.put("_uid", tabId)
                        values.put("_tkey", cacheKey)
                        values.put("_thumb", png)
                        if (state == 1) {
                            updateWhere(db, "DbTabThumb_table", values, "_uid=?", args)
                        } else {
                            insert(db, "DbTabThumb_table", values)
                        }
                    }
                }
            }
            if (!isBitmapOk(bitmap)) return
            // kept in the memory cache, so the overview shows it without reading the database again; the overview
            // open right now (it was opened while this was captured) gets it directly
            com.mycompany.app.glide.ImageCache.put(TabSnapshot.memoryKey(tabId, cacheKey), bitmap)
            activity.mainHandler?.post { activity.tabsState.composeOverview?.onSnapshot(tabId, bitmap!!) }
        }
    }

    override fun run() {
        wa.runInBackground(SaveThumbStep())
    }
}

class WvaSaveTabItem(@JvmField val wa: WebViewActivity, @JvmField val loadTabItemAsyncTabItem: WebTabItem? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTabItem = this@WvaSaveTabItem.loadTabItemAsyncTabItem
        if (webTabItem != null) {
            val context = webViewActivity.dialogsState.appContext
            val uid = webTabItem.uid
            var tabUrl = webTabItem.tabUrl
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                tabUrl = webNestView.displayUrl(tabUrl)
            }
            var tabTitle = webTabItem.tabTitle
            val deskMode = webTabItem.deskMode
            val secretMode = SyncSettings.secretMode.value
            val dbBookTab = DbBookTab.cached
            if (context == null || uid <= 0) {
                return
            }
            val fileTitle = pageTitleForFile(context, tabUrl)
            if (TextUtils.isEmpty(fileTitle)) {
                return
            }
            if (TextUtils.isEmpty(tabTitle)) {
                tabTitle =
                    if ("file:///android_asset/shortcut.html" == fileTitle) "OPUM" else MainUtil.hostNameOf(
                        fileTitle,
                        false
                    )
                if (TextUtils.isEmpty(tabTitle)) {
                    return
                }
            }
            val contentValues = ContentValues()
            contentValues.put("_secret", com.mycompany.app.pref.store.Workspaces.numberFor(secretMode))
            contentValues.put("_path", fileTitle)
            contentValues.put("_title", tabTitle)
            contentValues.put("_desk", if (deskMode) 1 else 0)
            DbUtil.updateWhere(
                DbBookTab.get(context).getWritableDatabase(),
                "DbBookTab3_table",
                contentValues,
                "_uid=?",
                arrayOf<String?>(uid.toString())
            )
        }
    }
}

class WvaTabBarThumbUpdate(@JvmField val wa: WebViewActivity, @JvmField val bitmap: android.graphics.Bitmap? = null, @JvmField val tabIndex: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val bitmap = this@WvaTabBarThumbUpdate.bitmap
        val tabIndex = this@WvaTabBarThumbUpdate.tabIndex
        val webTabBarAdapter = webViewActivity.tabBar
        if (webTabBarAdapter != null) {
            val barPos = webTabBarAdapter.barPosOf(tabIndex)
            val webTabItemB: WebTabItem? = webTabBarAdapter.itemAt(barPos)
            if (webTabItemB != null && webTabItemB.groupTabs == null) {
                val webTabItemE: WebTabItem? = webTabBarAdapter.shownAt(WebTabBar.groupColor(webTabItemB))
                webTabBarAdapter.setIcon(webTabItemE, barPos, bitmap)
            }
        }
    }
}

class WvaOpenDownloadDialogTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val disposition: String? = null, @JvmField val mime: String? = null, @JvmField val referer: String? = null, @JvmField val title: String? = null, @JvmField val fileName: String? = null, @JvmField val faceItems: ArrayList<Any?>? = null, @JvmField val listItems: ArrayList<com.mycompany.app.main.MainDownAdapter.DownListItem?>? = null, @JvmField val pageUrl: String? = null, @JvmField val pageTitle: String? = null, @JvmField val length: Long = 0, @JvmField val type: Byte = 0, @JvmField val external: Boolean = false, @JvmField val index: Int = 0, @JvmField val fromBridge: Boolean = false) : Runnable {

    internal inner class PrepareDownloadStep : Runnable {

        /**
         * Second half wa.of [wa.v8]: works out the media type and file name wa.of a
         * download, then opens the download dialog (or, with wa.an album folder set, saves wa.an image
         * straight to it).
         */
        override fun run() {
            val activity = wa
            val fromMenu = activity.pageState.fromMenu
            var url = this@WvaOpenDownloadDialogTask.url
            val disposition = this@WvaOpenDownloadDialogTask.disposition
            var mime = this@WvaOpenDownloadDialogTask.mime
            val length = this@WvaOpenDownloadDialogTask.length
            var type = this@WvaOpenDownloadDialogTask.type
            val referer = this@WvaOpenDownloadDialogTask.referer
            val title = this@WvaOpenDownloadDialogTask.title
            val fileName = this@WvaOpenDownloadDialogTask.fileName
            val extFlag = this@WvaOpenDownloadDialogTask.external
            val faceItemsNow = this@WvaOpenDownloadDialogTask.faceItems
            val index = this@WvaOpenDownloadDialogTask.index
            val listItemsNow = this@WvaOpenDownloadDialogTask.listItems
            val fromBridge = this@WvaOpenDownloadDialogTask.fromBridge
            val pageUrl = this@WvaOpenDownloadDialogTask.pageUrl
            val pageTitle = this@WvaOpenDownloadDialogTask.pageTitle

            var source = (if (extFlag) 1 else 0).toByte()
            var reel = false
            if (type.toInt() == 5 && !TextUtils.isEmpty(url)) {
                if (url!!.startsWith("https://www.instagram.com/")) {
                    source = 2
                } else if (url == "insta_dummy") {
                    url = instagramReelUrl(pageUrl)
                    if (TextUtils.isEmpty(url)) {
                        url = pageUrl
                    }
                    source = 2
                    reel = true
                } else if (url == "twit_dummy") {
                    url = nextPageUrl(pageUrl)
                    if (TextUtils.isEmpty(url)) {
                        url = pageUrl
                    }
                    source = 1
                }
            }
            if (type.toInt() == 0 && !TextUtils.isEmpty(mime)) {
                if (mime!!.startsWith("image")) {
                    type = 4
                } else if (mime.startsWith("video")) {
                    type = 5
                } else if (mime.startsWith("audio") || mime == "application/ogg" || mime == "application/x-ogg") {
                    type = 6
                }
            }
            var name: String?
            if (!TextUtils.isEmpty(fileName)) {
                name = fileName
            } else if (type.toInt() == 5) {
                if (!TextUtils.isEmpty(activity.dialogsState.pageHost) && activity.dialogsState.pageHost!!.endsWith("soundcloud.com") && !TextUtils.isEmpty(
                        url
                    ) && url!!.contains(".mp3")
                ) {
                    mime = "audio/*"
                    name = "Sound.mp3"
                    type = 6
                } else {
                    name = videoName(activity, url, disposition, pageTitle)
                }
            } else if (type.toInt() == 4 && !TextUtils.isEmpty(activity.dialogsState.pageHost) && activity.dialogsState.pageHost!!.endsWith("pornhub.com") && MainUtil.isPhnGif(
                    url
                )
            ) {
                name = safeFileName(pageTitle)
                if (!TextUtils.isEmpty(name)) {
                    name = phTitle(name) + ".gif"
                }
            } else {
                name = null
            }
            if (!TextUtils.isEmpty(AlbumSettings.dappPkg.value) && !TextUtils.isEmpty(AlbumSettings.dappCls.value) && source.toInt() == 0) {
                // An album folder is configured: save directly without asking.
                if (activity.mainHandler != null) {
                    activity.mainHandler!!.post(WvaShowDownloadDialogTask(activity, tabsOpenDownloadDialogTaskText = name, url = url, miscOpenDownloadDialogTaskText = mime))
                }
                return
            }
            val mode = 0.toByte()
            activity.dialogsState.downloadPageUrl = referer
            if (activity.videoFrame != null) {
                activity.videoState.videoDialogOpen = true
                AppState.darkUi = true
            }
            val label = if (TextUtils.isEmpty(title)) activity.resolvePageLink(url) else title
            val pubList: MutableList<Any?>? = (if (activity.currentPage != null) activity.currentPage!!.pubList else null) as MutableList<Any?>?
            if (activity.webCoord != null) {
                activity.webCoord!!.post(WvaShowDownUrlDialog(activity, fromBridge = fromBridge, name = name, source = source, list = pubList, reel = reel, mode = mode, url = url, disposition = disposition, mime = mime, length = length, type = type, label = label, faceItems = faceItemsNow, index = index, listItems = listItemsNow))
            }
        }

        /** Turns wa.an Instagram ".../reels/<id>/..." page url into the ".../reel/<id>/..." post url. </wa.id></wa.id> */
        private fun instagramReelUrl(page: String?): String? {
            if (TextUtils.isEmpty(page) || !page!!.startsWith("https://www.instagram.com/")) {
                return null
            }
            val at = page.indexOf("/reels/")
            if (at <= 0) {
                return null
            }
            val idStart = at + 7
            val slash = page.indexOf('/', idStart)
            if (slash > idStart && slash + 1 != page.length) {
                val head = page.substring(0, at)
                val tail = page.substring(slash)
                if (!TextUtils.isEmpty(head) && !TextUtils.isEmpty(tail)) {
                    return "$head/reel$tail"
                }
            }
            return page.replace("/reels/", "/reel/")
        }

        /** File name for a video: from Content-Disposition or the page title, with a video extension.  */
        private fun videoName(
            webViewActivity: WebViewActivity?,
            url: String?,
            disposition: String?,
            pageTitle: String?
        ): String? {
            var name = dispositionFileName(disposition)
            if (!TextUtils.isEmpty(name)) {
                if (name!!.endsWith(".m3u8")) {
                    return name.substring(0, name.length - 4) + "mp4"
                }
                if (isVideoExt(lowerExtOf(name))) {
                    return name
                }
            } else {
                name = safeFileName(pageTitle)
                if (TextUtils.isEmpty(name)) {
                    return null
                }
            }
            if (!TextUtils.isEmpty(webViewActivity!!.dialogsState.pageHost) && webViewActivity.dialogsState.pageHost!!.endsWith("pornhub.com")) {
                name = phTitle(name)
            }
            var ext: String? = "mp4"
            if (!TextUtils.isEmpty(url) && !url!!.startsWith("tsfile:")) {
                val urlExt = fileExtOf(url, false)
                if (isVideoExt(urlExt)) {
                    ext = urlExt
                }
            }
            return "$name.$ext"
        }
    }

    override fun run() {
        wa.runInBackground(PrepareDownloadStep())
    }
}

class WvaRefreshStartPageTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestFrame: WebNestFrame? = if (webViewActivity.onStartPage && ZtwoSettings.addPage.value) webViewActivity.tabsState.currentFrame else null
        if (webViewActivity.onStartPage && ZtwoSettings.addPage.value && webNestFrame != null) {
            webNestFrame.trimBefore()
        }
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.setStartPage(webViewActivity.onStartPage)
        }
        webViewActivity.updateRssButton(webViewActivity.onStartPage && !ZtwoSettings.quickDown.value)
    }
}

class WvaTabDeleteListener(@JvmField val wa: WebViewActivity, @JvmField val tabsShowDeleteTabsDialogValue: Int = 0, @JvmField val dialogsShowDeleteTabsDialogValue: Int = 0, @JvmField val start: Int = 0, @JvmField val deleteMode: Byte = 0) : DelItemListener {

    internal inner class CloseDeleteDialogStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.closeDeleteItemDialog()
        }
    }

    override fun onDelete() {
        var deleted: Int
        val webViewActivity = wa
        if (webViewActivity.deleteItemDialog != null) {
            val deleteMode = this@WvaTabDeleteListener.deleteMode
            var rebuilt = false
            var index = 0
            rebuilt = false
            rebuilt = false
            rebuilt = false
            rebuilt = false
            rebuilt = false
            rebuilt = false
            rebuilt = false
            if (deleteMode.toInt() == 1) {
                val tabsShowDeleteTabsDialogValue = this@WvaTabDeleteListener.tabsShowDeleteTabsDialogValue
                val webTabItemD: WebTabItem? = if (webViewActivity.currentPage != null) webViewActivity.tabItemAt(tabsShowDeleteTabsDialogValue) else null
                if (webViewActivity.currentPage != null && webTabItemD != null) {
                    webViewActivity.finishProgress()
                    webViewActivity.exitFind(true)
                    synchronized(webViewActivity.tabLock) {
                        val arrayList = webViewActivity.tabsState.tabItems
                        val size = arrayList!!.size
                        while (index < size) {
                            val entry: Any? = arrayList.get(index)
                            index++
                            val webTabItem = entry as WebTabItem?
                            val webNestFrame: WebNestFrame? = if (webTabItem != null && webTabItem.uid != webTabItemD.uid) webTabItem.webFrame else null
                            if (webTabItem != null && webTabItem.uid != webTabItemD.uid && webNestFrame != null) {
                                webNestFrame.destroyFrame(null)
                                webTabItem.webFrame = null
                            }
                        }
                    }
                    webViewActivity.runInBackground(WvaTabDeleteListenerTask(webViewActivity, tabDeleteListenerTabItem = webTabItemD))
                    rebuilt = true
                }
            } else if (deleteMode.toInt() == 2) {
                val webTabItemD = webViewActivity.tabItemAt(this@WvaTabDeleteListener.tabsShowDeleteTabsDialogValue)
                if (webTabItemD != null) {
                    val groupId = webTabItemD.groupId
                    if (groupId != 0L) {
                        val closedTabs: ArrayList<WebTabItem?> = ArrayList()
                        val undoTabs: ArrayList<WebTabItem?> = ArrayList()
                        synchronized(webViewActivity.tabLock) {
                            val size = webViewActivity.tabsState.tabItems!!.size
                            deleted = 0
                            for (index2 in 0..<size) {
                                val tab = webViewActivity.tabsState.tabItems!!.get(index2)
                                if (tab != null) {
                                    if (tab.groupId == groupId) {
                                        closedTabs.add(tab)
                                    } else {
                                        val webNestFrame = tab.webFrame
                                        if (webNestFrame != null) {
                                            webNestFrame.destroyFrame(null)
                                            tab.webFrame = null
                                        }
                                        undoTabs.add(tab)
                                        if (tab.position <= webViewActivity.currentTabIndex) {
                                            deleted++
                                        }
                                    }
                                }
                            }
                        }
                        if (!undoTabs.isEmpty()) {
                            webViewActivity.runInBackground(WvaDeleteGroupTabsTask(webViewActivity, deletedTabs = closedTabs, groupTabs = undoTabs, deleteMode = deleted))
                            rebuilt = true
                        }
                    }
                }
            } else if (deleteMode.toInt() == 3) {
                val webTabItemD = webViewActivity.tabItemAt(this@WvaTabDeleteListener.tabsShowDeleteTabsDialogValue)
                if (webTabItemD != null) {
                    val groupId = webTabItemD.groupId
                    if (groupId != 0L) {
                        val uid = webTabItemD.uid
                        val otherTabs: ArrayList<Any?> = ArrayList<Any?>()
                        synchronized(webViewActivity.tabLock) {
                            val size = webViewActivity.tabsState.tabItems!!.size
                            for (index3 in this@WvaTabDeleteListener.start..<size) {
                                val otherTab = webViewActivity.tabsState.tabItems!!.get(index3)
                                if (otherTab != null && otherTab.uid != uid) {
                                    if (otherTab.groupId != groupId) {
                                        break
                                    }
                                    val webNestFrame = otherTab.webFrame
                                    if (webNestFrame != null) {
                                        webNestFrame.destroyFrame(null)
                                        otherTab.webFrame = null
                                    }
                                    otherTabs.add(otherTab)
                                }
                            }
                        }
                        if (!otherTabs.isEmpty()) {
                            webViewActivity.runInBackground(WvaDeleteTabsTask(webViewActivity, otherTabs))
                            rebuilt = true
                        }
                    }
                }
            } else {
                rebuilt = webViewActivity.rebuildTabs(false)
            }
            val myWebCoord: MyWebCoord? = if (rebuilt) null else wa.webCoord
            if (rebuilt || myWebCoord == null) {
                return
            }
            myWebCoord.post(CloseDeleteDialogStep())
        }
    }

    override fun bindHead(myRoundImage: MyRoundImage?, appCompatTextView: AppCompatTextView?) {
        val webViewActivity = wa
        val dialogsShowDeleteTabsDialogValue = this@WvaTabDeleteListener.dialogsShowDeleteTabsDialogValue
        if (webViewActivity.deleteItemDialog == null) {
            return
        }
        val sb = StringBuilder()
        sb.append(dialogsShowDeleteTabsDialogValue)
        sb.append(" ")
        sb.append(webViewActivity.dialogsState.appContext!!.getString(if (dialogsShowDeleteTabsDialogValue > 1) AppR.string.tab_items else AppR.string.tab_item))
        myRoundImage!!.showIcon(-460552, AppR.drawable.outline_public_black_24)
        appCompatTextView!!.setText(sb.toString())
    }
}

class WvaTabListDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeTabGroupDeleteDialog()
    }
}

class WvaTabEditApplied(@JvmField val wa: WebViewActivity) : DialogApplyListener {
    override fun onApply() {
        val webViewActivity = wa
        val dialogTabEdit = webViewActivity.dialogsState.tabEditDialog
        if (dialogTabEdit != null) {
            dialogTabEdit.dismiss()
            webViewActivity.dialogsState.tabEditDialog = null
        }
        val webTabBarAdapter = webViewActivity.tabBar
        if (webTabBarAdapter != null) {
            webTabBarAdapter.notifyAllTabs()
        }
    }
}

class TabOverviewListener(@JvmField val wa: WebViewActivity) : ListTabListener {
    override fun newTabInGroup(arrayList: ArrayList<WebTabItem?>?, position: Int, groupId: Long, groupName: String?, groupColor: Int) {
        wa.addTabToGroupFromOverview(arrayList, position, groupId, groupName, groupColor)
    }

    internal inner class ApplyOverviewStep(
                                         @JvmField val apply: Boolean,
                                         @JvmField val items: ArrayList<WebTabItem?>?,
                                         @JvmField val position: Int,
                                         @JvmField val secret: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            val secretMode = SyncSettings.secretMode.value
            val apply = this.apply
            val arrayList = this.items
            val position = this.position
            val secret = this.secret
            if (secretMode == secret) {
                if (apply) {
                    webViewActivity.applyTabList(position, arrayList)
                    return
                } else {
                    webViewActivity.selectTab(position, false)
                    return
                }
            }
            if (apply) {
                webViewActivity.tabsState.tabItems = arrayList
                webViewActivity.currentTabIndex = position
            }
            if (secret) {
                PrefSync.currentSecretIndex = position
            } else {
                SyncSettings.editLocal { set(SyncSettings.normalIndex, position) }
            }
            webViewActivity.switchSecretMode(secret)
        }
    }

    internal inner class ReloadForModeStep(
                                         @JvmField val secret: Boolean,
                                         @JvmField val items: ArrayList<WebTabItem?>?,
                                         @JvmField val tabIndex: Int,
                                         @JvmField val secretMode: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.reloadTabsForMode(this.secret, this.items, this.tabIndex, this.secretMode)
        }
    }

    override fun onClose(isSecret: Boolean, arrayList: ArrayList<WebTabItem?>?, position: Int, isLast: Boolean) {
        val webViewActivity = wa
        webViewActivity.closeTabOverview()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ReloadForModeStep(isSecret, arrayList, position, isLast))
    }

    override fun barHeight(): Int {
        val myWebBody = wa.dialogsState.webBody
        if (myWebBody == null) {
            return 0
        }
        return myWebBody.rectWidth
    }

    override fun refreshBar() {
        val webTabBarAdapter = wa.tabBar
        if (webTabBarAdapter != null) {
            webTabBarAdapter.notifyAllTabs()
        }
    }

    override fun applyList(position: Int, arrayList: ArrayList<WebTabItem?>?) {
        wa.applyTabList(position, arrayList)
    }

    override fun selectTab(isSecret: Boolean, arrayList: ArrayList<WebTabItem?>?, position: Int, isNew: Boolean) {
        val webViewActivity = wa
        webViewActivity.closeTabOverview()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ApplyOverviewStep(isSecret, arrayList, position, isNew))
    }
}

class MiniTabOverviewListener(@JvmField val wa: WebViewActivity) : ListTabListener {
    override fun newTabInGroup(arrayList: ArrayList<WebTabItem?>?, position: Int, groupId: Long, groupName: String?, groupColor: Int) {
        wa.addTabToGroupFromOverview(arrayList, position, groupId, groupName, groupColor)
    }

    internal inner class ApplyMiniOverviewStep(
                                         @JvmField val apply: Boolean,
                                         @JvmField val items: ArrayList<WebTabItem?>?,
                                         @JvmField val position: Int,
                                         @JvmField val secret: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            val secretMode = SyncSettings.secretMode.value
            val apply = this.apply
            val arrayList = this.items
            val position = this.position
            val secret = this.secret
            if (secretMode == secret) {
                if (apply) {
                    webViewActivity.applyTabList(position, arrayList)
                    return
                } else {
                    webViewActivity.selectTab(position, false)
                    return
                }
            }
            if (apply) {
                webViewActivity.tabsState.tabItems = arrayList
                webViewActivity.currentTabIndex = position
            }
            if (secret) {
                PrefSync.currentSecretIndex = position
            } else {
                SyncSettings.editLocal { set(SyncSettings.normalIndex, position) }
            }
            webViewActivity.switchSecretMode(secret)
        }
    }

    internal inner class ReloadMiniForModeStep(
                                         @JvmField val secret: Boolean,
                                         @JvmField val items: ArrayList<WebTabItem?>?,
                                         @JvmField val tabIndex: Int,
                                         @JvmField val secretMode: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.reloadTabsForMode(this.secret, this.items, this.tabIndex, this.secretMode)
        }
    }

    override fun onClose(isSecret: Boolean, arrayList: ArrayList<WebTabItem?>?, position: Int, isLast: Boolean) {
        val webViewActivity = wa
        webViewActivity.closeMiniTabOverview()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ReloadMiniForModeStep(isSecret, arrayList, position, isLast))
    }

    override fun barHeight(): Int {
        val myWebBody = wa.dialogsState.webBody
        if (myWebBody == null) {
            return 0
        }
        return myWebBody.rectWidth
    }

    override fun refreshBar() {
        val webTabBarAdapter = wa.tabBar
        if (webTabBarAdapter != null) {
            webTabBarAdapter.notifyAllTabs()
        }
    }

    override fun applyList(position: Int, arrayList: ArrayList<WebTabItem?>?) {
        wa.applyTabList(position, arrayList)
    }

    override fun selectTab(isSecret: Boolean, arrayList: ArrayList<WebTabItem?>?, position: Int, isNew: Boolean) {
        val webViewActivity = wa
        webViewActivity.closeMiniTabOverview()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ApplyMiniOverviewStep(isSecret, arrayList, position, isNew))
    }
}

class WvaGestureZoneListener(@JvmField val wa: WebViewActivity) : SimpleOnGestureListener() {
    fun sense(): Int {
        val gestureZone = wa.floatingState.gestureZone
        if (gestureZone.toInt() == 1) {
            return ZtwoSettings.senseTop.value
        }
        if (gestureZone.toInt() == 2) {
            return ZtwoSettings.senseBot.value
        }
        if (gestureZone.toInt() == 3) {
            return ZtwoSettings.senseLeft.value
        }
        return if (gestureZone.toInt() == 4) ZtwoSettings.senseRight.value else ZtwoSettings.senseCenter.value
    }

    fun senseSlop(): Int {
        val slop: Int = sense()
        if (slop == 200) {
            return 10
        }
        if (slop >= 100) {
            return Math.round((5.0f * (200 - slop)) + 10.0f)
        }
        return Math.round(((2.1f - (slop / 100.0f)) * 5.0f * (200 - slop)) + 10.0f)
    }

    override fun onFling(down: MotionEvent?, move: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val activity = wa
        if (activity.tabsState.currentFrame != null && activity.currentPage != null) {
            if (activity.pageState.pullMode.toInt() == 2) {
                val pullView = activity.pullView
                if (pullView != null) {
                    pullView.resetSpinner()
                }
            }

            var int = activity.tabsState.gestureDirection.toInt()
            if (int == 3) {
                if (velocityY > 1800.0f) {
                    val swipeTabFrame = activity.swipeTabFrame
                    if (swipeTabFrame != null) {
                        activity.revealPrevTab(swipeTabFrame.tabY, true)
                    }
                } else {
                    activity.settleTabSwipe(int)
                }

                return super.onFling(down, move, velocityX, velocityY)
            } else {
                run label315@{
                    if (activity.floatingState.gestureMode.toInt() != 0) {
                        run label233@{
                        if (int == 1) {
                            run label231@{
                                var swipeTabFrame = activity.swipeTabFrame
                                if (swipeTabFrame != null) {
                                    if (!(swipeTabFrame.pageX > 0.0f)) {
                                        return@label231
                                    }
                                } else {
                                    swipeTabFrame = activity.swipeNextTabFrame
                                    if (swipeTabFrame == null || !(swipeTabFrame.swipeOffset > 0.0f)) {
                                        return@label231
                                    }
                                }
                                if (velocityX > this.senseSlop()) {
                                    if (activity.tabsState.layoutRtl) {
                                        activity.animateTabSwipe(4, false)
                                    } else if (activity.tabsState.tabSwipeActive) {
                                        activity.animateTabSwipe(2, true)
                                    } else {
                                        activity.animateTabSwipe(4, true)
                                    }
                                    return@label233
                                }
                            }

                            if (activity.floatingState.edgeTouchActive && activity.tabsState.tabSwipeActive && velocityX > this.senseSlop()) {
                                activity.goBackFromBackKey(null)
                            } else {
                                activity.settleTabSwipe(activity.tabsState.gestureDirection.toInt())
                            }
                        } else {
                            run label217@{
                            if (int == 2) {
                                run label215@{
                                    var swipeTabFrame = activity.swipeTabFrame
                                    if (swipeTabFrame != null) {
                                        if (!(swipeTabFrame.pageX > 0.0f)) {
                                            return@label215
                                        }
                                    } else {
                                        swipeTabFrame = activity.swipeNextTabFrame
                                        if (swipeTabFrame == null || !(swipeTabFrame.swipeOffset > 0.0f)) {
                                            return@label215
                                        }
                                    }
                                    if (velocityX < -this.senseSlop()) {
                                        if (activity.tabsState.layoutRtl) {
                                            if (activity.tabsState.tabSwipeActive) {
                                                activity.animateTabSwipe(2, true)
                                            } else {
                                                activity.animateTabSwipe(4, true)
                                            }
                                        } else {
                                            activity.animateTabSwipe(4, false)
                                        }
                                        return@label217
                                    }
                                }

                                if (activity.floatingState.edgeTouchActive && activity.tabsState.tabSwipeActive && velocityX < -this.senseSlop()) {
                                    activity.goBackFromBackKey(null)
                                } else {
                                    activity.settleTabSwipe(activity.tabsState.gestureDirection.toInt())
                                }
                            } else if (activity.tabsState.touchBusy) {
                                if (abs(velocityY) > abs(velocityX)) {
                                    if (velocityY < -400.0f) {
                                        int = BrowserSettings.tabSwipeUp.value
                                        if (int >= 0 && int < com.mycompany.app.main.MainConst.mainMenuNames.size && int != 0 && (abs(move.getX() - activity.tabsState.touchDownX) < Xn || abs(
                                                move.getY() - activity.findState.touchDownY
                                            ) > MainApp.dp80 shl 1)
                                        ) {
                                            int = BrowserSettings.tabSwipeUp.value
                                            if (int == 38) {
                                                activity.onTabBarItemClick(activity.tabsState.touchTabIndex, activity.tabsState.dragTabIndex)
                                            } else {
                                                activity.runMenuItem(null, int)
                                            }
                                        }
                                    } else if (velocityY > 400.0f) {
                                        int = BrowserSettings.tabSwipeDown.value
                                        if (int >= 0 && int < com.mycompany.app.main.MainConst.mainMenuNames.size && int != 0 && (abs(move.getX() - activity.tabsState.touchDownX) < Xn || abs(
                                                move.getY() - activity.findState.touchDownY
                                            ) > MainApp.dp80 shl 1)
                                        ) {
                                            int = BrowserSettings.tabSwipeDown.value
                                            if (int == 38) {
                                                activity.onTabBarItemClick(activity.tabsState.touchTabIndex, activity.tabsState.dragTabIndex)
                                            } else {
                                                activity.runMenuItem(null, int)
                                            }
                                        }
                                    }
                                }
                            } else {
                                val touchBarView = activity.tabsState.touchBarView
                                if (touchBarView != null) {
                                    int = activity.tabsState.touchBarItem
                                    activity.tabsState.touchBarView = null
                                    activity.tabsState.touchBarItem = -1
                                    if (velocityY < -400.0f) {
                                        activity.runMenuItem(touchBarView, int)
                                    }
                                }
                            }
                            }
                        }
                        }
                    } else {
                        if (activity.floatingState.edgeTouchActive) {
                            if (int == 1) {
                                if ((this.sense() >= 100 || abs(velocityX) > abs(velocityY)) && velocityX > this.senseSlop()) {
                                    activity.goBackFromBackKey(null)
                                    return@label315
                                }
                            } else if (int == 2 && (this.sense() >= 100 || abs(velocityX) > abs(velocityY)) && velocityX < -this.senseSlop()) {
                                activity.goBackFromBackKey(null)
                                return@label315
                            }
                        }

                        var leftDone: Boolean
                        var rightDone: Boolean
                        if (ZtwoSettings.addPage.value) {
                            val canGoBack = activity.pageCanGoBack()
                            val noBack = canGoBack xor true
                            val canGoForward = activity.pageCanGoForward()
                            val noForward = canGoForward xor true
                            val gestureDirection = activity.tabsState.gestureDirection
                            if (gestureDirection.toInt() == 1) {
                                if (activity.tabsState.layoutRtl) {
                                    leftDone = noBack
                                    rightDone = noForward
                                    if (!canGoForward) {
                                        if (activity.isSwipeMoving() && velocityX > this.senseSlop()) {
                                            activity.animatePageSwipe(4, false)
                                            leftDone = noBack
                                            rightDone = noForward
                                        } else {
                                            activity.finishPageSwipe(activity.tabsState.gestureDirection.toInt())
                                            leftDone = noBack
                                            rightDone = noForward
                                        }
                                    }
                                } else {
                                    leftDone = noBack
                                    rightDone = noForward
                                    if (!canGoBack) {
                                        if (activity.isSwipeMoving() && velocityX > this.senseSlop()) {
                                            activity.animatePageSwipe(4, true)
                                            leftDone = noBack
                                            rightDone = noForward
                                        } else {
                                            activity.finishPageSwipe(activity.tabsState.gestureDirection.toInt())
                                            leftDone = noBack
                                            rightDone = noForward
                                        }
                                    }
                                }
                            } else {
                                leftDone = noBack
                                rightDone = noForward
                                if (gestureDirection.toInt() == 2) {
                                    if (activity.tabsState.layoutRtl) {
                                        leftDone = noBack
                                        rightDone = noForward
                                        if (!canGoBack) {
                                            if (activity.isSwipeMoving() && velocityX < -this.senseSlop()) {
                                                activity.animatePageSwipe(4, true)
                                                leftDone = noBack
                                                rightDone = noForward
                                            } else {
                                                activity.finishPageSwipe(activity.tabsState.gestureDirection.toInt())
                                                leftDone = noBack
                                                rightDone = noForward
                                            }
                                        }
                                    } else {
                                        leftDone = noBack
                                        rightDone = noForward
                                        if (!canGoForward) {
                                            if (activity.isSwipeMoving() && velocityX < -this.senseSlop()) {
                                                activity.animatePageSwipe(4, false)
                                                leftDone = noBack
                                                rightDone = noForward
                                            } else {
                                                activity.finishPageSwipe(activity.tabsState.gestureDirection.toInt())
                                                leftDone = noBack
                                                rightDone = noForward
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            leftDone = false
                            rightDone = false
                        }

                        if (this.sense() >= 100 || abs(velocityX) > abs(velocityY)) {
                            val gestureDirection = activity.tabsState.gestureDirection
                            if (gestureDirection.toInt() == 1) {
                                if (activity.tabsState.layoutRtl) {
                                    if (!rightDone) {
                                        val leftScrollNavi = activity.leftScrollNavi
                                        val leftOut: Boolean
                                        if (leftScrollNavi == null) {
                                            leftOut = false
                                        } else {
                                            leftOut = leftScrollNavi.isOut()
                                        }

                                        if (leftOut && velocityX > this.senseSlop()) {
                                            activity.startGoForward()
                                        }
                                    }
                                } else if (!leftDone) {
                                    val leftScrollNavi = activity.leftScrollNavi
                                    val leftOut: Boolean
                                    if (leftScrollNavi == null) {
                                        leftOut = false
                                    } else {
                                        leftOut = leftScrollNavi.isOut()
                                    }

                                    if (leftOut && velocityX > this.senseSlop()) {
                                        activity.goBackFromBackKey(null)
                                    }
                                }
                            } else if (gestureDirection.toInt() == 2) {
                                if (activity.tabsState.layoutRtl) {
                                    if (!leftDone) {
                                        val rightScrollNavi = activity.floatingState.rightScrollNavi
                                        val rightOut: Boolean
                                        if (rightScrollNavi == null) {
                                            rightOut = false
                                        } else {
                                            rightOut = rightScrollNavi.isOut()
                                        }

                                        if (rightOut && velocityX < -this.senseSlop()) {
                                            activity.goBackFromBackKey(null)
                                        }
                                    }
                                } else if (!rightDone) {
                                    val rightScrollNavi = activity.floatingState.rightScrollNavi
                                    val rightOut: Boolean
                                    if (rightScrollNavi == null) {
                                        rightOut = false
                                    } else {
                                        rightOut = rightScrollNavi.isOut()
                                    }

                                    if (rightOut && velocityX < -this.senseSlop()) {
                                        activity.startGoForward()
                                    }
                                }
                            }
                        }
                    }
                    activity.tabsState.touchBusy = false
                    activity.tabsState.touchTabIndex = -1
                    activity.tabsState.touchBarView = null
                    activity.tabsState.touchBarItem = -1
                    return super.onFling(down, move, velocityX, velocityY)
                }

                activity.tabsState.touchBusy = false
                activity.tabsState.touchTabIndex = -1
                activity.tabsState.touchBarView = null
                activity.tabsState.touchBarItem = -1
                return super.onFling(down, move, velocityX, velocityY)
            }
        } else {
            return false
        }
    }
}

class WvaExitApp(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class FinishActivityStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.finish()
            webViewActivity.tabsState.exitAppBusy = false
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.tabsState.exitAppBusy) {
            return
        }
        webViewActivity.tabsState.exitAppBusy = true
        if (webViewActivity.tabsState.currentFrame != null && webViewActivity.tabItemAt(webViewActivity.currentTabIndex) != null) {
            webViewActivity.isExiting = true
            webViewActivity.removeTabAt(webViewActivity.currentTabIndex)
            val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
            val pageUrl = if (webTabItemD != null) webTabItemD.tabUrl else null
            if (TextUtils.isEmpty(pageUrl)) {
                webViewActivity.dialogsState.pageUrl = "file:///android_asset/shortcut.html"
                webViewActivity.onStartPage = true
            } else {
                webViewActivity.dialogsState.pageUrl = pageUrl
                webViewActivity.onStartPage = "file:///android_asset/shortcut.html" == pageUrl
            }
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(FinishActivityStep())
    }
}

class WvaClearOnExit(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val exitDelete = BrowserSettings.exitDelete.value
        val webViewActivity = wa
        if (exitDelete == 126) {
            webViewActivity.sendAppToBackground()
            return
        }
        if (SyncSettings.secretMode.value) {
            if ((exitDelete and 64) == 64) {
                webViewActivity.sendAppToBackground()
                return
            }
        } else if ((exitDelete and 32) == 32) {
            webViewActivity.sendAppToBackground()
            return
        }
        val webNestView: WebNestView? = if (webViewActivity.isExiting) null else webViewActivity.currentPage
        if (webViewActivity.isExiting || webNestView == null || webNestView.blankPending) {
            webViewActivity.sendAppToBackground()
            return
        }
        val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
        if (webTabItemD == null) {
            webViewActivity.sendAppToBackground()
            return
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.postDelayed(SendToBackgroundTask(webViewActivity), 1000L)
        webViewActivity.currentPage!!.saveTabState(
            webViewActivity.tabsState.currentFrame,
            webTabItemD.uid,
            webTabItemD.tabUrl,
            BundleSavedToBackground(webViewActivity)
        )
    }
}

class WvaSetupCreatePagesTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class CreatePagesStep : Runnable {

        internal inner class PagesDarkStep : Runnable {

            internal inner class PagesListenersStep : Runnable {

                internal inner class BindFrameStep : Runnable {

                    internal inner class AddPullViewStep : Runnable {
                        override fun run() {
                            val pagesListenersStep = this@PagesListenersStep
                            val webViewActivity = wa
                            webViewActivity.addPullView()
                            val activity = wa
                            if (activity.currentPage == null) {
                                activity.miscState.startPageSetupBusy = false
                                return
                            }
                            val myWebCoord = activity.webCoord
                            if (myWebCoord == null) {
                                return
                            }
                            myWebCoord.post(WvaFinishStartupTask(activity))
                        }
                    }

                    override fun run() {
                        val pagesDarkStep = this@PagesDarkStep
                        val webViewActivity = wa
                        webViewActivity.bindFrameToTab()
                        val myWebCoord = wa.webCoord
                        if (myWebCoord == null) {
                            return
                        }
                        myWebCoord.post(AddPullViewStep())
                    }
                }

                override fun run() {
                    val createPagesStep = this@CreatePagesStep
                    val webViewActivity = wa
                    webViewActivity.attachPageListeners(webViewActivity.tabsState.currentFrame, webViewActivity.currentPage, 0)
                    val myWebCoord = wa.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(BindFrameStep())
                }
            }

            override fun run() {
                val wvaSetupCreatePagesTask = this@WvaSetupCreatePagesTask
                WebViewActivity.applyDarkSettings(wa.currentPage)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(PagesListenersStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.addJsBridge(webViewActivity.currentPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(PagesDarkStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.applyWebSettings(webViewActivity.currentPage, 0)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(CreatePagesStep())
    }
}

class WvaMainBarListener(@JvmField val wa: WebViewActivity) : BarListener {
    override fun onItemClick(id: Int, view: View?, isLong: Boolean) {
        val webViewActivity = wa
        if (id == 0) {
            if (webViewActivity.tabsState.currentFrame == null || webViewActivity.isBusy() || webViewActivity.isAnyDialogOpen() || webViewActivity.dialogsState.addressPrefixMenu != null) {
                return
            }
            webViewActivity.closeAddressPrefixMenu()
            if (view != null) {
                val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                arrayList.add(PopMenuItem(0, "https://"))
                arrayList.add(PopMenuItem(1, "www."))
                val myPopupMenu = MyPopupMenu(
                    webViewActivity,
                    webViewActivity.dialogsState.webBody,
                    view,
                    arrayList,
                    MainUtil.isDarkPage(webViewActivity.onStartPage),
                    WvaEditorPrefixPopup(webViewActivity)
                )
                webViewActivity.dialogsState.addressPrefixMenu = myPopupMenu
                webViewActivity.popupMenu = myPopupMenu
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(WvaAddressPrefixMenuClosed(webViewActivity))
                return
            }
            return
        }
        if (id != 1) {
            if (id != 2 || webViewActivity.dialogsState.menuItemBusy) {
                return
            }
            webViewActivity.dialogsState.menuItemBusy = true
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaVoiceSearch(webViewActivity))
                return
            }
            return
        }
        if (webViewActivity.currentPage != null) {
            webViewActivity.pageState.addressEditBusy = false
            webViewActivity.setAddressEditing(false)
        }
    }
}

class WvaCloseExpiredTabs(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        var list: MutableList<WebNestFrame.PageItem?>? = null
        val dayMemory = ZtwoSettings.dayMemory.value
        val webViewActivity = wa
        val arrayList: ArrayList<WebTabItem?>? = if (dayMemory != 0) webViewActivity.tabsState.tabItems else null
        if (dayMemory != 0 && arrayList != null) {
            val now =
                System.currentTimeMillis() - ((ZtwoSettings.dayMemory.value.toLong()) * 86400000)
            try {
                val currentFrame = webViewActivity.tabsState.currentFrame
                if (currentFrame != null) {
                    currentFrame.removePagesOfTab(now, true)
                    val size = arrayList.size
                    for (index in 0..<size) {
                        val webTabItem: WebTabItem? = if (index != webViewActivity.currentTabIndex) (arrayList.get(index)) else null
                        val webNestFrame: WebNestFrame? = if (index != webViewActivity.currentTabIndex && webTabItem != null) webTabItem.webFrame else null
                        if (index != webViewActivity.currentTabIndex && webTabItem != null && webNestFrame != null) {
                            if (!(webNestFrame.getChildCount() == 0 || (webNestFrame.pageItems.also {
                                    list = it
                                }) == null || list!!.isEmpty())) {
                                webTabItem.webFrame!!.removePagesOfTab(now, false)
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
            }
        }
        webViewActivity.dialogsState.applySettingsChangeBusy = false
    }
}

class WvaTrimOldTabs(@JvmField val activity: WebViewActivity,
                                      @JvmField val cutoff: Long
) : Runnable {
    /**
     * Memory saving: blanks the web views wa.of tabs more than one position away from the current tab (wa.L2) that
     * were last used before a, unless their site is wa.on the keep-in-memory list (DataBookTmem).
     */
    override fun run() {
        val cutoff = this.cutoff
        val activity = this.activity
        val tabs = activity.tabsState.tabItems
        if (cutoff == 0L || tabs == null) {
            return
        }
        try {
            val keep = DataBookTmem.instance(activity.dialogsState.appContext)
            val hasKeep = keep.sites != null && !keep.sites!!.isEmpty()
            val low = activity.currentTabIndex - 2
            val high = activity.currentTabIndex + 2
            val size = tabs.size
            for (index in 0..<size) {
                if (index > low && index < high) {
                    continue
                }
                val tab = tabs.get(index)
                if (tab == null || tab.lastUsed >= cutoff || tab.webFrame == null) {
                    continue
                }
                val frame = tab.webFrame
                val empty = frame!!.getChildCount() == 0 || frame.pageItems == null || frame.pageItems!!.isEmpty()
                if (empty) {
                    continue
                }
                if (hasKeep && kept(activity, tab.tabUrl)) {
                    continue
                }
                frame.setBlankView(false)
            }
        } catch (ignored: Exception) {
        }
    }

    private fun kept(activity: WebViewActivity?, url: String?): Boolean =
        MemoryRelease.keptInMemory(activity?.dialogsState?.appContext, url)
}

class WvaAddTabImplTask(@JvmField val wa: WebViewActivity, @JvmField val frame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val url: String? = null, @JvmField val inGroup: Boolean = false, @JvmField val autoGroup: Boolean = false, @JvmField val fromIntent: Boolean = false, @JvmField val isTop: Boolean = false, @JvmField val addTabImplAddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val pending: Boolean = false) : Runnable {
    override fun run() {
        val tabColor: Int
        val tabLight: Int
        var slideIn: Boolean
        val webViewActivity = wa
        val webNestFrame = this@WvaAddTabImplTask.frame
        val url = this@WvaAddTabImplTask.url
        val inGroup = this@WvaAddTabImplTask.inGroup
        val autoGroup = this@WvaAddTabImplTask.autoGroup
        val fromIntent = this@WvaAddTabImplTask.fromIntent
        val isTop = this@WvaAddTabImplTask.isTop
        val tabAddListener = this@WvaAddTabImplTask.addTabImplAddListener
        val pending = this@WvaAddTabImplTask.pending
        val webNestView = webViewActivity.currentPage
        var desktop = false
        if (webNestView != null) {
            val deskMode = webNestView.deskMode
            tabColor = webViewActivity.pageThemeColor()
            tabLight = webViewActivity.pageThemeLight()
            slideIn = true
            val quickView: QuickView? = if (webViewActivity.onStartPage) webViewActivity.currentPage!!.quickPanel else null
            if (webViewActivity.onStartPage && quickView != null) {
                quickView.editing = true
            }
            webViewActivity.currentPage!!.setWebViewClient(MainUtil.quietWebClient)
            webViewActivity.currentPage!!.setWebChromeClient(null)
            webViewActivity.currentPage!!.setDownloadListener(null)
            if (isTop && AlbumSettings.tabAnim.value) {
                webViewActivity.addTabAnim = true
            } else {
                webViewActivity.addTabAnim = true
                webViewActivity.animFromStartPage = webViewActivity.onStartPage
                slideIn = false
            }
            desktop = deskMode
        } else {
            tabColor = 0
            tabLight = 0
            slideIn = false
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaAddTabPauseTask(webViewActivity, frame = webNestFrame, url = url, inGroup = inGroup, autoGroup = autoGroup, fromIntent = fromIntent, isTop = isTop, desktop = desktop, themeColor = tabColor, themeLight = tabLight, slideIn = slideIn, addTabImplTaskAddListener = tabAddListener, pending = pending))
    }
}

class WvaAddTabPauseTask(@JvmField val wa: WebViewActivity, @JvmField val frame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val url: String? = null, @JvmField val inGroup: Boolean = false, @JvmField val autoGroup: Boolean = false, @JvmField val fromIntent: Boolean = false, @JvmField val isTop: Boolean = false, @JvmField val desktop: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0, @JvmField val slideIn: Boolean = false, @JvmField val addTabImplTaskAddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val pending: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestFrame = this@WvaAddTabPauseTask.frame
        val url = this@WvaAddTabPauseTask.url
        val inGroup = this@WvaAddTabPauseTask.inGroup
        val autoGroup = this@WvaAddTabPauseTask.autoGroup
        val fromIntent = this@WvaAddTabPauseTask.fromIntent
        val isTop = this@WvaAddTabPauseTask.isTop
        val desktop = this@WvaAddTabPauseTask.desktop
        val themeColor = this@WvaAddTabPauseTask.themeColor
        val themeLight = this@WvaAddTabPauseTask.themeLight
        val slideIn = this@WvaAddTabPauseTask.slideIn
        val tabAddListener = this@WvaAddTabPauseTask.addTabImplTaskAddListener
        val pending = this@WvaAddTabPauseTask.pending
        WebViewActivity.pauseMedia(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaAddTabReleasePageTask(webViewActivity, frame = webNestFrame, url = url, inGroup = inGroup, autoGroup = autoGroup, fromIntent = fromIntent, isTop = isTop, desktop = desktop, themeColor = themeColor, slideIn = slideIn, addTabImplTask2AddListener = tabAddListener, pending = pending, themeLight = themeLight))
    }
}

class WvaAddTabReleasePageTask(@JvmField val wa: WebViewActivity, @JvmField val frame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val url: String? = null, @JvmField val inGroup: Boolean = false, @JvmField val autoGroup: Boolean = false, @JvmField val fromIntent: Boolean = false, @JvmField val isTop: Boolean = false, @JvmField val desktop: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val slideIn: Boolean = false, @JvmField val addTabImplTask2AddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val pending: Boolean = false, @JvmField val themeLight: Int = 0) : Runnable {
    override fun run() {
        val pageCreate: WebNestView?
        val alwaysFalse: Boolean
        val reusedPending: Boolean
        val freshPage: Boolean
        val webViewActivity = wa
        var webNestFrame = this@WvaAddTabReleasePageTask.frame
        val url = this@WvaAddTabReleasePageTask.url
        val inGroup = this@WvaAddTabReleasePageTask.inGroup
        val autoGroup = this@WvaAddTabReleasePageTask.autoGroup
        val fromIntent = this@WvaAddTabReleasePageTask.fromIntent
        val isTop = this@WvaAddTabReleasePageTask.isTop
        val desktop = this@WvaAddTabReleasePageTask.desktop
        val themeColor = this@WvaAddTabReleasePageTask.themeColor
        val id = this@WvaAddTabReleasePageTask.themeLight
        val slideIn = this@WvaAddTabReleasePageTask.slideIn
        val tabAddListener = this@WvaAddTabReleasePageTask.addTabImplTask2AddListener
        val pending = this@WvaAddTabReleasePageTask.pending
        TabFrameLog.step(webViewActivity, "step95 before swap", webViewActivity.tabsState.currentFrame, "given=${webNestFrame != null}")
        if (webNestFrame != null) {
            detachFromParent(webNestFrame)
            pageCreate = webNestFrame.pageCreate
            if (pageCreate == null) {
                webNestFrame.destroyFrame(null)
                webNestFrame = null
            }
        } else {
            pageCreate = null
        }
        var animateFrame = false
        if (webNestFrame == null || pageCreate == null) {
            val isShortcutPage = "file:///android_asset/shortcut.html" == url
            val webNestView = webViewActivity.pageState.pendingPage
            if (webNestView != null) {
                val myWebBody = webViewActivity.dialogsState.webBody
                if (myWebBody != null) {
                    myWebBody.removeViewInLayout(webNestView)
                }
                val pendingPage = webViewActivity.pageState.pendingPage
                webViewActivity.currentPage = pendingPage
                webViewActivity.pageState.pendingPage = null
                alwaysFalse = false
                pendingPage!!.setVisibility(View.VISIBLE)
                reusedPending = true
            } else {
                alwaysFalse = false
                webViewActivity.pauseWebDestroy()
                val currentPage = WebNestView(webViewActivity)
                webViewActivity.currentPage = currentPage
                webViewActivity.attachWebDestroy(currentPage)
                webViewActivity.currentPage!!.startPage = isShortcutPage
                reusedPending = false
            }
            animateFrame = alwaysFalse
            freshPage = true
        } else {
            webViewActivity.tabsState.currentFrame = webNestFrame
            webViewActivity.currentPage = pageCreate
            if (slideIn) {
                freshPage = false
                reusedPending = false
                animateFrame = true
            } else {
                webViewActivity.addTabAnim = false
                freshPage = false
                reusedPending = false
            }
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaAddTabAddPageTask(webViewActivity, freshPage = freshPage, frame = webNestFrame, url = url, inGroup = inGroup, autoGroup = autoGroup, fromIntent = fromIntent, isTop = isTop, desktop = desktop, themeColor = themeColor, themeLight = id, slideIn = slideIn, animateFrame = animateFrame, addTabImplTask3AddListener = tabAddListener, pending = pending, reusedPending = reusedPending))
    }
}

class WvaAddTabAddPageTask(@JvmField val wa: WebViewActivity, @JvmField val freshPage: Boolean = false, @JvmField val frame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val url: String? = null, @JvmField val inGroup: Boolean = false, @JvmField val autoGroup: Boolean = false, @JvmField val fromIntent: Boolean = false, @JvmField val isTop: Boolean = false, @JvmField val desktop: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0, @JvmField val slideIn: Boolean = false, @JvmField val animateFrame: Boolean = false, @JvmField val addTabImplTask3AddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val pending: Boolean = false, @JvmField val reusedPending: Boolean = false) : Runnable {
    override fun run() {
        val pendingReused: Boolean
        val webViewActivity = wa
        val webNestFrame = this@WvaAddTabAddPageTask.frame
        val url = this@WvaAddTabAddPageTask.url
        val inGroup = this@WvaAddTabAddPageTask.inGroup
        val autoGroup = this@WvaAddTabAddPageTask.autoGroup
        val fromIntent = this@WvaAddTabAddPageTask.fromIntent
        val isTop = this@WvaAddTabAddPageTask.isTop
        val desktop = this@WvaAddTabAddPageTask.desktop
        val themeColor = this@WvaAddTabAddPageTask.themeColor
        val themeLight = this@WvaAddTabAddPageTask.themeLight
        val slideIn = this@WvaAddTabAddPageTask.slideIn
        val animateFrame = this@WvaAddTabAddPageTask.animateFrame
        val tabAddListener = this@WvaAddTabAddPageTask.addTabImplTask3AddListener
        val pending = this@WvaAddTabAddPageTask.pending
        val reusedPending = this@WvaAddTabAddPageTask.reusedPending
        val freshPage = this@WvaAddTabAddPageTask.freshPage
        if (!freshPage || webViewActivity.currentPage == null) {
            pendingReused = reusedPending
        } else {
            val isShortcutPage = "file:///android_asset/shortcut.html" == url
            pendingReused = reusedPending
            webViewActivity.currentPage!!.setDeskMode(desktop)
            webViewActivity.currentPage!!.setBarColors(themeColor, themeLight)
            val currentFrame = WebNestFrame(webViewActivity)
            webViewActivity.tabsState.currentFrame = currentFrame
            currentFrame.setDarkBars(isShortcutPage)
            val currentFrameNow = webViewActivity.tabsState.currentFrame
            val webNestView = webViewActivity.currentPage
            if (currentFrameNow != null) {
                currentFrameNow.addPage(webNestView, 0, 0)
            }
        }
        webViewActivity.destroyPendingPage()
        val currentFrame = webViewActivity.tabsState.currentFrame
        if (currentFrame != null) {
            currentFrame.setAddNeed(slideIn)
        }
        webViewActivity.updateScrollBar()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaAddTabSettingsTask(webViewActivity, frame = webNestFrame, url = url, inGroup = inGroup, autoGroup = autoGroup, fromIntent = fromIntent, isTop = isTop, animateFrame = animateFrame, addTabImplTask4AddListener = tabAddListener, pending = pending, reusedPending = pendingReused))
    }
}

class WvaAddTabSettingsTask(@JvmField val wa: WebViewActivity, @JvmField val frame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val url: String? = null, @JvmField val inGroup: Boolean = false, @JvmField val autoGroup: Boolean = false, @JvmField val fromIntent: Boolean = false, @JvmField val isTop: Boolean = false, @JvmField val animateFrame: Boolean = false, @JvmField val addTabImplTask4AddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val pending: Boolean = false, @JvmField val reusedPending: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestFrame = this@WvaAddTabSettingsTask.frame
        val url = this@WvaAddTabSettingsTask.url
        val inGroup = this@WvaAddTabSettingsTask.inGroup
        val autoGroup = this@WvaAddTabSettingsTask.autoGroup
        val fromIntent = this@WvaAddTabSettingsTask.fromIntent
        val isTop = this@WvaAddTabSettingsTask.isTop
        val animateFrame = this@WvaAddTabSettingsTask.animateFrame
        val tabAddListener = this@WvaAddTabSettingsTask.addTabImplTask4AddListener
        val pending = this@WvaAddTabSettingsTask.pending
        val reusedPending = this@WvaAddTabSettingsTask.reusedPending
        webViewActivity.applyWebSettings(webViewActivity.currentPage, 0)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaAddTabInsertTask(webViewActivity, frame = webNestFrame, url = url, inGroup = inGroup, autoGroup = autoGroup, fromIntent = fromIntent, isTop = isTop, animateFrame = animateFrame, addTabImplTask5AddListener = tabAddListener, pending = pending, reusedPending = reusedPending))
    }
}

class WvaAddTabInsertTask(@JvmField val wa: WebViewActivity, @JvmField val frame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val url: String? = null, @JvmField val inGroup: Boolean = false, @JvmField val autoGroup: Boolean = false, @JvmField val fromIntent: Boolean = false, @JvmField val isTop: Boolean = false, @JvmField val animateFrame: Boolean = false, @JvmField val addTabImplTask5AddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val pending: Boolean = false, @JvmField val reusedPending: Boolean = false) : Runnable {

    internal inner class AddTabStep : Runnable {

        internal inner class NewTabListenersStep : Runnable {

            internal inner class LoadNewTabLaterStep : Runnable {

                internal inner class LoadNewTabStep : Runnable {
                    override fun run() {
                        val insertAt: Int
                        val webViewActivity = wa
                        val webNestFrame = this@WvaAddTabInsertTask.frame
                        val url = this@WvaAddTabInsertTask.url
                        val inGroup = this@WvaAddTabInsertTask.inGroup
                        val autoGroup = this@WvaAddTabInsertTask.autoGroup
                        val fromIntent = this@WvaAddTabInsertTask.fromIntent
                        val isTop = this@WvaAddTabInsertTask.isTop
                        val animateFrame = this@WvaAddTabInsertTask.animateFrame
                        val tabAddListener = this@WvaAddTabInsertTask.addTabImplTask5AddListener
                        val pending = this@WvaAddTabInsertTask.pending
                        val reusedPending = this@WvaAddTabInsertTask.reusedPending
                        val currentFrame = webViewActivity.tabsState.currentFrame
                        val backItem =
                            if (currentFrame != null) currentFrame.backItem else null
                        if (backItem != null) {
                            insertAt = backItem.position
                        } else {
                            insertAt = webViewActivity.nextTabInsertIndex(inGroup || autoGroup)
                        }
                        val insertedIndex = webViewActivity.insertTabItem(insertAt, url, webViewActivity.tabsState.currentFrame, inGroup, autoGroup)
                        webViewActivity.currentTabIndex = insertedIndex
                        val webTabItemD: WebTabItem? = if (pending) webViewActivity.tabItemAt(insertedIndex) else null
                        if (pending && webTabItemD != null) {
                            webTabItemD.reloadPending = true
                        }
                        val myWebCoord = webViewActivity.webCoord
                        if (myWebCoord == null) {
                            return
                        }
                        myWebCoord.post(WvaAddTabLoadTask(webViewActivity, frame = webNestFrame, url = url, fromIntent = fromIntent, isTop = isTop, animateFrame = animateFrame, addTabImplTask6AddListener = tabAddListener, reusedPending = reusedPending))
                    }
                }

                override fun run() {
                    wa.runInBackground(LoadNewTabStep())
                }
            }

            override fun run() {
                val wvaAddTabInsertTask = this@WvaAddTabInsertTask
                val webViewActivity = wa
                webViewActivity.attachPageListeners(webViewActivity.tabsState.currentFrame, webViewActivity.currentPage, 0)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(LoadNewTabLaterStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            WebViewActivity.applyDarkSettings(webViewActivity.currentPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(NewTabListenersStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.addJsBridge(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(AddTabStep())
    }
}

class WvaAddTabLoadTask(@JvmField val wa: WebViewActivity, @JvmField val frame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val url: String? = null, @JvmField val fromIntent: Boolean = false, @JvmField val isTop: Boolean = false, @JvmField val animateFrame: Boolean = false, @JvmField val addTabImplTask6AddListener: com.mycompany.app.web.WebViewActivity.TabAddListener? = null, @JvmField val reusedPending: Boolean = false) : Runnable {

    internal inner class TabLimitResult : WebViewActivity.TabLimitListener {

        internal inner class AddLimitedTabStep : Runnable {
            override fun run() {
                val webViewActivity = wa
                val webNestFrame = this@WvaAddTabLoadTask.frame
                val url = this@WvaAddTabLoadTask.url
                val fromIntent = this@WvaAddTabLoadTask.fromIntent
                val isTop = this@WvaAddTabLoadTask.isTop
                val animateFrame = this@WvaAddTabLoadTask.animateFrame
                val tabAddListener = this@WvaAddTabLoadTask.addTabImplTask6AddListener
                val reusedPending = this@WvaAddTabLoadTask.reusedPending
                if (webNestFrame != null || reusedPending) {
                    webViewActivity.dialogsState.hasRealPage = "about:blank" != url
                    webViewActivity.loadPageUrl(url, true, true)
                    webViewActivity.checkPageLanguage(true)
                } else {
                    if (fromIntent) {
                        webViewActivity.dialogsState.pageUrl = null
                        webViewActivity.onStartPage = false
                        webViewActivity.dialogsState.pageHost = null
                        webViewActivity.dialogsState.blobPageUrl = null
                        webViewActivity.dialogsState.onYouTube = false
                        webViewActivity.checkVideoPage(null)
                        webViewActivity.dialogsState.chromeClientBusy = false
                        webViewActivity.dialogsState.lastHamTag = null
                    }
                    if (webViewActivity.currentPage != null) {
                        var pendingTabUrl = webViewActivity.videoState.pendingTabUrl
                        webViewActivity.videoState.pendingTabUrl = null
                        if (TextUtils.isEmpty(pendingTabUrl)) {
                            pendingTabUrl = webViewActivity.dialogsState.pageUrl
                        }
                        val resolvedUrl = webViewActivity.currentPage!!.realUrl(url, false)
                        webViewActivity.loadPageUrl(resolvedUrl, false, true)
                        webViewActivity.dialogsState.blobPageUrl = pendingTabUrl
                        webViewActivity.currentPage!!.openUrl(resolvedUrl, pendingTabUrl)
                    }
                }
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(WvaAddTabAnimateTask(webViewActivity, isTop = isTop, animateFrame = animateFrame, addTabImplTask7AddListener = tabAddListener, reusedPending = reusedPending))
            }
        }

        override fun onLimitApplied(isChanged: Boolean) {
            val webViewActivity = wa
            webViewActivity.layoutTabFrames()
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(AddLimitedTabStep())
        }
    }

    override fun run() {
        val tabLimitResult: TabLimitResult = TabLimitResult()
        wa.applyTabLimit(tabLimitResult)
    }
}

/**
 * "New tab" in a group of the tab overview: takes over the overview's list ([list], current tab at [position]), then adds a
 * new tab right after the group (the `inGroup` path keeps it next to the group's tabs) and makes it join group [groupId]
 * with [name] and [colour] ([WebTabsState.nextTabGroup], used by `insertTabItem`). Works for a new, empty group too.
 */
fun WebViewActivity.addTabToGroupFromOverview(list: ArrayList<WebTabItem?>?, position: Int, groupId: Long, name: String?, colour: Int) {
    closeTabOverview()
    val coord = webCoord ?: return
    val activity = this
    coord.post {
        val add = {
            activity.tabsState.nextTabGroup = Triple(groupId, name, colour)
            activity.addTabImpl(null, WebViewActivity.newTabUrl(), true, true, false, object : WebViewActivity.TabAddListener {
                override fun onTabAdded(isAdded: Boolean) {
                    activity.tabsState.nextTabGroup = null
                    activity.onTabAdded(isAdded)
                }
            })
        }
        if (list != null && list.isNotEmpty()) {
            activity.tabsState.tabItems = list
            activity.currentTabIndex = position
            activity.reloadTabViews(object : WebViewActivity.TabViewListener { override fun onTabView() = add() })
        } else {
            add()
        }
    }
}
