package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.indexOfCharIn
import com.mycompany.app.main.MainUtil.openHeadConnection
import com.mycompany.app.main.MainUtil.wildcardHost
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.indexOfWord
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.matchesNativeHost
import com.mycompany.app.main.MainUtil.isFilmizle
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.isYouTubeHost
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.openUriInput
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.isEmgUrl
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.isHmgPath
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.isHmgUrl
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainUtil.snsJs2
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.snsUrl
import com.mycompany.app.main.MainUtil.snsJs1
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.openWithCookies
import com.mycompany.app.main.MainUtil.restartLater
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.searchUrlFor
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.main.MainUtil.browserIntent
import com.mycompany.app.main.MainUtil.isLockEnabled
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.parseFontList
import com.mycompany.app.main.MainUtil.imageUrlFallback
import com.mycompany.app.main.MainUtil.extractUrl
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.urlInParens
import com.mycompany.app.main.MainUtil.siteNameOf
import com.mycompany.app.main.MainUtil.hostLabelUpper
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.fileNumberSuffix
import com.mycompany.app.main.MainUtil.comicSiteKind
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.setUserAgent
import com.mycompany.app.main.MainUtil.evalPageJs
import com.mycompany.app.main.MainUtil.isGoogleSearch
import com.mycompany.app.main.MainUtil.lacksAlgorithmicDark
import com.mycompany.app.main.MainUtil.setAlgorithmicDark
import com.mycompany.app.main.MainUtil.needsDesktopMode
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.isYouTubeUrl
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.nativeJs5b
import com.mycompany.app.main.MainUtil.nativeJs5c
import com.mycompany.app.main.MainUtil.viewportJs
import com.mycompany.app.main.MainUtil.isImagePath
import com.mycompany.app.main.MainUtil.injectOverlayJs
import com.mycompany.app.main.MainUtil.isHitomi
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.applyDesktopMode
import com.mycompany.app.main.MainUtil.backupBitmapCopy
import com.mycompany.app.main.MainUtil.isLandscape
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
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.main.MainUtil.youTubeVideoId
import com.mycompany.app.main.MainUtil.youTubeIdOf
import com.mycompany.app.main.MainUtil.isFullScreenOff
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.restartTask
import com.mycompany.app.main.MainUtil.clearAppData
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.translateUrlFor
import com.mycompany.app.main.MainUtil.hasVideoButton
import com.mycompany.app.main.MainUtil.pipAspectRatio
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
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.openWithChooser
import com.mycompany.app.main.MainUtil.quickViewPdf
import com.mycompany.app.main.MainUtil.glideUrlFor
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.shareText
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.openCustomTab
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.transLangFromCookie
import com.mycompany.app.main.MainUtil.hideDivsByClass
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
import com.mycompany.app.main.MainUtil.clearCookies
import com.mycompany.app.main.MainUtil.applyNightMode
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.main.MainUtil.isDarkResources
import com.mycompany.app.main.MainUtil.openPlayStore
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.lockAppData
import com.mycompany.app.main.MainUtil.isAudioPlaying
import com.mycompany.app.main.MainUtil.findMediaJs
import com.mycompany.app.db.book.DbBookRecent.Companion.clearAll
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
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
import com.mycompany.app.compress.Compress.Companion.isImageExt
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.schemeKind
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.addShortcut
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.checkTransClassJs
import com.mycompany.app.main.MainUtil.parseColorText
import com.mycompany.app.main.MainUtil.lighterColor
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.hasFragmentPage
import com.mycompany.app.main.MainUtil.isFakeStream
import com.mycompany.app.main.MainUtil.themeIconColor
import com.mycompany.app.main.MainUtil.hasTopTitle
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.hasTopTabBar
import com.mycompany.app.main.MainUtil.injectVideoObserver
import com.mycompany.app.main.MainUtil.needsDesktopUa
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.main.MainUtil.iconResFor
import com.mycompany.app.main.MainUtil.hasBottomTitle
import com.mycompany.app.main.MainUtil.windowStatusColor
import com.mycompany.app.main.MainUtil.setWindowStatusColor
import com.mycompany.app.main.MainUtil.setNavBarColor
import com.mycompany.app.main.MainUtil.isRootPath
import com.mycompany.app.main.MainUtil.isXLoginPage
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.main.MainUtil.navBarInset
import com.mycompany.app.main.MainUtil.viewScreenX
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.clearHistoryData
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.isGoogleHome
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.isUserScriptUrl
import com.mycompany.app.main.MainUtil.downMenuCount
import com.mycompany.app.main.MainUtil.isDarkTheme
import com.mycompany.app.main.MainUtil.isChinaLocale
import com.mycompany.app.main.MainUtil.applyScreenOrientation
import com.mycompany.app.main.MainUtil.applyImageOrientation
import com.mycompany.app.main.MainUtil.applyIncognitoIme
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.db.book.DbBookHistory.Companion.clearAll
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.db.book.DbBookTab.Companion.clearAll
import com.mycompany.app.main.MainUtil.userFontResponse
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUtil.mimeForPath
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.readTextFile
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.main.MainUtil.highlightText
import com.mycompany.app.main.MainUtil.durationText
import com.mycompany.app.main.MainUtil.screenBrightnessPercent
import com.mycompany.app.web.WebClean.WebCleanListener
import com.mycompany.app.web.WebClean
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebEmgLoad
import com.mycompany.app.main.MainActivity
import com.mycompany.app.async.MyAsyncTask
import android.webkit.ValueCallback
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.ui.browser.IconButton
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import com.mycompany.app.web.WebViewActivity
import android.os.Bundle
import android.content.Intent
import com.mycompany.app.web.WebLoadView
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.main.MainWebDestroy
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.db.DbUtil
import android.os.Looper
import android.view.MotionEvent
import android.os.Build
import com.mycompany.app.quick.QuickControl
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import android.app.Activity
import com.mycompany.app.ui.components.LoadCover
import com.mycompany.app.view.MyScrollBar
import com.mycompany.app.dialog.DialogConfirm
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.web.WebVideoFrame
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.dialog.DialogSeekBright
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.ui.browser.ComposeTabBar
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.dialog.DialogTransLang
import com.mycompany.app.dialog.DialogWebSelect
import com.mycompany.app.dialog.DialogSetMsg
import com.mycompany.app.dialog.DialogTransMsg
import com.mycompany.app.dialog.DialogMenuMain
import com.mycompany.app.dialog.DialogViewTrans
import com.mycompany.app.web.WebViewActivity.TabLimitListener
import com.mycompany.app.pull.MyPullView
import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.web.WebViewActivity.TabViewListener
import com.mycompany.app.wview.WebUpView
import com.mycompany.app.web.WebViewActivity.PrevPageListener
import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.dialog.DialogLoadImg
import com.mycompany.app.dialog.DialogLoadEmg
import com.mycompany.app.wview.WebDownView
import com.mycompany.app.dialog.DialogLoadHmg
import com.mycompany.app.view.MyEngineMenu
import com.mycompany.app.view.MyTouchFrame
import com.mycompany.app.dialog.DialogVideoMenu
import com.mycompany.app.dialog.DialogVideoList
import com.mycompany.app.dialog.DialogTabMenu
import com.mycompany.app.dialog.DialogPreview
import com.mycompany.app.dialog.DialogOpenType
import com.mycompany.app.dialog.DialogPreImage
import com.mycompany.app.web.WebViewActivity.PrevBackListener
import com.mycompany.app.dialog.DialogPopupMenu
import com.mycompany.app.help.KeyHelper
import com.mycompany.app.dialog.DialogNewsMenu
import com.mycompany.app.dialog.DialogWebView
import com.mycompany.app.web.WebTabBarSubView
import com.mycompany.app.dialog.DialogSeekWebText
import com.mycompany.app.ui.browser.ScrollNavi
import com.mycompany.app.dialog.DialogNewsLocale
import com.mycompany.app.dialog.DialogAllowPopup
import com.mycompany.app.dialog.DialogSetFull
import com.mycompany.app.view.MyWebCoord
import androidx.recyclerview.widget.ItemTouchHelper
import com.mycompany.app.dialog.DialogNewsSearch
import com.mycompany.app.dialog.DialogBlockLink
import com.mycompany.app.ui.browser.BarScroll
import com.mycompany.app.wview.WebMoveText
import com.mycompany.app.web.WebNestLayout
import com.mycompany.app.wview.WebAreaText
import com.mycompany.app.dialog.DialogSetAdblock
import com.mycompany.app.view.MyBarFrame
import com.mycompany.app.dialog.DialogSetJava
import com.mycompany.app.dialog.DialogSetTmem
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.dialog.DialogEditShort
import com.mycompany.app.dialog.DialogUrlLink
import com.mycompany.app.behavior.MyBehaviorBot
import androidx.viewpager2.widget.ViewPager2
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogGuideArea
import com.mycompany.app.dialog.DialogEditArea
import com.mycompany.app.dialog.DialogPrompt
import android.webkit.PermissionRequest
import com.mycompany.app.dialog.DialogEditAuth
import com.mycompany.app.view.MyEditAuto
import com.mycompany.app.dialog.DialogPermission
import android.webkit.GeolocationPermissions
import com.mycompany.app.quick.QuickSearch
import com.mycompany.app.dialog.DialogCapture
import com.mycompany.app.dialog.DialogDownPage
import com.mycompany.app.view.MyFindView
import com.mycompany.app.dialog.DialogPrintPage
import com.mycompany.app.web.WebSearchAdapter
import com.mycompany.app.dialog.DialogSetTrans
import com.mycompany.app.dialog.DialogPassInfo
import com.mycompany.app.view.MyEditAuto.KeyBackListener
import com.mycompany.app.script.ScriptCriteria
import com.mycompany.app.web.WebSearchAdapter.SearchItem
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookWeb
import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.view.ActionMode
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.webkit.URLUtil
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Comparator
import com.mycompany.app.db.book.DbBookQuick

