package com.mycompany.app.web

import androidx.core.view.isVisible
import android.animation.Animator
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.MainSettings
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
import com.mycompany.app.db.book.DbTabState.Companion.saveState
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
import android.animation.ValueAnimator.AnimatorUpdateListener
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
import com.mycompany.app.web.WebNestFrame.WebFrameListener
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
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.webkit.HttpAuthHandler
import android.widget.ArrayAdapter
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("deprecation")
class WebNestFrame(context: Context?) : FrameLayout(context!!) {
    /** The picture of this tab shown while its page loads again, and the drawable showing it ([ReloadCover]). */
    @JvmField
    var coverShot: ReloadCover.Shot? = null
    @JvmField
    var coverDrawable: android.graphics.drawable.Drawable? = null
    /** The picture while it fades out, over the page that has loaded. */
    @JvmField
    var coverFading: android.graphics.drawable.Drawable? = null

    /** Forgets the picture ([ReloadCover] keeps only the last few). */
    fun dropCover() {
        ReloadCover.hide(this, false)
        coverShot = null
    }

    @JvmField
    var swipeOffset: Float = 0f
    @JvmField
    var animState: Byte = 0
    @JvmField
    var tabYAnim: ValueAnimator? = null
    @JvmField
    var backShadeX: Float = 0f
    @JvmField
    var backAnim: ValueAnimator? = null
    @JvmField
    var keepVisible: Boolean = false
    @JvmField
    var drawingPages: Boolean = false
    @JvmField
    var shadeAlpha: Int = 0
    @JvmField
    var shadePaint: Paint? = null
    @JvmField
    var edgePaint: Paint? = null
    @JvmField
    var shadeColor: Int = 0
    @JvmField
    var edgeColor: Int = 0
    @JvmField
    var darkBars: Boolean = false
    @JvmField
    var backColorState: Int = 0
    @JvmField
    var addPageState: Boolean
    @JvmField
    var backdropBitmap: Bitmap? = null
    @JvmField
    var backdropPaint: Paint? = null
    @JvmField
    var backdropLandscape: Boolean = false
    @JvmField
    var executorCache: ExecutorService? = null
    @JvmField
    var webDestroy: MainWebDestroy? = null
    @JvmField
    var visibilityChanged: Boolean = false
    @JvmField
    var addAnimValue: Float = 0f
    @JvmField
    var addAnimRunning: Boolean = false
    @JvmField
    var addAnimEnd: Runnable?
    @JvmField
    var alive: Boolean = true
    @JvmField
    var tabXState: Byte = 0
    @JvmField
    var uiHandler: Handler?
    @JvmField
    var tabXForward: Boolean = false
    @JvmField
    var swipeReverse: Boolean
    @JvmField
    var fakeAnimValue: Float = 0f
    @JvmField
    var frameListener: WebFrameListener? = null
    @JvmField
    var fakeAnimRunning: Boolean = false
    @JvmField
    var tabUid: Long = 0
    @JvmField
    var fakeAnimEnd: Runnable?
    @JvmField
    var visibleOnce: Boolean = false
    @JvmField
    var swipeState: Byte = 0
    @JvmField
    var detached: Boolean = false
    @JvmField
    var swipeForward: Boolean = false
    @JvmField
    var destroyNotified: Boolean = false
    @JvmField
    var tabXAnimValue: Float = 0f
    @JvmField
    var backItem: WebTabItem? = null
    @JvmField
    var tabXAnimRunning: Boolean = false
    @JvmField
    var blankTimes: MutableList<Any?>? = null
    @JvmField
    var tabXAnimEnd: Runnable?
    @JvmField
    var pageItems: MutableList<PageItem?>? = null
    @JvmField
    var tabYState: Byte = 0
    @JvmField
    var currentPageIndex: Int = 0
    @JvmField
    var tabYAnimValue: Float = 0f
    @JvmField
    var addNeeded: Boolean = false
    @JvmField
    var tabYAnimRunning: Boolean = false
    @JvmField
    var addPending: Boolean = false
    @JvmField
    var tabYAnimEnd: Runnable?
    @JvmField
    var addHidden: Boolean = false
    @JvmField
    var backAnimValue: Int = 0
    @JvmField
    var inserting: Boolean = false
    @JvmField
    var backAnimRunning: Boolean = false
    @JvmField
    var touchBlocked: Boolean = false
    @JvmField
    var backAnimEnd: Runnable?
    @JvmField
    var addAnim: ValueAnimator? = null
    @JvmField
    var animFromUser: Boolean = false
    @JvmField
    var fakeX: Float = 0f
    @JvmField
    var tabAnimStart: Float = 0f
    @JvmField
    var tabXAnim: ValueAnimator? = null
    @JvmField
    var pageX: Float = 0f
    @JvmField
    var pageY: Float = 0f
    @JvmField
    var animStartX: Float = 0f
    @JvmField
    var animStartY: Float = 0f

    internal inner class PausePageTask(@JvmField val view: WebNestView) : Runnable {
        override fun run() {
            this.view.onPause()
        }
    }

