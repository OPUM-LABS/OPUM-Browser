@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.RequestCode
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.userAgentPref
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.isFilmizle
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.isYouTubeHost
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.parseJson
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.openWithCookies
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.searchUrlFor
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.main.MainUtil.isLockEnabled
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.imageUrlFallback
import com.mycompany.app.main.MainUtil.extractUrl
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.urlInParens
import com.mycompany.app.main.MainUtil.siteNameOf
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.extOf
import com.mycompany.app.main.MainUtil.chromeVersion
import com.mycompany.app.main.MainUtil.isGoogleSearch
import com.mycompany.app.main.MainUtil.lacksAlgorithmicDark
import com.mycompany.app.main.MainUtil.setAlgorithmicDark
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.isYouTubeUrl
import com.mycompany.app.main.MainUtil.appendAmpJs
import com.mycompany.app.main.MainUtil.mediaNameFor
import com.mycompany.app.main.MainUtil.isImagePath
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.main.MainUtil.initUserAgent
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.main.MainUtil.offlineUrlFor
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.main.MainUtil.shortHostOf
import com.mycompany.app.main.MainUtil.resetTextSizePref
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.main.MainUtil.isFullScreenOff
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.main.MainUtil.restartTask
import com.mycompany.app.main.MainUtil.clearAppData
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.translateUrlFor
import com.mycompany.app.main.MainUtil.hasVideoButton
import com.mycompany.app.main.MainUtil.pipAspectRatio
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.showToastText
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.lockIntent
import com.mycompany.app.main.MainUtil.applyWindowBrightness
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.applyDisplayCutout
import com.mycompany.app.main.MainUtil.parseFloatSafe
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.clearSecret
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.db.book.DbBookTab.Companion.updateParents
import com.mycompany.app.db.book.DbBookTab.Companion.insertTab
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
import com.mycompany.app.main.MainUtil.scriptNameFor
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.pref.PrefSync.Companion.saveIndexes
import com.mycompany.app.main.MainUtil.flushCookies
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.youTubeAutoplayJs
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookRecent.Companion.clearSecret
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.handleDownloadLink
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.clipboardText
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.pref.PrefWeb.Companion.saveQuickBack
import com.mycompany.app.main.MainUtil.notifyBookUpdate
import com.mycompany.app.db.book.DbBookTab.Companion.newUid
import com.mycompany.app.db.book.DbBookTab.Companion.prevUid
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.nextPageUrl
import com.mycompany.app.main.MainUtil.phTitle
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.shareText
import com.mycompany.app.main.MainUtil.freeMemory
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.main.MainUtil.areaUpScript
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookRecent.Companion.loadItems
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.newMediaUri
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.main.MainUtil.clearCookies
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.applyNightMode
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
import com.mycompany.app.main.MainUtil.findMediaJs
import com.mycompany.app.db.book.DbBookRecent.Companion.clearAll
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
import com.mycompany.app.main.MainUtil.absoluteUrl
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.main.MainUtil.addShortcut
import com.mycompany.app.main.MainUtil.checkTransClassJs
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.themeIconColor
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.db.book.DbBookTab.Companion.setGroup
import com.mycompany.app.db.book.DbBookTab.Companion.setParent
import com.mycompany.app.main.MainUtil.needsDesktopUa
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.main.MainUtil.iconResFor
import com.mycompany.app.main.MainUtil.windowStatusColor
import com.mycompany.app.main.MainUtil.setNavBarColor
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.main.MainUtil.navBarInset
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.isDarkTheme
import com.mycompany.app.main.MainUtil.isChinaLocale
import com.mycompany.app.main.MainUtil.applyScreenOrientation
import com.mycompany.app.main.MainUtil.lofiUrl
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.db.book.DbBookHistory.Companion.clearAll
import com.mycompany.app.db.book.DbBookHistory.Companion.clearOld
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
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.main.MainUtil.durationText
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import android.view.ViewGroup
import com.mycompany.app.web.WebNestView
import android.widget.RelativeLayout
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import com.mycompany.app.web.WebViewActivity
import android.os.Bundle
import android.content.Intent
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.quick.QuickView
import com.mycompany.app.main.MainWebDestroy
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.db.DbUtil
import com.mycompany.app.quick.QuickAdapter
import com.mycompany.app.view.MyWebBody
import android.os.Looper
import com.mycompany.app.main.MainConst
import android.os.Build
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefRead
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.mycompany.app.dialog.DialogCreateAlbum
import com.mycompany.app.web.WebVideoFrame
import com.mycompany.app.db.book.DbBookTab

import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.web.WebViewActivity.PrevPageListener
import com.mycompany.app.web.WebViewActivity.PrevBackListener
import com.mycompany.app.dialog.DialogWebView
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.ui.browser.BarScroll
import com.mycompany.app.web.WebNestLayout
import com.mycompany.app.view.MyBarFrame
import com.mycompany.app.behavior.MyBehaviorTop
import com.mycompany.app.behavior.MyBehaviorBot
import android.webkit.PermissionRequest
import com.mycompany.app.view.MyEditAuto
import android.webkit.GeolocationPermissions
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.data.DataTrans
import com.mycompany.app.data.book.DataBookPop
import com.mycompany.app.web.WebViewActivity.AnchorHandler
import com.bumptech.glide.Glide
import com.mycompany.app.dialog.DialogSaveSource
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Rect
import android.net.Uri
import android.os.Handler
import android.view.ActionMode
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import com.mycompany.app.ui.browser.ClipRow
import android.view.View
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ch.opumlabs.opumbrowser.R
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.Locale
import kotlin.math.max
import com.mycompany.app.web.WebViewActivity.Companion.Zn
import com.mycompany.app.web.WebViewActivity.Companion.homePageUrl

/*
 * The bodies of the browser activity's lifecycle callbacks (onCreate, onResume, onPause,
 * onDestroy, onConfigurationChanged, onNewIntent, onKeyDown, …) and of its other large members, moved out of
 * `WebViewActivity`. The activity keeps each member with its signature as a one-line call; `super` calls come in
 * as lambdas (`superOnCreate`, …), so they run at the same point as before.
 */

fun WebViewActivity.insertTabItem(insertAt: Int, tabUrl: String?, webNestFrame: WebNestFrame?, wasInGroup: kotlin.Boolean, newGrouped: kotlin.Boolean): Int {
    synchronized(this.tabLock) {
        try {
            val wasGrouped: kotlin.Boolean
            val grouped: kotlin.Boolean
            if (this.tabsState.tabItems == null) {
                this.tabsState.tabItems = ArrayList()
                wasGrouped = false
                grouped = false
            } else {
                wasGrouped = (wasInGroup) == true
                grouped = (newGrouped) == true
            }
            val backItem: WebTabItem?
            if (webNestFrame != null) {
                backItem = webNestFrame.backItem
                webNestFrame.backItem = null
            } else {
                backItem = null
            }
            var currentTabIndex = -1
            if (backItem != null && backItem.groupId != 0L) {
                val webTabItemD = tabItemAt(backItem.position - 1)
                if (webTabItemD != null && webTabItemD.groupId == backItem.groupId) {
                    currentTabIndex = backItem.position - 1
                } else {
                    val backNeighbour = tabItemAt(backItem.position)
                    if (backNeighbour != null && backNeighbour.groupId == backItem.groupId) {
                        currentTabIndex = backItem.position
                    }
                }
            }
            if (currentTabIndex == -1) {
                currentTabIndex = insertAt - 1
            }
            val currentItem = tabItemAt(currentTabIndex)
            var now: Long = 0
            var groupName: String? = null
            var groupColor = 0
            var groupTab = false
            if (currentItem != null) {
                var updated = false
                if (backItem != null && backItem.groupId != 0L) {
                    if (currentItem.groupId == 0L) {
                        currentItem.groupId = backItem.groupId
                        currentItem.groupName = backItem.groupName
                        currentItem.groupColor = backItem.groupColor
                        updated = true
                    }
                }
                now = currentItem.groupId
                groupName = currentItem.groupName
                groupColor = currentItem.groupColor
                if (grouped && now == 0L) {
                    now = System.currentTimeMillis()
                    currentItem.groupId = now
                    updated = true
                }
                if (updated) {
                    setGroup(this.dialogsState.appContext, currentItem.uid, now)
                }
                groupTab = wasGrouped || grouped
            }
            var size = insertAt
            if (groupTab && now != 0L) {
                while (size < this.tabsState.tabItems!!.size) {
                    val lastTab = this.tabsState.tabItems!!.get(size)
                    if (lastTab != null && lastTab.groupId != now) {
                        break
                    }
                    size++
                }
            }
            if (size > this.tabsState.tabItems!!.size) {
                size = this.tabsState.tabItems!!.size
            }
            val webTabItem = if (backItem != null) backItem else WebTabItem()
            var tabTitle = webTabItem.tabTitle
            if (TextUtils.isEmpty(tabTitle)) {
                if ("file:///android_asset/shortcut.html" == tabUrl) {
                    tabTitle = "OPUM"
                } else {
                    tabTitle = hostNameOf(tabUrl, true)
                }
            }
            if (groupTab || now != 0L) {
                webTabItem.groupId = now
                webTabItem.groupName = groupName
                webTabItem.groupColor = groupColor
            }
            // "New tab" in a group of the tab overview (also a brand-new, still empty group)
            val forcedGroup = this.tabsState.nextTabGroup
            if (forcedGroup != null) {
                this.tabsState.nextTabGroup = null
                webTabItem.groupId = forcedGroup.first
                webTabItem.groupName = forcedGroup.second
                webTabItem.groupColor = forcedGroup.third
            }
            webTabItem.uid = DbBookTab.newUid((this.tabsState.tabItems))
            webTabItem.parentId = DbBookTab.prevUid(size, (this.tabsState.tabItems))
            webTabItem.position = size
            webTabItem.tabUrl = tabUrl
            webTabItem.tabTitle = tabTitle
            webTabItem.deskMode = BrowserSettings.deskLock.value
            webTabItem.webFrame = webNestFrame
            if (webNestFrame != null) {
                webNestFrame.setTabUid(webTabItem.uid)
            }
            insertTab(this.dialogsState.appContext, webTabItem, SyncSettings.secretMode.value)
            val webTabItemD = tabItemAt(size)
            if (webTabItemD != null && webTabItemD.parentId != webTabItem.uid) {
                webTabItemD.parentId = webTabItem.uid
                setParent(this.dialogsState.appContext, webTabItemD.uid, webTabItem.uid)
            }
            if (size >= this.tabsState.tabItems!!.size) {
                this.tabsState.tabItems!!.add(webTabItem)
            } else {
                this.tabsState.tabItems!!.add(size, webTabItem)
            }
            var position = size + 1
            if (position < this.tabsState.tabItems!!.size) {
                val tabItemsSize = this.tabsState.tabItems!!.size
                var prevUid = DbBookTab.prevUid(position, (this.tabsState.tabItems))
                var reparented: ArrayList<Any?>? = null
                while (position < tabItemsSize) {
                    val tab = this.tabsState.tabItems!!.get(position)
                    if (tab != null) {
                        val changed = tab.parentId != prevUid
                        tab.parentId = prevUid
                        tab.position = position
                        prevUid = tab.uid
                        if (changed) {
                            if (reparented == null) {
                                reparented = ArrayList<Any?>()
                            }
                            val copy = WebTabItem()
                            copy.uid = tab.uid
                            copy.parentId = tab.parentId
                            reparented.add(copy)
                        }
                    }
                    position++
                }
                if (reparented != null && !reparented.isEmpty()) {
                    DbBookTab.updateParents(this.dialogsState.appContext, (reparented as ArrayList<WebTabItem?>?))
                }
            }
            return size
        } catch (unused: Exception) {
            return 0
        }
    }
}

fun WebViewActivity.addTabImpl(webNestFrame: WebNestFrame?, url: String?, isTop: kotlin.Boolean, inGroup: kotlin.Boolean, autoGroup: kotlin.Boolean, tabAddListener: TabAddListener?) {
    if (this.videoState.addTabImplBusy) {
        TabFrameLog.step(this, "addTabImpl dropped (add already running)", this.tabsState.currentFrame)
        return
    }
    TabFrameLog.step(this, "addTabImpl start", this.tabsState.currentFrame, "url=${url?.take(60)}")
    this.videoState.addTabImplBusy = true
    this.dialogsState.hasRealPage = false
    val addTabPending = this.addTabPending
    val addTabFromIntent = this.addTabFromIntent
    this.addTabPending = false
    this.addTabFromIntent = false
    if (this.webCoord == null) {
        if (tabAddListener != null) {
            tabAddListener.onTabAdded(false)
        }
        this.videoState.addTabImplBusy = false
        return
    }
    if (this.videoFrame != null) {
        exitVideoFrame()
    }
    stopWebClean()
    hidePageOverlays(true)
    hideKeyboard()
    this.tabsState.tabUndoAllowed = false
    if (addTabFromIntent && !addTabPending && !TextUtils.isEmpty(this.dialogsState.pageUrl) && this.dialogsState.pageUrl == url) {
        if (tabAddListener != null) {
            tabAddListener.onTabAdded(false)
        }
        finishProgress()
        this.videoState.addTabImplBusy = false
        return
    }
    if (webNestFrame == null && openTextLink(url)) {
        if (tabAddListener != null) {
            tabAddListener.onTabAdded(false)
        }
        finishProgress()
        this.videoState.addTabImplBusy = false
        return
    }
    val appBarLayout = this.barScroll
    if (appBarLayout != null) {
        appBarLayout.setExpanded(true)
    }
    closeSnackbar(true)
    exitFind(false)
    settleSwipes()
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaAddTabImplTask(this, frame = webNestFrame, url = url, inGroup = (inGroup) == true, autoGroup = (autoGroup) == true, fromIntent = addTabFromIntent, isTop = (isTop) == true, addTabImplAddListener = tabAddListener, pending = addTabPending))
}

fun WebViewActivity.startBackImpl(fromKey: kotlin.Boolean, prevBackListener: PrevBackListener?, prevPageListener: PrevPageListener?) {
    val fromEdgeGesture = this.miscState.fromEdgeGesture
    this.miscState.fromEdgeGesture = false
    this.dialogsState.savePassHost = null
    this.dialogsState.savePassUser = null
    this.dialogsState.savePassWord = null
    this.dialogsState.savePassUrl = null
    resetScrollNavis(true, true)
    val webNestView = this.currentPage
    if (webNestView == null) {
        if (prevPageListener != null) {
            prevPageListener.onDone()
        }
        if (prevBackListener != null) {
            prevBackListener.onBack(false)
        }
        this.tabsState.goBackPageBusy = false
        return
    }
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaGoBackTask(this, fromEdgeGesture = fromEdgeGesture, canGoBack = webNestView.canGoBack(), fromKey = (fromKey) == true, prevBackListener = prevBackListener, startBackImplBackListener = prevPageListener))
}