@Suppress("deprecation")
class WebViewActivity : MainActivity() {
    /** The touch handling (was the 6,100-line dispatchTouchEvent; see WebTouchController). */
    @JvmField
    val touch = WebTouchController(this)

    /** The activity's state, grouped by feature (see WebViewActivityState.kt). */
    @JvmField
    val findState = WebFindState()
    @JvmField
    val videoState = WebVideoState()
    @JvmField
    val translateState = WebTranslateState()
    @JvmField
    val speechState = WebSpeechState()
    @JvmField
    val tabsState = WebTabsState()
    @JvmField
    val quickState = WebQuickState()
    @JvmField
    val downloadsState = WebDownloadsState()
    @JvmField
    val barsState = WebBarsState()
    @JvmField
    val floatingState = WebFloatingState()
    @JvmField
    val dialogsState = WebDialogsState()
    @JvmField
    val pageState = WebPageState()
    @JvmField
    val miscState = WebMiscState()
    @JvmField
    var newIntentPending: Boolean = false
    @JvmField
    var searchTask: SearchTask? = null
    @JvmField
    var quickDeleteDialog: DialogDeleteItem? = null
    @JvmField
    var savedDarkStart: Boolean = false
    @JvmField
    var savedEditorN: Int = 0
    @JvmField
    var transLangDialog: DialogTransLang? = null
    @JvmField
    var webSelectDialog: DialogWebSelect? = null
    @JvmField
    var isPaused: Boolean = false
    @JvmField
    var clearRecentDialog: DialogSetMsg? = null
    @JvmField
    var savedDarkWeb: Boolean = false
    @JvmField
    var transMsgDialog: DialogTransMsg? = null
    @JvmField
    var menuMainDialog: DialogMenuMain? = null
    @JvmField
    var isClosing: Boolean = false
    @JvmField
    var viewTransDialog: DialogViewTrans? = null
    @JvmField
    var hasWindowFocusNow: Boolean = false
    @JvmField
    var deleteItemDialog: DialogDeleteItem? = null
    @JvmField
    var savedImgBright: Int = 0
    @JvmField
    var mainMenuAnchor: View? = null
    @JvmField
    var focusPending: Boolean = false
    @JvmField
    var currentPage: WebNestView? = null
    @JvmField
    var tabGroupDeleteDialog: DialogDeleteItem? = null
    @JvmField
    var savedZtwoA0: Boolean = false
    @JvmField
    var transActive: Boolean = false
    @JvmField
    var mainMenuActionDone: Boolean = false
    @JvmField
    var menuJustClosed: Boolean = false
    @JvmField
    var nextPageAnim: Boolean = false
    @JvmField
    var savedNavColor: Int = 0
    @JvmField
    var autoTransPending: Boolean = false
    @JvmField
    var mainMenuBusy: Boolean = false
    @JvmField
    var quickMenuJustClosed: Boolean = false
    @JvmField
    var addTabAnim: Boolean = false
    @JvmField
    var savedShowStatus: Boolean = false
    @JvmField
    var savedZtwoU: Boolean = false
    @JvmField
    var mainMenuItems: IntArray? = null
    @JvmField
    var animFromStartPage: Boolean = false
    @JvmField
    var keyboardOpen: Boolean = false
    @JvmField
    var savedShowNavi: Boolean = false
    @JvmField
    var secureFlagOn: Boolean = false
    @JvmField
    var mainMenuItems2: IntArray? = null
    @JvmField
    var pullView: MyPullView? = null
    @JvmField
    var keyboardHiding: Boolean = false
    @JvmField
    var savedAddrType: Int = 0
    @JvmField
    var fileChooserCallback: ValueCallback<*>? = null
    @JvmField
    var transLangCode: String? = null
    @JvmField
    var addTabPending: Boolean = false
    @JvmField
    var tabUsageOrder: MutableList<Any?>? = null
    @JvmField
    var keyboardInset: Int = 0
    @JvmField
    var savedZoneX: Int = 0
    @JvmField
    var fileChooserCallback2: ValueCallback<*>? = null
    @JvmField
    var transText: String? = null
    @JvmField
    var addTabFromIntent: Boolean = false
    @JvmField
    var keyboardBottomMargin: Int = 0
    @JvmField
    var savedEditorI: Int = 0
    @JvmField
    var fileChooserCallback3: ValueCallback<*>? = null
    @JvmField
    var transResult: String? = null
    @JvmField
    var currentTabIndex: Int = 0
    @JvmField
    var upButton: WebUpView? = null
    @JvmField
    var cameraUri: Uri? = null
    @JvmField
    var pageLangCode: String? = null
    @JvmField
    var tabOverviewAnim: Boolean = false
    @JvmField
    var isExiting: Boolean = false
    @JvmField
    var swipeTabFrame: WebNestFrame? = null
    @JvmField
    var savedZoneY: Int = 0
    @JvmField
    var transRequested: Boolean = false
    @JvmField
    var tabOverviewFast: Boolean = false
    @JvmField
    var launchedExternally: Boolean = false
    @JvmField
    var swipeNextTabFrame: WebNestFrame? = null
    @JvmField
    var topDownIcon: com.mycompany.app.ui.browser.BarIndicator? = null
    @JvmField
    var savedZtwoE: Boolean = false
    @JvmField
    var videoFrame: WebVideoFrame? = null
    @JvmField
    var transCheckedUrl: String? = null
    @JvmField
    var loadImgDialog: DialogLoadImg? = null
    @JvmField
    var launchUrl: String? = null
    @JvmField
    var savedZoneU: Boolean = false
    @JvmField
    var loadEmgDialog: DialogLoadEmg? = null
    @JvmField
    var deskModeStart: Boolean = false
    @JvmField
    var imageButton: WebDownView? = null
    @JvmField
    var savedZoneN: Int = 0
    @JvmField
    var videoExitDark: Boolean = false
    @JvmField
    var loadHmgDialog: DialogLoadHmg? = null
    @JvmField
    var onStartPage: Boolean = false
    @JvmField
    var downIconColor: Int = 0
    @JvmField
    var savedZoneO_Q4: Boolean = false
    @JvmField
    var addressMenu: MyPopupMenu? = null
    @JvmField
    var transFloatButton: View? = null
    @JvmField
    var videoDownCount: Int = 0
    @JvmField
    var savedZoneP: Boolean = false
    @JvmField
    var barItemView: View? = null
    @JvmField
    var transControl: WebTransControl? = null
    @JvmField
    var setupLock: Any? = null
    @JvmField
    var videoDownFound: Boolean = false
    @JvmField
    var savedZoneQ: Boolean = false
    @JvmField
    var engineMenu: MyEngineMenu? = null
    @JvmField
    var transControlFrame: FrameLayout? = null
    @JvmField
    var videoMenuDialog: DialogVideoMenu? = null
    @JvmField
    var savedEditorM: Int = 0
    @JvmField
    var videoListDialog: DialogVideoList? = null
    @JvmField
    var tabBarView: ComposeTabBar? = null
    @JvmField
    var sideButton: IconButton? = null
    @JvmField
    var tabMenuDialog: DialogTabMenu? = null
    @JvmField
    var previewDialog: DialogPreview? = null
    @JvmField
    var tabBarHeight: Int = 0
    @JvmField
    var savedZoneT: Boolean = false
    @JvmField
    var tabBarItemMenu: MyPopupMenu? = null
    @JvmField
    var openTypeDialog: DialogOpenType? = null
    @JvmField
    var preImageDialog: DialogPreImage? = null
    @JvmField
    var tabBar: WebTabBar? = null
    @JvmField
    var popupMenuDialog: DialogPopupMenu? = null
    @JvmField
    var previewFromLink: Boolean = false
    @JvmField
    var keyHelper: KeyHelper? = null
    @JvmField
    var savedBlockSsl: Boolean = false
    @JvmField
    var newsMenuDialog: DialogNewsMenu? = null
    @JvmField
    var webViewDialog: DialogWebView? = null
    @JvmField
    var seekBrightDialog: DialogSeekBright? = null
    @JvmField
    var webDestroy: MainWebDestroy? = null
    @JvmField
    var tabBarSubView: WebTabBarSubView? = null
    @JvmField
    var newsMenuOpen: Boolean = false
    @JvmField
    var seekWebTextDialog: DialogSeekWebText? = null
    @JvmField
    var leftScrollNavi: ScrollNavi? = null
    @JvmField
    var savedZoneO_a5: Int = 0
    @JvmField
    var newsLocaleDialog: DialogNewsLocale? = null
    @JvmField
    var allowPopupDialog: DialogAllowPopup? = null
    @JvmField
    var setFullDialog: DialogSetFull? = null
    @JvmField
    var webCoord: MyWebCoord? = null
    @JvmField
    var newsSearchDialog: DialogNewsSearch? = null
    @JvmField
    var blockLinkDialog: DialogBlockLink? = null
    /** The bar scroll-hide state (was the coordinator's `AppBarLayout`); read by the page view. */
    @JvmField
    var barScroll: BarScroll? = null

