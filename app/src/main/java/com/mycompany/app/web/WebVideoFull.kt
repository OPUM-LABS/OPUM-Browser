package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.RequestCode
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.VideoSettings
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
import com.mycompany.app.view.MyWebBody
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
import com.mycompany.app.view.MyFadeFrame
import android.view.GestureDetector
import android.webkit.WebChromeClient.CustomViewCallback
import com.mycompany.app.web.WebVideoFrame.VideoFrameListener
import com.mycompany.app.view.MySizeFrame
import com.mycompany.app.view.MyFadeRelative
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.dialog.DialogSeekBright
import com.mycompany.app.dialog.DialogSeekAudio
import com.mycompany.app.web.WebVideoFull
import com.mycompany.app.web.WvaVideoFrameListener
import com.mycompany.app.view.MyFadeLinear.MyVisibleListener
import android.view.GestureDetector.SimpleOnGestureListener
import com.mycompany.app.view.MyFadeListener
import android.view.View.OnTouchListener
import android.view.View.OnLongClickListener
import com.mycompany.app.web.WebVideoProgress.WebVidProgListener
import android.graphics.Typeface
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.view.PopMenuItem
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
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
import android.graphics.drawable.Icon
import android.util.Rational
import com.mycompany.app.dialog.DialogSetTts
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.webkit.HttpAuthHandler
import com.mycompany.app.view.MyFadeLinear
import android.widget.ArrayAdapter
import android.widget.ImageView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.web.WebSearchAdapter2
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("deprecation")
class WebVideoFull(@JvmField var ctx: Context?) : WebVideoFrame(ctx) {
    /** The Compose controls behind the fields below (n..F, J..L). */
    @JvmField
    var controls: VideoFullControls? = null
    @JvmField
    var notiCheck: VfCheck? = null
    @JvmField
    var videoWidth: Int = 0
    @JvmField
    var downloadBtn: VfButton? = null
    @JvmField
    var videoHeight: Int = 0
    @JvmField
    var brightBtn: VfButton? = null
    @JvmField
    var pinchScale: Float = 0f
    @JvmField
    var brightText: VfText? = null
    @JvmField
    var pinchApplied: Boolean = false
    @JvmField
    var volumeBtn: VfButton? = null
    @JvmField
    var pinchShrink: Boolean = false
    @JvmField
    var volumeText: VfText? = null
    @JvmField
    var dialogOpen: Boolean = false
    @JvmField
    var seekProgress: WebVideoProgress? = null
    @JvmField
    var reloadPending: Boolean = false
    @JvmField
    var brightProgress: WebVideoProgress? = null
    @JvmField
    var executorCache: ExecutorService? = null
    @JvmField
    var volumeProgress: WebVideoProgress? = null
    @JvmField
    var scriptPending: Boolean = false
    @JvmField
    var liftSpacer1: VfSpacer? = null
    @JvmField
    var touchHandled: Boolean = false
    @JvmField
    var liftSpacer2: VfSpacer? = null
    @JvmField
    var clickLocked: Boolean = false
    @JvmField
    var liftSpacer3: VfSpacer? = null
    @JvmField
    var sizeKnown: Boolean = false
    @JvmField
    var liftShown: Boolean = false
    @JvmField
    var zoneLeft: Int = 0
    @JvmField
    var doubleTapGuide: MyFadeFrame? = null
    @JvmField
    var zoneTop: Int = 0
    @JvmField
    var longPressGuide: MyFadeFrame? = null
    @JvmField
    var zoneRight: Int = 0
    @JvmField
    var videoKind: Byte = 0
    @JvmField
    var zoneBottom: Int = 0
    @JvmField
    var videoTitle: String? = null
    @JvmField
    var bottomInset: Int = 0

    /**
     * Room kept free at the bottom for Android's gesture area (2026-11-20): with gesture navigation the page's own
     * controls (its seek bar) sat right on the bottom edge, where a drag can turn into "go home". 0 with buttons.
     */
    var gestureLift: Int = 0
    @JvmField
    var videoReady: Boolean = false
    @JvmField
    var cutOut: Boolean = false
    @JvmField
    var videoGone: Boolean = false
    @JvmField
    var seekPending: Boolean = false
    @JvmField
    var seekStarted: Boolean = false
    @JvmField
    var guideShown: Boolean = false
    @JvmField
    var videoPaused: Boolean = false
    @JvmField
    var guideReady: Boolean = false
    @JvmField
    var seekBase: Long = 0
    @JvmField
    var downStateChanged: Boolean = false
    @JvmField
    var videoSeeked: Boolean = false
    @JvmField
    var pendingSpeed: Float = 0f
    @JvmField
    var brightnessState: Int = 0
    @JvmField
    var brightnessApplied: Boolean = false
    @JvmField
    var hideRunnable: Runnable?
    @JvmField
    var ownerActivity: MainActivity? = null
    @JvmField
    var duration: Long = 0
    @JvmField
    var pausedState: Boolean = false
    @JvmField
    var videoPosition: Long = 0
    @JvmField
    var rateValue: Float = 0f
    @JvmField
    var layoutRtl: Boolean = false
    @JvmField
    var seekTarget: Long = 0
    @JvmField
    var pendingRate: Float = 0f
    @JvmField
    var topHidePending: Boolean = false
    @JvmField
    var positionRequested: Boolean = false
    @JvmField
    var videoSizeText: String? = null
    @JvmField
    var menuHidePending: Boolean = false
    @JvmField
    var gestures: GestureDetector? = null
    @JvmField
    var moveRunnable: Runnable?
    @JvmField
    var container: ViewGroup? = null
    @JvmField
    var touchMoved: Boolean = false
    @JvmField
    var lastEvent: MotionEvent? = null
    @JvmField
    var page: WebNestView? = null
    @JvmField
    var downX: Float = 0f
    @JvmField
    var longPressRunnable: Runnable?
    @JvmField
    var videoUrl: String? = null
    @JvmField
    var downY: Float = 0f
    @JvmField
    var lastGesture: Byte = 0
    @JvmField
    var touchView: View? = null
    @JvmField
    var gesture: Byte = 0
    @JvmField
    var lastSeekBase: Long = 0
    @JvmField
    var viewCallback: CustomViewCallback? = null
    @JvmField
    var seekMoved: Boolean = false
    @JvmField
    var seekTouchUpTime: Long = 0
    @JvmField
    var frameListener: VideoFrameListener? = null
    @JvmField
    var youContinue: Boolean = false
    @JvmField
    var sizeFrame: MySizeFrame? = null
    @JvmField
    var youExpanded: Boolean = false
    @JvmField
    var controlsPanel: MyFadeRelative? = null
    @JvmField
    var youDialog: Boolean = false
    @JvmField
    var titleText: VfText? = null
    @JvmField
    var longPressed: Boolean = false
    @JvmField
    var backBtn: VfButton? = null
    @JvmField
    var doubleTapped: Boolean = false
    @JvmField
    var topRow: VfSpacer? = null
    @JvmField
    var rotationMenu: MyPopupMenu? = null
    @JvmField
    var rotateBtn: VfButton? = null
    @JvmField
    var zoomMenu: MyPopupMenu? = null
    @JvmField
    var zoomBtn: VfButton? = null
    @JvmField
    var speedMenu: MyPopupMenu? = null
    @JvmField
    var loopBtn: VfButton? = null
    @JvmField
    var pageBusy: Boolean = false
    @JvmField
    var speedBtn: VfButton? = null
    @JvmField
    var brightDialog: DialogSeekBright? = null
    @JvmField
    var pipBtn: VfButton? = null
    @JvmField
    var audioDialog: DialogSeekAudio? = null
    @JvmField
    var settingsBtn: VfButton? = null
    @JvmField
    var pipBuilder: PictureInPictureParams.Builder? = null
    @JvmField
    var lockCheck: VfCheck? = null
    @JvmField
    var pipAuto: Boolean = false
    @JvmField
    var rewindBtn: VfButton? = null
    @JvmField
    var downloadable: Boolean = false
    @JvmField
    var playBtn: VfButton? = null
    @JvmField
    var downShown: Boolean = false
    @JvmField
    var forwardBtn: VfButton? = null
    @JvmField
    var pipPending: Boolean = false

    internal inner class VideoSizeListener : ImageSizeListener {
        override fun onSize(view: View?, width: Int, height: Int) {
            val webVideoFull = this@WebVideoFull
            webVideoFull.sizeKnown = true
            webVideoFull.zoneLeft = 0
            webVideoFull.zoneTop = 0
            webVideoFull.zoneRight = 0
            webVideoFull.zoneBottom = 0
            webVideoFull.enterPip()
            val webVideoProgress = webVideoFull.volumeProgress
            if (webVideoProgress != null) {
                webVideoProgress.applySize(width, height)
            }
        }
    }

