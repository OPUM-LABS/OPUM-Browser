package com.mycompany.app.web

import androidx.compose.runtime.getValue
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.SuggestOrder
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.browser.prepareForLegacyRecycler
import androidx.compose.runtime.setValue
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
import com.mycompany.app.db.book.DbBookWeb.Companion.deleteById
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
import com.mycompany.app.db.book.DbBookRecent.Companion.isEnabled
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
import com.mycompany.app.script.CriterionMatcher.matches
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
import com.mycompany.app.db.book.DbBookTab.Companion.deleteById
import com.mycompany.app.main.MainUtil.userFontResponse
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUtil.mimeForPath
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.db.book.DbBookRecent.Companion.deleteById
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
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
import androidx.appcompat.widget.AppCompatTextView
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
import com.mycompany.app.view.MyRecyclerView
import com.mycompany.app.web.WebGridAdapter
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.dialog.DialogImageType
import com.mycompany.app.dialog.DialogDownList
import com.mycompany.app.dialog.DialogDownZip
import com.mycompany.app.dialog.DialogCreateAlbum
import androidx.recyclerview.widget.RecyclerView
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
import android.view.View.OnLongClickListener
import android.graphics.Typeface
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainItem.ChildItem
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
import com.mycompany.app.web.WebSearchAdapter.WebSearchListener
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
import android.graphics.Outline
import android.os.Handler
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.webkit.HttpAuthHandler
import com.mycompany.app.script.CriterionMatcher
import com.mycompany.app.web.WvaQuickSearchListener
import android.widget.Filter.FilterResults
import com.mycompany.app.main.InitialSearch
import android.widget.ArrayAdapter
import android.widget.Filter
import ch.opumlabs.opumbrowser.R
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.regex.Pattern
import kotlin.math.min

