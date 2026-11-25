@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.dialog.ListBookListener
import com.mycompany.app.main.RequestCode
import com.mycompany.app.ui.state.AppState
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
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.nativeJsN
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.matchesNativeHost
import com.mycompany.app.main.MainUtil.isFilmizle
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainUtil.snsJs2
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.stripHitomiTitle
import com.mycompany.app.main.MainUtil.urlInParens
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.extOf
import com.mycompany.app.main.MainUtil.setAlgorithmicDark
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.mediaNameFor
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.db.book.DbTabState.Companion.saveState
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.main.MainUtil.injectPhVideoJs
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.resetTextSizePref
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.main.MainUtil.translateUrlFor
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.restartActivity
import com.mycompany.app.main.MainUtil.setActivityOrientation
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.db.book.DbBookTab.Companion.updateParents
import com.mycompany.app.db.book.DbBookTab.Companion.deleteTabs
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.isYouTubeShorts
import com.mycompany.app.main.MainUtil.hasZtwoFlag
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.flushCookies
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.filesSubdir
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.clipboardText
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.pref.PrefWeb.Companion.saveQuickBack
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.openWithChooser
import com.mycompany.app.main.MainUtil.glideUrlFor
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.openCustomTab
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.parseTransResult
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.hideDivsByClass
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookRecent.Companion.loadItems
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.requestCamera
import com.mycompany.app.main.MainUtil.newMediaUri
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.main.MainUtil.clearCookies
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.main.MainUtil.isDarkResources
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.findMediaJs
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.main.MainUtil.checkTransJs
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
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.parseColorText
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUtil.setWindowStatusColor
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.applyIncognitoIme
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUtil.userFontResponse
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.web.WebClean.WebCleanListener
import com.mycompany.app.web.WebClean
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.app.NotificationManager
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.web.WebNestView
import android.webkit.WebViewClient
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import com.mycompany.app.web.WebNestView.WebViewListener
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.mycompany.app.web.WebViewActivity
import android.content.Intent
import android.webkit.WebChromeClient
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.quick.QuickView
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.data.book.DataBookOver
import com.mycompany.app.db.DbUtil
import com.mycompany.app.quick.QuickAdapter
import com.mycompany.app.quick.QuickView.QuickViewListener
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.data.book.DataBookAds
import android.view.MotionEvent
import com.mycompany.app.data.book.DataBookJava
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.view.MyScrollBar
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.web.WebNestFrame.WebFrameListener
import com.mycompany.app.view.MyFadeFrame
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.dialog.DialogSeekAudio
import android.view.View.OnTouchListener
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.db.book.DbBookTab

import com.mycompany.app.quick.QuickSubView
import com.mycompany.app.pull.MyPullView
import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.web.WebViewActivity.TabViewListener
import android.speech.tts.TextToSpeech
import com.mycompany.app.dialog.DialogVideoList
import com.mycompany.app.dialog.DialogSetDesk
import com.mycompany.app.web.WebViewActivity.PrevBackListener
import com.mycompany.app.dialog.DialogPopupMenu
import com.mycompany.app.dialog.DialogSetFull
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.dialog.DialogSetAdblock
import com.mycompany.app.dialog.DialogEditShort
import com.mycompany.app.dialog.DialogUrlLink
import com.mycompany.app.dialog.DialogViewRead
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.dialog.DialogEditArea
import com.mycompany.app.view.MyEditAuto
import com.mycompany.app.quick.QuickSearch
import com.mycompany.app.dialog.DialogDownPage
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.web.WebSearchAdapter.SearchItem
import android.webkit.WebView.WebViewTransport
import com.mycompany.app.web.WebBookmarksDialog.BookListListener
import com.mycompany.app.web.WebBookmarksDialog.BookInfoListener
import com.mycompany.app.dialog.DialogEditShort.EditShortListener
import com.mycompany.app.dialog.DialogUrlLink.UrlLinkListener
import com.mycompany.app.dialog.DialogDownUrl.DownPosterListener
import com.mycompany.app.view.MyGlideTarget
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideApp
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.dialog.DialogSetAdblock.DialogAdsListener
import com.mycompany.app.dialog.DialogEditArea.EditAreaListener
import com.mycompany.app.quick.QuickSearch.QuickSearchListener
import com.mycompany.app.quick.QuickSubView.QuickSubListener
import com.mycompany.app.dialog.DialogVideoList.VideoListListener
import com.mycompany.app.dialog.DialogViewRead.DialogReadListener
import com.mycompany.app.quick.QuickAdapter.QuickHolder
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.db.book.DbBookAds
import com.mycompany.app.db.book.DbBookOver
import com.mycompany.app.data.book.DataBookPop
import com.mycompany.app.db.book.DbBookPop
import com.mycompany.app.db.book.DbBookLink
import com.mycompany.app.db.book.DbBookJava
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.db.book.DbBookTmem
import com.mycompany.app.data.book.DataBookTrans
import com.mycompany.app.db.book.DbBookTrans
import com.mycompany.app.view.MyWebCoord.MyTouchListener
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.request.transition.Transition
import ch.opumlabs.opumbrowser.R
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLDecoder
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.Executors
import com.mycompany.app.web.WebViewActivity.Companion.pauseMedia
import com.mycompany.app.web.WebViewActivity.Companion.pauseWebView
import com.mycompany.app.web.WebViewActivity.Companion.homePageUrl
import com.mycompany.app.web.WebViewActivity.Companion.applyDarkSettings
import com.mycompany.app.web.WebViewActivity.Companion.checkedPageUrl
import com.mycompany.app.web.WebViewActivity.Companion.newTabUrl
import com.mycompany.app.web.WebViewActivity.Companion.tabTitleFor

