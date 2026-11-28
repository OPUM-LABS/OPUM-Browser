@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import com.mycompany.app.web.WebEmgLoad.CreateWebTask
import com.mycompany.app.main.RequestCode
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.FloatSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.VideoSettings
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.indexOfCharIn
import com.mycompany.app.main.MainUtil.nativeJsN
import com.mycompany.app.main.MainUtil.githubRawToRepo
import com.mycompany.app.main.MainUtil.wildcardHost
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.indexOfWord
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.matchesNativeHost
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.isYouTubeHost
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.isEmgUrl
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.isHmgPath
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.isHmgUrl
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.snsJs2
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.snsUrl
import com.mycompany.app.main.MainUtil.snsJs1
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.openWithCookies
import com.mycompany.app.main.MainUtil.restartLater
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.searchUrlFor
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.main.MainUtil.browserIntent
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.stripHitomiTitle
import com.mycompany.app.main.MainUtil.parseFontList
import com.mycompany.app.main.MainUtil.extractUrl
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.pinterestOriginal
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.comicSiteKind
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.setUserAgent
import com.mycompany.app.main.MainUtil.chromeVersion
import com.mycompany.app.main.MainUtil.evalPageJs
import com.mycompany.app.main.MainUtil.isGoogleSearch
import com.mycompany.app.main.MainUtil.lacksAlgorithmicDark
import com.mycompany.app.main.MainUtil.setAlgorithmicDark
import com.mycompany.app.main.MainUtil.needsDesktopMode
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.isYouTubeUrl
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.appendAmpJs
import com.mycompany.app.main.MainUtil.nativeJs5b
import com.mycompany.app.main.MainUtil.nativeJs5c
import com.mycompany.app.main.MainUtil.viewportJs
import com.mycompany.app.main.MainUtil.isImagePath
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.main.MainUtil.injectOverlayJs
import com.mycompany.app.main.MainUtil.isHitomi
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.backupBitmapCopy
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.main.MainUtil.initUserAgent
import com.mycompany.app.main.MainUtil.defaultUserAgent
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.main.MainUtil.offlineUrlFor
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.main.MainUtil.shortHostOf
import com.mycompany.app.main.MainUtil.cachedIcon
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isSecretLocked
import com.mycompany.app.main.MainUtil.offlinePathFor
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.main.MainUtil.youTubeVideoId
import com.mycompany.app.main.MainUtil.youTubeIdOf
import com.mycompany.app.main.MainUtil.isFullScreenOff
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.restartTask
import com.mycompany.app.main.MainUtil.clearAppData
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.hasVideoButton
import com.mycompany.app.main.MainUtil.pipAspectRatio
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.animateIcon
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.showToastText
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.imageViewerDualPage
import com.mycompany.app.main.MainUtil.restartActivity
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.main.MainUtil.lockIntent
import com.mycompany.app.main.MainUtil.applyWindowBrightness
import com.mycompany.app.main.MainUtil.injectWindowOpenJs
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.applyVideoRotation
import com.mycompany.app.main.MainUtil.applySystemUiFlags
import com.mycompany.app.main.MainUtil.applyDisplayCutout
import com.mycompany.app.main.MainUtil.applyScreenRotation
import com.mycompany.app.main.MainUtil.openPermissionScreen
import com.mycompany.app.main.MainUtil.parseFloatSafe
import com.mycompany.app.main.MainUtil.parseFloatOr
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.insetsController
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.isYouTubeShorts
import com.mycompany.app.main.MainUtil.initMaxTextureSize
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.flushCookies
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.youTubeAutoplayJs
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openKeyboard
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.main.MainUtil.pageTitleForFile
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.setIcon
import com.mycompany.app.main.MainUtil.applyBarAppearance
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.openIntentUri
import com.mycompany.app.main.MainUtil.handleDownloadLink
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.main.MainUtil.isHitomiReader
import com.mycompany.app.main.MainUtil.isPdfUrl
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.main.MainUtil.openInExternalApp
import com.mycompany.app.main.MainUtil.clipboardText
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.notifyBookUpdate
import com.mycompany.app.main.MainUtil.pageUrlOrNull
import com.mycompany.app.main.MainUtil.isGoogleUserContent
import com.mycompany.app.main.MainUtil.openVideoWithApp
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.nextPageUrl
import com.mycompany.app.main.MainUtil.phTitle
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.openWithChooser
import com.mycompany.app.main.MainUtil.quickViewPdf
import com.mycompany.app.main.MainUtil.glideUrlFor
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.shareText
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.openCustomTab
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.hideDivsByClass
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.main.MainUtil.areaUpScript
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.main.MainUtil.hasClipboard
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.requestCamera
import com.mycompany.app.main.MainUtil.newMediaUri
import com.mycompany.app.main.MainUtil.getContent
import com.mycompany.app.main.MainUtil.pickImages
import com.mycompany.app.main.MainUtil.pickContent
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.applyNightMode
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.main.MainUtil.isDarkResources
import com.mycompany.app.main.MainUtil.openPlayStore
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
import com.mycompany.app.main.MainUtil.lockAppData
import com.mycompany.app.main.MainUtil.findMediaJs
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
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.main.MainUtil.addShortcut
import com.mycompany.app.main.MainUtil.lighterColor
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.hasFragmentPage
import com.mycompany.app.main.MainUtil.isFakeStream
import com.mycompany.app.main.MainUtil.themeIconColor
import com.mycompany.app.main.MainUtil.hasTopTitle
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.main.MainUtil.hasTopTabBar
import com.mycompany.app.main.MainUtil.injectVideoObserver
import com.mycompany.app.main.MainUtil.needsDesktopUa
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.hasBottomTitle
import com.mycompany.app.main.MainUtil.windowStatusColor
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUtil.setWindowStatusColor
import com.mycompany.app.main.MainUtil.setNavBarColor
import com.mycompany.app.main.MainUtil.isXLoginPage
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.db.book.DbBookPass.Companion.itemsForHost
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.main.MainUtil.viewScreenX
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.clearHistoryData
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.getFaceJs
import com.mycompany.app.main.MainUtil.getInstaJs
import com.mycompany.app.main.MainUtil.getTwitJs
import com.mycompany.app.main.MainUtil.getRedditJs
import com.mycompany.app.main.MainUtil.getTiktokJs
import com.mycompany.app.main.MainUtil.isGoogleHome
import com.mycompany.app.main.MainUtil.getFansJs
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.isUserScriptUrl
import com.mycompany.app.main.MainUtil.downMenuCount
import com.mycompany.app.main.MainUtil.isDarkTheme
import com.mycompany.app.main.MainUtil.isChinaLocale
import com.mycompany.app.main.MainUtil.applyScreenOrientation
import com.mycompany.app.main.MainUtil.applyImageOrientation
import com.mycompany.app.main.MainUtil.applyIncognitoIme
import com.mycompany.app.main.MainUtil.lofiUrl
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
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.main.MainUtil.highlightText
import com.mycompany.app.main.MainUtil.screenBrightnessPercent
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import android.view.ViewGroup
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebEmgLoad
import org.jsoup.Jsoup
import com.mycompany.app.web.WebHmgLoad
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import com.mycompany.app.web.WebViewActivity
import android.content.Intent
import com.mycompany.app.web.WebLoadTask
import com.mycompany.app.web.WebLoadView
import android.animation.ValueAnimator
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.quick.QuickView
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.db.DbUtil
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebViewActivity.FaceItem
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefMain
import android.os.Build
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.web.WebReadTask
import android.app.Activity
import com.mycompany.app.pref.PrefVideo
import com.mycompany.app.view.MyProgressBar
import com.mycompany.app.view.MyScrollBar
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.web.WebGridDialog
import com.mycompany.app.web.WebNestFrame.PageItem
import com.mycompany.app.web.WebVideoFull
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.ui.browser.ComposeTabBar
import android.view.ViewGroup.MarginLayoutParams
import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.web.WebViewActivity.TabLimitListener
import com.mycompany.app.pull.MyPullView
import com.mycompany.app.web.WebViewActivity.PrevPageListener
import com.mycompany.app.dialog.DialogLoadImg
import com.mycompany.app.dialog.DialogLoadEmg
import com.mycompany.app.dialog.DialogLoadHmg
import com.mycompany.app.dialog.DialogPreview
import com.mycompany.app.dialog.DialogPreImage
import com.mycompany.app.dialog.DialogWebView
import com.mycompany.app.dialog.DialogSetRate
import com.mycompany.app.dialog.DialogAllowPopup
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.dialog.DialogBlockLink
import com.mycompany.app.dialog.DialogBlockImage
import com.mycompany.app.view.MyBarFrame
import com.mycompany.app.dialog.DialogUrlLink
import com.mycompany.app.view.MyBarView
import com.mycompany.app.dialog.DialogWebCerti
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.dialog.DialogConfirm.DialogConfBtnListener
import android.content.ActivityNotFoundException
import com.mycompany.app.db.book.DbBookDown
import android.graphics.drawable.BitmapDrawable
import com.mycompany.app.data.book.DataBookTrans
import android.graphics.drawable.ColorDrawable
import com.mycompany.app.main.MainNative
import com.mycompany.app.pref.PrefFloat
import androidx.webkit.internal.WebViewFeatureInternal
import org.chromium.support_lib_boundary.util.BoundaryInterfaceReflectionUtil
import org.chromium.support_lib_boundary.WebStorageBoundaryInterface
import androidx.webkit.internal.WebViewGlueCommunicator
import com.mycompany.app.web.WebViewActivity.SortFace
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import android.view.Window
import android.webkit.URLUtil
import com.google.gson.JsonParser
import ch.opumlabs.opumbrowser.R
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLDecoder
import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.Executors
import kotlin.math.max
import com.mycompany.app.web.WebViewActivity.Companion.Zn
import com.mycompany.app.web.WebViewActivity.Companion.pauseMedia
import com.mycompany.app.web.WebViewActivity.Companion.pauseWebView
import com.mycompany.app.web.WebViewActivity.Companion.applyDarkSettings
import com.mycompany.app.web.WebViewActivity.Companion.checkedPageUrl
import com.mycompany.app.web.WebViewActivity.Companion.newTabUrl
import com.mycompany.app.web.WebViewActivity.Companion.isVkVideoPage
import com.mycompany.app.web.WebViewActivity.Companion.hasCustomZones

