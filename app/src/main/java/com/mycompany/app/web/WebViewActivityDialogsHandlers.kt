@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.ui.state.AppState
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.VideoSettings
import com.mycompany.app.web.WebNestView.ClearCheckStateTask
import com.mycompany.app.web.WebNestView.RemoveSslViewTask
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.wildcardHost
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.indexOfWord
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.isFilmizle
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.isYouTubeHost
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.restartLater
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.browserIntent
import com.mycompany.app.main.MainUtil.isLockEnabled
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.imageUrlFallback
import com.mycompany.app.main.MainUtil.extractUrl
import com.mycompany.app.main.MainUtil.imageUrlFor
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
import com.mycompany.app.main.MainUtil.isYouTubeUrl
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.nativeJs5b
import com.mycompany.app.main.MainUtil.mediaNameFor
import com.mycompany.app.main.MainUtil.viewportJs
import com.mycompany.app.main.MainUtil.isImagePath
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.main.MainUtil.injectOverlayJs
import com.mycompany.app.main.MainUtil.isHitomi
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.db.book.DbTabState.Companion.stateById
import com.mycompany.app.main.MainUtil.backupBitmapCopy
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.main.MainUtil.initUserAgent
import com.mycompany.app.main.MainUtil.defaultUserAgent
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.main.MainUtil.isOfflineMht
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.main.MainUtil.shortHostOf
import com.mycompany.app.main.MainUtil.cachedIcon
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.main.MainUtil.youTubeIdOf
import com.mycompany.app.main.MainUtil.isFullScreenOff
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.restartTask
import com.mycompany.app.main.MainUtil.instaAid
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.hasVideoButton
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.animateIcon
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.showToastText
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.restartActivity
import com.mycompany.app.main.MainUtil.lockIntent
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
import com.mycompany.app.db.book.DbBookTab.Companion.clearSecret
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.db.book.DbBookTab.Companion.updateParents
import com.mycompany.app.db.book.DbBookTab.Companion.deleteTabs
import com.mycompany.app.main.MainUtil.toggleSystemBars
import com.mycompany.app.main.MainUtil.applySystemBarsMode
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.isYouTubeShorts
import com.mycompany.app.main.MainUtil.hasZtwoFlag
import com.mycompany.app.main.MainUtil.initMaxTextureSize
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.db.book.DbBookQuick.Companion.hasPath
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.book.DbBookQuick.Companion.nextColor
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.pref.PrefZtwo.Companion.saveNews
import com.mycompany.app.db.book.DbBookTab.Companion.loadTabs
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.pref.PrefSync.Companion.saveIndexes
import com.mycompany.app.main.MainUtil.flushCookies
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.filesSubdir
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.youTubeAutoplayJs
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookRecent.Companion.clearSecret
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.main.MainUtil.pageTitleForFile
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.setIcon
import com.mycompany.app.main.MainUtil.applyBarAppearance
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.handleDownloadLink
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.main.MainUtil.openInExternalApp
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.clipboardText
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.notifyBookUpdate
import com.mycompany.app.db.book.DbBookWeb.Companion.idByPath
import com.mycompany.app.main.MainUtil.pageUrlOrNull
import com.mycompany.app.db.book.DbBookWeb.Companion.deleteById
import com.mycompany.app.main.MainUtil.isGoogleUserContent
import com.mycompany.app.main.MainUtil.openVideoWithApp
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.nextPageUrl
import com.mycompany.app.main.MainUtil.isPhnGif
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.openWithChooser
import com.mycompany.app.main.MainUtil.quickViewPdf
import com.mycompany.app.main.MainUtil.glideUrlFor
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.db.book.DbBookDown.Companion.addFile
import com.mycompany.app.main.MainUtil.shareText
import com.mycompany.app.main.MainUtil.freeMemory
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookIcon.Companion.iconOf
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.openCustomTab
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.evalConfirmJs
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaMultiJs
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.main.MainUtil.areaUpScript
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookQuick.Companion.iconRes
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
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
import com.mycompany.app.main.MainUtil.lockAppData
import com.mycompany.app.main.MainUtil.isAudioPlaying
import com.mycompany.app.main.MainUtil.findMediaJs
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
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
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.addShortcut
import com.mycompany.app.main.MainUtil.getPassJs3
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.checkTransClassJs
import com.mycompany.app.pref.PrefAlbum.Companion.saveTrans
import com.mycompany.app.main.MainUtil.parseColorText
import com.mycompany.app.main.MainUtil.lighterColor
import com.mycompany.app.db.book.DbBookScript.Companion.findScript
import com.mycompany.app.db.book.DbBookScript.Companion.getValue
import com.mycompany.app.db.book.DbBookScript.Companion.valueKeys
import com.mycompany.app.db.book.DbBookScript.Companion.putValue
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.main.MainUtil.needsDesktopUa
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUtil.setNavBarColor
import com.mycompany.app.main.MainUtil.isXLoginPage
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookWeb.Companion.hasPath
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.isGoogleHome
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.isUserScriptUrl
import com.mycompany.app.main.MainUtil.applyScreenOrientation
import com.mycompany.app.main.MainUtil.applyImageOrientation
import com.mycompany.app.main.MainUtil.applyIncognitoIme
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
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.main.MainUtil.highlightText
import com.mycompany.app.main.MainUtil.durationText
import com.mycompany.app.main.MainUtil.screenBrightnessPercent
import com.mycompany.app.web.WebClean
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.view.ViewGroup
import com.mycompany.app.web.WebNestView
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.webkit.WebResourceError
import android.webkit.RenderProcessGoneDetail
import org.jsoup.Jsoup
import com.mycompany.app.main.MainDownAdapter.DownListItem
import android.webkit.ValueCallback
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.ui.browser.IconButton
import android.webkit.SslErrorHandler
import android.net.http.SslError
import com.mycompany.app.web.WebSslView
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import com.mycompany.app.web.WebViewActivity
import android.content.Intent
import com.mycompany.app.web.WebLoadTask
import android.webkit.WebChromeClient
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.quick.QuickView
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbTabState
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.data.book.DataBookAds
import android.view.ViewParent
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefMain
import android.os.Build
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefRead
import android.app.Activity
import com.mycompany.app.ui.components.LoadCover
import com.mycompany.app.pref.PrefVideo
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.dialog.DialogConfirm.DialogConfListener
import android.content.DialogInterface
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.web.WebVideoFrame
import android.webkit.WebChromeClient.CustomViewCallback
import com.mycompany.app.view.MyFadeRelative
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.dialog.DialogSeekBright
import com.mycompany.app.dialog.DialogSeekAudio
import com.mycompany.app.web.WebVideoFull
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.view.PopMenuItem
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.web.WebVideoImage
import com.mycompany.app.web.WebTransControl.TransCtrlListener
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.dialog.DialogTransLang
import com.mycompany.app.dialog.DialogWebSelect
import com.mycompany.app.dialog.DialogSetMsg
import com.mycompany.app.dialog.DialogMenuMain
import com.mycompany.app.dialog.DialogViewTrans
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.dialog.DialogTabEdit
import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.web.WebViewActivity.TabViewListener
import android.speech.tts.TextToSpeech
import com.mycompany.app.dialog.DialogUpdateScript
import com.mycompany.app.dialog.DialogSetDesk
import com.mycompany.app.dialog.DialogTabMenu
import com.mycompany.app.dialog.DialogPreview
import com.mycompany.app.dialog.DialogOpenType
import com.mycompany.app.web.WebViewActivity.PrevBackListener
import com.mycompany.app.dialog.DialogPopupMenu
import com.mycompany.app.dialog.DialogNewsMenu
import com.mycompany.app.dialog.DialogSeekWebText
import com.mycompany.app.dialog.DialogNewsLocale
import com.mycompany.app.dialog.DialogSetFull
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.dialog.DialogNewsSearch
import com.mycompany.app.dialog.DialogSetDark
import com.mycompany.app.dialog.DialogSetUseTts
import com.mycompany.app.dialog.DialogWebBookEdit
import com.mycompany.app.dialog.DialogSetAdblock
import android.webkit.WebChromeClient.FileChooserParams
import com.mycompany.app.dialog.DialogSetJava
import com.mycompany.app.wview.WebAreaView
import com.mycompany.app.dialog.DialogSetTmem
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.dialog.DialogSetRead
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.dialog.DialogGuideNoti
import androidx.viewpager2.widget.ViewPager2
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.dialog.DialogViewSrc
import com.mycompany.app.dialog.DialogGuideArea
import com.mycompany.app.dialog.DialogSetPrivacy
import com.mycompany.app.dialog.DialogDownBlob
import com.mycompany.app.view.MyDialogBottom
import com.mycompany.app.dialog.DialogEditArea
import com.mycompany.app.dialog.DialogSetCookie
import com.mycompany.app.dialog.DialogSetScrFil
import android.webkit.JsResult
import com.mycompany.app.dialog.DialogPrompt
import android.webkit.PermissionRequest
import com.mycompany.app.dialog.DialogEditAuth
import com.mycompany.app.dialog.DialogPermission
import android.webkit.GeolocationPermissions
import com.mycompany.app.dialog.DialogCapture
import com.mycompany.app.dialog.DialogDownPage
import com.mycompany.app.dialog.DialogPrintPage
import com.mycompany.app.dialog.DialogPassInfo
import com.mycompany.app.view.MySnackbar.SnackbarListener
import com.mycompany.app.view.MySnackbar.SnackItem
import com.mycompany.app.wview.WebFltView.FltViewListener
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.db.book.DbBookQuick
import android.content.ContentValues
import com.mycompany.app.script.ScriptCriteria
import com.mycompany.app.main.MainDownSvc.DownBusyListener
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.main.Md5FileNameGenerator
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.dialog.DialogConfirm.DialogConfBtnListener
import com.mycompany.app.dialog.DialogSetMsg.DialogMsgListener
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.dialog.DialogMenuMain.DownMenuListener
import com.mycompany.app.dialog.DialogWebBookEdit.BookEditListener
import android.content.ActivityNotFoundException
import com.mycompany.app.dialog.DialogDownUrl.DownUrlListener
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.dialog.DialogDownBlob.DialogBlobListener
import com.mycompany.app.view.MyWebCoord.MyCaptureListener
import android.graphics.drawable.BitmapDrawable
import com.mycompany.app.db.book.DbBookPage
import com.mycompany.app.dialog.DialogPrintPage.PathChangeListener
import android.print.PrintManager
import android.print.PrintAttributes
import com.mycompany.app.data.DataTrans
import com.mycompany.app.wview.WebAreaView.WebAreaListener
import com.mycompany.app.dialog.DialogGuideArea.DialogAreaListener
import com.mycompany.app.dialog.DialogDeleteItem.DelItemListener
import com.mycompany.app.db.book.DbBookPms
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.dialog.DialogPassInfo.PassInfoListener
import com.mycompany.app.dialog.DialogWebSelect.WebSelectListener
import com.mycompany.app.pref.PrefList
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.dialog.DialogSetDark.SetDarkListener
import com.mycompany.app.dialog.DialogSetRead.SetReadListener
import android.graphics.drawable.ColorDrawable
import android.webkit.JsPromptResult
import com.mycompany.app.web.WebViewActivity.SortName
import com.mycompany.app.main.MainNative
import android.webkit.MimeTypeMap
import com.mycompany.app.script.WebViewXmlHttpRequest
import com.mycompany.app.main.MainUrl
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import com.mycompany.app.main.MainUrl.MedItem
import com.mycompany.app.script.Script
import ch.opumlabs.opumbrowser.R
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLDecoder
import java.util.ArrayList
import java.util.Collections
import java.util.Scanner
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import com.mycompany.app.web.WebViewActivity.Companion.Wn
import com.mycompany.app.web.WebViewActivity.Companion.homePageUrl
import com.mycompany.app.web.WebViewActivity.Companion.checkedPageUrl
import com.mycompany.app.web.WebViewActivity.Companion.newTabUrl
import com.mycompany.app.web.WebViewActivity.Companion.isSegmentUrl
import com.mycompany.app.web.WebViewActivity.Companion.isVkVideoPage
import com.mycompany.app.web.WebViewActivity.Companion.extractSourceUrl
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbBookRecent

/*
 * The browser activity's dialogs callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaTabGroupDeleteTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val tabs: ArrayList<Any?>,
                                       @JvmField val index: Int
) : Runnable {

    internal inner class GroupDeleteDoneStep(@JvmField val outer: WvaTabGroupDeleteTask,
                                         @JvmField val index: Int
    ) : Runnable {

        internal inner class CloseGroupDialogStep : Runnable {
            override fun run() {
                val deleteTask = this@GroupDeleteDoneStep.outer
                val webViewActivity = deleteTask.webViewActivity
                webViewActivity.closeTabGroupDeleteDialog()
                showToast(deleteTask.webViewActivity, AppR.string.deleted)
            }
        }

        internal inner class GroupDeleteReloadedListener(@JvmField val outer: GroupDeleteDoneStep,
                                             @JvmField val tabIndex: Int
        ) : WebViewActivity.TabViewListener {

            internal inner class CloseGroupDialogAfterReloadStep : Runnable {
                override fun run() {
                    val doneStep = this@GroupDeleteReloadedListener.outer
                    val webViewActivity = doneStep.outer.webViewActivity
                    webViewActivity.closeTabGroupDeleteDialog()
                    showToast(doneStep.outer.webViewActivity, AppR.string.deleted)
                }
            }

            override fun onTabView() {
                val deleteTask = this.outer.outer
                val webViewActivity = deleteTask.webViewActivity
                webViewActivity.refreshTabBarItems(this.tabIndex)
                val myWebCoord = deleteTask.webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CloseGroupDialogAfterReloadStep())
            }
        }

        override fun run() {
            val webViewActivity = this.outer.webViewActivity
            val arrayList = webViewActivity.tabsState.tabItems
            if (arrayList != null) {
                val size = arrayList.size
                val index = this.index
                if (size != 0) {
                    webViewActivity.reloadTabViews(GroupDeleteReloadedListener(this, index))
                    return
                }
                webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), false, null)
                webViewActivity.refreshTabBarItems(index)
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CloseGroupDialogStep())
            }
        }
    }

    override fun run() {
        synchronized(this.webViewActivity.tabLock) {
            val arrayList = this.tabs
            val size = arrayList.size
            var closedBefore = 0
            var index = 0
            while (index < size) {
                val entry: Any? = arrayList.get(index)
                index++
                val webTabItem = entry as WebTabItem
                val tabItems = this.webViewActivity.tabsState.tabItems
                if (tabItems == null || tabItems.isEmpty()) {
                    break
                }
                this.webViewActivity.forgetTabItem(webTabItem)
                this.webViewActivity.tabsState.tabItems!!.remove(webTabItem)
                if (webTabItem.position <= this.webViewActivity.currentTabIndex) {
                    closedBefore++
                }
            }
            val webViewActivity = this.webViewActivity
            webViewActivity.currentTabIndex -= closedBefore
            webViewActivity.clampTabIndex()
            DbBookTab.updateParents(this.webViewActivity.dialogsState.appContext, (this.webViewActivity.tabsForSave() as ArrayList<WebTabItem?>?))
            DbBookTab.deleteTabs(this.webViewActivity.dialogsState.appContext, (this.tabs as ArrayList<WebTabItem?>?))
        }
        val myWebCoord = this.webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(GroupDeleteDoneStep(this, this.index))
    }
}

class WvaRunDownActionTask(@JvmField val wa: WebViewActivity, @JvmField val action: Byte = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val action = this@WvaRunDownActionTask.action
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            if (action.toInt() == 0 && webViewActivity.videoFrame != null && webViewActivity.dialogsState.videoMode.toInt() == 4) {
                webViewActivity.openMediaUrl(webViewActivity.dialogsState.pageUrl, null)
            } else {
                val downVideo = webNestView.downVideo
                if (TextUtils.isEmpty(downVideo)) {
                    if (action.toInt() == 0) {
                        showToast(webViewActivity, AppR.string.no_down_video)
                    } else {
                        showToast(webViewActivity, AppR.string.not_support_video)
                    }
                } else if (downVideo == "face_dummy") {
                    evalJs(
                        webViewActivity.currentPage,
                        StringBuilder(("(function(){var nam=null;var eles=document.querySelectorAll(\"button[id='sb_btn_dn1']\");if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){var tp=eles[i].getBoundingClientRect().top;if((tp>6)&&(tp<14)){nam=eles[i].name;break;}}}android.onFaceDet(nam,")).append(action.toInt()).append(");})();").toString(),
                        true
                    )
                } else if (downVideo == "insta_dummy") {
                    if (action.toInt() == 0) {
                        webViewActivity.openMediaUrl(downVideo, null)
                    } else {
                        webViewActivity.playVideoUrl(downVideo, false, action.toInt())
                    }
                } else if (downVideo == "twit_dummy") {
                    if (action.toInt() == 0) {
                        webViewActivity.openMediaUrl(downVideo, null)
                    } else {
                        webViewActivity.playVideoUrl(downVideo, false, action.toInt())
                    }
                } else if (downVideo == "vid_dummy") {
                    webViewActivity.showVideoListDialog(action.toInt())
                    val dialogVideoList = webViewActivity.videoListDialog
                    if (dialogVideoList != null) {
                        dialogVideoList.startScan(1)
                    }
                } else if (downVideo == "pub_dummy") {
                    webViewActivity.showVideoListDialog(action.toInt())
                    val videoListDialog = webViewActivity.videoListDialog
                    if (videoListDialog != null) {
                        videoListDialog.startScan(2)
                    }
                } else if (downVideo == "pgf_dummy") {
                    webViewActivity.showVideoListDialog(action.toInt())
                    val existingDialog = webViewActivity.videoListDialog
                    if (existingDialog != null) {
                        existingDialog.startScan(3)
                    }
                } else if (downVideo == "vkv_dummy") {
                    webViewActivity.showVideoListDialog(action.toInt())
                    if (webViewActivity.videoListDialog != null) {
                        val arrayList = webViewActivity.dialogsState.vkvListListenerList
                        if (arrayList == null || arrayList.isEmpty()) {
                            webViewActivity.videoListDialog!!.startVkv(
                                webViewActivity.webCoord,
                                WvaVkvListListener(webViewActivity)
                            )
                        } else {
                            val newDialog = webViewActivity.videoListDialog
                            val vkvListListenerList = webViewActivity.dialogsState.vkvListListenerList
                            newDialog!!.scanState = 5.toByte()
                            newDialog.foundItems = vkvListListenerList
                            newDialog.foundState = 0.toByte()
                        }
                    }
                } else {
                    val downList = webViewActivity.currentPage!!.downList
                    if (downList != null && downList.size > 1) {
                        webViewActivity.showVideoListDialog(action.toInt())
                    } else if (action.toInt() == 0) {
                        webViewActivity.openMediaUrl(downVideo, null)
                    } else {
                        webViewActivity.playVideoUrl(downVideo, false, action.toInt())
                    }
                }
            }
        }
        webViewActivity.miscState.runDownActionBusy = false
    }
}

class VideoRateQuery(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.setRateDialog == null) {
            return
        }
        evalJs(
            webViewActivity.currentPage,
            "(function(){var ele=document.querySelector(\"video\");if(ele){android.onRateGet(ele.playbackRate);}})();",
            true
        )
    }
}

class WvaSetRateDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {

    internal inner class InjectRateScriptStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.injectVideoRateScript(true, false)
        }
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetRate = webViewActivity.dialogsState.setRateDialog
        if (dialogSetRate != null) {
            dialogSetRate.dismiss()
            webViewActivity.dialogsState.setRateDialog = null
        }
        val lastYouSpeedSave = webViewActivity.dialogsState.lastYouSpeedSave
        val changed = ZtwoSettings.youSpeedSave.value
        if (lastYouSpeedSave != changed) {
            webViewActivity.dialogsState.lastYouSpeedSave = changed
            val myWebCoord: MyWebCoord? = if (!changed) null else webViewActivity.webCoord
            if (!changed || myWebCoord == null) {
                return
            }
            myWebCoord.post(InjectRateScriptStep())
        }
    }
}

class WvaCreateSetup(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class CreateSetupStep : Runnable {

        internal inner class LockedSetupStep : Runnable {
            override fun run() {
                val setupLock = wa.setupLock
                if (setupLock == null) {
                    return
                }
                synchronized(setupLock) {
                    val webViewActivity = wa
                    webViewActivity.dialogsState.setupDone = true
                    if (webViewActivity.dialogsState.viewsCreated) {
                        webViewActivity.startPageSetup()
                    }
                }
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.updateTabCountIcons()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(LockedSetupStep())
        }
    }

    override fun run() {
        var onStartPage: Boolean = false
        val uid: Long
        val pageUrl: String?
        val webViewActivity = wa
        MainApp.loadSettingsOnce(webViewActivity.dialogsState.appContext, webViewActivity.getResources())
        initUserAgent(webViewActivity.dialogsState.appContext)
        initMaxTextureSize(webViewActivity.dialogsState.appContext)
        MainApp.applyCookieSetting(webViewActivity.dialogsState.appContext)
        if (SecretSettings.initQuick.value) {
            SecretSettings.editLocal { set(SecretSettings.initQuick, false) }
            putBoolNow(9, webViewActivity.dialogsState.appContext, "mInitQuick", false)
            val context = webViewActivity.dialogsState.appContext
            val dbBookQuick = DbBookQuick.cached
            if (context != null) {
                val siteUrls = arrayOf<String?>(
                    "https://www.google.com/",
                    "https://m.youtube.com/",
                    "https://m.facebook.com/",
                    "https://www.instagram.com/",
                    "https://x.com/"
                )
                val args =
                    arrayOf<String?>("Google", "YouTube", "Facebook", "Instagram", "X")
                val siteIcons = intArrayOf(
                    AppR.drawable.baseline_search_google,
                    AppR.drawable.baseline_search_youtube,
                    AppR.drawable.ic_facebook,
                    AppR.drawable.ic_instagram,
                    AppR.drawable.ic_twitter
                )
                for (index in 0..4) {
                    val uriString = siteUrls[index]
                    if (!DbBookQuick.hasPath(context, uriString)) {
                        val contentValuesG = android.content.ContentValues().also { cv -> cv.put("_path", uriString) }
                        contentValuesG.put("_title", args[index])
                        contentValuesG.put("_order", index)
                        contentValuesG.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
                        val bitmapD = decodeResource(context.getResources(), siteIcons[index])
                        if (isBitmapOk(bitmapD)) {
                            try {
                                val byteArrayOutputStream = ByteArrayOutputStream()
                                bitmapD!!.compress(
                                    Bitmap.CompressFormat.PNG,
                                    100,
                                    byteArrayOutputStream
                                )
                                contentValuesG.put("_icon", byteArrayOutputStream.toByteArray())
                                byteArrayOutputStream.close()
                            } catch (unused: Exception) {
                            }
                        } else {
                            contentValuesG.put("_rsv4", nextColor())
                        }
                        insert(DbBookQuick.get(context).getWritableDatabase(), "DbBookQuick_table", contentValuesG)
                    }
                }
            }
        }
        if (ZtwoSettings.newsTopic.value == 9) {
            ZtwoSettings.editLocal { set(ZtwoSettings.newsTopic, 0) }
            ZtwoSettings.editLocal { set(ZtwoSettings.newsSearch, "") }
            PrefZtwo.saveNews(webViewActivity.dialogsState.appContext)
        }
        val createBusy = webViewActivity.dialogsState.createBusy
        val addTabPending = webViewActivity.addTabPending
        val createFlag = webViewActivity.dialogsState.extraStart
        webViewActivity.dialogsState.createBusy = false
        webViewActivity.addTabPending = false
        webViewActivity.dialogsState.extraStart = false
        try {
            val exitDelete = BrowserSettings.exitDelete.value
            if (exitDelete != 126) {
                val secret = SyncSettings.secretMode.value
                if (secret) {
                    if ((exitDelete and 64) != 64) {
                        webViewActivity.tabsState.tabItems = loadTabs(webViewActivity.dialogsState.appContext, secret)
                    }
                } else if ((exitDelete and 32) != 32) {
                    webViewActivity.tabsState.tabItems = loadTabs(webViewActivity.dialogsState.appContext, secret)
                }
            }
            val arrayList = webViewActivity.tabsState.tabItems
            if (arrayList == null || arrayList.isEmpty()) {
                webViewActivity.currentTabIndex = webViewActivity.insertTabItem(0, WebViewActivity.newTabUrl(), null, false, false)
            }
            if (webViewActivity.dialogsState.useCurrentTab) {
                val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
                if (webTabItemD != null) {
                    uid = webTabItemD.uid
                    pageUrl = webTabItemD.tabUrl
                    webViewActivity.deskModeStart = webTabItemD.deskMode
                } else {
                    uid = 0
                    pageUrl = null
                }
                if (createBusy || TextUtils.isEmpty(BrowserSettings.startPage.value)) {
                    if (TextUtils.isEmpty(pageUrl)) {
                        webViewActivity.dialogsState.pageUrl = "file:///android_asset/shortcut.html"
                        webViewActivity.onStartPage = true
                        if (webTabItemD != null) {
                            webTabItemD.tabUrl = "file:///android_asset/shortcut.html"
                        }
                    } else {
                        webViewActivity.dialogsState.pageUrl = pageUrl
                        webViewActivity.onStartPage = "file:///android_asset/shortcut.html" == pageUrl
                        webViewActivity.pageState.stateItem = DbTabState.stateById(webViewActivity.dialogsState.appContext, uid)
                    }
                    if (webViewActivity.pageState.startupBusy) {
                        webViewActivity.onStartPage = false
                    }
                }
            } else if (!addTabPending) {
                if (!createFlag) {
                    val tabItems = webViewActivity.tabsState.tabItems
                    val size = tabItems!!.size
                    var index = 0
                    while (true) {
                        if (index >= size) {
                            webViewActivity.currentTabIndex = webViewActivity.insertTabItem(
                                webViewActivity.nextTabInsertIndex(false),
                                webViewActivity.dialogsState.pageUrl,
                                null,
                                false,
                                false
                            )
                            break
                        }
                        val entry: Any? = tabItems.get(index)
                        index++
                        val webTabItem = entry as WebTabItem?
                        if (webTabItem != null && sameObject(webViewActivity.dialogsState.pageUrl, webTabItem.tabUrl)) {
                            webViewActivity.currentTabIndex = webTabItem.position
                            webViewActivity.deskModeStart = webTabItem.deskMode
                            webViewActivity.pageState.stateItem = DbTabState.stateById(webViewActivity.dialogsState.appContext, webTabItem.uid)
                            break
                        }
                    }
                }
            } else if (!TextUtils.isEmpty(webViewActivity.dialogsState.pageUrl)) {
                val insertedIndex = webViewActivity.insertTabItem(
                    webViewActivity.nextTabInsertIndex(false),
                    webViewActivity.dialogsState.pageUrl,
                    null,
                    false,
                    false
                )
                webViewActivity.currentTabIndex = insertedIndex
                val webTabItemD = webViewActivity.tabItemAt(insertedIndex)
                if (webTabItemD != null) {
                    webTabItemD.reloadPending = true
                }
            }
            webViewActivity.clampTabIndex()
            if (BrowserSettings.deskLock.value) {
                webViewActivity.deskModeStart = true
            }
        } catch (unused2: Exception) {
        }
        if (!webViewActivity.pageState.startupBusy && webViewActivity.onStartPage != (("file:///android_asset/shortcut.html" == webViewActivity.dialogsState.pageUrl).also {
                onStartPage = it
            })) {
            webViewActivity.onStartPage = onStartPage
            webViewActivity.dialogsState.setupStarted = true
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(CreateSetupStep())
    }
}

class WvaOnResumeTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.mainHandler != null) {
            applyWindowBrightness(webViewActivity.getWindow(), PdfSettings.brightness.value, PdfSettings.userBrightness.value)
            if (PdfSettings.userScript.value) {
                webViewActivity.runInBackground(WvaResumeScriptCacheTask(webViewActivity))
            } else {
                val cacheExpired = webViewActivity.miscState.scriptCacheTime != 0L
                webViewActivity.miscState.scriptCacheTime = 0L
                val scriptCache = webViewActivity.miscState.scriptCache
                if (scriptCache != null) {
                    val scriptCriteriaArr: Array<ScriptCriteria>? = scriptCache.criteriaList as Array<ScriptCriteria>?
                    scriptCache.criteriaList = null
                    if (scriptCriteriaArr != null) {
                        try {
                            scriptCache.scriptsByUrl.clear()
                        } catch (unused: Exception) {
                        }
                    }
                    webViewActivity.miscState.scriptCache = null
                }
                webViewActivity.miscState.scriptCacheUrl = null
                if (cacheExpired) {
                    webViewActivity.reloadPage()
                }
            }
        }
    }
}

class WvaDownBusyListener(@JvmField val wa: WebViewActivity) : DownBusyListener {
    override fun hasFocus(): Boolean {
        return wa.hasWindowFocusNow
    }

    override fun isWaiting(): Boolean {
        val webViewActivity = wa
        if (webViewActivity.focusPending || webViewActivity.isBusy() || webViewActivity.menuJustClosed || webViewActivity.quickMenuJustClosed) {
            return true
        }
        val dialogSetMsg = webViewActivity.clearRecentDialog
        if (dialogSetMsg != null && dialogSetMsg.isOpen()) {
            return true
        }
        val dialogDeleteItem = webViewActivity.quickDeleteDialog
        if (dialogDeleteItem != null && dialogDeleteItem.isOpen()) {
            return true
        }
        val dialogQuickEdit = webViewActivity.dialogsState.quickEditDialog
        if (dialogQuickEdit != null && dialogQuickEdit.isOpen()) {
            return true
        }
        val deleteItemDialog = webViewActivity.deleteItemDialog
        if (deleteItemDialog != null && deleteItemDialog.isOpen()) {
            return true
        }
        val tabGroupDeleteDialog = webViewActivity.tabGroupDeleteDialog
        if (tabGroupDeleteDialog != null && tabGroupDeleteDialog.isOpen()) {
            return true
        }
        val dialogTabEdit = webViewActivity.dialogsState.tabEditDialog
        if (dialogTabEdit != null && dialogTabEdit.isOpen()) {
            return true
        }
        val dialogSetRate = webViewActivity.dialogsState.setRateDialog
        if (dialogSetRate != null && dialogSetRate.isOpen()) {
            return true
        }
        val dialogTabMenu = webViewActivity.tabMenuDialog
        if (dialogTabMenu != null && dialogTabMenu.isOpen()) {
            return true
        }
        val dialogNewsLocale = webViewActivity.newsLocaleDialog
        if (dialogNewsLocale != null && dialogNewsLocale.isOpen()) {
            return true
        }
        val dialogNewsSearch = webViewActivity.newsSearchDialog
        if (dialogNewsSearch != null && dialogNewsSearch.isOpen()) {
            return true
        }
        val dialogConfirm = webViewActivity.dialogsState.pathsResetDialog
        if (dialogConfirm != null && dialogConfirm.isOpen()) {
            return true
        }
        val dialogWebBookEdit = webViewActivity.dialogsState.bookEditDialog
        if (dialogWebBookEdit != null && dialogWebBookEdit.isOpen()) {
            return true
        }
        val dialogWebBookList = webViewActivity.bookmarksDialog
        if (dialogWebBookList != null && dialogWebBookList.isFullOpen()) {
            return true
        }
        val dialogListBook = webViewActivity.historyDialog
        if (dialogListBook != null && dialogListBook.isFullOpen()) {
            return true
        }
        val dialogEditShort = webViewActivity.editShortDialog
        if (dialogEditShort != null && dialogEditShort.isOpen()) {
            return true
        }
        val dialogUrlLink = webViewActivity.urlLinkDialog
        if (dialogUrlLink != null && dialogUrlLink.isOpen()) {
            return true
        }
        val dialogGuideNoti = webViewActivity.dialogsState.guideNotiDialog
        if (dialogGuideNoti != null && dialogGuideNoti.isOpen()) {
            return true
        }
        val downGuideDialog = webViewActivity.downGuideDialog
        if (downGuideDialog != null && downGuideDialog.isOpen()) {
            return true
        }
        val dialogDownUrl = webViewActivity.videoState.downUrlDialog
        if (dialogDownUrl != null && dialogDownUrl.isOpen()) {
            return true
        }
        val dialogSetDown = webViewActivity.setDownDialog
        if (dialogSetDown != null && dialogSetDown.isOpen()) {
            return true
        }
        val dialogDownBlob = webViewActivity.dialogsState.downBlobDialog
        if (dialogDownBlob != null && dialogDownBlob.isOpen()) {
            return true
        }
        val captureSheet = webViewActivity.captureModeSheet
        if (captureSheet != null && captureSheet.isOpen()) {
            return true
        }
        val dialogCapture = webViewActivity.captureDialog
        if (dialogCapture != null && dialogCapture.isFullOpen()) {
            return true
        }
        val dialogDownPage = webViewActivity.downPageDialog
        if (dialogDownPage != null && dialogDownPage.isOpen()) {
            return true
        }
        val dialogPrintPage = webViewActivity.printPageDialog
        if (dialogPrintPage != null && dialogPrintPage.isOpen()) {
            return true
        }
        val dialogSetTrans = webViewActivity.setTransDialog
        if (dialogSetTrans != null && dialogSetTrans.isOpen()) {
            return true
        }
        val dialogTransLang = webViewActivity.transLangDialog
        if (dialogTransLang != null && dialogTransLang.isOpen()) {
            return true
        }
        val dialogTransMsg = webViewActivity.transMsgDialog
        if (dialogTransMsg != null && dialogTransMsg.isOpen()) {
            return true
        }
        val dialogViewTrans = webViewActivity.viewTransDialog
        if (dialogViewTrans != null && dialogViewTrans.isOpen()) {
            return true
        }
        val dialogSetDesk = webViewActivity.dialogsState.setDeskDialog
        if (dialogSetDesk != null && dialogSetDesk.isOpen()) {
            return true
        }
        val dialogSetUrl = webViewActivity.tabsState.setUrlDialog
        if (dialogSetUrl != null && dialogSetUrl.isOpen()) {
            return true
        }
        val dialogOpenType = webViewActivity.openTypeDialog
        if (dialogOpenType != null && dialogOpenType.isOpen()) {
            return true
        }
        val dialogPopupMenu = webViewActivity.popupMenuDialog
        if (dialogPopupMenu != null && dialogPopupMenu.isOpen()) {
            return true
        }
        val dialogWebView = webViewActivity.webViewDialog
        if (dialogWebView != null && dialogWebView.isOpen()) {
            return true
        }
        val dialogAllowPopup = webViewActivity.allowPopupDialog
        if (dialogAllowPopup != null && dialogAllowPopup.isOpen()) {
            return true
        }
        val dialogBlockLink = webViewActivity.blockLinkDialog
        if (dialogBlockLink != null && dialogBlockLink.isOpen()) {
            return true
        }
        val dialogBlockImage = webViewActivity.dialogsState.blockImageDialog
        if (dialogBlockImage != null && dialogBlockImage.isOpen()) {
            return true
        }
        val dialogGuideArea = webViewActivity.dialogsState.guideAreaDialog
        if (dialogGuideArea != null && dialogGuideArea.isOpen()) {
            return true
        }
        val guideAreaDialog = webViewActivity.guideAreaDialog
        if (guideAreaDialog != null && guideAreaDialog.isOpen()) {
            return true
        }
        val dialogEditArea = webViewActivity.editAreaDialog
        if (dialogEditArea != null && dialogEditArea.isOpen()) {
            return true
        }
        val jsMessageDialog = webViewActivity.jsMessageDialog
        if (jsMessageDialog != null && jsMessageDialog.isOpen()) {
            return true
        }
        val dialogPrompt = webViewActivity.promptDialog
        if (dialogPrompt != null && dialogPrompt.isOpen()) {
            return true
        }
        val dialogEditAuth = webViewActivity.editAuthDialog
        if (dialogEditAuth != null && dialogEditAuth.isOpen()) {
            return true
        }
        val dialogPermission = webViewActivity.permissionDialog
        if (dialogPermission != null && dialogPermission.isOpen()) {
            return true
        }
        val dialogPassInfo = webViewActivity.passInfoDialog
        if (dialogPassInfo != null && dialogPassInfo.isOpen()) {
            return true
        }
        val dialogWebSelect = webViewActivity.webSelectDialog
        if (dialogWebSelect != null && dialogWebSelect.isOpen()) {
            return true
        }
        val dialogMenuMain = webViewActivity.menuMainDialog
        if (dialogMenuMain != null && dialogMenuMain.isOpen()) {
            return true
        }
        val overview = webViewActivity.tabsState.composeOverview
        if (overview != null && overview.isFullOpen()) {
            return true
        }
        val dialogLoadImg = webViewActivity.loadImgDialog
        if (dialogLoadImg != null && dialogLoadImg.isOpen()) {
            return true
        }
        val dialogLoadEmg = webViewActivity.loadEmgDialog
        if (dialogLoadEmg != null && dialogLoadEmg.isOpen()) {
            return true
        }
        val dialogLoadHmg = webViewActivity.loadHmgDialog
        if (dialogLoadHmg != null && dialogLoadHmg.isOpen()) {
            return true
        }
        val webGridDialog = webViewActivity.dialogsState.gridDialog
        if (webGridDialog != null && webGridDialog.isFullOpen()) {
            return true
        }
        val siteDlg = webViewActivity.dialogsState.siteDialog
        if (siteDlg != null && siteDlg.isFullOpen()) {
            return true
        }
        val dialogVideoList = webViewActivity.videoListDialog
        if (dialogVideoList != null && dialogVideoList.isOpen()) {
            return true
        }
        val dialogDownFont = webViewActivity.dialogsState.downFontDialog
        if (dialogDownFont != null && dialogDownFont.isOpen()) {
            return true
        }
        val dialogPreview = webViewActivity.previewDialog
        if (dialogPreview != null && dialogPreview.isFullOpen()) {
            return true
        }
        val dialogPreImage = webViewActivity.preImageDialog
        if (dialogPreImage != null && dialogPreImage.isOpen()) {
            return true
        }
        val dialogSeekBright = webViewActivity.seekBrightDialog
        if (dialogSeekBright != null && dialogSeekBright.isOpen()) {
            return true
        }
        val dialogSeekWebText = webViewActivity.seekWebTextDialog
        if (dialogSeekWebText != null && dialogSeekWebText.isOpen()) {
            return true
        }
        val dialogSetFull = webViewActivity.setFullDialog
        if (dialogSetFull != null && dialogSetFull.isOpen()) {
            return true
        }
        val dialogSetDark = webViewActivity.dialogsState.setDarkDialog
        if (dialogSetDark != null && dialogSetDark.isOpen()) {
            return true
        }
        val darkConfirmDialog = webViewActivity.darkConfirmDialog
        if (darkConfirmDialog != null && darkConfirmDialog.isOpen()) {
            return true
        }
        val dialogSetUseTts = webViewActivity.dialogsState.setUseTtsDialog
        if (dialogSetUseTts != null && dialogSetUseTts.isOpen()) {
            return true
        }
        val dialogSetAdblock = webViewActivity.setAdblockDialog
        if (dialogSetAdblock != null && dialogSetAdblock.isOpen()) {
            return true
        }
        val dialogSetJava = webViewActivity.setJavaDialog
        if (dialogSetJava != null && dialogSetJava.isOpen()) {
            return true
        }
        val dialogSetTmem = webViewActivity.setTmemDialog
        if (dialogSetTmem != null && dialogSetTmem.isOpen()) {
            return true
        }
        val dialogSetRead = webViewActivity.setReadDialog
        if (dialogSetRead != null && dialogSetRead.isOpen()) {
            return true
        }
        val dialogViewSrc = webViewActivity.dialogsState.viewSrcDialog
        if (dialogViewSrc != null && dialogViewSrc.isFullOpen()) {
            return true
        }
        val dialogSetPrivacy = webViewActivity.dialogsState.setPrivacyDialog
        if (dialogSetPrivacy != null && dialogSetPrivacy.isOpen()) {
            return true
        }
        val dialogSetCookie = webViewActivity.dialogsState.setCookieDialog
        if (dialogSetCookie != null && dialogSetCookie.isOpen()) {
            return true
        }
        val dialogSetScrFil = webViewActivity.dialogsState.setScrFilDialog
        if (dialogSetScrFil != null && dialogSetScrFil.isOpen()) {
            return true
        }
        val dialogWebCerti = webViewActivity.dialogsState.certDialog
        if (dialogWebCerti != null && dialogWebCerti.isOpen()) {
            return true
        }
        val restartGuideDialog = webViewActivity.dialogsState.restartGuideDialog
        if (restartGuideDialog != null && restartGuideDialog.isOpen()) {
            return true
        }
        val restartNoticeDialog = webViewActivity.dialogsState.restartNoticeDialog
        if (restartNoticeDialog != null && restartNoticeDialog.isOpen()) {
            return true
        }
        val scriptInstallDialog = webViewActivity.dialogsState.scriptInstallDialog
        if (scriptInstallDialog != null && scriptInstallDialog.isOpen()) {
            return true
        }
        val dialogUpdateScript = webViewActivity.dialogsState.updateScriptDialog
        if (dialogUpdateScript != null && dialogUpdateScript.isOpen()) {
            return true
        }
        val webVideoFrame = webViewActivity.videoFrame
        return webVideoFrame != null && webVideoFrame.consumesBack()
    }
}

class WvaShowUpdateScriptDialog(@JvmField val wa: WebViewActivity) : DialogApplyListener {
    override fun onApply() {
        val webViewActivity = wa
        val scriptInstallUrl = webViewActivity.dialogsState.scriptInstallUrl
        webViewActivity.dialogsState.scriptInstallUrl = null
        val dialogSetMsg = webViewActivity.dialogsState.scriptInstallDialog
        if (dialogSetMsg != null) {
            dialogSetMsg.dismiss()
            webViewActivity.dialogsState.scriptInstallDialog = null
        }
        val dialogUpdateScript: DialogUpdateScript? = if (webViewActivity.isClosing) null else webViewActivity.dialogsState.updateScriptDialog
        if (webViewActivity.isClosing || dialogUpdateScript != null) {
            return
        }
        val updateScriptDialog = DialogUpdateScript(
            webViewActivity,
            null,
            null,
            scriptInstallUrl,
            WvaEditUrlListener(webViewActivity)
        )
        webViewActivity.dialogsState.updateScriptDialog = updateScriptDialog
        updateScriptDialog.setOnDismissListener(WvaUpdateScriptDialogDismissed(webViewActivity))
    }
}

class ScriptInstallDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetMsg = webViewActivity.dialogsState.scriptInstallDialog
        if (dialogSetMsg != null) {
            dialogSetMsg.dismiss()
            webViewActivity.dialogsState.scriptInstallDialog = null
        }
        webViewActivity.dialogsState.scriptInstallUrl = null
    }
}

class WvaUpdateScriptDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogUpdateScript = webViewActivity.dialogsState.updateScriptDialog
        if (dialogUpdateScript != null) {
            dialogUpdateScript.dismiss()
            webViewActivity.dialogsState.updateScriptDialog = null
        }
    }
}

class WvaPauseSave(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val settingsLoaded = AppState.settingsLoaded
        val webViewActivity = wa
        if (!settingsLoaded && !webViewActivity.isClosing) {
            MainApp.loadSettingsOnce(webViewActivity.dialogsState.appContext, webViewActivity.getResources())
            webViewActivity.snapshotPrefs()
        }
        PrefSync.saveIndexes(webViewActivity.dialogsState.appContext)
        flushCookies(webViewActivity.dialogsState.appContext, false)
        if (webViewActivity.isClosing) {
            return
        }
        MainUtil.trimMemoryCaches()
    }
}

class WvaSavedPageLoad(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class LoadSavedPageStep(
                                         @JvmField val checked: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.currentPage == null) {
                return
            }
            webViewActivity.loadPageUrl(webViewActivity.dialogsState.pageUrl, false, true)
            webViewActivity.currentPage!!.openUrl(webViewActivity.dialogsState.pageUrl, null)
            if (this.checked) {
                webViewActivity.currentPage!!.setFileUrl(null, webViewActivity.dialogsState.pageUrl)
            } else {
                showToast(webViewActivity, AppR.string.fail)
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.appContext != null) {
            val path = filesSubdir(
                webViewActivity.dialogsState.appContext,
                ".page"
            ) + "/" + Md5FileNameGenerator().generate(webViewActivity.dialogsState.pageUrl) + ".mht"
            val fileReady =
                if (File(path).exists()) true else copyFileOrUri(webViewActivity.dialogsState.appContext, webViewActivity.dialogsState.pageUrl, path)
            if (fileReady) {
                webViewActivity.dialogsState.pageUrl = "file://" + path
            } else {
                webViewActivity.dialogsState.pageUrl = "about:blank"
            }
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(LoadSavedPageStep(fileReady))
        }
    }
}

class WvaStoreTabPreviewTask(@JvmField val wa: WebViewActivity, @JvmField val nestView: com.mycompany.app.web.WebNestView? = null, @JvmField val tabId: Long = 0, @JvmField val cacheKey: String? = null, @JvmField val f12fi: android.graphics.Bitmap? = null, @JvmField val canvas: android.graphics.Canvas? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webNestView = this@WvaStoreTabPreviewTask.nestView
        val tabId = this@WvaStoreTabPreviewTask.tabId
        val cacheKey = this@WvaStoreTabPreviewTask.cacheKey
        val bitmap = this@WvaStoreTabPreviewTask.f12fi
        val canvas = this@WvaStoreTabPreviewTask.canvas
        if (canvas == null || !isBitmapOk(bitmap) || webNestView == null || webNestView.isNotLive()) {
            return
        }
        webNestView.drawBackdropOnce = isDarkBars(webNestView.startPage)
        webNestView.swipeOffset = 0.0f
        webNestView.shadeAlpha = 0
        try {
            webNestView.draw(canvas)
            // a page that has not drawn yet gives a blank picture: keep the older snapshot instead
            if (TabSnapshot.isBlank(bitmap!!)) return
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaStoreTabThumb(webViewActivity, tabId = tabId, cacheKey = cacheKey, bitmap = bitmap))
            }
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
    }
}

class PathsResetDialogButtons(@JvmField val wa: WebViewActivity) : DialogConfBtnListener {
    override fun onConfirm(confirmed: Boolean) {
        val webViewActivity = wa
        val dialogConfirm = webViewActivity.dialogsState.pathsResetDialog
        if (dialogConfirm != null) {
            dialogConfirm.dismiss()
            webViewActivity.dialogsState.pathsResetDialog = null
        }
    }

    override fun onButton() {
    }

    override fun onCancel() {
        val webViewActivity = wa
        val dialogConfirm = webViewActivity.dialogsState.pathsResetDialog
        if (dialogConfirm != null) {
            dialogConfirm.dismiss()
            webViewActivity.dialogsState.pathsResetDialog = null
        }
        if (webViewActivity.currentPage == null) {
            return
        }
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.DownSettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 2)
        webViewActivity.startActivity(intent)
    }
}

class PathsResetDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogConfirm = webViewActivity.dialogsState.pathsResetDialog
        if (dialogConfirm != null) {
            dialogConfirm.dismiss()
            webViewActivity.dialogsState.pathsResetDialog = null
        }
    }
}

class WvaDialogMsgListener(@JvmField val wa: WebViewActivity) : DialogMsgListener {

    internal inner class ClearRecentStep : Runnable {

        internal inner class CloseClearRecentStep : Runnable {
            override fun run() {
                val webViewActivity = wa
                val dialogSetMsg = webViewActivity.clearRecentDialog
                if (dialogSetMsg != null) {
                    dialogSetMsg.dismiss()
                    webViewActivity.clearRecentDialog = null
                }
            }
        }

        override fun run() {
            val webViewActivity = wa
            DbBookRecent.clearSecret(webViewActivity.dialogsState.appContext, SyncSettings.secretMode.value)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(CloseClearRecentStep())
        }
    }

    override fun onConfirm() {
        val webViewActivity = wa
        val dialogSetMsg = webViewActivity.clearRecentDialog
        if (dialogSetMsg == null) {
            return
        }
        dialogSetMsg.startBusy()
        webViewActivity.runInBackground(ClearRecentStep())
    }

    override fun onHelp() {
        val webViewActivity = wa
        val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
        // recent searches live on Start page & search › Search (2026-11-26)
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.SearchSettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_PATH", checkedUrl)
        webViewActivity.startActivity(intent)
    }
}

class ClearRecentDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetMsg = webViewActivity.clearRecentDialog
        if (dialogSetMsg != null) {
            dialogSetMsg.dismiss()
            webViewActivity.clearRecentDialog = null
        }
    }
}

class WvaVideoJsonResult(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val items: ArrayList<Any?>?,
                                       @JvmField val url: String,
                                       @JvmField val result: Int
) : Runnable {
    override fun run() {
        val webViewActivity = this.webViewActivity
        val arrayList = this.items
        if (arrayList == null || TextUtils.isEmpty(this.url)) {
            showToast(webViewActivity, AppR.string.no_down_video)
            return
        }
        webViewActivity.pageState.videoJsonResultList = arrayList
        webViewActivity.dialogsState.videoJsonIndex = this.result
        val url = this.url
        webViewActivity.openDownloadDialog(url, null, "video/*", 0L, 5, url)
    }
}

class WvaSaveBookIconTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val title: String? = null, @JvmField val bitmap: android.graphics.Bitmap? = null, @JvmField val isBook: Boolean = false, @JvmField val update: Boolean = false) : Runnable {

    override fun run() {
        val saved: Boolean
        val webViewActivity = wa
        val url = this@WvaSaveBookIconTask.url
        val title = this@WvaSaveBookIconTask.title
        val bitmap = this@WvaSaveBookIconTask.bitmap
        val isBook = this@WvaSaveBookIconTask.isBook
        val update = this@WvaSaveBookIconTask.update
        // start-page shortcuts of this site without a picture take the favicon too
        try { com.mycompany.app.quick.QuickIcons.rememberVisit(webViewActivity.dialogsState.appContext, url, bitmap) } catch (_: Exception) { }
        if (isBitmapOk(bitmap)) {
            val shortHost = MainUtil.shortHostOf(url)
            val context = webViewActivity.dialogsState.appContext
            val dbBookIcon = DbBookIcon.cached
            if (context == null || TextUtils.isEmpty(shortHost) || isYouTubeHost(shortHost) || !isBitmapOk(bitmap)) {
                saved = isBook
            } else {
                val now = System.currentTimeMillis()
                val hostArgs = arrayOf<String?>(shortHost)
                val writableDatabase = DbBookIcon.get(context).getWritableDatabase()
                val hostExists = DbUtil.existsWhere(
                    writableDatabase,
                    "DbBookIcon_table",
                    arrayOf<String?>("_icon"),
                    "_host=?",
                    hostArgs
                )
                if (hostExists != 0) {
                    saved = isBook
                    val contentValuesG = android.content.ContentValues().also { cv -> cv.put("_host", shortHost) }
                    contentValuesG.put("_time", now)
                    if (hostExists == 1) {
                        updateWhere(
                            writableDatabase,
                            "DbBookIcon_table",
                            contentValuesG,
                            "_host=?",
                            hostArgs
                        )
                    } else {
                        try {
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap!!.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                byteArrayOutputStream
                            )
                            contentValuesG.put("_icon", byteArrayOutputStream.toByteArray())
                            byteArrayOutputStream.close()
                            insert(writableDatabase, "DbBookIcon_table", contentValuesG)
                        } catch (unused: Exception) {
                        }
                    }
                } else {
                    saved = isBook
                }
            }
        } else {
            saved = isBook
        }
        if (saved) {
            val context = webViewActivity.dialogsState.appContext
            val webNestView = webViewActivity.currentPage
            val displayUrl = if (webNestView == null) url else webNestView.displayUrl(url)
            val dbBookHistory = DbBookHistory.cached
            if (BrowserSettings.historyTime.value != 0 && ((!SyncSettings.secretMode.value || SecretSettings.secretHist.value != 0L) && context != null)) {
                val fileTitle = pageTitleForFile(context, displayUrl)
                if (!TextUtils.isEmpty(fileTitle)) {
                    val now = System.currentTimeMillis()
                    val titleArgs = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), fileTitle)
                    val writableDatabase = DbBookHistory.get(context).getWritableDatabase()
                    val titleExists = existsWhere(
                        writableDatabase,
                        "DbBookHistory_table",
                        null,
                        "_secret=? AND _path=?",
                        titleArgs
                    )
                    if (titleExists != 0) {
                        val contentValuesE = android.content.ContentValues().also { cv -> cv.put("_path", fileTitle); cv.put("_title", title) }
                        contentValuesE.put("_time", now)
                        if (titleExists == 1) {
                            updateWhere(
                                writableDatabase,
                                "DbBookHistory_table",
                                contentValuesE,
                                "_secret=? AND _path=?",
                                titleArgs
                            )
                        } else {
                            contentValuesE.put("_secret", com.mycompany.app.pref.store.Workspaces.dataNumber())
                            insert(writableDatabase, "DbBookHistory_table", contentValuesE)
                        }
                    }
                }
            }
        }
        if (update) {
            setIcon(webViewActivity.dialogsState.appContext, bitmap, url)
            val dialogListBook = webViewActivity.historyDialog
            if (dialogListBook != null) {
                dialogListBook.clearIcons()
            }
        }
    }
}

class WvaVideoEnterCutoutTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class VideoEnterStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.videoFrame == null) {
                webViewActivity.videoState.videoEnterBusy = false
                return
            }
            applyDisplayCutout(webViewActivity.getWindow(), webViewActivity.dialogsState.showStatusBar, VideoSettings.cutOut.value)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaVideoEnterSystemBarsTask(webViewActivity))
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.videoFrame == null) {
            webViewActivity.videoState.videoEnterBusy = false
            return
        }
        applyBarAppearance(webViewActivity.getWindow(), webViewActivity.decorViewCached(), webViewActivity.insetsControllerCompat(), true, true)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(VideoEnterStep())
    }
}

class WvaHandleUrlOverrideTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val url: String,
                                       @JvmField val linkKind: Int,
                                       @JvmField val referer: String
) : Runnable {
    override fun run() {
        val host = hostOf(this.url, true)
        val type = this.linkKind - 100
        val webViewActivity = this.webViewActivity
        this.webViewActivity.runStartAction(
            MainUtil.handleDownloadLink(
                webViewActivity,
                webViewActivity.dialogsState.appContext,
                webViewActivity.currentPage,
                webViewActivity.dialogsState.pageUrl,
                webViewActivity.dialogsState.pageHost,
                this.url,
                host,
                type
            ), this.referer
        )
    }
}

class WvaLocalChromeClientTask(@JvmField val wa: WebViewActivity, @JvmField val popupMessage: android.os.Message? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val message = this@WvaLocalChromeClientTask.popupMessage
        if (message == null) {
            webViewActivity.pageState.chromeClientBusy = false
            return
        }
        flushCookies(webViewActivity.dialogsState.appContext, true)
        if (webViewActivity.pageState.popupPage != null) {
            webViewActivity.destroyPopupPage()
        }
        webViewActivity.finishProgress()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCreatePopupPage(webViewActivity, localChromeClientTaskMessage = message))
    }
}

class WvaFileChooserTask(@JvmField val wa: WebViewActivity, @JvmField val fileChooserStartValue: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val fileChooserStartValue = this@WvaFileChooserTask.fileChooserStartValue
        if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
            webViewActivity.deliverFileChooserResult(null)
            return
        }
        val dialogWebSelect = webViewActivity.webSelectDialog
        if (dialogWebSelect != null) {
            dialogWebSelect.dismiss()
            webViewActivity.webSelectDialog = null
        }
        var theme = AppR.style.DialogExpandTheme
        if (webViewActivity.isLandscapeScreen()) {
            theme = 0
        }
        val webSelectDialog =
            DialogWebSelect(webViewActivity, theme, fileChooserStartValue, WvaWebSelectListener(webViewActivity))
        webViewActivity.webSelectDialog = webSelectDialog
        webSelectDialog.setOnDismissListener(WvaWebSelectDialogDismissed(webViewActivity))
        if (MainConst.isApi30Insets || theme != 0) {
            webViewActivity.webSelectDialog!!.applyColors(
                webViewActivity.onStartPage,
                webViewActivity.useDarkWindow(),
                webViewActivity.pageThemeColor(),
                webViewActivity.navBarColorNow(),
                true
            )
        }
    }
}

class InvalidPathToast(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        showToast(wa, AppR.string.invalid_path)
    }
}

class WvaAddressMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.addressMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class WvaOnAddressMenuItemTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class AddressMenuStep(
                                         @JvmField val addressText: String?
    ) : Runnable {
        override fun run() {
            val addressText = this.addressText
            val addressTextEmpty = TextUtils.isEmpty(addressText)
            val webViewActivity = wa
            if (addressTextEmpty) {
                val empty = AppR.string.empty
                webViewActivity.javaClass
                showToast(webViewActivity, empty)
            } else {
                webViewActivity.dialogsState.addressMenuText = addressText
                webViewActivity.setAddressEditing(true)
                webViewActivity.dialogsState.addressMenuText = null
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        val clipText = MainUtil.clipboardText(webViewActivity.dialogsState.appContext)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(AddressMenuStep(clipText))
    }
}

class WvaPasteAddressTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class OpenAddressUrlStep(
                                         @JvmField val url: String?
    ) : Runnable {
        override fun run() {
            val url = this.url
            val urlEmpty = TextUtils.isEmpty(url)
            val webViewActivity = wa
            if (!urlEmpty) {
                webViewActivity.openInCurrentPage(url, null)
            } else {
                val empty = AppR.string.empty
                webViewActivity.javaClass
                showToast(webViewActivity, empty)
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        val clipText = MainUtil.clipboardText(webViewActivity.dialogsState.appContext)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenAddressUrlStep(clipText))
    }
}

class DeletedToast(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class DeletedToastStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val deleted = AppR.string.deleted
            webViewActivity.javaClass
            showToast(webViewActivity, deleted)
        }
    }

    override fun run() {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(DeletedToastStep())
    }
}

class WvaAddressPrefixMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.addressPrefixMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class WvaPopupMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.floatingState.popupMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class WvaShowSetTmemDialog(@JvmField val wa: WebViewActivity) : DialogSeekListener {

    internal inner class NewTabAdded : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            val webViewActivity = wa
            webViewActivity.onTabAdded(isAdded)
        }
    }

    internal inner class PlainTabAdded : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            val webViewActivity = wa
            webViewActivity.onTabAdded(isAdded)
        }
    }

    internal inner class OpenedTabAdded : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            val webViewActivity = wa
            webViewActivity.onTabAdded(isAdded)
        }
    }

    override fun onValue(newValue: Int) {
        val webViewActivity = wa
        webViewActivity.closeTabMenuDialog()
        if (newValue == 8) {
            com.mycompany.app.ui.browser.WorkspaceSheet.show(webViewActivity)
            return
        }
        if (newValue == 0) {
            webViewActivity.showDeleteTabsDialog(0, 0, 0)
            return
        }
        if (newValue == 1) {
            webViewActivity.showDeleteTabsDialog(0, webViewActivity.currentTabIndex, 1)
            return
        }
        if (newValue == 2) {
            webViewActivity.previewPrevTab()
            return
        }
        if (newValue == 3) {
            webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), true, NewTabAdded())
            return
        }
        if (newValue == 4) {
            webViewActivity.addTabPlain(
                null,
                webViewActivity.currentPageUrl(false),
                true,
                PlainTabAdded()
            )
            return
        }
        if (newValue == 5) {
            webViewActivity.openInNewTab(WebViewActivity.newTabUrl(), OpenedTabAdded())
            return
        }
        if (newValue == 6) {
            webViewActivity.createBackTab(WebViewActivity.newTabUrl())
            return
        }
        if (newValue != 7 || webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
            return
        }
        val dialogSetTmem = webViewActivity.setTmemDialog
        if (dialogSetTmem != null) {
            dialogSetTmem.dismiss()
            webViewActivity.setTmemDialog = null
        }
        val setTmemDialog = DialogSetTmem(webViewActivity, webViewActivity.currentPageUrl(false))
        webViewActivity.setTmemDialog = setTmemDialog
        setTmemDialog.setOnDismissListener(WvaSetTmemDialogDismissed(webViewActivity))
    }
}

class WvaTabBarItemMenuListener(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val index: Int
) : MyPopupListener {
    override fun onClose() {
        this.webViewActivity.closeTabBarItemMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        val webViewActivity = this.webViewActivity
        webViewActivity.closeMenuDialogs()
        var index = this.index
        if (id != 0) {
            webViewActivity.showTabGroupDeleteDialog(index)
            return true
        }
        if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
            val dialogTabEdit = webViewActivity.dialogsState.tabEditDialog
            if (dialogTabEdit != null) {
                dialogTabEdit.dismiss()
                webViewActivity.dialogsState.tabEditDialog = null
            }
            val webTabItemD = webViewActivity.tabItemAt(index)
            if (webTabItemD != null) {
                val groupId = webTabItemD.groupId
                if (groupId != 0L) {
                    val arrayList: ArrayList<WebTabItem?> = ArrayList()
                    val size = webViewActivity.tabsState.tabItems!!.size
                    while (index < size) {
                        val webTabItem = webViewActivity.tabsState.tabItems!!.get(index)
                        if (webTabItem != null) {
                            if (webTabItem.groupId != groupId) {
                                break
                            }
                            arrayList.add(webTabItem)
                        }
                        index++
                    }
                    if (!arrayList.isEmpty()) {
                        webViewActivity.closeSnackbar(false)
                        val tabEditDialog = DialogTabEdit(
                            webViewActivity,
                            webViewActivity.tabsState.tabItems,
                            arrayList,
                            webTabItemD.groupName,
                            webTabItemD.groupColor,
                            WvaTabEditApplied(webViewActivity)
                        )
                        webViewActivity.dialogsState.tabEditDialog = tabEditDialog
                        tabEditDialog.setOnDismissListener(WvaTabEditDialogDismissed(webViewActivity))
                    }
                }
            }
        }
        return true
    }
}

class WvaTabBarItemMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.tabBarItemMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class WvaQuickMenuListener(@JvmField val wa: WebViewActivity) : MyPopupListener {
    override fun onClose() {
        wa.closeQuickSettingsMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        val webViewActivity = wa
        if (id == 0) {
            webViewActivity.launchExternalForResult(
                Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.QuickSettingsActivity::class.java as Class<*>),
                RequestCode.QUICK_SETTINGS
            )
            return true
        }
        if (id == 1) {
            webViewActivity.startActivity(
                Intent(
                    webViewActivity.dialogsState.appContext,
                    com.mycompany.app.ui.settings.NewsSettingsActivity::class.java as Class<*>
                )
            )
            return true
        }
        if (id != 2) {
            if (id != 3) {
                if (id == 4) {
                    val quickDown = !ZtwoSettings.quickDown.value
                    ZtwoSettings.edit(webViewActivity.dialogsState.appContext) { set(ZtwoSettings.quickDown, quickDown) }
                    val webNestView = webViewActivity.currentPage
                    val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
                    if (webNestView != null && quickView != null) {
                        quickView.applyLayout()
                        return true
                    }
                }
            } else if (webViewActivity.currentPage != null) {
                webViewActivity.pageState.addressEditBusy = false
                webViewActivity.setAddressEditing(false)
                webViewActivity.launchExternalForResult(
                    Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.lists.QuickAddActivity::class.java as Class<*>),
                    RequestCode.QUICK_ADD
                )
                return true
            }
            return true
        }
        if (webViewActivity.dialogsState.quickSubMenu == null) {
            webViewActivity.closeSubMenu()
            if (view != null) {
                webViewActivity.cameraUri = null
                val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                arrayList.add(PopMenuItem(0, AppR.string.do_change))
                arrayList.add(PopMenuItem(1, AppR.string.not_used))
                val myPopupMenu = MyPopupMenu(
                    webViewActivity,
                    webViewActivity.dialogsState.webBody,
                    view,
                    arrayList,
                    MainUtil.isDarkPage(webViewActivity.onStartPage),
                    WvaQuickSubMenuListener(webViewActivity)
                )
                webViewActivity.dialogsState.quickSubMenu = myPopupMenu
                myPopupMenu.menuKind = 3.toByte()
                webViewActivity.popupMenu = myPopupMenu
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.post(WvaQuickMenuHoldListener(webViewActivity))
                    return false
                }
            }
        }
        return false
    }
}

class WvaQuickSettingsMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.quickSettingsMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class WvaShowNewsSearchDialog(@JvmField val wa: WebViewActivity) : DownMenuListener {
    override fun onClose() {
        val webViewActivity = wa
        webViewActivity.newsMenuOpen = false
        webViewActivity.closeNewsMenuDialog()
    }

    override fun onItem(view: View?, id: Int) {
        val webViewActivity = wa
        if (webViewActivity.floatingState.rssButton == null || id < 0) {
            return
        }
        val topic = id % 10
        webViewActivity.closeNewsMenuDialog()
        if (topic != 9) {
            if (ZtwoSettings.newsTopic.value != topic) {
                ZtwoSettings.edit(webViewActivity.dialogsState.appContext) { set(ZtwoSettings.newsTopic, topic) }
            }
            webViewActivity.stopNews()
            return
        }
        if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
            return
        }
        val dialogNewsSearch = webViewActivity.newsSearchDialog
        if (dialogNewsSearch != null) {
            dialogNewsSearch.dismiss()
            webViewActivity.newsSearchDialog = null
        }
        webViewActivity.updateRssButton(false)
        val newsSearchDialog = DialogNewsSearch(
            webViewActivity,
            webViewActivity.webCoord,
            WvaNewsSearchEntered(webViewActivity)
        )
        webViewActivity.newsSearchDialog = newsSearchDialog
        newsSearchDialog.setOnDismissListener(WvaNewsSearchDialogDismissed(webViewActivity))
    }

    override fun onDownloads() {
    }

    override fun onHistory() {
    }

    override fun onLongPress() {
    }

    override fun onSettings() {
        val webViewActivity = wa
        if (webViewActivity.floatingState.rssButton != null) {
            webViewActivity.closeNewsMenuDialog()
            if (webViewActivity.isAnyDialogOpen()) {
                return
            }
            val dialogNewsLocale = webViewActivity.newsLocaleDialog
            if (dialogNewsLocale != null) {
                dialogNewsLocale.dismiss()
                webViewActivity.newsLocaleDialog = null
            }
            val newsLocaleDialog =
                DialogNewsLocale(webViewActivity, null, WvaNewsLocalePicked(webViewActivity))
            webViewActivity.newsLocaleDialog = newsLocaleDialog
            newsLocaleDialog.setOnDismissListener(WvaNewsLocaleDialogDismissed(webViewActivity))
        }
    }

    override fun onEdit() {
    }

    override fun onExtra() {
    }
}

class WvaNewsLocaleDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogNewsLocale = webViewActivity.newsLocaleDialog
        if (dialogNewsLocale != null) {
            dialogNewsLocale.dismiss()
            webViewActivity.newsLocaleDialog = null
        }
    }
}

class WvaNewsSearchDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogNewsSearch = webViewActivity.newsSearchDialog
        if (dialogNewsSearch != null) {
            dialogNewsSearch.dismiss()
            webViewActivity.newsSearchDialog = null
        }
        webViewActivity.updateRssButton(true)
    }
}

class WvaQuickMenuHoldListener(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.quickSubMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class WvaBookEditListener(@JvmField val wa: WebViewActivity) : BookEditListener {
    override fun onSaved(id: Long, folder: String?, url: String?) {
        val added = AppR.string.added
        val webViewActivity = wa
        showToast(webViewActivity, added)
        webViewActivity.tintBars(true)
        notifyBookUpdate(webViewActivity.dialogsState.appContext, false)
    }

    override val icon: Bitmap?
        get() {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return null
        }
        return webNestView.getFavicon()
    }
}

class WvaBookEditDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogWebBookEdit = webViewActivity.dialogsState.bookEditDialog
        if (dialogWebBookEdit != null) {
            dialogWebBookEdit.dismiss()
            webViewActivity.dialogsState.bookEditDialog = null
        }
    }
}

class WvaMainMenuItemTask(@JvmField val wa: WebViewActivity, @JvmField val menuItemText: String? = null) : Runnable {

    internal inner class UntintBarsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (sameObject(webViewActivity.currentPageUrl(false), this@WvaMainMenuItemTask.menuItemText)) {
                webViewActivity.tintBars(false)
            }
            webViewActivity.dialogsState.bookmarkEditBusy = false
            notifyBookUpdate(webViewActivity.dialogsState.appContext, false)
        }
    }

    override fun run() {
        val webViewActivity = wa
        val bookId = idByPath(webViewActivity.dialogsState.appContext, this@WvaMainMenuItemTask.menuItemText)
        if (bookId > 0) {
            val context = webViewActivity.dialogsState.appContext
            pageUrlOrNull(this@WvaMainMenuItemTask.menuItemText)
            DbBookWeb.deleteById(context, bookId)
        }
        val myWebBody: MyWebBody? = if (webViewActivity.currentPage == null) null else webViewActivity.dialogsState.webBody
        if (webViewActivity.currentPage == null || myWebBody == null) {
            return
        }
        myWebBody.post(UntintBarsStep())
    }
}

class WvaEditShortDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogEditShort = webViewActivity.editShortDialog
        if (dialogEditShort != null) {
            dialogEditShort.dismiss()
            webViewActivity.editShortDialog = null
        }
    }
}

class OpenImagePreviewTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val previewImageUrl = webViewActivity.dialogsState.previewImageUrl
        var previewReferer = webViewActivity.dialogsState.previewReferer
        val previewFromLink = webViewActivity.dialogsState.previewFromLink
        webViewActivity.dialogsState.previewImageUrl = null
        webViewActivity.dialogsState.previewReferer = null
        if (TextUtils.isEmpty(previewReferer)) {
            previewReferer = webViewActivity.dialogsState.pageUrl
        }
        try {
            val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.viewer.ImagePreviewActivity::class.java as Class<*>)
            intent.putExtra("EXTRA_PATH", previewImageUrl)
            intent.putExtra("EXTRA_REFERER", previewReferer)
            intent.putExtra("EXTRA_POPUP", previewFromLink)
            webViewActivity.startActivity(intent)
        } catch (unused: Exception) {
            showToast(webViewActivity, AppR.string.not_supported)
        }
        webViewActivity.miscState.requestImagePreviewBusy = false
    }
}

class WvaPlayVideoInPlayerTask(@JvmField val wa: WebViewActivity, @JvmField val external: Boolean = false, @JvmField val url: String? = null, @JvmField val referer: String? = null) : Runnable {

    override fun run() {
        val videoPos: Int
        val webNestView: WebNestView?
        var playPageUrl: String?
        val webViewActivity = wa
        val url = this@WvaPlayVideoInPlayerTask.url
        val referer = this@WvaPlayVideoInPlayerTask.referer
        val pip = this@WvaPlayVideoInPlayerTask.external
        if (webViewActivity.currentPage != null) {
            var opened = true
            if (pip) {
                val mainAppO = MainApp.from(webViewActivity.dialogsState.appContext)
                val pendingUrl = if (mainAppO != null) mainAppO.pendingVideoUrl else null
                if (TextUtils.isEmpty(pendingUrl) || pendingUrl != url) {
                    videoPos = webViewActivity.currentPage!!.videoIndex(referer)
                    if (MainUtil.isGoogleUserContent(referer)) {
                        playPageUrl = referer
                    } else {
                        webNestView = webViewActivity.currentPage
                        if (webNestView == null) {
                            playPageUrl = webViewActivity.dialogsState.pageUrl
                        } else {
                            playPageUrl = webNestView.videoPageUrl(videoPos)
                            if (TextUtils.isEmpty(playPageUrl)) {
                                playPageUrl = webViewActivity.dialogsState.pageUrl
                            }
                        }
                    }
                    val posterUrl = webViewActivity.currentPage!!.videoPoster(videoPos)
                    val intent =
                        Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.viewer.VideoPlayerActivity::class.java as Class<*>)
                    intent.setData(Uri.parse(url))
                    intent.putExtra("EXTRA_ORG", referer)
                    intent.putExtra("EXTRA_PIP", pip)
                    intent.putExtra("EXTRA_POPUP", true)
                    intent.putExtra("EXTRA_URL", playPageUrl)
                    intent.putExtra("EXTRA_HOST", webViewActivity.dialogsState.pageHost)
                    intent.putExtra("EXTRA_THUMB", posterUrl)
                    intent.putExtra("EXTRA_INDEX", 0)
                    webViewActivity.startActivity(intent)
                }
            } else if (TextUtils.isEmpty(AlbumSettings.playPkg.value) || TextUtils.isEmpty(AlbumSettings.playCls.value)) {
                videoPos = webViewActivity.currentPage!!.videoIndex(referer)
                if (MainUtil.isGoogleUserContent(referer)) {
                    playPageUrl = referer
                } else {
                    webNestView = webViewActivity.currentPage
                    if (webNestView == null) {
                        playPageUrl = webViewActivity.dialogsState.pageUrl
                    } else {
                        playPageUrl = webNestView.videoPageUrl(videoPos)
                        if (TextUtils.isEmpty(playPageUrl)) {
                            playPageUrl = webViewActivity.dialogsState.pageUrl
                        }
                    }
                }
                val posterUrl = webViewActivity.currentPage!!.videoPoster(videoPos)
                val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.viewer.VideoPlayerActivity::class.java as Class<*>)
                intent.setData(Uri.parse(url))
                intent.putExtra("EXTRA_ORG", referer)
                intent.putExtra("EXTRA_PIP", pip)
                intent.putExtra("EXTRA_POPUP", true)
                intent.putExtra("EXTRA_URL", playPageUrl)
                intent.putExtra("EXTRA_HOST", webViewActivity.dialogsState.pageHost)
                intent.putExtra("EXTRA_THUMB", posterUrl)
                intent.putExtra("EXTRA_INDEX", 0)
                webViewActivity.startActivity(intent)
            } else {
                val playPkg: String = AlbumSettings.playPkg.value!!
                val playCls: String = AlbumSettings.playCls.value!!
                if (!openVideoWithApp(webViewActivity, playPkg, playCls, url, 1) && !openVideoWithApp(
                        webViewActivity,
                        playPkg,
                        playCls,
                        url,
                        2
                    ) && !openVideoWithApp(webViewActivity, playPkg, playCls, url, 3)
                ) {
                    opened = openVideoWithApp(webViewActivity, playPkg, playCls, url, 0)
                }
                webViewActivity.dialogsState.leavingByIntent = opened
            }
        }
        webViewActivity.barsState.playVideoInPlayerBusy = false
    }
}

class WvaVoiceSearch(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        try {
            val intent = Intent("android.speech.action.RECOGNIZE_SPEECH")
            intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "web_search")
            val localeTag = languageTag(AppState.appLocale)
            if (!TextUtils.isEmpty(localeTag)) {
                intent.putExtra("android.speech.extra.LANGUAGE", localeTag)
            }
            webViewActivity.launchExternalForResult(intent, RequestCode.VOICE_SEARCH)
            webViewActivity.pageState.addressEditBusy = false
            webViewActivity.setAddressEditing(false)
        } catch (unused: ActivityNotFoundException) {
            showToast(webViewActivity, AppR.string.apps_none)
        } catch (unused2: Exception) {
            showToast(webViewActivity, AppR.string.not_supported)
        }
        webViewActivity.dialogsState.menuItemBusy = false
    }
}

class WvaShowDownBlobDialog(@JvmField val wa: WebViewActivity) : DownUrlListener {
    override fun currentWeb(): WebNestView? {
        return wa.currentPage
    }

    override fun openApp(url: String?, name: String?) {
        val webViewActivity = wa
        webViewActivity.closeDownUrlDialog()
        webViewActivity.openIntentUrl(url, name)
    }

    override fun openWith(url: String?, name: String?, dirName: String?) {
        val webViewActivity = wa
        webViewActivity.closeDownUrlDialog()
        webViewActivity.requestSetDownDialog(url, name, dirName)
    }

    override fun onDownload(
        url: String?,
        uriItem: UriItem?,
        type: Int,
        isAudio: Boolean,
        name: String?,
        dirName: String?
    ) {
        var isAudio = isAudio
        var dirName = dirName
        val webViewActivity = wa
        val phpDownload = webViewActivity.dialogsState.phpDownload
        webViewActivity.dialogsState.phpDownload = false
        if (uriItem == null) {
            webViewActivity.closeDownUrlDialog()
            return
        }
        if (type == 1) {
            if (phpDownload) {
                webViewActivity.loadImageForOpen(url, uriItem.uriString, true)
                return
            } else if (!URLUtil.isNetworkUrl(url)) {
                webViewActivity.loadImageForOpen(url, uriItem.uriString, false)
                return
            }
        } else if (type == 3) {
            webViewActivity.closeDownUrlDialog()
            if (webViewActivity.isClosing || webViewActivity.dialogsState.downBlobDialog != null) {
                return
            }
            webViewActivity.closeDownBlobDialog()
            val downItem = DownItem()
            downItem.url = url
            downItem.referer = webViewActivity.dialogsState.pageUrl
            downItem.uriItem = uriItem
            downItem.path = uriItem.uriString
            if (webViewActivity.videoFrame != null) {
                webViewActivity.videoState.videoDialogOpen = true
                AppState.darkUi = true
            }
            val dialogDownBlob = DialogDownBlob(
                webViewActivity,
                webViewActivity.currentPage,
                downItem,
                WvaDialogBlobListener(webViewActivity)
            )
            webViewActivity.dialogsState.downBlobDialog = dialogDownBlob
            dialogDownBlob.setOnDismissListener(DownBlobDialogDismissed(webViewActivity))
            if (webViewActivity.videoFrame != null) {
                webViewActivity.dialogsState.downBlobDialog!!.noDim = true
                return
            }
            return
        }
        webViewActivity.closeDownUrlDialog()
        val mainAppO = MainApp.from(webViewActivity.getApplicationContext())
        if (mainAppO == null) {
            showToast(webViewActivity, AppR.string.down_fail)
            return
        }
        if (isGoogleUserContent(url)) {
            dirName = url
        } else if (sameObject(url, dirName)) {
            dirName = webViewActivity.dialogsState.blobPageUrl
        }
        if (type == 1) {
            isAudio = false
        }
        mainAppO.startDownload(url, dirName, uriItem, isAudio)
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            if (type == 1) {
                webNestView.addDownloadedVideo(url, false)
                return
            }
            if (!TextUtils.isEmpty(webViewActivity.dialogsState.downloadPageUrl)) {
                webViewActivity.currentPage!!.addDownloadedVideo(webViewActivity.dialogsState.downloadPageUrl, true)
            }
            if (TextUtils.isEmpty(name)) {
                return
            }
            webViewActivity.currentPage!!.addDownloadedVideo(name, true)
        }
    }

    override fun openInApp(appCode: Int, url: String?) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null || TextUtils.isEmpty(url)) {
            return
        }
        webViewActivity.closeDownUrlDialog()
        if (webViewActivity.dialogsState.downBlobBusy) {
            return
        }
        webViewActivity.dialogsState.downBlobBusy = true
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaOpenMediaTask(webViewActivity, showDownBlobDialogValue = appCode, showDownBlobDialogText = url))
    }

    override fun preview(url: String?, name: String?, dirName: String?, isImage: Boolean) {
        val webViewActivity = wa
        if (isImage) {
            val opened = quickViewPdf(webViewActivity, url)
            webViewActivity.dialogsState.leavingByIntent = opened
            if (opened) {
                webViewActivity.closeDownUrlDialog()
                return
            }
            return
        }
        if (TextUtils.isEmpty(dirName) || !dirName!!.startsWith("text")) {
            webViewActivity.showPreviewDialog(url, dirName, false)
        } else {
            webViewActivity.openTextViewer(url, name)
        }
    }

    override fun share(url: String?, name: String?, dirName: String?) {
        val webViewActivity = wa
        webViewActivity.closeDownUrlDialog()
        webViewActivity.dialogsState.leavingByIntent = MainUtil.openWithChooser(
            webViewActivity,
            AlbumSettings.dappPkg.value,
            AlbumSettings.dappCls.value,
            url,
            webViewActivity.dialogsState.pageUrl,
            name,
            dirName
        )
    }
}

class WvaOpenLoadedImage(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class OpenSavedImageStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val openImageSaved = webViewActivity.dialogsState.openImageSaved
            val openLoadedImageText = webViewActivity.dialogsState.openLoadedImageText
            val openImageDownId = webViewActivity.dialogsState.openImageDownId
            webViewActivity.dialogsState.openLoadedImageText = null
            webViewActivity.closeDownUrlDialog()
            if (!openImageSaved) {
                webViewActivity.requestOpenTypeDialog(0L, 4, null, null, 1)
            } else if (ZoneSettings.notiDown.value) {
                webViewActivity.requestOpenTypeDialog(openImageDownId, 3, openLoadedImageText, "image/*", 0)
            } else {
                showToast(webViewActivity, AppR.string.down_complete)
            }
        }
    }

    override fun run() {
        val saved: Boolean
        val webViewActivity = wa
        val openImagePath = webViewActivity.dialogsState.openImagePath
        val file = webViewActivity.dialogsState.openImageFile
        val bitmap = webViewActivity.dialogsState.openImageBitmap
        webViewActivity.dialogsState.openImagePath = null
        webViewActivity.dialogsState.openImageFile = null
        webViewActivity.dialogsState.openImageBitmap = null
        var downId: Long = 0
        if (isBitmapOk(bitmap)) {
            saved = saveBitmapPng(webViewActivity.dialogsState.appContext, bitmap, openImagePath)
        } else {
            saved = if (file == null || file.length() <= 0) false else copyFileOrUri(
                webViewActivity.dialogsState.appContext,
                file.getPath(),
                openImagePath
            )
        }
        val uriItemJ: UriItem? = if (saved) createIn(webViewActivity.dialogsState.appContext, openImagePath, MainUri.storageVolume()) else null
        if (saved && uriItemJ != null) {
            downId = DbBookDown.addFile(webViewActivity.dialogsState.appContext, openImagePath, webViewActivity.dialogsState.pageUrl, uriItemJ)
        }
        webViewActivity.dialogsState.openImageSaved = saved
        webViewActivity.dialogsState.openLoadedImageText = openImagePath
        webViewActivity.dialogsState.openImageDownId = downId
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenSavedImageStep())
    }
}

class WvaDialogBlobListener(@JvmField val wa: WebViewActivity) : DialogBlobListener {
    override fun onSaved(id: Long, path: String?, mimeType: String?) {
        val notiDown = ZoneSettings.notiDown.value
        val webViewActivity = wa
        if (!notiDown) {
            showToast(webViewActivity, AppR.string.down_complete)
        } else {
            webViewActivity.requestOpenTypeDialog(id, 3, path, mimeType, 0)
        }
    }
}

class WvaOpenIntentUrlTask(@JvmField val wa: WebViewActivity, @JvmField val path: String? = null, @JvmField val name: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val path = this@WvaOpenIntentUrlTask.path
        val name = this@WvaOpenIntentUrlTask.name
        webViewActivity.dialogsState.leavingByIntent = shareText(webViewActivity, path, name)
        webViewActivity.miscState.openIntentUrlBusy = false
    }
}

/** The screenshot-mode sheet's choice (old `SettingListAdapter.SettingListener`; [a] gets the row index). */
class WvaSettingListener(@JvmField val wa: WebViewActivity) {

    internal inner class ApplySettingStep : Runnable {

        internal inner class ShowCoverStep : Runnable {

            internal inner class CapturePageStep : Runnable {

                internal inner class CaptureDoneListener : MyCaptureListener {
                    override fun onCapture() {
                        val webViewActivity = wa
                        webViewActivity.finishCoverView()
                    }
                }

                override fun run() {
                    val applySettingStep = this@ApplySettingStep
                    val webViewActivity = wa
                    val webNestView = webViewActivity.currentPage
                    if (webNestView == null) {
                        return
                    }
                    webViewActivity.coverScrollY = webNestView.getScrollY()
                    val activity = wa
                    activity.webCoord!!.startCapture(activity.currentPage, activity.coverTime, CaptureDoneListener())
                }
            }

            override fun run() {
                var memoryHeight: Int = 0
                val wvaSettingListener = this@WvaSettingListener
                val webViewActivity = wa
                val coverTime = webViewActivity.coverTime
                if (webViewActivity.coverView == null) {
                    val myWebBody = webViewActivity.dialogsState.webBody
                    var bitmap: Bitmap? = null
                    if (myWebBody != null) {
                        try {
                            val width = myWebBody.getWidth()
                            var height = myWebBody.getHeight()
                            if (width != 0 && height != 0) {
                                val memoryHeight = if (coverTime > 0) (coverTime / ((width.toLong()) shl 5)).toInt() else 0
                                if (coverTime > 0 && height > memoryHeight) {
                                    height = memoryHeight
                                }
                                val thumbWidth = Math.round(width * 0.2f)
                                val thumbHeight = Math.round(height * 0.2f)
                                if (thumbWidth != 0 && thumbHeight != 0) {
                                    val bitmapCreateBitmap = Bitmap.createBitmap(
                                        thumbWidth,
                                        thumbHeight,
                                        Bitmap.Config.RGB_565
                                    )
                                    val canvas = Canvas(bitmapCreateBitmap)
                                    canvas.scale(0.2f, 0.2f)
                                    myWebBody.draw(canvas)
                                    canvas.drawColor(-1593835520)
                                    bitmap = bitmapCreateBitmap
                                }
                            }
                        } catch (unused: Exception) {
                        } catch (unused: OutOfMemoryError) {
                        }
                    }
                    webViewActivity.coverView =
                        LoadCover(webViewActivity, -328966, MainApp.dp3, MainApp.dp24)
                    val bitmapOk = isBitmapOk(bitmap)
                    val myCoverView = webViewActivity.coverView
                    if (bitmapOk) {
                        myCoverView!!.setBackground(
                            BitmapDrawable(
                                webViewActivity.getResources(),
                                bitmap
                            )
                        )
                    } else {
                        myCoverView!!.setBackgroundColor(-1593835520)
                    }
                    webViewActivity.coverView!!.setOnClickListener(WvaNoOpClick())
                    wa.floatingState.overlays!!.addView(webViewActivity.coverView, -1, -1)
                }
                val handler = wa.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(CapturePageStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.coverTime = MainUtil.freeMemory(webViewActivity.dialogsState.appContext)
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ShowCoverStep())
        }
    }

    fun onSetting(viewHolder: Any?, index: Int, checked: Boolean, intValue: Int) {
        val webViewActivity = wa
        if (index == 0) {
            webViewActivity.closeCaptureModeSheet()
            webViewActivity.runInBackground(WvaShowCaptureDialog(webViewActivity))
            return
        }
        if (webViewActivity.onStartPage) {
            showToast(webViewActivity, AppR.string.not_supported_page)
        } else {
            if (webViewActivity.mainHandler == null) {
                return
            }
            webViewActivity.closeCaptureModeSheet()
            webViewActivity.runInBackground(ApplySettingStep())
        }
    }
}

class WvaShowCaptureDialog(@JvmField val wa: WebViewActivity, @JvmField val coverViewCheckBusy: Boolean = false) : Runnable {

    internal inner class ShowCaptureStep : Runnable {
        override fun run() {
            var memoryHeight: Int = 0
            var maxHeight: Int = 0
            val webViewActivity = wa
            val coverViewCheckBusy = this@WvaShowCaptureDialog.coverViewCheckBusy
            val captureFreeMemory = webViewActivity.dialogsState.captureFreeMemory
            if (webViewActivity.currentPage == null || webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
                return
            }
            webViewActivity.resumePausedPage()
            val dialogCapture = webViewActivity.captureDialog
            var bitmap: Bitmap? = null
            if (dialogCapture != null) {
                dialogCapture.dismiss()
                webViewActivity.captureDialog = null
            }
            if (!coverViewCheckBusy || webViewActivity.onStartPage) {
                val webNestFrame = webViewActivity.tabsState.currentFrame
                if (webNestFrame != null) {
                    try {
                        val width = webNestFrame.getWidth()
                        var height = webNestFrame.getHeight()
                        if (width != 0 && height != 0) {
                            val memoryHeight = if (captureFreeMemory > 0) (captureFreeMemory / ((width.toLong()) shl 5)).toInt() else 0
                            if (captureFreeMemory > 0 && height > memoryHeight) {
                                height = memoryHeight
                            }
                            val float = -MainUtil.topBarHeight().toFloat()
                            val frameWidth = Math.round(width.toFloat())
                            val frameHeight = Math.round(height + float)
                            val parent: ViewParent? = if (frameWidth != 0 && frameHeight > 0) webNestFrame.getParent() else null
                            if (frameWidth != 0 && frameHeight > 0 && parent != null && (parent is ViewGroup)) {
                                webNestFrame.resetOffsets()
                                val viewGroup = parent
                                val bitmapCreateBitmap = Bitmap.createBitmap(
                                    frameWidth,
                                    frameHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmapCreateBitmap)
                                canvas.drawColor(if (AppState.darkWeb) -16777216 else -1)
                                if ((float).compareTo(0.0f) != 0) {
                                    canvas.translate(0.0f, float)
                                }
                                viewGroup.draw(canvas)
                                bitmap = bitmapCreateBitmap
                            }
                        }
                    } catch (unused: Exception) {
                    } catch (unused: OutOfMemoryError) {
                    }
                }
            } else {
                if (webViewActivity.coverView == null) {
                    webViewActivity.removeCoverView()
                    return
                }
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    try {
                        val width = webNestView.getWidth()
                        var height = webNestView.getHeight()
                        if (width != 0 && height != 0) {
                            val maxHeight = if (captureFreeMemory > 0) (captureFreeMemory / ((width.toLong()) shl 5)).toInt() else 0
                            if (captureFreeMemory > 0 && height > maxHeight) {
                                height = maxHeight
                            }
                            val halfWidth = Math.round(width * 0.5f)
                            val halfHeight = Math.round(height * 0.5f)
                            if (halfWidth != 0 && halfHeight != 0) {
                                webNestView.invalidate()
                                val bitmapCreateBitmap = Bitmap.createBitmap(
                                    halfWidth,
                                    halfHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmapCreateBitmap)
                                canvas.drawColor(if (AppState.darkWeb) -16777216 else -1)
                                canvas.scale(0.5f, 0.5f)
                                webNestView.draw(canvas)
                                bitmap = bitmapCreateBitmap
                            }
                        }
                    } catch (unused2: Exception) {
                    } catch (unused2: OutOfMemoryError) {
                    }
                }
                webViewActivity.removeCoverView()
            }
            if (!isBitmapOk(bitmap)) {
                showToast(webViewActivity, AppR.string.image_fail)
                return
            }
            webViewActivity.pausePage()
            val captureDialog = DialogCapture(
                webViewActivity,
                bitmap,
                coverViewCheckBusy,
                webViewActivity.pageTitleOf(webViewActivity.currentPage, webViewActivity.dialogsState.pageUrl)
            )
            webViewActivity.captureDialog = captureDialog
            captureDialog.setOnDismissListener(WvaCaptureDialogDismissed(webViewActivity))
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.dialogsState.captureFreeMemory = MainUtil.freeMemory(webViewActivity.dialogsState.appContext)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ShowCaptureStep())
    }
}

class WvaCaptureDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.resumePausedPage()
        val dialogCapture = webViewActivity.captureDialog
        if (dialogCapture != null) {
            dialogCapture.dismiss()
            webViewActivity.captureDialog = null
        }
    }
}

class WvaDownPageResult(@JvmField val wa: WebViewActivity, @JvmField val pageUrl: String? = null, @JvmField val path: String? = null, @JvmField val icon: android.graphics.Bitmap? = null) : ValueCallback<String> {

    internal inner class SavedSnackbarListener : SnackbarListener {
        override fun onSecond() {
        }

        override fun onDismissed() {
        }

        override fun onFirst() {
        }

        override fun onDismiss() {
            wa.snackbar = null
        }
    }

    internal inner class SaveResultStep : Runnable {

        internal inner class ShowSaveResultStep : Runnable {

            internal inner class FailSnackbarListener : SnackbarListener {
                override fun onSecond() {
                }

                override fun onDismissed() {
                }

                override fun onFirst() {
                }

                override fun onDismiss() {
                    wa.snackbar = null
                }
            }

            internal inner class OpenSavedSnackbarListener : SnackbarListener {
                override fun onSecond() {
                }

                override fun onDismissed() {
                }

                override fun onFirst() {
                    val saveResultStep = this@SaveResultStep
                    val webViewActivity = wa
                    val uriItem = webViewActivity.dialogsState.uriItem
                    webViewActivity.dialogsState.uriItem = null
                    val context: Context? = if (uriItem == null) null else webViewActivity.dialogsState.appContext
                    if (uriItem == null || context == null) {
                        return
                    }
                    val intent = Intent(context, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
                    intent.putExtra("EXTRA_PATH", uriItem.uriString)
                    wa.launchExternalForResult(intent, RequestCode.DOWNLOAD_LIST)
                }

                override fun onDismiss() {
                    wa.snackbar = null
                }
            }

            override fun run() {
                val wvaDownPageResult = this@WvaDownPageResult
                val webViewActivity = wa
                if (webViewActivity.currentPage == null) {
                    return
                }
                webViewActivity.updateUpButton(false)
                wa.closeAllSnackbars()
                wa.snackbar = MySnackbar(wa)
                val activity = wa
                activity.snackbar!!.setAppBarView(activity.barScroll)
                val act = wa
                val uriItem = act.dialogsState.uriItem
                val mySnackbar = act.snackbar
                if (uriItem == null) {
                    mySnackbar!!.showSingle(act.webCoord, AppR.string.save_fail, FailSnackbarListener())
                } else {
                    mySnackbar!!.showWithButton(act.webCoord, AppR.string.save_success, 2, OpenSavedSnackbarListener())
                }
            }
        }

        /**
         * Moves the archived page into the download folder, records it in DbBookPage (with the
         * site icon) and shows the result.
         */
        override fun run() {
            val activity = wa
            val pageUrl = this@WvaDownPageResult.pageUrl
            val name = this@WvaDownPageResult.path
            val icon = this@WvaDownPageResult.icon
            val temp = activity.dialogsState.downPageResultText
            activity.dialogsState.downPageResultText = null
            if (TextUtils.isEmpty(temp)) {
                return
            }
            var saved: UriItem? = null
            val size = File(temp!!).length()
            if (size > 0) {
                val target = MainUri.createUri(activity.dialogsState.appContext, MainUri.storageVolume(), null, name)
                if (target == null) {
                    saved = null
                } else if (MainUri.moveUri(activity.dialogsState.appContext, temp, target.uriString, false, true)) {
                    target.size = size
                    recordPage(activity.dialogsState.appContext, target, pageUrl, icon)
                    saved = target
                }
            }
            activity.dialogsState.uriItem = saved
            if (activity.mainHandler != null) {
                activity.mainHandler!!.post(ShowSaveResultStep())
            }
        }

        private fun recordPage(
            context: Context?,
            item: UriItem?,
            pageUrl: String?,
            icon: Bitmap?
        ) {
            var icon = icon
            if (context == null || TextUtils.isEmpty(item!!.uriString)) {
                return
            }
            if (!isBitmapOk(icon) && !TextUtils.isEmpty(pageUrl)) {
                val host = MainUtil.shortHostOf(pageUrl)
                icon = cachedIcon(host)
                if (!isBitmapOk(icon)) {
                    icon = DbBookIcon.iconOf(context, host)
                }
            }
            val values = ContentValues()
            values.put("_dir", item.dirPath)
            values.put("_dname", item.dirName)
            values.put("_path", item.uriString)
            values.put("_name", item.name)
            values.put("_time", System.currentTimeMillis())
            values.put("_size", item.size)
            values.put("_url", pageUrl)
            val where = "_secret=? AND _path=?"
            val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), item.uriString)
            var state = 0
            var hasIcon = false
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookPage.get(context).getWritableDatabase(),
                    "DbBookPage_table",
                    arrayOf<String?>("_icon"),
                    where,
                    args,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    state = 1
                    try {
                        val blob =
                            if (isBitmapOk(icon)) cursor.getBlob(cursor.getColumnIndex("_icon")) else null
                        hasIcon = blob != null && blob.size > 0
                    } catch (unused: Exception) {
                    }
                } else {
                    state = 2
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (state == 0) {
                return
            }
            if (!hasIcon && isBitmapOk(icon)) {
                try {
                    val out = ByteArrayOutputStream()
                    icon!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                    values.put("_icon", out.toByteArray())
                    out.close()
                } catch (unused: Exception) {
                }
            }
            if (state == 1) {
                updateWhere(
                    DbBookPage.get(context).getWritableDatabase(),
                    "DbBookPage_table",
                    values,
                    where,
                    args
                )
            } else {
                values.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
                insert(DbBookPage.get(context).getWritableDatabase(), "DbBookPage_table", values)
            }
        }
    }

    override fun onReceiveValue(value: String?) {
        val result = value
        val resultEmpty = TextUtils.isEmpty(result)
        val webViewActivity = wa
        if (!resultEmpty) {
            webViewActivity.dialogsState.downPageResultText = result
            webViewActivity.runInBackground(SaveResultStep())
        } else {
            if (webViewActivity.webCoord == null) {
                return
            }
            webViewActivity.updateUpButton(false)
            webViewActivity.closeAllSnackbars()
            val mySnackbar = MySnackbar(webViewActivity)
            webViewActivity.snackbar = mySnackbar
            mySnackbar.setAppBarView(webViewActivity.barScroll)
            webViewActivity.snackbar!!.showSingle(
                webViewActivity.webCoord,
                AppR.string.save_fail,
                SavedSnackbarListener()
            )
        }
    }
}

class PrintPagePathListener(@JvmField val wa: WebViewActivity) : PathChangeListener {
    override fun onPath(path: String?) {
        val pathEmpty = TextUtils.isEmpty(path)
        val webViewActivity = wa
        if (pathEmpty) {
            showToast(webViewActivity, AppR.string.input_name)
            return
        }
        val dialogPrintPage = webViewActivity.printPageDialog
        if (dialogPrintPage != null) {
            dialogPrintPage.dismiss()
            webViewActivity.printPageDialog = null
        }
        webViewActivity.printPage(webViewActivity.currentPage, path)
    }
}

class WvaPrintPageDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogPrintPage = webViewActivity.printPageDialog
        if (dialogPrintPage != null) {
            dialogPrintPage.dismiss()
            webViewActivity.printPageDialog = null
        }
    }
}

class WvaPrintPageTask(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val title: String
) : Runnable {

    internal inner class NoPrintAppToastStep : Runnable {
        override fun run() {
            val webViewActivity = this@WvaPrintPageTask.webViewActivity
            val appsNone = AppR.string.apps_none
            webViewActivity.javaClass
            showToast(webViewActivity, appsNone)
        }
    }

    internal inner class PrintNotSupportedToastStep : Runnable {
        override fun run() {
            val webViewActivity = this@WvaPrintPageTask.webViewActivity
            val notSupported = AppR.string.not_supported
            webViewActivity.javaClass
            showToast(webViewActivity, notSupported)
        }
    }

    override fun run() {
        val webViewActivity = this.webViewActivity
        val printDocumentAdapter = webViewActivity.dialogsState.printAdapter
        webViewActivity.dialogsState.printAdapter = null
        try {
            webViewActivity.dialogsState.printJob =
                (webViewActivity.getSystemService("print") as PrintManager).print(
                    this.title,
                    printDocumentAdapter!!,
                    PrintAttributes.Builder().build()
                )
        } catch (unused: ActivityNotFoundException) {
            webViewActivity.dialogsState.printJob = null
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(NoPrintAppToastStep())
            }
        } catch (unused2: Exception) {
            webViewActivity.dialogsState.printJob = null
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(PrintNotSupportedToastStep())
            }
        }
    }
}

class WvaShowSetTransDialogListener(@JvmField val wa: WebViewActivity) : DialogApplyListener {
    override fun onApply() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView == null) {
            return
        }
        val url = webNestView.getUrl()
        if (TextUtils.isEmpty(url) || "about:blank" == url) {
            showToast(webViewActivity, AppR.string.blank_page)
            webViewActivity.lastTransLang = null
        } else if (url!!.startsWith("file:///")) {
            showToast(webViewActivity, AppR.string.not_supported_page)
            webViewActivity.lastTransLang = null
        } else {
            webViewActivity.onTranslateButton(AlbumSettings.transPick.value)
            webViewActivity.lastTransLang = null
        }
    }
}

class WvaShowTransMsgDialogListener(@JvmField val wa: WebViewActivity) : SetDeskListener {
    override fun onChanged(deskMode: Boolean) {
        val webViewActivity = wa
        webViewActivity.closeTransMsgDialog()
        if (deskMode) {
            webViewActivity.detectSiteFix()
            return
        }
        val currentUrl = webViewActivity.currentPageUrl(false)
        if (URLUtil.isNetworkUrl(currentUrl)) {
            webViewActivity.dialogsState.leavingByIntent = openCustomTab(webViewActivity, null, currentUrl)
        } else {
            showToast(webViewActivity, AppR.string.not_supported_page)
        }
    }
}

class WvaCreateIntro(@JvmField val wa: WebViewActivity) : Runnable {
    /**
     * First start: opens the setup wizard ([com.mycompany.app.ui.onboarding.OnboardingActivity], 2026-11-20; it
     * replaced the four-picture intro overlay). The wizard stores "seen" itself.
     */
    override fun run() {
        com.mycompany.app.ui.onboarding.OnboardingActivity.open(wa)
    }
}

class WvaTransCtrlListener(@JvmField val wa: WebViewActivity) : TransCtrlListener {
    override fun onClose() {
        wa.closeTransControl()
    }

    override fun onOriginal() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null && !webNestView.jsAdded) {
            showToast(webViewActivity, AppR.string.http_warning)
            return
        }
        if (webViewActivity.dialogsState.transState.toInt() == 1) {
            showToast(webViewActivity, AppR.string.wait_retry)
            return
        }
        webViewActivity.hideTransControl()
        if (webViewActivity.dialogsState.transState.toInt() == 3) {
            evalConfirmJs(webViewActivity.currentPage, "restore")
        }
    }

    override fun onMenu(view: View?) {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.transCtrlMenu == null) {
            webViewActivity.closeTransCtrlMenu()
            if (view != null) {
                val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                arrayList.add(PopMenuItem(0, AppR.string.setting))
                arrayList.add(PopMenuItem(1, AppR.string.locale))
                arrayList.add(PopMenuItem(2, AppR.string.info))
                val myPopupMenu = MyPopupMenu(
                    webViewActivity,
                    webViewActivity.dialogsState.webBody,
                    view,
                    arrayList,
                    AppState.darkUi,
                    TransLangMenuListener(webViewActivity)
                )
                webViewActivity.dialogsState.transCtrlMenu = myPopupMenu
                webViewActivity.popupMenu = myPopupMenu
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(WvaTransCtrlMenuClosed(webViewActivity))
            }
        }
    }

    override fun onTranslate() {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null && !webNestView.jsAdded) {
            showToast(webViewActivity, AppR.string.http_warning)
            return
        }
        webViewActivity.onTranslateButton(webViewActivity.transLangCode)
        if (webViewActivity.dialogsState.transState.toInt() == 3 && TextUtils.isEmpty(webViewActivity.transLangCode)) {
            showToast(webViewActivity, AppR.string.select_lang)
        }
    }
}

class TransLangMenuListener(@JvmField val wa: WebViewActivity) : MyPopupListener {
    override fun onClose() {
        wa.closeTransCtrlMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        val webViewActivity = wa
        if (id == 0) {
            webViewActivity.hideTransControl()
            webViewActivity.translateState.transLangMenuListenerBusy = true
            webViewActivity.showSetTransDialog()
            return true
        }
        if (id == 1) {
            webViewActivity.closeTransControl()
            if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                val dialogTransLang = webViewActivity.transLangDialog
                if (dialogTransLang != null) {
                    dialogTransLang.dismiss()
                    webViewActivity.transLangDialog = null
                }
                if (!DataTrans.instance(webViewActivity.dialogsState.appContext).hasLanguages() && webViewActivity.dialogsState.transState.toInt() == 1) {
                    showToast(webViewActivity, AppR.string.wait_retry)
                    return true
                }
                val transLangDialog =
                    DialogTransLang(webViewActivity, false, WvaTransLangPicked(webViewActivity))
                webViewActivity.transLangDialog = transLangDialog
                transLangDialog.setOnDismissListener(WvaTransLangDialogDismiss(webViewActivity))
                return true
            }
        } else if (id == 2) {
            webViewActivity.hideTransControl()
            webViewActivity.addTabInGroup(null, "https://support.google.com/translate", true, null)
        }
        return true
    }
}

class WvaTransCtrlMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.transCtrlMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class SetDeskDialogListener(@JvmField val wa: WebViewActivity) : SetDeskListener {
    override fun onChanged(deskMode: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        val dialogSetDesk = webViewActivity.dialogsState.setDeskDialog
        if (dialogSetDesk != null) {
            dialogSetDesk.dismiss()
            webViewActivity.dialogsState.setDeskDialog = null
        }
        webViewActivity.currentPage!!.setDeskMode(deskMode)
        val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
        if (webTabItemD != null) {
            val url = webViewActivity.currentPage!!.getUrl()
            webTabItemD.tabUrl = url
            webTabItemD.tabTitle = webViewActivity.pageTitleOf(webViewActivity.currentPage, url)
            webTabItemD.deskMode = deskMode
            webViewActivity.loadTabItemAsync(webTabItemD)
        }
        val webNestView = webViewActivity.currentPage
        if (deskMode) {
            setUserAgent(webNestView!!.getSettings(), chromeVersion(webViewActivity.dialogsState.appContext))
        } else {
            webNestView!!.applyUserAgent(webViewActivity.dialogsState.appContext, ZtwoSettings.agentType.value, null)
        }
        if (webViewActivity.dialogsState.deskModeBefore != webViewActivity.currentPage!!.isDesktopAgent()) {
            evalPageJs(webViewActivity.currentPage, webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost)
        }
        webViewActivity.reloadPage()
    }
}

class WvaSetDeskDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetDesk = webViewActivity.dialogsState.setDeskDialog
        if (dialogSetDesk != null) {
            dialogSetDesk.dismiss()
            webViewActivity.dialogsState.setDeskDialog = null
        }
    }
}

class WvaSetUrlMenuListener(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val isLink: Boolean
) : DialogSeekListener {
    override fun onValue(newValue: Int) {
        val webViewActivity = this.webViewActivity
        webViewActivity.closeSetUrlDialog()
        if (!this.isLink) {
            webViewActivity.onAddressMenuItem(newValue)
            return
        }
        if (newValue == 0) {
            webViewActivity.showBookmarkEditDialog()
            return
        }
        if (newValue == 1) {
            webViewActivity.showQuickEditDialog(null)
            return
        }
        if (newValue == 2) {
            webViewActivity.showEditShortDialog()
            return
        }
        if (newValue == 3) {
            val homeUrl = webViewActivity.currentPageUrl(true)
            if (TextUtils.isEmpty(homeUrl)) {
                return
            }
            if (homeUrl != BrowserSettings.homePage.value) {
                BrowserSettings.edit(webViewActivity.dialogsState.appContext) { set(BrowserSettings.homePage, homeUrl) }
            }
            showToast(webViewActivity, AppR.string.setted)
            return
        }
        if (newValue == 4) {
            val startUrl = webViewActivity.currentPageUrl(true)
            if (TextUtils.isEmpty(startUrl)) {
                return
            }
            if (startUrl != BrowserSettings.startPage.value) {
                BrowserSettings.edit(webViewActivity.dialogsState.appContext) { set(BrowserSettings.startPage, startUrl) }
            }
            showToast(webViewActivity, AppR.string.setted)
        }
    }
}

class WvaOpenTypeDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogOpenType = webViewActivity.openTypeDialog
        if (dialogOpenType != null) {
            dialogOpenType.dismiss()
            webViewActivity.openTypeDialog = null
        }
    }
}

class WvaShowPopupMenuDialog(@JvmField val wa: WebViewActivity) : SnackbarListener {

    internal inner class PopupTabAdded : WebViewActivity.TabAddListener {
        override fun onTabAdded(isAdded: Boolean) {
            wa.videoState.pendingTabUrl = null
        }
    }

    override fun onSecond() {
        val webViewActivity = wa
        val mySnackbar = webViewActivity.dialogsState.popupBlockSnackbar
        val snackItem: SnackItem? = if (mySnackbar == null) null else mySnackbar.snackItem
        if (mySnackbar == null || snackItem == null) {
            return
        }
        val pkg = snackItem.pkg
        var pendingUrl = snackItem.pendingUrl
        val popNoti = ZoneSettings.popNoti.value
        if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
            webViewActivity.closePopupMenuDialog()
            if (!TextUtils.isEmpty(pkg) && "about:blank" != pkg) {
                val theme = if (webViewActivity.isLandscapeScreen()) 0 else AppR.style.DialogExpandTheme
                if (TextUtils.isEmpty(pendingUrl)) {
                    pendingUrl = webViewActivity.dialogsState.pageUrl
                }
                val dialogPopupMenu = DialogPopupMenu(
                    webViewActivity,
                    theme,
                    pkg,
                    pendingUrl,
                    popNoti,
                    WvaPopupMenuListener(webViewActivity)
                )
                webViewActivity.popupMenuDialog = dialogPopupMenu
                dialogPopupMenu.setOnDismissListener(PopupMenuDialogDismissed(webViewActivity))
                if (MainConst.isApi30Insets || theme != 0) {
                    webViewActivity.popupMenuDialog!!.applyColors(
                        webViewActivity.onStartPage,
                        webViewActivity.useDarkWindow(),
                        webViewActivity.pageThemeColor(),
                        webViewActivity.navBarColorNow(),
                        true
                    )
                }
            }
        }
        if (ZoneSettings.popNoti.value) {
            ZoneSettings.edit(webViewActivity.dialogsState.appContext) { set(ZoneSettings.popNoti, false) }
        }
    }

    override fun onDismissed() {
    }

    override fun onFirst() {
        val webViewActivity = wa
        val mySnackbar = webViewActivity.dialogsState.popupBlockSnackbar
        val snackItem: SnackItem? = if (mySnackbar == null) null else mySnackbar.snackItem
        if (mySnackbar == null || snackItem == null) {
            return
        }
        val pkg = snackItem.pkg
        webViewActivity.videoState.pendingTabUrl = snackItem.pendingUrl
        webViewActivity.addTabInGroup(null, pkg, true, PopupTabAdded())
    }

    override fun onDismiss() {
        wa.dialogsState.popupBlockSnackbar = null
    }
}

class WvaShowAppBlockedSnackbarListener(@JvmField val wa: WebViewActivity) : SnackbarListener {
    override fun onSecond() {
    }

    override fun onDismissed() {
    }

    override fun onFirst() {
        val webViewActivity = wa
        val mySnackbar = webViewActivity.dialogsState.appBlockSnackbar
        val snackItem: SnackItem? = if (mySnackbar == null) null else mySnackbar.snackItem
        if (mySnackbar == null || snackItem == null) {
            return
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaOpenInOtherAppPost(webViewActivity, showAppBlockedSnackbarListenerText = snackItem.siteUrl))
    }

    override fun onDismiss() {
        wa.dialogsState.appBlockSnackbar = null
    }
}

class WvaAppBlockedSnackbarTask(@JvmField val wa: WebViewActivity) : SnackbarListener {
    override fun onSecond() {
        val webViewActivity = wa
        val mySnackbar = webViewActivity.dialogsState.appBlockSnackbar
        val snackItem: SnackItem? = if (mySnackbar == null) null else mySnackbar.snackItem
        if (mySnackbar == null || snackItem == null) {
            return
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaOpenInOtherAppPost(webViewActivity, showAppBlockedSnackbarListenerText = snackItem.siteUrl))
    }

    override fun onDismissed() {
    }

    override fun onFirst() {
        val webViewActivity = wa
        val mySnackbar = webViewActivity.dialogsState.appBlockSnackbar
        val snackItem: SnackItem? = if (mySnackbar == null) null else mySnackbar.snackItem
        if (mySnackbar == null || snackItem == null) {
            return
        }
        webViewActivity.addTabInGroup(null, snackItem.url, true, null)
    }

    override fun onDismiss() {
        wa.dialogsState.appBlockSnackbar = null
    }
}

class OpenInOtherAppTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class NoAppToastStep : Runnable {
        override fun run() {
            showToast(wa, AppR.string.apps_none)
        }
    }

    override fun run() {
        val webViewActivity = wa
        val openInOtherAppPostText = webViewActivity.dialogsState.openInOtherAppPostText
        webViewActivity.dialogsState.openInOtherAppPostText = null
        val openedExternal = openInExternalApp(webViewActivity, openInOtherAppPostText)
        webViewActivity.dialogsState.leavingByIntent = openedExternal
        val handler: Handler? = if (openedExternal) null else webViewActivity.mainHandler
        if (openedExternal || handler == null) {
            return
        }
        handler.post(NoAppToastStep())
    }
}

class WvaShowIntentSnackbarListener(@JvmField val wa: WebViewActivity) : SnackbarListener {
    override fun onSecond() {
    }

    override fun onDismissed() {
    }

    override fun onFirst() {
        val webViewActivity = wa
        val mySnackbar = webViewActivity.dialogsState.appBlockSnackbar
        val snackItem: SnackItem? = if (mySnackbar == null) null else mySnackbar.snackItem
        if (mySnackbar == null || snackItem == null) {
            return
        }
        try {
            webViewActivity.startExternalActivity(snackItem.intent)
        } catch (unused: ActivityNotFoundException) {
            showToast(webViewActivity, AppR.string.apps_none)
        } catch (unused2: Exception) {
            showToast(webViewActivity, AppR.string.not_supported)
        }
    }

    override fun onDismiss() {
        wa.dialogsState.appBlockSnackbar = null
    }
}

class WvaAllowPopupDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogAllowPopup = webViewActivity.allowPopupDialog
        if (dialogAllowPopup != null) {
            dialogAllowPopup.dismiss()
            webViewActivity.allowPopupDialog = null
        }
    }
}

class WvaBlockLinkDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogBlockLink = webViewActivity.blockLinkDialog
        if (dialogBlockLink != null) {
            dialogBlockLink.dismiss()
            webViewActivity.blockLinkDialog = null
        }
    }
}

class WvaBlockImageDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogBlockImage = webViewActivity.dialogsState.blockImageDialog
        if (dialogBlockImage != null) {
            dialogBlockImage.dismiss()
            webViewActivity.dialogsState.blockImageDialog = null
        }
    }
}

class WvaAreaToolbarListener(@JvmField val wa: WebViewActivity) : WebAreaListener {
    override fun onClose() {
        wa.hidePageOverlays(true)
    }

    override fun onDone() {
        val webViewActivity = wa
        val webNestView: WebNestView? = if (webViewActivity.dialogsState.areaToolbar == null) null else webViewActivity.currentPage
        if (webViewActivity.dialogsState.areaToolbar == null || webNestView == null) {
            return
        }
        webNestView.evaluateJavascript(
            "(function(){if(!window.sbcmd)return null;var cmd=window.sbcmd;var eles=window.sbattr;if(eles){eles.forEach((ele)=>{if(!ele)ele='null';cmd+='!@!'+ele;});}return cmd;})();",
            WvaAreaResultCallback(webViewActivity)
        )
    }

    override fun onMultiToggled() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar != null) {
            if (TextUtils.isEmpty(webViewActivity.areaScript)) {
                val singleJs = areaMultiJs(0)
                webViewActivity.areaScript = singleJs
                if (TextUtils.isEmpty(singleJs)) {
                    return
                }
            }
            if (TextUtils.isEmpty(webViewActivity.dialogsState.areaToolbarListenerText)) {
                val multiJs = areaMultiJs(1)
                webViewActivity.dialogsState.areaToolbarListenerText = multiJs
                if (TextUtils.isEmpty(multiJs)) {
                    return
                }
            }
            val sb = StringBuilder()
            sb.append(webViewActivity.areaScript)
            sb.append(if (ReadSettings.areaMulti.value) "true;" else "false;")
            sb.append(webViewActivity.dialogsState.areaToolbarListenerText)
            evalJs(webViewActivity.currentPage, sb.toString(), true)
        }
    }

    override fun onHelp() {
        val webViewActivity = wa
        if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
            return
        }
        val dialogGuideArea = webViewActivity.dialogsState.guideAreaDialog
        if (dialogGuideArea != null) {
            dialogGuideArea.dismiss()
            webViewActivity.dialogsState.guideAreaDialog = null
        }
        val guideAreaDialog =
            DialogGuideArea(webViewActivity, 0, WvaShowGuideAreaDialog(webViewActivity))
        webViewActivity.dialogsState.guideAreaDialog = guideAreaDialog
        guideAreaDialog.setOnDismissListener(WvaGuideAreaDialogDismissed(webViewActivity))
        webViewActivity.dialogsState.guideAreaDialog!!.pickMode = true
    }
}

class WvaAreaReinjectScript(@JvmField val wa: WebViewActivity, @JvmField val areaPageTouchBusy: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar == null || this@WvaAreaReinjectScript.areaPageTouchBusy) {
            return
        }
        if (TextUtils.isEmpty(webViewActivity.dialogsState.areaReinjectScriptText)) {
            val upScript = MainUtil.areaUpScript()
            webViewActivity.dialogsState.areaReinjectScriptText = upScript
            if (TextUtils.isEmpty(upScript)) {
                return
            }
        }
        evalJs(webViewActivity.currentPage, webViewActivity.dialogsState.areaReinjectScriptText, true)
    }
}

class WvaAreaResultCallback(@JvmField val wa: WebViewActivity) : ValueCallback<String> {

    internal inner class AreaResultStep : Runnable {

        override fun run() {
            val url: String?
            val webViewActivity = wa
            val areaResultCallbackText = webViewActivity.dialogsState.areaResultCallbackText
            webViewActivity.dialogsState.areaResultCallbackText = null
            if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
                return
            }
            if (TextUtils.isEmpty(areaResultCallbackText)) {
                url = null
            } else {
                val areaText = unescapeJavaText(stripQuotes(areaResultCallbackText))
                if (TextUtils.isEmpty(areaText) || "null" == areaText || "undefined" == areaText) {
                    url = null
                } else {
                    url = areaText
                }
            }
            if (TextUtils.isEmpty(url)) {
                showToast(webViewActivity, AppR.string.backup_target)
                return
            }
            val dialogEditArea = webViewActivity.editAreaDialog
            if (dialogEditArea != null) {
                dialogEditArea.dismiss()
                webViewActivity.editAreaDialog = null
            }
            val editAreaDialog = DialogEditArea(
                webViewActivity,
                webViewActivity.dialogsState.pageUrl,
                webViewActivity.dialogsState.pageHost,
                url,
                webViewActivity.dialogsState.transState.toInt() == 3 && webViewActivity.dialogsState.transReady,
                WvaEditAreaListener(webViewActivity)
            )
            webViewActivity.editAreaDialog = editAreaDialog
            editAreaDialog.setOnDismissListener(WvaEditAreaDialogDismissed(webViewActivity))
        }
    }

    override fun onReceiveValue(value: String?) {
        val result = value
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar != null) {
            webViewActivity.dialogsState.areaResultCallbackText = result
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(AreaResultStep())
        }
    }
}

class WvaShowGuideAreaDialog(@JvmField val wa: WebViewActivity) : DialogAreaListener {
    override fun onPick() {
        val webViewActivity = wa
        val dialogGuideArea = webViewActivity.dialogsState.guideAreaDialog
        if (dialogGuideArea != null) {
            dialogGuideArea.dismiss()
            webViewActivity.dialogsState.guideAreaDialog = null
        }
        val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.PopupSettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 3)
        intent.putExtra("EXTRA_PATH", checkedUrl)
        webViewActivity.launchExternalForResult(intent, RequestCode.CLEAN_SETTINGS)
    }

    override fun onHelp() {
        val webViewActivity = wa
        val dialogGuideArea: DialogGuideArea? = if (webViewActivity.isClosing) null else webViewActivity.guideAreaDialog
        if (webViewActivity.isClosing || dialogGuideArea != null) {
            return
        }
        val guideAreaDialog = DialogGuideArea(webViewActivity, 1, null)
        webViewActivity.guideAreaDialog = guideAreaDialog
        guideAreaDialog.setOnDismissListener(WvaGuideAreaDialogDismissed2(webViewActivity))
    }
}

class WvaGuideAreaDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogGuideArea = webViewActivity.dialogsState.guideAreaDialog
        if (dialogGuideArea != null) {
            dialogGuideArea.dismiss()
            webViewActivity.dialogsState.guideAreaDialog = null
        }
    }
}

class WvaGuideAreaDialogDismissed2(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogGuideArea = webViewActivity.guideAreaDialog
        if (dialogGuideArea != null) {
            dialogGuideArea.dismiss()
            webViewActivity.guideAreaDialog = null
        }
    }
}

class WvaEditAreaDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogEditArea = webViewActivity.editAreaDialog
        if (dialogEditArea != null) {
            dialogEditArea.dismiss()
            webViewActivity.editAreaDialog = null
        }
    }
}

class WvaTabGroupDeleteListener(@JvmField val wa: WebViewActivity, @JvmField val showTabGroupDeleteDialogTabItem: WebTabItem? = null, @JvmField val index: Int = 0, @JvmField val groupId: Long = 0) : DelItemListener {

    internal inner class GroupDeleteDialogStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val dialogDeleteItem = webViewActivity.tabGroupDeleteDialog
            if (dialogDeleteItem == null) {
                return
            }
            dialogDeleteItem.applyBusy(false)
            showToast(webViewActivity, AppR.string.fail)
        }
    }

    override fun onDelete() {
        var size: Int = 0
        val webViewActivity = wa
        val startIndex = this@WvaTabGroupDeleteListener.index
        val groupId = this@WvaTabGroupDeleteListener.groupId
        if (webViewActivity.tabGroupDeleteDialog != null) {
            val arrayList = webViewActivity.tabsState.tabItems
            if (arrayList != null && startIndex < (arrayList.size.also { size = it })) {
                val items: ArrayList<Any?> = ArrayList<Any?>()
                synchronized(webViewActivity.tabLock) {
                    for (index in startIndex..<size) {
                        val webTabItem = webViewActivity.tabsState.tabItems!!.get(index)
                        if (webTabItem != null) {
                            if (webTabItem.groupId != groupId) {
                                break
                            }
                            val webNestFrame = webTabItem.webFrame
                            if (webNestFrame != null) {
                                webNestFrame.destroyFrame(null)
                                webTabItem.webFrame = null
                            }
                            items.add(webTabItem)
                        }
                    }
                }
                if (!items.isEmpty()) {
                    webViewActivity.runInBackground(WvaTabGroupDeleteTask(webViewActivity, items, startIndex))
                    return
                }
            }
            val myWebCoord = wa.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(GroupDeleteDialogStep())
        }
    }

    override fun bindHead(myRoundImage: MyRoundImage?, appCompatTextView: AppCompatTextView?) {
        val webViewActivity = wa
        val webTabItem = this@WvaTabGroupDeleteListener.showTabGroupDeleteDialogTabItem
        if (webTabItem == null || webViewActivity.tabGroupDeleteDialog == null) {
            return
        }
        var groupName = webTabItem.groupName
        if (TextUtils.isEmpty(groupName)) {
            groupName = webViewActivity.getString(AppR.string.group_title)
        }
        myRoundImage!!.showIcon(0, WebTabBar.colorIndex(webTabItem.groupColor, AppState.darkUi))
        appCompatTextView!!.setText(groupName)
    }
}

class WvaTabEditDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogTabEdit = webViewActivity.dialogsState.tabEditDialog
        if (dialogTabEdit != null) {
            dialogTabEdit.dismiss()
            webViewActivity.dialogsState.tabEditDialog = null
        }
    }
}

class WvaPromptDialogCancelled(@JvmField val wa: WebViewActivity) : DialogInterface.OnCancelListener {
    override fun onCancel(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogPrompt = webViewActivity.promptDialog
        if (dialogPrompt != null) {
            dialogPrompt.dismiss()
            webViewActivity.promptDialog = null
        }
    }
}

class WvaPromptDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogPrompt = webViewActivity.promptDialog
        if (dialogPrompt != null) {
            dialogPrompt.dismiss()
            webViewActivity.promptDialog = null
        }
    }
}

class WvaEditAuthDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogEditAuth = webViewActivity.editAuthDialog
        if (dialogEditAuth != null) {
            dialogEditAuth.dismiss()
            webViewActivity.editAuthDialog = null
        }
    }
}

class WvaHandlePermissionRequestTask(@JvmField val activity: WebViewActivity,
                                       @JvmField val request: PermissionRequest?,
                                       @JvmField val geoCallback: GeolocationPermissions.Callback?,
                                       @JvmField val origin: String?
) : Runnable {

    internal inner class PermissionRequestStep(@JvmField val outer: WvaHandlePermissionRequestTask,
                                         @JvmField val pending: Boolean,
                                         @JvmField val busy: Boolean,
                                         @JvmField val permissionRequest: PermissionRequest?,
                                         @JvmField val allow: Boolean,
                                         @JvmField val callback: GeolocationPermissions.Callback?,
                                         @JvmField val origin: String?,
                                         @JvmField val allowed: Boolean
    ) : Runnable {
        override fun run() {
            val webViewActivity = this.outer.activity
            val wantsCamera = webViewActivity.dialogsState.requestsCamera
            val permissionOrigin = this.origin
            val callback = this.callback
            val permissionRequest = this.permissionRequest
            var found = false
            if (!wantsCamera && !webViewActivity.dialogsState.requestsMic) {
                val wantsProtected = webViewActivity.dialogsState.requestsProtectedMedia
                if (wantsProtected) {
                    if (!webViewActivity.dialogsState.offerMidi) {
                        if (if (wantsProtected) this.allow else false) {
                            permissionRequest?.grant(permissionRequest.getResources())
                            return
                        } else {
                            permissionRequest?.deny()
                            return
                        }
                    }
                } else if (webViewActivity.dialogsState.requestsLocation && !webViewActivity.dialogsState.offerLocation) {
                    callback?.invoke(permissionOrigin, this.allowed, false)
                    return
                }
            } else if (!webViewActivity.dialogsState.offerCamera && !webViewActivity.dialogsState.offerMic) {
                val wantsMic = webViewActivity.dialogsState.requestsMic
                val busy = this.busy
                if (wantsCamera) {
                    val pending = this.pending
                    if (!wantsMic) {
                        found = pending
                    } else if (pending && busy) {
                        found = true
                    }
                } else if (wantsMic) {
                    found = busy
                }
                if (found) {
                    permissionRequest?.grant(permissionRequest.getResources())
                    return
                } else {
                    permissionRequest?.deny()
                    return
                }
            }
            if (webViewActivity.currentPage == null || webViewActivity.permissionDialog != null || webViewActivity.isShowingStartPage()) {
                return
            }
            webViewActivity.closePermissionDialog()
            webViewActivity.pageState.permissionRequest = permissionRequest
            webViewActivity.pageState.permissionOrigin = permissionOrigin
            webViewActivity.pageState.geoCallback = callback
            webViewActivity.dialogsState.permissionPageUrl = webViewActivity.currentPage!!.getUrl()
            webViewActivity.runInBackground(WvaShowPermissionDialog(webViewActivity, permissionPageTitle = webViewActivity.currentPage!!.getTitle()))
        }
    }

    /**
     * Site permissions for the current page: reads the saved allow/block bits wa.of the host (DbBookPms; 2 camera,
     * 4 microphone, 16 MIDI, 8 location), works out which requests still need asking (wa.im..om) and continues wa.on
     * the UI thread in PermissionRequestStep with the allowed bits, the web request and the geolocation callback.
     */
    override fun run() {
        val activity = this.activity
        val context = activity.dialogsState.appContext
        val url = activity.dialogsState.pageHost
        var allow = 0
        var block = 0
        if (context != null && !TextUtils.isEmpty(url)) {
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookPms.get(context).getWritableDatabase(),
                    "DbBookPms_table",
                    arrayOf<String?>("_allow", "_block"),
                    "_path=?",
                    arrayOf<String?>(url!!),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    allow = cursor.getInt(cursor.getColumnIndex("_allow"))
                    block = cursor.getInt(cursor.getColumnIndex("_block"))
                }
            } catch (ignored: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
        }
        activity.dialogsState.permissionAllow = allow
        activity.pageState.permissionBlock = block
        val allowCamera = (allow and 2) == 2
        val allowMic = (allow and 4) == 4
        val allowMidi = (allow and 16) == 16
        val allowLocation = (allow and 8) == 8
        // Site settings: a permission switched off for all sites counts as blocked unless this site was allowed it
        // (not saved: the site's own row keeps only its own decisions)
        val effBlock = block or (com.mycompany.app.pref.store.BrowserSettings.sitePermBlock.value and allow.inv())
        val blockCamera = (effBlock and 2) == 2
        val blockMic = (effBlock and 4) == 4
        val blockMidi = (effBlock and 16) == 16
        val blockLocation = (effBlock and 8) == 8
        val savedCamera = allowCamera || blockCamera
        val savedMic = allowMic || blockMic
        val savedMidi = allowMidi || blockMidi
        val savedLocation = allowLocation || blockLocation
        activity.dialogsState.grantCamera = activity.dialogsState.requestsCamera && !blockCamera && hasPermission(activity.dialogsState.appContext, 2)
        activity.dialogsState.grantMic = activity.dialogsState.requestsMic && !blockMic && hasPermission(activity.dialogsState.appContext, 3)
        activity.dialogsState.grantLocation = activity.dialogsState.requestsLocation && !blockLocation && hasPermission(activity.dialogsState.appContext, 4)
        activity.dialogsState.offerCamera = activity.dialogsState.grantCamera || (activity.dialogsState.requestsCamera && !savedCamera)
        activity.dialogsState.offerMic = activity.dialogsState.grantMic || (activity.dialogsState.requestsMic && !savedMic)
        activity.dialogsState.offerMidi = activity.dialogsState.requestsProtectedMedia && !savedMidi
        activity.dialogsState.offerLocation = activity.dialogsState.grantLocation || (activity.dialogsState.requestsLocation && !savedLocation)
        if (activity.currentPage == null || activity.dialogsState.webBody == null) {
            return
        }
        activity.dialogsState.webBody!!.post(
            PermissionRequestStep(
                this,
                allowCamera,
                allowMic,
                this.request,
                allowMidi,
                this.geoCallback,
                this.origin,
                allowLocation
            )
        )
    }
}

class WvaShowPermissionDialog(@JvmField val wa: WebViewActivity, @JvmField val permissionPageTitle: String? = null) : Runnable {

    internal inner class ShowPermissionStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val permissionPageTitle = this@WvaShowPermissionDialog.permissionPageTitle
            val permissionPageUrl = webViewActivity.dialogsState.permissionPageUrl
            val showPermissionDialogText = webViewActivity.dialogsState.showPermissionDialogText
            webViewActivity.dialogsState.permissionPageUrl = null
            webViewActivity.dialogsState.showPermissionDialogText = null
            if (webViewActivity.permissionDialog != null) {
                return
            }
            // what the site asks for (the requests still to be answered; all requested ones if none is open)
            val ds = webViewActivity.dialogsState
            var kinds = (if (ds.offerCamera) DialogPermission.CAMERA else 0) or (if (ds.offerMic) DialogPermission.MIC else 0) or
                (if (ds.offerMidi) DialogPermission.PROTECTED else 0) or (if (ds.offerLocation) DialogPermission.LOCATION else 0)
            if (kinds == 0) {
                kinds = (if (ds.requestsCamera) DialogPermission.CAMERA else 0) or
                    (if (ds.requestsMic) DialogPermission.MIC else 0) or
                    (if (ds.requestsProtectedMedia) DialogPermission.PROTECTED else 0) or
                    (if (ds.requestsLocation) DialogPermission.LOCATION else 0)
            }
            val host = ds.pageHost ?: com.mycompany.app.main.MainUtil.hostOfUrl(permissionPageUrl, true)
            val dialogPermission = DialogPermission(
                webViewActivity,
                host,
                kinds,
                WvaDesktopModeListener(webViewActivity),
                onOnce = { webViewActivity.pageState.permissionOnce = true },
                onSiteSettings = { com.mycompany.app.ui.settings.SiteSettingsActivity.open(webViewActivity, host) },
            )
            webViewActivity.permissionDialog = dialogPermission
            dialogPermission.setOnDismissListener(WvaPermissionRequestDismissed(webViewActivity))
        }
    }

    override fun run() {
        var message: String?
        val webViewActivity = wa
        var permissionPageUrl = webViewActivity.dialogsState.permissionPageUrl
        val decodedUrl = urlDecode(permissionPageUrl)
        if (!TextUtils.isEmpty(decodedUrl)) {
            permissionPageUrl = decodedUrl
        }
        val wantsCamera = webViewActivity.dialogsState.requestsCamera
        val wantsMic = webViewActivity.dialogsState.requestsMic
        if (wantsCamera) {
            val camera = AppR.string.camera
            if (wantsMic) {
                message =
                    webViewActivity.getString(camera) + " ]  [ " + webViewActivity.getString(AppR.string.audio)
            } else {
                message = webViewActivity.getString(camera)
            }
        } else if (wantsMic) {
            message = webViewActivity.getString(AppR.string.audio)
        } else if (webViewActivity.dialogsState.requestsProtectedMedia) {
            message = webViewActivity.getString(AppR.string.media)
        } else {
            message =
                if (webViewActivity.dialogsState.requestsLocation) webViewActivity.getString(AppR.string.location) else null
        }
        if (!TextUtils.isEmpty(message)) {
            message = "[ $message ]"
        }
        webViewActivity.dialogsState.permissionPageUrl = permissionPageUrl
        webViewActivity.dialogsState.showPermissionDialogText = message
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(ShowPermissionStep())
    }
}

class WvaShowPassInfoDialog(@JvmField val wa: WebViewActivity, @JvmField val savePassHost: String? = null, @JvmField val savePassUser: String? = null, @JvmField val savePassWord: String? = null, @JvmField val savePassUrl: String? = null) : Runnable {

    internal inner class ShowPassInfoStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val infoPassHost = webViewActivity.dialogsState.infoPassHost
            val infoPassUser = webViewActivity.dialogsState.infoPassUser
            val infoPassWord = webViewActivity.dialogsState.infoPassWord
            val infoPassUrl = webViewActivity.dialogsState.infoPassUrl
            webViewActivity.dialogsState.infoPassHost = null
            webViewActivity.dialogsState.infoPassUser = null
            webViewActivity.dialogsState.infoPassWord = null
            webViewActivity.dialogsState.infoPassUrl = null
            if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
                return
            }
            val dialogPassInfo = webViewActivity.passInfoDialog
            if (dialogPassInfo != null) {
                dialogPassInfo.dismiss()
                webViewActivity.passInfoDialog = null
            }
            if (TextUtils.isEmpty(infoPassHost) || TextUtils.isEmpty(infoPassUser) || TextUtils.isEmpty(infoPassWord)) {
                return
            }
            val passInfoDialog = DialogPassInfo(
                webViewActivity,
                0L,
                infoPassHost,
                infoPassUser,
                infoPassWord,
                infoPassUrl,
                WvaPassInfoListener(webViewActivity)
            )
            webViewActivity.passInfoDialog = passInfoDialog
            passInfoDialog.setOnDismissListener(WvaPassInfoDialogDismissed(webViewActivity))
        }
    }

    override fun run() {
        val webViewActivity = wa
        val savePassHost = this@WvaShowPassInfoDialog.savePassHost
        val savePassUser = this@WvaShowPassInfoDialog.savePassUser
        val savePassWord = this@WvaShowPassInfoDialog.savePassWord
        val savePassUrl = this@WvaShowPassInfoDialog.savePassUrl
        var cursorG: Cursor? = null
        if (TextUtils.isEmpty(savePassHost) || TextUtils.isEmpty(savePassUser) || TextUtils.isEmpty(savePassWord) || savePassHost == webViewActivity.dialogsState.passInfoListenerText) {
            return
        }
        val context = webViewActivity.dialogsState.appContext
        val dbBookPass = DbBookPass.cached
        var found = false
        if (context != null && !TextUtils.isEmpty(savePassHost) && !TextUtils.isEmpty(savePassUser) && !TextUtils.isEmpty(
                savePassWord
            )
        ) {
            try {
                cursorG = DbUtil.query(
                    DbBookPass.get(context).getWritableDatabase(),
                    "DbBookPass_table",
                    null,
                    "_secret=? AND _path=? AND _user_val=? AND _pass_val=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), savePassHost!!, savePassUser!!, savePassWord!!),
                    null
                )
                if (cursorG != null && cursorG.moveToFirst()) {
                    found = true
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
        }
        if (found) {
            return
        }
        webViewActivity.dialogsState.infoPassHost = savePassHost
        webViewActivity.dialogsState.infoPassUser = savePassUser
        webViewActivity.dialogsState.infoPassWord = savePassWord
        webViewActivity.dialogsState.infoPassUrl = savePassUrl
        val myWebBody: MyWebBody? = if (webViewActivity.currentPage == null) null else webViewActivity.dialogsState.webBody
        if (webViewActivity.currentPage == null || myWebBody == null) {
            return
        }
        myWebBody.post(ShowPassInfoStep())
    }
}

class WvaPassInfoListener(@JvmField val wa: WebViewActivity) : PassInfoListener {
    override fun onSaved(name: String?, password: String?) {
        val saveSuccess = AppR.string.save_success
        val webViewActivity = wa
        showToast(webViewActivity, saveSuccess)
        val dialogPassInfo = webViewActivity.passInfoDialog
        if (dialogPassInfo != null) {
            dialogPassInfo.dismiss()
            webViewActivity.passInfoDialog = null
        }
    }

    override fun onDelete(name: String?) {
        wa.dialogsState.passInfoListenerText = name
    }

    override val icon: Bitmap?
        get() {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return null
        }
        return webNestView.getFavicon()
    }
}

class WvaPassInfoDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogPassInfo = webViewActivity.passInfoDialog
        if (dialogPassInfo != null) {
            dialogPassInfo.dismiss()
            webViewActivity.passInfoDialog = null
        }
    }
}

class WvaAddLockButton(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class LockButtonListener : FltViewListener {
        override fun onTap(view: View?, button: Int) {
            var stars: String?
            val webViewActivity = wa
            if (webViewActivity.dialogsState.lockMenu == null) {
                webViewActivity.closeLockMenu()
                val arrayList = webViewActivity.dialogsState.siteLockList
                if (arrayList == null || arrayList.isEmpty()) {
                    showToast(webViewActivity, AppR.string.empty)
                    return
                }
                val items: ArrayList<Any?> = ArrayList<Any?>()
                val size = arrayList.size
                var itemIndex = 0
                for (index in 0..<size) {
                    val childItem = arrayList.get(index) as ChildItem?
                    if (childItem != null && childItem.subText != null) {
                        val itemId = itemIndex + 1
                        items.add(
                            PopMenuItem(
                                itemIndex,
                                if (AppState.darkUi) AppR.drawable.outline_account_circle_dark_24 else AppR.drawable.outline_account_circle_black_24,
                                childItem.url
                            )
                        )
                        itemIndex += 2
                        val iconRes =
                            if (AppState.darkUi) AppR.drawable.outline_vpn_key_dark_24 else AppR.drawable.outline_vpn_key_black_24
                        val subText: String = childItem.subText!!
                        val length = subText.length
                        if (length < 2) {
                            stars = "*"
                        } else if (length == 2) {
                            stars = "**"
                        } else if (length == 3) {
                            stars = "***"
                        } else if (length == 4) {
                            stars = "****"
                        } else if (length == 5) {
                            stars = "*****"
                        } else if (length == 6) {
                            stars = "******"
                        } else if (length == 7) {
                            stars = "*******"
                        } else if (length == 8) {
                            stars = "********"
                        } else if (length == 9) {
                            stars = "*********"
                        } else if (length == 10) {
                            stars = "**********"
                        } else if (length == 11) {
                            stars = "***********"
                        } else if (length == 12) {
                            stars = "************"
                        } else if (length == 13) {
                            stars = "*************"
                        } else if (length == 14) {
                            stars = "**************"
                        } else if (length == 15) {
                            stars = "***************"
                        } else if (length == 16) {
                            stars = "****************"
                        } else if (length == 17) {
                            stars = "*****************"
                        } else if (length == 18) {
                            stars = "******************"
                        } else {
                            stars =
                                if (length == 19) "*******************" else "********************"
                        }
                        items.add(PopMenuItem(itemId, iconRes, stars))
                    }
                }
                val myPopupMenu = MyPopupMenu(
                    webViewActivity,
                    webViewActivity.dialogsState.webBody,
                    view,
                    items,
                    AppState.darkUi,
                    WvaListFloatPopup(webViewActivity)
                )
                webViewActivity.dialogsState.lockMenu = myPopupMenu
                webViewActivity.popupMenu = myPopupMenu
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(WvaLockMenuClosed(webViewActivity))
            }
        }

        override fun onGone() {
            val webViewActivity = wa
            webViewActivity.hideMoveHint()
        }

        override fun onLongPress(view: View?) {
            val webViewActivity = wa
            webViewActivity.showMoveHint(view)
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.floatingState.lockButton != null || webViewActivity.dialogsState.webBody == null) {
            return
        }
        try {
            val arrayList = webViewActivity.dialogsState.siteLockList
            if (arrayList == null || arrayList.isEmpty()) {
                return
            }
            val webFltView = WebFltView(webViewActivity, 2)
            webViewActivity.floatingState.lockButton = webFltView
            webFltView.refreshColors()
            webViewActivity.floatingState.lockButton!!.setHideBlocked(true)
            webViewActivity.floatingState.lockButton!!.setFltListener(LockButtonListener())
            val myWebBody = webViewActivity.dialogsState.webBody
            val lockButton = webViewActivity.floatingState.lockButton
            val width = MainApp.dp48
            wa.floatingState.overlays!!.addView(lockButton, width, width)
        } catch (unused: Exception) {
        }
    }
}

class WvaLockMenuClosed(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.lockMenu != null) {
            webViewActivity.holdMenuReopen()
        }
    }
}

class WvaWebSelectListener(@JvmField val wa: WebViewActivity) : WebSelectListener {
    override fun onPick(type: Int) {
        var type = type
        var mainActivity: WebViewActivity? = null
        run label45@{
            mainActivity = wa
            if (type == 10) {
                if (requestCamera(mainActivity, 28)) {
                    mainActivity.dialogsState.leavingByIntent = true
                    return
                }

                val mediaUri = newMediaUri(RequestCode.CAMERA_OR_FILE, mainActivity, false)
                mainActivity.cameraUri = mediaUri
                if (mediaUri != null) {
                    return@label45
                }
            } else if (type == 11) {
                if (requestCamera(mainActivity, 29)) {
                    mainActivity.dialogsState.leavingByIntent = true
                    return
                }

                val mediaUri = newMediaUri(RequestCode.CAMERA_OR_FILE, mainActivity, true)
                mainActivity.cameraUri = mediaUri
                if (mediaUri != null) {
                    return@label45
                }
            } else {
                if ((type == 4 || type == 5) && getContent(mainActivity, type, true, RequestCode.CAMERA_OR_FILE)) {
                    return@label45
                }

                if (type == 4 && pickImages(RequestCode.PICK_IMAGES, mainActivity, true)) {
                    mainActivity.fileChooserCallback2 = mainActivity.fileChooserCallback
                    mainActivity.fileChooserCallback = null
                    return@label45
                }

                if (pickContent(mainActivity, type, true, RequestCode.PICK_FILES)) {
                    mainActivity.fileChooserCallback3 = mainActivity.fileChooserCallback
                    mainActivity.fileChooserCallback = null
                    return@label45
                }

                showToast(mainActivity, AppR.string.apps_none)
            }

            type = Wn
            mainActivity.deliverFileChooserResult(null)
            return
        }

        mainActivity!!.dialogsState.leavingByIntent = true
    }

    override fun onCancel() {
        wa.deliverFileChooserResult(null)
    }
}

class WvaWebSelectDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogWebSelect = webViewActivity.webSelectDialog
        if (dialogWebSelect != null) {
            dialogWebSelect.dismiss()
            webViewActivity.webSelectDialog = null
        }
    }
}

class WvaSwitchSecretModeTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class SwitchSecretModeStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.webCoord != null) {
                val webVideoFrame = webViewActivity.videoFrame
                if (webVideoFrame != null) {
                    webVideoFrame.onPipEnded()
                    webViewActivity.exitVideoFrame()
                }
                webViewActivity.stopWebClean()
                webViewActivity.hidePageOverlays(true)
                webViewActivity.hideKeyboard()
                webViewActivity.closeSnackbar(false)
                webViewActivity.exitFind(false)
                if (!SyncSettings.secretMode.value && !SecretSettings.keepTab.value) {
                    webViewActivity.restartBrowser()
                    return
                }
                val webNestView: WebNestView? = if (webViewActivity.isExiting) null else webViewActivity.currentPage
                if (webViewActivity.isExiting || webNestView == null || webNestView.blankPending) {
                    webViewActivity.restartBrowser()
                    return
                }
                val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
                if (webTabItemD == null) {
                    webViewActivity.restartBrowser()
                    return
                }
                val handler = webViewActivity.mainHandler
                if (handler == null) {
                    return
                }
                handler.postDelayed(WvaRestartBrowserTask(webViewActivity), 1000L)
                webViewActivity.currentPage!!.saveTabState(
                    webViewActivity.tabsState.currentFrame,
                    webTabItemD.uid,
                    webTabItemD.tabUrl,
                    WvaRestartBundleListener(webViewActivity)
                )
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        val prefSyncR = PrefSync.get(webViewActivity.dialogsState.appContext, false)
        if (prefSyncR != null) {
            val normalIndex = PrefSync.normalIndex
            val newNormalIndex = SyncSettings.normalIndex.value
            if (normalIndex != newNormalIndex) {
                PrefSync.normalIndex = newNormalIndex
                prefSyncR.putInt(newNormalIndex, "mNormalIndex")
            }
            val savedIndex = PrefSync.savedIndex
            val currentSecretIndex = PrefSync.currentSecretIndex
            if (savedIndex != currentSecretIndex) {
                PrefSync.savedIndex = currentSecretIndex
                prefSyncR.putInt(currentSecretIndex, "mSecretIndex")
            }
            prefSyncR.putBool("mSecretMode", SyncSettings.secretMode.value)
        }
        if (!SyncSettings.secretMode.value && !SecretSettings.keepTab.value) {
            DbBookTab.clearSecret(webViewActivity.dialogsState.appContext, true)
            if (PrefSync.currentSecretIndex != 0) {
                PrefSync.currentSecretIndex = 0
                if (prefSyncR != null) {
                    val savedNormalIndex = PrefSync.normalIndex
                    val normalIndex = SyncSettings.normalIndex.value
                    if (savedNormalIndex != normalIndex) {
                        PrefSync.normalIndex = normalIndex
                        prefSyncR.putInt(normalIndex, "mNormalIndex")
                    }
                    val savedIndex = PrefSync.savedIndex
                    val currentSecretIndex = PrefSync.currentSecretIndex
                    if (savedIndex != currentSecretIndex) {
                        PrefSync.savedIndex = currentSecretIndex
                        prefSyncR.putInt(currentSecretIndex, "mSecretIndex")
                    }
                }
            }
        }
        if (prefSyncR != null) {
            prefSyncR.save()
        }
        if (isLockEnabled() && !SecretSettings.lockSkip.value) {
            SecretSettings.editLocal { set(SecretSettings.lockSkip, true) }
            putBoolNow(9, webViewActivity.dialogsState.appContext, "mLockSkip", true)
        }
        if (TtsSettings.appKeypad.value && !TtsSettings.skipKeypad.value) {
            TtsSettings.editLocal { set(TtsSettings.skipKeypad, true) }
            putBoolNow(12, webViewActivity.dialogsState.appContext, "mSkipKeypad", true)
        }
        val mainAppO = MainApp.from(webViewActivity.getApplicationContext())
        val mainDownSvc: MainDownSvc? = if (mainAppO != null && mainAppO.downBinding) mainAppO.downService else null
        if (mainAppO != null && mainAppO.downBinding && mainDownSvc != null) {
            mainDownSvc.stopAll(false, false)
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(SwitchSecretModeStep())
    }
}

class WvaRestartWithUrl(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        restartTask(webViewActivity, "web_restart_url", webViewActivity.onStartPage)
        webViewActivity.dialogsState.switchSecretModeBusy = false
    }
}

class MainMenuActions(@JvmField val wa: WebViewActivity) : DownMenuListener {
    override fun onClose() {
        val webViewActivity = wa
        webViewActivity.mainMenuBusy = false
        webViewActivity.closeMenuDialogs()
        if (webViewActivity.mainMenuActionDone) {
            webViewActivity.showMainMenu(webViewActivity.mainMenuAnchor)
        }
    }

    override fun onItem(view: View?, id: Int) {
        wa.runMenuItem(view, id)
    }

    override fun onDownloads() {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        // the menu's shield: the Web Filter summary for a web page, the Web Filter settings on the start page
        if (!webViewActivity.onStartPage && webViewActivity.openAdblockDialog()) return
        webViewActivity.closeMenuDialogs()
        val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.CleanSettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_PATH", checkedUrl)
        webViewActivity.launchExternalForResult(intent, RequestCode.CLEAN_SETTINGS)
    }

    override fun onHistory() {
        val webViewActivity = wa
        webViewActivity.mainMenuActionDone = true
        webViewActivity.closeMenuDialogs()
    }

    override fun onLongPress() {
        val webViewActivity = wa
        webViewActivity.closeMenuDialogs()
        webViewActivity.startActivity(
            Intent(
                webViewActivity.dialogsState.appContext,
                com.mycompany.app.ui.settings.MenuSettingsActivity::class.java as Class<*>
            )
        )
    }

    override fun onSettings() {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.closeMenuDialogs()
        val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
        val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.MainSettingsActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_PATH", checkedUrl)
        webViewActivity.launchExternalForResult(intent, RequestCode.MAIN_SETTINGS)
    }

    override fun onEdit() {
        val guideRest = ZtriSettings.guideRest.value
        val webViewActivity = wa
        if (!guideRest) {
            val dialogSetMsg: DialogSetMsg? = if (webViewActivity.isClosing) null else webViewActivity.dialogsState.restartNoticeDialog
            if (webViewActivity.isClosing || dialogSetMsg != null) {
                return
            }
            val restartNoticeDialog = DialogSetMsg(
                webViewActivity,
                AppR.string.restart_noti,
                AppR.string.restart_title,
                RestartNoticeDialogListener(webViewActivity)
            )
            webViewActivity.dialogsState.restartNoticeDialog = restartNoticeDialog
            restartNoticeDialog.setOnDismissListener(RestartNoticeDialogDismissed(webViewActivity))
            return
        }
        val dialogConfirm: DialogConfirm? = if (ZtriSettings.guideRest.value && !webViewActivity.isClosing) webViewActivity.dialogsState.restartGuideDialog else null
        if (ZtriSettings.guideRest.value && !webViewActivity.isClosing && dialogConfirm == null) {
            val restartGuideDialog = DialogConfirm(
                webViewActivity as Activity,
                webViewActivity.getString(
                    AppR.string.restart_guide_1
                ) + "\n\n" + webViewActivity.getString(AppR.string.restart_guide_2) + "\n\n" + webViewActivity.getString(
                    AppR.string.restart_guide_3
                ) + "\n" + webViewActivity.getString(AppR.string.restart_guide_4),
                true,
                RestartGuideDialogListener(webViewActivity) as DialogConfListener
            )
            webViewActivity.dialogsState.restartGuideDialog = restartGuideDialog
            restartGuideDialog.setOnDismissListener(RestartGuideDialogDismissed(webViewActivity))
        }
    }

    override fun onExtra() {
        val webViewActivity = wa
        webViewActivity.closeMenuDialogs()
    }
}

class MainMenuItemTask(@JvmField val wa: WebViewActivity) : Runnable {

    override fun run() {
        val webViewActivity = wa
        val menuItemCode = webViewActivity.dialogsState.menuItemCode
        val view = webViewActivity.dialogsState.menuItemView
        webViewActivity.dialogsState.menuItemView = null
        run sw16@{
when (menuItemCode) {
            1 -> {
                webViewActivity.closeMenuDialogs()
                if (webViewActivity.currentPage != null) {
                    if (!webViewActivity.dialogsState.touchBusy) {
                        webViewActivity.setAddressEditing(true)
                    } else {
                        webViewActivity.dialogsState.touchBusy = false
                    }
                    return@sw16
                }
            }

            2 -> webViewActivity.showMainMenu(view)
            3 -> {
                webViewActivity.closeMenuDialogs()
                if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    webViewActivity.closeHistoryDialog()
                    webViewActivity.pausePage()
                    val historyDialog = WebHistoryDialog(webViewActivity, WvaHistoryListListener(webViewActivity))
                    webViewActivity.historyDialog = historyDialog
                    historyDialog.setOnDismissListener(WvaHistoryDialogDismiss(webViewActivity))
                    return@sw16
                }
            }

            4 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.launchExternalForResult(
                    Intent(
                        webViewActivity.dialogsState.appContext,
                        com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>
                    ), RequestCode.DOWNLOAD_LIST
                )
            }

            5 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.addTabPlain(null, WebViewActivity.newTabUrl(), true, WvaTabAddDone(webViewActivity))
            }

            6 -> if (webViewActivity.currentPage != null) {
                webViewActivity.closeMenuDialogs()
                val hideImage = !ZoneSettings.hideImage.value
                ZoneSettings.edit(webViewActivity.dialogsState.appContext) { set(ZoneSettings.hideImage, hideImage) }
                webViewActivity.refreshButtonIcons(menuItemCode)
                webViewActivity.currentPage!!.setHideImage(ZoneSettings.hideImage.value)
                webViewActivity.reloadPage()
            }

            7 -> {
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    val url = webNestView.getUrl()
                    if (TextUtils.isEmpty(url) || "file:///android_asset/shortcut.html" == url || "about:blank" == url) {
                        showToast(webViewActivity, AppR.string.not_supported_page)
                    } else {
                        webViewActivity.closeMenuDialogs()
                        if (!webViewActivity.isBusy()) {
                            webViewActivity.openLoadDialogFor(url, false)
                        }
                    }
                }
            }

            8 -> {
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    val url = webNestView.getUrl()
                    if (TextUtils.isEmpty(url) || "file:///android_asset/shortcut.html" == url || "about:blank" == url) {
                        showToast(webViewActivity, AppR.string.not_supported_page)
                    } else {
                        webViewActivity.closeMenuDialogs()
                        if (!webViewActivity.isBusy()) {
                            webViewActivity.openEmgGallery(url, false)
                        }
                    }
                }
            }

            9 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    webViewActivity.closeCaptureModeSheet()
                    // Capture mode: "Normal mode" / "Scroll mode" (was a bare MyDialogBottom + SettingListAdapter).
                    val sheet = com.mycompany.app.ui.browser.ChoiceSheetDialog(
                        webViewActivity,
                        listOf(AppR.string.normal_mode, AppR.string.scroll_mode),
                    ) { index -> WvaSettingListener(webViewActivity).onSetting(null, index, false, 0) }
                    webViewActivity.captureModeSheet = sheet
                    sheet.setOnDismissListener(CaptureModeSheetDismissed(webViewActivity))
                    return@sw16
                }
            }

            10 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.launchExternalForResult(
                    Intent(
                        webViewActivity.dialogsState.appContext,
                        com.mycompany.app.ui.viewer.ImageEditActivity::class.java as Class<*>
                    ), RequestCode.EDIT_IMAGE
                )
            }

            11 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.showSetTransDialog()
            }

            12 -> if (webViewActivity.currentPage != null) {
                val currentUrl = webViewActivity.currentPageUrl(false)
                if (TextUtils.isEmpty(currentUrl) || "about:blank" == currentUrl) {
                    showToast(webViewActivity, AppR.string.blank_page)
                } else {
                    webViewActivity.closeMenuDialogs()
                    webViewActivity.openIntentUrl(currentUrl, webViewActivity.currentPage!!.getTitle())
                }
            }

            13 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.showEditShortDialog()
            }

            14 -> {
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    if ("file:///android_asset/shortcut.html" != webNestView.getUrl()) {
                        webViewActivity.closeMenuDialogs()
                        webViewActivity.openFindBar(null)
                    } else {
                        showToast(webViewActivity, AppR.string.not_supported_page)
                    }
                }
            }

            15 -> {
                val currentUrl = webViewActivity.currentPageUrl(false)
                if (TextUtils.isEmpty(currentUrl) || "file:///android_asset/shortcut.html" == currentUrl) {
                    showToast(webViewActivity, AppR.string.not_supported_page)
                    return@sw16
                } else {
                    webViewActivity.closeMenuDialogs()
                    if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                        val dialogDownPage = webViewActivity.downPageDialog
                        if (dialogDownPage != null) {
                            dialogDownPage.dismiss()
                            webViewActivity.downPageDialog = null
                        }
                        val downPageDialog = DialogDownPage(
                            webViewActivity,
                            currentUrl,
                            webViewActivity.pageTitleOf(webViewActivity.currentPage, currentUrl),
                            webViewActivity.currentPage!!.getFavicon(),
                            WvaDownPageListener(webViewActivity)
                        )
                        webViewActivity.downPageDialog = downPageDialog
                        downPageDialog.setOnDismissListener(WvaDownPageDialogDismiss(webViewActivity))
                        return@sw16
                    }
                }
            }

            16 -> {
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    if ("file:///android_asset/shortcut.html" == webNestView.getUrl()) {
                        showToast(webViewActivity, AppR.string.not_supported_page)
                        return@sw16
                    } else {
                        webViewActivity.closeMenuDialogs()
                        if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                            val dialogPrintPage = webViewActivity.printPageDialog
                            if (dialogPrintPage != null) {
                                dialogPrintPage.dismiss()
                                webViewActivity.printPageDialog = null
                            }
                            val printPageDialog = DialogPrintPage(
                                webViewActivity,
                                webViewActivity.pageTitleOf(webViewActivity.currentPage, webViewActivity.dialogsState.pageUrl),
                                webViewActivity.currentPage!!.getFavicon(),
                                PrintPagePathListener(webViewActivity)
                            )
                            webViewActivity.printPageDialog = printPageDialog
                            printPageDialog.setOnDismissListener(WvaPrintPageDialogDismissed(webViewActivity))
                            return@sw16
                        }
                    }
                }
            }

            17 -> if (webViewActivity.currentPage != null) {
                webViewActivity.closeMenuDialogs()
                if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetDesk = webViewActivity.dialogsState.setDeskDialog
                    if (dialogSetDesk != null) {
                        dialogSetDesk.dismiss()
                        webViewActivity.dialogsState.setDeskDialog = null
                    }
                    webViewActivity.dialogsState.deskModeBefore = webViewActivity.currentPage!!.isDesktopAgent()
                    val setDeskDialog = DialogSetDesk(
                        webViewActivity,
                        webViewActivity.currentPage!!.deskMode,
                        SetDeskDialogListener(webViewActivity)
                    )
                    webViewActivity.dialogsState.setDeskDialog = setDeskDialog
                    setDeskDialog.setOnDismissListener(WvaSetDeskDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            18 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSeekBright = webViewActivity.seekBrightDialog
                    if (dialogSeekBright != null) {
                        dialogSeekBright.dismiss()
                        webViewActivity.seekBrightDialog = null
                    }
                    val seekBrightDialog =
                        DialogSeekBright(webViewActivity, webViewActivity.getWindow(), 0, null)
                    webViewActivity.seekBrightDialog = seekBrightDialog
                    seekBrightDialog.setOnDismissListener(WvaSeekBrightDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            19 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSeekWebText = webViewActivity.seekWebTextDialog
                    if (dialogSeekWebText != null) {
                        dialogSeekWebText.dismiss()
                        webViewActivity.seekWebTextDialog = null
                    }
                    webViewActivity.updateZoomButton(false)
                    val dialog = DialogSeekWebText(
                        webViewActivity,
                        webViewActivity.currentPage,
                        WvaZoomButtonChanged(webViewActivity)
                    )
                    webViewActivity.seekWebTextDialog = dialog
                    dialog.setOnDismissListener(WvaSeekWebTextDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            20 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetFull = webViewActivity.setFullDialog
                    if (dialogSetFull != null) {
                        dialogSetFull.dismiss()
                        webViewActivity.setFullDialog = null
                    }
                    webViewActivity.dialogsState.showStatusBar = BrowserSettings.showStatus.value
                    webViewActivity.videoState.showNavBar = BrowserSettings.showNavi.value
                    webViewActivity.savedFixTop = BrowserSettings.fixTop.value
                    webViewActivity.savedFixBot = BrowserSettings.fixBot.value
                    webViewActivity.miscState.barsSettingApplyBusy = false
                    val setFullDialog =
                        DialogSetFull(webViewActivity, WvaBarsSettingApply(webViewActivity))
                    webViewActivity.setFullDialog = setFullDialog
                    setFullDialog.setOnDismissListener(WvaSetFullDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            21 -> {
                webViewActivity.closeMenuDialogs()
                if (!DbBookTab.isSaving) {
                    val secret = SyncSettings.secretMode.value
                    if (!webViewActivity.dialogsState.switchSecretModeBusy) {
                        webViewActivity.switchSecretMode(!secret)
                    } else {
                        showToast(webViewActivity, AppR.string.wait_retry)
                    }
                } else {
                    showToast(webViewActivity, AppR.string.wait_retry)
                }
            }

            22 -> if (webViewActivity.currentPage != null) {
                webViewActivity.closeMenuDialogs()
                val themeSys = BrowserSettings.themeSys.value
                val themeUi = BrowserSettings.themeUi.value
                val themeWeb = BrowserSettings.themeWeb.value
                val darkPage = MainUtil.isDarkPage(webViewActivity.onStartPage)
                val darkWeb = AppState.darkWeb
                val navBarColor = webViewActivity.navBarColorNow()
                if (AppState.darkUi && AppState.darkWeb) {
                    BrowserSettings.editLocal { set(BrowserSettings.themeSys, false) }
                    BrowserSettings.editLocal { set(BrowserSettings.themeUi, 0) }
                    BrowserSettings.editLocal { set(BrowserSettings.themeWeb, 0) }
                    AppState.darkUi = false
                    AppState.darkWeb = false
                } else {
                    BrowserSettings.editLocal { set(BrowserSettings.themeSys, false) }
                    BrowserSettings.editLocal { set(BrowserSettings.themeUi, 1) }
                    BrowserSettings.editLocal { set(BrowserSettings.themeWeb, 1) }
                    AppState.darkUi = true
                    AppState.darkWeb = true
                }
                if (themeSys != BrowserSettings.themeSys.value || themeUi != BrowserSettings.themeUi.value || themeWeb != BrowserSettings.themeWeb.value) {
                    val context = webViewActivity.dialogsState.appContext
                    val prefWebR: PrefWeb? = if (context != null) PrefWeb.get(context, false) else null
                    if (context != null && prefWebR != null) {
                        prefWebR.putBool("mThemeSys", BrowserSettings.themeSys.value)
                        prefWebR.putInt(BrowserSettings.themeUi.value, "mThemeUi")
                        prefWebR.putInt(BrowserSettings.themeWeb.value, "mThemeWeb")
                        prefWebR.saveAsync()
                    }
                    MainUtil.applyNightMode()
                }
                // only light / dark changes here; the chosen colours stay (coloured bars take their night tone)
                webViewActivity.applyDarkMode(navBarColor, darkPage, darkWeb)
                if (AppState.webViewHint.toInt() == 1 && ZtriSettings.guideDark.value && AppState.darkWeb && !darkWeb) {
                    webViewActivity.showDarkConfirmDialog()
                    return@sw16
                }
            }

            23 -> {
                webViewActivity.closeMenuDialogs()
                val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.lists.AlbumListActivity::class.java as Class<*>)
                intent.putExtra("EXTRA_TYPE", ListSettings.viewType.value)
                webViewActivity.startActivity(intent)
                webViewActivity.miscState.dataCachesDirty = true
            }

            25 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.finish()
            }

            26 -> if (webViewActivity.tabsState.currentFrame != null) {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isBusy()) {
                    webViewActivity.goBackFromBackKey(null)
                }
            }

            27 -> if (webViewActivity.tabsState.currentFrame != null) {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isBusy()) {
                    webViewActivity.startGoForward()
                }
            }

            28 -> {
                webViewActivity.closeMenuDialogs()
                // on the news page, Home goes back to the start page's shortcuts
                val newsPanel = if (webViewActivity.onStartPage) webViewActivity.currentPage?.quickPanel else null
                if (newsPanel != null && newsPanel.newsPageOpen) {
                    newsPanel.closeNewsPage()
                } else if (!ZoneSettings.homeClose.value) {
                    webViewActivity.openInCurrentPage(WebViewActivity.homePageUrl(), null)
                } else {
                    webViewActivity.rebuildTabs(true)
                }
            }

            29 -> {
                webViewActivity.closeMenuDialogs()
                if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    webViewActivity.closeBookmarksDialog()
                    val searchUrl = AlbumSettings.webBookDir.value
                    webViewActivity.pausePage()
                    val dialogWebBookList = WebBookmarksDialog(
                        webViewActivity,
                        searchUrl,
                        null,
                        0,
                        WvaBookListListener(webViewActivity)
                    )
                    webViewActivity.bookmarksDialog = dialogWebBookList
                    dialogWebBookList.infoListener = WvaBookInfoListener(webViewActivity)
                    dialogWebBookList.setOnDismissListener(WvaMainMenuItemDismiss(webViewActivity))
                    return@sw16
                }
            }

            30 -> if (webViewActivity.tabsState.currentFrame != null) {
                webViewActivity.closeMenuDialogs()
                if (webViewActivity.dialogsState.viewsCreated && !webViewActivity.isBusy()) {
                    if (!ZoneSettings.tabMiniMode.value) {
                        webViewActivity.showTabOverview()
                    } else {
                        webViewActivity.showMiniTabOverview()
                    }
                    return@sw16
                }
            }

            31 -> {
                val currentUrl = webViewActivity.currentPageUrl(false)
                if (!TextUtils.isEmpty(currentUrl) && "file:///android_asset/shortcut.html" != currentUrl) {
                    webViewActivity.closeMenuDialogs()
                    val readMode = ZtwoSettings.readMode.value
                    if (readMode == 2) {
                        webViewActivity.startReadTask(currentUrl)
                        return@sw16
                    } else if (readMode == 1) {
                        webViewActivity.showQuickAddDialog(currentUrl, null, false)
                        return@sw16
                    } else if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                        val dialogSetRead = webViewActivity.setReadDialog
                        if (dialogSetRead != null) {
                            dialogSetRead.dismiss()
                            webViewActivity.setReadDialog = null
                        }
                        val setReadDialog = DialogSetRead(
                            webViewActivity,
                            currentUrl,
                            WvaSetReadListener(webViewActivity)
                        )
                        webViewActivity.setReadDialog = setReadDialog
                        setReadDialog.setOnDismissListener(WvaSetReadDialogDismissed(webViewActivity))
                        return@sw16
                    }
                } else {
                    showToast(webViewActivity, AppR.string.not_supported_page)
                    return@sw16
                }
            }

            32 -> {
                val currentUrl = webViewActivity.currentPageUrl(false)
                if (TextUtils.isEmpty(currentUrl) || "file:///android_asset/shortcut.html" == currentUrl) {
                    showToast(webViewActivity, AppR.string.not_supported_page)
                    return@sw16
                } else {
                    webViewActivity.closeMenuDialogs()
                    if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                        webViewActivity.resumePausedPage()
                        val dialogViewSrc = webViewActivity.dialogsState.viewSrcDialog
                        if (dialogViewSrc != null) {
                            dialogViewSrc.dismiss()
                            webViewActivity.dialogsState.viewSrcDialog = null
                        }
                        webViewActivity.pausePage()
                        val webNestView = webViewActivity.currentPage
                        val viewSrcDialog = DialogViewSrc(
                            webViewActivity,
                            webNestView,
                            currentUrl,
                            webViewActivity.pageTitleOf(webNestView, currentUrl)
                        )
                        webViewActivity.dialogsState.viewSrcDialog = viewSrcDialog
                        viewSrcDialog.setOnDismissListener(WvaViewSrcDialogDismissed(webViewActivity))
                        return@sw16
                    }
                }
            }

            33 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.startActivity(
                    Intent(
                        webViewActivity.dialogsState.appContext,
                        com.mycompany.app.ui.lists.MemoListActivity::class.java as Class<*>
                    )
                )
            }

            34 -> webViewActivity.closeMenuDialogs()

            35 -> if (webViewActivity.currentPage != null) {
                webViewActivity.closeMenuDialogs()
                val webNestView = webViewActivity.currentPage
                val webCrashView = webNestView!!.crashView
                if (webCrashView != null) {
                    webNestView.removeCrashView(webCrashView.crashUrl)
                } else {
                    webViewActivity.onRefreshButton()
                }
            }

            36 -> {
                webViewActivity.closeMenuDialogs()
                val arrayList: ArrayList<WebTabItem?>? = if (webViewActivity.tabsState.tabSwipeState.toInt() == 0) webViewActivity.tabsState.tabItems else null
                if (webViewActivity.tabsState.tabSwipeState.toInt() == 0 && arrayList != null && arrayList.size >= 2) {
                    val myWebCoord: MyWebCoord? = if ((ZtwoSettings.tabLoop.value || webViewActivity.currentTabIndex > 0)) webViewActivity.webCoord else null
                    if ((ZtwoSettings.tabLoop.value || webViewActivity.currentTabIndex > 0) && myWebCoord != null) {
                        myWebCoord.post(WvaSwipeToPrevTab(webViewActivity))
                    }
                    return@sw16
                }
            }

            37 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.goToNextTab()
            }

            38 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.previewPrevTab()
            }

            39 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.showDeleteTabsDialog(0, 0, 0)
            }

            40 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetPrivacy = webViewActivity.dialogsState.setPrivacyDialog
                    if (dialogSetPrivacy != null) {
                        dialogSetPrivacy.dismiss()
                        webViewActivity.dialogsState.setPrivacyDialog = null
                    }
                    val setPrivacyDialog = DialogSetPrivacy(
                        webViewActivity,
                        false,
                        true,
                        WvaTabDeletedListener(webViewActivity)
                    )
                    webViewActivity.dialogsState.setPrivacyDialog = setPrivacyDialog
                    setPrivacyDialog.setOnDismissListener(WvaSetPrivacyDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            41 -> {}

            42 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.startActivity(
                    Intent(
                        webViewActivity.dialogsState.appContext,
                        com.mycompany.app.ui.lists.UserAgentListActivity::class.java as Class<*>
                    )
                )
            }

            43 -> if (webViewActivity.currentPage != null) {
                val currentUrl = webViewActivity.currentPageUrl(false)
                if (!TextUtils.isEmpty(currentUrl)) {
                    webViewActivity.closeMenuDialogs()
                    copyTextToast(webViewActivity, AppR.string.copied_clipboard, "Copied URL", currentUrl)
                } else {
                    showToast(webViewActivity, AppR.string.blank_page)
                }
            }

            44 -> if (webViewActivity.currentPage != null) {
                if (!webViewActivity.dialogsState.touchBusy) {
                    webViewActivity.closeMenuDialogs()
                    webViewActivity.setAddressEditing(true)
                } else {
                    webViewActivity.dialogsState.touchBusy = false
                }
            }

            45 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.showSetUrlDialog(true)
            }

            46 -> if (!webViewActivity.isPortraitSingleWindow()) {
                showToast(webViewActivity, AppR.string.not_support_land)
            } else {
                webViewActivity.closeMenuDialogs()
                if (!TtsSettings.handMode.value) {
                    webViewActivity.enterHandMode()
                } else {
                    webViewActivity.exitHandMode()
                }
            }

            47 -> {
                val currentUrl = webViewActivity.currentPageUrl(false)
                if (TextUtils.isEmpty(currentUrl) || "file:///android_asset/shortcut.html" == currentUrl) {
                    showToast(webViewActivity, AppR.string.not_supported_page)
                } else {
                    webViewActivity.closeMenuDialogs()
                    webViewActivity.applyTextZoom()
                }
            }

            48 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetCookie = webViewActivity.dialogsState.setCookieDialog
                    if (dialogSetCookie != null) {
                        dialogSetCookie.dismiss()
                        webViewActivity.dialogsState.setCookieDialog = null
                    }
                    val setCookieDialog =
                        DialogSetCookie(webViewActivity, WvaApplyViewCookie(webViewActivity))
                    webViewActivity.dialogsState.setCookieDialog = setCookieDialog
                    setCookieDialog.setOnDismissListener(WvaSetCookieDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            49 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetJava = webViewActivity.setJavaDialog
                    if (dialogSetJava != null) {
                        dialogSetJava.dismiss()
                        webViewActivity.setJavaDialog = null
                    }
                    val setJavaDialog = DialogSetJava(
                        webViewActivity,
                        webViewActivity.currentPageUrl(false),
                        WvaJsToggleListener(webViewActivity)
                    )
                    webViewActivity.setJavaDialog = setJavaDialog
                    setJavaDialog.setOnDismissListener(WvaSetJavaDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            50 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.startActivity(
                    Intent(
                        webViewActivity.dialogsState.appContext,
                        com.mycompany.app.ui.viewer.TextViewerActivity::class.java as Class<*>
                    )
                )
            }

            51 -> {
                webViewActivity.closeMenuDialogs()
                val barItemView = webViewActivity.barItemView
                if (barItemView == null) {
                    webViewActivity.showSetUrlDialog(false)
                } else {
                    if (webViewActivity.addressMenu == null) {
                        webViewActivity.closeAddressMenu()
                        webViewActivity.dialogsState.addressMenuText = null
                        val items: ArrayList<Any?> = ArrayList<Any?>()
                        // icons as in the address dialog (DialogSetUrl), for quicker scanning (owner, 2026-11-20)
                        val darkMenu = MainUtil.isDarkPage(webViewActivity.onStartPage)
                        fun icon(item: PopMenuItem, black: Int, dark: Int) = item.also { it.iconRes = if (darkMenu) dark else black }
                        items.add(icon(PopMenuItem(0, AppR.string.copy_url), AppR.drawable.outline_link_black_24, AppR.drawable.outline_link_dark_24))
                        items.add(icon(PopMenuItem(1, AppR.string.share_url), AppR.drawable.outline_share_black_24, AppR.drawable.outline_share_dark_24))
                        items.add(icon(PopMenuItem(2, AppR.string.paste_url), AppR.drawable.outline_content_paste_black_24, AppR.drawable.outline_content_paste_dark_24))
                        items.add(icon(PopMenuItem(3, AppR.string.open_copied_url), AppR.drawable.outline_open_in_new_black_24, AppR.drawable.outline_open_in_new_dark_24))
                        items.add(icon(PopMenuItem(4, AppR.string.open_new_tab), AppR.drawable.outline_tab_new_right_black_24, AppR.drawable.outline_tab_new_right_dark_24))
                        items.add(icon(PopMenuItem(5, AppR.string.open_with), AppR.drawable.outline_apps_black_24, AppR.drawable.outline_apps_dark_24))
                        val webNestView = webViewActivity.currentPage
                        if (webNestView != null) {
                            val url = webNestView.getUrl()
                            if (!TextUtils.isEmpty(url) && ("file:///android_asset/shortcut.html" != url) && ("about:blank" != url)) {
                                items.add(
                                    icon(PopMenuItem(
                                        6,
                                        webViewActivity.getString(AppR.string.delete_cookie) + " (" + stripWww(
                                            webViewActivity.dialogsState.pageHost
                                        ) + ")"
                                    ), AppR.drawable.outline_delete_sweep_black_24, AppR.drawable.outline_delete_sweep_dark_24)
                                )
                                if (url!!.startsWith("https://")) {
                                    items.add(icon(PopMenuItem(7, AppR.string.view_certi), AppR.drawable.outline_verified_user_black_24, AppR.drawable.outline_verified_user_dark_24))
                                }
                            }
                        }
                        val myPopupMenu = MyPopupMenu(
                            webViewActivity,
                            webViewActivity.dialogsState.webBody,
                            barItemView,
                            items,
                            MainUtil.isDarkPage(webViewActivity.onStartPage),
                            WvaAddressMenuListener(webViewActivity)
                        )
                        // speech bubble pointing at the address bar, so it is clear where the menu belongs (2026-11-20)
                        myPopupMenu.bubble = true
                        webViewActivity.addressMenu = myPopupMenu
                        webViewActivity.popupMenu = myPopupMenu
                        val myWebCoord = webViewActivity.webCoord
                        if (myWebCoord != null) {
                            myWebCoord.post(WvaAddressMenuClosed(webViewActivity))
                        }
                    }
                    webViewActivity.barItemView = null
                }
            }

            52 -> webViewActivity.runInBackground(WvaPasteMenuUrlTask(webViewActivity))
            53 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.dialogsState.menuItemBusy) {
                    webViewActivity.dialogsState.menuItemBusy = true
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord != null) {
                        myWebCoord.post(WvaVoiceSearch(webViewActivity))
                    }
                }
            }

            54 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetScrFil = webViewActivity.dialogsState.setScrFilDialog
                    if (dialogSetScrFil != null) {
                        dialogSetScrFil.dismiss()
                        webViewActivity.dialogsState.setScrFilDialog = null
                    }
                    val setScrFilDialog =
                        DialogSetScrFil(webViewActivity, WvaNavColorSeek(webViewActivity))
                    webViewActivity.dialogsState.setScrFilDialog = setScrFilDialog
                    setScrFilDialog.setOnDismissListener(WvaSetScrFilDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            55 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.pageScroll(true, true)
            }

            56 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.pageScroll(false, true)
            }

            57 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.pageScroll(true, false)
            }

            58 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.pageScroll(false, false)
            }

            59 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.showDeleteTabsDialog(0, webViewActivity.currentTabIndex, 1)
            }

            60 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetUseTts = webViewActivity.dialogsState.setUseTtsDialog
                    if (dialogSetUseTts != null) {
                        dialogSetUseTts.dismiss()
                        webViewActivity.dialogsState.setUseTtsDialog = null
                    }
                    val setUseTtsDialog =
                        DialogSetUseTts(webViewActivity, WvaTtsSettingApply(webViewActivity))
                    webViewActivity.dialogsState.setUseTtsDialog = setUseTtsDialog
                    setUseTtsDialog.setOnDismissListener(WvaUseTtsDialogDismiss(webViewActivity))
                    return@sw16
                }
            }

            61 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.startActivity(
                    Intent(
                        webViewActivity.dialogsState.appContext,
                        com.mycompany.app.ui.settings.GestureSettingsActivity::class.java as Class<*>
                    )
                )
            }

            62 -> if (webViewActivity.openAdblockDialog()) return@sw16

            63 -> {
                webViewActivity.closeMenuDialogs()
                if (webViewActivity.currentPage != null && !webViewActivity.dialogsState.bookmarkEditBusy) {
                    webViewActivity.dialogsState.bookmarkEditBusy = true
                    if (!webViewActivity.dialogsState.barsTinted) {
                        webViewActivity.showBookmarkEditDialog()
                        webViewActivity.dialogsState.bookmarkEditBusy = false
                    } else {
                        val menuUrl = webViewActivity.currentPageUrl(false)
                        if (!TextUtils.isEmpty(menuUrl)) {
                            webViewActivity.runInBackground(WvaMainMenuItemTask(webViewActivity, menuItemText = menuUrl))
                        } else {
                            webViewActivity.tintBars(false)
                            webViewActivity.dialogsState.bookmarkEditBusy = false
                            notifyBookUpdate(webViewActivity.dialogsState.appContext, false)
                        }
                    }
                    return@sw16
                }
            }

            64 -> {
                webViewActivity.closeMenuDialogs()
                if (!ReadSettings.guideDown.value) {
                    webViewActivity.runDownAction(0)
                } else {
                    webViewActivity.showDownGuideDialog()
                }
            }

            65 -> {
                webViewActivity.closeMenuDialogs()
                val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
                val intent = Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.settings.MainSettingsActivity::class.java as Class<*>)
                intent.putExtra("EXTRA_PATH", checkedUrl)
                webViewActivity.launchExternalForResult(intent, RequestCode.MAIN_SETTINGS)
            }

            66 -> {
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    val dialogSetDark = webViewActivity.dialogsState.setDarkDialog
                    if (dialogSetDark != null) {
                        dialogSetDark.dismiss()
                        webViewActivity.dialogsState.setDarkDialog = null
                    }
                    webViewActivity.snapshotDisplayPrefs(MainUtil.isDarkPage(webViewActivity.onStartPage))
                    val setDarkDialog =
                        DialogSetDark(webViewActivity, WvaSetDarkListener(webViewActivity))
                    webViewActivity.dialogsState.setDarkDialog = setDarkDialog
                    setDarkDialog.setOnDismissListener(WvaSetDarkDialogDismissed(webViewActivity))
                    return@sw16
                }
            }

            67 -> if (webViewActivity.currentPage != null) {
                webViewActivity.downloadLink(webViewActivity.currentPageUrl(false))
            }

            69 -> {
                val currentUrl = webViewActivity.currentPageUrl(false)
                if (TextUtils.isEmpty(currentUrl) || "file:///android_asset/shortcut.html" == currentUrl) {
                    showToast(webViewActivity, AppR.string.not_supported_page)
                } else {
                    webViewActivity.closeMenuDialogs()
                    webViewActivity.showQuickAddDialog(currentUrl, null, true)
                }
            }

            70 -> {
                webViewActivity.closeMenuDialogs()
                if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
                    webViewActivity.closeTabMenuDialog()
                    var theme = AppR.style.DialogExpandTheme
                    if (webViewActivity.isLandscapeScreen()) {
                        theme = 0
                    }
                    val tabItems = webViewActivity.tabsState.tabItems
                    val dialogTabMenu = DialogTabMenu(
                        webViewActivity,
                        theme,
                        tabItems != null && tabItems.size > 1,
                        WvaShowSetTmemDialog(webViewActivity)
                    )
                    webViewActivity.tabMenuDialog = dialogTabMenu
                    dialogTabMenu.setOnDismissListener(TabMenuDialogDismissed(webViewActivity))
                    if (MainConst.isApi30Insets || theme != 0) {
                        webViewActivity.tabMenuDialog!!.applyColors(
                            webViewActivity.onStartPage,
                            webViewActivity.useDarkWindow(),
                            webViewActivity.pageThemeColor(),
                            webViewActivity.navBarColorNow(),
                            true
                        )
                    }
                    return@sw16
                }
            }

            71 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.openInNewTab(WebViewActivity.newTabUrl(), WvaTabAddDone(webViewActivity))
            }

            72 -> {
                webViewActivity.closeMenuDialogs()
                webViewActivity.createBackTab(WebViewActivity.newTabUrl())
            }

            74 -> {
                // Workspaces: pick Main, a workspace or a new one (WorkspaceSheet)
                webViewActivity.closeMenuDialogs()
                if (!webViewActivity.isClosing) com.mycompany.app.ui.browser.WorkspaceSheet.show(webViewActivity)
                return@sw16
            }

            73 -> {
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    val url = webNestView.getUrl()
                    if (TextUtils.isEmpty(url) || "file:///android_asset/shortcut.html" == url || "about:blank" == url) {
                        showToast(webViewActivity, AppR.string.not_supported_page)
                    } else {
                        webViewActivity.closeMenuDialogs()
                        webViewActivity.addAreaToolbar(false)
                    }
                }
            }
        }
}
    }
}

class WvaPasteMenuUrlTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class OpenMenuUrlStep(
                                         @JvmField val url: String?
    ) : Runnable {
        override fun run() {
            val url = this.url
            val urlEmpty = TextUtils.isEmpty(url)
            val webViewActivity = wa
            if (urlEmpty) {
                showToast(webViewActivity, AppR.string.empty)
                return
            }
            webViewActivity.closeMenuDialogs()
            webViewActivity.openInCurrentPage(url, null)
        }
    }

    override fun run() {
        val webViewActivity = wa
        val clipText = MainUtil.clipboardText(webViewActivity.dialogsState.appContext)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenMenuUrlStep(clipText))
    }
}

class UrlLinkPreviewListener(@JvmField val wa: WebViewActivity) : PreviewListener {
    override fun copyUrl(url: String?) {
        copyTextToast(wa, AppR.string.copied_clipboard, "Copied URL", url)
    }

    override fun openFile(url: String?, size: Long, isVideo: Boolean) {
        val webViewActivity = wa
        webViewActivity.closePreImageDialog()
        webViewActivity.closeUrlLinkDialog()
        webViewActivity.requestImagePreview(url, null, false)
    }

    override fun download(url: String?, mimeType: String?) {
        val webViewActivity = wa
        val linkActionUrl = webViewActivity.dialogsState.linkActionUrl
        webViewActivity.closePreImageDialog()
        webViewActivity.closeUrlLinkDialog()
        webViewActivity.requestSetDownDialog(url, linkActionUrl, mimeType)
    }

    override fun share(url: String?) {
        val webViewActivity = wa
        val linkActionUrl = webViewActivity.dialogsState.linkActionUrl
        webViewActivity.closePreImageDialog()
        webViewActivity.closeUrlLinkDialog()
        webViewActivity.openIntentUrl(url, linkActionUrl)
    }

    override fun openWith(url: String?, mimeType: String?) {
        val webViewActivity = wa
        webViewActivity.closePreImageDialog()
        val dialogUrlLink = webViewActivity.urlLinkDialog
        if (dialogUrlLink != null) {
            dialogUrlLink.loadImageData(true)
        }
    }
}

class WvaSeekBrightDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSeekBright = webViewActivity.seekBrightDialog
        if (dialogSeekBright != null) {
            dialogSeekBright.dismiss()
            webViewActivity.seekBrightDialog = null
        }
    }
}

class WvaSeekWebTextDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSeekWebText = webViewActivity.seekWebTextDialog
        if (dialogSeekWebText != null) {
            dialogSeekWebText.dismiss()
            webViewActivity.seekWebTextDialog = null
        }
    }
}

class WvaSetFullDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetFull = webViewActivity.setFullDialog
        if (dialogSetFull != null) {
            dialogSetFull.dismiss()
            webViewActivity.setFullDialog = null
        }
    }
}

class WvaSetDarkListener(@JvmField val wa: WebViewActivity) : SetDarkListener {

    internal inner class ApplyDarkSettingsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val darkSettingsChanged = webViewActivity.dialogsState.darkSettingsChanged
            webViewActivity.dialogsState.darkSettingsChanged = false
            if (darkSettingsChanged) {
                MainUtil.applyNightMode()
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaApplyDarkSetting(webViewActivity))
        }
    }

    override fun onChanged(changed: Boolean) {
        val webViewActivity = wa
        webViewActivity.dialogsState.darkSettingsChanged = changed
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ApplyDarkSettingsStep())
    }
}

class WvaSetDarkDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetDark = webViewActivity.dialogsState.setDarkDialog
        if (dialogSetDark != null) {
            dialogSetDark.dismiss()
            webViewActivity.dialogsState.setDarkDialog = null
        }
    }
}

class WvaSetAdblockDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetAdblock = webViewActivity.setAdblockDialog
        if (dialogSetAdblock != null) {
            dialogSetAdblock.dismiss()
            webViewActivity.setAdblockDialog = null
        }
    }
}

class WvaSetJavaDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetJava = webViewActivity.setJavaDialog
        if (dialogSetJava != null) {
            dialogSetJava.dismiss()
            webViewActivity.setJavaDialog = null
        }
    }
}

class WvaSetTmemDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetTmem = webViewActivity.setTmemDialog
        if (dialogSetTmem != null) {
            dialogSetTmem.dismiss()
            webViewActivity.setTmemDialog = null
        }
    }
}

class WvaSetReadListener(@JvmField val wa: WebViewActivity) : SetReadListener {
    override fun onPick(url: String?, startRead: Boolean) {
        val webViewActivity = wa
        val dialogSetRead = webViewActivity.setReadDialog
        if (dialogSetRead != null) {
            dialogSetRead.dismiss()
            webViewActivity.setReadDialog = null
        }
        if (startRead) {
            webViewActivity.startReadTask(url)
        } else {
            webViewActivity.showQuickAddDialog(url, null, false)
        }
    }
}

class WvaSetReadDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetRead = webViewActivity.setReadDialog
        if (dialogSetRead != null) {
            dialogSetRead.dismiss()
            webViewActivity.setReadDialog = null
        }
    }
}

class WvaStartReadListener(@JvmField val wa: WebViewActivity) : ValueCallback<String> {

    internal inner class StartReadStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val startReadListenerText = webViewActivity.dialogsState.startReadListenerText
            webViewActivity.dialogsState.startReadListenerText = null
            if (webViewActivity.currentPage != null) {
                val readText = unescapeJavaText(stripQuotes(startReadListenerText))
                val webReadTask = webViewActivity.dialogsState.readTask
                if (webReadTask != null) {
                    webReadTask.startLoad(readText)
                }
            }
        }
    }

    override fun onReceiveValue(value: String?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.startReadListenerText = value
        webViewActivity.runInBackground(StartReadStep())
    }
}

class WvaSetPrivacyDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetPrivacy = webViewActivity.dialogsState.setPrivacyDialog
        if (dialogSetPrivacy != null) {
            dialogSetPrivacy.dismiss()
            webViewActivity.dialogsState.setPrivacyDialog = null
        }
    }
}

class WvaSetCookieDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetCookie = webViewActivity.dialogsState.setCookieDialog
        if (dialogSetCookie != null) {
            dialogSetCookie.dismiss()
            webViewActivity.dialogsState.setCookieDialog = null
        }
    }
}

class WvaApplySettingsChangeTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ApplyTtsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.applyTtsSettings()
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.applyBodyColors(webViewActivity.pageThemeColor(), webViewActivity.navBarColorNow())
        webViewActivity.updateSecureFlag(false)
        if (SyncSettings.secretMode.value) {
            lockAppData(webViewActivity.dialogsState.appContext)
        }
        if (webViewActivity.dialogsState.setUseTtsDialog == null) {
            if (TtsSettings.ttsMode.value) {
                webViewActivity.runInBackground(ApplyTtsStep())
            } else {
                webViewActivity.shutdownTts()
            }
        }
    }
}

class WvaSetScrFilDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetScrFil = webViewActivity.dialogsState.setScrFilDialog
        if (dialogSetScrFil != null) {
            dialogSetScrFil.dismiss()
            webViewActivity.dialogsState.setScrFilDialog = null
        }
    }
}

class WvaCertDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogWebCerti = webViewActivity.dialogsState.certDialog
        if (dialogWebCerti != null) {
            dialogWebCerti.dismiss()
            webViewActivity.dialogsState.certDialog = null
        }
    }
}

class WvaComicDetectScript(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val selector: String?
        val webViewActivity = wa
        val webStyleListenerBusy = webViewActivity.miscState.webStyleListenerBusy
        if (webStyleListenerBusy) {
            selector = if (webStyleListenerBusy) "div[class='mobile-navbar-inner']" else null
        } else {
            selector = "span[class='page-desc']"
        }
        if (selector == null) {
            return
        }
        val sbN = StringBuilder(("function myComic(doc){if(!doc)return false;var ele=doc.querySelector(\"")).append(selector).append("\");if(!ele)return false;")
        if (!webStyleListenerBusy) {
            sbN.append("if(ele.parentNode.innerHTML.includes('Captcha'))return true;")
        }
        sbN.append("android.onComicDet(")
        sbN.append(if (webStyleListenerBusy) 1 else 0)
        sbN.append(");return true;}(function(){if(myComic(document))return;if(!document.documentElement||window.sb_comic_osv)return;window.sb_comic_osv=new MutationObserver(e=>{for(var i=0;i<e.length;i++){var tgt=e[i].target;if(myComic(tgt)){if(window.sb_comic_osv){window.sb_comic_osv.disconnect();window.sb_comic_osv=null;}break;}if(tgt.tagName=='BODY')break;}}),window.sb_comic_osv.observe(document.documentElement,{childList:!0,subtree:!0});})();")
        evalJs(webViewActivity.currentPage, sbN.toString(), true)
    }
}

class WvaAddRssButton(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class RssButtonListener : FltViewListener {
        override fun onTap(view: View?, button: Int) {
            val webViewActivity = wa
            val webFltView = webViewActivity.floatingState.rssButton
            if (webFltView != null) {
                if (webFltView.isLoading) {
                    showToast(webViewActivity, AppR.string.wait_retry)
                    return
                }
                if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen() || webViewActivity.newsMenuOpen) {
                    return
                }
                webViewActivity.closeNewsMenuDialog()
                val rssButton = webViewActivity.floatingState.rssButton
                if (rssButton != null) {
                    rssButton.setHideBlocked(true)
                }
                webViewActivity.newsMenuOpen = false
                webViewActivity.newsMenuDialog = DialogNewsMenu(
                    webViewActivity,
                    webViewActivity.dialogsState.webBody,
                    view,
                    webViewActivity.tabsState.layoutRtl,
                    WvaShowNewsSearchDialog(webViewActivity)
                )
            }
        }

        override fun onGone() {
            val webViewActivity = wa
            webViewActivity.hideMoveHint()
        }

        override fun onLongPress(view: View?) {
            val webViewActivity = wa
            webViewActivity.showMoveHint(view)
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.floatingState.rssButton != null || webViewActivity.dialogsState.webBody == null) {
            return
        }
        try {
            val webFltView = WebFltView(webViewActivity, 1)
            webViewActivity.floatingState.rssButton = webFltView
            webFltView.refreshColors()
            if (ZtwoSettings.quickDown.value || !webViewActivity.rssButtonAllowed()) {
                webViewActivity.floatingState.rssButton!!.setVisibility(View.GONE)
            } else {
                if (webViewActivity.quickState.quickNews != null) {
                    webViewActivity.setRssLoading(true)
                }
                webViewActivity.floatingState.rssButton!!.setVisibility(View.VISIBLE)
            }
            webViewActivity.floatingState.rssButton!!.setFltListener(RssButtonListener())
            val myWebBody = webViewActivity.dialogsState.webBody
            val rssButton = webViewActivity.floatingState.rssButton
            val width = MainApp.dp48
            wa.floatingState.overlays!!.addView(rssButton, width, width)
        } catch (unused: Exception) {
        }
    }
}

class WvaShowOpenTypeDialog(@JvmField val wa: WebViewActivity, @JvmField val openTypeId: Long = 0, @JvmField val openKind: Int = 0, @JvmField val withoutFile: Boolean = false, @JvmField val path: String? = null, @JvmField val mime: String? = null) : Runnable {

    internal inner class ClearSnackbarListener : SnackbarListener {
        override fun onSecond() {
        }

        override fun onDismissed() {
        }

        override fun onFirst() {
        }

        override fun onDismiss() {
            wa.snackbar = null
        }
    }

    internal inner class InvalidUrlSnackbar : SnackbarListener {
        override fun onSecond() {
        }

        override fun onDismissed() {
        }

        override fun onFirst() {
        }

        override fun onDismiss() {
            wa.snackbar = null
        }
    }

    internal inner class LiveFailSnackbar : SnackbarListener {

        internal inner class RetryOpenTypeStep : Runnable {
            override fun run() {
                val webViewActivity = wa
                val openTypeId = webViewActivity.dialogsState.openTypeId
                val mainAppO = MainApp.from(webViewActivity.dialogsState.appContext)
                if (mainAppO != null) {
                    mainAppO.sendDownAction(openTypeId)
                }
            }
        }

        override fun onSecond() {
        }

        override fun onDismissed() {
        }

        override fun onFirst() {
            val webViewActivity = wa
            if (webViewActivity.dialogsState.openTypeId == -1L) {
                return
            }
            webViewActivity.runInBackground(RetryOpenTypeStep())
        }

        override fun onDismiss() {
            wa.snackbar = null
        }
    }

    internal inner class OpenWithSnackbar : SnackbarListener {
        override fun onSecond() {
            val webViewActivity = wa
            val openTypeUrl = webViewActivity.dialogsState.openTypeUrl
            val showOpenTypeDialogText = webViewActivity.dialogsState.showOpenTypeDialogText
            webViewActivity.dialogsState.openTypeUrl = null
            webViewActivity.dialogsState.showOpenTypeDialogText = null
            if (openWithApp(webViewActivity, openTypeUrl, showOpenTypeDialogText, false, false)) {
                webViewActivity.dialogsState.leavingByIntent = true
                return
            }
            if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
                return
            }
            val dialogOpenType = webViewActivity.openTypeDialog
            if (dialogOpenType != null) {
                dialogOpenType.dismiss()
                webViewActivity.openTypeDialog = null
            }
            var theme = AppR.style.DialogExpandTheme
            if (webViewActivity.isLandscapeScreen()) {
                theme = 0
            }
            val openTypeDialog = DialogOpenType(webViewActivity, theme, openTypeUrl, false)
            webViewActivity.openTypeDialog = openTypeDialog
            openTypeDialog.setOnDismissListener(WvaOpenTypeDialogDismissed(webViewActivity))
            if (MainConst.isApi30Insets || theme != 0) {
                webViewActivity.openTypeDialog!!.applyColors(
                    webViewActivity.onStartPage,
                    webViewActivity.useDarkWindow(),
                    webViewActivity.pageThemeColor(),
                    webViewActivity.navBarColorNow(),
                    true
                )
            }
        }

        override fun onDismissed() {
        }

        override fun onFirst() {
            val webViewActivity = wa
            val openTypeId = webViewActivity.dialogsState.openTypeId
            val openTypeUrl = webViewActivity.dialogsState.openTypeUrl
            webViewActivity.dialogsState.openTypeUrl = null
            val context = webViewActivity.dialogsState.appContext
            if (context == null) {
                return
            }
            val intent = Intent(context, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
            if (openTypeId > 0) {
                intent.putExtra("EXTRA_ID", openTypeId)
            } else {
                intent.putExtra("EXTRA_PATH", openTypeUrl)
            }
            webViewActivity.launchExternalForResult(intent, RequestCode.DOWNLOAD_LIST)
        }

        override fun onDismiss() {
            val webViewActivity = wa
            webViewActivity.snackbar = null
            webViewActivity.dialogsState.openTypeUrl = null
            webViewActivity.dialogsState.showOpenTypeDialogText = null
        }
    }

    override fun run() {
        val webViewActivity = wa
        val openTypeId = this@WvaShowOpenTypeDialog.openTypeId
        val openKind = this@WvaShowOpenTypeDialog.openKind
        val path = this@WvaShowOpenTypeDialog.path
        val mime = this@WvaShowOpenTypeDialog.mime
        val withoutFile = this@WvaShowOpenTypeDialog.withoutFile
        if (webViewActivity.webCoord != null) {
            webViewActivity.updateUpButton(false)
            webViewActivity.closeAllSnackbars()
            val mySnackbar = MySnackbar(webViewActivity)
            webViewActivity.snackbar = mySnackbar
            mySnackbar.setAppBarView(webViewActivity.barScroll)
            if (TextUtils.isEmpty(path)) {
                val saveFail = if (withoutFile) AppR.string.save_fail else AppR.string.server_error
                if (webViewActivity.videoFrame != null) {
                    AppState.darkUi = true
                }
                webViewActivity.snackbar!!.showSingle(
                    webViewActivity.webCoord,
                    saveFail,
                    ClearSnackbarListener()
                )
                if (webViewActivity.videoFrame != null) {
                    applyScreenRotation(webViewActivity, false)
                    return
                }
                return
            }
            if (openKind == 5) {
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    webNestView.addFailedImage(path)
                }
                if (webViewActivity.videoFrame != null) {
                    AppState.darkUi = true
                }
                webViewActivity.snackbar!!.showSingle(
                    webViewActivity.webCoord,
                    AppR.string.invalid_url,
                    InvalidUrlSnackbar()
                )
                if (webViewActivity.videoFrame != null) {
                    applyScreenRotation(webViewActivity, false)
                    return
                }
                return
            }
            if (openKind == 4) {
                val webNestView = webViewActivity.currentPage
                if (webNestView != null) {
                    webNestView.addFailedImage(path)
                }
                val saveSuccess = if ("live" == path) AppR.string.live_fail else AppR.string.server_error
                if (webViewActivity.videoFrame != null) {
                    AppState.darkUi = true
                }
                webViewActivity.dialogsState.openTypeId = openTypeId
                webViewActivity.snackbar!!.showWithButton(
                    webViewActivity.webCoord,
                    saveSuccess,
                    5,
                    LiveFailSnackbar()
                )
                if (webViewActivity.videoFrame != null) {
                    applyScreenRotation(webViewActivity, false)
                    return
                }
                return
            }
            val moveHintX = if (withoutFile) AppR.string.save_success else AppR.string.down_complete
            if (webViewActivity.videoFrame != null) {
                AppState.darkUi = true
            }
            webViewActivity.dialogsState.openTypeId = openTypeId
            webViewActivity.dialogsState.openTypeUrl = path
            webViewActivity.dialogsState.showOpenTypeDialogText = mime
            val snackbar = webViewActivity.snackbar
            val myWebCoord = webViewActivity.webCoord
            val downloadDoneSnackbar: OpenWithSnackbar = OpenWithSnackbar()
            snackbar!!.snackType = 0.toByte()
            snackbar.show(myWebCoord, moveHintX, 2, 1, downloadDoneSnackbar)
            if (webViewActivity.videoFrame != null) {
                applyScreenRotation(webViewActivity, false)
            }
        }
    }
}

class WvaPageAdScripts(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val currentPage = webViewActivity.currentPage
        if (currentPage != null) {
            if (TtsSettings.checkOver.value != 0) {
                injectOverlayJs(currentPage)
            }
            val webNestView: WebNestView? = if (webViewActivity.onStartPage || !BrowserSettings.adsBlock.value) null else webViewActivity.currentPage
            if (webViewActivity.onStartPage || !BrowserSettings.adsBlock.value || webNestView == null) {
                return
            }
            webNestView.startAdsClean(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, true)
        }
    }
}

class WvaOfferLinkTitle(@JvmField val wa: WebViewActivity, @JvmField val linkTitleText: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val linkTitleText = this@WvaOfferLinkTitle.linkTitleText
        val webNestView: WebNestView? = if (TextUtils.isEmpty(linkTitleText) || webViewActivity.videoState.longPressPending || webViewActivity.dialogsState.longPressBusy || webViewActivity.urlLinkDialog != null || webViewActivity.dialogsState.actionMode != null) null else webViewActivity.currentPage
        if (TextUtils.isEmpty(linkTitleText) || webViewActivity.videoState.longPressPending || webViewActivity.dialogsState.longPressBusy || webViewActivity.urlLinkDialog != null || webViewActivity.dialogsState.actionMode != null || webNestView == null) {
            return
        }
        webNestView.swipeTouch = true
        if (webViewActivity.isClosing || webViewActivity.videoState.linkPillBusy) {
            return
        }
        webViewActivity.videoState.linkPillBusy = true
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaShowLinkTitlePill(webViewActivity, linkPillText = linkTitleText))
    }
}

class WvaOfferAreaMenu(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        if (BrowserSettings.areaBlock.value) {
            val webViewActivity = wa
            val webNestView: WebNestView? = if (webViewActivity.videoState.longPressPending || webViewActivity.dialogsState.longPressBusy || webViewActivity.urlLinkDialog != null || webViewActivity.dialogsState.actionMode != null) null else webViewActivity.currentPage
            if (webViewActivity.videoState.longPressPending || webViewActivity.dialogsState.longPressBusy || webViewActivity.urlLinkDialog != null || webViewActivity.dialogsState.actionMode != null || webNestView == null) {
                return
            }
            webNestView.swipeTouch = true
            if (webViewActivity.isClosing || webViewActivity.videoState.areaMenuBusy) {
                return
            }
            webViewActivity.videoState.areaMenuBusy = true
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaAddAreaMenu(webViewActivity))
        }
    }
}

class WvaLongPressInspect2(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val dialogUrlLink = wa.urlLinkDialog
        if (dialogUrlLink != null) {
            dialogUrlLink.updateVideoLabel()
        }
    }
}

class WvaShowTransResult(@JvmField val wa: WebViewActivity, @JvmField val transText: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val transText = this@WvaShowTransResult.transText
        val actionModeMenuClickText = webViewActivity.dialogsState.actionModeMenuClickText
        webViewActivity.dialogsState.actionModeMenuClickText = null
        if (webViewActivity.currentPage != null && !webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
            val dialogViewTrans = webViewActivity.viewTransDialog
            if (dialogViewTrans != null) {
                dialogViewTrans.dismiss()
                webViewActivity.viewTransDialog = null
            }
            if (TextUtils.isEmpty(transText) || transText == "\"\"") {
                showToast(webViewActivity, AppR.string.empty)
            } else {
                webViewActivity.stopTts()
                val viewTransDialog =
                    DialogViewTrans(webViewActivity, transText, actionModeMenuClickText, webViewActivity.transLangCode)
                webViewActivity.viewTransDialog = viewTransDialog
                viewTransDialog.setOnDismissListener(WvaViewTransDialogDismiss(webViewActivity))
            }
        }
        webViewActivity.finishActionMode()
    }
}

class ExitOnBackListener(@JvmField val wa: WebViewActivity) : WebViewActivity.PrevBackListener {
    override fun onBack(isLong: Boolean) {
        val webViewActivity = wa
        if (isLong) {
            webViewActivity.dialogsState.exitOnBackArmed = false
            return
        }
        val exitOnBackArmed = webViewActivity.dialogsState.exitOnBackArmed
        webViewActivity.dialogsState.exitOnBackArmed = false
        if (webViewActivity.webCoord == null) {
            return
        }
        if (MainSettings.doubleBack.value) {
            val now = System.currentTimeMillis()
            if (now - webViewActivity.lastBackTime > 2000) {
                webViewActivity.lastBackTime = now
                showToast(webViewActivity, AppR.string.press_again)
                return
            }
            cancelToast()
        }
        if (exitOnBackArmed) {
            webViewActivity.runInBackground(WvaExitApp(webViewActivity))
        } else {
            webViewActivity.finish()
        }
    }
}

class WvaApplyBodyColorsTask(@JvmField val webViewActivity: WebViewActivity,
                                      @JvmField val isDark: Boolean,
                                      @JvmField val isWebDark: Boolean
) : Runnable {
    override fun run() {
        val webViewActivity = this.webViewActivity
        // status icons light on a dark top-bar colour (the status bar takes it when the top bar is at the top)
        // (white or black, whichever reads better on the strip, as on the bars: AppColor.barInk)
        val fromBar = webViewActivity.statusColorFromTopBar()
        val dark = if (fromBar != 0) com.mycompany.app.ui.theme.AppColor.barInk(fromBar) != 0 else this.isDark
        // navigation bar icons on a coloured navigation strip (bars in the theme colour)
        val fromBottom = webViewActivity.naviColorFromBottomBar()
        val webDark = if (fromBottom != 0) com.mycompany.app.ui.theme.AppColor.barInk(fromBottom) != 0 else this.isWebDark
        applyBarAppearance(
            webViewActivity.getWindow(),
            webViewActivity.decorViewCached(),
            webViewActivity.insetsControllerCompat(),
            dark,
            webDark
        )
    }
}

class RestartGuideDialogListener(@JvmField val wa: WebViewActivity) : DialogConfListener {

    internal inner class CloseRestartGuideStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val dialogConfirm = webViewActivity.dialogsState.restartGuideDialog
            if (dialogConfirm != null) {
                dialogConfirm.dismiss()
                webViewActivity.dialogsState.restartGuideDialog = null
            }
            webViewActivity.restartWithTab()
        }
    }

    override fun onResult(confirmed: Boolean) {
        val webViewActivity = wa
        if (confirmed) {
            ZtriSettings.edit(webViewActivity.dialogsState.appContext) { set(ZtriSettings.guideRest, false) }
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(CloseRestartGuideStep())
    }
}

class RestartGuideDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogConfirm = webViewActivity.dialogsState.restartGuideDialog
        if (dialogConfirm != null) {
            dialogConfirm.dismiss()
            webViewActivity.dialogsState.restartGuideDialog = null
        }
    }
}

class RestartNoticeDialogListener(@JvmField val wa: WebViewActivity) : DialogApplyListener {

    internal inner class CloseRestartNoticeStep : Runnable {

        internal inner class ClearPageHistoryStep : Runnable {

            internal inner class CloseNoticeDialogStep : Runnable {
                override fun run() {
                    val closeRestartNoticeStep = this@CloseRestartNoticeStep
                    val webViewActivity = wa
                    val dialogSetMsg = webViewActivity.dialogsState.restartNoticeDialog
                    if (dialogSetMsg != null) {
                        dialogSetMsg.dismiss()
                        webViewActivity.dialogsState.restartNoticeDialog = null
                    }
                    wa.restartWithTab()
                }
            }

            override fun run() {
                val restartNoticeDialogListener = this@RestartNoticeDialogListener
                val webNestView = wa.currentPage
                if (webNestView != null) {
                    try {
                        webNestView.clearHistory()
                    } catch (unused: Exception) {
                    }
                }
                val handler = wa.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(CloseNoticeDialogStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                try {
                    webNestView.clearCache(true)
                } catch (unused: Exception) {
                }
            }
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ClearPageHistoryStep())
        }
    }

    override fun onApply() {
        val webViewActivity = wa
        val dialogSetMsg = webViewActivity.dialogsState.restartNoticeDialog
        if (dialogSetMsg != null) {
            dialogSetMsg.startBusy()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(CloseRestartNoticeStep())
        }
    }
}

class WvaApplyWindowBackground(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val windowBgColor = webViewActivity.dialogsState.windowBgColor
        if (windowBgColor != 1234) {
            try {
                webViewActivity.getWindow().setBackgroundDrawable(ColorDrawable(windowBgColor))
                webViewActivity.dialogsState.windowBgMode = 3.toByte()
                webViewActivity.dialogsState.windowBgKey = windowBgColor
            } catch (unused: Exception) {
                webViewActivity.dialogsState.windowBgMode = 0.toByte()
                webViewActivity.dialogsState.windowBgKey = 1234
            }
            val handler = webViewActivity.mainHandler
            if (handler != null) {
                handler.postDelayed(WvaWindowBackgroundCheck(webViewActivity), 100L)
                return
            }
            return
        }
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.setBackgroundColor(0)
        }
        val landscape = webViewActivity.isLandscapeScreen()
        val bitmapI = backupBitmap(webViewActivity.dialogsState.appContext, landscape)
        if (isBitmapOk(bitmapI)) {
            webViewActivity.setWindowBackground(bitmapI, landscape)
        } else {
            webViewActivity.runInBackground(WvaSetWindowBackgroundTask(webViewActivity))
        }
    }
}

class RestartNoticeDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetMsg = webViewActivity.dialogsState.restartNoticeDialog
        if (dialogSetMsg != null) {
            dialogSetMsg.dismiss()
            webViewActivity.dialogsState.restartNoticeDialog = null
        }
    }
}

class RestartAppTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        PrefSync.saveIndexes(webViewActivity.dialogsState.appContext)
        flushCookies(webViewActivity.dialogsState.appContext, false)
        val mainAppO = MainApp.from(webViewActivity.getApplicationContext())
        val mainDownSvc: MainDownSvc? = if (mainAppO != null && mainAppO.downBinding) mainAppO.downService else null
        if (mainAppO != null && mainAppO.downBinding && mainDownSvc != null) {
            mainDownSvc.stopAll(false, false)
        }
        restartActivity(webViewActivity, "web_restart_url", webViewActivity.onStartPage)
    }
}

class WvaOpenSavedPage(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val path: String?,
                                       @JvmField val title: String?
) : Runnable {

    internal inner class OpenSavedPageStep(@JvmField val outer: WvaOpenSavedPage,
                                         @JvmField val fileSourceUrl: String,
                                         @JvmField val loaded: Boolean,
                                         @JvmField val fileUrl: String?
    ) : Runnable {
        override fun run() {
            val webViewActivity = this.outer.webViewActivity
            if (webViewActivity.currentPage == null) {
                return
            }
            val fileSourceUrl = this.fileSourceUrl
            webViewActivity.openInCurrentPage(fileSourceUrl, null)
            if (this.loaded) {
                webViewActivity.currentPage!!.setFileUrl(this.fileUrl, fileSourceUrl)
            } else {
                showToast(webViewActivity, AppR.string.fail)
            }
        }
    }

    override fun run() {
        val webViewActivity = this.webViewActivity
        if (webViewActivity.dialogsState.appContext != null) {
            val md5FileNameGenerator = Md5FileNameGenerator()
            val pathNow = this.path
            val path =
                filesSubdir(webViewActivity.dialogsState.appContext, ".page") + "/" + md5FileNameGenerator.generate(pathNow) + ".mht"
            val fileReady = if (File(path).exists()) true else MainUri.copyUri(
                webViewActivity.dialogsState.appContext,
                pathNow,
                path,
                true,
                false
            )
            val fileUrl = if (fileReady) ("file://" + path) else "about:blank"
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(OpenSavedPageStep(this, fileUrl, fileReady, this.title))
        }
    }
}

class ClearRecentClick(@JvmField val wa: WebViewActivity) : View.OnClickListener {

    internal inner class ShowClearRecentStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen()) {
                return
            }
            val dialogSetMsg = webViewActivity.clearRecentDialog
            if (dialogSetMsg != null) {
                dialogSetMsg.dismiss()
                webViewActivity.clearRecentDialog = null
            }
            val clearRecentDialog = DialogSetMsg(
                webViewActivity,
                true,
                AppR.string.recent_delete,
                AppR.string.delete,
                0,
                WvaDialogMsgListener(webViewActivity)
            )
            webViewActivity.clearRecentDialog = clearRecentDialog
            clearRecentDialog.setOnDismissListener(ClearRecentDialogDismissed(webViewActivity))
        }
    }

    override fun onClick(view: View?) {
        val webViewActivity = wa
        if (webViewActivity.webCoord == null || webViewActivity.isAnyDialogOpen()) {
            return
        }
        webViewActivity.pageState.addressEditBusy = false
        webViewActivity.setAddressEditing(false)
        webViewActivity.webCoord!!.post(ShowClearRecentStep())
    }
}

class WvaLocalChromeClient(@JvmField val wa: WebViewActivity) : WebChromeClient() {

    inner class RedrawTabLaterStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webTabBarAdapter = webViewActivity.tabBar
            if (webTabBarAdapter != null) {
                webTabBarAdapter.redrawAt(webTabBarAdapter.barPosOf(webViewActivity.currentTabIndex))
            }
        }
    }

    inner class CheckPageHostTask : Runnable {

        internal inner class CheckPageHostLaterStep : Runnable {

            internal inner class CheckXHostStep : Runnable {
                override fun run() {
                    val checkPageHostTask = this@CheckPageHostTask
                    val pageHost = wa.dialogsState.pageHost
                    if (TextUtils.isEmpty(pageHost) || pageHost != "x.com") {
                        return
                    }
                    evalJs(wa.currentPage, "(function(){mySns(document);})();", false)
                }
            }

            override fun run() {
                val localChromeClient = this@WvaLocalChromeClient
                val webViewActivity = wa
                if (webViewActivity.dialogsState.snsFixBusy) {
                    evalJs(webViewActivity.currentPage, "(function(){mySns(document);})();", false)
                    val pageHost = wa.dialogsState.pageHost
                    val handler: Handler? = if (TextUtils.isEmpty(pageHost) || (pageHost != "x.com")) null else wa.mainHandler
                    if (TextUtils.isEmpty(pageHost) || (pageHost != "x.com") || handler == null) {
                        return
                    }
                    handler.postDelayed(CheckXHostStep(), 1000L)
                }
            }
        }

        override fun run() {
            val adsChanged: Boolean
            val webViewActivity = wa
            val pageTitle = webViewActivity.dialogsState.pageTitle
            webViewActivity.dialogsState.pageTitle = null
            if (webViewActivity.dialogsState.chromeClientBusy) {
                adsChanged = false
            } else {
                val pageHost = webViewActivity.dialogsState.pageHost
                if (TextUtils.isEmpty(pageHost) || pageHost!!.endsWith("pornoanne.com") || TextUtils.isEmpty(
                        pageTitle
                    ) || !pageTitle!!.lowercase().contains("xhamster")
                ) {
                    adsChanged = false
                } else {
                    adsChanged = true
                    webViewActivity.dialogsState.chromeClientBusy = true
                    webViewActivity.dialogsState.lastHamTag = null
                    val nativeHost = webViewActivity.dialogsState.pageHost
                    MainUtil.nativeHost = nativeHost
                    MainUtil.nativeHostBare = MainUtil.stripHostPrefix(nativeHost)
                    webViewActivity.applySnsFixes()
                }
            }
            if (webViewActivity.dialogsState.snsFixBusy && !adsChanged) {
                val handler = webViewActivity.mainHandler
                if (handler == null) {
                    return
                } else {
                    handler.postDelayed(CheckPageHostLaterStep(), 1000L)
                }
            }
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                if ((adsChanged || !webNestView.adsRunning) && !webViewActivity.onStartPage && BrowserSettings.adsBlock.value) {
                    if (!adsChanged) {
                        webNestView.startAdsClean(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, false)
                        return
                    }
                    webNestView.setAdsChanged(adsChanged)
                    webViewActivity.currentPage!!.startAdsClean(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, false)
                    webViewActivity.currentPage!!.setAdsChanged(false)
                }
            }
        }
    }

    override fun onCloseWindow(webView: WebView?) {
        val webViewActivity = wa
        if (webView != null && webView == webViewActivity.pageState.popupPage) {
            webViewActivity.destroyPopupPage()
            return
        }
        val webNestView = webViewActivity.currentPage
        if (webNestView == null || webNestView.windowClosed || (webNestView != webView)) {
            return
        }
        webViewActivity.previewPrevTab()
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        var canTranslate = false
        var message: String? = null
        var refusedAt = 0
        var offset = 0
        var myWebBody: MyWebBody? = null
        var transState: Byte = 0
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar == null || consoleMessage == null) {
            transState = if (webViewActivity.isTranslateActive()) 0 else webViewActivity.dialogsState.transState
            if (webViewActivity.isTranslateActive() || transState.toInt() == 1 || transState.toInt() == 3) {
                canTranslate = false
            } else {
                canTranslate = !webViewActivity.transRequested
            }
            if (canTranslate && consoleMessage != null) {
                message = consoleMessage.message()
                if (!TextUtils.isEmpty(message) && (message.indexOf("Refused to load the s")
                        .also { refusedAt = it }) != -1
                ) {
                    offset = refusedAt + 21
                    if ((message.startsWith("cript", offset) || message.startsWith(
                            "tylesheet",
                            offset
                        )) && message.indexOf("translate", refusedAt + 27) != -1
                    ) {
                        webViewActivity.dialogsState.transState = 2.toByte()
                        webViewActivity.dialogsState.transReady = false
                        webViewActivity.transLangCode = null
                        webViewActivity.transText = null
                        if (webViewActivity.currentPage != null && (webViewActivity.dialogsState.webBody.also {
                                myWebBody = it
                            }) != null) {
                            myWebBody!!.post(WvaCheckTransTask(webViewActivity))
                        }
                    }
                }
            }
        } else {
            val consoleText = consoleMessage.message()
            if (TextUtils.isEmpty(consoleText) || !consoleText.startsWith("Uncaught") || consoleText.indexOf(
                    "borderRadius",
                    8
                ) == -1
            ) {
                if (webViewActivity.isTranslateActive()) {
                    canTranslate = false
                } else {
                    canTranslate = false
                }
                if (canTranslate) {
                    message = consoleMessage.message()
                    if (!TextUtils.isEmpty(message)) {
                        offset = refusedAt + 21
                        if (message.startsWith("cript", offset)) {
                            webViewActivity.dialogsState.transState = 2.toByte()
                            webViewActivity.dialogsState.transReady = false
                            webViewActivity.transLangCode = null
                            webViewActivity.transText = null
                            if (webViewActivity.currentPage != null) {
                                myWebBody!!.post(WvaCheckTransTask(webViewActivity))
                            }
                        } else {
                            webViewActivity.dialogsState.transState = 2.toByte()
                            webViewActivity.dialogsState.transReady = false
                            webViewActivity.transLangCode = null
                            webViewActivity.transText = null
                            if (webViewActivity.currentPage != null) {
                                myWebBody!!.post(WvaCheckTransTask(webViewActivity))
                            }
                        }
                    }
                }
            } else if (webViewActivity.dialogsState.areaToolbar != null && !webViewActivity.dialogsState.areaSelectBusy) {
                webViewActivity.dialogsState.areaSelectBusy = true
                webViewActivity.runInBackground(WvaStartAreaSelect(webViewActivity))
            }
        }
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onCreateWindow(
        webView: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        message: Message?
    ): Boolean {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar == null && !webViewActivity.isPaused && webViewActivity.dialogsState.loadDialogKind.toInt() == 0 && webViewActivity.videoFrame == null && webViewActivity.dialogsState.popupBlockSnackbar == null && webViewActivity.popupMenuDialog == null && webViewActivity.pageState.pendingPage == null && !webViewActivity.pageState.chromeClientBusy) {
            webViewActivity.pageState.chromeClientBusy = true
            webViewActivity.pageState.popupLoadState = 0.toByte()
            webViewActivity.pageState.popupUrl = null
            webViewActivity.pageState.popupHost = null
            webViewActivity.pageState.popupPending = false
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaLocalChromeClientTask(webViewActivity, popupMessage = message))
                return true
            }
        }
        return false
    }

    override fun onGeolocationPermissionsHidePrompt() {
        wa.closePermissionDialog()
        super.onGeolocationPermissionsHidePrompt()
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        if (wa.handlePermissionRequest(null, origin, callback)) {
            return
        }
        super.onGeolocationPermissionsShowPrompt(origin, callback)
    }

    override fun onHideCustomView() {
        val webViewActivity = wa
        val webVideoFrame = webViewActivity.videoFrame
        val isInPip = webVideoFrame != null && webVideoFrame.isInPip()
        webViewActivity.exitVideoFrame()
        val context: Context? = if (!isInPip || webViewActivity.dialogsState.onYouTube || webViewActivity.videoState.videoPip != null) null else webViewActivity.dialogsState.appContext
        if (!isInPip || webViewActivity.dialogsState.onYouTube || webViewActivity.videoState.videoPip != null || context == null) {
            return
        }
        val intentL = browserIntent(context)
        intentL.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        webViewActivity.startActivity(intentL)
    }

    override fun onJsAlert(
        webView: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (result == null) {
            return false
        }
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar != null) {
            result.cancel()
            return true
        }
        if (TextUtils.isEmpty(message)) {
            result.confirm()
            return true
        }
        if (!TextUtils.isEmpty(message) && message!!.startsWith("sb:")) {
            if (message == "sb:ads_preview") {
                webViewActivity.showWebViewDialog(webViewActivity.dialogsState.pageUrl, 0, null, true)
            } else if (message == "sb:ads_open") {
                webViewActivity.dialogsState.adsDialogUrl = webViewActivity.dialogsState.pageUrl
                webViewActivity.dialogsState.adsAllowed = true
                webViewActivity.reloadPage()
            } else if (message == "sb:link_setting") {
                webViewActivity.onUrlLinkAction(11, webViewActivity.dialogsState.pageUrl, null)
            } else if (message.startsWith("sb:lang=")) {
                val alertText = if (message.length > 8) message.substring(8) else null
                if ("0" == alertText) {
                    webViewActivity.dialogsState.langCheckBusy = false
                    webViewActivity.checkPageLanguage(false)
                } else {
                    webViewActivity.runInBackground(WvaAutoTranslateCheck(webViewActivity, title = alertText))
                }
            }
            result.confirm()
            return true
        }
        webViewActivity.showJsAlert(message, result)
        return true
    }

    override fun onJsConfirm(
        webView: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (result == null) {
            return false
        }
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar != null) {
            result.cancel()
            return true
        }
        if (TextUtils.isEmpty(message)) {
            result.confirm()
            return true
        }
        webViewActivity.showJsAlert(message, result)
        return true
    }

    override fun onJsPrompt(
        webView: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        if (result == null) {
            return false
        }
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar != null) {
            result.cancel()
            return true
        }
        if (TextUtils.isEmpty(message)) {
            result.confirm()
            return true
        }
        if (webViewActivity.isClosing) {
            return true
        }
        val dialogPrompt = webViewActivity.promptDialog
        if (dialogPrompt != null) {
            dialogPrompt.dismiss()
            webViewActivity.promptDialog = null
        }
        val promptDialog = DialogPrompt(webViewActivity, message, defaultValue, result)
        webViewActivity.promptDialog = promptDialog
        promptDialog.setOnCancelListener(WvaPromptDialogCancelled(webViewActivity))
        webViewActivity.promptDialog!!.setOnDismissListener(WvaPromptDialogDismissed(webViewActivity))
        return true
    }

    override fun onPermissionRequest(permissionRequest: PermissionRequest?) {
        if (wa.handlePermissionRequest(permissionRequest, null, null)) {
            return
        }
        super.onPermissionRequest(permissionRequest)
    }

    override fun onPermissionRequestCanceled(permissionRequest: PermissionRequest?) {
        wa.closePermissionDialog()
        super.onPermissionRequestCanceled(permissionRequest)
    }

    override fun onProgressChanged(webView: WebView?, done: Int) {
        var done = done
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            if (if (!webNestView.googleSearch) false else isGoogleSearch(webViewActivity.dialogsState.pageUrl)) {
                done = 100
            }
            webViewActivity.updatePageProgress(done)
            val loadDialogKind = webViewActivity.dialogsState.loadDialogKind
            if (loadDialogKind.toInt() == 1 || loadDialogKind.toInt() == 2) {
                WebLoadTask.Companion.instance().onProgress(done)
            }
        }
    }

    override fun onReceivedIcon(webView: WebView?, bitmap: Bitmap?) {
        val webViewActivity = wa
        webViewActivity.onPageIcon(webView, bitmap, webViewActivity.currentTabIndex, false)
    }

    override fun onReceivedTitle(webView: WebView?, title: String?) {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null || webView == null) {
            return
        }
        val url = webView.getUrl()
        if (TextUtils.isEmpty(url)) {
            return
        }
        val pageTitle = webViewActivity.pageTitleOf(webView, url)
        val webVideoFrame = webViewActivity.videoFrame
        if (webVideoFrame != null && webViewActivity.dialogsState.onYouTube) {
            webVideoFrame.setTitle(pageTitle)
        }
        if (!webViewActivity.onStartPage && PdfSettings.addrType.value == 2) {
            webViewActivity.setBarAddress(pageTitle)
        }
        val webTabItemD = webViewActivity.tabItemAt(webViewActivity.currentTabIndex)
        if (webTabItemD != null) {
            webTabItemD.tabUrl = url
            webTabItemD.tabTitle = pageTitle
            webTabItemD.deskMode = webViewActivity.currentPage!!.deskMode
            webViewActivity.loadTabItemAsync(webTabItemD)
            if (webViewActivity.tabBar != null) {
                val myWebBody = webViewActivity.dialogsState.webBody
                if (myWebBody == null) {
                    return
                } else {
                    myWebBody.post(RedrawTabLaterStep())
                }
            }
        }
        if (BrowserSettings.historyTime.value != 0 && (!SyncSettings.secretMode.value || SecretSettings.secretHist.value != 0L)) {
            if ("file:///android_asset/shortcut.html" == url) {
                return
            } else {
                webViewActivity.saveBookIcon(url, pageTitle, null, true, false)
            }
        }
        webViewActivity.dialogsState.pageTitle = pageTitle
        webViewActivity.runInBackground(CheckPageHostTask())
    }

    override fun onShowCustomView(view: View?, customViewCallback: CustomViewCallback?) {
        val webVideoImage: WebVideoFrame?
        if (view != null) {
            val webViewActivity = wa
            if (webViewActivity.videoFrame != null) {
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden()
                    return
                }
                return
            }
            if (webViewActivity.currentPage != null) {
                webViewActivity.videoState.videoEnterBusy = true
                webViewActivity.videoExitDark = AppState.darkUi
                webViewActivity.dialogsState.videoMode = 0.toByte()
                val pageHost = webViewActivity.dialogsState.pageHost
                if (TextUtils.isEmpty(pageHost)) {
                    webViewActivity.dialogsState.videoMode = 1.toByte()
                } else {
                    val lowerCase = pageHost!!.lowercase()
                    if (lowerCase.contains("youtube")) {
                        webViewActivity.dialogsState.videoMode = 2.toByte()
                    } else if (lowerCase.endsWith("instagram.com")) {
                        webViewActivity.dialogsState.videoMode = 4.toByte()
                    } else {
                        val numberSuffix = fileNumberSuffix(lowerCase)
                        if (!TextUtils.isEmpty(numberSuffix) && (comicSiteKind(numberSuffix) != 0 || numberSuffix!!.endsWith("atmegame.com"))) {
                            webViewActivity.dialogsState.videoMode = 5.toByte()
                        }
                    }
                }
                val videoMode = webViewActivity.dialogsState.videoMode
                if (videoMode.toInt() == 5 || videoMode.toInt() == 4) {
                    webVideoImage = WebVideoImage(webViewActivity)
                    webViewActivity.videoFrame = webVideoImage
                } else {
                    webVideoImage = WebVideoFull(webViewActivity)
                    webViewActivity.videoFrame = webVideoImage
                }
                webVideoImage.showVideo(
                    webViewActivity,
                    webViewActivity.dialogsState.webBody,
                    webViewActivity.currentPage,
                    webViewActivity.dialogsState.pageUrl,
                    webViewActivity.dialogsState.videoMode.toInt(),
                    view,
                    customViewCallback
                )
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(WvaVideoEnterTask(webViewActivity))
            }
        }
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        val webViewActivity = wa
        webViewActivity.pageState.valueCallback = filePathCallback
        webViewActivity.pageState.fileChooserParams = fileChooserParams
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return true
        }
        myWebCoord.post(WvaFileChooserStart(webViewActivity))
        return true
    }
}

class WvaMainMenuItemDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {

    internal inner class MenuDismissUrlStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val mainMenuItemDismissUrl = webViewActivity.dialogsState.mainMenuItemDismissUrl
            webViewActivity.dialogsState.mainMenuItemDismissUrl = null
            webViewActivity.updateFileUrl(mainMenuItemDismissUrl)
        }
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeBookmarksDialog()
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webViewActivity.dialogsState.mainMenuItemDismissUrl = webNestView.getUrl()
            webViewActivity.runInBackground(MenuDismissUrlStep())
        }
        notifyBookUpdate(webViewActivity.dialogsState.appContext, false)
    }
}

class WvaGridDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {

    internal inner class TrimCachesTask : Runnable {
        override fun run() {
            MainUtil.trimMemoryCaches()
        }
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.resumePausedPage()
        val webGridDialog = webViewActivity.dialogsState.gridDialog
        if (webGridDialog != null) {
            webGridDialog.dismiss()
            webViewActivity.dialogsState.gridDialog = null
        }
        webViewActivity.clearDataCaches()
        webViewActivity.runInBackground(TrimCachesTask())
    }
}

class WvaViewSrcDialogDismissed(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {

    internal inner class RestoreBrightnessStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.mainHandler == null) {
                return
            }
            applyWindowBrightness(webViewActivity.getWindow(), PdfSettings.brightness.value, PdfSettings.userBrightness.value)
        }
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.resumePausedPage()
        val dialogViewSrc = webViewActivity.dialogsState.viewSrcDialog
        if (dialogViewSrc != null) {
            dialogViewSrc.dismiss()
            webViewActivity.dialogsState.viewSrcDialog = null
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(RestoreBrightnessStep())
    }
}

class WvaWebAppInterface(@JvmField val wa: WebViewActivity) {
    var url: String? = null

    inner class SnsUrlStep : Runnable {

        override fun run() {
            val webViewActivity = wa
            var snsLink = webViewActivity.dialogsState.snsUrl
            webViewActivity.dialogsState.snsUrl = null
            if (TextUtils.isEmpty(snsLink)) {
                showToast(webViewActivity, AppR.string.no_down_video)
                return
            }
            if (TextUtils.isEmpty(snsLink)) {
                snsLink = null
            } else if (snsLink!!.endsWith("/liked_by/")) {
                val length = snsLink.length - 10
                if (length > 0) {
                    val trimmed = snsLink.substring(0, length)
                    val lastSlash = if (TextUtils.isEmpty(trimmed)) 0 else trimmed.lastIndexOf(47.toChar())
                    val prevSlash = if (TextUtils.isEmpty(trimmed) || lastSlash <= 0) 0 else trimmed.lastIndexOf(
                            "/p",
                            lastSlash
                        )
                    if (TextUtils.isEmpty(trimmed) || lastSlash <= 0 || prevSlash <= 0 || prevSlash != lastSlash - 2) {
                        snsLink = null
                    } else {
                        val head = trimmed.substring(0, prevSlash)
                        if (TextUtils.isEmpty(head)) {
                            snsLink = null
                        } else {
                            val tail = trimmed.substring(lastSlash)
                            if (TextUtils.isEmpty(tail)) {
                                snsLink = null
                            } else {
                                snsLink = "$head/reel$tail"
                            }
                        }
                    }
                } else {
                    snsLink = null
                }
            } else if (TextUtils.isEmpty(snsLink)) {
                snsLink = null
            } else if (!URLUtil.isNetworkUrl(snsLink) && snsLink.indexOf("/reel/") > 0) {
                val sb = StringBuilder()
                if (snsLink.startsWith("/")) {
                    sb.append("https://www.instagram.com")
                } else {
                    sb.append("https://www.instagram.com/")
                }
                sb.append(snsLink)
                snsLink = sb.toString()
            }
            if (!URLUtil.isNetworkUrl(snsLink)) {
                snsLink = webViewActivity.dialogsState.pageUrl
            }
            webViewActivity.openDownloadDialog(snsLink, null, "video/*", 0L, 5, null)
        }
    }

    inner class OpenDownloadDialogTask : Runnable {

        internal inner class ImageResultsStep : Runnable {
            override fun run() {
                val webAppInterface = this@WvaWebAppInterface
                val webViewActivity = wa
                val arrayList = webViewActivity.dialogsState.imageResults
                webViewActivity.dialogsState.imageResults = null
                if (arrayList == null || arrayList.isEmpty()) {
                    showToast(wa, AppR.string.no_down_video)
                    return
                }
                val downListItem = arrayList.get(0)
                if (downListItem == null) {
                    showToast(wa, AppR.string.no_down_video)
                    return
                }
                val url = downListItem.url
                val activity = wa
                activity.dialogsState.downloadItems = arrayList
                activity.dialogsState.downloadFromBridge = false
                activity.openDownloadDialog(url, null, "video/*", 0L, 5, null)
            }
        }

        /**
         * Collects the page's video urls (<video src> then <source type=video/ *></source>, blob: and YouTube skipped,
         * relative urls resolved) from the html wa.Og, names them from the file name (text after the last '_'),
         * sorts by name and picks a poster (wa.Pg or the web view's poster for the url). Mode Qg 0 opens them
         * directly; otherwise they become MP4 download items for ImageResultsStep.
        </video> */
        override fun run() {
            val activity = wa
            val html = activity.dialogsState.fansHtml
            var poster = activity.dialogsState.fansPoster
            val mode = activity.dialogsState.fansMode
            activity.dialogsState.fansHtml = null
            activity.dialogsState.fansPoster = null
            var result: ArrayList<DownListItem?>? = null
            var doc: Document?
            try {
                doc = Jsoup.parse(html!!)
            } catch (exception: Exception) {
                doc = null
            }
            if (doc != null) {
                val base = MainUtil.hostOf(activity.dialogsState.pageUrl, true)
                val urls = ArrayList<String?>()
                try {
                    addVideos(urls, doc.select("video"), base, false)
                } catch (ignored: Exception) {
                }
                try {
                    addVideos(urls, doc.select("source"), base, true)
                } catch (ignored: Exception) {
                }
                val items = ArrayList<ChildItem>()
                if (!urls.isEmpty()) {
                    if (TextUtils.isEmpty(poster)) {
                        try {
                            for (url in urls) {
                                val found = if (activity.currentPage == null) null else activity.currentPage!!.videoPoster(
                                    activity.currentPage!!.videoIndex(url)
                                )
                                if (!TextUtils.isEmpty(found)) {
                                    poster = found
                                    break
                                }
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                    for (url in urls) {
                        if (TextUtils.isEmpty(url)) {
                            continue
                        }
                        var name = stripExtension(guessDownloadName(url, null, "video/*", false))
                        if (name == null || name.length == 0) {
                            continue
                        }
                        val under = name.lastIndexOf('_')
                        if (under > 0 && under + 1 < name.length) {
                            name = name.substring(under + 1)
                        }
                        val item = ChildItem()
                        item.path = url
                        item.name = name
                        items.add(item)
                    }
                }
                if (!items.isEmpty()) {
                    try {
                        items.sortWith(WebViewActivity.SortName())
                    } catch (ignored: Exception) {
                    }
                    if (mode == 0) {
                        val list: ArrayList<Any?> = ArrayList<Any?>()
                        for (item in items) {
                            list.add(item.path)
                        }
                        activity.findPageVideos(null, null, poster, list)
                    } else {
                        result = ArrayList()
                        for (index in items.indices) {
                            val item = items.get(index)
                            val down = DownListItem(index, item.name, "MP4")
                            down.url = item.path
                            down.thumb = poster
                            result.add(down)
                        }
                    }
                }
            }
            if (mode == 0) {
                return
            }
            activity.dialogsState.imageResults = result
            if (activity.mainHandler != null) {
                activity.mainHandler!!.post(ImageResultsStep())
            }
        }

        private fun addVideos(
            urls: ArrayList<String?>?,
            elements: Elements?,
            base: String?,
            typed: Boolean
        ) {
            if (elements == null) {
                return
            }
            for (element in elements) {
                if (typed) {
                    val type = element.attr("type")
                    if (TextUtils.isEmpty(type) || !type.startsWith("video")) {
                        continue
                    }
                }
                var src = element.attr("src")
                if (TextUtils.isEmpty(src) || src.startsWith("blob:") || src.contains("youtube.com")) {
                    continue
                }
                if (!URLUtil.isNetworkUrl(src)) {
                    src = absoluteUrl(src, base) ?: continue
                    if (!URLUtil.isNetworkUrl(src)) {
                        continue
                    }
                }
                if (!urls!!.contains(src)) {
                    urls.add(src)
                }
            }
        }
    }

    inner class InitTtsTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            var speechText = webViewActivity.dialogsState.speechText
            webViewActivity.dialogsState.speechText = null
            if (!TtsSettings.ttsMode.value || webViewActivity.speechState.jsBridgeBusy || webViewActivity.isPaused || webViewActivity.videoFrame != null || TextUtils.isEmpty(
                    speechText
                )
            ) {
                return
            }
            webViewActivity.speechState.jsBridgeBusy = true
            webViewActivity.initTts()
            if (webViewActivity.speechState.tts == null) {
                webViewActivity.speechState.jsBridgeBusy = false
                return
            }
            webViewActivity.dialogsState.ttsSpeaking = true
            try {
                val maxSpeechInputLength = TextToSpeech.getMaxSpeechInputLength()
                if (speechText!!.length > maxSpeechInputLength) {
                    speechText = speechText.substring(0, maxSpeechInputLength)
                }
                if (webViewActivity.speechState.tts!!.isSpeaking()) {
                    webViewActivity.speechState.tts!!.stop()
                }
                // a dead engine is made again once; when it still does not speak, the browser says so (2026-11-26)
                if (com.mycompany.app.main.SpeechGuard.speak(
                        webViewActivity.speechState.tts, speechText, "0",
                        prepare = { webViewActivity.applyTtsLanguage() },
                        recreate = { done -> webViewActivity.recreateTts(done) },
                        onFailed = { webViewActivity.ttsFailed() },
                    )
                ) {
                    webViewActivity.updateTtsPanel(true, true)
                }
            } catch (unused: Exception) {
                webViewActivity.speechState.jsBridgeBusy = false
                webViewActivity.dialogsState.ttsSpeaking = false
            }
        }
    }

    inner class StopBackgroundPlayTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val addedPlaySrc = webViewActivity.dialogsState.addedPlaySrc
            val backPlayPaused = webViewActivity.dialogsState.backPlayPaused
            val backPlayAudio = webViewActivity.dialogsState.backPlayAudio
            webViewActivity.dialogsState.addedPlaySrc = null
            if ("undefined" == addedPlaySrc) {
                webViewActivity.dialogsState.backPlaySrc = null
            } else {
                webViewActivity.dialogsState.backPlaySrc = addedPlaySrc
            }
            webViewActivity.dialogsState.backPlayIsPaused = backPlayPaused
            val backPlayOn = webViewActivity.videoState.backPlayOn
            if (backPlayOn && backPlayOn && webViewActivity.currentPage != null) {
                if (!(if (backPlayAudio || TextUtils.isEmpty(webViewActivity.dialogsState.pageHost) || !(webViewActivity.dialogsState.pageHost!!.endsWith(
                            "soundcloud.com"
                        ) || webViewActivity.dialogsState.pageHost!!.endsWith("promodj.com") || webViewActivity.dialogsState.pageHost!!.endsWith(
                            "radiorecord.ru"
                        ))
                    ) backPlayAudio else true)
                ) {
                    webViewActivity.stopBackgroundPlay(false, false)
                } else {
                    webViewActivity.dialogsState.backPlayOn = backPlayAudio
                    addShortcut(webViewActivity.dialogsState.appContext, webViewActivity.currentPage, webViewActivity.dialogsState.backPlayIsPaused, backPlayAudio)
                }
            }
        }
    }

    inner class AddShortcutTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val pausedSrc = webViewActivity.dialogsState.pausedSrc
            val videoPaused = webViewActivity.dialogsState.videoPaused
            webViewActivity.dialogsState.pausedSrc = null
            if ("undefined" == pausedSrc) {
                webViewActivity.dialogsState.backPlaySrc = null
            } else {
                webViewActivity.dialogsState.backPlaySrc = pausedSrc
            }
            webViewActivity.dialogsState.backPlayIsPaused = videoPaused
            if (webViewActivity.videoState.backPlayOn) {
                addShortcut(webViewActivity.dialogsState.appContext, webViewActivity.currentPage, videoPaused, true)
            }
        }
    }

    inner class EvalJsTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                var arrayList: ArrayList<Any?>? = null
                if (!partsReady(35, MainNative.requestFixParts)) {
                    val requestFixParts: ArrayList<Any?> = ArrayList<Any?>()
                    var index = 0
                    while (true) {
                        if (index >= 35) {
                            MainNative.requestFixParts = requestFixParts
                            arrayList = requestFixParts
                            break
                        } else {
                            val passJs = MainUtil.getPassJs3(index)
                            if (TextUtils.isEmpty(passJs)) {
                                break
                            }
                            requestFixParts.add(passJs)
                            index++
                        }
                    }
                } else {
                    arrayList = MainNative.requestFixParts
                }
                if (arrayList != null && arrayList.size == 35) {
                    val sb = StringBuilder()
                    for (index in 0..34) {
                        val entry = arrayList.get(index) as String?
                        if (!TextUtils.isEmpty(entry)) {
                            sb.append(entry)
                        }
                    }
                    evalJs(webNestView, sb.toString(), true)
                }
            }
            webViewActivity.dialogsState.jsBridgeBusy = false
        }
    }

    inner class OpenBlobDialogTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val blobUrl = webViewActivity.dialogsState.blobUrl
            val blobDisposition = webViewActivity.dialogsState.blobDisposition
            val blobMime = webViewActivity.dialogsState.blobMime
            val blobLength = webViewActivity.videoState.blobLength
            webViewActivity.dialogsState.blobUrl = null
            webViewActivity.dialogsState.blobDisposition = null
            webViewActivity.dialogsState.blobMime = null
            webViewActivity.pageState.jsBridgeBusy = true
            webViewActivity.openDownloadDialog(blobUrl, blobDisposition, blobMime, blobLength, 38, null)
        }
    }

    inner class CloseDownBlobDialogTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.closeDownBlobDialog()
            webViewActivity.requestOpenTypeDialog(0L, 4, null, null, 0)
        }
    }

    inner class ResetMediaTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val vidDetKind = webViewActivity.dialogsState.vidDetKind
            val webNestView = webViewActivity.currentPage
            if (webNestView == null) {
                return
            }
            if (vidDetKind == 1) {
                webNestView.resetMedia(null)
                webViewActivity.findPageVideos("pub_dummy", null, null, null)
            } else if (vidDetKind != 2) {
                webViewActivity.findPageVideos("vid_dummy", null, null, null)
            } else {
                webNestView.resetMedia(null)
                webViewActivity.findPageVideos("pgf_dummy", null, null, null)
            }
        }
    }

    inner class FindPageVideosTask : Runnable {
        override fun run() {
            val pageHost: String?
            val webViewActivity = wa
            val vidDe2Url = webViewActivity.dialogsState.vidDe2Url
            var vidDe2Poster = webViewActivity.dialogsState.vidDe2Poster
            webViewActivity.dialogsState.vidDe2Url = null
            webViewActivity.dialogsState.vidDe2Poster = null
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                if (webNestView.hasDummyVideo) {
                    try {
                        val arrayList = webNestView.videoUrls
                        if (arrayList != null && !arrayList.isEmpty()) {
                            val size = arrayList.size
                            var videoUrls: ArrayList<String?>? = null
                            var index = 0
                            while (index < size) {
                                val entry: Any? = arrayList.get(index)
                                index++
                                val selector = entry as String?
                                if (selector != null && selector != "vid_dummy") {
                                    if (videoUrls == null) {
                                        videoUrls = ArrayList()
                                    }
                                    videoUrls.add(selector)
                                }
                            }
                            webNestView.videoUrls = videoUrls
                            webNestView.hasDummyVideo = false
                        }
                    } catch (unused: Exception) {
                    }
                }
                if (URLUtil.isNetworkUrl(vidDe2Url) && ((MainUtil.hostOf(webViewActivity.dialogsState.pageUrl, true)
                        .also { pageHost = it }) == null || pageHost!!.length + 1 < vidDe2Url!!.length)
                ) {
                    if (!URLUtil.isNetworkUrl(vidDe2Poster)) {
                        vidDe2Poster = null
                    }
                    webViewActivity.dialogsState.snsFixUrl = null
                    webViewActivity.findPageVideos(vidDe2Url, null, vidDe2Poster, null)
                }
                val dialogVideoList = webViewActivity.videoListDialog
                if (dialogVideoList != null) {
                    dialogVideoList.checkScan()
                }
            }
        }
    }

    inner class ShowEmptyVideoListTask : Runnable {
        override fun run() {
            val dialogVideoList = wa.videoListDialog
            if (dialogVideoList == null || dialogVideoList.liveOrNull == null) {
                return
            }
            dialogVideoList.applyBusy(false)
            dialogVideoList.showEmpty()
        }
    }

    inner class NextPageUrlTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val snsUrl = webViewActivity.dialogsState.snsUrl
            webViewActivity.dialogsState.snsUrl = null
            val nextUrl = nextPageUrl(snsUrl)
            if (TextUtils.isEmpty(nextUrl)) {
                showToast(webViewActivity, AppR.string.no_down_video)
            } else {
                webViewActivity.pageState.externalDownload = true
                webViewActivity.openDownloadDialog(nextUrl, null, "video/*", 0L, 5, null)
            }
        }
    }

    inner class ParseMediaDefinitionsTask : Runnable {
        override fun run() {
            var arrayList: ArrayList<Any?>?
            var poster: String?
            var posterText: String?
            val webViewActivity = wa
            val vidDe3Text = webViewActivity.dialogsState.vidDe3Text
            val vidDe3Kind = webViewActivity.dialogsState.vidDe3Kind
            webViewActivity.dialogsState.vidDe3Text = null
            if (webViewActivity.currentPage != null) {
                if (vidDe3Kind == 1) {
                    try {
                        val scanner = Scanner(vidDe3Text)
                        while (scanner.hasNextLine() && !webViewActivity.parseMediaDefinitions(scanner.nextLine())) {
                        }
                        scanner.close()
                    } catch (unused: Exception) {
                    }
                } else if (vidDe3Kind == 2 && !TextUtils.isEmpty(vidDe3Text)) {
                    try {
                        val entries =
                            vidDe3Text!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if (entries.size != 0) {
                            arrayList = null
                            poster = null
                            posterText = null
                            for (entry in entries) {
                                try {
                                    if (URLUtil.isNetworkUrl(entry)) {
                                        if (entry.endsWith(".jpg")) {
                                            poster = entry
                                        } else {
                                            if (entry.endsWith(".gif")) {
                                                posterText = entry
                                            }
                                            if (arrayList == null) {
                                                arrayList = ArrayList<Any?>()
                                            }
                                            if (!arrayList.contains(entry)) {
                                                arrayList.add(entry)
                                            }
                                            webViewActivity.dialogsState.snsFixUrl = null
                                        }
                                    }
                                } catch (unused2: Exception) {
                                }
                            }
                            if (arrayList != null && !arrayList.isEmpty()) {
                                webViewActivity.dialogsState.videoQualitiesFound = true
                                if (TextUtils.isEmpty(poster)) {
                                    poster = posterText
                                }
                                val webNestView = webViewActivity.currentPage
                                if (webNestView != null) {
                                    webNestView.resetMedia(null)
                                }
                                webViewActivity.findPageVideos(null, null, poster, arrayList)
                            }
                        }
                    } catch (unused3: Exception) {
                        arrayList = null
                        poster = null
                        posterText = null
                    }
                }
                val dialogVideoList = webViewActivity.videoListDialog
                if (dialogVideoList != null) {
                    dialogVideoList.checkScan()
                }
            }
        }
    }

    inner class AddBlockCountTask : Runnable {
        /** Hides the elements of the class name the page reported ([class*="x"],[data-role*="x"]) on this page. */
        override fun run() {
            val activity = wa
            val cls = activity.dialogsState.hamTagClass
            activity.dialogsState.hamTagClass = null
            if (TextUtils.isEmpty(cls)) {
                return
            }
            // hide the reported class on this page right away (the old engine also saved it per host in DbAdsCmd)
            val selector = "[class*=\"" + cls + "\"],[data-role*=\"" + cls + "\"]"
            evalJs(
                activity.currentPage,
                "(function(){var s=document.createElement('style');s.textContent='" + selector.replace("'", "\\'") +
                    "{display:none !important;}';(document.head||document.documentElement).appendChild(s);})();",
                true,
            )
            activity.addBlockCount(1, false)
        }
    }

    inner class ClearFndTagTask : Runnable {
        /**
         * Google ad tags the old engine's tag finder reported (`android.onFndTag`). The finder was only injected by the
         * old engine (removed with the uBlock Origin migration), so this only clears the reported values.
         */
        override fun run() {
            val webViewActivity = wa
        }
    }

    inner class NoteVideoSourceTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val pstMatch = webViewActivity.dialogsState.pstMatch
            val pstPoster = webViewActivity.dialogsState.pstPoster
            val pstPosterUrl = webViewActivity.dialogsState.pstPoster2
            webViewActivity.dialogsState.pstMatch = null
            webViewActivity.dialogsState.pstPoster = null
            webViewActivity.dialogsState.pstPoster2 = null
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.noteVideoSource(pstMatch, pstPoster, pstPosterUrl)
            }
        }
    }

    inner class NotePhVideoSourceTask : Runnable {
        override fun run() {
            var index: Int = 0
            val webViewActivity = wa
            val phPstPoster = webViewActivity.dialogsState.phPstPoster
            val phPosterUrl = webViewActivity.dialogsState.phPstPoster2
            val phPstMatch = webViewActivity.dialogsState.phPstMatch
            webViewActivity.dialogsState.phPstPoster = null
            webViewActivity.dialogsState.phPstPoster2 = null
            webViewActivity.dialogsState.phPstMatch = null
            if (webViewActivity.currentPage != null) {
                if (URLUtil.isNetworkUrl(phPosterUrl)) {
                    webViewActivity.currentPage!!.noteVideoSource(phPstPoster, phPosterUrl, null)
                } else {
                    val dataSrcIndex = if (TextUtils.isEmpty(phPstMatch)) 0 else phPstMatch!!.indexOf("data-src")
                    val urlStart = if (TextUtils.isEmpty(phPstMatch) || dataSrcIndex <= 0) 0 else phPstMatch!!.indexOf("http", dataSrcIndex + 8)
                    val urlEnd = if (TextUtils.isEmpty(phPstMatch) || dataSrcIndex <= 0 || urlStart <= 0) 0 else phPstMatch!!.indexOf(
                            34.toChar(),
                            ((urlStart + 4).also { index = it })
                        )
                    if (TextUtils.isEmpty(phPstMatch) || dataSrcIndex <= 0 || urlStart <= 0 || urlEnd <= index) {
                        return
                    }
                    webViewActivity.currentPage!!.noteVideoSource(phPstPoster, phPstMatch!!.substring(urlStart, urlEnd), null)
                }
            }
        }
    }

    inner class HasLanguagesTask : Runnable {

        internal inner class LongPressJsTask : Runnable {
            override fun run() {
                val webAppInterface = this@WvaWebAppInterface
                val webViewActivity = wa
                val transResult = webViewActivity.transResult
                webViewActivity.transResult = null
                evalLongPressJs(webViewActivity.currentPage, transResult)
                val activity = wa
                val context = activity.dialogsState.appContext
                if (context != null && !DataTrans.instance(context).hasLanguages()) {
                    injectTransJs(activity.currentPage)
                }
                val act = wa
                if (TextUtils.isEmpty(act.pageLangCode)) {
                    MainUtil.checkTransClassJs(act.currentPage)
                }
            }
        }

        internal inner class CheckLanguagesTask : Runnable {
            override fun run() {
                val webAppInterface = this@WvaWebAppInterface
                val webViewActivity = wa
                val context = webViewActivity.dialogsState.appContext
                if (context != null && !DataTrans.instance(context).hasLanguages()) {
                    injectTransJs(webViewActivity.currentPage)
                }
                val activity = wa
                if (TextUtils.isEmpty(activity.pageLangCode)) {
                    MainUtil.checkTransClassJs(activity.currentPage)
                }
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.updateTransIcon(false)
            if (webViewActivity.dialogsState.transState.toInt() == 1) {
                return
            }
            val transText = webViewActivity.transText
            webViewActivity.transText = null
            if (!TextUtils.isEmpty(transText)) {
                webViewActivity.transResult = transText
                webViewActivity.runInBackground(LongPressJsTask())
            } else {
                val webNestView: WebNestView? = if (!TextUtils.isEmpty(AlbumSettings.transPick.value)) webViewActivity.currentPage else null
                if (!TextUtils.isEmpty(AlbumSettings.transPick.value) && webNestView != null) {
                    webNestView.evaluateJavascript(
                        "document.cookie",
                        WvaTransCodeResult(webViewActivity)
                    )
                }
                webViewActivity.runInBackground(CheckLanguagesTask())
            }
        }
    }

    inner class OpenLoadDialogTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.openLoadDialog(webViewActivity.dialogsState.comicMode)
        }
    }

    inner class ExtractImagesTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val loadHtml = webViewActivity.dialogsState.loadHtml
            webViewActivity.dialogsState.loadHtml = null
            WebLoadTask.Companion.instance().extractImages(loadHtml)
        }
    }

    inner class VideoInfoTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val vidHasVideo = webViewActivity.dialogsState.vidHasVideo
            val vidPaused = webViewActivity.dialogsState.vidPaused
            val vidDuration = webViewActivity.dialogsState.vidDuration
            webViewActivity.dialogsState.vidDuration = null
            val webVideoFrame = webViewActivity.videoFrame
            if (webVideoFrame != null) {
                webVideoFrame.onVideoInfo(vidHasVideo, vidPaused, vidDuration)
            }
        }
    }

    inner class AddTabPlainTask : Runnable {
        override fun run() {
            val webAppInterface = this@WvaWebAppInterface
            val url = webAppInterface.url
            webAppInterface.url = null
            val webViewActivity = wa
            webViewActivity.addTabPlain(null, url, true, null)
        }
    }

    inner class YouTubeImageTask : Runnable {

        internal inner class YouTubeImageStep : Runnable {
            override fun run() {
                val webAppInterface = this@WvaWebAppInterface
                val webViewActivity = wa
                val youTubeUrl = webViewActivity.dialogsState.youTubeUrl
                val imageUrl = webViewActivity.dialogsState.imageUrl
                webViewActivity.dialogsState.youTubeUrl = null
                webViewActivity.dialogsState.imageUrl = null
                if (TextUtils.isEmpty(youTubeUrl)) {
                    showToast(wa, AppR.string.no_down_video)
                    return
                }
                if (!TextUtils.isEmpty(imageUrl)) {
                    wa.openDownloadDialog(imageUrl, null, "image/*", 0L, 4, null)
                    return
                }
                if (youTubeUrl!!.contains("youtube.com")) {
                    showToast(wa, AppR.string.video_down_guide_3)
                    return
                }
                val host = MainUtil.hostOf(youTubeUrl, true)
                if (TextUtils.isEmpty(host) || !host!!.endsWith("redgifs.com")) {
                    wa.openDownloadDialog(youTubeUrl, null, "video/*", 0L, 5, null)
                } else if (!ZtwoSettings.addPage.value) {
                    wa.addTabInGroup(null, youTubeUrl, true, null)
                } else {
                    val activity = wa
                    activity.openInNextPage(youTubeUrl, activity.dialogsState.pageUrl, false)
                }
            }
        }

        override fun run() {
            var videoUrl: String?
            val webViewActivity = wa
            var snsUrl = webViewActivity.dialogsState.snsUrl
            var imageUrl: String? = null
            webViewActivity.dialogsState.snsUrl = null
            if (TextUtils.isEmpty(snsUrl)) {
                videoUrl = null
            } else {
                val unquoted = snsUrl!!.replace("&quot;", "\"")
                if (!TextUtils.isEmpty(unquoted)) {
                    snsUrl = unquoted
                }
                val sources =
                    snsUrl.split("<source".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                videoUrl = null
                if (sources.size != 0) {
                    videoUrl = WebViewActivity.extractSourceUrl(sources, "application/vnd.apple.mpegURL")
                    if (videoUrl == null) {
                        videoUrl = WebViewActivity.extractSourceUrl(sources, "application/dash+xml")
                    }
                }
                val httpStart = if (videoUrl == null) snsUrl.indexOf("http") else 0
                val quoteEnd = if (videoUrl == null && httpStart != -1) snsUrl.indexOf(
                        34.toChar(),
                        httpStart + 4
                    ) else 0
                if (videoUrl == null && httpStart != -1 && quoteEnd != -1) {
                    val rawUrl = snsUrl.substring(httpStart, quoteEnd)
                    if (!TextUtils.isEmpty(rawUrl)) {
                        videoUrl = rawUrl.replace("amp;", "")
                    }
                }
            }
            val gfycatAt = if (!TextUtils.isEmpty(videoUrl) && !TextUtils.isEmpty(videoUrl)) videoUrl!!.lastIndexOf(
                    "thumbs.gfycat.com"
                ) else 0
            val imageStart = if (!TextUtils.isEmpty(videoUrl) && !TextUtils.isEmpty(videoUrl) && gfycatAt != -1) videoUrl!!.lastIndexOf(
                    "http",
                    gfycatAt
                ) else 0
            val imageEnd = if (!TextUtils.isEmpty(videoUrl) && !TextUtils.isEmpty(videoUrl) && gfycatAt != -1 && imageStart != -1) videoUrl!!.lastIndexOf(".gif") else 0
            if (!TextUtils.isEmpty(videoUrl) && !TextUtils.isEmpty(videoUrl) && gfycatAt != -1 && imageStart != -1 && imageEnd > imageStart) {
                val index = imageEnd + 4
                imageUrl = if (index < videoUrl!!.length) videoUrl.substring(
                    imageStart,
                    index
                ) else videoUrl.substring(imageStart)
                try {
                    imageUrl = URLDecoder.decode(imageUrl, "UTF-8")
                } catch (unused: Exception) {
                }
            }
            webViewActivity.dialogsState.youTubeUrl = videoUrl
            webViewActivity.dialogsState.imageUrl = imageUrl
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(YouTubeImageStep())
        }
    }

    inner class FindSnsVideosTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val snsUrl = webViewActivity.dialogsState.snsUrl
            webViewActivity.dialogsState.snsUrl = null
            webViewActivity.findPageVideos(snsUrl, null, null, null)
        }
    }

    inner class SnsNoVideoToastStep : Runnable {
        override fun run() {
            showToast(wa, AppR.string.no_down_video)
        }
    }

    inner class ParseSnsVideoJsonTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val snsUrl = webViewActivity.dialogsState.snsUrl
            webViewActivity.dialogsState.snsUrl = null
            webViewActivity.parseVideoJson(0, snsUrl)
        }
    }

    inner class FaceNoVideoToastStep : Runnable {
        override fun run() {
            showToast(wa, AppR.string.no_down_video)
        }
    }

    inner class ParseVideoJsonTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val faceJson = webViewActivity.dialogsState.faceJson
            val faceKind = webViewActivity.dialogsState.faceKind
            webViewActivity.dialogsState.faceJson = null
            webViewActivity.parseVideoJson(faceKind, faceJson)
        }
    }

    inner class OpenFansDownloadTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val fansVideoUrl = webViewActivity.dialogsState.fansVideoUrl
            webViewActivity.dialogsState.fansVideoUrl = null
            if (TextUtils.isEmpty(fansVideoUrl)) {
                showToast(webViewActivity, AppR.string.no_down_video)
                return
            }
            webViewActivity.dialogsState.downloadItems = null
            webViewActivity.dialogsState.downloadFromBridge = true
            webViewActivity.openDownloadDialog(fansVideoUrl, null, "video/*", 0L, 5, null)
        }
    }

    @JavascriptInterface
    fun onBlobDown(url: String?, disposition: String?, mime: String?, length: Long) {
        val webViewActivity = wa
        webViewActivity.dialogsState.blobUrl = url
        webViewActivity.dialogsState.blobDisposition = disposition
        webViewActivity.dialogsState.blobMime = mime
        webViewActivity.videoState.blobLength = length
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenBlobDialogTask())
    }

    @JavascriptInterface
    fun onBlobRead(dataText: String?, index: Int, total: Int, size: Int) {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.downBlobDialog != null) {
            if (!TextUtils.isEmpty(dataText)) {
                webViewActivity.dialogsState.downBlobDialog!!.onChunk(index, total, size, dataText)
                return
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(CloseDownBlobDialogTask())
        }
    }

    @JavascriptInterface
    fun onCheckHtml(checkHtml: String?) {
        val webNestView = wa.currentPage
        if (webNestView == null || webNestView.scrollHelper == null) {
            return
        }
        if (!TextUtils.isEmpty(checkHtml)) {
            webNestView.runInBackground(webNestView.ClearCheckStateTask())
        }
    }

    @JavascriptInterface
    fun onComicDet(navMode: Int) {
        val webViewActivity = wa
        webViewActivity.runInBackground(WvaComicNavScript(webViewActivity, navMode = navMode))
    }

    @JavascriptInterface
    fun onComicView(comicMode: Int) {
        val webViewActivity = wa
        if (webViewActivity.currentPage != null) {
            webViewActivity.dialogsState.comicMode = comicMode
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody == null) {
                return
            }
            myWebBody.post(OpenLoadDialogTask())
        }
    }

    @JavascriptInterface
    fun onDocHtml(html: String?) {
        if (TextUtils.isEmpty(html)) {
            return
        }
        val webViewActivity = wa
        val myWebBody: MyWebBody? = if (webViewActivity.currentPage == null) null else webViewActivity.dialogsState.webBody
        if (webViewActivity.currentPage == null || myWebBody == null) {
            return
        }
        myWebBody.post(WvaApplyPendingDocFind(webViewActivity, findText = html))
    }

    @JavascriptInterface
    fun onFaceDet(json: String?, faceKind: Int) {
        val jsonEmpty = TextUtils.isEmpty(json)
        val webViewActivity = wa
        if (!jsonEmpty) {
            webViewActivity.dialogsState.faceJson = json
            webViewActivity.dialogsState.faceKind = faceKind
            webViewActivity.runInBackground(ParseVideoJsonTask())
        } else {
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(FaceNoVideoToastStep())
        }
    }

    @JavascriptInterface
    fun onFansDet(html: String?, poster: String?, mode: Int) {
        val isIframe = "iframe" == poster
        val webViewActivity = wa
        if (!isIframe) {
            webViewActivity.dialogsState.fansHtml = html
            webViewActivity.dialogsState.fansPoster = poster
            webViewActivity.dialogsState.fansMode = mode
            webViewActivity.runInBackground(OpenDownloadDialogTask())
            return
        }
        webViewActivity.dialogsState.fansVideoUrl = html
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(OpenFansDownloadTask())
    }

    @JavascriptInterface
    fun onFndTag(fndTagIds: String?, fndTagJson: String?) {
        if (TextUtils.isEmpty(fndTagIds) || TextUtils.isEmpty(fndTagJson)) {
            return
        }
        val webViewActivity = wa
        webViewActivity.runInBackground(ClearFndTagTask())
    }

    @JavascriptInterface
    fun onHamTag(lastHamTag: String?) {
        if (TextUtils.isEmpty(lastHamTag)) {
            return
        }
        val webViewActivity = wa
        if (lastHamTag == webViewActivity.dialogsState.lastHamTag) {
            return
        }
        webViewActivity.dialogsState.lastHamTag = lastHamTag
        webViewActivity.dialogsState.hamTagClass = lastHamTag
        webViewActivity.runInBackground(AddBlockCountTask())
    }

    @JavascriptInterface
    fun onJsResult(callback: String?, result: String?) {
        if (TextUtils.isEmpty(callback)) {
            return
        }
        val isRedditCheck = callback == "onCheckReddit"
        val webViewActivity = wa
        if (isRedditCheck) {
            val myWebBody: MyWebBody? = if (("1" != result) || webViewActivity.currentPage == null) null else webViewActivity.dialogsState.webBody
            if (("1" != result) || webViewActivity.currentPage == null || myWebBody == null) {
                return
            }
            myWebBody.post(WvaWebAppInterfaceTask(webViewActivity))
            return
        }
        var autoTransPending = false
        if (callback == "onCheckPass") {
            if ("1" == result) {
                webViewActivity.dialogsState.keyboardRelayout = true
                webViewActivity.runInBackground(WvaCheckSiteLock(webViewActivity))
                return
            } else {
                webViewActivity.dialogsState.lockCheckedHost = null
                webViewActivity.dialogsState.checkSiteLockBusy = false
                return
            }
        }
        if (callback == "onCheckLang") {
            if ("0" == result) {
                webViewActivity.dialogsState.langCheckBusy = false
                webViewActivity.checkPageLanguage(false)
                return
            } else {
                webViewActivity.runInBackground(WvaAutoTranslateCheck(webViewActivity, title = result))
                return
            }
        }
        if (callback == "onTransUser") {
            if ("1" != result) {
                webViewActivity.runInBackground(WvaTransScriptTask(webViewActivity, "2" == result))
                return
            }
            webViewActivity.dialogsState.transState = 2.toByte()
            webViewActivity.dialogsState.transReady = false
            webViewActivity.transLangCode = null
            webViewActivity.transText = null
            val myWebBody: MyWebBody? = if (webViewActivity.currentPage == null) null else webViewActivity.dialogsState.webBody
            if (webViewActivity.currentPage == null || myWebBody == null) {
                return
            }
            myWebBody.post(WvaCheckTransTask(webViewActivity))
            return
        }
        if (callback == "onTransExist") {
            val transExists = "2" == result
            val autoTransPendingNow = webViewActivity.autoTransPending
            if (AlbumSettings.transMode.value == 0) {
                webViewActivity.autoTransPending = false
            } else {
                autoTransPending = transExists || !webViewActivity.dialogsState.pageInTransLang
                webViewActivity.autoTransPending = autoTransPending
            }
            if (autoTransPendingNow != autoTransPending) {
                webViewActivity.postTransIconUpdate()
            }
            if (!transExists && !webViewActivity.transRequested) {
                if (webViewActivity.dialogsState.pageInTransLang) {
                    return
                }
                val transMode = AlbumSettings.transMode.value
                if (transMode != 1 && transMode != 2) {
                    return
                }
            }
            webViewActivity.transText = AlbumSettings.transPick.value
            webViewActivity.runInBackground(WvaTransScriptTask(webViewActivity, transExists))
            return
        }
        if (callback == "onTransList") {
            if (webViewActivity.currentPage == null || TextUtils.isEmpty(result)) {
                return
            }
            webViewActivity.runInBackground(WvaParseTransResultTask(webViewActivity, html = result))
            return
        }
        if (callback != "onTransClass") {
            if (callback == "onActionTrans") {
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.post(WvaShowTransResult(webViewActivity, transText = result))
                    return
                }
                return
            }
            return
        }
        if (webViewActivity.currentPage != null) {
            if ("0" == result) {
                webViewActivity.pageLangCode = "-"
                return
            }
            if (TextUtils.isEmpty(result)) {
                webViewActivity.pageLangCode = "-"
            } else if (TextUtils.isEmpty(webViewActivity.pageLangCode)) {
                webViewActivity.runInBackground(WvaLangPrefixTask(webViewActivity, html = result))
            }
        }
    }

    @JavascriptInterface
    fun onLoadHtml(loadHtml: String?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.loadHtml = loadHtml
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody == null) {
            return
        }
        myWebBody.post(ExtractImagesTask())
    }

    @JavascriptInterface
    fun onLongPars2(html: String?) {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar == null && webViewActivity.videoFrame == null && TextUtils.isEmpty(
                webViewActivity.quickState.longPressImage
            ) && webViewActivity.videoFrame == null
        ) {
            webViewActivity.runInBackground(WvaLongPressInspect(webViewActivity, html = html))
        }
    }

    @JavascriptInterface
    fun onLongParse() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar == null && webViewActivity.videoFrame == null && !webViewActivity.dialogsState.longParseBusy && TextUtils.isEmpty(
                webViewActivity.quickState.longPressImage
            ) && webViewActivity.videoFrame == null
        ) {
            webViewActivity.dialogsState.longParseBusy = true
            webViewActivity.runInBackground(WvaClearLongPressScript(webViewActivity))
        }
    }

    @JavascriptInterface
    fun onObserDet(transLangCode: String?, transReady: Int) {
        val webViewActivity = wa
        if (transReady == 0) {
            webViewActivity.dialogsState.transState = 1.toByte()
        } else {
            webViewActivity.dialogsState.transState = 3.toByte()
            webViewActivity.dialogsState.transReady = transReady == 2
            webViewActivity.transLangCode = transLangCode
            if (TextUtils.isEmpty(AlbumSettings.transPick.value)) {
                AlbumSettings.editLocal { set(AlbumSettings.transPick, transLangCode) }
                AlbumSettings.editLocal { set(AlbumSettings.transCode, "") }
                PrefAlbum.saveTrans(webViewActivity.dialogsState.appContext)
            }
            if (sameObject(webViewActivity.transText, transLangCode)) {
                webViewActivity.transText = null
            }
        }
        if (AlbumSettings.transMode.value != 0) {
            webViewActivity.autoTransPending = true
        }
        webViewActivity.miscState.transMsgPending = false
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(HasLanguagesTask())
    }

    @JavascriptInterface
    fun onPageState(pageState: Int) {
        val webNestView = wa.currentPage
        if (webNestView != null) {
            webNestView.setPageState(pageState)
        }
    }

    @JavascriptInterface
    fun onPassClick() {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null || webViewActivity.dialogsState.jsBridgeBusy) {
            return
        }
        webViewActivity.dialogsState.jsBridgeBusy = true
        webViewActivity.runInBackground(EvalJsTask())
    }

    @JavascriptInterface
    fun onPassResult(savePassUser: String?, savePassWord: String?) {
        if (savePassUser == null || savePassUser.length < 3 || savePassWord == null || savePassWord.length < 8 || savePassUser == savePassWord) {
            return
        }
        val webViewActivity = wa
        webViewActivity.dialogsState.keyboardRelayout = true
        webViewActivity.dialogsState.savePassHost = webViewActivity.dialogsState.pageHost
        webViewActivity.dialogsState.savePassUser = savePassUser
        webViewActivity.dialogsState.savePassWord = savePassWord
        webViewActivity.dialogsState.savePassUrl = webViewActivity.dialogsState.pageUrl
    }

    @JavascriptInterface
    fun onPhPstDet(poster: String?, poster2Url: String?, match: String?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.phPstPoster = poster
        webViewActivity.dialogsState.phPstPoster2 = poster2Url
        webViewActivity.dialogsState.phPstMatch = match
        if (webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.runInBackground(NotePhVideoSourceTask())
    }

    @JavascriptInterface
    fun onPstDet(match: String?, poster: String?, poster2Url: String?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.pstMatch = match
        webViewActivity.dialogsState.pstPoster = poster
        webViewActivity.dialogsState.pstPoster2 = poster2Url
        if (webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.runInBackground(NoteVideoSourceTask())
    }

    @JavascriptInterface
    fun onRateDet(playbackRate: kotlin.Float) {
        val rateDiff = (ZtwoSettings.youSpeedRate.value).compareTo(playbackRate)
        val webViewActivity = wa
        if (rateDiff != 0) {
            if (webViewActivity.isSameRatePage()) {
                ZtwoSettings.editLocal { set(ZtwoSettings.youSpeedRate, playbackRate) }
                putSpeedRate(webViewActivity.dialogsState.appContext, playbackRate)
            }
        }
        val dialogSetRate = webViewActivity.dialogsState.setRateDialog
        if (dialogSetRate != null) {
            dialogSetRate.selectCurrent()
        }
    }

    @JavascriptInterface
    fun onRateGet(playbackRate: kotlin.Float) {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.setRateDialog != null) {
            if ((ZtwoSettings.youSpeedRate.value).compareTo(playbackRate) != 0) {
                ZtwoSettings.editLocal { set(ZtwoSettings.youSpeedRate, playbackRate) }
                putSpeedRate(webViewActivity.dialogsState.appContext, playbackRate)
            }
            val dialogSetRate = webViewActivity.dialogsState.setRateDialog
            if (dialogSetRate != null) {
                dialogSetRate.selectCurrent()
            }
        }
    }

    @JavascriptInterface
    fun onReadHtml(innerHTML: String?) {
        val webViewActivity = wa
        val dialogViewRead = webViewActivity.dialogsState.viewReadDialog
        if (dialogViewRead != null) {
            dialogViewRead.setReadHtml(innerHTML)
            return
        }
        val webReadTask = webViewActivity.dialogsState.readTask
        if (webReadTask != null) {
            webReadTask.startLoad(innerHTML)
        }
    }

    @JavascriptInterface
    fun onSlideDet() {
        wa.barsState.resetIcons = true
    }

    @JavascriptInterface
    fun onSnsDet(snsUrl: String?, snsType: Int) {
        val webViewActivity = wa
        if (snsType == 1) {
            webViewActivity.dialogsState.snsUrl = snsUrl
            val handler = webViewActivity.mainHandler
            if (handler != null) {
                handler.post(SnsUrlStep())
                return
            }
            return
        }
        if (snsType == 2) {
            webViewActivity.dialogsState.snsUrl = snsUrl
            val handler = webViewActivity.mainHandler
            if (handler != null) {
                handler.post(NextPageUrlTask())
                return
            }
            return
        }
        if (snsType == 3) {
            webViewActivity.dialogsState.snsUrl = snsUrl
            webViewActivity.runInBackground(YouTubeImageTask())
            return
        }
        if (snsType == 4) {
            if (!URLUtil.isNetworkUrl(snsUrl) || snsUrl == webViewActivity.dialogsState.lastSnsUrl) {
                return
            }
            webViewActivity.dialogsState.lastSnsUrl = snsUrl
            webViewActivity.dialogsState.snsUrl = snsUrl
            webViewActivity.runInBackground(FindSnsVideosTask())
            return
        }
        if (!TextUtils.isEmpty(snsUrl)) {
            webViewActivity.dialogsState.snsUrl = snsUrl
            webViewActivity.runInBackground(ParseSnsVideoJsonTask())
        } else {
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(SnsNoVideoToastStep())
        }
    }

    @JavascriptInterface
    fun onTextDet(speechText: String?) {
        if (TtsSettings.ttsMode.value) {
            val webViewActivity = wa
            if (webViewActivity.speechState.jsBridgeBusy || webViewActivity.isPaused || webViewActivity.videoFrame != null) {
                return
            }
            val dialogViewRead = webViewActivity.dialogsState.viewReadDialog
            if ((dialogViewRead == null || !dialogViewRead.speakMode) && !TextUtils.isEmpty(speechText)) {
                webViewActivity.dialogsState.speechText = speechText
                webViewActivity.runInBackground(InitTtsTask())
            }
        }
    }

    @JavascriptInterface
    fun onThemeDet(part: String?, color: String?) {
        val partColor = parseColorSafe(part)
        if (partColor != -1) {
            val webViewActivity = wa
            if (webViewActivity.tabsState.currentFrame == null || webViewActivity.currentPage == null) {
                return
            }
            val webTabItemD = webViewActivity.tabItemAt(partColor)
            val webNestFrame: WebNestFrame? = if (webTabItemD != null) webTabItemD.webFrame else null
            if (webTabItemD != null && webNestFrame != null && webNestFrame == webViewActivity.tabsState.currentFrame) {
                val themeColor = parseColorText(color)
                if (webViewActivity.barColorsChanged(themeColor)) {
                    webViewActivity.dialogsState.pageBarColor = themeColor
                    val lighterTheme = lighterColor(themeColor)
                    webViewActivity.currentPage!!.setBarColors(webViewActivity.dialogsState.pageBarColor, lighterTheme)
                    val handler = webViewActivity.mainHandler
                    if (handler != null) {
                        handler.post(WvaApplyPageBarColor(webViewActivity, barColorSource = lighterTheme))
                        return
                    }
                    return
                }
                return
            }
            if (webTabItemD == null || webTabItemD.webFrame == null) {
                return
            }
            val parsedColor = parseColorText(color)
            val lightColor = lighterColor(parsedColor)
            val webFrame = webTabItemD.webFrame
            val pageItemX = webFrame!!.pageItemAt(webFrame.currentPageIndex)
            if (pageItemX != null) {
                val webNestView = pageItemX.webNestView
                if (webNestView != null) {
                    webNestView.setBarColors(parsedColor, lightColor)
                } else {
                    pageItemX.themeColor = parsedColor
                    pageItemX.themeLight = lightColor
                }
            }
        }
    }

    @JavascriptInterface
    fun onUsDeleteValue(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, key: String?) {
        val webViewActivity = wa
        try {
            if (webViewActivity.isScriptHost(cacheUrl)) {
                val context = webViewActivity.dialogsState.appContext
                val dbBookScript = DbBookScript.cached
                if (TextUtils.isEmpty(scriptName) || TextUtils.isEmpty(scriptNamespace) || TextUtils.isEmpty(key)) {
                    return
                }
                DbUtil.deleteWhere(
                    DbBookScript.get(context).getWritableDatabase(),
                    "DbBookScript_table_value",
                    "_name=? AND _namespace=? AND _key=?",
                    arrayOf<String?>(scriptName!!, scriptNamespace!!, key!!)
                )
            }
        } catch (unused: Exception) {
        }
    }

    @JavascriptInterface
    fun onUsGetResourceText(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, resourceName: String?): String {
        var scriptI: Script? = null
        val webViewActivity = wa
        try {
            if (!webViewActivity.isScriptHost(cacheUrl) || (findScript(webViewActivity.dialogsState.appContext, scriptName, scriptNamespace).also {
                    scriptI = it
                }) == null) {
                return ""
            }
            for (scriptResource in scriptI!!.resourceList!!) {
                if (scriptResource!!.name == resourceName) {
                    return kotlin.text.String(scriptResource.data!!, charset("UTF-8"))
                }
            }
            return ""
        } catch (unused: Exception) {
            return ""
        }
    }

    @JavascriptInterface
    fun onUsGetResourceURL(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, resourceName: String?): String {
        val webViewActivity = wa
        try {
            val scriptI: Script? = if (!webViewActivity.isScriptHost(cacheUrl)) null else findScript(webViewActivity.dialogsState.appContext, scriptName, scriptNamespace)
            if (!webViewActivity.isScriptHost(cacheUrl) || scriptI == null) {
                return ""
            }
            for (scriptResource in scriptI.resourceList!!) {
                if (scriptResource!!.name == resourceName) {
                    val fileExtensionFromUrl =
                        MimeTypeMap.getFileExtensionFromUrl(scriptResource.url)
                    val sbN = StringBuilder("data:").append(if (TextUtils.isEmpty(fileExtensionFromUrl)) "application/octet-stream" else MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(fileExtensionFromUrl)).append(";base64,")
                    sbN.append(Base64.encodeToString(scriptResource.data, 0))
                    return sbN.toString()
                }
            }
            return ""
        } catch (unused: Exception) {
            return ""
        }
    }

    @JavascriptInterface
    fun onUsGetValue(
        scriptName: String?,
        scriptNamespace: String?,
        cacheUrl: String?,
        key: String?,
        defaultValue: String?
    ): String? {
        var storedValue: String? = null
        val webViewActivity = wa
        try {
            return if (!webViewActivity.isScriptHost(cacheUrl) || (getValue(
                    webViewActivity.dialogsState.appContext,
                    scriptName,
                    scriptNamespace,
                    key
                ).also { storedValue = it }) == null
            ) defaultValue else storedValue
        } catch (unused: Exception) {
        }
        return null
    }

    @JavascriptInterface
    fun onUsListValues(scriptName: String?, scriptNamespace: String?, cacheUrl: String?): String {
        val webViewActivity = wa
        try {
            val keys: Array<String?>? = if (!webViewActivity.isScriptHost(cacheUrl)) null else valueKeys(webViewActivity.dialogsState.appContext, scriptName, scriptNamespace)
            if (!webViewActivity.isScriptHost(cacheUrl) || keys == null || keys.size == 0) {
                return ""
            }
            var sb: StringBuilder? = null
            for (key in keys) {
                if (sb == null) {
                    sb = StringBuilder()
                } else {
                    sb.append(",")
                }
                sb.append(key)
            }
            return if (sb != null) sb.toString() else ""
        } catch (unused: Exception) {
            return ""
        }
    }

    @JavascriptInterface
    fun onUsLog(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, message: String?) {
    }

    @JavascriptInterface
    fun onUsOpenInTab(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, url: String?) {
        val webViewActivity = wa
        try {
            if (TextUtils.isEmpty(url)) {
                return
            }
            if (webViewActivity.isScriptHost(cacheUrl)) {
                this.url = url
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord != null) {
                    myWebCoord.post(AddTabPlainTask())
                }
            }
        } catch (unused: Exception) {
        }
    }

    @JavascriptInterface
    fun onUsSetClipboard(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, dataText: String?) {
        val webViewActivity = wa
        try {
            if (TextUtils.isEmpty(dataText)) {
                return
            }
            if (webViewActivity.isScriptHost(cacheUrl)) {
                copyTextToast(webViewActivity, AppR.string.copied_clipboard, "Copied data", dataText)
            }
        } catch (unused: Exception) {
        }
    }

    @JavascriptInterface
    fun onUsSetValue(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, key: String?, valueText: String?) {
        val webViewActivity = wa
        try {
            if (webViewActivity.isScriptHost(cacheUrl)) {
                putValue(webViewActivity.dialogsState.appContext, scriptName, scriptNamespace, key, valueText)
            }
        } catch (unused: Exception) {
        }
    }

    @JavascriptInterface
    fun onUsXmlHttpRequest(scriptName: String?, scriptNamespace: String?, cacheUrl: String?, details: String?): String {
        val webViewActivity = wa
        try {
            return if (webViewActivity.isScriptHost(cacheUrl)) WebViewXmlHttpRequest(
                webViewActivity.dialogsState.appContext,
                webViewActivity.currentPage,
                details
            ).send().toJson() else ""
        } catch (unused: Exception) {
            return ""
        }
    }

    @JavascriptInterface
    fun onVidDe2(url: String?, poster: String?) {
        val webViewActivity = wa
        webViewActivity.dialogsState.vidDe2Url = url
        webViewActivity.dialogsState.vidDe2Poster = poster
        webViewActivity.runInBackground(FindPageVideosTask())
    }

    @JavascriptInterface
    fun onVidDe3(text: String?, kind: Int) {
        val textEmpty = TextUtils.isEmpty(text)
        val webViewActivity = wa
        if (!textEmpty) {
            webViewActivity.dialogsState.vidDe3Text = text
            webViewActivity.dialogsState.vidDe3Kind = kind
            webViewActivity.runInBackground(ParseMediaDefinitionsTask())
        } else {
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ShowEmptyVideoListTask())
        }
    }

    @JavascriptInterface
    fun onVidDet(detKind: Int) {
        val webViewActivity = wa
        if (sameObject(webViewActivity.dialogsState.snsFixUrl, webViewActivity.dialogsState.pageUrl)) {
            webViewActivity.dialogsState.snsFixUrl = null
            if (detKind == 1 || detKind == 2) {
                webViewActivity.dialogsState.videoQualitiesFound = true
            }
            webViewActivity.dialogsState.vidDetKind = detKind
            webViewActivity.runInBackground(ResetMediaTask())
        }
    }

    @JavascriptInterface
    fun onVidFound(hasVideo: Boolean, paused: Boolean, duration: String?) {
        val webViewActivity = wa
        if (webViewActivity.videoFrame != null) {
            webViewActivity.dialogsState.vidHasVideo = hasVideo
            webViewActivity.dialogsState.vidPaused = paused
            webViewActivity.dialogsState.vidDuration = duration
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(VideoInfoTask())
        }
    }

    @JavascriptInterface
    fun onVidResult(event: String?, youConVal: String?) {
        val webVideoFrame = wa.videoFrame
        if (webVideoFrame != null) {
            webVideoFrame.onVideoEvent(event, youConVal)
        }
    }

    @JavascriptInterface
    fun onVidTime(position: String?, duration: String?) {
        val webVideoFrame = wa.videoFrame
        if (webVideoFrame != null) {
            webVideoFrame.onSeekResult(position, duration)
        }
    }

    @JavascriptInterface
    fun onVideoAddListener(playSrc: String?, paused: Boolean, audio: Boolean) {
        val webViewActivity = wa
        webViewActivity.dialogsState.addedPlaySrc = playSrc
        webViewActivity.dialogsState.backPlayPaused = paused
        webViewActivity.dialogsState.backPlayAudio = audio
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(StopBackgroundPlayTask())
    }

    @JavascriptInterface
    fun onVideoPaused(src: String?, flag: Boolean) {
        val webViewActivity = wa
        webViewActivity.dialogsState.pausedSrc = src
        webViewActivity.dialogsState.videoPaused = flag
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(AddShortcutTask())
    }

    @JavascriptInterface
    fun onViewHtml(href: String?, innerHTML: String?) {
        val webViewActivity = wa
        val dialogLoadEmg = webViewActivity.loadEmgDialog
        if (dialogLoadEmg != null) {
            val webNestView = dialogLoadEmg.webNest
            if (webNestView != null) {
                webNestView.notifyHtml(href, innerHTML)
                return
            }
            return
        }
        val dialogLoadHmg = webViewActivity.loadHmgDialog
        val webNestView: WebNestView? = if (dialogLoadHmg == null) null else dialogLoadHmg.webNest
        if (dialogLoadHmg == null || webNestView == null) {
            return
        }
        webNestView.notifyHtml(href, innerHTML)
    }
}

class WvaLocalWebViewClient(@JvmField val wa: WebViewActivity) : WebViewClient() {

    inner class PageStartedStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.stopWebClean()
            webViewActivity.applyPageStyle()
            val webNestView: WebNestView? = if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value) webViewActivity.currentPage else null
            if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value && webNestView != null) {
                webNestView.startAdsClean(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, false)
            }
            flushCookies(webViewActivity.dialogsState.appContext, false)
            webViewActivity.hidePageOverlays(false)
            webViewActivity.injectUserScripts(false)
        }
    }

    inner class ApplyPageStyleTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.applyPageStyle()
            val webNestView: WebNestView? = if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value) webViewActivity.currentPage else null
            if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value && webNestView != null) {
                webNestView.startAdsClean(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, true)
            }
            flushCookies(webViewActivity.dialogsState.appContext, false)
            webViewActivity.injectUserScripts(true)
        }
    }

    inner class PageFinishedStep : Runnable {

        internal inner class CheckSavePasswordStep : Runnable {
            override fun run() {
                val localWebViewClient = this@WvaLocalWebViewClient
                val webViewActivity = wa
                webViewActivity.checkSavePassword()
                val activity = wa
                val handler = activity.mainHandler
                if (handler != null) {
                    handler.post(WvaLocalWebViewClientTask(activity))
                }
                val act = wa
                val pageUrl = act.dialogsState.pageUrl
                if (act.dialogsState.videoCheckStarted || TextUtils.isEmpty(pageUrl) || pageUrl == act.dialogsState.lastVideoCheckPage) {
                    return
                }
                act.dialogsState.videoCheckStarted = true
                act.dialogsState.lastVideoCheckPage = pageUrl
                val mainHandler = act.mainHandler
                if (mainHandler != null) {
                    mainHandler.post(WvaClearPageCacheTask(act))
                }
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.finishTabFrameAnim()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(CheckSavePasswordStep())
        }
    }

    inner class ApplyPageStyleAgainTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.applyPageStyle()
        }
    }

    inner class SslErrorTask(
                                @JvmField val sslErrorHandler: SslErrorHandler?,
                                @JvmField val sslError: SslError?,
                                @JvmField val view: WebView?
    ) : Runnable {
        override fun run() {
            var webView: WebView? = null
            val primaryError: Int
            val webViewActivity = wa
            val sslError = this.sslError
            val sslErrorHandler = this.sslErrorHandler
            if (sslErrorHandler != null) {
                if (PdfSettings.blockSsl.value) {
                    sslErrorHandler.cancel()
                    return
                }
                if ((sslError == null || ((sslError.getPrimaryError().also {
                        primaryError = it
                    }) != 0 && primaryError != 1 && primaryError != 3 && primaryError != 4 && primaryError != 5)) && (this.view.also {
                        webView = it
                    }) != null) {
                    val url = webView!!.getUrl()
                    if (!TextUtils.isEmpty(url) && url!!.startsWith("https://")) {
                        sslErrorHandler.cancel()
                        return
                    }
                }
            }
            if (webViewActivity.currentPage == null) {
                return
            }
            webViewActivity.finishProgress()
            webViewActivity.barScroll!!.setExpanded(true)
            val webNestView = webViewActivity.currentPage
            webNestView!!.removeCrashView(null)
            webNestView.sslShown = true
            if (webNestView.sslView != null) {
                webNestView.showSslError(sslErrorHandler, sslError)
            } else {
                webNestView.sslHandlerIn = sslErrorHandler
                webNestView.sslErrorIn = sslError
                val handler = webNestView.uiHandler
                if (handler != null) {
                    handler.post(webNestView.RemoveSslViewTask())
                }
            }
            webViewActivity.updateBarBehavior()
        }
    }

    inner class RestoreScaleStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.dialogsState.lastScale = webViewActivity.dialogsState.pageScale
            evalJs(
                webViewActivity.currentPage,
                "document.body.style.width=(window.visualViewport!=undefined?window.visualViewport.width:window.innerWidth)+'px';",
                false
            )
            webViewActivity.dialogsState.pageClientBusy = false
        }
    }

    inner class FinishTabFrameAnimStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.finishTabFrameAnim()
        }
    }

    override fun doUpdateVisitedHistory(webView: WebView?, url: String?, isReload: Boolean) {
        val newVideo: kotlin.Boolean
        val webViewActivity = wa
        if (webViewActivity.currentPage != null) {
            val samePage = sameObject(webViewActivity.dialogsState.pageUrl, url)
            if (samePage) {
                newVideo = false
            } else {
                var youTubeId: String? = null
                if (isYouTubeUrl(url) && isYouTubeWatch(url) && !isYouTubeShorts(url)) {
                    youTubeId = youTubeIdOf(url)
                }
                newVideo =
                    if (TextUtils.isEmpty(youTubeId) || youTubeId == webViewActivity.dialogsState.videoPageUrl) false else true
                webViewActivity.dialogsState.videoPageUrl = youTubeId
            }
            webViewActivity.loadPageUrl(url, true, false)
            if (webViewActivity.dialogsState.onYouTube) {
                if (newVideo) {
                    webViewActivity.pauseWebDestroy()
                    val webNestView = webViewActivity.currentPage
                    if (webNestView != null) {
                        webNestView.reload()
                        return
                    }
                    return
                }
                if (webViewActivity.videoFrame == null) {
                    webViewActivity.retryVideoRate()
                }
                if (samePage) {
                    return
                }
            }
            webViewActivity.runInBackground(ApplyPageStyleAgainTask())
            if (webViewActivity.dialogsState.passwordCheckDone) {
                return
            }
            webViewActivity.dialogsState.passwordCheckDone = true
            webViewActivity.checkSavePassword()
        }
    }

    override fun onPageCommitVisible(webView: WebView?, url: String?) {
        super.onPageCommitVisible(webView, url)
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(FinishTabFrameAnimStep())
    }

    override fun onPageFinished(webView: WebView?, url: String?) {
        var url = url
        val webViewActivity = wa
        // a page released to save memory scrolls back to where it was
        (webView as? WebNestView)?.restoreScrollIfPending()
        ReloadCover.pageFinished(webView)
        if (webViewActivity.currentPage == null) {
            webViewActivity.resumeWebDestroy()
            return
        }
        webViewActivity.syncJsBridge(webView, url)
        val overrideUrl = webViewActivity.miscState.overrideUrl
        webViewActivity.miscState.overrideUrl = null
        if (!TextUtils.isEmpty(overrideUrl) && overrideUrl == url && webViewActivity.isFreshPage()) {
            webViewActivity.goBackPage(null)
            webViewActivity.resumeWebDestroy()
            return
        }
        webViewActivity.pauseWebDestroy()
        if (webViewActivity.dialogsState.hasRealPage && "about:blank" == url) {
            url = webViewActivity.dialogsState.pageUrl
            val progress = webViewActivity.currentPage!!.getProgress()
            if (progress > 0) {
                webViewActivity.updatePageProgress(max(progress, 50))
            }
        } else {
            webViewActivity.dialogsState.hasRealPage = false
        }
        webViewActivity.loadPageUrl(url, true, false)
        TabSnapshot.afterLoad(webViewActivity)
        val currentPage = webViewActivity.currentPage
        currentPage!!.javaClass
        currentPage.googleSearch = isGoogleSearch(url)
        currentPage.loading = false
        webViewActivity.currentPage!!.resetBlockCounts(webViewActivity.dialogsState.pageUrl)
        val page: WebNestView? = if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value) webViewActivity.currentPage else null
        if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value && page != null) {
            page.setAdsRunning(true)
        }
        webViewActivity.runInBackground(ApplyPageStyleTask())
        if (webViewActivity.videoFrame == null && BrowserSettings.enableJs.value) {
            webViewActivity.currentPage!!.applyJsBlock(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, true)
        }
        val loadDialogKind = webViewActivity.dialogsState.loadDialogKind
        if (loadDialogKind.toInt() == 1 || loadDialogKind.toInt() == 2) {
            val webLoadTaskB: WebLoadTask = WebLoadTask.Companion.instance()
            if (webLoadTaskB.loadWeb != null) {
                webLoadTaskB.loadState = 2.toByte()
                val webLoadTaskListener = webLoadTaskB.taskListener
                if (webLoadTaskListener != null) {
                    webLoadTaskListener.onStopped()
                }
            }
        }
        val handler = webViewActivity.mainHandler
        if (handler != null) {
            handler.post(PageFinishedStep())
            val webNestView: WebNestView? = if (!webViewActivity.dialogsState.siteFixMainFrame) null else webViewActivity.currentPage
            if (!webViewActivity.dialogsState.siteFixMainFrame || webNestView == null) {
                return
            }
            evalJs(
                webNestView,
                "(function(){var tag='onCheckReddit';var val=0;var ele=document.querySelector(\"shreddit-player,shreddit-embed\");if(ele){val=1;}android.onJsResult(tag,val);})();",
                false
            )
        }
    }

    override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
        var url = url
        val webViewActivity = wa
        webViewActivity.videoState.videoCheckUrl = null
        if (webViewActivity.currentPage != null) {
            webViewActivity.syncJsBridge(webView, url)
            webViewActivity.miscState.overrideUrl = null
            webViewActivity.pauseWebDestroy()
            if (webViewActivity.dialogsState.hasRealPage && "about:blank" == url) {
                url = webViewActivity.dialogsState.pageUrl
                val progress = webViewActivity.currentPage!!.getProgress()
                if (progress > 0) {
                    webViewActivity.updatePageProgress(max(progress, 50))
                }
            } else {
                webViewActivity.dialogsState.hasRealPage = false
            }
            webViewActivity.loadPageUrl(url, false, true)
            val webNestView = webViewActivity.currentPage
            webNestView!!.googleSearch = false
            webNestView.loading = true
            webNestView.blockUrl = null
            webNestView.adsBlocked = 0
            webNestView.popsBlocked = 0
            if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value) {
                webNestView.setAdsRunning(true)
            }
            webViewActivity.runInBackground(PageStartedStep())
            if (webViewActivity.videoFrame == null && BrowserSettings.enableJs.value) {
                webViewActivity.currentPage!!.applyJsBlock(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, false)
            }
            val loadDialogKind = webViewActivity.dialogsState.loadDialogKind
            if (loadDialogKind.toInt() == 1 || loadDialogKind.toInt() == 2) {
                WebLoadTask.Companion.instance().onProgress(0)
            }
        }
    }

    override fun onReceivedError(
        webView: WebView?,
        webResourceRequest: WebResourceRequest?,
        webResourceError: WebResourceError?
    ) {
        super.onReceivedError(webView, webResourceRequest, webResourceError)
        if (wa.currentPage == null || webResourceError == null) {
            return
        }
        WebLoadTask.Companion.instance().checkNetError(webResourceError.getErrorCode())
        if (webResourceRequest?.isForMainFrame == true) {
            com.mycompany.app.ui.game.IbexOffer.onMainFrameError(wa, webView as? WebNestView, webResourceError.getErrorCode())
        }
    }

    override fun onReceivedHttpAuthRequest(
        webView: WebView?,
        httpAuthHandler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        val webViewActivity = wa
        if (webViewActivity.isClosing) {
            return
        }
        val dialogEditAuth = webViewActivity.editAuthDialog
        if (dialogEditAuth != null) {
            dialogEditAuth.dismiss()
            webViewActivity.editAuthDialog = null
        }
        val editAuthDialog = DialogEditAuth(webViewActivity, httpAuthHandler)
        webViewActivity.editAuthDialog = editAuthDialog
        editAuthDialog.setOnDismissListener(WvaEditAuthDialogDismissed(webViewActivity))
    }

    override fun onReceivedSslError(
        webView: WebView?,
        sslErrorHandler: SslErrorHandler?,
        sslError: SslError?
    ) {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(
            SslErrorTask(
                sslErrorHandler,
                sslError,
                webView
            )
        )
    }

    override fun onRenderProcessGone(
        webView: WebView?,
        renderProcessGoneDetail: RenderProcessGoneDetail?
    ): kotlin.Boolean {
        onPageRenderGone(webView, renderProcessGoneDetail)
        return true
    }

    override fun onScaleChanged(webView: WebView?, oldScale: Float, newScale: Float) {
        val webViewActivity = wa
        webViewActivity.pageState.pinchActive = false
        if (ZtwoSettings.webScale.value != 2 || webViewActivity.dialogsState.pageClientBusy) {
            return
        }
        webViewActivity.dialogsState.pageScale = newScale
        val myWebBody: MyWebBody? = if (abs(newScale - webViewActivity.dialogsState.lastScale) <= 0.01f || webViewActivity.currentPage == null) null else webViewActivity.dialogsState.webBody
        if (abs(newScale - webViewActivity.dialogsState.lastScale) <= 0.01f || webViewActivity.currentPage == null || myWebBody == null) {
            return
        }
        webViewActivity.dialogsState.pageClientBusy = true
        myWebBody.postDelayed(RestoreScaleStep(), 100L)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        var mediaUrl: String? = null
        var mutableMap2: MutableMap<Any?, Any?>? = null
        var hostKey: String? = null
        var mainApp: MainApp? = null
        var segmentKind = 0
        var hundredsDigit = 0.toChar()
        var tensDigit = 0.toChar()
        var onesDigit = 0.toChar()
        var lastDigit = 0.toChar()
        var isDcinside = false
        var variantList = false
        var items: ArrayList<Any?>? = null
        var medItem: MedItem? = null
        var medItem2: MedItem? = null
        var medItem3: MedItem? = null
        var medItem4: MedItem? = null
        var medItem5: MedItem? = null
        var medItem6: MedItem? = null
        var host: String? = null
        var appId: String? = null
        var stringBuilder: StringBuilder? = null
        var mutableIterator: MutableIterator<Any?>? = null
        val activity = wa
        if (activity.currentPage != null && request != null && request.getUrl() != null) {
            val requestUrl = request.getUrl().toString()
            com.mycompany.app.web.filter.UboFilter.notePage(activity.dialogsState.pageUrl)
            if (request.isForMainFrame() && view is WebNestView) {
                view.uboCosmetic?.topUrl = requestUrl
            }
            activity.syncJsBridge(view, requestUrl)
            val currentPage = activity.currentPage
            if (currentPage != null) {
                currentPage.resetBlockCounts(activity.dialogsState.pageUrl)
                if (ZoneSettings.userFont.value) {
                    val fontResponse = userFontResponse(activity.dialogsState.appContext, requestUrl)
                    if (fontResponse != null) {
                        return fontResponse
                    }
                }

                var sameAdsPage = sameObject(activity.dialogsState.adsDialogUrl, activity.dialogsState.pageUrl)
                val idFound = true
                if (!sameAdsPage) {
                    val pageUrl = activity.dialogsState.pageUrl
                    activity.dialogsState.adsDialogUrl = pageUrl
                    if (BrowserSettings.adsBlock.value) {
                        if ("file:///android_asset/shortcut.html" == pageUrl) {
                            activity.dialogsState.adsAllowed = true
                        } else {
                            activity.dialogsState.adsAllowed = DataBookAds.instance(activity.dialogsState.appContext).hasSiteOf(activity.dialogsState.pageUrl, activity.dialogsState.pageHost)
                        }
                    } else {
                        activity.dialogsState.adsAllowed = true
                    }

                    activity.dialogsState.siteFixModeNow = activity.siteFixModeFor(activity.dialogsState.pageHost)
                }

                if (activity.dialogsState.siteFixModeNow == 1 && request.isForMainFrame()) {
                    activity.dialogsState.siteFixMainFrame = true
                }

                if (!activity.dialogsState.adsAllowed) {
                    val pageUrl = activity.dialogsState.pageUrl
                    val pageHost = activity.dialogsState.pageHost
                    val siteFixModeNow = activity.dialogsState.siteFixModeNow
                    val webClean = activity.miscState.webClean
                    val blockResponse: WebResourceResponse?
                    if (webClean == null) {
                        blockResponse = null
                    } else {
                        blockResponse = webClean.intercept(activity.dialogsState.appContext, view, request, pageUrl, pageHost, requestUrl, siteFixModeNow)
                    }

                    if (blockResponse != null) {
                        activity.addBlockCount(1, false)
                        return blockResponse
                    }
                } else {
                    val siteFixModeNow = activity.dialogsState.siteFixModeNow
                    if (siteFixModeNow != 0) {
                        return WebClean.Companion.blockPageResponse(activity.dialogsState.appContext, request, requestUrl, siteFixModeNow)
                    }
                }

                run label1091@{
                    run label614@{
                    if (activity.videoFrame == null) {
                        val currentPageNow = activity.currentPage
                        if (currentPageNow != null && (!currentPageNow.hasM3u8 || currentPageNow.videoState <= 9)) {
                            val pageHost = activity.dialogsState.pageHost
                            if (TextUtils.isEmpty(pageHost)
                                || isYouTubeHost(pageHost)
                                || pageHost!!.endsWith("facebook.com")
                                || pageHost == "x.com"
                                || pageHost.endsWith("reddit.com")
                                || pageHost.endsWith("tiktok.com")
                                || pageHost.endsWith("giphy.com")
                                || pageHost.endsWith("yandex.ru")
                            ) {
                                return@label614
                            }

                            if (pageHost.endsWith("instagram.com")) {
                                if (!activity.dialogsState.instaIdLoaded) {
                                    if (TextUtils.isEmpty(activity.dialogsState.instaAppId)) {
                                        sameAdsPage = true
                                        val instaId: String?
                                        if (sameAdsPage && sameAdsPage) {
                                            if (TextUtils.isEmpty(MainNative.instaAid)) {
                                                MainNative.instaAid = instaAid
                                            }

                                            instaId = MainNative.instaAid
                                        } else {
                                            instaId = null
                                        }

                                        activity.dialogsState.instaAppId = instaId
                                    }

                                    run label566@{
                                        val appContext = activity.dialogsState.appContext
                                        val instaAppId = activity.dialogsState.instaAppId
                                        run label565@{
                                        if (!TextUtils.isEmpty(requestUrl) && !TextUtils.isEmpty(
                                                instaAppId
                                            ) && requestUrl.startsWith("https://www.instagram.com/api")
                                        ) {
                                            val mutableMap: MutableMap<Any?, Any?>?
                                            try {
                                                mutableMap = request.getRequestHeaders() as MutableMap<Any?, Any?>?
                                            } catch (exception: Exception) {
                                                return@label565
                                            }

                                            run label559@{
                                            if (mutableMap != null) {
                                                val mutableSet: MutableSet<Any?>?
                                                try {
                                                    mutableSet = mutableMap.keys
                                                } catch (exception2: Exception) {
                                                    return@label559
                                                }

                                                run label555@{
                                                try {
                                                    mutableIterator = mutableSet.iterator()
                                                } catch (exception3: Exception) {
                                                    return@label555
                                                }

                                                try {
                                                    while (true) {
                                                        if (!mutableIterator.hasNext()) {
                                                            return@label555
                                                        }

                                                        val headerName =
                                                            mutableIterator.next() as String?
                                                        if (instaAppId == headerName) {
                                                            appId =
                                                                mutableMap.get(headerName) as String?
                                                            if (!TextUtils.isEmpty(appId)) {
                                                                mainApp = MainApp.from(appContext)
                                                                break
                                                            }
                                                        }
                                                    }
                                                } catch (exception4: Exception) {
                                                    return@label555
                                                }

                                                sameAdsPage = idFound
                                                if (mainApp == null) {
                                                    return@label566
                                                }

                                                try {
                                                    mainApp!!.instaAppId = appId!!
                                                } catch (exception5: Exception) {
                                                    return@label555
                                                }

                                                sameAdsPage = idFound
                                                return@label566
                                                }
                                            }
                                            }
                                        }
                                        }
                                        sameAdsPage = false
                                    }

                                    activity.dialogsState.instaIdLoaded = sameAdsPage
                                }
                                return@label614
                            }

                            if (WebViewActivity.isVkVideoPage(pageHost, activity.dialogsState.pageUrl)) {
                                return@label614
                            }

                            if (pageHost == "fonts.google.com" && requestUrl.startsWith("https://fonts.google.com/download/list?family=")) {
                                if (activity.videoFrame == null) {
                                    val webCoord = activity.webCoord
                                    if (webCoord != null) {
                                        webCoord.post(WvaShowVideoDownDialog(wa, pageUrl = requestUrl))
                                    }
                                }
                                return@label614
                            }

                            if (isGoogleHome(pageHost) || !URLUtil.isNetworkUrl(requestUrl)) {
                                return@label614
                            }

                            val requestHost = hostOf(requestUrl, false)
                            if (TextUtils.isEmpty(requestHost) || isYouTubeHost(requestHost)) {
                                return@label614
                            }

                            if (pageHost.endsWith("pornhub.com")) {
                                if (!activity.dialogsState.videoQualitiesFound) {
                                    mediaUrl = requestUrl
                                    if (isMediaUrl(requestUrl, true)) {
                                        return@label1091
                                    }

                                    if (isPhnGif(requestUrl)) {
                                        mediaUrl = requestUrl
                                        return@label1091
                                    }
                                }
                                return@label614
                            }

                            if (pageHost.endsWith("pinterest.com")) {
                                if (!requestUrl.endsWith("_audio.m3u8")) {
                                    mediaUrl = requestUrl
                                    if (isMediaUrl(requestUrl, true)) {
                                        return@label1091
                                    }

                                    if (isSubtitleUrl(requestUrl)) {
                                        mediaUrl = requestUrl
                                        return@label1091
                                    }
                                }
                                return@label614
                            }

                            if (pageHost.endsWith("kakao.com")) {
                                if (requestUrl.contains(".mpd") && !requestUrl.contains("/cenc/")) {
                                    mediaUrl = "kakao1:" + requestUrl
                                    return@label1091
                                }

                                if (requestUrl.contains("/seeking.mp4")) {
                                    return@label614
                                }
                            } else {
                                if (pageHost.endsWith("dzen.ru")) {
                                    if (requestUrl.contains(".mpd") && !requestUrl.contains("&json_preview")) {
                                        mediaUrl = "dzen1:" + requestUrl
                                        return@label1091
                                    }
                                    return@label614
                                }

                                if (pageHost.endsWith("vimeo.com")) {
                                    if (requestUrl.contains("/master.json")) {
                                        mediaUrl = "vimeo1:" + requestUrl
                                        return@label1091
                                    }
                                    return@label614
                                }

                                if (!pageHost.contains("tvwiki") && !pageHost.contains("tvmon")) {
                                    if (isFilmizle(pageHost)
                                        && !TextUtils.isEmpty(requestUrl) && !TextUtils.isEmpty(requestHost) && !requestUrl.endsWith(
                                            "a1"
                                        ) && !requestUrl.endsWith("a2")
                                    ) {
                                        val length = requestHost!!.length
                                        val pathStart = length + 3
                                        if (pathStart < requestUrl.length && requestUrl.startsWith(
                                                "m",
                                                length + 1
                                            )
                                            && requestUrl.startsWith("/", pathStart)
                                            && !requestHost.contains("google")
                                        ) {
                                            val parentPath = parentPathOf(null, requestUrl)
                                            if (parentPath != null && parentPath.length >= 10 && (extOf(
                                                    requestUrl
                                                ) == null || TextUtils.isEmpty(
                                                    mimeForPath(requestUrl)
                                                ))
                                            ) {
                                                mediaUrl = "izle:" + requestUrl
                                                return@label1091
                                            }
                                        }
                                    }
                                } else {
                                    if (requestUrl.contains(".html")) {
                                        mediaUrl = "m3fake:" + requestUrl
                                        return@label1091
                                    }

                                    if (!activity.dialogsState.segmentsFound && activity.videoFrame == null) {
                                        val segmentSplit = activity.urlSplitIndex(pageHost, requestUrl)
                                        if (segmentSplit > 0 && WebViewActivity.isSegmentUrl(segmentSplit, requestUrl)) {
                                            if (!activity.dialogsState.segmentsFound) {
                                                activity.dialogsState.segmentsFound = true
                                                activity.dialogsState.segmentLengths = null
                                                activity.dialogsState.segmentSplits = null
                                                activity.dialogsState.segmentUrls = null
                                                mediaUrl = "tsfake:" + requestUrl
                                                return@label1091
                                            }
                                            return@label614
                                        }
                                    }
                                }
                            }

                            if (requestUrl.endsWith(".aaa")) {
                                if (!activity.dialogsState.segmentsFound) {
                                    activity.dialogsState.segmentsFound = true
                                    activity.dialogsState.segmentLengths = null
                                    activity.dialogsState.segmentSplits = null
                                    activity.dialogsState.segmentUrls = null
                                    mediaUrl = "tsfake:" + requestUrl
                                    return@label1091
                                }
                                return@label614
                            }

                            run label1003@{
                            if (!requestUrl.endsWith(".ts") && !requestUrl.contains(".ts?")) {
                                val splitStart: Int
                                var splitIndex: Int
                                run label1102@{
                                    val pathSplit: Int
                                    if (!activity.dialogsState.segmentsFound && activity.videoFrame == null) {
                                        splitIndex = activity.urlSplitIndex(pageHost, requestUrl)
                                        pathSplit = splitIndex
                                        if (splitIndex > 0) {
                                            val extStart = splitIndex + 1

                                            run label991@{
                                                run label990@{
                                                    run label1053@{
                                                        try {
                                                            if (requestUrl.startsWith(
                                                                    "m4s",
                                                                    extStart
                                                                ) || requestUrl.startsWith(
                                                                    "m4a",
                                                                    extStart
                                                                )
                                                            ) {
                                                                return@label1053
                                                            }

                                                            sameAdsPage =
                                                                requestUrl.startsWith("html", extStart)
                                                        } catch (exception6: Exception) {
                                                            segmentKind = 0
                                                            return@label991
                                                        }
                                                        val requestKind: Byte
                                                        if (sameAdsPage) {
                                                            try {
                                                                hundredsDigit = requestUrl.get(splitIndex - 3)
                                                            } catch (exception7: Exception) {
                                                                return@label990
                                                            }

                                                            if (hundredsDigit < '0' || hundredsDigit > '9') {
                                                                return@label990
                                                            }

                                                            try {
                                                                tensDigit = requestUrl.get(splitIndex - 2)
                                                            } catch (exception8: Exception) {
                                                                return@label990
                                                            }

                                                            if (tensDigit < '0' || tensDigit > '9') {
                                                                return@label990
                                                            }

                                                            try {
                                                                onesDigit = requestUrl.get(splitIndex - 1)
                                                            } catch (exception9: Exception) {
                                                                return@label990
                                                            }

                                                            if (onesDigit < '0' || onesDigit > '9') {
                                                                return@label990
                                                            }

                                                            requestKind = 1
                                                        } else {
                                                            try {
                                                                sameAdsPage = WebViewActivity.isSegmentUrl(splitIndex, requestUrl)
                                                            } catch (exception10: Exception) {
                                                                segmentKind = 0
                                                                return@label991
                                                            }

                                                            if (!sameAdsPage) {
                                                                try {
                                                                    if (requestUrl.get(splitIndex - 3) != '0' || requestUrl.get(
                                                                            splitIndex - 2
                                                                        ) != '0'
                                                                    ) {
                                                                        return@label1053
                                                                    }

                                                                    lastDigit =
                                                                        requestUrl.get(splitIndex - 1)
                                                                } catch (exception11: Exception) {
                                                                    return@label1053
                                                                }

                                                                if (lastDigit < '0' || lastDigit > '9') {
                                                                    return@label1053
                                                                }
                                                            }

                                                            requestKind = 0
                                                        }

                                                        val urlLength: Int
                                                        val scanEnd: Int
                                                        try {
                                                            urlLength = requestUrl.length
                                                            scanEnd = min(splitIndex + 6, urlLength)
                                                        } catch (exception12: Exception) {
                                                            segmentKind = requestKind.toInt()
                                                            return@label991
                                                        }

                                                        var extLength = 0

                                                        for (pos in extStart..<scanEnd) {
                                                            val char: Char
                                                            try {
                                                                char = requestUrl.get(pos)
                                                            } catch (exception13: Exception) {
                                                                segmentKind = requestKind.toInt()
                                                                return@label991
                                                            }

                                                            if (char < 'a' || char > 'z') {
                                                                break
                                                            }

                                                            extLength++
                                                        }

                                                        segmentKind = requestKind.toInt()
                                                        if (extLength == 0) {
                                                            return@label991
                                                        }

                                                        val segmentExt: String?
                                                        try {
                                                            segmentExt = requestUrl.substring(
                                                                extStart,
                                                                extStart + extLength
                                                            )
                                                        } catch (exception14: Exception) {
                                                            segmentKind = requestKind.toInt()
                                                            return@label991
                                                        }

                                                        run label936@{
                                                            val items3: MutableList<Any?>?
                                                            val items4: MutableList<Any?>?
                                                            try {
                                                                items = activity.dialogsState.segmentLengths
                                                                items3 = activity.dialogsState.segmentSplits
                                                                items4 = activity.dialogsState.segmentUrls
                                                            } catch (exception15: Exception) {
                                                                return@label936
                                                            }
                                                            run label933@{
                                                            if (items != null) {
                                                                try {
                                                                    extLength = items!!.size
                                                                } catch (exception16: Exception) {
                                                                    return@label933
                                                                }

                                                                for (slot in 0..<extLength) {
                                                                    try {
                                                                        if (urlLength != items!!.get(
                                                                                slot
                                                                            ) as Int? || splitIndex != items3!!.get(
                                                                                slot
                                                                            ) as Int?
                                                                        ) {
                                                                            continue
                                                                        }

                                                                        sameAdsPage =
                                                                            segmentExt == items4!!.get(
                                                                                slot
                                                                            )
                                                                    } catch (exception17: Exception) {
                                                                        break
                                                                    }

                                                                    if (sameAdsPage) {
                                                                        segmentKind = 2
                                                                        return@label991
                                                                    }
                                                                }
                                                            }
                                                            }
                                                        }

                                                        val items2: ArrayList<Any?>?
                                                        var urlList: MutableList<Any?>?
                                                        var splitList: MutableList<Any?>?
                                                        try {
                                                            items2 = activity.dialogsState.segmentLengths
                                                            splitList = activity.dialogsState.segmentSplits
                                                            urlList = activity.dialogsState.segmentUrls
                                                        } catch (exception18: Exception) {
                                                            segmentKind = requestKind.toInt()
                                                            return@label991
                                                        }

                                                        var lengthList = items2
                                                        if (items2 == null) {
                                                            try {
                                                                lengthList = ArrayList<Any?>()
                                                                splitList = ArrayList<Any?>()
                                                                urlList = ArrayList<Any?>()
                                                            } catch (exception19: Exception) {
                                                                segmentKind = requestKind.toInt()
                                                                return@label991
                                                            }
                                                        }

                                                        try {
                                                            lengthList.add(urlLength)
                                                            splitList!!.add(splitIndex)
                                                            urlList!!.add(segmentExt)
                                                            activity.dialogsState.segmentLengths = lengthList
                                                            activity.dialogsState.segmentSplits = splitList
                                                            activity.dialogsState.segmentUrls = urlList
                                                        } catch (exception20: Exception) {
                                                            segmentKind = requestKind.toInt()
                                                            return@label991
                                                        }

                                                        segmentKind = requestKind.toInt()
                                                        return@label991
                                                    }
                                                    segmentKind = 0
                                                    return@label991
                                                }
                                                segmentKind = 1
                                            }

                                            splitStart = splitIndex
                                            splitIndex = segmentKind
                                            if (segmentKind == 2) {
                                                activity.dialogsState.segmentsFound = true
                                                activity.dialogsState.segmentLengths = null
                                                activity.dialogsState.segmentSplits = null
                                                activity.dialogsState.segmentUrls = null
                                                val referer = activity.refererForRequest(requestUrl, request)
                                                activity.runInBackground(WvaFakeTsCheck(wa, url = requestUrl, page = referer))
                                                return@label1003
                                            }
                                            return@label1102
                                        }
                                    } else {
                                        pathSplit = 0
                                    }

                                    splitIndex = 0
                                    splitStart = pathSplit
                                }

                                var matchedUrl: String? = null
                                run label1094@{
                                    val length = requestUrl.length
                                    matchedUrl = requestHost
                                    run label870@{
                                    if (length != 0) {
                                        var index: Int
                                        try {
                                            index = requestUrl.indexOf(63.toChar())
                                        } catch (exception21: Exception) {
                                            matchedUrl = requestHost
                                            return@label870
                                        }

                                        run label1095@{
                                            if (index < 0) {
                                                matchedUrl = requestHost

                                                try {
                                                    if (!isMediaUrl(requestUrl, true)) {
                                                        return@label870
                                                    }
                                                } catch (exception22: Exception) {
                                                    matchedUrl = requestHost
                                                    return@label870
                                                }

                                                try {
                                                    medItem6 = MedItem(requestUrl, null)
                                                } catch (exception23: Exception) {
                                                    matchedUrl = requestHost
                                                    return@label870
                                                }

                                                medItem = medItem6
                                            } else {
                                                run label1088@{
                                                    val queryStart = index + 1
                                                    run label1066@{
                                                        try {
                                                            if (requestUrl.indexOf(
                                                                    "http",
                                                                    queryStart
                                                                ) >= queryStart
                                                            ) {
                                                                return@label1066
                                                            }
                                                        } catch (exception24: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }
                                                        run label1067@{
                                                            try {
                                                                if (isMediaUrl(requestUrl, true)) {
                                                                    return@label1067
                                                                }
                                                            } catch (exception25: Exception) {
                                                                matchedUrl = requestHost
                                                                return@label870
                                                            }
                                                            try {
                                                                if (streamKeyIndex(queryStart, requestUrl) < queryStart) {
                                                                    return@label1095
                                                                }
                                                            } catch (exception26: Exception) {
                                                                matchedUrl = requestHost
                                                                return@label870
                                                            }

                                                            try {
                                                                medItem2 = MedItem(
                                                                    "m3fake:" + requestUrl,
                                                                    null
                                                                )
                                                            } catch (exception27: Exception) {
                                                                matchedUrl = requestHost
                                                                return@label870
                                                            }

                                                            medItem = medItem2
                                                            return@label1088
                                                        }

                                                        try {
                                                            medItem3 = MedItem(requestUrl, null)
                                                        } catch (exception28: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        medItem = medItem3
                                                        return@label1088
                                                    }

                                                    run label848@{
                                                        var paramEnd = queryStart
                                                        label847@ while (paramEnd < length) {
                                                            val index5: Int
                                                            try {
                                                                index5 = requestUrl.indexOf(
                                                                    "http",
                                                                    paramEnd
                                                                )
                                                            } catch (exception29: Exception) {
                                                                matchedUrl = requestHost
                                                                return@label870
                                                            }

                                                            if (index5 < paramEnd) {
                                                                break
                                                            }

                                                            val afterHttp = index5 + 4

                                                            var itemIndex: Int
                                                            try {
                                                                itemIndex = requestUrl.indexOf(
                                                                    38.toChar(),
                                                                    afterHttp
                                                                )
                                                            } catch (exception30: Exception) {
                                                                matchedUrl = requestHost
                                                                return@label870
                                                            }

                                                            paramEnd = itemIndex
                                                            if (itemIndex < afterHttp) {
                                                                paramEnd = length
                                                            }

                                                            val mediaExts: Array<String>?
                                                            try {
                                                                mediaExts = MainUrl.mediaExts
                                                            } catch (exception31: Exception) {
                                                                matchedUrl = requestHost
                                                                return@label870
                                                            }

                                                            itemIndex = 0
                                                            while (itemIndex < 5) {
                                                                val ext = mediaExts[itemIndex]

                                                                val index7: Int
                                                                try {
                                                                    index7 = indexOfWord(
                                                                        requestUrl,
                                                                        afterHttp,
                                                                        paramEnd,
                                                                        ext
                                                                    )
                                                                } catch (exception32: Exception) {
                                                                    matchedUrl = requestHost
                                                                    return@label870
                                                                }

                                                                if (index7 != -1) {
                                                                    run label1071@{
                                                                        try {
                                                                            if (ext != ".m3u8") {
                                                                                return@label1071
                                                                            }
                                                                        } catch (exception33: Exception) {
                                                                            matchedUrl = requestHost
                                                                            return@label870
                                                                        }
                                                                        try {
                                                                            itemIndex =
                                                                                requestUrl.lastIndexOf(
                                                                                    47.toChar(),
                                                                                    index7
                                                                                )
                                                                        } catch (exception34: Exception) {
                                                                            matchedUrl = requestHost
                                                                            return@label870
                                                                        }

                                                                        run label833@{
                                                                            run label832@{
                                                                                if (itemIndex >= 0) {
                                                                                    try {
                                                                                        if (indexOfWord(
                                                                                                requestUrl,
                                                                                                itemIndex,
                                                                                                index7,
                                                                                                "rendition"
                                                                                            ) != -1
                                                                                        ) {
                                                                                            return@label832
                                                                                        }
                                                                                    } catch (exception35: Exception) {
                                                                                        matchedUrl =
                                                                                            requestHost
                                                                                        return@label870
                                                                                    }

                                                                                    try {
                                                                                        if (indexOfWord(
                                                                                                requestUrl,
                                                                                                itemIndex,
                                                                                                index7,
                                                                                                "chunk"
                                                                                            ) != -1
                                                                                        ) {
                                                                                            return@label832
                                                                                        }
                                                                                    } catch (exception36: Exception) {
                                                                                        matchedUrl =
                                                                                            requestHost
                                                                                        return@label870
                                                                                    }
                                                                                }
                                                                                variantList = false
                                                                                return@label833
                                                                            }
                                                                            variantList = true
                                                                        }
                                                                        if (variantList) {
                                                                            break@label847
                                                                        }
                                                                    }

                                                                    try {
                                                                        matchedUrl = innerHttpUrl(
                                                                            requestUrl.substring(
                                                                                index5,
                                                                                paramEnd
                                                                            )
                                                                        )
                                                                        return@label848
                                                                    } catch (exception37: Exception) {
                                                                        matchedUrl = requestHost
                                                                        return@label870
                                                                    }
                                                                }
                                                                itemIndex++
                                                            }
                                                            paramEnd++
                                                        }
                                                        matchedUrl = null
                                                    }

                                                    run label1073@{
                                                        try {
                                                            if (!TextUtils.isEmpty(matchedUrl)) {
                                                                return@label1073
                                                            }
                                                        } catch (exception38: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }
                                                        try {
                                                            index = streamKeyIndex(queryStart, requestUrl)
                                                        } catch (exception39: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        val foundUrl: String?
                                                        if (index < queryStart) {
                                                            foundUrl = null
                                                        } else {
                                                            var index6: Int
                                                            try {
                                                                index6 = requestUrl.indexOf(
                                                                    "=http",
                                                                    index
                                                                )
                                                            } catch (exception40: Exception) {
                                                                matchedUrl = requestHost
                                                                return@label870
                                                            }

                                                            if (index6 > index && index6 + 5 < length) {
                                                                index = index6 + 1
                                                            } else {
                                                                try {
                                                                    index6 = requestUrl.lastIndexOf(
                                                                        "=http",
                                                                        index
                                                                    )
                                                                } catch (exception41: Exception) {
                                                                    matchedUrl = requestHost
                                                                    return@label870
                                                                }

                                                                if (index6 > 0 && index6 + 5 < index) {
                                                                    index = index6 + 1
                                                                } else {
                                                                    run label1105@{
                                                                        try {
                                                                            index6 =
                                                                                requestUrl.indexOf(
                                                                                    "http",
                                                                                    index
                                                                                )
                                                                        } catch (exception42: Exception) {
                                                                            matchedUrl = requestHost
                                                                            return@label870
                                                                        }
                                                                        if (index6 > index) {
                                                                            index = index6
                                                                            if (index6 + 4 < length) {
                                                                                return@label1105
                                                                            }
                                                                        }
                                                                        index = -1
                                                                    }
                                                                }
                                                            }

                                                            if (index < queryStart) {
                                                                foundUrl = requestUrl
                                                            } else {
                                                                try {
                                                                    index6 = requestUrl.indexOf(
                                                                        38.toChar(),
                                                                        index
                                                                    )
                                                                } catch (exception43: Exception) {
                                                                    matchedUrl = requestHost
                                                                    return@label870
                                                                }

                                                                if (index6 > index && index6 < length) {
                                                                    try {
                                                                        matchedUrl =
                                                                            requestUrl.substring(
                                                                                index,
                                                                                index6
                                                                            )
                                                                    } catch (exception44: Exception) {
                                                                        matchedUrl = requestHost
                                                                        return@label870
                                                                    }
                                                                } else {
                                                                    try {
                                                                        matchedUrl =
                                                                            requestUrl.substring(
                                                                                index
                                                                            )
                                                                    } catch (exception45: Exception) {
                                                                        matchedUrl = requestHost
                                                                        return@label870
                                                                    }
                                                                }

                                                                try {
                                                                    foundUrl = innerHttpUrl(matchedUrl)
                                                                } catch (exception46: Exception) {
                                                                    matchedUrl = requestHost
                                                                    return@label870
                                                                }
                                                            }
                                                        }

                                                        matchedUrl = requestHost

                                                        try {
                                                            if (TextUtils.isEmpty(foundUrl)) {
                                                                return@label870
                                                            }
                                                        } catch (exception47: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        val foundUrl2: String?
                                                        try {
                                                            foundUrl2 = findHttpUrl(length, queryStart, requestUrl)
                                                        } catch (exception48: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        try {
                                                            stringBuilder = StringBuilder()
                                                        } catch (exception49: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        try {
                                                            stringBuilder.append("m3fake:")
                                                        } catch (exception50: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        try {
                                                            stringBuilder.append(foundUrl)
                                                        } catch (exception51: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        try {
                                                            medItem4 = MedItem(
                                                                stringBuilder.toString(),
                                                                foundUrl2
                                                            )
                                                        } catch (exception52: Exception) {
                                                            matchedUrl = requestHost
                                                            return@label870
                                                        }

                                                        medItem = medItem4
                                                        matchedUrl = requestHost
                                                        return@label1094
                                                    }

                                                    try {
                                                        medItem5 = MedItem(
                                                            matchedUrl,
                                                            findHttpUrl(length, queryStart, requestUrl)
                                                        )
                                                    } catch (exception53: Exception) {
                                                        matchedUrl = requestHost
                                                        return@label870
                                                    }

                                                    medItem = medItem5
                                                    matchedUrl = requestHost
                                                    return@label1094
                                                }
                                            }
                                            matchedUrl = requestHost
                                            return@label1094
                                        }

                                        matchedUrl = requestHost
                                    }
                                    }
                                    medItem = null
                                }

                                if (medItem != null) {
                                    val first = medItem!!.first
                                    val second = medItem!!.second
                                    if (!TextUtils.isEmpty(first)) {
                                        activity.findPageVideos(first, activity.refererForRequest(first, request), second, null)
                                    }
                                } else {
                                    run label1100@{
                                        mediaUrl = requestUrl
                                        if (isSubtitleUrl(requestUrl)) {
                                            return@label1091
                                        }

                                        run label705@{
                                            run label704@{
                                                try {
                                                    mutableMap2 = request.getRequestHeaders() as MutableMap<Any?, Any?>?
                                                } catch (exception54: Exception) {
                                                    return@label704
                                                }
                                                run label701@{
                                                if (mutableMap2 != null) {
                                                    var index: Int = 0
                                                    run label1079@{
                                                        try {
                                                            val rangeHeader =
                                                                mutableMap2!!.get("Range") as String?
                                                            if (TextUtils.isEmpty(rangeHeader) || !rangeHeader!!.startsWith(
                                                                    "bytes"
                                                                )
                                                            ) {
                                                                return@label701
                                                            }

                                                            if (isHitomi(pageHost)) {
                                                                if (requestUrl.length < 11) {
                                                                    return@label705
                                                                }

                                                                index = requestUrl.indexOf(
                                                                    47.toChar(),
                                                                    10
                                                                )
                                                                return@label1079
                                                            }
                                                        } catch (exception55: Exception) {
                                                            return@label701
                                                        }
                                                        try {
                                                            sameAdsPage = TextUtils.isEmpty(pageHost)
                                                        } catch (exception56: Exception) {
                                                            return@label701
                                                        }

                                                        run label1096@{
                                                            run label677@{
                                                            if (!sameAdsPage) {
                                                                try {
                                                                    if (!pageHost.endsWith("dcinside.com") && !pageHost.endsWith(
                                                                            "dcinside.co.kr"
                                                                        )
                                                                    ) {
                                                                        return@label677
                                                                    }
                                                                } catch (exception57: Exception) {
                                                                    return@label701
                                                                }

                                                                isDcinside = true
                                                                return@label1096
                                                            }
                                                            }
                                                            isDcinside = false
                                                        }

                                                        if (!isDcinside) {
                                                            return@label705
                                                        }

                                                        run label670@{
                                                            run label1082@{
                                                                try {
                                                                    if (!TextUtils.isEmpty(
                                                                            matchedUrl
                                                                        )
                                                                    ) {
                                                                        return@label1082
                                                                    }

                                                                    host = hostOf(requestUrl, true)
                                                                } catch (exception58: Exception) {
                                                                    return@label701
                                                                }
                                                                hostKey = host
                                                                try {
                                                                    if (TextUtils.isEmpty(host)) {
                                                                        return@label705
                                                                    }
                                                                    return@label670
                                                                } catch (exception59: Exception) {
                                                                    return@label701
                                                                }
                                                            }
                                                            hostKey = matchedUrl
                                                        }

                                                        try {
                                                            if (!hostKey!!.endsWith("dcinside.com") && !hostKey!!.endsWith(
                                                                    "dcinside.co.kr"
                                                                )
                                                            ) {
                                                                return@label705
                                                            }
                                                        } catch (exception60: Exception) {
                                                            return@label701
                                                        }

                                                        try {
                                                            index = hostKey!!.length
                                                            if (!requestUrl.startsWith(
                                                                    "dccon",
                                                                    index + 1
                                                                )
                                                            ) {
                                                                return@label705
                                                            }

                                                            index = requestUrl.indexOf(
                                                                ".php",
                                                                index + 6
                                                            )
                                                        } catch (exception61: Exception) {
                                                            return@label701
                                                        }

                                                        if (index == -1) {
                                                            return@label705
                                                        }
                                                        return@label701
                                                    }

                                                    if (index < 10) {
                                                        return@label705
                                                    }

                                                    index++

                                                    try {
                                                        if (!requestUrl.startsWith(
                                                                "index-all.nozomi",
                                                                index
                                                            )
                                                            && !requestUrl.startsWith(
                                                                "languagesindex/languages.",
                                                                index
                                                            )
                                                        ) {
                                                            return@label705
                                                        }
                                                    } catch (exception62: Exception) {
                                                    }
                                                }
                                                }
                                            }
                                            if (splitIndex == 1) {
                                                var index: Int
                                                try {
                                                    index =
                                                        requestUrl.lastIndexOf(47.toChar(), splitStart)
                                                } catch (exception63: Exception) {
                                                    return@label1100
                                                }

                                                if (index > 0) {
                                                    if (++index < splitStart) {
                                                        index = splitStart - index
                                                        if (index > 0 && index < 5) {
                                                            mediaUrl = "m3fake:" + requestUrl
                                                            return@label1091
                                                        }
                                                    }
                                                }
                                            }
                                            return@label1100
                                        }

                                        mediaUrl = "m3fake:" + requestUrl
                                        return@label1091
                                    }
                                }
                            } else if (!activity.dialogsState.segmentsFound) {
                                activity.dialogsState.segmentsFound = true
                                activity.dialogsState.segmentLengths = null
                                activity.dialogsState.segmentSplits = null
                                activity.dialogsState.segmentUrls = null
                                val currentPage4 = activity.currentPage
                                if (currentPage4 != null && !currentPage4.hasM3u8) {
                                    mediaUrl = "tsfile:" + requestUrl
                                    return@label1091
                                }
                            }
                            }
                        }

                        mediaUrl = null
                        return@label1091
                    }
                    }
                    mediaUrl = null
                }

                if (!TextUtils.isEmpty(mediaUrl) && !TextUtils.isEmpty(mediaUrl)) {
                    activity.findPageVideos(mediaUrl, activity.refererForRequest(mediaUrl, request), null, null)
                }
            }
        }

        return null
    }

    override fun shouldOverrideUrlLoading(
        webView: WebView?,
        webResourceRequest: WebResourceRequest?
    ): kotlin.Boolean {
        val webViewActivity = wa
        // link clicks / redirects: load the address without tracking parameters ($removeparam rules) instead
        if (webView != null && webViewActivity.currentPage != null) {
            val clean = com.mycompany.app.web.filter.UboFilter.cleanNavigation(webViewActivity.dialogsState.appContext, webResourceRequest)
            if (clean != null) {
                webView.loadUrl(clean)
                return true
            }
        }
        // A server redirect (computerbase.de -> www.computerbase.de, Google's /url?q= link -> the site) is no new page:
        // load it in place. Handled like a tap, the different host got a page of its own, and Back then landed on the
        // redirect step, which never draws anything (a white page). Other schemes (intent:, market:) still go below.
        if (webResourceRequest != null && webResourceRequest.isRedirect && webResourceRequest.isForMainFrame) {
            val scheme = webResourceRequest.url?.scheme
            if (scheme.equals("http", true) || scheme.equals("https", true)) return false
        }
        if (webViewActivity.currentPage != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
            val source = webResourceRequest.getUrl().toString()
            webViewActivity.syncJsBridge(webView, source)
            flushCookies(webViewActivity.dialogsState.appContext, true)
            if (!webViewActivity.isPaused && webViewActivity.dialogsState.loadDialogKind.toInt() == 0) {
                if (webViewActivity.videoFrame == null) {
                    return webViewActivity.handleUrlOverride(source)
                }
                val videoMode = webViewActivity.dialogsState.videoMode
                if (videoMode.toInt() == 2) {
                    return false
                }
                if (videoMode.toInt() == 3 || videoMode.toInt() == 5) {
                    return webViewActivity.handleUrlOverride(source)
                }
            }
        }
        return true
    }
}

/**
 * Opens the Web Filter dialog for the current page ([DialogSetAdblock]: what the filter did on the page, the ad-block
 * switches and the settings icon); used by the menu's shield and the "Ad blocker" action. False when it could not open.
 */
internal fun WebViewActivity.openAdblockDialog(): Boolean {
    if (currentPage == null) return false
    closeMenuDialogs()
    if (isClosing || isAnyDialogOpen()) return false
    setAdblockDialog?.let {
        it.dismiss()
        setAdblockDialog = null
    }
    val dialog = DialogSetAdblock(this, currentPageUrl(false), WvaAdsDialogListener(this), currentPage)
    setAdblockDialog = dialog
    dialog.setOnDismissListener(WvaSetAdblockDialogDismissed(this))
    return true
}