    internal inner class AddAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.addAnim == null) {
                webNestFrame.setTranslationX(0.0f)
                return
            }
            val animatedValue = (valueAnimator.getAnimatedValue() as Float)
            if (webNestFrame.addAnim != null) {
                webNestFrame.addAnimValue = animatedValue
                if (webNestFrame.isDrawingPages() || webNestFrame.addAnimRunning) {
                    return
                }
                webNestFrame.addAnimRunning = true
                MainApp.postToMain(webNestFrame.getContext(), webNestFrame.addAnimEnd!!)
            }
        }
    }

    internal inner class NotifySwipeEndAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.addAnim == null) {
                return
            }
            webNestFrame.addAnim = null
            webNestFrame.setTranslationX(0.0f)
            webNestFrame.notifySwipeEnd(1, false)
        }

        override fun onAnimationEnd(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.addAnim == null) {
                return
            }
            MainApp.postToMain(webNestFrame.getContext(), webNestFrame.AddAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class ShowFrameTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            webNestFrame.showFrame()
            val valueAnimator = webNestFrame.addAnim
            if (valueAnimator != null) {
                valueAnimator.start()
            } else {
                webNestFrame.setTranslationX(0.0f)
                webNestFrame.notifySwipeEnd(1, false)
            }
        }
    }

    internal inner class SetValAnimAddTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            webNestFrame.addAnimRunning = false
            if (webNestFrame.addAnim == null) {
                return
            }
            webNestFrame.setValAnimAdd(webNestFrame.addAnimValue)
        }
    }

    internal inner class AddAnimEndTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.addAnim == null) {
                return
            }
            webNestFrame.addAnim = null
            webNestFrame.setTranslationX(0.0f)
            webNestFrame.notifySwipeEnd(1, false)
        }
    }

    internal inner class TabXAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabXAnim != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (webNestFrame.tabXAnim != null) {
                    webNestFrame.fakeAnimValue = animatedValue
                    if (webNestFrame.isDrawingPages() || webNestFrame.fakeAnimRunning) {
                        return
                    }
                    webNestFrame.fakeAnimRunning = true
                    MainApp.postToMain(webNestFrame.getContext(), webNestFrame.fakeAnimEnd!!)
                }
            }
        }
    }

    internal inner class NotifyTabAnimEndAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabXAnim == null) {
                return
            }
            webNestFrame.tabXAnim = null
            webNestFrame.animState = 0.toByte()
            webNestFrame.notifyTabAnimEnd(webNestFrame.tabXState.toInt(), webNestFrame.tabXForward)
        }

        override fun onAnimationEnd(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            webNestFrame.animState = 0.toByte()
            if (webNestFrame.tabXAnim == null) {
                return
            }
            MainApp.postToMain(webNestFrame.getContext(), webNestFrame.TabXAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimFakeTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            webNestFrame.fakeAnimRunning = false
            if (webNestFrame.tabXAnim == null) {
                return
            }
            webNestFrame.setValAnimFake(webNestFrame.fakeAnimValue)
        }
    }

    internal inner class TabXAnimEndTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabXAnim == null) {
                return
            }
            webNestFrame.tabXAnim = null
            val tabAnimStart = webNestFrame.tabAnimStart
            webNestFrame.fakeX = tabAnimStart
            webNestFrame.drawShade(tabAnimStart, 0)
            webNestFrame.invalidate()
            webNestFrame.notifyTabAnimEnd(webNestFrame.tabXState.toInt(), webNestFrame.tabXForward)
        }
    }

    internal inner class PageSwipeUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabYAnim != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (webNestFrame.tabYAnim != null) {
                    webNestFrame.tabXAnimValue = animatedValue
                    if (webNestFrame.isDrawingPages() || webNestFrame.tabXAnimRunning) {
                        return
                    }
                    webNestFrame.tabXAnimRunning = true
                    MainApp.postToMain(webNestFrame.getContext(), webNestFrame.tabXAnimEnd!!)
                }
            }
        }
    }

    internal inner class ForceRelayoutTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.alive) {
                if (webNestFrame.getPaddingBottom() == 0) {
                    webNestFrame.setPadding(0, 0, 0, 1)
                } else {
                    webNestFrame.setPadding(0, 0, 0, 0)
                }
                webNestFrame.visibilityChanged = false
            }
        }
    }

    internal inner class TabYAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabYAnim == null) {
                return
            }
            webNestFrame.tabYAnim = null
            webNestFrame.animState = 0.toByte()
            webNestFrame.notifySwipeEnd(webNestFrame.swipeState.toInt(), webNestFrame.swipeForward)
        }

        override fun onAnimationEnd(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            webNestFrame.animState = 0.toByte()
            if (webNestFrame.tabYAnim == null) {
                return
            }
            MainApp.postToMain(webNestFrame.getContext(), webNestFrame.PageSwipeEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class PageSwipeStepTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            webNestFrame.tabXAnimRunning = false
            if (webNestFrame.tabYAnim == null) {
                return
            }
            webNestFrame.setValAnimTabX(webNestFrame.tabXAnimValue)
        }
    }

    internal inner class PageSwipeEndTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabYAnim == null) {
                return
            }
            webNestFrame.tabYAnim = null
            val animStartX = webNestFrame.animStartX
            webNestFrame.pageX = animStartX
            webNestFrame.drawShade(animStartX, 1)
            webNestFrame.invalidate()
            webNestFrame.notifySwipeEnd(webNestFrame.swipeState.toInt(), webNestFrame.swipeForward)
        }
    }

    internal inner class VerticalSwipeUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabYAnim != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (webNestFrame.tabYAnim != null) {
                    webNestFrame.tabYAnimValue = animatedValue
                    if (webNestFrame.isDrawingPages() || webNestFrame.tabYAnimRunning) {
                        return
                    }
                    webNestFrame.tabYAnimRunning = true
                    MainApp.postToMain(webNestFrame.getContext(), webNestFrame.tabYAnimEnd!!)
                }
            }
        }
    }

    internal inner class VerticalSwipeListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabYAnim == null) {
                return
            }
            webNestFrame.tabYAnim = null
            webNestFrame.animState = 0.toByte()
            webNestFrame.notifySwipeEnd(webNestFrame.tabYState.toInt(), false)
        }

        override fun onAnimationEnd(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            webNestFrame.animState = 0.toByte()
            if (webNestFrame.tabYAnim == null) {
                return
            }
            MainApp.postToMain(webNestFrame.getContext(), webNestFrame.VerticalSwipeEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimTabYTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            webNestFrame.tabYAnimRunning = false
            if (webNestFrame.tabYAnim == null) {
                return
            }
            webNestFrame.setValAnimTabY(webNestFrame.tabYAnimValue)
        }
    }

    internal inner class VerticalSwipeEndTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.tabYAnim == null) {
                return
            }
            webNestFrame.tabYAnim = null
            val animStartY = webNestFrame.animStartY
            webNestFrame.pageY = animStartY
            webNestFrame.drawShade(animStartY, 2)
            webNestFrame.invalidate()
            webNestFrame.notifySwipeEnd(webNestFrame.tabYState.toInt(), false)
        }
    }

    internal inner class BackAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.backAnim != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Int)
                if (webNestFrame.backAnim != null) {
                    webNestFrame.backAnimValue = animatedValue
                    if (webNestFrame.isDrawingPages() || webNestFrame.backAnimRunning) {
                        return
                    }
                    webNestFrame.backAnimRunning = true
                    MainApp.postToMain(webNestFrame.getContext(), webNestFrame.backAnimEnd!!)
                }
            }
        }
    }

    internal inner class DrawShadeAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.backAnim == null) {
                return
            }
            webNestFrame.backAnim = null
            webNestFrame.backShadeX = 0.0f
            webNestFrame.drawShade(0.0f, 3)
        }

        override fun onAnimationEnd(animator: Animator) {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.backAnim == null) {
                return
            }
            MainApp.postToMain(webNestFrame.getContext(), webNestFrame.BackAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimBackTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            webNestFrame.backAnimRunning = false
            if (webNestFrame.backAnim == null) {
                return
            }
            webNestFrame.setValAnimBack(webNestFrame.backAnimValue)
        }
    }

    internal inner class MarkBlankOldTask(@JvmField val outer: WebNestFrame,
                                         @JvmField val blankTime: Long
    ) : Runnable {
        override fun run() {
            this.outer.setBlankOld(this.blankTime)
        }
    }

    internal inner class BackAnimEndTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.backAnim == null) {
                return
            }
            webNestFrame.backAnim = null
            webNestFrame.backShadeX = 0.0f
            webNestFrame.drawShade(0.0f, 3)
            webNestFrame.invalidate()
        }
    }

    internal inner class BackupBitmapCopyTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            webNestFrame.backdropBitmap = MainUtil.backupBitmapCopy(webNestFrame.getContext(), webNestFrame.backdropLandscape)
        }
    }

    internal inner class ClearBackdropTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            if (webNestFrame.keepVisible) {
                webNestFrame.keepVisible = false
                webNestFrame.clearBackdrop()
                webNestFrame.invalidate()
            }
        }
    }

    internal inner class CollectDroppedPagesTask(@JvmField val outer: WebNestFrame,
                                         @JvmField val pageIndex: Int
    ) : Runnable {
        override fun run() {
            val webNestFrame = this.outer
            try {
                val currentPageIndex = this.pageIndex + 1
                var arrayListO = webNestFrame.pageUrlsFrom(currentPageIndex)
                val arrayListF = webNestFrame.saveState()
                if (arrayListO == null) {
                    arrayListO = arrayListF
                } else if (arrayListF != null) {
                    arrayListO.addAll(arrayListF)
                }
                webNestFrame.setDelList(arrayListO)
            } catch (unused: Exception) {
            }
            webNestFrame.inserting = false
        }
    }

    internal inner class SetDetachedTask(@JvmField val outer: WebNestFrame,
                                         @JvmField val items: MutableList<PageItem?>?
    ) : Runnable {
        override fun run() {
            if (this.items == null) {
                return
            }
            try {
                var detached: ArrayList<WebView?>? = null
                for (pageItem2 in this.items) {
                    val pageItem = pageItem2
                    val webNestView: WebNestView? = if (pageItem != null) pageItem.webNestView else null
                    if (pageItem != null && webNestView != null) {
                        webNestView.setDetached(true)
                        if (detached == null) {
                            detached = ArrayList()
                        }
                        detached.add(webNestView)
                    }
                }
                this.outer.setDelList(detached)
            } catch (unused: Exception) {
            }
        }
    }

    internal inner class FrameAnimEndTask(@JvmField val outer: WebNestFrame, byteValueParam: Int,
                                         @JvmField val forward: Boolean
    ) : Runnable {
        val byteValue: Byte

        init {
            this.byteValue = (byteValueParam).toByte()
        }

        override fun run() {
            val webNestFrame = this.outer
            val webFrameListener = webNestFrame.frameListener
            if (webFrameListener != null) {
                webFrameListener.onFrameAnimEnd(this.byteValue.toInt(), this.forward, webNestFrame.animFromUser)
            }
        }
    }

    internal inner class NotifyFrameAnimTask(@JvmField val outer: WebNestFrame, byteValueParam: Int,
                                         @JvmField val forward: Boolean
    ) : Runnable {
        val byteValue: Byte

        init {
            this.byteValue = (byteValueParam).toByte()
        }

        override fun run() {
            val webFrameListener = this.outer.frameListener
            if (webFrameListener != null) {
                webFrameListener.onFrameAnim(this.byteValue.toInt(), this.forward)
            }
        }
    }

    internal inner class RestartActivityTask : Runnable {
        override fun run() {
            restartActivity(this@WebNestFrame.getContext(), null, false)
        }
    }

    internal inner class PageItemAtTask : Runnable {
        override fun run() {
            val webNestFrame = this@WebNestFrame
            val tabUid = webNestFrame.tabUid
            val pageItemX: PageItem? = if (tabUid > 0) webNestFrame.pageItemAt(webNestFrame.currentPageIndex) else null
            val webNestView: WebNestView? = if (tabUid > 0 && pageItemX != null) pageItemX.webNestView else null
            if (tabUid > 0 && pageItemX != null && webNestView != null) {
                saveState(
                    webNestFrame.getContext(), tabUid, webNestView.validPageUrl, null,
                    webNestFrame.listToStr
                )
            }
            webNestFrame.addHidden = false
        }
    }

    class PageItem {
        @JvmField
        var time: Long = 0
        @JvmField
        var blankPending: Boolean = false
        @JvmField
        var blankId: Long = 0
        @JvmField
        var pageUrl: String? = null
        @JvmField
        var url: String? = null
        @JvmField
        var deskMode: Boolean = false
        @JvmField
        var themeColor: Int = 0
        @JvmField
        var themeLight: Int = 0
        /** Where the page was scrolled when it was released; it scrolls back there when it loads again (2026-11-21). */
        @JvmField
        var scrollX: Int = 0
        @JvmField
        var scrollY: Int = 0
        @JvmField
        var webNestView: WebNestView? = null
    }

    interface WebFrameListener {
        fun onFrameAnim(direction: Int, forward: Boolean)

        fun onWebRemoved(webNestView: WebNestView?, pageIndex: Int)

        fun onFrameClosed(reason: Int, toPrevious: Boolean)

        fun onFrameShown()

        fun onFrameAnimEnd(direction: Int, forward: Boolean, fromUser: Boolean)
    }

    init {
        this.addAnimEnd = SetValAnimAddTask()
        this.fakeAnimEnd = SetValAnimFakeTask()
        this.tabXAnimEnd = PageSwipeStepTask()
        this.tabYAnimEnd = SetValAnimTabYTask()
        this.backAnimEnd = SetValAnimBackTask()
        this.uiHandler = Handler(Looper.getMainLooper())
        this.swipeReverse = isRtl(context)
        this.addPageState = ZtwoSettings.addPage.value
    }

    private val backColor: Int
        get() {
            if (MainUtil.isDarkBars(this.darkBars)) {
                return 16777216
            }
            return if (AppState.darkWeb) -16777216 else -1
        }

    private val blankTime: Long
        get() {
            val list: MutableList<PageItem?>? = if (ZtwoSettings.pageMemory.value < 3) null else this.pageItems
            if (ZtwoSettings.pageMemory.value < 3 || list == null || list.size <= ZtwoSettings.pageMemory.value) {
                return 0L
            }
            try {
                val times = this.blankTimes
                if (times != null && !times.isEmpty()) {
                    return (this.blankTimes!!.get(0) as Long)
                }
            } catch (unused: Exception) {
            }
            return 0L
        }

    private val wallColor: Int
        get() {
            if (MainUtil.isDarkBars(this.darkBars)) {
                return 0
            }
            return if (AppState.darkWeb) -16777216 else -1
        }

    fun setBlankOld(time: Long) {
        val list: MutableList<PageItem?>? = if (time == 0L) null else this.pageItems
        if (time == 0L || list == null) {
            return
        }
        try {
            val currentPageIndex = this.currentPageIndex
            val keepFrom = currentPageIndex - 2
            val keepTo = currentPageIndex + 2
            val size = list.size
            var arrayList: MutableList<WebView?>? = null
            for (index in 0..<size) {
                val pageItem: PageItem? = if ((index <= keepFrom || index >= keepTo)) (list.get(index)) else null
                val webNestView: WebNestView? = if ((index <= keepFrom || index >= keepTo) && pageItem != null && pageItem.time < time) pageItem.webNestView else null
                if ((index <= keepFrom || index >= keepTo) && pageItem != null && pageItem.time < time && webNestView != null) {
                    savePageItem(pageItem, webNestView)
                    webNestView.setDetached(true)
                    if (arrayList == null) {
                        arrayList = ArrayList<WebView?>()
                    }
                    arrayList.add(webNestView)
                }
            }
            setDelList(arrayList)
        } catch (unused: Exception) {
        }
    }

    /**
     * Memory pressure (web/WebViewActivityMemory): releases the pages more than one step behind or ahead of the shown
     * one (the address is kept, the page loads again when opened); returns how many were released.
     */
    fun releaseFarPages(): Int {
        val list = this.pageItems ?: return 0
        val current = this.currentPageIndex
        var views: MutableList<WebView?>? = null
        try {
            for (index in list.indices) {
                if (index >= current - 1 && index <= current + 1) continue
                val item = list[index] ?: continue
                val view = item.webNestView ?: continue
                savePageItem(item, view)
                view.setDetached(true)
                (views ?: ArrayList<WebView?>().also { views = it }).add(view)
            }
        } catch (_: Exception) {
        }
        setDelList(views)
        return views?.size ?: 0
    }

    private fun setBlankTime(newSize: Int) {
        val pageItemX = pageItemAt(newSize)
        if (pageItemX == null) {
            this.blankTimes = null
            return
        }
        try {
            if (ZtwoSettings.pageMemory.value < 3) {
                pageItemX.time = System.currentTimeMillis()
                this.blankTimes = null
                return
            }
            val list = this.blankTimes
            if (list == null) {
                this.blankTimes = ArrayList<Any?>()
            } else {
                val time = pageItemX.time
                if (time != 0L) {
                    list.remove(time)
                }
            }
            val now = System.currentTimeMillis()
            pageItemX.time = now
            this.blankTimes!!.add(now)
            val limit = min(ZtwoSettings.pageMemory.value, this.pageItems!!.size)
            val size = this.blankTimes!!.size
            if (size > limit) {
                this.blankTimes = this.blankTimes!!.subList(size - limit, size)
            }
        } catch (unused: Exception) {
        }
    }

    fun setDelList(list: MutableList<WebView?>?) {
        if (list == null || list.isEmpty()) {
            return
        }
        var mainWebDestroyV = this.webDestroy
        if (mainWebDestroyV == null) {
            mainWebDestroyV = MainApp.webDestroy(getContext())
            this.webDestroy = mainWebDestroyV
        }
        if (mainWebDestroyV != null) {
            mainWebDestroyV.addAll(list)
        }
    }

    private fun setDelView(webView: WebView?) {
        if (webView != null) {
            var mainWebDestroyV = this.webDestroy
            if (mainWebDestroyV == null) {
                mainWebDestroyV = MainApp.webDestroy(getContext())
                this.webDestroy = mainWebDestroyV
            }
            if (mainWebDestroyV != null) {
                mainWebDestroyV.add(webView)
            }
        }
    }

    fun setDetached(detached: Boolean) {
        if (this.detached != detached) {
            this.detached = detached
            val list = this.pageItems
            if (list == null || list.isEmpty()) {
                return
            }
            for (pageItem2 in list) {
                val pageItem = pageItem2
                val webNestView: WebNestView? = if (pageItem != null) pageItem.webNestView else null
                if (pageItem != null && webNestView != null) {
                    webNestView.setDetached(detached)
                }
            }
        }
    }

    fun setValAnimAdd(addAnimValue: Float) {
        if (isDrawingPages()) {
            return
        }
        setTranslationX(addAnimValue)
    }

    fun setValAnimBack(backAnimValue: Int) {
        val dp48Px = MainApp.dp48
        val fade = (if (backAnimValue < dp48Px) -backAnimValue else -((dp48Px shl 1) - backAnimValue)).toFloat()
        this.backShadeX = fade
        drawShade(fade, 3)
        if (isDrawingPages()) {
            return
        }
        invalidate()
    }

    fun setValAnimFake(fakeX: Float) {
        this.fakeX = fakeX
        drawShade(fakeX, 0)
        if (isDrawingPages()) {
            return
        }
        invalidate()
    }

    fun setValAnimTabX(pageX: Float) {
        this.pageX = pageX
        drawShade(pageX, 1)
        if (isDrawingPages()) {
            return
        }
        invalidate()
    }

    fun setValAnimTabY(pageY: Float) {
        this.pageY = pageY
        drawShade(pageY, 2)
        if (isDrawingPages()) {
            return
        }
        invalidate()
    }

    fun ensureShadePaints() {
        if (this.shadePaint == null) {
            val shadePaint = Paint()
            this.shadePaint = shadePaint
            shadePaint.setStyle(Paint.Style.FILL)
        }
        if (this.edgePaint == null) {
            val edgePaint = Paint()
            this.edgePaint = edgePaint
            edgePaint.setStyle(Paint.Style.FILL)
        }
    }

    fun releasePage(pageItem: PageItem?, webNestView: WebNestView?, keepCurrent: Boolean) {
        if (pageItem == null) {
            return
        }
        val page: WebNestView? = if (keepCurrent) pageItem.webNestView else null
        if (keepCurrent && page != null && (webNestView == null || webNestView != page)) {
            page.setDetached(true)
            setDelView(page)
        }
        pageItem.blankPending = false
        pageItem.blankId = 0L
        pageItem.pageUrl = null
        pageItem.url = null
        pageItem.deskMode = false
        pageItem.themeColor = 0
        pageItem.themeLight = 0
        pageItem.scrollX = 0
        pageItem.scrollY = 0
        pageItem.webNestView = webNestView
    }

    fun resetOffsets() {
        clearBackdrop()
        if (java.lang.Float.compare(this.fakeX, 0.0f) == 0 && java.lang.Float.compare(
                this.pageX,
                0.0f
            ) == 0 && java.lang.Float.compare(this.pageY, 0.0f) == 0 && java.lang.Float.compare(
                this.backShadeX,
                0.0f
            ) == 0
        ) {
            return
        }
        this.fakeX = 0.0f
        this.pageX = 0.0f
        this.pageY = 0.0f
        this.backShadeX = 0.0f
        this.shadeAlpha = 0
        invalidate()
    }

    fun setDarkBars(darkBars: Boolean) {
        this.darkBars = darkBars
        this.backColorState = this.backColor
    }

    fun isDrawingPages(): Boolean {
        if (this.drawingPages) {
            return true
        }
        val list: MutableList<PageItem?>? = this.pageItems
        if (list == null || list.isEmpty()) {
            return false
        }
        for (pageItem in list) {
            val webNestView: WebNestView? = if (pageItem != null) pageItem.webNestView else null
            if (pageItem != null && webNestView != null && (webNestView.inOnDraw || webNestView.inDispatchDraw)) {
                return true
            }
        }
        return false
    }

    fun saveState(): ArrayList<WebView?>? {
        val list: MutableList<PageItem?>? = if (ZtwoSettings.pageMemory.value >= 3) this.pageItems else null
        if (ZtwoSettings.pageMemory.value >= 3 && list != null) {
            try {
                val size = list.size
                if (size > 100) {
                    var index = this.currentPageIndex - 50
                    if (index < 0) {
                        index = 0
                    }
                    var key = index + 99
                    if (key >= size) {
                        key = size - 1
                        index = size - 100
                    }
                    val firstAfter = key + 1
                    var arrayList: ArrayList<WebView?>? = null
                    if (index > 0) {
                        for (index3 in 0..<index) {
                            val pageItem = list.get(index3)
                            val page: WebNestView? = if (pageItem != null) pageItem.webNestView else null
                            if (pageItem != null && page != null) {
                                page.setDetached(true)
                                if (arrayList == null) {
                                    arrayList = ArrayList<WebView?>()
                                }
                                arrayList.add(page)
                            }
                        }
                    }
                    if (firstAfter < size) {
                        for (index4 in firstAfter..<size) {
                            val item = list.get(index4)
                            val webNestView: WebNestView? = if (item != null) item.webNestView else null
                            if (item != null && webNestView != null) {
                                webNestView.setDetached(true)
                                if (arrayList == null) {
                                    arrayList = ArrayList<WebView?>()
                                }
                                arrayList.add(webNestView)
                            }
                        }
                    }
                    this.pageItems = list.subList(index, firstAfter)
                    this.currentPageIndex -= index
                    this.addPending = this.addNeeded
                    return arrayList
                }
            } catch (unused: Exception) {
            }
        }
        return null
    }

    fun clearBackdrop() {
        this.backdropBitmap = null
        this.backdropPaint = null
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

    fun startAddAnim() {
        if (this.addAnim == null) {
            var width = getWidth()
            if (width == 0) {
                val parent: Any? = getParent()
                if (parent != null) {
                    width = (parent as View).getWidth()
                }
                if (width == 0) {
                    showFrame()
                    setTranslationX(0.0f)
                    notifySwipeEnd(1, false)
                    return
                }
            }
            if (this.swipeReverse) {
                width = -width
            }
            val startX = width.toFloat()
            this.addAnimValue = startX
            this.addAnimRunning = false
            val valueAnimatorOfFloat = ValueAnimator.ofFloat(startX, 0.0f)
            this.addAnim = valueAnimatorOfFloat
            valueAnimatorOfFloat.setDuration(300L)
            this.addAnim!!.addUpdateListener(AddAnimUpdate())
            this.addAnim!!.addListener(NotifySwipeEndAnimListener())
            setTranslationX(startX)
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ShowFrameTask())
        }
    }

    fun showFrame() {
        this.touchBlocked = false
        if (getVisibility() == View.INVISIBLE) {
            this.keepVisible = true
            super.setVisibility(View.VISIBLE)
        }
    }

    fun removePagesOfTab(olderThan: Long, keepCurrent: Boolean) {
        val list: MutableList<PageItem?>? = if (olderThan == 0L) null else this.pageItems
        if (olderThan == 0L || list == null) {
            return
        }
        try {
            val size = list.size
            var arrayList: MutableList<WebView?>? = null
            for (index in 0..<size) {
                val pageItem: PageItem? = if ((!keepCurrent || index != this.currentPageIndex)) (list.get(index)) else null
                val webNestView: WebNestView? = if ((!keepCurrent || index != this.currentPageIndex) && pageItem != null && pageItem.time < olderThan) pageItem.webNestView else null
                if ((!keepCurrent || index != this.currentPageIndex) && pageItem != null && pageItem.time < olderThan && webNestView != null) {
                    savePageItem(pageItem, webNestView)
                    webNestView.setDetached(true)
                    if (arrayList == null) {
                        arrayList = ArrayList<WebView?>()
                    }
                    arrayList.add(webNestView)
                }
            }
            setDelList(arrayList)
        } catch (unused: Exception) {
        }
    }

    fun animateTabY(target: Int): Boolean {
        val parent: Any?
        if ((target == 2 || target == 3) && this.tabYAnim == null) {
            cancelAddAnim()
            var height = getHeight()
            if (height != 0 || ((getParent().also {
                    parent = it
                }) != null && ((parent as View).getHeight().also { height = it }) != 0)) {
                ensureShadePaints()
                val targetY = (if (target == 3) 0 else height).toFloat()
                if (java.lang.Float.compare(this.pageY, targetY) == 0) {
                    this.animState = 0.toByte()
                    notifySwipeEnd(target, false)
                    return false
                }
                this.animState = (target).toByte()
                this.animStartY = targetY
                this.tabYState = (target).toByte()
                val pageY = this.pageY
                this.tabYAnimValue = pageY
                this.tabYAnimRunning = false
                val valueAnimatorOfFloat = ValueAnimator.ofFloat(pageY, targetY)
                this.tabYAnim = valueAnimatorOfFloat
                valueAnimatorOfFloat.setDuration(((abs(targetY - this.pageY) / height) * 300.0f).toLong())
                this.tabYAnim!!.addUpdateListener(VerticalSwipeUpdate())
                this.tabYAnim!!.addListener(VerticalSwipeListener())
                this.tabYAnim!!.start()
                return true
            }
        }
        return false
    }

    fun animateTabX(target: Int, forward: Boolean): Boolean {
        val startX: Float
        val parent: Any?
        if (target != 0 && this.tabXAnim == null) {
            cancelAddAnim()
            var width = getWidth()
            if (width != 0 || ((getParent().also {
                    parent = it
                }) != null && ((parent as View).getWidth().also { width = it }) != 0)) {
                ensureShadePaints()
                val swipeReverse = this.swipeReverse
                if (target == 1) {
                    if (swipeReverse) {
                        startX = 0.0f
                        this.fakeX = 0.0f
                    } else {
                        startX = width.toFloat()
                        this.fakeX = startX
                    }
                    drawShade(startX, 0)
                }
                val targetX =
                    (if (if (!swipeReverse) !(if (!forward) target == 3 else target != 3) else !(if (!forward) target != 3 else target == 3)) 0 else width).toFloat()
                if (java.lang.Float.compare(this.fakeX, targetX) == 0) {
                    this.animState = 0.toByte()
                    notifyTabAnimEnd(target, forward)
                    return false
                }
                this.animState = (target).toByte()
                this.tabAnimStart = targetX
                this.tabXState = (target).toByte()
                this.tabXForward = forward
                val fakeX = this.fakeX
                this.fakeAnimValue = fakeX
                this.fakeAnimRunning = false
                val valueAnimatorOfFloat = ValueAnimator.ofFloat(fakeX, targetX)
                this.tabXAnim = valueAnimatorOfFloat
                valueAnimatorOfFloat.setDuration(((abs(targetX - this.fakeX) / width) * 300.0f).toLong())
                this.tabXAnim!!.addUpdateListener(TabXAnimUpdate())
                this.tabXAnim!!.addListener(NotifyTabAnimEndAnimListener())
                this.tabXAnim!!.start()
                return true
            }
        }
        return false
    }

    fun setSwipeOffset(pageX: Float, animFromUser: Boolean) {
        var pageX = pageX
        val shaded: Boolean
        this.animFromUser = animFromUser
        if (this.keepVisible) {
            shaded = false
        } else {
            shaded = true
            this.keepVisible = true
        }
        if (this.swipeReverse) {
            if (pageX <= 0.0f) {
                pageX = 0.1f
            } else if (pageX >= getWidth()) {
                pageX = getWidth() - 0.1f
            }
        }
        if (java.lang.Float.compare(this.fakeX, pageX) == 0) {
            if (shaded && MainUtil.isDarkBars(this.darkBars)) {
                invalidate()
                return
            }
            return
        }
        this.fakeX = pageX
        ensureShadePaints()
        drawShade(this.fakeX, 0)
        invalidate()
    }

    fun onPageDestroyed(webNestView: WebNestView?, swiped: Boolean) {
        if (swiped) {
            destroyWebView(webNestView, false)
            val webFrameListener = this.frameListener
            if (webFrameListener != null) {
                webFrameListener.onFrameShown()
                return
            } else {
                if (this.destroyNotified) {
                    return
                }
                this.destroyNotified = true
                runInBackground(RestartActivityTask())
                return
            }
        }
        val list: MutableList<PageItem?>? = this.pageItems
        var pageItem: PageItem? = null
        if (list != null && !list.isEmpty()) {
            for (pageItem2 in list) {
                val page: WebNestView? = if (pageItem2 != null) pageItem2.webNestView else null
                if (pageItem2 != null && page != null && page == webNestView) {
                    pageItem = pageItem2
                    break
                }
            }
        }
        savePageItem(pageItem, webNestView)
        val childIndex = indexOfChild(webNestView)
        destroyWebView(webNestView, false)
        val frameListener = this.frameListener
        if (frameListener != null) {
            frameListener.onWebRemoved(webNestView, childIndex)
        }
    }

    fun drawShade(fakeX: Float, direction: Int) {
        var fakeX = fakeX
        var direction = direction
        if (this.shadePaint != null && this.edgePaint != null) {
            var extent: Int
            if (direction == 2) {
                extent = this.getHeight()
            } else {
                extent = this.getWidth()
            }

            if (extent == 0) {
                this.shadeAlpha = 0
                return
            }

            var shade: Float = 0f
            run label39@{
                run label38@{
                    if (direction != 2) {
                        if (direction == 3) {
                            val edgeX = extent.toFloat()
                            shade = abs(fakeX)
                            fakeX = edgeX
                            return@label38
                        }

                        shade = fakeX
                        if (this.swipeReverse) {
                            return@label39
                        }
                    }
                    val edgeX = extent.toFloat()
                    shade = fakeX
                    fakeX = edgeX
                }
                shade = fakeX - shade
            }

            direction = (shade / extent * 161.0f).toInt()
            this.shadeAlpha = direction
            if (direction != 0) {
                direction = this.wallColor
                extent = this.shadeAlpha shl 24
                if (this.shadeColor != direction) {
                    this.shadeColor = direction
                    if (direction != 0) {
                        this.shadePaint!!.setColor(direction)
                    }
                }

                if (this.edgeColor != extent) {
                    this.edgeColor = extent
                    this.edgePaint!!.setColor(extent)
                }
            }
        }
    }

    fun restorePage(webNestView: WebNestView?, arrayList: ArrayList<PageItem?>?) {
        val pageItemX: PageItem? = if (arrayList!!.isEmpty()) null else pageItemAt(this.currentPageIndex)
        if (arrayList.isEmpty() || pageItemX == null || (webNestView != pageItemX.webNestView)) {
            return
        }
        for (size in arrayList.size - 1 downTo -1 + 1) {
            val pageItem = arrayList.get(size)
            if (pageItem != null && "sb_curr_page" == pageItem.url) {
                releasePage(pageItem, webNestView, true)
                this.pageItems = arrayList
                this.currentPageIndex = size
                this.addNeeded = true
                setBlankTime(size)
                return
            }
        }
    }

    public override fun dispatchDraw(canvas: Canvas) {
        if (!this.alive || this.detached) {
            return
        }
        // the page loads again under the picture of how it looked ([ReloadCover]); it shows once it has loaded
        val cover = this.coverDrawable ?: this.coverFading
        if (cover != null) {
            // while it fades out, the page shows through
            if (this.coverDrawable == null) super.dispatchDraw(canvas)
            cover.draw(canvas)
            return
        }
        this.drawingPages = true
        if (this.shadePaint == null || this.edgePaint == null) {
            val backColorState = this.backColorState
            if (backColorState != 0) {
                canvas.drawColor(backColorState)
            }
            if (this.keepVisible) {
                drawBackdrop(canvas)
            } else {
                clearBackdrop()
            }
            super.dispatchDraw(canvas)
            this.drawingPages = false
            return
        }
        val fakeX = this.fakeX
        val swipeReverse = this.swipeReverse
        if (fakeX > 0.0f) {
            val stateColor = this.backColorState
            if (stateColor != 0) {
                canvas.drawColor(stateColor)
            }
            super.dispatchDraw(canvas)
            if (this.shadeAlpha == 0) {
                this.drawingPages = false
                return
            }
            val width = getWidth()
            if (width != 0) {
                val right = width.toFloat()
                if (this.fakeX < right) {
                    val height = getHeight()
                    val edgeX = this.fakeX
                    val paint = this.edgePaint
                    if (swipeReverse) {
                        val bottom = height.toFloat()
                        canvas.drawRect(edgeX, 0.0f, right, bottom, paint!!)
                        if (this.shadeColor != 0) {
                            canvas.drawRect(0.0f, 0.0f, this.fakeX, bottom, this.shadePaint!!)
                        }
                    } else {
                        val fullBottom = height.toFloat()
                        canvas.drawRect(0.0f, 0.0f, edgeX, fullBottom, paint!!)
                        if (this.shadeColor != 0) {
                            canvas.drawRect(this.fakeX, 0.0f, right, fullBottom, this.shadePaint!!)
                        }
                    }
                    drawBackdrop(canvas)
                    this.drawingPages = false
                    return
                }
            }
            this.drawingPages = false
            return
        }
        if (this.pageX > 0.0f) {
            if (this.animState.toInt() == 1) {
                val stateColor = this.backColorState
                if (stateColor != 0) {
                    canvas.drawColor(stateColor)
                }
                super.dispatchDraw(canvas)
                if (this.shadeAlpha == 0) {
                    this.drawingPages = false
                    return
                }
                val pageWidth = getWidth()
                if (pageWidth != 0) {
                    val pageRight = pageWidth.toFloat()
                    if (this.pageX < pageRight) {
                        val pageHeight = getHeight()
                        val pageX = this.pageX
                        val edgePaint = this.edgePaint
                        if (swipeReverse) {
                            val pageBottom = pageHeight.toFloat()
                            canvas.drawRect(pageX, 0.0f, pageRight, pageBottom, edgePaint!!)
                            if (this.shadeColor != 0) {
                                canvas.drawRect(0.0f, 0.0f, this.pageX, pageBottom, this.shadePaint!!)
                            }
                        } else {
                            val pageBottom = pageHeight.toFloat()
                            canvas.drawRect(0.0f, 0.0f, pageX, pageBottom, edgePaint!!)
                            if (this.shadeColor != 0) {
                                canvas.drawRect(this.pageX, 0.0f, pageRight, pageBottom, this.shadePaint!!)
                            }
                        }
                        drawBackdrop(canvas)
                        this.drawingPages = false
                        return
                    }
                }
                this.drawingPages = false
                return
            }
            if (this.shadeAlpha == 0) {
                this.drawingPages = false
                return
            }
            val pageWidth = getWidth()
            if (pageWidth != 0) {
                val pageRight = pageWidth.toFloat()
                if (this.pageX < pageRight) {
                    val pageHeight = getHeight()
                    val shadeColor = this.shadeColor
                    if (swipeReverse) {
                        if (shadeColor != 0) {
                            canvas.drawRect(0.0f, 0.0f, this.pageX, pageHeight.toFloat(), this.shadePaint!!)
                        }
                        drawBackdrop(canvas)
                        val dx = this.pageX - pageRight
                        canvas.translate(dx, 0.0f)
                        super.dispatchDraw(canvas)
                        canvas.translate(-dx, 0.0f)
                        canvas.drawRect(this.pageX, 0.0f, pageRight, pageHeight.toFloat(), this.edgePaint!!)
                    } else {
                        if (shadeColor != 0) {
                            canvas.drawRect(this.pageX, 0.0f, pageRight, pageHeight.toFloat(), this.shadePaint!!)
                        }
                        drawBackdrop(canvas)
                        val shiftX = this.pageX
                        canvas.translate(shiftX, 0.0f)
                        super.dispatchDraw(canvas)
                        canvas.translate(-shiftX, 0.0f)
                        canvas.drawRect(0.0f, 0.0f, this.pageX, pageHeight.toFloat(), this.edgePaint!!)
                    }
                }
            }
            this.drawingPages = false
            return
        }
        if (this.pageY > 0.0f) {
            if (this.shadeAlpha == 0) {
                this.drawingPages = false
                return
            }
            val frameHeight = getHeight()
            if (frameHeight != 0) {
                val frameBottom = frameHeight.toFloat()
                if (this.pageY < frameBottom) {
                    val frameWidth = getWidth()
                    if (this.shadeColor != 0) {
                        canvas.drawRect(0.0f, this.pageY, frameWidth.toFloat(), frameBottom, this.shadePaint!!)
                    }
                    drawBackdrop(canvas)
                    val pageY = this.pageY
                    canvas.translate(0.0f, pageY)
                    super.dispatchDraw(canvas)
                    canvas.translate(0.0f, -pageY)
                    canvas.drawRect(0.0f, 0.0f, frameWidth.toFloat(), this.pageY, this.edgePaint!!)
                    this.drawingPages = false
                    return
                }
            }
            this.drawingPages = false
            return
        }
        val backShadeX = this.backShadeX
        if (backShadeX <= 0.0f && backShadeX >= 0.0f) {
            val stateColor = this.backColorState
            if (stateColor != 0) {
                canvas.drawColor(stateColor)
            }
            if (this.keepVisible) {
                drawBackdrop(canvas)
            } else {
                clearBackdrop()
            }
            super.dispatchDraw(canvas)
            this.drawingPages = false
            return
        }
        if (this.shadeAlpha == 0) {
            this.drawingPages = false
            return
        }
        val fadeWidth = getWidth()
        if (fadeWidth != 0) {
            val fade = this.backShadeX
            val fadeRight = fadeWidth.toFloat()
            if (fade < fadeRight && fade > (-fadeWidth)) {
                drawBackdrop(canvas)
                var shift = this.backShadeX
                if (swipeReverse) {
                    shift = -shift
                }
                canvas.translate(shift, 0.0f)
                val fadeShade = this.shadeColor
                if (fadeShade != 0) {
                    canvas.drawColor(fadeShade)
                }
                super.dispatchDraw(canvas)
                canvas.translate(-shift, 0.0f)
                if (shift > 0.0f) {
                    canvas.drawRect(0.0f, 0.0f, shift, getHeight().toFloat(), this.edgePaint!!)
                } else {
                    canvas.drawRect(shift + fadeRight, 0.0f, fadeRight, getHeight().toFloat(), this.edgePaint!!)
                }
                this.drawingPages = false
                return
            }
        }
        this.drawingPages = false
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        // a touch on the reload picture: show the real page at once
        if (this.coverDrawable != null && event?.actionMasked == MotionEvent.ACTION_DOWN) ReloadCover.hide(this, false)
        if (this.keepVisible && event!!.getActionMasked() == 0) {
            this.keepVisible = false
            clearBackdrop()
            if (MainUtil.isDarkBars(this.darkBars)) {
                invalidate()
            }
        }
        if (this.touchBlocked || this.addAnim != null || this.fakeX > 0.0f || this.pageX > 0.0f || this.pageY > 0.0f) {
            return true
        }
        val backShadeX = this.backShadeX
        if (backShadeX > 0.0f || backShadeX < 0.0f) {
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        if (!this.alive || this.detached) {
            return
        }
        super.draw(canvas)
    }

    val listToStr: String?
        get() {
            var validPageUrl: String?
            var str: String?
            if (ZtwoSettings.addPage.value) {
                try {
                    val list = this.pageItems
                    val size = if (list != null) list.size else 0
                    val loadEnd = if (list != null && size != 0) min(
                            this.currentPageIndex + 3,
                            size
                        ) else 0
                    if (list != null && size != 0 && loadEnd > 0) {
                        var i = loadEnd - 10
                        var i2 = 0
                        if (i < 0) {
                            i = 0
                        }
                        var sb: StringBuilder? = null
                        var z = false
                        while (true) {
                            var z2 = true
                            if (i >= loadEnd) {
                                break
                            }
                            val pageItem = list.get(i)
                            if (pageItem != null) {
                                val webNestView = pageItem.webNestView
                                if (i == this.currentPageIndex) {
                                    str = "sb_curr_page"
                                } else {
                                    if (webNestView != null) {
                                        validPageUrl = webNestView.validPageUrl
                                    } else {
                                        validPageUrl = if (pageItem.blankPending) pageItem.pageUrl else pageItem.url
                                    }
                                    val pageUrlText = validPageUrl
                                    z2 = z
                                    str = pageUrlText
                                }
                                if (!TextUtils.isEmpty(str)) {
                                    if (sb == null) {
                                        sb = StringBuilder()
                                    } else {
                                        sb.append("!@!")
                                    }
                                    sb.append(str)
                                    i2++
                                }
                                z = z2
                            }
                            i++
                        }
                        if (sb != null && (i2 != 1 || !z)) {
                            return sb.toString()
                        }
                    }
                } catch (unused: Exception) {
                }
            }
            return null
        }

    val pageCreate: WebNestView?
        get() {
            val i = this.currentPageIndex
            val pageItemX = pageItemAt(i)
            var webNestView2: WebNestView? = null
            val webNestView: WebNestView? = if (pageItemX != null) pageItemX.webNestView else null
            if (pageItemX != null && webNestView != null) {
                if (webNestView.getParent() == null) {
                    attachPage(webNestView, childIndexFor(i))
                }
                webNestView2 = webNestView
            }
            if (webNestView2 != null) return webNestView2
            // the page was released: it loads again, under the picture of how it looked
            // read before the page is created: creating it clears the saved address
            val coverUrl = if (pageItemX == null) null else if (pageItemX.blankPending) pageItemX.pageUrl else pageItemX.url
            val coverScroll = pageItemX?.scrollY ?: 0
            val created = createPageAt(i)
            if (created != null) ReloadCover.show(this, coverUrl, coverScroll)
            return created
        }

    var pageIndex: Int
        get() = this.currentPageIndex
        set(i) {
            if (this.currentPageIndex == i) {
                return
            }
            this.currentPageIndex = i
            this.addPending = this.addNeeded
            setBlankTime(i)
        }

    val pageValid: WebNestView?
        get() {
            val i = this.currentPageIndex
            val pageItemX = pageItemAt(i)
            val webNestView: WebNestView? = if (pageItemX == null) null else pageItemX.webNestView
            if (pageItemX == null || webNestView == null) {
                return null
            }
            if (webNestView.getParent() == null) {
                attachPage(webNestView, childIndexFor(i))
            }
            return webNestView
        }

    var tabX: Float
        get() = this.pageX
        set(f) {
            var f = f
            val z: Boolean
            if (this.keepVisible) {
                z = false
            } else {
                this.keepVisible = true
                z = true
            }
            if (this.swipeReverse) {
                if (f <= 0.0f) {
                    f = 0.1f
                } else if (f >= getWidth()) {
                    f = getWidth() - 0.1f
                }
            }
            if (java.lang.Float.compare(this.pageX, f) == 0) {
                if (z && MainUtil.isDarkBars(this.darkBars)) {
                    invalidate()
                    return
                }
                return
            }
            this.pageX = f
            ensureShadePaints()
            drawShade(this.pageX, 1)
            invalidate()
        }

    var tabY: Float
        get() = this.pageY
        set(f) {
            val z: Boolean
            if (this.keepVisible) {
                z = false
            } else {
                z = true
                this.keepVisible = true
            }
            if (java.lang.Float.compare(this.pageY, f) == 0) {
                if (z && MainUtil.isDarkBars(this.darkBars)) {
                    invalidate()
                    return
                }
                return
            }
            this.pageY = f
            ensureShadePaints()
            drawShade(this.pageY, 2)
            invalidate()
        }

    fun attachPage(view: View?, pageFrameListenerValue: Int) {
        var pageFrameListenerValue = pageFrameListenerValue
        if (view != null) {
            try {
                detachFromParent(view)
                if (pageFrameListenerValue < 0) {
                    pageFrameListenerValue = 0
                } else if (pageFrameListenerValue > getChildCount()) {
                    pageFrameListenerValue = getChildCount()
                }
                addViewInLayout(view, pageFrameListenerValue, LayoutParams(-1, -1), true)
            } catch (unused: Exception) {
            }
        }
    }

    fun addPage(webNestView: WebNestView?, currentPageIndex: Int, childIndex: Int) {
        if (webNestView == null || this.inserting) {
            return
        }
        this.inserting = true
        attachPage(webNestView, childIndex)
        var arrayList = this.pageItems
        if (arrayList == null) {
            arrayList = ArrayList()
        }
        var pageItemX = pageItemAt(currentPageIndex)
        if (pageItemX == null) {
            pageItemX = PageItem()
            arrayList.add(pageItemX)
        }
        releasePage(pageItemX, webNestView, true)
        this.pageItems = arrayList
        this.currentPageIndex = currentPageIndex
        val size = arrayList.size
        if (size > 1) {
            this.addNeeded = true
        }
        this.addPending = this.addNeeded
        setBlankTime(this.currentPageIndex)
        if (size < 2) {
            this.inserting = false
            return
        }
        val handler = this.uiHandler
        if (handler == null) {
            this.inserting = false
        } else {
            handler.post(CollectDroppedPagesTask(this, currentPageIndex))
        }
    }

    fun cancelAddAnim() {
        val valueAnimator = this.addAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.addAnim = null
            showFrame()
            setTranslationX(0.0f)
        }
        this.touchBlocked = false
    }

    fun cancelAnims() {
        cancelAddAnim()
        val valueAnimator = this.tabXAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.tabXAnim = null
        }
        val tabYAnim = this.tabYAnim
        if (tabYAnim != null) {
            tabYAnim.cancel()
            this.tabYAnim = null
        }
        val backAnim = this.backAnim
        if (backAnim != null) {
            backAnim.cancel()
            this.backAnim = null
        }
        this.animState = 0.toByte()
    }

    fun postBlankTime() {
        val blankTime = this.blankTime
        val handler: Handler? = if (blankTime == 0L) null else this.uiHandler
        if (blankTime == 0L || handler == null) {
            return
        }
        handler.post(MarkBlankOldTask(this, blankTime))
    }

    fun trimBefore() {
        val list = this.pageItems
        var size = if (list == null) 0 else list.size
        if (list == null || size == 0) {
            return
        }
        val currentPageIndex = this.currentPageIndex
        var index = currentPageIndex - 4
        val windowEnd = currentPageIndex + 4
        if (index < 0) {
            index = 0
        }
        if (windowEnd <= size) {
            size = windowEnd
        }
        val keepFrom = currentPageIndex - 1
        val keepTo = currentPageIndex + 1
        while (index < size) {
            val pageItem = list.get(index)
            val webNestView: WebNestView? = if (pageItem != null) pageItem.webNestView else null
            if (pageItem != null && webNestView != null) {
                if (index == this.currentPageIndex) {
                    if (webNestView.getVisibility() != View.VISIBLE) {
                        webNestView.setVisibility(View.VISIBLE)
                    }
                    webNestView.onResume()
                } else {
                    val visibility = webNestView.getVisibility() != View.GONE
                    if (!visibility && (index < keepFrom || index > keepTo)) {
                        webNestView.stopIfLoading()
                    }
                    if (visibility) {
                        webNestView.setVisibility(View.GONE)
                    }
                    webNestView.onPause()
                }
            }
            index++
        }
    }

    fun trimAfter(visibility: Boolean) {
        val list = this.pageItems
        var size = if (list == null) 0 else list.size
        if (list == null || size == 0) {
            return
        }
        val currentPageIndex = this.currentPageIndex
        var index = currentPageIndex - 1
        val windowEnd = currentPageIndex + 2
        if (index < 0) {
            index = 0
        }
        if (windowEnd <= size) {
            size = windowEnd
        }
        while (index < size) {
            val pageItem: PageItem? = if ((!visibility || index != this.currentPageIndex)) (list.get(index)) else null
            val webNestView: WebNestView? = if ((!visibility || index != this.currentPageIndex) && pageItem != null) pageItem.webNestView else null
            if ((!visibility || index != this.currentPageIndex) && pageItem != null && webNestView != null) {
                webNestView.stopIfLoading()
            }
            index++
        }
    }

    fun pageUrlsFrom(startIndex: Int): ArrayList<WebView?>? {
        var startIndex = startIndex
        val list = this.pageItems
        var arrayList: ArrayList<WebView?>? = null
        val size = if (list == null) 0 else list.size
        if (list == null || size <= startIndex) {
            return null
        }
        this.pageItems = list.subList(0, startIndex)
        if (this.currentPageIndex >= startIndex) {
            this.currentPageIndex = startIndex - 1
        }
        this.addPending = this.addNeeded
        while (startIndex < size) {
            val pageItem = list.get(startIndex)
            val webNestView: WebNestView? = if (pageItem != null) pageItem.webNestView else null
            if (pageItem != null && webNestView != null) {
                webNestView.setDetached(true)
                if (arrayList == null) {
                    arrayList = ArrayList<WebView?>()
                }
                arrayList.add(webNestView)
            }
            startIndex++
        }
        return arrayList
    }

    public override fun onAttachedToWindow() {
        setDetached(false)
        super.onAttachedToWindow()
    }

    public override fun onDetachedFromWindow() {
        cancelAnims()
        setDetached(true)
        super.onDetachedFromWindow()
    }

    public override fun onDraw(canvas: Canvas) {
        if (!this.alive || this.detached) {
            return
        }
        super.onDraw(canvas)
    }

    fun drawBackdrop(canvas: Canvas?) {
        var shiftY: Float
        val width: Int
        if (!MainUtil.isDarkBars(this.darkBars) || canvas == null) {
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
        val parent: Any? = getParent()
        if (parent != null) {
            var fakeX = this.fakeX
            val swipeReverse = this.swipeReverse
            if (fakeX > 0.0f) {
                if (swipeReverse) {
                    width = getWidth()
                    fakeX -= width.toFloat()
                }
                shiftY = 0.0f
            } else {
                fakeX = this.pageX
                if (fakeX <= 0.0f) {
                    val pageY = this.pageY
                    if (pageY > 0.0f) {
                        shiftY = pageY
                        fakeX = 0.0f
                    } else {
                        fakeX = this.backShadeX
                        if (fakeX <= 0.0f && fakeX >= 0.0f) {
                            fakeX = 0.0f
                            shiftY = 0.0f
                        } else if (swipeReverse) {
                            fakeX = -fakeX
                        }
                    }
                } else if (swipeReverse) {
                    width = getWidth()
                    fakeX -= width.toFloat()
                }
                shiftY = 0.0f
            }
            val scaleX = (screenWidth / backdropBitmap!!.getWidth()).toFloat()
            val height = (screenHeight / backdropBitmap.getHeight()).toFloat()
            canvas.save()
            if (fakeX > 0.0f || shiftY > 0.0f) {
                canvas.clipRect(
                    max(fakeX, 0.0f),
                    max(shiftY, 0.0f),
                    getWidth().toFloat(),
                    getHeight().toFloat()
                )
            }
            val webLand = TtsSettings.webLand.value
            if (webLand != 0 && screenWidth > screenHeight) {
                fakeX -= webLand.toFloat()
            }
            if (MainConst.isApi30) {
                fakeX -= MainActivity.leftInset.toFloat()
            }
            var translationY = shiftY - (parent as View).getTranslationY()
            if (BrowserSettings.showStatus.value) {
                translationY -= MainSettings.statusHeight.value.toFloat()
            }
            if (BrowserSettings.fixTop.value) {
                translationY -= topBarHeight().toFloat()
            }
            if (TtsSettings.handMode.value) {
                translationY -= WebViewActivity.Companion.Zn.toFloat()
            }
            canvas.translate(fakeX, translationY)
            canvas.scale(scaleX, height)
            try {
                canvas.drawBitmap(backdropBitmap, 0.0f, 0.0f, paint)
            } catch (unused: Exception) {
            }
            canvas.restore()
        }
    }

    fun notifySwipeEnd(direction: Int, swipeForward: Boolean) {
        val handler: Handler? = if (this.frameListener == null) null else this.uiHandler
        if (this.frameListener == null || handler == null) {
            return
        }
        handler.post(NotifyFrameAnimTask(this, direction, swipeForward))
    }

    fun notifyTabAnimEnd(reason: Int, tabXForward: Boolean) {
        val webFrameListener = this.frameListener
        if (webFrameListener != null) {
            if (this.animFromUser) {
                webFrameListener.onFrameClosed(reason, tabXForward)
            }
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(FrameAnimEndTask(this, reason, tabXForward))
        }
    }

    fun destroyFrame(webNestView: WebNestView?): Boolean {
        var webNestView = webNestView
        this.alive = false
        detachFromParent(this)
        val list: MutableList<PageItem?>? = this.pageItems
        clearHandler(this.uiHandler)
        this.uiHandler = null
        cancelAnims()
        clearBackdrop()
        this.shadePaint = null
        this.edgePaint = null
        this.backItem = null
        this.pageItems = null
        this.blankTimes = null
        this.frameListener = null
        this.executorCache = null
        if (list == null || list.isEmpty()) {
            this.webDestroy = null
            return false
        }
        var found = false
        var arrayList: MutableList<WebView?>? = null
        for (pageItem in list) {
            val page: WebNestView? = if (pageItem != null) pageItem.webNestView else null
            if (pageItem != null && page != null) {
                if (webNestView != null && webNestView == page) {
                    webNestView = null
                    found = true
                }
                releasePage(pageItem, null, false)
                page.setDetached(true)
                if (arrayList == null) {
                    arrayList = ArrayList<WebView?>()
                }
                arrayList.add(page)
            }
        }
        setDelList(arrayList)
        this.webDestroy = null
        return found
    }

    fun setAddNeed(touchBlocked: Boolean) {
        this.touchBlocked = touchBlocked
        if (touchBlocked) {
            super.setVisibility(View.INVISIBLE)
        }
    }

    fun setAddPage(addPageState: Boolean) {
        var index: Int = 0
        if (this.addPageState != addPageState) {
            this.addPageState = addPageState
            val list: MutableList<PageItem?>? = if (addPageState) null else this.pageItems
            val pageItemX: PageItem? = if (addPageState || list == null || list.size < 2) null else pageItemAt((this.currentPageIndex.also {
                    index = it
                }))
            if (addPageState || list == null || list.size < 2 || pageItemX == null) {
                return
            }
            val arrayList: ArrayList<PageItem?> = ArrayList()
            arrayList.add(pageItemX)
            this.pageItems = arrayList
            this.currentPageIndex = 0
            this.addPending = this.addNeeded
            setBlankTime(0)
            list.removeAt(index)
            if (list.isEmpty()) {
                return
            }
            removeAllViewsInLayout()
            attachPage(pageItemX.webNestView, 0)
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(SetDetachedTask(this, list))
        }
    }

    fun setBlankView(blank: Boolean) {
        val list: MutableList<PageItem?>? = this.pageItems
        if (list == null || list.isEmpty()) {
            return
        }
        var arrayList: MutableList<WebView?>? = null
        try {
            if (!blank) {
                if (getChildCount() != 0) {
                    for (pageItem in list) {
                        val webNestView: WebNestView? = if (pageItem != null) pageItem.webNestView else null
                        if (pageItem != null && webNestView != null) {
                            savePageItem(pageItem, webNestView)
                            webNestView.setDetached(true)
                            if (arrayList == null) {
                                arrayList = ArrayList<WebView?>()
                            }
                            arrayList.add(webNestView)
                        }
                    }
                    setDelList(arrayList)
                    return
                }
                return
            }
            if (getChildCount() >= 2) {
                val size = list.size
                for (index in 0..<size) {
                    val pageItem: PageItem? = if (index != this.currentPageIndex) list.get(index) else null
                    val page: WebNestView? = if (index != this.currentPageIndex && pageItem != null) pageItem.webNestView else null
                    if (index != this.currentPageIndex && pageItem != null && page != null) {
                        savePageItem(pageItem, page)
                        page.setDetached(true)
                        if (arrayList == null) {
                            arrayList = ArrayList<WebView?>()
                        }
                        arrayList.add(page)
                    }
                }
                setDelList(arrayList)
            }
        } catch (unused: Exception) {
        }
    }

    fun setDarkMode(darkBars: Boolean) {
        this.darkBars = darkBars
        val backColor = this.backColor
        if (this.backColorState == backColor) {
            return
        }
        this.backColorState = backColor
        invalidate()
    }

    fun setListener(webFrameListener: WebFrameListener?) {
        this.frameListener = webFrameListener
    }

    fun setTabUid(tabUid: Long) {
        this.tabUid = tabUid
    }

    fun setValid(alive: Boolean) {
        this.alive = alive
    }

    override fun setVisibility(visibility: Int) {
        if (visibility != 0 && this.isVisible) {
            // the tab is left: keep how it looks, for when its page has to load again
            ReloadCover.hide(this, false)
            ReloadCover.take(this)
        }
        if (visibility != 0) {
            if (!this.visibleOnce) {
                this.visibleOnce = true
                if (this.addPending && !this.addHidden && this.alive && ZtwoSettings.addPage.value && this.tabUid > 0) {
                    this.addPending = false
                    this.addHidden = true
                    runInBackground(PageItemAtTask())
                }
            }
            clearBackdrop()
        }
        val pageValid = this.pageValid
        if (pageValid != null && !pageValid.visibilityChanged && visibility == 0 && getVisibility() != View.VISIBLE && !this.visibilityChanged) {
            this.visibilityChanged = true
            val handler = this.uiHandler
            if (handler != null) {
                handler.post(ForceRelayoutTask())
            }
        }
        super.setVisibility(visibility)
        if (pageValid == null) {
            return
        }
        if (visibility != 0 || pageValid.getVisibility() == View.VISIBLE) {
            pageValid.setWebVisible(visibility)
        } else {
            pageValid.setVisibility(View.VISIBLE)
        }
    }

    fun refreshPage() {
        val pageValid = this.pageValid
        val handler: Handler? = if (pageValid == null || !pageValid.alive || pageValid.lifeState.toInt() == 2) null else this.uiHandler
        if (pageValid == null || !pageValid.alive || pageValid.lifeState.toInt() == 2 || handler == null) {
            return
        }
        handler.post(PausePageTask(pageValid))
    }

    fun releaseFrame() {
        this.alive = false
        clearHandler(this.uiHandler)
        this.uiHandler = null
        cancelAnims()
        clearBackdrop()
        this.shadePaint = null
        this.edgePaint = null
        this.backItem = null
        this.pageItems = null
        this.blankTimes = null
        this.frameListener = null
        this.executorCache = null
        this.webDestroy = null
    }

    fun syncAddPage(blank: Boolean) {
        val addPageState = this.addPageState
        val addPage = ZtwoSettings.addPage.value
        if (addPageState != addPage) {
            setAddPage(addPage)
        }
        val pageCreate = this.pageCreate
        if (pageCreate == null) {
            return
        }
        this.visibleOnce = false
        val quickView: QuickView? = if (blank) pageCreate.quickPanel else null
        if (blank && quickView != null) {
            quickView.refreshPending = quickView.newsChanged
            quickView.refreshed = false
        }
        pageCreate.onResume()
    }

    fun createPageAt(pageIndex: Int): WebNestView? {
        val pageItemX = pageItemAt(pageIndex)
        if (pageItemX == null) {
            return null
        }
        var startUrl = if (pageItemX.blankPending) pageItemX.pageUrl else pageItemX.url
        if (TextUtils.isEmpty(startUrl)) {
            startUrl = "about:blank"
        }
        try {
            val webNestView = WebNestView(getContext())
            if (pageItemX.blankPending) {
                val blankId = pageItemX.blankId
                webNestView.blankPending = true
                webNestView.blankUrl = startUrl
            } else {
                webNestView.setBlankPage(startUrl)
            }
            webNestView.setDeskMode(pageItemX.deskMode)
            webNestView.setBarColors(pageItemX.themeColor, pageItemX.themeLight)
            webNestView.restoreScrollX = pageItemX.scrollX
            webNestView.restoreScrollY = pageItemX.scrollY
            webNestView.setScrollPos(ZoneSettings.scrollPos.value)
            webNestView.setVisibility(View.GONE)
            webNestView.ensureWebViewClient()
            attachPage(webNestView, childIndexFor(pageIndex))
            releasePage(pageItemX, webNestView, true)
            return webNestView
        } catch (unused: Exception) {
            return null
        }
    }

    fun pageItemAt(currentPageIndex: Int): PageItem? {
        val list = this.pageItems
        if (currentPageIndex < 0 || list == null || currentPageIndex >= list.size) {
            return null
        }
        return list.get(currentPageIndex)
    }

    fun childIndexFor(pageIndex: Int): Int {
        if (pageIndex == this.currentPageIndex) {
            return getChildCount()
        }
        val pageValid = this.pageValid
        if (pageValid == null) {
            return getChildCount()
        }
        val childIndex = indexOfChild(pageValid)
        if (childIndex == -1) {
            return getChildCount()
        }
        return if (pageIndex > this.currentPageIndex) childIndex + 1 else childIndex
    }

    fun resetFakeX() {
        clearBackdrop()
        if (java.lang.Float.compare(this.fakeX, 0.0f) == 0) {
            return
        }
        this.fakeX = 0.0f
        invalidate()
    }

    companion object {
        @JvmStatic
        fun savePageItem(pageItem: PageItem?, webNestView: WebNestView?) {
            if (pageItem == null || webNestView == null) {
                return
            }
            val pageUrl = webNestView.pageUrl
            pageItem.blankPending = webNestView.blankPending
            pageItem.deskMode = webNestView.deskMode
            pageItem.themeColor = webNestView.themeColor
            pageItem.themeLight = webNestView.themeLight
            pageItem.scrollX = webNestView.scrollX
            pageItem.scrollY = webNestView.scrollY
            pageItem.webNestView = null
            if (pageItem.blankPending) {
                pageItem.blankId = webNestView.blankId
                pageItem.pageUrl = pageUrl
                pageItem.url = null
            } else {
                pageItem.blankId = 0L
                pageItem.pageUrl = null
                pageItem.url = pageUrl
            }
        }
    }
}