/*
 * The browser activity's dialogs functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.loadImageForOpen(imageOpenUrl: String?, uriString: String?, isOpen: Boolean) {
    if (this.dialogsState.appContext == null) {
        return
    }
    val dialogDownUrl = this.videoState.downUrlDialog
    if (dialogDownUrl == null) {
        showToast(this, AppR.string.down_start)
    } else if (dialogDownUrl.rootNode != null && !dialogDownUrl.imageOpened) {
        dialogDownUrl.imageOpened = true
        val height = dialogDownUrl.formNode!!.getHeight() + dialogDownUrl.headerNode!!.getHeight()
        dialogDownUrl.lineFrame!!.setLineDn(true)
        dialogDownUrl.bodyNode!!.setVisibility(4)
        dialogDownUrl.formNode!!.setVisibility(4)
        dialogDownUrl.rootNode!!.setBusy(height, 0, true, false)
    }
    this.pageState.imageOpenUrl = imageOpenUrl
    this.pageState.imageOpenReferer = uriString
    runInBackground(WvaLoadImageForOpenTask(this, isOpen))
}

fun WebViewActivity.showLoadHmgDialog(url: String?, forceLoad: kotlin.Boolean) {
    val loadUrl: String?
    val found: kotlin.Boolean
    var length: Int = 0
    var cutIndex: Int = 0
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeLoadHmgDialog()
    if (TextUtils.isEmpty(url) || "file:///android_asset/shortcut.html" == url || "about:blank" == url) {
        showToast(this, AppR.string.not_supported_page)
        return
    }
    if (this.dialogsState.loadDialogKind.toInt() == 0) {
        this.dialogsState.loadDialogKind = 5.toByte()
        if (hasFragmentPage(url)) {
            loadUrl = if (url == null || (url.length.also {
                    length = it
                }) == 0 || (url.lastIndexOf(45.toChar())
                    .also { cutIndex = it }) <= 0 || cutIndex >= length
            ) null else url.substring(0, cutIndex)
            found = true
        } else {
            loadUrl = url
            found = false
        }
        if (((forceLoad == true) || found) && this.miscState.hmgLoad == null) {
            val myWebCoord = this.webCoord
            val webHmgLoad = WebHmgLoad()
            if (myWebCoord != null) {
                webHmgLoad.appContext = getApplicationContext()
                webHmgLoad.parentView = myWebCoord
                webHmgLoad.pageUrl = loadUrl
                webHmgLoad.imagePos = hmgPageNumber(loadUrl)
                val webNestView = WebNestView(this)
                webHmgLoad.webNest = webNestView
                webNestView.setNoAutofill(true)
                webHmgLoad.webNest!!.setVisibility(View.INVISIBLE)
                webHmgLoad.parentView!!.addView(webHmgLoad.webNest, 0, ViewGroup.LayoutParams(-1, -1))
                webHmgLoad.parentView!!.post(webHmgLoad.CreateWebTask())
            }
            this.miscState.hmgLoad = webHmgLoad
        }
        val webHmgLoad = this.miscState.hmgLoad
        val webNestView = if (webHmgLoad != null) webHmgLoad.webNest else this.currentPage
        if (webNestView == null) {
            return
        }
        val dialogLoadHmg =
            DialogLoadHmg(this, webNestView, loadUrl, forceLoad, WvaLoadHmgListener(this))
        this.loadHmgDialog = dialogLoadHmg
        dialogLoadHmg.setOnDismissListener(WvaHmgLoadDismissed(this))
    }
}

fun WebViewActivity.addHandModeBar() {
    val myWebBody: MyWebBody? = if (!TtsSettings.handMode.value) null else this.dialogsState.webBody
    if (!TtsSettings.handMode.value || myWebBody == null) {
        return
    }
    if (!TtsSettings.handNoti.value) {
        val onStartPage = this.onStartPage
        val themeColor = pageThemeColor()
        if (myWebBody.handNotiIcon == null) {
            myWebBody.updateHandIcon(this, onStartPage, themeColor)
            return
        }
        return
    }
    if (this.dialogsState.handModeBar != null) {
        return
    }
    // Compose since 2026-11-17 (ui/browser/BrowserGuides.kt)
    this.dialogsState.handModeBar = com.mycompany.app.ui.browser.HandModeBar(this)
    val webBody = this.dialogsState.webBody
    val handTop = if (webBody == null) 0 else webBody.handTopPos(this)
    Zn = handTop
    if (MainConst.isApi30) {
        val webBodyNow = this.dialogsState.webBody
        pageThemeColor()
        webBodyNow!!.insetTop = handTop
        webBodyNow.topColor = webBodyNow.topBarColor
    }
    tintHandModeBar(pageThemeColor())
    this.dialogsState.webBody!!.addView(this.dialogsState.handModeBar, 0, ViewGroup.LayoutParams(-1, handTop))
}

fun WebViewActivity.closeAllDialogs() {
    val dialogSetMsg = this.clearRecentDialog
    if (dialogSetMsg != null) {
        dialogSetMsg.dismiss()
        this.clearRecentDialog = null
    }
    closeQuickDeleteDialog()
    closeQuickEditDialog()
    closeQuickSubView()
    closeDeleteItemDialog()
    closeTabGroupDeleteDialog()
    val dialogTabEdit = this.dialogsState.tabEditDialog
    if (dialogTabEdit != null) {
        dialogTabEdit.dismiss()
        this.dialogsState.tabEditDialog = null
    }
    closeVideoMenuDialog()
    val dialogSetRate = this.dialogsState.setRateDialog
    if (dialogSetRate != null) {
        dialogSetRate.dismiss()
        this.dialogsState.setRateDialog = null
    }
    closeTabMenuDialog()
    closeNewsMenuDialog()
    val dialogNewsLocale = this.newsLocaleDialog
    if (dialogNewsLocale != null) {
        dialogNewsLocale.dismiss()
        this.newsLocaleDialog = null
    }
    val dialogNewsSearch = this.newsSearchDialog
    if (dialogNewsSearch != null) {
        dialogNewsSearch.dismiss()
        this.newsSearchDialog = null
    }
    val dialogWebBookEdit = this.dialogsState.bookEditDialog
    if (dialogWebBookEdit != null) {
        dialogWebBookEdit.dismiss()
        this.dialogsState.bookEditDialog = null
    }
    closeBookmarksDialog()
    closeHistoryDialog()
    val dialogEditShort = this.editShortDialog
    if (dialogEditShort != null) {
        dialogEditShort.dismiss()
        this.editShortDialog = null
    }
    closeUrlLinkDialog()
    closeDownGuideDialog()
    closeDownUrlDialog()
    closeSetDownDialog()
    closeDownBlobDialog()
    closeCaptureModeSheet()
    resumePausedPage()
    val dialogCapture = this.captureDialog
    if (dialogCapture != null) {
        dialogCapture.dismiss()
        this.captureDialog = null
    }
    val dialogDownPage = this.downPageDialog
    if (dialogDownPage != null) {
        dialogDownPage.dismiss()
        this.downPageDialog = null
    }
    val dialogPrintPage = this.printPageDialog
    if (dialogPrintPage != null) {
        dialogPrintPage.dismiss()
        this.printPageDialog = null
    }
    closeSetTransDialog()
    val dialogTransLang = this.transLangDialog
    if (dialogTransLang != null) {
        dialogTransLang.dismiss()
        this.transLangDialog = null
    }
    closeTransMsgDialog()
    val dialogViewTrans = this.viewTransDialog
    if (dialogViewTrans != null) {
        dialogViewTrans.dismiss()
        this.viewTransDialog = null
    }
    val dialogSetDesk = this.dialogsState.setDeskDialog
    if (dialogSetDesk != null) {
        dialogSetDesk.dismiss()
        this.dialogsState.setDeskDialog = null
    }
    closeSetUrlDialog()
    val dialogOpenType = this.openTypeDialog
    if (dialogOpenType != null) {
        dialogOpenType.dismiss()
        this.openTypeDialog = null
    }
    closePopupMenuDialog()
    closeWebViewDialog()
    val dialogAllowPopup = this.allowPopupDialog
    if (dialogAllowPopup != null) {
        dialogAllowPopup.dismiss()
        this.allowPopupDialog = null
    }
    val dialogBlockLink = this.blockLinkDialog
    if (dialogBlockLink != null) {
        dialogBlockLink.dismiss()
        this.blockLinkDialog = null
    }
    val dialogBlockImage = this.dialogsState.blockImageDialog
    if (dialogBlockImage != null) {
        dialogBlockImage.dismiss()
        this.dialogsState.blockImageDialog = null
    }
    val dialogGuideArea = this.dialogsState.guideAreaDialog
    if (dialogGuideArea != null) {
        dialogGuideArea.dismiss()
        this.dialogsState.guideAreaDialog = null
    }
    val guideAreaDialog = this.guideAreaDialog
    if (guideAreaDialog != null) {
        guideAreaDialog.dismiss()
        this.guideAreaDialog = null
    }
    val dialogEditArea = this.editAreaDialog
    if (dialogEditArea != null) {
        dialogEditArea.dismiss()
        this.editAreaDialog = null
    }
    closeJsMessageDialog()
    val dialogPrompt = this.promptDialog
    if (dialogPrompt != null) {
        dialogPrompt.dismiss()
        this.promptDialog = null
    }
    val dialogEditAuth = this.editAuthDialog
    if (dialogEditAuth != null) {
        dialogEditAuth.dismiss()
        this.editAuthDialog = null
    }
    closePermissionDialog()
    val dialogPassInfo = this.passInfoDialog
    if (dialogPassInfo != null) {
        dialogPassInfo.dismiss()
        this.passInfoDialog = null
    }
    val dialogWebSelect = this.webSelectDialog
    if (dialogWebSelect != null) {
        dialogWebSelect.dismiss()
        this.webSelectDialog = null
    }
    closeMenuDialogs()
    closeTabOverview()
    closeMiniTabOverview()
    closeLoadImgDialog()
    closeLoadEmgDialog()
    closeLoadHmgDialog()
    resumePausedPage()
    val webGridDialog = this.dialogsState.gridDialog
    if (webGridDialog != null) {
        webGridDialog.dismiss()
        this.dialogsState.gridDialog = null
    }
    resumePausedPage()
    val siteDlg = this.dialogsState.siteDialog
    if (siteDlg != null) {
        siteDlg.dismiss()
        this.dialogsState.siteDialog = null
    }
    resumePausedPage()
    resumePausedPage()
    closeVideoListDialog()
    closeDownFontDialog()
    closePreviewDialog()
    closePreImageDialog()
    val dialogSeekBright = this.seekBrightDialog
    if (dialogSeekBright != null) {
        dialogSeekBright.dismiss()
        this.seekBrightDialog = null
    }
    val dialogSeekWebText = this.seekWebTextDialog
    if (dialogSeekWebText != null) {
        dialogSeekWebText.dismiss()
        this.seekWebTextDialog = null
    }
    val dialogSetFull = this.setFullDialog
    if (dialogSetFull != null) {
        dialogSetFull.dismiss()
        this.setFullDialog = null
    }
    val dialogSetDark = this.dialogsState.setDarkDialog
    if (dialogSetDark != null) {
        dialogSetDark.dismiss()
        this.dialogsState.setDarkDialog = null
    }
    closeDarkConfirmDialog()
    val dialogSetUseTts = this.dialogsState.setUseTtsDialog
    if (dialogSetUseTts != null) {
        dialogSetUseTts.dismiss()
        this.dialogsState.setUseTtsDialog = null
    }
    val dialogSetAdblock = this.setAdblockDialog
    if (dialogSetAdblock != null) {
        dialogSetAdblock.dismiss()
        this.setAdblockDialog = null
    }
    val dialogSetJava = this.setJavaDialog
    if (dialogSetJava != null) {
        dialogSetJava.dismiss()
        this.setJavaDialog = null
    }
    val dialogSetTmem = this.setTmemDialog
    if (dialogSetTmem != null) {
        dialogSetTmem.dismiss()
        this.setTmemDialog = null
    }
    if (isReaderOpen()) {
        val dialogViewRead = this.dialogsState.viewReadDialog
        this.dialogsState.viewReadDialog = null
        if (dialogViewRead != null && !this.pageState.readDialogOpen) {
            this.pageState.readDialogOpen = dialogViewRead.handleBack()
        }
        this.quickState.quickAddBusy = false
    }
    resumePausedPage()
    val dialogViewSrc = this.dialogsState.viewSrcDialog
    if (dialogViewSrc != null) {
        dialogViewSrc.dismiss()
        this.dialogsState.viewSrcDialog = null
    }
    val dialogSetPrivacy = this.dialogsState.setPrivacyDialog
    if (dialogSetPrivacy != null) {
        dialogSetPrivacy.dismiss()
        this.dialogsState.setPrivacyDialog = null
    }
    val dialogSetCookie = this.dialogsState.setCookieDialog
    if (dialogSetCookie != null) {
        dialogSetCookie.dismiss()
        this.dialogsState.setCookieDialog = null
    }
    val dialogSetScrFil = this.dialogsState.setScrFilDialog
    if (dialogSetScrFil != null) {
        dialogSetScrFil.dismiss()
        this.dialogsState.setScrFilDialog = null
    }
    val dialogWebCerti = this.dialogsState.certDialog
    if (dialogWebCerti != null) {
        dialogWebCerti.dismiss()
        this.dialogsState.certDialog = null
    }
    val dialogConfirm = this.dialogsState.restartGuideDialog
    if (dialogConfirm != null) {
        dialogConfirm.dismiss()
        this.dialogsState.restartGuideDialog = null
    }
    val restartNoticeDialog = this.dialogsState.restartNoticeDialog
    if (restartNoticeDialog != null) {
        restartNoticeDialog.dismiss()
        this.dialogsState.restartNoticeDialog = null
    }
    val scriptInstallDialog = this.dialogsState.scriptInstallDialog
    if (scriptInstallDialog != null) {
        scriptInstallDialog.dismiss()
        this.dialogsState.scriptInstallDialog = null
    }
    val dialogUpdateScript = this.dialogsState.updateScriptDialog
    if (dialogUpdateScript != null) {
        dialogUpdateScript.dismiss()
        this.dialogsState.updateScriptDialog = null
    }
}

fun WebViewActivity.postBarUpdate() {
    if (this.dialogsState.barUpdateBusy) {
        return
    }
    this.dialogsState.barUpdateBusy = true
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    val runnable = this.barUpdateTask
    myWebCoord.removeCallbacks(runnable)
    this.webCoord!!.post(runnable)
}

fun WebViewActivity.closeCaptureModeSheet() {
    val sheet = this.captureModeSheet
    if (sheet != null) {
        sheet.dismiss()
        this.captureModeSheet = null
    }
}

fun WebViewActivity.shouldAutoTranslate(auto: kotlin.Boolean): kotlin.Boolean {
    var siteListed = false
    if ((AlbumSettings.transMode.value != 0 || this.transRequested) && !this.onStartPage && this.dialogsState.transState.toInt() != 2) {
        if ((auto == true)) {
            val pageUrl = this.dialogsState.pageUrl
            val pageHost = this.dialogsState.pageHost
            if ((TextUtils.isEmpty(pageHost) || (pageHost != "x.com" && !pageHost!!.endsWith("onlyfans.com"))) && !isImagePath(
                    pageUrl
                )
            ) {
                val transMode = AlbumSettings.transMode.value
                if ((transMode == 1 || transMode == 2) && !this.transRequested) {
                    val dataBookTransL = DataBookTrans.instance(this.dialogsState.appContext)
                    dataBookTransL.javaClass
                    try {
                        val arrayList: ArrayList<String?>? = dataBookTransL.sites
                        if (arrayList == null || arrayList.isEmpty()) {
                            siteListed = false
                        } else {
                            val trimmedUrl = trimTrailingSlash(pageUrl)
                            if (TextUtils.isEmpty(trimmedUrl)) {
                                siteListed = false
                            } else if (dataBookTransL.sites!!.contains(trimmedUrl)) {
                                siteListed = true
                            } else if (TextUtils.isEmpty(pageHost)) {
                                siteListed = false
                            } else {
                                siteListed = dataBookTransL.sites!!.contains(pageHost)
                            }
                        }
                    } catch (unused: Exception) {
                    }
                    if (siteListed) {
                    }
                }
            }
        }
        return true
    }
    return false
}

fun WebViewActivity.applyUpButtonPos() {
    val view = this.transFloatButton
    val layoutParams = this.floatingState.overlays?.params(view)
    if (view == null || layoutParams == null) {
        return
    }
    val anchorGravity = if (ZoneSettings.showUpPos.value == 3) 51 else 53
    if (layoutParams.anchorGravity == anchorGravity && layoutParams.gravity == anchorGravity) {
        return
    }
    layoutParams.anchorGravity = anchorGravity
    layoutParams.gravity = anchorGravity
    this.floatingState.overlays?.relayout(view)
}

fun WebViewActivity.showLoadImgDialog(pageUrl: String?, forceLoad: kotlin.Boolean) {
    var mode: Int = 0
    val sourceUrl: String?
    val fromCache: kotlin.Boolean
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeLoadImgDialog()
    if (TextUtils.isEmpty(pageUrl) || "file:///android_asset/shortcut.html" == pageUrl || "about:blank" == pageUrl) {
        showToast(this, AppR.string.not_supported_page)
        return
    }
    if (this.dialogsState.loadDialogKind.toInt() != 0) {
        return
    }
    this.dialogsState.loadDialogKind = 1.toByte()
    if ((forceLoad == true)) {
        if (this.loadView == null) {
            this.loadView = WebLoadView(this, this.webCoord, pageUrl)
        }
        sourceUrl = pageUrl
        fromCache = false
        mode = 0
    } else if (TextUtils.isEmpty(this.currentPage!!.fileUrl)) {
        val webLoadTaskB: WebLoadTask = WebLoadTask.Companion.instance()
        val webNestView = this.currentPage
        webLoadTaskB.started = true
        webLoadTaskB.loadWeb = webNestView
        webLoadTaskB.taskListener = null
        webLoadTaskB.loadState = 0.toByte()
        webLoadTaskB.netError = false
        sourceUrl = pageUrl
        mode = 2
        fromCache = false
    } else {
        val fileUrl = this.currentPage!!.fileUrl
        if ("file:///android_asset/shortcut.html" == fileUrl || "about:blank" == fileUrl) {
            this.dialogsState.loadDialogKind = 0.toByte()
            showToast(this, AppR.string.not_supported_page)
            return
        } else if (!URLUtil.isNetworkUrl(fileUrl)) {
            this.dialogsState.loadDialogKind = 0.toByte()
            showToast(this, AppR.string.invalid_url)
            return
        } else {
            if (this.loadView == null) {
                this.loadView = WebLoadView(this, this.webCoord, fileUrl)
            }
            sourceUrl = fileUrl
            fromCache = true
            mode = 0
        }
    }
    val dialogLoadImg = DialogLoadImg(this, sourceUrl, fromCache, forceLoad, mode, LoadImgDialogListener(this))
    this.loadImgDialog = dialogLoadImg
    dialogLoadImg.setOnDismissListener(LoadImgDialogDismissed(this))
}

fun WebViewActivity.resumeCurrentPage() {
    val pendingDeskMode = this.tabsState.pendingDeskMode
    val pendingPageUrl = this.dialogsState.pendingPageUrl
    this.dialogsState.pendingPageUrl = null
    val webNestView: WebNestView? = if (this.tabsState.currentFrame == null) null else this.currentPage
    if (this.tabsState.currentFrame == null || webNestView == null) {
        return
    }
    webNestView.onResume()
    this.currentPage!!.setVisibility(View.VISIBLE)
    val currentPage = this.currentPage
    if (currentPage!!.deskMode != pendingDeskMode) {
        val isDesktopAgent = currentPage.isDesktopAgent()
        currentPage.deskMode = pendingDeskMode
        if (pendingDeskMode) {
            setUserAgent(currentPage.getSettings(), chromeVersion(currentPage.getContext()))
        } else {
            currentPage.applyUserAgent(currentPage.getContext(), ZtwoSettings.agentType.value, null)
        }
        if (isDesktopAgent != currentPage.isDesktopAgent()) {
            val url = currentPage.getUrl()
            evalPageJs(currentPage, url, MainUtil.hostOf(url, true))
        }
        currentPage.prepareReload()
    }
    this.tabsState.currentFrame!!.trimBefore()
    loadPageUrl(pendingPageUrl, true, true)
    checkPageLanguage(true)
    this.tabsState.currentFrame!!.postBlankTime()
    this.tabsState.goBackAnimBusy = false
}

fun WebViewActivity.startUrlDownload(url: String?, disposition: String?, mime: String?, length: Long) {
    if (TextUtils.isEmpty(url)) {
        showToast(this, AppR.string.invalid_url)
        return
    }
    if (url!!.startsWith("blob:")) {
        runInBackground(WvaBlobDownloadScript(this, url = url, mime = disposition, disposition = mime, length = length))
        return
    }
    if (!URLUtil.isNetworkUrl(url) && !url.startsWith("data:image/")) {
        showToast(this, AppR.string.invalid_url)
    } else {
        this.pageState.phpDownload =
            "application/octet-stream" == mime && !TextUtils.isEmpty(url) && url.contains("/index.php?")
        openDownloadDialog(url, disposition, mime, length, 0, null)
    }
}

fun WebViewActivity.showPreviewDialog(url: String?, dirName: String?, fromLink: kotlin.Boolean) {
    if (this.isClosing || this.previewDialog != null) {
        return
    }
    closePreviewDialog()
    if (TextUtils.isEmpty(url)) {
        showToast(this, AppR.string.invalid_url)
        return
    }
    this.previewFromLink = (fromLink) == true
    this.dialogsState.previewDialogUrl = url
    val dialogPreview = DialogPreview(this, url, resolvePageLink(url), null, dirName, PreviewDialogListener(this))
    this.previewDialog = dialogPreview
    dialogPreview.setOnDismissListener(PreviewDialogDismissed(this))
}

fun WebViewActivity.settleSwipes() {
    val tabSwipeState = this.tabsState.tabSwipeState
    if (tabSwipeState.toInt() != 0) {
        finishTabAnim(tabSwipeState.toInt(), this.tabsState.tabSwipeForward)
    }
    val pageSwipeState = this.tabsState.pageSwipeState
    if (pageSwipeState.toInt() != 0) {
        finishPageAnim(pageSwipeState.toInt(), this.pageSwipeForward)
        if (this.barsState.pageItem != null) {
            pauseMedia(this.currentPage)
            pauseWebView(this.currentPage)
            restorePageItem()
            val restoreDeskMode = this.barsState.restoreDeskMode
            val restoredPageUrl = this.dialogsState.restoredPageUrl
            this.dialogsState.restoredPageUrl = null
            val webNestFrame = this.tabsState.currentFrame
            val webNestView: WebNestView? = if (webNestFrame != null) this.currentPage else null
            if (webNestFrame != null && webNestView != null) {
                if (webNestView.hasSetting) {
                    webNestView.setWebViewClient(WvaLocalWebViewClient(this))
                    this.currentPage!!.setWebChromeClient(WvaLocalChromeClient(this))
                    this.currentPage!!.setDownloadListener(PageDownloadListener(this))
                } else {
                    applyWebSettings(webNestView, 0)
                    addJsBridge(webNestView)
                    applyDarkSettings(webNestView)
                    attachPageListeners(webNestFrame, webNestView, 0)
                }
                this.tabsState.pendingDeskMode = restoreDeskMode
                this.dialogsState.pendingPageUrl = restoredPageUrl
            }
            resumeCurrentPage()
        }
    }
}

fun WebViewActivity.onPageDownload(url: String?, disposition: String?, mime: String?, length: Long, useCurrent: kotlin.Boolean) {
    var pageState: Int = 0
    if (TextUtils.isEmpty(url)) {
        showToast(this, AppR.string.invalid_url)
        return
    }
    if (offerScriptInstall(url)) {
        return
    }
    val webNestView: WebNestView? = if (!useCurrent) null else this.currentPage
    pageState = if (!useCurrent || webNestView == null || webNestView.canGoBack()) 0 else this.currentPage!!.pageState
    if (!useCurrent || webNestView == null || webNestView.canGoBack() || !(url == this.dialogsState.pageUrl || pageState == 0 || pageState == 1)) {
        startUrlDownload(url, disposition, mime, length)
        return
    }
    this.miscState.pendingDownloadUrl = url
    this.miscState.pendingDownloadDisposition = disposition
    this.miscState.pendingDownloadMime = mime
    this.miscState.pendingDownloadLength = length
    this.miscState.pendingDownloadUseCurrent = (useCurrent) == true
    goBackPage(WvaOnPageDownloadListener(this))
}

fun WebViewActivity.closeDarkConfirmDialog() {
    val dialogConfirm = this.darkConfirmDialog
    if (dialogConfirm != null) {
        dialogConfirm.dismiss()
        this.darkConfirmDialog = null
    }
}

fun WebViewActivity.onPageIcon(webView: WebView?, bitmap: Bitmap?, tabIndex: Int, tabOnly: kotlin.Boolean) {
    val found: kotlin.Boolean
    if (webView != null) {
        val url = webView.getUrl()
        if (TextUtils.isEmpty(url) || url!!.startsWith("file:///") || !isBitmapOk(bitmap) || (webView !is WebNestView)) {
            return
        }
        val webNestView = webView
        webNestView.setHasIcon(true)
        val bookUrl = webNestView.bookUrl
        if (TextUtils.isEmpty(bookUrl) || bookUrl != url) {
            found = false
        } else {
            webNestView.bookUrl = null
            found = true
        }
        saveBookIcon(url, null, bitmap, false, found)
        val webTabItemD: WebTabItem? = if (this.tabBar != null) tabItemAt(tabIndex) else null
        val myWebCoord: MyWebCoord? = if (this.tabBar != null && webTabItemD != null && sameObject(
                MainUtil.shortHostOf(url),
                shortHostOf(webTabItemD.tabUrl)
            )) this.webCoord else null
        if (this.tabBar != null && webTabItemD != null && sameObject(
                MainUtil.shortHostOf(url),
                shortHostOf(webTabItemD.tabUrl)
            ) && myWebCoord != null) {
            myWebCoord.post(WvaTabBarThumbUpdate(this, bitmap = bitmap, tabIndex = tabIndex))
        }
        if ((tabOnly == true)) {
            return
        }
        val dialogQuickEdit = this.dialogsState.quickEditDialog
        if (dialogQuickEdit != null) {
            if (dialogQuickEdit.iconImage == null || dialogQuickEdit.largeIcon || dialogQuickEdit.editMode || dialogQuickEdit.iconLoading || dialogQuickEdit.iconColor != 0 || !isBitmapOk(
                    bitmap
                )
            ) {
                return
            }
            dialogQuickEdit.setIcon(bitmap)
            return
        }
        val dialogWebBookEdit = this.dialogsState.bookEditDialog
        if (dialogWebBookEdit != null) {
            dialogWebBookEdit.setIcon(bitmap)
            return
        }
        val dialogEditShort = this.editShortDialog
        if (dialogEditShort != null) {
            if (dialogEditShort.iconLoading && dialogEditShort.iconImage != null && isBitmapOk(bitmap)) {
                dialogEditShort.setIcon(bitmap)
                return
            }
            return
        }
        val dialogDownPage = this.downPageDialog
        if (dialogDownPage != null) {
            dialogDownPage.setIcon(bitmap)
            return
        }
        val dialogPrintPage = this.printPageDialog
        if (dialogPrintPage == null || !isBitmapOk(bitmap)) {
            return
        }
        dialogPrintPage.setThumb(bitmap)
    }
}

fun WebViewActivity.closeHistoryDialog() {
    resumePausedPage()
    val dialogListBook = this.historyDialog
    if (dialogListBook != null) {
        dialogListBook.dismiss()
        this.historyDialog = null
    }
}

fun WebViewActivity.runStartAction(action: Int, referer: String?): kotlin.Boolean {
    if (action != 0) {
        val myWebCoord = this.webCoord
        if (myWebCoord != null) {
            myWebCoord.post(WvaRunStartActionTask(this, action, referer!!))
            return true
        }
    } else {
        if (!isShowingStartPage()) {
            return false
        }
        loadPageUrl("file:///android_asset/shortcut.html", false, true)
        val webNestView = this.currentPage
        if (webNestView != null) {
            webNestView.setStartPage(this.onStartPage)
            return true
        }
    }
    return true
}

fun WebViewActivity.applyThemeColors(themeColor: Int, themeLight: Int, filterTint: Int) {
    if (this.webCoord != null) {
        this.dialogsState.themeColor = themeColor
        this.miscState.themeLight = themeLight
        updateWindowBgColor(themeColor, themeLight)
        applyBodyColors(themeColor, filterTint)
        tintBarFrames(themeColor)
        refreshTopBar(themeColor, themeLight)
        tintTabBar(themeColor, themeLight, true)
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaApplyBarColors(this))
    }
}

fun WebViewActivity.closeLoadEmgDialog() {
    val dialogLoadEmg = this.loadEmgDialog
    if (dialogLoadEmg != null) {
        dialogLoadEmg.dismiss()
        this.loadEmgDialog = null
    }
}

fun WebViewActivity.handleUrlOverride(source: String?): kotlin.Boolean {
    var url: String?
    var webViewActivity: WebViewActivity?
    var linkMode: Int
    val decodedUrl: String?
    if (this.currentPage != null) {
        if (this.dialogsState.areaToolbar != null) {
            finishProgress()
            return true
        }
        if (!TextUtils.isEmpty(source) && source!!.endsWith("aionet.meb.gov.tr/sahte.aspx")) {
            finishProgress()
            return true
        }
        if (openTextLink(source)) {
            finishProgress()
            return true
        }
        if (hasKnownScheme(source)) {
            val linkType = 4
            if (TextUtils.isEmpty(source)) {
                url = source
            } else if (source!!.startsWith("http")) {
                if (!source.startsWith("s://", 4) && !source.startsWith("://", 4)) {
                    try {
                        decodedUrl = URLDecoder.decode(source, "UTF-8")
                        url = decodedUrl
                    } catch (unused: Exception) {
                        url = source
                    }
                }
                url = source
            } else {
                if (source.startsWith("file:") && source.endsWith("#")) {
                    decodedUrl = "file:///android_asset/shortcut.html"
                    if (source.startsWith("file:///android_asset/shortcut.html")) {
                        url = decodedUrl
                    }
                }
                url = source
            }
            val context = this.dialogsState.appContext
            val webNestView = this.currentPage
            val pageUrl = this.dialogsState.pageUrl
            val pageHost = this.dialogsState.pageHost
            var startAction = 0
            if (webNestView == null || TextUtils.isEmpty(url) || url!!.startsWith("blob:")) {
                webViewActivity = this
            } else {
                if (url.startsWith("data:")) {
                    if (url.startsWith("text/html", 5)) {
                        startAction = 11
                    } else if (!url.startsWith("image/", 5)) {
                    }
                    webViewActivity = this
                }
                val host = hostOf(url, false)
                // 0 load in this page, 3 next page of the tab (Instant back), 4 new tab ("open links in a new tab")
                linkMode = if (needsDesktopUa(pageHost) || needsDesktopUa(host)) {
                    0
                } else if ((ZtwoSettings.tabOpen.value and 2) != 2 || sameObject(pageUrl, url) || webNestView.pageState != 4) {
                    // A page that moves on by itself while loading (a script redirect, e.g. a benchmark's next test)
                    // stays in its page: the 0 here was overwritten with 4 before, so each such step became a new tab.
                    if (MainUtil.shouldOpenInNewTab(webNestView, pageUrl, pageHost, url, host, true)) 3 else 0
                } else {
                    linkType
                }
                if (linkMode == 0) {
                    val linkUrl: String? = url
                    webViewActivity = this
                    startAction = MainUtil.handleDownloadLink(
                        webViewActivity,
                        context,
                        webNestView,
                        pageUrl,
                        pageHost,
                        linkUrl,
                        host,
                        linkMode
                    )
                    url = linkUrl
                } else {
                    webViewActivity = this
                    startAction = linkMode + 100
                }
            }
            if (startAction < 100) {
                return webViewActivity.runStartAction(startAction, source)
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaHandleUrlOverrideTask(webViewActivity, url!!, startAction, source!!))
                return true
            }
        } else {
            val myWebCoord = this.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaUrlOverrideTask(this, source))
                return true
            }
        }
    }
    return true
}

fun WebViewActivity.showDeleteTabsDialog(start: Int, tabIndex: Int, deleteMode: Int) {
    var tabCount: Int
    if (this.webCoord == null || this.dialogsState.showDeleteTabsDialogBusy) {
        return
    }
    this.dialogsState.showDeleteTabsDialogBusy = true
    if (!this.isClosing && (this.tabBarSubView != null || !isAnyDialogOpen())) {
        closeDeleteItemDialog()
        val arrayList = this.tabsState.tabItems
        if (arrayList != null) {
            var size = arrayList.size
            if (deleteMode == 1) {
                size--
            } else if (deleteMode == 2) {
                val webTabItemD = tabItemAt(tabIndex)
                if (webTabItemD != null && webTabItemD.groupId != 0L) {
                    synchronized(this.tabLock) {
                        tabCount = 0
                        for (index in 0..<size) {
                            val webTabItem = this.tabsState.tabItems!!.get(index)
                            if (webTabItem != null && webTabItem.groupId != webTabItemD.groupId) {
                                tabCount++
                            }
                        }
                    }
                    size = tabCount
                }
            } else if (deleteMode == 3) {
                val webTabItemD: WebTabItem? = if (start >= 0) tabItemAt(tabIndex) else null
                if (start >= 0 && webTabItemD != null && webTabItemD.groupId != 0L) {
                    synchronized(this.tabLock) {
                        tabCount = 0
                        for (index in start..<size) {
                            val tab = this.tabsState.tabItems!!.get(index)
                            if (tab != null && tab.uid != webTabItemD.uid) {
                                if (tab.groupId != webTabItemD.groupId) {
                                    break
                                } else {
                                    tabCount++
                                }
                            }
                        }
                    }
                    size = tabCount
                }
            }
            if (size > 0) {
                closeSnackbar(false)
                val dialogDeleteItem = DialogDeleteItem(this, WvaTabDeleteListener(this, tabsShowDeleteTabsDialogValue = tabIndex, dialogsShowDeleteTabsDialogValue = size, start = start, deleteMode = deleteMode.toByte()))
                this.deleteItemDialog = dialogDeleteItem
                dialogDeleteItem.setOnDismissListener(DeleteItemDialogDismissed(this))
            }
        }
    }
    this.webCoord!!.post(WvaShowDeleteTabsDialogTask(this))
}

fun WebViewActivity.closeLoadHmgDialog() {
    val dialogLoadHmg = this.loadHmgDialog
    if (dialogLoadHmg != null) {
        dialogLoadHmg.dismiss()
        this.loadHmgDialog = null
    }
}

fun WebViewActivity.setUpPageUi() {
    if (this.webCoord == null) {
        return
    }
    addQuickSearch()
    createEditorBar()
    setUpEditorBar()
    if (this.pullView == null && this.webCoord != null && BrowserSettings.tabBar.value != 0) {
        val themeColor = this.dialogsState.themeColor
        val themeLight = this.miscState.themeLight
        addTabBar(themeColor, themeLight)
        bindTabBar(themeColor, themeLight)
        this.webCoord!!.post(WvaExpandBars(this))
    }
    createPageViews()
    val webNestView: WebNestView? = if (this.pullView == null && this.webCoord != null) this.currentPage else null
    if (this.pullView == null && this.webCoord != null && webNestView != null) {
        val webNestFrame = this.tabsState.currentFrame
        applyWebSettings(webNestView, 0)
        addJsBridge(webNestView)
        applyDarkSettings(webNestView)
        attachPageListeners(webNestFrame, webNestView, 0)
    }
    bindFrameToTab()
    addPullView()
    addFloatingButtons()
    finishStartup()
}

fun WebViewActivity.onAddressMenuItem(newValue: Int) {
    if (this.currentPage != null) {
        var handled = false
        run sw14@{
when (newValue) {
            0 -> {
                val currentUrl = currentPageUrl(false)
                if (TextUtils.isEmpty(currentUrl)) {
                    showToast(this, AppR.string.empty)
                    return
                } else {
                    copyTextToast(this, AppR.string.copied_clipboard, "Copied URL", currentUrl)
                    return
                }
            }

            1 -> {
                val currentUrl = currentPageUrl(false)
                if (TextUtils.isEmpty(currentUrl)) {
                    showToast(this, AppR.string.empty)
                    return
                } else {
                    openIntentUrl(currentUrl, this.currentPage!!.getTitle())
                    return
                }
            }

            2 -> {
                runInBackground(WvaOnAddressMenuItemTask(this))
                return
            }

            3 -> {
                runInBackground(WvaPasteAddressTask(this))
                return
            }

            4 -> {
                addTabPlain(null, currentPageUrl(false), true, null)
                return
            }

            5 -> {
                downloadLink(currentPageUrl(false))
                return
            }

            6 -> {
                // "Delete cookies (site)": the site's cookies and stored data.
                val pageUrl = this.dialogsState.pageUrl
                val host = try { android.net.Uri.parse(pageUrl ?: "").host } catch (_: Exception) { null }
                if (TextUtils.isEmpty(host)) {
                    return
                }
                val done = DeletedToast(this)
                if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.DELETE_BROWSING_DATA)) {
                    try {
                        androidx.webkit.WebStorageCompat.deleteBrowsingDataForSite(WebProfiles.webStorage()!!, host!!, { it.run() }, done)
                        return
                    } catch (_: Exception) {
                    }
                }
                // older WebView: expire the site's cookies one by one
                deleteSiteCookies(pageUrl!!, host!!)
                done.run()
                return
            }

            7 -> {
                if (this.isClosing || isAnyDialogOpen()) {
                    return
                }
                val dialogWebCerti = this.dialogsState.certDialog
                if (dialogWebCerti != null) {
                    dialogWebCerti.dismiss()
                    this.dialogsState.certDialog = null
                }
                val certDialog = DialogWebCerti(this, this.currentPage)
                this.dialogsState.certDialog = certDialog
                certDialog.setOnDismissListener(WvaCertDialogDismissed(this))
                return
            }

            else -> return
        }
}
    }
}

fun WebViewActivity.createTabFrame(blankId: Long, pageUrl: String?, deskMode: kotlin.Boolean, loadNow: kotlin.Boolean): WebNestFrame {
    val isShortcutPage = "file:///android_asset/shortcut.html" == pageUrl
    val webNestFrame = WebNestFrame(this)
    webNestFrame.setTabUid(blankId)
    webNestFrame.setDarkBars(isShortcutPage)
    webNestFrame.setVisibility(View.INVISIBLE)
    if (!loadNow) {
        val pageItem = PageItem()
        pageItem.blankPending = true
        pageItem.blankId = blankId
        pageItem.pageUrl = pageUrl
        pageItem.deskMode = (deskMode) == true
        val arrayList: ArrayList<WebNestFrame.PageItem?> = ArrayList()
        arrayList.add(pageItem)
        webNestFrame.pageItems = arrayList
        webNestFrame.currentPageIndex = 0
        webNestFrame.addPending = webNestFrame.addNeeded
        webNestFrame.setListener(WvaPageFrameListener(this))
        return webNestFrame
    }
    pauseWebDestroy()
    val webNestView = WebNestView(this)
    attachWebDestroy(webNestView)
    webNestView.startPage = isShortcutPage
    webNestView.blankPending = true
    webNestView.blankUrl = pageUrl
    webNestView.setDeskMode(deskMode)
    webNestFrame.addPage(webNestView, 0, 0)
    webNestView.setScrollPos(ZoneSettings.scrollPos.value)
    webNestView.ensureWebViewClient()
    webNestFrame.setListener(WvaPageFrameListener(this))
    return webNestFrame
}

fun WebViewActivity.runLinkAction(imageUrl: String?, action: Int, type: Int, linkUrl: String?, isImage: kotlin.Boolean, dirName: String?) {
    run sw13@{
when (action) {
        1 -> if ((isImage == true)) {
            showPreviewDialog(imageUrl, dirName, true)
            return@sw13
        } else if (!TextUtils.isEmpty(imageUrl) && imageUrl!!.startsWith("blob:")) {
            showPreviewDialog(imageUrl, "blob:", true)
            return@sw13
        } else if (!this.isClosing && this.preImageDialog == null) {
            closePreImageDialog()
            if (!TextUtils.isEmpty(imageUrl)) {
                this.dialogsState.linkActionUrl = linkUrl
                val dialogPreImage = DialogPreImage(this, imageUrl, this.dialogsState.pageUrl, UrlLinkPreviewListener(this))
                this.preImageDialog = dialogPreImage
                dialogPreImage.setOnDismissListener(PreImageDialogDismissed(this))
                // one layer: the link menu is hidden while the preview is open (2026-11-20)
                this.urlLinkDialog?.window?.decorView?.visibility = View.INVISIBLE
            } else {
                showToast(this, AppR.string.invalid_url)
            }
            return@sw13
        }

        2 -> {
            closeUrlLinkDialog()
            if (!isImage) {
                requestImagePreview(imageUrl, null, false)
            } else {
                playVideoInPlayer(imageUrl, imageUrl, false)
            }
        }

        3 -> {
            closeUrlLinkDialog()
            addTabInGroup(null, imageUrl, true, null)
        }

        4 -> {
            closeUrlLinkDialog()
            if (!isImage) {
                openDownloadDialog(imageUrl, null, dirName, 0L, 4, null)
            } else {
                openDownloadDialog(imageUrl, null, dirName, 0L, 5, null)
            }
        }

        5 -> if (!this.onStartPage) {
            closeUrlLinkDialog()
            val webNestView = this.currentPage
            if (webNestView != null) {
                openEmgGallery(webNestView.getUrl(), false)
            }
        } else {
            showToast(this, AppR.string.not_supported_page)
        }

        6 -> {
            closeUrlLinkDialog()
            val searchUrl = searchImageUrl(type, imageUrl)
            if (!TextUtils.isEmpty(searchUrl)) {
                addTabInGroup(null, searchUrl, true, null)
            } else {
                showToast(this, AppR.string.fail)
            }
        }

        7 -> {
            closeUrlLinkDialog()
            copyTextToast(this, AppR.string.copied_clipboard, "Copied URL", imageUrl)
        }

        8 -> {
            closeUrlLinkDialog()
            openIntentUrl(imageUrl, linkUrl)
        }

        10 -> {
            closeUrlLinkDialog()
            val isDefaultType = type == 0
            val checkedUrl: String? = WebViewActivity.checkedPageUrl(imageUrl)
            if (!isDefaultType) {
                val intent = Intent(this.dialogsState.appContext, com.mycompany.app.ui.viewer.WallpaperActivity::class.java as Class<*>)
                intent.putExtra("EXTRA_PATH", checkedUrl)
                intent.putExtra("EXTRA_REFERER", this.dialogsState.pageUrl)
                startActivity(intent)
            } else {
                val intent = Intent(this.dialogsState.appContext, com.mycompany.app.ui.viewer.BackgroundImageActivity::class.java as Class<*>)
                intent.putExtra("EXTRA_PATH", checkedUrl)
                intent.putExtra("EXTRA_REFERER", this.dialogsState.pageUrl)
                launchExternalForResult(intent, RequestCode.IMAGE_FILE)
            }
        }

        11 -> if (this.onStartPage) {
            showToast(this, AppR.string.not_supported_page)
            return@sw13
        } else {
            closeUrlLinkDialog()
            if (this.currentPage != null && !this.isClosing && !isAnyDialogOpen()) {
                val dialogBlockImage = this.dialogsState.blockImageDialog
                if (dialogBlockImage != null) {
                    dialogBlockImage.dismiss()
                    this.dialogsState.blockImageDialog = null
                }
                if (!TextUtils.isEmpty(imageUrl)) {
                    val blockImageDialog =
                        DialogBlockImage(this, this.dialogsState.pageUrl, imageUrl, WvaRunLinkActionListener(this))
                    this.dialogsState.blockImageDialog = blockImageDialog
                    blockImageDialog.setOnDismissListener(WvaBlockImageDialogDismissed(this))
                } else {
                    showToast(this, AppR.string.invalid_url)
                }
                return@sw13
            }
        }

        12 -> if (!this.onStartPage) {
            closeUrlLinkDialog()
            addAreaToolbar(true)
        } else {
            showToast(this, AppR.string.not_supported_page)
        }
    }
}
}

fun WebViewActivity.applySystemBars() {
    if (this.webCoord != null) {
        this.dialogsState.applySystemBarsBusy = true
        val cutOut = if (this.videoFrame != null) VideoSettings.cutOut.value else true
        applyDisplayCutout(getWindow(), this.dialogsState.showStatusBar, cutOut)
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaApplySystemBarsModeTask(this))
    }
}

/** The menu tip waits for the first menu to close (see [showMainMenu]). */
private var menuTipDue = false

/** A menu opens: when it closes, the menu tip may come (once ever). */
fun markMenuTipDue() { menuTipDue = true }

fun WebViewActivity.closeMenuDialogs() {
    if (menuTipDue && (this.menuMainDialog != null || this.dialogsState.menuListDialog != null)) {
        menuTipDue = false
        com.mycompany.app.ui.tips.TipManager.forEvent(com.mycompany.app.ui.tips.Tip.MENU)?.let { tip -> this.mainHandler?.postDelayed({ showTip(tip) }, 800) }
    }
    val dialogMenuMain = this.menuMainDialog
    if (dialogMenuMain != null) {
        dialogMenuMain.dismiss()
        this.menuMainDialog = null
        return
    }
    val dialogMenuList = this.dialogsState.menuListDialog
    if (dialogMenuList != null) {
        if (!this.mainMenuBusy) {
            this.mainMenuBusy = dialogMenuList.startClose()
        }
        this.dialogsState.menuListDialog = null
    }
}