    /** Bar layouts 1 / 2: the page sits below the top bar and follows it while it slides (see `pushBars`). */
    @JvmField
    var pageFollowsTopBar = false

    @JvmField
    var savedWebLand2: Int = 0
    @JvmField
    var darkConfirmDialog: DialogConfirm? = null
    @JvmField
    var savedHandTop: Int = 0
    @JvmField
    var moveHint: WebMoveText? = null
    @JvmField
    var pageLayout: WebNestLayout? = null
    @JvmField
    var tabDragStart: Int = 0
    @JvmField
    var savedMainB: String? = null
    @JvmField
    var linkTitlePill: WebAreaText? = null
    @JvmField
    var setAdblockDialog: DialogSetAdblock? = null
    @JvmField
    var topBarFrame: MyBarFrame? = null
    @JvmField
    var bookmarksDialog: WebBookmarksDialog? = null
    @JvmField
    var setJavaDialog: DialogSetJava? = null

    @JvmField
    var bottomBarFrame: MyBarFrame? = null
    @JvmField
    var savedAlbumU: Int = 0
    @JvmField
    var historyDialog: WebHistoryDialog? = null
    @JvmField
    var setTmemDialog: DialogSetTmem? = null
    @JvmField
    var savedTopToBottom: Boolean = false
    @JvmField
    var savedAlbumW: Boolean = false
    var savedTransHide: Boolean = false
    @JvmField
    var editShortDialog: DialogEditShort? = null
    @JvmField
    var areaToolbarOn: Boolean = false
    @JvmField
    var savedFixTop: Boolean = false
    @JvmField
    var lastTransLang: String? = null
    @JvmField
    var urlLinkDialog: DialogUrlLink? = null
    @JvmField
    var behaviorBot: MyBehaviorBot? = null
    @JvmField
    var savedFixBot: Boolean = false
    @JvmField
    var savedEditorZ: Int = 0
    @JvmField
    var savedUtilD3: Int = 0
    @JvmField
    var savedZtriK: Boolean = false
    @JvmField
    var downGuideDialog: DialogConfirm? = null
    @JvmField
    var areaScript: String? = null
    @JvmField
    var animPage: WebNestView? = null
    @JvmField
    var savedUtilH0: Int = 0
    @JvmField
    var savedEditorU: Int = 0
    @JvmField
    var animFrame: WebNestFrame? = null
    @JvmField
    var savedTabBar: Int = 0
    @JvmField
    var setDownDialog: DialogSetDown? = null
    @JvmField
    var pageSwipeForward: Boolean = false
    @JvmField
    var savedTabAccent: Boolean = false
    @JvmField
    var savedFloatK: Int = 0
    @JvmField
    var guideAreaDialog: DialogGuideArea? = null
    @JvmField
    var savedTabColor: Int = 0
    @JvmField
    var savedFloatL: Int = 0
    @JvmField
    var captureModeSheet: com.mycompany.app.ui.browser.ComposeBottomDialog? = null
    @JvmField
    var editAreaDialog: DialogEditArea? = null
    @JvmField
    var engineIcon: com.mycompany.app.view.AddrIcon? = null
    @JvmField
    var jsMessageDialog: DialogSetMsg? = null
    @JvmField
    var snackbar: MySnackbar? = null
    @JvmField
    var savedTabWidth: Int = 0
    @JvmField
    var savedFloatW: Int = 0
    @JvmField
    var scrollBar: MyScrollBar? = null
    @JvmField
    var savedTabHeight: Int = 0
    @JvmField
    var savedFloatA: Int = 0
    @JvmField
    var promptDialog: DialogPrompt? = null
    @JvmField
    var deleteIcon: com.mycompany.app.view.AddrIcon? = null
    @JvmField
    var scrollBarSide: Int = 0
    @JvmField
    var savedPdfF2: Int = 0
    @JvmField
    var savedThemeHead: Int = 0
    @JvmField
    var coverView: LoadCover? = null
    @JvmField
    var editAuthDialog: DialogEditAuth? = null
    @JvmField
    var editAuto: MyEditAuto? = null
    @JvmField
    var loadView: WebLoadView? = null
    @JvmField
    var savedMainE: String? = null
    @JvmField
    var savedThemeColor: Int = 0
    @JvmField
    var permissionDialog: DialogPermission? = null
    @JvmField
    var editorBarReady: Boolean = false
    @JvmField
    var emgLoad: WebEmgLoad? = null
    @JvmField
    var savedMainF: String? = null
    @JvmField
    var savedNewsUse: Boolean = false
    @JvmField
    var coverScrollY: Int = 0
    @JvmField
    var permissionRequest2: PermissionRequest? = null
    @JvmField
    var savedMainZ: String? = null
    @JvmField
    var savedZtwoL: String? = null
    @JvmField
    var coverTime: Long = 0
    @JvmField
    var geoOrigin: String? = null
    @JvmField
    var quickSearch: QuickSearch? = null
    @JvmField
    var savedZtwoN: Int = 0
    @JvmField
    var captureDialog: DialogCapture? = null
    @JvmField
    var geoCallback: GeolocationPermissions.Callback? = null
    @JvmField
    var quickControl: QuickControl? = null
    @JvmField
    var savedMainL: Int = 0
    @JvmField
    var savedNewsTitle: Boolean = false
    @JvmField
    var downPageDialog: DialogDownPage? = null
    @JvmField
    var findView: MyFindView? = null
    @JvmField
    var printPageDialog: DialogPrintPage? = null
    @JvmField
    var permissionKind: Int = 0
    @JvmField
    var lastBackTime: Long = 0
    @JvmField
    var searchAdapter: WebSearchAdapter? = null
    @JvmField
    var savedCustomZones: Boolean = false
    @JvmField
    var savedZtriC: Boolean = false
    @JvmField
    var setTransDialog: DialogSetTrans? = null
    @JvmField
    var passInfoDialog: DialogPassInfo? = null
    @JvmField
    val tabLock: Any = Any()
    @JvmField
    val insetTask: Runnable = WvaKeyboardRelayout(this@WebViewActivity)
    @JvmField
    val keyBackListener: KeyBackListener = WvaQuickBackListener(this@WebViewActivity)
    @JvmField
    val webCleanListener: WebCleanListener = WvaWebCleanListener(this@WebViewActivity)
    @JvmField
    val videoRateRetry: Runnable = WvaVideoRateInject(this@WebViewActivity)
    @JvmField
    val videoActionRetry: Runnable = WvaVideoRateInjectForce(this@WebViewActivity)
    @JvmField
    val progressTask: Runnable = WvaUpdateProgress(this@WebViewActivity)
    @JvmField
    val pageViewTask: Runnable = WvaScrollStopped(this@WebViewActivity)
    val `in`: Runnable = WvaHandModeEnterApply(this@WebViewActivity)
    @JvmField
    val handExitTask: Runnable = WvaHandModeExitApply(this@WebViewActivity)
    @JvmField
    val touchTask: Runnable = WvaShowFloatingButtons(this@WebViewActivity)
    @JvmField
    val touchTask2: Runnable = WvaRefreshFloatingButtons(this@WebViewActivity)
    @JvmField
    val barUpdateTask: Runnable = WvaGestureEnd(this@WebViewActivity)

