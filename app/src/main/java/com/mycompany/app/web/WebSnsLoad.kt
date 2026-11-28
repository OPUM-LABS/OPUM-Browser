package com.mycompany.app.web

import android.view.View

import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.indexOfCharIn
import com.mycompany.app.main.MainUtil.openHeadConnection
import com.mycompany.app.main.MainUtil.userAgentPref
import com.mycompany.app.main.MainUtil.nativeJsN
import com.mycompany.app.main.MainUtil.githubRawToRepo
import com.mycompany.app.main.MainUtil.wildcardHost
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.indexOfWord
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.matchesNativeHost
import com.mycompany.app.main.MainUtil.isFilmizle
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.isYouTubeHost
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.openUriInput
import com.mycompany.app.main.MainUtil.applyZoomSettings
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.isEmgUrl
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.isHmgPath
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.isHmgUrl
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.parseJson
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainUtil.snsJs2
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.getTwitReq
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
import com.mycompany.app.main.MainUtil.isLockEnabled
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.stripHitomiTitle
import com.mycompany.app.main.MainUtil.parseFontList
import com.mycompany.app.main.MainUtil.imageUrlFallback
import com.mycompany.app.main.MainUtil.extractUrl
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.urlInParens
import com.mycompany.app.main.MainUtil.pinterestOriginal
import com.mycompany.app.main.MainUtil.siteNameOf
import com.mycompany.app.main.MainUtil.hostLabelUpper
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.fileNumberSuffix
import com.mycompany.app.main.MainUtil.comicSiteKind
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.extOf
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
import com.mycompany.app.main.MainUtil.mediaNameFor
import com.mycompany.app.main.MainUtil.viewportJs
import com.mycompany.app.main.MainUtil.isImagePath
import com.mycompany.app.main.MainUtil.injectOverlayJs
import com.mycompany.app.main.MainUtil.isHitomi
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.applyDesktopMode
import com.mycompany.app.main.MainUtil.backupBitmapCopy
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.main.MainUtil.initUserAgent
import com.mycompany.app.main.MainUtil.defaultUserAgent
import com.mycompany.app.main.MainUtil.injectPhVideoJs
import com.mycompany.app.main.MainUtil.isOfflineMht
import com.mycompany.app.main.MainUtil.offlineUrlFor
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.shortHostOf
import com.mycompany.app.main.MainUtil.cachedIcon
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.resetTextSizePref
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
import com.mycompany.app.main.MainUtil.instaAid
import com.mycompany.app.main.MainUtil.instaMid1
import com.mycompany.app.main.MainUtil.instaMid2
import com.mycompany.app.main.MainUtil.getInstaData
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.translateUrlFor
import com.mycompany.app.main.MainUtil.hasVideoButton
import com.mycompany.app.main.MainUtil.pipAspectRatio
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.main.MainUtil.countLabel
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
import com.mycompany.app.main.MainUtil.setActivityOrientation
import com.mycompany.app.main.MainUtil.applyWindowBrightness
import com.mycompany.app.main.MainUtil.injectWindowOpenJs
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.applyVideoRotation
import com.mycompany.app.main.MainUtil.applySystemUiFlags
import com.mycompany.app.main.MainUtil.applyDisplayCutout
import com.mycompany.app.main.MainUtil.applyScreenRotation
import com.mycompany.app.main.MainUtil.hasPermission
import com.mycompany.app.main.MainUtil.openPermissionScreen
import com.mycompany.app.main.MainUtil.parseFloatSafe
import com.mycompany.app.main.MainUtil.parseFloatOr
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.insetsController
import com.mycompany.app.main.MainUtil.toggleSystemBars
import com.mycompany.app.main.MainUtil.applySystemBarsMode
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.isYouTubeShorts
import com.mycompany.app.main.MainUtil.hasZtwoFlag
import com.mycompany.app.main.MainUtil.initMaxTextureSize
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.main.MainUtil.scriptNameFor
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.flushCookies
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.filesSubdir
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.youTubeAutoplayJs
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openKeyboard
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.main.MainUtil.pageTitleForFile
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.applyBarAppearance
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.openIntentUri
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.handleDownloadLink
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.main.MainUtil.isHitomiReader
import com.mycompany.app.main.MainUtil.isPdfUrl
import com.mycompany.app.main.MainUtil.openInExternalApp
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.clipboardText
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.notifyBookUpdate
import com.mycompany.app.main.MainUtil.pageUrlOrNull
import com.mycompany.app.main.MainUtil.isGoogleUserContent
import com.mycompany.app.main.MainUtil.openVideoWithApp
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.nextPageUrl
import com.mycompany.app.main.MainUtil.isPhnGif
import com.mycompany.app.main.MainUtil.phTitle
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.openWithChooser
import com.mycompany.app.main.MainUtil.quickViewPdf
import com.mycompany.app.main.MainUtil.glideUrlFor
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.shareText
import com.mycompany.app.main.MainUtil.freeMemory
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.openCustomTab
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.transLangFromCookie
import com.mycompany.app.main.MainUtil.parseTransResult
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.hideDivsByClass
import com.mycompany.app.main.MainUtil.evalConfirmJs
import com.mycompany.app.main.MainUtil.areaMultiJs
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.main.MainUtil.areaUpScript
import com.mycompany.app.main.MainUtil.hasClipboard
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.requestCamera
import com.mycompany.app.main.MainUtil.newMediaUri
import com.mycompany.app.main.MainUtil.getContent
import com.mycompany.app.main.MainUtil.pickImages
import com.mycompany.app.main.MainUtil.pickContent
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.main.MainUtil.clearCookies
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.applyNightMode
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.main.MainUtil.isDarkResources
import com.mycompany.app.main.MainUtil.openPlayStore
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
import com.mycompany.app.main.MainUtil.lockAppData
import com.mycompany.app.main.MainUtil.isAudioPlaying
import com.mycompany.app.main.MainUtil.findMediaJs
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.main.MainUtil.checkTransJs
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
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.schemeKind
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.addShortcut
import com.mycompany.app.main.MainUtil.getPassJs3
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.checkTransClassJs
import com.mycompany.app.main.MainUtil.parseColorText
import com.mycompany.app.main.MainUtil.lighterColor
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.hasFragmentPage
import com.mycompany.app.main.MainUtil.isFakeStream
import com.mycompany.app.main.MainUtil.firstUrlIn
import com.mycompany.app.main.MainUtil.themeIconColor
import com.mycompany.app.main.MainUtil.hasTopTitle
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.main.MainUtil.hasTopTabBar
import com.mycompany.app.main.MainUtil.getAdsHamJs
import com.mycompany.app.main.MainUtil.injectVideoObserver
import com.mycompany.app.main.MainUtil.needsDesktopUa
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.main.MainUtil.iconResFor
import com.mycompany.app.main.MainUtil.hasBottomTitle
import com.mycompany.app.main.MainUtil.windowStatusColor
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUtil.setWindowStatusColor
import com.mycompany.app.main.MainUtil.setNavBarColor
import com.mycompany.app.main.MainUtil.isRootPath
import com.mycompany.app.main.MainUtil.isXLoginPage
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.urlFromText
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.main.MainUtil.navBarInset
import com.mycompany.app.main.MainUtil.viewScreenX
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.clearHistoryData
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUtil.editTextValue
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
import com.mycompany.app.main.MainUtil.userFontResponse
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUtil.mimeForPath
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.MainUtil.readTextFile
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.main.MainUtil.highlightText
import com.mycompany.app.main.MainUtil.durationText
import com.mycompany.app.main.MainUtil.screenBrightnessPercent
import android.util.SparseArray
import com.mycompany.app.web.WebClean
import com.mycompany.app.dialog.DialogSetFilter
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.app.NotificationManager
import com.mycompany.app.db.book.DbAdsCmd
import android.app.NotificationChannel
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.compress.Compress
import com.mycompany.app.main.MainFilterDown
import com.mycompany.app.web.WebSearch
import com.mycompany.app.web.WebLoadWrap
import android.view.ViewGroup
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebEmgTask
import com.mycompany.app.web.WebEmgLoad
import android.webkit.JavascriptInterface
import com.mycompany.app.main.MainActivity
import com.mycompany.app.data.DataUrl
import android.webkit.WebViewClient
import android.webkit.WebResourceError
import android.webkit.RenderProcessGoneDetail
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.web.NetError
import com.mycompany.app.web.WebHmgTask
import android.util.SparseIntArray
import com.mycompany.app.web.WebHmgLoad
import com.mycompany.app.web.WebSnsLoad.SnsLoadListener
import com.mycompany.app.view.MyWebSafe
import com.mycompany.app.web.WebSnsTask
import com.mycompany.app.web.WebSnsTwit
import com.mycompany.app.web.WebSnsInsta
import com.mycompany.app.web.WebSnsLoad
import com.mycompany.app.web.WebSnsTask.SnsTaskListener
import org.json.JSONObject
import org.json.JSONArray
import com.mycompany.app.main.MainDownAdapter.DownListItem
import android.webkit.ValueCallback
import android.webkit.WebSettings
import org.json.JSONException
import android.widget.RelativeLayout
import android.webkit.SslErrorHandler
import android.net.http.SslError
import com.mycompany.app.web.WebSslView
import android.net.http.SslCertificate
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WebVkvLoad
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mycompany.app.web.WebLauncher
import android.os.Bundle
import android.content.Intent
import com.mycompany.app.web.WebLoadTask
import android.text.Spanned
import android.text.Html
import com.mycompany.app.web.WebLoadView
import android.webkit.WebChromeClient
import android.animation.ValueAnimator
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.quick.QuickView
import com.mycompany.app.web.WebCrashView
import com.mycompany.app.main.MainWebDestroy
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.data.book.DataBookOver
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.db.DbUtil
import com.mycompany.app.quick.QuickAdapter
import com.mycompany.app.db.book.DbTabState
import android.os.Looper
import com.mycompany.app.data.book.DataBookAds
import android.view.MotionEvent
import com.mycompany.app.data.book.DataBookJava
import android.view.ViewParent
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefMain
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.os.Build
import com.mycompany.app.quick.QuickControl
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import android.webkit.WebHistoryItem
import android.webkit.WebBackForwardList
import com.mycompany.app.web.WebReadTask
import net.dankito.readability4j.Article
import net.dankito.readability4j.Readability4J
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.web.WebShortcut
import android.app.Activity
import com.mycompany.app.web.WebShortTab
import com.mycompany.app.web.WebVideoPip
import android.content.IntentFilter
import com.mycompany.app.pref.PrefVideo
import android.app.PendingIntent
import android.app.RemoteAction
import android.app.PictureInPictureParams
import com.mycompany.app.web.WebGridAdapter
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.dialog.DialogImageType
import com.mycompany.app.dialog.DialogDownList
import com.mycompany.app.dialog.DialogDownZip
import com.mycompany.app.dialog.DialogCreateAlbum
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import android.content.DialogInterface
import com.mycompany.app.web.WebGridDialog
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.pref.PrefImage
import com.mycompany.app.web.WebVideoFrame
import com.mycompany.app.web.WebVideoProgress
import android.view.GestureDetector
import com.mycompany.app.dialog.DialogSeekBright
import com.mycompany.app.dialog.DialogSeekAudio
import com.mycompany.app.web.WebVideoFull
import android.graphics.Typeface
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.pref.PrefEditor
import android.database.sqlite.SQLiteDatabase
import com.mycompany.app.web.WebVideoImage
import android.graphics.RectF
import android.view.ViewOutlineProvider
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.dialog.DialogTransLang
import com.mycompany.app.dialog.DialogWebSelect
import com.mycompany.app.dialog.DialogSetMsg
import com.mycompany.app.dialog.DialogQuickEdit
import com.mycompany.app.dialog.DialogTransMsg
import com.mycompany.app.dialog.DialogMenuMain
import com.mycompany.app.quick.QuickSubView
import com.mycompany.app.dialog.DialogViewTrans
import com.mycompany.app.dialog.DialogMenuList
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.dialog.DialogTabEdit
import com.mycompany.app.pull.MyPullView
import com.mycompany.app.main.MainFilterSvc
import com.mycompany.app.main.MainScriptSvc
import com.mycompany.app.wview.WebUpView
import com.mycompany.app.quick.QuickNews
import android.speech.tts.TextToSpeech
import com.mycompany.app.script.ScriptCache
import com.mycompany.app.dialog.DialogLoadImg
import com.mycompany.app.main.MainTransNews
import com.mycompany.app.dialog.DialogLoadEmg
import com.mycompany.app.dialog.DialogUpdateScript
import com.mycompany.app.wview.WebDownView
import com.mycompany.app.dialog.DialogLoadHmg
import com.mycompany.app.dialog.DialogVideoMenu
import com.mycompany.app.dialog.DialogVideoList
import com.mycompany.app.dialog.DialogSetDesk
import com.mycompany.app.dialog.DialogDownFont
import com.mycompany.app.wview.WebTtsView
import com.mycompany.app.dialog.DialogTabMenu
import com.mycompany.app.dialog.DialogSetUrl
import com.mycompany.app.dialog.DialogPreview
import com.mycompany.app.dialog.DialogOpenType
import com.mycompany.app.dialog.DialogPreImage
import com.mycompany.app.dialog.DialogPopupMenu
import com.mycompany.app.help.KeyHelper
import com.mycompany.app.dialog.DialogNewsMenu
import com.mycompany.app.dialog.DialogWebView
import com.mycompany.app.web.WebTabBarSubView
import com.mycompany.app.dialog.DialogSetRate
import com.mycompany.app.dialog.DialogSeekWebText
import com.mycompany.app.dialog.DialogNewsLocale
import com.mycompany.app.dialog.DialogAllowPopup
import com.mycompany.app.dialog.DialogSetFull
import com.mycompany.app.dialog.DialogNewsSearch
import com.mycompany.app.dialog.DialogBlockLink
import com.mycompany.app.dialog.DialogSetDark
import com.mycompany.app.dialog.DialogBlockImage
import com.mycompany.app.wview.WebMoveText
import com.mycompany.app.dialog.DialogSetUseTts
import com.mycompany.app.web.WebNestLayout
import com.mycompany.app.dialog.DialogWebBookEdit
import com.mycompany.app.wview.WebAreaText
import com.mycompany.app.dialog.DialogSetAdblock
import com.mycompany.app.wview.WebAreaMenu
import com.mycompany.app.dialog.DialogSetJava
import com.mycompany.app.wview.WebAreaView
import com.mycompany.app.dialog.DialogSetTmem
import com.mycompany.app.dialog.DialogEditShort
import com.mycompany.app.behavior.MyBehaviorTop
import com.mycompany.app.dialog.DialogUrlLink
import com.mycompany.app.dialog.DialogViewRead
import com.mycompany.app.behavior.MyBehaviorBot
import com.mycompany.app.dialog.DialogGuideNoti
import android.print.PrintDocumentAdapter
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.dialog.DialogViewSrc
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogGuideArea
import com.mycompany.app.dialog.DialogSetPrivacy
import com.mycompany.app.dialog.DialogDownBlob
import com.mycompany.app.dialog.DialogEditArea
import com.mycompany.app.dialog.DialogSetCookie
import com.mycompany.app.dialog.DialogSetScrFil
import android.webkit.JsResult
import com.mycompany.app.dialog.DialogWebCerti
import com.mycompany.app.dialog.DialogPrompt
import android.webkit.PermissionRequest
import com.mycompany.app.dialog.DialogEditAuth
import com.mycompany.app.dialog.DialogPermission
import android.webkit.GeolocationPermissions
import com.mycompany.app.quick.QuickSearch
import com.mycompany.app.dialog.DialogCapture
import com.mycompany.app.dialog.DialogDownPage
import com.mycompany.app.quick.QuickSchEdit
import com.mycompany.app.dialog.DialogPrintPage
import com.mycompany.app.web.WebSearchAdapter
import com.mycompany.app.dialog.DialogSetTrans
import com.mycompany.app.dialog.DialogPassInfo
import android.view.ViewTreeObserver
import android.webkit.DownloadListener
import com.mycompany.app.quick.TabDragListener
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.quick.QuickNewsCustom
import com.mycompany.app.quick.QuickNewsGoogle
import com.mycompany.app.db.book.DbBookQuick
import android.content.ContentValues
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.script.ScriptCriteria
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookWeb
import android.content.ActivityNotFoundException
import com.mycompany.app.main.MainUri
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.ui.settings.SettingItem
import android.graphics.drawable.BitmapDrawable
import com.mycompany.app.db.book.DbBookPage
import android.print.PrintManager
import android.print.PrintAttributes
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import com.mycompany.app.data.DataTrans
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.book.DataBookRecent
import com.mycompany.app.db.book.DbBookPms
import com.mycompany.app.db.book.DbBookPass
import android.webkit.WebViewDatabase
import com.mycompany.app.pref.PrefList
import android.text.style.ClickableSpan
import com.mycompany.app.dialog.ListTabListener
import com.mycompany.app.widget.WidgetSearchProviderLight
import com.mycompany.app.widget.WidgetSearchProviderDark
import android.speech.tts.UtteranceProgressListener
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
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import android.webkit.JsPromptResult
import com.mycompany.app.main.MainNative
import android.webkit.MimeTypeMap
import com.mycompany.app.script.WebViewXmlHttpRequest
import com.mycompany.app.script.ScriptId
import com.mycompany.app.pref.PrefFloat
import com.mycompany.app.pref.PrefPath
import android.webkit.WebStorage
import org.chromium.support_lib_boundary.util.BoundaryInterfaceReflectionUtil
import org.chromium.support_lib_boundary.WebStorageBoundaryInterface
import com.google.gson.JsonElement
import com.mycompany.app.main.MainUrl
import com.mycompany.app.data.DataAlbum
import com.mycompany.app.data.DataPdf
import com.mycompany.app.data.DataCmp
import com.mycompany.app.dialog.DialogSaveSource
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.main.MainSelectAdapter
import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.LayoutInflater
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import java.lang.Exception
import java.util.ArrayList