fun WebViewActivity.onUrlLinkAction(action: Int, url: String?, label: String?) {
    when (action) {
        1 -> {
            closeUrlLinkDialog()
            showWebViewDialog(url, 0, null, false)
        }

        2 -> {
            closeUrlLinkDialog()
            openInCurrentPage(url, this.dialogsState.pageUrl)
        }

        3 -> {
            closeUrlLinkDialog()
            addTabPlain(null, url, true, null)
        }

        4 -> {
            closeUrlLinkDialog()
            openInNewTab(url, null)
        }

        5 -> {
            closeUrlLinkDialog()
            createBackTab(url)
        }

        6 -> {
            closeUrlLinkDialog()
            runInBackground(WvaRestoreTabFromUrl(this, url = url))
        }

        7 -> {
            closeUrlLinkDialog()
            copyTextToast(this, AppR.string.copied_clipboard, "Copied URL", url)
        }

        8 -> {
            closeUrlLinkDialog()
            openIntentUrl(url, label)
        }

        9 -> if (!TextUtils.isEmpty(label)) {
            closeUrlLinkDialog()
            copyTextToast(this, AppR.string.copied_clipboard, "Copied Title", label)
        } else {
            showToast(this, AppR.string.save_empty)
        }

        10 -> downloadLink(url)
        11 -> if (!this.onStartPage) {
            closeUrlLinkDialog()
            val lockType = SecretSettings.guardType(2)
            if (lockType != 0) {
                this.dialogsState.pendingLinkUrl = url
                val intentH = lockIntent(this.dialogsState.appContext, lockType)
                intentH.putExtra("EXTRA_PASS", SecretSettings.guardPass(2))
                intentH.putExtra("EXTRA_TYPE", 2)
                launchExternalForResult(intentH, RequestCode.UNLOCK)
            } else {
                showBlockLinkDialog(url)
            }
        } else {
            showToast(this, AppR.string.not_supported_page)
        }

        12 -> if (!this.onStartPage) {
            closeUrlLinkDialog()
            addAreaToolbar(true)
        } else {
            showToast(this, AppR.string.not_supported_page)
        }
    }
}

fun WebViewActivity.attachWebDestroy(webNestView: WebNestView?) {
    if (webNestView != null) {
        webNestView.setWebDestroy(this.webDestroy)
    }
}

fun WebViewActivity.layoutTabFrames() {
    val prevTabIndex: Int
    val curTabIndex: Int
    val nextTabIndex: Int
    var prevFrame: WebNestFrame?
    var nextFrame: WebNestFrame?
    var curFrame: WebNestFrame?
    var keptPrevFrame: WebNestFrame?
    var found: kotlin.Boolean
    if (this.pageLayout != null) {
        val arrayList = this.tabsState.tabItems
        var insertPos = 0
        val size = if (arrayList == null) 0 else arrayList.size
        if (size == 1) {
            curTabIndex = this.currentTabIndex
            prevTabIndex = -1
            nextTabIndex = -1
        } else if (size == 2) {
            val currentTabIndexNow = this.currentTabIndex
            if (currentTabIndexNow == 0) {
                curTabIndex = currentTabIndexNow
                nextTabIndex = currentTabIndexNow + 1
                prevTabIndex = -1
            } else {
                prevTabIndex = currentTabIndexNow - 1
                curTabIndex = currentTabIndexNow
                nextTabIndex = -1
            }
        } else if (size > 2) {
            val currentTabIndexNow = this.currentTabIndex
            var index = currentTabIndexNow - 1
            val nextIndex = currentTabIndexNow + 1
            if (ZtwoSettings.tabLoop.value) {
                if (index < 0) {
                    index = size - 1
                }
                if (nextIndex >= size) {
                    curTabIndex = currentTabIndexNow
                    prevTabIndex = index
                    nextTabIndex = 0
                } else {
                    curTabIndex = currentTabIndexNow
                    prevTabIndex = index
                    nextTabIndex = nextIndex
                }
            } else {
                curTabIndex = currentTabIndexNow
                prevTabIndex = index
                nextTabIndex = nextIndex
            }
        } else {
            prevTabIndex = -1
            curTabIndex = -1
            nextTabIndex = -1
        }
        val childCount = this.pageLayout!!.getChildCount()
        if (childCount > 0) {
            prevFrame = null
            nextFrame = null
            curFrame = null
            for (index in childCount - 1 downTo -1 + 1) {
                val childAt = this.pageLayout!!.getChildAt(index)
                if (childAt != null && (childAt is WebNestFrame)) {
                    val childFrame = childAt
                    val webTabItemD = tabItemAt(prevTabIndex)
                    val prevTabFrame: WebNestFrame? = if (webTabItemD == null) null else webTabItemD.webFrame
                    if (webTabItemD == null || prevTabFrame == null || (prevTabFrame != childAt)) {
                        val curItem = tabItemAt(curTabIndex)
                        val curTabFrame: WebNestFrame? = if (curItem == null) null else curItem.webFrame
                        if (curItem == null || curTabFrame == null || (curTabFrame != childAt)) {
                            val nextItem = tabItemAt(nextTabIndex)
                            val nextTabFrame: WebNestFrame? = if (nextItem == null) null else nextItem.webFrame
                            if (nextItem == null || nextTabFrame == null || (nextTabFrame != childAt)) {
                                detachFrame(childFrame)
                            } else {
                                nextFrame = childFrame
                            }
                        } else {
                            curFrame = childFrame
                        }
                    } else {
                        prevFrame = childFrame
                    }
                }
            }
        } else {
            prevFrame = null
            nextFrame = null
            curFrame = null
        }
        if (this.tabsState.tabItems != null) {
            if (prevFrame == null || tabItemAt(prevTabIndex) != null) {
                keptPrevFrame = prevFrame
            } else {
                detachFrame(prevFrame)
                keptPrevFrame = null
            }
            if (nextFrame != null && tabItemAt(nextTabIndex) == null) {
                detachFrame(nextFrame)
                nextFrame = null
            }
            val prevItemToAttach: WebTabItem? = if (keptPrevFrame == null) tabItemAt(prevTabIndex) else null
            if (keptPrevFrame == null && prevItemToAttach != null) {
                try {
                    val webFrame = prevItemToAttach.webFrame
                    if (webFrame == null) {
                        prevItemToAttach.webFrame =
                            createTabFrame(prevItemToAttach.uid, prevItemToAttach.tabUrl, prevItemToAttach.deskMode, false)
                    } else {
                        detachFromParent(webFrame)
                    }
                    this.pageLayout!!.moveFrameTo(prevItemToAttach.webFrame, 0)
                    keptPrevFrame = prevItemToAttach.webFrame
                } catch (unused: Exception) {
                }
            }
            val curItemToAttach: WebTabItem? = if (curFrame != null) null else tabItemAt(curTabIndex)
            if (curFrame != null || curItemToAttach == null) {
                found = false
            } else {
                try {
                    val webFrame = curItemToAttach.webFrame
                    if (webFrame == null) {
                        curItemToAttach.webFrame =
                            createTabFrame(curItemToAttach.uid, curItemToAttach.tabUrl, curItemToAttach.deskMode, true)
                    } else {
                        detachFromParent(webFrame)
                    }
                    curItemToAttach.webFrame!!.pageCreate
                    val prevIndex = if (keptPrevFrame == null) 0 else this.pageLayout!!.indexOfChild(keptPrevFrame)
                    this.pageLayout!!.moveFrameTo(
                        curItemToAttach.webFrame,
                        if (keptPrevFrame == null || prevIndex < 0) 0 else prevIndex + 1
                    )
                    curFrame = curItemToAttach.webFrame
                    found = true
                } catch (unused2: Exception) {
                    found = false
                }
            }
            val webTabItemD: WebTabItem? = if (nextFrame == null) tabItemAt(nextTabIndex) else null
            if (nextFrame == null && webTabItemD != null) {
                try {
                    val webFrame = webTabItemD.webFrame
                    if (webFrame == null) {
                        webTabItemD.webFrame =
                            createTabFrame(webTabItemD.uid, webTabItemD.tabUrl, webTabItemD.deskMode, false)
                    } else {
                        detachFromParent(webFrame)
                    }
                    val frameIndex = if (curFrame != null) this.pageLayout!!.indexOfChild(curFrame) else 0
                    if (curFrame != null && frameIndex >= 0) {
                        insertPos = frameIndex + 1
                    }
                    this.pageLayout!!.moveFrameTo(webTabItemD.webFrame, insertPos)
                    nextFrame = webTabItemD.webFrame
                } catch (unused3: Exception) {
                }
            }
            if (found) {
                this.pageLayout!!.requestLayout()
            }
            touchTabUsage(this.currentTabIndex)
            if (this.addTabAnim) {
                return
            }
            if (keptPrevFrame != null) {
                try {
                    val visibility = keptPrevFrame.getVisibility()
                    if (visibility == 0) {
                        keptPrevFrame.setVisibility(View.GONE)
                    }
                    if (visibility != 4) {
                        keptPrevFrame.refreshPage()
                    }
                } catch (unused4: Exception) {
                }
            }
            if (nextFrame != null) {
                try {
                    val visibility = nextFrame.getVisibility()
                    if (visibility == 0) {
                        nextFrame.setVisibility(View.GONE)
                    }
                    if (visibility != 4) {
                        nextFrame.refreshPage()
                    }
                } catch (unused5: Exception) {
                }
            }
            trimOldTabs()
        }
    }
}

fun WebViewActivity.showUrlLinkDialog(linkUrl: String?, imageUrl: String?, linkFlag: kotlin.Boolean, label: String?, fromFollowUp: kotlin.Boolean) {
    var openImageMode: Int = 0
    val shownImage: String?
    val quickItemIsLink = this.dialogsState.quickItemIsLink
    this.dialogsState.quickItemIsLink = false
    if (this.mainHandler == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeUrlLinkDialog()
    val redirectTarget = googleRedirectTarget(linkUrl)
    val noRedirect = TextUtils.isEmpty(redirectTarget)
    val noImage = TextUtils.isEmpty(imageUrl)
    if (noImage || TextUtils.isEmpty(this.dialogsState.pageHost)) {
        shownImage = imageUrl
    } else if (this.dialogsState.pageHost!!.startsWith("www.pinterest.")) {
        val originalImage = pinterestOriginal(imageUrl)
        if (TextUtils.isEmpty(originalImage)) {
            shownImage = imageUrl
        } else {
            shownImage = originalImage
        }
    } else if (isHitomi(this.dialogsState.pageHost) && imageUrl!!.endsWith(".avif")) {
        shownImage = imageUrl.replace("/avif/", "/webp/").replace(".avif", ".webp")
    } else {
        shownImage = imageUrl
    }
    if ((fromFollowUp == true)) {
        if (noImage) {
            if (noRedirect) {
                if (this.dialogsState.areaToolbar == null) {
                    showToast(this, AppR.string.invalid_url)
                    return
                }
                return
            }
        } else if (shownImage!!.startsWith("https://pbs.twimg.com/ext_tw_video_thumb/") && !TextUtils.isEmpty(
                this.dialogsState.pageHost
            ) && this.dialogsState.pageHost == "x.com"
        ) {
            return
        }
    }
    if (quickItemIsLink) {
        openImageMode = 0
    } else {
        if (!noRedirect && ZoneSettings.openLink.value != 0) {
            if (this.dialogsState.areaToolbar == null) {
                hidePageOverlays(true)
                onUrlLinkAction(ZoneSettings.openLink.value, redirectTarget, label)
                return
            }
            return
        }
        var openImageMode = if (noImage) 0 else ZoneSettings.openImage.value
        if (noImage || openImageMode == 0) {
            openImageMode = 0
        } else if (openImageMode != 4 && openImageMode != 9) {
            if (this.dialogsState.areaToolbar == null) {
                hidePageOverlays(true)
                runLinkAction(shownImage, ZoneSettings.openImage.value, 0, label, linkFlag, null)
                return
            }
            return
        }
    }
    hidePageOverlays(true)
    var dialogTheme = AppR.style.DialogExpandTheme
    if (openImageMode == 4 || openImageMode == 9 || isLandscapeScreen()) {
        dialogTheme = 0
    }
    val dialogUrlLink =
        DialogUrlLink(this, dialogTheme, redirectTarget, shownImage, linkFlag, label, this.dialogsState.pageUrl, openImageMode, WvaUrlLinkListener(this))
    this.urlLinkDialog = dialogUrlLink
    dialogUrlLink.setOnDismissListener(UrlLinkDialogDismissed(this))
    if (MainConst.isApi30Insets || dialogTheme != 0) {
        this.urlLinkDialog!!.applyColors(this.onStartPage, useDarkWindow(), pageThemeColor(), navBarColorNow(), true)
    }
}

fun WebViewActivity.closePermissionDialog() {
    val dialogPermission = this.permissionDialog
    if (dialogPermission != null) {
        dialogPermission.dismiss()
        this.permissionDialog = null
    }
}

fun WebViewActivity.setPageBottomMargin(bottomMargin: Int) {
    val webNestLayout = this.pageLayout
    val layoutParams: android.widget.FrameLayout.LayoutParams? = if (webNestLayout == null) null else (webNestLayout.getLayoutParams() as android.widget.FrameLayout.LayoutParams)
    val currentMargin = if (webNestLayout == null || layoutParams == null) 0 else (layoutParams as MarginLayoutParams).bottomMargin
    if (webNestLayout == null || layoutParams == null || currentMargin == bottomMargin) {
        return
    }
    this.barsState.findBarMargin = currentMargin
    (layoutParams as MarginLayoutParams).bottomMargin = bottomMargin
    this.pageLayout!!.requestLayout()
}

fun WebViewActivity.parseVideoJson(kind: Int, json: String?) {
    var resolution = 0
    var videoSrc: String? = null
    var dashXml: String? = null
    var document: Document? = null
    var arrayList: ArrayList<Any?>? = null
    var posterUrl: String? = null
    var faceItems: ArrayList<Any?>? = null
    var elements: Elements? = null
    var childCount = 0
    var elementsChildren: Elements? = null
    var quality = 0
    var qualityLabel: String? = null
    var handler: Handler? = null
    var faceItem: FaceItem? = null
    if (this.currentPage == null || TextUtils.isEmpty(json)) {
        return
    }
    var labelStart = 0
    if (!URLUtil.isNetworkUrl(json)) {
        try {
            val jsonObjectO = JsonParser.parseString(json).getAsJsonObject()
            val jsonElementD = jsonObjectO.get("src")
            videoSrc = if (jsonElementD != null) jsonElementD.getAsString() else null
            try {
                val widthJson = jsonObjectO.get("width")
                resolution = if (widthJson != null) widthJson.getAsInt() else 0
                try {
                    var dashJson = jsonObjectO.get("dashManifest")
                    if (dashJson == null) {
                        dashJson = jsonObjectO.get("dash_manifest")
                    }
                    dashXml = if (dashJson != null) dashJson.getAsString() else null
                } catch (unused: Exception) {
                }
            } catch (unused2: Exception) {
                resolution = 0
            }
        } catch (unused3: Exception) {
            resolution = 0
            videoSrc = null
        }
        if (!TextUtils.isEmpty(dashXml)) {
            try {
                document = Jsoup.parse(dashXml!!, this.dialogsState.pageUrl!!, Parser.xmlParser())
            } catch (unused4: Exception) {
                document = null
            } catch (unused4: LinkageError) {
                document = null
            } catch (unused4: OutOfMemoryError) {
                document = null
            }
            if (document != null) {
                try {
                    var elementsSelect = document.select("[mimeType*='mp4']")
                    if (elementsSelect.size != 0) {
                        var size = elementsSelect.size
                        var index = 0
                        arrayList = null
                        posterUrl = null
                        while (index < size) {
                            try {
                                val rawElement: Any? = elementsSelect.get(index)
                                index++
                                val element = rawElement as Element?
                                if (element != null && element.children().also { elementsChildren = it }.size != 0) {
                                    val isAudio = "audio/mp4" == element.attr("mimeType")
                                    if (isAudio) {
                                        quality = labelStart
                                        qualityLabel = null
                                    } else {
                                        qualityLabel = element.attr("FBQualityLabel")
                                        if (qualityLabel.length < 2) {
                                            elements = elementsSelect
                                            childCount = size
                                            break
                                        }
                                        quality = parseIntSafe(qualityLabel.substring(labelStart, qualityLabel.length - 1))
                                    }
                                    val elementsChildrenSize = elementsChildren!!.size
                                    var childIndex = labelStart
                                    while (true) {
                                        if (childIndex >= elementsChildrenSize) {
                                            elements = elementsSelect
                                            childCount = size
                                            break
                                        }
                                        val any: Any? = elementsChildren.get(childIndex)
                                        childIndex++
                                        val child = any as Element?
                                        if (child != null) {
                                            elements = elementsSelect
                                            if ("BaseURL" == child.tagName()) {
                                                val html = child.html()
                                                if (!TextUtils.isEmpty(html)) {
                                                    childCount = size
                                                    val cleanHtml = html.replace("amp;", "")
                                                    if (!TextUtils.isEmpty(cleanHtml)) {
                                                        if (!isAudio) {
                                                            if (arrayList == null) {
                                                                val newFaces: ArrayList<Any?> =
                                                                    ArrayList<Any?>()
                                                                if (resolution != 0) {
                                                                    try {
                                                                        if (!TextUtils.isEmpty(
                                                                                videoSrc
                                                                            )
                                                                        ) {
                                                                            newFaces.add(
                                                                                FaceItem(
                                                                                    videoSrc,
                                                                                    resolution,
                                                                                    resolution.toString() + "p",
                                                                                    false
                                                                                )
                                                                            )
                                                                        }
                                                                    } catch (unused5: Exception) {
                                                                        arrayList = newFaces
                                                                    }
                                                                }
                                                                arrayList = newFaces
                                                            }
                                                            if (quality == resolution && !TextUtils.isEmpty(
                                                                    videoSrc
                                                                )
                                                            ) {
                                                                break
                                                            }
                                                            arrayList.add(
                                                                FaceItem(
                                                                    cleanHtml,
                                                                    quality,
                                                                    qualityLabel,
                                                                    true
                                                                )
                                                            )
                                                            break
                                                        }
                                                        posterUrl = cleanHtml
                                                        break
                                                    }
                                                    size = childCount
                                                }
                                            }
                                            elementsSelect = elements
                                        }
                                    }
                                } else {
                                    elements = elementsSelect
                                    childCount = size
                                    break
                                }
                                size = childCount
                                elementsSelect = elements
                                labelStart = 0
                            } catch (unused6: Exception) {
                            }
                        }
                        if (arrayList != null && !arrayList.isEmpty()) {
                            if (!TextUtils.isEmpty(posterUrl)) {
                                val faceCount = arrayList.size
                                var faceIndex = 0
                                while (faceIndex < faceCount) {
                                    val rawFaceItem: Any? = arrayList.get(faceIndex)
                                    faceIndex++
                                    val face = rawFaceItem as FaceItem?
                                    if (face != null && face.isHidden) {
                                        face.isHidden = false
                                        face.posterUrl = posterUrl
                                    }
                                }
                            }
                            try {
                                Collections.sort<FaceItem?>((arrayList as MutableList<WebViewActivity.FaceItem?>), SortFace())
                            } catch (unused7: Exception) {
                            }
                            faceItems = arrayList
                        }
                    }
                } catch (unused8: Exception) {
                    arrayList = null
                    posterUrl = null
                }
            }
        }
        if (faceItems == null && !TextUtils.isEmpty(videoSrc)) {
            faceItems = ArrayList<Any?>()
            if (resolution != 0) {
                faceItems.add(FaceItem(videoSrc, resolution, resolution.toString() + "p", false))
            } else {
                faceItems.add(FaceItem(videoSrc, 0, "SRC", false))
            }
        }
        if (TextUtils.isEmpty(videoSrc) && faceItems != null && !faceItems.isEmpty() && ((faceItems.get(
                0
            ) as FaceItem?).also { faceItem = it }) != null
        ) {
            videoSrc = faceItem!!.url
        }
        handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaVideoJsonResult(this, faceItems, videoSrc!!, kind))
    }
    videoSrc = json
    resolution = 0
    faceItems = null
    faceItems = ArrayList<Any?>()
    if (resolution != 0) {
        faceItems.add(FaceItem(videoSrc, resolution, resolution.toString() + "p", false))
    } else {
        faceItems.add(FaceItem(videoSrc, 0, "SRC", false))
    }
    if (TextUtils.isEmpty(videoSrc)) {
        videoSrc = faceItem!!.url
    }
    handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.post(WvaVideoJsonResult(this, faceItems, videoSrc!!, kind))
}

fun WebViewActivity.applyWebSettings(webNestView: WebNestView?, stage: Int) {
    if (webNestView == null) {
        return
    }
    val index = ZoneSettings.textSize.value
    if (index < 50 || index > 500) {
        ZoneSettings.editLocal { set(ZoneSettings.textSize, 100) }
    }
    webNestView.setHasSetting(true)
    attachWebDestroy(webNestView)
    val settings = webNestView.getSettings()
    settings.setTextZoom(ZoneSettings.textSize.value)
    settings.setSupportZoom(true)
    settings.setBuiltInZoomControls(true)
    settings.setDisplayZoomControls(false)
    settings.setUseWideViewPort(true)
    settings.setLoadWithOverviewMode(true)
    settings.setDomStorageEnabled(true)
    settings.setJavaScriptCanOpenWindowsAutomatically(true)
    settings.setSupportMultipleWindows(true)
    settings.setMediaPlaybackRequiresUserGesture(false)
    settings.setDatabaseEnabled(true)
    settings.setMixedContentMode(0)
    settings.setAllowFileAccess(true)
    if (ZoneSettings.hideImage.value) {
        settings.setLoadsImagesAutomatically(false)
    }
    if (ZoneSettings.preRaster.value) {
        settings.setOffscreenPreRaster(true)
    }
    enableEngineCaches(settings)
    webNestView.applyRendererPriority()
    if (stage == 0) {
        initUserAgent(this.dialogsState.appContext)
    }
    val deskMode = webNestView.deskMode
    val context = this.dialogsState.appContext
    if (deskMode) {
        setUserAgent(settings, chromeVersion(context))
    } else {
        webNestView.applyUserAgent(context, ZtwoSettings.agentType.value, settings)
    }
    webNestView.setEnableJs(BrowserSettings.enableJs.value)
    webNestView.setViewCookie(this.dialogsState.appContext)
    webNestView.setOverScrollMode(2)
}

fun WebViewActivity.finishPageChange(direction: Int, pageSwipeForward: kotlin.Boolean, prevPageListener: PrevPageListener?) {
    finishPageAnim(direction, pageSwipeForward)
    if (this.barsState.pageItem == null) {
        refreshAfterPageChange()
        if (prevPageListener != null) {
            prevPageListener.onDone()
        }
        this.tabsState.goBackPageBusy = false
        return
    }
    this.miscState.prevPageListener = prevPageListener
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaFinishPageChangeTask(this))
}

fun WebViewActivity.closePreviewDialog() {
    val dialogPreview = this.previewDialog
    if (dialogPreview != null) {
        dialogPreview.dismiss()
        this.previewDialog = null
    }
    this.dialogsState.previewDialogUrl = null
}

fun WebViewActivity.closeQuickDeleteDialog() {
    val dialogDeleteItem = this.quickDeleteDialog
    if (dialogDeleteItem != null) {
        dialogDeleteItem.dismiss()
        this.quickDeleteDialog = null
    }
}

fun WebViewActivity.addPullView() {
    if (this.pullView != null || this.webCoord == null || this.currentPage == null) {
        return
    }
    this.pullView = MyPullView(this, this.onStartPage)
    val dp40Px = MainApp.dp40
    // These three sat between the bars (child indices 2–4): first in the overlay stack's coordinator band
    val layoutParams = com.mycompany.app.ui.browser.OverlayParams(dp40Px, dp40Px)
    layoutParams.gravity = 1
    layoutParams.unclipped = true
    this.floatingState.overlays!!.addCoordView(this.pullView, layoutParams, 0)
    var topMargin = 0
    this.dialogsState.progressBar = MyProgressBar(this, 0)
    tintProgressBar(this.dialogsState.themeColor)
    this.dialogsState.progressBar!!.setVisibility(View.GONE)
    val barParams = com.mycompany.app.ui.browser.OverlayParams(-1, MainApp.dp4)
    barParams.anchor = com.mycompany.app.ui.browser.OverlayParams.TOP_BAR
    if (PdfSettings.topToBottom.value) {
        // the address bar is at the bottom: the loading line sits on its top edge, the one facing the page (2026-11-19)
        barParams.anchor = com.mycompany.app.ui.browser.OverlayParams.BOTTOM_BAR
        barParams.anchorGravity = 48
        barParams.gravity = 48
        this.dialogsState.progressBar!!.alongTopEdge = true
    } else {
        barParams.anchorGravity = 80
        barParams.gravity = 80
    }
    if (!BrowserSettings.showStatus.value && MainUtil.topBarHeight() == 0) {
        topMargin = MainApp.dp4 / 2
    }
    barParams.topMargin = topMargin
    this.floatingState.overlays!!.addCoordView(this.dialogsState.progressBar, barParams, 1)
    val myScrollBar = MyScrollBar(this)
    this.scrollBar = myScrollBar
    myScrollBar.setPreColor(if (AppState.darkUi) -12632257 else -2434342)
    this.scrollBarSide = ZoneSettings.scrollPos.value
    val sideParams = com.mycompany.app.ui.browser.OverlayParams(MainApp.dp16 shl 1, -1)
    if (this.scrollBarSide == 1) {
        sideParams.gravity = 3
    } else {
        sideParams.gravity = 5
    }
    this.floatingState.overlays!!.addCoordView(this.scrollBar, sideParams, 2)
    val scrollPos = ZoneSettings.scrollPos.value
    val scrollBar = this.scrollBar
    if (scrollPos != 0) {
        scrollBar!!.setVisibility(View.INVISIBLE)
    } else {
        scrollBar!!.setVisibility(View.GONE)
    }
    this.currentPage!!.setScrollPos(ZoneSettings.scrollPos.value)
    applyPageLayout()
    this.pullView!!.setListener(WvaOnRefreshListener(this))
    this.scrollBar!!.setListener(WvaScrollBarListener(this))
    if (this.onStartPage) {
        val myWebBody = this.dialogsState.webBody
        if (myWebBody == null) {
            return
        } else {
            myWebBody.post(WvaAddPullViewTask(this))
        }
    }
    val isPaused = this.isPaused
    val webNestView = this.currentPage
    if (isPaused) {
        webNestView!!.pauseOnce()
    } else {
        webNestView!!.onResume()
    }
    this.dialogsState.pullViewAdded = true
    setBarAddress(barAddressText())
    if (this.dialogsState.addPullViewBusy) {
        return
    }
    this.dialogsState.addPullViewBusy = true
    runInBackground(WvaStartupLoadTask(this))
}

fun WebViewActivity.refreshTopBar(themeColor: Int, themeLight: Int) {
    var themeColor = themeColor
    val iconTint: Int
    val webViewActivity: WebViewActivity?
    val addressText = barAddressText()
    val pathText = addressPathText()
    val scheme = pageScheme(addressText)
    val showTrans = showTransIcon()
    val arrayList = this.tabsState.tabItems
    val size = if (arrayList != null) arrayList.size else 0
    if (PdfSettings.topToBottom.value) {
        themeColor = 0
        iconTint = 0
    } else {
        iconTint = themeLight
    }
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        val currentTabIndex = this.currentTabIndex
        val onStartPage = this.onStartPage
        webViewActivity = this
        myBarView.retint(webViewActivity, addressText, pathText, scheme, showTrans, currentTabIndex, size, onStartPage, MainUtil.barTint(themeColor, onStartPage), iconTint)
    } else {
        webViewActivity = this
    }
    val tintNow = iconTint
    val bottomBarView = webViewActivity.barsState.bottomBarView
    if (bottomBarView != null) {
        val currentTabIndex = webViewActivity.currentTabIndex
        val onStartPage = webViewActivity.onStartPage
        bottomBarView.retint(webViewActivity, addressText, pathText, scheme, showTrans, currentTabIndex, size, onStartPage, MainUtil.barTint(MainUtil.bottomBarColor(), onStartPage), MainUtil.bottomBarLight())
    }
    webViewActivity.applyDownIconColor(themeColor, tintNow)
}