/*
 * The browser activity's page callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaNextPagePauseTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val refererOrHtml: String? = null, @JvmField val isHtml: Boolean = false, @JvmField val pageIndex: Int = 0, @JvmField val childIndex: Int = 0, @JvmField val deskMode: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaNextPagePauseTask.url
        val refererOrHtml = this@WvaNextPagePauseTask.refererOrHtml
        val isHtml = this@WvaNextPagePauseTask.isHtml
        val pageIndex = this@WvaNextPagePauseTask.pageIndex
        val childIndex = this@WvaNextPagePauseTask.childIndex
        val deskMode = this@WvaNextPagePauseTask.deskMode
        val themeColor = this@WvaNextPagePauseTask.themeColor
        val themeLight = this@WvaNextPagePauseTask.themeLight
        WebViewActivity.pauseMedia(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaNextPageReleaseTask(webViewActivity, deskMode = deskMode, themeColor = themeColor, themeLight = themeLight, url = url, refererOrHtml = refererOrHtml, isHtml = isHtml, pageIndex = pageIndex, childIndex = childIndex))
    }
}

class WvaNextPageReleaseTask(@JvmField val wa: WebViewActivity, @JvmField val deskMode: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0, @JvmField val url: String? = null, @JvmField val refererOrHtml: String? = null, @JvmField val isHtml: Boolean = false, @JvmField val pageIndex: Int = 0, @JvmField val childIndex: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaNextPageReleaseTask.url
        val refererOrHtml = this@WvaNextPageReleaseTask.refererOrHtml
        val isHtml = this@WvaNextPageReleaseTask.isHtml
        val pageIndex = this@WvaNextPageReleaseTask.pageIndex
        val childIndex = this@WvaNextPageReleaseTask.childIndex
        val deskMode = this@WvaNextPageReleaseTask.deskMode
        val themeColor = this@WvaNextPageReleaseTask.themeColor
        val themeLight = this@WvaNextPageReleaseTask.themeLight
        var reusedPending = false
        if (webViewActivity.tabsState.currentFrame == null) {
            webViewActivity.destroyPendingPage()
            webViewActivity.findState.openInNextPageBusy = false
            return
        }
        val webNestView = webViewActivity.pageState.pendingPage
        if (webNestView != null) {
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody != null) {
                myWebBody.removeViewInLayout(webNestView)
            }
            val pendingPage = webViewActivity.pageState.pendingPage
            webViewActivity.currentPage = pendingPage
            webViewActivity.pageState.pendingPage = null
            pendingPage!!.setVisibility(View.VISIBLE)
            reusedPending = true
        } else {
            webViewActivity.pauseWebDestroy()
            val currentPage = WebNestView(webViewActivity)
            webViewActivity.currentPage = currentPage
            webViewActivity.attachWebDestroy(currentPage)
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaNextPageAddTask(webViewActivity, url = url, refererOrHtml = refererOrHtml, isHtml = isHtml, pageIndex = pageIndex, childIndex = childIndex, deskMode = deskMode, themeColor = themeColor, themeLight = themeLight, reusedPending = reusedPending))
    }
}

class WvaNextPageAddTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val refererOrHtml: String? = null, @JvmField val isHtml: Boolean = false, @JvmField val pageIndex: Int = 0, @JvmField val childIndex: Int = 0, @JvmField val deskMode: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0, @JvmField val reusedPending: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaNextPageAddTask.url
        val refererOrHtml = this@WvaNextPageAddTask.refererOrHtml
        val isHtml = this@WvaNextPageAddTask.isHtml
        val pageIndex = this@WvaNextPageAddTask.pageIndex
        val childIndex = this@WvaNextPageAddTask.childIndex
        val deskMode = this@WvaNextPageAddTask.deskMode
        val themeColor = this@WvaNextPageAddTask.themeColor
        val themeLight = this@WvaNextPageAddTask.themeLight
        val reusedPending = this@WvaNextPageAddTask.reusedPending
        val webNestView: WebNestView? = if (webViewActivity.tabsState.currentFrame == null) null else webViewActivity.currentPage
        if (webViewActivity.tabsState.currentFrame == null || webNestView == null) {
            webViewActivity.findState.openInNextPageBusy = false
            return
        }
        webNestView.setDeskMode(deskMode)
        webViewActivity.currentPage!!.setBarColors(themeColor, themeLight)
        val webNestFrame = webViewActivity.tabsState.currentFrame
        val currentPage = webViewActivity.currentPage
        if (webNestFrame != null) {
            webNestFrame.addPage(currentPage, pageIndex, childIndex)
        }
        webViewActivity.updateScrollBar()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaNextPageSettingsTask(webViewActivity, url = url, refererOrHtml = refererOrHtml, isHtml = isHtml, themeColor = themeColor, themeLight = themeLight, reusedPending = reusedPending))
    }
}

class WvaNextPageSettingsTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val refererOrHtml: String? = null, @JvmField val isHtml: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0, @JvmField val reusedPending: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaNextPageSettingsTask.url
        val refererOrHtml = this@WvaNextPageSettingsTask.refererOrHtml
        val isHtml = this@WvaNextPageSettingsTask.isHtml
        val themeColor = this@WvaNextPageSettingsTask.themeColor
        val themeLight = this@WvaNextPageSettingsTask.themeLight
        val reusedPending = this@WvaNextPageSettingsTask.reusedPending
        webViewActivity.applyWebSettings(webViewActivity.currentPage, 0)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaNextPageLoadTask(webViewActivity, url = url, refererOrHtml = refererOrHtml, isHtml = isHtml, themeColor = themeColor, themeLight = themeLight, reusedPending = reusedPending))
    }
}

class WvaNextPageLoadTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val refererOrHtml: String? = null, @JvmField val isHtml: Boolean = false, @JvmField val themeColor: Int = 0, @JvmField val themeLight: Int = 0, @JvmField val reusedPending: Boolean = false) : Runnable {

    internal inner class OpenNextPageStep : Runnable {

        internal inner class AttachNextPageStep : Runnable {

            internal inner class LoadNextPageStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    val url = this@WvaNextPageLoadTask.url
                    val refererOrHtml = this@WvaNextPageLoadTask.refererOrHtml
                    val isHtml = this@WvaNextPageLoadTask.isHtml
                    val themeColor = this@WvaNextPageLoadTask.themeColor
                    val themeLight = this@WvaNextPageLoadTask.themeLight
                    val reusedPending = this@WvaNextPageLoadTask.reusedPending
                    val webNestView: WebNestView? = if (webViewActivity.tabsState.currentFrame == null) null else webViewActivity.currentPage
                    if (webViewActivity.tabsState.currentFrame == null || webNestView == null) {
                        webViewActivity.findState.openInNextPageBusy = false
                        return
                    }
                    if (reusedPending) {
                        webViewActivity.dialogsState.hasRealPage = "about:blank" != url
                        webViewActivity.loadPageUrl(url, false, true)
                        webViewActivity.checkPageLanguage(true)
                        webViewActivity.applyPageScripts(true)
                    } else {
                        val resolvedUrl = webNestView.realUrl(url, false)
                        webViewActivity.loadPageUrl(resolvedUrl, false, true)
                        if (isHtml) {
                            loadHtml(webViewActivity.currentPage, resolvedUrl, refererOrHtml)
                        } else {
                            webViewActivity.dialogsState.blobPageUrl = refererOrHtml
                            webViewActivity.currentPage!!.openUrl(resolvedUrl, refererOrHtml)
                        }
                    }
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(WvaNextPageTintTask(webViewActivity, themeColor = themeColor, themeLight = themeLight))
                }
            }

            override fun run() {
                val wvaNextPageLoadTask = this@WvaNextPageLoadTask
                val webViewActivity = wa
                webViewActivity.attachPageListeners(webViewActivity.tabsState.currentFrame, webViewActivity.currentPage, 0)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(LoadNextPageStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            WebViewActivity.applyDarkSettings(webViewActivity.currentPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(AttachNextPageStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.addJsBridge(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenNextPageStep())
    }
}

class WvaAttachBackTabTask(@JvmField val wa: WebViewActivity, @JvmField val createBackTabTaskPage: com.mycompany.app.web.WebNestView? = null, @JvmField val nestFrame: com.mycompany.app.web.WebNestFrame? = null, @JvmField val backUrl: String? = null, @JvmField val ownerIndex: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView = this@WvaAttachBackTabTask.createBackTabTaskPage
        val webNestFrame = this@WvaAttachBackTabTask.nestFrame
        val backUrl = this@WvaAttachBackTabTask.backUrl
        val ownerIndex = this@WvaAttachBackTabTask.ownerIndex
        if (webNestFrame == null || webNestView == null) {
            webViewActivity.tabsState.createBackTabBusy = false
            return
        }
        webNestView.backUrlValue = backUrl
        webNestView.setWebViewClient(BackPageClient(webViewActivity))
        webNestView.setWebChromeClient(WvaWebChromeClient(webViewActivity))
        webNestFrame.setVisibility(View.INVISIBLE)
        webViewActivity.tabsState.nestView = webNestView
        webViewActivity.tabsState.backTabUrl = backUrl
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaApplyTabLimit(webViewActivity))
    }
}

class WvaSelectTabTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class SelectTabStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            WebViewActivity.pauseWebView(webViewActivity.currentPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaSelectTabReloadTask(webViewActivity))
        }
    }

    override fun run() {
        val webViewActivity = wa
        WebViewActivity.pauseMedia(webViewActivity.currentPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(SelectTabStep())
    }
}

class WvaDeleteTabsTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val tabs: ArrayList<Any?>
) : Runnable {

    internal inner class DeleteDoneStep : Runnable {

        internal inner class CloseDeleteDialogStep : Runnable {
            override fun run() {
                val webViewActivity = this@WvaDeleteTabsTask.webViewActivity
                webViewActivity.closeDeleteItemDialog()
            }
        }

        internal inner class DeleteReloadedListener : WebViewActivity.TabViewListener {

            internal inner class CloseDeleteDialogAfterReloadStep : Runnable {
                override fun run() {
                    val webViewActivity = this@WvaDeleteTabsTask.webViewActivity
                    webViewActivity.closeDeleteItemDialog()
                }
            }

            override fun onTabView() {
                val wvaDeleteTabsTask = this@WvaDeleteTabsTask
                val webViewActivity = wvaDeleteTabsTask.webViewActivity
                webViewActivity.refreshTabBar(2, true)
                val myWebCoord = wvaDeleteTabsTask.webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CloseDeleteDialogAfterReloadStep())
            }
        }

        override fun run() {
            val webViewActivity = this@WvaDeleteTabsTask.webViewActivity
            val arrayList = webViewActivity.tabsState.tabItems
            if (arrayList != null) {
                if (arrayList.size != 0) {
                    webViewActivity.reloadTabViews(DeleteReloadedListener())
                    return
                }
                webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), false, null)
                webViewActivity.refreshTabBar(2, true)
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CloseDeleteDialogStep())
            }
        }
    }

    override fun run() {
        synchronized(this.webViewActivity.tabLock) {
            val arrayList = this.tabs
            val size = arrayList.size
            var itemIndex = 0
            var index = 0
            while (index < size) {
                val entry: Any? = arrayList.get(index)
                index++
                val webTabItem = entry as WebTabItem
                val tabItems = this.webViewActivity.tabsState.tabItems
                if (tabItems == null || tabItems.isEmpty()) {
                    break
                }
                this.webViewActivity.forgetTabItem(webTabItem)
                this.webViewActivity.tabsState.tabItems!!.remove(webTabItem)
                if (webTabItem.position <= this.webViewActivity.currentTabIndex) {
                    itemIndex++
                }
            }
            val webViewActivity = this.webViewActivity
            webViewActivity.currentTabIndex -= itemIndex
            webViewActivity.clampTabIndex()
            DbBookTab.updateParents(this.webViewActivity.dialogsState.appContext, (this.webViewActivity.tabsForSave() as ArrayList<WebTabItem?>?))
            DbBookTab.deleteTabs(this.webViewActivity.dialogsState.appContext, (this.tabs as ArrayList<WebTabItem?>?))
        }
        val myWebCoord = this.webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(DeleteDoneStep())
    }
}

class WvaDeleteGroupTabsTask(@JvmField val wa: WebViewActivity, @JvmField val deletedTabs: ArrayList<WebTabItem?>? = null, @JvmField val groupTabs: ArrayList<WebTabItem?>? = null, @JvmField val deleteMode: Int = 0) : Runnable {

    internal inner class RefreshAfterDeleteStep : Runnable {

        internal inner class CloseDialogAfterRefreshStep : Runnable {
            override fun run() {
                val webViewActivity = wa
                webViewActivity.closeDeleteItemDialog()
            }
        }

        internal inner class RefreshAfterDeleteListener : WebViewActivity.TabViewListener {

            internal inner class CloseDialogAfterReloadStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    webViewActivity.closeDeleteItemDialog()
                }
            }

            override fun onTabView() {
                val wvaDeleteGroupTabsTask = this@WvaDeleteGroupTabsTask
                val webViewActivity = wa
                webViewActivity.refreshTabBar(2, true)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CloseDialogAfterReloadStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            val arrayList = webViewActivity.tabsState.tabItems
            if (arrayList != null) {
                if (arrayList.size != 0) {
                    webViewActivity.reloadTabViews(RefreshAfterDeleteListener())
                    return
                }
                webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), false, null)
                webViewActivity.refreshTabBar(2, true)
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CloseDialogAfterRefreshStep())
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        val arrayList = this@WvaDeleteGroupTabsTask.deletedTabs
        val groupTabs = this@WvaDeleteGroupTabsTask.groupTabs
        val deleteMode = this@WvaDeleteGroupTabsTask.deleteMode
        synchronized(webViewActivity.tabLock) {
            val activity = wa
            activity.tabsState.tabItems = arrayList
            activity.currentTabIndex -= deleteMode
            activity.clampTabIndex()
            DbBookTab.updateParents(wa.dialogsState.appContext, (wa.tabsForSave() as ArrayList<WebTabItem?>?))
            DbBookTab.deleteTabs(wa.dialogsState.appContext, (groupTabs))
        }
        val myWebCoord = wa.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(RefreshAfterDeleteStep())
    }
}

class WvaSaveOpenTabsTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val keepCurrent: Boolean
) : Runnable {

    internal inner class RebuildTabsStep(@JvmField val outer: WvaSaveOpenTabsTask,
                                         @JvmField val checked: Boolean
    ) : Runnable {

        internal inner class CloseDialogAfterRebuildStep : Runnable {
            override fun run() {
                val webViewActivity = this@RebuildTabsStep.outer.webViewActivity
                webViewActivity.closeDeleteItemDialog()
            }
        }

        override fun run() {
            val webViewActivity = this.outer.webViewActivity
            if (this.checked) {
                webViewActivity.addTabPlain(null, WebViewActivity.homePageUrl(), false, null)
            } else {
                webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), false, null)
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(CloseDialogAfterRebuildStep())
        }
    }

    override fun run() {
        val webViewActivity = this.webViewActivity
        DbBookTab.clearOpen(webViewActivity.dialogsState.appContext, SyncSettings.secretMode.value)
        webViewActivity.tabsState.tabItems = null
        webViewActivity.currentTabIndex = 0
        webViewActivity.clampTabIndex()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(RebuildTabsStep(this, this.keepCurrent))
    }
}

class WvaGoForwardStart(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.resetScrollNavis(true, false)
        val webNestView = webViewActivity.currentPage
        if (webNestView == null) {
            webViewActivity.tabsState.startGoForwardBusy = false
            return
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaGoForwardFinish(webViewActivity, goForwardStartFlag = webNestView.canGoForward()))
    }
}

class WvaPageViewListener(@JvmField val wa: WebViewActivity) : WebViewListener {
    /**
     * The current page stayed locked behind a dialog the web filter hid (filter/ScrollUnlock): a bar offers "Unlock
     * scrolling" (lifts the lock now and on every later visit of the site) or "Show dialog" (reloads the page once
     * without element hiding, so the dialog can be answered).
     */
    override fun onScrollStuck(view: WebNestView) {
        val activity = wa
        if (view !== activity.currentPage || activity.webCoord == null || activity.isClosing) return
        val host = MainUtil.hostOfUrl(view.getUrl(), false)
        if (host.isNullOrEmpty()) return
        activity.updateUpButton(false)
        activity.closeAllSnackbars()
        val bar = com.mycompany.app.view.MySnackbar(activity)
        activity.snackbar = bar
        bar.setAppBarView(activity.barScroll)
        bar.snackType = 5.toByte()
        bar.show(activity.webCoord, AppR.string.scroll_lock_hint, AppR.string.scroll_unlock, AppR.string.scroll_show_dialog,
            object : com.mycompany.app.view.MySnackbar.SnackbarListener {
                override fun onFirst() {
                    view.unlockScroll()
                    com.mycompany.app.web.filter.ScrollUnlock.remember(activity.dialogsState.appContext, host)
                }
                override fun onSecond() {
                    com.mycompany.app.web.filter.ScrollUnlock.showDialogOnce(host)
                    view.reload()
                }
                override fun onDismissed() {}
                override fun onDismiss() {
                    if (activity.snackbar === bar) activity.snackbar = null
                }
            })
    }

    override fun onCrashAction(action: Int) {
        val webViewActivity = wa
        val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.MainSettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", action)
        intent.putExtra("EXTRA_PATH", checkedUrl)
        webViewActivity.launchExternalForResult(intent, RequestCode.MAIN_SETTINGS)
    }

    override fun openAdvanced() {
        val webViewActivity = wa
        // SSL error blocking lives on the Privacy page since the settings were regrouped (2026-11-19)
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.SiteSafetySettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 12)
        webViewActivity.startActivity(intent)
    }

    override fun onProgress(progress: Int) {
        val webViewActivity = wa
        val runnable = webViewActivity.pageViewTask
        if (webViewActivity.currentPage == null || webViewActivity.pageState.pageViewListenerBusy) {
            return
        }
        webViewActivity.pageState.pageViewListenerBusy = true
        webViewActivity.scrollBar!!.removeCallbacks(runnable)
        webViewActivity.scrollBar!!.post(runnable)
    }

    override fun canPull(): Boolean {
        val pullMode: Byte
        val webViewActivity = wa
        val myPullView = webViewActivity.pullView
        if (myPullView == null) {
            return false
        }
        if (webViewActivity.pageState.barOffset != 0) {
            myPullView.resetSpinner()
            return false
        }
        if (sameObject(webViewActivity.pageState.pageViewListenerText, webViewActivity.dialogsState.pageUrl)) {
            pullMode = webViewActivity.pageState.pullMode
        } else {
            val pageUrl = webViewActivity.dialogsState.pageUrl
            webViewActivity.pageState.pageViewListenerText = pageUrl
            val pageHost = webViewActivity.dialogsState.pageHost
            if (TextUtils.isEmpty(pageHost)) {
                pullMode = 0
            } else if (pageHost!!.endsWith("youtube.com")) {
                if (isYouTubeShorts(pageUrl)) {
                    pullMode = 1
                } else {
                    pullMode = 0
                }
            } else if (pageHost.endsWith("pornhub.com")) {
                val length = if (pageUrl == null) 0 else pageUrl.length
                val hostIndex = if (pageUrl == null || length == 0) 0 else pageUrl.indexOf("pornhub.com/")
                val pathIndex = if (pageUrl == null || length == 0 || hostIndex == -1) 0 else pageUrl.indexOf(
                        "shorties/",
                        hostIndex + 12
                    )
                if (pageUrl == null || length == 0 || hostIndex == -1 || pathIndex == -1 || pathIndex + 9 >= length) {
                    pullMode = 0
                } else {
                    pullMode = 1
                }
            } else if (pageHost.endsWith("tiktok.com")) {
                pullMode = 1
            } else if (pageHost.endsWith("flipkart.com")) {
                pullMode = 2
            } else {
                pullMode = 0
            }
            webViewActivity.pageState.pullMode = pullMode
        }
        if (pullMode.toInt() != 1) {
            return true
        }
        webViewActivity.pullView!!.resetSpinner()
        return false
    }

    override fun onScrollStop() {
        wa.updateBarBehavior()
    }

    override fun onBlockCount(count: Int) {
        wa.addBlockCount(count, false)
    }

    override fun onPull(x: Float, y: Float, action: Int): Boolean {
        val myPullView = wa.pullView
        if (myPullView == null) {
            return false
        }
        return myPullView.onTouchStep(x, y, action)
    }

    override fun onPageCommit(pageUrl: String?) {
        val webViewActivity = wa
        webViewActivity.updateBarBehavior()
        val webNestView: WebNestView? = if (TextUtils.isEmpty(pageUrl)) null else webViewActivity.currentPage
        if (TextUtils.isEmpty(pageUrl) || webNestView == null) {
            return
        }
        webNestView.openUrl(pageUrl, null)
    }
}

class WvaPageFrameListener(@JvmField val wa: WebViewActivity) : WebFrameListener {

    internal inner class UpdateWindowBgStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.dialogsState.windowBgColor == 1234) {
                val themeColor = webViewActivity.pageThemeColor()
                webViewActivity.updateWindowBgColor(themeColor, webViewActivity.pageThemeLight())
                if (isDarkMode() && webViewActivity.dialogsState.colorsForStart != webViewActivity.onStartPage) {
                    webViewActivity.applyBodyColors(themeColor, webViewActivity.navBarColorNow())
                }
            }
            webViewActivity.resumeAddressEditAfterAdd()
            webViewActivity.refreshAfterPageChange()
        }
    }

    internal inner class CheckFramePageTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestFrame = webViewActivity.tabsState.currentFrame
            if (webNestFrame != null) {
                val pageUrl = webViewActivity.dialogsState.pageUrl
                if (webNestFrame.addPending && !webNestFrame.addHidden && ZtwoSettings.addPage.value) {
                    val tabUid = webNestFrame.tabUid
                    if (tabUid > 0) {
                        saveState(webNestFrame.getContext(), tabUid, pageUrl, null, webNestFrame.listToStr)
                    }
                }
            }
            restartActivity(webViewActivity, null, webViewActivity.onStartPage)
            webViewActivity.pageState.pageFrameListenerBusy = false
        }
    }

    override fun onFrameAnim(direction: Int, forward: Boolean) {
        val webViewActivity = wa
        if (direction == 1) {
            webViewActivity.finishTabFrameAnim()
            webViewActivity.layoutTabFrames()
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(UpdateWindowBgStep())
            return
        }
        webViewActivity.finishTabAnim(direction, forward)
        val webNestView: WebNestView? = if (!webViewActivity.onStartPage) null else webViewActivity.currentPage
        val quickView: QuickView? = if (!webViewActivity.onStartPage || webNestView == null) null else webNestView.quickPanel
        if (!webViewActivity.onStartPage || webNestView == null || quickView == null) {
            return
        }
        quickView.refreshPending = false
        if (quickView.refreshed) {
            quickView.refreshed = false
            quickView.notifyShown(false)
        }
    }

    override fun onWebRemoved(webNestView: WebNestView?, pageIndex: Int) {
        val webViewActivity = wa
        if (webViewActivity.pageState.frameShownHandled || webViewActivity.tabsState.currentFrame == null || (webNestView != webViewActivity.currentPage) || webViewActivity.videoState.pageFrameBusy) {
            return
        }
        webViewActivity.videoState.pageFrameBusy = true
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord != null) {
            myWebCoord.post(WvaPageFrameListenerTask(webViewActivity, pageFrameListenerValue = pageIndex))
        }
    }

    override fun onFrameClosed(reason: Int, toPrevious: Boolean) {
        var webViewActivity: WebViewActivity? = null
        val webNestFrame: WebNestFrame? = if (reason != 4) null else (wa.also {
                webViewActivity = it
            }).tabsState.currentFrame
        if (reason != 4 || webNestFrame == null || webViewActivity!!.currentPage == null) {
            return
        }
        if (webViewActivity.tabsState.currentFrame!!.pageItemAt(if (toPrevious) webNestFrame.pageIndex - 1 else webNestFrame.pageIndex + 1) != null) {
            webViewActivity.currentPage!!.setVisibility(View.GONE)
        }
    }

    override fun onFrameShown() {
        val webViewActivity = wa
        if (webViewActivity.pageState.frameShownHandled) {
            return
        }
        webViewActivity.pageState.frameShownHandled = true
        if (webViewActivity.pageState.pageFrameListenerBusy) {
            return
        }
        webViewActivity.pageState.pageFrameListenerBusy = true
        webViewActivity.runInBackground(CheckFramePageTask())
    }

    override fun onFrameAnimEnd(direction: Int, forward: Boolean, fromUser: Boolean) {
        val webViewActivity = wa
        if (fromUser) {
            webViewActivity.finishPageChange(direction, forward, null)
        } else {
            webViewActivity.finishTabAnim(direction, forward)
        }
    }
}

class WvaPageFrameSettingsTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        webViewActivity.applyWebSettings(webViewActivity.currentPage, 0)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaPageFrameLoadTask(webViewActivity))
    }
}

class JsBridgeRemoveTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView = webViewActivity.pageState.jsBridgePage
        webViewActivity.pageState.jsBridgePage = null
        if (webNestView == null) {
            return
        }
        webNestView.setJsAdded(false)
        webNestView.removeJavascriptInterface("android")
    }
}

class JsBridgeAddTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView = webViewActivity.pageState.jsBridgePage
        webViewActivity.pageState.jsBridgePage = null
        if (webNestView == null) {
            return
        }
        webNestView.setJsAdded(true)
        webNestView.addJavascriptInterface(WvaWebAppInterface(webViewActivity), "android")
    }
}

class WvaWebCleanListener(@JvmField val wa: WebViewActivity) : WebCleanListener {

    internal inner class ApplyWebCleanStep : Runnable {

        internal inner class RunWebCleanStep : Runnable {
            override fun run() {
                val webViewActivity = wa
                val webClean = webViewActivity.miscState.webClean
                if (webClean != null) {
                    val context = webViewActivity.dialogsState.appContext
                    val notificationManagerP: NotificationManager? = if (context == null) null else MainApp.notifications(context)
                    if (context == null || notificationManagerP == null) {
                        return
                    }
                    notificationManagerP.cancel(2147483638)
                }
            }
        }

        override fun run() {
            wa.runInBackground(RunWebCleanStep())
        }
    }

    override fun injectCss(host: String?, css: String?) {
        val activity = wa
        if (activity.currentPage == null || !sameObject(host, MainUtil.stripHostPrefix(activity.dialogsState.pageHost))) {
            return
        }
        evalJs(activity.currentPage, css, true)
        activity.addBlockCount(1, false)
    }

    /**
     * Filters finished loading: clear the loading notification, start the periodic filter update
     * when it is due, and wa.re-apply wa.ad hiding to the current page if the rules changed.
     */
    override fun filtersLoaded(notified: Boolean, changed: Boolean) {
        val activity = wa
        if (notified) {
            if (activity.dialogsState.webBody == null) {
                return
            }
            activity.dialogsState.webBody!!.postDelayed(ApplyWebCleanStep(), 1000L)
        }
        if (activity.dialogsState.webBody != null && activity.miscState.filterService == null && PdfSettings.filterTime.value != 0L && PdfSettings.filterDay.value != 0) {
            val now = System.currentTimeMillis()
            if (now >= PdfSettings.filterTime.value + (PdfSettings.filterDay.value.toLong()) * 86400000L) {
                PdfSettings.editLocal { set(PdfSettings.filterTime, now) }
                val pref = PrefPdf.get(activity.dialogsState.appContext, false)
                pref!!.putLong(PdfSettings.filterTime.value, "mFilterTime")
                pref.saveAsync()
                activity.runInBackground(WvaLoadFilterList(activity))
            }
        }
        if (changed && !activity.onStartPage && BrowserSettings.adsBlock.value && activity.dialogsState.progressBar != null && activity.currentPage != null) {
            val web = activity.currentPage
            web!!.setAdsChanged(true)
            web.startAdsClean(activity.dialogsState.pageUrl, activity.dialogsState.pageHost, activity.dialogsState.progressBar!!.drawSkipped)
            web.setAdsChanged(false)
        }
    }
}

class WvaRunJavascriptUrl(@JvmField val wa: WebViewActivity, @JvmField val bookListListenerText: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val bookListListenerText = this@WvaRunJavascriptUrl.bookListListenerText
        var script: String? = null
        if (webViewActivity.currentPage == null) {
            return
        }
        val listText: String? = if (bookListListenerText != null) bookListListenerText.trim { it <= ' ' } else null
        if (bookListListenerText != null && listText != null && listText.length >= 12 && listText.startsWith("javascript:")) {
            script = listText.substring(11)
        }
        evalJs(webViewActivity.currentPage, script, true)
    }
}

class WvaRestorePageStateTask(@JvmField val wa: WebViewActivity, @JvmField val restoreUrl: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val restoreUrl = this@WvaRestorePageStateTask.restoreUrl
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.restoreState(restoreUrl, webViewActivity.pageState.stateItem)
            webViewActivity.pageState.stateItem = null
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(WvaStartupNewTabTask(webViewActivity))
        }
    }
}

class WvaStartupNewTabTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        if (webViewActivity.pageState.startupBusy) {
            webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), false, null)
        }
        val myEditAuto: MyEditAuto? = if (webViewActivity.pageState.useAppKeypad && TtsSettings.appKeypad.value) webViewActivity.editAuto else null
        if (webViewActivity.pageState.useAppKeypad && TtsSettings.appKeypad.value && myEditAuto != null) {
            myEditAuto.postDelayed(WvaStartAddressEdit(webViewActivity), 200L)
        }
        webViewActivity.pageState.startupBusy = false
        webViewActivity.pageState.useAppKeypad = false
    }
}

class WvaLoadPageUrlTask(@JvmField val wa: WebViewActivity, @JvmField val urlChanged: Boolean = false, @JvmField val hostChanged: Boolean = false, @JvmField val videoHostEntered: Boolean = false, @JvmField val pageChanged: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val urlChanged = this@WvaLoadPageUrlTask.urlChanged
        val hostChanged = this@WvaLoadPageUrlTask.hostChanged
        val videoHostEntered = this@WvaLoadPageUrlTask.videoHostEntered
        val pageChanged = this@WvaLoadPageUrlTask.pageChanged
        if (webViewActivity.currentPage == null) {
            webViewActivity.dialogsState.lastLoadUrl = null
            return
        }
        val themeColor = webViewActivity.pageThemeColor()
        var deskMode = true
        if ((webViewActivity.quickState.editingAddress || !webViewActivity.barColorsChanged(themeColor)) && (!isDarkMode() || webViewActivity.pageState.themeOnStart == webViewActivity.onStartPage)) {
            deskMode = false
        }
        webViewActivity.pageState.themeOnStart = webViewActivity.onStartPage
        if (deskMode) {
            webViewActivity.applyThemeColors(themeColor, webViewActivity.pageThemeLight(), webViewActivity.navBarColorNow())
        }
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody == null) {
            return
        }
        myWebBody.post(WvaPageUrlChangedTask(webViewActivity, urlChanged = urlChanged, hostChanged = hostChanged, videoHostEntered = videoHostEntered, pageChanged = pageChanged))
    }
}

class WvaPageUrlChangedTask(@JvmField val wa: WebViewActivity, @JvmField val urlChanged: Boolean = false, @JvmField val hostChanged: Boolean = false, @JvmField val videoHostEntered: Boolean = false, @JvmField val pageChanged: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val urlChanged = this@WvaPageUrlChangedTask.urlChanged
        val hostChanged = this@WvaPageUrlChangedTask.hostChanged
        val videoHostEntered = this@WvaPageUrlChangedTask.videoHostEntered
        val pageChanged = this@WvaPageUrlChangedTask.pageChanged
        if (webViewActivity.currentPage == null) {
            webViewActivity.dialogsState.lastLoadUrl = null
            return
        }
        if (pageChanged) {
            if (webViewActivity.transRequested) {
                webViewActivity.resetTranslate(urlChanged)
            } else {
                webViewActivity.dialogsState.transState = 0.toByte()
                webViewActivity.transActive = false
                webViewActivity.miscState.transMsgPending = false
                webViewActivity.dialogsState.transReady = false
                webViewActivity.transLangCode = null
                webViewActivity.transText = null
                webViewActivity.transRequested = false
                if (AlbumSettings.transMode.value == 0) {
                    webViewActivity.autoTransPending = false
                }
                webViewActivity.updateTransIcon(urlChanged)
            }
        } else if (urlChanged) {
            webViewActivity.setBarAddress(webViewActivity.barAddressText())
        }
        webViewActivity.updateNavButtons()
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody == null) {
            return
        }
        myWebBody.post(WvaTextZoomNudge(webViewActivity, urlChanged = urlChanged, hostChanged = hostChanged, videoHostEntered = videoHostEntered))
    }
}

class WvaTextZoomNudge(@JvmField val wa: WebViewActivity, @JvmField val urlChanged: Boolean = false, @JvmField val hostChanged: Boolean = false, @JvmField val videoHostEntered: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val urlChanged = this@WvaTextZoomNudge.urlChanged
        val hostChanged = this@WvaTextZoomNudge.hostChanged
        val videoHostEntered = this@WvaTextZoomNudge.videoHostEntered
        val webNestView = webViewActivity.currentPage
        if (webNestView == null) {
            webViewActivity.dialogsState.lastLoadUrl = null
            return
        }
        if (webViewActivity.pageState.textZoomPending) {
            webViewActivity.pageState.textZoomPending = false
            if (ZoneSettings.textSize.value != 100) {
                webViewActivity.pageState.textZoomNudged = true
                webNestView.getSettings().setTextZoom(ZoneSettings.textSize.value - 1)
            }
        } else {
            if (!webViewActivity.pageState.textZoomNudged) {
                webViewActivity.pageState.urlChanged = urlChanged
                webViewActivity.pageState.hostChanged = hostChanged
                webViewActivity.pageState.videoHostEntered = videoHostEntered
                webViewActivity.refreshPageState()
                return
            }
            webViewActivity.pageState.textZoomNudged = false
            webNestView.getSettings().setTextZoom(ZoneSettings.textSize.value)
        }
        webViewActivity.pageState.urlChanged = urlChanged
        webViewActivity.pageState.hostChanged = hostChanged
        webViewActivity.pageState.videoHostEntered = videoHostEntered
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody == null) {
            return
        }
        myWebBody.post(WvaTextZoomNudge2(webViewActivity))
    }
}

class WvaVideoRateCheck(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class RateScriptCallback : ValueCallback<String> {
        override fun onReceiveValue(value: String?) {
            if ("true" == value) {
                return
            }
            wa.miscState.rateScriptUrl = null
        }
    }

    override fun run() {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return
        }
        webNestView.evaluateJavascript("window.yspd", RateScriptCallback())
    }
}

class WvaWebAppInterfaceTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ResumePageStep : Runnable {
        override fun run() {
            val webNestView = wa.currentPage
            if (webNestView == null) {
                return
            }
            webNestView.onResume()
        }
    }

    override fun run() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView == null || webViewActivity.dialogsState.webBody == null) {
            return
        }
        webNestView.pauseOnce()
        webViewActivity.dialogsState.webBody!!.postDelayed(ResumePageStep(), 1000L)
    }
}

class WvaCaptureTabPreviewTask(@JvmField val wa: WebViewActivity, @JvmField val captureTabPreviewPage: com.mycompany.app.web.WebNestView? = null, @JvmField val tabItem: WebTabItem? = null) : Runnable {

    internal inner class CapturePreviewStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView = this@WvaCaptureTabPreviewTask.captureTabPreviewPage
            val webTabItem = this@WvaCaptureTabPreviewTask.tabItem
            if (webTabItem == null || webNestView == null || webNestView.isNotLive()) {
                return
            }
            val thumbKey = com.mycompany.app.glide.ImageCache.key(2, webTabItem.tabUrl + webTabItem.deskMode + SyncSettings.secretMode.value)
            if (webNestView.isNotLive()) {
                return
            }
            try {
                val width = webNestView.getWidth()
                val height = webNestView.getHeight()
                if (width == 0 || height == 0) {
                    return
                }
                // only the top of the page, about the shape of an overview card (TabSnapshot.size)
                val size = TabSnapshot.size(width, height) ?: return
                val thumbWidth = size.first
                val thumbHeight = size.second
                val scrollX = webNestView.getScrollX()
                val scrollY = webNestView.getScrollY()
                val bitmapCreateBitmap =
                    Bitmap.createBitmap(thumbWidth, thumbHeight, Bitmap.Config.RGB_565)
                val canvas = Canvas(bitmapCreateBitmap)
                canvas.drawColor(if (AppState.darkWeb) -16777216 else -1)
                canvas.scale(TabSnapshot.SCALE, TabSnapshot.SCALE)
                if (scrollX != 0 || scrollY != 0) {
                    canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
                }
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.post(WvaStoreTabPreviewTask(webViewActivity, nestView = webNestView, tabId = webTabItem.uid, cacheKey = thumbKey, f12fi = bitmapCreateBitmap, canvas = canvas))
                }
            } catch (unused: Exception) {
            } catch (unused: OutOfMemoryError) {
            }
        }
    }

    override fun run() {
        wa.runInBackground(CapturePreviewStep())
    }
}

class WvaClearPageCacheTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        clearWebCache(webViewActivity.currentPage)
        webViewActivity.dialogsState.videoCheckStarted = false
    }
}

class WvaXcomSnsScript(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val pageHost = webViewActivity.dialogsState.pageHost
        if (TextUtils.isEmpty(pageHost) || pageHost != "x.com") {
            return
        }
        evalJs(webViewActivity.currentPage, "(function(){mySns(document);})();", false)
    }
}

class WvaRunStartActionTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val action: Int,
                                       @JvmField val url: String
) : Runnable {
    override fun run() {
        var titlePart: String? = null
        val webViewActivity = this.webViewActivity
        if (webViewActivity.currentPage != null) {
            if (webViewActivity.isShowingStartPage()) {
                webViewActivity.loadPageUrl("file:///android_asset/shortcut.html", false, true)
                webViewActivity.currentPage!!.setStartPage(webViewActivity.onStartPage)
                return
            }
            val action = this.action
            val url = this.url
            var targetUrl: String? = null
            if (action == 2) {
                webViewActivity.finishProgress()
                webViewActivity.currentPage!!.openUrl(url, null)
                return
            }
            if (action == 3) {
                webViewActivity.openInNextPage(url, webViewActivity.dialogsState.pageUrl, false)
                return
            }
            if (action == 4) {
                webViewActivity.addTabInGroup(null, url, true, null)
                return
            }
            if (action == 6) {
                webViewActivity.finishProgress()
                webViewActivity.openDownloadDialog(url, null, "application/pdf", 0L, 2, null)
                return
            }
            if (action == 7) {
                webViewActivity.finishProgress()
                if (!TextUtils.isEmpty(url)) {
                    if (url.startsWith("abp:subscribe")) {
                        val httpIndex = url.indexOf("http")
                        val endMark = if (httpIndex > 0 && httpIndex < url.length) url.indexOf(
                                ".txt",
                                httpIndex
                            ) else 0
                        val index = if (httpIndex > 0 && httpIndex < url.length && endMark > httpIndex) (endMark + 4) else 0
                        if (httpIndex > 0 && httpIndex < url.length && endMark > httpIndex && index <= url.length) {
                            targetUrl = url.substring(httpIndex, index)
                        }
                    } else if (url.startsWith("https://subscribe.adblockplus.org/?location=http")) {
                        val txtIndex = url.indexOf(".txt", 44)
                        val endIndex = if (txtIndex <= 44) 0 else (txtIndex + 4)
                        if (txtIndex <= 44 || endIndex > url.length) {
                            val titleIndex = url.indexOf("&title=", 44)
                            if (titleIndex > 44 && titleIndex <= url.length) {
                                titlePart = url.substring(44, titleIndex)
                            }
                        } else {
                            titlePart = url.substring(44, endIndex)
                        }
                        if (!TextUtils.isEmpty(titlePart)) {
                            try {
                                targetUrl = URLDecoder.decode(titlePart, "UTF-8")
                            } catch (unused: Exception) {
                                targetUrl = titlePart
                            }
                        }
                    }
                }
                copyTextToast(webViewActivity, AppR.string.copied_clipboard, "Copied URL", targetUrl)
                return
            }
            if (action == 8) {
                webViewActivity.finishProgress()
                webViewActivity.addBlockCount(1, true)
                return
            }
            if (action == 9) {
                val webNestView = webViewActivity.currentPage
                webNestView!!.offlineUrl = url
                webNestView.runInBackground(webNestView.LoadHtmlTask())
                return
            }
            if (action == 11) {
                webViewActivity.finishProgress()
                if (webViewActivity.isFreshPage()) {
                    webViewActivity.goBackPage(WvaPopupBlockedOnBack(webViewActivity, url))
                    return
                } else {
                    webViewActivity.showPopupBlockedSnackbar(url, null)
                    return
                }
            }
            if (action == 13) {
                webViewActivity.finishProgress()
                if (webViewActivity.isFreshPage()) {
                    webViewActivity.miscState.overrideUrl = url
                }
                webViewActivity.showAppBlockedSnackbar(url, url)
                return
            }
            if (action != 14) {
                webViewActivity.finishProgress()
                return
            }
            webViewActivity.finishProgress()
            if (webViewActivity.isFreshPage()) {
                webViewActivity.miscState.overrideUrl = url
            }
        }
    }
}