    internal class AnchorHandler(webViewActivity: WebViewActivity?, var linkUrl: String?, var srcUrl: String?) :
        Handler(Looper.getMainLooper()) {
        var activityRef: WeakReference<*>?
        var title: String? = null

        init {
            this.activityRef = WeakReference<Any?>(webViewActivity)
        }

        override fun handleMessage(message: Message) {
            val href: String?
            val imageSrc: String?
            val weakReference = this.activityRef
            val webViewActivity: WebViewActivity? = if (weakReference == null) null else (weakReference.get() as WebViewActivity?)
            if (weakReference == null || webViewActivity == null) {
                return
            }
            val data: Bundle? = message.getData()
            if (data == null) {
                href = null
                imageSrc = null
            } else {
                href = cleanUrl(Companion.bundleString("url", data))
                imageSrc = cleanUrl(Companion.bundleString("src", data))
                val bundleTitle = Companion.bundleString("title", data)
                this.title = bundleTitle
                if (!TextUtils.isEmpty(bundleTitle)) {
                    this.title = this.title!!.trim { it <= ' ' }
                }
            }
            if (!TextUtils.isEmpty(this.linkUrl) || TextUtils.isEmpty(this.srcUrl)) {
                if (TextUtils.isEmpty(this.linkUrl) || !TextUtils.isEmpty(this.srcUrl)) {
                    if (isMediaUrl(imageSrc)) {
                        this.srcUrl = imageSrc
                    } else if (isMediaUrl(href)) {
                        this.srcUrl = href
                    }
                    if (URLUtil.isNetworkUrl(href) && href != this.srcUrl) {
                        this.linkUrl = href
                    } else if (URLUtil.isNetworkUrl(imageSrc) && imageSrc != this.srcUrl) {
                        this.linkUrl = imageSrc
                    }
                } else if (URLUtil.isNetworkUrl(href) && href != this.linkUrl) {
                    this.srcUrl = href
                } else if (URLUtil.isNetworkUrl(imageSrc) && imageSrc != this.linkUrl) {
                    this.srcUrl = imageSrc
                }
            } else if (URLUtil.isNetworkUrl(href) && href != this.srcUrl) {
                this.linkUrl = href
            } else if (URLUtil.isNetworkUrl(imageSrc) && imageSrc != this.srcUrl) {
                this.linkUrl = imageSrc
            }
            if (!TextUtils.isEmpty(this.srcUrl) && !this.srcUrl!!.startsWith("data:image/") && !this.srcUrl!!.startsWith(
                    "blob:"
                ) && WebClean.Companion.isPlainHostUrl(this.srcUrl)
            ) {
                if (TextUtils.isEmpty(this.linkUrl)) {
                    this.linkUrl = this.srcUrl
                }
                this.srcUrl = null
            }
            val link = this.linkUrl
            val source = this.srcUrl
            val titleText = this.title
            if (webViewActivity.dialogsState.longPressBusy || webViewActivity.videoFrame != null) {
                return
            }
            if (TextUtils.isEmpty(webViewActivity.videoState.longPressLinkUrl)) {
                webViewActivity.videoState.longPressLinkUrl = link
            }
            if (TextUtils.isEmpty(webViewActivity.videoState.longPressSrcUrl)) {
                webViewActivity.videoState.longPressSrcUrl = source
            }
            if (TextUtils.isEmpty(webViewActivity.videoState.longPressTitle)) {
                webViewActivity.videoState.longPressTitle = titleText
            }
            webViewActivity.videoState.webViewActivityBusy = true
            webViewActivity.handleLinkLongPress()
        }

        companion object {
            @JvmStatic
            fun bundleString(key: String?, bundle: Bundle?): String? {
                try {
                    return if (Build.VERSION.SDK_INT >= 33) bundle!!.getString(
                        key,
                        null
                    ) else bundle!!.get(key) as String?
                } catch (unused: Exception) {
                    return null
                }
            }

            @JvmStatic
            fun cleanUrl(url: String?): String? {
                val length = if (url == null) 0 else url.length
                if (url == null || length == 0) {
                    return url
                }
                try {
                    val httpStart = url.indexOf("http")
                    val start = if (httpStart < 0) 0 else (httpStart + 4)
                    if (httpStart < 0 || start >= length) {
                        return url
                    }
                    var urlEnd = url.indexOf(34.toChar(), start)
                    if ((urlEnd <= httpStart || urlEnd >= length) && ((url.indexOf(
                            39.toChar(),
                            start
                        ).also { urlEnd = it }) <= httpStart || urlEnd >= length)
                    ) {
                        urlEnd = length
                    }
                    return if (httpStart > 0 || urlEnd < length) url.substring(
                        httpStart,
                        urlEnd
                    ) else url
                } catch (unused: Exception) {
                    return url
                }
            }

            @JvmStatic
            fun isMediaUrl(url: String?): Boolean {
                if (!TextUtils.isEmpty(url)) {
                    if (url!!.startsWith("data:image/") || url.startsWith("blob:")) {
                        return true
                    }
                    if (URLUtil.isNetworkUrl(url)) {
                        if (isImageExt(fileExtOf(url, false))) {
                            return true
                        }
                        return MainUtil.isDcinsideImage(url)
                    }
                }
                return false
            }
        }
    }

