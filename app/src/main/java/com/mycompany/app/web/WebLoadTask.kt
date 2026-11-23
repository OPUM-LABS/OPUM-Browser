package com.mycompany.app.web

import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
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
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.openUriInput
import com.mycompany.app.main.MainUtil.applyZoomSettings
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.data.DataUrl.Companion.instance
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
import com.mycompany.app.data.book.DataBookOver.Companion.instance
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
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.main.MainUtil.injectPhVideoJs
import com.mycompany.app.main.MainUtil.isOfflineMht
import com.mycompany.app.main.MainUtil.offlineUrlFor
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.data.book.DataBookJava.Companion.instance
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
import com.mycompany.app.data.book.DataBookPop.Companion.instance
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
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaMultiJs
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
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.main.MainUtil.videoMenuCount
import com.mycompany.app.main.MainUtil.getPassJs1
import com.mycompany.app.main.MainUtil.getPassJs2
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
import org.jsoup.Jsoup
import com.mycompany.app.web.NetError
import com.mycompany.app.web.WebHmgTask
import android.util.SparseIntArray
import com.mycompany.app.web.WebHmgLoad
import com.mycompany.app.web.WebSnsTask
import com.mycompany.app.web.WebSnsTwit
import com.mycompany.app.web.WebSnsInsta
import com.mycompany.app.web.WebSnsLoad
import org.json.JSONObject
import org.json.JSONArray
import android.webkit.ValueCallback
import com.google.android.gms.common.internal.ImagesContract
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
import com.mycompany.app.web.WebLoadTask.WebLoadTaskListener
import com.mycompany.app.web.WebLoadTask
import com.mycompany.app.data.DataUrl.ImgCntItem
import android.text.Spanned
import android.text.Html
import androidx.webkit.URLUtilCompat
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
import com.mycompany.app.dialog.DialogSetRead
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnSuccessListener
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
import com.google.android.gms.security.ProviderInstaller
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
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.LayoutInflater
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import com.mycompany.app.web.WebSearchAdapter2
import org.jsoup.nodes.Document
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.ArrayList

@Suppress("deprecation")
class WebLoadTask {
    @JvmField
    var started: Boolean = false
    @JvmField
    var loadWeb: WebView? = null
    @JvmField
    var taskListener: WebLoadTaskListener? = null
    @JvmField
    var imageTask: LoadTask? = null
    @JvmField
    var loadState: Byte = 0
    @JvmField
    var netError: Boolean = false
    @JvmField
    var jsValue: String? = null
    @JvmField
    var pageHtml: String? = null

    internal inner class PageHtmlCallback : ValueCallback<String> {

        internal inner class ReadHtmlStep : Runnable {
            override fun run() {
                val webLoadTask = this@WebLoadTask
                val jsValue = webLoadTask.jsValue
                webLoadTask.jsValue = null
                if (webLoadTask.loadWeb != null) {
                    webLoadTask.pageHtml = MainUtil.unescapeJavaText(stripQuotes(jsValue))
                    val webView = webLoadTask.loadWeb
                    if (webView == null) {
                        return
                    }
                    webView.post(webLoadTask.ExtractImagesTask())
                }
            }
        }

        override fun onReceiveValue(value: String?) {
            val webLoadTask = this@WebLoadTask
            webLoadTask.jsValue = value
            val webView = webLoadTask.loadWeb
            if (webView == null) {
                return
            }
            MainApp.runInBackground(webView.getContext(), ReadHtmlStep())
        }
    }

    internal inner class ExtractImagesTask : Runnable {
        override fun run() {
            val webLoadTask = this@WebLoadTask
            val pageHtml = webLoadTask.pageHtml
            webLoadTask.pageHtml = null
            instance().extractImages(pageHtml)
        }
    }

