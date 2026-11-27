package com.mycompany.app.web

import androidx.core.view.ViewCompat

import com.mycompany.app.web.WebNestFrame.ForceRelayoutTask
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import android.animation.Animator
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
import com.mycompany.app.db.book.DbTabState.Companion.stateById
import com.mycompany.app.db.book.DbTabState.Companion.saveState
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
import com.mycompany.app.main.CrashGuard
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
import com.mycompany.app.web.WebNestView.WebHtmlListener
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
import com.mycompany.app.web.WebNestView.WebViewListener
import android.widget.RelativeLayout
import com.mycompany.app.web.WebSslView.SslViewListener
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
import com.mycompany.app.view.MyWebView
import androidx.core.view.NestedScrollingChild
import android.animation.ValueAnimator
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.web.WebNestView.WebBundleListener
import com.mycompany.app.web.WebNestView.WebStyleListener
import com.mycompany.app.quick.QuickView
import com.mycompany.app.web.WebCrashView
import androidx.core.view.NestedScrollingChildHelper
import com.mycompany.app.main.MainWebDestroy
import com.mycompany.app.web.WebNestView.WebQuickListener
import com.mycompany.app.web.WebNestView.WebAnimListener
import com.mycompany.app.web.WebNestView.WebBackListener
import com.mycompany.app.db.book.DbTabState.StateItem
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.web.WebCrashView.CrashViewListener
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.data.book.DataBookOver
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.db.DbUtil
import android.animation.ValueAnimator.AnimatorUpdateListener
import com.mycompany.app.quick.QuickAdapter
import com.mycompany.app.db.book.DbTabState
import com.mycompany.app.quick.QuickView.QuickViewListener
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.view.MyWebBody
import android.os.Looper
import com.mycompany.app.data.book.DataBookAds
import android.view.MotionEvent
import com.mycompany.app.data.book.DataBookJava
import com.mycompany.app.web.WebViewActivity.FaceItem
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
import com.mycompany.app.web.WebNestFrame.PageItem
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
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
class WebNestView(context: Context?) : MyWebView(context), NestedScrollingChild {
    @JvmField
    var fileShownUrl: String? = null
    @JvmField
    var darkUiState: Boolean
    @JvmField
    var animTargetX: Int = 0
    @JvmField
    var sslErrorIn: SslError? = null
    @JvmField
    var savingState: Boolean = false
    @JvmField
    var darkWebState: Boolean
    @JvmField
    var pageAnimator: ValueAnimator? = null
    @JvmField
    var sslHandlerAdd: SslErrorHandler? = null
    @JvmField
    var saveFrame: WebNestFrame? = null
    @JvmField
    var themeImgState: Boolean
    @JvmField
    var drawBackdrop: Boolean = false
    @JvmField
    var sslErrorAdd: SslError? = null
    @JvmField
    var saveTabId: Long = 0
    @JvmField
    var imgBrightState: Int
    @JvmField
    var inOnDraw: Boolean = false
    @JvmField
    var sslHandlerShow: SslErrorHandler? = null
    @JvmField
    var saveUrl: String? = null
    @JvmField
    var darkModeState: Boolean = false
    @JvmField
    var inDispatchDraw: Boolean = false
    @JvmField
    var sslErrorShow: SslError? = null
    /** Host of the certificate error page now shown, and of the last one the user chose "Continue" on (the lock's
     *  security sheet then warns instead of saying "secure"; 2026-11-20). */
    var sslPendingHost: String? = null
    var sslBypassedHost: String? = null
    @JvmField
    var saveBundle: Bundle? = null
    @JvmField
    var darkHomeState: Boolean = false
    @JvmField
    var shadeAlpha: Int = 0
    @JvmField
    var crashUrlIn: String? = null
    @JvmField
    var saveListener: WebBundleListener? = null
    @JvmField
    var startPageState: Boolean = false
    @JvmField
    var shadePaint: Paint? = null
    @JvmField
    var crashUrlAdd: String? = null
    @JvmField
    var webScaleState: Int
    @JvmField
    var wallPaint: Paint? = null
    @JvmField
    var crashUrlShow: String? = null
    @JvmField
    var agentTypeState: Int
    @JvmField
    var edgePaint: Paint? = null
    @JvmField
    var pendingLoadUrl: String? = null
    @JvmField
    var hideImageState: Boolean
    @JvmField
    var shadeColor: Int = 0
    @JvmField
    var pendingLoadHeaders: HashMap<String, String>? = null
    @JvmField
    var preRasterState: Boolean
    @JvmField
    var lastWallColor: Int = 0
    @JvmField
    var offlineUrl: String? = null
    @JvmField
    var textSizeState: Int
    @JvmField
    var edgeColor: Int = 0
    @JvmField
    var offlineHtml: String? = null
    @JvmField
    var scrollPosState: Int
    @JvmField
    var overScrollX: Boolean = false
    @JvmField
    var styleRetries: Int = 0
    @JvmField
    var fontPathState: String?
    @JvmField
    var overScrollY: Boolean = false
    @JvmField
    var styleRunning: Boolean = false
    @JvmField
    var fontBoldState: Boolean
    @JvmField
    var swipeTouch: Boolean = false
    @JvmField
    var styleState: Int = 0
    @JvmField
    var cookieState: Byte = 0
    @JvmField
    var multiTouch: Boolean = false
    @JvmField
    var styleListener: WebStyleListener? = null
    @JvmField
    var blockAmpState: Boolean
    @JvmField
    var pullAllowed: Boolean = false
    @JvmField
    var styleJs: String? = null
    @JvmField
    var enableJsState: Boolean
    @JvmField
    var hasScrolled: Boolean = false
    @JvmField
    var pageState: Int = 0
    @JvmField
    var newPageMode: Boolean
    @JvmField
    var scrollTop: Int = 0
    @JvmField
    var styleStateDone: Int = 0
    @JvmField
    var currentUrl: String? = null
    @JvmField
    var scrollExtent: Int = 0
    @JvmField
    var styleUrl: String? = null
    @JvmField
    var fileUrl: String? = null
    @JvmField
    var scrollRange: Int = 0
    @JvmField
    var namuUrl: String? = null
    @JvmField
    var fileSourceUrl: String? = null
    @JvmField
    var quickPanel: QuickView? = null
    @JvmField
    var overlayUrl: String? = null
    @JvmField
    var bookUrl: String? = null
    @JvmField
    var startPage: Boolean = false
    @JvmField
    var overlayRetries: Int = 0
    @JvmField
    var sslView: WebSslView? = null
    @JvmField
    var overlayRunnable: Runnable
    @JvmField
    var sslShown: Boolean = false
    @JvmField
    var adsRunning: Boolean = false
    @JvmField
    var crashView: WebCrashView? = null
    @JvmField
    var adsChanged: Boolean = false
    @JvmField
    var alive: Boolean
    @JvmField
    var downUrl: String? = null
    @JvmField
    var crashShown: Boolean = false
    @JvmField
    var cleanCheckJs: String? = null
    @JvmField
    var uiHandler: Handler?
    @JvmField
    var downVideo: String? = null
    @JvmField
    var backPlay: Boolean = false
    @JvmField
    var adsRecheck: Boolean = false
    @JvmField
    var hasSetting: Boolean = false
    @JvmField
    var videoUrls: ArrayList<String?>? = null
    @JvmField
    var backPlayHeld: Boolean = false
    @JvmField
    var cleanNewPageIn: Boolean = false
    @JvmField
    var layoutRtl: Boolean
    @JvmField
    var videoPageUrls: ArrayList<String?>? = null
    @JvmField
    var jsAdded: Boolean = false
    @JvmField
    var cleanUrlIn: String? = null
    @JvmField
    var lifeState: Byte = 0
    @JvmField
    var videoPosters: ArrayList<String?>? = null
    @JvmField
    var adsHost: String? = null
    @JvmField
    var cleanBaseHostIn: String? = null
    @JvmField
    var resumedFromPause: Boolean = false
    @JvmField
    var downloadedVideos: ArrayList<String?>? = null
    @JvmField
    var adsBareHost: String? = null
    @JvmField
    var cleanHostIn: String? = null
    @JvmField
    var windowClosed: Boolean = false
    @JvmField
    var failedImages: ArrayList<String?>? = null
    @JvmField
    var backdropBitmap: Bitmap? = null
    @JvmField
    var cleanScanHtmlIn: Boolean = false
    @JvmField
    var detached: Boolean = false
    @JvmField
    var faceItems: ArrayList<WebViewActivity.FaceItem?>? = null
    @JvmField
    var backdropPaint: Paint? = null
    @JvmField
    var cleanAdsRecheckIn: Boolean = false
    @JvmField
    var loading: Boolean = false
    @JvmField
    var hasDummyVideo: Boolean = false
    @JvmField
    var backdropLandscape: Boolean = false
    @JvmField
    var cleanNewPage: Boolean = false
    @JvmField
    var visibilityChanged: Boolean = false
    @JvmField
    var hasM3u8: Boolean = false
    @JvmField
    var drawBackdropOnce: Boolean = false
    @JvmField
    var cleanUrl: String? = null
    @JvmField
    var clientSet: Boolean = false
    @JvmField
    var videoState: Int = 0
    @JvmField
    var fillBackground: Boolean = false
    @JvmField
    var cleanBaseHost: String? = null
    @JvmField
    var touchY: Int = 0
    @JvmField
    var downNoti: Boolean = false
    @JvmField
    var googleSearch: Boolean = false
    @JvmField
    var cleanHost: String? = null
    @JvmField
    var touchLocation: IntArray
    @JvmField
    var downNotiHidden: Boolean = false
    @JvmField
    var instanceTag: String? = null
    @JvmField
    var cleanScanHtml: Boolean = false
    @JvmField
    var touchLocation2: IntArray
    @JvmField
    var blockUrl: String? = null
    @JvmField
    var checkedDocUrl: String? = null
    @JvmField
    var cleanAdsRecheck: Boolean = false
    @JvmField
    var touchOffsetY: Int = 0
    @JvmField
    var adsBlocked: Int = 0
    @JvmField
    var webCleanCache: WebClean? = null
    /** Cosmetic bridge of the uBlock Origin-compatible engine (null unless its test switch was on when attached). */
    @JvmField
    var uboCosmetic: com.mycompany.app.web.filter.UboCosmetic? = null
    @JvmField
    var cleanDoneUrl: String? = null
    @JvmField
    var scrollHelper: NestedScrollingChildHelper?
    @JvmField
    var popsBlocked: Int = 0
    @JvmField
    var executorCache: ExecutorService? = null
    @JvmField
    var viewListener: WebViewListener? = null
    @JvmField
    var backTab: Boolean = false
    @JvmField
    var webDestroy: MainWebDestroy? = null
    @JvmField
    var quickListener: WebQuickListener? = null
    @JvmField
    var backFrame: WebNestFrame? = null
    @JvmField
    var resumeUrl: String? = null
    @JvmField
    var animListener: WebAnimListener? = null
    @JvmField
    var backUrlValue: String? = null
    @JvmField
    var resumeHost: String? = null
    @JvmField
    var htmlListener: WebHtmlListener? = null
    @JvmField
    var backHost: String? = null
    @JvmField
    var resumeBlank: Boolean = false
    @JvmField
    var animIndex: Byte = 0
    @JvmField
    var backListener: WebBackListener? = null
    @JvmField
    var backIndex: Int = 0
    @JvmField
    var resumeReload: Boolean = false
    @JvmField
    var animForward: Boolean = false
    @JvmField
    var barColor: Int = 0
    @JvmField
    var blankPending: Boolean = false
    @JvmField
    var resumeDarkFix: Boolean = false
    @JvmField
    var animValue: Float = 0f
    @JvmField
    var barColor2: Int = 0
    @JvmField
    var blankId: Long = 0
    @JvmField
    var resumeQuick: Boolean = false
    @JvmField
    var animRunning: Boolean = false
    @JvmField
    var hasIcon: Boolean = false
    @JvmField
    var blankUrl: String? = null
    @JvmField
    var resumeUiChanged: Boolean = false
    @JvmField
    var animRunnable: Runnable?
    @JvmField
    var deskMode: Boolean = false
    @JvmField
    var blankPageUrl: String? = null
    @JvmField
    var quickPanelNew: QuickView? = null
    @JvmField
    var blankState: StateItem? = null
    @JvmField
    var addPageState: Boolean
    @JvmField
    var swipeOffset: Float = 0f
    @JvmField
    var sslHandlerIn: SslErrorHandler? = null
    @JvmField
    var docCheckUrl: String? = null

