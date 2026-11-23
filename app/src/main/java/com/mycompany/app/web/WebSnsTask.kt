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
import com.mycompany.app.web.WebSnsTask.SnsTaskListener
import org.json.JSONObject
import org.json.JSONArray
import com.mycompany.app.main.MainDownAdapter.DownListItem
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
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.LayoutInflater
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import com.mycompany.app.web.WebSearchAdapter2
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

@Suppress("deprecation")
class WebSnsTask {
    @JvmField
    var snsMode: Byte = 0
    @JvmField
    var taskListener: SnsTaskListener? = null
    @JvmField
    var loadTask: LoadTask? = null

    class LoadTask(webSnsTask: WebSnsTask?, baseUrl: String?, html: String?) : MyAsyncTask() {
        @JvmField
        var taskRef: WeakReference<*>? = null
        @JvmField
        var baseUrl: String? = null
        @JvmField
        var html: String? = null
        @JvmField
        var links: ArrayList<DownListItem?>? = null

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(webSnsTask)
            this.taskRef = weakReference
            if ((weakReference.get() as WebSnsTask?) == null) {
                return@init
            }
            this.baseUrl = baseUrl
            this.html = html

        }
    }

        public override fun doInBackground() {
            val webSnsTask = this.taskRef!!.get() as WebSnsTask?
            if (webSnsTask == null || webSnsTask.loadTask == null) {
                return
            }
            try {
                val document = Jsoup.parse(this.html!!, this.baseUrl!!)
                val snsMode = webSnsTask.snsMode
                if (snsMode.toInt() == 0) {
                    this.links = webSnsTask.parseInsta(document)
                } else if (snsMode.toInt() == 1) {
                    this.links = webSnsTask.parseTwitter(document)
                } else {
                    this.links = webSnsTask.parseVideoLinks(document)
                }
            } catch (unused: Exception) {
            } catch (unused: LinkageError) {
            } catch (unused: OutOfMemoryError) {
            }
        }

        public override fun onCancelled() {
            val webSnsTask = this.taskRef!!.get() as WebSnsTask?
            if (webSnsTask == null) {
                return
            }
            webSnsTask.loadTask = null
            this.baseUrl = null
            this.html = null
            this.links = null
        }

        public override fun onPostExecute() {
            val task = this.taskRef!!.get() as WebSnsTask?
            if (task == null) {
                return
            }
            task.loadTask = null
            if (task.taskListener != null) {
                task.taskListener!!.onResult(this.links)
            }
            this.baseUrl = null
            this.html = null
            this.links = null
        }
    }

    interface SnsTaskListener {
        /** The scraped video variants (null or empty when nothing was found).  */
        fun onResult(arrayList: ArrayList<DownListItem?>?)
    }

    class SortSize : Comparator<DownListItem?> {
        override fun compare(downListItem: DownListItem?, other: DownListItem?): Int {
            var quality: Int = 0
            val left = downListItem
            val right = other
            if (left == null) {
                return if (right == null) 0 else 1
            }
            val index = if (right == null) 0 else left.quality
            if (right == null || index < (right.quality.also { quality = it })) {
                return -1
            }
            return if (index > quality) 1 else 0
        }
    }

    /**
     * Download-helper pages: ".mp4" links. Mode (this.a) 2 lists every link; 3 returns the HD link (else SD, else the
     * last one); 4 the SD link (else HD, else the last one).
     */
    fun parseVideoLinks(document: Document?): ArrayList<DownListItem?>? {
        val mode = this.snsMode
        if (document == null || this.loadTask == null) {
            return null
        }
        var list: ArrayList<DownListItem?>? = null
        try {
            val links = document.select("a[href*='.mp4']")
            var url: String? = null
            var hd: String? = null
            var sd: String? = null
            var index = 0
            if (links.size != 0) {
                for (link in links) {
                    if (this.loadTask == null) {
                        return null
                    }
                    val href = link.attr("href")
                    if (TextUtils.isEmpty(href)) {
                        continue
                    }
                    url = href
                    if (url.endsWith("&dl=1") && url.length > 5) {
                        val trimmed = url.substring(0, url.length - 5)
                        if (!TextUtils.isEmpty(trimmed)) {
                            url = trimmed
                        }
                    }
                    val title = link.attr("title")
                    var label: String? = null
                    if (!TextUtils.isEmpty(title)) {
                        if (title.contains("HD")) {
                            hd = url
                            if (mode.toInt() == 3) {
                                break
                            }
                            label = "HD"
                        } else if (title.contains("SD")) {
                            sd = url
                            if (mode.toInt() == 4) {
                                break
                            }
                            label = "SD"
                        }
                    }
                    if (mode.toInt() == 2) {
                        if (TextUtils.isEmpty(label)) {
                            label = "" + index
                        }
                        val item = DownListItem(index, label, "MP4")
                        item.url = url
                        if (list == null) {
                            list = ArrayList()
                        }
                        list.add(item)
                        index++
                    }
                }
            }
            if (this.loadTask == null) {
                return null
            }
            val pick: String?
            val pickLabel: String?
            if (mode.toInt() == 3) {
                pick = if (!TextUtils.isEmpty(hd)) hd else if (!TextUtils.isEmpty(sd)) sd else url
                pickLabel = "HD"
            } else if (mode.toInt() == 4) {
                pick = if (!TextUtils.isEmpty(sd)) sd else if (!TextUtils.isEmpty(hd)) hd else url
                pickLabel = "SD"
            } else {
                return list
            }
            if (TextUtils.isEmpty(pick)) {
                return list
            }
            val item = DownListItem(0, pickLabel, "MP4")
            item.url = pick
            if (list == null) {
                list = ArrayList()
            }
            list.add(item)
            return list
        } catch (unused: Exception) {
            return list
        }
    }

    fun parseTwitter(document: Document?): ArrayList<DownListItem?>? {
        var items: ArrayList<DownListItem?>? = null
        var element: Element? = null
        var url: String? = null
        var downListItem: DownListItem? = null
        if (document != null && this.loadTask != null) {
            val mp4Link: Element?
            try {
                mp4Link = document.selectFirst("a[href*='.mp4']")
            } catch (exception: Exception) {
                return null
            }

            var foundUrl: String?
            if (mp4Link != null) {
                try {
                    if (this.loadTask == null) {
                        return null
                    }

                    foundUrl = mp4Link.attr("href")
                } catch (exception2: Exception) {
                    return null
                }
            } else {
                foundUrl = null
            }

            var videoUrl = foundUrl

            run label140@{
                try {
                    if (!TextUtils.isEmpty(foundUrl)) {
                        return@label140
                    }

                    element = document.selectFirst("source[src*='.mp4']")
                } catch (exception3: Exception) {
                    return null
                }
                if (element != null) {
                    try {
                        if (this.loadTask == null) {
                            return null
                        }

                        foundUrl = element.attr("src")
                    } catch (exception4: Exception) {
                        return null
                    }
                }

                try {
                    if (TextUtils.isEmpty(foundUrl)) {
                        return null
                    }
                } catch (exception5: Exception) {
                    return null
                }
                videoUrl = foundUrl
            }

            var plainUrl = videoUrl

            run label141@{
                try {
                    if (!videoUrl!!.endsWith("&dl=1")) {
                        return@label141
                    }
                } catch (exception6: Exception) {
                    return null
                }
                plainUrl = videoUrl

                try {
                    if (videoUrl.length <= 5) {
                        return@label141
                    }

                    url = videoUrl.substring(0, videoUrl.length - 5)
                } catch (exception7: Exception) {
                    return null
                }

                plainUrl = videoUrl

                try {
                    if (TextUtils.isEmpty(url)) {
                        return@label141
                    }
                } catch (exception8: Exception) {
                    return null
                }
                plainUrl = url
            }

            val host: String?
            try {
                downListItem = DownListItem(0, "Video", "MP4")
                host = MainUtil.hostOf(plainUrl!!, true)
            } catch (exception9: Exception) {
                return null
            }

            var newUrl = plainUrl

            run label142@{
                try {
                    if (TextUtils.isEmpty(host)) {
                        return@label142
                    }
                } catch (exception10: Exception) {
                    return null
                }
                newUrl = plainUrl

                val start: Int
                try {
                    if (!host!!.contains("indown")) {
                        return@label142
                    }

                    start = plainUrl.indexOf("http", 10)
                } catch (exception11: Exception) {
                    return null
                }

                newUrl = plainUrl
                if (start >= 10) {
                    try {
                        newUrl = URLDecoder.decode(plainUrl.substring(start), "UTF-8")
                    } catch (exception12: Exception) {
                        newUrl = plainUrl
                    }
                }
            }

            try {
                downListItem.url = newUrl
                items = ArrayList()
            } catch (exception13: Exception) {
                return null
            }

            try {
                items.add(downListItem)
            } catch (exception14: Exception) {
            }

            return items
        } else {
            return null
        }
    }

    fun parseInsta(document: Document?): ArrayList<DownListItem?>? {
        var arrayList: ArrayList<DownListItem?>?
        var title: String?
        var quality: Int
        var found: ArrayList<DownListItem?>? = null
        title = null
        var newThumb: String? = null
        if (document != null && this.loadTask != null) {
            try {
                val elementsSelect = document.select("a[href*='.mp4']")
                var newIndex = 0
                if (elementsSelect.size == 0) {
                    arrayList = null
                } else {
                    val size = elementsSelect.size
                    arrayList = null
                    var best = 0
                    var index = 0
                    while (index < size) {
                        try {
                            val rawElement: Any? = elementsSelect.get(index)
                            index++
                            val element = rawElement as Element?
                            if (this.loadTask != null) {
                                if (element != null) {
                                    val linkHref = element.attr("href")
                                    if (!TextUtils.isEmpty(linkHref)) {
                                        val downListItemI: DownListItem? = itemFromPath(linkHref)
                                        if (downListItemI != null) {
                                            title = downListItemI.title
                                            quality = downListItemI.quality
                                        } else {
                                            title = null
                                            quality = best
                                        }
                                        val downListItem = DownListItem(best, title, "MP4")
                                        downListItem.url = linkHref
                                        downListItem.quality = quality
                                        if (arrayList == null) {
                                            arrayList = ArrayList()
                                        }
                                        arrayList.add(downListItem)
                                        best++
                                    }
                                }
                            }
                        } catch (unused: Exception) {
                            found = arrayList
                            return found
                        }
                    }
                }
                if (this.loadTask != null) {
                    if (arrayList == null || arrayList.isEmpty()) {
                        return arrayList
                    }
                    val images = document.select("img[src*='http']")
                    if (images.size != 0) {
                        val size = images.size
                        var thumb: String? = null
                        var index = 0
                        while (index < size) {
                            val rawElement: Any? = images.get(index)
                            index++
                            val element = rawElement as Element?
                            if (this.loadTask != null) {
                                if (element != null) {
                                    for (attribute in element.attributes()) {
                                        val value = attribute.value
                                        if (URLUtil.isNetworkUrl(value) && (value.contains(".jpg") || value.contains(
                                                ".png"
                                            ))
                                        ) {
                                            thumb = value
                                            break
                                        }
                                    }
                                }
                            }
                        }
                        if (this.loadTask != null) {
                            newThumb = thumb
                        }
                    }
                    try {
                        Collections.sort<DownListItem?>(arrayList, SortSize())
                    } catch (unused2: Exception) {
                    }
                    val size = arrayList.size
                    var index = 0
                    while (index < size) {
                        val entry: Any? = arrayList.get(index)
                        index++
                        val downItem = entry as DownListItem?
                        if (downItem != null) {
                            downItem.index = newIndex
                            if (TextUtils.isEmpty(downItem.title)) {
                                downItem.title = "" + newIndex
                            }
                            downItem.thumb = newThumb
                            newIndex++
                        }
                    }
                    return arrayList
                }
            } catch (unused3: Exception) {
            }
        }
        return null
    }

    companion object {
        @JvmStatic
        fun flattenArray(array: JSONArray?): JSONArray? {
            if (array == null) {
                return null
            }
            val length = array.length()
            for (index in 0..<length) {
                try {
                    try {
                        val nestedMedia: JSONArray? = findMediaArray(array.getJSONObject(index))
                        if (nestedMedia != null) {
                            return nestedMedia
                        }
                    } catch (unused: Exception) {
                        val flattened: JSONArray? = flattenArray(array.getJSONArray(index))
                        if (flattened != null) {
                            return flattened
                        }
                    }
                } catch (unused2: Exception) {
                }
            }
            return null
        }

        @JvmStatic
        fun findMediaArray(json: JSONObject?): JSONArray? {
            var child: JSONObject?
            var childArray: JSONArray?
            if (json == null) {
                return null
            }
            try {
                val itKeys = json.keys()
                if (itKeys == null) {
                    return null
                }
                while (itKeys.hasNext()) {
                    val next = itKeys.next()
                    try {
                        child = json.getJSONObject(next)
                    } catch (unused: Exception) {
                        child = null
                    }
                    if (child == null) {
                        try {
                            childArray = json.getJSONArray(next)
                        } catch (unused2: Exception) {
                            childArray = null
                        }
                        if (childArray == null) {
                            continue
                        } else {
                            if ("items" == next) {
                                return childArray
                            }
                            val flat: JSONArray? = flattenArray(childArray)
                            if (flat != null) {
                                return flat
                            }
                        }
                    } else {
                        if ("items" == next) {
                            return null
                        }
                        val found: JSONArray? = findMediaArray(child)
                        if (found != null) {
                            return found
                        }
                    }
                }
            } catch (unused3: Exception) {
            }
            return null
        }

        /** Instagram item `index`: MP4 variants and DASH variants, plus the smallest .jpg as thumbnail.  */
        @JvmStatic
        fun mediaItems(items: JSONArray?, index: Int): ArrayList<DownListItem?>? {
            var list: ArrayList<DownListItem?>?
            try {
                val mp: ArrayList<DownListItem?>? = mp4Items(items, index)
                val dash: ArrayList<DownListItem?>? = dashItems(items, index)
                if (mp == null) {
                    list = dash
                } else if (dash == null || dash.isEmpty()) {
                    list = mp
                } else if (mp.size == 1) {
                    // a single MP4 gets the best DASH quality label; the DASH list is what gets returned
                    (mp.get(0) as DownListItem).title = (dash.get(dash.size - 1) as DownListItem).title
                    list = dash
                } else {
                    mp.addAll(dash)
                    list = mp
                }
            } catch (unused: Exception) {
                list = null
            }
            if (list == null || list.isEmpty()) {
                return null
            }
            var thumb: String? = null
            try {
                val candidates = items!!.getJSONObject(index).getJSONObject("image_versions2")
                    .getJSONArray("candidates")
                val length = candidates.length()
                var bestArea = 0
                for (k in 0..<length) {
                    var candidate: JSONObject?
                    try {
                        candidate = candidates.getJSONObject(k)
                    } catch (unused2: Exception) {
                        candidate = null
                    }
                    if (candidate == null) {
                        continue
                    }
                    var width: Int
                    var height: Int
                    try {
                        width = candidate.getInt("width")
                    } catch (unused3: Exception) {
                        width = 0
                    }
                    try {
                        height = candidate.getInt("height")
                    } catch (unused4: Exception) {
                        height = 0
                    }
                    val area = if (width > 0 && height > 0) width * height else 0
                    if (area == 0 || (bestArea != 0 && area >= bestArea)) {
                        continue
                    }
                    var url: String?
                    try {
                        url = candidate.getString(ImagesContract.URL)
                    } catch (unused5: Exception) {
                        url = null
                    }
                    if (URLUtil.isNetworkUrl(url) && url!!.contains(".jpg")) {
                        thumb = url
                        bestArea = area
                    }
                }
            } catch (unused6: Exception) {
            }
            if (URLUtil.isNetworkUrl(thumb)) {
                for (item2 in list) {
                    val item = item2
                    if (item != null) {
                        item.thumb = thumb
                    }
                }
            }
            return list
        }

        @JvmStatic
        fun mp4Items(items: JSONArray?, indexValue: Int): ArrayList<DownListItem?>? {
            var versions: JSONArray?
            var arrayList: ArrayList<DownListItem?>?
            var version: JSONObject?
            var videoUrl: String?
            var videoWidth: Int
            var videoHeight: Int
            var title: String?
            var quality: Int
            try {
                versions = items!!.getJSONObject(indexValue).getJSONArray("video_versions")
            } catch (unused: Exception) {
                versions = null
            }
            if (versions != null) {
                try {
                    val length = versions.length()
                    arrayList = null
                    var best = 0
                    for (index3 in 0..<length) {
                        try {
                            version = versions.getJSONObject(index3)
                        } catch (unused2: Exception) {
                            version = null
                        }
                        if (version != null) {
                            try {
                                videoUrl = version.getString(ImagesContract.URL)
                            } catch (unused3: Exception) {
                                videoUrl = null
                            }
                            try {
                                if (URLUtil.isNetworkUrl(videoUrl) && videoUrl!!.contains(".mp4")) {
                                    try {
                                        videoWidth = version.getInt("width")
                                    } catch (unused4: Exception) {
                                        videoWidth = 0
                                    }
                                    try {
                                        videoHeight = version.getInt("height")
                                    } catch (unused5: Exception) {
                                        videoHeight = 0
                                    }
                                    if (videoWidth <= 0 || videoHeight <= 0) {
                                        title = null
                                        quality = best
                                    } else {
                                        title = videoWidth.toString() + "x" + videoHeight
                                        var dup = false
                                        if (arrayList != null && !arrayList.isEmpty()) {
                                            try {
                                                val size = arrayList.size
                                                for (index in 0..<size) {
                                                    val downListItem =
                                                        arrayList.get(index)
                                                    if (downListItem != null && title == downListItem.title) {
                                                        dup = true
                                                        break
                                                    }
                                                }
                                            } catch (unused6: Exception) {
                                            }
                                        }
                                        if (dup) {
                                            continue
                                        }
                                        quality = videoWidth * videoHeight
                                    }
                                    val item = DownListItem(best, title, "MP4")
                                    item.url = videoUrl
                                    item.quality = quality
                                    if (arrayList == null) {
                                        arrayList = ArrayList()
                                    }
                                    arrayList.add(item)
                                    best++
                                }
                            } catch (unused7: Exception) {
                            }
                        }
                    }
                } catch (unused8: Exception) {
                    arrayList = null
                }
                if (arrayList != null && !arrayList.isEmpty()) {
                    try {
                        Collections.sort<DownListItem?>(arrayList, SortSize())
                    } catch (unused9: Exception) {
                    }
                    return arrayList
                }
            }
            return null
        }

        /**
         * Instagram item `indexValue`: video variants from its DASH manifest (labelled by FBQualityLabel, e.g. "720p");
         * each needs the separate audio track merged in.
         */
        @JvmStatic
        fun dashItems(items: JSONArray?, indexValue: Int): ArrayList<DownListItem?>? {
            var manifest: String?
            try {
                manifest = items!!.getJSONObject(indexValue).getString("video_dash_manifest")
            } catch (unused: Exception) {
                try {
                    manifest =
                        items!!.getJSONObject(indexValue).getJSONArray("carousel_media").getJSONObject(0)
                            .getString("video_dash_manifest")
                } catch (unused2: Exception) {
                    manifest = null
                }
            }
            if (TextUtils.isEmpty(manifest)) {
                return null
            }
            var doc: Document?
            try {
                doc = Jsoup.parse(manifest!!, Parser.xmlParser())
            } catch (unused3: OutOfMemoryError) {
                doc = null
            } catch (unused3: LinkageError) {
                doc = null
            } catch (unused3: Exception) {
                doc = null
            }
            if (doc == null) {
                return null
            }
            var videos: ArrayList<DownListItem?>? = null
            var audioUrl: String? = null
            try {
                val reps = doc.select("[mimeType*='mp4']")
                if (reps.size == 0) {
                    return null
                }
                var index = 0
                for (rep in reps) {
                    val children = rep.children()
                    if (children.size == 0) {
                        continue
                    }
                    val audio = "audio/mp4" == rep.attr("mimeType")
                    var label: String? = null
                    var quality = 0
                    if (!audio) {
                        label = rep.attr("FBQualityLabel")
                        if (label.length < 2) {
                            continue
                        }
                        quality = parseIntSafe(label.substring(0, label.length - 1))
                    }
                    for (child in children) {
                        if ("BaseURL" != child.tagName()) {
                            continue
                        }
                        var url = child.html()
                        if (TextUtils.isEmpty(url)) {
                            continue
                        }
                        url = url.replace("amp;", "")
                        if (TextUtils.isEmpty(url)) {
                            continue
                        }
                        if (audio) {
                            audioUrl = url
                        } else {
                            val item = DownListItem(index, label, "MP4")
                            item.url = url
                            item.quality = quality
                            item.isHidden = true
                            if (videos == null) {
                                videos = ArrayList()
                            }
                            videos.add(item)
                            index++
                        }
                        break
                    }
                }
            } catch (unused4: Exception) {
            }
            if (videos == null || videos.isEmpty()) {
                return null
            }
            if (!TextUtils.isEmpty(audioUrl)) {
                for (item2 in videos) {
                    val item = item2
                    if (item != null && item.isHidden) {
                        item.isHidden = false
                        item.audioUrl = audioUrl
                    }
                }
            }
            try {
                Collections.sort<DownListItem?>(videos, SortSize())
            } catch (unused5: Exception) {
            }
            return videos
        }

        @JvmStatic
        fun itemFromPath(link: String?): DownListItem? {
            val pathParts: Array<String?>? = if (TextUtils.isEmpty(link)) null else link!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (TextUtils.isEmpty(link) || pathParts == null || pathParts.size == 0) {
                return null
            }
            for (length in pathParts.indices.reversed()) {
                val title = pathParts[length]
                val titleColor = if (!TextUtils.isEmpty(title) && MainUtil.hasChar(120, title)) parseColorSafe(
                        title!!.replace(
                            "x",
                            ""
                        )
                    ) else 0
                if (!TextUtils.isEmpty(title) && MainUtil.hasChar(120, title) && titleColor != -1) {
                    val downListItem = DownListItem()
                    downListItem.title = title
                    downListItem.quality = titleColor
                    return downListItem
                }
            }
            return null
        }
    }
}