fun WebViewActivity.snapshotDisplayPrefs(darkStart: kotlin.Boolean) {
    this.savedDarkStart = (darkStart) == true
    this.savedDarkWeb = AppState.darkWeb
    this.dialogsState.savedThemeImg = BrowserSettings.themeImg.value
    this.savedImgBright = BrowserSettings.imgBright.value
    this.savedThemeColor = pageThemeColor()
}

fun WebViewActivity.showWebViewDialog(url: String?, mode: Int, referer: String?, externalFlag: kotlin.Boolean) {
    var referer = referer
    if (this.isClosing || this.webViewDialog != null) {
        return
    }
    closeWebViewDialog()
    if (TextUtils.isEmpty(referer)) {
        referer = this.dialogsState.pageUrl
    }
    val dialogWebView = DialogWebView(this, url, referer, externalFlag, mode, WvaDialogWebListener(this, url = url))
    this.webViewDialog = dialogWebView
    dialogWebView.setOnDismissListener(WebViewDialogDismissed(this))
}

fun WebViewActivity.printPage(webView: WebView?, path: String?) {
    if (webView != null) {
        if (TextUtils.isEmpty(path)) {
            showToast(this, AppR.string.input_name)
            return
        }
        try {
            this.dialogsState.printAdapter = webView.createPrintDocumentAdapter(path!!)
            runInBackground(WvaPrintPageTask(this, path))
        } catch (unused: ActivityNotFoundException) {
            this.dialogsState.printJob = null
            showToast(this, AppR.string.apps_none)
        } catch (unused2: Exception) {
            this.dialogsState.printJob = null
            showToast(this, AppR.string.not_supported)
        }
    }
}

/**
 * The system bar strips next to the browser bars take the bars' tone (MyBarFrame.BAR_LIGHT / BAR_DARK) instead of pure white /
 * black, so bar and strip read as one surface (2026-11-19). Other colours (site theme, see-through) stay.
 */
private fun barTone(color: Int): Int = when (color) {
    -1, -460552 -> com.mycompany.app.view.MyBarFrame.BAR_LIGHT
    -16777216 -> com.mycompany.app.view.MyBarFrame.BAR_DARK
    else -> color
}

/**
 * The status bar's colour when the top bar sits at the top: the top bar's own colour (the site's theme colour, or the
 * light / dark bar colour), so the two read as one band. 0 when the top bar is moved to the bottom.
 */
fun WebViewActivity.statusColorFromTopBar(): Int {
    if (PdfSettings.topToBottom.value || this.topBarFrame == null) return 0
    // the bars in the theme colour: the strip takes that colour, like the bar does
    val source = if (BrowserSettings.headMode() == 2) pageThemeColor() else this.dialogsState.themeColor
    val tint = MainUtil.barTint(source, this.onStartPage)
    return when (tint) {
        0 -> com.mycompany.app.view.MyBarFrame.BAR_LIGHT
        1 -> com.mycompany.app.view.MyBarFrame.BAR_DARK
        else -> tint or -0x1000000
    }
}

/**
 * The navigation bar's colour while the bars are in the theme colour (and the bottom bar is shown): the bottom bar's
 * colour, so bar and strip read as one band; 0 otherwise (the strip keeps its usual tone).
 */
fun WebViewActivity.naviColorFromBottomBar(): Int {
    if (BrowserSettings.headMode() != 2 || this.bottomBarFrame == null) return 0
    val tint = MainUtil.barTint(MainUtil.bottomBarColor(), this.onStartPage)
    return when (tint) {
        0, 1 -> 0
        else -> tint or -0x1000000
    }
}

fun WebViewActivity.captureBarColors(window: Window?, topBarColor: Int, bottomBarColor: Int) {
    // top bar at the top (owner's request, 2026-11-19): the status bar takes its colour
    val fromBar = statusColorFromTopBar()
    val newTopBarColor = if (fromBar != 0) fromBar else topBarColor
    // coloured bars: the navigation bar takes the bottom bar's colour
    val fromBottom = naviColorFromBottomBar()
    val newBottomBarColor = if (fromBottom != 0) fromBottom else bottomBarColor
    val isApi = MainConst.isApi30
    if (isApi) {
        val myWebBody = this.dialogsState.webBody
        if (myWebBody == null) {
            return
        }
        if (this.dialogsState.topBarColor == 1234) {
            this.dialogsState.topBarColor = myWebBody.topColor
        }
        if (this.dialogsState.bottomBarColor == 1234) {
            this.dialogsState.bottomBarColor = this.dialogsState.webBody!!.botColor
        }
    } else {
        if (window == null) {
            return
        }
        if (this.dialogsState.topBarColor == 1234) {
            this.dialogsState.topBarColor = windowStatusColor(window)
        }
        if (this.dialogsState.bottomBarColor == 1234) {
            this.dialogsState.bottomBarColor = navBarColor(window)
        }
    }
    if (this.dialogsState.topBarColor != newTopBarColor) {
        this.dialogsState.topBarColor = newTopBarColor
        if (isApi) {
            this.dialogsState.webBody!!.topColor = newTopBarColor
        } else {
            setWindowStatusColor(window, newTopBarColor)
        }
    } else if (isApi) {
        // the body may have been rebuilt (theme switched while open): give it the colour again (its setter skips
        // repeats), as for the bottom strip below
        this.dialogsState.webBody!!.topColor = newTopBarColor
    }
    if (this.dialogsState.bottomBarColor != newBottomBarColor) {
        this.dialogsState.bottomBarColor = newBottomBarColor
        if (isApi) {
            this.dialogsState.webBody!!.botColor = newBottomBarColor
        } else {
            setNavBarColor(window, newBottomBarColor)
        }
    } else if (isApi) {
        // the body may have been rebuilt (theme switched while open): give it the colour again (its setter skips repeats)
        this.dialogsState.webBody!!.botColor = newBottomBarColor
    }
    val dialogViewRead = this.dialogsState.viewReadDialog
    if (dialogViewRead != null) {
        val statusColor = this.dialogsState.topBarColor
        val naviColor = this.dialogsState.bottomBarColor
        val windowBgColor = this.dialogsState.windowBgColor
        dialogViewRead.statusColor = statusColor
        dialogViewRead.naviColor = naviColor
        dialogViewRead.windowColor = windowBgColor
        dialogViewRead.savedStatusColor = statusColor
        dialogViewRead.savedNaviColor = naviColor
        dialogViewRead.savedWindowColor = windowBgColor
    }
}

fun WebViewActivity.updatePageProgress(pageProgress: Int) {
    this.dialogsState.pageProgress = pageProgress
    if (pageProgress == 100) maybeShowPageTip()
    val myProgressBar = this.dialogsState.progressBar
    if (myProgressBar != null) {
        if (this.dialogsState.areaToolbar != null) {
            myProgressBar.setSkipDraw(true)
            setBarIconsLoading(false)
            return
        }
        val webNestView = this.currentPage
        if (webNestView != null && webNestView.drawBackdropOnce) {
            myProgressBar.setSkipDraw(true)
            setBarIconsLoading(false)
            return
        }
        val progressValue = Math.round(myProgressBar.progress)
        if (pageProgress == 100 && progressValue == 100) {
            this.dialogsState.progressBar!!.setSkipDraw(true)
            setBarIconsLoading(false)
            return
        }
        setBarIconsLoading(true)
        val progressBar = this.dialogsState.progressBar
        val drawSkipped = progressBar!!.drawSkipped
        val runnable = this.progressTask
        if (drawSkipped) {
            progressBar.startBar()
            this.dialogsState.pageProgress = max(pageProgress, 50)
            this.dialogsState.progressBar!!.removeCallbacks(runnable)
            this.dialogsState.progressBar!!.post(runnable)
            return
            return
        }
        if (progressValue < pageProgress) {
            progressBar.progress = (progressValue + 3).toFloat()
            this.dialogsState.progressBar!!.removeCallbacks(runnable)
            this.dialogsState.progressBar!!.post(runnable)
        }
    }
}

/**
 * Shows or hides the video button: on a YouTube watch page always (its YouTube actions), elsewhere when the page has
 * a video. (Until 2026-11-26 YouTube had a button of its own.)
 */
fun WebViewActivity.updateImageButton(visible: kotlin.Boolean) {
    val youTube = this.dialogsState.onYouTube
    val webNestView: WebNestView? = if (!visible || isInputBusy() || (youTube && !this.dialogsState.isVideoPage) || SyncSettings.splash.value) null else this.currentPage
    if (!visible || isInputBusy() || (youTube && !this.dialogsState.isVideoPage) || SyncSettings.splash.value || webNestView == null) {
        val webDownView = this.imageButton
        if (webDownView != null) {
            webDownView.hide(false)
        }
        showDownIcons(false)
        setDownIconAlert(false)
        return
    }
    val noVideo = !youTube && TextUtils.isEmpty(webNestView.downVideo)
    val webDownView = this.imageButton
    if (noVideo) {
        if (webDownView != null) {
            webDownView.hide(false)
        }
        showDownIcons(false)
        setDownIconAlert(false)
        return
    }
    if (webDownView != null) {
        if (ZoneSettings.downPos.value == 3) {
            webDownView.showNow()
        } else {
            webDownView.hide(false)
        }
    }
    showDownIcons(true)
    setDownIconAlert(true)
}

fun WebViewActivity.snapshotPrefs() {
    this.dialogsState.savedZtwoF = ZtwoSettings.tabLimit.value
    this.savedMainB = MainSettings.topItems.value
    this.dialogsState.savedMainC = MainSettings.botItems.value
    this.dialogsState.savedMainD = MainSettings.botLongs.value
    this.dialogsState.savedGroups = MainSettings.barGroups.value
    this.savedTopToBottom = PdfSettings.topToBottom.value
    this.savedFixTop = BrowserSettings.fixTop.value
    this.savedFixBot = BrowserSettings.fixBot.value
    this.savedUtilD3 = MainUtil.topBarSize()
    this.savedUtilH0 = MainUtil.bottomBarHeightPref()
    val tabBar = BrowserSettings.tabBar.value
    this.savedTabBar = tabBar
    this.savedTabAccent = BrowserSettings.tabAccent.value
    this.savedTabColor = BrowserSettings.tabColor.value
    this.dialogsState.savedTabClose = PdfSettings.tabClose.value
    this.savedTabWidth = PdfSettings.tabWidth.value
    var found = false
    this.savedTabHeight = if (tabBar == 1 || tabBar == 2) PdfSettings.tabHeight.value else 0
    this.savedPdfF2 = if (MainUtil.hasTopTabBar()) PdfSettings.tabHeight.value else 0
    this.savedMainE = MainSettings.botSwipe.value
    this.savedMainF = MainSettings.addrItems.value
    this.savedMainZ = MainSettings.menuItems.value
    this.dialogsState.savedMainA = MainSettings.midItems.value
    this.savedMainL = MainSettings.appRotation.value
    this.dialogsState.savedZoneGestureOff = isZoneGestureOff()
    this.savedCustomZones = hasCustomZones()
    this.savedDarkStart = MainUtil.isDarkPage(this.onStartPage)
    this.savedDarkWeb = AppState.darkWeb
    this.dialogsState.savedThemeImg = BrowserSettings.themeImg.value
    this.savedImgBright = BrowserSettings.imgBright.value
    this.dialogsState.savedDarkHome = BrowserSettings.darkHome.value
    this.savedNavColor = navBarColorNow()
    this.savedShowStatus = BrowserSettings.showStatus.value
    this.savedShowNavi = BrowserSettings.showNavi.value
    this.savedAddrType = PdfSettings.addrType.value
    this.savedZoneX = ZoneSettings.scrollPos.value
    this.savedEditorI = EditorSettings.botAlpha.value + EditorSettings.topAlpha.value * 1000
    this.dialogsState.savedEditorJ = EditorSettings.upAlpha.value
    this.savedZoneY = ZoneSettings.showUpPos.value
    this.savedZtwoE = ZtwoSettings.addPage.value
    this.savedZoneU = ZoneSettings.hideImage.value
    this.savedZoneN = ZoneSettings.downPos.value
    this.savedZoneO_Q4 = ZoneSettings.showPip.value
    this.savedZoneP = ZoneSettings.showFull.value
    this.savedZoneQ = ZoneSettings.showDown.value
    this.savedEditorM = PrefEditor.iconArgb
    this.savedZoneT = ZoneSettings.youRate.value
    this.savedBlockSsl = PdfSettings.blockSsl.value
    this.dialogsState.savedEditorq = PrefEditor.color
    this.savedZoneO_a5 = ZoneSettings.gesCenter.value
    this.dialogsState.savedWebLand = TtsSettings.webLand.value
    this.savedWebLand2 = TtsSettings.webLand2.value
    this.savedHandTop = TtsSettings.handTop.value
    this.savedAlbumU = AlbumSettings.transMode.value
    this.savedAlbumW = AlbumSettings.transPage.value
    this.savedTransHide = AlbumSettings.transHide.value
    this.lastTransLang = this.transLangCode
    this.savedEditorZ = PrefEditor.trnsArgb
    this.savedZtriK = ZtriSettings.zoomIcon.value
    this.savedEditorU = PrefEditor.zoomArgb
    this.dialogsState.savedFloatj = FloatSettings.flt1Icon.value
    this.savedFloatK = FloatSettings.flt2Icon.value
    this.savedFloatL = FloatSettings.flt3Icon.value
    this.dialogsState.savedFloats = PrefFloat.flt1Argb
    this.savedFloatW = PrefFloat.flt2Argb
    this.savedFloatA = PrefFloat.flt3Argb
    this.savedThemeHead = BrowserSettings.headMode()
    this.savedThemeColor = pageThemeColor()
    val newsUse = ZtwoSettings.newsUse.value
    this.savedNewsUse = newsUse
    this.savedZtwoL = ZtwoSettings.newsUrl.value
    this.savedZtwoN = ZtwoSettings.newsLang.value
    if (newsUse && ZtwoSettings.newsTitle.value) {
        found = true
    }
    this.savedNewsTitle = found
    this.dialogsState.savedNewsPick = if (found) ZtwoSettings.newsPick.value else ""
    this.savedZtriC = ZtriSettings.newsIcon.value
    this.savedEditorN = PrefEditor.newsArgb
    this.dialogsState.savedZtwor = ZtwoSettings.tabIndi.value
    this.dialogsState.savedSecretLocked = isSecretLocked()
    this.dialogsState.savedZtwoZ = ZtwoSettings.recentLimit.value
    this.savedZtwoA0 = ZtwoSettings.recentSecret.value
    this.dialogsState.savedSecretx = SecretSettings.savePass.value
    this.savedZtwoU = ZtwoSettings.youUnmute.value
}

fun WebViewActivity.tintProgressBar(pressed: Int) {
    if (this.dialogsState.progressBar == null) {
        return
    }
    if (isDarkBars(this.onStartPage)) {
        this.dialogsState.progressBar!!.applyColors(-922746881, 0)
        return
    }
    if (BrowserSettings.headMode() == 0 || pressed == 0) {
        val darkUi = AppState.darkUi
        val myProgressBar = this.dialogsState.progressBar
        if (darkUi) {
            myProgressBar!!.applyColors(-922746881, -16777216)
            return
        } else {
            myProgressBar!!.applyColors(-13022805, -1)
            return
        }
    }
    val darkUi = AppState.darkUi
    val myProgressBar = this.dialogsState.progressBar
    if (darkUi) {
        myProgressBar!!.applyColors(-922746881, pressed)
    } else {
        myProgressBar!!.applyColors(-1, pressed)
    }
}

fun WebViewActivity.loadPageUrl(url: String?, isReload: kotlin.Boolean, pageChanged: kotlin.Boolean) {
    var url = url
    var pageChanged = pageChanged
    var handler: Handler? = null
    var addButtonHandler: Handler? = null
    var length = 0
    var readdButtonHandler: Handler? = null
    var pathLength = 0
    var hostChanged = false
    var videoHostEntered = false
    var removeButtonHandler: Handler? = null
    var removeLockHandler: Handler? = null
    var urlChanged = true
    this.videoState.freshLoad = !isReload
    if (!this.dialogsState.pullViewAdded || this.currentPage == null) {
        this.dialogsState.lastLoadUrl = null
        return
    }
    if (!TextUtils.isEmpty(url) && url!!.startsWith("data:text/html")) {
        url = this.currentPage!!.getUrl()
    }
    if (sameObject(this.dialogsState.lastLoadUrl, url)) {
        return
    }
    this.dialogsState.lastLoadUrl = url
    exitFind(true)
    val onYouTube = this.dialogsState.onYouTube
    val videoRateOff = this.dialogsState.videoRateOff
    val pageUrl = this.dialogsState.pageUrl
    var hostChangedOut = false
    if (!sameObject(pageUrl, url) || TextUtils.isEmpty(this.dialogsState.pageHost)) {
        if (this.transRequested && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(this.dialogsState.pageUrl) && !url!!.startsWith(
                "data:text/html"
            ) && !this.dialogsState.pageUrl!!.startsWith("data:text/html")
        ) {
            this.transRequested = false
        }
        val pageHost = this.dialogsState.pageHost
        this.dialogsState.pageUrl = url
        this.dialogsState.pageHost = hostOfUrl(url, true)
        val blobPageUrl = this.dialogsState.pageUrl
        this.dialogsState.blobPageUrl = blobPageUrl
        val isShortcutPage = "file:///android_asset/shortcut.html" == blobPageUrl
        this.onStartPage = isShortcutPage
        this.dialogsState.chromeClientBusy = !isShortcutPage && matchesNativeHost(this.dialogsState.pageHost, false)
        this.dialogsState.lastHamTag = null
        this.pageState.textZoomPending = true
        this.pageState.textZoomNudged = false
        this.dialogsState.segmentsFound = false
        this.dialogsState.segmentLengths = null
        this.dialogsState.segmentSplits = null
        this.dialogsState.segmentUrls = null
        this.dialogsState.siteFixMainFrame = false
        this.dialogsState.lastSnsUrl = null
        this.dialogsState.backPlaySrc = null
        this.dialogsState.langCheckBusy = false
        this.videoState.longPressPending = true
        hideMoveHint()
        val webAreaText = this.linkTitlePill
        if (webAreaText != null) {
            webAreaText.hideSoon()
        }
        val webAreaMenu = this.videoState.areaMenu
        if (webAreaMenu != null) {
            webAreaMenu.hideSoon()
        }
        this.currentPage!!.setUrlChanged(this.dialogsState.pageUrl)
        finishActionMode()
        if (!isXLoginPage(this.dialogsState.pageUrl, this.dialogsState.pageHost)) {
            this.dialogsState.passwordCheckDone = false
            this.dialogsState.keyboardRelayout = false
            this.dialogsState.lockCheckedUrl = null
            this.dialogsState.siteLockList = null
            if (this.floatingState.lockButton != null && (this.mainHandler.also { removeLockHandler = it }) != null) {
                removeLockHandler!!.post(WvaRemoveListFloat(this))
            }
        }
        val pageHostNow = this.dialogsState.pageHost
        if (TextUtils.isEmpty(pageHostNow)) {
            if (this.floatingState.fullscreenButton != null && (this.mainHandler.also { removeButtonHandler = it }) != null) {
                removeButtonHandler!!.post(WvaRemoveFullscreenButton(this))
            }
        } else if (pageHostNow!!.endsWith("e-hentai.org")) {
            val host = MainUtil.hostOf(this.dialogsState.pageUrl, true)
            if (if (host == null || (host.length.also {
                        pathLength = it
                    }) == 0) false else this.dialogsState.pageUrl!!.startsWith("/s/", pathLength)) {
                this.dialogsState.fullscreenButtonMode = 2.toByte()
                if (this.floatingState.fullscreenButton == null && (this.mainHandler.also { readdButtonHandler = it }) != null) {
                    readdButtonHandler!!.post(WvaAddFullscreenButton(this))
                }
            } else {
                if (if (host == null || (host.length.also {
                            length = it
                        }) == 0) false else this.dialogsState.pageUrl!!.startsWith("/lofi/s/", length)) {
                    this.dialogsState.fullscreenButtonMode = 3.toByte()
                    if (this.floatingState.fullscreenButton == null && (this.mainHandler.also { addButtonHandler = it }) != null) {
                        addButtonHandler!!.post(WvaAddFullscreenButton(this))
                    }
                } else if (this.floatingState.fullscreenButton != null && (this.mainHandler.also { handler = it }) != null) {
                    handler!!.post(WvaRemoveFullscreenButton(this))
                }
            }
        } else if (this.floatingState.fullscreenButton != null) {
            handler!!.post(WvaRemoveFullscreenButton(this))
        }
        if (sameObject(pageHost, this.dialogsState.pageHost)) {
            hostChanged = false
        } else {
            this.quickState.fakeTsMisses = 0
            clearCache()
            if (this.onStartPage) {
                this.currentPage!!.setBarColors(0, 0)
            }
            this.dialogsState.passInfoListenerText = null
            this.dialogsState.instaAppId = null
            this.dialogsState.onYouTube = isYouTubeHost(this.dialogsState.pageHost)
            val transMode = AlbumSettings.transMode.value
            if (transMode != 1 && transMode != 2 && !this.transRequested) {
                this.transCheckedUrl = null
                this.barsState.siteFixMode = 0.toByte()
            }
            hostChanged = true
        }
        val isVideoHost = this.dialogsState.isVideoHost
        checkVideoPage(this.dialogsState.pageUrl)
        if (!isVideoHost && this.dialogsState.isVideoHost) {
            hostChangedOut = true
        }
        val webVideoFrame = this.videoFrame
        if (webVideoFrame != null) {
            webVideoFrame.setYouDialog(this.dialogsState.videoRateOff)
        }
        val enteredVideoHost = hostChangedOut
        hostChangedOut = hostChanged
        videoHostEntered = enteredVideoHost
        pageChanged = true
    } else {
        videoHostEntered = false
        urlChanged = false
    }
    if (onYouTube && this.dialogsState.onYouTube) {
        if (this.dialogsState.videoRateOff) {
            this.dialogsState.lastLoadUrl = null
            return
        } else if (videoRateOff && !TextUtils.isEmpty(pageUrl) && pageUrl!!.startsWith(this.dialogsState.pageUrl!!)) {
            this.dialogsState.lastLoadUrl = null
            return
        }
    }
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return
    }
    myWebBody.post(WvaLoadPageUrlTask(this, urlChanged = urlChanged, hostChanged = hostChangedOut, videoHostEntered = videoHostEntered, pageChanged = (pageChanged) == true))
}

fun WebViewActivity.onThemeChanged(force: kotlin.Boolean) {
    val onStartPage = this.onStartPage
    this.pageState.themeOnStart = onStartPage
    this.dialogsState.windowBgColor = 0
    val dialogViewRead = this.dialogsState.viewReadDialog
    if (dialogViewRead != null) {
        val topBarColor = this.dialogsState.topBarColor
        val bottomBarColor = this.dialogsState.bottomBarColor
        dialogViewRead.statusColor = topBarColor
        dialogViewRead.naviColor = bottomBarColor
        dialogViewRead.windowColor = 0
        dialogViewRead.savedStatusColor = topBarColor
        dialogViewRead.savedNaviColor = bottomBarColor
        dialogViewRead.savedWindowColor = 0
    }
    if (!onStartPage || this.currentPage == null) {
        return
    }
    if (!force) {
        updateWindowBgColor(pageThemeColor(), pageThemeLight())
        return
    }
    applyThemeColors(pageThemeColor(), pageThemeLight(), navBarColorNow())
    val quickView = this.currentPage!!.quickPanel
    if (quickView != null) {
        quickView.setColor(true)
    }
}

fun WebViewActivity.applyPageStyle() {
    var sb: StringBuilder?
    if (this.videoFrame == null) {
        val webNestView = this.currentPage
        if (webNestView == null) {
            return
        }
        webNestView.applyPageStyle(true, this.currentTabIndex, WvaWebStyleListener(this))
        return
    }
    if (this.dialogsState.onYouTube) {
        val webNestView = this.currentPage
        if (webNestView != null) {
            if (BrowserSettings.adsBlock.value) {
                val styleJs = nativeJs5b()
                if (TextUtils.isEmpty(styleJs)) {
                    sb = null
                } else {
                    sb = StringBuilder().append(styleJs)
                }
            } else {
                sb = null
            }
            if (sb == null) {
                sb = StringBuilder()
            }
            sb.append("if(window.sb_win_open){window.sb_win_open.disconnect();window.sb_win_open=null;}")
            sb.insert(0, "(function(){")
            sb.append("})();")
            evalJs(webNestView, sb.toString(), true)
        }
        retryVideoRate()
    }
    this.videoFrame!!.setUrl(this.dialogsState.pageUrl)
}

fun WebViewActivity.closeTabGroupDeleteDialog() {
    val dialogDeleteItem = this.tabGroupDeleteDialog
    if (dialogDeleteItem != null) {
        dialogDeleteItem.dismiss()
        this.tabGroupDeleteDialog = null
    }
}

/**
 * The reader lies over the page: back, forward and the reader button close it first and leave the page where it was
 * (back used to step the hidden page back while the reader stayed). True when it was open.
 */
fun WebViewActivity.closeReaderFirst(): kotlin.Boolean {
    val reader = this.dialogsState.viewReadDialog ?: return false
    if (!isReaderOpen()) return false
    reader.handleBack()
    return true
}

fun WebViewActivity.isReaderOpen(): kotlin.Boolean {
    val dialogViewRead = this.dialogsState.viewReadDialog
    if (dialogViewRead == null) {
        return false
    }
    return !dialogViewRead.miniMode
}

fun WebViewActivity.setSystemBarsHidden(savedG: kotlin.Boolean, savedWebU: kotlin.Boolean, force: kotlin.Boolean) {
    if (!force && this.dialogsState.showStatusBar == savedG && this.videoState.showNavBar == savedWebU) {
        watchWindowHeight()
        return
    }
    if (this.dialogsState.webBody != null) {
        this.dialogsState.setSystemBarsHiddenBusy = true
        this.dialogsState.showStatusBar = (savedG) == true
        this.videoState.showNavBar = (savedWebU) == true
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaExpandBarsDelayed(this))
    }
}

fun WebViewActivity.isInTabGroup(currentTabIndex: Int): kotlin.Boolean {
    val webTabItemD = tabItemAt(currentTabIndex)
    if (webTabItemD == null) {
        return false
    }
    val prevItem = tabItemAt(currentTabIndex - 1)
    if (prevItem != null && prevItem.groupId == webTabItemD.groupId) {
        return true
    }
    val nextItem = tabItemAt(currentTabIndex + 1)
    return nextItem != null && nextItem.groupId == webTabItemD.groupId
}

fun WebViewActivity.refreshStartPage() {
    if (this.barScroll != null) {
        updateBarBehavior()
        val themeColor = pageThemeColor()
        updateWindowBgColor(themeColor, pageThemeLight())
        if (isDarkMode() && this.dialogsState.colorsForStart != this.onStartPage) {
            applyBodyColors(themeColor, navBarColorNow())
        }
        val myWebBody: MyWebBody? = if (this.currentPage == null) null else this.dialogsState.webBody
        if (this.currentPage == null || myWebBody == null) {
            return
        }
        myWebBody.post(WvaRefreshStartPageTask(this))
    }
}

fun WebViewActivity.postWindowBackgroundUpdate() {
    if (this.dialogsState.postWindowBackgroundUpdateBusy) {
        return
    }
    this.dialogsState.postWindowBackgroundUpdateBusy = true
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.post(WvaApplyWindowBackground(this))
}

fun WebViewActivity.setWindowBackground(bitmap: Bitmap?, fromImage: kotlin.Boolean) {
    try {
        if (isBitmapOk(bitmap)) {
            getWindow().setBackgroundDrawable(BitmapDrawable(getResources(), bitmap))
        } else {
            getWindow().setBackgroundDrawable(ColorDrawable(-16777216))
        }
        this.dialogsState.windowBgMode = if ((fromImage == true)) 2.toByte() else 1.toByte()
        this.dialogsState.windowBgKey = 1234
    } catch (unused: Exception) {
        this.dialogsState.windowBgMode = 0.toByte()
        this.dialogsState.windowBgKey = 1234
    }
    val handler = this.mainHandler
    if (handler != null) {
        handler.postDelayed(WvaWindowBackgroundCheck(this), 100L)
    }
}