    class LoadTask(webLoadTask: WebLoadTask?, baseUrl: String?, html: String?) : MyAsyncTask() {
        @JvmField
        var taskRef: WeakReference<*>? = null
        @JvmField
        var baseUrl: String? = null
        @JvmField
        var html: String? = null
        @JvmField
        var host: String? = null
        @JvmField
        var rawTitle: String? = null
        @JvmField
        var found: Boolean = false
        @JvmField
        var title: String? = null
        @JvmField
        var urls: ArrayList<String?>? = null
        @JvmField
        var keys: ArrayList<String?>? = null
        @JvmField
        var imgCount: ImgCntItem? = null
        @JvmField
        var mode: Int = 0
        @JvmField
        var sourceKind: Byte = 0
        @JvmField
        var isPinterest: Boolean = false
        @JvmField
        var isFacebook: Boolean = false
        @JvmField
        var isInstagram: Boolean = false
        @JvmField
        var useScript: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(webLoadTask)
            this.taskRef = weakReference
            if ((weakReference.get() as WebLoadTask?) == null) {
                return@init
            }
            this.baseUrl = baseUrl
            this.html = html
            this.urls = ArrayList()
            this.keys = ArrayList()
            this.imgCount = ImgCntItem()

        }
    }

        public override fun doInBackground() {
            val webLoadTask = this.taskRef!!.get() as WebLoadTask?
            if (webLoadTask == null || !webLoadTask.started) {
                return
            }
            var index = 0
            for (index8 in 0..1) {
                if (!webLoadTask.started) {
                    return
                }
                try {
                    val document: Document
                    if (index8 == 0) {
                        document = Jsoup.parse(this.html!!, this.baseUrl!!)
                    } else {
                        val arrayList = this.urls
                        if (arrayList != null && !arrayList.isEmpty()) {
                            break
                        }
                        val context = this.taskContext
                        val baseUrl = this.baseUrl
                        document =
                            Jsoup.connect(baseUrl!!).headers(MainUtil.requestHeadersFor2(context, baseUrl, baseUrl)).get()
                    }
                    if (TextUtils.isEmpty(this.title)) {
                        val elementsSelect = document.select("meta[property='og:title']")
                        if (elementsSelect.size != 0) {
                            for (index9 in elementsSelect.indices) {
                                val element = elementsSelect.get(index9)
                                if (!webLoadTask.started) {
                                    return
                                }
                                var metaContent = element.attr("content")
                                if (!TextUtils.isEmpty(metaContent)) {
                                    metaContent = metaContent.trim { it <= ' ' }
                                }
                                if (!TextUtils.isEmpty(metaContent)) {
                                    this.title = metaContent
                                }
                                break
                            }
                        }
                        if (TextUtils.isEmpty(this.title)) {
                            val titles = document.select("title")
                            if (titles.size != 0) {
                                for (index10 in titles.indices) {
                                    val element = titles.get(index10)
                                    if (!webLoadTask.started) {
                                        return
                                    }
                                    val elementText = element.text()
                                    if (TextUtils.isEmpty(elementText)) {
                                        continue
                                    }
                                    val spannedFromHtml = Html.fromHtml(elementText, 0)
                                    if (spannedFromHtml == null) {
                                        continue
                                    }
                                    var title = spannedFromHtml.toString()
                                    if (!TextUtils.isEmpty(title)) {
                                        title = title.trim { it <= ' ' }
                                    }
                                    if (!TextUtils.isEmpty(title)) {
                                        this.title = title
                                        break
                                    }
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(this.title)) {
                            this.title = MainUtil.safeFileName(MainUtil.stripHitomiTitle(this.rawTitle, this.title))
                        }
                    }
                    if (!webLoadTask.started) {
                        return
                    }
                    val images = document.select("img")
                    for (index11 in images.indices) {
                        val element = images.get(index11)
                        if (!webLoadTask.started) {
                            return
                        }
                        for (attribute in element.attributes()) {
                            val value = attribute.value
                            val parsedSrc: String? = if (TextUtils.isEmpty(value) || value.startsWith("data:image/")) null else MainUtil.parseFontList(
                                    value
                                )
                            val fixedUrl: String? = if (TextUtils.isEmpty(value) || value.startsWith("data:image/") || parsedSrc == null) null else Companion.fixImageUrl(
                                    this.mode,
                                    this.sourceKind.toInt(),
                                    parsedSrc
                                )
                            if (TextUtils.isEmpty(value) || value.startsWith("data:image/") || parsedSrc == null || fixedUrl == null) {
                                continue
                            }
                            val imageUrlValue = if (URLUtil.isNetworkUrl(fixedUrl)) fixedUrl else MainUtil.imageUrlFallback(
                                fixedUrl,
                                this.host
                            )
                            val urlKey = if (URLUtil.isNetworkUrl(fixedUrl)) MainUtil.extractUrl(
                                MainUtil.imageUrlFor(
                                    fixedUrl,
                                    this.found
                                )
                            ) else MainUtil.extractUrl(imageUrlValue)
                            if (TextUtils.isEmpty(urlKey)) {
                                continue
                            }
                            val keyIndex = this.keys!!.indexOf(urlKey)
                            if (keyIndex == -1) {
                                this.urls!!.add(imageUrlValue)
                                this.keys!!.add(urlKey)
                                countImage(urlKey, this.imgCount)
                            } else if (this.useScript) {
                                this.urls!!.set(keyIndex, imageUrlValue)
                            }
                        }
                    }
                    if (this.isFacebook) {
                        val elementsSelect = document.select("i")
                        for (index12 in elementsSelect.indices) {
                            val element = elementsSelect.get(index12)
                            if (!webLoadTask.started) {
                                return
                            }
                            for (attribute in element.attributes()) {
                                val parenUrl = urlInParens(attribute.value)
                                if (!TextUtils.isEmpty(parenUrl) && !this.urls!!.contains(parenUrl)) {
                                    this.urls!!.add(parenUrl)
                                    countImage(parenUrl, this.imgCount)
                                }
                            }
                        }
                    }
                } catch (unused: Exception) {
                } catch (unused: LinkageError) {
                } catch (unused: OutOfMemoryError) {
                }
            }
            if (this.mode != 2) {
                if (this.isPinterest) {
                    var urls = this.urls
                    if (urls != null && !urls.isEmpty()) {
                        val singleImage: ArrayList<String?> = ArrayList()
                        val size = urls.size
                        while (index < size) {
                            val rawImageUrl: Any? = urls.get(index)
                            index++
                            var imageUrl = rawImageUrl as String?
                            if (!TextUtils.isEmpty(imageUrl)) {
                                val originalUrl = MainUtil.pinterestOriginal(imageUrl)
                                if (!TextUtils.isEmpty(originalUrl)) {
                                    imageUrl = originalUrl
                                }
                                singleImage.add(imageUrl)
                            }
                        }
                        if (!singleImage.isEmpty()) {
                            urls = singleImage
                        }
                    }
                    this.urls = urls
                    return
                }
                return
            }
            var urls = this.urls
            if (urls != null && !urls.isEmpty()) {
                val dirs: ArrayList<MimagDir?> = ArrayList()
                val urlsSize = urls.size
                var urlIndex = 0
                while (urlIndex < urlsSize) {
                    val rawUrl: Any? = urls.get(urlIndex)
                    urlIndex++
                    val parentDir: String? = parentDirName(rawUrl as String?)
                    if (!TextUtils.isEmpty(parentDir)) {
                        val size = dirs.size
                        var dirIndex = 0
                        while (true) {
                            if (dirIndex >= size) {
                                val mimagDir = MimagDir()
                                mimagDir.name = parentDir
                                mimagDir.count = 1
                                dirs.add(mimagDir)
                                break
                            }
                            val rawDir: Any? = dirs.get(dirIndex)
                            dirIndex++
                            val mimagDir: MimagDir? = rawDir as MimagDir?
                            if (mimagDir != null && parentDir == mimagDir.name) {
                                mimagDir.count++
                                break
                            }
                        }
                    }
                }
                if (!dirs.isEmpty()) {
                    val dirsSize = dirs.size
                    var bestCount = 0
                    var dirPos = 0
                    var dirName: String? = null
                    while (dirPos < dirsSize) {
                        val rawDirItem: Any? = dirs.get(dirPos)
                        dirPos++
                        val mimagDir: MimagDir? = rawDirItem as MimagDir?
                        val dirCount = if (mimagDir != null) mimagDir.count else 0
                        if (mimagDir != null && dirCount >= bestCount) {
                            dirName = mimagDir.name
                            bestCount = dirCount
                        }
                    }
                    if (!TextUtils.isEmpty(dirName)) {
                        val imagePaths: ArrayList<String?> = ArrayList()
                        val urlsCount = urls.size
                        var pathIndex = 0
                        while (pathIndex < urlsCount) {
                            val rawImagePath: Any? = urls.get(pathIndex)
                            pathIndex++
                            val imagePath = rawImagePath as String?
                            val imageDirName: String? = parentDirName(imagePath)
                            if (!TextUtils.isEmpty(imageDirName) && dirName == imageDirName) {
                                imagePaths.add(imagePath)
                            }
                        }
                        if (!imagePaths.isEmpty()) {
                            if (imagePaths.isEmpty()) {
                                urls = imagePaths
                            } else {
                                val seenNames: ArrayList<String?> = ArrayList()
                                val pathsCount = imagePaths.size
                                var nameIndex = 0
                                var dupNames: ArrayList<String?>? = null
                                while (nameIndex < pathsCount) {
                                    val rawPath: Any? = imagePaths.get(nameIndex)
                                    nameIndex++
                                    val fileName = URLUtilCompat.guessFileName((rawPath as String?)!!, null, "image/*")
                                    if (!TextUtils.isEmpty(fileName)) {
                                        if (seenNames.contains(fileName)) {
                                            if (dupNames == null) {
                                                dupNames = ArrayList()
                                            }
                                            dupNames.add(fileName)
                                        } else {
                                            seenNames.add(fileName)
                                        }
                                    }
                                }
                                if (dupNames == null || dupNames.isEmpty()) {
                                    urls = imagePaths
                                } else {
                                    val hostDirs: ArrayList<MimagDir?> = ArrayList()
                                    val hostPathCount = imagePaths.size
                                    var hostIndex = 0
                                    while (hostIndex < hostPathCount) {
                                        val rawHostPath: Any? = imagePaths.get(hostIndex)
                                        hostIndex++
                                        val entryHost = MainUtil.hostOf((rawHostPath as kotlin.String?)!!, true)
                                        if (!TextUtils.isEmpty(entryHost)) {
                                            val size = hostDirs.size
                                            var hostDirIndex = 0
                                            while (true) {
                                                if (hostDirIndex >= size) {
                                                    val mimagDir = MimagDir()
                                                    mimagDir.name = entryHost
                                                    mimagDir.count = 1
                                                    hostDirs.add(mimagDir)
                                                    break
                                                }
                                                val rawHostDir: Any? = hostDirs.get(hostDirIndex)
                                                hostDirIndex++
                                                val mimagDir: MimagDir? = rawHostDir as MimagDir?
                                                if (mimagDir != null && entryHost == mimagDir.name) {
                                                    mimagDir.count++
                                                    break
                                                }
                                            }
                                        }
                                    }
                                    if (hostDirs.isEmpty()) {
                                        urls = imagePaths
                                    } else {
                                        val hostDirsSize = hostDirs.size
                                        var maxDirCount = 0
                                        var hostDirPos = 0
                                        var bestDirName: String? = null
                                        while (hostDirPos < hostDirsSize) {
                                            val rawBestDir: Any? = hostDirs.get(hostDirPos)
                                            hostDirPos++
                                            val mimagDir: MimagDir? = rawBestDir as MimagDir?
                                            val dirCount = if (mimagDir != null) mimagDir.count else 0
                                            if (mimagDir != null && dirCount >= maxDirCount) {
                                                bestDirName = mimagDir.name
                                                maxDirCount = dirCount
                                            }
                                        }
                                        if (TextUtils.isEmpty(bestDirName)) {
                                            urls = imagePaths
                                        } else {
                                            urls = ArrayList()
                                            val size = imagePaths.size
                                            while (index < size) {
                                                val rawImageUrl: Any? = imagePaths.get(index)
                                                index++
                                                val imageUrl = rawImageUrl as String
                                                val imageFileName = URLUtilCompat.guessFileName(imageUrl, null, "image/*")
                                                if (!TextUtils.isEmpty(imageFileName) && dupNames.contains(
                                                        imageFileName
                                                    )
                                                ) {
                                                    val imageHost = hostOf(imageUrl, true)
                                                    if (TextUtils.isEmpty(imageHost) || bestDirName != imageHost) {
                                                        continue
                                                    }
                                                }
                                                urls.add(imageUrl)
                                            }
                                            if (urls.isEmpty()) {
                                                urls = imagePaths
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.urls = urls
        }

        public override fun onCancelled() {
            val webLoadTask = this.taskRef!!.get() as WebLoadTask?
            if (webLoadTask == null) {
                return
            }
            webLoadTask.imageTask = null
            this.baseUrl = null
            this.html = null
            this.host = null
            this.rawTitle = null
            this.title = null
            this.urls = null
            this.keys = null
            this.imgCount = null
        }

        public override fun onPostExecute() {
            var siteName: String?
            val webLoadTask = this.taskRef!!.get() as WebLoadTask?
            if (webLoadTask == null) {
                return
            }
            webLoadTask.imageTask = null
            val arrayList = this.urls
            if (arrayList == null || arrayList.isEmpty() || !webLoadTask.started) {
                val webLoadTaskListener = webLoadTask.taskListener
                if (webLoadTaskListener != null) {
                    webLoadTaskListener.onResult(null, null, null)
                }
                this.baseUrl = null
                this.html = null
                this.host = null
                this.rawTitle = null
                this.title = null
                this.urls = null
                this.keys = null
                this.imgCount = null
                return
            }
            if (TextUtils.isEmpty(this.title)) {
                val rawTitle = this.rawTitle
                val baseUrl = this.baseUrl
                val webView = webLoadTask.loadWeb
                if (webView != null) {
                    siteName = MainUtil.siteNameOf(webView.getTitle(), true)
                    if (TextUtils.isEmpty(siteName)) {
                        siteName = MainUtil.hostLabelUpper(MainUtil.hostNameOf(baseUrl!!, true))
                    }
                } else {
                    siteName = MainUtil.hostLabelUpper(MainUtil.hostNameOf(baseUrl!!, true))
                }
                this.title = MainUtil.safeFileName(MainUtil.stripHitomiTitle(rawTitle, siteName))
            }
            val webLoadTaskListener = webLoadTask.taskListener
            if (webLoadTaskListener != null) {
                webLoadTaskListener.onResult(this.title, this.urls, this.imgCount)
            }
            this.baseUrl = null
            this.html = null
            this.host = null
            this.rawTitle = null
            this.title = null
            this.urls = null
            this.keys = null
            this.imgCount = null
        }

        public override fun onPreExecute() {
            val sourceKind: Byte
            val webLoadTask = this.taskRef!!.get() as WebLoadTask?
            if (webLoadTask == null || !webLoadTask.started) {
                return
            }
            val webLoadTaskListener = webLoadTask.taskListener
            if (webLoadTaskListener != null) {
                webLoadTaskListener.onLoading()
            }
            var useScript = true
            var baseHost = MainUtil.hostOf(this.baseUrl!!, true)
            this.host = baseHost
            if (baseHost == null) {
                baseHost = ""
                this.host = ""
            }
            val hostSuffix = fileNumberSuffix(baseHost)
            this.rawTitle = hostSuffix
            this.found = true
            val comicKind = MainUtil.comicSiteKind(hostSuffix)
            this.mode = comicKind
            if (comicKind == 0) {
                val baseUrl = this.baseUrl
                if (TextUtils.isEmpty(baseUrl)) {
                    sourceKind = 0
                } else if (baseUrl!!.contains("m.comic.naver.com/webtoon/detail")) {
                    sourceKind = 1
                } else if (baseUrl.contains("comic.naver.com/webtoon/detail")) {
                    sourceKind = 2
                } else if (baseUrl.contains("m.webtoon.daum.net/m/webtoon/viewer/")) {
                    sourceKind = 3
                } else if (baseUrl.contains("webtoon.daum.net/webtoon/viewer/")) {
                    sourceKind = 4
                } else {
                    sourceKind = 0
                }
                this.sourceKind = sourceKind
                if (sourceKind.toInt() != 0) {
                    this.found = false
                } else if (this.host!!.endsWith("pinterest.com")) {
                    this.isPinterest = true
                } else if (this.host!!.endsWith("facebook.com")) {
                    this.isFacebook = true
                } else if (this.host!!.endsWith("instagram.com")) {
                    this.isInstagram = true
                }
            }
            if (!this.isInstagram && this.mode != 2) {
                useScript = false
            }
            this.useScript = useScript
        }
    }

    private class MimagDir {
        var name: String? = null
        var count: Int = 0
    }

    interface WebLoadTaskListener {
        fun onFailed()

        fun onLoading()

        fun onResult(title: String?, arrayList: ArrayList<String?>?, imgCntItem: ImgCntItem?)

        fun onProgress(progress: Int)

        fun onStopped()
    }

    fun onPageDone(url: String?) {
        this.netError = false
        if (this.loadWeb == null) {
            val webLoadTaskListener = this.taskListener
            if (webLoadTaskListener != null) {
                webLoadTaskListener.onFailed()
                return
            }
            return
        }
        this.netError = false
        val loadTask = this.imageTask
        if (loadTask != null) {
            loadTask.cancelled = true
        }
        this.imageTask = null
        val isHttp = isHttpUrl(url)
        val webView = this.loadWeb
        if (!isHttp) {
            evalJs(
                webView,
                "(function(){android.onLoadHtml(document.documentElement.innerHTML);})();",
                false
            )
        } else {
            if (webView == null) {
                return
            }
            webView.evaluateJavascript(
                "document.documentElement.innerHTML",
                PageHtmlCallback()
            )
        }
    }

    fun checkNetError(code: Int) {
        var netError = true
        when (code) {
            NetError.ERR_SOCKET_NOT_CONNECTED, NetError.ERR_UPLOAD_FILE_CHANGED, NetError.ERR_OUT_OF_MEMORY, NetError.ERR_INSUFFICIENT_RESOURCES, NetError.ERR_NOT_IMPLEMENTED, NetError.ERR_ACCESS_DENIED, NetError.ERR_UNEXPECTED, NetError.ERR_FILE_TOO_BIG, NetError.ERR_TIMED_OUT, -6, -5, -4, -3, -2, -1 -> {}
            else -> netError = false
        }
        this.netError = netError
    }

    fun extractImages(html: String?) {
        val webView = this.loadWeb
        if (webView == null) {
            val webLoadTaskListener = this.taskListener
            if (webLoadTaskListener != null) {
                webLoadTaskListener.onFailed()
                return
            }
            return
        }
        val context = webView.getContext()
        val url = this.loadWeb!!.getUrl()
        if (this.loadWeb == null) {
            val webLoadTaskListener = this.taskListener
            if (webLoadTaskListener != null) {
                webLoadTaskListener.onFailed()
                return
            }
            return
        }
        this.netError = false
        val loadTask = this.imageTask
        if (loadTask != null) {
            loadTask.cancelled = true
        }
        this.imageTask = null
        if (URLUtil.isNetworkUrl(url)) {
            val imageTask = LoadTask(this, url, html)
            this.imageTask = imageTask
            imageTask.start(context)
        } else {
            val webLoadTaskListener = this.taskListener
            if (webLoadTaskListener != null) {
                webLoadTaskListener.onFailed()
            }
        }
    }

    fun onProgress(done: Int) {
        if (this.loadWeb != null) {
            this.loadState = 1.toByte()
            val webLoadTaskListener = this.taskListener
            if (webLoadTaskListener != null) {
                webLoadTaskListener.onProgress(done)
            }
        }
    }

    companion object {
        /**
         * The one task of the picture loading screens. Its [loadWeb] is the current page only while one is open:
         * `WebGridDialog.closeChildren` (also run when the activity finishes) and `DialogLoadImg.dismiss` clear it.
         */
        @android.annotation.SuppressLint("StaticFieldLeak")
        @JvmField
        var cached: WebLoadTask? = null
        @JvmStatic
        fun countImage(path: String?, imgCntItem: ImgCntItem?) {
            if (imgCntItem == null) {
                return
            }
            val ext = fileExtFromName(path, true)
            if (TextUtils.isEmpty(ext)) {
                imgCntItem.noExtCount++
                return
            }
            if (ext == "jpg") {
                imgCntItem.jpgCount++
                return
            }
            if (ext == "png") {
                imgCntItem.pngCount++
                return
            }
            if (ext == "gif") {
                imgCntItem.gifCount++
            } else if (ext == "webp") {
                imgCntItem.webpCount++
            } else {
                imgCntItem.otherCount++
            }
        }

        @JvmStatic
        fun instance(): WebLoadTask {
            var webLoadTask: WebLoadTask?
            val existing: WebLoadTask? = cached
            if (existing != null) {
                return existing
            }
            synchronized(WebLoadTask::class.java) {
                webLoadTask = cached
                if (webLoadTask == null) {
                    webLoadTask = WebLoadTask()
                    cached = webLoadTask
                }
            }
            return webLoadTask!!
        }

        @JvmStatic
        fun parentDirName(path: String?): String? {
            val lastSlash = if (TextUtils.isEmpty(path)) 0 else path!!.lastIndexOf(47.toChar())
            val prevSlash = if (TextUtils.isEmpty(path) || lastSlash <= 0 || lastSlash + 1 >= path!!.length) 0 else path.lastIndexOf(
                    47.toChar(),
                    lastSlash - 1
                )
            val nameStart = if (TextUtils.isEmpty(path) || lastSlash <= 0 || lastSlash + 1 >= path!!.length || prevSlash == -1) 0 else (prevSlash + 1)
            if (TextUtils.isEmpty(path) || lastSlash <= 0 || lastSlash + 1 >= path!!.length || prevSlash == -1 || nameStart >= lastSlash) {
                return null
            }
            return path.substring(nameStart, lastSlash)
        }

        @JvmStatic
        fun fixImageUrl(mode: Int, site: Int, url: String?): String? {
            if (url != "https://www.gstatic.com/images/branding/product/1x/translate_24dp.png") {
                var isComic = false
                if (site == 0) {
                    if (mode != 2) {
                        return url
                    }
                    if (TextUtils.isEmpty(url)) {
                        return null
                    }
                    if (url!!.contains("viewimage.php") || url.contains(".blogspot.com/")) {
                        return url
                    }
                    if (!url.contains("/data/") || url.contains("/data/member/") || url.contains("/data/member_image/") || url.contains(
                            "/thumb_"
                        )
                    ) {
                        return null
                    }
                    if (url.contains("/thumb-") && !url.contains("/data/file/origin/")) {
                        return null
                    }
                    var ext = extOf(url)
                    if (TextUtils.isEmpty(ext)) {
                        return url
                    }
                    val queryIndex = ext!!.indexOf(63.toChar())
                    if (queryIndex != -1) {
                        ext = ext.substring(0, queryIndex)
                        if (TextUtils.isEmpty(ext)) {
                            return url
                        }
                    }
                    val lowerCase = ext.lowercase()
                    if (TextUtils.isEmpty(lowerCase) || lowerCase != "png") {
                        return url
                    }
                    val guessedName = URLUtilCompat.guessFileName(url, null, "image/*")
                    if (TextUtils.isEmpty(guessedName)) {
                        return url
                    }
                    if (guessedName.contains("logo_") || guessedName.contains("logo-") || guessedName.contains("sns_") || guessedName.contains(
                            "sns-"
                        )
                    ) {
                        return null
                    }
                    return url
                }
                if (!TextUtils.isEmpty(url)) {
                    isComic = true
                    if (site == 1) {
                        isComic = url!!.contains("image-comic.pstatic.net/mobilewebimg/")
                    } else if (site == 2) {
                        isComic = url!!.contains("image-comic.pstatic.net/webtoon/")
                    } else if (site == 3 || site == 4) {
                        isComic = url!!.contains("t1.daumcdn.net/webtoon_episode/")
                    }
                }
                if (isComic) {
                    return url
                }
            }
            return null
        }
    }
}