fun WebViewActivity.lifecycleFinish() {
    val messageId: Int
    if (this.tabsState.restartBusy) {
        return
    }
    this.tabsState.restartBusy = true
    this.dialogsState.sendAppToBackgroundBusy = false
    if (BrowserSettings.exitNoti.value) {
        val deleteSummary = com.mycompany.app.ui.settings.PrivacySettingsActivity.exitDeleteSummary(this)
        if (!TextUtils.isEmpty(deleteSummary)) {
            val exitDelete = BrowserSettings.exitDelete.value
            if (exitDelete == 126) {
                messageId = AppR.string.exit_noti_2
            } else {
                var exitNoti = AppR.string.exit_noti_1
                val noteText = if ((exitDelete and 4) == 4) AppR.string.exit_noti_2 else exitNoti
                if ((exitDelete and 8) != 8 && (exitDelete and 16) != 16) {
                    exitNoti = noteText
                }
                messageId = if ((exitDelete and 32) == 32 || (exitDelete and 64) == 64) AppR.string.exit_noti_2 else exitNoti
            }
            showToastText(this, String.format(Locale.US, getString(messageId), deleteSummary))
        }
    }
    val mainFilterSvc = this.miscState.filterService
    if (mainFilterSvc != null) {
        mainFilterSvc.release()
        this.miscState.filterService = null
    }
    val mainScriptSvc = this.miscState.scriptService
    if (mainScriptSvc != null) {
        mainScriptSvc.release()
        this.miscState.scriptService = null
    }
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.post(WvaExitCleanup(this))
}

fun WebViewActivity.handlePermissionRequest(permissionRequest: PermissionRequest?, origin: String?, callback: GeolocationPermissions.Callback?): kotlin.Boolean {
    if (this.currentPage != null && this.permissionDialog == null && !isShowingStartPage()) {
        closePermissionDialog()
        clearPermissionRequest()
        this.dialogsState.requestsCamera = false
        this.dialogsState.requestsMic = false
        this.dialogsState.requestsProtectedMedia = false
        this.dialogsState.requestsLocation = false
        this.dialogsState.grantCamera = false
        this.dialogsState.grantMic = false
        this.dialogsState.grantLocation = false
        this.dialogsState.offerCamera = false
        this.dialogsState.offerMic = false
        this.dialogsState.offerMidi = false
        this.dialogsState.offerLocation = false
        this.dialogsState.permissionAllow = 0
        this.pageState.permissionBlock = 0
        this.pageState.permissionOnce = false
        if (permissionRequest != null) {
            val resources = permissionRequest.getResources()
            if (resources != null && resources.size > 0) {
                for (resource in resources) {
                    if ("android.webkit.resource.VIDEO_CAPTURE" == resource) {
                        this.dialogsState.requestsCamera = true
                    } else if ("android.webkit.resource.AUDIO_CAPTURE" == resource) {
                        this.dialogsState.requestsMic = true
                    } else if ("android.webkit.resource.PROTECTED_MEDIA_ID" == resource) {
                        this.dialogsState.requestsProtectedMedia = true
                    }
                }
            }
        } else if (callback != null && !TextUtils.isEmpty(origin)) {
            this.dialogsState.requestsLocation = true
        }
        if (this.dialogsState.requestsCamera || this.dialogsState.requestsMic || this.dialogsState.requestsProtectedMedia || this.dialogsState.requestsLocation) {
            runInBackground(WvaHandlePermissionRequestTask(this, permissionRequest, callback, origin))
            return true
        }
    }
    return false
}