@Suppress("deprecation")
class WebSnsLoad(
    mainActivity: android.app.Activity?,
    viewGroup: ViewGroup?,
    webNestView: WebNestView?,
    mediaUrl: String?,
    requestId: Int,
    snsType: Int,
    instaMode: Int,
    snsLoadListener: SnsLoadListener?
) {
    @JvmField
    var storyJson: String? = null
    @JvmField
    var docHtml: String? = null
    @JvmField
    var hostActivity: android.app.Activity? = null
    @JvmField
    var appContext: Context? = null
    @JvmField
    var loadListener: SnsLoadListener? = null
    @JvmField
    var parentView: ViewGroup? = null
    @JvmField
    var webSafe: MyWebSafe? = null
    @JvmField
    var pageLoading: Boolean = false
    @JvmField
    var requestId: Int = 0
    @JvmField
    var snsType: Byte = 0
    @JvmField
    var mediaUrl: String? = null
    @JvmField
    var siteKind: Byte = 0
    @JvmField
    var pageUrl: String? = null
    @JvmField
    var jsPart1: String? = null
    @JvmField
    var jsPart2: String? = null
    @JvmField
    var webNest: WebNestView? = null
    @JvmField
    var instaMode: Byte = 0
    @JvmField
    var storyPending: Boolean = false
    @JvmField
    var loading: Boolean = false
    @JvmField
    var retryPosted: Boolean = false
    @JvmField
    var retryCount: Int = 0
    @JvmField
    var snsTask: WebSnsTask? = null
    @JvmField
    var twitLoad: WebSnsTwit? = null
    @JvmField
    var instaLoad: WebSnsInsta? = null
    @JvmField
    var urlLoading: Boolean = false
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var storyFound: Boolean = false
    @JvmField
    val storyTimeout: Runnable = StartCheckTask()
    @JvmField
    val docHtmlRunnable: Runnable = DocHtmlTask()

    internal inner class StartLoadTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            if (webSnsLoad.snsType.toInt() != 1) {
                webSnsLoad.startWeb()
                return
            }
            if (webSnsLoad.instaMode.toInt() == 1) {
                webSnsLoad.storyPending = true
                webSnsLoad.startWeb()
                return
            }
            val viewGroup: ViewGroup? = if (webSnsLoad.loading && webSnsLoad.instaLoad == null) webSnsLoad.parentView else null
            if (webSnsLoad.loading && webSnsLoad.instaLoad == null && viewGroup != null) {
                val mainActivity = webSnsLoad.hostActivity
                val mediaUrl = webSnsLoad.mediaUrl
                val instaListener = webSnsLoad.InstaTaskListener()
                val webSnsInsta = WebSnsInsta()
                if (mainActivity != null) {
                    webSnsInsta.hostActivity = mainActivity
                    webSnsInsta.appContext = mainActivity.getApplicationContext()
                    webSnsInsta.taskListener = instaListener
                    webSnsInsta.parentView = viewGroup
                    webSnsInsta.pageUrl = mediaUrl
                    viewGroup.post(webSnsInsta.CreateWebTask())
                }
                webSnsLoad.instaLoad = webSnsInsta
            }
        }
    }

    internal inner class InstaTaskListener : SnsTaskListener {

        internal inner class ReleaseInstaStep : Runnable {
            override fun run() {
                val webSnsLoad = this@WebSnsLoad
                val webSnsInsta = webSnsLoad.instaLoad
                if (webSnsInsta != null) {
                    webSnsInsta.release()
                    webSnsLoad.instaLoad = null
                }
                if (webSnsLoad.instaMode.toInt() == 2) {
                    val webNestView: WebNestView? = if (!webSnsLoad.loading) null else webSnsLoad.webNest
                    if (!webSnsLoad.loading || webNestView == null) {
                        return
                    }
                    webNestView.evaluateJavascript(
                        "(function(){var vds=document.querySelectorAll(\"video\");if(vds&&(vds.length>0)){for(var i=0;i<vds.length;i++){if(!vds[i].paused){return vds[i].src;}}}return null;})();",
                        webSnsLoad.PageItemsCallback()
                    )
                    return
                }
                val snsLoadListener = webSnsLoad.loadListener
                if (snsLoadListener != null) {
                    snsLoadListener.onStart()
                }
                webSnsLoad.startWeb()
            }
        }

        override fun onResult(arrayList: ArrayList<DownListItem?>?) {
            val webSnsLoad = this@WebSnsLoad
            if (arrayList != null && !arrayList.isEmpty()) {
                webSnsLoad.deliver(arrayList)
                return
            }
            val viewGroup = webSnsLoad.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.post(ReleaseInstaStep())
        }
    }

    internal inner class StoryDataTask : Runnable {

        internal inner class StartCheckStep : Runnable {
            override fun run() {
                val webSnsLoad = this@WebSnsLoad
                if (webSnsLoad.storyPending) {
                    webSnsLoad.storyPending = false
                    webSnsLoad.startCheck()
                }
            }
        }

        /** Instagram Story JSON (A): one "Insta Story n" entry per item with a video; otherwise fall back.  */
        override fun run() {
            val load = this@WebSnsLoad
            val json = load.storyJson
            load.storyJson = null
            if (load.parentView == null) {
                return
            }
            var list: ArrayList<DownListItem?>? = null
            if (!TextUtils.isEmpty(json)) {
                try {
                    val root = parseJson(json)
                    val items: JSONArray? = if (root != null) WebSnsTask.Companion.findMediaArray(root) else null
                    if (items != null) {
                        val length = items.length()
                        var index = 1
                        var found: ArrayList<DownListItem?>? = null
                        try {
                            for (k in 0..<length) {
                                val item = items.getJSONObject(k)
                                if (item == null) {
                                    continue
                                }
                                var video: String?
                                var thumb: String?
                                try {
                                    video = item.getJSONArray("video_versions").getJSONObject(0)
                                        .getString("url")
                                    try {
                                        thumb = item.getJSONObject("image_versions2")
                                            .getJSONArray("candidates").getJSONObject(0)
                                            .getString("url")
                                    } catch (unused: Exception) {
                                        thumb = null
                                    }
                                } catch (unused2: Exception) {
                                    video = null
                                    thumb = null
                                }
                                if (TextUtils.isEmpty(video)) {
                                    continue
                                }
                                val label =
                                    if (index == 1) "Insta Story" else "Insta Story " + index
                                val entry = DownListItem(index, label, "MP4")
                                entry.url = video
                                entry.thumb = thumb
                                if (found == null) {
                                    found = ArrayList()
                                }
                                found.add(entry)
                                index++
                            }
                        } catch (unused3: Exception) {
                        }
                        list = found
                    }
                } catch (unused4: Exception) {
                    list = null
                }
            }
            if (list != null && !list.isEmpty()) {
                load.deliver(list)
                return
            }
            load.storyFound = false
            if (load.parentView != null) {
                load.parentView!!.post(StartCheckStep())
            }
        }
    }

    internal inner class StartCheckTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            if (webSnsLoad.storyFound || !webSnsLoad.storyPending) {
                return
            }
            webSnsLoad.storyPending = false
            webSnsLoad.startCheck()
        }
    }

    internal inner class PageItemsCallback : ValueCallback<String> {

        internal inner class StartWebStep : Runnable {
            override fun run() {
                this@WebSnsLoad.startWeb()
            }
        }

        override fun onReceiveValue(value: String?) {
            val text = stripQuotes(value)
            var arrayList: ArrayList<DownListItem?>? = null
            if (URLUtil.isNetworkUrl(text)) {
                val items: ArrayList<DownListItem?> = ArrayList()
                val downListItem = DownListItem(0, null, "MP4")
                downListItem.url = text
                items.add(downListItem)
                arrayList = items
            }
            val webSnsLoad = this@WebSnsLoad
            if (arrayList != null && !arrayList.isEmpty()) {
                webSnsLoad.deliver(arrayList)
                return
            }
            val viewGroup = webSnsLoad.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.post(StartWebStep())
        }
    }

    internal inner class LoadDocHtmlTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            val docHtml = webSnsLoad.docHtml
            webSnsLoad.docHtml = null
            val myWebSafe: MyWebSafe? = if (TextUtils.isEmpty(docHtml)) null else webSnsLoad.webSafe
            if (TextUtils.isEmpty(docHtml) || myWebSafe == null) {
                return
            }
            var webSnsTask = webSnsLoad.snsTask
            if (webSnsTask == null) {
                val snsType = webSnsLoad.snsType
                val deliverListener = webSnsLoad.DeliverListener()
                webSnsTask = WebSnsTask()
                webSnsTask.snsMode = snsType
                webSnsTask.taskListener = deliverListener
                webSnsLoad.snsTask = webSnsTask
            }
            val context = webSnsLoad.appContext
            val url = myWebSafe.getUrl()
            val loadTask = webSnsTask.loadTask
            if (loadTask != null) {
                loadTask.cancelled = true
            }
            webSnsTask.loadTask = null
            val newLoadTask = WebSnsTask.LoadTask(webSnsTask, url, docHtml)
            webSnsTask.loadTask = newLoadTask
            newLoadTask.start(context)
        }
    }

    internal inner class DeliverListener : SnsTaskListener {
        /** Page-scrape result: show the variants, or try again in a second.  */
        override fun onResult(arrayList: ArrayList<DownListItem?>?) {
            val load = this@WebSnsLoad
            if (arrayList != null && !arrayList.isEmpty()) {
                load.deliver(arrayList)
                return
            }
            if (load.webSafe != null) {
                load.webSafe!!.removeCallbacks(load.docHtmlRunnable)
                load.webSafe!!.postDelayed(load.docHtmlRunnable, 1000L)
            }
        }
    }

    internal inner class DocHtmlTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            if (webSnsLoad.loading) {
                evalJs(
                    webSnsLoad.webSafe,
                    "(function(){android.onDocHtml(document.body.innerHTML);})();",
                    false
                )
            }
        }
    }

    internal inner class RetryLoadTask : Runnable {

        internal inner class RetryLoadStep : Runnable {

            internal inner class PrepareRetryStep : Runnable {

                internal inner class StartCheckLaterStep : Runnable {
                    override fun run() {
                        this@WebSnsLoad.startCheck()
                    }
                }

                override fun run() {
                    val retryLoadTask = this@RetryLoadTask
                    val webSnsLoad = this@WebSnsLoad
                    val myWebSafe = webSnsLoad.webSafe
                    if (myWebSafe != null) {
                        webSnsLoad.urlLoading = true
                        myWebSafe.addJavascriptInterface(webSnsLoad.WebAppInterface(), "android")
                        val viewGroup = this@WebSnsLoad.parentView
                        if (viewGroup == null) {
                            return
                        }
                        viewGroup.post(StartCheckLaterStep())
                    }
                }
            }

            override fun run() {
                val webSnsLoad = this@WebSnsLoad
                val myWebSafe = webSnsLoad.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(webSnsLoad.LocalWebViewClient())
                    val viewGroup = webSnsLoad.parentView
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(PrepareRetryStep())
                }
            }
        }

        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            val myWebSafe = webSnsLoad.webSafe
            if (myWebSafe != null) {
                applyZoomSettings(myWebSafe, true)
                val viewGroup = webSnsLoad.parentView
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(RetryLoadStep())
            }
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            val myWebSafe = webSnsLoad.webSafe
            if (myWebSafe == null) {
                return
            }
            webSnsLoad.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            val myWebSafe = webSnsLoad.webSafe
            if (myWebSafe == null) {
                return
            }
            webSnsLoad.urlLoading = true
            myWebSafe.addJavascriptInterface(webSnsLoad.WebAppInterface(), "android")
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            MainUtil.clearWebCache(webSnsLoad.webSafe)
            webSnsLoad.finishHandled = false
        }
    }

    internal inner class ReportErrorTask : Runnable {
        override fun run() {
            val webSnsLoad = this@WebSnsLoad
            val snsLoadListener = webSnsLoad.loadListener
            if (snsLoadListener != null) {
                snsLoadListener.onError(webSnsLoad.snsType.toInt())
            }
        }
    }

    internal inner class CheckValueTask : Runnable {
        override fun run() {
            this@WebSnsLoad.checkValue()
        }
    }

    internal inner class CheckValueCallback : ValueCallback<String> {

        internal inner class RetryCheckStep : Runnable {
            override fun run() {
                val webSnsLoad = this@WebSnsLoad
                val retryCount = webSnsLoad.retryCount + 1
                webSnsLoad.retryCount = retryCount
                if (retryCount <= 10) {
                    webSnsLoad.checkValue()
                    return
                }
                val snsLoadListener = webSnsLoad.loadListener
                if (snsLoadListener != null) {
                    snsLoadListener.onError(webSnsLoad.snsType.toInt())
                }
            }
        }

        internal inner class InjectJsPartStep : Runnable {
            override fun run() {
                val webSnsLoad = this@WebSnsLoad
                if (webSnsLoad.webSafe != null) {
                    if (TextUtils.isEmpty(webSnsLoad.jsPart2)) {
                        webSnsLoad.jsPart2 = snsJs2(webSnsLoad.siteKind.toInt())
                    }
                    evalJsNow(webSnsLoad.webSafe, webSnsLoad.jsPart2, true)
                    val runnable = webSnsLoad.docHtmlRunnable
                    val myWebSafe = webSnsLoad.webSafe
                    if (myWebSafe != null) {
                        myWebSafe.removeCallbacks(runnable)
                        webSnsLoad.webSafe!!.postDelayed(runnable, 1000L)
                    }
                }
            }
        }

        override fun onReceiveValue(value: String?) {
            val valueText = value
            val webSnsLoad = this@WebSnsLoad
            val mediaUrl = webSnsLoad.mediaUrl
            if (TextUtils.isEmpty(mediaUrl)) {
                return
            }
            if (TextUtils.isEmpty(valueText) || !valueText!!.contains(mediaUrl!!)) {
                val myWebSafe = webSnsLoad.webSafe
                if (myWebSafe == null) {
                    return
                }
                myWebSafe.postDelayed(RetryCheckStep(), 400L)
                return
            }
            webSnsLoad.retryCount = 0
            val myWebSafe = webSnsLoad.webSafe
            if (myWebSafe != null) {
                myWebSafe.postDelayed(InjectJsPartStep(), 400L)
            }
        }
    }

    internal inner class ReleaseStep : SnsTaskListener {

        internal inner class ReleaseTwitStep : Runnable {
            override fun run() {
                val webSnsLoad = this@WebSnsLoad
                val webSnsTwit = webSnsLoad.twitLoad
                if (webSnsTwit != null) {
                    webSnsTwit.release()
                    webSnsLoad.twitLoad = null
                }
                webSnsLoad.startWeb()
            }
        }

        override fun onResult(arrayList: ArrayList<DownListItem?>?) {
            val webSnsLoad = this@WebSnsLoad
            if (arrayList != null && !arrayList.isEmpty()) {
                webSnsLoad.deliver(arrayList)
                return
            }
            val viewGroup = webSnsLoad.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.post(ReleaseTwitStep())
        }
    }

    interface SnsLoadListener {
        fun onStart()

        fun onResult(pickIndex: Int, arrayList: ArrayList<DownListItem?>?)

        fun onError(errorType: Int)
    }

    private inner class WebAppInterface {
        @JavascriptInterface
        fun onDocHtml(docHtml: String?) {
            val webSnsLoad = this@WebSnsLoad
            webSnsLoad.docHtml = docHtml
            val viewGroup = webSnsLoad.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.post(webSnsLoad.LoadDocHtmlTask())
        }

        @JavascriptInterface
        fun onStoryData(storyJson: String?) {
            val webSnsLoad = this@WebSnsLoad
            val runnable = webSnsLoad.storyTimeout
            val viewGroup = webSnsLoad.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.removeCallbacks(runnable)
            if (TextUtils.isEmpty(storyJson)) {
                webSnsLoad.storyFound = false
                webSnsLoad.parentView!!.postDelayed(runnable, 5000L)
            } else {
                webSnsLoad.storyFound = true
                webSnsLoad.storyJson = storyJson
                MainApp.runInBackground(webSnsLoad.appContext, webSnsLoad.StoryDataTask())
            }
        }
    }

     /**
     * source: 0 Twitter (WebSnsTwit helper), 1 another WebView page (webNestView, with its mode), else a URL
     * loaded in a hidden WebView.
     */
    init {
        run init@{
        if (mainActivity == null || viewGroup == null) {
            return@init
        }
        this.hostActivity = mainActivity
        this.appContext = mainActivity.getApplicationContext()
        this.loadListener = snsLoadListener
        this.parentView = viewGroup
        this.requestId = requestId
        this.snsType = snsType.toByte()
        this.mediaUrl = mediaUrl
        this.loading = true
        this.retryPosted = false
        this.retryCount = 0
        if (snsType == 0) {
            this.siteKind = 2
            if (this.twitLoad != null) {
                return@init
            }
            val twit = WebSnsTwit()
            if (this.hostActivity != null) {
                twit.hostActivity = this.hostActivity
                twit.appContext = this.hostActivity!!.getApplicationContext()
                twit.taskListener = ReleaseStep()
                twit.parentView = viewGroup
                twit.pageUrl = mediaUrl
                try {
                    var keys: MutableList<Any?>? = null
                    if (partsReady(4, MainNative.storyKeys)) {
                        keys = MainNative.storyKeys
                    } else {
                        var built: ArrayList<Any?>? = ArrayList<Any?>()
                        for (k in 0..3) {
                            val key = getTwitReq(k)
                            if (TextUtils.isEmpty(key)) {
                                built = null
                                break
                            }
                            built!!.add(key)
                        }
                        if (built != null) {
                            MainNative.storyKeys = built
                        }
                        keys = built
                    }
                    if (keys != null && keys.size == 4) {
                        twit.matchKey1 = keys.get(0) as String?
                        twit.matchKey2 = keys.get(1) as String?
                        twit.matchKey3 = keys.get(2) as String?
                        twit.matchKey4 = keys.get(3) as String?
                    }
                } catch (unused: Exception) {
                }
                if (twit.parentView != null) {
                    twit.parentView!!.post(twit.CreateWebTask())
                }
            }
            this.twitLoad = twit
            return@init
        }
        if (snsType == 1) {
            this.siteKind = 1
            this.webNest = webNestView
            this.instaMode = instaMode.toByte()
        } else {
            this.siteKind = 0
        }
        viewGroup.post(StartLoadTask())

        }
    }

    fun loadUrl(pageUrl: String?) {
        if (this.webSafe != null) {
            val isHttp = isHttpUrl(pageUrl)
            val urlLoading = this.urlLoading
            if (isHttp) {
                if (urlLoading) {
                    this.urlLoading = false
                    val myWebSafe = this.webSafe
                    if (myWebSafe != null) {
                        myWebSafe.post(RemoveJsBridgeTask())
                        return
                    }
                    return
                }
                return
            }
            if (urlLoading) {
                return
            }
            this.urlLoading = true
            val myWebSafe = this.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(AddJsBridgeTask())
        }
    }

    fun startWeb() {
        if (this.hostActivity == null || this.parentView == null || this.webSafe != null) {
            return
        }
        val startUrl = snsUrl(this.siteKind.toInt())
        this.pageUrl = startUrl
        if (!URLUtil.isNetworkUrl(startUrl)) {
            deliver(null)
            return
        }
        if (PdfSettings.debugMode.value) {
            WebView.setWebContentsDebuggingEnabled(false)
        }
        val myWebSafe = MyWebSafe(this.hostActivity)
        this.webSafe = myWebSafe
        MainApp.resumeWebTimers(this.appContext, myWebSafe)
        this.webSafe!!.setVisibility(View.INVISIBLE)
        this.parentView!!.addView(this.webSafe, 0, ViewGroup.LayoutParams(-1, -1))
        this.parentView!!.post(RetryLoadTask())
    }

    fun startCheck() {
        val myWebSafe = this.webSafe
        if (myWebSafe == null) {
            return
        }
        this.loading = true
        this.retryPosted = false
        this.retryCount = 0
        if (this.storyPending) {
            myWebSafe.loadUrl(this.mediaUrl!!)
        } else {
            myWebSafe.loadUrl(this.pageUrl!!)
        }
    }

    fun checkValue() {
        if (this.webSafe == null) {
            return
        }
        if (TextUtils.isEmpty(this.jsPart1)) {
            this.jsPart1 = snsJs1(this.siteKind.toInt())
        }
        this.webSafe!!.evaluateJavascript(
            this.jsPart1 + this.mediaUrl + "';return ele.value;}else{return 0;}})();",
            CheckValueCallback()
        )
    }

    fun deliver(arrayList: ArrayList<DownListItem?>?) {
        if (this.loading) {
            this.loading = false
            this.retryPosted = false
            this.retryCount = 0
            val snsLoadListener = this.loadListener
            if (snsLoadListener != null) {
                snsLoadListener.onResult(this.requestId, arrayList)
            }
        }
    }

    private inner class LocalWebViewClient : WebViewClient() {

        inner class PageFinishedStep : Runnable {
            override fun run() {
                val webSnsLoad = this@WebSnsLoad
                val myWebSafe = webSnsLoad.webSafe
                if (myWebSafe != null) {
                    if (webSnsLoad.storyPending) {
                        if (webSnsLoad.loading) {
                            evalJs(
                                myWebSafe,
                                "(function(){var vds=document.querySelectorAll(\"script[type='application/json']\");var txt=null;if(vds&&(vds.length>0)){for(var i=0;i<vds.length;i++){if(vds[i].innerText.includes('.mp4')){txt=vds[i].innerText;break;}}}android.onStoryData(txt);})();",
                                false
                            )
                            return
                        }
                        return
                    }
                    val viewGroup = webSnsLoad.parentView
                    if (viewGroup == null || webSnsLoad.retryPosted) {
                        return
                    }
                    webSnsLoad.retryPosted = true
                    webSnsLoad.retryCount = 0
                    viewGroup.postDelayed(webSnsLoad.CheckValueTask(), 400L)
                }
            }
        }

        override fun onPageFinished(webView: WebView?, url: String?) {
            val webSnsLoad = this@WebSnsLoad
            if (webSnsLoad.webSafe != null) {
                webSnsLoad.pageLoading = false
                webSnsLoad.loadUrl(url)
                if (!webSnsLoad.finishHandled && !TextUtils.isEmpty(url) && (url != webSnsLoad.lastFinishedUrl)) {
                    webSnsLoad.finishHandled = true
                    webSnsLoad.lastFinishedUrl = url
                    val viewGroup = webSnsLoad.parentView
                    if (viewGroup != null) {
                        viewGroup.post(webSnsLoad.ClearCacheTask())
                    }
                }
                val myWebSafe = webSnsLoad.webSafe
                if (myWebSafe != null) {
                    evalJsNow(
                        myWebSafe,
                        "(function(){if(document.head){var ele=document.createElement('style');ele.id='sb_cln_style';ele.innerText='ins[class*=\"adsbygoogle\"],ins[class*=\"adsbyadop\"],[id*=\"div-gpt-ad\"],amp-ad{display:none !important;}';document.head.appendChild(ele);}{var eles=document.querySelectorAll('ins[class*=\"adsbygoogle\"],ins[class*=\"adsbyadop\"],[id*=\"div-gpt-ad\"],amp-ad');if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){var ele=eles[i];var par=ele.parentNode;if(par){par.removeChild(ele);}else{ele.style.display='none';}}}}})();",
                        true
                    )
                }
                val viewGroup = webSnsLoad.parentView
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(PageFinishedStep())
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val webSnsLoad = this@WebSnsLoad
            if (webSnsLoad.webSafe != null) {
                webSnsLoad.pageLoading = true
                webSnsLoad.loadUrl(url)
                val myWebSafe = webSnsLoad.webSafe
                if (myWebSafe == null) {
                    return
                }
                evalJsNow(
                    myWebSafe,
                    "(function(){if(document.head){var ele=document.createElement('style');ele.id='sb_cln_style';ele.innerText='ins[class*=\"adsbygoogle\"],ins[class*=\"adsbyadop\"],[id*=\"div-gpt-ad\"],amp-ad{display:none !important;}';document.head.appendChild(ele);}{var eles=document.querySelectorAll('ins[class*=\"adsbygoogle\"],ins[class*=\"adsbyadop\"],[id*=\"div-gpt-ad\"],amp-ad');if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){var ele=eles[i];var par=ele.parentNode;if(par){par.removeChild(ele);}else{ele.style.display='none';}}}}})();",
                    true
                )
            }
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val webSnsLoad = this@WebSnsLoad
            webSnsLoad.webSafe = null
            onRenderGone(webView, renderProcessGoneDetail)
            val viewGroup = webSnsLoad.parentView
            if (viewGroup == null) {
                return true
            }
            viewGroup.post(webSnsLoad.ReportErrorTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val webSnsLoad = this@WebSnsLoad
            if (webSnsLoad.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                webSnsLoad.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val webSnsLoad = this@WebSnsLoad
            if (webSnsLoad.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    webSnsLoad.loadUrl(url)
                    webSnsLoad.webSafe!!.loadUrl(url)
                }
            }
            return true
        }
    }
}