class WvaDestroyOldPage(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val page: WebNestView?
) : Runnable {
    override fun run() {
        val webViewActivity = this.webViewActivity
        webViewActivity.attachWebDestroy(webViewActivity.currentPage)
        val mainWebDestroy = webViewActivity.webDestroy
        if (mainWebDestroy != null) {
            mainWebDestroy.add(this.page)
        }
    }
}

class WvaCreatePopupPage(@JvmField val wa: WebViewActivity, @JvmField val localChromeClientTaskMessage: android.os.Message? = null) : Runnable {
    override fun run() {
        val themeColor: Int
        val themeLight: Int
        val webViewActivity = wa
        val message = this@WvaCreatePopupPage.localChromeClientTaskMessage
        var deskMode = false
        if (message == null) {
            webViewActivity.pageState.chromeClientBusy = false
            return
        }
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            deskMode = webNestView.deskMode
            themeColor = webViewActivity.pageThemeColor()
            themeLight = webViewActivity.pageThemeLight()
        } else {
            themeColor = 0
            themeLight = 0
        }
        webViewActivity.pauseWebDestroy()
        val popupPage = WebNestView(webViewActivity)
        webViewActivity.pageState.popupPage = popupPage
        webViewActivity.attachWebDestroy(popupPage)
        webViewActivity.pageState.popupPage!!.setDeskMode(deskMode)
        webViewActivity.pageState.popupPage!!.setBarColors(themeColor, themeLight)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCreatePopupPage2(webViewActivity, createPopupPageMessage = message))
    }
}

class WvaCreatePopupPage2(@JvmField val wa: WebViewActivity, @JvmField val createPopupPageMessage: android.os.Message? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val message = this@WvaCreatePopupPage2.createPopupPageMessage
        val webNestView: WebNestView? = if (message == null) null else webViewActivity.pageState.popupPage
        if (message == null || webNestView == null) {
            webViewActivity.pageState.chromeClientBusy = false
            return
        }
        webNestView.setVisibility(View.INVISIBLE)
        webViewActivity.dialogsState.webBody!!.addViewQuick(webViewActivity.pageState.popupPage, RelativeLayout.LayoutParams(-1, -1))
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCreatePopupPage3(webViewActivity, createPopupPage2Message = message))
    }
}

class WvaCreatePopupPage3(@JvmField val wa: WebViewActivity, @JvmField val createPopupPage2Message: android.os.Message? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val message = this@WvaCreatePopupPage3.createPopupPage2Message
        val webNestView: WebNestView? = if (message == null) null else webViewActivity.pageState.popupPage
        if (message == null || webNestView == null) {
            webViewActivity.pageState.chromeClientBusy = false
            return
        }
        webViewActivity.applyWebSettings(webNestView, 2)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaPopupTransport(webViewActivity, createPopupPage3Message = message))
    }
}

class WvaPopupTransport(@JvmField val wa: WebViewActivity, @JvmField val createPopupPage3Message: android.os.Message? = null) : Runnable {

    internal inner class CreatePopupStep : Runnable {

        internal inner class AttachPopupStep : Runnable {

            internal inner class SendPopupMessageStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    val message = this@WvaPopupTransport.createPopupPage3Message
                    val webNestView: WebNestView? = if (message == null) null else webViewActivity.pageState.popupPage
                    if (message == null || webNestView == null) {
                        webViewActivity.pageState.chromeClientBusy = false
                        return
                    }
                    try {
                        val webViewTransport = message.obj as WebViewTransport?
                        if (webViewTransport != null) {
                            webViewTransport.setWebView(webNestView)
                            message.sendToTarget()
                        }
                    } catch (unused: Exception) {
                    }
                    webViewActivity.pageState.chromeClientBusy = false
                }
            }

            override fun run() {
                val wvaPopupTransport = this@WvaPopupTransport
                val webViewActivity = wa
                webViewActivity.attachPageListeners(null, webViewActivity.pageState.popupPage, 2)
                try {
                    val activity = wa
                    val webNestView = activity.pageState.popupPage
                    if (webNestView != null) {
                        webNestView.setWebViewClient(WvaPopupTransport2(activity))
                        activity.pageState.popupPage!!.setWebChromeClient(WvaPopupTransport3(activity))
                        activity.pageState.popupPage!!.setDownloadListener(PopupDownloadListener(activity))
                    }
                } catch (unused: Exception) {
                }
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(SendPopupMessageStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            WebViewActivity.applyDarkSettings(webViewActivity.pageState.popupPage)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(AttachPopupStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.addJsBridge(webViewActivity.pageState.popupPage)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(CreatePopupStep())
    }
}

class WvaPopupTransport3(@JvmField val wa: WebViewActivity) : WebChromeClient() {
    override fun onCloseWindow(webView: WebView?) {
        if (webView != null) {
            val webViewActivity = wa
            if (webView == webViewActivity.pageState.popupPage) {
                webViewActivity.destroyPopupPage()
            }
        }
    }

    override fun onProgressChanged(webView: WebView?, newProgress: Int) {
        val webViewActivity = wa
        if (webViewActivity.pageState.popupPage == null) {
            return
        }
        webViewActivity.updatePageProgress(newProgress)
    }
}

class WvaFileChooserStart(@JvmField val wa: WebViewActivity) : Runnable {

    override fun run() {
        val webViewActivity = wa
        val valueCallback = webViewActivity.pageState.valueCallback
        val fileChooserParams = webViewActivity.pageState.fileChooserParams
        webViewActivity.pageState.valueCallback = valueCallback
        webViewActivity.pageState.fileChooserParams = fileChooserParams
        webViewActivity.deliverFileChooserResult(null)
        webViewActivity.fileChooserCallback = valueCallback
        webViewActivity.fileChooserCallback2 = null
        webViewActivity.fileChooserCallback3 = null
        var index = 0
        val acceptTypes: Array<String?>? = if (fileChooserParams != null) fileChooserParams.getAcceptTypes() else null
        if (fileChooserParams != null && acceptTypes != null && acceptTypes.size > 0) {
            val length = acceptTypes.size
            var acceptMask = 0
            while (index < length) {
                var acceptType = acceptTypes[index]
                if (acceptType != null) {
                    if (acceptType.startsWith(".")) {
                        acceptType = mimeForExt(acceptType)
                    }
                    if (acceptType == null) {
                        acceptMask = acceptMask or 16
                    } else if (acceptType.startsWith("image")) {
                        acceptMask = acceptMask or 2
                    } else if (acceptType.startsWith("video")) {
                        acceptMask = acceptMask or 4
                    } else if (acceptType.startsWith("audio") || acceptType == "application/ogg" || acceptType == "application/x-ogg") {
                        acceptMask = acceptMask or 8
                    } else {
                        acceptMask = acceptMask or 16
                    }
                }
                index++
            }
            index = if (acceptMask == 16) acceptMask or 2 else acceptMask
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaFileChooserTask(webViewActivity, fileChooserStartValue = index))
    }
}

class WvaTabPopupListener(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val start: Int,
                                       @JvmField val count: Int,
                                       @JvmField val isGroup: Boolean
) : MyPopupListener {
    override fun onClose() {
        this.webViewActivity.closeFloatingPopupMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        val webViewActivity = this.webViewActivity
        webViewActivity.closeMenuDialogs()
        val count = this.count
        if (id == 0) {
            webViewActivity.closeTabBarSubView()
            webViewActivity.showDeleteTabsDialog(this.start, count, 3)
            return true
        }
        if (id == 1) {
            webViewActivity.closeTabBarSubView()
            if (this.isGroup) {
                webViewActivity.showDeleteTabsDialog(0, count, 2)
                return true
            }
            webViewActivity.showDeleteTabsDialog(0, count, 1)
            return true
        }
        if (id == 2) {
            webViewActivity.closeTabBarSubView()
            webViewActivity.showDeleteTabsDialog(0, 0, 0)
            return true
        }
        if (id == 3) {
            if (webViewActivity.tabBar != null) {
                webViewActivity.closeTabBarSubView()
                webViewActivity.currentTabIndex = count
                val webTabItemE = webViewActivity.tabBar!!.shownAt(count)
                webViewActivity.addTabPlain(
                    null,
                    if (webTabItemE == null) null else webTabItemE.tabUrl,
                    true,
                    null
                )
                return true
            }
        } else if (id == 4) {
            if (webViewActivity.tabBar != null) {
                webViewActivity.closeTabBarSubView()
                webViewActivity.currentTabIndex = count
                val webTabItemE = webViewActivity.tabBar!!.shownAt(count)
                webViewActivity.openInNewTab(if (webTabItemE == null) null else webTabItemE.tabUrl, null)
                return true
            }
        } else if (id == 5) {
            if (webViewActivity.tabBar != null) {
                webViewActivity.closeTabBarSubView()
                val webTabItemE = webViewActivity.tabBar!!.shownAt(count)
                webViewActivity.createBackTab(if (webTabItemE != null) webTabItemE.tabUrl else null)
                return true
            }
        } else if (id == 6) {
            val webTabBarAdapter = webViewActivity.tabBar
            if (webTabBarAdapter != null) {
                val webTabItemE = webTabBarAdapter.shownAt(count)
                copyTextToast(
                    webViewActivity,
                    AppR.string.copied_clipboard,
                    "Copied URL",
                    if (webTabItemE != null) webTabItemE.tabUrl else null
                )
                return true
            }
        } else if (id == 7) {
            val webTabBarAdapter = webViewActivity.tabBar
            val webTabItemE: WebTabItem? = if (webTabBarAdapter != null) webTabBarAdapter.shownAt(count) else null
            if (webTabBarAdapter != null && webTabItemE != null) {
                webViewActivity.closeTabBarSubView()
                webViewActivity.openIntentUrl(webTabItemE.tabUrl, WebViewActivity.tabTitleFor(webViewActivity.dialogsState.appContext, webTabItemE))
                return true
            }
        } else if (id == 8 && webViewActivity.tabBar != null) {
            webViewActivity.closeTabBarSubView()
            val webTabItemE = webViewActivity.tabBar!!.shownAt(count)
            var tabUrl = if (webTabItemE != null) webTabItemE.tabUrl else null
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                tabUrl = webNestView.displayUrl(tabUrl)
            }
            webViewActivity.downloadLink(tabUrl)
        }
        return true
    }
}

class WvaQuickSubMenuListener(@JvmField val wa: WebViewActivity) : MyPopupListener {

    internal inner class LaunchExternalStep : Runnable {

        internal inner class OpenBackupPickerStep : Runnable {
            override fun run() {
                val wvaQuickSubMenuListener = this@WvaQuickSubMenuListener
                if (wa.dialogsState.appContext == null) {
                    return
                }
                wa.launchExternalForResult(
                    Intent(
                        wa.dialogsState.appContext,
                        com.mycompany.app.ui.viewer.BackgroundImageActivity::class.java as Class<*>
                    ), RequestCode.IMAGE_FILE
                )
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.closeQuickSettingsMenu()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(OpenBackupPickerStep())
        }
    }

    internal inner class BackupDirStep : Runnable {

        internal inner class BackupDirResultStep : Runnable {

            internal inner class ApplyBackupDirStep : Runnable {
                override fun run() {
                    val context = wa.dialogsState.appContext
                    val portraitPath = backupDirPath(context, SyncSettings.secretMode.value, false)
                    val landscapePath = backupDirPath(context, SyncSettings.secretMode.value, true)
                    if (!TextUtils.isEmpty(portraitPath)) {
                        try {
                            com.mycompany.app.glide.ImageCache.remove(com.mycompany.app.glide.ImageCache.key(0, portraitPath))
                        } catch (unused: Exception) {
                        }
                    }
                    if (!TextUtils.isEmpty(landscapePath)) {
                        try {
                            com.mycompany.app.glide.ImageCache.remove(com.mycompany.app.glide.ImageCache.key(0, landscapePath))
                        } catch (unused2: Exception) {
                        }
                    }
                    if (!TextUtils.isEmpty(portraitPath)) {
                        try {
                            MainUtil.deleteUri(context, portraitPath)
                        } catch (unused3: Exception) {
                        }
                    }
                    if (TextUtils.isEmpty(landscapePath)) {
                        return
                    }
                    try {
                        MainUtil.deleteUri(context, landscapePath)
                    } catch (unused4: Exception) {
                    }
                }
            }

            override fun run() {
                val wvaQuickSubMenuListener = this@WvaQuickSubMenuListener
                val context = wa.dialogsState.appContext
                if (context == null) {
                    return
                }
                saveQuickBack(context, false)
                wa.onThemeChanged(true)
                wa.runInBackground(ApplyBackupDirStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.closeQuickSettingsMenu()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(BackupDirResultStep())
        }
    }

    override fun onClose() {
        wa.closeSubMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        val webViewActivity = wa
        if (id == 0) {
            webViewActivity.closeSubMenu()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(LaunchExternalStep())
            return false
        }
        if (!isDarkMode()) {
            return true
        }
        webViewActivity.closeSubMenu()
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return true
        }
        handler.post(BackupDirStep())
        return false
    }
}

class WvaBookListListener(@JvmField val wa: WebViewActivity) : BookListListener {

    internal inner class BookActionStep(
                                         @JvmField val bookUrl: String?, stateParam: Int
    ) : Runnable {
        val state: Byte

        init {
            this.state = stateParam.toByte()
        }

        override fun run() {
            val webViewActivity = wa
            val bookUrl = this.bookUrl
            var isScript = false
            val trimmedUrl: String? = if (bookUrl != null) bookUrl.trim { it <= ' ' } else null
            if (bookUrl != null && trimmedUrl != null && trimmedUrl.length >= 12) {
                isScript = trimmedUrl.startsWith("javascript:")
            }
            if (isScript) {
                webViewActivity.runInBackground(WvaRunJavascriptUrl(webViewActivity, bookListListenerText = bookUrl))
                return
            }
            val state = this.state
            if (state.toInt() == 3) {
                webViewActivity.addTabPlain(null, bookUrl, true, null)
                return
            }
            if (state.toInt() == 4) {
                webViewActivity.openInNewTab(bookUrl, null)
            } else if (state.toInt() == 5) {
                webViewActivity.createBackTab(bookUrl)
            } else if (hasZtwoFlag(16)) {
                webViewActivity.addTabPlain(null, bookUrl, true, null)
            } else {
                webViewActivity.openInCurrentPage(bookUrl, null)
            }
        }
    }

    // WebBookmarksDialog.BookListListener
    override fun onPick(path: String?) {
    }

    // WebBookmarksDialog.BookListListener
    override fun onAction(action: Int, path: String?) {
        val webViewActivity = wa
        webViewActivity.closeBookmarksDialog()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(BookActionStep(path, action))
    }
}

class WvaBookInfoListener(@JvmField val wa: WebViewActivity) : BookInfoListener {
    // WebBookmarksDialog.BookInfoListener
    override val icon: Bitmap?
        get() {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return null
        }
        return webNestView.getFavicon()
    }

    // WebBookmarksDialog.BookInfoListener
    override val title: String?
        get() {
        val webViewActivity = wa
        return webViewActivity.pageTitleOf(webViewActivity.currentPage, webViewActivity.dialogsState.pageUrl)
    }

    // WebBookmarksDialog.BookInfoListener
    override val url: String?
        get() {
        return wa.currentPageUrl(true)
    }
}

class WvaHistoryListListener(@JvmField val wa: WebViewActivity) : ListBookListener {