fun WebViewActivity.updateWindowBgColor(themeColor: Int, themeLight: Int) {
    val windowBgColor: Int
    if (this.animFromStartPage || this.webCoord == null) {
        return
    }
    var newWindowBgColor = 1234
    if (useDarkWindow()) {
        windowBgColor = 1234
    } else {
        windowBgColor = if (AppState.darkUi) -16777216 else -1
    }
    val windowBgColorNow = this.dialogsState.windowBgColor
    val currentFrame: WebNestFrame? = if (windowBgColorNow != 1234 || windowBgColor == 1234) null else this.tabsState.currentFrame
    if (windowBgColorNow != 1234 || windowBgColor == 1234 || currentFrame == null || (!currentFrame.touchBlocked && currentFrame.addAnim == null)) {
        newWindowBgColor = windowBgColor
    }
    if (windowBgColorNow != newWindowBgColor) {
        this.dialogsState.windowBgColor = newWindowBgColor
        val dialogViewRead = this.dialogsState.viewReadDialog
        if (dialogViewRead != null) {
            val topBarColor = this.dialogsState.topBarColor
            val bottomBarColor = this.dialogsState.bottomBarColor
            dialogViewRead.statusColor = topBarColor
            dialogViewRead.naviColor = bottomBarColor
            dialogViewRead.windowColor = newWindowBgColor
            dialogViewRead.savedStatusColor = topBarColor
            dialogViewRead.savedNaviColor = bottomBarColor
            dialogViewRead.savedWindowColor = newWindowBgColor
        }
        postWindowBackgroundUpdate()
        tintBarFrames(themeColor)
        refreshTopBar(themeColor, themeLight)
        tintTabBar(themeColor, themeLight, true)
    }
    val webNestFrame: WebNestFrame? = if (this.nextPageAnim || this.addTabAnim) null else this.tabsState.currentFrame
    if (this.nextPageAnim || this.addTabAnim || webNestFrame == null) {
        return
    }
    webNestFrame.setDarkMode(this.onStartPage)
}

fun WebViewActivity.closeTabMenuDialog() {
    val dialogTabMenu = this.tabMenuDialog
    if (dialogTabMenu != null) {
        dialogTabMenu.dismiss()
        this.tabMenuDialog = null
    }
}

fun WebViewActivity.applyEditColor(keep: kotlin.Boolean) {
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return
    }
    if (!keep) {
        myWebBody.setEditColor(0)
        return
    }
    if (!isSingleBarMode()) {
        if (AppState.darkUi || this.onStartPage) {
            this.dialogsState.webBody!!.setEditColor(-2130706432)
            return
        } else {
            this.dialogsState.webBody!!.setEditColor(553648128)
            return
        }
    }
    val darkBars = isDarkBars(this.onStartPage)
    val webBody = this.dialogsState.webBody
    if (darkBars) {
        webBody!!.setEditColor(-2130706432)
    } else {
        webBody!!.setEditColor(0)
    }
}

/**
 * Watches a page with a password field: when a login is sent (a click, Enter or the form's submit) the page reads the
 * name and password the user typed (ScriptParts.passJs3) right then and hands them to the app (`android.onPassResult`),
 * which offers to save them; `android.onPassClick` keeps Android's autofill from offering the same. Holds no saved
 * data. Added once per page. (Before 2026-11-28 the app read the fields in a second step, often after the page had
 * already gone, so the offer was missed.)
 */
private val SAVE_WATCH_JS: String by lazy {
    "(function(){if(window.__opumSaveWatch)return;" +
        "if(!document.querySelector(\"input[type='password']\"))return;window.__opumSaveWatch=1;" +
        "var grab=function(){try{" + com.mycompany.app.main.ScriptParts.passJs3.joinToString("") + "}catch(e){}};" +
        "document.addEventListener('click',function(){android.onPassClick();grab();},true);" +
        "document.addEventListener('keyup',function(e){if(e.keyCode==13){android.onPassClick();grab();}},true);" +
        "document.addEventListener('submit',function(){android.onPassClick();grab();},true);})();"
}

fun WebViewActivity.checkSiteLock(enabled: kotlin.Boolean) {
    var found = false
    var arrayListD: ArrayList<Any?>? = null
    // another app handles logins (PassManager): OPUM's manager stays out of the page entirely
    if (!PassManager.builtIn) { offerLoginFill(null); return }
    if (!enabled || !SecretSettings.loginLock.value || SecretSettings.guardType(1) == 0 || this.dialogsState.checkSiteLockBusy) {
        found = false
        arrayListD = null
    } else {
        arrayListD = itemsForHost(this.dialogsState.appContext, this.dialogsState.pageHost) as ArrayList<Any?>?
        if (arrayListD != null && !arrayListD.isEmpty()) {
            if (sameObject(this.dialogsState.lockCheckedHost, this.dialogsState.pageHost)) {
                return
            }
            this.dialogsState.lockCheckedHost = this.dialogsState.pageHost
            val intentH = lockIntent(this.dialogsState.appContext, SecretSettings.guardType(1))
            intentH.putExtra("EXTRA_PASS", SecretSettings.guardPass(1))
            intentH.putExtra("EXTRA_TYPE", 2)
            launchExternalForResult(intentH, RequestCode.UNLOCK_SITE)
            return
        }
        this.dialogsState.checkSiteLockBusy = true
        found = true
    }
    if (sameObject(this.dialogsState.lockCheckedUrl, this.dialogsState.pageUrl)) {
        return
    }
    this.dialogsState.lockCheckedUrl = this.dialogsState.pageUrl
    if (!found) {
        arrayListD = itemsForHost(this.dialogsState.appContext, this.dialogsState.pageHost) as ArrayList<Any?>?
    }
    this.dialogsState.siteLockList = arrayListD
    if (isXLoginPage(this.dialogsState.pageUrl, this.dialogsState.pageHost)) {
        if (!PassManager.button || arrayListD == null || arrayListD.isEmpty()) {
            return
        }
        addLockButton()
        return
    }
    // Saved passwords never go into the page (2026-11-28; before, every saved login of the site was put into the
    // page as script variables that any script there could read): they are filled only when the user picks one
    // (password button or the fill bar, fillLogin). The page only reports a submitted login, to offer saving it.
    evalJs(this.currentPage, SAVE_WATCH_JS, true)
    offerLoginFill(arrayListD)
    if (!PassManager.button || arrayListD == null || arrayListD.isEmpty()) {
        return
    }
    addLockButton()
}

fun WebViewActivity.openQuickFolder(originX: Int, originY: Int, cellSize: Int, folderPath: String?, rowCount: Int) {
    if (this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeQuickSubView()
    if (this.dialogsState.openQuickFolderBusy) {
        return
    }
    this.dialogsState.openQuickFolderBusy = true
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.post(WvaCreateQuickSubView(this, folderPath = folderPath, originX = originX, originY = originY, cellSize = cellSize, rowCount = rowCount))
}

/** The video button on a YouTube page: its YouTube actions (picture-in-picture, play speed), or their menu. */
fun WebViewActivity.onVideoButtonClick(view: View?) {
    val youPip = ZoneSettings.showPip.value
    if ((if (ZoneSettings.youRate.value) (if (youPip) 1 else 0) + 1 else if (youPip) 1 else 0) > 1) {
        showVideoMenu(view, true)
    } else if (youPip) {
        runVideoAction(2)
    } else {
        runVideoAction(3)
    }
}

fun WebViewActivity.applyChangedPrefs(themeColor: Int, themeLight: Int) {
    val found: kotlin.Boolean
    val barChanged: kotlin.Boolean
    val needRebuild: kotlin.Boolean
    var barsChanged: kotlin.Boolean
    val viewGroup: ViewGroup?
    val layoutChanged: kotlin.Boolean
    var webTabBarAdapter: WebTabBar? = null
    var webViewActivity = this
    if (webViewActivity.webCoord != null) {
        val savedZtwoF = webViewActivity.dialogsState.savedZtwoF
        val tabLimit = ZtwoSettings.tabLimit.value
        if (savedZtwoF != tabLimit) {
            webViewActivity.dialogsState.savedZtwoF = tabLimit
            webViewActivity.applyTabLimit(WvaTabLimitApplied(webViewActivity))
        }
        // a group's buttons changed: both bars are rebuilt (a group's icon is its lead button's)
        val groupsChanged = !sameObject(webViewActivity.dialogsState.savedGroups, MainSettings.barGroups.value)
        webViewActivity.dialogsState.savedGroups = null
        val changed = groupsChanged || !sameObject(webViewActivity.savedMainB, MainSettings.topItems.value)
        var tintChanged = false
        val bottomChanged = if (!groupsChanged && sameObject(webViewActivity.dialogsState.savedMainC, MainSettings.botItems.value) && sameObject(
                webViewActivity.dialogsState.savedMainD,
                MainSettings.botLongs.value
            )
        ) false else true
        webViewActivity.savedMainB = null
        webViewActivity.dialogsState.savedMainC = null
        webViewActivity.dialogsState.savedMainD = null
        if (webViewActivity.savedTopToBottom != PdfSettings.topToBottom.value) {
            barChanged = true
            found = true
        } else {
            found = changed
            barChanged = false
        }
        if (found || bottomChanged) {
            needRebuild = false
            val addressText = webViewActivity.barAddressText()
            val pathText = webViewActivity.addressPathText()
            val topChanged = barChanged
            val scheme = webViewActivity.pageScheme(addressText)
            val arrayList = webViewActivity.tabsState.tabItems
            val size = if (arrayList != null) arrayList.size else 0
            val areaWidth = webViewActivity.pageAreaWidth()
            webViewActivity.updateSplitLand(webViewActivity.isLandscapeScreen())
            val myBarView: MyBarView? = if (topChanged) webViewActivity.barsState.topBarView else null
            if (topChanged && myBarView != null) {
                val topBarFrame = webViewActivity.topBarFrame
                if (topBarFrame != null && topBarFrame.mainBar != null) {
                    topBarFrame.mainBar = null
                    removeFromParent(myBarView)
                }
                val myBarFrame = webViewActivity.bottomBarFrame
                val topBar: MyBarView? = if (myBarFrame != null) webViewActivity.barsState.topBarView else null
                if (myBarFrame != null && topBar != null && myBarFrame.mainBar != null) {
                    myBarFrame.mainBar = null
                    removeFromParent(topBar)
                }
                webViewActivity.barsState.topBarView = null
                val myIconView = webViewActivity.topDownIcon
                if (myIconView != null) {
                    removeFromParent(myIconView)
                    webViewActivity.topDownIcon!!.release()
                    webViewActivity.topDownIcon = null
                }
            }
            val count = size
            if (found) {
                webViewActivity.buildMainBars(addressText, scheme, count, pathText, areaWidth)
            }
            webViewActivity = this
            if (bottomChanged) {
                webViewActivity.buildBottomBar(addressText, scheme, count, pathText, areaWidth)
            }
            webViewActivity.tintBars(webViewActivity.dialogsState.barsTinted)
            webViewActivity.updateNavButtons()
            val myBarFrame: MyBarFrame? = if (found && isDarkBars(webViewActivity.onStartPage)) webViewActivity.topBarFrame else null
            if (found && isDarkBars(webViewActivity.onStartPage) && myBarFrame != null) {
                myBarFrame.reapplyRound()
            }
            barsChanged = true
        } else {
            barsChanged = false
        }
        val myBarView: MyBarView? = if (!sameObject(webViewActivity.savedMainE, MainSettings.botSwipe.value)) webViewActivity.barsState.bottomBarView else null
        if (!sameObject(webViewActivity.savedMainE, MainSettings.botSwipe.value) && myBarView != null) {
            myBarView.shownIds = MainUtil.parseIntList(MainSettings.botSwipe.value)
        }
        webViewActivity.savedMainE = null
        if (!sameObject(webViewActivity.savedMainF, MainSettings.addrItems.value) && webViewActivity.barsState.editorBar != null) {
            val barTint = MainUtil.barTint(if (webViewActivity.isSingleBarMode()) themeColor else 0, webViewActivity.onStartPage)
            val iconColor = themeIconColor(barTint, themeLight)
            webViewActivity.barsState.editorBar!!.useExtraIds(MainSettings.addrIds())
            webViewActivity.barsState.editorBar!!.applyExtraTint(barTint, iconColor)
        }
        webViewActivity.savedMainF = null
        val topBarSize = MainUtil.topBarSize()
        val bottomBarHeight = MainUtil.bottomBarHeightPref()
        val tabBar = BrowserSettings.tabBar.value
        val savedPdfF = if (tabBar == 1 || tabBar == 2) PdfSettings.tabHeight.value else 0
        val tabBarHeightNow = if (MainUtil.hasTopTabBar()) PdfSettings.tabHeight.value else 0
        if (webViewActivity.savedFixTop == BrowserSettings.fixTop.value && webViewActivity.savedFixBot == BrowserSettings.fixBot.value && webViewActivity.savedUtilD3 == topBarSize && webViewActivity.savedUtilH0 == bottomBarHeight && webViewActivity.savedTabBar == BrowserSettings.tabBar.value && webViewActivity.savedTabAccent == BrowserSettings.tabAccent.value && webViewActivity.savedTabColor == BrowserSettings.tabColor.value && webViewActivity.dialogsState.savedTabClose == PdfSettings.tabClose.value && webViewActivity.savedTabWidth == PdfSettings.tabWidth.value && webViewActivity.savedTabHeight == savedPdfF && webViewActivity.savedPdfF2 == tabBarHeightNow) {
            if (barsChanged) {
                webViewActivity.refreshBarFrames()
                webViewActivity.layoutBars()
                webViewActivity.applyPageLayout()
                return
            }
            return
        }
        webViewActivity.exitFind(true)
        if (webViewActivity.savedTabBar != BrowserSettings.tabBar.value) {
            val myTouchFrame = webViewActivity.barsState.touchFrame
            if (myTouchFrame != null) {
                removeFromParent(myTouchFrame)
                webViewActivity.barsState.touchFrame = null
                webViewActivity.tabBarView = null
                val myButtonImage = webViewActivity.sideButton
                if (myButtonImage != null) {
                    myButtonImage.release()
                    webViewActivity.sideButton = null
                }
                val tabBarNow = webViewActivity.tabBar
                if (tabBarNow != null) {
                    tabBarNow.release()
                    webViewActivity.tabBar = null
                }
            }
            if (BrowserSettings.tabBar.value != 0) {
                addTabBar(themeColor, themeLight)
                bindTabBar(themeColor, themeLight)
                tintChanged = true
            }
            val myBarFrame: MyBarFrame? = if (!found && isDarkBars(webViewActivity.onStartPage)) webViewActivity.topBarFrame else null
            if (!found && isDarkBars(webViewActivity.onStartPage) && myBarFrame != null) {
                myBarFrame.reapplyRound()
            }
            layoutChanged = true
        } else {
            layoutChanged = false
        }
        if (webViewActivity.savedUtilD3 != topBarSize || webViewActivity.savedUtilH0 != bottomBarHeight || webViewActivity.savedTabHeight != savedPdfF || webViewActivity.savedPdfF2 != tabBarHeightNow) {
            webViewActivity.updateSplitLand(webViewActivity.isLandscapeScreen())
            webViewActivity.applyThemeColors(themeColor, themeLight, webViewActivity.navBarColorNow())
            barsChanged = true
        }
        if (barsChanged || layoutChanged) {
            webViewActivity.refreshBarFrames()
            if (barsChanged) {
                webViewActivity.layoutBars()
            }
        }
        webViewActivity.applyPageLayout()
        webViewActivity.updateBarBehavior()
        if (tintChanged) {
            webViewActivity.refreshTabBar(2, true)
        } else if ((webViewActivity.savedTabAccent != BrowserSettings.tabAccent.value || webViewActivity.savedTabColor != BrowserSettings.tabColor.value || webViewActivity.dialogsState.savedTabClose != PdfSettings.tabClose.value || webViewActivity.savedTabWidth != PdfSettings.tabWidth.value || webViewActivity.savedTabHeight != savedPdfF || webViewActivity.savedPdfF2 != tabBarHeightNow) && (webViewActivity.tabBar.also {
                webTabBarAdapter = it
            }) != null) {
            webTabBarAdapter!!.notifyAllTabs()
        }
        webViewActivity.savedFixTop = BrowserSettings.fixTop.value
        webViewActivity.savedFixBot = BrowserSettings.fixBot.value
        webViewActivity.savedUtilD3 = topBarSize
        webViewActivity.savedUtilH0 = bottomBarHeight
        webViewActivity.savedTabBar = BrowserSettings.tabBar.value
        webViewActivity.savedTabAccent = BrowserSettings.tabAccent.value
        webViewActivity.savedTabColor = BrowserSettings.tabColor.value
        webViewActivity.dialogsState.savedTabClose = PdfSettings.tabClose.value
        webViewActivity.savedTabWidth = PdfSettings.tabWidth.value
        webViewActivity.savedTabHeight = savedPdfF
        webViewActivity.savedPdfF2 = tabBarHeightNow
    }
}

fun WebViewActivity.closeUrlLinkDialog() {
    val dialogUrlLink = this.urlLinkDialog
    if (dialogUrlLink != null) {
        dialogUrlLink.dismiss()
        this.urlLinkDialog = null
    }
}

fun WebViewActivity.appLinkKind(url: String?): Int {
    if (if (TextUtils.isEmpty(url)) false else url!!.startsWith("market:")) {
        return 1
    }
    if (if (TextUtils.isEmpty(url)) false else url!!.startsWith("https://play.google.com/store/apps/details")) {
        return 2
    }
    return if (!this.dialogsState.onYouTube || (!ZtwoSettings.addPage.value && (ZtwoSettings.tabOpen.value and 2) != 2)) 0 else if (isYouTubeUrl(url)) 3 else 0
}

fun WebViewActivity.injectVideoRateScript(withRate: kotlin.Boolean, withSpeed: kotlin.Boolean) {
    if (this.currentPage == null) {
        return
    }
    this.miscState.rateScriptUrl = this.dialogsState.pageUrl
    val sb =
        StringBuilder("(function(){var ele=document.querySelector(\"video\");if(!ele)return;window.yspd=true;")
    if ((withRate == true)) {
        sb.append("ele.addEventListener(\"ratechange\",function(){android.onRateDet(this.playbackRate);});")
    }
    if ((withSpeed == true)) {
        sb.append("ele.playbackRate=")
        sb.append(ZtwoSettings.youSpeedRate.value)
        sb.append(";")
    }
    sb.append("})();")
    evalJs(this.currentPage, sb.toString(), false)
}

fun WebViewActivity.retryVideoRate(): kotlin.Boolean {
    if (checkVideoRate()) {
        return false
    }
    this.miscState.rateScriptUrl = null
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return false
    }
    myWebCoord.postDelayed(WvaRetryVideoRateTask(this), 1000L)
    return true
}

fun WebViewActivity.isBusy(): kotlin.Boolean {
    if (this.quickState.quickSearchImeAnimationBusy) {
        return true
    }
    val webVideoFrame = this.videoFrame
    if (webVideoFrame != null) {
        return webVideoFrame.isBusy() || this.videoState.pageFrameBusy || this.pageState.pageFrameListenerBusy || this.dialogsState.setSystemBarsHiddenBusy || this.dialogsState.applySystemBarsBusy || this.videoState.videoEnterBusy || this.videoState.videoExitBusy || this.dialogsState.unusedBusy || this.dialogsState.unusedBusy2
    }
    if (isFrameIdle() || this.isExiting || this.miscState.startPageSetupBusy || this.dialogsState.applySettingsChangeBusy || this.tabsState.goBackPageBusy || this.tabsState.startGoForwardBusy || this.findState.openInNextPageBusy || this.videoState.addTabImplBusy || this.tabsState.createBackTabBusy || this.tabsState.reloadTabViewsBusy || this.dialogsState.applyTabLimitBusy || this.pageState.rebuildTabsBusy || this.tabsState.switchToTabBusy || this.tabsState.exitAppBusy || this.tabsState.finishTabAnimBusy || this.findState.selectTabBusy || this.pageState.chromeClientBusy || this.dialogsState.showDeleteTabsDialogBusy || this.dialogsState.switchSecretModeBusy || this.videoState.webQuickListenerBusy || this.miscState.videoRateInjectBusy || this.videoState.pageFrameBusy || this.pageState.pageFrameListenerBusy || this.dialogsState.setSystemBarsHiddenBusy || this.dialogsState.applySystemBarsBusy || this.videoState.videoEnterBusy || this.videoState.videoExitBusy || this.dialogsState.unusedBusy || this.dialogsState.unusedBusy2) {
        return true
    }
    val dialogSetMsg = this.clearRecentDialog
    if (dialogSetMsg != null && dialogSetMsg.isShownNow()) {
        return true
    }
    val dialogDeleteItem = this.quickDeleteDialog
    if (dialogDeleteItem != null && dialogDeleteItem.isShownNow()) {
        return true
    }
    val dialogQuickEdit = this.dialogsState.quickEditDialog
    if (dialogQuickEdit != null && dialogQuickEdit.isShownNow()) {
        return true
    }
    val deleteItemDialog = this.deleteItemDialog
    if (deleteItemDialog != null && deleteItemDialog.isShownNow()) {
        return true
    }
    val tabGroupDeleteDialog = this.tabGroupDeleteDialog
    if (tabGroupDeleteDialog != null && tabGroupDeleteDialog.isShownNow()) {
        return true
    }
    val dialogTabEdit = this.dialogsState.tabEditDialog
    if (dialogTabEdit != null && dialogTabEdit.isShownNow()) {
        return true
    }
    val dialogSetRate = this.dialogsState.setRateDialog
    if (dialogSetRate != null && dialogSetRate.isShownNow()) {
        return true
    }
    val dialogTabMenu = this.tabMenuDialog
    if (dialogTabMenu != null && dialogTabMenu.isShownNow()) {
        return true
    }
    val dialogNewsLocale = this.newsLocaleDialog
    if (dialogNewsLocale != null && dialogNewsLocale.isShownNow()) {
        return true
    }
    val dialogNewsSearch = this.newsSearchDialog
    if (dialogNewsSearch != null && dialogNewsSearch.isShownNow()) {
        return true
    }
    val dialogConfirm = this.dialogsState.pathsResetDialog
    if (dialogConfirm != null && dialogConfirm.isShownNow()) {
        return true
    }
    val dialogWebBookEdit = this.dialogsState.bookEditDialog
    if (dialogWebBookEdit != null && dialogWebBookEdit.isShownNow()) {
        return true
    }
    val dialogWebBookList = this.bookmarksDialog
    if (dialogWebBookList != null && dialogWebBookList.hasPopup()) {
        return true
    }
    val dialogListBook = this.historyDialog
    if (dialogListBook != null && dialogListBook.hasPopup()) {
        return true
    }
    val dialogEditShort = this.editShortDialog
    if (dialogEditShort != null && dialogEditShort.isShownNow()) {
        return true
    }
    val dialogUrlLink = this.urlLinkDialog
    if (dialogUrlLink != null && dialogUrlLink.isShownNow()) {
        return true
    }
    val dialogGuideNoti = this.dialogsState.guideNotiDialog
    if (dialogGuideNoti != null && dialogGuideNoti.isShownNow()) {
        return true
    }
    val downGuideDialog = this.downGuideDialog
    if (downGuideDialog != null && downGuideDialog.isShownNow()) {
        return true
    }
    val dialogDownUrl = this.videoState.downUrlDialog
    if (dialogDownUrl != null && dialogDownUrl.isShownNow()) {
        return true
    }
    val dialogSetDown = this.setDownDialog
    if (dialogSetDown != null && dialogSetDown.isShownNow()) {
        return true
    }
    val dialogDownBlob = this.dialogsState.downBlobDialog
    if (dialogDownBlob != null && dialogDownBlob.isShownNow()) {
        return true
    }
    val captureSheet = this.captureModeSheet
    if (captureSheet != null && captureSheet.isShownNow()) {
        return true
    }
    val dialogCapture = this.captureDialog
    if (dialogCapture != null && dialogCapture.isOpen()) {
        return true
    }
    val dialogDownPage = this.downPageDialog
    if (dialogDownPage != null && dialogDownPage.isShownNow()) {
        return true
    }
    val dialogPrintPage = this.printPageDialog
    if (dialogPrintPage != null && dialogPrintPage.isShownNow()) {
        return true
    }
    val dialogSetTrans = this.setTransDialog
    if (dialogSetTrans != null && dialogSetTrans.isShownNow()) {
        return true
    }
    val dialogTransLang = this.transLangDialog
    if (dialogTransLang != null && dialogTransLang.isShownNow()) {
        return true
    }
    val dialogTransMsg = this.transMsgDialog
    if (dialogTransMsg != null && dialogTransMsg.isShownNow()) {
        return true
    }
    val dialogViewTrans = this.viewTransDialog
    if (dialogViewTrans != null && dialogViewTrans.isShownNow()) {
        return true
    }
    val dialogSetDesk = this.dialogsState.setDeskDialog
    if (dialogSetDesk != null && dialogSetDesk.isShownNow()) {
        return true
    }
    val dialogSetUrl = this.tabsState.setUrlDialog
    if (dialogSetUrl != null && dialogSetUrl.isShownNow()) {
        return true
    }
    val dialogOpenType = this.openTypeDialog
    if (dialogOpenType != null && dialogOpenType.isShownNow()) {
        return true
    }
    val dialogPopupMenu = this.popupMenuDialog
    if (dialogPopupMenu != null && dialogPopupMenu.isShownNow()) {
        return true
    }
    val dialogWebView = this.webViewDialog
    if (dialogWebView != null && dialogWebView.isShownNow()) {
        return true
    }
    val dialogAllowPopup = this.allowPopupDialog
    if (dialogAllowPopup != null && dialogAllowPopup.isShownNow()) {
        return true
    }
    val dialogBlockLink = this.blockLinkDialog
    if (dialogBlockLink != null && dialogBlockLink.isShownNow()) {
        return true
    }
    val dialogBlockImage = this.dialogsState.blockImageDialog
    if (dialogBlockImage != null && dialogBlockImage.isShownNow()) {
        return true
    }
    val dialogGuideArea = this.dialogsState.guideAreaDialog
    if (dialogGuideArea != null && dialogGuideArea.isShownNow()) {
        return true
    }
    val guideAreaDialog = this.guideAreaDialog
    if (guideAreaDialog != null && guideAreaDialog.isShownNow()) {
        return true
    }
    val dialogEditArea = this.editAreaDialog
    if (dialogEditArea != null && dialogEditArea.isShownNow()) {
        return true
    }
    val jsMessageDialog = this.jsMessageDialog
    if (jsMessageDialog != null && jsMessageDialog.isShownNow()) {
        return true
    }
    val dialogPrompt = this.promptDialog
    if (dialogPrompt != null && dialogPrompt.isShownNow()) {
        return true
    }
    val dialogEditAuth = this.editAuthDialog
    if (dialogEditAuth != null && dialogEditAuth.isShownNow()) {
        return true
    }
    val dialogPermission = this.permissionDialog
    if (dialogPermission != null && dialogPermission.isShownNow()) {
        return true
    }
    val dialogPassInfo = this.passInfoDialog
    if (dialogPassInfo != null && dialogPassInfo.isShownNow()) {
        return true
    }
    val dialogWebSelect = this.webSelectDialog
    if (dialogWebSelect != null && dialogWebSelect.isShownNow()) {
        return true
    }
    val dialogMenuMain = this.menuMainDialog
    if (dialogMenuMain != null && dialogMenuMain.isShownNow()) {
        return true
    }
    val dialogLoadImg = this.loadImgDialog
    if (dialogLoadImg != null && dialogLoadImg.isShownNow()) {
        return true
    }
    val dialogLoadEmg = this.loadEmgDialog
    if (dialogLoadEmg != null && dialogLoadEmg.isShownNow()) {
        return true
    }
    val dialogLoadHmg = this.loadHmgDialog
    if (dialogLoadHmg != null && dialogLoadHmg.isShownNow()) {
        return true
    }
    val webGridDialog = this.dialogsState.gridDialog
    if (webGridDialog != null && webGridDialog.hasPopup()) {
        return true
    }
    val siteDlg = this.dialogsState.siteDialog
    if (siteDlg != null && siteDlg.hasPopup()) {
        return true
    }
    val dialogVideoList = this.videoListDialog
    if (dialogVideoList != null && dialogVideoList.isShownNow()) {
        return true
    }
    val dialogDownFont = this.dialogsState.downFontDialog
    if (dialogDownFont != null && dialogDownFont.isShownNow()) {
        return true
    }
    val dialogPreview = this.previewDialog
    if (dialogPreview != null && dialogPreview.isShowingNow()) {
        return true
    }
    val dialogPreImage = this.preImageDialog
    if (dialogPreImage != null && dialogPreImage.isShownNow()) {
        return true
    }
    val dialogSeekBright = this.seekBrightDialog
    if (dialogSeekBright != null && dialogSeekBright.isShownNow()) {
        return true
    }
    val dialogSeekWebText = this.seekWebTextDialog
    if (dialogSeekWebText != null && dialogSeekWebText.isShownNow()) {
        return true
    }
    val dialogSetFull = this.setFullDialog
    if (dialogSetFull != null && dialogSetFull.isShownNow()) {
        return true
    }
    val dialogSetDark = this.dialogsState.setDarkDialog
    if (dialogSetDark != null && dialogSetDark.isShownNow()) {
        return true
    }
    val darkConfirmDialog = this.darkConfirmDialog
    if (darkConfirmDialog != null && darkConfirmDialog.isShownNow()) {
        return true
    }
    val dialogSetUseTts = this.dialogsState.setUseTtsDialog
    if (dialogSetUseTts != null && dialogSetUseTts.isShownNow()) {
        return true
    }
    val dialogSetAdblock = this.setAdblockDialog
    if (dialogSetAdblock != null && dialogSetAdblock.isShownNow()) {
        return true
    }
    val dialogSetJava = this.setJavaDialog
    if (dialogSetJava != null && dialogSetJava.isShownNow()) {
        return true
    }
    val dialogSetTmem = this.setTmemDialog
    if (dialogSetTmem != null && dialogSetTmem.isShownNow()) {
        return true
    }
    val dialogViewSrc = this.dialogsState.viewSrcDialog
    if (dialogViewSrc != null && dialogViewSrc.isOpen()) {
        return true
    }
    val dialogSetPrivacy = this.dialogsState.setPrivacyDialog
    if (dialogSetPrivacy != null && dialogSetPrivacy.isShownNow()) {
        return true
    }
    val dialogSetCookie = this.dialogsState.setCookieDialog
    if (dialogSetCookie != null && dialogSetCookie.isShownNow()) {
        return true
    }
    val dialogSetScrFil = this.dialogsState.setScrFilDialog
    if (dialogSetScrFil != null && dialogSetScrFil.isShownNow()) {
        return true
    }
    val dialogWebCerti = this.dialogsState.certDialog
    if (dialogWebCerti != null && dialogWebCerti.isShownNow()) {
        return true
    }
    val restartGuideDialog = this.dialogsState.restartGuideDialog
    if (restartGuideDialog != null && restartGuideDialog.isShownNow()) {
        return true
    }
    val restartNoticeDialog = this.dialogsState.restartNoticeDialog
    if (restartNoticeDialog != null && restartNoticeDialog.isShownNow()) {
        return true
    }
    val scriptInstallDialog = this.dialogsState.scriptInstallDialog
    if (scriptInstallDialog != null && scriptInstallDialog.isShownNow()) {
        return true
    }
    val dialogUpdateScript = this.dialogsState.updateScriptDialog
    return dialogUpdateScript != null && dialogUpdateScript.isShownNow()
}