    class FaceItem(@JvmField var url: String?, @JvmField var index: Int, @JvmField var label: String?, @JvmField var isHidden: Boolean) {
        @JvmField
        var posterUrl: String? = null
        @JvmField
        var fileSize: Long = 0
    }

    class PopItem {
        @JvmField
        var type: Byte = 0
        @JvmField
        var url: String? = null
        @JvmField
        var referer: String? = null
        @JvmField
        var intentUrl: String? = null
    }

    interface PrevBackListener {
        fun onBack(isLong: Boolean)
    }

    interface PrevPageListener {
        fun onDone()
    }

    class SearchTask(webViewActivity: WebViewActivity?) : MyAsyncTask() {
        @JvmField
        var activityRef: WeakReference<*>
        @JvmField
        var withHistory: Boolean
        @JvmField
        var withBooks: Boolean
        @JvmField
        var withQuick: Boolean
        @JvmField
        var historyItems: ArrayList<Any?>? = null
        @JvmField
        var bookItems: ArrayList<Any?>? = null
        @JvmField
        var quickItems: ArrayList<Any?>? = null
        @JvmField
        var domains: ArrayList<String?>? = null

        init {
            this.activityRef = WeakReference<Any?>(webViewActivity)
            val sugType = BrowserSettings.sugType.value
            this.withHistory = (sugType and 4) == 4
            this.withBooks = (sugType and 8) == 8
            this.withQuick = (sugType and 16) == 16
        }