    internal inner class HistoryActionStep(
                                         byteValueParam: Int,
                                         @JvmField val childItem: ChildItem
    ) : Runnable {
        val byteValue: Byte

        init {
            this.byteValue = byteValueParam.toByte()
        }

        override fun run() {
            val webViewActivity = wa
            val childItem = this.childItem
            val byteValue = this.byteValue
            if (byteValue.toInt() == 3) {
                val path = childItem.path
                webViewActivity.addTabPlain(null, path, true, null)
            } else if (byteValue.toInt() == 4) {
                val path = childItem.path
                webViewActivity.openInNewTab(path, null)
            } else if (byteValue.toInt() == 5) {
                val path = childItem.path
                webViewActivity.createBackTab(path)
            } else if (hasZtwoFlag(32)) {
                webViewActivity.addTabPlain(null, childItem.path, true, null)
            } else {
                webViewActivity.openInCurrentPage(childItem.path, null)
            }
        }
    }

    override fun onItem(position: Int, childItem: ChildItem?, action: Int) {
        val webViewActivity = wa
        webViewActivity.closeHistoryDialog()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(HistoryActionStep(action, childItem!!))
    }

    override fun onClosed() {
    }
}

class WvaEditShortListener(@JvmField val wa: WebViewActivity) : EditShortListener {
    override fun onCreated() {
        wa.dialogsState.leavingByIntent = true
    }

    override val icon: Bitmap?
        get() {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return null
        }
        return webNestView.getFavicon()
    }
}

class WvaUrlLinkListener(@JvmField val wa: WebViewActivity) : UrlLinkListener {
    override fun onAction(action: Int, url: String?, label: String?) {
        wa.onUrlLinkAction(action, url, label)
    }

    override fun barHeight(): Int {
        val webViewActivity = wa
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody == null) {
            return 0
        }
        if (MainConst.isApi30) {
            return myWebBody.rectHeight
        }
        val sizeItemD = screenSize(webViewActivity)
        if (sizeItemD == null) {
            return 0
        }
        return sizeItemD.height
    }

    override fun runAction(url: String?, action: Int, type: Int, referer: String?, isImage: Boolean, title: String?) {
        wa.runLinkAction(url, action, type, referer, isImage, title)
    }

    override fun linkUrl(): String? {
        return wa.quickState.longPressVideo
    }
}

class WvaOpenTextViewerTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val title: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaOpenTextViewerTask.url
        val title = this@WvaOpenTextViewerTask.title
        if (webViewActivity.currentPage != null) {
            val pageLink = webViewActivity.resolvePageLink(url)
            val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.viewer.TextViewerActivity::class.java as Class<*>)
            intent.putExtra("EXTRA_PATH", url)
            intent.putExtra("EXTRA_REFERER", pageLink)
            intent.putExtra("EXTRA_NAME", title)
            webViewActivity.startActivity(intent)
        }
        webViewActivity.miscState.openTextViewerBusy = false
    }
}

class WvaDownPosterListener(@JvmField val wa: WebViewActivity) : DownPosterListener {
    override fun posterUrl(url: String?): String? {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return null
        }
        return webNestView.videoPoster(webNestView.videoIndex(url))
    }
}

class WvaLoadImageForOpenTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val isOpen: Boolean
) : Runnable {

    internal inner class FileTarget : MyGlideTarget<File>() {

        internal inner class OpenFileStep : Runnable {
            override fun run() {
                val wvaLoadImageForOpenTask = this@WvaLoadImageForOpenTask
                val webViewActivity = wvaLoadImageForOpenTask.webViewActivity
                webViewActivity.closeDownUrlDialog()
                wvaLoadImageForOpenTask.webViewActivity.requestOpenTypeDialog(0L, 4, null, null, 1)
            }
        }

        override fun onResourceReady(resource: File, transition: Transition<in File>?) {
            val file = resource as File?
            val webViewActivity = this@WvaLoadImageForOpenTask.webViewActivity
            val imageOpenUrl = webViewActivity.pageState.imageOpenUrl
            val imageOpenReferer = webViewActivity.pageState.imageOpenReferer
            webViewActivity.pageState.imageOpenUrl = null
            webViewActivity.pageState.imageOpenReferer = null
            if (webViewActivity.dialogsState.appContext != null) {
                webViewActivity.dialogsState.openImagePath = imageOpenReferer
                webViewActivity.dialogsState.openImageFile = file
                webViewActivity.dialogsState.openImageBitmap = null
                webViewActivity.runInBackground(WvaOpenLoadedImage(webViewActivity))
            }
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.addDownloadedVideo(imageOpenUrl, false)
            }
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            val webViewActivity = this@WvaLoadImageForOpenTask.webViewActivity
            val imageOpenUrl = webViewActivity.pageState.imageOpenUrl
            webViewActivity.pageState.imageOpenUrl = null
            webViewActivity.pageState.imageOpenReferer = null
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.addFailedImage(imageOpenUrl)
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(OpenFileStep())
        }
    }

    internal inner class BitmapTarget : MyGlideTarget<Bitmap>() {

        internal inner class OpenImageStep : Runnable {
            override fun run() {
                val wvaLoadImageForOpenTask = this@WvaLoadImageForOpenTask
                val webViewActivity = wvaLoadImageForOpenTask.webViewActivity
                webViewActivity.closeDownUrlDialog()
                wvaLoadImageForOpenTask.webViewActivity.requestOpenTypeDialog(0L, 4, null, null, 1)
            }
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val bitmap = resource as Bitmap?
            val webViewActivity = this@WvaLoadImageForOpenTask.webViewActivity
            val imageOpenUrl = webViewActivity.pageState.imageOpenUrl
            val imageOpenReferer = webViewActivity.pageState.imageOpenReferer
            webViewActivity.pageState.imageOpenUrl = null
            webViewActivity.pageState.imageOpenReferer = null
            if (webViewActivity.dialogsState.appContext != null) {
                webViewActivity.dialogsState.openImagePath = imageOpenReferer
                webViewActivity.dialogsState.openImageFile = null
                webViewActivity.dialogsState.openImageBitmap = bitmap
                webViewActivity.runInBackground(WvaOpenLoadedImage(webViewActivity))
            }
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.addDownloadedVideo(imageOpenUrl, false)
            }
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            val webViewActivity = this@WvaLoadImageForOpenTask.webViewActivity
            val imageOpenUrl = webViewActivity.pageState.imageOpenUrl
            webViewActivity.pageState.imageOpenUrl = null
            webViewActivity.pageState.imageOpenReferer = null
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.addFailedImage(imageOpenUrl)
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(OpenImageStep())
        }
    }

    override fun run() {
        val webViewActivity = this.webViewActivity
        val imageOpenUrl = webViewActivity.pageState.imageOpenUrl
        val isOpen = this.isOpen
        if (!isOpen) {
            val glideRequestO = (GlideApp.with(webViewActivity)!!.asBitmap().load(imageOpenUrl) as GlideRequest<*>).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            @Suppress("UNCHECKED_CAST")
            (glideRequestO as GlideRequest<Any?>).into(BitmapTarget() as com.bumptech.glide.request.target.Target<Any?>)
        } else {
            val glideRequestO = (GlideApp.with(webViewActivity)!!.downloadOnly().load(
                MainUtil.glideUrlFor(
                    webViewActivity.dialogsState.appContext,
                    imageOpenUrl,
                    webViewActivity.dialogsState.pageUrl
                )
            ) as GlideRequest<*>).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            @Suppress("UNCHECKED_CAST")
            (glideRequestO as GlideRequest<Any?>).into(FileTarget() as com.bumptech.glide.request.target.Target<Any?>)
        }
    }
}

class WvaBlobDownloadScript(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val mime: String? = null, @JvmField val disposition: String? = null, @JvmField val length: Long = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaBlobDownloadScript.url
        val mime = this@WvaBlobDownloadScript.mime
        val disposition = this@WvaBlobDownloadScript.disposition
        val length = this@WvaBlobDownloadScript.length
        val webNestView = webViewActivity.currentPage
        if (webNestView == null) {
            return
        }
        val sbW = StringBuilder("(function(){var xhr=new XMLHttpRequest();xhr.open('GET','").append(url).append("',true);xhr.responseType='blob';xhr.onload=function(){var sblnk='").append(url).append("';if(this.status==200){sblnk=URL.createObjectURL(this.response);var sbblb=document.getElementById('sb_down_blob');if(sbblb){document.body.removeChild(sbblb);}sbblb=document.createElement('a');sbblb.href=sblnk;sbblb.id='sb_down_blob';sbblb.style='display:none';document.body.appendChild(sbblb);}android.onBlobDown(sblnk,'")
        sbW.append(mime).append("','").append(disposition).append("','")
        sbW.append(length)
        sbW.append("');};xhr.send();})();")
        evalJs(webNestView, sbW.toString(), true)
    }
}

class WvaCoverViewCheck(@JvmField val wa: WebViewActivity, @JvmField val scrollRange: Int = 0) : Runnable {

    internal inner class ShowCaptureDialogStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.runInBackground(WvaShowCaptureDialog(webViewActivity, coverViewCheckBusy = true))
        }
    }

    override fun run() {
        val webViewActivity = wa
        val webNestView: WebNestView? = if (webViewActivity.coverView == null) null else webViewActivity.currentPage
        if (webViewActivity.coverView == null || webNestView == null) {
            webViewActivity.removeCoverView()
            return
        }
        if (this@WvaCoverViewCheck.scrollRange != webNestView.computeVerticalScrollRange()) {
            webViewActivity.finishCoverView()
            return
        }
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody == null) {
            return
        }
        myWebBody.postDelayed(ShowCaptureDialogStep(), 200L)
    }
}

class WvaRestoreScroll(@JvmField val wa: WebViewActivity, @JvmField val coverScrollY: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val coverScrollY = this@WvaRestoreScroll.coverScrollY
        val webNestView = webViewActivity.currentPage
        if (webNestView == null || coverScrollY == webNestView.getScrollY()) {
            return
        }
        webViewActivity.currentPage!!.scrollTo(0, coverScrollY)
    }
}

class WvaDownPageListener(@JvmField val wa: WebViewActivity) : DownPageListener {
    override fun onSave(url: String?, path: String?, bitmap: Bitmap?) {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView == null) {
            return
        }
        webViewActivity.currentPage!!.saveWebArchive(
            filesSubdir(
                webViewActivity.dialogsState.appContext,
                ".page"
            ) + "/" + System.currentTimeMillis() + ".mht",
            false,
            WvaDownPageResult(webViewActivity, pageUrl = url, path = path, icon = webNestView.getFavicon())
        )
    }
}

class WvaApplyTransLangTask(@JvmField val outer: WebViewActivity,
                                       @JvmField val lang: String?
) : Runnable {
    override fun run() {
        evalLongPressJs(this.outer.currentPage, this.lang)
    }
}

class WvaTransScriptTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val isShown: Boolean
) : Runnable {
    override fun run() {
        val sbK = MainUtil.transScriptJs()
        val webViewActivity = this.webViewActivity
        var script: String? = null
        if (sbK != null) {
            if (this.isShown) {
                script = sbK.toString()
            } else {
                val classJs = classSelectorJs(webViewActivity.pageLangCode)
                if (!TextUtils.isEmpty(classJs)) {
                    sbK.insert(0, classJs)
                    script = sbK.toString()
                }
            }
        }
        evalJs(webViewActivity.currentPage, script, true)
    }
}

class WvaParseTransResultTask(@JvmField val wa: WebViewActivity, @JvmField val html: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val html = this@WvaParseTransResultTask.html
        if (webViewActivity.currentPage == null) {
            return
        }
        parseTransResult(webViewActivity.dialogsState.appContext, html)
    }
}

class WvaLangPrefixTask(@JvmField val wa: WebViewActivity, @JvmField val html: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val html = this@WvaLangPrefixTask.html
        if (webViewActivity.currentPage == null || !TextUtils.isEmpty(webViewActivity.pageLangCode)) {
            return
        }
        val prefix = langPrefix(html)
        webViewActivity.pageLangCode = prefix
        if (prefix.length > 2) {
            hideDivsByClass(webViewActivity.currentPage, prefix)
        }
    }
}

class WvaPopupMenuListener(@JvmField val wa: WebViewActivity) : DialogPopupMenu.PopupMenuListener {

    internal inner class PopupTabAdded : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            wa.videoState.pendingTabUrl = null
        }
    }

    internal inner class ClosePopupMenuStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.closePopupMenuDialog()
        }
    }

    override fun onItem(id: Int, url: String?, referer: String?) {
        val webViewActivity = wa
        if (id == 0) {
            webViewActivity.closePopupMenuDialog()
            webViewActivity.showWebViewDialog(url, 1, referer, false)
            return
        }
        if (id == 1) {
            webViewActivity.videoState.pendingTabUrl = referer
            webViewActivity.addTabInGroup(null, url, true, PopupTabAdded())
            webViewActivity.closePopupMenuDialog()
            return
        }
        if (id == 2) {
            webViewActivity.closePopupMenuDialog()
            webViewActivity.openLoadDialogFor(url, true)
        } else if (id == 3) {
            webViewActivity.closePopupMenuDialog()
            webViewActivity.openEmgGallery(url, true)
        } else if (id == 4) {
            webViewActivity.showAllowPopupDialog()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ClosePopupMenuStep())
        }
    }
}

class WvaRunLinkActionListener(@JvmField val wa: WebViewActivity) : DialogAdsListener {

    internal inner class RunLinkActionStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val runLinkActionListenerText = webViewActivity.pageState.runLinkActionListenerText
            webViewActivity.pageState.runLinkActionListenerText = null
            WebClean.Companion.removeImages(webViewActivity.currentPage, runLinkActionListenerText)
        }
    }

    override fun onApply(
        changed: Boolean,
        jsChanged: Boolean,
        listChanged: Boolean,
        userChanged: Boolean,
        refreshPage: Boolean,
        pageUrl: String?
    ) {
        val noPageUrl = TextUtils.isEmpty(pageUrl)
        val webViewActivity = wa
        if (!noPageUrl) {
            webViewActivity.pageState.runLinkActionListenerText = pageUrl
            webViewActivity.runInBackground(RunLinkActionStep())
        }
        webViewActivity.onAdsDialogResult(jsChanged, listChanged, userChanged, refreshPage)
    }
}

class WvaRemoveAreaToolbar(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webAreaView = webViewActivity.dialogsState.areaToolbar
        if (webAreaView != null) {
            webAreaView.release()
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody != null) {
                myWebBody.removeView(webViewActivity.dialogsState.areaToolbar)
            }
            webViewActivity.dialogsState.areaToolbar = null
            webViewActivity.dialogsState.areaReinjectScriptText = null
            webViewActivity.areaScript = null
            webViewActivity.dialogsState.areaToolbarListenerText = null
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.setOnTouchListener(null)
            }
        }
    }
}

