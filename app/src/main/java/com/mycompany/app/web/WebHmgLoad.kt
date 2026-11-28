package com.mycompany.app.web

import android.view.View

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
import com.mycompany.app.web.WebLoadWrap.EmgLoadListener
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
import com.mycompany.app.web.WebHmgTask.HmgTaskListener
import com.mycompany.app.web.WebSnsTask
import com.mycompany.app.web.WebSnsTwit
import com.mycompany.app.web.WebSnsInsta
import com.mycompany.app.web.WebSnsLoad
import org.json.JSONObject
import org.json.JSONArray
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
import java.util.ArrayList

@Suppress("deprecation")
class WebHmgLoad : WebLoadWrap {
    constructor() : super()

    @JvmField
    var appContext: Context? = null
    @JvmField
    var loadListener: EmgLoadListener? = null
    @JvmField
    var parentView: ViewGroup? = null
    @JvmField
    var webNest: WebNestView? = null
    @JvmField
    var pageUrl: String? = null
    @JvmField
    var imagePos: Int = 0
    @JvmField
    var forward: Boolean = false
    @JvmField
    var pendingUrl: String? = null
    @JvmField
    var loadCount: Int = 0
    @JvmField
    var loading: Boolean = false
    @JvmField
    var hmgTask: WebHmgTask? = null
    @JvmField
    var urlLoading: Boolean = false
    @JvmField
    var triedPositions: SparseIntArray? = null
    @JvmField
    var stopped: Boolean = false
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var lastFinishedUrl: String? = null