fun WebViewActivity.checkPendingDownloads() {
    val mainAppO = MainApp.from(this.dialogsState.appContext)
    var found = false
    if (if (mainAppO == null) false else mainAppO.downBinding) {
        clearDownCheckFlag()
        return
    }
    val context = this.dialogsState.appContext
    if (context != null) {
        var cursorG: Cursor? = null
        try {
            cursorG = DbUtil.query(
                DbBookDown.get(context).getWritableDatabase(),
                "DbBookDown_table",
                arrayOf<String?>("_status"),
                "_status!=?",
                arrayOf<String?>("3"),
                null
            )
            if (cursorG != null && cursorG.moveToFirst()) {
                val columnIndex = cursorG.getColumnIndex("_status")
                do {
                    val int = cursorG.getInt(columnIndex)
                    if (int != 3 && int != 4 && int != 5) {
                        found = true
                        break
                    }
                } while (cursorG.moveToNext())
            }
        } catch (unused: Exception) {
        }
        if (cursorG != null) {
            try {
                cursorG.close()
            } catch (unused2: Exception) {
            }
        }
    }
    if (!found) {
        clearDownCheckFlag()
        return
    }
    if (this.isPaused) {
        return
    }
    clearDownCheckFlag()
    val mainApp = MainApp.from(getApplicationContext())
    if (mainApp == null || mainApp.downService != null || mainApp.downStarting) {
        return
    }
    mainApp.downStarting = true
    mainApp.pendingRefresh = true
    mainApp.bindDownService()
}

fun WebViewActivity.checkVideoPage(pageUrl: String?) {
    var videoRateOff = false
    if (!this.dialogsState.onYouTube) {
        this.dialogsState.isVideoPage = false
        this.dialogsState.isVideoHost = false
        this.dialogsState.videoRateOff = false
        this.dialogsState.videoPageUrl = null
        this.videoState.videoCheckUrl = null
        this.miscState.rateScriptUrl = null
        return
    }
    this.dialogsState.isVideoPage = isYouTubeWatch(pageUrl)
    this.dialogsState.isVideoHost = isYouTubeShorts(pageUrl)
    val length = if (pageUrl != null) pageUrl.length else 0
    val hashIndex = if (pageUrl != null && length != 0) pageUrl.lastIndexOf(35.toChar()) else 0
    if (pageUrl != null && length != 0 && hashIndex > 0 && hashIndex < length - 1) {
        videoRateOff = true
    }
    this.dialogsState.videoRateOff = videoRateOff
    val videoPageUrl = this.dialogsState.videoPageUrl
    if (this.dialogsState.isVideoPage && !this.dialogsState.isVideoHost) {
        this.dialogsState.videoPageUrl = youTubeIdOf(pageUrl)
    } else {
        this.dialogsState.videoPageUrl = null
    }
    if (sameObject(this.dialogsState.videoPageUrl, videoPageUrl)) {
        return
    }
    this.miscState.rateScriptUrl = null
}

fun WebViewActivity.closeBookmarksDialog() {
    resumePausedPage()
    val dialogWebBookList = this.bookmarksDialog
    if (dialogWebBookList != null) {
        dialogWebBookList.dismiss()
        this.bookmarksDialog = null
    }
}

fun WebViewActivity.checkPageLanguage(force: kotlin.Boolean) {
    if ((force == true)) {
        this.dialogsState.langCheckTries = 10
    }
    if (this.dialogsState.langCheckBusy) {
        return
    }
    this.dialogsState.langCheckBusy = true
    val langCheckTries = this.dialogsState.langCheckTries
    if (langCheckTries <= 0) {
        this.dialogsState.langCheckBusy = false
        return
    }
    this.dialogsState.langCheckTries = langCheckTries - 1
    if (!this.onStartPage && (TtsSettings.ttsMode.value || shouldAutoTranslate(false))) {
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.postDelayed(WvaLanguageCheckScript(this), 200L)
        return
    }
    this.speechState.ttsLang = null
    if (this.autoTransPending) {
        this.autoTransPending = false
        postTransIconUpdate()
    }
    this.dialogsState.langCheckTries = 0
    this.dialogsState.langCheckBusy = false
}

fun WebViewActivity.runVideoAction(action: Int) {
    if (this.currentPage != null) {
        if (action != 3) {
            if (!isYouTubeWatch(this.dialogsState.pageUrl)) {
                showToast(this, AppR.string.not_support_video)
                return
            } else {
                val pageUrl = this.dialogsState.pageUrl
                playVideoInPlayer(pageUrl, pageUrl, true)
                return
            }
        }
        if (this.isClosing || isAnyDialogOpen()) {
            return
        }
        val dialogSetRate = this.dialogsState.setRateDialog
        if (dialogSetRate != null) {
            dialogSetRate.dismiss()
            this.dialogsState.setRateDialog = null
        }
        this.dialogsState.lastYouSpeedSave = ZtwoSettings.youSpeedSave.value
        var rateIndex = -1
        if (hasVideoButton() && ZtwoSettings.youSpeedSave.value) {
            var index = 0
            while (true) {
                val speeds: FloatArray = WebVideoFull.Companion.SPEEDS
                if (index >= 8) {
                    break
                }
                if ((ZtwoSettings.youSpeedRate.value).compareTo(speeds[index]) == 0) {
                    rateIndex = index
                    break
                }
                index++
            }
        } else {
            val myWebCoord = this.webCoord
            if (myWebCoord == null) {
                return
            } else {
                myWebCoord.postDelayed(VideoRateQuery(this), 100L)
            }
        }
        val setRateDialog = DialogSetRate(this, rateIndex, WvaRunVideoActionListener(this))
        this.dialogsState.setRateDialog = setRateDialog
        setRateDialog.setOnDismissListener(WvaSetRateDialogDismissed(this))
    }
}

fun WebViewActivity.closeWebViewDialog() {
    val dialogWebView = this.webViewDialog
    if (dialogWebView != null) {
        dialogWebView.dismiss()
        this.webViewDialog = null
    }
}

fun WebViewActivity.checkSavePassword() {
    if (PassManager.saving) {
        val savePassHostNow = this.dialogsState.savePassHost
        val savePassUserNow = this.dialogsState.savePassUser
        val savePassWordNow = this.dialogsState.savePassWord
        val savePassUrlNow = this.dialogsState.savePassUrl
        this.dialogsState.savePassHost = null
        this.dialogsState.savePassUser = null
        this.dialogsState.savePassWord = null
        this.dialogsState.savePassUrl = null
        if (this.dialogsState.isVideoPage || TextUtils.isEmpty(this.dialogsState.pageUrl) || "file:///android_asset/shortcut.html" == this.dialogsState.pageUrl || "about:blank" == this.dialogsState.pageUrl) {
            return
        }
        if (!TextUtils.isEmpty(savePassHostNow) && !TextUtils.isEmpty(savePassUserNow) && !TextUtils.isEmpty(savePassWordNow)) {
            runInBackground(WvaShowPassInfoDialog(this, savePassHost = savePassHostNow, savePassUser = savePassUserNow, savePassWord = savePassWordNow, savePassUrl = savePassUrlNow))
            return
        }
        if (isXLoginPage(this.dialogsState.pageUrl, this.dialogsState.pageHost)) {
            this.dialogsState.keyboardRelayout = true
            if (ZtriSettings.passIcon.value) {
                runInBackground(WvaCheckSiteLock(this))
                return
            }
            return
        }
        val myWebBody: MyWebBody? = if (this.currentPage == null) null else this.dialogsState.webBody
        if (this.currentPage == null || myWebBody == null) {
            return
        }
        myWebBody.postDelayed(WvaCheckPasswordField(this), 400L)
    }
}

fun WebViewActivity.updateTransFloatButton(visible: kotlin.Boolean) {
    if (this.transFloatButton == null) {
        return
    }
    if ((visible == true)) {
        if ((if (isTranslateActive() && this.dialogsState.transState.toInt() == 3) this.dialogsState.transReady else false) && !this.quickState.editingAddress && !this.keyboardOpen && this.dialogsState.tabUndoSnackbar == null && this.dialogsState.popupBlockSnackbar == null && this.dialogsState.appBlockSnackbar == null && this.snackbar == null && !SyncSettings.splash.value) {
            this.transFloatButton!!.setVisibility(View.VISIBLE)
            return
        }
    }
    this.transFloatButton!!.setVisibility(View.GONE)
}

fun WebViewActivity.isAnyDialogOpen(): kotlin.Boolean {
    if (this.clearRecentDialog != null || this.quickDeleteDialog != null || this.dialogsState.quickEditDialog != null || this.quickState.quickSubView != null || this.deleteItemDialog != null || this.tabGroupDeleteDialog != null || this.dialogsState.tabEditDialog != null || this.videoMenuDialog != null || this.dialogsState.setRateDialog != null || this.tabMenuDialog != null || this.newsMenuDialog != null || this.newsLocaleDialog != null || this.newsSearchDialog != null || this.dialogsState.pathsResetDialog != null || this.dialogsState.bookEditDialog != null || this.bookmarksDialog != null || this.historyDialog != null || this.editShortDialog != null || this.urlLinkDialog != null || this.dialogsState.guideNotiDialog != null || this.downGuideDialog != null || this.videoState.downUrlDialog != null || this.setDownDialog != null || this.dialogsState.downBlobDialog != null || this.captureModeSheet != null || this.captureDialog != null || this.downPageDialog != null || this.printPageDialog != null || this.setTransDialog != null || this.transLangDialog != null || this.transMsgDialog != null || this.viewTransDialog != null || this.dialogsState.setDeskDialog != null || this.tabsState.setUrlDialog != null || this.openTypeDialog != null || this.popupMenuDialog != null || this.webViewDialog != null || this.allowPopupDialog != null || this.blockLinkDialog != null || this.dialogsState.blockImageDialog != null || this.dialogsState.guideAreaDialog != null || this.guideAreaDialog != null || this.editAreaDialog != null || this.jsMessageDialog != null || this.promptDialog != null || this.editAuthDialog != null || this.permissionDialog != null || this.passInfoDialog != null || this.webSelectDialog != null || this.menuMainDialog != null || this.dialogsState.menuListDialog != null || this.tabsState.composeOverview != null || this.loadImgDialog != null || this.loadEmgDialog != null || this.loadHmgDialog != null || this.dialogsState.gridDialog != null || this.videoListDialog != null || this.dialogsState.downFontDialog != null || this.previewDialog != null || this.preImageDialog != null || this.seekBrightDialog != null || this.seekWebTextDialog != null || this.setFullDialog != null || this.dialogsState.setDarkDialog != null || this.darkConfirmDialog != null || this.dialogsState.setUseTtsDialog != null || this.setAdblockDialog != null || this.setJavaDialog != null || this.setTmemDialog != null || isReaderOpen() || this.dialogsState.viewSrcDialog != null || this.dialogsState.setPrivacyDialog != null || this.dialogsState.setCookieDialog != null || this.dialogsState.setScrFilDialog != null || this.dialogsState.certDialog != null || this.dialogsState.restartGuideDialog != null || this.dialogsState.restartNoticeDialog != null || this.dialogsState.scriptInstallDialog != null || this.dialogsState.updateScriptDialog != null || this.tabBarSubView != null || this.transControl != null
    ) {
        return true
    }
    val webVideoFrame = this.videoFrame
    return webVideoFrame != null && webVideoFrame.hasSeekDialog()
}

fun WebViewActivity.openLoadDialog(mode: Int) {
    if (mode == 0) {
        showLoadImgDialog(this.dialogsState.pageUrl, false)
        return
    }
    if (mode == 1) {
        showLoadHmgDialog(this.dialogsState.pageUrl, false)
    } else if (mode == 2) {
        showLoadEmgDialog(this.dialogsState.pageUrl, false)
    } else if (mode == 3) {
        showLoadEmgDialog(this.dialogsState.pageUrl, false)
    }
}

fun WebViewActivity.applySnsFixes() {
    var urlLength: Int = 0
    var sepIndex: Int = 0
    var audioIndex: Int = 0
    val pageChanged = !sameObject(this.dialogsState.snsFixUrl, this.dialogsState.pageUrl)
    val pageHost = this.dialogsState.pageHost
    var arrayList: ArrayList<Any?>? = null
    arrayList = null
    var faceParts: ArrayList<Any?>? = null
    arrayList = null
    var instaParts: ArrayList<Any?>? = null
    arrayList = null
    var twitParts: ArrayList<Any?>? = null
    arrayList = null
    var redditParts: ArrayList<Any?>? = null
    arrayList = null
    var tiktokParts: ArrayList<Any?>? = null
    arrayList = null
    this.dialogsState.snsFixUrl = null
    this.dialogsState.videoQualitiesFound = false
    this.dialogsState.snsFixBusy = false
    this.dialogsState.vkvListListenerList = null
    if (this.onStartPage || TextUtils.isEmpty(this.dialogsState.pageUrl) || "about:blank" == this.dialogsState.pageUrl || TextUtils.isEmpty(
            pageHost
        ) || isYouTubeHost(pageHost)
    ) {
        return
    }
    if (pageHost!!.endsWith("facebook.com")) {
        this.dialogsState.snsFixBusy = true
        val webNestView = this.currentPage
        if (webNestView != null) {
            if (!partsReady(17, MainNative.snsFixParts1)) {
                val newFaceParts: ArrayList<Any?> = ArrayList<Any?>()
                var index = 0
                while (true) {
                    if (index >= 17) {
                        MainNative.snsFixParts1 = newFaceParts
                        faceParts = newFaceParts
                        break
                    } else {
                        val faceJs = MainUtil.getFaceJs(index)
                        if (TextUtils.isEmpty(faceJs)) {
                            break
                        }
                        newFaceParts.add(faceJs)
                        index++
                    }
                }
            } else {
                faceParts = MainNative.snsFixParts1
            }
            if (faceParts != null && faceParts.size == 17) {
                val sb = StringBuilder()
                var index = 0
                while (true) {
                    if (index >= 17) {
                        evalJs(webNestView, sb.toString(), true)
                        break
                    }
                    val partText = faceParts.get(index) as String?
                    if (TextUtils.isEmpty(partText)) {
                        break
                    }
                    sb.append(partText)
                    index++
                }
            }
        }
        val pageUrl = this.dialogsState.pageUrl
        val handler: Handler? = if (!(if (TextUtils.isEmpty(pageUrl)) false else pageUrl!!.startsWith("https://m.facebook.com/reel"))) null else this.mainHandler
        if (!(if (TextUtils.isEmpty(pageUrl)) false else pageUrl!!.startsWith("https://m.facebook.com/reel")) || handler == null) {
            return
        }
        handler.post(FacebookVideoScan(this))
        return
    }
    if (pageHost.endsWith("instagram.com")) {
        val pageUrl = this.dialogsState.pageUrl
        val host = MainUtil.hostOf(pageUrl, true)
        val length = if (host != null) host.length else 0
        val index = if (host != null && length != 0) (length + 1) else 0
        if (host != null && length != 0 && index < (pageUrl!!.length.also {
                urlLength = it
            }) && (pageUrl.indexOf(47.toChar(), index).also {
                sepIndex = it
            }) != -1 && sepIndex + 1 != urlLength && pageUrl.startsWith(
                "/reels/",
                length
            ) && ((length + 7).also { audioIndex = it }) < urlLength && !pageUrl.startsWith("audio/", audioIndex)) {
            val handler = this.mainHandler
            if (handler != null) {
                handler.post(InstagramVideoScan(this))
                return
            }
            return
        }
        this.dialogsState.snsFixBusy = true
        val webNestView = this.currentPage
        if (webNestView == null) {
            return
        }
        if (!partsReady(21, MainNative.snsFixParts2)) {
            val newInstaParts: ArrayList<Any?> = ArrayList<Any?>()
            var partIndex = 0
            while (true) {
                if (partIndex >= 21) {
                    MainNative.snsFixParts2 = newInstaParts
                    instaParts = newInstaParts
                    break
                } else {
                    val instaJs = MainUtil.getInstaJs(partIndex)
                    if (TextUtils.isEmpty(instaJs)) {
                        break
                    }
                    newInstaParts.add(instaJs)
                    partIndex++
                }
            }
        } else {
            instaParts = MainNative.snsFixParts2
        }
        if (instaParts == null || instaParts.size != 21) {
            return
        }
        val sb = StringBuilder()
        for (index2 in 0..20) {
            val part = instaParts.get(index2) as String?
            if (TextUtils.isEmpty(part)) {
                return
            }
            sb.append(part)
        }
        evalJs(webNestView, sb.toString(), true)
        return
    }
    if (pageHost == "x.com") {
        val pageUrl = this.dialogsState.pageUrl
        if (if (TextUtils.isEmpty(pageUrl)) false else pageUrl!!.contains("/mediaViewer?currentTweet=")) {
            val handler = this.mainHandler
            if (handler == null) {
                return
            } else {
                handler.post(TwitterVideoScan(this))
            }
        }
        this.dialogsState.snsFixBusy = true
        val webNestView = this.currentPage
        val context = this.dialogsState.appContext
        if (webNestView != null && context != null) {
            if (!partsReady(16, MainNative.snsFixParts3)) {
                val newTwitParts: ArrayList<Any?> = ArrayList<Any?>()
                var index = 0
                while (true) {
                    if (index >= 16) {
                        MainNative.snsFixParts3 = newTwitParts
                        twitParts = newTwitParts
                        break
                    } else {
                        val twitJs = MainUtil.getTwitJs(index)
                        if (TextUtils.isEmpty(twitJs)) {
                            break
                        }
                        newTwitParts.add(twitJs)
                        index++
                    }
                }
            } else {
                twitParts = MainNative.snsFixParts3
            }
            if (twitParts != null && twitParts.size == 16) {
                // parts 0..5, the localised "Download" label, parts 6..15; injected once
                run build@{
                    val sb = StringBuilder()
                    for (part in 0 until 6) {
                        val js = twitParts.get(part) as String?
                        if (TextUtils.isEmpty(js)) return@build
                        sb.append(js)
                    }
                    sb.append(context.getString(AppR.string.download))
                    for (part in 6 until 16) {
                        val js = twitParts.get(part) as String?
                        if (TextUtils.isEmpty(js)) return@build
                        sb.append(js)
                    }
                    evalJs(webNestView, sb.toString(), true)
                }
            }
        }
        val handler = this.mainHandler
        if (handler != null) {
            handler.postDelayed(WvaXcomSnsScript(this), 1000L)
            return
        }
        return
    }
    if (pageHost.endsWith("reddit.com")) {
        this.dialogsState.snsFixBusy = true
        val webNestView = this.currentPage
        if (webNestView == null) {
            return
        }
        if (!partsReady(22, MainNative.snsFixParts4)) {
            val newRedditParts: ArrayList<Any?> = ArrayList<Any?>()
            var index = 0
            while (true) {
                if (index >= 22) {
                    MainNative.snsFixParts4 = newRedditParts
                    redditParts = newRedditParts
                    break
                } else {
                    val redditJs = MainUtil.getRedditJs(index)
                    if (TextUtils.isEmpty(redditJs)) {
                        break
                    }
                    newRedditParts.add(redditJs)
                    index++
                }
            }
        } else {
            redditParts = MainNative.snsFixParts4
        }
        if (redditParts == null || redditParts.size != 22) {
            return
        }
        val sb = StringBuilder()
        for (index in 0..21) {
            val part = redditParts.get(index) as String?
            if (TextUtils.isEmpty(part)) {
                return
            }
            sb.append(part)
        }
        evalJs(webNestView, sb.toString(), true)
        return
    }
    if (pageHost.endsWith("tiktok.com")) {
        this.dialogsState.snsFixBusy = true
        val webNestView = this.currentPage
        if (webNestView == null) {
            return
        }
        if (!partsReady(9, MainNative.snsFixParts5)) {
            val newTiktokParts: ArrayList<Any?> = ArrayList<Any?>()
            var index = 0
            while (true) {
                if (index >= 9) {
                    MainNative.snsFixParts5 = newTiktokParts
                    tiktokParts = newTiktokParts
                    break
                } else {
                    val tiktokJs = MainUtil.getTiktokJs(index)
                    if (TextUtils.isEmpty(tiktokJs)) {
                        break
                    }
                    newTiktokParts.add(tiktokJs)
                    index++
                }
            }
        } else {
            tiktokParts = MainNative.snsFixParts5
        }
        if (tiktokParts == null || tiktokParts.size != 9) {
            return
        }
        val sb = StringBuilder()
        for (index in 0..8) {
            val part = tiktokParts.get(index) as String?
            if (TextUtils.isEmpty(part)) {
                return
            }
            sb.append(part)
        }
        evalJs(webNestView, sb.toString(), true)
        return
    }
    if (!pageHost.endsWith("onlyfans.com")) {
        if (!BrowserSettings.enableJs.value || this.currentPage == null) {
            return
        }
        if (this.dialogsState.chromeClientBusy) {
            this.dialogsState.snsFixUrl = this.dialogsState.pageUrl
            injectVideoDetect(true, pageChanged)
            return
        }
        val isPornhub = pageHost.endsWith("pornhub.com")
        val pageUrl = this.dialogsState.pageUrl
        if (isPornhub) {
            this.dialogsState.snsFixUrl = pageUrl
            injectVideoObserver(
                this.currentPage,
                StringBuilder().append("function myVd(doc,win){if(!doc||!win)return false;var eles=doc.querySelectorAll(\"div[id='mobileContainer']>script[type='text/javascript']\");if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){var val=eles[i].innerHTML;if(val&&val.includes('mediaDefinitions')){android.onVidDet(1);return true;}}}eles=doc.querySelectorAll(\"div[id='js-gifWebMWrapper'][data-gif^='http']\");if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){if(eles[i].dataset.gif){android.onVidDet(2);return true;}}}return false;}"),
                pageChanged
            )
            return
        }
        if (isVkVideoPage(pageHost, pageUrl)) {
            val handler = this.mainHandler
            if (handler != null) {
                handler.post(VkVideoScan(this))
                return
            }
            return
        }
        if (isGoogleHome(pageHost) || pageHost.endsWith("vimeo.com") || pageHost.endsWith("dailymotion.com") || pageHost.endsWith(
                "nicovideo.jp"
            ) || pageHost.endsWith("dzen.ru") || pageHost.endsWith("giphy.com")
        ) {
            return
        }
        this.dialogsState.snsFixUrl = this.dialogsState.pageUrl
        injectVideoDetect(false, pageChanged)
        return
    }
    this.dialogsState.snsFixBusy = true
    val webNestView = this.currentPage
    val context = this.dialogsState.appContext
    if (webNestView == null || context == null) {
        return
    }
    if (!partsReady(16, MainNative.snsFixParts6)) {
        val newFansParts: ArrayList<Any?> = ArrayList<Any?>()
        var index = 0
        while (true) {
            if (index >= 16) {
                MainNative.snsFixParts6 = newFansParts
                arrayList = newFansParts
                break
            } else {
                val fansJs = MainUtil.getFansJs(index)
                if (TextUtils.isEmpty(fansJs)) {
                    break
                }
                newFansParts.add(fansJs)
                index++
            }
        }
    } else {
        arrayList = MainNative.snsFixParts6
    }
    if (arrayList == null || arrayList.size != 16) {
        return
    }
    val sb = StringBuilder()
    for (index in 0..5) {
        val part = arrayList.get(index) as String?
        if (TextUtils.isEmpty(part)) {
            return
        }
        sb.append(part)
    }
    sb.append(context.getString(AppR.string.download))
    for (index in 6 until 16) {
        val part = arrayList.get(index) as String?
        if (TextUtils.isEmpty(part)) {
            return
        }
        sb.append(part)
    }
    evalJs(webNestView, sb.toString(), true)
}

fun WebViewActivity.updateUpButton(visible: kotlin.Boolean) {
    var quickView: QuickView? = null
    val webUpView = this.upButton
    val webNestView: WebNestView? = if (webUpView == null) null else this.currentPage
    if (webUpView == null || webNestView == null) {
        return
    }
    if (!visible || ZoneSettings.showUpPos.value == 0 || this.floatingState.upButtonHidden || this.quickState.editingAddress || this.keyboardOpen || this.dialogsState.tabUndoSnackbar != null || this.dialogsState.popupBlockSnackbar != null || this.dialogsState.appBlockSnackbar != null || this.snackbar != null || SyncSettings.splash.value) {
        webUpView.hide()
        return
    }
    if (if (!this.onStartPage) webNestView.getScrollY() <= this.dialogsState.webBody!!.rectBottom else ((webNestView.quickPanel.also {
            quickView = it
        }) == null || !quickView!!.onStartPage || quickView.scrollState <= (quickView.getHeight() shl 1))) {
        this.upButton!!.hide()
    } else {
        showUpButton()
    }
}