class WvaStartAreaSelect(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class StartAreaSelectStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView: WebNestView? = if (webViewActivity.dialogsState.areaToolbar != null) webViewActivity.currentPage else null
            if (webViewActivity.dialogsState.areaToolbar != null && webNestView != null) {
                webNestView.setOnTouchListener(WvaAreaPageTouch(webViewActivity))
            }
            webViewActivity.dialogsState.areaSelectBusy = false
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar != null) {
            val areaJs = areaScriptJs(webViewActivity.areaToolbarOn)
            if (!TextUtils.isEmpty(areaJs) && webViewActivity.dialogsState.areaToolbar != null) {
                evalJs(webViewActivity.currentPage, areaJs, true)
            }
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(StartAreaSelectStep())
    }
}

class WvaAreaPageTouch(@JvmField val wa: WebViewActivity) : OnTouchListener {
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        val actionMasked = event!!.getActionMasked()
        val webViewActivity = wa
        if (actionMasked == 0) {
            if (webViewActivity.dialogsState.areaToolbar != null) {
                evalJs(webViewActivity.currentPage, "window.sbtouch=true;", true)
                return false
            }
        } else if (actionMasked == 1) {
            if (webViewActivity.dialogsState.areaToolbar != null) {
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.postDelayed(WvaAreaReinjectScript(webViewActivity, areaPageTouchBusy = true), 200L)
                    return false
                }
            }
        }
        return false
    }
}

class WvaEditAreaListener(@JvmField val wa: WebViewActivity) : EditAreaListener {
    override fun onApply(host: String?, cmd: String?, script: String?) {
        if (TextUtils.isEmpty(cmd)) {
            return
        }
        val webViewActivity = wa
        // the element picker saved a user rule: the uBlock Origin-compatible engine picks it up
        com.mycompany.app.web.filter.UboFilter.onListsChanged(webViewActivity.dialogsState.appContext)
        evalJs(webViewActivity.currentPage, script, true)
    }
}

class WvaQuickSearchEditListener(@JvmField val wa: WebViewActivity) : QuickSearchListener {
    override fun webBody(): MyWebBody? {
        return wa.dialogsState.webBody
    }

    override fun setQuery(cursor: Int, query: String?) {
        val myEditAuto = wa.editAuto
        if (myEditAuto == null) {
            return
        }
        myEditAuto.setTextAt(cursor, query)
    }

    override fun onItemClick(searchItem: SearchItem?) {
        wa.onSearchItemClick(searchItem)
    }

    override fun openText(input: String?) {
        wa.finishAddressEdit(input)
    }

    override fun fillText(input: String?) {
        val myEditAuto: MyEditAuto? = if (TextUtils.isEmpty(input)) null else wa.editAuto
        if (TextUtils.isEmpty(input) || myEditAuto == null) {
            return
        }
        myEditAuto.setTextAt(input!!.length, input)
    }

    override fun onClose() {
        val webViewActivity = wa
        webViewActivity.pageState.addressEditBusy = false
        webViewActivity.setAddressEditing(false)
    }

    override fun openUrl(type: Int, url: String?) {
        val webViewActivity = wa
        if (webViewActivity.currentPage != null) {
            webViewActivity.pageState.addressEditBusy = false
            webViewActivity.setAddressEditing(false)
            if (type == 0) {
                if (TextUtils.isEmpty(url)) {
                    return
                }
                if (hasZtwoFlag(8)) {
                    webViewActivity.addTabPlain(null, url, true, null)
                    return
                } else {
                    webViewActivity.openInCurrentPage(url, null)
                    return
                }
            }
            if (type == 1) {
                val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.lists.QuickAddActivity::class.java as Class<*>)
                if (!webViewActivity.onStartPage) {
                    val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
                    intent.putExtra("EXTRA_PATH", checkedUrl)
                    intent.putExtra("EXTRA_NAME", webViewActivity.pageTitleOf(webViewActivity.currentPage, checkedUrl))
                }
                webViewActivity.launchExternalForResult(intent, RequestCode.QUICK_ADD)
            }
        }
    }

    override fun openEngine(engine: Int) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.showQuickSchEdit(engine, true)
        webViewActivity.pageState.addressEditBusy = false
        webViewActivity.setAddressEditing(false)
    }

    override fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.openQuickFolder(type, left, top, path, color)
    }
}

class WvaShowQuickSearch(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView: WebNestView? = if (!webViewActivity.quickState.editingAddress) null else webViewActivity.currentPage
        if (!webViewActivity.quickState.editingAddress || webNestView == null) {
            return
        }
        webNestView.setQuickSearch(true)
        webViewActivity.runInBackground(WvaQuickRecentRefresh(webViewActivity))
    }
}

class WvaQuickSubListener(@JvmField val wa: WebViewActivity) : QuickSubListener {
    override fun isLandscape(): Boolean {
        return wa.isLandscapeScreen()
    }

    override fun setBarsDisabled(disabled: Boolean) {
        val webViewActivity = wa
        webViewActivity.setBarsDisabled(3, disabled)
        if (disabled) {
            webViewActivity.updateRssButton(false)
        }
    }

    override fun closeDeleteDialog() {
        wa.closeQuickDeleteDialog()
    }

    override fun showEditDialog(quickItem: QuickItem?) {
        wa.showQuickEditDialog(quickItem)
    }

    override fun showDeleteDialog(quickItem: QuickItem?, position: Int) {
        wa.showQuickDeleteDialog(quickItem, position)
    }

    override fun openUrl(url: String?) {
        val webViewActivity = wa
        webViewActivity.closeQuickSubView()
        if (webViewActivity.currentPage != null) {
            webViewActivity.pageState.addressEditBusy = false
            webViewActivity.setAddressEditing(false)
            if (TextUtils.isEmpty(url)) {
                return
            }
            if (hasZtwoFlag(8)) {
                webViewActivity.addTabPlain(null, url, true, null)
            } else {
                webViewActivity.openInCurrentPage(url, null)
            }
        }
    }

    override fun close() {
        wa.closeQuickSubView()
    }
}

class WvaDesktopModeListener(@JvmField val wa: WebViewActivity) : SetDeskListener {

    internal inner class ClosePermissionDialogStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.closePermissionDialog()
        }
    }

    override fun onChanged(deskMode: Boolean) {
        val webViewActivity = wa
        val permissionRequest = webViewActivity.pageState.permissionRequest
        val permissionOrigin = webViewActivity.pageState.permissionOrigin
        val callback = webViewActivity.pageState.geoCallback
        webViewActivity.pageState.permissionRequest = null
        webViewActivity.pageState.permissionOrigin = null
        webViewActivity.pageState.geoCallback = null
        var permissionAllow = webViewActivity.dialogsState.permissionAllow
        if (deskMode) {
            var permissionBlock = webViewActivity.pageState.permissionBlock
            if (webViewActivity.dialogsState.offerCamera) {
                permissionAllow = permissionAllow or 2
                permissionBlock = permissionBlock and -3
            }
            if (webViewActivity.dialogsState.offerMic) {
                permissionAllow = permissionAllow or 4
                permissionBlock = permissionBlock and -5
            }
            if (webViewActivity.dialogsState.offerMidi) {
                permissionAllow = permissionAllow or 16
                permissionBlock = permissionBlock and -17
            }
            if (webViewActivity.dialogsState.offerLocation) {
                permissionAllow = permissionAllow or 8
                permissionBlock = permissionBlock and -9
            }
            val grantCamera = webViewActivity.dialogsState.grantCamera
            val grantMic = webViewActivity.dialogsState.grantMic
            if (grantCamera) {
                if (grantMic) {
                    webViewActivity.dialogsState.leavingByIntent = webViewActivity.requestSitePermission(1)
                } else {
                    webViewActivity.dialogsState.leavingByIntent = webViewActivity.requestSitePermission(2)
                }
            } else if (grantMic) {
                webViewActivity.dialogsState.leavingByIntent = webViewActivity.requestSitePermission(3)
            } else if (webViewActivity.dialogsState.grantLocation) {
                webViewActivity.dialogsState.leavingByIntent = webViewActivity.requestSitePermission(4)
            }
            if (webViewActivity.dialogsState.leavingByIntent) {
                webViewActivity.permissionRequest2 = permissionRequest
                webViewActivity.geoOrigin = permissionOrigin
                webViewActivity.geoCallback = callback
                webViewActivity.pageState.pendingPermissionAllow = permissionAllow
                webViewActivity.permissionKind = permissionBlock
            } else {
                if (permissionRequest != null) {
                    permissionRequest.grant(permissionRequest.getResources())
                } else if (callback != null && !TextUtils.isEmpty(permissionOrigin)) {
                    callback.invoke(permissionOrigin, true, false)
                }
                webViewActivity.pageState.permissionHost = webViewActivity.dialogsState.pageHost
                webViewActivity.pageState.sitePermissionAllow = permissionAllow
                webViewActivity.pageState.permissionKind = permissionBlock
                webViewActivity.runInBackground(WvaSavePermissionTask(webViewActivity))
            }
        } else {
            var blockMask = webViewActivity.pageState.permissionBlock
            if (webViewActivity.dialogsState.offerCamera) {
                permissionAllow = permissionAllow and -3
                blockMask = blockMask or 2
            }
            if (webViewActivity.dialogsState.offerMic) {
                permissionAllow = permissionAllow and -5
                blockMask = blockMask or 4
            }
            if (webViewActivity.dialogsState.offerMidi) {
                permissionAllow = permissionAllow and -17
                blockMask = blockMask or 16
            }
            if (webViewActivity.dialogsState.offerLocation) {
                permissionAllow = permissionAllow and -9
                blockMask = blockMask or 8
            }
            if (permissionRequest != null) {
                permissionRequest.deny()
            } else if (callback != null && !TextUtils.isEmpty(permissionOrigin)) {
                callback.invoke(permissionOrigin, false, false)
            }
            webViewActivity.pageState.permissionHost = webViewActivity.dialogsState.pageHost
            webViewActivity.pageState.sitePermissionAllow = permissionAllow
            webViewActivity.pageState.permissionKind = blockMask
            webViewActivity.runInBackground(WvaSavePermissionTask(webViewActivity))
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(ClosePermissionDialogStep())
    }
}

class WvaCheckPasswordField(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return
        }
        evalJs(
            webNestView,
            "(function(){var tag='onCheckPass';var val=0;var ele=document.querySelector(\"input[type='password']\");if(ele){val=1;}android.onJsResult(tag,val);})();",
            false
        )
    }
}

class WvaReloadTabsAdd(@JvmField val wa: WebViewActivity) : WebViewActivity.TabViewListener {

    internal inner class TabAddedListener : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            val webViewActivity = wa
            webViewActivity.onTabAdded(isAdded)
        }
    }

    override fun onTabView() {
        wa.addTabPlain(null, WebViewActivity.newTabUrl(), true, TabAddedListener())
    }
}

class WvaVideoListListener(@JvmField val wa: WebViewActivity) : VideoListListener {
    override fun download(url: String?, type: Int, referer: String?, isLive: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null || TextUtils.isEmpty(url)) {
            return
        }
        webViewActivity.closeVideoListDialog()
        if (webViewActivity.dialogsState.downBlobBusy) {
            return
        }
        webViewActivity.dialogsState.downBlobBusy = true
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaOpenMediaTask(webViewActivity, videoListListenerFlag = isLive, showDownBlobDialogValue = type, showDownBlobDialogText = url))
    }

    override fun play(url: String?, referer: String?) {
        val webViewActivity = wa
        webViewActivity.closeVideoListDialog()
        webViewActivity.openIntentUrl(url, referer)
    }

    override fun openWith(url: String?, mimeType: String?, referer: String?) {
        val webViewActivity = wa
        webViewActivity.closeVideoListDialog()
        webViewActivity.requestSetDownDialog(url, mimeType, referer)
    }

    override fun onScanEmpty() {
        val webNestView = wa.currentPage
        if (webNestView != null) {
            try {
                val arrayList = webNestView.videoUrls
                if (arrayList == null || arrayList.isEmpty()) {
                    webNestView.hasDummyVideo = false
                    webNestView.hasM3u8 = false
                }
            } catch (unused: Exception) {
            }
        }
    }

    override fun showInfo(url: String?, info: String?, isLive: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null || TextUtils.isEmpty(url)) {
            return
        }
        if (isLive) {
            webViewActivity.openTextViewer(url, info)
        } else {
            webViewActivity.showPreviewDialog(url, null, true)
        }
    }
}

class WvaVideoDownListener(@JvmField val wa: WebViewActivity) : VideoListListener {
    override fun download(url: String?, type: Int, referer: String?, isLive: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null || TextUtils.isEmpty(url)) {
            return
        }
        webViewActivity.closeDownFontDialog()
        webViewActivity.pageState.videoDownListenerText = referer
        webViewActivity.openDownloadDialog(url, null, null, 0L, 0, null)
    }

    override fun play(url: String?, referer: String?) {
        val webViewActivity = wa
        webViewActivity.closeDownFontDialog()
        webViewActivity.openIntentUrl(url, referer)
    }

    override fun openWith(url: String?, mimeType: String?, referer: String?) {
        val webViewActivity = wa
        webViewActivity.closeDownFontDialog()
        webViewActivity.requestSetDownDialog(url, mimeType, referer)
    }

    override fun onScanEmpty() {
    }

    override fun showInfo(url: String?, info: String?, isLive: Boolean) {
    }
}

class WvaApplyDarkSetting(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val darkWeb = AppState.darkWeb
        val webViewActivity = wa
        val navBarColor = webViewActivity.navBarColorNow()
        val resources = webViewActivity.getResources()
        AppState.darkUi = isDarkResources(resources, true)
        AppState.darkWeb = isDarkResources(resources, false)
        val darkApplied = webViewActivity.applyDarkMode(navBarColor, webViewActivity.savedDarkStart, darkWeb)
        val savedWebM = webViewActivity.dialogsState.savedThemeImg
        val savedThemeImg = BrowserSettings.themeImg.value
        if (savedWebM != savedThemeImg || webViewActivity.savedImgBright != BrowserSettings.imgBright.value) {
            webViewActivity.dialogsState.savedThemeImg = savedThemeImg
            webViewActivity.savedImgBright = BrowserSettings.imgBright.value
            val webNestView: WebNestView? = if (!darkApplied) webViewActivity.currentPage else null
            if (!darkApplied && webNestView != null) {
                webNestView.applyDarkImageStyle(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost)
            }
        }
        if (AppState.webViewHint.toInt() == 1 && ZtriSettings.guideDark.value && AppState.darkWeb && !webViewActivity.savedDarkWeb) {
            webViewActivity.showDarkConfirmDialog()
        }
        val darkPage = MainUtil.isDarkPage(webViewActivity.onStartPage)
        if (webViewActivity.savedDarkStart != darkPage) {
            webViewActivity.snapshotDisplayPrefs(darkPage)
            return
        }
        val themeColor = webViewActivity.pageThemeColor()
        if (webViewActivity.savedThemeColor != themeColor) {
            webViewActivity.applyPageBarColor(themeColor, webViewActivity.pageThemeLight())
        }
        webViewActivity.snapshotDisplayPrefs(darkPage)
    }
}

class WvaOnAdsDialogResultTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.startAdsClean(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, true)
        }
    }
}