    internal inner class EnterPipModeClick : OnClickListener {

        internal inner class EnterPipStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                if (webVideoFull.ownerActivity == null) {
                    return
                }
                webVideoFull.enterPipMode(true)
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(EnterPipStep(), 100L)
        }
    }

    internal inner class SetNotiClick : OnClickListener {

        internal inner class ToggleNotiStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                if (webVideoFull.ownerActivity == null) {
                    return
                }
                if (VideoSettings.dotSettings.value) {
                    VideoSettings.edit(webVideoFull.ctx) { set(VideoSettings.dotSettings, false) }
                    val myButtonImage = webVideoFull.settingsBtn
                    if (myButtonImage != null) {
                        myButtonImage.setNoti(false)
                    }
                }
                webVideoFull.pipAuto = true
                val intent = Intent(webVideoFull.ctx, com.mycompany.app.ui.settings.VideoSettingsActivity::class.java as Class<*>)
                intent.putExtra("EXTRA_WEB", true)
                webVideoFull.ownerActivity!!.startActivity(intent)
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(ToggleNotiStep(), 100L)
        }
    }

    internal inner class LockIntentClick : OnClickListener {

        internal inner class ToggleLockStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val myButtonCheck = webVideoFull.lockCheck
                if (myButtonCheck != null) {
                    if (!myButtonCheck.isChecked) {
                        webVideoFull.setTouchLock(true)
                        webVideoFull.postLayout()
                        return
                    }
                    val type = SecretSettings.touchLockType.value
                    if (type == 0) {
                        webVideoFull.setTouchLock(false)
                        webVideoFull.postLayout()
                    } else {
                        if (webVideoFull.ownerActivity == null) {
                            return
                        }
                        webVideoFull.pipAuto = true
                        val intentH = MainUtil.lockIntent(webVideoFull.ctx, type)
                        intentH.putExtra("EXTRA_PASS", 3)
                        intentH.putExtra("EXTRA_TYPE", 3)
                        webVideoFull.ownerActivity!!.launchExternalForResult(intentH, RequestCode.UNLOCK)
                        webVideoFull.postLayout()
                    }
                }
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(ToggleLockStep(), 100L)
        }
    }

    internal inner class SeekBackClick : OnClickListener {
        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.videoGone) {
                showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
            } else if (webVideoFull.layoutRtl) {
                webVideoFull.seekBack()
            } else {
                webVideoFull.seekForward()
            }
        }
    }

    internal inner class TogglePlayClick : OnClickListener {
        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.videoGone) {
                showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
            } else {
                webVideoFull.togglePlay()
            }
        }
    }

    internal inner class SeekForwardClick : OnClickListener {
        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.videoGone) {
                showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
            } else if (webVideoFull.layoutRtl) {
                webVideoFull.seekForward()
            } else {
                webVideoFull.seekBack()
            }
        }
    }

    internal inner class SetIconShowClick : OnClickListener {

        internal inner class ToggleIconStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                if (webVideoFull.notiCheck == null) {
                    return
                }
                if (VideoSettings.dotIcon.value) {
                    VideoSettings.edit(webVideoFull.ctx) { set(VideoSettings.dotIcon, false) }
                    webVideoFull.notiCheck!!.setNoti(false)
                }
                val showIcon = !PdfSettings.videoIcon.value
                PdfSettings.edit(webVideoFull.ctx) { set(PdfSettings.videoIcon, showIcon) }
                webVideoFull.notiCheck!!.setAlpha(if (PdfSettings.videoIcon.value) 1.0f else 0.4f)
                webVideoFull.notiCheck!!.setChecked(PdfSettings.videoIcon.value, true)
                webVideoFull.setIconShow(PdfSettings.videoIcon.value)
                webVideoFull.controlsPanel!!.show(false)
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(ToggleIconStep(), 100L)
        }
    }

    internal inner class DownloadClick : OnClickListener {

        internal inner class DownloadStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val videoFrameListener = webVideoFull.frameListener
                if (videoFrameListener != null) {
                    (videoFrameListener as WvaVideoFrameListener).onDownload()
                }
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(DownloadStep(), 100L)
        }
    }

    internal inner class ShowBrightDialogClick : OnClickListener {

        internal inner class ShowBrightDialogStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val speeds: FloatArray? = SPEEDS
                if (webVideoFull.ownerActivity != null && webVideoFull.brightDialog == null && webVideoFull.audioDialog == null) {
                    webVideoFull.dismissBrightDialog()
                    val myFadeRelative = webVideoFull.controlsPanel
                    if (myFadeRelative != null) {
                        myFadeRelative.hide(false)
                    }
                    webVideoFull.dialogOpen = true
                    AppState.darkUi = true
                    val mainActivity = webVideoFull.ownerActivity
                    val dialogSeekBright = DialogSeekBright(
                        mainActivity,
                        mainActivity!!.getWindow(),
                        1,
                        webVideoFull.SeekProgressListener()
                    )
                    webVideoFull.brightDialog = dialogSeekBright
                    dialogSeekBright.setOnDismissListener(webVideoFull.BrightDialogDismiss())
                    webVideoFull.brightDialog!!.noDim = true
                }
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(ShowBrightDialogStep(), 100L)
        }
    }

    internal inner class ShowAudioDialogClick : OnClickListener {

        internal inner class ShowAudioDialogStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val speeds: FloatArray? = SPEEDS
                if (webVideoFull.ownerActivity != null && webVideoFull.brightDialog == null && webVideoFull.audioDialog == null) {
                    webVideoFull.dismissAudioDialog()
                    val myFadeRelative = webVideoFull.controlsPanel
                    if (myFadeRelative != null) {
                        myFadeRelative.hide(false)
                    }
                    webVideoFull.dialogOpen = true
                    AppState.darkUi = true
                    val dialogSeekAudio =
                        DialogSeekAudio(webVideoFull.ownerActivity, webVideoFull.BrightProgressListener())
                    webVideoFull.audioDialog = dialogSeekAudio
                    dialogSeekAudio.setOnDismissListener(webVideoFull.AudioDialogDismiss())
                    webVideoFull.audioDialog!!.noDim = true
                }
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(ShowAudioDialogStep(), 100L)
        }
    }

    internal inner class ControlsVisibleListener : MyVisibleListener {

        internal inner class LayoutTopRowStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val speeds: FloatArray? = SPEEDS
                val linearLayout: View? = if (!webVideoFull.sizeKnown) null else webVideoFull.topRow?.anchor
                val width = if (!webVideoFull.sizeKnown || linearLayout == null) 0 else linearLayout.getWidth()
                val height = if (!webVideoFull.sizeKnown || linearLayout == null || width == 0) 0 else linearLayout.getHeight()
                if (!webVideoFull.sizeKnown || linearLayout == null || width == 0 || height == 0) {
                    return
                }
                webVideoFull.sizeKnown = false
                val location = IntArray(2)
                linearLayout.getLocationOnScreen(location)
                val zoneLeft = location[0]
                val zoneTop = location[1]
                val dp4Px = MainApp.dp4
                webVideoFull.zoneLeft = zoneLeft - dp4Px
                webVideoFull.zoneTop = zoneTop - dp4Px
                webVideoFull.zoneRight = width + zoneLeft + dp4Px
                webVideoFull.zoneBottom = height + zoneTop + dp4Px
            }
        }

        override fun onVisible(visible: Boolean) {
            val viewGroup: ViewGroup? = if (!visible) null else this@WebVideoFull.container
            if (!visible || viewGroup == null) {
                return
            }
            viewGroup.post(LayoutTopRowStep())
        }
    }

    internal inner class SeekForwardGestures : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val leftArea: Int
            val rightArea: Int
            val action: Int
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            var guided = false
            val myButtonCheck: VfCheck? = if (!webVideoFull.isControlPressed(false) && webVideoFull.controlsPanel != null) webVideoFull.lockCheck else null
            if (!webVideoFull.isControlPressed(false) && webVideoFull.controlsPanel != null && myButtonCheck != null && webVideoFull.pipBuilder == null && !myButtonCheck.isChecked) {
                val mainActivity = webVideoFull.ownerActivity
                if (if (mainActivity == null) isLandscape(webVideoFull.ctx) else mainActivity.isLandscapeScreen()) {
                    leftArea = VideoSettings.landAreaLeft.value
                    rightArea = VideoSettings.landAreaRight.value
                } else {
                    leftArea = VideoSettings.portAreaLeft.value
                    rightArea = VideoSettings.portAreaRight.value
                }
                val x = e.getX()
                if (x < leftArea) {
                    action = VideoSettings.tapLeft.value
                } else {
                    action =
                        if (x > ((webVideoFull.getWidth() - rightArea).toFloat())) VideoSettings.tapRight.value else VideoSettings.tapCenter.value
                }
                guided = true
                if (action == 0) {
                    if (webVideoFull.videoGone) {
                        showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
                        return true
                    }
                    webVideoFull.seekForward()
                    webVideoFull.doubleTapped = true
                    return true
                }
                if (action == 1) {
                    if (webVideoFull.videoGone) {
                        showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
                        return true
                    }
                    webVideoFull.seekBack()
                    webVideoFull.doubleTapped = true
                    return true
                }
                if (action == 2) {
                    if (webVideoFull.videoGone) {
                        showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
                        return true
                    }
                    webVideoFull.togglePlay()
                    webVideoFull.doubleTapped = true
                }
            }
            return guided
        }

        override fun onLongPress(e: MotionEvent) {
            val webVideoFull = this@WebVideoFull
            webVideoFull.longPressed = true
            webVideoFull.showControls(true, false)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val speeds: FloatArray? = SPEEDS
            return this@WebVideoFull.showControls(false, false)
        }
    }

    internal inner class ShowDoubleTapGuideTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val guidePlayer = VideoSettings.guidePlayer.value
            val webVideoFull = this@WebVideoFull
            if (!guidePlayer) {
                webVideoFull.javaClass
                return
            }
            val myFadeFrameO: MyFadeFrame? = if (webVideoFull.doubleTapGuide != null || webVideoFull.sizeFrame == null) null else com.mycompany.app.video.VideoGuide.updownGuide(webVideoFull.ownerActivity)
            if (webVideoFull.doubleTapGuide != null || webVideoFull.sizeFrame == null || myFadeFrameO == null) {
                return
            }
            webVideoFull.doubleTapGuide = myFadeFrameO
            myFadeFrameO.setListener(webVideoFull.DoubleTapGuideFade())
            webVideoFull.doubleTapGuide!!.setOnTouchListener(webVideoFull.DoubleTapGuideTouch())
            // TalkBack activates by click, not by touch: the same dismissal as a click
            webVideoFull.doubleTapGuide!!.setOnClickListener { webVideoFull.dismissDoubleTapGuide() }
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.post(webVideoFull.AddDoubleTapGuideTask())
        }
    }

    internal inner class DoubleTapGuideFade : MyFadeListener {

        internal inner class RemoveDoubleTapGuideStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val myFadeFrame = webVideoFull.doubleTapGuide
                if (myFadeFrame != null) {
                    myFadeFrame.release()
                    webVideoFull.removeView(webVideoFull.doubleTapGuide)
                    webVideoFull.doubleTapGuide = null
                }
                if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value) {
                    webVideoFull.showLongPressGuide(true)
                }
            }
        }

        override fun onFadeEnd(shown: Boolean) {
            val viewGroup: ViewGroup? = if (shown) null else this@WebVideoFull.container
            if (shown || viewGroup == null) {
                return
            }
            viewGroup.post(RemoveDoubleTapGuideStep())
        }

        override fun onFadeStart(shown: Boolean, animate: Boolean) {
        }
    }

    /** Hides the one-time double-tap guide for good (any touch on it, or a click from TalkBack). */
    fun dismissDoubleTapGuide() {
        if (VideoSettings.guidePlayer.value) {
            VideoSettings.edit(ctx) { set(VideoSettings.guidePlayer, false) }
        }
        doubleTapGuide?.hide(true)
    }

    internal inner class DoubleTapGuideTouch : OnTouchListener {
        // dismisses on the first touch, as before; TalkBack uses the click listener set next to it
        @android.annotation.SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            dismissDoubleTapGuide()
            return true
        }
    }

    internal inner class AddDoubleTapGuideTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val guidePlayer = VideoSettings.guidePlayer.value
            val webVideoFull = this@WebVideoFull
            if (!guidePlayer) {
                webVideoFull.javaClass
                return
            }
            val myFadeFrame = webVideoFull.doubleTapGuide
            if (myFadeFrame == null) {
                return
            }
            webVideoFull.addView(myFadeFrame, -1, -1)
        }
    }

    internal inner class ShowLongPressGuideTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            val guideShown = webVideoFull.guideShown
            val mainActivity: MainActivity? = if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value && webVideoFull.doubleTapGuide == null && webVideoFull.longPressGuide == null && webVideoFull.sizeFrame != null) webVideoFull.ownerActivity else null
            if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value && webVideoFull.doubleTapGuide == null && webVideoFull.longPressGuide == null && webVideoFull.sizeFrame != null && mainActivity != null) {
                val myFadeFrame = MyFadeFrame(mainActivity)
                val left = MainApp.dp40
                myFadeFrame.setPadding(left, left, left, left)
                val guideText = mainActivity.getString(AppR.string.video_icon_guide) + "\n" + mainActivity.getString(AppR.string.change_in_setting)
                val card = VideoGuideCardView(mainActivity, guideText) { view -> webVideoFull.LongPressGuideCloseClick().onClick(view) }
                myFadeFrame.addView(card, LayoutParams(-2, -2).also { it.gravity = 17 })
                webVideoFull.longPressGuide = myFadeFrame
                val myFadeRelative = webVideoFull.controlsPanel
                if (myFadeRelative != null) {
                    myFadeRelative.hide(false)
                }
                webVideoFull.dismissBrightDialog()
                webVideoFull.dismissAudioDialog()
                webVideoFull.dismissRotationMenu()
                webVideoFull.dismissZoomMenu()
                webVideoFull.dismissSpeedMenu()
                if (guideShown) {
                    webVideoFull.longPressGuide!!.setVisibility(View.INVISIBLE)
                }
                webVideoFull.longPressGuide!!.setListener(webVideoFull.LongPressGuideFade())
                webVideoFull.longPressGuide!!.setOnClickListener(LongPressGuideClick())
                webVideoFull.longPressGuide!!.setOnLongClickListener(webVideoFull.LongPressGuideLongClick())
                webVideoFull.guideReady = guideShown
                val viewGroup = webVideoFull.container
                if (viewGroup != null) {
                    viewGroup.post(webVideoFull.CheckGuidesTask())
                }
            }
        }
    }

    internal inner class LongPressGuideCloseClick : OnClickListener {
        override fun onClick(view: View?) {
            val speeds: FloatArray? = SPEEDS
            val guideLong = VideoSettings.guideLongPress.value
            val webVideoFull = this@WebVideoFull
            if (guideLong) {
                VideoSettings.edit(webVideoFull.ctx) { set(VideoSettings.guideLongPress, false) }
            }
            val myFadeFrame = webVideoFull.longPressGuide
            if (myFadeFrame != null) {
                myFadeFrame.hide(true)
            }
        }
    }

    internal inner class LongPressGuideFade : MyFadeListener {

        internal inner class RemoveLongPressGuideStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val myFadeFrame = webVideoFull.longPressGuide
                if (myFadeFrame != null) {
                    myFadeFrame.release()
                    webVideoFull.removeView(webVideoFull.longPressGuide)
                    webVideoFull.longPressGuide = null
                }
            }
        }

        override fun onFadeEnd(shown: Boolean) {
            val viewGroup: ViewGroup? = if (shown) null else this@WebVideoFull.container
            if (shown || viewGroup == null) {
                return
            }
            viewGroup.post(RemoveLongPressGuideStep())
        }

        override fun onFadeStart(shown: Boolean, animate: Boolean) {
        }
    }

    internal inner class LongPressGuideClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class LongPressGuideLongClick : OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            val speeds: FloatArray? = SPEEDS
            val guideLong = VideoSettings.guideLongPress.value
            val webVideoFull = this@WebVideoFull
            if (guideLong) {
                VideoSettings.edit(webVideoFull.ctx) { set(VideoSettings.guideLongPress, false) }
            }
            val myFadeFrame = webVideoFull.longPressGuide
            if (myFadeFrame != null) {
                myFadeFrame.hide(true)
            }
            webVideoFull.longPressed = true
            webVideoFull.showControls(true, true)
            return true
        }
    }

    internal inner class BrightnessProgressListener : WebVidProgListener {
        override fun onLevelSent(level: Int) {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.applyBrightness()
        }
    }

    internal inner class CheckGuidesTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            val guideReady = webVideoFull.guideReady
            val myFadeFrame: MyFadeFrame? = if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value) webVideoFull.longPressGuide else null
            if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value && myFadeFrame != null) {
                webVideoFull.addView(myFadeFrame, -1, -1)
                if (guideReady) {
                    webVideoFull.longPressGuide!!.show(true)
                }
            }
        }
    }

    internal inner class SetBackgroundColorTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.ownerActivity == null) {
                return
            }
            webVideoFull.layoutRtl = isRtl(webVideoFull.ctx)
            webVideoFull.setBackgroundColor(-16777216)
            webVideoFull.setOnClickListener(FrameClick())
            webVideoFull.runInBackground(webVideoFull.ApplyOrientationTask())
        }
    }

    internal inner class FrameClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class ApplyOrientationTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            val mainActivity = webVideoFull.ownerActivity
            if (mainActivity != null) {
                val rotation = VideoSettings.rotation.value
                if (rotation == 1) {
                    MainUtil.setActivityOrientation(mainActivity, 7)
                } else if (rotation == 2) {
                    MainUtil.setActivityOrientation(mainActivity, 6)
                } else {
                    MainUtil.setActivityOrientation(mainActivity, 4)
                }
                val viewGroup = webVideoFull.container
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(webVideoFull.HoldScreenOnTask())
            }
        }
    }

    internal inner class HoldScreenOnTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            val mainActivity = webVideoFull.ownerActivity
            if (mainActivity != null) {
                mainActivity.holdScreenOn(webVideoFull, true)
                val viewGroup = webVideoFull.container
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(webVideoFull.SetWindowBrightnessTask())
            }
        }
    }

    internal inner class SetWindowBrightnessTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            val mainActivity = webVideoFull.ownerActivity
            if (mainActivity != null) {
                webVideoFull.brightnessState = VideoSettings.brightness.value
                MainUtil.applyWindowBrightness(mainActivity.getWindow(), VideoSettings.brightness.value, VideoSettings.userBrightness.value)
                val viewGroup = webVideoFull.container
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(webVideoFull.QueryVideoTask())
            }
        }
    }

    internal inner class QueryVideoTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.ownerActivity == null) {
                return
            }
            val webNestView = webVideoFull.page
            if (webNestView != null && !webNestView.deskMode) {
                val videoKind = webVideoFull.videoKind
                if (videoKind.toInt() != 2) {
                    if (videoKind.toInt() != 3 && videoKind.toInt() == 0) {
                        evalJs(
                            webNestView,
                            "(function(){var val=0;var ele=document.querySelector(\"iframe[src*='youtube']\");if(ele){val=1;}android.onVidResult('onYouType',val);})();",
                            false
                        )
                    }
                } else if (!webVideoFull.liftShown && webVideoFull.liftSpacer1 != null) {
                    webVideoFull.liftShown = true
                    val viewGroup = webVideoFull.container
                    if (viewGroup != null) {
                        viewGroup.post(webVideoFull.UpdateLiftTask())
                    }
                }
            }
            webVideoFull.runInBackground(webVideoFull.InjectWindowOpenJsTask())
        }
    }

    internal inner class InjectWindowOpenJsTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.ownerActivity != null) {
                MainUtil.injectWindowOpenJs(webVideoFull.page, webVideoFull.videoKind.toInt(), webVideoFull.videoUrl!!, true)
                val viewGroup = webVideoFull.container
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(webVideoFull.BuildControlsTask())
            }
        }
    }

    internal inner class BuildControlsTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            val mainActivity = webVideoFull.ownerActivity
            if (mainActivity != null) {
                if (webVideoFull.sizeFrame == null) {
                    val mySizeFrame = MySizeFrame(mainActivity)
                    val myFadeRelative = MyFadeRelative(mainActivity)
                    myFadeRelative.setAnimTime(100)
                    myFadeRelative.setTouchable(true)
                    myFadeRelative.setAutoHide(true)
                    myFadeRelative.setVisibility(View.GONE)
                    mySizeFrame.addView(myFadeRelative, -1, -1)
                    // all buttons and labels are one Compose page (VideoFullControls) inside the fading frame
                    val controls = VideoFullControls()
                    myFadeRelative.addView(VideoFullControlsView(mainActivity, controls), -1, -1)
                    webVideoFull.controls = controls
                    webVideoFull.sizeFrame = mySizeFrame
                    webVideoFull.controlsPanel = myFadeRelative
                    webVideoFull.titleText = controls.title
                    webVideoFull.backBtn = controls.back
                    webVideoFull.topRow = controls.topRow
                    webVideoFull.rotateBtn = controls.rotate
                    webVideoFull.zoomBtn = controls.zoom
                    webVideoFull.loopBtn = controls.next
                    webVideoFull.speedBtn = controls.speed
                    webVideoFull.pipBtn = controls.pip
                    webVideoFull.settingsBtn = controls.settings
                }
                val viewGroup = webVideoFull.container
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(webVideoFull.InitControlsTask())
            }
        }
    }

    internal inner class InitControlsTask : Runnable {

        internal inner class InitControlsStep : Runnable {

            internal inner class BuildControlsStep : Runnable {

                internal inner class StartVideoScriptsStep : Runnable {
                    override fun run() {
                        val webVideoFull = this@WebVideoFull
                        val speeds: FloatArray? = SPEEDS
                        if (webVideoFull.ownerActivity != null) {
                            webVideoFull.runInBackground(webVideoFull.StartVideoJsTask())
                            val viewGroup = webVideoFull.container
                            if (viewGroup == null) {
                                return
                            }
                            viewGroup.postDelayed(webVideoFull.ClearReloadPendingTask(), 1000L)
                        }
                    }
                }

                override fun run() {
                    val initControlsTask = this@InitControlsTask
                    val webVideoFull = this@WebVideoFull
                    val speeds: FloatArray? = SPEEDS
                    webVideoFull.initControls()
                    val viewGroup = this@WebVideoFull.container
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(StartVideoScriptsStep())
                }
            }

            override fun run() {
                val ratio: Int
                val webVideoFull = this@WebVideoFull
                val speeds: FloatArray? = SPEEDS
                val mySizeFrame: MySizeFrame? = if (webVideoFull.seekProgress == null) webVideoFull.sizeFrame else null
                val mainActivity: MainActivity? = if (webVideoFull.seekProgress == null && mySizeFrame != null) webVideoFull.ownerActivity else null
                if (webVideoFull.seekProgress == null && mySizeFrame != null && mainActivity != null) {
                    // brightness, volume and seek overlays (content is Compose, see WebVideoProgress)
                    val webVideoProgress = WebVideoProgress(mainActivity)
                    webVideoProgress.build(1)
                    // the pills sit at the top centre under the back button, like OPUM's player (were mid-screen)
                    val pillTop = MainApp.dp64 + MainApp.dp8
                    mySizeFrame.addView(webVideoProgress, LayoutParams(-2, -2).also { it.gravity = 49; it.topMargin = pillTop })
                    val brightProgress = WebVideoProgress(mainActivity)
                    brightProgress.build(2)
                    mySizeFrame.addView(brightProgress, LayoutParams(-2, -2).also { it.gravity = 49; it.topMargin = pillTop })
                    val width = mySizeFrame.getWidth()
                    val height = mySizeFrame.getHeight()
                    val guideSize = if (width == 0 || height == 0) 0 else (min(width, height) - (MainApp.dp64 shl 1))
                    val volumeProgress = WebVideoProgress(mainActivity)
                    if (width == 0 || height == 0 || guideSize <= 0) {
                        volumeProgress.build(3, -1)
                    } else {
                        volumeProgress.barWidth = MainApp.dp40 + guideSize
                        volumeProgress.build(3, guideSize)
                    }
                    mySizeFrame.addView(volumeProgress, LayoutParams(-2, -2).also { it.gravity = 49; it.topMargin = pillTop })
                    webVideoFull.seekProgress = webVideoProgress
                    webVideoFull.brightProgress = brightProgress
                    webVideoFull.volumeProgress = volumeProgress
                }
                val viewGroup = webVideoFull.container
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(BuildControlsStep())
            }
        }

        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            val controls = webVideoFull.controls
            if (webVideoFull.lockCheck == null && controls != null) {
                webVideoFull.lockCheck = controls.lock
                webVideoFull.rewindBtn = controls.rewind
                webVideoFull.playBtn = controls.play
                webVideoFull.forwardBtn = controls.forward
                webVideoFull.notiCheck = controls.icons
                webVideoFull.downloadBtn = controls.download
                webVideoFull.brightBtn = controls.bright
                webVideoFull.brightText = controls.brightText
                webVideoFull.volumeBtn = controls.volume
                webVideoFull.volumeText = controls.volumeText
                webVideoFull.liftSpacer1 = controls.lift1
                webVideoFull.liftSpacer2 = controls.lift2
                webVideoFull.liftSpacer3 = controls.lift3
            }
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.post(InitControlsStep())
        }
    }

    internal inner class VolumeProgressListener : WebVidProgListener {
        override fun onLevelSent(level: Int) {
            this@WebVideoFull.seekBase = level.toLong()
        }
    }

    internal inner class StartVideoJsTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            val webVideoFull = this@WebVideoFull
            webVideoFull.injectVideoJs()
            webVideoFull.scriptPending = false
        }
    }

    internal inner class ClearReloadPendingTask : Runnable {
        override fun run() {
            this@WebVideoFull.reloadPending = false
        }
    }

    internal inner class ClearTopHidePendingTask : Runnable {
        override fun run() {
            this@WebVideoFull.topHidePending = false
        }
    }

    internal inner class ClearMenuHidePendingTask : Runnable {
        override fun run() {
            this@WebVideoFull.menuHidePending = false
        }
    }

    internal inner class NoOpTask : Runnable {
        override fun run() {
            this@WebVideoFull.videoSize
        }
    }

    internal inner class ApplyWindowBrightnessTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val mainActivity = webVideoFull.ownerActivity
            if (mainActivity == null) {
                return
            }
            webVideoFull.brightnessState = VideoSettings.brightness.value
            MainUtil.applyWindowBrightness(mainActivity.getWindow(), VideoSettings.brightness.value, VideoSettings.userBrightness.value)
        }
    }

    internal inner class SetPipParamTask(@JvmField val outer: WebVideoFull,
                                          @JvmField val enabled: Boolean
    ) : Runnable {
        override fun run() {
            this.outer.setPipParam2(this.enabled)
        }
    }

    internal inner class PipBrightnessTask : Runnable {
        override fun run() {
            val mainActivity = this@WebVideoFull.ownerActivity
            if (mainActivity == null) {
                return
            }
            MainUtil.applyWindowBrightness(mainActivity.getWindow(), PdfSettings.brightness.value, PdfSettings.userBrightness.value)
        }
    }

    internal inner class UnlockClickTask : Runnable {
        override fun run() {
            this@WebVideoFull.clickLocked = false
        }
    }

    internal inner class UpdateLiftTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.liftSpacer1 == null) {
                return
            }
            webVideoFull.updateLift()
            webVideoFull.liftSpacer3!!.setVisibility(0)
        }
    }

    internal inner class BackBtnClick : OnClickListener {

        internal inner class ExitVideoStep : Runnable {
            override fun run() {
                val full = this@WebVideoFull
                if (full.frameListener != null && full.frameListener!!.activity() != null) {
                    full.frameListener!!.activity()!!.exitVideoFrame()
                }
                full.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(ExitVideoStep(), 100L)
        }
    }

    internal inner class HideControlsTask : Runnable {
        override fun run() {
            val myFadeRelative = this@WebVideoFull.controlsPanel
            if (myFadeRelative != null) {
                myFadeRelative.hide(false)
            }
        }
    }

    internal inner class RestartControlsTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val myFadeRelative = webVideoFull.controlsPanel
            if (myFadeRelative == null) {
                return
            }
            if (myFadeRelative.isShowingNow()) {
                webVideoFull.controlsPanel!!.hide(false)
            } else {
                webVideoFull.controlsPanel!!.show(false)
            }
        }
    }

    internal inner class InjectVideoJsTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.injectVideoJs()
        }
    }

    internal inner class HideOnSeekTask : Runnable {

        internal inner class HideOnSeekStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val speeds: FloatArray? = SPEEDS
                webVideoFull.hideOnSeek()
            }
        }

        override fun run() {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.controlsPanel != null) {
                webVideoFull.updateRotateIcon()
                webVideoFull.updateLoopIcon()
                if (webVideoFull.rewindBtn != null) {
                    webVideoFull.updateLift()
                    if (PdfSettings.videoIcon.value && webVideoFull.videoReady && !webVideoFull.videoGone) {
                        webVideoFull.rewindBtn!!.setVisibility(0)
                        webVideoFull.playBtn!!.setVisibility(0)
                        webVideoFull.forwardBtn!!.setVisibility(0)
                    } else {
                        webVideoFull.rewindBtn!!.setVisibility(8)
                        webVideoFull.playBtn!!.setVisibility(8)
                        webVideoFull.forwardBtn!!.setVisibility(8)
                    }
                    val userBrightness = VideoSettings.userBrightness.value
                    val myTextView = webVideoFull.brightText
                    if (userBrightness) {
                        myTextView!!.setText(VideoSettings.brightness.value.toString() + "%")
                    } else {
                        myTextView!!.setText("S")
                    }
                    val progress = webVideoFull.brightProgress!!.progress
                    webVideoFull.volumeText!!.setText(progress.toString())
                    val myButtonImage = webVideoFull.volumeBtn
                    if (progress == 0) {
                        myButtonImage!!.setImageResource(AppR.drawable.outline_volume_off_white_24)
                    } else {
                        myButtonImage!!.setImageResource(AppR.drawable.outline_volume_up_white_24)
                    }
                }
                webVideoFull.controlsPanel!!.show(false)
                val videoKind = webVideoFull.videoKind
                val viewGroup: ViewGroup? = if ((videoKind.toInt() == 2 || videoKind.toInt() == 3)) webVideoFull.container else null
                if ((videoKind.toInt() == 2 || videoKind.toInt() == 3) && viewGroup != null) {
                    viewGroup.postDelayed(HideOnSeekStep(), 200L)
                }
            }
        }
    }

    internal inner class SetVideoDownTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            webVideoFull.setVideoDown(webVideoFull.downStateChanged)
        }
    }

    internal inner class RotationMenuListener : MyPopupListener {
        override fun onClose() {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.dismissRotationMenu()
        }

        override fun onItem(view: View?, id: Int): Boolean {
            if (VideoSettings.rotation.value == id) {
                return true
            }
            val webVideoFull = this@WebVideoFull
            VideoSettings.edit(webVideoFull.ctx) { set(VideoSettings.rotation, id) }
            applyVideoRotation(webVideoFull.ownerActivity)
            webVideoFull.updateRotateIcon()
            return true
        }
    }

    internal inner class HideTopLaterTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.rotationMenu != null) {
                webVideoFull.hideTopLater()
            }
        }
    }

    internal inner class ZoomMenuListener : MyPopupListener {
        override fun onClose() {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.dismissZoomMenu()
        }

        override fun onItem(view: View?, id: Int): Boolean {
            val length = id % MainConst.videoSizeNames.size
            val ratio = VideoSettings.ratio.value
            val webVideoFull = this@WebVideoFull
            if (ratio == length) {
                val myFadeRelative = webVideoFull.controlsPanel
                if (myFadeRelative != null) {
                    myFadeRelative.hide(false)
                    return true
                }
            } else {
                VideoSettings.edit(webVideoFull.ctx) { set(VideoSettings.ratio, length) }
                webVideoFull.applyPageZoom()
                val myFadeRelative = webVideoFull.controlsPanel
                if (myFadeRelative != null) {
                    myFadeRelative.hide(false)
                }
            }
            return true
        }
    }

    internal inner class DismissRotationMenuClick : OnClickListener {

        internal inner class RotationMenuStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val myButtonImage = webVideoFull.rotateBtn
                if (myButtonImage == null) {
                    return
                }
                if (webVideoFull.rotationMenu == null) {
                    webVideoFull.dismissRotationMenu()
                    val myFadeRelative = webVideoFull.controlsPanel
                    if (myFadeRelative != null) {
                        myFadeRelative.setAutoHide(false)
                    }
                    val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                    arrayList.add(PopMenuItem(0, AppR.string.rotation, VideoSettings.rotation.value == 0))
                    arrayList.add(PopMenuItem(1, AppR.string.view_port, VideoSettings.rotation.value == 1))
                    arrayList.add(PopMenuItem(2, AppR.string.view_land, VideoSettings.rotation.value == 2))
                    val myPopupMenu = MyPopupMenu(
                        webVideoFull.ownerActivity,
                        webVideoFull.container,
                        myButtonImage.anchor,
                        arrayList,
                        true,
                        webVideoFull.RotationMenuListener()
                    )
                    webVideoFull.rotationMenu = myPopupMenu
                    val mainActivity = webVideoFull.ownerActivity
                    if (mainActivity != null) {
                        mainActivity.popupMenu = myPopupMenu
                    }
                    val viewGroup = webVideoFull.container
                    if (viewGroup != null) {
                        viewGroup.post(webVideoFull.HideTopLaterTask())
                    }
                }
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(RotationMenuStep(), 100L)
        }
    }

    internal inner class HideTopIfZoomMenuTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.zoomMenu != null) {
                webVideoFull.hideTopLater()
            }
        }
    }

    internal inner class SpeedMenuListener : MyPopupListener {

        internal inner class ApplyRateStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val speeds: FloatArray? = SPEEDS
                val pendingRate = webVideoFull.pendingRate
                webVideoFull.pendingRate = 0.0f
                if ((pendingRate).compareTo(0.0f) == 0 || webVideoFull.page == null) {
                    return
                }
                evalJs(webVideoFull.page, "myVidRat2(" + pendingRate + ");", false)
            }
        }

        override fun onClose() {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.dismissSpeedMenu()
        }

        override fun onItem(view: View?, id: Int): Boolean {
            val webVideoFull = this@WebVideoFull
            val pendingSpeed = webVideoFull.pendingSpeed
            webVideoFull.pendingSpeed = 0.0f
            val pendingRate: kotlin.Float = SPEEDS[id % 8]
            if ((pendingRate).compareTo(pendingSpeed) != 0) {
                webVideoFull.pendingRate = pendingRate
                val viewGroup = webVideoFull.container
                if (viewGroup != null) {
                    viewGroup.postDelayed(ApplyRateStep(), 500L)
                    return true
                }
            }
            return true
        }
    }

    internal inner class HideTopIfSpeedMenuTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.speedMenu != null) {
                webVideoFull.hideTopLater()
            }
        }
    }

    internal inner class SeekProgressListener : DialogSeekListener {
        override fun onValue(newValue: Int) {
            val webVideoProgress = this@WebVideoFull.seekProgress
            if (webVideoProgress != null) {
                webVideoProgress.progress = newValue
            }
        }
    }

    internal inner class BrightDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.dismissBrightDialog()
        }
    }

    internal inner class ApplyBrightnessTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            webVideoFull.brightnessApplied = false
            webVideoFull.applyBrightness()
        }
    }

    internal inner class BrightProgressListener : DialogSeekListener {
        override fun onValue(newValue: Int) {
            val webVideoProgress = this@WebVideoFull.brightProgress
            if (webVideoProgress != null) {
                webVideoProgress.progress = newValue
            }
        }
    }

    internal inner class AudioDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.dismissAudioDialog()
        }
    }

    internal inner class EvalJsTask : Runnable {
        override fun run() {
            evalJs(this@WebVideoFull.page, "myChkPlay();", false)
        }
    }

    internal inner class ApplyDisplayCutoutTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val mainActivity = webVideoFull.ownerActivity
            if (mainActivity == null) {
                return
            }
            applyDisplayCutout(mainActivity.getWindow(), false, VideoSettings.cutOut.value)
        }
    }

    internal inner class ZoomBtnClick : OnClickListener {

        internal inner class ZoomStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val myButtonImage = webVideoFull.zoomBtn
                if (myButtonImage == null) {
                    return
                }
                if (webVideoFull.pinchShrink) {
                    showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
                } else if (webVideoFull.zoomMenu == null) {
                    webVideoFull.dismissZoomMenu()
                    val myFadeRelative = webVideoFull.controlsPanel
                    if (myFadeRelative != null) {
                        myFadeRelative.setAutoHide(false)
                    }
                    val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                    val length = MainConst.videoSizeNames.size
                    var index = 0
                    while (true) {
                        var loop = true
                        if (index >= length) {
                            break
                        }
                        val nameRes = MainConst.videoSizeNames[index]
                        if (index != VideoSettings.ratio.value) {
                            loop = false
                        }
                        arrayList.add(PopMenuItem(index, nameRes, loop))
                        index++
                    }
                    val myPopupMenu = MyPopupMenu(
                        webVideoFull.ownerActivity,
                        webVideoFull.container,
                        myButtonImage.anchor,
                        arrayList,
                        true,
                        webVideoFull.ZoomMenuListener()
                    )
                    webVideoFull.zoomMenu = myPopupMenu
                    val mainActivity = webVideoFull.ownerActivity
                    if (mainActivity != null) {
                        mainActivity.popupMenu = myPopupMenu
                    }
                    val viewGroup = webVideoFull.container
                    if (viewGroup != null) {
                        viewGroup.post(webVideoFull.HideTopIfZoomMenuTask())
                    }
                }
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(ZoomStep(), 100L)
        }
    }

    internal inner class HideDownloadBtnTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val myButtonImage = webVideoFull.downloadBtn
            if (myButtonImage == null) {
                return
            }
            webVideoFull.downShown = false
            myButtonImage.setVisibility(8)
        }
    }

    internal inner class RestartPlaybackTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val pausedState = webVideoFull.pausedState
            val webNestView = webVideoFull.page
            if (pausedState) {
                evalJs(webNestView, "myVidPause();", false)
            } else {
                evalJs(webNestView, "myVidPlay();", false)
            }
            webVideoFull.postLayout()
        }
    }

    internal inner class TogglePlayTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            evalJs(webVideoFull.page, "myVidToggle();", false)
            webVideoFull.postLayout()
        }
    }

    internal inner class ApplyVideoSizeTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            webVideoFull.setIconPlay(webVideoFull.videoPaused)
            if (webVideoFull.pipBuilder != null) {
                webVideoFull.applyVideoSize(false, false)
            }
        }
    }

    internal inner class SeekForwardTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            evalJs(webVideoFull.page, "myVidFrwd();", false)
            webVideoFull.postLayout()
        }
    }

    internal inner class SeekBackTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            evalJs(webVideoFull.page, "myVidFfwd();", false)
            webVideoFull.postLayout()
        }
    }

    internal inner class ClearSeekTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            webVideoFull.videoSeeked = false
            webVideoFull.seekTarget = -1L
        }
    }

    internal inner class DismissSpeedMenuTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val myButtonImage = webVideoFull.speedBtn
            val rateValue = webVideoFull.rateValue
            webVideoFull.rateValue = 0.0f
            if (webVideoFull.speedMenu == null) {
                webVideoFull.dismissSpeedMenu()
                if (myButtonImage != null) {
                    val myFadeRelative = webVideoFull.controlsPanel
                    if (myFadeRelative != null) {
                        myFadeRelative.setAutoHide(false)
                    }
                    val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                    var index = 0
                    while (true) {
                        val speeds: FloatArray = SPEEDS
                        if (index >= 8) {
                            break
                        }
                        val rate = speeds[index]
                        if (index == 3) {
                            arrayList.add(
                                PopMenuItem(
                                    index,
                                    AppR.string.normal_rate,
                                    (rate).compareTo(rateValue) == 0
                                )
                            )
                        } else {
                            arrayList.add(PopMenuItem(rate.toString(), (rate).compareTo(rateValue) == 0, index))
                        }
                        index++
                    }
                    webVideoFull.pendingSpeed = rateValue
                    val myPopupMenu = MyPopupMenu(
                        webVideoFull.ownerActivity,
                        webVideoFull.container,
                        myButtonImage.anchor,
                        arrayList,
                        true,
                        webVideoFull.SpeedMenuListener()
                    )
                    webVideoFull.speedMenu = myPopupMenu
                    val mainActivity = webVideoFull.ownerActivity
                    if (mainActivity != null) {
                        mainActivity.popupMenu = myPopupMenu
                    }
                    val viewGroup = webVideoFull.container
                    if (viewGroup != null) {
                        viewGroup.post(webVideoFull.HideTopIfSpeedMenuTask())
                    }
                }
            }
            webVideoFull.pageBusy = false
        }
    }

    internal inner class ApplyPageZoomTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.pinchShrink || webVideoFull.pipBuilder != null || webVideoFull.page == null) {
                return
            }
            if (webVideoFull.pinchApplied) {
                webVideoFull.applyPageZoom()
            } else {
                webVideoFull.videoSize
            }
        }
    }

    internal inner class ResetVideoSizeTask : Runnable {

        override fun run() {
            val webVideoFull = this@WebVideoFull
            val videoSizeText = webVideoFull.videoSizeText
            webVideoFull.videoSizeText = null
            webVideoFull.videoWidth = 0
            webVideoFull.videoHeight = 0
            var pinchScale = -1.0f
            if (!TextUtils.isEmpty(videoSizeText) && videoSizeText != "0") {
                val sizeParts: Array<String?>? = if (videoSizeText == "/") null else videoSizeText!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                if (videoSizeText == "/" || sizeParts == null || sizeParts.size != 2) {
                    pinchScale = 0.0f
                } else {
                    val videoWidthPx: Int = parsePx(sizeParts[0])
                    val videoHeightPx: Int = parsePx(sizeParts[1])
                    if (videoWidthPx == 0 || videoHeightPx == 0) {
                        pinchScale = 0.0f
                    } else {
                        webVideoFull.videoWidth = videoWidthPx
                        webVideoFull.videoHeight = videoHeightPx
                        pinchScale = (videoHeightPx / videoWidthPx).toFloat()
                    }
                }
            }
            webVideoFull.pinchScale = pinchScale
            webVideoFull.pinchApplied = true
            val pinchShrink = pinchScale < 0.0f
            webVideoFull.pinchShrink = pinchShrink
            if (pinchShrink) {
                return
            }
            webVideoFull.applyPageZoom()
        }
    }

    internal inner class LoopBtnClick : OnClickListener {

        internal inner class LoopStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                val context = webVideoFull.ctx
                if (context == null) {
                    return
                }
                if (webVideoFull.videoGone) {
                    showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
                    webVideoFull.postLayout()
                    return
                }
                val loop = !VideoSettings.loop.value
                VideoSettings.edit(context) { set(VideoSettings.loop, loop) }
                webVideoFull.updateLoopIcon()
                if (VideoSettings.loop.value) {
                    showToast(webVideoFull.ownerActivity, AppR.string.repeat_on)
                } else {
                    showToast(webVideoFull.ownerActivity, AppR.string.repeat_off)
                }
                webVideoFull.setVideoLoop(VideoSettings.loop.value)
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(LoopStep(), 100L)
        }
    }

    internal inner class HideControlsUnlessContinueTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val myFadeRelative: MyFadeRelative? = if (webVideoFull.youContinue) null else webVideoFull.controlsPanel
            if (webVideoFull.youContinue || myFadeRelative == null) {
                return
            }
            myFadeRelative.hide(false)
        }
    }

    internal inner class HideOnSeekEventTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.hideOnSeek()
        }
    }

    internal inner class HideOnSeekDialogTask : Runnable {
        override fun run() {
            val speeds: FloatArray? = SPEEDS
            this@WebVideoFull.hideOnSeek()
        }
    }

    internal inner class HandleMoveTask : Runnable {
        override fun run() {
            try {
                val webVideoFull = this@WebVideoFull
                val speeds: FloatArray? = SPEEDS
                webVideoFull.handleMove()
            } catch (unused: Exception) {
            }
        }
    }

    internal inner class FinishGestureTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            webVideoFull.touchHandled = false
            webVideoFull.finishGesture()
        }
    }

    internal inner class SeekToTouchTask : Runnable {
        override fun run() {
            val webVideoFull = this@WebVideoFull
            val seekTouchUpTime = webVideoFull.seekTouchUpTime
            if (seekTouchUpTime >= 0) {
                var duration = webVideoFull.duration
                val seekMs = (seekTouchUpTime / 1000.0f) * duration
                val seekSec = seekMs / 1000.0f
                val seekLong = seekMs.toLong()
                if (seekLong <= duration) {
                    duration = seekLong
                }
                webVideoFull.seekTarget = if (duration >= 0) duration else 0L
                if (webVideoFull.videoSeeked || webVideoFull.page == null) {
                    return
                }
                evalJs(webVideoFull.page, "myVidSeekPos(" + seekSec + ");", false)
            }
        }
    }

    internal inner class SpeedBtnClick : OnClickListener {

        internal inner class SpeedStep : Runnable {
            override fun run() {
                val webVideoFull = this@WebVideoFull
                if (webVideoFull.videoGone) {
                    showToast(webVideoFull.ownerActivity, AppR.string.not_support_video)
                } else {
                    webVideoFull.videoRate
                }
                webVideoFull.postLayout()
            }
        }

        override fun onClick(view: View?) {
            val webVideoFull = this@WebVideoFull
            if (webVideoFull.clickLocked) {
                return
            }
            webVideoFull.clickLocked = true
            val viewGroup = webVideoFull.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(SpeedStep(), 100L)
        }
    }

    init {
        this.hideRunnable = ApplyBrightnessTask()
        this.moveRunnable = HandleMoveTask()
        this.longPressRunnable = FinishGestureTask()
    }

    private val position: Unit
        get() {
            val webNestView: WebNestView? = if (this.videoGone || this.videoPosition >= 0) null else this.page
            if (this.videoGone || this.videoPosition >= 0 || webNestView == null || this.positionRequested) {
                return
            }
            this.positionRequested = true
            if (this.duration > 0) {
                evalJs(webNestView, "myVidPos();", true)
            } else {
                evalJs(webNestView, "myVidTime();", true)
            }
        }

    val videoRate: Unit

        get() {
            val webNestView = this.page
            if (webNestView == null || this.pageBusy) {
                return
            }
            this.pageBusy = true
            evalJs(webNestView, "myVidRate();", false)
        }

    val videoSize: Unit

        get() {
            val webNestView: WebNestView? = if (this.pipBuilder != null) null else this.page
            if (this.pipBuilder != null || webNestView == null) {
                return
            }
            evalJs(webNestView, "myVidSize();", false)
        }

    fun setIconPlay(videoPaused: Boolean) {
        val myButtonImage = this.playBtn
        if (myButtonImage == null) {
            return
        }
        if (videoPaused) {
            myButtonImage.setImageResource(AppR.drawable.baseline_play_arrow_white_24)
        } else {
            myButtonImage.setImageResource(AppR.drawable.baseline_pause_white_24)
        }
    }

    fun setIconShow(shown: Boolean) {
        if (this.backBtn == null) {
            return
        }
        if (shown) {
            updateLift()
            if (this.videoKind.toInt() == 2) {
                this.titleText!!.setVisibility(0)
            }
            this.backBtn!!.setVisibility(0)
            this.topRow!!.setVisibility(0)
            this.lockCheck!!.setVisibility(0)
            if (!this.videoReady || this.videoGone) {
                this.rewindBtn!!.setVisibility(8)
                this.playBtn!!.setVisibility(8)
                this.forwardBtn!!.setVisibility(8)
            } else {
                this.rewindBtn!!.setVisibility(0)
                this.playBtn!!.setVisibility(0)
                this.forwardBtn!!.setVisibility(0)
            }
            val videoKind = this.videoKind
            if (videoKind.toInt() == 2 || videoKind.toInt() == 3) {
                this.downShown = false
                this.downloadBtn!!.setVisibility(8)
            } else {
                val downloadable = this.downloadable
                this.downShown = downloadable
                this.downloadBtn!!.setVisibility(if (downloadable) 0 else 8)
            }
            this.brightBtn!!.setVisibility(0)
            this.brightText!!.setVisibility(0)
            this.volumeBtn!!.setVisibility(0)
            this.volumeText!!.setVisibility(0)
        } else {
            this.titleText!!.setVisibility(8)
            this.backBtn!!.setVisibility(8)
            this.topRow!!.setVisibility(8)
            this.lockCheck!!.setVisibility(8)
            this.rewindBtn!!.setVisibility(8)
            this.playBtn!!.setVisibility(8)
            this.forwardBtn!!.setVisibility(8)
            this.downShown = false
            this.downloadBtn!!.setVisibility(8)
            this.brightBtn!!.setVisibility(8)
            this.brightText!!.setVisibility(8)
            this.volumeBtn!!.setVisibility(8)
            this.volumeText!!.setVisibility(8)
        }
        applyShowTime()
    }

    fun setPipParam2(enabled: Boolean) {
        val playIcon: Int
        val action: String
        val rewindIcon: Int
        val forwardIcon: Int
        val context: Context? = this.ctx
        if (context == null || this.pipBuilder == null) {
            return
        }
        try {
            if (this.videoReady && !this.videoGone) {
                val videoPaused = this.videoPaused
                val packageName = context.getPackageName()
                val intent = Intent("ch.opumlabs.opumbrowser.ACTION_PIP_FRWD")
                intent.setPackage(packageName)
                val broadcast = PendingIntent.getBroadcast(this.ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                val playIntent = Intent("ch.opumlabs.opumbrowser.ACTION_PIP_PLAY")
                playIntent.setPackage(packageName)
                val playBroadcast = PendingIntent.getBroadcast(this.ctx, 1, playIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                val ffwdIntent = Intent("ch.opumlabs.opumbrowser.ACTION_PIP_FFWD")
                ffwdIntent.setPackage(packageName)
                val ffwdBroadcast = PendingIntent.getBroadcast(this.ctx, 2, ffwdIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                if (videoPaused) {
                    playIcon = AppR.drawable.baseline_play_arrow_white_24
                    action = "play"
                } else {
                    playIcon = AppR.drawable.baseline_pause_white_24
                    action = "pause"
                }
                if (this.layoutRtl) {
                    rewindIcon = AppR.drawable.baseline_fast_forward_white_24
                    forwardIcon = AppR.drawable.baseline_fast_rewind_white_24
                } else {
                    rewindIcon = AppR.drawable.baseline_fast_rewind_white_24
                    forwardIcon = AppR.drawable.baseline_fast_forward_white_24
                }
                val iconCreateWithResource = Icon.createWithResource(this.ctx, rewindIcon)
                val createWithResource = Icon.createWithResource(this.ctx, playIcon)
                val ffwdIcon = Icon.createWithResource(this.ctx, forwardIcon)
                val arrayList: ArrayList<RemoteAction?> = ArrayList()
                arrayList.add(RemoteAction(iconCreateWithResource, "frwd", "frwd", broadcast))
                arrayList.add(RemoteAction(createWithResource, action, action, playBroadcast))
                arrayList.add(RemoteAction(ffwdIcon, "ffwd", "ffwd", ffwdBroadcast))
                this.pipBuilder!!.setActions(arrayList)
            }
            val mainActivity = this.ownerActivity
            val builder = this.pipBuilder
            if (!enabled) {
                mainActivity!!.setPictureInPictureParams(builder!!.build())
                return
            }
            mainActivity!!.enterPictureInPictureMode(builder!!.build())
            val viewGroup = this.container
            if (viewGroup != null) {
                viewGroup.post(PipBrightnessTask())
            }
        } catch (unused: Exception) {
        }
    }

    private fun setSeekTouchUp(seekTouchUpTime: Long) {
        if (seekTouchUpTime >= 0) {
            this.seekTouchUpTime = seekTouchUpTime
            val viewGroup = this.container
            if (viewGroup == null) {
                return
            }
            viewGroup.postDelayed(SeekToTouchTask(), 100L)
        }
    }

    fun setTouchLock(locked: Boolean) {
        val myButtonCheck = this.lockCheck
        if (myButtonCheck == null || locked == myButtonCheck.isChecked) {
            return
        }
        if (locked) {
            setIconShow(false)
            this.lockCheck!!.setVisibility(0)
            this.lockCheck!!.setChecked(true, true)
            this.notiCheck!!.setVisibility(8)
            showToast(this.ownerActivity, AppR.string.touch_locked)
            return
        }
        setIconShow(PdfSettings.videoIcon.value)
        this.lockCheck!!.setVisibility(0)
        this.lockCheck!!.setChecked(false, true)
        this.notiCheck!!.setVisibility(0)
        showToast(this.ownerActivity, AppR.string.touch_unlocked)
    }

    private fun setTouchMove(motionEvent: MotionEvent?) {
        this.lastEvent = motionEvent
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        val runnable = this.moveRunnable
        viewGroup.removeCallbacks(runnable)
        this.container!!.post(runnable)
    }

    fun setVideoLoop(loop: Boolean) {
        if (this.page == null) {
            return
        }
        evalJs(this.page, "myVidLoop(" + loop + ");", true)
    }

    private fun setVideoPaused(pausedState: Boolean) {
        if (this.clickLocked) {
            return
        }
        this.clickLocked = true
        this.pausedState = pausedState
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.postDelayed(RestartPlaybackTask(), 100L)
    }

    fun enterPip() {
        if (this.pinchShrink || this.pipBuilder != null || this.page == null) {
            return
        }
        if (!this.videoReady) {
            this.pipPending = true
            return
        }
        val viewGroup = this.container
        if (viewGroup != null) {
            viewGroup.postDelayed(ApplyPageZoomTask(), 200L)
        }
    }

    fun hideOnSeek() {
        if (this.youExpanded || this.youDialog) {
            hideControlsNow()
        }
    }

    fun dismissAudioDialog() {
        val dialogSeekAudio = this.audioDialog
        if (dialogSeekAudio != null) {
            dialogSeekAudio.dismiss()
            this.audioDialog = null
            this.dialogOpen = false
            applyScreenRotation(this.ownerActivity, false)
        }
    }

    fun dismissBrightDialog() {
        val dialogSeekBright = this.brightDialog
        if (dialogSeekBright != null) {
            dialogSeekBright.dismiss()
            this.brightDialog = null
            this.dialogOpen = false
            applyScreenRotation(this.ownerActivity, false)
        }
    }

    fun dismissSpeedMenu() {
        if (this.speedMenu != null) {
            val mainActivity = this.ownerActivity
            if (mainActivity != null) {
                mainActivity.popupMenu = null
            }
            hideMenuLater()
            this.speedMenu!!.dismissNow()
            this.speedMenu = null
        }
        val myFadeRelative = this.controlsPanel
        if (myFadeRelative != null) {
            myFadeRelative.setAutoHide(true)
        }
    }

    fun dismissRotationMenu() {
        if (this.rotationMenu != null) {
            val mainActivity = this.ownerActivity
            if (mainActivity != null) {
                mainActivity.popupMenu = null
            }
            hideMenuLater()
            this.rotationMenu!!.dismissNow()
            this.rotationMenu = null
        }
        val myFadeRelative = this.controlsPanel
        if (myFadeRelative != null) {
            myFadeRelative.setAutoHide(true)
        }
    }

    fun dismissZoomMenu() {
        if (this.zoomMenu != null) {
            val mainActivity = this.ownerActivity
            if (mainActivity != null) {
                mainActivity.popupMenu = null
            }
            hideMenuLater()
            this.zoomMenu!!.dismissNow()
            this.zoomMenu = null
        }
        val myFadeRelative = this.controlsPanel
        if (myFadeRelative != null) {
            myFadeRelative.setAutoHide(true)
        }
    }

    fun initControls() {
        if (this.sizeFrame != null) {
            setTitle(this.videoTitle)
            this.sizeFrame!!.setListener(VideoSizeListener())
            this.controlsPanel!!.setVisibleListener(ControlsVisibleListener())
            this.seekProgress!!.start(1, BrightnessProgressListener())
            this.brightProgress!!.start(2, null)
            this.volumeProgress!!.start(3, VolumeProgressListener())
            this.backBtn!!.setOnClickListener(BackBtnClick())
            updateRotateIcon()
            this.rotateBtn!!.setOnClickListener(DismissRotationMenuClick())
            this.zoomBtn!!.setOnClickListener(ZoomBtnClick())
            updateLoopIcon()
            this.loopBtn!!.setOnClickListener(LoopBtnClick())
            this.speedBtn!!.setOnClickListener(SpeedBtnClick())
            this.pipBtn!!.setOnClickListener(EnterPipModeClick())
            this.settingsBtn!!.setNoti(VideoSettings.dotSettings.value)
            this.settingsBtn!!.setOnClickListener(SetNotiClick())
            this.lockCheck!!.setCheckArea(true)
            this.lockCheck!!.setOnClickListener(LockIntentClick())
            this.rewindBtn!!.setOnClickListener(SeekBackClick())
            this.playBtn!!.setOnClickListener(TogglePlayClick())
            this.forwardBtn!!.setOnClickListener(SeekForwardClick())
            this.notiCheck!!.setNoti(VideoSettings.dotIcon.value)
            this.notiCheck!!.setAlpha(if (PdfSettings.videoIcon.value) 1.0f else 0.4f)
            this.notiCheck!!.setChecked(PdfSettings.videoIcon.value, false)
            this.notiCheck!!.setOnClickListener(SetIconShowClick())
            this.downloadBtn!!.setOnClickListener(DownloadClick())
            this.brightBtn!!.setOnClickListener(ShowBrightDialogClick())
            this.volumeBtn!!.setOnClickListener(ShowAudioDialogClick())
            this.gestures = GestureDetector(this.ctx, SeekForwardGestures())
            setIconShow(PdfSettings.videoIcon.value)
            if (this.videoKind.toInt() == 2 && !this.liftShown && this.liftSpacer1 != null) {
                this.liftShown = true
                val viewGroup = this.container
                if (viewGroup != null) {
                    viewGroup.post(UpdateLiftTask())
                }
            }
            addView(this.sizeFrame, LayoutParams(-1, -1).also { it.bottomMargin = this.gestureLift })
            val guidePlayer = VideoSettings.guidePlayer.value
            if (!guidePlayer) {
                if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value) {
                    showLongPressGuide(false)
                    return
                }
                return
            }
            val viewGroup: ViewGroup? = if (!guidePlayer || this.doubleTapGuide != null || this.sizeFrame == null) null else this.container
            if (!guidePlayer || this.doubleTapGuide != null || this.sizeFrame == null || viewGroup == null) {
                return
            }
            viewGroup.post(ShowDoubleTapGuideTask())
        }
    }

    fun resetTouch() {
        this.touchMoved = false
        this.gesture = 0.toByte()
        this.seekMoved = false
        this.longPressed = false
        this.seekBase = -1L
    }

    fun injectVideoJs() {
        if (this.page == null) {
            return
        }
        val sb = StringBuilder("var vdEle=null;var vdSeek=false;")
        val videoKind = this.videoKind
        if (videoKind.toInt() == 2 || videoKind.toInt() == 3) {
            sb.append("var youConEle=null;var youConVal=null;var youConOsb=null;var youExpEle=null;var youExpVal=null;var youExpOsb=null;function myYouCon(name){return (name&&name.endsWith('fadein'))?1:0;}")
        }
        sb.append(
            "function myVidPlaying(){android.onVidResult('onVidPaused',0);}function myVidPaused(){android.onVidResult('onVidPaused',1);}function myVidSeeking(){vdSeek=true;android.onVidResult('onVidSeek',1);}function myVidSeeked(){vdSeek=false;android.onVidResult('onVidSeek',0);}function myVidPlay(){if(vdEle){vdEle.play();}}function myChkPlay(){if(vdEle){if(vdEle.paused){vdEle.play();}}}function myVidPause(){if(vdEle){vdEle.pause();}}function myVidToggle(){if(vdEle){if(vdEle.paused){vdEle.play();}else{vdEle.pause();}}}function myVidDur(){if(vdEle){var val=vdEle.duration;if(val){vdEle.removeEventListener('timeupdate',myVidDur);android.onVidResult('onVidDur',val);}}}function myVidPos(){var val='undefined';if(vdEle){val=vdEle.currentTime;}android.onVidResult('onVidPos',val);}function myVidTime(){var val='undefined';var va2='undefined';if(vdEle){val=vdEle.duration;va2=vdEle.currentTime;}android.onVidTime(val,va2);}function myVidFrwd(){if(vdEle&&!vdSeek){var pos=vdEle.currentTime;if(pos>10){vdEle.currentTime=pos-10;}else{vdEle.currentTime=0;}}}function myVidFfwd(){if(vdEle&&!vdSeek){var pos=vdEle.currentTime;if(pos+10<vdEle.duration){vdEle.currentTime=pos+10;}else{vdEle.currentTime=vdEle.duration;}}}function myVidSeekPos(time){if(vdEle&&!vdSeek){vdEle.currentTime=time;}}function myVidLoop(loop){if(vdEle){vdEle.loop=loop;}}function myVidRate(){var val=1;if(vdEle){val=vdEle.playbackRate;}android.onVidResult('onVidRate',val);}function myVidRat2(rate){var val=0;if(vdEle){vdEle.playbackRate=rate;val=rate;}android.onVidResult('onVidRat2',val);}function myVidSize(){var val=0;if(vdEle){var vst=vdEle.style;val=vst.width+'/'+vst.height;}android.onVidResult('onVidSize',val);}(function(){if(vdEle)return;var eles=document.querySelectorAll(\"video\");if(eles&&(eles.length>0)){if(eles.length==1){vdEle=eles[0];}else{for(var i=0;i<eles.length;i++){if(!eles[i].paused&&eles[i].duration){vdEle=eles[i];break;}}if(!vdEle){vdEle=eles[0];}}}if(!vdEle){eles=document.querySelectorAll(\"source[type*='video']\");if(eles&&(eles.length>0)){if(eles.length==1){vdEle=eles[0].parentNode;}else{for(var i=0;i<eles.length;i++){if(!eles[i].parentNode.paused&&eles[i].parentNode.duration){vdEle=eles[i].parentNode;break;}}if(!vdEle){vdEle=eles[0].parentNode;}}}}if(!vdEle){android.onVidFound(false,false,null);return;}vdEle.addEventListener('playing',myVidPlaying);vdEle.addEventListener('pause',myVidPaused);vdEle.addEventListener('seeking',myVidSeeking);vdEle.addEventListener('seeked',myVidSeeked);"
        )
        val videoKindNow = this.videoKind
        if (videoKindNow.toInt() == 2 || videoKindNow.toInt() == 3) {
            sb.append("youConEle=document.querySelector(\"div[id='player-control-overlay']\");if(youConEle){youConVal=myYouCon(youConEle.className);android.onVidResult('onYouCon',youConVal);youConOsb=new MutationObserver(function(){var val=myYouCon(youConEle.className);if(val!=youConVal){youConVal=val;android.onVidResult('onYouCon',youConVal);}});youConOsb.observe(youConEle,{attributes:true,childList:true});}youExpEle=document.querySelector(\"player-fullscreen-top-controls\");if(youExpEle){youExpVal=youExpEle.style.visibility;android.onVidResult('onYouExp',youExpVal);youExpOsb=new MutationObserver(function(){var val=youExpEle.style.visibility;if(youExpVal!=val){youExpVal=val;android.onVidResult('onYouExp',youExpVal);}});youExpOsb.observe(youExpEle,{attributes:true,childList:true});}")
        }
        sb.append("var psd=vdEle.paused;var dur=vdEle.duration;if(!dur){vdEle.addEventListener('timeupdate',myVidDur);}android.onVidFound(true,psd,dur);vdEle.loop=")
        sb.append(VideoSettings.loop.value)
        sb.append(";")
        if (this.videoKind.toInt() == 2) {
            sb.append("if(psd){vdEle.play();}")
        }
        sb.append("})();")
        evalJs(this.page, sb.toString(), true)
    }

    fun isControlPressed(longPress: Boolean): Boolean {
        val myButtonImage = this.backBtn
        if (myButtonImage == null) {
            return false
        }
        // the new bottom row's "more" button and the tray's brightness / volume are buttons too (2026-11-20)
        if (myButtonImage.isPressed() || this.lockCheck!!.isPressed() || this.rewindBtn!!.isPressed() || this.playBtn!!.isPressed() || this.forwardBtn!!.isPressed() || this.notiCheck!!.isPressed() || this.downloadBtn!!.isPressed() ||
            this.controls?.more?.isPressed() == true || this.brightBtn?.isPressed() == true || this.volumeBtn?.isPressed() == true) {
            return true
        }
        if (longPress) {
            return false
        }
        return this.rotateBtn!!.isPressed() || this.zoomBtn!!.isPressed() || this.loopBtn!!.isPressed() || this.speedBtn!!.isPressed() || this.pipBtn!!.isPressed() || this.settingsBtn!!.isPressed()
    }

    fun inZone(x: Int, y: Int): Boolean {
        if (this.sizeKnown) {
            return false
        }
        val zoneLeft = this.zoneLeft
        val zoneTop = this.zoneTop
        val zoneRight = this.zoneRight
        val zoneBottom = this.zoneBottom
        return zoneLeft < zoneRight && zoneTop < zoneBottom && x >= zoneLeft && x < zoneRight && y >= zoneTop && y < zoneBottom
    }

    fun markVideoGone() {
        if (this.pipBuilder != null) {
            return
        }
        this.videoGone = true
        this.duration = -1L
        this.videoPosition = -1L
        this.seekTarget = -1L
    }

    fun postLayout() {
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.postDelayed(UnlockClickTask(), 100L)
    }

    fun runInBackground(runnable: Runnable?) {
        var executorServiceH = this.executorCache
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(this.ctx)
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

    fun applyShowTime() {
        val myFadeRelative = this.controlsPanel
        if (myFadeRelative == null) {
            return
        }
        val videoKind = this.videoKind
        if (videoKind.toInt() == 2 || videoKind.toInt() == 3) {
            myFadeRelative.setShowTime(2000)
        } else if (PdfSettings.videoIcon.value) {
            myFadeRelative.setShowTime(2000)
        } else {
            myFadeRelative.setShowTime(1000)
        }
    }

    fun showControls(delayed: Boolean, force: Boolean): Boolean {
        var myFadeRelative: MyFadeRelative? = null
        var myButtonCheck: VfCheck? = null
        if (isControlPressed(false) || (!(force || (this.doubleTapGuide == null && this.longPressGuide == null)) || (this.controlsPanel.also {
                myFadeRelative = it
            }) == null || (this.lockCheck.also { myButtonCheck = it }) == null)) {
            return false
        }
        if (this.pipBuilder != null) {
            val viewGroup = this.container
            if (viewGroup != null) {
                viewGroup.post(HideControlsTask())
                return true
            }
        } else if (myButtonCheck!!.isChecked) {
            val viewGroup = this.container
            if (viewGroup != null) {
                viewGroup.post(RestartControlsTask())
                return true
            }
        } else if (this.youExpanded || this.youDialog || myFadeRelative!!.isShowingNow()) {
            val viewGroup = this.container
            if (viewGroup != null) {
                viewGroup.post(HideControlsTask())
                return true
            }
        } else if (this.youContinue && !delayed) {
            val viewGroup = this.container
            if (viewGroup != null) {
                viewGroup.post(HideControlsTask())
                return true
            }
        } else if (PdfSettings.iconLong.value == delayed) {
            if (!this.videoReady && !this.videoGone && !this.scriptPending) {
                runInBackground(InjectVideoJsTask())
            }
            val viewGroup = this.container
            if (viewGroup != null) {
                viewGroup.post(HideOnSeekTask())
                return true
            }
        }
        return true
    }

    fun updateLoopIcon() {
        val myButtonImage = this.loopBtn
        if (myButtonImage == null) {
            return
        }
        if (VideoSettings.loop.value) {
            myButtonImage.setImageResource(AppR.drawable.outline_repeat_white_24)
        } else {
            myButtonImage.setImageResource(AppR.drawable.outline_arrow_right_alt_white_24)
        }
    }

    fun updateRotateIcon() {
        val myButtonImage = this.rotateBtn
        if (myButtonImage == null) {
            return
        }
        val rotation = VideoSettings.rotation.value
        if (rotation == 1) {
            myButtonImage.setImageResource(AppR.drawable.outline_screen_lock_portrait_white_24)
        } else if (rotation == 2) {
            myButtonImage.setImageResource(AppR.drawable.outline_screen_lock_landscape_white_24)
        } else {
            myButtonImage.setImageResource(AppR.drawable.outline_screen_rotation_white_24)
        }
    }

    override fun setupFrame(title: String?, downloadable: Boolean, videoFrameListener: VideoFrameListener?) {
        this.videoTitle = title
        this.frameListener = videoFrameListener
        setTitle(title)
        setVideoDown(downloadable)
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.post(SetBackgroundColorTask())
    }

    fun applyVideoSize(manualSize: Boolean, manual: Boolean) {
        if (this.ctx == null) {
            return
        }
        var videoWidth = this.videoWidth
        var videoHeight = this.videoHeight
        if (videoWidth == 0 || videoHeight == 0) {
            videoWidth = 1280
            videoHeight = 720
        }
        val builderA = android.app.PictureInPictureParams.Builder()
        this.pipBuilder = builderA
        builderA.setAspectRatio(Rational(videoWidth, videoHeight))
        if (!manualSize || manual) {
            runInBackground(SetPipParamTask(this, manualSize))
        } else {
            setPipParam2(manualSize)
        }
    }

    override fun showVideo(
        mainActivity: MainActivity?,
        viewGroup: ViewGroup?,
        webNestView: WebNestView?,
        pageUrl: String?,
        mode: Int,
        view: View?,
        customViewCallback: CustomViewCallback?
    ) {
        if (viewGroup == null || webNestView == null) {
            return
        }
        this.scriptPending = true
        this.ownerActivity = mainActivity
        this.container = viewGroup
        this.page = webNestView
        this.videoUrl = pageUrl
        this.videoKind = (mode).toByte()
        this.touchView = view
        this.viewCallback = customViewCallback
        this.reloadPending = true
        this.sizeKnown = true
        this.duration = -1L
        this.videoPosition = -1L
        this.seekTarget = -1L
        if (mode == 2) {
            this.bottomInset = MainApp.dp64 + MainApp.dp16
        } else {
            this.bottomInset = MainApp.dp64
        }
        this.cutOut = VideoSettings.cutOut.value
        // On the browser screen the full-screen video goes into the Compose overlay stack (top band), so it
        // covers the bars, which are Compose layers above the page body now.
        val stack = (viewGroup as? com.mycompany.app.view.MyWebBody)?.overlays
        if (stack != null) stack.addView(this, -1, -1) else viewGroup.addView(this, -1, -1)
        // the page's video (with its own controls) and OPUM's controls end above the gesture area
        this.gestureLift = gestureAreaHeight(viewGroup)
        this.bottomInset += this.gestureLift
        addView(this.touchView, LayoutParams(-1, -1).also { it.bottomMargin = this.gestureLift })
    }

    /** Height of the bottom gesture area with gesture navigation; 0 with the 3-button bar (it has no such strip). */
    private fun gestureAreaHeight(view: View): Int {
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(view) ?: return 0
        return try {
            val buttons = insets.getInsetsIgnoringVisibility(androidx.core.view.WindowInsetsCompat.Type.tappableElement()).bottom
            if (buttons > 0) 0
            else insets.getInsetsIgnoringVisibility(androidx.core.view.WindowInsetsCompat.Type.mandatorySystemGestures()).bottom
        } catch (_: Exception) {
            0
        }
    }

    fun hideMenuLater() {
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        this.menuHidePending = true
        viewGroup.postDelayed(ClearMenuHidePendingTask(), 800L)
    }

    override fun updateDownIcon() {
        val videoKind = this.videoKind
        var found = false
        val canDownload = if (videoKind.toInt() == 2 || videoKind.toInt() == 3) false else true
        if (this.downloadBtn != null && canDownload && PdfSettings.videoIcon.value && !this.lockCheck!!.isChecked) {
            found = true
        }
        if (canDownload == this.downloadable && found == this.downShown) {
            return
        }
        this.downStateChanged = true
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.post(SetVideoDownTask())
    }

    fun hideTopLater() {
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        this.topHidePending = true
        viewGroup.postDelayed(ClearTopHidePendingTask(), 800L)
    }

    /**
     * Enters picture-in-picture (Android 8+, when allowed); `manual` = requested by the user
     * (shows hints), otherwise automatic on leaving the app when VideoSettings.pipOnHome.value is on.
     */
    override fun enterPipMode(manual: Boolean) {
        if (this.ownerActivity == null || !(manual || VideoSettings.pipOnHome.value) || this.pipAuto) {
            return
        }
        if (hasPermission(this.ctx, 6)) {
            if (manual) {
                openPermissionScreen(this.ownerActivity, 6)
            }
            return
        }
        if (this.videoKind.toInt() == 2 && this.frameListener != null) {
            val activity = this.frameListener!!.activity()
            if (activity != null && activity.videoState.videoPip == null) {
                val pip = WebVideoPip(activity)
                pip.appContext = activity
                if (this.videoWidth != 0 && this.videoHeight != 0) {
                    pip.pipWidth = this.videoWidth
                    pip.pipHeight = this.videoHeight
                    pip.youSizeSet = true
                }
                activity.videoState.videoPip = pip
                if (activity.mainHandler != null) {
                    activity.mainHandler!!.post((this.frameListener as WvaVideoFrameListener).PipFrameStep())
                }
            }
            this.seekPending = true
            this.videoPosition = -1L
            this.position
        }
        if (this.controlsPanel != null) {
            this.controlsPanel!!.hide(false)
        }
        dismissBrightDialog()
        dismissAudioDialog()
        dismissRotationMenu()
        dismissZoomMenu()
        dismissSpeedMenu()
        applyVideoSize(true, manual)
    }

    fun applyBrightness() {
        val mainActivity: MainActivity? = if (this.container == null) null else this.ownerActivity
        if (this.container == null || mainActivity == null || this.brightnessApplied) {
            return
        }
        val brightnessState = this.brightnessState
        val brightness = VideoSettings.brightness.value
        if (brightnessState != brightness) {
            this.brightnessApplied = true
            this.brightnessState = brightness
            MainUtil.applyWindowBrightness(mainActivity.getWindow(), VideoSettings.brightness.value, VideoSettings.userBrightness.value)
            val viewGroup = this.container
            val runnable = this.hideRunnable
            viewGroup!!.removeCallbacks(runnable)
            this.container!!.postDelayed(runnable, 100L)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val viewGroup: ViewGroup?
        if (VideoSettings.guidePlayer.value || this.doubleTapGuide != null || this.longPressGuide != null || this.pipBuilder != null || event == null || this.seekProgress == null || this.brightProgress == null || this.volumeProgress == null) {
            return super.dispatchTouchEvent(event)
        }
        if (this.touchHandled) {
            resetTouch()
            return true
        }
        val myButtonCheck = this.lockCheck
        if (myButtonCheck != null && myButtonCheck.isChecked) {
            resetTouch()
            val gestureDetector = this.gestures
            if (gestureDetector != null) {
                gestureDetector.onTouchEvent(event)
            }
            this.lockCheck!!.onTouchEvent(event)
            return true
        }
        if (this.doubleTapped) {
            resetTouch()
            val actionMasked = event.getActionMasked()
            if (actionMasked == 1 || actionMasked == 3) {
                this.doubleTapped = false
            }
            val gestureDetector = this.gestures
            if (gestureDetector != null) {
                gestureDetector.onTouchEvent(event)
            }
            return true
        }
        val actionMasked = event.getActionMasked()
        if (actionMasked == 0) {
            resetTouch()
            val videoKind = this.videoKind
            if ((videoKind.toInt() != 2 && videoKind.toInt() != 3) || (!this.youExpanded && !this.youDialog)) {
                this.downX = event.getX()
                this.downY = event.getY()
                val myFadeRelative = this.controlsPanel
                if (myFadeRelative == null || myFadeRelative.getVisibility() != View.VISIBLE || (!inZone(
                        this.downX.toInt(),
                        this.downY.toInt()
                    ) && !isControlPressed(true))) {
                    val downX = this.downX
                    if (downX >= MainApp.dp20) {
                        val width = getWidth()
                        val dp20Px = MainApp.dp20
                        if (downX <= width - dp20Px) {
                            val downY = this.downY
                            if (downY >= dp20Px && downY <= getHeight() - MainApp.dp20) {
                                this.touchMoved = true
                                this.videoPosition = -1L
                            }
                        }
                    }
                }
            }
        } else if (actionMasked == 1) {
            this.lastEvent = null
            if (!this.touchHandled) {
                this.touchHandled = true
                this.lastGesture = this.gesture
                this.lastSeekBase = this.seekBase
                viewGroup = this.container
                if (viewGroup != null) {
                    val runnable = this.longPressRunnable
                    viewGroup.removeCallbacks(runnable)
                    this.container!!.post(runnable)
                }
            }
            resetTouch()
        } else if (actionMasked == 2) {
            setTouchMove(event)
        } else if (actionMasked == 3) {
            this.lastEvent = null
            if (!this.touchHandled) {
                this.touchHandled = true
                this.lastGesture = this.gesture
                this.lastSeekBase = this.seekBase
                viewGroup = this.container
                if (viewGroup != null) {
                    val runnable = this.longPressRunnable
                    viewGroup.removeCallbacks(runnable)
                    this.container!!.post(runnable)
                }
            }
            resetTouch()
        }
        val gestureDetector = this.gestures
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onPipEnded() {
        this.pipBuilder = null
        enterPip()
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.post(ApplyWindowBrightnessTask())
    }

    fun handleMove() {
        var lastEvent = this.lastEvent
        if (!this.touchHandled && lastEvent != null && this.container != null) {
            if (this.touchMoved) {
                if (this.youExpanded || this.youDialog) {
                    this.hideControlsNow()
                    return
                }

                var controlsPanel = this.controlsPanel
                if (controlsPanel != null && controlsPanel.getVisibility() == View.VISIBLE && (this.inZone(
                        lastEvent.getX().toInt(),
                        lastEvent.getY().toInt()
                    ) || this.isControlPressed(true))) {
                    this.resetTouch()
                    this.seekProgress!!.hide(true, false)
                    this.brightProgress!!.hide(true, false)
                    this.volumeProgress!!.hide(true, false)
                    return
                }

                var gesture = this.gesture.toInt()
                if (gesture == 0) {
                    var x = lastEvent.getX()
                    val distX = abs(x - this.downX)
                    val distY = abs(lastEvent.getY() - this.downY)
                    if (this.longPressed) {
                        x = MainApp.dp10.toFloat()
                        if (distX > x || distY > x) {
                            this.resetTouch()
                            val controlsPanelNow = this.controlsPanel
                            if (controlsPanelNow != null) {
                                controlsPanelNow.hide(false)
                            }

                            this.seekProgress!!.hide(false, false)
                            this.brightProgress!!.hide(false, false)
                            this.volumeProgress!!.hide(false, false)
                            return
                        }
                    } else if (distX > distY) {
                        if (distX > MainApp.dp10) {
                            run label142@{
                            if (VideoSettings.dragSeek.value && this.downY < this.getHeight() - this.bottomInset) {
                                if (this.videoSeeked) {
                                    val seekTarget = this.seekTarget
                                    this.videoPosition = seekTarget
                                    if (this.duration <= 0L || seekTarget < 0L) {
                                        return@label142
                                    }
                                }

                                this.position
                                if (this.duration > 0L && this.videoPosition >= 0L) {
                                    this.seekMoved = false
                                    this.gesture = 3
                                    controlsPanel = this.controlsPanel
                                    if (controlsPanel != null) {
                                        controlsPanel.hide(false)
                                    }

                                    this.seekProgress!!.hide(false, false)
                                    this.brightProgress!!.hide(false, false)
                                    this.volumeProgress!!.showSeek(this.duration, this.videoPosition)
                                    if (this.gesture.toInt() != 0 && this.touchView != null) {
                                        lastEvent = MotionEvent.obtain(lastEvent)
                                        lastEvent.setAction(1)
                                        this.touchView!!.dispatchTouchEvent(lastEvent)
                                        return
                                    }

                                    return
                                }

                                if (!this.seekMoved && this.videoGone && !this.seekStarted) {
                                    this.seekStarted = true
                                    showToast(this.ownerActivity, AppR.string.not_support_video)
                                }

                                this.seekMoved = true
                                val controlsPanelNow = this.controlsPanel
                                if (controlsPanelNow != null) {
                                    controlsPanelNow.hide(false)
                                }

                                this.seekProgress!!.hide(false, false)
                                this.brightProgress!!.hide(false, false)
                                return
                            }
                            }

                            this.seekMoved = true
                            val controlsPanelNow = this.controlsPanel
                            if (controlsPanelNow != null) {
                                controlsPanelNow.hide(false)
                            }

                            this.seekProgress!!.hide(false, false)
                            this.brightProgress!!.hide(false, false)
                            return
                        }
                    } else if (!this.seekMoved && distY > MainApp.dp10) {
                        if (x < this.getWidth() / 2.0f) {
                            if (VideoSettings.dragBright.value) {
                                this.gesture = 1
                                controlsPanel = this.controlsPanel
                                if (controlsPanel != null) {
                                    controlsPanel.hide(false)
                                }

                                this.brightProgress!!.hide(false, false)
                                this.volumeProgress!!.hide(false, false)
                                this.seekProgress!!.applyBrightness()
                            }
                        } else if (VideoSettings.dragVolume.value) {
                            this.gesture = 2
                            controlsPanel = this.controlsPanel
                            if (controlsPanel != null) {
                                controlsPanel.hide(false)
                            }

                            this.seekProgress!!.hide(false, false)
                            this.volumeProgress!!.hide(false, false)
                            this.brightProgress!!.applyBrightness()
                        }

                        if (this.gesture.toInt() != 0 && this.touchView != null) {
                            lastEvent = MotionEvent.obtain(lastEvent)
                            lastEvent.setAction(1)
                            this.touchView!!.dispatchTouchEvent(lastEvent)
                            return
                        }
                    }
                } else if (gesture == 1) {
                    if ((this.seekProgress!!.onDrag(lastEvent, lastEvent.getY() - this.downY)).compareTo(-1.0f) == 0) {
                        this.downY = lastEvent.getY()
                        return
                    }
                } else if (gesture == 2) {
                    if ((this.brightProgress!!.onDrag(lastEvent, lastEvent.getY() - this.downY)).compareTo(-1.0f) == 0) {
                        this.downY = lastEvent.getY()
                        return
                    }
                } else if (gesture == 3) {
                    val deltaX = lastEvent.getX() - this.downX
                    gesture = VideoSettings.seekSense.value
                    var dy = deltaX
                    if (gesture > 0) {
                        dy = deltaX
                        if (gesture != 100) {
                            dy = deltaX * (gesture / 100.0f)
                        }
                    }

                    if ((this.volumeProgress!!.onDrag(lastEvent, dy)).compareTo(-1.0f) == 0) {
                        this.downX = lastEvent.getX()
                        return
                    }
                }
            } else if (this.videoKind.toInt() == 2 && this.downY > this.getHeight() - MainApp.dp20 && abs(lastEvent.getY() - this.downY) > MainApp.dp10) {
                val controlsPanel = this.controlsPanel
                if (controlsPanel != null) {
                    controlsPanel.hide(false)
                }
            }
        }
    }

    override fun onPermissionResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode != RequestCode.UNLOCK) {
            return false
        }
        if (resultCode != -1) {
            return true
        }
        setTouchLock(false)
        return true
    }

    fun finishGesture() {
        val lastGesture = this.lastGesture
        val lastSeekBase = this.lastSeekBase
        val myFadeRelative = this.controlsPanel
        if (myFadeRelative != null) {
            myFadeRelative.restartTimer()
        }
        if (lastGesture.toInt() == 1) {
            val webVideoProgress = this.seekProgress
            if (webVideoProgress != null) {
                webVideoProgress.hide(true, false)
                return
            }
            return
        }
        if (lastGesture.toInt() == 2) {
            val webVideoProgress = this.brightProgress
            if (webVideoProgress != null) {
                webVideoProgress.hide(true, false)
                return
            }
            return
        }
        if (lastGesture.toInt() == 3) {
            val webVideoProgress = this.volumeProgress
            if (webVideoProgress != null) {
                webVideoProgress.hide(true, false)
                setSeekTouchUp(lastSeekBase)
                return
            }
            return
        }
        val videoKind = this.videoKind
        if (videoKind.toInt() == 2 || videoKind.toInt() == 3) {
            hideOnSeek()
        }
    }

    override fun onMediaKey(keyCode: Int): Boolean {
        if (keyCode == 79 || keyCode == 85) {
            if (this.controlsPanel != null && this.gesture.toInt() != 3) {
                if (this.videoGone) {
                    showToast(this.ownerActivity, AppR.string.not_support_video)
                    return true
                }
                togglePlay()
                return true
            }
        } else if (keyCode == 86) {
            if (this.controlsPanel != null && this.gesture.toInt() != 3) {
                if (this.videoGone) {
                    showToast(this.ownerActivity, AppR.string.not_support_video)
                    return true
                }
                setVideoPaused(true)
                return true
            }
        } else if (keyCode != 126) {
            if (keyCode != 127) {
                return false
            }
            if (this.controlsPanel != null) {
                if (this.videoGone) {
                    showToast(this.ownerActivity, AppR.string.not_support_video)
                    return true
                }
                setVideoPaused(true)
                return true
            }
        } else if (this.controlsPanel != null && this.gesture.toInt() != 3) {
            if (this.videoGone) {
                showToast(this.ownerActivity, AppR.string.not_support_video)
                return true
            }
            setVideoPaused(false)
            return true
        }
        return true
    }

    fun seekBack() {
        if (this.clickLocked || this.videoSeeked) {
            return
        }
        this.clickLocked = true
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.postDelayed(SeekBackTask(), 100L)
    }

    override val videoType: Int
        get() {
        return this.videoKind.toInt()
    }

    override fun consumesBack(): Boolean {
        if (this.topHidePending || this.menuHidePending) {
            return true
        }
        val dialogSeekBright = this.brightDialog
        if (dialogSeekBright != null && dialogSeekBright.isOpen()) {
            return true
        }
        val dialogSeekAudio = this.audioDialog
        return dialogSeekAudio != null && dialogSeekAudio.isOpen()
    }

    fun seekForward() {
        if (this.clickLocked || this.videoSeeked) {
            return
        }
        this.clickLocked = true
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.postDelayed(SeekForwardTask(), 100L)
    }

    override fun scriptLoaded() {
        this.scriptPending = false
    }

    fun applyPageZoom() {
        val left: Int
        val objectFit: String?
        val pageWidth: Int
        if (this.pinchShrink || this.page == null) {
            return
        }
        var widthDp: Int = Math.round(MainUtil.pxToDp(this.ctx, getWidth().toFloat()))
        var heightDp: Int = Math.round(MainUtil.pxToDp(this.ctx, getHeight().toFloat()))
        val pinchScale = this.pinchScale
        var top = 0
        val zoomed = pinchScale > 0.0f
        if (VideoSettings.ratio.value == 0 && zoomed) {
            var zoomedWidth = Math.round(widthDp * pinchScale)
            if (zoomedWidth > heightDp) {
                pageWidth = Math.round(heightDp / pinchScale)
                zoomedWidth = heightDp
            } else {
                pageWidth = widthDp
            }
            var index = (widthDp - pageWidth) / 2
            val offsetX = (heightDp - zoomedWidth) / 2
            if (index < 0) {
                index = 0
            }
            top = index
            widthDp = pageWidth
            left = if (offsetX >= 0) offsetX else 0
            heightDp = zoomedWidth
        } else {
            left = 0
        }
        val ratio = VideoSettings.ratio.value
        if (ratio == 1) {
            objectFit = "fill"
        } else {
            objectFit = if (ratio == 2) "cover" else "contain"
        }
        val sb = StringBuilder("(function(){if(!vdEle)return;var vst=vdEle.style;")
        if (zoomed) {
            sb.append("vst.left='")
            sb.append(top)
            sb.append("px';vst.top='")
            sb.append(left)
            sb.append("px';vst.width='")
            sb.append(widthDp)
            sb.append("px';vst.height='")
            sb.append(heightDp)
            sb.append("px';")
        }
        sb.append("vst.objectFit='").append(objectFit).append("';})();")
        evalJs(this.page, sb.toString(), true)
    }

    override fun isReloadPending(): Boolean {
        return this.reloadPending
    }

    fun togglePlay() {
        if (this.clickLocked) {
            return
        }
        this.clickLocked = true
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.postDelayed(TogglePlayTask(), 100L)
    }

    override fun isDialogOpen(): Boolean {
        return this.dialogOpen
    }

    fun updateLift() {
        if (this.liftSpacer1 == null) {
            return
        }
        val mainActivity = this.ownerActivity
        val landscape = if (mainActivity == null) isLandscape(this.ctx) else mainActivity.isLandscapeScreen()
        val view = this.liftSpacer1
        if (landscape) {
            view!!.setVisibility(8)
            this.liftSpacer2!!.setVisibility(0)
        } else {
            view!!.setVisibility(0)
            this.liftSpacer2!!.setVisibility(8)
        }
    }

    override fun isBusy(): Boolean {
        val dialogSeekBright = this.brightDialog
        if (dialogSeekBright != null && dialogSeekBright.isShownNow()) {
            return true
        }
        val dialogSeekAudio = this.audioDialog
        if (dialogSeekAudio == null || !dialogSeekAudio.isShownNow()) {
            return this.scriptPending
        }
        return true
    }

    fun hideControlsNow() {
        if (this.controlsPanel == null) {
            return
        }
        resetTouch()
        this.controlsPanel!!.hide(false)
        this.seekProgress!!.hide(false, false)
        this.brightProgress!!.hide(false, false)
        this.volumeProgress!!.hide(false, false)
    }

    override fun hasSeekDialog(): Boolean {
        return if (this.brightDialog == null && this.audioDialog == null) false else true
    }

    fun showLongPressGuide(guideShown: Boolean) {
        if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value && this.doubleTapGuide == null && this.longPressGuide == null && this.sizeFrame != null && this.ctx != null) {
            this.guideShown = guideShown
            val viewGroup = this.container
            if (viewGroup != null) {
                viewGroup.post(ShowLongPressGuideTask())
            }
        }
    }

    override fun isInPip(): Boolean {
        return this.pipBuilder != null
    }

    override fun hideMenus() {
        dismissRotationMenu()
        dismissZoomMenu()
        dismissSpeedMenu()
        val myFadeRelative = this.controlsPanel
        if (myFadeRelative != null) {
            myFadeRelative.hide(false)
        }
    }

    override fun stopPipAuto() {
        if (this.pipAuto) {
            this.pipAuto = false
            val viewGroup = this.container
            if (viewGroup == null) {
                return
            } else {
                viewGroup.postDelayed(EvalJsTask(), 500L)
            }
        }
        applyShowTime()
        if (VideoSettings.guideLongPress.value && PdfSettings.iconLong.value) {
            showLongPressGuide(false)
        }
        val cutOut = this.cutOut
        val newCutOut = VideoSettings.cutOut.value
        if (cutOut != newCutOut) {
            this.cutOut = newCutOut
            val viewGroup = this.container
            if (viewGroup == null) {
                return
            }
            viewGroup.post(ApplyDisplayCutoutTask())
        }
    }

    override fun onVideoInfo(hasVideo: Boolean, paused: Boolean, duration: String?) {
        this.videoReady = true
        if (!hasVideo || TextUtils.isEmpty(duration) || "undefined" == duration) {
            markVideoGone()
        } else {
            var durationMs = (MainUtil.parseFloatSafe(duration) * 1000.0f).toLong()
            if (durationMs < 0) {
                durationMs = 0
            }
            this.duration = durationMs
        }
        this.videoPaused = paused
        val viewGroup = this.container
        if (viewGroup != null) {
            viewGroup.post(ApplyVideoSizeTask())
        }
        if (this.pipPending) {
            this.pipPending = false
            enterPip()
        }
    }

    /** Messages from the page's video script (state, duration, position, rate, size, YouTube UI).  */
    override fun onVideoEvent(event: String?, eventValue: String?) {
        if (TextUtils.isEmpty(event)) {
            return
        }
        run sw10@{
when (event) {
            "onYouType" -> if ("1" == eventValue) {
                this.videoKind = 3
                if (this.frameListener != null && this.frameListener!!.activity() != null && this.frameListener!!.activity()!!.videoFrame != null) {
                    this.frameListener!!.activity()!!.dialogsState.videoMode = 3
                }
                if (this.downloadable) {
                    this.downloadable = false
                    if (this.downloadBtn != null && this.container != null) {
                        this.container!!.post(HideDownloadBtnTask())
                    }
                }
            }

            "onVidPaused" -> {
                this.videoPaused = "1" == eventValue
                if (this.container != null) {
                    this.container!!.post(ApplyVideoSizeTask())
                }
            }

            "onVidDur" -> {
                if (this.videoGone) {
                    return@sw10
                }
                if (TextUtils.isEmpty(eventValue) || "undefined" == eventValue) {
                    markVideoGone()
                } else {
                    this.duration = max((MainUtil.parseFloatSafe(eventValue) * 1000.0f).toLong(), 0L)
                }
            }

            "onVidPos" -> {
                val report = this.seekPending
                this.seekPending = false
                this.positionRequested = false
                if (this.videoGone) {
                    return@sw10
                }
                if (TextUtils.isEmpty(eventValue) || "undefined" == eventValue) {
                    markVideoGone()
                    return@sw10
                }
                var position = (MainUtil.parseFloatSafe(eventValue) * 1000.0f).toLong()
                position = max(min(position, this.duration), 0L)
                this.videoPosition = position
                this.seekTarget = -1L
                if (report && this.frameListener is WvaVideoFrameListener) {
                    (this.frameListener as WvaVideoFrameListener).onFrame(position)
                }
            }

            "onVidRate" -> if (this.speedBtn != null) {
                val rate: kotlin.Float = MainUtil.parseFloatOr(eventValue, 1.0f)
                this.rateValue = rate
                if (this.container != null) {
                    this.container!!.post(DismissSpeedMenuTask())
                    rememberRate(rate)
                }
            }

            "onVidRat2" -> if (TextUtils.isEmpty(eventValue) || "0" == eventValue) {
                markVideoGone()
            } else {
                rememberRate(MainUtil.parseFloatOr(eventValue, 1.0f))
            }

            "onVidSize" -> {
                this.videoSizeText = eventValue
                runInBackground(ResetVideoSizeTask())
            }

            "onYouCon" -> {
                if ((this.videoKind.toInt() != 2 && this.videoKind.toInt() != 3) || PdfSettings.iconLong.value) {
                    this.youContinue = false
                    return@sw10
                }
                this.youContinue = "1" == eventValue
                if (!this.youContinue && this.container != null) {
                    this.container!!.post(HideControlsUnlessContinueTask())
                }
            }

            "onYouExp" -> {
                this.youExpanded = "visible" == eventValue
                if (this.youExpanded && this.container != null) {
                    this.container!!.post(HideOnSeekEventTask())
                }
            }

            "onVidSeek" -> if ("1" == eventValue) {
                this.videoSeeked = true
            } else if (this.container != null) {
                this.container!!.postDelayed(ClearSeekTask(), 100L)
            }

            else -> {}
        }
}
    }

    /** Remembers the playback speed for YouTube-type players when speed memory is on.  */
    private fun rememberRate(rate: kotlin.Float) {
        if ((this.videoKind.toInt() == 2 || this.videoKind.toInt() == 3) && MainUtil.hasVideoButton() && (ZtwoSettings.youSpeedRate.value).compareTo(rate) != 0
        ) {
            ZtwoSettings.editLocal { set(ZtwoSettings.youSpeedRate, rate) }
            putSpeedRate(this.ctx, rate)
        }
    }

    override fun onSeekResult(position: String?, duration: String?) {
        val seekPending = this.seekPending
        this.seekPending = false
        this.positionRequested = false
        if (this.videoGone) {
            return
        }
        if (TextUtils.isEmpty(position) || "undefined" == position) {
            markVideoGone()
            return
        }
        var positionMs = (MainUtil.parseFloatSafe(position) * 1000.0f).toLong()
        if (positionMs < 0) {
            positionMs = 0
        }
        this.duration = positionMs
        var durationMs = (MainUtil.parseFloatSafe(duration) * 1000.0f).toLong()
        val durationNow = this.duration
        if (durationMs > durationNow) {
            durationMs = durationNow
        }
        val videoPosition = if (durationMs >= 0) durationMs else 0L
        this.videoPosition = videoPosition
        this.seekTarget = -1L
        val videoFrameListener: VideoFrameListener? = if (!seekPending) null else this.frameListener
        if (!seekPending || videoFrameListener == null) {
            return
        }
        (videoFrameListener as WvaVideoFrameListener).onFrame(videoPosition)
    }

    override fun setTitle(title: String?) {
        this.videoTitle = title
        val appCompatTextView: VfText? = if (this.videoKind.toInt() != 2) null else this.titleText
        if (this.videoKind.toInt() != 2 || appCompatTextView == null) {
            return
        }
        appCompatTextView.setText(title)
        if (!PdfSettings.videoIcon.value || this.lockCheck!!.isChecked) {
            this.titleText!!.setVisibility(8)
        } else {
            this.titleText!!.setVisibility(0)
        }
    }

    override fun setUrl(pageUrl: String?) {
        if (MainUtil.sameObject(this.videoUrl, pageUrl)) {
            return
        }
        this.videoUrl = pageUrl
        this.duration = -1L
        this.videoPosition = -1L
        this.seekTarget = -1L
        val viewGroup = this.container
        if (viewGroup == null) {
            return
        }
        viewGroup.postDelayed(NoOpTask(), 800L)
    }

    override fun setVideoDown(downloadable: Boolean) {
        var downloadable = downloadable
        val videoKind = this.videoKind
        if (videoKind.toInt() == 2 || videoKind.toInt() == 3) {
            this.downloadable = false
            downloadable = false
        } else {
            this.downloadable = downloadable
        }
        val myButtonImage = this.downloadBtn
        if (myButtonImage == null) {
            return
        }
        if (downloadable && PdfSettings.videoIcon.value && !this.lockCheck!!.isChecked) {
            this.downShown = true
            myButtonImage.setVisibility(0)
        } else {
            this.downShown = false
            myButtonImage.setVisibility(8)
        }
    }

    override fun setYouDialog(rateOff: Boolean) {
        this.youDialog = rateOff
        val viewGroup: ViewGroup? = if (!rateOff) null else this.container
        if (!rateOff || viewGroup == null) {
            return
        }
        viewGroup.post(HideOnSeekDialogTask())
    }

    override fun releaseFrame() {
        dismissBrightDialog()
        dismissAudioDialog()
        dismissRotationMenu()
        dismissZoomMenu()
        dismissSpeedMenu()
        val mainActivity = this.ownerActivity
        if (mainActivity != null) {
            mainActivity.holdScreenOn(this, false)
            this.ownerActivity = null
        }
        val mySizeFrame = this.sizeFrame
        if (mySizeFrame != null) {
            mySizeFrame.sizeCallback = null
            this.sizeFrame = null
        }
        val myFadeRelative = this.controlsPanel
        if (myFadeRelative != null) {
            myFadeRelative.release()
            this.controlsPanel = null
        }
        val myButtonImage = this.backBtn
        if (myButtonImage != null) {
            myButtonImage.clearClick()
            this.backBtn = null
        }
        val rotateBtn = this.rotateBtn
        if (rotateBtn != null) {
            rotateBtn.clearClick()
            this.rotateBtn = null
        }
        val zoomBtn = this.zoomBtn
        if (zoomBtn != null) {
            zoomBtn.clearClick()
            this.zoomBtn = null
        }
        val loopBtn = this.loopBtn
        if (loopBtn != null) {
            loopBtn.clearClick()
            this.loopBtn = null
        }
        val speedBtn = this.speedBtn
        if (speedBtn != null) {
            speedBtn.clearClick()
            this.speedBtn = null
        }
        val pipBtn = this.pipBtn
        if (pipBtn != null) {
            pipBtn.clearClick()
            this.pipBtn = null
        }
        val settingsBtn = this.settingsBtn
        if (settingsBtn != null) {
            settingsBtn.clearClick()
            this.settingsBtn = null
        }
        val myButtonCheck = this.lockCheck
        if (myButtonCheck != null) {
            myButtonCheck.clearClick()
            this.lockCheck = null
        }
        val rewindBtn = this.rewindBtn
        if (rewindBtn != null) {
            rewindBtn.clearClick()
            this.rewindBtn = null
        }
        val playBtn = this.playBtn
        if (playBtn != null) {
            playBtn.clearClick()
            this.playBtn = null
        }
        val forwardBtn = this.forwardBtn
        if (forwardBtn != null) {
            forwardBtn.clearClick()
            this.forwardBtn = null
        }
        val notiCheck = this.notiCheck
        if (notiCheck != null) {
            notiCheck.clearClick()
            this.notiCheck = null
        }
        val downloadBtn = this.downloadBtn
        if (downloadBtn != null) {
            downloadBtn.clearClick()
            this.downloadBtn = null
        }
        val brightBtn = this.brightBtn
        if (brightBtn != null) {
            brightBtn.clearClick()
            this.brightBtn = null
        }
        val volumeBtn = this.volumeBtn
        if (volumeBtn != null) {
            volumeBtn.clearClick()
            this.volumeBtn = null
        }
        val webVideoProgress = this.seekProgress
        if (webVideoProgress != null) {
            webVideoProgress.release()
            this.seekProgress = null
        }
        val brightProgress = this.brightProgress
        if (brightProgress != null) {
            brightProgress.release()
            this.brightProgress = null
        }
        val volumeProgress = this.volumeProgress
        if (volumeProgress != null) {
            volumeProgress.release()
            this.volumeProgress = null
        }
        val myFadeFrame = this.doubleTapGuide
        if (myFadeFrame != null) {
            myFadeFrame.release()
            this.doubleTapGuide = null
        }
        val longPressGuide = this.longPressGuide
        if (longPressGuide != null) {
            longPressGuide.release()
            this.longPressGuide = null
        }
        this.ctx = null
        this.page = null
        this.videoUrl = null
        this.frameListener = null
        this.titleText = null
        this.topRow = null
        this.brightText = null
        this.liftSpacer1 = null
        this.liftSpacer2 = null
        this.liftSpacer3 = null
        this.volumeText = null
        this.videoTitle = null
        this.gestures = null
        this.pipBuilder = null
        this.executorCache = null
        val viewGroup = this.container
        if (viewGroup != null) {
            (viewGroup as? com.mycompany.app.view.MyWebBody)?.overlays?.removeView(this)
            viewGroup.removeView(this)
            this.container = null
        }
    }

    override fun exitCustomView() {
        val customViewCallback = this.viewCallback
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden()
            this.viewCallback = null
        }
        val view = this.touchView
        if (view != null) {
            removeView(view)
            this.touchView = null
        }
        val sb = StringBuilder("(function(){")
        val videoKind = this.videoKind
        if (videoKind.toInt() == 2 || videoKind.toInt() == 3) {
            sb.append("if(youConOsb){youConOsb.disconnect();youConOsb=null;}if(youExpOsb){youExpOsb.disconnect();youExpOsb=null;}")
        }
        sb.append("if(vdEle){vdEle.removeEventListener('playing',myVidPlaying);vdEle.removeEventListener('pause',myVidPaused);vdEle.removeEventListener('seeking',myVidSeeking);vdEle.removeEventListener('seeked',myVidSeeked);vdEle.removeEventListener('timeupdate',myVidDur);vdEle=null;}})();")
        evalJs(this.page, sb.toString(), true)
    }

    override fun cancelHides() {
        this.topHidePending = false
        this.menuHidePending = false
    }

    override fun pipSeekBack() {
        if (this.pipBuilder == null) {
            return
        }
        if (this.videoGone) {
            showToast(this.ownerActivity, AppR.string.not_support_video)
        } else {
            seekBack()
        }
    }

    override fun pipSeekForward() {
        if (this.pipBuilder == null) {
            return
        }
        if (this.videoGone) {
            showToast(this.ownerActivity, AppR.string.not_support_video)
        } else {
            seekForward()
        }
    }

    override fun pipTogglePlay() {
        if (this.pipBuilder == null) {
            return
        }
        if (this.videoGone) {
            showToast(this.ownerActivity, AppR.string.not_support_video)
        } else {
            togglePlay()
        }
    }

    companion object {
        @JvmField
        val SPEEDS: FloatArray = floatArrayOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        @JvmStatic
        fun parsePx(valueText: String?): Int {
            if (!TextUtils.isEmpty(valueText)) {
                if (!valueText!!.endsWith("px")) {
                    return parseIntSafe(valueText)
                }
                if (valueText.length >= 3) {
                    return parseIntSafe(valueText.substring(0, valueText.length - 2))
                }
            }
            return 0
        }
    }
}