fun WebViewActivity.pageVisibleHeight(): Int {
    val height = (this.currentPage!!.getHeight() - MainUtil.topBarHeight()) - MainUtil.bottomInsetTotal()
    if (height < 0) {
        return 0
    }
    return height
}

fun WebViewActivity.applyVideoOrientation(lock: kotlin.Boolean) {
    val webVideoFrame = this.videoFrame
    if (webVideoFrame != null && (this.videoState.videoDialogOpen || webVideoFrame.isDialogOpen())) {
        applyScreenRotation(this, lock)
        return
    }
    val dialogCapture = this.captureDialog
    if (dialogCapture == null || !dialogCapture.editShown) {
        return
    }
    applyScreenRotation(this, lock)
}

fun WebViewActivity.applyBodyColors(themeColor: Int, filterTint: Int) {
    var themeColor = themeColor
    var myWebBody: MyWebBody? = null
    var darkBody = false
    var found = false
    var sdkInt = 0
    var barShade = 0
    var navBarColor = 0
    var myWebCoord: MyWebCoord? = null
    this.dialogsState.colorsForStart = this.onStartPage
    if (this.videoFrame != null || (this.dialogsState.webBody.also { myWebBody = it }) == null) {
        return
    }
    var tinted = false
    if (MainConst.isApi30) {
        myWebBody!!.setFilterColor(filterTint)
    } else {
        val newThemeColor = if (!TtsSettings.handMode.value) 0 else themeColor
        if (myWebBody!!.themeColor != newThemeColor || myWebBody.filterTint != filterTint) {
            myWebBody.themeColor = newThemeColor
            myWebBody.filterTint = filterTint
            myWebBody.invalidate()
        }
    }
    tintHandModeBar(themeColor)
    var darkPage = MainUtil.isDarkPage(this.onStartPage)
    val darkWindow = useDarkWindow()
    val barsDisabledMode = this.tabsState.barsDisabledMode
    if (barsDisabledMode.toInt() == 1) {
        darkBody = darkPage
    } else {
        if (barsDisabledMode.toInt() != 2) {
            if (darkWindow) {
                darkBody = darkPage || BrowserSettings.darkHome.value
                found = true
            } else {
                darkBody = darkPage || !(BrowserSettings.headMode() == 0 || themeColor == 0)
            }
            sdkInt = Build.VERSION.SDK_INT
            if (sdkInt >= 26) {
                darkPage = true
            } else if (barsDisabledMode.toInt() != 1) {
                if (barsDisabledMode.toInt() == 2) {
                    darkPage = AppState.darkUi
                } else if (barsDisabledMode.toInt() != 3 && darkWindow) {
                    darkPage = !darkPage || BrowserSettings.darkHome.value
                    tinted = true
                }
            }
            barShade = -1
            navBarColor = -16777216
            if (barsDisabledMode.toInt() == 1) {
                if (barsDisabledMode.toInt() == 2) {
                    if (!darkBody) {
                        themeColor = -460552
                    }
                } else if (found) {
                    if (darkBody) {
                        themeColor = 134217728
                    } else {
                        themeColor = 150994943
                    }
                } else if (BrowserSettings.headMode() != 0 || themeColor == 0) {
                    if (!AppState.darkUi) {
                        themeColor = -1
                    }
                }
                themeColor = -16777216
            } else if (darkBody) {
                themeColor = -16777216
            } else {
                themeColor = -460552
            }
            if (sdkInt >= 26) {
                if (barsDisabledMode.toInt() != 0) {
                    navBarColor = if (darkPage) -16777216 else -460552
                } else {
                    if (tinted) {
                        if (darkPage) {
                            barShade = 822083584
                        } else {
                            barShade = 838860799
                        }
                    } else if (AppState.darkUi) {
                        barShade = -16777216
                    }
                    navBarColor = barShade
                }
            }
            if (filterTint != 0) {
                themeColor = blendColors(themeColor, filterTint)
                navBarColor = blendColors(navBarColor, filterTint)
            }
            captureBarColors(getWindow(), if (PdfSettings.topToBottom.value) themeColor else barTone(themeColor), barTone(navBarColor))
            myWebCoord = this.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaApplyBodyColorsTask(this, darkBody, darkPage))
        }
        darkBody = AppState.darkUi
    }
    found = false
    sdkInt = Build.VERSION.SDK_INT
    if (sdkInt >= 26) {
        darkPage = true
    } else if (barsDisabledMode.toInt() != 1) {
        if (barsDisabledMode.toInt() == 2) {
            darkPage = AppState.darkUi
        } else if (barsDisabledMode.toInt() != 3) {
            darkPage = !darkPage || BrowserSettings.darkHome.value
            tinted = true
        }
    }
    barShade = -1
    navBarColor = -16777216
    if (barsDisabledMode.toInt() == 1) {
        if (barsDisabledMode.toInt() == 2) {
            if (!darkBody) {
                themeColor = -460552
            }
        } else if (found) {
            if (darkBody) {
                themeColor = 134217728
            } else {
                themeColor = 150994943
            }
        } else if (BrowserSettings.headMode() != 0) {
            if (!AppState.darkUi) {
                themeColor = -1
            }
        } else if (!AppState.darkUi) {
            themeColor = -1
        }
        themeColor = -16777216
    } else if (darkBody) {
        themeColor = -16777216
    } else {
        themeColor = -460552
    }
    if (sdkInt >= 26) {
        if (barsDisabledMode.toInt() != 0) {
            navBarColor = if (darkPage) -16777216 else -460552
        } else {
            if (tinted) {
                if (darkPage) {
                    barShade = 822083584
                } else {
                    barShade = 838860799
                }
            } else if (AppState.darkUi) {
                barShade = -16777216
            }
            navBarColor = barShade
        }
    }
    if (filterTint != 0) {
        themeColor = blendColors(themeColor, filterTint)
        navBarColor = blendColors(navBarColor, filterTint)
    }
    captureBarColors(getWindow(), if (PdfSettings.topToBottom.value) themeColor else barTone(themeColor), barTone(navBarColor))
    myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaApplyBodyColorsTask(this, darkBody, darkPage))
}

fun WebViewActivity.finishProgress() {
    val myProgressBar = this.dialogsState.progressBar
    if (if (myProgressBar == null) false else !myProgressBar.drawSkipped) {
        updatePageProgress(100)
    }
}

fun WebViewActivity.reloadTabsForMode(secret: kotlin.Boolean, arrayList: ArrayList<WebTabItem?>?, currentTabIndex: Int, secretMode: kotlin.Boolean) {
    if (SyncSettings.secretMode.value != secretMode) {
        if ((secret == true)) {
            this.tabsState.tabItems = arrayList
            this.currentTabIndex = currentTabIndex
        }
        switchSecretMode(secretMode)
        return
    }
    if (!secret) {
        addTabPlain(null, newTabUrl(), true, WvaTabAddDone(this))
        return
    }
    if (arrayList == null || arrayList.isEmpty()) {
        rebuildTabs(false)
        return
    }
    this.tabsState.tabItems = arrayList
    this.currentTabIndex = currentTabIndex
    reloadTabViews(WvaReloadTabsAdd(this))
}

fun WebViewActivity.offerScriptInstall(scriptInstallUrl: String?): kotlin.Boolean {
    if (!isUserScriptUrl(scriptInstallUrl)) {
        return false
    }
    if (this.dialogsState.scriptInstallDialog != null || this.dialogsState.updateScriptDialog != null || !TextUtils.isEmpty(this.dialogsState.scriptInstallUrl)) {
        return true
    }
    this.dialogsState.scriptInstallUrl = scriptInstallUrl
    runInBackground(WvaShowSetMsgDialog(this))
    return true
}

fun WebViewActivity.downloadLink(url: String?) {
    closeAllDialogs()
    if (TextUtils.isEmpty(url)) {
        showToast(this, AppR.string.empty)
        return
    }
    if ("file:///android_asset/shortcut.html" == url) {
        showToast(this, AppR.string.not_supported_page)
        return
    }
    val openedExternal = openInExternalApp(this, url)
    this.dialogsState.leavingByIntent = openedExternal
    if (openedExternal) {
        return
    }
    requestSetDownDialog(url, null, null)
}

fun WebViewActivity.closeQuickSettingsMenu() {
    if (this.dialogsState.quickSettingsMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.dialogsState.quickSettingsMenu!!.dismissNow()
        this.dialogsState.quickSettingsMenu = null
    }
}

fun WebViewActivity.applySettingsChange(themeOnly: kotlin.Boolean) {
    val darkChanged: kotlin.Boolean
    var handler: Handler? = null
    var removeLockHandler: Handler? = null
    var topOffset: Int = 0
    if (!themeOnly) {
        val themeColor = pageThemeColor()
        val themeLight = pageThemeLight()
        if (this.webCoord != null) {
            applyChangedPrefs(themeColor, themeLight)
            applyBarScrollRange()
            val currentPage = this.currentPage
            val quickPanel: QuickView? = if (currentPage != null) currentPage.quickPanel else null
            if (currentPage != null && quickPanel != null && quickPanel.isQuickDown() && quickPanel.itemAdapter != null && quickPanel.topOffset != (quickPanel.availableHeight(
                    0
                ).also { topOffset = it })) {
                quickPanel.topOffset = topOffset
                quickPanel.itemAdapter!!.updateHeaderHeight(quickPanel.availableHeight(quickPanel.gridView!!.getHeight()), true)
            }
            if (!sameObject(this.savedMainZ, MainSettings.menuItems.value)) {
                this.mainMenuItems = MainUtil.parseIntList(MainSettings.menuItems.value)
            }
            this.savedMainZ = null
            if (!sameObject(this.dialogsState.savedMainA, MainSettings.midItems.value)) {
                this.mainMenuItems2 = MainUtil.parseIntList(MainSettings.midItems.value)
            }
            this.dialogsState.savedMainA = null
            val savedMainL = this.savedMainL
            val appRotation = MainSettings.appRotation.value
            if (savedMainL != appRotation) {
                this.savedMainL = appRotation
                applyImageOrientation(this)
            }
            val zoneGestureOff = isZoneGestureOff()
            if (this.dialogsState.savedZoneGestureOff != zoneGestureOff) {
                this.dialogsState.savedZoneGestureOff = zoneGestureOff
                if (zoneGestureOff) {
                    addScrollNavis()
                } else {
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
                }
            }
            val customZones: kotlin.Boolean = hasCustomZones()
            if (this.savedCustomZones != customZones) {
                this.savedCustomZones = customZones
                if (customZones) {
                    addGestureHint()
                } else {
                    val myGesNoti = this.floatingState.gestureHint
                    if (myGesNoti != null) {
                        myGesNoti.release()
                        val myWebCoord = this.webCoord
                        if (myWebCoord != null) {
                            this.floatingState.overlays?.removeView(this.floatingState.gestureHint)
                        }
                        this.floatingState.gestureHint = null
                    }
                }
            }
            val savedShowStatus = this.savedShowStatus
            val savedG = BrowserSettings.showStatus.value
            if (savedShowStatus != savedG || this.savedShowNavi != BrowserSettings.showNavi.value) {
                this.savedShowStatus = savedG
                val savedWebU = BrowserSettings.showNavi.value
                this.savedShowNavi = savedWebU
                setSystemBarsHidden(savedG, savedWebU, false)
            }
            val savedPdfH = this.savedAddrType
            val savedAddrType = PdfSettings.addrType.value
            if (savedPdfH != savedAddrType) {
                this.savedAddrType = savedAddrType
                this.barsState.barAddressSource = null
                this.barsState.barAddressCached = null
                this.pageState.addressPathSource = null
                this.pageState.addressPathCached = null
                setBarAddress(barAddressText())
            }
            val tabBarHeight = this.tabBarHeight
            val tabAdd = PdfSettings.tabAdd.value
            if (tabBarHeight != tabAdd) {
                this.tabBarHeight = tabAdd
                val myRecyclerView: ComposeTabBar? = if (this.barsState.touchFrame != null) this.tabBarView else null
                if (this.barsState.touchFrame != null && myRecyclerView != null) {
                    if (tabAdd == 0) {
                        val marginLayoutParams =
                            myRecyclerView.getLayoutParams() as MarginLayoutParams?
                        if (marginLayoutParams != null) {
                            marginLayoutParams.setMargins(0, 0, 0, 0)
                        }
                        this.tabBarView!!.requestLayout()
                        val myButtonImage = this.sideButton
                        if (myButtonImage != null) {
                            myButtonImage.release()
                            this.barsState.touchFrame!!.removeView(this.sideButton)
                            this.sideButton = null
                        }
                    } else {
                        val marginLayoutParams =
                            myRecyclerView.getLayoutParams() as MarginLayoutParams?
                        if (marginLayoutParams != null) {
                            if (PdfSettings.tabAdd.value == 3) {
                                marginLayoutParams.setMargins(0, 0, MainApp.dp48, 0)
                            } else {
                                marginLayoutParams.setMargins(MainApp.dp48, 0, 0, 0)
                            }
                        }
                        this.tabBarView!!.requestLayout()
                        addSideButton(pageThemeColor(), pageThemeLight())
                    }
                }
            }
            val savedZoneX = this.savedZoneX
            val scrollPos = ZoneSettings.scrollPos.value
            if (savedZoneX != scrollPos) {
                this.savedZoneX = scrollPos
                updateScrollBar()
            }
            val savedEditorI = this.savedEditorI
            // both bars' transparency (top × 1000 + bottom)
            val newSavedEditorI = EditorSettings.botAlpha.value + EditorSettings.topAlpha.value * 1000
            if (savedEditorI != newSavedEditorI) {
                this.savedEditorI = newSavedEditorI
                tintBarFrames(themeColor)
            }
            val savedEditorJ = this.dialogsState.savedEditorJ
            val upAlpha = EditorSettings.upAlpha.value
            if (savedEditorJ != upAlpha) {
                this.dialogsState.savedEditorJ = upAlpha
                val webUpView = this.upButton
                if (webUpView != null) {
                    webUpView.setBgColors(this.onStartPage)
                }
            }
            val savedZoneY = this.savedZoneY
            val showUpPos = ZoneSettings.showUpPos.value
            if (savedZoneY != showUpPos) {
                this.savedZoneY = showUpPos
                if (showUpPos != 0) {
                    placeUpButton()
                    updateUpButton(true)
                } else {
                    updateUpButton(false)
                }
                applyUpButtonPos()
            }
            val savedZoneN = this.savedZoneN
            val downPos = ZoneSettings.downPos.value
            if (savedZoneN != downPos || this.savedZoneO_Q4 != ZoneSettings.showPip.value || this.savedZoneP != ZoneSettings.showFull.value || this.savedZoneQ != ZoneSettings.showDown.value || this.savedEditorM != PrefEditor.iconArgb) {
                this.savedZoneN = downPos
                this.savedZoneO_Q4 = ZoneSettings.showPip.value
                this.savedZoneP = ZoneSettings.showFull.value
                this.savedZoneQ = ZoneSettings.showDown.value
                this.savedEditorM = PrefEditor.iconArgb
                updateDownBadge()
                if (ZoneSettings.downPos.value != 0) {
                    placeDownIcons()
                }
                val webDownView = this.imageButton
                if (webDownView != null) {
                    webDownView.refreshColors()
                }
                applyDownIconColor(pageThemeColor(), pageThemeLight())
                updateImageButton(true)
            }
            // the YouTube play speed (the YouTube button merged into the video button, 2026-11-26)
            if (this.savedZoneT != ZoneSettings.youRate.value) {
                this.savedZoneT = ZoneSettings.youRate.value
                if ((!hasVideoButton() || !ZtwoSettings.youSpeedSave.value) && (ZtwoSettings.youSpeedRate.value).compareTo(1.0f) != 0) {
                    ZtwoSettings.editLocal { set(ZtwoSettings.youSpeedRate, 1.0f) }
                    putSpeedRate(this.dialogsState.appContext, 1.0f)
                }
                updateImageButton(true)
            }
            val savedZtwoE = this.savedZtwoE
            val addPage = ZtwoSettings.addPage.value
            if (savedZtwoE != addPage) {
                this.savedZtwoE = addPage
                val webNestView = this.currentPage
                if (webNestView != null) {
                    webNestView.setAddPage(addPage)
                }
                val webNestFrame = this.tabsState.currentFrame
                if (webNestFrame != null) {
                    webNestFrame.setAddPage(ZtwoSettings.addPage.value)
                }
            }
            val savedZoneU = this.savedZoneU
            val hideImage = ZoneSettings.hideImage.value
            if (savedZoneU != hideImage) {
                this.savedZoneU = hideImage
                refreshButtonIcons(6)
            }
            val darkPage = MainUtil.isDarkPage(this.onStartPage)
            if (this.savedDarkStart == darkPage && this.savedDarkWeb == AppState.darkWeb && this.savedNavColor == navBarColorNow()) {
                darkChanged = false
            } else {
                darkChanged = applyDarkMode(this.savedNavColor, this.savedDarkStart, this.savedDarkWeb)
                this.savedDarkStart = darkPage
                this.savedDarkWeb = AppState.darkWeb
                this.savedNavColor = navBarColorNow()
            }
            val savedWebM = this.dialogsState.savedThemeImg
            val savedThemeImg = BrowserSettings.themeImg.value
            if (savedWebM != savedThemeImg || this.savedImgBright != BrowserSettings.imgBright.value) {
                this.dialogsState.savedThemeImg = savedThemeImg
                this.savedImgBright = BrowserSettings.imgBright.value
                val webNestView: WebNestView? = if (!darkChanged) this.currentPage else null
                if (!darkChanged && webNestView != null) {
                    webNestView.applyDarkImageStyle(this.dialogsState.pageUrl, this.dialogsState.pageHost)
                }
            }
            val savedPdfS = this.savedBlockSsl
            val savedBlockSsl = PdfSettings.blockSsl.value
            if (savedPdfS != savedBlockSsl) {
                this.savedBlockSsl = savedBlockSsl
                val webNestView: WebNestView? = if (savedBlockSsl) this.currentPage else null
                if (savedBlockSsl && webNestView != null) {
                    webNestView.removeSslView()
                }
            }
            val savedEditorq = this.dialogsState.savedEditorq
            val color = PrefEditor.color
            if (savedEditorq != color) {
                this.dialogsState.savedEditorq = color
                val webTtsView = this.speechState.ttsButton
                if (webTtsView != null) {
                    webTtsView.refreshColors()
                }
            }
            val savedZoneOANow = this.savedZoneO_a5
            val savedZoneOA = ZoneSettings.gesCenter.value
            if (savedZoneOANow != savedZoneOA) {
                this.savedZoneO_a5 = savedZoneOA
                val webNestView: WebNestView? = if (savedZoneOA != 2) this.currentPage else null
                if (savedZoneOA != 2 && webNestView != null) {
                    val sb = StringBuilder()
                    sb.append("var elw=document.querySelectorAll(\"*[style*='translate']\");if(elw&&(elw.length>0)){for(var i=0;i<elw.length;i++){elw[i].addEventListener(\"touchstart\",function(){android.onSlideDet();});}}")
                    sb.insert(0, "(function(){")
                    sb.append("})();")
                    evalJs(webNestView, sb.toString(), true)
                }
            }
            val savedTtsF = this.dialogsState.savedWebLand
            val savedWebLand = TtsSettings.webLand.value
            if (savedTtsF != savedWebLand || this.savedWebLand2 != TtsSettings.webLand2.value) {
                this.dialogsState.savedWebLand = savedWebLand
                this.savedWebLand2 = TtsSettings.webLand2.value
                val landscape = isLandscapeScreen()
                applySideMargins(landscape)
                updateSplitLand(landscape)
                refreshBarFrames()
            }
            val savedTtsJ = this.savedHandTop
            val savedHandTop = TtsSettings.handTop.value
            if (savedTtsJ != savedHandTop) {
                this.savedHandTop = savedHandTop
                if (TtsSettings.handMode.value && !isLandscapeScreen()) {
                    applyTopInset(true)
                    val myWebBody: MyWebBody? = if (TtsSettings.handMode.value && TtsSettings.handNoti.value && this.dialogsState.handModeBar != null) this.dialogsState.webBody else null
                    if (TtsSettings.handMode.value && TtsSettings.handNoti.value && this.dialogsState.handModeBar != null && myWebBody != null) {
                        val handTop = myWebBody.handTopPos(this)
                        Zn = handTop
                        if (MainConst.isApi30) {
                            val webBody = this.dialogsState.webBody
                            pageThemeColor()
                            webBody!!.insetTop = handTop
                            webBody.topColor = webBody.topBarColor
                        }
                        if (this.dialogsState.handModeBar!!.getHeight() != handTop) {
                            this.dialogsState.handModeBar!!.getLayoutParams().height = handTop
                            this.dialogsState.handModeBar!!.requestLayout()
                        }
                    }
                }
            }
            val savedAlbumU = this.savedAlbumU
            val transMode = AlbumSettings.transMode.value
            if (savedAlbumU != transMode || this.savedAlbumW != AlbumSettings.transPage.value || this.savedTransHide != AlbumSettings.transHide.value) {
                this.savedAlbumU = transMode
                this.savedAlbumW = AlbumSettings.transPage.value
                this.savedTransHide = AlbumSettings.transHide.value
                updateTransIcon(false)
            }
            applyTransLang()
            val savedEditorZ = this.savedEditorZ
            val trnsArgb = PrefEditor.trnsArgb
            if (savedEditorZ != trnsArgb) {
                this.savedEditorZ = trnsArgb
                val webFltView = this.floatingState.translateButton
                if (webFltView != null) {
                    webFltView.refreshColors()
                }
            }
            val savedZtriK = this.savedZtriK
            val zoomIcon = ZtriSettings.zoomIcon.value
            if (savedZtriK != zoomIcon) {
                this.savedZtriK = zoomIcon
                if (zoomIcon) {
                    addZoomButton()
                } else {
                    val webFltView = this.floatingState.zoomButton
                    if (webFltView != null) {
                        webFltView.release()
                        val myWebBody = this.dialogsState.webBody
                        if (myWebBody != null) {
                            this.floatingState.overlays!!.removeView(this.floatingState.zoomButton)
                        }
                        this.floatingState.zoomButton = null
                    }
                }
            }
            val savedEditorU = this.savedEditorU
            val zoomArgb = PrefEditor.zoomArgb
            if (savedEditorU != zoomArgb) {
                this.savedEditorU = zoomArgb
                val webFltView = this.floatingState.zoomButton
                if (webFltView != null) {
                    webFltView.refreshColors()
                }
            }
            // the size of all floating buttons (2026-11-27)
            if (this.floatingState.savedSize != FloatSettings.fltSize.value) {
                this.floatingState.savedSize = FloatSettings.fltSize.value
                val f = this.floatingState
                for (button in listOf(this.imageButton, this.speechState.ttsButton, f.lockButton, f.translateButton, f.zoomButton, f.userButton1, f.userButton2, f.userButton3, f.fullscreenButton, f.rssButton)) {
                    button?.applySize()
                }
            }
            val savedFloatj = this.dialogsState.savedFloatj
            val flt1Icon = FloatSettings.flt1Icon.value
            if (savedFloatj != flt1Icon) {
                this.dialogsState.savedFloatj = flt1Icon
                addUserButton(6)
                val webFltView = this.floatingState.userButton1
                if (webFltView != null) {
                    webFltView.setIconView(FloatSettings.flt1Icon.value)
                }
            }
            val savedFloatK = this.savedFloatK
            val flt2Icon = FloatSettings.flt2Icon.value
            if (savedFloatK != flt2Icon) {
                this.savedFloatK = flt2Icon
                addUserButton(7)
                val webFltView = this.floatingState.userButton2
                if (webFltView != null) {
                    webFltView.setIconView(FloatSettings.flt2Icon.value)
                }
            }
            val savedFloatL = this.savedFloatL
            val flt3Icon = FloatSettings.flt3Icon.value
            if (savedFloatL != flt3Icon) {
                this.savedFloatL = flt3Icon
                addUserButton(8)
                val webFltView = this.floatingState.userButton3
                if (webFltView != null) {
                    webFltView.setIconView(FloatSettings.flt3Icon.value)
                }
            }
            val savedFloats = this.dialogsState.savedFloats
            val flt1Argb = PrefFloat.flt1Argb
            if (savedFloats != flt1Argb) {
                this.dialogsState.savedFloats = flt1Argb
                val webFltView = this.floatingState.userButton1
                if (webFltView != null) {
                    webFltView.refreshColors()
                }
            }
            val savedFloatW = this.savedFloatW
            val flt2Argb = PrefFloat.flt2Argb
            if (savedFloatW != flt2Argb) {
                this.savedFloatW = flt2Argb
                val webFltView = this.floatingState.userButton2
                if (webFltView != null) {
                    webFltView.refreshColors()
                }
            }
            val savedFloatA = this.savedFloatA
            val flt3Argb = PrefFloat.flt3Argb
            if (savedFloatA != flt3Argb) {
                this.savedFloatA = flt3Argb
                val webFltView = this.floatingState.userButton3
                if (webFltView != null) {
                    webFltView.refreshColors()
                }
            }
            showUserButtons(true)
            val savedWebO = this.savedThemeHead
            val headMode = BrowserSettings.headMode()
            if (savedWebO != headMode) {
                this.savedThemeHead = headMode
                this.savedThemeColor = themeColor
                if (headMode == 1) {
                    val currentTabIndex = this.currentTabIndex
                    val webNestView = this.currentPage
                    val pageUrl = this.dialogsState.pageUrl
                    if (webNestView != null && headMode == 1 && currentTabIndex != -1) {
                        val sb = StringBuilder()
                        if (MainUtil.isYouTubeUrl(pageUrl) || isImagePath(pageUrl)) {
                            sb.append("android.onThemeDet('")
                            sb.append(currentTabIndex)
                            sb.append("','btc');")
                        } else {
                            sb.append("if(document.head){var tema=document.querySelector(\"meta[name=theme-color]\");if(tema){android.onThemeDet('")
                            sb.append(currentTabIndex)
                            sb.append("',tema.content);}else{android.onThemeDet('")
                            sb.append(currentTabIndex)
                            sb.append("',null);}}")
                        }
                        sb.insert(0, "(function(){")
                        sb.append("})();")
                        evalJs(webNestView, sb.toString(), true)
                    }
                } else {
                    applyPageBarColor(themeColor, themeLight)
                }
            } else if (this.savedThemeColor != themeColor) {
                this.savedThemeColor = themeColor
                applyPageBarColor(themeColor, themeLight)
            } else {
                val savedDarkHome = this.dialogsState.savedDarkHome
                val savedWebU = BrowserSettings.darkHome.value
                if (savedDarkHome != savedWebU) {
                    this.dialogsState.savedDarkHome = savedWebU
                    if (headMode == 2 && this.onStartPage) {
                        this.dialogsState.themeColor = themeColor + 1
                        applyPageBarColor(themeColor, themeLight)
                    }
                }
            }
            if (this.savedNewsUse != ZtwoSettings.newsUse.value || this.savedZtriC != ZtriSettings.newsIcon.value || !sameObject(this.savedZtwoL, ZtwoSettings.newsUrl.value)) {
                val savedZtriC = ZtriSettings.newsIcon.value
                this.savedZtriC = savedZtriC
                if (ZtwoSettings.newsUse.value && savedZtriC && !QuickView.isCustomNewsUrl()) {
                    addRssButton()
                } else if (this.floatingState.rssButton != null && (this.mainHandler.also { handler = it }) != null) {
                    handler!!.post(WvaRemoveNewsFloat(this))
                }
            }
            val savedEditorN = this.savedEditorN
            val newsArgb = PrefEditor.newsArgb
            if (savedEditorN != newsArgb) {
                this.savedEditorN = newsArgb
                val webFltView = this.floatingState.rssButton
                if (webFltView != null) {
                    webFltView.refreshColors()
                }
            }
            val newsUse = ZtwoSettings.newsUse.value
            val newsTitle = newsUse && ZtwoSettings.newsTitle.value
            val newsPick = if (newsTitle) ZtwoSettings.newsPick.value else ""
            if (this.savedNewsUse != newsUse || this.savedZtwoN != ZtwoSettings.newsLang.value || this.savedNewsTitle != newsTitle || !sameObject(
                    this.dialogsState.savedNewsPick,
                    newsPick
                ) || !sameObject(this.savedZtwoL, ZtwoSettings.newsUrl.value)
            ) {
                this.savedNewsUse = ZtwoSettings.newsUse.value
                this.savedZtwoN = ZtwoSettings.newsLang.value
                this.savedNewsTitle = newsTitle
                val webNestView = this.currentPage
                val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
                if (webNestView != null && quickView != null) {
                    quickView.applyNewsChange(true)
                }
            }
            this.dialogsState.savedNewsPick = null
            this.savedZtwoL = null
            val savedZtwor = this.dialogsState.savedZtwor
            val tabIndi = ZtwoSettings.tabIndi.value
            if (savedZtwor != tabIndi) {
                this.dialogsState.savedZtwor = tabIndi
                updateTabCountIcons()
            }
            val secretLocked = isSecretLocked()
            if (this.dialogsState.savedSecretLocked != secretLocked) {
                this.dialogsState.savedSecretLocked = secretLocked
                applyIncognitoIme(this.editAuto)
                val myFindView = this.findView
                if (myFindView != null) {
                    applyIncognitoIme(myFindView.editField)
                }
            }
            val savedZtwoZ = this.dialogsState.savedZtwoZ
            val recentLimit = ZtwoSettings.recentLimit.value
            if (savedZtwoZ != recentLimit || this.savedZtwoA0 != ZtwoSettings.recentSecret.value) {
                this.dialogsState.savedZtwoZ = recentLimit
                val savedZtwoA = ZtwoSettings.recentSecret.value
                this.savedZtwoA0 = savedZtwoA
                if (recentLimit != -1 || !savedZtwoA) {
                    runInBackground(WvaApplyRecentLimitTask(this))
                }
            }
            applyPageScripts(false)
            if (PassManager.button) {
                addLockButton()
            } else if (this.floatingState.lockButton != null && (this.mainHandler.also { removeLockHandler = it }) != null) {
                removeLockHandler!!.post(WvaRemoveListFloat(this))
            }
            val savedSecretx = this.dialogsState.savedSecretx
            val savePass = SecretSettings.savePass.value
            if (savedSecretx != savePass) {
                this.dialogsState.savedSecretx = savePass
                if (savePass) {
                    checkSavePassword()
                }
            }
            val savedZtwoU = this.savedZtwoU
            val youUnmute = ZtwoSettings.youUnmute.value
            if (savedZtwoU != youUnmute) {
                this.savedZtwoU = youUnmute
                val webNestView: WebNestView? = if (youUnmute && isYouTubeWatch(this.dialogsState.pageUrl)) this.currentPage else null
                if (youUnmute && isYouTubeWatch(this.dialogsState.pageUrl) && webNestView != null) {
                    evalJs(
                        webNestView,
                        "(function(){var ele=document.querySelector(\"button[class*='ytp-unmute']\");if(ele){ele.click();}})();",
                        true
                    )
                }
            }
        }
        updateImageButton(true)
        if (ZtwoSettings.dayMemory.value != 0 && !this.dialogsState.applySettingsChangeBusy) {
            this.dialogsState.applySettingsChangeBusy = true
            val myWebCoord = this.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaCloseExpiredTabs(this))
            }
        }
    }
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaApplySettingsChangeTask(this))
}

