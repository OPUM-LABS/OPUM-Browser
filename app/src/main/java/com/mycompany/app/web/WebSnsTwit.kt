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
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import kotlin.math.min

@Suppress("deprecation")
class WebSnsTwit {
    @JvmField
    var hostActivity: android.app.Activity? = null
    @JvmField
    var appContext: Context? = null
    @JvmField
    var taskListener: SnsTaskListener? = null
    @JvmField
    var parentView: ViewGroup? = null
    @JvmField
    var webSafe: MyWebSafe? = null
    @JvmField
    var pageLoading: Boolean = false
    @JvmField
    var pageUrl: String? = null
    @JvmField
    var matchKey1: String? = null
    @JvmField
    var matchKey2: String? = null
    @JvmField
    var matchKey3: String? = null
    @JvmField
    var matchKey4: String? = null
    @JvmField
    var urlLoading: Boolean = false
    @JvmField
    var intercepted: Boolean = false
    @JvmField
    var jsonText: String? = null
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var tweetOnly: Boolean = false
    @JvmField
    var wantedTweetId: String? = null
    @JvmField
    var mediaPath: String? = null
    @JvmField
    var lastPath: String? = null
    @JvmField
    var lastIndex: String? = null
    @JvmField
    var itemCount: Int = 0
    @JvmField
    var urlSeen: Boolean = false
    @JvmField
    var mediaSeen: Boolean = false
    @JvmField
    /** Per item, the media links found for it. */
    var mediaItems: ArrayList<ArrayList<String?>?>? = null
    @JvmField
    var thumbItems: ArrayList<String?>? = null

    internal inner class CreateWebTask : Runnable {
        override fun run() {
            val webSnsTwit = this@WebSnsTwit
            if (webSnsTwit.hostActivity == null || webSnsTwit.parentView == null || webSnsTwit.webSafe != null) {
                return
            }
            val myWebSafe = MyWebSafe(webSnsTwit.hostActivity)
            webSnsTwit.webSafe = myWebSafe
            MainApp.resumeWebTimers(webSnsTwit.appContext, myWebSafe)
            webSnsTwit.webSafe!!.setVisibility(View.INVISIBLE)
            webSnsTwit.parentView!!.addView(webSnsTwit.webSafe, 0, ViewGroup.LayoutParams(-1, -1))
            webSnsTwit.parentView!!.post(webSnsTwit.LoadPageTask())
        }
    }

    internal inner class LoadPageTask : Runnable {

        internal inner class LoadPageStep : Runnable {

            internal inner class PrepareWebStep : Runnable {

                internal inner class StartLoadStep : Runnable {
                    override fun run() {
                        val webSnsTwit = this@WebSnsTwit
                        val myWebSafe = webSnsTwit.webSafe
                        if (myWebSafe == null) {
                            return
                        }
                        myWebSafe.loadUrl(webSnsTwit.pageUrl!!)
                    }
                }

                override fun run() {
                    val loadPageTask = this@LoadPageTask
                    val webSnsTwit = this@WebSnsTwit
                    val myWebSafe = webSnsTwit.webSafe
                    if (myWebSafe != null) {
                        webSnsTwit.urlLoading = true
                    }
                    val viewGroup = this@WebSnsTwit.parentView
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(StartLoadStep())
                }
            }

            override fun run() {
                val webSnsTwit = this@WebSnsTwit
                val myWebSafe = webSnsTwit.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(webSnsTwit.LocalWebViewClient())
                }
                val viewGroup = webSnsTwit.parentView
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(PrepareWebStep())
            }
        }

        override fun run() {
            val webSnsTwit = this@WebSnsTwit
            val myWebSafe = webSnsTwit.webSafe
            if (myWebSafe != null) {
                val settings = myWebSafe.getSettings()
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setDomStorageEnabled(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(true)
                settings.setJavaScriptEnabled(true)
                settings.setDatabaseEnabled(true)
                settings.setMixedContentMode(0)
                MainApp.setAcceptCookies(webSnsTwit.appContext, true)
                MainApp.setThirdPartyCookies(webSnsTwit.appContext, myWebSafe, true)
            }
            val viewGroup = webSnsTwit.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.post(LoadPageStep())
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val webSnsTwit = this@WebSnsTwit
            MainUtil.clearWebCache(webSnsTwit.webSafe)
            webSnsTwit.finishHandled = false
        }
    }