    internal inner class CreateWebTask : Runnable {

        internal inner class SetClientStep : Runnable {

            internal inner class ApplySettingsStep : Runnable {

                internal inner class StartLoadStep : Runnable {
                    override fun run() {
                        val webHmgLoad = this@WebHmgLoad
                        webHmgLoad.startLoad(webHmgLoad.pageUrl)
                    }
                }

                override fun run() {
                    val createWebTask = this@CreateWebTask
                    val webHmgLoad = this@WebHmgLoad
                    val webNestView = webHmgLoad.webNest
                    if (webNestView != null) {
                        webHmgLoad.urlLoading = true
                        webNestView.addJavascriptInterface(webHmgLoad.WebAppInterface(), "android")
                        val viewGroup = this@WebHmgLoad.parentView
                        if (viewGroup == null) {
                            return
                        }
                        viewGroup.post(StartLoadStep())
                    }
                }
            }

            override fun run() {
                val webHmgLoad = this@WebHmgLoad
                val webNestView = webHmgLoad.webNest
                if (webNestView != null) {
                    webNestView.setWebViewClient(webHmgLoad.LocalWebViewClient())
                    val viewGroup = webHmgLoad.parentView
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(ApplySettingsStep())
                }
            }
        }

        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            val webNestView = webHmgLoad.webNest
            if (webNestView != null) {
                applyZoomSettings(webNestView, false)
                val viewGroup = webHmgLoad.parentView
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(SetClientStep())
            }
        }
    }

    internal inner class StartLoadTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            if (webHmgLoad.stopped) {
                webHmgLoad.stopped = false
                webHmgLoad.startLoad(webHmgLoad.pageUrl)
            }
        }
    }

    internal inner class LoadImageStartTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            webHmgLoad.startLoad(webHmgLoad.pageUrl)
        }
    }

    internal inner class LoadPageTask : Runnable {

        internal inner class LoadPageStep : Runnable {

            internal inner class PreparePageStep : Runnable {

                internal inner class StartPageLoadStep : Runnable {
                    override fun run() {
                        val webHmgLoad = this@WebHmgLoad
                        webHmgLoad.startLoad(webHmgLoad.pageUrl)
                    }
                }

                override fun run() {
                    val loadPageTask = this@LoadPageTask
                    val webHmgLoad = this@WebHmgLoad
                    val webNestView = webHmgLoad.webNest
                    if (webNestView == null) {
                        webHmgLoad.loading = false
                        return
                    }
                    webHmgLoad.urlLoading = true
                    webNestView.addJavascriptInterface(webHmgLoad.WebAppInterface(), "android")
                    val viewGroup = this@WebHmgLoad.parentView
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(StartPageLoadStep())
                }
            }

            override fun run() {
                val webHmgLoad = this@WebHmgLoad
                val webNestView = webHmgLoad.webNest
                if (webNestView == null) {
                    webHmgLoad.loading = false
                    return
                }
                webNestView.setWebViewClient(webHmgLoad.LocalWebViewClient())
                val viewGroup = webHmgLoad.parentView
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(PreparePageStep())
            }
        }

        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            val webNestView = webHmgLoad.webNest
            if (webNestView == null) {
                webHmgLoad.loading = false
                return
            }
            applyZoomSettings(webNestView, false)
            val viewGroup = webHmgLoad.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.post(LoadPageStep())
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            val webNestView = webHmgLoad.webNest
            if (webNestView == null) {
                return
            }
            webHmgLoad.urlLoading = false
            webNestView.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            val webNestView = webHmgLoad.webNest
            if (webNestView == null) {
                return
            }
            webHmgLoad.urlLoading = true
            webNestView.addJavascriptInterface(webHmgLoad.WebAppInterface(), "android")
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            MainUtil.clearWebCache(webHmgLoad.webNest)
            webHmgLoad.finishHandled = false
        }
    }

    internal inner class ReportDoneTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            webHmgLoad.loading = false
            val emgLoadListener = webHmgLoad.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
            }
        }
    }

    internal inner class ReadPageTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            val webHmgTask: WebHmgTask? = if (webHmgLoad.stopped) null else webHmgLoad.hmgTask
            if (webHmgLoad.stopped || webHmgTask == null) {
                return
            }
            webHmgTask.readPage()
        }
    }

    internal inner class ShowImagesStartTask : Runnable {
        override fun run() {
            val webHmgLoad = this@WebHmgLoad
            webHmgLoad.startLoad(webHmgLoad.pageUrl)
        }
    }

    private inner class WebAppInterface {
        @JavascriptInterface
        fun onViewHtml(href: String?, innerHTML: String?) {
            val webNestView = this@WebHmgLoad.webNest
            if (webNestView != null) {
                webNestView.notifyHtml(href, innerHTML)
            }
        }
    }

    constructor(
        mainActivity: MainActivity?,
        viewGroup: ViewGroup?,
        str: String?,
        i: Int,
        z: Boolean,
        emgLoadListener: EmgLoadListener?
    ) {
        if (mainActivity == null || viewGroup == null) {
            return
        }
        this.appContext = mainActivity.getApplicationContext()
        this.loadListener = emgLoadListener
        this.parentView = viewGroup
        this.pageUrl = str
        this.imagePos = i
        this.forward = z
        this.loading = true
        val webNestView = WebNestView(mainActivity)
        this.webNest = webNestView
        webNestView.setNoAutofill(true)
        this.webNest!!.setVisibility(View.INVISIBLE)
        this.parentView!!.addView(this.webNest, 0, ViewGroup.LayoutParams(-1, -1))
        this.hmgTask = WebHmgTask(this.appContext, this.webNest, HmgTaskResult())
        this.parentView!!.post(LoadPageTask())
    }

    override fun release() {
        val webHmgTask = this.hmgTask
        if (webHmgTask != null) {
            webHmgTask.release()
            this.hmgTask = null
        }
        this.appContext = null
        this.loadListener = null
        this.pageUrl = null
        this.pendingUrl = null
        val webNestView = this.webNest
        if (webNestView != null) {
            destroyWebViewNow(webNestView)
            this.webNest = null
        }
        this.parentView = null
    }

    override fun loadImage(index: Int) {
        if (this.stopped || this.loading || this.hmgTask == null || this.webNest == null) {
            return
        }
        val arrayList: ArrayList<String?>? = DataUrl.instance(this.appContext).urls
        if (arrayList == null || arrayList.isEmpty()) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        val thumbs: ArrayList<String?>? = DataUrl.instance(this.appContext).thumbUrls
        if (thumbs == null || thumbs.isEmpty()) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        val size = thumbs.size
        if (size != arrayList.size) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        if (index < 0 || index >= size) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        if (triedBefore(index)) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        arrayList.set(index, index.toString() + ".webp")
        val pageUrl = thumbs.get(index)
        if (!URLUtil.isNetworkUrl(pageUrl)) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        this.pageUrl = pageUrl
        this.imagePos = index
        val webNestView = this.webNest
        if (webNestView != null) {
            webNestView.postDelayed(LoadImageStartTask(), 200L)
        }
    }

    override fun onViewHtml(position: Int, url: String?) {
        if (!this.loading || this.hmgTask == null || this.webNest == null || this.imagePos == position) {
            return
        }
        if (!URLUtil.isNetworkUrl(url)) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        val arrayList: ArrayList<String?>? = DataUrl.instance(this.appContext).urls
        if (arrayList == null || arrayList.isEmpty()) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        val thumbs: ArrayList<String?>? = DataUrl.instance(this.appContext).thumbUrls
        if (thumbs == null || thumbs.isEmpty()) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        val size = thumbs.size
        if (size != arrayList.size) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        if (position < 0 || position >= size) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        val pageUrl = arrayList.get(position)
        if (URLUtil.isNetworkUrl(pageUrl)) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onImage(position, pageUrl)
                return
            }
            return
        }
        if (triedBefore(position)) {
            val emgLoadListener = this.loadListener
            if (emgLoadListener != null) {
                emgLoadListener.onDone()
                return
            }
            return
        }
        if (this.imagePos != position) {
            this.stopped = true
            this.pageUrl = url
            this.imagePos = position
            val webNestView = this.webNest
            val loading = webNestView!!.loading
            if (loading) {
                webNestView.stopLoading()
            }
            this.hmgTask!!.cancel()
            val webNest = this.webNest
            if (webNest != null) {
                webNest.postDelayed(StartLoadTask(), if (loading) 800L else 200L)
            }
        }
    }

    fun loadUrl(pageUrl: String?) {
        if (this.webNest != null) {
            val isHttp = isHttpUrl(pageUrl)
            val urlLoading = this.urlLoading
            if (isHttp) {
                if (urlLoading) {
                    this.urlLoading = false
                    val webNestView = this.webNest
                    if (webNestView != null) {
                        webNestView.post(RemoveJsBridgeTask())
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
            val webNestView = this.webNest
            if (webNestView == null) {
                return
            }
            webNestView.post(AddJsBridgeTask())
        }
    }

    fun triedBefore(current: Int): Boolean {
        val sparseIntArray = this.triedPositions
        if (sparseIntArray == null) {
            val triedPositions = SparseIntArray()
            this.triedPositions = triedPositions
            triedPositions.put(current, 1)
            return false
        }
        val tries = sparseIntArray.get(current, -1)
        if (tries == -1) {
            this.triedPositions!!.put(current, 1)
            return false
        }
        if (tries >= 10) {
            return true
        }
        this.triedPositions!!.put(current, tries + 1)
        return false
    }

    /** arrayList: image url per page (network url once known), arrayList2: page url per index; the index of the page just read.  */
    fun showImages(urls: ArrayList<String?>?, thumbs: ArrayList<String?>?, imagePos: Int) {
        if (this.stopped) {
            return
        }
        if (this.webNest == null) {
            this.loading = false
            return
        }
        if (urls == null || urls.isEmpty()) {
            if (!this.stopped && this.webNest != null) {
                this.webNest!!.postDelayed(ReadPageTask(), 200L)
            }
            return
        }
        var current = this.imagePos
        if (current < 0) {
            this.imagePos = imagePos
            current = imagePos
        }
        if (thumbs == null || thumbs.isEmpty() || thumbs.size != urls.size) {
            stop()
            return
        }
        val size = thumbs.size
        if (imagePos >= 0 && imagePos < size) {
            this.imagePos = imagePos
            val url = urls.get(imagePos)
            if (URLUtil.isNetworkUrl(url) && this.loadListener != null) {
                this.loadListener!!.onImage(imagePos, url)
            }
            current = imagePos
        }
        val following = imagePos + 1
        if (following >= 0 && following < size) {
            val url = urls.get(following)
            if (URLUtil.isNetworkUrl(url) && this.loadListener != null) {
                this.loadListener!!.onImage(following, url)
            }
        }
        var done = 0
        for (o in urls) {
            if (URLUtil.isNetworkUrl(o)) {
                done++
            }
        }
        if (done == size) {
            this.loading = false
            if (this.loadListener != null) {
                this.loadListener!!.onFailed()
            }
            return
        }
        // Next page (forward or backward) with a page url and no image yet.
        var next = -1
        val total = thumbs.size
        if (total == urls.size) {
            val start = if (current >= 0 && current < total) current else 0
            var pos = start
            for (step in 0..<total) {
                pos = if (this.forward) (pos + 1) % total else ((pos - 1) + total) % total
                if (pos == start || pos < 0 || pos >= total) {
                    break
                }
                if (URLUtil.isNetworkUrl(thumbs.get(pos)) && !URLUtil.isNetworkUrl(
                        urls.get(pos)
                    )
                ) {
                    next = pos
                    break
                }
            }
        }
        if (next == -1) {
            stop()
            return
        }
        val page = if (next < size) thumbs.get(next) else null
        if (URLUtil.isNetworkUrl(page)) {
            this.pageUrl = page
            this.imagePos = next
            current = next
        }
        if (triedBefore(current)) {
            // Image already cached: mark it and continue without loading the page.
            urls.set(current, "https://" + current + ".webp")
            showImages(urls, thumbs, current)
            return
        }
        if (this.webNest != null) {
            this.webNest!!.postDelayed(ShowImagesStartTask(), 200L)
        }
    }

    private fun stop() {
        this.loading = false
        if (this.loadListener != null) {
            this.loadListener!!.onDone()
        }
    }

    fun startLoad(pageUrl: String?) {
        if (this.stopped) {
            return
        }
        val webNestView = this.webNest
        if (webNestView == null) {
            this.loading = false
        } else {
            this.pendingUrl = null
            webNestView.openUrl(pageUrl, null)
        }
    }

    internal inner class HmgTaskResult : HmgTaskListener {
        override fun onLoading() {
        }

        override fun onResult(urls: ArrayList<String?>?, thumbs: ArrayList<String?>?, position: Int) {
            val webHmgLoad = this@WebHmgLoad
            if (webHmgLoad.hmgTask == null) {
                return
            }
            webHmgLoad.showImages(urls, thumbs, position)
        }

        override fun onFailed() {
        }
    }

    private inner class LocalWebViewClient : WebViewClient() {

        inner class RestartLoadStep : Runnable {
            override fun run() {
                val webHmgLoad = this@WebHmgLoad
                if (webHmgLoad.stopped) {
                    webHmgLoad.stopped = false
                    webHmgLoad.startLoad(webHmgLoad.pageUrl)
                }
            }
        }

        inner class RetryLoadStep : Runnable {
            override fun run() {
                val webHmgLoad = this@WebHmgLoad
                webHmgLoad.startLoad(webHmgLoad.pageUrl)
            }
        }

        override fun onPageFinished(webView: WebView?, url: String?) {
            val webHmgLoad = this@WebHmgLoad
            val webNest = webHmgLoad.webNest
            if (webNest == null) {
                webHmgLoad.loading = false
                return
            }
            webNest.setWebLoading(false)
            webHmgLoad.loadUrl(url)
            if (!webHmgLoad.finishHandled && !TextUtils.isEmpty(url) && (url != webHmgLoad.lastFinishedUrl)) {
                webHmgLoad.finishHandled = true
                webHmgLoad.lastFinishedUrl = url
                val viewGroup = webHmgLoad.parentView
                if (viewGroup != null) {
                    viewGroup.post(webHmgLoad.ClearCacheTask())
                }
            }
            var page: String? = null
            if (webHmgLoad.stopped) {
                webHmgLoad.pendingUrl = null
                webHmgLoad.webNest!!.postDelayed(RestartLoadStep(), 200L)
                return
            }
            if (!MainUtil.isHmgUrl(url)) {
                webHmgLoad.pendingUrl = null
                val loadCount = webHmgLoad.loadCount
                if (loadCount < 3) {
                    webHmgLoad.loadCount = loadCount + 1
                    webHmgLoad.webNest!!.postDelayed(RetryLoadStep(), 400L)
                    return
                }
                var imagePos = webHmgLoad.imagePos
                val context = webHmgLoad.appContext
                val arrayList: ArrayList<String?>? = if (context != null) DataUrl.instance(context).thumbUrls else null
                if (context != null && arrayList != null && !arrayList.isEmpty()) {
                    val size = arrayList.size
                    if (imagePos < 0 || imagePos >= size) {
                        imagePos = 0
                    }
                    var index = imagePos
                    var itemIndex = 0
                    while (itemIndex < size) {
                        itemIndex++
                        index = if (webHmgLoad.forward) (index + 1) % size else ((index - 1) + size) % size
                        if (index == imagePos || index < 0 || index >= size) {
                            break
                        }
                        val entry = arrayList.get(index)
                        if (URLUtil.isNetworkUrl(entry)) {
                            page = entry
                            break
                        }
                    }
                }
                if (page == null) {
                    webHmgLoad.loading = false
                    val emgLoadListener = webHmgLoad.loadListener
                    if (emgLoadListener != null) {
                        emgLoadListener.onDone()
                        return
                    }
                    return
                }
            }
            webHmgLoad.loadCount = 0
            if (MainUtil.sameObject(webHmgLoad.pendingUrl, url)) {
                return
            }
            webHmgLoad.pendingUrl = url
            val webNestView: WebNestView? = if (webHmgLoad.stopped) null else webHmgLoad.webNest
            if (webHmgLoad.stopped || webNestView == null) {
                return
            }
            webNestView.postDelayed(webHmgLoad.ReadPageTask(), 200L)
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val webHmgLoad = this@WebHmgLoad
            val webNestView = webHmgLoad.webNest
            if (webNestView == null) {
                return
            }
            webNestView.setWebLoading(true)
            webHmgLoad.loadUrl(url)
        }

        override fun onReceivedError(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?,
            webResourceError: WebResourceError?
        ) {
            super.onReceivedError(webView, webResourceRequest, webResourceError)
            val webHmgTask: WebHmgTask? = if (webResourceError == null) null else this@WebHmgLoad.hmgTask
            if (webResourceError == null || webHmgTask == null) {
                return
            }
            webHmgTask.checkNetError(webResourceError.getErrorCode())
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val webHmgLoad = this@WebHmgLoad
            webHmgLoad.webNest = null
            onRenderGone(webView, renderProcessGoneDetail)
            val viewGroup = webHmgLoad.parentView
            if (viewGroup == null) {
                return true
            }
            viewGroup.post(webHmgLoad.ReportDoneTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val webHmgLoad = this@WebHmgLoad
            if (webHmgLoad.webNest != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                webHmgLoad.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val webHmgLoad = this@WebHmgLoad
            if (webHmgLoad.webNest != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val pageUrl = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(pageUrl)) {
                    webHmgLoad.loadUrl(pageUrl)
                    if (MainUtil.isHmgUrl(pageUrl)) {
                        webHmgLoad.startLoad(pageUrl)
                    }
                }
            }
            return true
        }
    }
}