    internal inner class PageSetupTask : Runnable {
        override fun run() {
            val url: String?
            val resumeBlank: Boolean
            var host: String?
            val addPage: Boolean
            val addPageSetting: Boolean
            val desktop: Boolean
            val visibilityChanged: Boolean
            var needsRefresh: Boolean
            var settings: WebSettings?
            val hideImage: Boolean
            val hideImageSetting: Boolean
            val preRaster: Boolean
            val preRasterSetting: Boolean
            val textSize: Int
            val textSizeSetting: Int
            val scrollPos: Int
            val scrollPosSetting: Int
            val blockAmp: Boolean
            val blockAmpSetting: Boolean
            val enableJs: Boolean
            val enableJsSetting: Boolean
            val darkWeb: Boolean
            val darkWebSetting: Boolean
            val resumeDarkFix: Boolean
            val resumeQuick: Boolean
            val isStartPage: Boolean
            val darkUi: Boolean
            val darkUiSetting: Boolean
            var uiChanged: Boolean
            val uiHandler: Handler?
            val webSslView: WebSslView?
            val webCrashView: WebCrashView?
            val textZoom: Int
            val newTextSize: Int
            val webNestView = this@WebNestView
            if (webNestView.alive) {
                var newPage = true
                if (webNestView.lifeState.toInt() == 1) {
                    val context = webNestView.getContext()
                    webNestView.setViewCookie(context)
                    if (!webNestView.blankPending) {
                        if (TextUtils.isEmpty(webNestView.blankPageUrl)) {
                            url = webNestView.getUrl()
                            resumeBlank = false
                        } else {
                            url = webNestView.blankPageUrl
                            resumeBlank = true
                        }
                        webNestView.currentUrl = url
                        host = null
                        webNestView.blankPageUrl = null
                        addPage = webNestView.addPageState
                        addPageSetting = ZtwoSettings.addPage.value
                        if (addPage != addPageSetting) {
                            webNestView.setAddPage(addPageSetting)
                        }
                        desktop = webNestView.isDesktopAgent()
                        visibilityChanged = webNestView.visibilityChanged
                        webNestView.visibilityChanged = false
                        if (!BrowserSettings.deskLock.value || webNestView.deskMode || webNestView.mobileChosen) {
                            needsRefresh = visibilityChanged
                            settings = null
                        } else {
                            webNestView.deskMode = true
                            settings = webNestView.getSettings()
                            setUserAgent(settings, chromeVersion(context))
                            needsRefresh = true
                        }
                        if (webNestView.agentTypeState != ZtwoSettings.agentType.value) {
                            if (settings == null) {
                                settings = webNestView.getSettings()
                            }
                            if (webNestView.applyUserAgent(context, ZtwoSettings.agentType.value, settings)) {
                                needsRefresh = true
                            }
                        }
                        if (desktop != webNestView.isDesktopAgent()) {
                            evalPageJs(webNestView, url, MainUtil.hostOf((url ?: ""), true))
                            needsRefresh = true
                        }
                        hideImage = webNestView.hideImageState
                        hideImageSetting = ZoneSettings.hideImage.value
                        if (hideImage != hideImageSetting) {
                            webNestView.setHideImage(hideImageSetting)
                            needsRefresh = true
                        }
                        preRaster = webNestView.preRasterState
                        preRasterSetting = ZoneSettings.preRaster.value
                        if (preRaster != preRasterSetting) {
                            webNestView.setPreRaster(preRasterSetting)
                        }
                        textSize = webNestView.textSizeState
                        textSizeSetting = ZoneSettings.textSize.value
                        if (textSize != textSizeSetting) {
                            webNestView.setTextSize(textSizeSetting)
                        }
                        scrollPos = webNestView.scrollPosState
                        scrollPosSetting = ZoneSettings.scrollPos.value
                        if (scrollPos != scrollPosSetting) {
                            webNestView.setScrollPos(scrollPosSetting)
                        }
                        // reload only when the font setting really changed (not on every return to the page from a
                        // menu, the bookmarks or another app)
                        if (webNestView.fontBoldState != ZoneSettings.fontBold.value || !MainUtil.sameObject(webNestView.fontPathState, ZoneSettings.fontPath.value)) {
                            webNestView.fontPathState = ZoneSettings.fontPath.value
                            webNestView.fontBoldState = ZoneSettings.fontBold.value
                            needsRefresh = true
                        }
                        if (webNestView.webScaleState != ZtwoSettings.webScale.value) {
                            if (settings == null) {
                                settings = webNestView.getSettings()
                            }
                            textZoom = settings.getTextZoom()
                            newTextSize = ZoneSettings.textSize.value
                            if (textZoom != newTextSize) {
                                settings.setTextZoom(newTextSize)
                            }
                            webNestView.webScaleState = ZtwoSettings.webScale.value
                            needsRefresh = true
                        }
                        blockAmp = webNestView.blockAmpState
                        blockAmpSetting = PdfSettings.blockAmp.value
                        if (blockAmp != blockAmpSetting) {
                            webNestView.blockAmpState = blockAmpSetting
                            if (!needsRefresh && MainUtil.isGoogleSearch(url)) {
                                if (PdfSettings.blockAmp.value) {
                                    webNestView.runInBackground(webNestView.InjectAmpJsTask())
                                } else {
                                    needsRefresh = true
                                }
                            }
                        }
                        enableJs = webNestView.enableJsState
                        enableJsSetting = BrowserSettings.enableJs.value
                        if (enableJs != enableJsSetting) {
                            webNestView.setEnableJs(enableJsSetting)
                            if (BrowserSettings.enableJs.value) {
                                webNestView.applyJsBlock(url, MainUtil.hostOfUrl((url ?: ""), true), true)
                            }
                        }
                        darkWeb = webNestView.darkWebState
                        darkWebSetting = AppState.darkWeb
                        if (darkWeb != darkWebSetting) {
                            webNestView.darkWebState = darkWebSetting
                            webNestView.themeImgState = BrowserSettings.themeImg.value
                            webNestView.imgBrightState = BrowserSettings.imgBright.value
                            if (MainUtil.lacksAlgorithmicDark()) {
                                if (!needsRefresh || webNestView.blankPending || resumeBlank) {
                                    resumeDarkFix = false
                                } else {
                                    resumeDarkFix = true
                                }
                                if (resumeDarkFix) {
                                    host = MainUtil.hostOf((url ?: ""), true)
                                }
                            } else {
                                if (settings == null) {
                                    settings = webNestView.getSettings()
                                }
                                setAlgorithmicDark(settings, AppState.darkWeb)
                                host = MainUtil.hostOf((url ?: ""), true)
                                if (!MainUtil.needsDesktopMode(url, host) && !needsRefresh && !webNestView.blankPending && !resumeBlank) {
                                    resumeDarkFix = true
                                } else {
                                    resumeDarkFix = false
                                }
                            }
                        } else {
                            resumeDarkFix = false
                            if (webNestView.themeImgState == BrowserSettings.themeImg.value || webNestView.imgBrightState != BrowserSettings.imgBright.value) {
                                host = if (darkWebSetting) MainUtil.hostOf((url ?: ""), true) else null
                                webNestView.applyDarkImageStyle(url, host)
                            }
                        }
                        if (webNestView.quickPanel != null) {
                            resumeQuick = true
                        } else {
                            resumeQuick = false
                        }
                        isStartPage = "file:///android_asset/shortcut.html" == url
                        if (webNestView.startPageState != isStartPage) {
                            webNestView.startPageState = isStartPage
                            webNestView.setStartPage(isStartPage)
                        }
                        if (webNestView.coversPage()) {
                            webNestView.barColor = 0
                            webNestView.barColor2 = 0
                        }
                        darkUi = webNestView.darkUiState
                        darkUiSetting = AppState.darkUi
                        if (darkUi != darkUiSetting) {
                            webNestView.darkUiState = darkUiSetting
                            webSslView = webNestView.sslView
                            if (webSslView != null) {
                                webSslView.applyDark()
                            }
                            webCrashView = webNestView.crashView
                            if (webCrashView != null) {
                                webCrashView.applyDark()
                            }
                            uiChanged = true
                        } else {
                            uiChanged = false
                        }
                        if (webNestView.darkModeState == isDarkMode() || webNestView.darkHomeState != BrowserSettings.darkHome.value) {
                            webNestView.darkModeState = isDarkMode()
                            webNestView.darkHomeState = BrowserSettings.darkHome.value
                            uiChanged = true
                        }
                        if (!ZtwoSettings.addPage.value && (ZtwoSettings.tabOpen.value and 2) != 2) {
                            newPage = false
                        }
                        if (webNestView.newPageMode != newPage) {
                            webNestView.newPageMode = newPage
                            if (!needsRefresh) {
                                needsRefresh = MainUtil.isYouTubeUrl((url ?: ""))
                            }
                        }
                    } else {
                        url = null
                        resumeBlank = false
                        host = null
                        needsRefresh = false
                        resumeDarkFix = false
                        resumeQuick = false
                        uiChanged = false
                    }
                    webNestView.resumeUrl = url
                    webNestView.resumeHost = host
                    webNestView.resumeBlank = resumeBlank
                    webNestView.resumeReload = needsRefresh
                    webNestView.resumeDarkFix = resumeDarkFix
                    webNestView.resumeQuick = resumeQuick
                    webNestView.resumeUiChanged = uiChanged
                    uiHandler = webNestView.uiHandler
                    if (uiHandler == null) {
                        return
                    }
                    uiHandler.post(webNestView.ResumePageTask())
                }
            }
        }
    }

    internal inner class SslViewCallbacks : SslViewListener {
        override fun onClose() {
            // "Continue" on the certificate error page
            this@WebNestView.sslBypassedHost = this@WebNestView.sslPendingHost
            this@WebNestView.removeSslView()
        }

        override fun onSettings() {
            val webViewListener = this@WebNestView.viewListener
            if (webViewListener != null) {
                webViewListener.openAdvanced()
            }
        }

        override fun onProceed() {
            this@WebNestView.removeSslView()
        }
    }