fun WebViewActivity.handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    if (requestCode == RequestCode.UNLOCK_SPACE) {
        WorkspaceSwitch.onUnlockResult(this, resultCode)
        return
    }
    val uri: Uri?
    val uriArr: Array<Uri?>?
    val dialogQuickEdit = this.dialogsState.quickEditDialog
    var pickedUris: Array<Uri?>? = null
    if (dialogQuickEdit != null) {
        if (requestCode == RequestCode.CAMERA_OR_FILE) {
            var cameraUri = dialogQuickEdit.cameraUri
            dialogQuickEdit.cameraUri = null
            if (resultCode == -1) {
                val data = if (intent != null) intent.getData() else null
                if (data != null) {
                    cameraUri = data
                }
                keepUriPermission(dialogQuickEdit.appContext, cameraUri)
                if (dialogQuickEdit.hostActivity != null) {
                    val context = dialogQuickEdit.appContext
                    if (cameraUri == null) {
                        showToast(context, AppR.string.invalid_path)
                    }
                    val cachePath = newCacheFilePath(context)
                    dialogQuickEdit.picturePath = cachePath
                    if (TextUtils.isEmpty(cachePath)) {
                        showToast(dialogQuickEdit.appContext, AppR.string.invalid_path)
                        return
                    }
                    val cropIntent =
                        Intent(dialogQuickEdit.appContext, com.mycompany.app.ui.viewer.ImageCropActivity::class.java as Class<*>)
                    cropIntent.setData(cameraUri)
                    cropIntent.putExtra("EXTRA_DST", dialogQuickEdit.picturePath)
                    cropIntent.putExtra("EXTRA_ICON", true)
                    dialogQuickEdit.hostActivity!!.launchExternalForResult(cropIntent, RequestCode.IMAGE_FILE)
                    return
                }
                return
            }
            return
        }
        if (requestCode == RequestCode.IMAGE_FILE) {
            val picturePath = dialogQuickEdit.picturePath
            dialogQuickEdit.picturePath = null
            if (resultCode == -1) {
                if (TextUtils.isEmpty(picturePath) || !File(picturePath!!).exists()) {
                    showToast(dialogQuickEdit.appContext, AppR.string.invalid_path)
                    return
                } else {
                    dialogQuickEdit.iconChanged = true
                    dialogQuickEdit.loadIcon(picturePath, dialogQuickEdit.nameText(), null, 0, 0)
                    return
                }
            }
            return
        }
    }
    val dialogWebBookList = this.bookmarksDialog
    if (dialogWebBookList == null || !dialogWebBookList.onPickResult(requestCode, resultCode, intent)) {
        val dialogEditShort = this.editShortDialog
        if (dialogEditShort == null || !dialogEditShort.onPickResult(requestCode, resultCode, intent)) {
            val dialogDownUrl = this.videoState.downUrlDialog
            if (dialogDownUrl == null || !dialogDownUrl.onFolderResult(requestCode, resultCode, intent)) {
                val dialogCapture = this.captureDialog
                if (dialogCapture == null || !dialogCapture.onEditResult(requestCode, resultCode, intent)) {
                    val dialogDownPage = this.downPageDialog
                    if (dialogDownPage != null && requestCode == RequestCode.SAVE_LOCATION) {
                        if (resultCode != -1 || intent == null) {
                            return
                        }
                        val treeData = intent.getData()
                        if (treeData == null) {
                            showToast(dialogDownPage.appContext, AppR.string.invalid_path)
                            return
                        }
                        val treeUri = treeDocUri(treeData)
                        if (TextUtils.isEmpty(treeUri)) {
                            showToast(dialogDownPage.appContext, AppR.string.invalid_path)
                            return
                        } else {
                            dialogDownPage.setFolderPath(treeUri)
                            keepUriPermission(dialogDownPage.appContext, treeData)
                            return
                        }
                    }
                    val dialogSetDesk = this.dialogsState.setDeskDialog
                    if (dialogSetDesk != null && requestCode == RequestCode.DIALOG_SETTINGS && intent != null && intent.getIntExtra(
                            "EXTRA_TYPE",
                            0
                        ) == 42
                    ) {
                        if (resultCode != -1) {
                            return
                        }
                        dialogSetDesk.refreshAgent()
                        return
                    }
                    run {
                        run {
                            val webGridDialog = this.dialogsState.gridDialog
                            if (webGridDialog != null) {
                                val dialogDownList = webGridDialog.downListDialog
                                if (dialogDownList != null && dialogDownList.onFolderResult(requestCode, resultCode, intent)) {
                                    return
                                }
                                val dialogDownZip = webGridDialog.downZipDialog
                                if (dialogDownZip != null && dialogDownZip.onActivityResultCode(requestCode, resultCode, intent)) {
                                    return
                                }
                                val dialogCreateAlbum = webGridDialog.albumDialog
                                if (dialogCreateAlbum != null && dialogCreateAlbum.onActivityResultCode(
                                        requestCode,
                                        resultCode,
                                        intent
                                    )
                                ) {
                                    return
                                }
                            }
                            val siteDlg = this.dialogsState.siteDialog
                            if (siteDlg != null) {
                                val siteDown = siteDlg.downListDialog
                                if (siteDown != null && siteDown.onFolderResult(requestCode, resultCode, intent)) {
                                    return
                                }
                                val siteZip = siteDlg.downZipDialog
                                if (siteZip != null && siteZip.onActivityResultCode(requestCode, resultCode, intent)) {
                                    return
                                }
                                val siteAlbum = siteDlg.albumDialog
                                if (siteAlbum != null && siteAlbum.onActivityResultCode(requestCode, resultCode, intent)) {
                                    return
                                }
                            }
                            val dialogSetUseTts = this.dialogsState.setUseTtsDialog
                            if (dialogSetUseTts != null && requestCode == RequestCode.DIALOG_SETTINGS && intent != null && intent.getIntExtra(
                                    "EXTRA_TYPE",
                                    0
                                ) == 60
                            ) {
                                if (resultCode != -1) {
                                    return
                                }
                                dialogSetUseTts.refreshMode()
                                return
                            }
                            val dialogAllowPopup = this.allowPopupDialog
                            if (dialogAllowPopup != null && requestCode == RequestCode.DIALOG_SETTINGS && intent != null && intent.getIntExtra(
                                    "EXTRA_TYPE",
                                    0
                                ) == 62
                            ) {
                                if (resultCode == -1) {
                                    dialogAllowPopup.listChanged = true
                                    if (dialogAllowPopup.settingsKept) {
                                        return
                                    }
                                    dialogAllowPopup.settingsKept =
                                        intent.getBooleanExtra("EXTRA_FILTER", true)
                                    return
                                }
                                return
                            }
                            val dialogBlockLink = this.blockLinkDialog
                            if (dialogBlockLink != null) {
                                if (requestCode == RequestCode.DIALOG_SETTINGS) {
                                    if (intent != null) {
                                        if (intent.getIntExtra("EXTRA_TYPE", 0) == 62) {
                                            if (resultCode == -1) {
                                                dialogBlockLink.listChanged = true
                                                if (dialogBlockLink.settingsKept) {
                                                    return
                                                }
                                                dialogBlockLink.settingsKept =
                                                    intent.getBooleanExtra("EXTRA_FILTER", true)
                                                return
                                            }
                                            return
                                        }
                                    }
                                } else if (requestCode == RequestCode.UNLOCK_SECRET) {
                                    if (resultCode != -1) {
                                        return
                                    }
                                    dialogBlockLink.refresh()
                                    return
                                }
                            }
                            val dialogBlockImage = this.dialogsState.blockImageDialog
                            if (dialogBlockImage != null && requestCode == RequestCode.DIALOG_SETTINGS && intent != null && intent.getIntExtra(
                                    "EXTRA_TYPE",
                                    0
                                ) == 62
                            ) {
                                if (resultCode == -1) {
                                    dialogBlockImage.listChanged = true
                                    if (dialogBlockImage.settingsKept) {
                                        return
                                    }
                                    dialogBlockImage.settingsKept =
                                        intent.getBooleanExtra("EXTRA_FILTER", true)
                                    return
                                }
                                return
                            }
                            val dialogSetAdblock = this.setAdblockDialog
                            if (dialogSetAdblock != null && requestCode == RequestCode.DIALOG_SETTINGS && intent != null && intent.getIntExtra(
                                    "EXTRA_TYPE",
                                    0
                                ) == 62
                            ) {
                                if (resultCode == -1) {
                                    dialogSetAdblock.listChanged = true
                                    if (dialogSetAdblock.settingsKept) {
                                        return
                                    }
                                    dialogSetAdblock.settingsKept =
                                        intent.getBooleanExtra("EXTRA_FILTER", true)
                                    return
                                }
                                return
                            }
                            val dialogSetRead = this.setReadDialog
                            if (dialogSetRead != null && requestCode == RequestCode.DIALOG_SETTINGS && intent != null && intent.getIntExtra(
                                    "EXTRA_TYPE",
                                    0
                                ) == 31
                            ) {
                                if (resultCode != -1 || dialogSetRead.resultTarget == null || ZtwoSettings.readMode.value == 0) {
                                    return
                                }
                                dialogSetRead.dismiss()
                                return
                            }
                            val dialogViewRead = this.dialogsState.viewReadDialog
                            val saveSource: DialogSaveSource? = if (dialogViewRead == null) null else dialogViewRead.saveSourceDialog
                            if (dialogViewRead == null || saveSource == null || !saveSource.onPickResult(requestCode, resultCode, intent)) {
                                val dialogViewSrc = this.dialogsState.viewSrcDialog
                                val dialogSaveSource: DialogSaveSource? = if (dialogViewSrc == null) null else dialogViewSrc.saveDialog
                                if (dialogViewSrc == null || dialogSaveSource == null || !dialogSaveSource.onPickResult(requestCode, resultCode, intent)) {
                                    val dialogSetPrivacy = this.dialogsState.setPrivacyDialog
                                    if (dialogSetPrivacy != null && requestCode == RequestCode.DIALOG_SETTINGS && intent != null && intent.getIntExtra(
                                            "EXTRA_TYPE",
                                            0
                                        ) == 40
                                    ) {
                                        if (resultCode == -1) {
                                            if (intent.getBooleanExtra("EXTRA_STATUS", false)) {
                                                dialogSetPrivacy.settingsOpened = true
                                            }
                                            dialogSetPrivacy.loadMask()
                                            return
                                        }
                                        return
                                    }
                                    val webVideoFrame = this.videoFrame
                                    if (webVideoFrame == null || !webVideoFrame.onPermissionResult(requestCode, resultCode)) {
                                        if (requestCode == RequestCode.QUICK_ADD) {
                                            if (resultCode != -1 || intent == null) {
                                                return
                                            }
                                            val stringExtra =
                                                intent.getStringExtra("EXTRA_PATH")
                                            if (TextUtils.isEmpty(stringExtra)) {
                                                showToast(this, AppR.string.invalid_url)
                                                return
                                            }
                                            var name =
                                                intent.getStringExtra("EXTRA_NAME")
                                            if (TextUtils.isEmpty(name)) {
                                                name = MainUtil.pathOfUrl(stringExtra, false)
                                            }
                                            val intExtra = intent.getIntExtra("EXTRA_ICON", 0)
                                            val itemIndex = intent.getIntExtra("EXTRA_INDEX", 0)
                                            showToast(this, AppR.string.added)
                                            val webNestView = this.currentPage
                                            val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
                                            if (webNestView != null && quickView != null) {
                                                quickView.addItem(
                                                    stringExtra,
                                                    intExtra,
                                                    itemIndex,
                                                    name
                                                )
                                            }
                                            val quickSearch = this.quickSearch
                                            val quickAdapter: QuickAdapter? = if (quickSearch == null) null else quickSearch.gridAdapter
                                            if (quickSearch == null || quickAdapter == null) {
                                                return
                                            }
                                            quickAdapter.insertItem(
                                                stringExtra,
                                                intExtra,
                                                itemIndex,
                                                name
                                            )
                                            return
                                        }
                                        if (requestCode == RequestCode.MAIN_SETTINGS) {
                                            if (resultCode != -1 || intent == null || this.currentPage == null) {
                                                return
                                            }
                                            if (intent.getBooleanExtra(
                                                    "EXTRA_PRELOAD",
                                                    false
                                                )
                                            ) {
                                                runInBackground(
                                                    WvaWebCleanRun(
                                                        this,
                                                        !intent.getBooleanExtra(
                                                            "EXTRA_FILTER",
                                                            true
                                                        )
                                                    )
                                                )
                                            }
                                            if (intent.getBooleanExtra("EXTRA_STATUS", false)) {
                                                rebuildTabs(false)
                                                return
                                            }
                                            return
                                        }
                                        if (requestCode == RequestCode.UNLOCK) {
                                            val pendingLinkUrl = this.dialogsState.pendingLinkUrl
                                            this.dialogsState.pendingLinkUrl = null
                                            if (resultCode != -1 || TextUtils.isEmpty(pendingLinkUrl)) {
                                                return
                                            }
                                            showBlockLinkDialog(pendingLinkUrl)
                                            return
                                        }
                                        if (requestCode == RequestCode.UNLOCK_SECRET) {
                                            if (resultCode == -1) {
                                                switchSecretMode(true)
                                                return
                                            }
                                            return
                                        }
                                        if (requestCode == RequestCode.VOICE_SEARCH) {
                                            if (resultCode != -1 || intent == null) {
                                                return
                                            }
                                            val stringArrayListExtra =
                                                intent.getStringArrayListExtra("android.speech.extra.RESULTS")
                                            if (stringArrayListExtra != null && !stringArrayListExtra.isEmpty()) {
                                                val spoken = stringArrayListExtra.get(0)
                                                if (!TextUtils.isEmpty(spoken)) {
                                                    val searchUrl = MainUtil.searchUrlFor(spoken)
                                                    if (hasZtwoFlag(4)) {
                                                        addTabPlain(null, searchUrl, true, null)
                                                        return
                                                    } else {
                                                        openInCurrentPage(searchUrl, null)
                                                        return
                                                    }
                                                }
                                            }
                                            showToast(this, AppR.string.fail)
                                            return
                                        }
                                        if (requestCode == RequestCode.CLEAN_SETTINGS) {
                                            if (resultCode != -1 || intent == null || this.currentPage == null) {
                                                return
                                            }
                                            runInBackground(
                                                WvaWebCleanRun(
                                                    this,
                                                    !intent.getBooleanExtra(
                                                        "EXTRA_FILTER",
                                                        true
                                                    )
                                                )
                                            )
                                            return
                                        }
                                        if (requestCode == RequestCode.QUICK_SETTINGS) {
                                            if (resultCode != -1 || intent == null) {
                                                return
                                            }
                                            val webNestView: WebNestView? = if (intent.getBooleanExtra(
                                                    "change",
                                                    false
                                                )) this.currentPage else null
                                            val quickPanel: QuickView? = if (intent.getBooleanExtra(
                                                    "change",
                                                    false
                                                ) && webNestView != null) webNestView.quickPanel else null
                                            if (intent.getBooleanExtra(
                                                    "change",
                                                    false
                                                ) && webNestView != null && quickPanel != null) {
                                                quickPanel.applyLayout()
                                            }
                                            if (intent.getBooleanExtra("reload", false)) {
                                                val currentPage = this.currentPage
                                                val quickView: QuickView? = if (currentPage != null) currentPage.quickPanel else null
                                                if (currentPage != null && quickView != null) {
                                                    quickView.notifyShown(true)
                                                }
                                                val quickSearch = this.quickSearch
                                                if (quickSearch != null) {
                                                    quickSearch.loadItems()
                                                    return
                                                }
                                                return
                                            }
                                            return
                                        }
                                        if (requestCode == RequestCode.UNLOCK_SITE) {
                                            if (resultCode == -1) {
                                                this.dialogsState.checkSiteLockBusy = true
                                                runInBackground(WvaCheckSiteLockLater(this))
                                                return
                                            }
                                            return
                                        }
                                        if (requestCode == RequestCode.UNLOCK_LAUNCH) {
                                            if (resultCode == -1) {
                                                this.launchUrl = null
                                                return
                                            }
                                            return
                                        }
                                        run sw12@{
when (requestCode) {
                                            RequestCode.DOWNLOAD_LIST -> if (resultCode == -1 && intent != null && this.currentPage != null) {
                                                val path =
                                                    intent.getStringExtra("EXTRA_PATH")
                                                if (TextUtils.isEmpty(path)) {
                                                    showToast(this, AppR.string.invalid_url)
                                                } else {
                                                    val stringExtra =
                                                        intent.getStringExtra("EXTRA_URL")
                                                    if (if (TextUtils.isEmpty(stringExtra)) false else stringExtra!!.startsWith(
                                                            "file://"
                                                        )
                                                    ) {
                                                        openInCurrentPage(stringExtra, null)
                                                        this.currentPage!!.setFileUrl(null, stringExtra)
                                                    } else if (isContentUri(path)) {
                                                        runInBackground(
                                                            WvaOpenSavedPage(
                                                                this,
                                                                path,
                                                                stringExtra
                                                            )
                                                        )
                                                    } else {
                                                        val fileSourceUrl = "file://" + path
                                                        openInCurrentPage(fileSourceUrl, null)
                                                        this.currentPage!!.setFileUrl(stringExtra, fileSourceUrl)
                                                    }
                                                }
                                                return@sw12
                                            }

                                            RequestCode.CAMERA_OR_FILE, RequestCode.PICK_IMAGES, RequestCode.PICK_FILES -> if (resultCode != -1) {
                                                val valueCallback =
                                                    if (requestCode == RequestCode.PICK_IMAGES) this.fileChooserCallback2 else if (requestCode == RequestCode.PICK_FILES) this.fileChooserCallback3 else this.fileChooserCallback
                                                if (valueCallback != null) {
                                                    valueCallback.onReceiveValue(null)
                                                    this.fileChooserCallback = null
                                                    this.fileChooserCallback2 = null
                                                    this.fileChooserCallback3 = null
                                                    this.cameraUri = null
                                                }
                                                return@sw12
                                            } else {
                                                if (intent == null) {
                                                    uri = this.cameraUri
                                                    if (uri != null) {
                                                        pickedUris = arrayOf<Uri>(uri) as Array<Uri?>?
                                                    }
                                                } else {
                                                    val clipData = intent.getClipData()
                                                    val itemCount = if (clipData == null) 0 else clipData.getItemCount()
                                                    if (clipData == null || itemCount == 0) {
                                                        uriArr = null
                                                    } else {
                                                        var pickedList: ArrayList<Any?>? = null
                                                        run {
                                                            var index = 0
                                                            while (index < itemCount) {
                                                                val itemAt =
                                                                    clipData.getItemAt(index)
                                                                val clipUri: Uri? = if (itemAt != null) itemAt.getUri() else null
                                                                if (itemAt != null && clipUri != null) {
                                                                    if (pickedList == null) {
                                                                        pickedList =
                                                                            ArrayList<Any?>()
                                                                    }
                                                                    pickedList.add(clipUri)
                                                                }
                                                                index++
                                                            }
                                                        }
                                                        if (pickedList == null || pickedList.isEmpty()) {
                                                            uriArr = null
                                                        } else {
                                                            uriArr =
                                                                pickedList.toTypedArray() as Array<Uri?>
                                                        }
                                                    }
                                                    if (uriArr != null) {
                                                        pickedUris = uriArr
                                                    } else {
                                                        val singleData = intent.getData()
                                                        if (singleData != null) {
                                                            pickedUris = arrayOf<Uri>(singleData) as Array<Uri?>?
                                                        } else {
                                                            uri = this.cameraUri
                                                            if (uri != null) {
                                                                pickedUris = arrayOf<Uri>(uri) as Array<Uri?>?
                                                            }
                                                        }
                                                    }
                                                }
                                                deliverFileChooserResult(pickedUris)
                                                val myWebCoord: MyWebCoord? = if (pickedUris == null) this.webCoord else null
                                                if (pickedUris == null && myWebCoord != null) {
                                                    myWebCoord.post(InvalidPathToast(this))
                                                    return@sw12
                                                }
                                            }

                                            RequestCode.IMAGE_FILE -> if (resultCode == -1 && intent != null && this.currentPage != null) {
                                                val stringExtra =
                                                    intent.getStringExtra("EXTRA_PATH")
                                                if (TextUtils.isEmpty(stringExtra) || !File(
                                                        stringExtra!!
                                                    ).exists()
                                                ) {
                                                    showToast(this, AppR.string.invalid_path)
                                                } else if (isDarkMode()) {
                                                    onThemeChanged(false)
                                                } else {
                                                    saveQuickBack(this.dialogsState.appContext, true)
                                                    onThemeChanged(true)
                                                }
                                                return@sw12
                                            }

                                            RequestCode.EDIT_IMAGE -> if (resultCode == -1 && intent != null) {
                                                requestOpenTypeDialog(
                                                    0L,
                                                    3,
                                                    intent.getStringExtra("EXTRA_PATH"),
                                                    "image/*",
                                                    1
                                                )
                                                return@sw12
                                            }
                                        }
}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun WebViewActivity.startEdgeGesture() {
    val edgeTouchSide = this.miscState.edgeTouchSide
    val rightBack = ZoneSettings.gesRight.value != 2 && (if (!this.tabsState.layoutRtl) edgeTouchSide != 2 else edgeTouchSide != 1)
    resetEdgeTouch()
    this.floatingState.gestureMode = 2.toByte()
    this.floatingState.gestureZone = 0.toByte()
    if (isBarUpdatePending()) {
        if (this.tabsState.goBackPageBusy) {
            return
        }
        if (this.videoState.blockedTouchDownTime == 0L) {
            this.videoState.blockedTouchDownTime = System.currentTimeMillis()
            return
        } else {
            if (System.currentTimeMillis() - this.videoState.blockedTouchDownTime > 500) {
                resetBusyFlags()
                return
            }
            return
        }
    }
    this.videoState.blockedTouchDownTime = 0L
    if (SyncSettings.splash.value || this.currentPage == null) {
        finish()
        return
    }
    if (isReaderOpen()) {
        val dialogViewRead = this.dialogsState.viewReadDialog
        if (dialogViewRead!!.videoImage != null) {
            dialogViewRead.releaseVideo()
            return
        } else {
            dialogViewRead.handleBack()
            return
        }
    }
    if (this.dialogsState.menuListDialog != null) {
        closeMenuDialogs()
        return
    }
    if (this.videoMenuDialog != null) {
        closeVideoMenuDialog()
        return
    }
    if (this.newsMenuDialog != null) {
        closeNewsMenuDialog()
        return
    }
    val quickSubView = this.quickState.quickSubView
    if (quickSubView != null) {
        if (quickSubView.leaveEditMode()) {
            return
        }
        quickSubView.closeView()
        return
    }
    if (this.tabBarSubView != null) {
        closeTabBarSubView()
        return
    }
    if (this.transControl != null) {
        hideTransControl()
        return
    }
    if (this.coverView != null) {
        removeCoverView()
        return
    }
    if (this.pageState.popupPage != null) {
        destroyPopupPage()
        return
    }
    if (this.dialogsState.areaToolbar != null) {
        hidePageOverlays(true)
        return
    }
    if (!rightBack) {
        val webNestView = this.currentPage
        if (webNestView!!.crashShown) {
            webNestView.removeCrashView(null)
        }
        if (webNestView.sslShown) {
            webNestView.removeSslView()
            return
        }
    }
    val quickView = this.currentPage!!.quickPanel
    if (if (quickView == null) false else quickView.handleBack()) {
        return
    }
    val quickSchEdit = this.videoState.quickSchEdit
    if (quickSchEdit != null) {
        if (quickSchEdit.opening || quickSchEdit.edgeStarted) {
            return
        }
        val shownView = quickSchEdit.shownView
        if (shownView != null && shownView.handleBack()) {
            quickSchEdit.edgeStarted = true
            return
        }
    }
    if (this.videoFrame != null) {
        exitVideoFrame()
        return
    }
    if (this.findView != null) {
        exitFind(true)
        return
    }
    if (ReadSettings.guideGesture.value && edgeTouchSide != 0) {
        // a tip now, and the back swipe still goes back
        showGestureGuide()
    }
    if (!rightBack) {
        this.dialogsState.exitOnBackArmed = false
        this.miscState.edgeGestureStarted = edgeTouchSide != 0
        goBackFromBackKey(ExitOnBackListener(this))
    } else if (ZoneSettings.gesRight.value == 1) {
        goToNextTab()
    } else {
        startGoForward()
    }
}

fun WebViewActivity.lifecycleOnActionModeStarted(actionMode: ActionMode?, superOnActionModeStarted: (ActionMode?) -> Unit) {
    if (isReaderOpen()) {
        this.dialogsState.viewReadDialog!!.setActionMode(actionMode)
        superOnActionModeStarted(actionMode)
        return
    }
    this.dialogsState.actionMode = null
    if (!this.quickState.editingAddress && actionMode != null) {
        try {
            val menu = actionMode.getMenu()
            if (menu != null) {
                val size = menu.size()
                var maxId = -1
                var maxOrder = -1
                for (index in 0..<size) {
                    val item = menu.getItem(index)
                    if (item != null) {
                        maxId = max(maxId, item.getItemId())
                        maxOrder = max(maxOrder, item.getOrder())
                    }
                }
                menu.add(0, maxId + 1, maxOrder + 1, AppR.string.google_trans)
                    .setOnMenuItemClickListener(WvaActionModeMenuClick(this))
                this.dialogsState.actionMode = actionMode
                val myWebBody: MyWebBody? = if (this.currentPage != null) this.dialogsState.webBody else null
                if (this.currentPage != null && myWebBody != null) {
                    myWebBody.post(WvaInvalidateActionMode(this))
                }
            }
        } catch (unused: Exception) {
        }
    }
    superOnActionModeStarted(actionMode)
}

fun WebViewActivity.lifecycleOnConfigurationChanged(configuration: Configuration, superOnConfigurationChanged: (Configuration) -> Unit) {
    var myWebCoord: MyWebCoord? = null
    var dialogSetTts: DialogSetTts? = null
    myWebCoord = null
    var quickView: QuickView? = null
    var schPanel: QuickView? = null
    superOnConfigurationChanged(configuration)
    closeVideoMenuDialog()
    closeTabMenuDialog()
    closeNewsMenuDialog()
    closeUrlLinkDialog()
    closeSetUrlDialog()
    val dialogOpenType = this.openTypeDialog
    if (dialogOpenType != null) {
        dialogOpenType.dismiss()
        this.openTypeDialog = null
    }
    closePopupMenuDialog()
    val dialogWebSelect = this.webSelectDialog
    if (dialogWebSelect != null) {
        dialogWebSelect.dismiss()
        this.webSelectDialog = null
    }
    closeMenuDialogs()
    removeTabBarSubView()
    closeTransControl()
    hidePageOverlays(true)
    var landscape = false
    if (this.isPaused) {
        AppState.darkUi = isDarkTheme(true, configuration)
        AppState.darkWeb = isDarkTheme(false, configuration)
    } else {
        val darkPage = MainUtil.isDarkPage(this.onStartPage)
        val darkWeb = AppState.darkWeb
        val navBarColor = navBarColorNow()
        AppState.darkUi = isDarkTheme(true, configuration)
        AppState.darkWeb = isDarkTheme(false, configuration)
        applyDarkMode(navBarColor, darkPage, darkWeb)
    }
    if (this.videoFrame == null) {
        updateSystemBarPadding(BrowserSettings.showStatus.value, BrowserSettings.showNavi.value)
        val webCoord = this.webCoord
        if (webCoord != null) {
            webCoord.post(WvaConfigChangeRelayout(this))
        }
    }
    val isLandscape = isLandscapeScreen()
    val orientationChanged = this.miscState.lastLandscape != isLandscape
    this.miscState.lastLandscape = isLandscape
    applySideMargins(isLandscape)
    val webVideoFrame = this.videoFrame
    if (webVideoFrame != null && !this.isPaused) {
        webVideoFrame.hideMenus()
    }
    if (this.quickState.quickSubView != null) {
        closeQuickDeleteDialog()
        closeQuickEditDialog()
        closeQuickSubView()
    } else {
        val dialogCapture = this.captureDialog
        if (dialogCapture != null) {
            dialogCapture.applyEditSize(isLandscape)
        } else {
            val dialogSetTrans = this.setTransDialog
            if (dialogSetTrans != null) {
            } else {
                run {
                    run {
                        val dialogDownUrl = this.videoState.downUrlDialog
                        if (dialogDownUrl != null) {
                            dialogDownUrl.updateLayout(isLandscape)
                        } else {
                            val dialogAllowPopup = this.allowPopupDialog
                            if (dialogAllowPopup != null) {
                            } else {
                                val dialogBlockLink = this.blockLinkDialog
                                if (dialogBlockLink != null) {
                                } else {
                                    val dialogBlockImage = this.dialogsState.blockImageDialog
                                    if (dialogBlockImage != null) {
                                    } else {
                                        val dialogVideoList = this.videoListDialog
                                        if (dialogVideoList != null) {
                                        } else {
                                            val dialogDownFont = this.dialogsState.downFontDialog
                                            if (dialogDownFont != null) {
                                            } else {
                                                val dialogSeekWebText = this.seekWebTextDialog
                                                if (dialogSeekWebText == null) {
                                                    val dialogSetFull = this.setFullDialog
                                                    if (dialogSetFull != null) {
                                                        dialogSetFull.applyLandscape(isLandscape)
                                                    } else {
                                                        val dialogSetAdblock = this.setAdblockDialog
                                                        if (dialogSetAdblock != null) {
                                                        } else {
                                                            val dialogSetJava = this.setJavaDialog
                                                            if (dialogSetJava != null) {
                                                            } else {
                                                                val dialogSetTmem = this.setTmemDialog
                                                                if (dialogSetTmem != null) {
                                                                } else {
                                                                    val dialogViewRead = this.dialogsState.viewReadDialog
                                                                    if (dialogViewRead != null) {
                                                                        val ttsDialog =
                                                                            dialogViewRead.ttsDialog
                                                                        if (ttsDialog != null) {
                                                                            if (isLandscape) {
                                                                                val mainActivity =
                                                                                    dialogViewRead.activity
                                                                                if (mainActivity != null) {
                                                                                    landscape =
                                                                                        mainActivity.smallScreen
                                                                                }
                                                                            } else {
                                                                                landscape = isLandscape
                                                                            }
                                                                            ttsDialog.applyLandscape(landscape)
                                                                        }
                                                                        val darkShown =
                                                                            dialogViewRead.configChanged
                                                                        val darkUi = AppState.darkUi
                                                                        if (darkShown != darkUi) {
                                                                            dialogViewRead.configChanged =
                                                                                darkUi
                                                                            dialogViewRead.applyColors()
                                                                            if (dialogViewRead.page != null) {
                                                                                val sb =
                                                                                    StringBuilder(
                                                                                        "(function(){var els=document.querySelectorAll('p');if(els&&(els.length>0)){for(var i=0;i<els.length;i++){els[i].style.color="
                                                                                    )
                                                                                sb.append(if (AppState.darkUi) "'white'" else "'black'")
                                                                                sb.append(";}}})();")
                                                                                evalJs(
                                                                                    dialogViewRead.page,
                                                                                    sb.toString(),
                                                                                    true
                                                                                )
                                                                            }
                                                                        }
                                                                    } else {
                                                                        val dialogViewSrc =
                                                                            this.dialogsState.viewSrcDialog
                                                                        if (dialogViewSrc != null) {
                                                                            val darkShown =
                                                                                dialogViewSrc.darkShown
                                                                            val darkUi = AppState.darkUi
                                                                            if (darkShown != darkUi) {
                                                                                dialogViewSrc.darkShown =
                                                                                    darkUi
                                                                                dialogViewSrc.applyTheme()
                                                                                if (MainUtil.lacksAlgorithmicDark()) {
                                                                                    dialogViewSrc.applyDarkCss(
                                                                                        AppState.darkUi
                                                                                    )
                                                                                } else {
                                                                                    setAlgorithmicDark(
                                                                                        dialogViewSrc.srcView!!.getSettings(),
                                                                                        AppState.darkUi
                                                                                    )
                                                                                }
                                                                            }
                                                                            dialogViewSrc.refreshButtons()
                                                                        } else {
                                                                            val dialogWebBookList =
                                                                                this.bookmarksDialog
                                                                            if (dialogWebBookList != null) {
                                                                                dialogWebBookList.onConfig(
                                                                                    configuration
                                                                                )
                                                                            } else {
                                                                                val dialogListBook =
                                                                                    this.historyDialog
                                                                                if (dialogListBook != null) {
                                                                                    dialogListBook.onConfig(
                                                                                        configuration
                                                                                    )
                                                                                } else {
                                                                                    val webGridDialog =
                                                                                        this.dialogsState.gridDialog
                                                                                    if (webGridDialog != null) {
                                                                                        webGridDialog.refresh()
                                                                                    } else {
                                                                                        val siteDlg = this.dialogsState.siteDialog
                                                                                        if (siteDlg != null) {
                                                                                            siteDlg.refresh()
                                                                                        }
                                                                                        run {
                                                                                            run {
                                                                                                val dialogSetUseTts =
                                                                                                    this.dialogsState.setUseTtsDialog
                                                                                                if (dialogSetUseTts != null && (dialogSetUseTts.ttsDialog.also {
                                                                                                        dialogSetTts =
                                                                                                            it
                                                                                                    }) != null) {
                                                                                                    dialogSetTts!!.applyLandscape(
                                                                                                        isLandscape
                                                                                                    )
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else if (dialogSeekWebText.sampleFrame != null) {
                                                    dialogSeekWebText.sampleFrame!!.setVisibility(if (isLandscape && dialogSeekWebText.isSmallScreen()) View.GONE else View.VISIBLE)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    val dialogPreview = this.previewDialog
    if (dialogPreview != null) {
        dialogPreview.updateLayout(isLandscape)
    } else {
        val dialogPreImage = this.preImageDialog
        if (dialogPreImage != null) {
            dialogPreImage.applyLandscape(isLandscape)
        }
    }
    val quickSchEdit = this.videoState.quickSchEdit
    if (quickSchEdit != null && (quickSchEdit.shownView.also { schPanel = it }) != null) {
        schPanel!!.applyColumns(isLandscape)
    }
    val webNestView = this.currentPage
    quickView = if (webNestView != null) webNestView.quickPanel else null
    if (webNestView != null && quickView != null) {
        quickView.applyColumns(isLandscape)
    }
    val webSearchAdapter = this.searchAdapter
    if (webSearchAdapter != null) {
        webSearchAdapter.query = null
        val arrayList = webSearchAdapter.items
        if (arrayList != null && !arrayList.isEmpty()) {
            webSearchAdapter.notifyDataSetChanged()
        }
    }
    applyVideoOrientation(true)
    if (orientationChanged == isDarkBars(this.onStartPage)) {
        val webCoord = this.webCoord
        if (webCoord != null) {
            webCoord.post(WvaConfigThemeChanged(this))
            return
        }
        return
    }
    myWebCoord = if (!PdfSettings.topToBottom.value) null else this.webCoord
    if (!PdfSettings.topToBottom.value || myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaOnConfigurationChangedTask(this))
}

fun WebViewActivity.lifecycleOnCreate(bundle: Bundle?, superOnCreate: (Bundle?) -> Unit) {
    var debugEnabled = false
    var pageUrl: String? = null
    var createFlag = false
    var created = false
    var useAppKeypad = false
    var startUrl: String? = null
    var myWebBody: MyWebBody? = null
    var webViewActivity: WebViewActivity? = null
    var handler: Handler? = null
    var webNestLayout: WebNestLayout? = null
    var appBarLayout: BarScroll? = null
    var wvaBarOffsetListener: WvaBarOffsetListener? = null
    var arrayList: ArrayList<Any?>? = null
    var layoutParams: RelativeLayout.LayoutParams? = null
    var body: MyWebBody? = null
    var handTop = 0
    var themeColor = 0
    var themeLight = 0
    var mainAppO: MainApp? = null
    var app: MainApp? = null
    var sugEngine = 0
    val activity = this
    activity.ownBackHandling = true
    activity.isBrowserScreen = true
    // start the page renderer right after the first frame (first start of the process only; it waits for the
    // launch to finish, so the window is not touched here)
    WebStartUp.begin(activity)
    RendererRecovery.attach(activity)
    // rebuilt for another space: starts like after a restart (its open tab, nothing of the screen it replaces)
    val spaceSwitch = WorkspaceSwitch.takeRelaunch()
    if (spaceSwitch) {
        activity.setIntent(MainUtil.browserIntent(activity).putExtra("EXTRA_PATH", "web_restart_url"))
    }
    superOnCreate(if (spaceSwitch) null else bundle)
    activity.installSplashScreen()
    activity.registerBackCallback(activity.keyBackListener)
    val isApi = MainConst.isApi30
    if (isApi) {
        activity.installInsetsListener()
        applyDisplayCutout(activity.getWindow(), false, true)
    }
    val applicationContext = activity.getApplicationContext()
    activity.dialogsState.appContext = applicationContext
    activity.dialogsState.topBarColor = 1234
    activity.dialogsState.bottomBarColor = 1234
    val mainApp = MainApp.from(applicationContext)
    if (mainApp != null) {
        mainApp.payState = 0.toByte()
        mainApp.pendingVideoUrl = null
        mainApp.instaAppId = null
        mainApp.notificationCache = null
        mainApp.clipboardCache = null
        mainApp.inputMethodCache = null
        mainApp.cookieCache = null
        mainApp.mainHandler = null
        AppState.appLocale = null
        AppState.localeCache = null
    }
    activity.tabsState.layoutRtl = isRtl(activity.dialogsState.appContext)
    activity.miscState.createBusy = true
    if (BrowserSettings.sugEngine.value < 0) {
        if (MainUtil.isChinaLocale()) {
            BrowserSettings.editLocal { set(BrowserSettings.sugEngine, 1) }
            sugEngine = 1
        } else {
            BrowserSettings.editLocal { set(BrowserSettings.sugEngine, 2) }
            sugEngine = 2
        }
        putInt(activity.dialogsState.appContext, 14, sugEngine, "mSugEng")
    }
    activity.launchForResult(null, RequestCode.QUICK_SETTINGS)
    activity.launchForResult(null, RequestCode.QUICK_ADD)
    activity.launchForResult(null, RequestCode.MAIN_SETTINGS)
    activity.launchForResult(null, RequestCode.UNLOCK_SECRET)
    activity.launchForResult(null, RequestCode.DOWNLOAD_LIST)
    activity.launchForResult(null, RequestCode.CAMERA_OR_FILE)
    activity.launchForResult(null, RequestCode.PICK_IMAGES)
    activity.launchForResult(null, RequestCode.PICK_FILES)
    activity.launchForResult(null, RequestCode.IMAGE_FILE)
    activity.launchForResult(null, RequestCode.EDIT_IMAGE)
    activity.launchForResult(null, RequestCode.VOICE_SEARCH)
    activity.launchForResult(null, RequestCode.CLEAN_SETTINGS)
    activity.launchForResult(null, RequestCode.SAVE_LOCATION)
    activity.launchForResult(null, RequestCode.UNLOCK)
    activity.launchForResult(null, RequestCode.DIALOG_SETTINGS)
    activity.launchForResult(null, RequestCode.UNLOCK_SITE)
    activity.launchForResult(null, RequestCode.UNLOCK_LAUNCH)
    activity.launchForResult(null, RequestCode.UNLOCK_SPACE)
    val webTopView = AppR.id.web_top_view
    val webBotView = AppR.id.web_bot_view
    val webCastCtrl = AppR.id.web_cast_ctrl
    activity.dialogsState.topBehavior = MyBehaviorTop(activity, null)
    activity.behaviorBot = MyBehaviorBot(activity, null)
    val webBody = MyWebBody(activity)
    webBody.flipSides = isRtl(activity)
    if (isApi) {
        val topBarColor = if (AppState.darkUi) -16777216 else -1
        webBody.topBarColor = topBarColor
        webBody.bottomColor = topBarColor
        webBody.topPaint = MyWebBody.fillPaint(topBarColor)
        webBody.bottomPaint = MyWebBody.fillPaint(webBody.bottomColor)
        val topHeight = MainApp.dp48
        webBody.topHeight = topHeight
        webBody.bottomHeight = topHeight
        webBody.topRect = Rect()
        webBody.bottomRect = Rect()
        webBody.editTint = 0
        webBody.editPaint = MyWebBody.fillPaint(0)
        webBody.editLeftRect = Rect()
        webBody.editTopRect = Rect()
        webBody.editRightRect = Rect()
        webBody.editBottomRect = Rect()
    }
    webBody.setLayoutParams(ViewGroup.LayoutParams(-1, -1))
    val myWebCoord = MyWebCoord(activity)
    webBody.addView(myWebCoord, com.mycompany.app.outline.LegacyViews.relativeParams(-1, -1, 2, webCastCtrl))
    var barReserve = max(MainUtil.topBarHeight(), MainUtil.bottomInsetTotal())
    if (barReserve == 0) {
        barReserve = MainApp.dp48
    } else if (BrowserSettings.fixTop.value) {
        barReserve += MainApp.dp48
    }
    // The bars' scroll-hide is a Compose / Material 3 state (BarScroll) instead of an AppBarLayout in
    // the coordinator; the bars follow its offset through the listener below (formulas of MyBehaviorTop / Bot).
    val barScroll = BarScroll()
    barScroll.setRange(barReserve)
    val pageLayout = WebNestLayout(activity)
    myWebCoord.addView(pageLayout, -1, -1)
    // just switched to this space: the picture of its open tab shows from the first frame on
    if (!SyncSettings.secretMode.value) {
        SpaceCover.showEarly(activity, pageLayout, com.mycompany.app.pref.store.Workspaces.currentNumber(), com.mycompany.app.pref.PrefSync.normalIndex)
    }
    // The overlay stack (Compose layers above the legacy tree) exists before the bars, which are its
    // lowest coordinator-band slots: top bar at the top, bottom bar at the bottom of the coordinator's area.
    val overlayStack = com.mycompany.app.ui.browser.OverlayStack(webBody)
    webBody.overlays = overlayStack
    overlayStack.setPadding(webBody.paddingLeft, webBody.paddingTop, webBody.paddingRight, webBody.paddingBottom)
    activity.floatingState.overlays = overlayStack
    val myBarFrame = MyBarFrame(activity)
    myBarFrame.setIsTop(true)
    myBarFrame.setId(webTopView)
    val topParams = com.mycompany.app.ui.browser.OverlayParams(-1, -2)
    topParams.gravity = 48
    overlayStack.addCoordView(myBarFrame, topParams, 0)
    val bottomBarFrame = MyBarFrame(activity)
    bottomBarFrame.setId(webBotView)
    val bottomParams = com.mycompany.app.ui.browser.OverlayParams(-1, -2)
    bottomParams.gravity = 80
    overlayStack.addCoordView(bottomBarFrame, bottomParams, 1)
    val barTop = activity.dialogsState.topBehavior!!
    val barBot = activity.behaviorBot!!
    // The bars slide by moving their slots (shiftY). The overlays anchored to a bar follow its edges (coordinator
    // coordinates), and in bar layouts 1 / 2 the page follows the top bar's bottom edge (was MyBehaviorWebTop).
    val pushBars = Runnable {
        val coordH = myWebCoord.height
        val topTop = topParams.shiftY.toInt()
        val topBottom = topTop + (if (myBarFrame.visibility == View.GONE) 0 else myBarFrame.height)
        val botH = if (bottomBarFrame.visibility == View.GONE) 0 else bottomBarFrame.height
        val botTop = coordH - botH + bottomParams.shiftY.toInt()
        overlayStack.setBars(topTop, topBottom, botTop, coordH + bottomParams.shiftY.toInt())
        if (activity.pageFollowsTopBar) {
            activity.pageLayout?.translationY = topBottom.toFloat()
        }
    }
    activity.pageState.pushBars = pushBars
    barScroll.offsetListeners.add(BarScroll.OffsetListener { top ->
        topParams.shiftY = barTop.shift(myBarFrame, top, barScroll.getHeight())
        bottomParams.shiftY = barBot.shift(bottomBarFrame, top, barScroll.getHeight())
        overlayStack.relayout(myBarFrame)
        overlayStack.relayout(bottomBarFrame)
        pushBars.run()
    })
    // A bar's height or visibility change re-places it (the behaviours did this on every coordinator layout).
    val replace = View.OnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
        if (bottom - top != oldBottom - oldTop) barScroll.publish(true)
        pushBars.run()
    }
    myWebCoord.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
        activity.floatingState.overlays?.setCoordRect(left, top, right - left, bottom - top)
        pushBars.run()
    }
    myBarFrame.addOnLayoutChangeListener(replace)
    bottomBarFrame.addOnLayoutChangeListener(replace)
    val frameLayout = FrameLayout(activity)
    frameLayout.setId(webCastCtrl)
    frameLayout.setVisibility(View.GONE)
    val bottomViewParams = RelativeLayout.LayoutParams(-1, -2)
    bottomViewParams.addRule(12)
    webBody.addView(frameLayout, bottomViewParams)
    activity.dialogsState.webBody = webBody
    activity.webCoord = myWebCoord
    activity.barScroll = barScroll
    activity.pageLayout = pageLayout
    activity.topBarFrame = myBarFrame
    activity.bottomBarFrame = bottomBarFrame
    // The content view is a Compose root that hosts the hand-built layout (BrowserScreen).
    com.mycompany.app.ui.browser.BrowserScreen.setContent(activity, webBody, barScroll, overlayStack)
    activity.browserTicker = com.mycompany.app.ui.browser.BrowserState.Ticker(activity.browserState) { Pair(activity.currentPage, AppState.darkUi) }
    MainApp.updateDarkMode(activity.getResources())
    if (SyncSettings.splash.value) {
        // (the browser used to hold back its first draw until the intro overlay was there, WvaFirstDrawListener; the
        // first start now opens the setup wizard, OnboardingActivity, so the browser draws normally behind it)
        initUserAgent(activity.dialogsState.appContext)
        if (SecretSettings.checkTab.value) {
            SecretSettings.edit(activity.dialogsState.appContext) { set(SecretSettings.checkTab, false) }
        }
        debugEnabled = true
    } else {
        debugEnabled = true
        applyScreenOrientation(activity)
    }
    if (PdfSettings.debugMode.value) {
        WebView.setWebContentsDebuggingEnabled(debugEnabled)
    }
    activity.currentTabIndex = if (SyncSettings.secretMode.value) PrefSync.currentSecretIndex else SyncSettings.normalIndex.value
    val data = activity.getIntent().getData()
    if (data != null) {
        val newPageUrl = data.toString()
        activity.dialogsState.pageUrl = newPageUrl
        if (isContentUri(newPageUrl) && "content" == activity.getIntent()
                .getScheme() && ("text/html" != activity.getIntent().getType())
        ) {
            created = true
            activity.pageState.createBusy = true
            createFlag = created
            pageUrl = null
        } else {
            pageUrl = null
            createFlag = true
        }
    } else {
        val stringExtra = activity.getIntent().getStringExtra("EXTRA_PATH")
        activity.dialogsState.pageUrl = stringExtra
        if ("web_restart_url" == stringExtra) {
            created = true
            activity.dialogsState.createBusy = true
            createFlag = created
            pageUrl = null
        } else {
            val booleanExtra =
                activity.getIntent().getBooleanExtra("EXTRA_START", false)
            activity.dialogsState.extraStart = booleanExtra
            if (booleanExtra || "short_new_tab" != activity.dialogsState.pageUrl) {
                pageUrl = null
                createFlag = true
            } else {
                pageUrl = null
                activity.dialogsState.pageUrl = null
                createFlag = true
                activity.pageState.startupBusy = true
                activity.launchedExternally =
                    activity.getIntent().getBooleanExtra("EXTRA_LAUNCH_LOCK", false)
            }
        }
    }
    if (!activity.dialogsState.createBusy) {
        if (TextUtils.isEmpty(activity.dialogsState.pageUrl)) {
            activity.dialogsState.useCurrentTab = createFlag
            activity.pageState.useAppKeypad = TtsSettings.appKeypad.value
            if ("..home_link" == BrowserSettings.startPage.value) {
                startUrl = homePageUrl()
            } else {
                startUrl = normalizeUrl(null, BrowserSettings.startPage.value)
                if (TextUtils.isEmpty(startUrl)) {
                    startUrl = null
                }
            }
            activity.dialogsState.pageUrl = startUrl
        } else {
            if (TtsSettings.appKeypad.value) {
                useAppKeypad = false
                activity.pageState.useAppKeypad =
                    activity.getIntent().getBooleanExtra("EXTRA_KEYPAD", false)
            } else {
                useAppKeypad = false
            }
            activity.addTabPending = activity.getIntent().getBooleanExtra("EXTRA_EXT", useAppKeypad)
            activity.launchedExternally =
                activity.getIntent().getBooleanExtra("EXTRA_LAUNCH_LOCK", useAppKeypad)
        }
        activity.onStartPage = "file:///android_asset/shortcut.html" == activity.dialogsState.pageUrl
        if (TtsSettings.skipKeypad.value) {
            TtsSettings.edit(activity.dialogsState.appContext) { set(TtsSettings.skipKeypad, useAppKeypad) }
            activity.pageState.useAppKeypad = useAppKeypad
        }
        activity.dialogsState.showStatusBar = true
        activity.videoState.showNavBar = true
        activity.pageState.textZoomPending = true
        activity.pageState.textZoomNudged = useAppKeypad
        activity.setupLock = Any()
        activity.runInBackground(WvaCreateSetup(activity))
        if (activity.webDestroy == null) {
            activity.webDestroy = MainWebDestroy(
                activity,
                activity.mainHandler,
                WvaWebBusyListener(activity)
            )
            mainAppO = MainApp.from(activity.getApplicationContext())
            if (mainAppO != null) {
                val mainWebDestroy = activity.webDestroy
                val downBusyListener = WvaDownBusyListener(activity)
                mainAppO.webDestroy = mainWebDestroy
                mainAppO.downBusyListener = downBusyListener
            }
        }
        myWebBody = activity.dialogsState.webBody
        webViewActivity = activity
        if (myWebBody != null) {
            val insetListener = WvaMainInsetListener(activity)
            activity.insetView = myWebBody
            activity.insetListener = insetListener
            activity.initMainScreenOn(activity.webCoord)
            activity.screenOnFresh = true
            activity.updateScreenOn(true)
            activity.dialogsState.webBody!!.setSizeListener(WvaBodySizeListener(activity))
            webNestLayout = activity.pageLayout
            if (webNestLayout != null) {
                if (BrowserSettings.fixTop.value) {
                    webNestLayout.setTranslationY(0.0f)
                } else {
                    webNestLayout.setTranslationY(MainUtil.topBarHeight().toFloat())
                }
            }
            if (BrowserSettings.headMode() == 2) {
                themeColor = PrefWeb.headColor
                if (themeColor == 0) {
                    MainUtil.resetTextSizePref()
                    themeColor = PrefWeb.headColor
                }
                activity.dialogsState.themeColor = themeColor
                themeLight = PrefWeb.iconColor
                if (themeLight == 0) {
                    MainUtil.resetTextSizePref()
                    themeLight = PrefWeb.iconColor
                }
                activity.miscState.themeLight = themeLight
            } else {
                activity.dialogsState.themeColor = 0
                activity.miscState.themeLight = 0
            }
            activity.tintBarFrames(activity.dialogsState.themeColor)
            if (TtsSettings.webLand.value == 0 || TtsSettings.webLand2.value != 0) {
                val landscape = activity.isLandscapeScreen()
                activity.applySideMargins(landscape)
                activity.updateSplitLand(landscape)
                activity.refreshBarFrames()
            }
            if (TtsSettings.handMode.value) {
                layoutParams =
                    activity.webCoord!!.getLayoutParams() as RelativeLayout.LayoutParams?
                if (layoutParams != null) {
                    body = activity.dialogsState.webBody
                    if (body == null) {
                        handTop = 0
                    } else {
                        handTop = body.handTopPos(activity)
                    }
                    Zn = handTop
                    if (isApi) {
                        val webBodyNow = activity.dialogsState.webBody
                        activity.pageThemeColor()
                        webBodyNow!!.insetTop = handTop
                        webBodyNow.topColor = webBodyNow.topBarColor
                    }
                    layoutParams.topMargin = handTop
                }
                activity.updateHandListener()
                activity.addHandModeBar()
            }
            activity.updateBarBehavior()
            activity.webCoord!!.setTouchListener(WvaPageTouchListener(activity))
            activity.pageState.barOffset = 0
            appBarLayout = activity.barScroll
            wvaBarOffsetListener = WvaBarOffsetListener(activity)
            if (!appBarLayout!!.offsetListeners.contains(wvaBarOffsetListener)) {
                appBarLayout.offsetListeners.add(wvaBarOffsetListener)
            }
            val sizeItemD = screenSize(activity)
            val screenWidth = if (sizeItemD != null) sizeItemD.width else 0
            activity.updateSplitLand(activity.isLandscapeScreen())
            activity.buildMainBars(null, 0, 0, null, screenWidth)
            val wva = this
            wva.buildBottomBar(null, 0, 0, null, screenWidth)
            wva.refreshBarFrames()
            webViewActivity = wva
        }
        if (webViewActivity.miscState.eventReceiver == null) {
            webViewActivity.miscState.eventReceiver = WvaEventReceiver(webViewActivity)
            val intentFilter = IntentFilter()
            intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_COMPLETE")
            intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_DELETE")
            intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_PIP_FRWD")
            intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_PIP_PLAY")
            intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_PIP_FFWD")
            intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_BACK_STOP")
            intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_SECRET_EXIT")
            ContextCompat.registerReceiver(webViewActivity, webViewActivity.miscState.eventReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
        handler = webViewActivity.mainHandler
        if (handler != null) {
            handler.post(WvaCreateViews(webViewActivity))
            com.mycompany.app.ui.tips.TipManager.sessionStarted(webViewActivity)
            if (!SyncSettings.splash.value || webViewActivity.miscState.introView != null || webViewActivity.dialogsState.webBody == null) {
                return
            }
            webViewActivity.runInBackground(WvaInitGlide(webViewActivity))
            val mainHandler = webViewActivity.mainHandler
            if (mainHandler != null) {
                mainHandler.post(WvaCreateIntro(webViewActivity))
                return
            }
            return
        }
    }
    activity.dialogsState.useCurrentTab = createFlag
    activity.dialogsState.pageUrl = pageUrl
    useAppKeypad = false
    activity.onStartPage = "file:///android_asset/shortcut.html" == activity.dialogsState.pageUrl
    if (TtsSettings.skipKeypad.value) {
        TtsSettings.edit(activity.dialogsState.appContext) { set(TtsSettings.skipKeypad, useAppKeypad) }
        activity.pageState.useAppKeypad = useAppKeypad
    }
    activity.dialogsState.showStatusBar = true
    activity.videoState.showNavBar = true
    activity.pageState.textZoomPending = true
    activity.pageState.textZoomNudged = useAppKeypad
    activity.setupLock = Any()
    activity.runInBackground(WvaCreateSetup(activity))
    if (activity.webDestroy == null) {
        activity.webDestroy = MainWebDestroy(
            activity,
            activity.mainHandler,
            WvaWebBusyListener(activity)
        )
        mainAppO = MainApp.from(activity.getApplicationContext())
        if (mainAppO != null) {
            val mainWebDestroy = activity.webDestroy
            val wvaDownBusyListener = WvaDownBusyListener(activity)
            mainAppO.webDestroy = mainWebDestroy
            mainAppO.downBusyListener = wvaDownBusyListener
        }
    }
    myWebBody = activity.dialogsState.webBody
    webViewActivity = activity
    if (myWebBody != null) {
        val wvaMainInsetListener = WvaMainInsetListener(activity)
        activity.insetView = myWebBody
        activity.insetListener = wvaMainInsetListener
        activity.initMainScreenOn(activity.webCoord)
        activity.screenOnFresh = true
        activity.updateScreenOn(true)
        activity.dialogsState.webBody!!.setSizeListener(WvaBodySizeListener(activity))
        webNestLayout = activity.pageLayout
        if (webNestLayout != null) {
            if (BrowserSettings.fixTop.value) {
                webNestLayout.setTranslationY(0.0f)
            } else {
                webNestLayout.setTranslationY(MainUtil.topBarHeight().toFloat())
            }
        }
        if (BrowserSettings.headMode() == 2) {
            themeColor = PrefWeb.headColor
            if (themeColor == 0) {
                MainUtil.resetTextSizePref()
                themeColor = PrefWeb.headColor
            }
            activity.dialogsState.themeColor = themeColor
            themeLight = PrefWeb.iconColor
            if (themeLight == 0) {
                MainUtil.resetTextSizePref()
                themeLight = PrefWeb.iconColor
            }
            activity.miscState.themeLight = themeLight
        } else {
            activity.dialogsState.themeColor = 0
            activity.miscState.themeLight = 0
        }
        activity.tintBarFrames(activity.dialogsState.themeColor)
        if (TtsSettings.webLand.value == 0) {
            val landscape = activity.isLandscapeScreen()
            activity.applySideMargins(landscape)
            activity.updateSplitLand(landscape)
            activity.refreshBarFrames()
        } else {
            val landscape = activity.isLandscapeScreen()
            activity.applySideMargins(landscape)
            activity.updateSplitLand(landscape)
            activity.refreshBarFrames()
        }
        if (TtsSettings.handMode.value) {
            layoutParams = activity.webCoord!!.getLayoutParams() as RelativeLayout.LayoutParams?
            if (layoutParams != null) {
                body = activity.dialogsState.webBody
                if (body == null) {
                    handTop = 0
                } else {
                    handTop = body.handTopPos(activity)
                }
                Zn = handTop
                if (isApi) {
                    val webBodyNow = activity.dialogsState.webBody
                    activity.pageThemeColor()
                    webBodyNow!!.insetTop = handTop
                    webBodyNow.topColor = webBodyNow.topBarColor
                }
                layoutParams.topMargin = handTop
            }
            activity.updateHandListener()
            activity.addHandModeBar()
        }
        activity.updateBarBehavior()
        activity.webCoord!!.setTouchListener(WvaPageTouchListener(activity))
        activity.pageState.barOffset = 0
        appBarLayout = activity.barScroll
        wvaBarOffsetListener = WvaBarOffsetListener(activity)
        if (!appBarLayout!!.offsetListeners.contains(wvaBarOffsetListener)) {
            appBarLayout.offsetListeners.add(wvaBarOffsetListener)
        }
        val sizeItemD = screenSize(activity)
        val screenWidth = if (sizeItemD != null) sizeItemD.width else 0
        activity.updateSplitLand(activity.isLandscapeScreen())
        activity.buildMainBars(null, 0, 0, null, screenWidth)
        val wva = this
        wva.buildBottomBar(null, 0, 0, null, screenWidth)
        wva.refreshBarFrames()
        webViewActivity = wva
    }
    if (webViewActivity.miscState.eventReceiver == null) {
        webViewActivity.miscState.eventReceiver = WvaEventReceiver(webViewActivity)
        val intentFilter = IntentFilter()
        intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_COMPLETE")
        intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_DELETE")
        intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_PIP_FRWD")
        intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_PIP_PLAY")
        intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_PIP_FFWD")
        intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_BACK_STOP")
        intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_SECRET_EXIT")
        ContextCompat.registerReceiver(webViewActivity, webViewActivity.miscState.eventReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }
    handler = webViewActivity.mainHandler
    if (handler != null) {
        handler.post(WvaCreateViews(webViewActivity))
    }
}

fun WebViewActivity.lifecycleOnCreateContextMenu(contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenuInfo?) {
    val extra: String?
    if (!TextUtils.isEmpty(this.dialogsState.pageHost) && this.dialogsState.pageHost!!.startsWith("www.pinterest.")) {
        val myWebBody: MyWebBody? = if (this.currentPage == null) null else this.dialogsState.webBody
        if (this.currentPage == null || myWebBody == null) {
            return
        } else {
            myWebBody.postDelayed(WvaPinterestBodyReset(this), 400L)
        }
    }
    val webNestView: WebNestView? = if (this.dialogsState.areaToolbar == null && this.videoFrame == null) this.currentPage else null
    if (this.dialogsState.areaToolbar == null && this.videoFrame == null && webNestView != null) {
        try {
            val hitTestResult = webNestView.getHitTestResult()
            val type = hitTestResult.getType()
            var linkUrl: String? = null
            if (type == 5) {
                extra = hitTestResult.getExtra()
            } else if (type == 7) {
                linkUrl = hitTestResult.getExtra()
                extra = null
            } else {
                extra = if (type == 8) hitTestResult.getExtra() else null
            }
            this.currentPage!!.requestFocusNodeHref(AnchorHandler(this, linkUrl, extra).obtainMessage())
        } catch (unused: Exception) {
        }
    }
}

fun WebViewActivity.lifecycleOnDestroy(superOnDestroy: () -> Unit) {
    this.browserTicker?.stop()
    val eventReceiver = this.miscState.eventReceiver
    if (eventReceiver != null) {
        unregisterReceiver(eventReceiver)
        this.miscState.eventReceiver = null
    }
    DataTrans.instance(this.dialogsState.appContext).languages = null
    if (!this.isClosing) {
        this.isClosing = true
        this.dialogsState.addPullViewBusy = false
        closeAllDialogs()
        closePopupMenus()
        val mainFilterSvc = this.miscState.filterService
        if (mainFilterSvc != null) {
            mainFilterSvc.release()
            this.miscState.filterService = null
        }
        val mainScriptSvc = this.miscState.scriptService
        if (mainScriptSvc != null) {
            mainScriptSvc.release()
            this.miscState.scriptService = null
        }
        shutdownTts()
    }
    val webVideoFrame = this.videoFrame
    if (webVideoFrame != null) {
        webVideoFrame.exitCustomView()
        this.videoFrame = null
    }
    superOnDestroy()
    cancelSearchTask()
    val quickNews = this.quickState.quickNews
    if (quickNews != null) {
        quickNews.release()
        this.quickState.quickNews = null
    }
    this.quickState.newsListType = 0
    val mainTransNews = this.quickState.transNews
    if (mainTransNews != null) {
        mainTransNews.release()
        this.quickState.transNews = null
    }
    removeLoadView()
    val webEmgLoad = this.emgLoad
    if (webEmgLoad != null) {
        webEmgLoad.release()
        this.emgLoad = null
    }
    val webHmgLoad = this.miscState.hmgLoad
    if (webHmgLoad != null) {
        webHmgLoad.release()
        this.miscState.hmgLoad = null
    }
    removeIntro()
    exitFind(true)
    clearPermissionRequest()
    val mainWebDestroy = this.webDestroy
    if (mainWebDestroy != null) {
        mainWebDestroy.outer = null
        mainWebDestroy.handler = null
        mainWebDestroy.webBusyListener = null
        mainWebDestroy.delItems = null
        this.webDestroy = null
    }
    val keyHelper = this.keyHelper
    if (keyHelper != null) {
        keyHelper.keyListener = null
        keyHelper.decorRoot = null
        keyHelper.padView = null
        this.keyHelper = null
    }
    val myWebBody = this.dialogsState.webBody
    if (myWebBody != null) {
        myWebBody.topPaint = null
        myWebBody.topRect = null
        myWebBody.bottomPaint = null
        myWebBody.bottomRect = null
        myWebBody.editPaint = null
        myWebBody.editLeftRect = null
        myWebBody.editTopRect = null
        myWebBody.editRightRect = null
        myWebBody.editBottomRect = null
        myWebBody.sizeCallback = null
        myWebBody.handCallback = null
        this.dialogsState.webBody = null
    }
    val myWebCoord = this.webCoord
    if (myWebCoord != null) {
        myWebCoord.clearIcon()
        myWebCoord.seekListener = null
        myWebCoord.captureListener = null
        myWebCoord.pullListener = null
        this.webCoord = null
    }
    val myBarFrame = this.topBarFrame
    if (myBarFrame != null) {
        myBarFrame.linePaint = null
        myBarFrame.mainBar = null
        myBarFrame.secondBar = null
        myBarFrame.tabBarView = null
        this.topBarFrame = null
    }
    val bottomBarFrame = this.bottomBarFrame
    if (bottomBarFrame != null) {
        bottomBarFrame.linePaint = null
        bottomBarFrame.mainBar = null
        bottomBarFrame.secondBar = null
        bottomBarFrame.tabBarView = null
        this.bottomBarFrame = null
    }
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        myBarView.release()
        this.barsState.topBarView = null
    }
    val bottomBarView = this.barsState.bottomBarView
    if (bottomBarView != null) {
        bottomBarView.release()
        this.barsState.bottomBarView = null
    }
    val myAddrView = this.barsState.editorBar
    if (myAddrView != null) {
        myAddrView.release()
        this.barsState.editorBar = null
    }
    val myIconView = this.engineIcon
    if (myIconView != null) {
        myIconView.release()
        this.engineIcon = null
    }
    val addrLeftIcon = this.barsState.addrLeftIcon
    if (addrLeftIcon != null) {
        addrLeftIcon.release()
        this.barsState.addrLeftIcon = null
    }
    val addrRightIcon = this.barsState.addrRightIcon
    if (addrRightIcon != null) {
        addrRightIcon.release()
        this.barsState.addrRightIcon = null
    }
    val deleteIcon = this.deleteIcon
    if (deleteIcon != null) {
        deleteIcon.release()
        this.deleteIcon = null
    }
    val myEditAuto = this.editAuto
    if (myEditAuto != null) {
        myEditAuto.release()
        this.editAuto = null
    }
    val myProgressBar = this.dialogsState.progressBar
    if (myProgressBar != null) {
        myProgressBar.release()
        this.dialogsState.progressBar = null
    }
    val webNestFrame = this.tabsState.currentFrame
    if (webNestFrame != null) {
        webNestFrame.releaseFrame()
        this.tabsState.currentFrame = null
    }
    this.currentPage = null
    val myPullView = this.pullView
    if (myPullView != null) {
        myPullView.refreshListener = null
        myPullView.spinner = null
        myPullView.decelerate = null
        myPullView.scaleDownAnim = null
        myPullView.alphaStartAnim = null
        myPullView.alphaMaxAnim = null
        this.pullView = null
    }
    val swipeTabFrame = this.swipeTabFrame
    if (swipeTabFrame != null) {
        swipeTabFrame.releaseFrame()
        this.swipeTabFrame = null
    }
    val swipePrevFrame = this.tabsState.swipePrevFrame
    if (swipePrevFrame != null) {
        swipePrevFrame.releaseFrame()
        this.tabsState.swipePrevFrame = null
    }
    this.tabBarView = null
    val myButtonImage = this.sideButton
    if (myButtonImage != null) {
        myButtonImage.release()
        this.sideButton = null
    }
    val webTabBarAdapter = this.tabBar
    if (webTabBarAdapter != null) {
        webTabBarAdapter.release()
        this.tabBar = null
    }
    val myScrollBar = this.scrollBar
    if (myScrollBar != null) {
        myScrollBar.releaseAll()
        this.scrollBar = null
    }
    val webUpView = this.upButton
    if (webUpView != null) {
        webUpView.release()
        this.upButton = null
    }
    val topDownIcon = this.topDownIcon
    if (topDownIcon != null) {
        topDownIcon.release()
        this.topDownIcon = null
    }
    val bottomDownIcon = this.barsState.bottomDownIcon
    if (bottomDownIcon != null) {
        bottomDownIcon.release()
        this.barsState.bottomDownIcon = null
    }
    val webDownView = this.imageButton
    if (webDownView != null) {
        webDownView.release()
        this.imageButton = null
    }
    val myScrollNavi = this.leftScrollNavi
    if (myScrollNavi != null) {
        myScrollNavi.release()
        this.leftScrollNavi = null
    }
    val rightScrollNavi = this.floatingState.rightScrollNavi
    if (rightScrollNavi != null) {
        rightScrollNavi.release()
        this.floatingState.rightScrollNavi = null
    }
    val myGesNoti = this.floatingState.gestureHint
    if (myGesNoti != null) {
        myGesNoti.release()
        this.floatingState.gestureHint = null
    }
    this.mainMenuItems = null
    this.mainMenuItems2 = null
    this.barScroll = null
    this.pageLayout = null
    this.dialogsState.topBehavior = null
    this.behaviorBot = null
    this.barsState.touchFrame = null
    this.transLangCode = null
    this.transText = null
    this.pageLangCode = null
    this.dialogsState.pageUrl = null
    this.dialogsState.pageHost = null
    this.pageState.stateItem = null
    this.dialogsState.blobPageUrl = null
    this.dialogsState.instaAppId = null
    this.dialogsState.videoPageUrl = null
    this.videoState.videoCheckUrl = null
    this.miscState.rateScriptUrl = null
    this.dialogsState.adsDialogUrl = null
    this.dialogsState.printJob = null
    this.tabsState.gestureDetector = null
    this.tabsState.tabItems = null
    this.tabUsageOrder = null
    if (!this.pageState.finishImplBusy || AppState.exiting) {
        return
    }
    AppState.exiting = true
    try {
        System.exit(0)
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.lifecycleOnKeyDown(keyCode: Int, keyEvent: KeyEvent?, superOnKeyDown: (Int, KeyEvent?) -> kotlin.Boolean): kotlin.Boolean {
    if (keyCode == 24) {
        if (ZoneSettings.volScroll.value && !this.miscState.touchBusy && this.pageScroll(true, false)) {
            return true
        }
    } else if (keyCode != 25) {
        if (keyCode == 57) {
            this.runMenuItem(null, 26)
        } else {
            run label53@{
            if (keyCode != 58) {
                if (keyCode != 79) {
                    if (keyCode == 135) {
                        this.runMenuItem(null, 35)
                        return superOnKeyDown(keyCode, keyEvent)
                    }

                    if (keyCode != 85 && keyCode != 86) {
                        when (keyCode) {
                            125 -> return@label53
                            126, 127 -> {}
                            else -> return superOnKeyDown(keyCode, keyEvent)
                        }
                    }
                }

                val videoFrame = this.videoFrame
                if (videoFrame != null && videoFrame.onMediaKey(keyCode)) {
                    return true
                }

                return superOnKeyDown(keyCode, keyEvent)
            }
            }

            this.runMenuItem(null, 27)
        }
    } else if (ZoneSettings.volScroll.value && !this.miscState.touchBusy && this.pageScroll(false, false)) {
        return true
    }

    return superOnKeyDown(keyCode, keyEvent)
}

fun WebViewActivity.lifecycleOnNewIntent(intent: Intent?, superOnNewIntent: (Intent?) -> Unit) {
    val booleanExtra: kotlin.Boolean
    superOnNewIntent(intent)
    if (this.dialogsState.appContext == null) {
        this.dialogsState.appContext = getApplicationContext()
    }
    if (this.mainHandler == null) {
        this.mainHandler = Handler(Looper.getMainLooper())
    }
    this.tabsState.layoutRtl = isRtl(this.dialogsState.appContext)
    val data = intent!!.getData()
    val stringExtra = if (data != null) data.toString() else intent.getStringExtra("EXTRA_PATH")
    if (TextUtils.isEmpty(stringExtra)) {
        val myEditAuto: MyEditAuto? = if (TtsSettings.appKeypad.value) this.editAuto else null
        if (TtsSettings.appKeypad.value && myEditAuto != null) {
            myEditAuto.postDelayed(WvaStartAddressEdit(this), 200L)
        }
        this.pageState.startupBusy = false
        this.pageState.useAppKeypad = false
        return
    }
    MainApp.resumeWebTimers(this.dialogsState.appContext, this.currentPage)
    TabFrameLog.step(this, "newIntent", this.tabsState.currentFrame, "restored=${this.dialogsState.hasNewIntent} adding=${this.videoState.addTabImplBusy}")
    if (this.videoFrame != null) {
        exitVideoFrame()
    }
    stopWebClean()
    hidePageOverlays(true)
    closeAllDialogs()
    closePopupMenus()
    MainApp.applyCookieSetting(this.dialogsState.appContext)
    if (this.dialogsState.hasNewIntent) {
        startWebClean()
    } else {
        this.dialogsState.hasNewIntent = true
        setUpPageUi()
    }
    this.addTabPending = false
    val intentHost = MainUtil.hostOfUrl(stringExtra, true)
    if (TextUtils.isEmpty(intentHost) || !(intentHost!!.contains("google") || intentHost.contains("adclick") || intentHost.contains(
            "doubleclick"
        ))
    ) {
        booleanExtra = intent.getBooleanExtra("EXTRA_EXT", false)
        if (this.newIntentPending && booleanExtra) {
            this.addTabPending = true
        }
    } else {
        booleanExtra = false
    }
    this.launchedExternally = intent.getBooleanExtra("EXTRA_LAUNCH_LOCK", false)
    this.launchUrl = stringExtra
    if (!booleanExtra || BrowserSettings.popBlock.value == 0 || DataBookPop.instance(this.dialogsState.appContext).hasSiteOf(this.dialogsState.pageUrl, this.dialogsState.pageHost)) {
        this.addTabFromIntent = true
        addTabInGroup(null, stringExtra, false, null)
        this.addTabFromIntent = false
    } else if (!TextUtils.isEmpty(stringExtra)) {
        runInBackground(WvaOnNewIntentTask(this, intentUrl = stringExtra))
    }
    this.newIntentPending = false
    this.pageState.startupBusy = false
    this.pageState.useAppKeypad = false
}

fun WebViewActivity.lifecycleOnPause(superOnPause: () -> Unit) {
    this.browserTicker?.stop()
    // leaving the app: the page is fully drawn by now, a good moment for the tab overview snapshot
    // not while switching to another space: the picture would be stored under the new one
    if (!isFinishing() && !(this.tabsState.restartBusy || WorkspaceSwitch.isLeaving(this)) && this.tabsState.composeOverview == null) captureTabPreview(this.currentTabIndex)
    val webVideoFrame: WebVideoFrame?
    applyVideoOrientation(false)
    val runnable = this.quickState.newsRefreshTask
    val myWebBody: MyWebBody? = if (runnable != null) this.dialogsState.webBody else null
    if (runnable != null && myWebBody != null) {
        myWebBody.removeCallbacks(runnable)
    }
    this.isClosing = isFinishing()
    clearCache()
    val mainAppO = MainApp.from(this.dialogsState.appContext)
    if (mainAppO != null) {
        mainAppO.notificationCache = null
        mainAppO.clipboardCache = null
        mainAppO.inputMethodCache = null
        mainAppO.cookieCache = null
        mainAppO.mainHandler = null
    }
    superOnPause()
    var isGoogle = true
    this.isPaused = true
    this.dialogsState.hasNewIntent = false
    snapshotPrefs()
    val webNestView = this.currentPage
    if (webNestView != null) {
        val webTabItemD: WebTabItem? = if (!(this.tabsState.restartBusy || WorkspaceSwitch.isLeaving(this)) && !this.isExiting && !this.isClosing && !webNestView.blankPending) tabItemAt(this.currentTabIndex) else null
        if (!(this.tabsState.restartBusy || WorkspaceSwitch.isLeaving(this)) && !this.isExiting && !this.isClosing && !webNestView.blankPending && webTabItemD != null) {
            this.currentPage!!.saveTabState(this.tabsState.currentFrame, webTabItemD.uid, webTabItemD.tabUrl, null)
        }
        val app = MainApp.from(this.dialogsState.appContext)
        if (TextUtils.isEmpty(if (app == null) null else app.pendingVideoUrl) && ((this.videoFrame.also {
                webVideoFrame = it
            }) == null || !webVideoFrame!!.isInPip())) {
            if (ZoneSettings.backPlay.value) {
                isGoogle = isGoogleSearch(this.dialogsState.pageUrl)
            }
            if (isGoogle) {
                setBackgroundPlay(false)
            } else {
                runInBackground(WvaPauseBackPlay(this))
            }
        }
    }
    if (this.isClosing) {
        this.dialogsState.addPullViewBusy = false
        closeAllDialogs()
        closePopupMenus()
        val mainFilterSvc = this.miscState.filterService
        if (mainFilterSvc != null) {
            mainFilterSvc.release()
            this.miscState.filterService = null
        }
        val mainScriptSvc = this.miscState.scriptService
        if (mainScriptSvc != null) {
            mainScriptSvc.release()
            this.miscState.scriptService = null
        }
        shutdownTts()
    } else {
        if (this.coverView != null) {
            removeCoverView()
        }
        if (this.pageState.popupPage != null) {
            destroyPopupPage()
        }
        val quickSearch = this.quickSearch
        val myClipView: ClipRow? = if (quickSearch != null) quickSearch.clipView else null
        if (quickSearch != null && myClipView != null) {
            myClipView.hidePreview()
        }
        stopTts()
    }
    val isClosing = this.isClosing
    val dialogWebBookList = this.bookmarksDialog
    if (dialogWebBookList != null) {
        dialogWebBookList.closeSubs(isClosing)
        notifyBookUpdate(this.dialogsState.appContext, false)
    }
    val dialogListBook = this.historyDialog
    if (dialogListBook != null) {
        dialogListBook.onPrefChanged(isClosing)
    }
    val dialogSetTrans = this.setTransDialog
    val dialogWebView = this.webViewDialog
    if (dialogWebView != null) {
        dialogWebView.pausePage()
    }
    val dialogAllowPopup = this.allowPopupDialog
    val dialogBlockLink = this.blockLinkDialog
    val dialogBlockImage = this.dialogsState.blockImageDialog
    val dialogEditArea = this.editAreaDialog
    val previewDialog: DialogWebView? = if (dialogEditArea != null) dialogEditArea.previewDialog else null
    if (dialogEditArea != null && previewDialog != null) {
        previewDialog.pausePage()
    }
    val webGridDialog = this.dialogsState.gridDialog
    if (webGridDialog != null) {
        webGridDialog.closeChildren(isClosing)
    }
    val siteDlg = this.dialogsState.siteDialog
    if (siteDlg != null) {
        siteDlg.closeChildren(isClosing)
    }
    val dialogPreview = this.previewDialog
    if (dialogPreview != null) {
        dialogPreview.pauseMedia()
    }
    val dialogSetUseTts = this.dialogsState.setUseTtsDialog
    val dialogSetTts: DialogSetTts? = if (dialogSetUseTts != null) dialogSetUseTts.ttsDialog else null
    if (dialogSetUseTts != null && dialogSetTts != null) {
        dialogSetTts.stopSpeaking()
    }
    val dialogSetAdblock = this.setAdblockDialog
    val dialogSetJava = this.setJavaDialog
    val dialogSetTmem = this.setTmemDialog
    val dialogViewRead = this.dialogsState.viewReadDialog
    if (dialogViewRead != null) {
        val ttsDialog = dialogViewRead.ttsDialog
        if (ttsDialog != null) {
            ttsDialog.stopSpeaking()
        }
        dialogViewRead.finishActionMode()
    }
    val myWebCoord = this.webCoord
    if (myWebCoord != null) {
        myWebCoord.post(WvaPauseSecureFlag(this))
        if ((this.tabsState.restartBusy || WorkspaceSwitch.isLeaving(this))) {
            return
        }
        runInBackground(WvaPauseSave(this))
    }
}

fun WebViewActivity.lifecycleOnRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, superOnRequestPermissionsResult: (Int, Array<out String>, IntArray) -> Unit) {
    superOnRequestPermissionsResult(requestCode, permissions, grantResults)
    val dialogQuickEdit = this.dialogsState.quickEditDialog
    if (dialogQuickEdit != null && requestCode == 30) {
        if (grantResults.size <= 0 || grantResults[0] != 0) {
            return
        }
        dialogQuickEdit.cameraUri = newMediaUri(RequestCode.CAMERA_OR_FILE, dialogQuickEdit.hostActivity, false)
    }
    val dialogEditShort = this.editShortDialog
    if (dialogEditShort != null && requestCode == 30) {
        if (grantResults.size <= 0 || grantResults[0] != 0) {
            return
        }
        dialogEditShort.cameraUri = newMediaUri(RequestCode.CAMERA_OR_FILE, dialogEditShort.hostActivity, false)
        return
    }
    val webGridDialog = this.dialogsState.gridDialog
    val dialogCreateAlbum: DialogCreateAlbum? = if (webGridDialog == null) null else webGridDialog.albumDialog
    if (webGridDialog == null || dialogCreateAlbum == null || !dialogCreateAlbum.onPermissionResult(requestCode, grantResults)) {
        val siteDlg = this.dialogsState.siteDialog
        val siteAlbum: DialogCreateAlbum? = if (siteDlg == null) null else siteDlg.albumDialog
        if (siteDlg != null && siteAlbum != null && siteAlbum.onPermissionResult(requestCode, grantResults)) {
            return
        }
        run {
            run {
                if (requestCode == 28) {
                    if (grantResults.size <= 0 || grantResults[0] != 0) {
                        deliverFileChooserResult(null)
                        return
                    }
                    val uriZ = newMediaUri(RequestCode.CAMERA_OR_FILE, this, false)
                    this.cameraUri = uriZ
                    if (uriZ != null) {
                        this.dialogsState.leavingByIntent = true
                        return
                    } else {
                        deliverFileChooserResult(null)
                        return
                    }
                }
                if (requestCode == 29) {
                    if (grantResults.size <= 0 || grantResults[0] != 0) {
                        deliverFileChooserResult(null)
                        return
                    }
                    val uriZ = newMediaUri(RequestCode.CAMERA_OR_FILE, this, true)
                    this.cameraUri = uriZ
                    if (uriZ != null) {
                        this.dialogsState.leavingByIntent = true
                        return
                    } else {
                        deliverFileChooserResult(null)
                        return
                    }
                }
                when (requestCode) {
                    21 -> {
                        if (grantResults.size <= 1 || grantResults[0] != 0 || grantResults[1] != 0) {
                            showToast(this, AppR.string.permission_denied)
                        } else {
                            grantSitePermission(requestCode)
                            showToast(this, AppR.string.permission_granted)
                        }
                        clearPermissionRequest()
                    }

                    22, 23, 24 -> {
                        if (grantResults.size <= 0 || grantResults[0] != 0) {
                            showToast(this, AppR.string.permission_denied)
                        } else {
                            grantSitePermission(requestCode)
                            showToast(this, AppR.string.permission_granted)
                        }
                        clearPermissionRequest()
                    }
                }
            }
        }
    }
}

fun WebViewActivity.lifecycleOnResume(superOnResume: () -> Unit) {
    val payState: Byte
    this.newIntentPending = false
    this.browserTicker?.start()
    RendererRecovery.onResume(this)
    val launchedExternally = this.launchedExternally
    this.launchedExternally = false
    if (this.dialogsState.appContext == null) {
        this.dialogsState.appContext = getApplicationContext()
    }
    com.mycompany.app.glide.ImageCache.init(this.dialogsState.appContext!!)
    if (this.mainHandler == null) {
        this.mainHandler = Handler(Looper.getMainLooper())
    }
    this.tabsState.layoutRtl = isRtl(this.dialogsState.appContext)
    superOnResume()
    // inactive tabs to the archive (Settings > Tabs), a moment after the screen is back
    TabArchive.schedule(this)
    val createBusy = this.miscState.createBusy
    this.miscState.createBusy = false
    this.isPaused = false
    this.isClosing = false
    this.tabsState.restartBusy = false
    this.pageState.finishImplBusy = false
    this.dialogsState.adsDialogUrl = null
    val webVideoFrame = this.videoFrame
    if (webVideoFrame != null) {
        webVideoFrame.onPipEnded()
    }
    val webVideoPip = this.videoState.videoPip
    if (webVideoPip != null) {
        val eventReceiver = webVideoPip.eventReceiver
        if (eventReceiver != null) {
            val mainActivity = webVideoPip.hostActivity
            if (mainActivity != null) {
                mainActivity.unregisterReceiver(eventReceiver)
            }
            webVideoPip.eventReceiver = null
        }
        val viewGroup = webVideoPip.parentBody
        if (viewGroup != null) {
            viewGroup.removeView(webVideoPip)
            webVideoPip.parentBody = null
        }
        val myCoverView = webVideoPip.loadCover
        if (myCoverView != null) {
            myCoverView.release()
            webVideoPip.loadCover = null
        }
        val myWebSafe = webVideoPip.webSafe
        if (myWebSafe != null) {
            if (webVideoPip.pageLoading) {
                webVideoPip.pageLoading = false
                myWebSafe.stopLoading()
            }
            destroyWebViewNow(webVideoPip.webSafe)
            webVideoPip.webSafe = null
        }
        webVideoPip.hostActivity = null
        webVideoPip.appContext = null
        webVideoPip.pageUrl = null
        webVideoPip.videoId = null
        this.videoState.videoPip = null
    }
    if (this.videoState.backPlayOn || TtsSettings.checkBack.value) {
        clearBackPlay(true)
    }
    this.dialogsState.leavingByIntent = false
    if (this.dialogsState.gridDialog == null && this.dialogsState.siteDialog == null) {
        clearDataCaches()
    }
    if (createBusy) {
        updateWindowBgColor(pageThemeColor(), pageThemeLight())
    } else {
        if (this.dialogsState.hasNewIntent) {
            startWebClean()
        } else {
            this.dialogsState.hasNewIntent = true
            setUpPageUi()
        }
        resetBusyFlags()
        val webNestView = this.currentPage
        if (webNestView != null) {
            webNestView.onResume()
        }
        val printJob = this.dialogsState.printJob
        if (printJob != null) {
            if (this.dialogsState.webBody == null) {
                this.dialogsState.printJob = null
            } else {
                try {
                    if (printJob.isCompleted()) {
                        showToast(this, AppR.string.print_success)
                    } else if (this.dialogsState.printJob!!.isFailed()) {
                        showToast(this, AppR.string.print_fail)
                    } else if (!this.dialogsState.printJob!!.isCancelled()) {
                        this.dialogsState.printJob!!.cancel()
                    }
                } catch (unused: Exception) {
                }
                this.dialogsState.printJob = null
            }
        }
        val quickSearch = this.quickSearch
        if (quickSearch != null) {
            quickSearch.loadItems()
        }
        val dialogWebBookList = this.bookmarksDialog
        if (dialogWebBookList != null) {
            dialogWebBookList.refresh(true)
        }
        val dialogListBook = this.historyDialog
        if (dialogListBook != null) {
            dialogListBook.refresh(true)
        }
        val dialogSetTrans = this.setTransDialog
        if (dialogSetTrans != null) {
            dialogSetTrans.refreshState(true)
        }
        val dialogWebView = this.webViewDialog
        if (dialogWebView != null) {
            dialogWebView.resumePage()
        }
        val dialogAllowPopup = this.allowPopupDialog
        if (dialogAllowPopup != null) {
            dialogAllowPopup.refreshState(true)
        }
        val dialogBlockLink = this.blockLinkDialog
        if (dialogBlockLink != null) {
            dialogBlockLink.refreshBlock(true)
        }
        val dialogBlockImage = this.dialogsState.blockImageDialog
        if (dialogBlockImage != null) {
            dialogBlockImage.applyBlocked(true)
        }
        val dialogEditArea = this.editAreaDialog
        val previewDialog: DialogWebView? = if (dialogEditArea != null) dialogEditArea.previewDialog else null
        if (dialogEditArea != null && previewDialog != null) {
            previewDialog.resumePage()
        }
        val webGridDialog = this.dialogsState.gridDialog
        if (webGridDialog != null) {
            webGridDialog.onShowAgain()
        }
        val siteDlg = this.dialogsState.siteDialog
        if (siteDlg != null) {
            siteDlg.onShowAgain()
        }
        val dialogPreview = this.previewDialog
        if (dialogPreview != null) {
            dialogPreview.resumeMedia()
        }
        val dialogSetAdblock = this.setAdblockDialog
        if (dialogSetAdblock != null) {
            dialogSetAdblock.refreshState(true)
        }
        val dialogSetJava = this.setJavaDialog
        if (dialogSetJava != null) {
            dialogSetJava.refreshState(true)
        }
        val dialogSetTmem = this.setTmemDialog
        if (dialogSetTmem != null) {
            dialogSetTmem.refreshState(true)
        }
        val dialogViewRead = this.dialogsState.viewReadDialog
        if (dialogViewRead != null) {
            dialogViewRead.applyTextStyle()
        }
        val videoFrame = this.videoFrame
        if (videoFrame != null) {
            videoFrame.stopPipAuto()
        } else {
            val appBarLayout = this.barScroll
            if (appBarLayout != null) {
                appBarLayout.setExpanded(true)
            }
        }
        val context = this.dialogsState.appContext
        if (PdfSettings.payConfirm.value) {
            payState = 2
        } else {
            val mainAppO = MainApp.from(context)
            payState = if (mainAppO == null) 0.toByte() else mainAppO.payState
        }
        val handler = this.mainHandler
        if (handler != null) {
        }
    }
    if (createBusy) {
        com.mycompany.app.ui.viewer.ImagePreviewActivity.backgroundChanged = false
        applySettingsChange(true)
        if (AppState.startupPending || BrowserSettings.checkDown.value) {
            runInBackground(WvaResumeInit(this))
        }
    } else {
        runInBackground(WvaResumeSettingsTask(this))
    }
    applyVideoOrientation(true)
    if (launchedExternally && isLockEnabled()) {
        val intentH = lockIntent(this.dialogsState.appContext, SecretSettings.lockType.value)
        intentH.putExtra("EXTRA_TYPE", 0)
        if (TextUtils.isEmpty(this.launchUrl)) {
            intentH.putExtra("EXTRA_PATH", this.dialogsState.pageUrl)
        } else {
            intentH.putExtra("EXTRA_PATH", this.launchUrl)
        }
        launchExternalForResult(intentH, RequestCode.UNLOCK_LAUNCH)
    } else {
        this.launchUrl = null
    }
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.post(WvaOnResumeTask(this))
}

fun WebViewActivity.lifecycleOnWindowFocusChanged(hasWindowFocusNow: Boolean, superOnWindowFocusChanged: (Boolean) -> Unit) {
    this.hasWindowFocusNow = hasWindowFocusNow
    if (hasWindowFocusNow) {
        this.focusPending = false
        this.menuJustClosed = false
        this.quickMenuJustClosed = false
        val webVideoFrame = this.videoFrame
        if (webVideoFrame != null) {
            webVideoFrame.cancelHides()
        }
    } else {
        val handler = this.mainHandler
        if (handler != null) {
            this.focusPending = true
            handler.postDelayed(WvaFocusResumeDownloads(this), 800L)
        }
    }
    superOnWindowFocusChanged(hasWindowFocusNow)
    if (!hasWindowFocusNow) {
        if (this.floatingState.gestureMode.toInt() == 0 && this.currentPage != null) {
            resetGestureState(true, false)
            val myScrollNavi = this.leftScrollNavi
            if (myScrollNavi != null) {
                myScrollNavi.setVisibility(View.INVISIBLE)
            }
            val rightScrollNavi = this.floatingState.rightScrollNavi
            if (rightScrollNavi != null) {
                rightScrollNavi.setVisibility(View.INVISIBLE)
            }
        }
        val myPullView = this.pullView
        if (myPullView != null) {
            myPullView.resetSpinner()
        }
    }
}

fun WebViewActivity.lifecycleStartActivity(intent: Intent?) {
    if (intent != null) {
        val categories: MutableSet<String?>? = if (!this.dialogsState.miscBusy) intent.getCategories() else null
        if (!this.dialogsState.miscBusy && categories != null) {
            val it = categories.iterator()
            while (it.hasNext()) {
                if ("android.intent.category.BROWSABLE" == it.next()) {
                    if (this.webCoord != null) {
                        val data = intent.getData()
                        var isGoogleNews = false
                        if (data != null) {
                            val url = data.toString()
                            if (!TextUtils.isEmpty(url)) {
                                isGoogleNews = url.startsWith("https://news.google.com")
                            }
                        }
                        if (isGoogleNews) {
                            return
                        }
                        this.webCoord!!.post(WvaShowIntentSnackbar(this, pendingIntent = intent))
                        return
                    }
                    return
                }
            }
        }
        startExternalActivity(intent)
    }
}

fun WebViewActivity.finishImpl(superFinish: () -> Unit) {
    var found = false
    var cursorG: Cursor? = null
    var time: Long = 0
    if (SyncSettings.secretMode.value) {
        clearAppData(this.dialogsState.appContext)
    }
    PrefSync.saveIndexes(this.dialogsState.appContext)
    flushCookies(this.dialogsState.appContext, false)
    val webClean = this.miscState.webClean
    if (webClean != null) {
        webClean.userBlocks = null
        webClean.userBlocksCopy = null
        webClean.started = false
        webClean.listener = null
        this.miscState.webClean = null
    }
    if ((BrowserSettings.exitDelete.value and 2) == 2) {
        DbBookRecent.clearAll(this.dialogsState.appContext)
    } else if (ZtwoSettings.recentLimit.value > 0) {
        trimOld(this.dialogsState.appContext, false)
        if (ZtwoSettings.recentSecret.value) {
            trimOld(this.dialogsState.appContext, true)
        }
    }
    val deleteBit = BrowserSettings.exitDelete.value and 4
    val context = this.dialogsState.appContext
    if (deleteBit == 4) {
        DbBookHistory.clearAll(context)
        found = true
    } else {
        DbBookHistory.clearOld(context)
        found = false
    }
    if ((BrowserSettings.exitDelete.value and 8) == 8) {
        ensureWebViewDir(this.dialogsState.appContext)
    }
    if ((BrowserSettings.exitDelete.value and 16) == 16) {
        val appContext = this.dialogsState.appContext
        com.mycompany.app.glide.ImageCache.clearMemory()
        // Glide's disk cache may only be cleared off the main thread; finish() runs on it, so the old inline
        // assertBackgroundThread made this silently skip and the image cache survived "clear cache on exit"
        val glideContext = appContext!!.applicationContext
        Thread {
            try {
                Glide.get(glideContext).clearDiskCache()
            } catch (_: Exception) {
            }
        }.start()
        clearCacheDirs(this.dialogsState.appContext)
    }
    val exitDelete = BrowserSettings.exitDelete.value
    val deleteCookies = (exitDelete and 32) == 32
    val deleteCache = (exitDelete and 64) == 64
    val prefSyncR = PrefSync.get(this.dialogsState.appContext, false)
    if (deleteCookies) {
        val appContext = this.dialogsState.appContext
        if (deleteCache) {
            DbBookTab.clearAll(appContext)
            SyncSettings.editLocal { set(SyncSettings.normalIndex, 0) }
            PrefSync.currentSecretIndex = 0
            PrefSync.normalIndex = 0
            PrefSync.savedIndex = 0
            prefSyncR!!.removeKey("mNormalIndex")
            prefSyncR.removeKey("mSecretIndex")
        } else {
            DbBookTab.clearSecret(appContext, false)
            SyncSettings.editLocal { set(SyncSettings.normalIndex, 0) }
            PrefSync.normalIndex = 0
            prefSyncR!!.removeKey("mNormalIndex")
        }
    } else if (deleteCache) {
        DbBookTab.clearSecret(this.dialogsState.appContext, true)
        PrefSync.currentSecretIndex = 0
        PrefSync.savedIndex = 0
        prefSyncR!!.removeKey("mSecretIndex")
    }
    prefSyncR!!.save()
    if (found && deleteCookies && deleteCache) {
        val appContext = this.dialogsState.appContext
        if (appContext == null) {
            val dbBookIcon = DbBookIcon.cached
        } else {
            deleteWhere(DbBookIcon.get(appContext).getWritableDatabase(), "DbBookIcon_table", null, null)
        }
    } else {
        val appContext = this.dialogsState.appContext
        val dbBookIcon = DbBookIcon.cached
        if (appContext != null) {
            try {
                cursorG = DbUtil.query(
                    DbBookIcon.get(appContext).getWritableDatabase(),
                    "DbBookIcon_table",
                    arrayOf<String?>("_time"),
                    null,
                    null,
                    "_time DESC"
                )
                if (cursorG != null) {
                    try {
                        if (cursorG.getCount() <= 500 || !cursorG.moveToPosition(499)) {
                            time = 0
                        } else {
                            time = cursorG.getLong(cursorG.getColumnIndex("_time"))
                        }
                    } catch (unused4: Exception) {
                    }
                } else {
                    time = 0
                }
            } catch (unused5: Exception) {
                cursorG = null
            }
            if (cursorG != null) {
                cursorG.close()
            }
            if (time > 0) {
                DbUtil.deleteWhere(
                    DbBookIcon.get(appContext).getWritableDatabase(),
                    "DbBookIcon_table",
                    "_time<?",
                    arrayOf<String?>(time.toString())
                )
            }
        }
    }
    if (this.dialogsState.appContext != null) {
        try {
            deleteRecursive(File(this.dialogsState.appContext!!.getFilesDir(), ".page"))
        } catch (unused6: Exception) {
        }
    }
    MainApp.lastToast = null
    val mainAppO = MainApp.from(getApplicationContext())
    if (mainAppO == null || !mainAppO.downBinding) {
        this.pageState.finishImplBusy = true
    }
    superFinish()
}