    internal inner class ReportResultTask : Runnable {
        override fun run() {
            val snsTaskListener = this@WebSnsTwit.taskListener
            if (snsTaskListener != null) {
                (snsTaskListener as WebSnsLoad.ReleaseStep).onResult(null)
            }
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val webSnsTwit = this@WebSnsTwit
            val myWebSafe = webSnsTwit.webSafe
            if (myWebSafe == null) {
                return
            }
            webSnsTwit.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val webSnsTwit = this@WebSnsTwit
            val myWebSafe = webSnsTwit.webSafe
            if (myWebSafe == null) {
                return
            }
            webSnsTwit.urlLoading = true
        }
    }

    internal inner class ParseTweetTask : Runnable {
        /**
         * Parses the tweet JSON captured by shouldInterceptRequest. First pass finds the tweet node ("rest_id" equal to
         * the tweet id, which sets s); a second pass restarts inside that node when the first pass collected media from
         * elsewhere. Videos (grouped per media entry) become MP4 items with the matching thumbnail.
         */
        override fun run() {
            val tw = this@WebSnsTwit
            val json = tw.jsonText
            tw.jsonText = null
            if (tw.webSafe == null) {
                return
            }
            var tweetId: String? = null
            val url = tw.pageUrl
            if (url != null && url.length != 0) {
                val slash = url.lastIndexOf('/')
                if (slash > 0 && slash + 1 < url.length) {
                    tweetId = url.substring(slash + 1)
                }
            }
            var result: ArrayList<DownListItem?>? = null
            if (!TextUtils.isEmpty(tweetId) && !TextUtils.isEmpty(json)) {
                tw.tweetOnly = true
                tw.wantedTweetId = tweetId
                tw.mediaPath = null
                tw.lastPath = null
                tw.lastIndex = null
                tw.itemCount = 0
                tw.urlSeen = false
                tw.mediaSeen = false
                tw.mediaItems = null
                tw.thumbItems = null
                try {
                    val root = JSONObject(json!!)
                    tw.visitNode(null, root, false, "", "", null)
                    tw.tweetOnly = false
                    tw.wantedTweetId = null
                    tw.urlSeen = false
                    tw.mediaSeen = false
                    if (TextUtils.isEmpty(tw.mediaPath)) {
                        if (tw.mediaItems == null) {
                            tw.lastPath = null
                            tw.lastIndex = null
                            tw.mediaItems = null
                            tw.thumbItems = null
                            notifyDone(tw, null)
                            return
                        }
                    } else if (tw.mediaItems == null || TextUtils.isEmpty(tw.lastPath) || !tw.lastPath!!.startsWith(tw.mediaPath!!)) {
                        tw.lastPath = null
                        tw.lastIndex = null
                        tw.itemCount = 0
                        tw.mediaItems = null
                        tw.thumbItems = null
                        tw.visitNode(null, root, false, "", "", null)
                    }
                } catch (ignored: JSONException) {
                }
                var list: ArrayList<DownListItem?>? = null
                val groups = tw.mediaItems
                if (groups != null) {
                    try {
                        var group = 0
                        var idx = 0
                        for (o in groups) {
                            for (link in o!!) {
                                val info: DownListItem? = WebSnsTask.Companion.itemFromPath(link)
                                val label = if (info != null) info.title else null
                                val size = if (info != null) info.quality else idx
                                val item = DownListItem(idx, label, "MP4")
                                item.group = group
                                item.url = link
                                item.quality = size
                                var thumb: String? = null
                                val thumbs = tw.thumbItems
                                if (thumbs != null && thumbs.size != 0) {
                                    try {
                                        thumb = thumbs.get(min(group, thumbs.size - 1))
                                    } catch (ignored: Exception) {
                                    }
                                }
                                item.thumb = thumb
                                if (list == null) {
                                    list = ArrayList<DownListItem?>()
                                }
                                list.add(item)
                                idx++
                            }
                            group++
                        }
                        if (list != null && !list.isEmpty()) {
                            try {
                                Collections.sort<DownListItem?>(list, SortTwit())
                            } catch (ignored: Exception) {
                            }
                            val count = list.size
                            var prevGroup = 0
                            for (index in 0..<count) {
                                val item = list.get(index)
                                if (item == null) {
                                    continue
                                }
                                item.index = index
                                if (TextUtils.isEmpty(item.title)) {
                                    item.title = MainUtil.uriFileName(null, item.url)
                                    if (TextUtils.isEmpty(item.title)) {
                                        item.title = "" + index
                                    }
                                }
                                if (prevGroup != item.group) {
                                    prevGroup = item.group
                                }
                            }
                        } else {
                            list = null
                        }
                    } catch (ignored: Exception) {
                    }
                }
                tw.tweetOnly = false
                tw.wantedTweetId = null
                tw.mediaPath = null
                tw.lastPath = null
                tw.lastIndex = null
                tw.mediaItems = null
                tw.thumbItems = null
                result = list
            }
            notifyDone(tw, result)
        }

        private fun notifyDone(tw: WebSnsTwit?, list: ArrayList<DownListItem?>?) {
            if (tw!!.taskListener != null) {
                (tw.taskListener as WebSnsLoad.ReleaseStep).onResult(list)
            }
        }
    }