@Suppress("deprecation")
class WebSearchAdapter(
    var webViewActivity: WebViewActivity?,
    var historyItems: ArrayList<Any?>?,
    var bookItems: ArrayList<Any?>?,
    var suggestItems: ArrayList<Any?>?,
    var isDark: Boolean,
    var isReversed: Boolean,
    var webSearchListener: WebSearchListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @JvmField
    var recentEnabled: Boolean
    @JvmField
    var query: String? = null
    @JvmField
    var items: ArrayList<Any?>? = null
    @JvmField
    var listLoader: MainListLoader?
    @JvmField
    var connection: HttpURLConnection? = null
    @JvmField
    var filtering: Boolean = false
    @JvmField
    var mainHandler: Handler?
    @JvmField
    var searchExecutor: ExecutorService? = null
    @JvmField
    var listView: MyRecyclerView? = null
    @JvmField
    var lastCount: Int = 0
    @JvmField
    var pendingItems: ArrayList<Any?>? = null
    @JvmField
    var shownSize: Int = 0
    @JvmField
    val searchFilter: Filter = TitleFilter()
    @JvmField
    val hasHeader: Boolean = true
    @JvmField
    val koreanLocale: Boolean = isKoreanLocale()

    internal inner class IconLoadListener : ListLoadListener {
        override fun onFailed(childItem: ChildItem?, view: View?) {
        }

        override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
            var tag: Any? = null
            if (childItem != null) {
                val searchHolder = if (view == null || (view.getTag()
                        .also { tag = it }) == null || (tag !is SearchHolder)
                ) null else tag
                if (searchHolder is ComposeSearchHolder && searchHolder.bindingAdapterPosition == childItem.position) {
                    searchHolder.icon = bitmap
                }
            }
        }
    }

    internal inner class DisconnectTask(val connection: HttpURLConnection) : Runnable {
        override fun run() {
            this.connection.disconnect()
        }
    }

    internal inner class DeleteClick : View.OnClickListener {

        internal inner class DeleteItemTask(val outer: DeleteClick?,
                                             val position: Int
        ) : Runnable {

            internal inner class RefreshListStep : Runnable {
                override fun run() {
                    val deleteClick = this@DeleteItemTask.outer
                    this@WebSearchAdapter.notifyDataSetChanged()
                    this@WebSearchAdapter.filtering = false
                }
            }

            override fun run() {
                val webSearchAdapter = this@WebSearchAdapter
                val searchItemV = webSearchAdapter.itemAt(this.position)
                if (searchItemV == null) {
                    webSearchAdapter.filtering = false
                    return
                }
                val webViewActivity = webSearchAdapter.webViewActivity
                val arrayList = webSearchAdapter.items
                if (arrayList != null && !arrayList.isEmpty()) {
                    try {
                        webSearchAdapter.items!!.remove(searchItemV)
                        val dataBookRecentA = DataBookRecent.instance(webViewActivity)
                        dataBookRecentA.items!!.remove(searchItemV)
                        dataBookRecentA.changed = true
                        DbBookRecent.deleteById(webViewActivity, searchItemV.id)
                        val webSearchListener = webSearchAdapter.webSearchListener
                        if (webSearchListener != null) {
                            (webSearchListener as WvaQuickSearchListener).loadRecent()
                        }
                        val handler = webSearchAdapter.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(RefreshListStep())
                        return
                    } catch (unused: Exception) {
                    }
                }
                webSearchAdapter.filtering = false
            }
        }

        override fun onClick(view: View?) {
            val webSearchAdapter = this@WebSearchAdapter
            if (webSearchAdapter.filtering) {
                return
            }
            webSearchAdapter.filtering = true
            webSearchAdapter.runAsync(DeleteItemTask(this, positionOf(view)))
        }
    }

    internal inner class FillClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val webSearchAdapter = this@WebSearchAdapter
            val searchItemV: SearchItem? = if (webSearchAdapter.webSearchListener == null) null else webSearchAdapter.itemAt(positionOf(view))
            if (webSearchAdapter.webSearchListener == null || searchItemV == null) {
                return
            }
            if (!TextUtils.isEmpty(searchItemV.path)) {
                val webSearchListener = webSearchAdapter.webSearchListener
                val path = searchItemV.path!!
                (webSearchListener as WvaQuickSearchListener).setQuery(path.length, path)
            } else {
                if (TextUtils.isEmpty(searchItemV.title)) {
                    return
                }
                val fillText = (StringBuilder()).append(searchItemV.title).append(" ").toString()
                (webSearchAdapter.webSearchListener as WvaQuickSearchListener).setQuery(fillText.length, fillText)
            }
        }
    }

    internal inner class ItemClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val webSearchAdapter = this@WebSearchAdapter
            val searchItemV: SearchItem? = if (webSearchAdapter.webSearchListener == null) null else webSearchAdapter.itemAt(positionOf(view))
            if (webSearchAdapter.webSearchListener == null || searchItemV == null) {
                return
            }
            (webSearchAdapter.webSearchListener as WvaQuickSearchListener).onItemClick(searchItemV)
        }
    }

    internal inner class ItemLongClick : OnLongClickListener {

        internal inner class CopyItemTask(val outer: ItemLongClick?,
                                             val position: Int
        ) : Runnable {
            override fun run() {
                val itemPath: String?
                val copyLabel: String?
                val webSearchAdapter = this@WebSearchAdapter
                val searchItemV = webSearchAdapter.itemAt(this.position)
                if (searchItemV != null) {
                    if (TextUtils.isEmpty(searchItemV.path)) {
                        itemPath = searchItemV.title
                        copyLabel = "Copied text"
                    } else {
                        itemPath = searchItemV.path
                        copyLabel = "Copied URL"
                    }
                    copyText(webSearchAdapter.webViewActivity, R.string.copied_clipboard, copyLabel, itemPath)
                    val webSearchListener = webSearchAdapter.webSearchListener
                    if (webSearchListener != null) {
                        (webSearchListener as WvaQuickSearchListener).setClipText(itemPath)
                    }
                }
            }
        }

        override fun onLongClick(view: View?): Boolean {
            this@WebSearchAdapter.runAsync(CopyItemTask(this, positionOf(view)))
            return true
        }
    }

    internal inner class ItemOutline(val roundTop: Boolean,
                                         val roundBottom: Boolean
    ) : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (view == null || outline == null) {
                return
            }
            val dp16Px = MainApp.dp16
            val roundTop = this.roundTop
            val roundBottom = this.roundBottom
            if (!roundTop) {
                if (roundBottom) {
                    outline.setRoundRect(0, -dp16Px, view.getWidth(), view.getHeight(), dp16Px.toFloat())
                }
            } else if (roundBottom) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp16Px.toFloat())
            } else {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + dp16Px, dp16Px.toFloat())
            }
        }
    }

    internal inner class TitleFilter : Filter() {
        override fun convertResultToString(resultValue: Any?): CharSequence? {
            if (resultValue != null && (resultValue is SearchItem)) {
                return resultValue.title
            }
            return null
        }

        override fun performFiltering(charSequence: CharSequence?): FilterResults? {
            val webSearchAdapter = this@WebSearchAdapter
            val koreanLocaleNow = webSearchAdapter.koreanLocale
            val isReversedNow = webSearchAdapter.isReversed
            if (webSearchAdapter.webSearchListener != null && !TextUtils.isEmpty(charSequence)) {
                val query = charSequence.toString().trim { it <= ' ' }
                if (!TextUtils.isEmpty(query)) {
                    val lowerCase = query.lowercase()
                    val values: ArrayList<Any?> = ArrayList<Any?>()
                    // each source is collected on its own, then joined in the chosen order (SuggestOrder, nearest the bar first)
                    val groups = HashMap<Int, ArrayList<Any?>>()
                    fun group(source: Int): ArrayList<Any?> = groups.getOrPut(source) { ArrayList() }
                    val recentItems =
                        if (!webSearchAdapter.recentEnabled) null else DataBookRecent.instance(webSearchAdapter.webViewActivity).items
                    if (recentItems != null && !recentItems.isEmpty()) {
                        val size = recentItems.size
                        var recentMatches: ArrayList<Any?>? = null
                        var index = 0
                        while (index < size) {
                            val rawSearchItem: Any? = recentItems.get(index)
                            index++
                            val searchItem = rawSearchItem as SearchItem
                            if (!TextUtils.isEmpty(searchItem.title)) {
                                val title = searchItem.title!!
                                val locale = Locale.US
                                val lowerTitle = title.lowercase(locale)
                                if (koreanLocaleNow) {
                                    val initialHit = initialMatch(searchItem.title, lowerTitle, lowerCase)
                                    if (!TextUtils.isEmpty(initialHit)) {
                                        searchItem.query = initialHit!!.lowercase(locale)
                                        if (recentMatches == null) {
                                            recentMatches = ArrayList<Any?>()
                                        }
                                        if (isReversedNow) {
                                            recentMatches.add(0, searchItem)
                                        } else {
                                            recentMatches.add(searchItem)
                                        }
                                    }
                                }
                                if (lowerTitle.indexOf(lowerCase) != -1) {
                                    searchItem.query = lowerCase
                                    if (recentMatches == null) {
                                        recentMatches = ArrayList<Any?>()
                                    }
                                    if (isReversedNow) {
                                        recentMatches.add(0, searchItem)
                                    } else {
                                        recentMatches.add(searchItem)
                                    }
                                }
                            }
                        }
                        if (recentMatches != null && !recentMatches.isEmpty()) {
                            group(2).addAll(recentMatches)
                        }
                    }
                    // suggestions of the chosen search engine (SuggestEngines; custom engines have none)
                    val serviceUrl = if (BrowserSettings.sugEngine.value != 0) SuggestEngines.url(ZtwoSettings.searchType.value, query, MainUtil.appLocaleTag()) else null
                    if (serviceUrl != null) {
                        webSearchAdapter.closeConnection(true)
                        // 3 s each: 1 s was too short on mobile data, and the filter drops answers to old input anyway
                        val connection = openHttpConnection(3000, 3000, webSearchAdapter.webViewActivity, serviceUrl, null, false)
                        webSearchAdapter.connection = connection
                        var response: String? = null
                        if (connection != null) {
                            response = try { MainUtil.readTextFile(null, connection.getInputStream(), false) } catch (_: Exception) { null }
                            webSearchAdapter.closeConnection(true)
                        }
                        val engineItems = ArrayList<Any?>()
                        for (suggestion in SuggestEngines.parse(response, query)) {
                            val searchItem = SearchItem()
                            searchItem.title = suggestion.title
                            searchItem.path = suggestion.url
                            if (koreanLocaleNow) {
                                val initialHit = initialMatch(suggestion.title, suggestion.title.lowercase(Locale.US), lowerCase)
                                if (!TextUtils.isEmpty(initialHit)) searchItem.query = initialHit!!.lowercase(Locale.US)
                            }
                            engineItems.add(searchItem)
                        }
                        if (isReversedNow) Collections.reverse(engineItems)
                        group(SuggestOrder.ENGINE).addAll(engineItems)
                    }
                    val historyItems = webSearchAdapter.historyItems
                    if (historyItems != null && !historyItems.isEmpty()) {
                        webSearchAdapter.buildItems(
                            R.string.history,
                            lowerCase,
                            webSearchAdapter.historyItems!!,
                            group(4)
                        )
                    }
                    val bookItems = webSearchAdapter.bookItems
                    if (bookItems != null && !bookItems.isEmpty()) {
                        webSearchAdapter.buildItems(
                            R.string.bookmark,
                            lowerCase,
                            webSearchAdapter.bookItems!!,
                            group(8)
                        )
                    }
                    val suggestItems = webSearchAdapter.suggestItems
                    if (suggestItems != null && !suggestItems.isEmpty()) {
                        webSearchAdapter.buildItems(
                            R.string.quick_access,
                            lowerCase,
                            webSearchAdapter.suggestItems!!,
                            group(16)
                        )
                    }
                    // with the bar at the bottom the list is upside down: the first source goes to the bottom
                    for (source in SuggestOrder.current()) {
                        val items = groups[source] ?: continue
                        if (isReversedNow) values.addAll(0, items) else values.addAll(items)
                    }
                    val webSearchListener = webSearchAdapter.webSearchListener
                    if (webSearchListener != null && (webSearchListener as WvaQuickSearchListener).showClip()) {
                        val searchItem = SearchItem()
                        searchItem.itemKind = 2.toByte()
                        searchItem.title = lowerCase
                        if (isReversedNow) {
                            values.add(searchItem)
                        } else {
                            values.add(0, searchItem)
                        }
                    }
                    if (values.isEmpty()) {
                        return null
                    }
                    val searchItem = SearchItem()
                    searchItem.itemKind = 3.toByte()
                    if (isReversedNow) {
                        values.add(0, searchItem)
                    } else {
                        values.add(searchItem)
                    }
                    val results = FilterResults()
                    results.values = values
                    results.count = values.size
                    return results
                }
            }
            return null
        }

        override fun publishResults(
            charSequence: CharSequence?,
            filterResults: FilterResults?
        ) {
            val webSearchAdapter = this@WebSearchAdapter
            webSearchAdapter.query = null
            if (webSearchAdapter.webSearchListener != null) {
                if (filterResults == null || filterResults.count - (if (webSearchAdapter.hasHeader) 1 else 0) <= 0) {
                    webSearchAdapter.pendingItems = null
                    if (webSearchAdapter.filtering) {
                        return
                    }
                    webSearchAdapter.filtering = true
                    val handler = webSearchAdapter.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(webSearchAdapter.ApplyPendingItemsTask())
                    return
                }
                if (!TextUtils.isEmpty(charSequence)) {
                    val query = charSequence.toString().trim { it <= ' ' }
                    if (!TextUtils.isEmpty(query)) {
                        webSearchAdapter.query = query.lowercase()
                    }
                }
                // the filter hands its results over as `Any`: a list of search items (each checked)
                val list = filterResults.values as List<*>
                val arrayList: ArrayList<Any?> = ArrayList()
                for (entry in list) {
                    arrayList.add(entry as SearchItem?)
                }
                webSearchAdapter.pendingItems = arrayList
                if (webSearchAdapter.filtering) {
                    return
                }
                webSearchAdapter.filtering = true
                val handler = webSearchAdapter.mainHandler
                if (handler != null) {
                    handler.post(webSearchAdapter.ApplyPendingItemsTask())
                }
            }
        }
    }

    internal inner class ApplyPendingItemsTask : Runnable {
        override fun run() {
            val webSearchAdapter = this@WebSearchAdapter
            if (webSearchAdapter.webSearchListener == null) {
                return
            }
            val arrayList = webSearchAdapter.pendingItems
            val size = if (arrayList != null) arrayList.size else 0
            val count = if (webSearchAdapter.isReversed) size - 1 else -1
            if (webSearchAdapter.shownSize != size) {
                webSearchAdapter.shownSize = size
                val myRecyclerView = webSearchAdapter.listView
                if (myRecyclerView != null) {
                    myRecyclerView.stopScroll()
                }
            }
            webSearchAdapter.items = arrayList
            webSearchAdapter.notifyDataSetChanged()
            (webSearchAdapter.webSearchListener as WvaQuickSearchListener).onItemsShown(count, size != 0)
            webSearchAdapter.filtering = false
        }
    }

    /** Spacer rows (and the base of [ComposeSearchHolder]); used by [positionOf] to find a row's position. */
    open class SearchHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        /** Row position for lists that are not RecyclerViews (the news-search drop-down) and for the icon loader. */
        @JvmField
        var itemPos: Int = 0
    }

    /** A row of the list drawn by Compose ([com.mycompany.app.ui.browser.SuggestionRow]); the adapter feeds it in [bindCompose]. */
    class ComposeSearchHolder(view: View) : SearchHolder(view) {
        var data: com.mycompany.app.ui.browser.SuggestionRowData? by androidx.compose.runtime.mutableStateOf(null)
        var icon: Bitmap? by androidx.compose.runtime.mutableStateOf(null)
        @JvmField var position: Int = 0
    }

    class SearchItem {
        @JvmField
        var type: Byte = 0
        @JvmField
        var itemKind: Byte = 0
        @JvmField
        var order: Int = 0
        @JvmField
        var id: Long = 0
        @JvmField
        var path: String? = null
        @JvmField
        var title: String? = null
        @JvmField
        var letterColor: Int = 0
        @JvmField
        var sortKey: String? = null
        @JvmField
        var query: String? = null
        @JvmField
        var urlStart: Int = 0
    }

    private class SortSearch(var descending: Boolean) : Comparator<SearchItem?> {
        var sign: Int

        init {
            this.sign = if (descending) -1 else 1
        }

        override fun compare(first: SearchItem?, second: SearchItem?): Int {
            var order: Int = 0
            val searchItem = first
            val other = second
            val sign = this.sign
            if (searchItem == null) {
                if (other == null) {
                    return 0
                }
            } else {
                val index = if (other == null) 0 else searchItem.order
                if (other == null || index < (other.order.also { order = it })) {
                    return -sign
                }
                if (index <= order) {
                    return compareStringsIgnoreCase(searchItem.sortKey, other.sortKey, this.descending)
                }
            }
            return sign
        }
    }

    interface WebSearchListener {
        fun onItemsShown(count: Int, isEmpty: Boolean)

        fun headerCount(): Int

        fun loadRecent()

        fun showClip(): Boolean

        fun setClipText(clipText: String?)

        fun setQuery(cursor: Int, query: String?)

        fun onItemClick(searchItem: SearchItem?)
    }

    init {
        this.recentEnabled = DbBookRecent.isEnabled() && (BrowserSettings.sugType.value and 2) == 2
        this.mainHandler = Handler(Looper.getMainLooper())
        this.listLoader = MainListLoader(webViewActivity, false, IconLoadListener())
    }

    fun closeConnection(disconnect: Boolean) {
        val httpURLConnection = this.connection
        if (disconnect) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect()
                this.connection = null
                return
            }
            return
        }
        this.connection = null
        if (httpURLConnection == null) {
            return
        }
        runAsync(DisconnectTask(httpURLConnection))
    }

    override fun getItemCount(): Int {
        val lastCount = this.lastCount
        if (lastCount != 0) {
            return lastCount
        }
        val arrayList = this.items
        if (arrayList == null) {
            return 0
        }
        return arrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        var viewType: Int
        var headerHeight: Int = 0
        val searchItemV = itemAt(position)
        if (searchItemV == null) {
            return 0
        }
        val itemKind = searchItemV.itemKind
        if (itemKind.toInt() != 3) {
            return itemKind.toInt()
        }
        if (this.isReversed) {
            val webSearchListener = this.webSearchListener
            viewType = 1
            val headers = if (webSearchListener != null) (webSearchListener as WvaQuickSearchListener).headerCount() else 0
            if (webSearchListener != null && headers > (MainApp.dp56.also { headerHeight = it })) {
                viewType = headers - headerHeight
            }
        } else {
            viewType = MainApp.dp56
        }
        return viewType + 3
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ComposeSearchHolder) {
            bindCompose(holder, position)
            return
        }
        // The only other rows are the empty spacer rows (kind 3) that keep the list clear of the bar; a tap on one
        // goes to the listener like before.
        val row = holder.itemView
        row.setTag(holder)
        if (itemAt(position)?.itemKind?.toInt() == 3) row.setOnClickListener(ItemClick())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.getContext()
        if (context == null) {
            throw IllegalStateException("no view holder for view type")
        }
        if (viewType >= 3) {
            val spacer = FrameLayout(context)
            spacer.setLayoutParams(ViewGroup.LayoutParams(-1, viewType - 3))
            return SearchHolder(spacer)
        }
        return createComposeHolder(context)
    }

    private fun createComposeHolder(context: android.content.Context): RecyclerView.ViewHolder {
        val view = androidx.compose.ui.platform.ComposeView(context)
        view.setLayoutParams(ViewGroup.LayoutParams(-1, -2))
        // the list is a legacy RecyclerView: see prepareForLegacyRecycler
        view.prepareForLegacyRecycler()
        val holder = ComposeSearchHolder(view)
        view.setTag(holder)
        view.setContent {
            val data = holder.data
            if (data != null) {
                com.mycompany.app.ui.theme.OpumTheme(darkTheme = data.dark) {
                    com.mycompany.app.ui.browser.SuggestionRow(
                        data = data.copy(icon = holder.icon),
                        onClick = { itemAt(holder.position)?.let { item -> webSearchListener?.onItemClick(item) } },
                        onLongClick = {
                            val longClick = ItemLongClick()
                            runAsync(longClick.CopyItemTask(longClick, holder.position))
                        },
                        onDelete = {
                            if (!filtering) {
                                filtering = true
                                val delete = DeleteClick()
                                runAsync(delete.DeleteItemTask(delete, holder.position))
                            }
                        },
                        onFill = {
                            val item = itemAt(holder.position)
                            if (item != null) {
                                if (!TextUtils.isEmpty(item.path)) {
                                    webSearchListener?.setQuery(item.path!!.length, item.path)
                                } else if (!TextUtils.isEmpty(item.title)) {
                                    val query = item.title + " "
                                    webSearchListener?.setQuery(query.length, query)
                                }
                            }
                        },
                    )
                }
            }
        }
        return holder
    }

    /** Fills a Compose row from item [position]: kind, texts, highlight, icon (cached favicon, or load it) and the rounded outer row. */
    private fun bindCompose(holder: ComposeSearchHolder, position: Int) {
        val item = itemAt(position) ?: return
        holder.position = position
        holder.itemView.setTag(holder)
        val type = item.type.toInt()
        var icon: Bitmap? = null
        val loader = this.listLoader
        // a site the engine suggests (type 0 with an address) gets its favicon like a history entry (list type 18)
        if (item.itemKind.toInt() == 0 && loader != null && type != 37 && !TextUtils.isEmpty(item.path)) {
            val colour = if (type != 32) 0 else item.letterColor
            if (type != 32 || colour == 0 || colour == -460552) {
                val child = ChildItem()
                child.listType = if (type == 0) 18 else type
                child.itemType = 11.toByte()
                child.path = item.path
                child.name = item.title
                child.filePath = item.path
                child.id = item.id
                child.position = position
                val cached = MainListLoader.cachedIconFor(child)
                if (MainUtil.isBitmapOk(cached)) icon = cached else loader.enqueue(child, holder.itemView)
            }
        }
        holder.icon = icon
        val header = if (this.hasHeader) 1 else 0
        val corner = if (this.isReversed) (if (position == header) 1 else 0) else (if (position == itemCount - 1 - header) 2 else 0)
        holder.data = com.mycompany.app.ui.browser.SuggestionRowData(
            rowKind = item.itemKind.toInt(), type = type, title = item.title, url = item.path, query = this.query, urlStart = item.urlStart,
            icon = null, letterColor = if (type == 32) item.letterColor else 0,
            dark = MainUtil.isDarkPage(this.isDark), reversed = this.isReversed, corner = corner,
            // the chosen search engine, whose icon marks its suggestions
            engine = ZtwoSettings.searchType.value.let { if (SuggestEngines.hasService(it)) it else -1 },
        )
    }

    fun itemAt(pos: Int): SearchItem? {
        val arrayList = this.items
        if (arrayList == null || pos < 0 || pos >= arrayList.size) {
            return null
        }
        return this.items!!.get(pos) as SearchItem?
    }

    fun runAsync(runnable: Runnable?) {
        var executorServiceH = this.searchExecutor
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(this.webViewActivity)
            if (executorServiceH == null) {
                return
            } else {
                this.searchExecutor = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    fun applyCorners(view: View?, newTop: Int) {
        // Rounds the outer corner of the first (reversed list) or last suggestion row; [hasHeader] adds a header row.
        val header = if (this.hasHeader) 1 else 0
        val top: Boolean
        val bottom: Boolean
        if (this.isReversed) {
            top = newTop == header
            bottom = false
        } else {
            top = false
            bottom = newTop == getItemCount() - 1 - header
        }
        if (!top && !bottom) {
            view!!.setClipToOutline(false)
            return
        }
        view!!.setOutlineProvider(ItemOutline(top, bottom))
        view.setClipToOutline(true)
    }

    fun buildItems(history: Int, query: String?, arrayList: ArrayList<Any?>?, out: ArrayList<Any?>?) {
        val isReversed = this.isReversed
        try {
            val matches: ArrayList<WebSearchAdapter.SearchItem?> = ArrayList()
            val size = arrayList!!.size
            var matchIndex = 0
            var index = 0
            while (index < size) {
                val rawSearchItem: Any? = arrayList.get(index)
                index++
                val searchItem = rawSearchItem as SearchItem
                if (!TextUtils.isEmpty(searchItem.path) && !TextUtils.isEmpty(searchItem.title)) {
                    val title = searchItem.title!!
                    val locale = Locale.US
                    searchItem.sortKey = title.lowercase(locale)
                    val lowerCase = searchItem.path!!.lowercase(locale)
                    var keyMatch = searchItem.sortKey!!.indexOf(query!!)
                    val urlMatch = lowerCase.indexOf(query, searchItem.urlStart)
                    if (urlMatch == -1) {
                        searchItem.order = keyMatch
                    } else if (keyMatch == -1) {
                        searchItem.order = urlMatch
                        keyMatch = urlMatch
                    } else {
                        keyMatch = min(keyMatch, urlMatch)
                        searchItem.order = keyMatch
                    }
                    if (keyMatch != -1) {
                        matches.add(searchItem)
                        matchIndex++
                        if (matchIndex > 9) {
                            break
                        }
                    } else {
                        continue
                    }
                }
            }
            if (matches.isEmpty()) {
                return
            }
            try {
                Collections.sort<SearchItem?>(matches, SortSearch(isReversed))
            } catch (unused: Exception) {
            }
            val searchItem = SearchItem()
            searchItem.itemKind = 1.toByte()
            searchItem.title = this.webViewActivity!!.getString(history)
            if (isReversed) {
                matches.add(searchItem)
                out!!.addAll(0, matches)
            } else {
                matches.add(0, searchItem)
                out!!.addAll(matches)
            }
        } catch (unused2: Exception) {
        }
    }

    companion object {
        @JvmStatic
        fun positionOf(view: View?): Int {
            var tag: Any? = null
            val searchHolder = if (view == null || (view.getTag()
                    .also { tag = it }) == null || (tag !is SearchHolder)
            ) null else tag
            if (searchHolder == null) {
                return -1
            }
            return searchHolder.bindingAdapterPosition
        }
    }
}