class WvaJsToggleListener(@JvmField val wa: WebViewActivity) : DialogAdsListener {
    override fun onApply(
        changed: Boolean,
        jsChanged: Boolean,
        listChanged: Boolean,
        userChanged: Boolean,
        refreshPage: Boolean,
        pageUrl: String?
    ) {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            if (changed) {
                webNestView.setEnableJs(jsChanged)
            }
            if (refreshPage) {
                webViewActivity.reloadPage()
            }
        }
    }
}

class WvaReadDialogListener(@JvmField val wa: WebViewActivity) : DialogReadListener {

    internal inner class OpenInGroupStep(
                                         @JvmField val url: String
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.addTabInGroup(null, this.url, true, null)
        }
    }

    internal inner class ApplyBarColorsStep(
                                         @JvmField val topBarColor: Int,
                                         @JvmField val bottomBarColor: Int,
                                         @JvmField val windowBgColor: Int
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            val topBarColor = webViewActivity.dialogsState.topBarColor
            val bottomBarColor = this.bottomBarColor
            val topBarColorNow = this.topBarColor
            if (topBarColor != topBarColorNow || webViewActivity.dialogsState.bottomBarColor != bottomBarColor) {
                webViewActivity.dialogsState.topBarColor = topBarColorNow
                webViewActivity.dialogsState.bottomBarColor = bottomBarColor
                webViewActivity.applyBodyColors(webViewActivity.pageThemeColor(), webViewActivity.navBarColorNow())
            }
            if (webViewActivity.dialogsState.windowBgColor != this.windowBgColor) {
                webViewActivity.postWindowBackgroundUpdate()
            }
        }
    }

    override fun currentWeb(): WebNestView? {
        return wa.currentPage
    }

    override fun openUrl(url: String?) {
        val webViewActivity = wa
        val dialogViewRead = webViewActivity.dialogsState.viewReadDialog
        webViewActivity.dialogsState.viewReadDialog = null
        if (dialogViewRead != null && !webViewActivity.pageState.readDialogOpen) {
            webViewActivity.pageState.readDialogOpen = dialogViewRead.handleBack()
        }
        webViewActivity.quickState.quickAddBusy = false
        webViewActivity.lastTransLang = null
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenInGroupStep(url!!))
    }

    override fun openInNewTab(url: String?) {
        val webNestView = wa.currentPage
        val quickView: QuickView? = if (webNestView == null) null else webNestView.quickPanel
        val quickAdapter: QuickAdapter? = if (webNestView == null || quickView == null) null else quickView.itemAdapter
        val rows = if (webNestView == null || quickView == null || quickAdapter == null || TextUtils.isEmpty(url)) null else quickAdapter.host
        if (rows == null || quickAdapter == null || rows.firstVisible() < 0) {
            return
        }
        // the news card of the article just read gets its picture again
        for (iT in rows.firstVisible()..rows.lastVisible()) {
            val quickHolderI: QuickHolder? = rows.rowAt(iT)
            val quickItemD: QuickItem? = if (quickHolderI != null) quickAdapter.itemAt(quickHolderI.pos) else null
            if (quickHolderI != null && quickItemD != null && quickItemD.itemKind.toInt() == 7 && url == quickItemD.path) {
                quickAdapter.loadNewsImage(quickItemD, quickHolderI.thumb, iT)
                return
            }
        }
    }

    override fun onClosed() {
        val webViewActivity = wa
        webViewActivity.stopTts()
        webViewActivity.finishActionMode()
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.pauseOnce()
        }
    }

    override fun applyColors(topColor: Int, bottomColor: Int, backColor: Int) {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.onResume()
        }
        val myWebCoord: MyWebCoord? = if ((webViewActivity.dialogsState.topBarColor == topColor && webViewActivity.dialogsState.bottomBarColor == bottomColor && webViewActivity.dialogsState.windowBgColor == backColor)) null else webViewActivity.webCoord
        if ((webViewActivity.dialogsState.topBarColor == topColor && webViewActivity.dialogsState.bottomBarColor == bottomColor && webViewActivity.dialogsState.windowBgColor == backColor) || myWebCoord == null) {
            return
        }
        myWebCoord.post(ApplyBarColorsStep(topColor, bottomColor, backColor))
    }

    override fun printPage(webView: WebView?, title: String?) {
        wa.printPage(webView, title)
    }

    override fun onSpeak() {
        val webViewActivity = wa
        val dialogViewRead = webViewActivity.dialogsState.viewReadDialog
        webViewActivity.dialogsState.viewReadDialog = null
        if (dialogViewRead != null) {
            dialogViewRead.closeReader(null)
        }
        webViewActivity.pageState.readDialogOpen = false
        webViewActivity.quickState.quickAddBusy = false
        webViewActivity.applyTransLang()
    }

    override fun isSpeaking(): Boolean {
        val webViewActivity = wa
        if (!webViewActivity.speechState.ttsPanelShown) {
            return false
        }
        webViewActivity.stopTts()
        return true
    }
}

class WvaApplyViewCookie(@JvmField val wa: WebViewActivity) : DialogApplyListener {
    override fun onApply() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView == null) {
            return
        }
        webNestView.setViewCookie(webViewActivity.dialogsState.appContext)
    }
}

class WvaNavColorSeek(@JvmField val wa: WebViewActivity) : DialogSeekListener {
    override fun onValue(newValue: Int) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.applyBodyColors(webViewActivity.pageThemeColor(), newValue)
    }
}

class WvaVideoPlayListenerScript(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return
        }
        val sbW = findMediaJs(null)
        sbW.append("if(ele&&!ele.paused){android.onVideoAddListener(ele.src,false,true);")
        sbW.append("function myVideoPlaying(evt){var src=null;var ele=evt.target||evt.srcElement;if(ele){src=ele.src;}android.onVideoPaused(src,false);}")
        sbW.append("function myVideoPaused(evt){var src=null;var ele=evt.target||evt.srcElement;if(ele){src=ele.src;}android.onVideoPaused(src,true);}")
        sbW.append("ele.addEventListener(\"playing\",myVideoPlaying);")
        sbW.append("ele.addEventListener(\"pause\",myVideoPaused);}")
        sbW.append("else{android.onVideoAddListener(null,false,false);}")
        sbW.insert(0, "(function(){")
        sbW.append("})();")
        evalJs(webNestView, sbW.toString(), true)
    }
}

class WvaApplyThemeTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        if (isDarkMode()) {
            webViewActivity.onThemeChanged(false)
        } else {
            saveQuickBack(webViewActivity.dialogsState.appContext, true)
            webViewActivity.onThemeChanged(true)
        }
    }
}

class WvaLanguageCheckScript(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class CheckLangScriptStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.currentPage == null) {
                return
            }
            val sb =
                StringBuilder("(function(){var tag='onCheckLang';var val='0';var dce=document.documentElement;if(dce){val=dce.lang;")
            if (TtsSettings.ttsMode.value) {
                sb.append("if(!window.sbtts){window.sbtts=true;function myTts(evt){var ele=evt.target||evt.srcElement;if(ele){var txt=ele.innerText;if(txt&&(txt.length<")
                sb.append(TextToSpeech.getMaxSpeechInputLength())
                sb.append(")){android.onTextDet(txt);}}}document.addEventListener('click',myTts);}")
            }
            sb.append("}if(android&&android.onJsResult){android.onJsResult(tag,val);}else{alert('sb:lang='+val);}})();")
            evalJs(webViewActivity.currentPage, sb.toString(), true)
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (!webViewActivity.onStartPage && (TtsSettings.ttsMode.value || webViewActivity.shouldAutoTranslate(true))) {
            if (webViewActivity.currentPage != null) {
                webViewActivity.runInBackground(CheckLangScriptStep())
                return
            } else {
                webViewActivity.dialogsState.langCheckTries = 0
                webViewActivity.dialogsState.langCheckBusy = false
                return
            }
        }
        webViewActivity.speechState.ttsLang = null
        if (webViewActivity.autoTransPending) {
            webViewActivity.autoTransPending = false
            webViewActivity.postTransIconUpdate()
        }
        webViewActivity.dialogsState.langCheckTries = 0
        webViewActivity.dialogsState.langCheckBusy = false
    }
}

class WvaAutoTranslateCheck(@JvmField val wa: WebViewActivity, @JvmField val title: String? = null) : Runnable {

    override fun run() {
        val webViewActivity = wa
        val title = this@WvaAutoTranslateCheck.title
        var langMatch = false
        webViewActivity.dialogsState.langCheckBusy = false
        if (!TtsSettings.ttsMode.value) {
            webViewActivity.speechState.ttsLang = null
        } else if (!sameObject(webViewActivity.speechState.ttsLang, title)) {
            webViewActivity.speechState.ttsLang = title
        }
        if (!webViewActivity.shouldAutoTranslate(true)) {
            if (webViewActivity.autoTransPending) {
                webViewActivity.autoTransPending = false
                webViewActivity.postTransIconUpdate()
                return
            }
            return
        }
        if (AlbumSettings.transMode.value != 2) {
            if (webViewActivity.dialogsState.isVideoHost) {
                langMatch = true
            } else {
                val pageHost = webViewActivity.dialogsState.pageHost
                if (TextUtils.isEmpty(pageHost)) {
                    langMatch = true
                } else if (!pageHost!!.endsWith("tiktok.com") && !TextUtils.isEmpty(title)) {
                    if (TextUtils.isEmpty(AlbumSettings.transCode.value)) {
                        val localeI: Locale? = MainApp.currentLocale()
                        if (localeI != null) {
                            val language = localeI.getLanguage()
                            if (!TextUtils.isEmpty(language)) {
                                langMatch = title!!.startsWith(language)
                            }
                        }
                    } else {
                        langMatch = title!!.startsWith(AlbumSettings.transCode.value!!)
                    }
                }
            }
        }
        webViewActivity.dialogsState.pageInTransLang = langMatch
        checkTransJs(webViewActivity.currentPage, "onTransExist")
    }
}

class WvaComicNavScript(@JvmField val wa: WebViewActivity, @JvmField val navMode: Int = 0) : Runnable {
    override fun run() {
        val selector: String?
        val webViewActivity = wa
        val navMode = this@WvaComicNavScript.navMode
        var cssSelector: String? = null
        if (navMode == 0) {
            selector = "span[class='page-desc']"
        } else {
            selector = if (navMode == 1) "div[class='mobile-navbar-inner']" else null
        }
        if (selector != null) {
            if (navMode == 0) {
                cssSelector =
                    "float:right;width:28px;height:28px;padding:0;border:2px solid #818181;border-radius:14px;font-size:18px;text-align:center;color:white;background:black;"
            } else if (navMode == 1) {
                cssSelector =
                    "margin-top:8px;margin-left:10px;width:28px;height:28px;padding:0;border:none;border-radius:14px;font-size:18px;font-weight:bold;text-align:center;color:white;background:black;"
            }
            if (cssSelector == null) {
                return
            }
            val sbW = StringBuilder(("(function(){var ele=document.querySelector(\"")).append(selector).append("\");if(!ele)return;for(var i=0;i<ele.childNodes.length;i++){var bid=ele.childNodes[i].id;if(bid=='sb_comic_view')return;}var btn=document.createElement(\"button\");btn.id='sb_comic_view';btn.innerHTML='⛶';btn.style='").append(cssSelector).append("';btn.onclick=function(e){e.stopPropagation();android.onComicView(")
            sbW.append(navMode)
            sbW.append(");};ele.appendChild(btn);})();")
            evalJs(webViewActivity.currentPage, sbW.toString(), true)
        }
    }
}

class WvaStartupLoadTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ReloadStartPageStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.currentPage != null) {
                webViewActivity.closeAllDialogs()
                if (webViewActivity.pageState.createBusy) {
                    webViewActivity.pageState.createBusy = false
                    webViewActivity.pageState.stateItem = null
                    webViewActivity.runInBackground(WvaSavedPageLoad(webViewActivity))
                    return
                }
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    val resolvedUrl = webNestView.realUrl(webViewActivity.dialogsState.pageUrl, false)
                    if (!webViewActivity.pageState.startupBusy) {
                        webViewActivity.loadPageUrl(resolvedUrl, false, true)
                    }
                    val handler = webViewActivity.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(WvaRestorePageStateTask(webViewActivity, restoreUrl = resolvedUrl))
                }
            }
        }
    }

    private fun pathsOf(helper: SQLiteOpenHelper?, table: String?): ArrayList<String?> {
        val list = ArrayList<String?>()
        if (helper == null) {
            return list
        }
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(
                helper.getWritableDatabase(),
                table,
                arrayOf<String?>("_path"),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val col = cursor.getColumnIndex("_path")
                do {
                    val path = cursor.getString(col)
                    if (!TextUtils.isEmpty(path)) {
                        list.add(path)
                    }
                } while (cursor.moveToNext())
            }
        } catch (unused: Exception) {
        } finally {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (unused2: Exception) {
                }
            }
        }
        return list
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.addPullViewBusy && webViewActivity.dialogsState.appContext != null) {
            try {
                val ads = DataBookAds.instance(webViewActivity.dialogsState.appContext)
                if ((ads.sites == null || ads.sites!!.isEmpty())) {
                    ads.sites = pathsOf(DbBookAds.get(webViewActivity.dialogsState.appContext), "DbBookAds_table")
                }
                val over = DataBookOver.instance(webViewActivity.dialogsState.appContext)
                if ((over.paths == null || over.paths!!.isEmpty())) {
                    over.paths = pathsOf(DbBookOver.get(webViewActivity.dialogsState.appContext), "DbBookOver_table")
                }
                val pop = DataBookPop.instance(webViewActivity.dialogsState.appContext)
                if ((pop.sites == null || pop.sites!!.isEmpty())) {
                    pop.sites = pathsOf(DbBookPop.get(webViewActivity.dialogsState.appContext), "DbBookPop_table")
                }
                val link = DataBookLink.instance(webViewActivity.dialogsState.appContext)
                if ((link.links == null || link.links!!.isEmpty())) {
                    link.links = pathsOf(DbBookLink.get(webViewActivity.dialogsState.appContext), "DbBookLink_table")
                }
                val java = DataBookJava.instance(webViewActivity.dialogsState.appContext)
                if ((java.sites == null || java.sites!!.isEmpty())) {
                    java.sites = pathsOf(DbBookJava.get(webViewActivity.dialogsState.appContext), "DbBookJava_table")
                }
                val tmem = DataBookTmem.instance(webViewActivity.dialogsState.appContext)
                if ((tmem.sites == null || tmem.sites!!.isEmpty())) {
                    tmem.sites = pathsOf(DbBookTmem.get(webViewActivity.dialogsState.appContext), "DbBookTmem_table")
                }
                val trans = DataBookTrans.instance(webViewActivity.dialogsState.appContext)
                if ((trans.sites == null || trans.sites!!.isEmpty())) {
                    trans.sites = pathsOf(DbBookTrans.get(webViewActivity.dialogsState.appContext), "DbBookTrans_table")
                }
            } catch (unused: Exception) {
            }
        }
        webViewActivity.dialogsState.addPullViewBusy = false
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord != null) {
            myWebCoord.post(ReloadStartPageStep())
        }
    }
}

class WvaPinterestBodyReset(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView: WebNestView? = if (TextUtils.isEmpty(webViewActivity.dialogsState.pageHost) || !webViewActivity.dialogsState.pageHost!!.startsWith("www.pinterest.")) null else webViewActivity.currentPage
        if (TextUtils.isEmpty(webViewActivity.dialogsState.pageHost) || !webViewActivity.dialogsState.pageHost!!.startsWith("www.pinterest.") || webNestView == null) {
            return
        }
        webNestView.evaluateJavascript("document.body.style='';", null)
    }
}