    class SortTwit : Comparator<DownListItem?> {
        override fun compare(downListItem: DownListItem?, other: DownListItem?): Int {
            var group: Int = 0
            val left = downListItem
            val right = other
            if (left == null) {
                return if (right == null) 0 else 1
            }
            val index = if (right == null) 0 else left.group
            if (right == null || index < (right.group.also { group = it })) {
                return -1
            }
            if (index > group) {
                return 1
            }
            val quality = left.quality
            val qualityNow = right.quality
            if (quality < qualityNow) {
                return -1
            }
            return if (quality > qualityNow) 1 else 0
        }
    }

    /**
     * Walks the JSON tree depth first; path is the parent's path and step this node's own segment ("/O:key", "/A:i").
     * For the keys of interest ("rest_id" in the first pass, "url", "media_url_https") the node is rendered as a short
     * text and handed to [matchItem]. Returns true to stop the walk.
     */
    fun visitNode(
        key: Any?,
        value: Any?,
        hasNext: Boolean,
        path: String?,
        step: String?,
        keyName: String?
    ): Boolean {
        if (!this.tweetOnly && !path!!.startsWith(this.mediaPath!!) && (this.mediaItems != null || this.thumbItems != null)) {
            return true
        }
        val isObj = value is JSONObject
        val nonEmpty = (isObj && value.length() != 0)
                || (value is JSONArray && value.length() != 0)
        var nodeKind = 0
        if (keyName != null) {
            if (this.tweetOnly && "rest_id" == keyName) {
                nodeKind = 1
            } else if (!this.urlSeen && "url" == keyName) {
                nodeKind = 2
            } else if (!this.mediaSeen && "media_url_https" == keyName) {
                nodeKind = 3
            }
        }
        if (nodeKind != 0) {
            val sb = StringBuilder()
            if (key is String) {
                sb.append("\"").append(key).append("\": ")
            }
            if (isObj) {
                sb.append("{")
                if (!nonEmpty) {
                    sb.append(toJson(value, hasNext))
                }
            } else if (value is JSONArray) {
                sb.append("[")
                if (!nonEmpty) {
                    sb.append(toJson(value, hasNext))
                }
            } else if (value != null) {
                if (value is String) {
                    sb.append("\"").append(value).append("\"")
                } else {
                    sb.append(value)
                }
                if (hasNext) {
                    sb.append(",")
                }
                sb.insert(0, "    ")
            }
            if (matchItem(nodeKind, path, sb.toString())) {
                return true
            }
        }
        if (!nonEmpty) {
            return false
        }
        val childPath = path + step
        if (isObj) {
            val obj = value
            val it = obj.keys()
            while (it.hasNext()) {
                val key2 = it.next()
                try {
                    if (visitNode(key2, obj.get(key2), it.hasNext(), childPath, "/O:" + key2, key2)) {
                        return true
                    }
                } catch (ignored: JSONException) {
                }
            }
        } else {
            val arr = value as JSONArray
            val len = arr.length()
            for (key3 in 0..<len) {
                try {
                    if (visitNode(key3, arr.get(key3), key3 + 1 < len, childPath, "/A:" + key3, null)) {
                        return true
                    }
                } catch (ignored: JSONException) {
                }
            }
        }
        return nodeKind != 0 && matchItem(nodeKind, path, toJson(value, hasNext))
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

    /**
     * Handles one rendered node. kind 1 (first pass): the node holding the tweet id marks the tweet path (s). kind 2:
     * a video variant url (".mp4"), grouped by its media index. kind 3: a thumbnail, padded so the
     * index matches the group. Returns true when the walk can stop (a repeat was seen after the other list filled).
     */
    fun matchItem(nodeKind: Int, path: String?, valueText: String?): Boolean {
        if (TextUtils.isEmpty(valueText)) {
            return false
        }
        if (this.tweetOnly && nodeKind == 1) {
            if (!valueText!!.contains(this.wantedTweetId!!)) {
                return false
            }
            this.mediaPath = path
            return true
        }
        try {
            if (TextUtils.isEmpty(this.mediaPath) || path!!.startsWith(this.mediaPath!!)) {
                collect(nodeKind, path, valueText!!)
            }
        } catch (ignored: Exception) {
        }
        if (this.tweetOnly) {
            return false
        }
        return (this.mediaSeen && this.mediaItems != null) || (this.urlSeen && this.thumbItems != null)
    }

    private fun collect(nodeKind: Int, path: String?, text: String?) {
        var prefix: String? = null
        if (!TextUtils.isEmpty(path)) {
            val at = path!!.indexOf("media/A:")
            if (at != -1) {
                prefix = path.substring(0, at + 8)
            }
        }
        if (TextUtils.isEmpty(prefix)) {
            return
        }
        if (!TextUtils.isEmpty(this.lastPath) && prefix != this.lastPath) {
            return
        }
        var index: String? = null
        try {
            val start = prefix!!.length
            val end = path!!.indexOf('/', start)
            index = path.substring(start, if (end == -1) path.length else end)
        } catch (ignored: Exception) {
        }
        if (TextUtils.isEmpty(index)) {
            return
        }
        val newItem = !TextUtils.isEmpty(this.lastIndex) && index != this.lastIndex
        this.lastPath = prefix
        this.lastIndex = index
        var link: String? = null
        try {
            val from = text!!.indexOf("https://")
            if (from != -1) {
                val to = text.indexOf('"', from + 8)
                if (to != -1) {
                    var found = text.substring(from, to)
                    if (!TextUtils.isEmpty(found)) {
                        while (found.endsWith("\\") && found.length > 1) {
                            found = found.substring(0, found.length - 1)
                        }
                        link = found
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        if (!URLUtil.isNetworkUrl(link)) {
            return
        }
        if (nodeKind == 2 && !link!!.contains(".mp4")) {
            return
        }
        if (newItem) {
            this.itemCount++
        }
        if (nodeKind == 3) {
            if (this.thumbItems == null) {
                this.thumbItems = ArrayList()
            } else if (this.thumbItems!!.contains(link)) {
                this.mediaSeen = true
                return
            }
            while (this.thumbItems!!.size < this.itemCount) {
                this.thumbItems!!.add("")
            }
            this.thumbItems!!.add(link)
            return
        }
        if (this.mediaItems == null) {
            this.mediaItems = ArrayList()
        } else {
            for (o in this.mediaItems) {
                for (u in o!!) {
                    if (!TextUtils.isEmpty(u) && link == u) {
                        this.urlSeen = true
                        return
                    }
                }
            }
        }
        while (this.mediaItems!!.size < this.itemCount + 1) {
            this.mediaItems!!.add(ArrayList())
        }
        val bucket = this.mediaItems!!.get(this.itemCount)
        if (bucket != null) {
            bucket.add(link)
        }
    }

    fun release() {
        val myWebSafe = this.webSafe
        if (myWebSafe != null) {
            if (this.pageLoading) {
                this.pageLoading = false
                myWebSafe.stopLoading()
            }
            destroyWebViewNow(this.webSafe)
            this.webSafe = null
        }
        MainApp.applyCookieSetting(this.appContext)
        this.hostActivity = null
        this.appContext = null
        this.taskListener = null
        this.parentView = null
        this.pageUrl = null
        this.matchKey1 = null
        this.matchKey2 = null
        this.matchKey3 = null
        this.matchKey4 = null
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val webSnsTwit = this@WebSnsTwit
            if (webSnsTwit.webSafe != null) {
                webSnsTwit.pageLoading = false
                webSnsTwit.loadUrl(url)
                if (webSnsTwit.finishHandled || TextUtils.isEmpty(url) || url == webSnsTwit.lastFinishedUrl) {
                    return
                }
                webSnsTwit.finishHandled = true
                webSnsTwit.lastFinishedUrl = url
                val viewGroup = webSnsTwit.parentView
                if (viewGroup != null) {
                    viewGroup.post(webSnsTwit.ClearCacheTask())
                }
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val webSnsTwit = this@WebSnsTwit
            if (webSnsTwit.webSafe == null) {
                return
            }
            webSnsTwit.pageLoading = true
            webSnsTwit.loadUrl(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val webSnsTwit = this@WebSnsTwit
            webSnsTwit.webSafe = null
            onRenderGone(webView, renderProcessGoneDetail)
            val viewGroup = webSnsTwit.parentView
            if (viewGroup == null) {
                return true
            }
            viewGroup.post(webSnsTwit.ReportResultTask())
            return true
        }

        /**
         * Watches for the tweet-detail API call (url and authorization header matching the native key strings),
         * fetches it once with the same headers, hands the JSON to ParseTweetTask and blanks the page's own request.
         */
        override fun shouldInterceptRequest(
            webView: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val tw = this@WebSnsTwit
            if (tw.webSafe == null || request == null || request.getUrl() == null) {
                return null
            }
            val url = request.getUrl().toString()
            tw.loadUrl(url)
            if (tw.intercepted || TextUtils.isEmpty(url) || TextUtils.isEmpty(tw.matchKey1) || TextUtils.isEmpty(tw.matchKey2)
                || TextUtils.isEmpty(tw.matchKey3) || TextUtils.isEmpty(tw.matchKey4) || !url.contains(tw.matchKey1!!) || !(url.contains(
                    tw.matchKey2!!
                ) || url.contains(tw.matchKey3!!))
            ) {
                return null
            }
            val headers = request.getRequestHeaders()
            if (headers == null) {
                return null
            }
            var authorized = false
            for (name in headers.keys) {
                val value = headers.get(name)
                if (!TextUtils.isEmpty(value) && value!!.startsWith(tw.matchKey4!!)) {
                    authorized = true
                    break
                }
            }
            if (!authorized) {
                return null
            }
            tw.intercepted = true
            var body: String? = null
            var `in`: InputStream? = null
            var reader: InputStreamReader? = null
            var buffered: BufferedReader? = null
            var sb: StringBuilder? = null
            try {
                `in` = openWithCookies(tw.appContext, url, tw.pageUrl, request.getRequestHeaders())
                if (`in` != null) {
                    reader = InputStreamReader(`in`)
                    buffered = BufferedReader(reader)
                    sb = StringBuilder()
                    var line: String?
                    while ((buffered.readLine().also { line = it }) != null) {
                        line = line!!.trim { it <= ' ' }
                        if (!TextUtils.isEmpty(line)) {
                            sb.append(line)
                            sb.append('\n')
                        }
                    }
                }
            } catch (outOfMemoryError: OutOfMemoryError) {
                MainUtil.restartLater(tw.appContext)
            } catch (ignored: Exception) {
            }
            try {
                if (buffered != null) {
                    buffered.close()
                }
            } catch (ignored: Exception) {
            }
            try {
                if (reader != null) {
                    reader.close()
                }
            } catch (ignored: Exception) {
            }
            try {
                if (`in` != null) {
                    `in`.close()
                }
            } catch (ignored: Exception) {
            }
            if (sb != null) {
                body = sb.toString()
            }
            if (tw.webSafe == null) {
                return null
            }
            if (TextUtils.isEmpty(body)) {
                if (tw.taskListener != null) {
                    (tw.taskListener as WebSnsLoad.ReleaseStep).onResult(null)
                }
                return null
            }
            tw.jsonText = body
            if (tw.parentView != null) {
                tw.parentView!!.post(tw.ParseTweetTask())
            }
            return WebResourceResponse(
                "text/plain",
                "UTF-8",
                ByteArrayInputStream("".toByteArray())
            )
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val webSnsTwit = this@WebSnsTwit
            if (webSnsTwit.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    webSnsTwit.loadUrl(url)
                }
            }
            return false
        }
    }

    companion object {
        @JvmStatic
        fun toJson(node: Any?, hasNext: Boolean): String? {
            val sb = StringBuilder()
            if (node is JSONObject) {
                sb.append("}")
            } else {
                if (node !is JSONArray) {
                    return null
                }
                sb.append("]")
            }
            if (hasNext) {
                sb.append(",")
            }
            return sb.toString()
        }
    }
}
