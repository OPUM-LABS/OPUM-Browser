package com.mycompany.app.web

import android.view.View

import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZoneSettings
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
import com.mycompany.app.web.WebReadTask.WebReadListener
import com.mycompany.app.web.WebReadTask.ReadWebListener
import org.jsoup.nodes.TextNode
import com.mycompany.app.web.WebReadTask
import org.jsoup.nodes.CDataNode
import net.dankito.readability4j.Article
import net.dankito.readability4j.Readability4J
import org.jsoup.select.NodeTraversor
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
import ch.opumlabs.opumbrowser.R
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import org.jsoup.select.NodeVisitor
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.ArrayList

@Suppress("deprecation")
class WebReadTask(
    var activity: MainActivity?,
    var context: Context?,
    var wantSubTitle: Boolean,
    var withVideo: Boolean,
    var firstLoad: Boolean,
    var webReadListener: WebReadListener?
) {
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var pendingJs: String? = null
    @JvmField
    var pendingHtml: String? = null
    @JvmField
    var pageUrl: String? = null
    @JvmField
    var pageHost: String? = null
    @JvmField
    var articleTitle: String? = null
    @JvmField
    var subTitle: String? = null
    @JvmField
    var imageList: ArrayList<String?>? = null
    @JvmField
    var imageText: String? = null
    @JvmField
    var imageIndex: Int = 0
    @JvmField
    var runningLoad: LoadTask? = null
    @JvmField
    var readItems: ArrayList<ReadItem?>? = null
    @JvmField
    var headPending: Boolean = false
    @JvmField
    var headStarted: Boolean = false
    @JvmField
    var readListener: ReadWebListener? = null
    @JvmField
    var container: ViewGroup? = null
    @JvmField
    var readWeb: WebNestView? = null
    @JvmField
    var progressPending: Boolean = false
    @JvmField
    var pageCommitted: Boolean = false
    @JvmField
    var heightRetries: Int = 0
    @JvmField
    var attemptCount: Int = 0
    @JvmField
    var urlLoading: Boolean = false
    @JvmField
    var cleaner: WebClean? = null

    internal inner class TextNodeVisitor : NodeVisitor {

        override fun head(node: Node, nodeDepth: Int) {
            val isText = node is TextNode
            val webReadTask = this@WebReadTask
            if (!isText) {
                if (node is Element) {
                    val element = node
                    val tagName = element.tagName()
                    if ("img" != tagName && (!webReadTask.wantSubTitle || "amp-img" != tagName)) {
                        val sbQ = webReadTask.lastTextBuilder(false)
                        if (sbQ == null || sbQ.length <= 0) {
                            return
                        }
                        if (element.isBlock() || "br" == tagName) {
                            if (sbQ.length == 0 || sbQ.get(sbQ.length - 1) != '\n') {
                                val readItem = ReadItem()
                                readItem.itemKind = 0.toByte()
                                webReadTask.readItems!!.add(readItem)
                                return
                            }
                            return
                        }
                        return
                    }
                    if (webReadTask.readItems != null) {
                        val src: String = imageSrc(element)!!
                        if (TextUtils.isEmpty(src)) {
                            return
                        }
                        if (TextUtils.isEmpty(webReadTask.subTitle)) {
                            webReadTask.subTitle = src
                            val webReadListener = webReadTask.webReadListener
                            if (webReadListener != null) {
                                webReadListener.onImageUrl(src)
                            }
                            webReadTask.headStarted = false
                            return
                        }
                        if (webReadTask.headStarted) {
                            webReadTask.headStarted = false
                            return
                        }
                        val readItem = ReadItem()
                        readItem.itemKind = 3.toByte()
                        readItem.subTitle = src
                        webReadTask.readItems!!.add(readItem)
                        return
                    }
                    return
                }
                return
            }
            val textNode = node
            val wholeText = textNode.getWholeText()
            if (TextUtils.isEmpty(wholeText)) {
                return
            }
            val trimmedText = wholeText.trim { it <= ' ' }
            if (TextUtils.isEmpty(trimmedText)) {
                // Deliberate change from the original: a whitespace-only node between inline elements
                // (e.g. "<a>x</a> <a>y</a>") becomes one space instead of being dropped.
                val current = webReadTask.lastTextBuilder(false)
                if (current != null && current.length > 0) {
                    val last = current.get(current.length - 1)
                    if (last != ' ' && last != '\n') {
                        current.append(' ')
                    }
                }
                return
            }
            if (webReadTask.headPending) {
                webReadTask.headPending = false
                val articleTitle = webReadTask.articleTitle
                // the first text repeats the title (shown above it): dropped only when it is a heading or nearly the whole
                // title; a bold first word that merely starts the title ("Switzerland" of "Switzerland - Wikipedia")
                // stays (2026-11-21)
                val parentTag = textNode.parent()?.tagName()
                val inHeading = parentTag != null && parentTag.length == 2 && parentTag[0] == 'h' && parentTag[1].isDigit()
                if (if (TextUtils.isEmpty(articleTitle) || TextUtils.isEmpty(trimmedText) || articleTitle!!.length < trimmedText.length) false else articleTitle.startsWith(
                        trimmedText
                    ) && (inHeading || trimmedText.length * 10 >= articleTitle.length * 8)
                ) {
                    return
                }
            }
            val sb = webReadTask.lastTextBuilder(true)
            if (sb == null) {
                return
            }
            // inside <pre>/<textarea> (up to 6 levels up) or CDATA: keep the text as is
            val parent = textNode.parentNode()
            if (parent is Element) {
                var element = parent as Element?
                var depth = 0
                while (element != null) {
                    if (element.tag().preserveWhitespace()) {
                        sb.append(trimmedText)
                        return
                    }
                    element = element.parent()
                    depth++
                    if (depth >= 6) {
                        break
                    }
                }
            }
            if (textNode is CDataNode) {
                sb.append(trimmedText)
                return
            }
            // collapse whitespace runs to one space and drop zero-width space / soft hyphen
            // Deliberate change from the original: normalise the untrimmed text so the spaces around inline
            // elements (links, <b>, ...) survive; the original trimmed first and glued words together.
            var afterSpace = true
            if (sb.length != 0) {
                val last = sb.get(sb.length - 1)
                afterSpace = last == ' ' || last == '\n'
            }
            var seenText = false
            var lastWasSpace = false
            val length = wholeText.length
            var index = 0
            while (index < length) {
                val cp = wholeText.codePointAt(index)
                if (cp == 32 || cp == 9 || cp == 10 || cp == 12 || cp == 13 || cp == 160) {
                    if ((!afterSpace || seenText) && !lastWasSpace) {
                        sb.append(' ')
                        lastWasSpace = true
                    }
                } else if (cp != 8203 && cp != 173) {
                    sb.appendCodePoint(cp)
                    seenText = true
                    lastWasSpace = false
                }
                index += Character.charCount(cp)
            }
        }

        override fun tail(node: Node, nodeDepth: Int) {
            if (node is Element) {
                val element = node
                val sbQ = this@WebReadTask.lastTextBuilder(true)
                if (sbQ != null && element.isBlock() && (node.nextSibling() is TextNode)) {
                    val cCharAt = if (sbQ.length == 0) ' ' else sbQ.get(sbQ.length - 1)
                    if (sbQ.length == 0 || !(cCharAt == ' ' || cCharAt == '\n')) {
                        sbQ.append(' ')
                    }
                }
            }
        }
    }

    internal inner class StartLoadTask : Runnable {
        override fun run() {
            val webReadTask = this@WebReadTask
            val pendingHtml = webReadTask.pendingHtml
            webReadTask.pendingHtml = null
            if (webReadTask.readWeb == null) {
                return
            }
            val readWebListener = webReadTask.readListener
            if (readWebListener != null) {
                readWebListener.onDone()
            }
            webReadTask.startLoad(pendingHtml)
        }
    }

    internal inner class CreateWebTask : Runnable {

        internal inner class CreateWebStep : Runnable {

            internal inner class PrepareWebStep : Runnable {

                internal inner class StartReadStep : Runnable {
                    override fun run() {
                        val createWebStep = this@CreateWebStep
                        val webReadTask = this@WebReadTask
                        if (webReadTask.readWeb == null) {
                            return
                        }
                        val readWebListener = webReadTask.readListener
                        if (readWebListener != null) {
                            readWebListener.onProgress(0)
                        }
                        val task = this@WebReadTask
                        task.readWeb!!.openUrl(task.pageUrl, null)
                    }
                }

                override fun run() {
                    val createWebTask = this@CreateWebTask
                    val webReadTask = this@WebReadTask
                    val webNestView = webReadTask.readWeb
                    if (webNestView != null) {
                        webReadTask.urlLoading = true
                        webNestView.addJavascriptInterface(webReadTask.WebAppInterface(), "android")
                        val viewGroup = this@WebReadTask.container
                        if (viewGroup == null) {
                            return
                        }
                        viewGroup.post(StartReadStep())
                    }
                }
            }

            override fun run() {
                val webReadTask = this@WebReadTask
                val webNestView = webReadTask.readWeb
                if (webNestView != null) {
                    webNestView.setWebViewClient(webReadTask.LocalWebViewClient())
                    webNestView.setWebChromeClient(webReadTask.LocalChromeClient())
                    val viewGroup = webReadTask.container
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(PrepareWebStep())
                }
            }
        }

        override fun run() {
            val webReadTask = this@WebReadTask
            val webNestView = webReadTask.readWeb
            if (webNestView != null) {
                val settings = webNestView.getSettings()
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setDomStorageEnabled(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(false)
                settings.setJavaScriptEnabled(true)
                settings.setDatabaseEnabled(true)
                settings.setMixedContentMode(0)
                settings.setAllowFileAccess(true)
                MainApp.setAcceptCookies(webReadTask.context, true)
                MainApp.setThirdPartyCookies(webReadTask.context, webNestView, true)
                webNestView.setOverScrollMode(2)
                val viewGroup = webReadTask.container
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(CreateWebStep())
            }
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val webReadTask = this@WebReadTask
            val webNestView = webReadTask.readWeb
            if (webNestView == null) {
                return
            }
            webReadTask.urlLoading = false
            webNestView.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val webReadTask = this@WebReadTask
            val webNestView = webReadTask.readWeb
            if (webNestView == null) {
                return
            }
            webReadTask.urlLoading = true
            webNestView.addJavascriptInterface(webReadTask.WebAppInterface(), "android")
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val webReadTask = this@WebReadTask
            clearWebCache(webReadTask.readWeb)
            webReadTask.finishHandled = false
        }
    }

    internal inner class ReportFailedTask : Runnable {
        override fun run() {
            val webReadListener = this@WebReadTask.webReadListener
            if (webReadListener != null) {
                webReadListener.onFailed()
            }
        }
    }

    internal inner class CheckHeightTask : Runnable {
        override fun run() {
            this@WebReadTask.checkHeight()
        }
    }

    internal inner class ArticleCallback : ValueCallback<String> {

        internal inner class ArticleJsTask : Runnable {
            override fun run() {
                val webReadTask = this@WebReadTask
                val pendingJs = webReadTask.pendingJs
                webReadTask.pendingJs = null
                if (webReadTask.readWeb != null) {
                    webReadTask.pendingHtml = MainUtil.unescapeJavaText(stripQuotes(pendingJs))
                    val viewGroup = webReadTask.container
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(webReadTask.StartLoadTask())
                }
            }
        }

        override fun onReceiveValue(value: String?) {
            val webReadTask = this@WebReadTask
            webReadTask.pendingJs = value
            MainApp.runInBackground(webReadTask.context, ArticleJsTask())
        }
    }

    internal inner class CheckHeightLaterTask : Runnable {
        override fun run() {
            this@WebReadTask.checkHeight()
        }
    }

    class LoadTask(webReadTask: WebReadTask?, html: String?) : MyAsyncTask() {
        @JvmField
        var taskRef: WeakReference<*>? = null
        @JvmField
        var pageUrl: String? = null
        @JvmField
        var html: String? = null
        @JvmField
        var page: String? = null
        @JvmField
        var items: ArrayList<ReadItem?>? = null
        /** The article from inside the page ([ReaderExtract]): used instead of parsing [html] with Readability4J. */
        @JvmField
        var extracted: ReaderExtract.Result? = null

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(webReadTask)
            this.taskRef = weakReference
            val webReadTask = weakReference.get() as WebReadTask?
            if (webReadTask == null) {
                return@init
            }
            this.pageUrl = webReadTask.pageUrl
            this.html = html

        }
    }

        public override fun doInBackground() {
            var document: Document?
            var articleA: Article?
            val videoId: String?
            val pageUrl = this.pageUrl
            val webReadTask = this.taskRef!!.get() as WebReadTask?
            if (webReadTask != null) {
                val withVideo = webReadTask.withVideo
                if (this.cancelled) {
                    return
                }
                var htmlText: String? = null
                val fast = this.extracted
                try {
                    document = if (fast != null) fast.document(pageUrl!!) else Jsoup.parse(this.html!!, pageUrl!!)
                } catch (unused: Exception) {
                    document = null
                } catch (unused: LinkageError) {
                    document = null
                } catch (unused: OutOfMemoryError) {
                    document = null
                }
                try {
                    val pageOgUrl: String? = ogUrl(document)
                    if (MainUtil.isYouTubeWatch(pageOgUrl)) {
                        webReadTask.firstLoad = false
                        val webReadListener = webReadTask.webReadListener
                        if (webReadListener != null) {
                            webReadListener.onEmpty()
                        }
                        if (pageOgUrl!!.startsWith("https://www.youtube.com/watch?v=sb_read_")) {
                            videoId = pageOgUrl.substring(40)
                            webReadTask.parseParagraphs(videoId, document)
                        } else {
                            videoId = MainUtil.youTubeVideoId(pageOgUrl)
                            webReadTask.parseJsonArticle(videoId, document)
                        }
                        val arrayListT = webReadTask.headerItems()
                        this.items = arrayListT
                        this.page = buildPage(arrayListT, webReadTask.withVideo, true, false, videoId, webReadTask.imageIndex)
                        if (withVideo) {
                            this.items = filterItems(this.items)
                            return
                        }
                        return
                    }
                    if (TextUtils.isEmpty(webReadTask.subTitle)) {
                        val mainImageUrl: String? = mainImage(document, false)
                        if (!TextUtils.isEmpty(mainImageUrl)) {
                            webReadTask.subTitle = mainImageUrl
                            val webReadListener = webReadTask.webReadListener
                            if (webReadListener != null) {
                                webReadListener.onImageUrl(mainImageUrl)
                            }
                        }
                    }
                    try {
                        articleA = if (fast != null) fast.article(pageUrl!!, document!!) else Readability4J(pageUrl!!, document!!).parse()
                        try {
                            if (TextUtils.isEmpty(webReadTask.articleTitle)) {
                                webReadTask.articleTitle = articleA.title
                            }
                        } catch (unused2: Exception) {
                        }
                    } catch (unused3: Exception) {
                        articleA = null
                    }
                    if (withVideo) {
                        val arrayListE = webReadTask.buildItems(articleA)
                        this.items = arrayListE
                        this.page = buildPage(arrayListE, true, false, webReadTask.firstLoad, null, 0)
                        this.items = filterItems(this.items)
                        return
                    }
                    try {
                        val elementsSelect = document!!.select("meta[name=theme-color]")
                        if (elementsSelect.size != 0) {
                            val html = elementsSelect.toString()
                            if (!TextUtils.isEmpty(html) && html.startsWith("<meta") && html.endsWith(
                                    ">"
                                )
                            ) {
                                htmlText = html
                            }
                        }
                    } catch (unused4: Exception) {
                    }
                    this.page = webReadTask.buildHtml(articleA, htmlText)
                } catch (unused5: Exception) {
                }
            }
        }

        public override fun onCancelled() {
            val webReadTask = this.taskRef!!.get() as WebReadTask?
            if (webReadTask != null) {
                webReadTask.runningLoad = null
                val webReadListener = webReadTask.webReadListener
                if (webReadListener != null) {
                    webReadListener.onFailed()
                }
            }
        }

        public override fun onPostExecute() {
            val arrayList: ArrayList<WebReadTask.ReadItem?>?
            var attemptCount: Int = 0
            val webReadTask = this.taskRef!!.get() as WebReadTask?
            if (webReadTask != null) {
                webReadTask.runningLoad = null
                if (!webReadTask.wantSubTitle || (((this.items.also {
                        arrayList = it
                    }) != null && arrayList!!.size >= 3) || webReadTask.readWeb == null || (webReadTask.attemptCount.also {
                        attemptCount = it
                    }) >= 2)) {
                    webReadTask.finishProgress()
                    val webReadListener = webReadTask.webReadListener
                    if (webReadListener != null) {
                        webReadListener.onArticle(this.pageUrl, webReadTask.articleTitle, this.page, this.items)
                        return
                    }
                    return
                }
                webReadTask.attemptCount = attemptCount + 1
                val readWebListener = webReadTask.readListener
                if (readWebListener != null) {
                    readWebListener.onDone()
                }
                webReadTask.readWeb!!.postDelayed(webReadTask.CheckHeightLaterTask(), 200L)
            }
        }
    }

    private inner class LocalChromeClient : WebChromeClient() {
        override fun onProgressChanged(webView: WebView?, newProgress: Int) {
            val webReadTask = this@WebReadTask
            val readWebListener: ReadWebListener? = if (webReadTask.readWeb == null) null else webReadTask.readListener
            if (webReadTask.readWeb == null || readWebListener == null) {
                return
            }
            readWebListener.onProgress(newProgress)
        }
    }

    class ReadItem {
        @JvmField
        var itemKind: Byte = 0
        @JvmField
        var articleTitle: String? = null
        @JvmField
        var subTitle: String? = null
        @JvmField
        var imageText: String? = null
        @JvmField
        var textBuilder: StringBuilder? = null
        @JvmField
        var index: Int = 0
        @JvmField
        var end: Int = 0
        @JvmField
        var transText: String? = null
        @JvmField
        var mediaKind: Byte = 0
        @JvmField
        var isTitle: Boolean = false
    }

    interface ReadWebListener {
        fun onProgress(progress: Int)

        fun onDone()
    }

    private inner class WebAppInterface {
        @JavascriptInterface
        fun onDocHtml(pendingHtml: String?) {
            val webReadTask = this@WebReadTask
            webReadTask.pendingHtml = pendingHtml
            val viewGroup = webReadTask.container
            if (viewGroup == null) {
                return
            }
            viewGroup.post(webReadTask.StartLoadTask())
        }
    }

    interface WebReadListener {
        fun onFailed()

        fun onArticle(pageUrl: String?, pageTitle: String?, html: String?, arrayList: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?>?)

        fun onEmpty()

        fun onImageUrl(imageUrl: String?)
    }

    fun parseJsonArticle(videoId: String?, document: Document?) {
        var body: String? = null
        var imageList: ArrayList<String?>? = null
        var imageText: String? = null
        var subTitle: String? = null
        var embedJson: JSONObject? = null
        var thumbList: JSONArray? = null
        var imageIndex: Byte = 0
        var noImage: Any? = null
        var videoJson: JSONObject? = null
        var items: ArrayList<String?>? = null
        run label197@{
            videoJson = jsonLd(document)
            imageIndex = 0
            noImage = null
            run label196@{
            if (videoJson != null) {
                val array: Array<String>?
                try {
                    val rawBody = videoJson!!.getJSONObject("videoDetails").getString("shortDescription")
                    if (TextUtils.isEmpty(rawBody)) {
                        return@label196
                    }

                    val crlf = String(byteArrayOf(13, 10))
                    body = rawBody.replace(crlf, "\n")
                    if (TextUtils.isEmpty(body)) {
                        return@label196
                    }

                    array =
                        body.split("\n\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                } catch (exception: Exception) {
                    return@label196
                }

                run label186@{
                val count: Int
                try {
                    if (array.size < 2) {
                        return@label186
                    }

                    count = array.size
                } catch (exception2: Exception) {
                    return@label196
                }

                var paragraphIndex = 0
                var thumbItems: ArrayList<String?>? = null

                while (true) {
                    items = thumbItems
                    if (paragraphIndex >= count) {
                        return@label197
                    }

                    var paragraph: String? = array[paragraphIndex]
                    items = thumbItems

                    run label204@{
                        try {
                            if (TextUtils.isEmpty(paragraph)) {
                                return@label204
                            }

                            paragraph = paragraph!!.trim { it <= ' ' }
                        } catch (exception3: Exception) {
                            return@label196
                        }
                        items = thumbItems

                        try {
                            if (TextUtils.isEmpty(paragraph)) {
                                return@label204
                            }
                        } catch (exception4: Exception) {
                            return@label196
                        }

                        items = thumbItems
                        if (thumbItems == null) {
                            try {
                                items = ArrayList()
                            } catch (exception5: Exception) {
                                return@label196
                            }
                        }
                        try {
                            items!!.add(paragraph)
                        } catch (exception6: Exception) {
                            return@label196
                        }
                    }

                    paragraphIndex++
                    thumbItems = items
                }
                }

                try {
                    items = ArrayList()
                    items!!.add(body)
                    return@label197
                } catch (exception7: Exception) {
                }
            }
            }
            items = null
        }

        run label154@{
            if (items != null) {
                imageList = items
                if (!items.isEmpty()) {
                    return@label154
                }
            }
            imageList = ArrayList()
            imageList.add("YouTube")
        }

        run label149@{
            this.imageList = imageList
            run label148@{
            if (videoJson != null) {
                val hasImage: Boolean
                try {
                    imageText = videoJson.getJSONObject("microformat")
                        .getJSONObject("playerMicroformatRenderer").getJSONObject("embed")
                        .getString("iframeUrl")
                    hasImage = URLUtil.isNetworkUrl(imageText)
                } catch (exception8: Exception) {
                    return@label148
                }

                if (hasImage) {
                    return@label149
                }
            }
            }
            imageText = null
        }

        if (!TextUtils.isEmpty(imageText)) {
            var newImageIndex: Int
            newImageIndex = imageIndex.toInt()
            run label138@{
            if (videoJson != null) {
                try {
                    embedJson = videoJson.getJSONObject("microformat")
                        .getJSONObject("playerMicroformatRenderer").getJSONObject("embed")
                } catch (exception9: Exception) {
                    newImageIndex = imageIndex.toInt()
                    return@label138
                }

                newImageIndex = imageIndex.toInt()
                run label134@{
                if (embedJson != null) {
                    val width: Int
                    try {
                        width = embedJson.getInt("width")
                    } catch (exception10: Exception) {
                        newImageIndex = imageIndex.toInt()
                        return@label134
                    }

                    newImageIndex = imageIndex.toInt()
                    run label130@{
                    if (width > 0) {
                        val height: Int
                        try {
                            height = embedJson.getInt("height")
                        } catch (exception11: Exception) {
                            newImageIndex = imageIndex.toInt()
                            return@label130
                        }

                        newImageIndex = imageIndex.toInt()
                        if (height > 0) {
                            try {
                                newImageIndex = Math.round(height.toFloat() / width * 100.0f)
                            } catch (exception12: Exception) {
                                newImageIndex = imageIndex.toInt()
                            }
                        }
                    }
                    }
                }
                }
            }
            }

            this.imageIndex = newImageIndex
        } else if (!TextUtils.isEmpty(videoId)) {
            imageText = "https://www.youtube.com/embed/$videoId"
        }

        this.imageText = imageText
        if (this.wantSubTitle && TextUtils.isEmpty(this.subTitle)) {
            run label122@{
                run label121@{
                if (videoJson != null) {
                    try {
                        thumbList = videoJson.getJSONObject("thumbnail").getJSONArray("thumbnails")
                    } catch (exception13: Exception) {
                        return@label121
                    }

                    run label117@{
                    if (thumbList != null) {
                        var imageCount: Int
                        try {
                            imageCount = thumbList.length()
                        } catch (exception14: Exception) {
                            return@label117
                        }

                        imageCount--

                        while (imageCount > -1) {
                            val hasSubtitle: Boolean
                            try {
                                subTitle = thumbList.getJSONObject(imageCount).getString("url")
                                hasSubtitle = URLUtil.isNetworkUrl(subTitle)
                            } catch (exception15: Exception) {
                                imageCount--
                                continue
                            }

                            if (hasSubtitle) {
                                return@label122
                            }
                            imageCount--
                        }
                    }
                    }
                }
                }
                subTitle = null
            }

            var newSubTitle = subTitle
            if (TextUtils.isEmpty(subTitle)) {
                var imageUrl = noImage as String?
                if (!TextUtils.isEmpty(videoId)) {
                    imageUrl = "https://img.youtube.com/vi/$videoId/0.jpg"
                }

                newSubTitle = imageUrl
            }

            this.subTitle = newSubTitle
            val webReadListener = this.webReadListener
            if (webReadListener != null) {
                webReadListener.onImageUrl(newSubTitle)
            }
        }
    }

    fun parseParagraphs(videoId: String?, document: Document?) {
        var imgUrl: String?
        val elementsSelect: Elements? = if (document == null) null else document.select("p")
        if (document == null || elementsSelect == null || elementsSelect.size == 0) {
            return
        }
        val size = elementsSelect.size
        var frameIndex = 0
        var element: Element? = null
        var first = true
        var index = 0
        var arrayList: ArrayList<String?>? = null
        while (index < size) {
            val t: Any? = elementsSelect.get(index)
            index++
            val paragraph = t as Element?
            if (paragraph != null) {
                val html = paragraph.html()
                if (!TextUtils.isEmpty(html)) {
                    val oneLineHtml = html.replace("\n", "")
                    if (first) {
                        this.articleTitle = oneLineHtml
                        first = false
                    } else {
                        if (arrayList == null) {
                            arrayList = ArrayList()
                        }
                        arrayList.add(oneLineHtml)
                    }
                }
            }
        }
        this.imageList = arrayList
        if (TextUtils.isEmpty(this.articleTitle)) {
            this.articleTitle = "YouTube"
        }
        val frames = document.select("iframe")
        if (frames.size == 0) {
            return
        }
        val frameCount = frames.size
        while (true) {
            if (frameIndex >= frameCount) {
                imgUrl = null
                break
            }
            val rawElement: Any? = frames.get(frameIndex)
            frameIndex++
            val frame = rawElement as Element?
            if (frame != null) {
                imgUrl = frame.attr("src")
                if (URLUtil.isNetworkUrl(imgUrl)) {
                    element = frame
                    break
                }
            }
        }
        if (element != null) {
            if (TextUtils.isEmpty(imgUrl) && !TextUtils.isEmpty(videoId)) {
                imgUrl = "https://www.youtube.com/embed/" + videoId
            }
            if (TextUtils.isEmpty(imgUrl)) {
                return
            }
            this.imageText = imgUrl
            val imgWidth = parseIntSafe(element.attr("width"))
            val imgHeight = if (imgWidth <= 0) 0 else parseIntSafe(element.attr("height"))
            if (imgWidth <= 0 || imgHeight <= 0) {
                return
            }
            this.imageIndex = Math.round((imgHeight / imgWidth) * 100.0f)
        }
    }

    fun loadUrl(pageUrl: String?) {
        if (this.readWeb != null) {
            val isHttp = isHttpUrl(pageUrl)
            val urlLoading = this.urlLoading
            if (isHttp) {
                if (urlLoading) {
                    this.urlLoading = false
                    val webNestView = this.readWeb
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
            val webNestView = this.readWeb
            if (webNestView == null) {
                return
            }
            webNestView.post(AddJsBridgeTask())
        }
    }

    /** Starts from an article Readability.js found inside the page ([ReaderExtract]); no page HTML to parse. */
    fun startLoadArticle(extracted: ReaderExtract.Result) {
        this.runningLoad?.cancelled = true
        val loadTask = LoadTask(this, null)
        loadTask.extracted = extracted
        this.runningLoad = loadTask
        loadTask.start(this.context)
    }

    fun startLoad(pageUrl: String?) {
        val loadTask = this.runningLoad
        if (loadTask != null) {
            loadTask.cancelled = true
        }
        this.runningLoad = null
        val runningLoad = LoadTask(this, pageUrl)
        this.runningLoad = runningLoad
        runningLoad.start(this.context)
    }

    fun buildHtml(article: Article?, body: String?): String {
        val element: Element?
        val found: Boolean
        var imageName: String?
        val elementSelectFirst: Element?
        val posterNode: Element?
        val videoPoster: String?
        val firstImage: String?
        val articleTitle = this.articleTitle
        var subTitle = this.subTitle
        if (article != null) {
            element = article.articleContent
            cleanFigures(element)
        } else {
            element = null
        }
        val sb =
            StringBuilder("<!DOCTYPE html><html dir='auto'><head><meta charset=\"utf-8\"/><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes'/>")
        if (!TextUtils.isEmpty(body)) {
            sb.append(body)
        }
        if (!TextUtils.isEmpty(subTitle)) {
            sb.append("<meta property='og:image' content='").append(subTitle).append("'>")
        }
        if (ZoneSettings.userFont.value || ZoneSettings.fontBold.value) {
            sb.append("<style id='sb_font_style'>")
            if (ZoneSettings.userFont.value) {
                sb.append("@font-face{font-family:soul_user_font;src:url(\"soul_user_font.ttf\")}")
            }
            sb.append("body,body *:not([class*=\"icon\"]):not([class^=\"fa\"]):not(ion-icon){")
            if (ZoneSettings.userFont.value) {
                sb.append("font-family:soul_user_font !important;")
            }
            if (ZoneSettings.fontBold.value) {
                sb.append("font-weight:bold !important;")
            }
            sb.append("}</style>")
        }
        sb.append("<style>body{margin:0;padding:16px 16px 40px 16px;}figure,picture{margin:0;}figcaption{margin:0 auto 40px auto;}img{margin:0 auto 20px auto;display:block;max-width:100%;height:auto;}iframe{margin:0 auto 20px auto;display:block;max-width:100%;height:52vw;}svg{margin:0;width:16px;height:16px;}h1,h2{margin:0 auto 20px auto;line-height:1.4;word-wrap:break-word;font-size:24px;}p{margin:0 auto 32px auto;line-height:1.6;word-wrap:break-word;font-size:18px;}a{margin:0 auto 32px auto;line-height:1.6;word-wrap:break-word;font-size:18px;text-decoration:none;}")
        if (!AppState.darkWeb) {
            sb.append("a:link{color:#4080FF;}")
        }
        sb.append("</style></head><body>")
        var hasBody = true
        if (TextUtils.isEmpty(articleTitle)) {
            found = false
        } else {
            val heading: Element? = if (!TextUtils.isEmpty(articleTitle) && element != null) element.selectFirst("h1,h2") else null
            if (!TextUtils.isEmpty(articleTitle) && element != null && heading != null) {
                val headingText = heading.text()
                if (if (TextUtils.isEmpty(articleTitle) || TextUtils.isEmpty(headingText) || articleTitle!!.length < headingText.length) false else articleTitle.startsWith(
                        headingText
                    )
                ) {
                    heading.text("")
                }
            }
            sb.append("<title>").append(articleTitle).append("</title><h2>").append(articleTitle)
            sb.append("</h2>")
            found = true
        }
        if (element != null) {
            if (TextUtils.isEmpty(subTitle)) {
                imageName = null
            } else {
                imageName = MainUtil.stripExtension(URLUtilCompat.guessFileName(subTitle!!, null, "image/*"))
                if (TextUtils.isEmpty(imageName)) {
                    imageName = null
                } else {
                    val dotParts: Array<String>? =
                        imageName!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (dotParts != null && dotParts.size > 1) {
                        var longest = ""
                        for (part in dotParts) {
                            if (part.length > longest.length) {
                                longest = part
                            }
                        }
                        if (!TextUtils.isEmpty(longest)) {
                            imageName = longest
                        }
                    }
                    val underscoreParts: Array<String>? =
                        imageName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (underscoreParts != null && underscoreParts.size > 1) {
                        var longestPart = ""
                        for (longest in underscoreParts) {
                            if (longest.length > longestPart.length) {
                                longestPart = longest
                            }
                        }
                        if (!TextUtils.isEmpty(longestPart)) {
                            imageName = longestPart
                        }
                    }
                }
            }
            if (imageName == null || imageName.length <= 5) {
                elementSelectFirst = element.selectFirst("img[src*='http'],amp-img[src*='http']")
                if (elementSelectFirst != null) {
                    firstImage = imageSrc(elementSelectFirst)
                    if (TextUtils.isEmpty(firstImage)) {
                        posterNode = element.selectFirst("[poster*='http']")
                        if (posterNode != null) {
                            videoPoster = posterUrl(posterNode)
                            if (!TextUtils.isEmpty(videoPoster)) {
                                if (TextUtils.isEmpty(subTitle)) {
                                    subTitle = videoPoster
                                }
                                posterNode.removeAttr("poster")
                                posterNode.html("")
                            }
                        }
                    } else {
                        if (TextUtils.isEmpty(subTitle)) {
                            subTitle = firstImage
                        }
                        elementSelectFirst.removeAttr("src")
                        elementSelectFirst.html("")
                    }
                } else {
                    posterNode = element.selectFirst("[poster*='http']")
                    if (posterNode != null) {
                        videoPoster = posterUrl(posterNode)
                        if (!TextUtils.isEmpty(videoPoster)) {
                            if (TextUtils.isEmpty(subTitle)) {
                                subTitle = videoPoster
                            }
                            posterNode.removeAttr("poster")
                            posterNode.html("")
                        }
                    }
                }
            } else {
                val imageNode =
                    element.selectFirst("img[src*='$imageName'],amp-img[src*='$imageName']")
                if (imageNode != null) {
                    imageNode.removeAttr("src")
                    imageNode.html("")
                } else {
                    val posterTarget = element.selectFirst("[poster*='" + imageName + "']")
                    if (posterTarget != null) {
                        posterTarget.removeAttr("poster")
                        posterTarget.html("")
                    } else {
                        elementSelectFirst =
                            element.selectFirst("img[src*='http'],amp-img[src*='http']")
                        if (elementSelectFirst != null) {
                            firstImage = imageSrc(elementSelectFirst)
                            if (TextUtils.isEmpty(firstImage)) {
                                if (TextUtils.isEmpty(subTitle)) {
                                    subTitle = firstImage
                                }
                                elementSelectFirst.removeAttr("src")
                                elementSelectFirst.html("")
                            } else {
                                posterNode = element.selectFirst("[poster*='http']")
                                if (posterNode != null) {
                                    videoPoster = posterUrl(posterNode)
                                    if (!TextUtils.isEmpty(videoPoster)) {
                                        if (TextUtils.isEmpty(subTitle)) {
                                            subTitle = videoPoster
                                        }
                                        posterNode.removeAttr("poster")
                                        posterNode.html("")
                                    }
                                }
                            }
                        } else {
                            posterNode = element.selectFirst("[poster*='http']")
                            if (posterNode != null) {
                                videoPoster = posterUrl(posterNode)
                                if (!TextUtils.isEmpty(videoPoster)) {
                                    if (TextUtils.isEmpty(subTitle)) {
                                        subTitle = videoPoster
                                    }
                                    posterNode.removeAttr("poster")
                                    posterNode.html("")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(subTitle)) {
            val secureImage = subTitle!!.replace("http://", "https://")
            sb.append("<img src='")
            sb.append(secureImage)
            sb.append("' onerror=\"this.style.display='none';\"></img>")
        }
        if (article == null) {
            hasBody = found
        } else {
            val articleContent = article.articleContent
            val bodyHtml = if (articleContent != null) articleContent.html() else null
            if (TextUtils.isEmpty(bodyHtml)) {
                hasBody = found
            } else {
                val cleanBody =
                    bodyHtml!!.replace("<p>Your browser doesn't support HTML5 video.</p>", "")
                if (TextUtils.isEmpty(cleanBody)) {
                    hasBody = found
                } else {
                    sb.append(cleanBody)
                }
            }
        }
        // no article text: nothing to show (the reader says so and stays closed)
        if (!hasBody) return ""
        sb.append("</body></html>")
        return sb.toString()
    }

    fun buildItems(article: Article?): ArrayList<ReadItem?>? {
        this.readItems = ArrayList()
        if (article != null) {
            try {
                val element = article.articleContent
                if (element != null) {
                    cleanFigures(element)
                    this.headPending = !TextUtils.isEmpty(this.articleTitle)
                    this.headStarted = !TextUtils.isEmpty(this.subTitle)
                    NodeTraversor.traverse(TextNodeVisitor(), element)
                }
            } catch (unused: Exception) {
            }
        }
        if (!TextUtils.isEmpty(this.subTitle)) {
            val readItem = ReadItem()
            readItem.itemKind = 3.toByte()
            readItem.subTitle = this.subTitle
            this.readItems!!.add(0, readItem)
        }
        if (!TextUtils.isEmpty(this.articleTitle)) {
            val readItem = ReadItem()
            readItem.itemKind = 1.toByte()
            readItem.articleTitle = this.articleTitle
            this.readItems!!.add(0, readItem)
        }
        val arrayList = this.readItems
        if (arrayList == null || arrayList.isEmpty()) {
            return null
        }
        val rawItems: ArrayList<Any?> = ArrayList<Any?>()
        val readItems = this.readItems
        val size = readItems!!.size
        var index = 0
        while (index < size) {
            val rawReadItem: Any? = readItems.get(index)
            index++
            val readItem = rawReadItem as ReadItem?
            if (readItem != null && readItem.itemKind.toInt() != 0) {
                val sb = readItem.textBuilder
                if (sb == null) {
                    rawItems.add(readItem)
                } else {
                    val itemText = sb.toString()
                    readItem.textBuilder = null
                    if (!TextUtils.isEmpty(itemText)) {
                        var trimmedText: String? = itemText.trim { it <= ' ' }
                        if (TextUtils.isEmpty(trimmedText) || (trimmedText!!.startsWith("Your browser does") && (trimmedText.startsWith(
                                "n't support",
                                17
                            ) || trimmedText.startsWith(" not support", 17)))
                        ) {
                            trimmedText = null
                        }
                        if (!TextUtils.isEmpty(trimmedText)) {
                            readItem.articleTitle = trimmedText
                            rawItems.add(readItem)
                        }
                    }
                }
            }
        }
        if (rawItems.isEmpty()) {
            return null
        }
        val merged: ArrayList<ReadItem?> = ArrayList()
        val frameCount = rawItems.size
        var found = false
        for (index4 in 0..<frameCount) {
            val entry = rawItems.get(index4) as ReadItem?
            if (entry == null || entry.itemKind.toInt() != 2) {
                merged.add(entry)
            } else {
                val frameIndex = index4 - 1
                val readItem =
                    if (frameIndex < 0 || frameIndex >= rawItems.size) null else rawItems.get(frameIndex) as ReadItem?
                if (readItem == null || readItem.itemKind.toInt() != 2 || !MainUtil.sameObject(
                        entry.articleTitle,
                        readItem.articleTitle
                    )
                ) {
                    val articleTitle = entry.articleTitle
                    val length = if (articleTitle != null) articleTitle.length else 0
                    if (articleTitle != null && length != 0 && length <= 40) {
                        val last = length - 1
                        if (articleTitle.get(last) != '.' && articleTitle.get(last) != '?' && articleTitle.get(last).code != 12290) {
                            entry.isTitle = true
                            found = true
                        }
                    }
                    merged.add(entry)
                }
            }
        }
        if (merged.isEmpty()) {
            return null
        }
        if (found) {
            val mergedCount = merged.size
            for (index5 in 0..<mergedCount) {
                val readItem = merged.get(index5)
                if (readItem != null && readItem.itemKind.toInt() == 2) {
                    if ("\"" != readItem.articleTitle) {
                        break
                    }
                    merged.removeAt(index5)
                    break
                }
            }
            if (merged.isEmpty()) {
                return null
            }
            for (size4 in merged.size - 1 downTo -1 + 1) {
                val readItem = merged.get(size4)
                if (readItem != null && readItem.itemKind.toInt() == 2) {
                    if ("\"" != readItem.articleTitle) {
                        break
                    }
                    merged.removeAt(size4)
                    break
                }
            }
            if (merged.isEmpty()) {
                return null
            }
            val mergedSize = merged.size
            for (index6 in 0..<mergedSize) {
                val titleItem = merged.get(index6)
                if (titleItem != null && titleItem.isTitle) {
                    val nextIndex = index6 + 1
                    val readItem =
                        if (nextIndex < 0 || nextIndex >= merged.size) null else merged.get(nextIndex)
                    if (readItem == null || !readItem.isTitle) {
                        titleItem.isTitle = false
                    }
                }
            }
        }
        return merged
    }

    fun lastTextBuilder(create: Boolean): StringBuilder? {
        val arrayList = this.readItems
        if (arrayList != null) {
            val size = arrayList.size
            var readItem = if (size > 0) this.readItems!!.get(size - 1) else null
            if (create) {
                if (readItem == null || readItem.itemKind.toInt() != 2) {
                    readItem = ReadItem()
                    readItem.itemKind = 2.toByte()
                    this.readItems!!.add(readItem)
                }
                val sb = readItem.textBuilder
                if (sb != null) {
                    return sb
                }
                val textBuilder = StringBuilder()
                readItem.textBuilder = textBuilder
                return textBuilder
            }
            if (readItem != null && readItem.itemKind.toInt() == 2) {
                return readItem.textBuilder
            }
        }
        return null
    }

    fun headerItems(): ArrayList<ReadItem?> {
        val arrayList: ArrayList<ReadItem?> = ArrayList()
        if (!TextUtils.isEmpty(this.articleTitle)) {
            val readItem = ReadItem()
            readItem.itemKind = 1.toByte()
            readItem.articleTitle = this.articleTitle
            arrayList.add(readItem)
        }
        if (!TextUtils.isEmpty(this.imageText)) {
            val readItem = ReadItem()
            readItem.itemKind = 4.toByte()
            readItem.imageText = this.imageText
            arrayList.add(readItem)
        } else if (!TextUtils.isEmpty(this.subTitle)) {
            val readItem = ReadItem()
            readItem.itemKind = 3.toByte()
            readItem.subTitle = this.subTitle
            arrayList.add(readItem)
        }
        val imageList = this.imageList
        if (imageList != null && !imageList.isEmpty()) {
            val imageListNow = this.imageList
            val size = imageListNow!!.size
            var index = 0
            while (index < size) {
                val item: Any? = imageListNow.get(index)
                index++
                val itemText = item as String?
                if (!TextUtils.isEmpty(itemText)) {
                    val lines =
                        itemText!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (lines.size < 2) {
                        val readItem = ReadItem()
                        readItem.itemKind = 2.toByte()
                        readItem.articleTitle = itemText
                        arrayList.add(readItem)
                    } else {
                        var readItem: ReadItem? = null
                        for (articleTitle in lines) {
                            if (!TextUtils.isEmpty(articleTitle)) {
                                readItem = ReadItem()
                                readItem.itemKind = 2.toByte()
                                readItem.articleTitle = articleTitle
                                readItem.isTitle = true
                                arrayList.add(readItem)
                            }
                        }
                        if (readItem != null) {
                            readItem.isTitle = false
                        }
                    }
                }
            }
        }
        return arrayList
    }

    fun createWebView() {
        if (this.activity == null || this.container == null || this.readWeb != null) {
            return
        }
        if (this.cleaner == null) {
            this.cleaner = MainApp.webClean(this.context, false)
        }
        this.pageCommitted = false
        this.heightRetries = 0
        this.attemptCount = 0
        val webNestView = WebNestView(this.activity)
        this.readWeb = webNestView
        webNestView.setNoAutofill(true)
        MainApp.resumeWebTimers(this.context, this.readWeb)
        this.readWeb!!.setVisibility(View.INVISIBLE)
        this.container!!.addView(this.readWeb, 0, ViewGroup.LayoutParams(-1, -1))
        this.container!!.post(CreateWebTask())
    }

    fun checkHeight() {
        if (this.readWeb == null) {
            return
        }
        val readWebListener = this.readListener
        if (readWebListener != null) {
            readWebListener.onDone()
        }
        if (this.heightRetries < 10 && this.readWeb!!.getContentHeight() < this.readWeb!!.getHeight()) {
            this.heightRetries++
            this.readWeb!!.postDelayed(CheckHeightTask(), 200L)
            return
        }
        val isHttp = isHttpUrl(this.pageUrl)
        val webNestView = this.readWeb
        if (isHttp) {
            webNestView!!.evaluateJavascript(
                "document.documentElement.innerHTML",
                ArticleCallback()
            )
        } else {
            evalJs(
                webNestView,
                "(function(){var htm=null;if(document.documentElement){htm=document.documentElement.innerHTML;}android.onDocHtml(htm);})();",
                false
            )
        }
    }

    fun release() {
        val loadTask = this.runningLoad
        if (loadTask != null) {
            loadTask.cancelled = true
        }
        this.runningLoad = null
        finishProgress()
        MainApp.applyCookieSetting(this.context)
        this.activity = null
        this.context = null
        this.webReadListener = null
        this.pageUrl = null
        this.pageHost = null
        this.articleTitle = null
        this.subTitle = null
        this.imageList = null
        this.imageText = null
        this.readItems = null
        this.readListener = null
        this.container = null
        this.cleaner = null
    }

    fun finishProgress() {
        if (this.readWeb == null) {
            return
        }
        val readWebListener = this.readListener
        if (readWebListener != null) {
            readWebListener.onProgress(100)
        }
        if (this.progressPending) {
            this.progressPending = false
            this.readWeb!!.stopLoading()
        }
        destroyWebViewNow(this.readWeb)
        this.readWeb = null
    }

    fun cleanFigures(element: Element?) {
        if (element != null) {
            val figures = element.select("figure")
            var index = 0
            if (figures.size != 0) {
                val size = figures.size
                var figureIndex = 0
                while (figureIndex < size) {
                    val t: Any? = figures.get(figureIndex)
                    figureIndex++
                    val figure = t as Element?
                    if (figure != null) {
                        figure.html(childText(figure, false))
                    }
                }
            }
            val pictures = element.select("picture")
            if (pictures.size != 0) {
                val size = pictures.size
                var pictureIndex = 0
                while (pictureIndex < size) {
                    val rawElement: Any? = pictures.get(pictureIndex)
                    pictureIndex++
                    val picture = rawElement as Element?
                    if (picture != null) {
                        picture.html(childText(picture, false))
                    }
                }
            }
            val elementsSelect: Elements? = if (!this.withVideo) null else element.select("video")
            if (!this.withVideo || elementsSelect == null || elementsSelect.size == 0) {
                return
            }
            val size = elementsSelect.size
            while (index < size) {
                val rawElement: Any? = elementsSelect.get(index)
                index++
                val video = rawElement as Element?
                if (video != null) {
                    video.html(childText(video, true))
                }
            }
        }
    }

    private inner class LocalWebViewClient : WebViewClient() {

        inner class PageFinishedTask : Runnable {
            override fun run() {
                val webReadTask = this@WebReadTask
                val webNestView = webReadTask.readWeb
                if (webNestView != null) {
                    webNestView.startAdsClean(webReadTask.pageUrl, webReadTask.pageHost, false)
                }
            }
        }

        inner class PageStartedTask : Runnable {
            override fun run() {
                val webReadTask = this@WebReadTask
                val webNestView = webReadTask.readWeb
                if (webNestView != null) {
                    webNestView.startAdsClean(webReadTask.pageUrl, webReadTask.pageHost, false)
                }
            }
        }

        override fun doUpdateVisitedHistory(webView: WebView?, url: String?, isReload: Boolean) {
            val webReadTask = this@WebReadTask
            webReadTask.pageUrl = url
            webReadTask.pageHost = hostOfUrl(url, true)
        }

        override fun onPageCommitVisible(webView: WebView?, url: String?) {
            val webReadTask = this@WebReadTask
            if (webReadTask.pageCommitted) {
                return
            }
            webReadTask.pageCommitted = true
            webReadTask.checkHeight()
        }

        override fun onPageFinished(webView: WebView?, url: String?) {
            val webReadTask = this@WebReadTask
            if (webReadTask.readWeb == null) {
                return
            }
            webReadTask.progressPending = false
            webReadTask.loadUrl(url)
            if (!webReadTask.finishHandled && !TextUtils.isEmpty(url) && (url != webReadTask.lastFinishedUrl)) {
                webReadTask.finishHandled = true
                webReadTask.lastFinishedUrl = url
                val viewGroup = webReadTask.container
                if (viewGroup != null) {
                    viewGroup.post(webReadTask.ClearCacheTask())
                }
            }
            webReadTask.pageUrl = url
            webReadTask.pageHost = hostOfUrl(url, true)
            if (!webReadTask.pageCommitted) {
                webReadTask.pageCommitted = true
                webReadTask.checkHeight()
            }
            MainApp.runInBackground(webReadTask.context, PageStartedTask())
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val webReadTask = this@WebReadTask
            if (webReadTask.readWeb == null) {
                return
            }
            webReadTask.progressPending = true
            webReadTask.loadUrl(url)
            webReadTask.pageUrl = url
            webReadTask.pageHost = hostOfUrl(url, true)
            MainApp.runInBackground(webReadTask.context, PageFinishedTask())
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val webReadTask = this@WebReadTask
            webReadTask.readWeb = null
            onRenderGone(webView, renderProcessGoneDetail)
            val viewGroup = webReadTask.container
            if (viewGroup == null) {
                return true
            }
            viewGroup.post(webReadTask.ReportFailedTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val webReadTask = this@WebReadTask
            if (webReadTask.readWeb == null || webResourceRequest == null || webResourceRequest.getUrl() == null) {
                return null
            }
            val url = webResourceRequest.getUrl().toString()
            webReadTask.loadUrl(url)
            val webClean = webReadTask.cleaner
            val webResourceResponseJ: WebResourceResponse? = if (webClean == null) null else webClean.intercept(
                    webReadTask.context,
                    webView,
                    webResourceRequest,
                    webReadTask.pageUrl,
                    webReadTask.pageHost,
                    url,
                    0
                )
            if (webClean == null || webResourceResponseJ == null) {
                return null
            }
            return webResourceResponseJ
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val webReadTask = this@WebReadTask
            if (webReadTask.readWeb != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    webReadTask.loadUrl(url)
                }
            }
            return false
        }
    }

    companion object {

        @JvmStatic
        fun ampImage(document: Document?): String? {
            if (document == null) {
                return null
            }
            try {
                val elementsSelect = document.select("amp-img")
                if (elementsSelect.size != 0) {
                    val size = elementsSelect.size
                    var index = 0
                    while (index < size) {
                        val rawElement: Any? = elementsSelect.get(index)
                        index++
                        val element = rawElement as Element?
                        if (element != null) {
                            val src: String = imageSrc(element)!!
                            if (!TextUtils.isEmpty(src)) {
                                return src
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
            }
            return null
        }

        @JvmStatic
        fun figureImage(document: Document?): String? {
            if (document == null) {
                return null
            }
            try {
                val elementsSelect = document.select("figure")
                if (elementsSelect.size != 0) {
                    val size = elementsSelect.size
                    var index = 0
                    while (index < size) {
                        val rawElement: Any? = elementsSelect.get(index)
                        index++
                        val element = rawElement as Element?
                        if (element != null) {
                            val childImage: String? = firstChildImage(element)
                            if (!TextUtils.isEmpty(childImage)) {
                                return childImage
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
            }
            return null
        }

        @JvmStatic
        fun firstChildImage(element: Element?): String? {
            val elementsChildren: Elements? = if (element == null) null else element.children()
            if (element == null || elementsChildren == null) {
                return null
            }
            val size = elementsChildren.size
            var index = 0
            while (index < size) {
                val t: Any? = elementsChildren.get(index)
                index++
                val child = t as Element?
                val src: String = imageSrc(child)!!
                if (!TextUtils.isEmpty(src)) {
                    return src
                }
                val childImage: String? = firstChildImage(child)
                if (!TextUtils.isEmpty(childImage)) {
                    return childImage
                }
            }
            return null
        }

        @JvmStatic
        fun mainImage(document: Document?, poster: Boolean): String? {
            var src: String? = null
            if (document == null) {
                return null
            }
            if (poster) {
                val ogImage: String = ogUrl(document)!!
                if (MainUtil.isYouTubeWatch(ogImage)) {
                    val youTubeId = MainUtil.youTubeIdOf(ogImage)
                    val youTubeThumb = if (TextUtils.isEmpty(youTubeId)) null else "https://img.youtube.com/vi/$youTubeId/0.jpg"
                    if (!TextUtils.isEmpty(youTubeThumb)) {
                        return youTubeThumb
                    }
                }
            }
            try {
                val elementsSelect = document.select("meta[property=og:image]")
                if (elementsSelect.size != 0) {
                    val metaContent = elementsSelect.attr("content")
                    if (isGoodImage(metaContent)) {
                        src = metaContent
                    }
                }
            } catch (unused: Exception) {
            }
            if (!TextUtils.isEmpty(src)) {
                return src
            }
            val figure: String? = figureImage(document)
            if (!TextUtils.isEmpty(figure)) {
                return figure
            }
            val picture: String? = pictureImage(document)
            if (!TextUtils.isEmpty(picture)) {
                return picture
            }
            val firstImg: String = firstImage(document)!!
            return if (!TextUtils.isEmpty(firstImg)) firstImg else ampImage(document)
        }

        @JvmStatic
        fun htmlHead(dark: Boolean, firstLoad: Boolean): StringBuilder {
            val textColor = if (AppState.darkUi) "white;}" else "black;}"
            val sbR =
                StringBuilder().append("<!DOCTYPE html><html dir='auto'><head><meta charset=\"utf-8\"/><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'/><style>")
            if (AppState.darkUi) {
                sbR.append("::selection{background:#1976d2;color:white;}")
            } else {
                sbR.append("::selection{background:#b3e5fc;color:black;}")
            }
            sbR.append("font{background:transparent !important;box-shadow:none !important;}")
            if (dark) {
                sbR.append("body{margin:0;padding:16px 16px 40px 16px;}")
            } else {
                sbR.append("@font-face{font-family:soul_user_font;src:url(\"soul_user_font.ttf\")}body,body *:not([class*=\"icon\"]):not([class^=\"fa\"]):not(ion-icon){font-family:soul_user_font !important;}")
                if (firstLoad) {
                    if (MainUtil.isFullScreenOff()) {
                        sbR.append("body{margin:0;padding:48px 16px 440px 16px;}")
                    } else {
                        sbR.append("body{margin:0;padding:20px 16px 440px 16px;}")
                    }
                } else if (MainUtil.isFullScreenOff()) {
                    sbR.append("body{margin:0;padding:48px 16px 80px 16px;}")
                } else {
                    sbR.append("body{margin:0;padding:20px 16px 80px 16px;}")
                }
                sbR.append("img{margin:0 auto 20px auto;display:block;max-width:100%;height:auto;user-select:none;}")
            }
            sbR.append("p{margin:0 auto 32px auto;line-height:1.6;word-wrap:break-word;font-size:18px;color:").append(textColor).append("</style>")
            if (ReadSettings.fontBold.value) {
                sbR.append("<style id='sb_bold_style'>body,body *:not([class*=\"icon\"]):not([class^=\"fa\"]):not(ion-icon){font-weight:bold !important;}</style>")
            }
            sbR.append("</head><body>")
            return sbR
        }

        @JvmStatic
        fun childText(element: Element?, poster: Boolean): String {
            val elementsChildren: Elements? = if (element == null) null else element.children()
            if (element == null || elementsChildren == null) {
                return ""
            }
            val size = elementsChildren.size
            var index = 0
            while (index < size) {
                val t: Any? = elementsChildren.get(index)
                index++
                val child = t as Element?
                val childSrc: String? = if (poster) posterUrl(child) else imageSrc(child)
                if (!TextUtils.isEmpty(childSrc)) {
                    return "<img src='${childSrc!!.replace("http://", "https://")}' onerror=\"this.style.display='none';\"></img>"
                }
                val innerText: String = childText(child, poster)
                if (!TextUtils.isEmpty(innerText)) {
                    return innerText
                }
            }
            return ""
        }

        @JvmStatic
        fun imageSrc(element: Element?): String? {
            val attributes: Attributes? = if (element == null) null else element.attributes()
            if (element == null || attributes == null) {
                return null
            }
            if ("amp-img" == element.tagName()) {
                for (attribute in attributes) {
                    val key = attribute.key
                    if ("placeholder" == key) {
                        return null
                    }
                    if ("width" == key || "height" == key) {
                        if (parseIntSafe(attribute.value) < 40) {
                            return null
                        }
                    }
                }
            }
            for (attribute in attributes) {
                val parsedValue = MainUtil.parseFontList(attribute.value)
                if (Companion.isGoodImage(parsedValue!!)) {
                    return parsedValue
                }
            }
            return null
        }

        @JvmStatic
        fun firstImage(document: Document?): String? {
            if (document == null) {
                return null
            }
            try {
                val elementsSelect = document.select("img")
                if (elementsSelect.size != 0) {
                    val size = elementsSelect.size
                    var index = 0
                    while (index < size) {
                        val rawElement: Any? = elementsSelect.get(index)
                        index++
                        val element = rawElement as Element?
                        if (element != null) {
                            val src: String = imageSrc(element)!!
                            if (!TextUtils.isEmpty(src)) {
                                return src
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
            }
            return null
        }

        @JvmStatic
        fun buildPage(
            arrayList: ArrayList<ReadItem?>?,
            withVideo: Boolean,
            dark: Boolean,
            firstLoad: Boolean,
            head: String?,
            imageIndex: Int
        ): String? {
            val sbI: StringBuilder?
            var frameIndex = imageIndex
            if (arrayList == null || arrayList.isEmpty()) {
                return null
            }
            var inList = false
            if (dark) {
                val textColor = if (AppState.darkUi) "white;}" else "black;}"
                if (frameIndex <= 0) {
                    frameIndex = 56
                } else if (frameIndex > 100) {
                    frameIndex = 100
                }
                sbI = StringBuilder()
                sbI.append("<!DOCTYPE html><html dir='auto'><head><meta charset=\"utf-8\"/><meta property='og:url' content='https://www.youtube.com/watch?v=sb_read_")
                sbI.append(head)
                sbI.append("'>")
                if (withVideo) {
                    sbI.append("<meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'/><style>")
                    if (AppState.darkUi) {
                        sbI.append("::selection{background:#1976d2;color:white;}")
                    } else {
                        sbI.append("::selection{background:#b3e5fc;color:black;}")
                    }
                    sbI.append("font{background:transparent !important;box-shadow:none !important;}@font-face{font-family:soul_user_font;src:url(\"soul_user_font.ttf\")}body,body *:not([class*=\"icon\"]):not([class^=\"fa\"]):not(ion-icon){font-family:soul_user_font !important;}")
                    if (MainUtil.isFullScreenOff()) {
                        sbI.append("body{margin:0;padding:48px 0 80px 0;}")
                    } else {
                        sbI.append("body{margin:0;padding:20px 0 80px 0;}")
                    }
                    sbI.append("img{padding:0 16px 0 16px;margin:0 auto 20px auto;display:block;max-width:100%;height:auto;user-select:none;}iframe{margin:40px auto 40px auto;display:block;width:100vw;height:")
                    sbI.append(frameIndex)
                    sbI.append("vw;user-select:none;}p{padding:0 16px 0 16px;margin:0 auto 32px auto;line-height:1.6;word-wrap:break-word;font-size:18px;color:")
                    sbI.append(textColor)
                    sbI.append("</style>")
                    if (ReadSettings.fontBold.value) {
                        sbI.append("<style id='sb_bold_style'>body,body *:not([class*=\"icon\"]):not([class^=\"fa\"]):not(ion-icon){font-weight:bold !important;}</style>")
                    }
                    sbI.append("</head><body>")
                } else {
                    sbI.append("<meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes'/>")
                    if (ZoneSettings.userFont.value || ZoneSettings.fontBold.value) {
                        sbI.append("<style id='sb_font_style'>")
                        if (ZoneSettings.userFont.value) {
                            sbI.append("@font-face{font-family:soul_user_font;src:url(\"soul_user_font.ttf\")}")
                        }
                        sbI.append("body,body *:not([class*=\"icon\"]):not([class^=\"fa\"]):not(ion-icon){")
                        if (ZoneSettings.userFont.value) {
                            sbI.append("font-family:soul_user_font !important;")
                        }
                        if (ZoneSettings.fontBold.value) {
                            sbI.append("font-weight:bold !important;")
                        }
                        sbI.append("}</style>")
                    }
                    sbI.append("<style>")
                    if (MainUtil.isFullScreenOff()) {
                        sbI.append("body{margin:0;padding:48px 0 40px 0;}")
                    } else {
                        sbI.append("body{margin:0;padding:20px 0 40px 0;}")
                    }
                    sbI.append("img{padding:0 16px 0 16px;margin:0 auto 20px auto;display:block;max-width:100%;height:auto;}iframe{margin:40px auto 40px auto;display:block;width:100vw;height:")
                    sbI.append(frameIndex)
                    sbI.append("vw;}p{padding:0 16px 0 16px;margin:0 auto 32px auto;line-height:1.6;word-wrap:break-word;font-size:18px;}</style></head><body>")
                }
            } else {
                sbI = htmlHead(false, firstLoad)
            }
            val size = arrayList.size
            var newIndex = 0
            var index = 0
            var first = true
            while (index < size) {
                val rawReadItem: Any? = arrayList.get(index)
                index++
                val readItem = rawReadItem as ReadItem?
                if (readItem != null) {
                    val itemKind = readItem.itemKind
                    if (itemKind.toInt() != 1 && itemKind.toInt() != 3) {
                        first = inList
                    }
                    if (itemKind.toInt() == 1) {
                        if (!TextUtils.isEmpty(readItem.articleTitle)) {
                            val textColor = if (AppState.darkUi) "white;'>" else "black;'>"
                            readItem.index = newIndex
                            sbI.append("<p id='")
                            sbI.append(newIndex)
                            if (dark) {
                                sbI.append("'style='padding:0 16px 0 16px;margin:0 auto 20px auto;line-height:1.4;word-wrap:break-word;font-size:24px;font-weight:bold;color:")
                            } else {
                                sbI.append("'style='margin:0 auto 20px auto;line-height:1.4;word-wrap:break-word;font-size:24px;font-weight:bold;color:")
                            }
                            sbI.append(textColor)
                            sbI.append(readItem.articleTitle!!.replace("\n", "<br>"))
                            sbI.append("</p>")
                            newIndex++
                        }
                    } else if (itemKind.toInt() == 2) {
                        if (!TextUtils.isEmpty(readItem.articleTitle)) {
                            readItem.index = newIndex
                            sbI.append("<p id='")
                            sbI.append(newIndex)
                            if (readItem.isTitle) {
                                val textColor = if (AppState.darkUi) "white;'>" else "black;'>"
                                if (dark) {
                                    sbI.append("'style='padding:0 16px 0 16px;margin:0 auto 0 auto;line-height:1.6;word-wrap:break-word;font-size:18px;color:")
                                } else {
                                    sbI.append("'style='margin:0 auto 0 auto;line-height:1.6;word-wrap:break-word;font-size:18px;color:")
                                }
                                sbI.append(textColor)
                            } else {
                                sbI.append("'>")
                            }
                            sbI.append(readItem.articleTitle!!.replace("\n", "<br>"))
                            sbI.append("</p>")
                            newIndex++
                        }
                    } else if (itemKind.toInt() == 3) {
                        if (!TextUtils.isEmpty(readItem.subTitle)) {
                            readItem.subTitle = readItem.subTitle!!.replace("http://", "https://")
                            if (first) {
                                if (dark) {
                                    sbI.append("<img style='padding:0 16px 0 16px;margin:0 auto 24px auto;display:block;max-width:100%;width:100%;height:auto;user-select:none;' src='")
                                } else {
                                    sbI.append("<img style='margin:0 auto 24px auto;display:block;max-width:100%;width:100%;height:auto;user-select:none;' src='")
                                }
                                first = false
                            } else {
                                sbI.append("<img src='")
                            }
                            sbI.append(readItem.subTitle)
                            sbI.append("' onerror=\"this.style.display='none';\"></img>")
                        }
                    } else if (itemKind.toInt() == 4 && !TextUtils.isEmpty(readItem.imageText)) {
                        sbI.append("<iframe src='")
                        sbI.append(readItem.imageText)
                        sbI.append("' frameborder='0'></iframe>")
                    }
                    inList = false
                }
            }
            sbI.append("</body></html>")
            return sbI.toString()
        }

        @JvmStatic
        fun ogUrl(document: Document?): String? {
            if (document == null) {
                return null
            }
            try {
                val elementsSelect = document.select("meta[property=og:url]")
                if (elementsSelect.size != 0) {
                    val ogContent = elementsSelect.attr("content")
                    if (URLUtil.isNetworkUrl(ogContent)) {
                        return ogContent
                    }
                }
            } catch (unused: Exception) {
            }
            return null
        }

        @JvmStatic
        fun pictureImage(document: Document?): String? {
            if (document == null) {
                return null
            }
            try {
                val elementsSelect = document.select("picture")
                if (elementsSelect.size != 0) {
                    val size = elementsSelect.size
                    var index = 0
                    while (index < size) {
                        val rawElement: Any? = elementsSelect.get(index)
                        index++
                        val element = rawElement as Element?
                        if (element != null) {
                            val childImage: String? = firstChildImage(element)
                            if (!TextUtils.isEmpty(childImage)) {
                                return childImage
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
            }
            return null
        }

        @JvmStatic
        fun posterUrl(element: Element?): String? {
            val attributes: Attributes? = if (element == null) null else element.attributes()
            if (element == null || attributes == null) {
                return null
            }
            for (attribute in attributes) {
                if ("poster" == attribute.key) {
                    val parsedValue = MainUtil.parseFontList(attribute.value)
                    if (Companion.isGoodImage(parsedValue!!)) {
                        return parsedValue
                    }
                }
            }
            return null
        }

        @JvmStatic
        fun filterItems(arrayList: ArrayList<ReadItem?>?): ArrayList<ReadItem?>? {
            var itemKind: Byte
            var kept: ArrayList<ReadItem?>? = null
            if (arrayList != null && !arrayList.isEmpty()) {
                val size = arrayList.size
                var index = 0
                while (index < size) {
                    val rawReadItem: Any? = arrayList.get(index)
                    index++
                    val readItem = rawReadItem as ReadItem?
                    if (readItem != null && ((readItem.itemKind.also {
                            itemKind = it
                        }).toInt() == 1 || itemKind.toInt() == 2)) {
                        if (!TextUtils.isEmpty(readItem.articleTitle)) {
                            if (kept == null) {
                                kept = ArrayList()
                            }
                            kept.add(readItem)
                        }
                    }
                }
            }
            return kept
        }

        @JvmStatic
        fun jsonLd(document: Document?): JSONObject? {
            var jsonText: String? = null
            val elementsSelect: Elements? = if (document == null) null else document.select("script")
            if (document == null || elementsSelect == null || elementsSelect.size == 0) {
                return null
            }
            val size = elementsSelect.size
            var index = 0
            while (index < size) {
                val rawElement: Any? = elementsSelect.get(index)
                index++
                val element = rawElement as Element?
                if (element != null) {
                    jsonText = element.html()
                    if (!TextUtils.isEmpty(jsonText) && jsonText.startsWith("var ytInitialPlayerResponse = {\"responseContext")) {
                        jsonText = jsonText.substring(30)
                        break
                    }
                }
            }
            return parseJson(jsonText)
        }

        @JvmStatic
        fun isGoodImage(dir: String?): Boolean {
            if (!URLUtil.isNetworkUrl(dir) || dir!!.endsWith("icon.png") || dir.endsWith("logo.png") || dir.endsWith(
                    "svg"
                ) || dir.startsWith("https://news.google.com") || dir.startsWith("https://imgmo.seoul.co.kr/img//mexpand")
            ) {
                return false
            }
            val parentPath = MainUtil.parentPathOf(null, dir)
            if (parentPath == null || parentPath.length >= 10) {
                return true
            }
            return if (parentPath.contains("icon") || parentPath.contains("logo")) false else true
        }
    }
}