class WvaClearLongPressScript(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        evalJs(
            wa.currentPage,
            "(function(){var ele=window.sblong;window.sblong=null;window.sbfirst=null;var sel=window.getSelection();if(sel&&sel.toString())return;if(!ele||(ele.tagName=='HTML')||(ele.tagName=='BODY'))return;var fsel=ele;if(!ele.innerHTML){ele=ele.parentNode;}if(!ele||(ele.tagName=='HTML')||(ele.tagName=='BODY'))return;window.sbfirst=fsel;android.onLongPars2(ele.innerHTML);})();",
            true
        )
    }
}

class WvaActionModeMenuClick(@JvmField val wa: WebViewActivity) : MenuItem.OnMenuItemClickListener {

    internal inner class SelectedTextCallback : ValueCallback<String> {
        override fun onReceiveValue(value: String?) {
            val valueText = value
            val webViewActivity = wa
            if (webViewActivity.currentPage == null) {
                return
            }
            webViewActivity.dialogsState.actionModeMenuClickText = stripQuotes(valueText)
            evalJs(
                webViewActivity.currentPage,
                "(function(){var tag='onActionTrans';var val=window.getSelection().toString();android.onJsResult(tag,val);})();",
                true
            )
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return true
        }
        webNestView.evaluateJavascript(
            "document.documentElement.lang",
            SelectedTextCallback()
        )
        return true
    }
}

class WvaExitCleanup(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val historyBit = BrowserSettings.exitDelete.value and 4
        val webViewActivity = wa
        if (historyBit == 4) {
            MainUtil.clearWebData(webViewActivity.dialogsState.appContext)
        }
        val exitDelete = BrowserSettings.exitDelete.value
        val cacheBit = exitDelete and 8
        val handler = webViewActivity.mainHandler
        if (cacheBit == 8) {
            clearBrowsingData(handler, WvaExitCleanup2(webViewActivity))
            return
        }
        if ((exitDelete and 16) != 16) {
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

class WvaExitClearPage(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ClearHistoryStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                try {
                    webNestView.clearHistory()
                } catch (unused: Exception) {
                }
            }
            val handler = webViewActivity.mainHandler
            if (handler != null) {
                handler.post(WvaClearOnExit(webViewActivity))
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            try {
                webNestView.clearCache(true)
            } catch (unused: Exception) {
            }
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(ClearHistoryStep())
    }
}

class WvaBodySizeListener(@JvmField val wa: WebViewActivity) : ImageSizeListener {
    override fun onSize(view: View?, width: Int, height: Int) {
        val webViewActivity = wa
        webViewActivity.checkWindowBackground()
        val myWebCoord: MyWebCoord? = if (!TtsSettings.handMode.value) null else webViewActivity.webCoord
        if (!TtsSettings.handMode.value || myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaHandModeLayout(webViewActivity))
    }
}

class WvaPageTouchListener(@JvmField val wa: WebViewActivity) : MyTouchListener {
    override fun onPull(x: Float, y: Float, action: Int): Boolean {
        val webViewActivity = wa
        val myPullView: MyPullView? = if (webViewActivity.pageState.pinchActive) null else webViewActivity.pullView
        if (webViewActivity.pageState.pinchActive || myPullView == null) {
            return false
        }
        if (webViewActivity.pageState.barOffset != 0) {
            myPullView.resetSpinner()
            return false
        }
        val webNestView = webViewActivity.currentPage
        if (webNestView == null || !webNestView.coversPage()) {
            return webViewActivity.pullView!!.onTouchStep(x, y, action)
        }
        webViewActivity.pullView!!.resetSpinner()
        return false
    }
}

class WvaPrevBackListener(@JvmField val wa: WebViewActivity) : WebViewActivity.PrevBackListener {
    override fun onBack(isLong: Boolean) {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            if (!isLong) {
                webViewActivity.updatePageProgress(webNestView.getProgress())
                webViewActivity.currentPage!!.stopLoading()
            } else if (webViewActivity.addTabAnim) {
                webViewActivity.tabsState.tabUndoAllowed = BrowserSettings.tabRestore.value
            }
        }
    }
}

class WvaScrollBarListener(@JvmField val wa: WebViewActivity) : MyScrollBar.ScrollBarListener {
    override fun scrollTo(position: Int) {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return
        }
        webNestView.scrollTo(0, position)
    }

    override fun scrollPos(): Int {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return 0
        }
        return webNestView.scrollTop
    }

    override fun onDragEnd() {
    }

    override fun scrollRange(): Int {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return 0
        }
        return webNestView.scrollRange
    }

    override fun scrollExtent(): Int {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return 0
        }
        return webNestView.scrollExtent
    }
}

class WvaAddPullViewTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.setStartPage(webViewActivity.onStartPage)
        }
    }
}

class BackPageClient(@JvmField val wa: WebViewActivity) : WebViewClient() {

    internal inner class BackPageStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView = webViewActivity.pageState.backPageClientPage
            webViewActivity.pageState.backPageClientPage = null
            if (webNestView == null) {
                return
            }
            webNestView.applyPageStyle(true, webNestView.backIndex, null)
            if (BrowserSettings.adsBlock.value) {
                webNestView.startAdsClean(webNestView.backUrl, webNestView.backHost, false)
            }
            flushCookies(webViewActivity.dialogsState.appContext, false)
        }
    }

    internal inner class BackFinishedStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView = webViewActivity.pageState.backFinishedPage
            webViewActivity.pageState.backFinishedPage = null
            if (webNestView == null) {
                return
            }
            webNestView.applyPageStyle(true, webNestView.backIndex, null)
            if (BrowserSettings.adsBlock.value) {
                webNestView.startAdsClean(webNestView.backUrl, webNestView.backHost, false)
            }
            flushCookies(webViewActivity.dialogsState.appContext, false)
        }
    }

    internal inner class PauseTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView = webViewActivity.pageState.backHiddenPage
            webViewActivity.pageState.backHiddenPage = null
            val backFrame: WebNestFrame? = if (webNestView == null) null else webNestView.backFrame
            if (webNestView == null || backFrame == null || backFrame.getVisibility() != View.INVISIBLE) {
                return
            }
            webNestView.onPause()
            backFrame.setVisibility(View.GONE)
        }
    }

    override fun onPageFinished(webView: WebView?, url: String?) {
        // the back page is hidden: no loading indicator
        val webViewActivity: WebViewActivity? = wa
        val webNestView: WebNestView? = webView as? WebNestView ?: return
        webViewActivity!!.syncJsBridge(webView, url)
        webNestView!!.backUrl = url
        webViewActivity.pageState.backFinishedPage = webNestView
        webViewActivity.runInBackground(BackFinishedStep())
        if (BrowserSettings.enableJs.value) {
            webNestView.applyJsBlock(url, webNestView.backHost, true)
        }
        val backFrame = webNestView.backFrame
        if (backFrame == null || backFrame.visibility != 4) {
            return
        }
        webViewActivity.pageState.backHiddenPage = webNestView
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(PauseTask())
    }

    override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
        // the back page is hidden: no loading indicator
        val webViewActivity: WebViewActivity? = wa
        val webNestView: WebNestView? = webView as? WebNestView ?: return
        webViewActivity!!.syncJsBridge(webView, url)
        webNestView!!.backUrl = url
        webViewActivity.pageState.backPageClientPage = webNestView
        webViewActivity.runInBackground(BackPageStep())
        if (BrowserSettings.enableJs.value) {
            webNestView.applyJsBlock(url, webNestView.backHost, false)
        }
    }

    override fun onRenderProcessGone(
        webView: WebView?,
        renderProcessGoneDetail: RenderProcessGoneDetail?
    ): kotlin.Boolean {
        onPageRenderGone(webView, renderProcessGoneDetail)
        return true
    }

    override fun shouldInterceptRequest(
        webView: WebView?,
        webResourceRequest: WebResourceRequest?
    ): WebResourceResponse? {
        if (webView != null && (webView is WebNestView)) {
            val webNestView = webView
            if (webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                val webViewActivity = wa
                webViewActivity.syncJsBridge(webView, url)
                val webResourceResponseU: WebResourceResponse? = if (ZoneSettings.userFont.value) userFontResponse(webViewActivity.dialogsState.appContext, url) else null
                if (ZoneSettings.userFont.value && webResourceResponseU != null) {
                    return webResourceResponseU
                }
                val backUrl = webNestView.backUrl
                val backHost = webNestView.backHost
                val adsData = if (BrowserSettings.adsBlock.value) DataBookAds.instance(webViewActivity.dialogsState.appContext)
                    .hasSiteOf(backUrl, backHost) else true
                val siteFixMode = webViewActivity.siteFixModeFor(backHost)
                if (!adsData) {
                    val webClean = webViewActivity.miscState.webClean
                    val webResourceResponseJ = if (webClean == null) null else webClean.intercept(
                        webViewActivity.dialogsState.appContext,
                        webView,
                        webResourceRequest,
                        backUrl,
                        backHost,
                        url,
                        siteFixMode
                    )
                    if (webResourceResponseJ != null) {
                        return webResourceResponseJ
                    }
                } else if (siteFixMode != 0) {
                    return WebClean.Companion.blockPageResponse(
                        webViewActivity.dialogsState.appContext,
                        webResourceRequest,
                        url,
                        siteFixMode
                    )
                }
            }
        }
        return null
    }

    override fun shouldOverrideUrlLoading(
        webView: WebView?,
        webResourceRequest: WebResourceRequest?
    ): kotlin.Boolean {
        if (webResourceRequest != null && webResourceRequest.getUrl() != null) {
            val url = webResourceRequest.getUrl().toString()
            val webViewActivity = wa
            webViewActivity.syncJsBridge(webView, url)
            flushCookies(webViewActivity.dialogsState.appContext, true)
        }
        return false
    }
}

class WvaPopupTransport2(@JvmField val wa: WebViewActivity) : WebViewClient() {
    override fun onPageFinished(webView: WebView?, url: String?) {
        val webViewActivity = wa
        webViewActivity.syncJsBridge(webView, url)
        flushCookies(webViewActivity.dialogsState.appContext, true)
        if (webView is WebNestView) {
            (webView).setWebLoading(false)
        }
        if (webViewActivity.pageState.popupLoadState.toInt() != 2) {
            webViewActivity.onPopupPageResult(0, url, null)
        }
    }

    override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
        val webViewActivity = wa
        webViewActivity.syncJsBridge(webView, url)
        flushCookies(webViewActivity.dialogsState.appContext, true)
        if (webView is WebNestView) {
            (webView).setWebLoading(true)
        }
        if (webViewActivity.pageState.popupLoadState.toInt() == 0) {
            webViewActivity.onPopupPageResult(0, url, null)
        } else {
            webViewActivity.pageState.popupUrl = url
            webViewActivity.pageState.popupHost = hostOfUrl(url, true)
        }
    }

    override fun onRenderProcessGone(
        webView: WebView?,
        renderProcessGoneDetail: RenderProcessGoneDetail?
    ): kotlin.Boolean {
        val webViewActivity = wa
        webViewActivity.pageState.popupPage = null
        webViewActivity.pageState.pendingPage = null
        onPageRenderGone(webView, renderProcessGoneDetail)
        return true
    }

    override fun shouldInterceptRequest(
        webView: WebView?,
        webResourceRequest: WebResourceRequest?
    ): WebResourceResponse? {
        if (webResourceRequest != null && webResourceRequest.getUrl() != null) {
            val url = webResourceRequest.getUrl().toString()
            val webViewActivity = wa
            webViewActivity.syncJsBridge(webView, url)
            if (webViewActivity.pageState.popupLoadState.toInt() == 1 && (webResourceRequest.isForMainFrame() || sameObject(
                    url,
                    webViewActivity.pageState.popupUrl
                ))
            ) {
                val referer = webViewActivity.refererForRequest(null, webResourceRequest)
                webViewActivity.pageState.popupLoadState = 2.toByte()
                webViewActivity.onPopupPageResult(0, url, referer)
                return null
            }
            val popupUrl = webViewActivity.pageState.popupUrl
            val popupHost = webViewActivity.pageState.popupHost
            val webClean = webViewActivity.miscState.webClean
            val webResourceResponseJ = if (webClean == null) null else webClean.intercept(
                webViewActivity.dialogsState.appContext,
                webView,
                webResourceRequest,
                popupUrl,
                popupHost,
                url,
                0
            )
            if (webResourceResponseJ != null) {
                return webResourceResponseJ
            }
        }
        return null
    }

    override fun shouldOverrideUrlLoading(
        webView: WebView?,
        webResourceRequest: WebResourceRequest?
    ): kotlin.Boolean {
        val webViewActivity = wa
        if (webViewActivity.pageState.popupPage != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
            val popupUrl = webResourceRequest.getUrl().toString()
            if (!TextUtils.isEmpty(popupUrl)) {
                webViewActivity.syncJsBridge(webView, popupUrl)
                flushCookies(webViewActivity.dialogsState.appContext, true)
                val linkKind = webViewActivity.appLinkKind(popupUrl)
                if (linkKind != 0) {
                    webViewActivity.onPopupPageResult(linkKind, popupUrl, null)
                    return false
                }
                webViewActivity.pageState.popupLoadState = 1.toByte()
                webViewActivity.pageState.popupUrl = popupUrl
                webViewActivity.pageState.popupHost = hostOfUrl(popupUrl, true)
            }
        }
        return false
    }
}

class WvaQuickViewListener(@JvmField val wa: WebViewActivity) : QuickViewListener {
    override fun isBarUpdatePending(): kotlin.Boolean {
        return false
    }

    override fun isLandscape(): kotlin.Boolean {
        return wa.isLandscapeScreen()
    }

    override fun showLinkDialog(quickItem: QuickAdapter.QuickItem?, isFolder: Boolean) {
    }

    override fun hasTransNews(): kotlin.Boolean {
        return false
    }

    override fun openNewsItem(quickItem: QuickItem?) {
    }

    override fun webBody(): MyWebBody? {
        return wa.dialogsState.webBody
    }

    override fun onPull(x: Float, y: Float, action: Int): kotlin.Boolean {
        return false
    }

    override fun newsListType(): Int {
        return 0
    }

    override fun setQuickMode(enabled: Boolean) {
        val webViewActivity = wa
        webViewActivity.setBarsDisabled(2, enabled)
        if (enabled) {
            return
        }
        webViewActivity.showQuickSchEdit(-1, false)
    }

    override fun closeDeleteDialog() {
        wa.closeQuickDeleteDialog()
    }

    override fun runQuickAction2() {
    }

    override fun runQuickAction1() {
    }

    override fun showEditDialog(quickItem: QuickItem?) {
        wa.showQuickEditDialog(quickItem)
    }

    override fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?) {
    }

    override fun showDeleteDialog(quickItem: QuickItem?, position: Int) {
        wa.showQuickDeleteDialog(quickItem, position)
    }

    override fun isAddingTab(): kotlin.Boolean {
        return false
    }

    override fun addRssButton() {
    }

    override fun showSettingsMenu(view: View?) {
    }

    override fun openSearchSource(type: Int, url: String?) {
    }

    override fun refreshUpButton() {
    }

    override fun loadSearchItems() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
        if (webNestView != null && quickView != null) {
            quickView.notifyShown(true)
        }
        val quickSearch = webViewActivity.quickSearch
        if (quickSearch != null) {
            quickSearch.loadItems()
        }
    }

    override fun scrollPage() {
    }

    override fun hasNews(): kotlin.Boolean {
        return false
    }

    override fun loadNews() {
    }

    override fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int) {
    }
}