    internal inner class AddSslViewTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val sslErrorHandler = webNestView.sslHandlerAdd
            val sslError = webNestView.sslErrorAdd
            webNestView.sslHandlerAdd = null
            webNestView.sslErrorAdd = null
            if (!webNestView.sslShown) {
                webNestView.removeSslView()
                return
            }
            val webSslView = webNestView.sslView
            if (webSslView != null) {
                webNestView.addView(webSslView, -1, -1)
                webNestView.sslHandlerShow = sslErrorHandler
                webNestView.sslErrorShow = sslError
                webNestView.sslPendingHost = try { android.net.Uri.parse(sslError?.url ?: "").host } catch (_: Exception) { null }
                val handler = webNestView.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(webNestView.ShowSslViewTask())
            }
        }
    }

    internal inner class ShowSslViewTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val sslErrorHandler = webNestView.sslHandlerShow
            val sslError = webNestView.sslErrorShow
            webNestView.sslHandlerShow = null
            webNestView.sslErrorShow = null
            if (webNestView.sslShown) {
                webNestView.showSslError(sslErrorHandler, sslError)
            } else {
                webNestView.removeSslView()
            }
        }
    }

    internal inner class RemoveCrashViewTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val crashUrlIn = webNestView.crashUrlIn
            webNestView.crashUrlIn = null
            if (!webNestView.crashShown) {
                webNestView.removeCrashView(null)
                return
            }
            if (webNestView.crashView == null) {
                val webCrashView = WebCrashView(webNestView.getContext())
                webNestView.crashView = webCrashView
                webCrashView.setListener(webNestView.CrashViewCallbacks())
                webNestView.crashUrlAdd = crashUrlIn
                val handler = webNestView.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(webNestView.AddCrashViewTask())
            }
        }
    }

    internal inner class CrashViewCallbacks : CrashViewListener {
        override fun onAction(action: Int) {
            val webViewListener = this@WebNestView.viewListener
            if (webViewListener != null) {
                webViewListener.onCrashAction(action)
            }
        }

        override fun onRefresh(url: String?) {
            this@WebNestView.removeCrashView(url)
        }
    }

    internal inner class AddCrashViewTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val crashUrlAdd = webNestView.crashUrlAdd
            webNestView.crashUrlAdd = null
            if (!webNestView.crashShown) {
                webNestView.removeCrashView(null)
                return
            }
            val webCrashView = webNestView.crashView
            if (webCrashView != null) {
                webNestView.addView(webCrashView, -1, -1)
                webNestView.crashUrlShow = crashUrlAdd
                val handler = webNestView.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(webNestView.ShowCrashViewTask())
            }
        }
    }

    internal inner class ShowCrashViewTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val crashUrlShow = webNestView.crashUrlShow
            webNestView.crashUrlShow = null
            if (!webNestView.crashShown) {
                webNestView.removeCrashView(null)
                return
            }
            val webCrashView = webNestView.crashView
            if (webCrashView == null) {
                return
            }
            webCrashView.crashUrl = crashUrlShow
            webNestView.crashView!!.applyPadding()
            webNestView.crashView!!.setVisibility(View.VISIBLE)
        }
    }

    internal inner class StopLoadingTask : Runnable {
        override fun run() {
            this@WebNestView.stopLoading()
        }
    }

    internal inner class LoadPendingUrlTask : Runnable {
        override fun run() {
            this@WebNestView.loadPendingUrl()
        }
    }

    internal inner class LoadHtmlTask : Runnable {

        internal inner class LoadOfflineStep : Runnable {
            override fun run() {
                val webNestView = this@WebNestView
                val offlineUrl = webNestView.offlineUrl
                val offlineHtml = webNestView.offlineHtml
                webNestView.offlineUrl = null
                webNestView.offlineHtml = null
                if (TextUtils.isEmpty(offlineHtml)) {
                    return
                }
                MainUtil.loadHtml(webNestView, offlineUrl, offlineHtml)
            }
        }

        override fun run() {
            val webNestView = this@WebNestView
            webNestView.offlineHtml = WebClean.Companion.blockedPageHtml(webNestView.getContext(), webNestView.offlineUrl)
            val handler = webNestView.uiHandler
            if (handler == null) {
                return
            }
            handler.post(LoadOfflineStep())
        }
    }

    internal inner class InjectAmpJsTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val sbP: StringBuilder? = if (!webNestView.alive) null else appendAmpJs(null)
            if (!webNestView.alive || sbP == null) {
                return
            }
            sbP.insert(0, "(function(){")
            sbP.append("})();")
            evalJs(webNestView, sbP.toString(), true)
        }
    }

    internal inner class AnimEndTask(@JvmField val outer: WebNestView, byteValueParam: Int,
                                          @JvmField val forward: Boolean
    ) : Runnable {
        val byteValue: Byte

        init {
            this.byteValue = byteValueParam.toByte()
        }

        override fun run() {
            val webAnimListener = this.outer.animListener
            if (webAnimListener != null) {
                webAnimListener.onAnimEnd(this.byteValue.toInt(), this.forward)
            }
        }
    }

    internal inner class ApplyStyleTask : Runnable {

        internal inner class StyleResultCallback : ValueCallback<String> {

            internal inner class ApplyStyleStep : Runnable {
                override fun run() {
                    val webNestView = this@WebNestView
                    val styleStateDone = webNestView.styleStateDone
                    val styleUrl = webNestView.styleUrl
                    webNestView.styleUrl = null
                    val host = MainUtil.hostOfUrl(styleUrl, true)
                    var hasStyle = false
                    if ("file:///android_asset/shortcut.html" != styleUrl) {
                        val isYouTube = MainUtil.isYouTubeHost(host)
                        var sbR: StringBuilder? = StringBuilder().append("(function(){")
                        val pageState = webNestView.pageState
                        if (pageState == 0 || pageState == 2) {
                            sbR!!.append("function myPageLong(){var cl=")
                            sbR.append(webNestView.longHeight)
                            sbR.append(";return (cl>0)&&(document.documentElement.scrollHeight>cl);}function myPageComp(){return document.readyState=='complete';}function myPageState(){if(!android||!android.onPageState){return false;}var be=document.body;if(be&&be.hasChildNodes()){android.onPageState(myPageLong()?4:myPageComp()?3:2);return true;}if(myPageComp()){android.onPageState(1);return true;}return false;}if(!myPageState()){document.addEventListener('readystatechange',myPageState);}")
                        }
                        if (isYouTube) {
                            if (BrowserSettings.adsBlock.value) {
                                val jsPartB = MainUtil.nativeJs5b()
                                if (!TextUtils.isEmpty(jsPartB)) {
                                    sbR!!.append(jsPartB)
                                }
                            }
                            if (ZtwoSettings.addPage.value || (ZtwoSettings.tabOpen.value and 2) == 2) {
                                val jsPartC = MainUtil.nativeJs5c()
                                if (!TextUtils.isEmpty(jsPartC)) {
                                    sbR!!.append(jsPartC)
                                }
                            }
                        } else if (PdfSettings.blockAmp.value && MainUtil.isGoogleSearch(styleUrl)) {
                            sbR = appendAmpJs(sbR)
                        }
                        val mediaName = MainUtil.mediaNameFor(styleUrl, host, AppState.darkWeb, false)
                        if (!TextUtils.isEmpty(mediaName)) {
                            sbR!!.append(mediaName)
                        }
                        val viewportJs = MainUtil.viewportJs(sbR, webNestView, styleUrl, host)
                        val userFont = ZoneSettings.userFont.value
                        if (userFont || ZoneSettings.fontBold.value) {
                            val bold = ZoneSettings.fontBold.value
                            if (userFont || bold) {
                                viewportJs.append("if(document.head&&!document.getElementById('sb_font_style')){var ele=document.createElement('style');ele.id='sb_font_style';ele.innerText='")
                                if (userFont) {
                                    viewportJs.append("@font-face{font-family:soul_user_font;src:url(\"soul_user_font.ttf\")}")
                                }
                                if (TextUtils.isEmpty(host) || !host!!.endsWith("facebook.com")) {
                                    viewportJs.append("*:not([class*=\"icon\"],[class^=\"fa\"],ion-icon){")
                                } else {
                                    viewportJs.append("*:not([class*=\"icon\"],[class^=\"fa\"],ion-icon,span[class=\"f3\"],span[class=\"f5\"]){")
                                }
                                if (userFont) {
                                    viewportJs.append("font-family:soul_user_font !important;")
                                }
                                if (bold) {
                                    viewportJs.append("font-weight:bold !important;")
                                }
                                viewportJs.append("}';document.head.appendChild(ele);}")
                            }
                        }
                        if (BrowserSettings.headMode() == 1 && styleStateDone != -1) {
                            viewportJs.append("if(android&&android.onThemeDet){")
                            if (isYouTube || isImagePath(styleUrl)) {
                                viewportJs.append("android.onThemeDet('")
                                viewportJs.append(styleStateDone)
                                viewportJs.append("','btc');}")
                            } else {
                                viewportJs.append("if(document.head){var tema=document.querySelector(\"meta[name=theme-color]\");if(tema){android.onThemeDet('")
                                viewportJs.append(styleStateDone)
                                viewportJs.append("',tema.content);}else{android.onThemeDet('")
                                viewportJs.append(styleStateDone)
                                viewportJs.append("',null);}}}")
                            }
                        }
                        if (!TextUtils.isEmpty(host)) {
                            viewportJs.append("document.addEventListener(\"contextmenu\",function(evt){window.sblong=evt.target||evt.srcElement;android.onLongParse();});")
                            if (host!!.endsWith("meeco.kr")) {
                                viewportJs.append("if(document.head){var mece=document.createElement('style');mece.innerText='").append("div[style*=\"position:absolute;z-index:\"]{display:none !important;}div[class*=\"xe_content\"]{opacity:1 !important;}").append("';document.head.appendChild(mece);}")
                            }
                        }
                        if (ZoneSettings.gesCenter.value != 2) {
                            viewportJs.append("var elw=document.querySelectorAll(\"*[style*='translate']\");").append("if(elw&&(elw.length>0)){for(var i=0;i<elw.length;i++){").append("elw[i].addEventListener(\"touchstart\",function(){android.onSlideDet();});}}")
                        }
                        viewportJs.append("})();")
                        evalJs(webNestView, viewportJs.toString(), true)
                    }
                    if (TextUtils.isEmpty(host)) {
                        return
                    }
                    if (TtsSettings.checkOver.value != 0) {
                        val dataBookOverK = DataBookOver.instance(webNestView.getContext())
                        dataBookOverK.javaClass
                        try {
                            val arrayList: ArrayList<String?>? = dataBookOverK.paths
                            if (arrayList != null && !arrayList.isEmpty()) {
                                val trimmedUrl = trimTrailingSlash(styleUrl)
                                if (!TextUtils.isEmpty(trimmedUrl)) {
                                    if (dataBookOverK.paths!!.contains(trimmedUrl)) {
                                        hasStyle = true
                                    } else if (!TextUtils.isEmpty(host)) {
                                        hasStyle = dataBookOverK.paths!!.contains(host)
                                    }
                                }
                            }
                        } catch (unused: Exception) {
                        }
                        if (!hasStyle) {
                            if (TtsSettings.checkOver.value == 1) {
                                webNestView.injectOverlay(styleUrl, true)
                            } else {
                                MainUtil.injectOverlayJs(webNestView)
                            }
                        }
                    } else {
                        webNestView.overlayUrl = null
                    }
                    val handler: Handler? = if (!host!!.endsWith("namu.wiki")) null else webNestView.uiHandler
                    if (!host.endsWith("namu.wiki") || handler == null) {
                        return
                    }
                    handler.postDelayed(webNestView.NamuFixTask(), 800L)
                }
            }

            override fun onReceiveValue(value: String?) {
                val v = this@WebNestView
                v.styleRunning = false
                val state = v.styleState
                val listener = v.styleListener
                if ("0" == value) {
                    // Style not applied yet: try again.
                    v.applyPageStyle(false, state, listener)
                    return
                }
                v.styleJs = null
                if (v.pageState != 4) {
                    if ("4" == value) {
                        v.setPageState(4)
                    } else if ("2" == value) {
                        v.setPageState(2)
                    }
                }
                if (!v.alive) {
                    return
                }
                if (listener != null) {
                    listener.onStyleApplied()
                }
                v.styleStateDone = state
                v.styleUrl = v.getUrl()
                v.runInBackground(ApplyStyleStep())
            }
        }

        override fun run() {
            val webNestView = this@WebNestView
            if (!webNestView.alive) {
                webNestView.styleRetries = 0
                webNestView.styleRunning = false
                return
            }
            if (TextUtils.isEmpty(webNestView.styleJs)) {
                webNestView.styleJs =
                    "function myPageLong(){var cl=" + webNestView.longHeight + ";return (cl>0)&&(document.documentElement.scrollHeight>cl);}(function(){if(!document.head)return 0;var be=document.body;if(be&&be.hasChildNodes()){return myPageLong()?4:2;}return 1;})();"
            }
            webNestView.evaluateJavascript(webNestView.styleJs ?: "", StyleResultCallback())
        }
    }

    internal inner class NamuFixTask : Runnable {

        internal inner class NamuFixStep : Runnable {
            override fun run() {
                val webNestView = this@WebNestView
                val namuUrl = webNestView.namuUrl
                webNestView.namuUrl = null
                val host = MainUtil.hostOf(namuUrl!!, true)
                if (TextUtils.isEmpty(host) || !host!!.endsWith("namu.wiki")) {
                    return
                }
                evalPageJs(webNestView, namuUrl, host)
            }
        }

        override fun run() {
            val webNestView = this@WebNestView
            if (webNestView.alive) {
                webNestView.namuUrl = webNestView.getUrl()
                webNestView.runInBackground(NamuFixStep())
            }
        }
    }

    internal inner class InjectOverlayTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            webNestView.injectOverlay(webNestView.overlayUrl, false)
        }
    }

    internal inner class ContentBlockTask : Runnable {

        internal inner class CleanCheckCallback : ValueCallback<String> {

            internal inner class InjectBlockerStep : Runnable {
                /**
                 * Content blocker injection for the loaded page (url, base host, host, whether it is a new
                 * page, whether to scan the html afterwards, and the url the hide script was already built for). Re-injection on the same page only re-applies the known element commands;
                 * otherwise the cosmetic selectors, style rules and scriptlets are collected and injected.
                 */
                override fun run() {
                    val view = this@WebNestView
                    val newPage = view.cleanNewPage
                    val url = view.cleanUrl
                    val baseHost = view.cleanBaseHost
                    val host = view.cleanHost
                    val scanHtml = view.cleanScanHtml
                    val scanFlag = view.cleanAdsRecheck
                    val doneUrl = view.cleanDoneUrl
                    view.cleanNewPage = false
                    view.cleanUrl = null
                    view.cleanBaseHost = null
                    view.cleanHost = null
                    view.cleanScanHtml = false
                    view.cleanAdsRecheck = false
                    view.cleanDoneUrl = null
                    if (TextUtils.isEmpty(url)) {
                        view.adsRunning = false
                        return
                    }
                    // Filtering (network, cosmetic, scriptlets) is done by the uBlock Origin-compatible engine
                    // (UboFilter / UboCosmetic at document start). Left here: two site fixes of the app.
                    if (!newPage && !TextUtils.isEmpty(doneUrl) && doneUrl!!.contains(url!!)) {
                        if (MainUtil.isYouTubeHost(host) && view.uiHandler != null) {
                            view.uiHandler!!.postDelayed(view.HideYouTubeAdsTask(), 1000L)
                        }
                    } else if (MainUtil.isHitomi(host)) {
                        evalJs(
                            view,
                            "(function(){if(document.head&&!document.getElementById('sb_hmg_style')){var ele=document.createElement('style');ele.id='sb_hmg_style';ele.innerText='*{min-height:0px !important;}';document.head.appendChild(ele);}})();",
                            true
                        )
                    }
                    view.adsRunning = false
                }
            }

            override fun onReceiveValue(value: String?) {
                val doneUrl = value
                val webNestView = this@WebNestView
                if (webNestView.scrollHelper == null) {
                    return
                }
                val cleanNewPageIn = webNestView.cleanNewPageIn
                val cleanUrlIn = webNestView.cleanUrlIn
                val cleanBaseHostIn = webNestView.cleanBaseHostIn
                val cleanHostIn = webNestView.cleanHostIn
                val cleanScanHtmlIn = webNestView.cleanScanHtmlIn
                val cleanAdsRecheckIn = webNestView.cleanAdsRecheckIn
                webNestView.cleanNewPageIn = false
                webNestView.cleanUrlIn = null
                webNestView.cleanBaseHostIn = null
                webNestView.cleanHostIn = null
                webNestView.cleanScanHtmlIn = false
                webNestView.cleanAdsRecheckIn = false
                if (TextUtils.isEmpty(cleanUrlIn)) {
                    webNestView.adsRunning = false
                    return
                }
                webNestView.cleanNewPage = cleanNewPageIn
                webNestView.cleanUrl = cleanUrlIn
                webNestView.cleanBaseHost = cleanBaseHostIn
                webNestView.cleanHost = cleanHostIn
                webNestView.cleanScanHtml = cleanScanHtmlIn
                webNestView.cleanAdsRecheck = cleanAdsRecheckIn
                webNestView.cleanDoneUrl = doneUrl
                webNestView.runInBackground(InjectBlockerStep())
            }
        }

        override fun run() {
            val webNestView = this@WebNestView
            if (webNestView.scrollHelper == null) {
                return
            }
            val cleanCheckJs = webNestView.cleanCheckJs
            webNestView.cleanCheckJs = null
            webNestView.evaluateJavascript(cleanCheckJs ?: "", CleanCheckCallback())
        }
    }

    internal inner class HideYouTubeAdsTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            if (!webNestView.alive) {
                return
            }
            val selectors = "ad-slot-renderer,ytm-companion-ad-renderer,ytm-promoted-sparkles-text-search-renderer,ytm-promoted-sparkles-web-renderer,ytm-promoted-video-renderer"
            if (TextUtils.isEmpty(selectors)) {
                return
            }
            evalJs(
                webNestView,
                "(function(){{var eles=document.querySelectorAll('" + selectors + "');if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){var ele=eles[i];var par=ele.parentNode;if(par){par.removeChild(ele);}else{ele.style.display='none';}}}}})();",
                false
            )
        }
    }

    /**
     * The old engine's page scan ("Element inspection") read the page HTML for generic class / id rules. The uBlock
     * Origin-compatible engine does this in the page itself (UboCosmetic), so the scan only clears its inputs now.
     */
    internal inner class ClearCheckStateTask : Runnable {
        override fun run() {
            val view = this@WebNestView
        }
    }

    internal inner class PageAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val webNestView = this@WebNestView
            if (webNestView.pageAnimator != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (webNestView.pageAnimator != null) {
                    webNestView.animValue = animatedValue
                    if (webNestView.inOnDraw || webNestView.inDispatchDraw || webNestView.animRunning) {
                        return
                    }
                    webNestView.animRunning = true
                    MainApp.postToMain(webNestView.getContext(), webNestView.animRunnable!!)
                }
            }
        }
    }

    internal inner class NotifyAnimEndAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val webNestView = this@WebNestView
            if (webNestView.pageAnimator == null) {
                return
            }
            webNestView.pageAnimator = null
            webNestView.notifyAnimEnd(webNestView.animIndex.toInt(), webNestView.animForward)
        }

        override fun onAnimationEnd(animator: Animator) {
            val webNestView = this@WebNestView
            if (webNestView.pageAnimator == null) {
                return
            }
            MainApp.postToMain(webNestView.getContext(), webNestView.PageAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimPageTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            webNestView.animRunning = false
            if (webNestView.pageAnimator == null) {
                return
            }
            webNestView.setValAnimPage(webNestView.animValue)
        }
    }

    internal inner class ResumePageTask : Runnable {
        override fun run() {
            val found: Boolean
            val canCheck: Boolean
            val webBackListener: WebBackListener?
            val checkedUrl: String?
            val taggedUrl: String?
            val quickAdapter: QuickAdapter?
            val nestView = this@WebNestView
            val resumeUrl = nestView.resumeUrl
            val resumeHost = nestView.resumeHost
            val resumeBlank = nestView.resumeBlank
            val reloadOnResume = nestView.resumeReload
            val resumeDarkFix = nestView.resumeDarkFix
            val resumeQuick = nestView.resumeQuick
            val resumeUiChanged = nestView.resumeUiChanged
            nestView.resumeUrl = null
            nestView.resumeHost = null
            if (nestView.alive) {
                var sameDoc = true
                if (nestView.lifeState.toInt() == 1) {
                    if (nestView.blankPending) {
                        nestView.runInBackground(nestView.RestoreBlankTask())
                    } else if (resumeBlank) {
                        nestView.openUrl(resumeUrl, null)
                    } else {
                        if (!reloadOnResume) {
                            if (nestView.drawBackdropOnce) {
                                nestView.reload()
                                found = true
                            } else {
                                if (resumeDarkFix) {
                                    MainUtil.applyDesktopMode(nestView, resumeUrl!!, resumeHost, AppState.darkWeb)
                                    nestView.invalidate()
                                }
                                found = false
                            }
                            val quickPanel: QuickView? = if (resumeQuick) nestView.quickPanel else null
                            if (resumeQuick && quickPanel != null) {
                                if (resumeUiChanged) {
                                    quickPanel.setColor(true)
                                }
                                quickAdapter = quickPanel.itemAdapter
                                if (quickAdapter != null && !quickAdapter.editMode) {
                                    quickPanel.notifyShown(quickPanel.onStartPage)
                                }
                            }
                            if (nestView.resumedFromPause || found) {
                                canCheck = false
                            } else {
                                canCheck = true
                            }
                            nestView.resumedFromPause = false
                            if (canCheck && BrowserSettings.adsBlock.value && TtsSettings.checkEle.value && !TextUtils.isEmpty(resumeUrl) && ("file:///android_asset/shortcut.html" != resumeUrl)) {
                                checkedUrl = nestView.checkedDocUrl
                                if (TextUtils.isEmpty(checkedUrl)) {
                                    sameDoc = false
                                } else {
                                    taggedUrl = nestView.taggedUrl(resumeUrl)
                                    if (!TextUtils.isEmpty(taggedUrl)) {
                                        sameDoc = checkedUrl == taggedUrl
                                    }
                                }
                                if (!sameDoc) {
                                    nestView.docCheckUrl = resumeUrl
                                    nestView.runInBackground(nestView.DocCheckTask())
                                }
                            }
                            webBackListener = nestView.backListener
                            if (webBackListener != null) {
                                webBackListener.onBack()
                                nestView.backListener = null
                            }
                        } else {
                            // only when a setting that needs it changed (resumeReload); it used to run after every
                            // resume, so each return from the bookmarks, a menu or another app reloaded the page and
                            // the home page (owner, 2026-11-20)
                            nestView.prepareReload()
                        }
                    }
                }
            }
        }
    }

    internal inner class PageAnimEndTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            if (webNestView.pageAnimator == null) {
                return
            }
            webNestView.pageAnimator = null
            webNestView.swipeOffset = webNestView.animTargetX.toFloat()
            webNestView.updateShadeAlpha()
            webNestView.invalidate()
            webNestView.notifyAnimEnd(webNestView.animIndex.toInt(), webNestView.animForward)
        }
    }

    internal inner class RestoreBlankTask : Runnable {

        internal inner class RestoreBlankStep : Runnable {
            override fun run() {
                val webNestView = this@WebNestView
                val stateItem = webNestView.blankState
                webNestView.blankState = null
                webNestView.restoreState(webNestView.blankUrl, stateItem)
                webNestView.blankPending = false
                webNestView.blankUrl = null
            }
        }

        override fun run() {
            val webNestView = this@WebNestView
            webNestView.blankState = DbTabState.stateById(webNestView.getContext(), webNestView.blankId)
            val handler = webNestView.uiHandler
            if (handler == null) {
                return
            }
            handler.post(RestoreBlankStep())
        }
    }

    internal inner class DocCheckTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val docCheckUrl = webNestView.docCheckUrl
            webNestView.docCheckUrl = null
            val host = MainUtil.hostOfUrl(docCheckUrl, true)
            val shortHost = MainUtil.stripHostPrefix(host)
            if (TextUtils.isEmpty(shortHost)) {
                return
            }
            webNestView.setDocChecked(docCheckUrl) // no page scan any more (see ClearCheckStateTask)
        }
    }

    internal inner class BundleTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val webNestFrame = webNestView.saveFrame
            val saveTabId = webNestView.saveTabId
            val saveUrl = webNestView.saveUrl
            val bundle = webNestView.saveBundle
            val webBundleListener = webNestView.saveListener
            webNestView.saveFrame = null
            webNestView.saveTabId = 0L
            webNestView.saveUrl = null
            webNestView.saveBundle = null
            webNestView.saveListener = null
            saveState(
                webNestView.getContext(),
                saveTabId,
                saveUrl,
                bundle,
                if (webNestFrame != null) webNestFrame.listToStr else null
            )
            if (webBundleListener != null) {
                webBundleListener.onBundle()
            }
            webNestView.savingState = false
        }
    }

    internal inner class BackupBitmapCopyTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            webNestView.backdropBitmap = MainUtil.backupBitmapCopy(webNestView.getContext(), webNestView.backdropLandscape)
        }
    }

    internal inner class ClearBackdropTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            if (webNestView.drawBackdrop || webNestView.drawBackdropOnce) {
                webNestView.drawBackdrop = false
                webNestView.drawBackdropOnce = false
                webNestView.clearBackdrop()
                webNestView.invalidate()
            }
        }
    }

    internal inner class EvalJsRawTask(@JvmField val outer: WebNestView,
                                         @JvmField val script: String,
                                         @JvmField val callback: ValueCallback<String>?
    ) : Runnable {
        override fun run() {
            this.outer.evalJsRaw(this.script, this.callback)
        }
    }

    internal inner class RemoveQuickPanelTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            if (!webNestView.startPage) {
                webNestView.removeQuickPanel()
                return
            }
            if (webNestView.quickPanel == null && webNestView.quickPanelNew == null) {
                webNestView.quickPanelNew = QuickView(webNestView.getContext())
                val handler = webNestView.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(webNestView.AddQuickPanelTask())
            }
        }
    }

    internal inner class AddQuickPanelTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val quickView = webNestView.quickPanelNew
            webNestView.quickPanelNew = null
            if (!webNestView.startPage) {
                webNestView.removeQuickPanel()
                return
            }
            if (webNestView.quickPanel != null || quickView == null) {
                return
            }
            webNestView.quickPanel = quickView
            val quickCallbacks = webNestView.QuickPanelCallbacks()
            quickView.onStartPage = true
            quickView.quickListener = quickCallbacks
            webNestView.addView(quickView, -1, -1)
            val handler = webNestView.uiHandler
            if (handler == null) {
                return
            }
            handler.post(webNestView.QuickPanelRefreshTask())
        }
    }

    internal inner class QuickPanelCallbacks : QuickViewListener {
        override fun isBarUpdatePending(): Boolean {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener == null) {
                return false
            }
            return webQuickListener.isBarUpdatePending()
        }

        override fun isLandscape(): Boolean {
            val webNestView = this@WebNestView
            val webQuickListener = webNestView.quickListener
            return if (webQuickListener == null) isLandscape(webNestView.getContext()) else webQuickListener.isLandscape()
        }

        override fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.showLinkDialog(quickItem, isFolder)
            }
        }

        override fun hasTransNews(): Boolean {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener == null) {
                return false
            }
            return webQuickListener.hasTransNews()
        }

        override fun openNewsItem(quickItem: QuickItem?) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.openNewsItem(quickItem)
            }
        }

        override fun webBody(): MyWebBody? {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener == null) {
                return null
            }
            return webQuickListener.webBody()
        }

        override fun onPull(x: Float, y: Float, action: Int): Boolean {
            val webNestView = this@WebNestView
            val webViewListener = webNestView.viewListener
            if (webViewListener == null || !webViewListener.canPull()) {
                return false
            }
            return webNestView.viewListener!!.onPull(x, y, action)
        }

        override fun newsListType(): Int {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener == null) {
                return 0
            }
            return webQuickListener.newsListType()
        }

        override fun setQuickMode(enabled: Boolean) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.setQuickMode(enabled)
            }
        }

        override fun closeDeleteDialog() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.closeDeleteDialog()
            }
        }

        override fun runQuickAction2() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.runQuickAction2()
            }
        }

        override fun runQuickAction1() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.runQuickAction1()
            }
        }

        override fun showEditDialog(quickItem: QuickItem?) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.showEditDialog(quickItem)
            }
        }

        override fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.translateNews(list)
            }
        }

        override fun showDeleteDialog(quickItem: QuickItem?, position: Int) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.showDeleteDialog(quickItem, position)
            }
        }

        override fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.openFolder(type, left, top, path, color)
            }
        }

        override fun isAddingTab(): Boolean {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener == null) {
                return false
            }
            return webQuickListener.isAddingTab()
        }

        override fun addRssButton() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.addRssButton()
            }
        }

        override fun showSettingsMenu(view: View?) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.showSettingsMenu(view)
            }
        }

        override fun openSearchSource(type: Int, url: String?) {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.openSearchSource(type, url)
            }
        }

        override fun refreshUpButton() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.refreshUpButton()
            }
        }

        override fun loadSearchItems() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.loadSearchItems()
            }
        }

        override fun scrollPage() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.scrollPage()
            }
        }

        override fun hasNews(): Boolean {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener == null) {
                return false
            }
            return webQuickListener.hasNews()
        }

        override fun loadNews() {
            val webQuickListener = this@WebNestView.quickListener
            if (webQuickListener != null) {
                webQuickListener.loadNews()
            }
        }
    }

    internal inner class QuickPanelRefreshTask : Runnable {

        internal inner class QuickPanelRefreshStep : Runnable {

            internal inner class RefreshQuickPanelStep : Runnable {
                override fun run() {
                    val webNestView = this@WebNestView
                    if (!webNestView.startPage) {
                        webNestView.removeQuickPanel()
                        return
                    }
                    val quickView = webNestView.quickPanel
                    if (quickView == null) {
                        return
                    }
                    quickView.refresh()
                }
            }

            override fun run() {
                val webNestView = this@WebNestView
                if (!webNestView.startPage) {
                    webNestView.removeQuickPanel()
                    return
                }
                val quickView = webNestView.quickPanel
                if (quickView != null) {
                    quickView.setupView()
                    val handler = webNestView.uiHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(RefreshQuickPanelStep())
                }
            }
        }

        override fun run() {
            val webNestView = this@WebNestView
            if (!webNestView.startPage) {
                webNestView.removeQuickPanel()
                return
            }
            val quickView = webNestView.quickPanel
            if (quickView != null) {
                quickView.createButton()
                val handler = webNestView.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(QuickPanelRefreshStep())
            }
        }
    }

    internal inner class RemoveSslViewTask : Runnable {
        override fun run() {
            val webNestView = this@WebNestView
            val sslErrorHandler = webNestView.sslHandlerIn
            val sslError = webNestView.sslErrorIn
            webNestView.sslHandlerIn = null
            webNestView.sslErrorIn = null
            if (!webNestView.sslShown) {
                webNestView.removeSslView()
                return
            }
            if (webNestView.sslView == null) {
                val webSslView = WebSslView(webNestView.getContext())
                webNestView.sslView = webSslView
                webSslView.setListener(webNestView.SslViewCallbacks())
                webNestView.sslHandlerAdd = sslErrorHandler
                webNestView.sslErrorAdd = sslError
                val handler = webNestView.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(webNestView.AddSslViewTask())
            }
        }
    }

    private class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            if (webView is WebNestView) {
                webView.setWebLoading(false)
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            if (webView is WebNestView) {
                webView.setWebLoading(true)
            }
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            onPageRenderGone(webView, renderProcessGoneDetail)
            return true
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            return true
        }
    }

    interface WebAnimListener {
        fun onAnimEnd(direction: Int, forward: Boolean)
    }

    interface WebBackListener {
        fun onBack()
    }

    interface WebBundleListener {
        fun onBundle()
    }

    interface WebHtmlListener {
        fun onHtml(pageUrl: String?, html: String?)
    }

    interface WebQuickListener {
        fun isBarUpdatePending(): Boolean

        fun isLandscape(): Boolean

        fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean)

        fun hasTransNews(): Boolean

        fun openNewsItem(quickItem: QuickItem?)

        fun webBody(): MyWebBody?

        fun newsListType(): Int

        fun setQuickMode(enabled: Boolean)

        fun closeDeleteDialog()

        fun runQuickAction2()

        fun runQuickAction1()

        fun showEditDialog(quickItem: QuickItem?)

        fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?)

        fun showDeleteDialog(quickItem: QuickItem?, position: Int)

        fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int)

        fun isAddingTab(): Boolean

        fun addRssButton()

        fun showSettingsMenu(view: View?)

        fun openSearchSource(type: Int, url: String?)

        fun refreshUpButton()

        fun loadSearchItems()

        fun scrollPage()

        fun hasNews(): Boolean

        fun loadNews()
    }

    interface WebStyleListener {
        /** Called once the page style was applied. */
        fun onStyleApplied()
    }

    interface WebViewListener {
        fun onCrashAction(action: Int)

        fun openAdvanced()

        fun onProgress(progress: Int)

        fun canPull(): Boolean

        fun onScrollStop()

        fun onBlockCount(count: Int)

        fun onPull(x: Float, y: Float, action: Int): Boolean

        fun onPageCommit(pageUrl: String?)

        /** Swipes did not move a page that the web filter left locked behind a hidden dialog ([checkScrollLock]). */
        fun onScrollStuck(view: WebNestView) {}
    }

    init {
        this.touchLocation = IntArray(2)
        this.touchLocation2 = IntArray(2)
        this.overlayRunnable = InjectOverlayTask()
        this.animRunnable = SetValAnimPageTask()
        this.alive = true
        this.uiHandler = Handler(Looper.getMainLooper())
        MainApp.resumeWebTimers(context, this)
        this.layoutRtl = isRtl(context)
        if (BrowserSettings.deskLock.value) {
            this.deskMode = true
        }
        this.addPageState = ZtwoSettings.addPage.value
        this.darkUiState = AppState.darkUi
        this.darkWebState = AppState.darkWeb
        this.themeImgState = BrowserSettings.themeImg.value
        this.imgBrightState = BrowserSettings.imgBright.value
        this.webScaleState = ZtwoSettings.webScale.value
        this.agentTypeState = ZtwoSettings.agentType.value
        this.hideImageState = ZoneSettings.hideImage.value
        this.preRasterState = ZoneSettings.preRaster.value
        this.textSizeState = ZoneSettings.textSize.value
        this.scrollPosState = ZoneSettings.scrollPos.value
        this.fontPathState = ZoneSettings.fontPath.value
        this.fontBoldState = ZoneSettings.fontBold.value
        this.blockAmpState = PdfSettings.blockAmp.value
        this.enableJsState = BrowserSettings.enableJs.value
        this.newPageMode = ZtwoSettings.addPage.value || (ZtwoSettings.tabOpen.value and 2) == 2
        this.scrollHelper = NestedScrollingChildHelper(this)
        setNestedScrollingEnabled(true)
        setBackgroundColor(0)
    }

    private val wallColor: Int
        get() {
            if (MainUtil.isDarkBars(this.startPage)) {
                return 0
            }
            return if (AppState.darkWeb) -16777216 else -1
        }

    private val webBundle: Bundle?
        get() {
            if (this.blankPending) {
                return null
            }
            try {
                val bundle = Bundle()
                if (saveState(bundle) != null) {
                    return bundle
                }
                return null
            } catch (unused: Exception) {
                return null
            }
        }

    fun setValAnimPage(swipeOffset: Float) {
        this.swipeOffset = swipeOffset
        updateShadeAlpha()
        if (this.inOnDraw || this.inDispatchDraw) {
            return
        }
        invalidate()
    }

    fun pauseOnce() {
        if (this.lifeState.toInt() == 2) {
            return
        }
        onPause()
    }

    fun resetMedia(arrayList: ArrayList<WebViewActivity.FaceItem?>?) {
        this.downVideo = null
        this.videoUrls = null
        this.videoPageUrls = null
        this.videoPosters = null
        this.downloadedVideos = null
        this.failedImages = null
        this.faceItems = arrayList
        this.hasDummyVideo = false
        this.hasM3u8 = false
        this.videoState = 0
        this.downNoti = false
        this.downNotiHidden = false
    }

    fun clearBackdrop() {
        this.backdropBitmap = null
        this.backdropPaint = null
    }

    fun prepareReload(): Boolean {
        if (!this.alive) {
            return true
        }
        this.googleSearch = false
        val url = getUrl()
        if (!TextUtils.isEmpty(url)) {
            val realPageUrl = realUrl(url, true)
            if (!TextUtils.isEmpty(realPageUrl)) {
                this.blockUrl = null
                this.adsBlocked = 0
                this.popsBlocked = 0
                this.drawBackdrop = false
                this.drawBackdropOnce = false
                clearBackdrop()
                if (url == realPageUrl) {
                    reload()
                    return true
                }
                if (url!!.startsWith("file:///")) {
                    getContext()
                }
                openUrl(realPageUrl, null)
                return true
            }
        }
        return false
    }

    fun removeCrashView(crashUrl: String?) {
        this.crashShown = false
        val webCrashView = this.crashView
        if (webCrashView != null) {
            webCrashView.crashListener = null
            webCrashView.crashedUrl = null
            removeView(this.crashView)
            this.crashView = null
            val webViewListener = this.viewListener
            if (webViewListener != null) {
                webViewListener.onPageCommit(crashUrl)
            }
        }
    }

    fun removeQuickPanel() {
        val quickView = this.quickPanel
        if (quickView == null) {
            return
        }
        quickView.releaseView()
        removeView(this.quickPanel)
        this.quickPanel = null
    }

    fun removeSslView() {
        this.sslShown = false
        val webSslView = this.sslView
        if (webSslView != null) {
            val sslErrorHandler = webSslView.sslHandler
            if (sslErrorHandler != null) {
                sslErrorHandler.cancel()
                webSslView.sslHandler = null
            }
            val myButtonImage = webSslView.closeButton
            if (myButtonImage != null) {
                myButtonImage.release()
                webSslView.closeButton = null
            }
            webSslView.sslListener = null
            webSslView.pendingError = null
            removeView(this.sslView)
            this.sslView = null
            val webViewListener = this.viewListener
            if (webViewListener != null) {
                webViewListener.onScrollStop()
            }
        }
    }

    fun runInBackground(runnable: Runnable?) {
        var executorServiceH = this.executorCache
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(getContext())
            if (executorServiceH == null) {
                return
            } else {
                this.executorCache = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    fun applyUserAgent(context: Context?, agentTypeState: Int, webSettings: WebSettings?): Boolean {
        var webSettings = webSettings
        this.agentTypeState = agentTypeState
        if (this.deskMode) {
            return false
        }
        if (webSettings == null) {
            webSettings = getSettings()
        }
        if (agentTypeState == 1) {
            MainUtil.initUserAgent(context)
            val defaultAgent = MainApp.defaultUserAgent
            setUserAgent(
                webSettings,
                if (!TextUtils.isEmpty(defaultAgent)) defaultAgent!!.replace(
                    "; wv",
                    ""
                ) else "Mozilla/5.0 (Linux; Android 14; SM-S24 Build/PQ3A.190801.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/131.0.0.0 Mobile Safari/537.36"
            )
            return true
        }
        if (agentTypeState == 2) {
            setUserAgent(
                webSettings,
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/131.0.6778.154 Mobile/15E148 Safari/604.1"
            )
            return true
        }
        if (agentTypeState == 3) {
            setUserAgent(
                webSettings,
                "Mozilla/5.0 (iPad; CPU OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/131.0.6778.154 Mobile/15E148 Safari/604.1"
            )
            return true
        }
        if (agentTypeState <= 100) {
            setUserAgent(webSettings, MainUtil.defaultUserAgent(context))
            return true
        }
        if (TextUtils.isEmpty(ZtwoSettings.agentUser.value)) {
            setUserAgent(webSettings, MainUtil.defaultUserAgent(context))
            return true
        }
        setUserAgent(webSettings, ZtwoSettings.agentUser.value)
        return true
    }

    fun startAdsClean(cleanUrlIn: String?, adsHost: String?, cleanScanHtmlIn: Boolean) {
        val adsRecheck = this.adsRecheck
        this.adsRecheck = false
        if (DataBookAds.instance(getContext()).hasSiteOf(cleanUrlIn, adsHost)) {
            this.adsRunning = false
            return
        }
        if (!MainUtil.sameObject(this.adsHost, adsHost)) {
            this.adsHost = adsHost
            this.adsBareHost = MainUtil.stripHostPrefix(adsHost)
        }
        val adsBareHost = this.adsBareHost
        if (TextUtils.isEmpty(adsBareHost)) {
            this.adsRunning = false
            return
        }
        this.cleanCheckJs = "(function(){if(document.getElementById('sb_cln_style')){return '$cleanUrlIn';}else{return 0;}})();"
        val adsChanged = this.adsChanged
        this.adsChanged = false
        this.cleanNewPageIn = adsChanged
        this.cleanUrlIn = cleanUrlIn
        this.cleanBaseHostIn = adsHost
        this.cleanHostIn = adsBareHost
        this.cleanScanHtmlIn = cleanScanHtmlIn
        this.cleanAdsRecheckIn = adsRecheck
        val handler = this.uiHandler
        if (handler == null) {
            return
        }
        handler.post(ContentBlockTask())
    }

    fun applyDarkImageStyle(pageUrl: String?, pageHost: String?) {
        this.themeImgState = BrowserSettings.themeImg.value
        this.imgBrightState = BrowserSettings.imgBright.value
        if (!AppState.darkWeb || "file:///android_asset/shortcut.html" == pageUrl) {
            return
        }
        val sb =
            StringBuilder("(function(){{var edk=document.getElementById('sb_dark_style');if(edk){document.head.removeChild(edk);}}")
        val mediaName = MainUtil.mediaNameFor(pageUrl, pageHost, true, true)
        if (!TextUtils.isEmpty(mediaName)) {
            sb.append(mediaName)
        }
        sb.append("})();")
        evalJs(this, sb.toString(), true)
    }

    fun noteVideoSource(match: String?, poster: String?, newPoster: String?) {
        var poster = poster
        try {
            if (!URLUtil.isNetworkUrl(poster)) {
                if (!URLUtil.isNetworkUrl(newPoster)) {
                    return
                } else {
                    poster = newPoster
                }
            }
            val arrayList = this.videoUrls
            if (arrayList == null || arrayList.isEmpty()) {
                return
            }
            val videoUrls = this.videoUrls
            val size = videoUrls!!.size
            var videoIndex = 0
            var index = 0
            while (index < size) {
                val entry: Any? = videoUrls.get(index)
                index++
                val script = entry as String?
                if (script != null && script.contains(match!!)) {
                    if (this.videoPosters == null || videoIndex >= this.videoUrls!!.size) {
                        return
                    } else {
                        this.videoPosters!!.set(videoIndex, poster)
                    }
                }
                videoIndex++
            }
        } catch (unused: Exception) {
        }
    }

    fun addPageVideo(downUrl: String?, downVideo: String?, page: String?, poster: String?): Boolean {
        var page = page
        var poster = poster
        if (downVideo != null) {
            try {
                if (isOtherDownUrl(this.downUrl, downUrl)) {
                    this.videoUrls = null
                    this.videoPageUrls = null
                    this.videoPosters = null
                    this.downloadedVideos = null
                    this.failedImages = null
                    this.hasDummyVideo = false
                    this.hasM3u8 = false
                    this.videoState = 0
                }
                this.downUrl = downUrl
                this.downVideo = downVideo
                val host = hostOf(downUrl, true)
                if (TextUtils.isEmpty(host)) {
                    this.videoUrls = null
                    this.videoPageUrls = null
                    this.videoPosters = null
                    this.downloadedVideos = null
                    this.failedImages = null
                    this.hasDummyVideo = false
                    this.hasM3u8 = false
                    this.videoState = 0
                    return false
                }
                if ("vid_dummy" == downVideo) {
                    this.hasDummyVideo = true
                } else if (downVideo.contains(".m3u8")) {
                    this.hasM3u8 = true
                }
                if (host!!.endsWith("facebook.com")) {
                    val arrayList: ArrayList<String?> = ArrayList()
                    this.videoUrls = arrayList
                    arrayList.add(downVideo)
                    this.videoPageUrls = null
                    this.videoPosters = null
                    this.downloadedVideos = null
                    this.failedImages = null
                    this.videoState = 1
                    return true
                }
                if (page == null) {
                    page = ""
                }
                if (poster == null) {
                    poster = ""
                }
                if (this.videoPageUrls == null) {
                    this.videoPageUrls = ArrayList()
                }
                if (this.videoPosters == null) {
                    this.videoPosters = ArrayList()
                }
                val videoUrls = this.videoUrls
                if (videoUrls == null) {
                    val urls: ArrayList<String?> = ArrayList()
                    this.videoUrls = urls
                    urls.add(downVideo)
                    this.videoPageUrls!!.add(page)
                    this.videoPosters!!.add(poster)
                    this.downloadedVideos = null
                    this.failedImages = null
                    this.videoState = 1
                    if (TextUtils.isEmpty(poster)) {
                        MainUtil.injectPhVideoJs(this, downVideo, host)
                        return true
                    }
                } else {
                    val size = videoUrls.size
                    val urls = this.videoUrls
                    if (size == 0) {
                        urls!!.add(downVideo)
                        this.videoPageUrls!!.add(page)
                        this.videoPosters!!.add(poster)
                        this.videoState = 1
                        if (TextUtils.isEmpty(poster)) {
                            MainUtil.injectPhVideoJs(this, downVideo, host)
                            return true
                        }
                    } else {
                        val videoIndex = urls!!.indexOf(downVideo)
                        if (videoIndex != -1) {
                            if (!TextUtils.isEmpty(page) && videoIndex < this.videoPageUrls!!.size) {
                                this.videoPageUrls!!.set(videoIndex, page)
                            }
                            if (!TextUtils.isEmpty(poster) && videoIndex < this.videoPosters!!.size) {
                                this.videoPosters!!.set(videoIndex, poster)
                                return false
                            }
                        } else {
                            val count = this.videoUrls!!.size
                            if (count >= 50) {
                                this.videoUrls!!.removeAt(0)
                                if (this.videoPageUrls!!.size > 0) {
                                    this.videoPageUrls!!.removeAt(0)
                                }
                                if (this.videoPosters!!.size > 0) {
                                    this.videoPosters!!.removeAt(0)
                                }
                            }
                            this.videoUrls!!.add(downVideo)
                            this.videoPageUrls!!.add(page)
                            this.videoPosters!!.add(poster)
                            this.videoState = this.videoUrls!!.size
                            if (TextUtils.isEmpty(poster) && (count < 5 || host.endsWith("pornhub.com"))) {
                                MainUtil.injectPhVideoJs(this, downVideo, host)
                            }
                        }
                    }
                }
                return true
            } catch (unused: Exception) {
            }
        }
        return false
    }

    fun setFileUrl(fileUrl: String?, fileSourceUrl: String?) {
        this.fileUrl = fileUrl
        this.fileSourceUrl = fileSourceUrl
        if (TextUtils.isEmpty(fileUrl) && isOfflineMht(fileSourceUrl)) {
            this.fileUrl = offlineUrlFor(fileSourceUrl)
        }
    }

    fun updateShadeAlpha() {
        if (this.shadePaint == null || this.wallPaint == null || this.edgePaint == null) {
            return
        }
        val width = getWidth()
        if (width == 0) {
            this.shadeAlpha = 0
            return
        }
        var swipeOffset = this.swipeOffset
        if (!this.layoutRtl) {
            swipeOffset = width - swipeOffset
        }
        val alpha = ((swipeOffset / width) * 161.0f).toInt()
        this.shadeAlpha = alpha
        if (alpha != 0) {
            val color = if (AppState.darkWeb) -16777216 else -1
            val wallColor = this.wallColor
            val edgeColor = this.shadeAlpha shl 24
            if (this.shadeColor != color) {
                this.shadeColor = color
                this.shadePaint!!.setColor(color)
            }
            if (this.lastWallColor != wallColor) {
                this.lastWallColor = wallColor
                if (wallColor != 0) {
                    this.wallPaint!!.setColor(wallColor)
                }
            }
            if (this.edgeColor != edgeColor) {
                this.edgeColor = edgeColor
                this.edgePaint!!.setColor(edgeColor)
            }
        }
    }

    fun injectOverlay(overlayUrl: String?, show: Boolean) {
        if (this.alive) {
            if (!MainUtil.sameObject(this.overlayUrl, overlayUrl)) {
                this.overlayUrl = overlayUrl
                MainUtil.injectOverlayJs(this)
                return
            }
            if (show) {
                this.overlayRetries = 0
            } else {
                val retries = this.overlayRetries + 1
                this.overlayRetries = retries
                if (retries > 3) {
                    return
                }
            }
            MainUtil.injectOverlayJs(this)
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            val runnable = this.overlayRunnable
            handler.removeCallbacks(runnable)
            this.uiHandler!!.postDelayed(runnable, 400L)
        }
    }

    fun stopIfLoading() {
        if (this.loading) {
            this.loading = false
            this.visibilityChanged = true
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(StopLoadingTask())
        }
    }

    fun setBarColors(barColor: Int, color: Int) {
        var color = color
        this.barColor = barColor
        if (barColor == 0) {
            color = 0
        }
        this.barColor2 = color
    }

    fun notifyHtml(href: String?, innerHTML: String?) {
        val webHtmlListener = this.htmlListener
        if (webHtmlListener != null) {
            webHtmlListener.onHtml(href, innerHTML)
        }
    }

    fun saveTabState(
        webNestFrame: WebNestFrame?,
        saveTabId: Long,
        saveUrl: String?,
        webBundleListener: WebBundleListener?
    ) {
        if (this.savingState) {
            if (webBundleListener != null) {
                webBundleListener.onBundle()
                return
            }
            return
        }
        this.savingState = true
        if (!this.alive || webNestFrame == null || !webNestFrame.alive) {
            if (webBundleListener != null) {
                webBundleListener.onBundle()
            }
            this.savingState = false
            return
        }
        val webBundle =
            if (TextUtils.isEmpty(saveUrl) || !saveUrl!!.startsWith("file:///")) this.webBundle else null
        this.saveFrame = webNestFrame
        this.saveTabId = saveTabId
        this.saveUrl = saveUrl
        this.saveBundle = webBundle
        this.saveListener = webBundleListener
        runInBackground(BundleTask())
    }

    fun setStartPage(startPage: Boolean) {
        this.startPage = startPage
        if (!startPage) {
            removeQuickPanel()
            return
        }
        removeSslView()
        removeCrashView(null)
        this.barColor = 0
        this.barColor2 = 0
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.applySettings()
            return
        }
        val handler = this.uiHandler
        if (handler == null) {
            return
        }
        handler.post(RemoveQuickPanelTask())
    }

    fun showSslError(sslErrorHandler: SslErrorHandler?, sslError: SslError?) {
        val webSslView = this.sslView
        if (webSslView == null) {
            return
        }
        val sslHandler = webSslView.sslHandler
        if (sslHandler != null) {
            sslHandler.cancel()
        }
        webSslView.sslHandler = sslErrorHandler
        webSslView.pendingError = sslError
        webSslView.showError()
        this.sslView!!.applyPadding()
        this.sslView!!.setVisibility(View.VISIBLE)
    }

    fun evalJsRaw(script: String?, valueCallback: ValueCallback<String>?) {
        if (this.alive) {
            val mainWebDestroy = this.webDestroy
            if (mainWebDestroy != null) {
                mainWebDestroy.busyChecked = true
            }
            super.evaluateJavascript(script ?: "", valueCallback)
        }
    }

    fun loadPendingUrl() {
        val pendingLoadUrl = this.pendingLoadUrl
        val map = this.pendingLoadHeaders
        this.pendingLoadUrl = null
        this.pendingLoadHeaders = null
        if (pendingLoadUrl == null) {
            return
        }
        if (map == null) {
            super.loadUrl(pendingLoadUrl)
            return
        }
        try {
            super.loadUrl(pendingLoadUrl, map)
        } catch (unused: Exception) {
            super.loadUrl(pendingLoadUrl)
        }
    }

    fun addFailedImage(url: String?) {
        try {
            if (this.videoUrls == null || TextUtils.isEmpty(url)) {
                return
            }
            val arrayList = this.failedImages
            if (arrayList == null) {
                val newList: ArrayList<String?> = ArrayList()
                this.failedImages = newList
                newList.add(url)
            } else {
                if (arrayList.contains(url)) {
                    return
                }
                this.failedImages!!.add(url)
            }
        } catch (unused: Exception) {
        }
    }

    fun addDownloadedVideo(imageOpenUrl: String?, video: Boolean) {
        if (!video) {
            try {
                if (this.videoUrls == null) {
                    return
                }
            } catch (unused: Exception) {
                return
            }
        }
        if (TextUtils.isEmpty(imageOpenUrl)) {
            return
        }
        val arrayList = this.downloadedVideos
        if (arrayList == null) {
            val newList: ArrayList<String?> = ArrayList()
            this.downloadedVideos = newList
            newList.add(imageOpenUrl)
        } else {
            if (arrayList.contains(imageOpenUrl)) {
                return
            }
            this.downloadedVideos!!.add(imageOpenUrl)
        }
    }

    public override fun computeVerticalScrollExtent(): Int {
        val extent = super.computeVerticalScrollExtent()
        this.scrollExtent = extent
        return extent
    }

    public override fun computeVerticalScrollOffset(): Int {
        return super.computeVerticalScrollOffset()
    }

    public override fun computeVerticalScrollRange(): Int {
        val range = super.computeVerticalScrollRange()
        this.scrollRange = range
        if (this.pageState != 4) {
            if (range > getHeight()) {
                setPageState(4)
            } else {
                setPageState(2)
            }
        }
        return this.scrollRange
    }

    fun resetBlockCounts(newBlockUrl: String?) {
        if (!BrowserSettings.adsBlock.value && BrowserSettings.popBlock.value == 0) {
            this.blockUrl = null
            this.adsBlocked = 0
            this.popsBlocked = 0
            return
        }
        val blockUrl = this.blockUrl
        if (TextUtils.isEmpty(blockUrl) || !MainUtil.sameObject(blockUrl, newBlockUrl)) {
            this.adsBlocked = 0
            this.popsBlocked = 0
        } else {
            if (!BrowserSettings.adsBlock.value) {
                this.adsBlocked = 0
            }
            if (BrowserSettings.popBlock.value == 0) {
                this.popsBlocked = 0
            }
        }
        this.blockUrl = newBlockUrl
    }

    override fun destroy() {
        if (this.alive) {
            this.alive = false
            clearHandler(this.uiHandler)
            this.uiHandler = null
            removeAllViewsInLayout()
            super.destroy()
            this.animListener = null
            this.htmlListener = null
            this.backListener = null
            val valueAnimator = this.pageAnimator
            if (valueAnimator != null) {
                valueAnimator.cancel()
                this.pageAnimator = null
            }
            clearBackdrop()
            removeSslView()
            removeCrashView(null)
            this.startPage = false
            val quickView = this.quickPanel
            if (quickView != null) {
                quickView.releaseView()
                this.quickPanel = null
            }
            this.scrollHelper = null
            this.viewListener = null
            this.quickListener = null
            this.styleListener = null
            this.fontPathState = null
            this.fileUrl = null
            this.fileSourceUrl = null
            this.bookUrl = null
            this.downUrl = null
            this.downVideo = null
            this.videoUrls = null
            this.videoPageUrls = null
            this.videoPosters = null
            this.downloadedVideos = null
            this.failedImages = null
            this.faceItems = null
            this.blockUrl = null
            this.blankUrl = null
            this.shadePaint = null
            this.wallPaint = null
            this.edgePaint = null
            this.adsHost = null
            this.adsBareHost = null
            this.webCleanCache = null
            this.executorCache = null
            this.webDestroy = null
        }
    }

    public override fun dispatchDraw(canvas: Canvas) {
        if (!this.alive || this.detached) {
            return
        }
        this.inDispatchDraw = true
        if (this.wallPaint == null || this.edgePaint == null) {
            if (this.drawBackdropOnce) {
                drawDarkBackdrop(canvas)
                clearBackdrop()
            } else if (this.drawBackdrop) {
                drawDarkBackdrop(canvas)
            } else {
                clearBackdrop()
            }
            super.dispatchDraw(canvas)
            this.inDispatchDraw = false
            return
        }
        if (this.swipeOffset <= 0.0f) {
            if (this.drawBackdropOnce) {
                drawDarkBackdrop(canvas)
                clearBackdrop()
            } else if (this.drawBackdrop) {
                drawDarkBackdrop(canvas)
            } else {
                clearBackdrop()
            }
            super.dispatchDraw(canvas)
            this.inDispatchDraw = false
            return
        }
        if (!coversPage()) {
            super.dispatchDraw(canvas)
            this.inDispatchDraw = false
            return
        }
        if (this.shadeAlpha == 0) {
            this.inDispatchDraw = false
            return
        }
        val width = getWidth()
        if (width != 0) {
            val right = width.toFloat()
            if (this.swipeOffset < right) {
                val height = getHeight()
                val scrollX = getScrollX()
                val scrollY = getScrollY()
                val scrollXF = scrollX.toFloat()
                val scrollYF = scrollY.toFloat()
                canvas.translate(scrollXF, scrollYF)
                val lastWallColor = this.lastWallColor
                if (this.layoutRtl) {
                    if (lastWallColor != 0) {
                        canvas.drawRect(0.0f, 0.0f, this.swipeOffset, height.toFloat(), this.wallPaint!!)
                    }
                    drawDarkBackdrop(canvas)
                    val dx = this.swipeOffset - right
                    canvas.translate(dx - scrollXF, -scrollY.toFloat())
                    super.dispatchDraw(canvas)
                    canvas.translate(scrollXF - dx, scrollYF)
                    canvas.drawRect(this.swipeOffset, 0.0f, right, height.toFloat(), this.edgePaint!!)
                } else {
                    if (lastWallColor != 0) {
                        canvas.drawRect(this.swipeOffset, 0.0f, right, height.toFloat(), this.wallPaint!!)
                    }
                    drawDarkBackdrop(canvas)
                    val swipeOffset = this.swipeOffset
                    canvas.translate(swipeOffset - scrollXF, -scrollY.toFloat())
                    super.dispatchDraw(canvas)
                    canvas.translate(scrollXF - swipeOffset, scrollYF)
                    canvas.drawRect(0.0f, 0.0f, this.swipeOffset, height.toFloat(), this.edgePaint!!)
                }
                this.inDispatchDraw = false
                return
            }
        }
        this.inDispatchDraw = false
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return false
        }
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return false
        }
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    /**
     * The browser's bar scroll-hide: the page's touch-scroll deltas go straight to it instead of through
     * Android nested scrolling to the coordinator's old `AppBarLayout`. Null outside the browser activity.
     */
    private val screenPos = IntArray(2)

    /** This view's top on screen (includes the page container's translation). */
    private fun screenY(): Int {
        getLocationInWindow(screenPos)
        return screenPos[1]
    }

    private fun barScroll(): com.mycompany.app.ui.browser.BarScroll? {
        var ctx: android.content.Context? = context
        while (ctx != null) {
            if (ctx is WebViewActivity) {
                // Only the browser's own pages drive the bars. Web views in dialogs (link preview, …) share the
                // activity context; they must not slide the bars or the page behind them.
                val pageArea = ctx.pageLayout ?: return null
                var node: android.view.ViewParent? = parent
                while (node != null) {
                    if (node === pageArea) return ctx.barScroll
                    node = node.parent
                }
                return null
            }
            ctx = (ctx as? android.content.ContextWrapper)?.baseContext
        }
        return null
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        val bars = barScroll()
        if (bars != null) {
            // In bar layouts where the page follows the top bar, the page moves on screen while the bars slide. Report
            // that movement as the window offset (as the old coordinator did) so the touch handling below compensates;
            // reporting 0 made the page scroll with the finger AND shift with the bar, so the content jumped.
            val before = screenY()
            val used = bars.preScroll(dy)
            val moved = screenY() - before
            if (offsetInWindow != null) { offsetInWindow[0] = 0; offsetInWindow[1] = moved }
            if (consumed != null) { consumed[0] = 0; consumed[1] = used }
            return used != 0 || moved != 0
        }
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return false
        }
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        val bars = barScroll()
        if (bars != null) {
            val before = screenY()
            // the touch loop passes its window offset as "consumed" here, which is not a page scroll;
            // the bars only need the unconsumed part, as before the offset was reported
            bars.postScroll(0, dyUnconsumed)
            val moved = screenY() - before
            if (offsetInWindow != null) { offsetInWindow[0] = 0; offsetInWindow[1] = moved }
            return moved != 0
        }
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return false
        }
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val actionMasked = event!!.getActionMasked()
        if (actionMasked == 0) {
            this.hasScrolled = false
            this.overScrollX = false
            this.overScrollY = false
            this.fillBackground = false
            if (this.drawBackdrop || this.drawBackdropOnce) {
                this.drawBackdrop = false
                this.drawBackdropOnce = false
                clearBackdrop()
                if (MainUtil.isDarkBars(this.startPage)) {
                    invalidate()
                }
            }
        } else if (actionMasked == 2 && this.swipeTouch) {
            event.setAction(3)
        }
        this.swipeTouch = false
        return super.dispatchTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        if (!this.alive || this.detached) {
            return
        }
        super.draw(canvas)
    }

    fun ensureWebViewClient() {
        if (this.clientSet) {
            return
        }
        setWebViewClient(LocalWebViewClient())
    }

    override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) {
        if (this.alive) {
            val mainWebDestroy = this.webDestroy
            if (mainWebDestroy != null) {
                mainWebDestroy.busyChecked = true
            }
            if (!(if (mainWebDestroy == null) false else mainWebDestroy.jsBlocked)) {
                super.evaluateJavascript(script, resultCallback)
                return
            }
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(EvalJsRawTask(this, script, resultCallback))
        }
    }

    fun realUrl(url: String?, strict: Boolean): String? {
        var url = url
        val length: Int
        val schemeEnd: Int
        val pathStart: Int
        if (TextUtils.isEmpty(url)) {
            return url
        }
        if (strict) {
            if (!TextUtils.isEmpty(this.fileUrl) && !this.fileUrl!!.startsWith("file:///offline")) {
                url = this.fileUrl
            }
            if (isOfflineMht(url) && url!!.startsWith("file:///data/")) {
                val filePath = url.substring(7)
                if (!TextUtils.isEmpty(filePath) && !File(filePath).exists()) {
                    url = "file:///expired"
                }
            }
        }
        if (url!!.startsWith("https://gdriveplayer")) {
            return "data:text/html;charset=utf-8,<?xml version=\"1.0\"encoding=\"UTF-8\"?><html><head><style>html{background-color:black;}</style></head><body><iframe src=\"" + url + "\"frameborder=\"0\"width=\"100%\"height=\"100%\"allowfullscreen></iframe></body></html>"
        }
        if (isDesktopAgent()) {
            if (url.startsWith("http")) {
                if (url.startsWith("://m.", 4)) {
                    length = url.length
                    schemeEnd = 9
                } else if (url.startsWith("s://m.", 4)) {
                    length = url.length
                    schemeEnd = 10
                } else {
                    length = 0
                    schemeEnd = 0
                }
                if (length > schemeEnd && ((url.indexOf(47.toChar(), schemeEnd)
                        .also { pathStart = it }) <= schemeEnd || pathStart >= length - 2)
                ) {
                    val sb = StringBuilder()
                    if (schemeEnd == 9) {
                        sb.append("http://")
                    } else {
                        sb.append("https://")
                    }
                    sb.append(url.substring(schemeEnd))
                    return sb.toString()
                }
            }
        } else if (url.startsWith("https://www.youtube.com")) {
            return "https://m." + url.substring(12)
        }
        return url
    }

    fun applyJsBlock(pageUrl: String?, pageHost: String?, enabled: Boolean) {
        if (this.enableJsState) {
            if (DataBookJava.instance(getContext()).hasSiteOf(pageUrl, pageHost)) {
                setEnableJs(false)
                if (enabled) {
                    reload()
                    return
                }
                return
            }
            return
        }
        if (DataBookJava.instance(getContext()).hasSiteOf(pageUrl, pageHost)) {
            return
        }
        setEnableJs(true)
        if (enabled) {
            reload()
        }
    }

    var backUrl: String?
        get() = this.backUrlValue
        set(str) {
            this.backUrlValue = str
            this.backHost = MainUtil.hostOfUrl(str!!, true)
        }

    val blockedCount: Int
        get() {
            if (!coversPage()) {
                return this.adsBlocked + this.popsBlocked
            }
            this.blockUrl = null
            this.adsBlocked = 0
            this.popsBlocked = 0
            return 0
        }

    val downFail: MutableList<String?>?
        get() = this.failedImages

    val downList: MutableList<String?>?
        get() = this.videoUrls

    val downPoster: MutableList<String?>?
        get() = this.videoPosters

    val downloaded: MutableList<String?>?
        get() = this.downloadedVideos

    override fun getFavicon(): Bitmap? {
        if (this.startPage) {
            return null
        }
        val shortHost = MainUtil.shortHostOf(getUrl())
        getContext()
        val cached = MainUtil.cachedIcon(shortHost)
        if (MainUtil.isBitmapOk(cached)) {
            return cached
        }
        if (this.hasIcon) {
            return super.getFavicon()
        }
        return null
    }

    val longHeight: Int
        get() {
            val height = getHeight()
            if (height == 0) {
                return 0
            }
            return MainUtil.pxToDp(getContext(), ((height / 2) + height).toFloat()).toInt()
        }

    val pageUrl: String?
        get() {
            if (this.blankPending) {
                return this.blankUrl
            }
            return if (!TextUtils.isEmpty(this.blankPageUrl)) this.blankPageUrl else this.currentUrl
        }

    var pageX: Float
        get() = this.swipeOffset
        set(f) {
            var f = f
            val z: Boolean
            if (this.drawBackdrop) {
                z = false
            } else {
                z = true
                this.drawBackdrop = true
            }
            if (this.layoutRtl) {
                if (f <= 0.0f) {
                    f = 0.1f
                } else if (f >= getWidth()) {
                    f = getWidth() - 0.1f
                }
            }
            if (java.lang.Float.compare(this.swipeOffset, f) == 0) {
                if (z && MainUtil.isDarkBars(this.startPage)) {
                    invalidate()
                    return
                }
                return
            }
            this.swipeOffset = f
            ensureShadePaints()
            updateShadeAlpha()
            invalidate()
        }

    val pubList: MutableList<FaceItem?>?
        get() = this.faceItems

    val themeColor: Int
        get() {
            val i = BrowserSettings.headMode()
            if (i == 0) {
                return 0
            }
            if (i != 2) {
                return this.barColor
            }
            val i2 = PrefWeb.headColor
            if (i2 != 0) {
                return i2
            }
            MainUtil.resetTextSizePref()
            return PrefWeb.headColor
        }

    val themeLight: Int
        get() {
            val i = BrowserSettings.headMode()
            if (i == 0) {
                return 0
            }
            if (i != 2) {
                if (this.barColor == 0) {
                    this.barColor2 = 0
                }
                return this.barColor2
            }
            val i2 = PrefWeb.iconColor
            if (i2 != 0) {
                return i2
            }
            MainUtil.resetTextSizePref()
            return PrefWeb.iconColor
        }

    override fun getTitle(): String? {
        if ("file:///android_asset/shortcut.html" == getUrl()) {
            return "OPUM"
        }
        val title = super.getTitle()
        if ("shortcut.html" == title) {
            return null
        }
        return title
    }

    val transPos: Int
        get() {
            val quickView = this.quickPanel
            if (quickView == null) {
                return 0
            }
            return quickView.transPos
        }

    override fun getUrl(): String? {
        val url = super.getUrl()
        this.currentUrl = url
        return url
    }

    val validPageUrl: String?
        get() = displayUrl(this.pageUrl)

    override fun goBack() {
        this.blockUrl = null
        this.adsBlocked = 0
        this.popsBlocked = 0
        super.goBack()
    }

    override fun goForward() {
        this.blockUrl = null
        this.adsBlocked = 0
        this.popsBlocked = 0
        super.goForward()
    }

    fun applyPageStyle(enabled: Boolean, styleState: Int, webStyleListener: WebStyleListener?) {
        if (enabled) {
            this.styleRetries = 10
        }
        this.styleState = styleState
        this.styleListener = webStyleListener
        if (this.styleRunning) {
            return
        }
        this.styleRunning = true
        val styleRetries = this.styleRetries
        if (styleRetries <= 0) {
            this.styleRunning = false
            return
        }
        this.styleRetries = styleRetries - 1
        if (!this.alive) {
            this.styleRetries = 0
            this.styleRunning = false
        } else {
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ApplyStyleTask())
        }
    }

    override fun hasNestedScrollingParent(): Boolean {
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return false
        }
        return nestedScrollingChildHelper.hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)
    }

    fun drawDarkBackdrop(canvas: Canvas?) {
        if (!MainUtil.isDarkBars(this.startPage) || canvas == null) {
            return
        }
        val screenWidth = MainActivity.screenWidth
        val screenHeight = MainActivity.screenHeight
        if (screenWidth == 0 || screenHeight == 0) {
            return
        }
        var paint = this.backdropPaint
        if (paint == null) {
            paint = Paint()
            this.backdropPaint = paint
        }
        var backdropBitmap = this.backdropBitmap
        if (!MainUtil.isBitmapOk(backdropBitmap)) {
            this.backdropLandscape = screenWidth > screenHeight
            backdropBitmap = MainUtil.backupBitmap(getContext(), this.backdropLandscape)
            this.backdropBitmap = backdropBitmap
            if (!MainUtil.isBitmapOk(backdropBitmap)) {
                runInBackground(BackupBitmapCopyTask())
                return
            }
        }
        val parentView = getParent()
        val parent: Any? = if (parentView == null) null else parentView.getParent()
        if (parentView == null || parent == null) {
            return
        }
        var width = this.swipeOffset
        if (width <= 0.0f) {
            width = 0.0f
        } else if (this.layoutRtl) {
            width -= getWidth().toFloat()
        }
        val scaleX = (screenWidth / backdropBitmap!!.getWidth()).toFloat()
        val height = (screenHeight / backdropBitmap.getHeight()).toFloat()
        canvas.save()
        if (width > 0.0f) {
            canvas.clipRect(width, 0.0f, getWidth().toFloat(), getHeight().toFloat())
        }
        val webLand = TtsSettings.webLand.value
        if (webLand != 0 && screenWidth > screenHeight) {
            width -= webLand.toFloat()
        }
        if (MainConst.isApi30) {
            width -= MainActivity.leftInset.toFloat()
        }
        var translationY = 0.0f - (parent as View).getTranslationY()
        if (BrowserSettings.showStatus.value) {
            translationY -= MainSettings.statusHeight.value.toFloat()
        }
        if (BrowserSettings.fixTop.value) {
            translationY -= topBarHeight().toFloat()
        }
        if (TtsSettings.handMode.value) {
            translationY -= WebViewActivity.Companion.Zn.toFloat()
        }
        canvas.translate(width, translationY)
        canvas.scale(scaleX, height)
        try {
            canvas.drawBitmap(backdropBitmap, 0.0f, 0.0f, paint)
        } catch (unused: Exception) {
        }
        canvas.restore()
    }

    override fun isNestedScrollingEnabled(): Boolean {
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return false
        }
        return nestedScrollingChildHelper.isNestedScrollingEnabled
    }

    fun notifyAnimEnd(direction: Int, animForward: Boolean) {
        val handler: Handler? = if (this.animListener == null) null else this.uiHandler
        if (this.animListener == null || handler == null) {
            return
        }
        handler.post(AnimEndTask(this, direction, animForward))
    }

    fun taggedUrl(url: String?): String? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        if (TextUtils.isEmpty(this.instanceTag)) {
            try {
                this.instanceTag = Integer.toHexString(System.identityHashCode(this))
            } catch (unused: Exception) {
            }
            if (TextUtils.isEmpty(this.instanceTag)) {
                return url
            }
        }
        val sbR = StringBuilder().append(url)
        sbR.append(this.instanceTag)
        return sbR.toString()
    }

    fun videoIndex(url: String?): Int {
        try {
            val arrayList = this.videoUrls
            if (arrayList == null || arrayList.isEmpty()) {
                return -1
            }
            return this.videoUrls!!.indexOf(url)
        } catch (unused: Exception) {
            return -1
        }
    }

    override fun loadUrl(url: String) {
        openUrl(url, null)
    }

    fun videoPoster(index: Int): String? {
        try {
            val arrayList = this.videoPosters
            if (arrayList == null || arrayList.isEmpty() || index < 0 || index >= arrayList.size) {
                return null
            }
            return arrayList.get(index)
        } catch (unused: Exception) {
            return null
        }
    }

    fun videoPageUrl(index: Int): String? {
        try {
            val arrayList = this.videoPageUrls
            if (arrayList == null || arrayList.isEmpty() || index < 0 || index >= arrayList.size) {
                return null
            }
            return arrayList.get(index)
        } catch (unused: Exception) {
            return null
        }
    }

    fun displayUrl(pageUrl: String?): String? {
        if (isOfflineMht(pageUrl)) {
            return if (!TextUtils.isEmpty(this.fileUrl)) this.fileUrl else offlineUrlFor(pageUrl)
        }
        return pageUrl
    }

    override fun onCheckIsTextEditor(): Boolean {
        try {
            return super.onCheckIsTextEditor()
        } catch (unused: Throwable) {
            return true
        }
    }

    override fun onCreateInputConnection(editorInfo: EditorInfo?): InputConnection? {
        var inputConnectionOnCreateInputConnection: InputConnection?
        try {
            inputConnectionOnCreateInputConnection = super.onCreateInputConnection(editorInfo)
        } catch (unused: Exception) {
            inputConnectionOnCreateInputConnection = null
        }
        if (editorInfo != null) {
            try {
                if (isSecretLocked()) {
                    editorInfo.imeOptions = editorInfo.imeOptions or 16777216
                }
                val imeOptions = editorInfo.imeOptions
                if ((imeOptions and 2) == 2) {
                    editorInfo.imeOptions = (imeOptions and (-8)) or 3
                } else {
                    if ((imeOptions and 3) == 3) {
                        editorInfo.imeOptions = (imeOptions and (-8)) or 3
                    } else {
                        if ((imeOptions and 5) == 5) {
                            editorInfo.imeOptions = (imeOptions and (-8)) or 3
                        } else {
                            if ((imeOptions and 4) == 4) {
                                editorInfo.imeOptions = (imeOptions and (-8)) or 3
                            }
                        }
                    }
                }
            } catch (unused2: Exception) {
            }
        }
        return inputConnectionOnCreateInputConnection
    }

    public override fun onDetachedFromWindow() {
        val valueAnimator = this.pageAnimator
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.pageAnimator = null
        }
        super.onDetachedFromWindow()
    }

    public override fun onDraw(canvas: Canvas) {
        if (!this.alive || this.detached) {
            return
        }
        this.inOnDraw = true
        val webSslView: WebSslView? = if (this.sslShown) this.sslView else null
        if (this.sslShown && webSslView != null) {
            webSslView.setTranslationY(getScrollY().toFloat())
        }
        val webCrashView: WebCrashView? = if (this.crashShown) this.crashView else null
        if (this.crashShown && webCrashView != null) {
            webCrashView.setTranslationY(getScrollY().toFloat())
        }
        if (this.shadePaint == null || this.edgePaint == null) {
            if ((this.drawBackdrop || this.fillBackground) && !coversPage()) {
                canvas.drawColor(if (AppState.darkWeb) -16777216 else -1)
            }
            super.onDraw(canvas)
            this.inOnDraw = false
            return
        }
        if (this.swipeOffset <= 0.0f) {
            if ((this.drawBackdrop || this.fillBackground) && !coversPage()) {
                canvas.drawColor(if (AppState.darkWeb) -16777216 else -1)
            }
            super.onDraw(canvas)
            this.inOnDraw = false
            return
        }
        if (coversPage()) {
            super.onDraw(canvas)
            this.inOnDraw = false
            return
        }
        if (this.shadeAlpha == 0) {
            this.inOnDraw = false
            return
        }
        val width = getWidth()
        if (width != 0) {
            val right = width.toFloat()
            if (this.swipeOffset < right) {
                val height = getHeight()
                val scrollX = getScrollX()
                val scrollY = getScrollY()
                val scrollXF = scrollX.toFloat()
                val scrollYF = scrollY.toFloat()
                canvas.translate(scrollXF, scrollYF)
                val swipeOffset = this.swipeOffset
                val paint = this.shadePaint
                if (this.layoutRtl) {
                    val bottom = height.toFloat()
                    canvas.drawRect(0.0f, 0.0f, swipeOffset, bottom, paint!!)
                    val dx = this.swipeOffset - right
                    canvas.translate(dx - scrollXF, -scrollY.toFloat())
                    super.onDraw(canvas)
                    canvas.translate(scrollXF - dx, scrollYF)
                    canvas.drawRect(this.swipeOffset, 0.0f, right, bottom, this.edgePaint!!)
                } else {
                    val pageBottom = height.toFloat()
                    canvas.drawRect(swipeOffset, 0.0f, right, pageBottom, paint!!)
                    val offset = this.swipeOffset
                    canvas.translate(offset - scrollXF, -scrollY.toFloat())
                    super.onDraw(canvas)
                    canvas.translate(scrollXF - offset, scrollYF)
                    canvas.drawRect(0.0f, 0.0f, this.swipeOffset, pageBottom, this.edgePaint!!)
                }
                this.inOnDraw = false
                return
            }
        }
        this.inOnDraw = false
    }

    public override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        this.overScrollX = clampedX
        this.overScrollY = clampedY
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    override fun onPause() {
        if (!this.alive || this.lifeState.toInt() == 2) {
            return
        }
        this.resumedFromPause = false
        this.lifeState = 2.toByte()
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.paused = true
        }
        this.drawBackdropOnce = MainUtil.isDarkBars(this.startPage)
        this.fillBackground = !coversPage()
        this.backPlay = false
        this.backPlayHeld = false
        ensureWebViewClient()
        super.onPause()
        this.darkModeState = isDarkMode()
        this.darkHomeState = BrowserSettings.darkHome.value
        this.startPageState = this.startPage
    }

    override fun onResume() {
        val lifeState = if (!this.alive) 0 else this.lifeState
        if (!this.alive || lifeState.toInt() == 1) {
            return
        }
        this.resumedFromPause = lifeState.toInt() == 2
        this.lifeState = 1.toByte()
        applyRendererPriority() // the memory mode may have changed in the settings meanwhile
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.paused = false
        }
        this.styleRetries = 0
        this.styleRunning = false
        this.swipeOffset = 0.0f
        this.shadeAlpha = 0
        this.drawBackdropOnce = MainUtil.isDarkBars(this.startPage)
        this.fillBackground = !coversPage()
        this.backTab = false
        this.backFrame = null
        this.backUrlValue = null
        this.backHost = null
        this.backPlay = false
        this.backPlayHeld = false
        super.onResume()
        MainApp.resumeWebTimers(getContext(), this)
        val handler = this.uiHandler
        if (handler == null) {
            return
        }
        handler.post(PageSetupTask())
    }

    public override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        this.hasScrolled = true
        this.scrollTop = scrollY
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)
        val webViewListener = this.viewListener
        if (webViewListener != null) {
            webViewListener.onProgress(scrollY)
        }
    }

    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        if (motionEvent != null) trackStuckScroll(motionEvent)
        if (this.swipeOffset > 0.0f) {
            return super.onTouchEvent(motionEvent)
        }
        val actionMasked = motionEvent!!.getActionMasked()
        var handled = false
        if (actionMasked == 5) {
            this.multiTouch = true
        } else if (actionMasked == 0 || actionMasked == 1 || actionMasked == 3) {
            this.multiTouch = false
        }
        if (this.multiTouch) {
            return super.onTouchEvent(motionEvent)
        }
        val y = Math.round(motionEvent.getY())
        if (actionMasked == 0) {
            this.touchOffsetY = 0
            if (BrowserSettings.showStatus.value) {
                this.pullAllowed = true
            } else {
                this.pullAllowed = y > MainApp.dp16
            }
        }
        if (this.pullAllowed && ZoneSettings.pullRefresh.value) {
            val canPull = !this.hasScrolled && this.pageState == 1
            val webViewListener: WebViewListener? = if ((canPull || this.overScrollY)) this.viewListener else null
            if ((canPull || this.overScrollY) && webViewListener != null && webViewListener.canPull() && ((canPull || this.scrollTop == 0) && this.viewListener!!.onPull(
                    motionEvent.getX(),
                    y.toFloat(),
                    actionMasked
                ))) {
                motionEvent.setAction(3)
                return super.onTouchEvent(motionEvent)
            }
        }
        val motionEventObtain = MotionEvent.obtain(motionEvent)
        motionEventObtain.offsetLocation(0.0f, this.touchOffsetY.toFloat())
        if (actionMasked == 0) {
            handled = super.onTouchEvent(motionEventObtain)
            this.touchY = y
            startNestedScroll(View.SCROLL_AXIS_VERTICAL)
        } else if (actionMasked == 1) {
            val result = super.onTouchEvent(motionEventObtain)
            stopNestedScroll()
            this.pullAllowed = false
            handled = result
        } else if (actionMasked == 2) {
            var dy = this.touchY - y
            val scrollConsumed = this.touchLocation2
            val touchLocation = this.touchLocation
            // Browser bars: when the page container moves with the top bar, the WebView must see the
            // finger in its own coordinates (a finger moving together with the page did not move over the content),
            // and only our last position takes the page's movement into account, so the bars keep getting the
            // finger's real travel. The old pattern also shifted the WebView's event, which made it scroll on top of
            // the page moving: the page jumped while the bars slid.
            val barMode = barScroll() != null
            var pageMoved = 0
            if (dispatchNestedPreScroll(0, dy, scrollConsumed, touchLocation)) {
                dy -= scrollConsumed[1]
                val offsetY = touchLocation[1]
                this.touchY = y - offsetY
                if (barMode) {
                    pageMoved = offsetY
                } else {
                    motionEventObtain.offsetLocation(0.0f, -offsetY.toFloat())
                    this.touchOffsetY += touchLocation[1]
                }
            }
            // "last" follows the finger even while nothing is consumed, so the bars get only the latest distance and
            // see the direction change when the finger turns back.
            if (barMode) this.touchY = y - pageMoved
            handled = super.onTouchEvent(motionEventObtain)
            if (dispatchNestedScroll(0, touchLocation[1], 0, dy, touchLocation)) {
                if (barMode) {
                    this.touchY -= touchLocation[1]
                } else {
                    motionEventObtain.offsetLocation(0.0f, touchLocation[1].toFloat())
                    val touchOffsetY = this.touchOffsetY
                    val consumed = touchLocation[1]
                    this.touchOffsetY = touchOffsetY + consumed
                    this.touchY -= consumed
                }
            }
        } else if (actionMasked == 3) {
            val result = super.onTouchEvent(motionEventObtain)
            stopNestedScroll()
            this.pullAllowed = false
            handled = result
        }
        motionEventObtain.recycle()
        return handled
    }

    // ---- pages that stay locked behind a hidden dialog (2026-11-23, see filter/ScrollUnlock) ----
    private var stuckDownX = 0f
    private var stuckDownY = 0f
    private var stuckDownScroll = 0
    private var stuckSwipes = 0
    /** The page address that was already checked, so a page is asked at most once. */
    private var stuckCheckedUrl: String? = null

    /** Counts clearly vertical swipes after which the page has hardly moved; the second one in a row asks the page. */
    private fun trackStuckScroll(ev: MotionEvent) {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                stuckDownX = ev.x
                stuckDownY = ev.y
                stuckDownScroll = scrollY
            }
            MotionEvent.ACTION_UP -> {
                val dy = Math.abs(ev.y - stuckDownY)
                if (dy < MainApp.dp48 * 2 || dy < 2 * Math.abs(ev.x - stuckDownX)) return
                // a page that scrolls moves about as far as the finger; a locked one only shows the bars' small offset
                if (Math.abs(scrollY - stuckDownScroll) > dy / 3) { stuckSwipes = 0; return }
                stuckSwipes++
                val url = getUrl()
                if (stuckSwipes >= 2 && url != null && url != stuckCheckedUrl) {
                    stuckCheckedUrl = url
                    checkScrollLock()
                }
            }
        }
    }

    /** Asks the page script whether it is locked behind a dialog the web filter hid; tells the listener if so. */
    private fun checkScrollLock() {
        try {
            evaluateJavascript("(window.__opumScrollCheck ? window.__opumScrollCheck() : null)") { result ->
                if (result != null && result.contains("\"stuck\":true")) viewListener?.onScrollStuck(this)
            }
        } catch (_: Exception) {
        }
    }

    /** Lifts the page's scroll lock (the user's "Unlock scrolling"). */
    fun unlockScroll() {
        try {
            evaluateJavascript("(window.__opumUnlock ? window.__opumUnlock() : false)", null)
        } catch (_: Exception) {
        }
    }

    public override fun onWindowVisibilityChanged(visibility: Int) {
        if (this.backPlay && visibility == 8) {
            this.backPlayHeld = true
        } else {
            this.backPlayHeld = false
            super.onWindowVisibilityChanged(visibility)
        }
    }

    fun ensureShadePaints() {
        if (this.shadePaint == null) {
            val paint = Paint()
            this.shadePaint = paint
            paint.setStyle(Paint.Style.FILL)
        }
        if (this.wallPaint == null) {
            val shadePaint = Paint()
            this.wallPaint = shadePaint
            shadePaint.setStyle(Paint.Style.FILL)
        }
        if (this.edgePaint == null) {
            val edgePaint = Paint()
            this.edgePaint = edgePaint
            edgePaint.setStyle(Paint.Style.FILL)
        }
    }

    override fun post(action: Runnable?): Boolean {
        if (this.alive) {
            return super.post(action)
        }
        return false
    }

    override fun postDelayed(action: Runnable?, delayMillis: Long): Boolean {
        if (this.alive) {
            return super.postDelayed(action, delayMillis)
        }
        return false
    }

    fun resetSwipeShade() {
        clearBackdrop()
        if (java.lang.Float.compare(this.swipeOffset, 0.0f) == 0) {
            return
        }
        this.swipeOffset = 0.0f
        this.shadeAlpha = 0
        invalidate()
    }

    fun cleanEngine(): WebClean? {
        val webClean = this.webCleanCache
        if (webClean != null) {
            return webClean
        }
        val webCleanU = MainApp.webClean(getContext(), false)
        this.webCleanCache = webCleanU
        return webCleanU
    }

    override fun reload() {
        if (this.alive) {
            this.loading = true
            this.overlayUrl = null
            val mainWebDestroy = this.webDestroy
            if (mainWebDestroy != null) {
                mainWebDestroy.paused = true
            }
            super.reload()
        }
    }

    fun setAddPage(addPageState: Boolean) {
        if (this.addPageState != addPageState) {
            this.addPageState = addPageState
            if (addPageState) {
                clearHistory()
            }
        }
    }

    fun setAdsChanged(adsChanged: Boolean) {
        this.adsChanged = adsChanged
    }

    fun setAdsRunning(adsRunning: Boolean) {
        this.adsRunning = adsRunning
    }

    fun setAnimListener(webAnimListener: WebAnimListener?) {
        this.animListener = webAnimListener
    }

    fun setBackListener(webBackListener: WebBackListener?) {
        this.backListener = webBackListener
    }

    fun setBackPlay(backPlay: Boolean) {
        if (this.backPlay != backPlay) {
            this.backPlay = backPlay
            if (backPlay) {
                this.backPlayHeld = false
            } else if (this.backPlayHeld) {
                this.backPlayHeld = false
                super.onWindowVisibilityChanged(View.GONE)
            }
        }
    }

    fun setBackTab(backTab: Boolean) {
        this.backTab = backTab
    }

    fun setBlankPage(blankPageUrl: String?) {
        this.blankPageUrl = blankPageUrl
    }

    fun setDarkUi(darkUiState: Boolean) {
        this.darkUiState = darkUiState
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.setColor(true)
        }
        val webSslView = this.sslView
        if (webSslView != null) {
            webSslView.applyDark()
        }
        val webCrashView = this.crashView
        if (webCrashView != null) {
            webCrashView.applyDark()
        }
    }

    fun setDarkWeb(darkWebState: Boolean) {
        this.darkWebState = darkWebState
        this.themeImgState = BrowserSettings.themeImg.value
        this.imgBrightState = BrowserSettings.imgBright.value
        val url = getUrl()
        val host = MainUtil.hostOf(url!!, true)
        if (MainUtil.lacksAlgorithmicDark()) {
            MainUtil.applyDesktopMode(this, url, host, darkWebState)
        } else {
            setAlgorithmicDark(getSettings(), darkWebState)
            if (MainUtil.needsDesktopMode(url, host)) {
                MainUtil.applyDesktopMode(this, url, host, darkWebState)
            }
        }
        if (coversPage()) {
            this.barColor = 0
            this.barColor2 = 0
        }
        invalidate()
    }

    fun setDeskMode(desktop: Boolean) {
        // desktop for all tabs makes every page desktop, unless this tab was switched back to mobile from the menu
        if (BrowserSettings.deskLock.value && !this.mobileChosen) {
            this.deskMode = true
        } else {
            this.deskMode = desktop
        }
    }

    /** Switched to mobile from the menu while desktop for all tabs is on: this tab stays mobile (2026-11-27). */
    @JvmField
    var mobileChosen: Boolean = false

    fun setDetached(detached: Boolean) {
        this.detached = detached
        if (detached) {
            stopIfLoading()
        }
    }

    fun setDocChecked(url: String?) {
        this.checkedDocUrl = taggedUrl(url)
    }

    fun setDownNoti(downNoti: Boolean) {
        this.downNoti = downNoti
        this.downNotiHidden = !downNoti
    }

    fun setEnableJs(enableJsState: Boolean) {
        this.enableJsState = enableJsState
        getSettings().setJavaScriptEnabled(enableJsState)
    }

    fun setErrorView(sslShown: Boolean) {
        val webSslView = this.sslView
        if (webSslView != null) {
            this.sslShown = sslShown
            webSslView.setVisibility(if (sslShown) View.VISIBLE else View.INVISIBLE)
        }
        val webCrashView = this.crashView
        if (webCrashView != null) {
            this.crashShown = sslShown
            webCrashView.setVisibility(if (sslShown) View.VISIBLE else View.INVISIBLE)
        }
    }

    fun setHasIcon(hasIcon: Boolean) {
        this.hasIcon = hasIcon
    }

    fun setHasSetting(hasSetting: Boolean) {
        this.hasSetting = hasSetting
    }

    fun setHideImage(hideImageState: Boolean) {
        this.hideImageState = hideImageState
        getSettings().setLoadsImagesAutomatically(!hideImageState)
    }

    fun setHtmlListener(webHtmlListener: WebHtmlListener?) {
        this.htmlListener = webHtmlListener
    }

    fun setJsAdded(jsAdded: Boolean) {
        this.jsAdded = jsAdded
    }

    fun setListener(webViewListener: WebViewListener?) {
        this.viewListener = webViewListener
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return
        }
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled)
    }

    fun setNewsPrepared(list: MutableList<QuickItem?>?) {
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.setNewsPrepared(list)
        }
    }

    fun setNewsProgress(newsProgress: Int) {
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.newsProgress = newsProgress
        }
    }

    fun setNoAutofill(noAutofill: Boolean) {
        try {
            setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)
        } catch (unused: Exception) {
        }
    }

    fun setPageState(pageState: Int) {
        if (pageState > this.pageState) {
            this.pageState = pageState
        }
    }

    /**
     * The renderer's priority from the memory mode ([com.mycompany.app.pref.store.MemoryPlan.yieldRenderer]): kept
     * important while the browser is on screen, and let go before the app when it is out of sight, if the mode allows.
     * The pages share one renderer process, so it is set on every page alike.
     */
    fun applyRendererPriority() {
        try {
            setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, com.mycompany.app.pref.store.MemoryPlan.yieldRenderer())
        } catch (_: Exception) {
        }
    }

    fun setPreRaster(preRasterState: Boolean) {
        this.preRasterState = preRasterState
        getSettings().setOffscreenPreRaster(preRasterState)
    }

    fun setQuickControl(quickControl: QuickControl?) {
        if (quickControl != null) {
            quickControl.setQuickView(this.quickPanel)
        }
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.setQuickControl(quickControl)
        }
    }

    fun setQuickEditMode(dragIndex: Int) {
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.setQuickEditMode(dragIndex)
        }
    }

    fun setQuickListener(webQuickListener: WebQuickListener?) {
        this.quickListener = webQuickListener
    }

    fun setQuickSearch(quickSearch: Boolean) {
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.setQuickSearch(quickSearch)
        }
    }

    fun setScrollPos(scrollPosState: Int) {
        this.scrollPosState = scrollPosState
        if (scrollPosState != 0) {
            setVerticalScrollBarEnabled(false)
        } else {
            setVerticalScrollBarEnabled(true)
        }
    }

    fun setTextSize(textSizeState: Int) {
        this.textSizeState = textSizeState
        getSettings().setTextZoom(textSizeState)
    }

    fun setTransLoaded(isOk: Boolean) {
        val quickView = this.quickPanel
        if (quickView != null) {
            quickView.setTransLoaded(isOk)
        }
    }

    fun setUrlChanged(currentUrl: String?) {
        this.currentUrl = currentUrl
        removeSslView()
        removeCrashView(null)
        this.hasIcon = false
    }

    fun setViewCookie(context: Context?) {
        MainApp.applyCookieSetting(context)
        val cookiesOn = MainApp.cookiesAllowed(true)
        val cookieState = if (cookiesOn) 1.toByte() else 2.toByte()
        if (this.cookieState == cookieState) {
            return
        }
        this.cookieState = cookieState
        MainApp.setThirdPartyCookies(context, this, cookiesOn)
    }

    override fun setVisibility(visibility: Int) {
        setWebVisible(visibility)
        val parent: ViewParent? = if (!this.visibilityChanged && visibility == 0 && getVisibility() != View.VISIBLE) getParent() else null
        if (!this.visibilityChanged && visibility == 0 && getVisibility() != View.VISIBLE && parent != null && (parent is WebNestFrame)) {
            val webNestFrame = parent
            if (!webNestFrame.visibilityChanged) {
                webNestFrame.visibilityChanged = true
                val handler = webNestFrame.uiHandler
                if (handler != null) {
                    handler.post(webNestFrame.ForceRelayoutTask())
                }
            }
        }
        super.setVisibility(visibility)
    }

    fun setWebDestroy(mainWebDestroy: MainWebDestroy?) {
        this.webDestroy = mainWebDestroy
    }

    fun setWebLoading(loading: Boolean) {
        this.loading = loading
    }

    /** Scroll position to bring back once this page has loaded again (it was released to save memory, 2026-11-21). */
    @JvmField
    var restoreScrollX: Int = 0
    @JvmField
    var restoreScrollY: Int = 0

    /**
     * The page finished loading: scrolls back to [restoreScrollY] / [restoreScrollX], once. Pages that grow while
     * loading images get a few more tries; a scroll by the user in between stops them.
     */
    fun restoreScrollIfPending() {
        if (this.restoreScrollY <= 0 && this.restoreScrollX <= 0) return
        val url = getUrl()
        if (url.isNullOrEmpty() || url == "about:blank") return
        val x = this.restoreScrollX
        val y = this.restoreScrollY
        this.restoreScrollX = 0
        this.restoreScrollY = 0
        var last = -1
        var stopped = false
        for (delay in longArrayOf(0L, 300L, 800L, 1600L)) {
            postDelayed({
                if (stopped || !this.alive || this.detached) return@postDelayed
                if (last >= 0 && scrollY != last) { stopped = true; return@postDelayed } // the user scrolled
                if (scrollY >= y) { stopped = true; return@postDelayed }
                scrollTo(x, y)
                last = scrollY
            }, delay)
        }
    }

    override fun setWebViewClient(webViewClient: WebViewClient) {
        var webViewClient = webViewClient
        super.setWebViewClient(webViewClient)
        this.clientSet = true
    }

    fun setWebVisible(visibility: Int) {
        if (visibility != 0) {
            clearBackdrop()
        }
    }

    override fun startNestedScroll(axes: Int): Boolean {
        if (barScroll() != null) return true
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return false
        }
        return nestedScrollingChildHelper.startNestedScroll(axes, ViewCompat.TYPE_TOUCH)
    }

    override fun stopNestedScroll() {
        val bars = barScroll()
        if (bars != null) {
            bars.stop()
            return
        }
        val nestedScrollingChildHelper = this.scrollHelper
        if (nestedScrollingChildHelper == null) {
            return
        }
        nestedScrollingChildHelper.stopNestedScroll(ViewCompat.TYPE_TOUCH)
    }

    fun isNotLive(): Boolean {
        return !this.alive || this.detached || this.backTab || this.blankPending || !TextUtils.isEmpty(this.blankPageUrl)
    }

    fun quickPanelOpen(): Boolean {
        val quickView = this.quickPanel
        return if (quickView == null || !this.startPage || quickView.controlPanel == null) false else true
    }

    fun isOverScrolled(): Boolean {
        return this.overScrollX && !this.overScrollY
    }

    fun coversPage(): Boolean {
        return this.startPage || this.sslShown || this.crashShown
    }

    fun openUrl(rawUrl: String?, blobPageUrl: String?) {
        // tracking parameters ($removeparam rules of the filter lists) are removed before the page is loaded
        val pageUrl = com.mycompany.app.web.filter.UboFilter.cleanAddress(getContext(), rawUrl) ?: rawUrl
        val found: Boolean
        if (this.alive) {
            var gpcOn = true
            this.loading = true
            var map: HashMap<String, String>? = null
            this.overlayUrl = null
            this.googleSearch = false
            val context = getContext()
            var offlinePath = offlinePathFor(context, pageUrl)
            if (DataBookLink.instance(context).hasPage(offlinePath)) {
                this.offlineUrl = offlinePath
                runInBackground(LoadHtmlTask())
                return
            }
            val mainWebDestroy = this.webDestroy
            if (mainWebDestroy != null) {
                mainWebDestroy.paused = true
            }
            MainApp.applyCookieSetting(context)
            if (offlinePath != null && offlinePath.startsWith("chrome://crash")) {
                offlinePath = "about:blank"
            }
            val dnt = SecretSettings.secureDnt.value
            if (dnt == 1) {
                found = true
            } else {
                found = if (dnt == 2) SyncSettings.secretMode.value else false
            }
            if (found) {
                map = HashMap()
                map.put("DNT", "1")
            }
            val gpc = SecretSettings.secureGpc.value
            if (gpc != 1) {
                gpcOn = if (gpc == 2) SyncSettings.secretMode.value else false
            }
            if (gpcOn) {
                if (map == null) {
                    map = HashMap()
                }
                map.put("Sec-GPC", "1")
            }
            if (PdfSettings.saveData.value) {
                if (map == null) {
                    map = HashMap()
                }
                map.put("Save-Data", "on")
            }
            if (!TextUtils.isEmpty(blobPageUrl) && !blobPageUrl!!.startsWith("file:///")) {
                if (map == null) {
                    map = HashMap()
                }
                map.put("referer", blobPageUrl)
            }
            this.pendingLoadUrl = offlinePath
            this.pendingLoadHeaders = map
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(LoadPendingUrlTask())
        }
    }

    fun restoreState(blankUrl: String?, stateItem: StateItem?) {
        if (CrashGuard.takeSafeStart()) {
            // the app crashed twice right after starting: show the start page instead of the page that was open
            com.mycompany.app.main.MainUtil.showToast(context, ch.opumlabs.opumbrowser.R.string.safe_start_note)
            openUrl("file:///android_asset/shortcut.html", null)
            return
        }
        val arrayList: ArrayList<WebNestFrame.PageItem?>?
        val bundle: Bundle?
        if (stateItem != null) {
            bundle = stateItem.bundle
            arrayList = stateItem.pages
        } else {
            arrayList = null
            bundle = null
        }
        if (arrayList != null && !arrayList.isEmpty()) {
            val parent = getParent()
            if (parent is WebNestFrame) {
                parent.restorePage(this, arrayList)
            }
        }
        if (bundle != null) {
            try {
                val webBackForwardListRestoreState = restoreState(bundle)
                val currentItem: WebHistoryItem? = if (webBackForwardListRestoreState != null) webBackForwardListRestoreState.getCurrentItem() else null
                if (webBackForwardListRestoreState != null && currentItem != null && !TextUtils.isEmpty(currentItem.getUrl())) {
                    return
                }
            } catch (unused: Exception) {
            }
        }
        openUrl(blankUrl, null)
    }

    fun isDesktopAgent(): Boolean {
        if (this.deskMode) {
            return true
        }
        val agent: String? = if (ZtwoSettings.agentType.value <= 100) null else ZtwoSettings.agentUser.value
        val length = if (ZtwoSettings.agentType.value <= 100 || agent == null) 0 else agent.length
        if (ZtwoSettings.agentType.value <= 100 || agent == null || length == 0 || !agent.startsWith("Mozilla/5.0 (") || length <= 13) {
            return false
        }
        if (agent.startsWith("X11", 13)) {
            return length > 16 && agent.indexOf("Android", 16) == -1
        }
        return agent.startsWith("Windows NT", 13) && length > 23 && agent.indexOf(
            "Android",
            23
        ) == -1
    }

    companion object {
        @JvmStatic
        fun isOtherDownUrl(downUrl: String?, pageUrl: String?): Boolean {
            val samePrefix: Boolean
            if (MainUtil.sameObject(downUrl, pageUrl)) {
                return false
            }
            val length = if (downUrl != null) downUrl.length else 0
            val pageLength = if (downUrl != null && length != 0 && pageUrl != null) pageUrl.length else 0
            if (downUrl != null && length != 0 && pageUrl != null && pageLength != 0) {
                if (length < pageLength) {
                    if (pageUrl.startsWith("#", length)) {
                        samePrefix = pageUrl.startsWith(downUrl)
                        return !samePrefix
                    }
                } else if (pageLength < length && downUrl.startsWith("#", pageLength)) {
                    samePrefix = downUrl.startsWith(pageUrl)
                    return !samePrefix
                }
            }
            return true
        }
    }
}