fun WebViewActivity.openEmgGallery(pageUrl: String?, forceLoad: kotlin.Boolean) {
    val mode: Int
    val found: kotlin.Boolean
    val url: String?
    val siteUrl: String?
    var siteBase: String? = null
    var isLofi = false
    if (isEmgUrl(pageUrl)) {
        if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
            return
        }
        resumePausedPage()
        if (TextUtils.isEmpty(pageUrl) || "file:///android_asset/shortcut.html" == pageUrl || "about:blank" == pageUrl) {
            showToast(this, AppR.string.not_supported_page)
            return
        }
        if (this.dialogsState.loadDialogKind.toInt() == 0) {
            this.dialogsState.loadDialogKind = 4.toByte()
            val host = hostOf(pageUrl, false)
            val length = if (host != null) host.length else 0
            if (host != null && length != 0) {
                isLofi = pageUrl!!.startsWith("/lofi/s/", length)
            }
            val galleryUrl = if (isLofi) lofiUrl(pageUrl, host) else pageUrl
            pausePage()
            openGallerySite(galleryUrl, 1)
            return
        }
        return
    }
    val isHmg = isHmgUrl(pageUrl)
    val webNestView = this.currentPage
    if (isHmg) {
        if (webNestView == null || this.isClosing || isAnyDialogOpen()) {
            return
        }
        resumePausedPage()
        if (TextUtils.isEmpty(pageUrl) || "file:///android_asset/shortcut.html" == pageUrl || "about:blank" == pageUrl) {
            showToast(this, AppR.string.not_supported_page)
            return
        }
        if (this.dialogsState.loadDialogKind.toInt() == 0) {
            this.dialogsState.loadDialogKind = 6.toByte()
            if (hasFragmentPage(pageUrl)) {
                val length = if (pageUrl != null) pageUrl.length else 0
                val dashIndex = if (pageUrl != null && length != 0) pageUrl.lastIndexOf(45.toChar()) else 0
                if (pageUrl != null && length != 0 && dashIndex > 0 && dashIndex < length) {
                    siteBase = pageUrl.substring(0, dashIndex)
                }
                siteUrl = siteBase
            } else {
                siteUrl = pageUrl
            }
            pausePage()
            openGallerySite(siteUrl, 2)
            return
        }
        return
    }
    if (webNestView == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    resumePausedPage()
    val webGridDialog = this.dialogsState.gridDialog
    if (webGridDialog != null) {
        webGridDialog.dismiss()
        this.dialogsState.gridDialog = null
    }
    if (TextUtils.isEmpty(pageUrl) || "file:///android_asset/shortcut.html" == pageUrl || "about:blank" == pageUrl) {
        showToast(this, AppR.string.not_supported_page)
        return
    }
    if (this.dialogsState.loadDialogKind.toInt() != 0) {
        return
    }
    this.dialogsState.loadDialogKind = 2.toByte()
    if ((forceLoad == true)) {
        url = pageUrl
        found = false
        mode = 0
    } else if (TextUtils.isEmpty(this.currentPage!!.fileUrl)) {
        val webLoadTaskB: WebLoadTask = WebLoadTask.Companion.instance()
        val currentPage = this.currentPage
        webLoadTaskB.started = true
        webLoadTaskB.loadWeb = currentPage
        webLoadTaskB.taskListener = null
        webLoadTaskB.loadState = 0.toByte()
        webLoadTaskB.netError = false
        mode = 2
        found = false
        url = pageUrl
    } else {
        val fileUrl = this.currentPage!!.fileUrl
        if ("file:///android_asset/shortcut.html" == fileUrl || "about:blank" == fileUrl) {
            this.dialogsState.loadDialogKind = 0.toByte()
            showToast(this, AppR.string.not_supported_page)
            return
        } else if (!URLUtil.isNetworkUrl(fileUrl)) {
            this.dialogsState.loadDialogKind = 0.toByte()
            showToast(this, AppR.string.invalid_url)
            return
        } else {
            url = fileUrl
            mode = 0
            found = true
        }
    }
    pausePage()
    val gridDialog = WebGridDialog(this, url, found, forceLoad, mode, WvaOpenEmgGalleryDismiss())
    this.dialogsState.gridDialog = gridDialog
    gridDialog.setOnDismissListener(WvaGridDialogDismissed(this))
}

/** Opens the Compose gallery-site picker (kind 1 / 2, see [WebSiteDialog]); closing it resets the pending-dialog state like the old pickers. */
fun WebViewActivity.openGallerySite(url: String?, siteKind: Int) {
    dialogsState.siteDialog?.dismiss()
    val dialog = WebSiteDialog(this, url, siteKind, WvaGallerySiteImageListener())
    dialogsState.siteDialog = dialog
    dialog.setOnDismissListener {
        resumePausedPage()
        dialogsState.siteDialog?.dismiss()
        dialogsState.siteDialog = null
        clearDataCaches()
        runInBackground(Runnable { MainUtil.trimMemoryCaches() })
    }
}

fun WebViewActivity.checkVideoRate(): kotlin.Boolean {
    if (this.dialogsState.isVideoPage && !this.dialogsState.videoRateOff) {
        if (isSameRatePage()) {
            val handler = this.mainHandler
            if (handler != null) {
                handler.post(WvaVideoRateCheck(this))
                return true
            }
        } else {
            if (hasVideoButton() && ZtwoSettings.youSpeedSave.value) {
                return false
            }
            if ((ZtwoSettings.youSpeedRate.value).compareTo(1.0f) != 0) {
                ZtwoSettings.editLocal { set(ZtwoSettings.youSpeedRate, 1.0f) }
                putSpeedRate(this.dialogsState.appContext, 1.0f)
            }
        }
    }
    return true
}

fun WebViewActivity.closeAddressPrefixMenu() {
    if (this.dialogsState.addressPrefixMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.dialogsState.addressPrefixMenu!!.dismissNow()
        this.dialogsState.addressPrefixMenu = null
    }
}

fun WebViewActivity.closeAllSnackbars() {
    val mySnackbar = this.dialogsState.tabUndoSnackbar
    if (mySnackbar != null) {
        mySnackbar.hide(false)
        this.dialogsState.tabUndoSnackbar = null
    }
    val popupBlockSnackbar = this.dialogsState.popupBlockSnackbar
    if (popupBlockSnackbar != null) {
        popupBlockSnackbar.hide(false)
        this.dialogsState.popupBlockSnackbar = null
    }
    val appBlockSnackbar = this.dialogsState.appBlockSnackbar
    if (appBlockSnackbar != null) {
        appBlockSnackbar.hide(false)
        this.dialogsState.appBlockSnackbar = null
    }
    val snackbar = this.snackbar
    if (snackbar != null) {
        snackbar.hide(false)
        this.snackbar = null
    }
}

fun WebViewActivity.applyTabList(currentTabIndex: Int, arrayList: ArrayList<WebTabItem?>?) {
    if (arrayList == null || arrayList.isEmpty()) {
        rebuildTabs(false)
        return
    }
    this.tabsState.tabItems = arrayList
    this.currentTabIndex = currentTabIndex
    reloadTabViews(TabBarRefresh(this))
}

fun WebViewActivity.showAllowPopupDialog() {
    if (this.currentPage == null || this.isClosing) {
        return
    }
    if (this.popupMenuDialog != null) {
        if (this.allowPopupDialog != null) {
            return
        }
    } else if (isAnyDialogOpen()) {
        return
    }
    val dialogAllowPopup = this.allowPopupDialog
    if (dialogAllowPopup != null) {
        dialogAllowPopup.dismiss()
        this.allowPopupDialog = null
    }
    if (TextUtils.isEmpty(this.dialogsState.pageHost)) {
        showToast(this, AppR.string.invalid_url)
        return
    }
    val allowPopupDialog = DialogAllowPopup(this, currentPageUrl(false), WvaAdsDialogListener(this))
    this.allowPopupDialog = allowPopupDialog
    allowPopupDialog.setOnDismissListener(WvaAllowPopupDialogDismissed(this))
}

fun WebViewActivity.enterHandMode() {
    if (this.webCoord == null || this.miscState.handEnterAnim != null || this.miscState.handExitAnim != null || TtsSettings.handMode.value) {
        return
    }
    TtsSettings.edit(this.dialogsState.appContext) { set(TtsSettings.handMode, true) }
    updateHandListener()
    addHandModeBar()
    val myWebBody = this.dialogsState.webBody
    val handTop = if (myWebBody == null) 0 else myWebBody.handTopPos(this)
    Zn = handTop
    val isApi = MainConst.isApi30
    val webBody = this.dialogsState.webBody
    if (isApi) {
        pageThemeColor()
        webBody!!.insetTop = handTop
        webBody.topColor = webBody.topBarColor
    } else {
        webBody!!.setOldTheme(pageThemeColor())
    }
    val layoutParams = this.webCoord!!.getLayoutParams() as RelativeLayout.LayoutParams?
    if (layoutParams == null || layoutParams.topMargin == handTop) {
        return
    }
    this.miscState.handEnterOffset = 0
    this.miscState.handEnterAnimBusy = false
    val valueAnimatorOfInt = ValueAnimator.ofInt(0, handTop)
    this.miscState.handEnterAnim = valueAnimatorOfInt
    valueAnimatorOfInt.setDuration(200L)
    this.miscState.handEnterAnim!!.interpolator = android.view.animation.DecelerateInterpolator()
    this.miscState.handEnterAnim!!.addUpdateListener(WvaHandModeEnterAnim(this))
    this.miscState.handEnterAnim!!.addListener(WvaHandModeEnterAnimListener(this))
    this.miscState.handEnterAnim!!.start()
}

fun WebViewActivity.checkWindowBackground() {
    val windowBgMode = this.dialogsState.windowBgMode
    if (windowBgMode.toInt() != 0) {
        if (this.dialogsState.windowBgKey != this.dialogsState.windowBgColor) {
            postWindowBackgroundUpdate()
        } else {
            if (windowBgMode.toInt() == 3 || !useDarkWindow()) {
                return
            }
            if ((this.dialogsState.windowBgMode.toInt() == 2) == isLandscapeScreen()) {
                return
            }
            postWindowBackgroundUpdate()
        }
    }
}

fun WebViewActivity.clampTabIndex() {
    val arrayList = this.tabsState.tabItems
    var currentSecretIndex = 0
    val size = if (arrayList != null) arrayList.size else 0
    var currentTabIndex = this.currentTabIndex
    if (currentTabIndex >= size) {
        currentTabIndex = size - 1
        this.currentTabIndex = currentTabIndex
    }
    if (currentTabIndex < 0) {
        this.currentTabIndex = 0
    } else {
        currentSecretIndex = currentTabIndex
    }
    if (SyncSettings.secretMode.value) {
        val tabBarScrollIndex = PrefSync.currentSecretIndex
        if (tabBarScrollIndex != currentSecretIndex) {
            this.dialogsState.tabBarScrollIndex = tabBarScrollIndex
            PrefSync.currentSecretIndex = currentSecretIndex
            return
        }
        return
    }
    val tabBarScrollIndex = SyncSettings.normalIndex.value
    if (tabBarScrollIndex != currentSecretIndex) {
        this.dialogsState.tabBarScrollIndex = tabBarScrollIndex
        SyncSettings.editLocal { set(SyncSettings.normalIndex, currentSecretIndex) }
    }
}

fun WebViewActivity.showBlockLinkDialog(url: String?) {
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    val dialogBlockLink = this.blockLinkDialog
    if (dialogBlockLink != null) {
        dialogBlockLink.dismiss()
        this.blockLinkDialog = null
    }
    if (TextUtils.isEmpty(url) || "file:///android_asset/shortcut.html" == url || "about:blank" == url) {
        showToast(this, AppR.string.not_supported_page)
        return
    }
    val blockLinkDialog = DialogBlockLink(this, this.dialogsState.pageUrl, url, WvaAdsDialogListener(this))
    this.blockLinkDialog = blockLinkDialog
    blockLinkDialog.setOnDismissListener(WvaBlockLinkDialogDismissed(this))
}

fun WebViewActivity.exitHandMode() {
    if (this.webCoord != null && this.miscState.handEnterAnim == null && this.miscState.handExitAnim == null && TtsSettings.handMode.value) {
        TtsSettings.edit(this.dialogsState.appContext) { set(TtsSettings.handMode, false) }
        updateHandListener()
        val layoutParams = this.webCoord!!.getLayoutParams() as RelativeLayout.LayoutParams?
        val handModeOffset = if (layoutParams == null) 0 else layoutParams.topMargin
        if (layoutParams == null || handModeOffset == 0) {
            return
        }
        this.miscState.handModeOffset = handModeOffset
        this.miscState.handExitAnimBusy = false
        val valueAnimatorOfInt = ValueAnimator.ofInt(0, -handModeOffset)
        this.miscState.handExitAnim = valueAnimatorOfInt
        valueAnimatorOfInt.setDuration(200L)
        this.miscState.handExitAnim!!.interpolator = android.view.animation.AccelerateInterpolator()
        this.miscState.handExitAnim!!.addUpdateListener(WvaHandModeExitAnim(this))
        this.miscState.handExitAnim!!.addListener(WvaHandModeExitAnimListener(this))
        this.miscState.handExitAnim!!.start()
    }
}

fun WebViewActivity.onTabAdded(isAdded: kotlin.Boolean) {
    val addressEditAfterAdd = BrowserSettings.tabKeypad.value && (isAdded == true)
    this.barsState.addressEditAfterAdd = addressEditAfterAdd
    if (!addressEditAfterAdd || AlbumSettings.tabAnim.value) {
        return
    }
    resumeAddressEditAfterAdd()
}

fun WebViewActivity.openInCurrentPage(url: String?, blobPageUrl: String?) {
    if (this.currentPage != null) {
        closeAllDialogs()
        stopWebClean()
        hidePageOverlays(true)
        if (MainUtil.shouldOpenInNewTab(this.currentPage, this.dialogsState.pageUrl, this.dialogsState.pageHost, url, null, false)) {
            openInNextPage(url, blobPageUrl, false)
        } else {
            if (openTextLink(url)) {
                return
            }
            val resolvedUrl = this.currentPage!!.realUrl(url, false)
            loadPageUrl(resolvedUrl, false, true)
            this.dialogsState.blobPageUrl = blobPageUrl
            this.currentPage!!.openUrl(resolvedUrl, blobPageUrl)
        }
    }
}

fun WebViewActivity.applyTabLimit(tabLimitListener: TabLimitListener?) {
    var tabsToClose: ArrayList<Any?>?
    var tabsToReparent: ArrayList<Any?>?
    val arrayList: ArrayList<WebTabItem?>? = if (ZtwoSettings.tabLimit.value < 3) null else this.tabsState.tabItems
    if (ZtwoSettings.tabLimit.value < 3 || arrayList == null || arrayList.size <= ZtwoSettings.tabLimit.value) {
        tabLimitListener!!.onLimitApplied(false)
        return
    }
    if (this.dialogsState.applyTabLimitBusy) {
        return
    }
    this.dialogsState.applyTabLimitBusy = true
    synchronized(this.tabLock) {
        val keptTabs: ArrayList<WebTabItem?> = ArrayList()
        val size = this.tabsState.tabItems!!.size
        val currentTabIndex = this.currentTabIndex
        val tabLimit = ZtwoSettings.tabLimit.value
        var index = currentTabIndex - (tabLimit / 2)
        if (index < 0) {
            index = 0
        }
        var lastIndex = (index + tabLimit) - 1
        if (lastIndex >= size) {
            lastIndex = size - 1
            index = size - tabLimit
        }
        var parentId: Long = -1
        var tabIndex = 0
        tabsToClose = null
        tabsToReparent = null
        var position = 0
        // every tab once: outside [index, lastIndex] it is closed, inside it is kept (a null entry is dropped)
        while (tabIndex < size) {
            val webTabItem = this.tabsState.tabItems!!.get(tabIndex)
            if (webTabItem != null) {
                if (tabIndex < index || tabIndex > lastIndex) {
                    val webNestFrame = webTabItem.webFrame
                    if (webNestFrame != null) {
                        webNestFrame.destroyFrame(null)
                        webTabItem.webFrame = null
                    }
                    if (tabsToClose == null) {
                        tabsToClose = ArrayList<Any?>()
                    }
                    tabsToClose.add(webTabItem)
                } else {
                    keptTabs.add(webTabItem)
                    val otherParent = webTabItem.parentId != parentId
                    webTabItem.parentId = parentId
                    webTabItem.position = position
                    parentId = webTabItem.uid
                    position++
                    if (otherParent) {
                        if (tabsToReparent == null) {
                            tabsToReparent = ArrayList<Any?>()
                        }
                        val copy = WebTabItem()
                        copy.uid = webTabItem.uid
                        copy.parentId = webTabItem.parentId
                        tabsToReparent.add(copy)
                    }
                }
            }
            tabIndex++
        }
        this.tabsState.tabItems = keptTabs
        this.currentTabIndex -= index
    }
    this.miscState.tabLimitListener = tabLimitListener
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaApplyTabLimitTask(this, closedTabs = tabsToClose, reparentedTabs = tabsToReparent))
}

fun WebViewActivity.closeSnackbar(animate: kotlin.Boolean) {
    val mySnackbar = this.dialogsState.tabUndoSnackbar
    if (mySnackbar != null) {
        mySnackbar.hide(animate)
        this.dialogsState.tabUndoSnackbar = null
    }
}

fun WebViewActivity.sendAppToBackground() {
    if (this.dialogsState.sendAppToBackgroundBusy) {
        return
    }
    this.dialogsState.sendAppToBackgroundBusy = true
    Thread(WvaSendAppToBackgroundTask(this)).start()
    moveTaskToBack(true)
    val webNestView = this.currentPage
    if (webNestView != null) {
        webNestView.pauseOnce()
        MainApp.pauseWebTimers(this.dialogsState.appContext, this.currentPage)
    }
}

fun WebViewActivity.addBlockCount(popsBlocked: Int, show: kotlin.Boolean) {
    val webNestView: WebNestView? = if ((BrowserSettings.adsBlock.value || BrowserSettings.popBlock.value != 0)) this.currentPage else null
    if ((BrowserSettings.adsBlock.value || BrowserSettings.popBlock.value != 0) && webNestView != null) {
        if ((show == true)) {
            webNestView.popsBlocked += popsBlocked
        } else {
            webNestView.adsBlocked += popsBlocked
        }
        val blockedCount = webNestView.adsBlocked + webNestView.popsBlocked
        val dialogMenuMain = this.menuMainDialog
        if (dialogMenuMain != null) {
            dialogMenuMain.applyBlocked(blockedCount)
            return
        }
        val dialogMenuList = this.dialogsState.menuListDialog
        if (dialogMenuList != null) {
            dialogMenuList.applyBlocked(blockedCount)
        }
    }
}

fun WebViewActivity.showDarkConfirmDialog() {
    if (!ZtriSettings.guideDark.value || this.isClosing) {
        return
    }
    if (this.dialogsState.setDarkDialog == null && isAnyDialogOpen()) {
        return
    }
    closeDarkConfirmDialog()
    val dialogConfirm =
        DialogConfirm(this as Activity, true, WvaDarkConfirmButtons(this) as DialogConfBtnListener)
    this.darkConfirmDialog = dialogConfirm
    dialogConfirm.setOnDismissListener(WvaShowDarkConfirmDialogDismiss(this))
}

fun WebViewActivity.updateHandListener() {
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return
    }
    if (TtsSettings.handMode.value) {
        myWebBody.setHandListener(WvaHandListener(this))
    } else {
        myWebBody.setHandListener(null)
    }
}

fun WebViewActivity.closeSubMenu() {
    if (this.dialogsState.quickSubMenu != null) {
        this.popupMenu = this.dialogsState.quickSettingsMenu
        holdQuickMenuReopen()
        this.dialogsState.quickSubMenu!!.dismissNow()
        this.dialogsState.quickSubMenu = null
    }
}

fun WebViewActivity.showLoadEmgDialog(url: String?, forceLoad: kotlin.Boolean) {
    val loadUrl: String?
    val found: kotlin.Boolean
    var length: Int = 0
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeLoadEmgDialog()
    if (TextUtils.isEmpty(url) || "file:///android_asset/shortcut.html" == url || "about:blank" == url) {
        showToast(this, AppR.string.not_supported_page)
        return
    }
    if (this.dialogsState.loadDialogKind.toInt() == 0) {
        this.dialogsState.loadDialogKind = 3.toByte()
        val host = MainUtil.hostOf(url, false)
        if (if (host == null || (host.length.also {
                    length = it
                }) == 0) false else url!!.startsWith("/lofi/s/", length)) {
            loadUrl = MainUtil.lofiUrl(url, host)
            found = true
        } else {
            loadUrl = url
            found = false
        }
        if (((forceLoad == true) || found) && this.emgLoad == null) {
            val myWebCoord = this.webCoord
            val webEmgLoad = WebEmgLoad()
            if (myWebCoord != null) {
                webEmgLoad.appContext = getApplicationContext()
                webEmgLoad.parentView = myWebCoord
                webEmgLoad.pageUrl = loadUrl
                webEmgLoad.imagePos = -1
                val webNestView = WebNestView(this)
                webEmgLoad.webNest = webNestView
                webNestView.setNoAutofill(true)
                webEmgLoad.webNest!!.setVisibility(View.INVISIBLE)
                webEmgLoad.parentView!!.addView(webEmgLoad.webNest, 0, ViewGroup.LayoutParams(-1, -1))
                webEmgLoad.parentView!!.post(webEmgLoad.CreateWebTask())
            }
            this.emgLoad = webEmgLoad
        }
        val webEmgLoad = this.emgLoad
        val webNestView = if (webEmgLoad != null) webEmgLoad.webNest else this.currentPage
        if (webNestView == null) {
            return
        }
        val dialogLoadEmg = DialogLoadEmg(this, webNestView, loadUrl, forceLoad, WvaLoadEmgListener(this))
        this.loadEmgDialog = dialogLoadEmg
        dialogLoadEmg.setOnDismissListener(WvaEmgLoadDismissed(this))
    }
}

/** The lock in the address bar: is the site secure, with a button to the full certificate (2026-11-20). */
fun WebViewActivity.showSiteSecurity() {
    if (this.isClosing || isAnyDialogOpen()) return
    closeMenuDialogs()
    com.mycompany.app.dialog.SiteSecurityDialog(this, this.currentPage) { onAddressMenuItem(7) }
}

/** Expires every cookie the cookie manager has for [url] (on [host] and its parent domains). */
fun WebViewActivity.deleteSiteCookies(url: String, host: String) {
    val manager = WebProfiles.cookieManager() ?: return
    val cookies = manager.getCookie(url) ?: return
    val domains = ArrayList<String>()
    var d = host
    while (d.count { it == '.' } >= 1) { domains.add(d); d = d.substringAfter('.') }
    for (part in cookies.split(';')) {
        val name = part.substringBefore('=').trim()
        if (name.isEmpty()) continue
        manager.setCookie(url, "$name=; Max-Age=0; Path=/")
        for (dom in domains) manager.setCookie(url, "$name=; Max-Age=0; Path=/; Domain=$dom")
    }
    manager.flush()
}

/**
 * Two WebView speed-ups (2026-11-21; prerendering is still experimental in androidx.webkit; no effect where the
 * WebView lacks them):
 * - the back/forward cache: a page left by a link inside the same web view stays frozen in memory for a while, so
 *   Back shows it at once without reloading; Chromium drops such pages by itself after a few minutes and when memory
 *   is short;
 * - prerendering of the pages a site marks as likely next (speculation rules), when the memory mode allows it
 *   ([com.mycompany.app.pref.store.MemoryPlan.prerender]).
 */
@androidx.annotation.OptIn(androidx.webkit.WebSettingsCompat.ExperimentalSpeculativeLoading::class)
fun enableEngineCaches(settings: android.webkit.WebSettings) {
    try {
        if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.BACK_FORWARD_CACHE)) {
            androidx.webkit.WebSettingsCompat.setBackForwardCacheEnabled(settings, true)
        }
    } catch (_: Exception) {
    }
    try {
        if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.SPECULATIVE_LOADING)) {
            androidx.webkit.WebSettingsCompat.setSpeculativeLoadingStatus(
                settings,
                if (com.mycompany.app.pref.store.MemoryPlan.prerender()) androidx.webkit.WebSettingsCompat.SPECULATIVE_LOADING_PRERENDER_ENABLED
                else androidx.webkit.WebSettingsCompat.SPECULATIVE_LOADING_DISABLED,
            )
        }
    } catch (_: Exception) {
    }
}