        /**
         * Loads the address-bar suggestion sources: recent history (`historyItems`, up to 100 urls within
         * BrowserSettings.historyTime.value days, -1 = all; in secret mode not older than the secret session), bookmarks
         * (`bookItems`, folders skipped) and quick links (`quickItems`), each only when enabled (`withHistory` /
         * `withBooks` / `withQuick`). With ZtwoSettings.autoComp.value the unique domains of all of them go to `domains`. Cancelling
         * clears every list.
         */
        public override fun doInBackground() {
            val activity = this.activityRef.get() as WebViewActivity?
            if (activity == null || this.cancelled) {
                return
            }
            val secret = com.mycompany.app.pref.store.Workspaces.dataKey()
            if ((this.withHistory || ZtwoSettings.autoComp.value) && BrowserSettings.historyTime.value != 0 && !(SyncSettings.secretMode.value && SecretSettings.secretHist.value == 0L)) {
                var since: Long = 0
                if (BrowserSettings.historyTime.value != -1) {
                    since = System.currentTimeMillis() - BrowserSettings.historyTime.value * 86400000L
                    if (SyncSettings.secretMode.value && SecretSettings.secretHist.value > since) {
                        since = SecretSettings.secretHist.value
                    }
                }
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        DbBookHistory.get(activity.dialogsState.appContext).getWritableDatabase(),
                        "DbBookHistory_table",
                        arrayOf<String?>("_id", "_path", "_title"),
                        "_secret=? AND _time>?",
                        arrayOf<String?>(secret, since.toString()),
                        "_time DESC"
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndex("_id")
                        val pathColumn = cursor.getColumnIndex("_path")
                        val titleColumn = cursor.getColumnIndex("_title")
                        if (this.withHistory && this.historyItems == null) {
                            this.historyItems = ArrayList<Any?>()
                        }
                        if (ZtwoSettings.autoComp.value && this.domains == null) {
                            this.domains = ArrayList()
                        }
                        var count = 0
                        do {
                            if (this.cancelled) {
                                clearAll()
                                cursor.close()
                                return
                            }
                            val path = cursor.getString(pathColumn)
                            if (URLUtil.isNetworkUrl(path)) {
                                if (this.historyItems != null) {
                                    this.historyItems!!.add(
                                        item(
                                            18,
                                            cursor.getLong(idColumn),
                                            path,
                                            cursor.getString(titleColumn)
                                        )
                                    )
                                }
                                addDomain(path)
                                count++
                            }
                        } while (count < 100 && cursor.moveToNext())
                    }
                } catch (ignored: Exception) {
                }
                if (cursor != null) {
                    cursor.close()
                }
                if (this.cancelled) {
                    return
                }
            }
            if (this.withBooks || ZtwoSettings.autoComp.value) {
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        DbBookWeb.get(activity.dialogsState.appContext).getWritableDatabase(),
                        "DbBookWeb_table",
                        arrayOf<String?>("_id", "_isdir", "_path", "_title"),
                        "_secret=?",
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()),
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndex("_id")
                        val dirColumn = cursor.getColumnIndex("_isdir")
                        val pathColumn = cursor.getColumnIndex("_path")
                        val titleColumn = cursor.getColumnIndex("_title")
                        if (this.withBooks) {
                            this.bookItems = ArrayList<Any?>()
                        }
                        if (ZtwoSettings.autoComp.value && this.domains == null) {
                            this.domains = ArrayList()
                        }
                        do {
                            if (this.cancelled) {
                                clearAll()
                                cursor.close()
                                return
                            }
                            if (cursor.getInt(dirColumn) == 1) {
                                continue
                            }
                            val path = cursor.getString(pathColumn)
                            if (!URLUtil.isNetworkUrl(path)) {
                                continue
                            }
                            if (this.bookItems != null) {
                                this.bookItems!!.add(
                                    item(
                                        17,
                                        cursor.getLong(idColumn),
                                        path,
                                        cursor.getString(titleColumn)
                                    )
                                )
                            }
                            addDomain(path)
                        } while (cursor.moveToNext())
                    }
                } catch (ignored: Exception) {
                }
                if (cursor != null) {
                    cursor.close()
                }
                if (this.cancelled) {
                    return
                }
            }
            if (!this.withQuick && !ZtwoSettings.autoComp.value) {
                return
            }
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookQuick.get(activity.dialogsState.appContext).getWritableDatabase(),
                    "DbBookQuick_table",
                    arrayOf<String?>("_id", "_path", "_title", "_rsv4"),
                    "_secret=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndex("_id")
                    val pathColumn = cursor.getColumnIndex("_path")
                    val titleColumn = cursor.getColumnIndex("_title")
                    val iconColumn = cursor.getColumnIndex("_rsv4")
                    if (this.withQuick) {
                        this.quickItems = ArrayList<Any?>()
                    }
                    if (ZtwoSettings.autoComp.value && this.domains == null) {
                        this.domains = ArrayList()
                    }
                    do {
                        if (this.cancelled) {
                            clearAll()
                            cursor.close()
                            return
                        }
                        val path = cursor.getString(pathColumn)
                        if (!URLUtil.isNetworkUrl(path)) {
                            continue
                        }
                        if (this.quickItems != null) {
                            val item: SearchItem =
                                item(32, cursor.getLong(idColumn), path, cursor.getString(titleColumn))
                            item.letterColor = cursor.getInt(iconColumn)
                            this.quickItems!!.add(item)
                        }
                        addDomain(path)
                    } while (cursor.moveToNext())
                }
            } catch (ignored: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
        }

        fun addDomain(path: String?) {
            if (this.domains == null) {
                return
            }
            val domain = pathOfUrl(path, true)
            if (!TextUtils.isEmpty(domain) && !this.domains!!.contains(domain)) {
                this.domains!!.add(domain)
            }
        }

        fun clearAll() {
            this.historyItems = null
            this.bookItems = null
            this.quickItems = null
            this.domains = null
        }

        public override fun onCancelled() {
            val webViewActivity = this.activityRef.get() as WebViewActivity?
            if (webViewActivity == null) {
                return
            }
            webViewActivity.searchTask = null
        }

        public override fun onPostExecute() {
            val webViewActivity = this.activityRef.get() as WebViewActivity?
            if (webViewActivity != null) {
                webViewActivity.searchTask = null
                if (webViewActivity.quickState.editingAddress) {
                    val history = this.historyItems
                    val bookmarks = this.bookItems
                    val quickLinks = this.quickItems
                    val domainList = this.domains
                    val myEditAuto = webViewActivity.editAuto
                    if (myEditAuto != null) {
                        if (ZtwoSettings.autoComp.value) {
                            myEditAuto.setAutoList(domainList)
                        }
                        val webSearchAdapter = WebSearchAdapter(
                            webViewActivity,
                            history,
                            bookmarks,
                            quickLinks,
                            webViewActivity.onStartPage,
                            !webViewActivity.isSingleBarMode(),
                            WvaQuickSearchListener(webViewActivity)
                        )
                        webViewActivity.searchAdapter = webSearchAdapter
                        webViewActivity.editAuto!!.setDropDownFilter(webSearchAdapter.searchFilter)
                        val quickSearch = webViewActivity.quickSearch
                        if (quickSearch != null) {
                            quickSearch.setAutoAdapter(webViewActivity.searchAdapter)
                        }
                    }
                }
            }
        }

        companion object {
            private fun item(type: Int, id: Long, path: String?, title: String?): SearchItem {
                val item = SearchItem()
                item.type = type.toByte()
                item.id = id
                item.path = path
                item.urlStart = schemeKind(path)
                item.title = title
                return item
            }
        }
    }

    internal class SortFace : Comparator<FaceItem?> {
        override fun compare(a: FaceItem?, b: FaceItem?): Int {
            val left = a
            val right = b
            if (left == null) {
                return if (right == null) 0 else 1
            }
            if (right == null) {
                return -1
            }
            val leftHidden = left.isHidden
            val rightHidden = right.isHidden
            if (leftHidden) {
                if (!rightHidden) {
                    return 1
                }
            } else if (rightHidden) {
                return -1
            }
            val leftIndex = left.index
            val rightIndex = right.index
            if (leftIndex < rightIndex) {
                return -1
            }
            return if (leftIndex > rightIndex) 1 else 0
        }
    }

    internal class SortName : Comparator<ChildItem?> {
        override fun compare(a: ChildItem?, b: ChildItem?): Int {
            val left = a
            val right = b
            if (left == null) {
                return if (right == null) 0 else 1
            }
            if (right == null) {
                return -1
            }
            val leftName = left.name
            if (TextUtils.isEmpty(leftName)) {
                return 1
            }
            val rightName = right.name
            if (TextUtils.isEmpty(rightName)) {
                return -1
            }
            return compareStrings(leftName, rightName, false)
        }
    }

    interface TabAddListener {
        fun onTabAdded(isAdded: Boolean)
    }

    interface TabLimitListener {
        fun onLimitApplied(isChanged: Boolean)
    }

    interface TabViewListener {
        fun onTabView()
    }

    fun addTabPlain(
        webNestFrame: WebNestFrame?,
        url: String?,
        isTop: kotlin.Boolean,
        tabAddListener: TabAddListener?
    ) {
        addTabImpl(webNestFrame, url, isTop, false, false, tabAddListener)
    }

    public override fun dispatchTouchEvent(event: MotionEvent?): kotlin.Boolean =
        touch.dispatch(event) { super.dispatchTouchEvent(it) }

    override fun finish() = lifecycleFinish()

    public override fun onLauncherResult(requestCode: Int, resultCode: Int, intent: Intent?) = handleActivityResult(requestCode, resultCode, intent)

    public override fun onBackRequested() = startEdgeGesture()

    override fun onActionModeFinished(actionMode: ActionMode?) {
        val dialogViewRead = this.dialogsState.viewReadDialog
        if (dialogViewRead != null) {
            dialogViewRead.actionModeRef = null
        }
        this.dialogsState.actionMode = null
        super.onActionModeFinished(actionMode)
    }

    override fun onActionModeStarted(actionMode: ActionMode?) = lifecycleOnActionModeStarted(actionMode, { p0 -> super.onActionModeStarted(p0) })

    /**
     * Android 12L only (minSdk 32): from Android 13 on, back goes through [BackKeyStep] instead. No `super` call on
     * purpose: it would close the activity, while back here first closes popups and steps back in the page / tabs.
     */
    @Deprecated("Deprecated in Java")
    @android.annotation.SuppressLint("MissingSuperCall")
    public override fun onBackPressed() {
        if (dismissPopups()) {
            return
        }
        onBackRequested()
    }

    public override fun onConfigurationChanged(configuration: Configuration) = lifecycleOnConfigurationChanged(configuration, { p0 -> super.onConfigurationChanged(p0) })

    public override fun onCreate(savedInstanceState: Bundle?) = lifecycleOnCreate(savedInstanceState, { p0 -> super.onCreate(p0) })

    override fun onCreateContextMenu(contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenuInfo?) = lifecycleOnCreateContextMenu(contextMenu, view, contextMenuInfo)

    public override fun onDestroy() = lifecycleOnDestroy({ super.onDestroy() })

    public override fun onKeyDown(keyCode: Int, event: KeyEvent?): kotlin.Boolean = lifecycleOnKeyDown(keyCode, event, { p0, p1 -> super.onKeyDown(p0, p1) })

    override fun onNewIntent(intent: Intent) = lifecycleOnNewIntent(intent, { p0 -> super.onNewIntent(p0!!) })

    public override fun onPause() = lifecycleOnPause({ super.onPause() })

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = lifecycleOnRequestPermissionsResult(requestCode, permissions, grantResults, { p0, p1, p2 -> super.onRequestPermissionsResult(p0, p1, p2) })

    public override fun onResume() = lifecycleOnResume({ super.onResume() })

    override fun onStart() {
        super.onStart()
        this.newIntentPending = true
    }

    /** Android is short of memory: hidden pages are released (web/WebViewActivityMemory, 2026-11-21). */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        releaseMemory(level)
    }

    @Suppress("DEPRECATION")
    override fun onLowMemory() {
        super.onLowMemory()
        releaseMemory(android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val webVideoFrame: WebVideoFrame? = if (!this.dialogsState.leavingByIntent) this.videoFrame else null
        if (!this.dialogsState.leavingByIntent && webVideoFrame != null) {
            webVideoFrame.enterPipMode(false)
        }
        this.dialogsState.leavingByIntent = false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) = lifecycleOnWindowFocusChanged(hasFocus, { p0 -> super.onWindowFocusChanged(p0) })

    override fun startActivity(intent: Intent?) = lifecycleStartActivity(intent)

    public override fun launchExternalForResult(intent: Intent?, requestCode: Int) {
        this.dialogsState.leavingByIntent = true
        launchForResult(intent, requestCode)
    }

    fun addTabInGroup(
        webNestFrame: WebNestFrame?,
        url: String?,
        isTop: kotlin.Boolean,
        tabAddListener: TabAddListener?
    ) {
        addTabImpl(webNestFrame, url, isTop, isInTabGroup(this.currentTabIndex), TtsSettings.autoGroup.value, tabAddListener)
    }

    fun startExternalActivity(intent: Intent?) {
        if (intent == null) {
            return
        }
        this.dialogsState.leavingByIntent = true
        super.startActivity(intent)
    }

    fun finishWithCleanup() = finishImpl({ super.finish() })

    companion object {
        /** How long a newly added tab may wait hidden for its page's first paint before it is shown anyway. */
        const val SHOW_ADDED_TAB_MS = 4000L

        /** Returns the first http URL of a `<source>` chunk whose type matches, or null.  */
        @JvmStatic
        fun extractSourceUrl(sources: Array<out String?>?, type: String?): String? {
            for (src in sources!!) {
                if (TextUtils.isEmpty(src) || !src!!.contains(type!!)) {
                    continue
                }
                val start = src.indexOf("http")
                if (start == -1) {
                    continue
                }
                val end = src.indexOf(34.toChar(), start + 4)
                if (end == -1) {
                    continue
                }
                val url = src.substring(start, end)
                if (!TextUtils.isEmpty(url)) {
                    return url.replace("amp;", "")
                }
            }
            return null
        }

        @JvmField
        val Wn: Int
        @JvmField
        val Xn: Int
        @JvmField
        val Yn: Int
        @JvmField
        var Zn: Int = 0

        init {
            val dp = MainApp.dp40
            Wn = dp
            Xn = dp
            Yn = dp / 2
        }

        @JvmStatic
        fun pauseMedia(webView: WebView?) {
            if (webView == null) {
                return
            }
            evalJs(
                webView,
                "(function(){var vds=document.querySelectorAll(\"video,audio\");if(vds&&(vds.length>0)){for(var i=0;i<vds.length;i++){if(!vds[i].paused){vds[i].pause();}}}})();",
                false
            )
        }

        @JvmStatic
        fun pauseWebView(webView: WebView?) {
            if (webView != null) {
                webView.onPause()
            }
        }

        @JvmStatic
        fun barSizeFor(isTop: kotlin.Boolean): Int {
            val index = if ((isTop == true)) PdfSettings.topHeight.value else PdfSettings.botHeight.value
            val dp48Px = MainApp.dp48
            return if (index < dp48Px) dp48Px else index
        }

        @JvmStatic
        fun homePageUrl(): String? {
            val homeUrl = normalizeUrl(null, BrowserSettings.homePage.value)
            return if (!TextUtils.isEmpty(homeUrl)) homeUrl else "file:///android_asset/shortcut.html"
        }

        @JvmStatic
        fun applyDarkSettings(webNestView: WebNestView?) {
            if (webNestView == null) {
                return
            }
            setAlgorithmicDark(webNestView.getSettings(), AppState.darkWeb)
        }

        @JvmStatic
        fun checkedPageUrl(url: String?): String? {
            if (TextUtils.isEmpty(url)) {
                return null
            }
            if (url!!.startsWith("http")) {
                if (url.length >= 10000) {
                    val host = MainUtil.hostOf(url, true)
                    if (TextUtils.isEmpty(host) || !host!!.startsWith("http") || host.length >= 10000) {
                        return null
                    }
                    return host
                }
            } else if ("file:///android_asset/shortcut.html" != url) {
                return null
            }
            return url
        }

        @JvmStatic
        fun isTouchFrameActive(myTouchFrame: MyTouchFrame?): kotlin.Boolean {
            if (myTouchFrame == null) {
                return false
            }
            return myTouchFrame.touchActive
        }

        @JvmStatic
        fun newTabUrl(): String? {
            if ("..home_link" == BrowserSettings.tabPage.value) {
                return homePageUrl()
            }
            val newTabPage = normalizeUrl(null, BrowserSettings.tabPage.value)
            return if (!TextUtils.isEmpty(newTabPage)) newTabPage else "file:///android_asset/shortcut.html"
        }

        @JvmStatic
        fun tabTitleFor(context: Context?, webTabItem: WebTabItem?): String? {
            var groupName: String? = null
            if (context == null || webTabItem == null) {
                return null
            }
            val members = webTabItem.groupTabs
            if (members != null) {
                val firstTab: WebTabItem? = if (members.size > 0) (webTabItem.groupTabs!!.get(0)) else null
                if (members.size > 0 && firstTab != null) {
                    groupName = firstTab.groupName
                }
                return if (!TextUtils.isEmpty(groupName)) groupName else context.getString(AppR.string.group_title)
            }
            if (TextUtils.isEmpty(webTabItem.tabUrl)) {
                return context.getString(AppR.string.no_title)
            }
            if ("file:///android_asset/shortcut.html" == webTabItem.tabUrl) {
                return "OPUM"
            }
            if (!TextUtils.isEmpty(webTabItem.tabTitle)) {
                val siteName = siteNameOf(webTabItem.tabTitle, false)
                webTabItem.tabTitle = siteName
                if (!TextUtils.isEmpty(siteName)) {
                    return webTabItem.tabTitle
                }
            }
            val hostLabel = hostLabelUpper(hostNameOf(webTabItem.tabUrl, true))
            webTabItem.tabTitle = hostLabel
            return if (!TextUtils.isEmpty(hostLabel)) webTabItem.tabTitle else context.getString(AppR.string.no_title)
        }

        @JvmStatic
        fun isSegmentUrl(digitEnd: Int, url: String?): kotlin.Boolean {
            val lastDigit = digitEnd - 1
            val lastChar = if (lastDigit < 0) ' ' else url!!.get(lastDigit)
            val slashIndex = if (lastDigit < 0 || lastChar < '0' || lastChar > '9') 0 else url!!.lastIndexOf(
                    47.toChar(),
                    lastDigit
                )
            if (lastDigit < 0 || lastChar < '0' || lastChar > '9' || slashIndex < 0) {
                return false
            }
            val digitStart = slashIndex + 1
            return if (indexOfWord(url, digitStart, lastDigit, "index") == -1 && indexOfWord(url, digitStart, lastDigit, "segment") == -1 && indexOfWord(
                    url,
                    digitStart,
                    lastDigit,
                    "seg-"
                ) == -1
            ) false else true
        }

        @JvmStatic
        fun isVkVideoPage(pageHost: String?, pageUrl: String?): kotlin.Boolean {
            if (TextUtils.isEmpty(pageHost)) {
                return false
            }
            if (pageHost!!.endsWith("vk.com")) {
                if (TextUtils.isEmpty(pageUrl)) {
                    return false
                }
                return pageUrl!!.startsWith("https://m.vk.com/video")
            }
            if (!pageHost.endsWith("vkvideo.ru") || TextUtils.isEmpty(pageUrl)) {
                return false
            }
            return pageUrl!!.startsWith("https://m.vkvideo.ru")
        }

        @JvmStatic
        fun hasCustomZones(): kotlin.Boolean {
            return if (ZoneSettings.gesUpLeft.value == 0 && ZoneSettings.gesUpRight.value == 0 && ZoneSettings.gesDownLeft.value == 0 && ZoneSettings.gesDownRight.value == 0) false else true
        }

        /** Greasemonkey rules: no @exclude may match; then any @include or @match must match (none given: all urls).  */
        internal fun scriptMatches(criteria: ScriptCriteria?, url: String?): kotlin.Boolean {
            if (criteria!!.excludes != null) {
                for (pattern in criteria.excludes) {
                    if (matches(pattern, url)) {
                        return false
                    }
                }
            }
            val includes = criteria.includes
            val matches = criteria.matches
            val hasIncludes = includes != null && includes.size != 0
            val hasMatches = matches != null && matches.size != 0
            if (!hasIncludes && !hasMatches) {
                return true
            }
            if (includes != null) {
                for (pattern in includes) {
                    if (matches(pattern, url)) {
                        return true
                    }
                }
            }
            if (matches != null) {
                for (pattern in matches) {
                    if (matches(pattern, url)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}
