@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import android.view.View

import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.compress.Compress.Companion.isImageName
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.imageUrlFallback
import com.mycompany.app.main.MainUtil.hostLabelUpper
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.main.MainUtil.isHitomi
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.applyDesktopMode
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.youTubeVideoId
import com.mycompany.app.main.MainUtil.translateUrlFor
import com.mycompany.app.main.MainUtil.pipAspectRatio
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.applyWindowBrightness
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.toggleSystemBars
import com.mycompany.app.main.MainUtil.applySystemBarsMode
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.youTubeAutoplayJs
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.main.MainUtil.isHitomiReader
import com.mycompany.app.main.MainUtil.isPdfUrl
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.compress.Compress.Companion.isVideoExt
import com.mycompany.app.compress.Compress.Companion.isAudioExt
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
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
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.db.book.DbBookDown.Companion.pathById
import com.mycompany.app.db.book.DbBookDown.Companion.urlById
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.web.WebNestView
import com.mycompany.app.main.MainActivity
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import android.content.Intent
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.web.WebVideoFrame
import com.mycompany.app.web.WebVideoFrame.VideoFrameListener
import com.mycompany.app.dialog.DialogDownFont
import com.mycompany.app.web.WebViewActivity.PopItem
import com.mycompany.app.wview.WebMoveText
import com.mycompany.app.wview.WebAreaText
import com.mycompany.app.wview.WebAreaMenu
import com.mycompany.app.wview.WebAreaView
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.main.MainActivity.MainHeightListener
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.main.MainDownSvc
import android.content.BroadcastReceiver
import com.mycompany.app.db.book.DbBookDown
import android.speech.tts.UtteranceProgressListener
import com.mycompany.app.data.book.DataBookPop
import android.content.Context
import android.os.Handler
import ch.opumlabs.opumbrowser.R
import java.lang.Exception
import java.net.URLConnection
import java.net.URLDecoder

/*
 * The browser activity's video callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaWindowHeightCheck(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class StatusBarCheckStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (BrowserSettings.showStatus.value) {
                MainSettings.editLocal { set(MainSettings.statusConfirmed, false) }
            }
            if (BrowserSettings.showNavi.value) {
                MainSettings.editLocal { set(MainSettings.naviConfirmed, false) }
                MainSettings.editLocal { set(MainSettings.naviHeight, 0) }
            }
            updateCutoutInsets(webViewActivity.dialogsState.appContext, webViewActivity.getWindow(), webViewActivity.decorViewCached())
            val handler: Handler? = if (webViewActivity.videoFrame != null) null else webViewActivity.mainHandler
            if (webViewActivity.videoFrame != null || handler == null) {
                return
            }
            handler.post(WvaUpdateSystemBarPadding(webViewActivity))
        }
    }

    override fun run() {
        wa.runInBackground(StatusBarCheckStep())
    }
}

class WvaWindowHeightListener(@JvmField val wa: WebViewActivity) : MainHeightListener {
    override fun onHeightChanged() {
        val webViewActivity = wa
        val handler: Handler? = if (webViewActivity.videoFrame != null) null else webViewActivity.mainHandler
        if (webViewActivity.videoFrame != null || handler == null) {
            return
        }
        handler.post(WvaUpdateSystemBarPadding(webViewActivity))
    }
}

class WvaPageFrameListenerTask(@JvmField val wa: WebViewActivity, @JvmField val pageFrameListenerValue: Int = 0) : Runnable {

    override fun run() {
        val themeColor: Int
        val themeLight: Int
        val webViewActivity = wa
        val pageFrameListenerValue = this@WvaPageFrameListenerTask.pageFrameListenerValue
        var guideDown = false
        if (webViewActivity.tabsState.currentFrame == null) {
            webViewActivity.videoState.pageFrameBusy = false
            return
        }
        if (webViewActivity.videoFrame != null) {
            webViewActivity.exitVideoFrame()
        }
        webViewActivity.stopWebClean()
        webViewActivity.hidePageOverlays(true)
        webViewActivity.hideKeyboard()
        webViewActivity.finishProgress()
        webViewActivity.exitFind(false)
        webViewActivity.settleSwipes()
        val pageIndex = webViewActivity.tabsState.currentFrame!!.pageIndex
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            guideDown = webNestView.deskMode
            themeColor = webViewActivity.pageThemeColor()
            themeLight = webViewActivity.pageThemeLight()
        } else {
            themeColor = 0
            themeLight = 0
        }
        webViewActivity.pauseWebDestroy()
        val currentPage = WebNestView(webViewActivity)
        webViewActivity.currentPage = currentPage
        webViewActivity.attachWebDestroy(currentPage)
        webViewActivity.currentPage!!.setDeskMode(guideDown)
        webViewActivity.currentPage!!.setBarColors(themeColor, themeLight)
        val webNestFrame = webViewActivity.tabsState.currentFrame
        val currentPageNow = webViewActivity.currentPage
        if (webNestFrame != null && currentPageNow != null) {
            webNestFrame.attachPage(currentPageNow, pageFrameListenerValue)
            webNestFrame.releasePage(webNestFrame.pageItemAt(pageIndex), currentPageNow, true)
        }
        webViewActivity.updateScrollBar()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaPageFrameSettingsTask(webViewActivity))
    }
}

class WvaLocalWebViewClientTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val retrying: Boolean
        val webViewActivity = wa
        if (!webViewActivity.dialogsState.isVideoPage || webViewActivity.dialogsState.videoRateOff) {
            retrying = false
        } else if (sameObject(webViewActivity.videoState.videoCheckUrl, webViewActivity.dialogsState.pageUrl)) {
            val webNestView: WebNestView? = if (ZtwoSettings.youUnmute.value) webViewActivity.currentPage else null
            if (ZtwoSettings.youUnmute.value && webNestView != null) {
                evalJs(
                    webNestView,
                    "(function(){var ele=document.querySelector(\"button[class*='ytp-unmute']\");if(ele){ele.click();}})();",
                    true
                )
            }
            retrying = false
        } else {
            webViewActivity.videoState.videoCheckUrl = webViewActivity.dialogsState.pageUrl
            youTubeAutoplayJs(webViewActivity.currentPage)
            retrying = webViewActivity.retryVideoRate()
        }
        webViewActivity.videoState.rateRetryPending = retrying
        if (retrying) {
            return
        }
        webViewActivity.resumeWebDestroy()
    }
}

class WvaShowVideoDownDialog(@JvmField val wa: WebViewActivity, @JvmField val pageUrl: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val pageUrl = this@WvaShowVideoDownDialog.pageUrl
        if (webViewActivity.isClosing || webViewActivity.isAnyDialogOpen() || TextUtils.isEmpty(pageUrl) || webViewActivity.videoFrame != null) {
            return
        }
        webViewActivity.closeDownFontDialog()
        val dialogDownFont = DialogDownFont(
            webViewActivity,
            pageUrl,
            webViewActivity.dialogsState.pageUrl,
            WvaVideoDownListener(webViewActivity)
        )
        webViewActivity.dialogsState.downFontDialog = dialogDownFont
        dialogDownFont.setOnDismissListener(DownFontDialogDismissed(webViewActivity))
    }
}

class WvaVideoEnterTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class VideoEnterStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.videoFrame == null) {
                webViewActivity.videoState.videoEnterBusy = false
                return
            }
            webViewActivity.dialogsState.showStatusBar = false
            webViewActivity.videoState.showNavBar = false
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                val settings = webNestView.getSettings()
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
            }
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaVideoEnterBarColorsTask(webViewActivity))
        }
    }

    override fun run() {
        val webViewActivity = wa
        webViewActivity.pageState.addressEditBusy = false
        webViewActivity.setAddressEditing(false)
        if (TtsSettings.handMode.value) {
            TtsSettings.edit(webViewActivity.dialogsState.appContext) { set(TtsSettings.handMode, false) }
            webViewActivity.updateHandListener()
            webViewActivity.applyTopInset(false)
            webViewActivity.removeHandModeBar()
        }
        webViewActivity.hideFloatingButtons()
        webViewActivity.closeAllSnackbars()
        webViewActivity.stopTts()
        webViewActivity.exitFind(true)
        webViewActivity.hidePageOverlays(true)
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(VideoEnterStep())
    }
}

class WvaVideoFrameListener(@JvmField val wa: WebViewActivity) : VideoFrameListener {
    override fun activity(): WebViewActivity {
        return wa
    }

    internal inner class PipFrameStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webVideoPip = webViewActivity.videoState.videoPip
            if (webVideoPip != null) {
                val myWebBody = webViewActivity.dialogsState.webBody
                val pageUrl = webViewActivity.dialogsState.pageUrl
                if (myWebBody != null) {
                    webVideoPip.layoutRtl = isRtl(webVideoPip.appContext)
                    webVideoPip.hostActivity = webViewActivity
                    webVideoPip.parentBody = myWebBody
                    webVideoPip.pageUrl = pageUrl
                    webVideoPip.videoId = youTubeVideoId(pageUrl)
                    webVideoPip.playState = 0.toByte()
                    webVideoPip.setBackgroundColor(-16777216)
                    webVideoPip.parentBody!!.addView(webVideoPip, -1, -1)
                    if (webVideoPip.appContext == null) {
                        return
                    }
                    webVideoPip.post(webVideoPip.AttachWebTask())
                }
            }
        }
    }

    override fun onDownload() {
        val guideDown = ReadSettings.guideDown.value
        val webViewActivity = wa
        if (guideDown) {
            webViewActivity.showDownGuideDialog()
        } else {
            webViewActivity.runDownAction(0)
        }
    }

    fun onFrame(position: Long) {
        val webVideoPip = wa.videoState.videoPip
        if (webVideoPip != null) {
            webVideoPip.setYouPos(position)
        }
    }
}

class WvaVideoEnterBarColorsTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class BlackBarsStep : Runnable {

        internal inner class VideoEnterCheckStep : Runnable {

            internal inner class CaptureBarColorsStep : Runnable {
                override fun run() {
                    val webViewActivity = wa
                    if (webViewActivity.videoFrame == null) {
                        webViewActivity.videoState.videoEnterBusy = false
                        return
                    }
                    webViewActivity.captureBarColors(webViewActivity.getWindow(), -16777216, -16777216)
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(WvaVideoEnterCutoutTask(webViewActivity))
                }
            }

            override fun run() {
                val wvaVideoEnterBarColorsTask = this@WvaVideoEnterBarColorsTask
                val webViewActivity = wa
                if (webViewActivity.videoFrame == null) {
                    webViewActivity.videoState.videoEnterBusy = false
                    return
                }
                webViewActivity.applySystemBarsBehavior(false)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(CaptureBarColorsStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.videoFrame == null) {
                webViewActivity.videoState.videoEnterBusy = false
                return
            }
            webViewActivity.updateSystemBarPadding(false, false)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(VideoEnterCheckStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.videoFrame == null) {
            webViewActivity.videoState.videoEnterBusy = false
            return
        }
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody != null) {
            myWebBody.setFilterColor(0)
        }
        val keyHelper = webViewActivity.keyHelper
        if (keyHelper != null) {
            keyHelper.applyMode(false, false)
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(BlackBarsStep())
    }
}

class WvaVideoEnterSystemBarsTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class VideoEnterBarsStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.videoFrame == null) {
                webViewActivity.videoState.videoEnterBusy = false
            } else {
                toggleSystemBars(
                    webViewActivity.getWindow(),
                    webViewActivity.decorViewCached(),
                    webViewActivity.insetsControllerCompat(),
                    webViewActivity.dialogsState.showStatusBar,
                    webViewActivity.videoState.showNavBar
                )
                webViewActivity.setUpVideoFrame()
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.videoFrame == null) {
            webViewActivity.videoState.videoEnterBusy = false
            return
        }
        applySystemBarsMode(
            webViewActivity.getWindow(),
            webViewActivity.decorViewCached(),
            webViewActivity.insetsControllerCompat(),
            webViewActivity.dialogsState.showStatusBar,
            webViewActivity.videoState.showNavBar
        )
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(VideoEnterBarsStep())
    }
}

class WvaExitVideoFrameTask(@JvmField val wa: WebViewActivity, @JvmField val exitedVideoFrame: com.mycompany.app.web.WebVideoFrame? = null, @JvmField val exitedVideoType: Int = 0, @JvmField val videoWasImage: Boolean = false) : Runnable {

    internal inner class ExitVideoFrameStep : Runnable {

        internal inner class ReloadDesktopModeStep : Runnable {

            internal inner class RestoreBrightnessStep : Runnable {

                internal inner class ExitRotationLockStep : Runnable {

                    internal inner class RestorePopupSettingsStep : Runnable {
                        override fun run() {
                            val webViewActivity = wa
                            if (webViewActivity.videoFrame != null) {
                                webViewActivity.videoState.videoExitBusy = false
                                return
                            }
                            val webNestView = webViewActivity.currentPage
                            if (webNestView != null) {
                                val settings = webNestView.getSettings()
                                settings.setJavaScriptCanOpenWindowsAutomatically(true)
                                settings.setSupportMultipleWindows(true)
                            }
                            val myWebCoord = webViewActivity.webCoord
                            if (myWebCoord == null) {
                                return
                            }
                            myWebCoord.post(WvaExitVideoDarkTask(webViewActivity))
                        }
                    }

                    override fun run() {
                        val reloadDesktopModeStep = this@ReloadDesktopModeStep
                        val webViewActivity = wa
                        if (webViewActivity.videoFrame != null) {
                            webViewActivity.videoState.videoExitBusy = false
                            return
                        }
                        applyRotationLock(this@WvaExitVideoFrameTask.exitedVideoType, webViewActivity, this@WvaExitVideoFrameTask.videoWasImage)
                        val myWebCoord = wa.webCoord
                        if (myWebCoord == null) {
                            return
                        }
                        myWebCoord.post(RestorePopupSettingsStep())
                    }
                }

                override fun run() {
                    val exitVideoFrameStep = this@ExitVideoFrameStep
                    val webViewActivity = wa
                    if (webViewActivity.videoFrame != null) {
                        webViewActivity.videoState.videoExitBusy = false
                        return
                    }
                    if (!this@WvaExitVideoFrameTask.videoWasImage) {
                        applyWindowBrightness(webViewActivity.getWindow(), PdfSettings.brightness.value, PdfSettings.userBrightness.value)
                    }
                    wa.runInBackground(ExitRotationLockStep())
                }
            }

            override fun run() {
                val wvaExitVideoFrameTask = this@WvaExitVideoFrameTask
                val webViewActivity = wa
                if (webViewActivity.videoFrame != null) {
                    webViewActivity.videoState.videoExitBusy = false
                    return
                }
                reloadWithDesktopMode(webViewActivity.currentPage, this@WvaExitVideoFrameTask.exitedVideoType, this@WvaExitVideoFrameTask.videoWasImage)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(RestoreBrightnessStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.closeDownGuideDialog()
            webViewActivity.closeDownUrlDialog()
            webViewActivity.closeSetDownDialog()
            webViewActivity.closeVideoListDialog()
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(ReloadDesktopModeStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        val webVideoFrame = this@WvaExitVideoFrameTask.exitedVideoFrame
        if (webVideoFrame != null) {
            webVideoFrame.releaseFrame()
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ExitVideoFrameStep())
    }
}

class WvaExitVideoDarkTask(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.videoFrame != null) {
            webViewActivity.videoState.videoExitBusy = false
            return
        }
        val videoExitDark = webViewActivity.videoExitDark
        val darkUi = AppState.darkUi
        if (videoExitDark != darkUi) {
            webViewActivity.videoExitDark = darkUi
            webViewActivity.refreshThemeColors()
        } else {
            webViewActivity.applyBodyColors(webViewActivity.pageThemeColor(), webViewActivity.navBarColorNow())
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaVideoExitRestoreBars(webViewActivity))
    }
}

class WvaVideoExitRestoreBars(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.videoFrame != null) {
            webViewActivity.videoState.videoExitBusy = false
        } else {
            webViewActivity.setSystemBarsHidden(BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, true)
            webViewActivity.videoState.videoExitBusy = false
        }
    }
}

class WvaPopupUrlTask(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val referer: String? = null) : Runnable {

    internal inner class OpenPopItemStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val popItem = webViewActivity.videoState.popItem
            webViewActivity.videoState.popItem = null
            if (popItem == null) {
                webViewActivity.destroyPendingPage()
                return
            }
            val type = popItem.type
            val offlineUrl = popItem.url
            var referer = popItem.referer
            val intentUrl = popItem.intentUrl
            if (TextUtils.isEmpty(referer)) {
                referer = webViewActivity.dialogsState.pageUrl
            }
            webViewActivity.finishProgress()
            if (type.toInt() == 4) {
                webViewActivity.videoState.pendingTabUrl = referer
                webViewActivity.addTabInGroup(null, offlineUrl, true, WvaTabAddFailedReset(webViewActivity))
                return
            }
            if (type.toInt() == 9) {
                webViewActivity.videoState.pendingTabUrl = referer
                val webNestView = webViewActivity.pageState.pendingPage
                if (webNestView != null) {
                    webNestView.offlineUrl = offlineUrl
                    webNestView.runInBackground(webNestView.LoadHtmlTask())
                }
                webViewActivity.addTabInGroup(null, offlineUrl, true, WvaTabAddFailedReset(webViewActivity))
                return
            }
            webViewActivity.destroyPendingPage()
            if (type.toInt() == 2) {
                webViewActivity.openInCurrentPage(offlineUrl, referer)
                return
            }
            if (type.toInt() == 5) {
                webViewActivity.showPopupBlockedSnackbar(offlineUrl, referer)
                return
            }
            if (type.toInt() == 6) {
                webViewActivity.pageState.popupTitle = referer
                webViewActivity.openDownloadDialog(offlineUrl, null, "application/pdf", 0L, 2, null)
                return
            }
            if (type.toInt() == 8) {
                webViewActivity.addBlockCount(1, true)
                return
            }
            if (type.toInt() != 10) {
                if (type.toInt() == 12) {
                    webViewActivity.requestImagePreview(offlineUrl, referer, true)
                }
            } else {
                if (PdfSettings.appBlock.value != 0) {
                    webViewActivity.showAppBlockedSnackbar(offlineUrl, intentUrl)
                    return
                }
                val handler = webViewActivity.mainHandler
                if (handler != null) {
                    handler.post(WvaPopupOpenIntentTask(webViewActivity, url = offlineUrl))
                }
            }
        }
    }

    /**
     * Decides what to do with a url a page opened in a new window (wa.Wi, from page wa.Xi) and hands the WebViewActivity.PopItem to
     * OpenPopItemStep: 10 = app/intent link (with its fallback), 9 = registered link handler, 4 = open as a
     * new tab (media, allowed popups), 2/8 = special-page links, 12 = data image, 6 = pdf, 5 = blocked popup.
     * A null item (empty url, Google favicon) just closes the popup state.
     */
    override fun run() {
        val activity = wa
        val url = this@WvaPopupUrlTask.url
        val referer = this@WvaPopupUrlTask.referer
        val item = if (TextUtils.isEmpty(url)) null else classify(activity, url)
        if (item != null) {
            item.referer = referer
        }
        activity.videoState.popItem = item
        if (activity.mainHandler != null) {
            activity.mainHandler!!.post(OpenPopItemStep())
        }
    }

    private fun pop(type: Int, url: String?): WebViewActivity.PopItem {
        val item = WebViewActivity.PopItem()
        item.type = type.toByte()
        item.url = url
        return item
    }

    private fun classify(activity: WebViewActivity?, url: String?): WebViewActivity.PopItem? {
        var url = url
        if (!hasKnownScheme(url)) {
            var target = if (url!!.contains("yahoo.co.jp")) intentToUrl(url, true) else null
            if (PdfSettings.appBlock.value == 1 && TextUtils.isEmpty(target)) {
                target = intentToUrl(url, false)
            }
            if (TextUtils.isEmpty(target)) {
                val item = pop(10, url)
                item.intentUrl = intentToUrl(url, false)
                return item
            }
            url = target
        }
        if (url!!.startsWith("http") && !url.startsWith("s://", 4) && !url.startsWith("://", 4)) {
            try {
                url = URLDecoder.decode(url, "UTF-8")
            } catch (ignored: Exception) {
            }
        }
        if (DataBookLink.instance(activity!!.dialogsState.appContext).hasPage(url)) {
            return pop(9, url)
        }
        val host = hostOf(url, false)
        if (isHitomiReader(url, host)) {
            return pop(4, url)
        }
        if (isHitomi(activity.dialogsState.pageHost)) {
            return pop(if (isHitomi(host)) 2 else 8, url)
        }
        if (url!!.startsWith("data:image/")) {
            return pop(12, url)
        }
        if (isPdfUrl(url)) {
            return pop(6, url)
        }
        val media = mediaKind(url, host)
        if (media == 4) {
            return if ("https://www.google.com/favicon.ico" == url) null else pop(4, url)
        }
        if (media != 0 || BrowserSettings.popBlock.value == 0 || DataBookPop.instance(activity.dialogsState.appContext)
                .hasSiteOf(activity.dialogsState.pageUrl, activity.dialogsState.pageHost)
            || exempt(activity.dialogsState.pageHost) || exempt(host)
        ) {
            return pop(4, url)
        }
        var block = BrowserSettings.popBlock.value == 3 || (BrowserSettings.popBlock.value == 2 && !sameSite(activity.dialogsState.pageHost, host))
        if (!block) {
            // uBlock Origin-compatible engine: the lists' $popup rules, blocked addresses, ad-like third-party targets
            block = com.mycompany.app.web.filter.UboFilter.checkPopup(url, activity.dialogsState.pageUrl) == true
        }
        return pop(if (block) 5 else 4, url)
    }

    /** x.com and the hosts in `MainConst.knownSites` never have their popups blocked.  */
    private fun exempt(host: String?): Boolean {
        if (TextUtils.isEmpty(host)) {
            return false
        }
        if (host == "x.com") {
            return true
        }
        for (part in MainConst.knownSites) {
            if (host!!.contains(part!!)) {
                return true
            }
        }
        return false
    }

    /** 4 image, 5 video, 6 audio or other playable file, 0 none (from the url's file type).  */
    private fun mediaKind(url: String?, host: String?): Int {
        try {
            if (TextUtils.isEmpty(url) || (!TextUtils.isEmpty(host) && host!!.endsWith("facebook.com"))) {
                return 0
            }
            val lower = url!!.lowercase()
            if (lower.contains("view_img.php?") || lower.contains("view_image.php?")) {
                return 4
            }
            var mime = URLConnection.getFileNameMap().getContentTypeFor(lower)
            if (!TextUtils.isEmpty(mime)) {
                mime = mime.lowercase()
                if (mime.startsWith("image")) {
                    return 4
                }
                if (mime.startsWith("video")) {
                    return 5
                }
                return if (mime.startsWith("audio")) 6 else 0
            }
            val at = lower.lastIndexOf("&subject=")
            if (at == -1) {
                return 0
            }
            val file = lower.substring(0, at)
            if (TextUtils.isEmpty(file)) {
                return 0
            }
            if (isImageName(file, false, false)) {
                return 4
            }
            val dot = file.lastIndexOf('.')
            val ext = if (dot == -1) null else file.substring(dot + 1)
            if (ext != null && isVideoExt(ext)) {
                return 5
            }
            return if (ext != null && isAudioExt(ext)) 6 else 0
        } catch (exception: Exception) {
            return 0
        }
    }
}

class WvaShowDownUrlDialog(@JvmField val wa: WebViewActivity, @JvmField val fromBridge: Boolean = false, @JvmField val name: String? = null, @JvmField val source: Byte = 0, @JvmField val list: MutableList<Any?>? = null, @JvmField val reel: Boolean = false, @JvmField val mode: Byte = 0, @JvmField val url: String? = null, @JvmField val disposition: String? = null, @JvmField val mime: String? = null, @JvmField val length: Long = 0, @JvmField val type: Byte = 0, @JvmField val label: String? = null, @JvmField val faceItems: ArrayList<Any?>? = null, @JvmField val index: Int = 0, @JvmField val listItems: ArrayList<com.mycompany.app.main.MainDownAdapter.DownListItem?>? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val mode = this@WvaShowDownUrlDialog.mode
        val url = this@WvaShowDownUrlDialog.url
        val disposition = this@WvaShowDownUrlDialog.disposition
        val mime = this@WvaShowDownUrlDialog.mime
        val length = this@WvaShowDownUrlDialog.length
        val type = this@WvaShowDownUrlDialog.type
        val label = this@WvaShowDownUrlDialog.label
        val arrayList = this@WvaShowDownUrlDialog.faceItems
        val index = this@WvaShowDownUrlDialog.index
        val listItems = this@WvaShowDownUrlDialog.listItems
        val fromBridge = this@WvaShowDownUrlDialog.fromBridge
        val name = this@WvaShowDownUrlDialog.name
        val source = this@WvaShowDownUrlDialog.source
        val list = this@WvaShowDownUrlDialog.list
        val reel = this@WvaShowDownUrlDialog.reel
        if (webViewActivity.videoState.downUrlDialog == null) {
            webViewActivity.closeDownUrlDialog()
            webViewActivity.updateRssButton(false)
            val dialogDownUrl = DialogDownUrl(
                webViewActivity,
                url,
                label,
                name,
                disposition,
                mime,
                length,
                type.toInt(),
                source.toInt(),
                arrayList,
                index,
                list,
                mode.toInt(),
                WvaShowDownBlobDialog(webViewActivity)
            )
            webViewActivity.videoState.downUrlDialog = dialogDownUrl
            if (source.toInt() == 1) {
                val myWebCoord = webViewActivity.webCoord
                val webNestView = webViewActivity.currentPage
                dialogDownUrl.webCoord = myWebCoord
                dialogDownUrl.sourcePage = webNestView
                dialogDownUrl.startState = 1.toByte()
            } else if (source.toInt() == 2) {
                val webVideoFrame = webViewActivity.videoFrame
                val myWebCoord = webViewActivity.webCoord
                if (webVideoFrame != null) {
                    val webNestView = webViewActivity.currentPage
                    dialogDownUrl.webCoord = myWebCoord
                    dialogDownUrl.sourcePage = webNestView
                    dialogDownUrl.snsState = 1.toByte()
                    dialogDownUrl.startState = 2.toByte()
                } else if (reel) {
                    val webNestView = webViewActivity.currentPage
                    dialogDownUrl.webCoord = myWebCoord
                    dialogDownUrl.sourcePage = webNestView
                    dialogDownUrl.snsState = 2.toByte()
                    dialogDownUrl.startState = 2.toByte()
                } else {
                    dialogDownUrl.webCoord = myWebCoord
                    dialogDownUrl.sourcePage = null
                    dialogDownUrl.snsState = 0.toByte()
                    dialogDownUrl.startState = 2.toByte()
                }
            } else if (arrayList != null && !arrayList.isEmpty()) {
                webViewActivity.videoState.downUrlDialog!!.webCoord = webViewActivity.webCoord
            } else if (listItems != null && !listItems.isEmpty()) {
                val downUrlDialog = webViewActivity.videoState.downUrlDialog
                downUrlDialog!!.javaClass
                if (listItems.isEmpty()) {
                    downUrlDialog.loadState = 0.toByte()
                } else {
                    downUrlDialog.loadState = 1.toByte()
                }
                downUrlDialog.itemsIn = listItems
            } else if (fromBridge) {
                val downUrlDialog = webViewActivity.videoState.downUrlDialog
                if (fromBridge) {
                    downUrlDialog!!.loadState = 2.toByte()
                } else {
                    downUrlDialog!!.loadState = 0.toByte()
                }
            }
            val downUrlDialog = webViewActivity.videoState.downUrlDialog
            downUrlDialog!!.posterListener = WvaDownPosterListener(webViewActivity)
            downUrlDialog.setOnDismissListener(DownUrlDialogDismissed(webViewActivity))
            if (webViewActivity.videoFrame != null) {
                webViewActivity.videoState.downUrlDialog!!.noDim = true
            }
        }
    }
}

class WvaShowSetDownDialog(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val name: String? = null, @JvmField val dirName: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaShowSetDownDialog.url
        val name = this@WvaShowSetDownDialog.name
        val dirName = this@WvaShowSetDownDialog.dirName
        if (!webViewActivity.isClosing && !webViewActivity.isAnyDialogOpen()) {
            webViewActivity.closeSetDownDialog()
            if (webViewActivity.videoFrame != null) {
                webViewActivity.videoState.videoDialogOpen = true
                AppState.darkUi = true
            }
            val dialogSetDown = DialogSetDown(
                webViewActivity,
                url,
                dirName,
                webViewActivity.isLandscapeScreen(),
                webViewActivity.onStartPage,
                webViewActivity.useDarkWindow(),
                webViewActivity.pageThemeColor(),
                WvaSetDownListener(webViewActivity, url = url, name = name, dirName = dirName)
            )
            webViewActivity.setDownDialog = dialogSetDown
            dialogSetDown.setOnDismissListener(SetDownDialogDismissed(webViewActivity))
            if (webViewActivity.videoFrame != null) {
                webViewActivity.setDownDialog!!.noDim = true
            }
        }
        webViewActivity.miscState.requestSetDownDialogBusy = false
    }
}

class WvaAddMoveHint(@JvmField val wa: WebViewActivity) : Runnable {

    override fun run() {
        val webViewActivity = wa
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody != null) {
            if (webViewActivity.videoFrame != null) {
                webViewActivity.videoState.moveHintBusy = false
                return
            }
            val webMoveText = webViewActivity.moveHint
            if (webMoveText == null) {
                try {
                    val moveText = WebMoveText(webViewActivity)
                    webViewActivity.moveHint = moveText
                    moveText.setVisibility(View.INVISIBLE)
                    val webBody = webViewActivity.dialogsState.webBody
                    val moveHint = webViewActivity.moveHint
                    webBody!!.addView(
                        moveHint,
                        moveHint!!.getViewWidth(),
                        webViewActivity.moveHint!!.getViewHeight()
                    )
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(WvaPlaceMoveHint(webViewActivity))
                    return
                } catch (unused: Exception) {
                    webViewActivity.videoState.moveHintBusy = false
                    return
                }
            }
            val moveHintX = webViewActivity.miscState.moveHintX
            val moveHintY = webViewActivity.miscState.moveHintY
            webMoveText.show(myWebBody, moveHintX, moveHintY)
            webViewActivity.videoState.moveHintBusy = false
        }
    }
}

class WvaShowLinkTitlePill(@JvmField val wa: WebViewActivity, @JvmField val linkPillText: String? = null) : Runnable {

    override fun run() {
        val webViewActivity = wa
        val linkPillText = this@WvaShowLinkTitlePill.linkPillText
        if (webViewActivity.dialogsState.webBody != null) {
            if (webViewActivity.videoFrame != null) {
                webViewActivity.videoState.linkPillBusy = false
                return
            }
            if (TextUtils.isEmpty(linkPillText)) {
                webViewActivity.videoState.linkPillBusy = false
                return
            }
            webViewActivity.videoState.linkPillX = webViewActivity.tabsState.touchDownX.toInt()
            webViewActivity.videoState.linkPillY = webViewActivity.findState.touchDownY.toInt()
            val webAreaText = webViewActivity.linkTitlePill
            if (webAreaText == null) {
                try {
                    val linkPill = WebAreaText(webViewActivity)
                    webViewActivity.linkTitlePill = linkPill
                    linkPill.setLinkText(linkPillText)
                    webViewActivity.linkTitlePill!!.setVisibility(View.INVISIBLE)
                    val myWebBody = webViewActivity.dialogsState.webBody
                    val linkTitlePill = webViewActivity.linkTitlePill
                    myWebBody!!.addView(
                        linkTitlePill,
                        linkTitlePill!!.getViewWidth(),
                        webViewActivity.linkTitlePill!!.getViewHeight()
                    )
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord == null) {
                        return
                    }
                    myWebCoord.post(WvaPlaceLinkTitlePill(webViewActivity))
                    return
                } catch (unused: Exception) {
                    webViewActivity.videoState.linkPillBusy = false
                    return
                }
            }
            webAreaText.setLinkText(linkPillText)
            val titlePill = webViewActivity.linkTitlePill
            val myWebBody = webViewActivity.dialogsState.webBody
            val linkPillX = webViewActivity.videoState.linkPillX
            val linkPillY = webViewActivity.videoState.linkPillY
            titlePill!!.show(myWebBody, linkPillX, linkPillY)
            webViewActivity.videoState.linkPillBusy = false
        }
    }
}

class WvaAddAreaMenu(@JvmField val wa: WebViewActivity) : Runnable {

    override fun run() {
        val webViewActivity = wa
        val myWebBody = webViewActivity.dialogsState.webBody
        if (myWebBody != null) {
            if (webViewActivity.videoFrame != null) {
                webViewActivity.videoState.areaMenuBusy = false
                return
            }
            val int = webViewActivity.tabsState.touchDownX.toInt()
            webViewActivity.miscState.areaMenuX = int
            val areaMenuY = webViewActivity.findState.touchDownY.toInt()
            webViewActivity.miscState.areaMenuY = areaMenuY
            val webAreaMenu = webViewActivity.videoState.areaMenu
            if (webAreaMenu != null) {
                webAreaMenu.show(myWebBody, int, areaMenuY)
                webViewActivity.videoState.areaMenuBusy = false
                return
            }
            try {
                val areaMenu = WebAreaMenu(webViewActivity)
                webViewActivity.videoState.areaMenu = areaMenu
                areaMenu.setVisibility(View.INVISIBLE)
                webViewActivity.videoState.areaMenu!!.setFltListener(WvaAreaMenuListener(webViewActivity))
                val webBody = webViewActivity.dialogsState.webBody
                val areaMenuNow = webViewActivity.videoState.areaMenu
                webBody!!.addView(
                    areaMenuNow,
                    areaMenuNow!!.getViewWidth(),
                    webViewActivity.videoState.areaMenu!!.getViewHeight()
                )
                val myWebCoord = webViewActivity.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(WvaPlaceAreaMenu(webViewActivity))
            } catch (unused: Exception) {
                webViewActivity.videoState.areaMenuBusy = false
            }
        }
    }
}

class WvaAddAreaToolbar(@JvmField val wa: WebViewActivity) : Runnable {

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.dialogsState.areaToolbar != null || webViewActivity.dialogsState.webBody == null || webViewActivity.videoFrame != null) {
            webViewActivity.videoState.areaToolbarBusy = false
            return
        }
        try {
            val webAreaView = WebAreaView(webViewActivity)
            webViewActivity.dialogsState.areaToolbar = webAreaView
            webAreaView.setVisibility(View.INVISIBLE)
            webViewActivity.dialogsState.areaToolbar!!.setFltListener(WvaAreaToolbarListener(webViewActivity))
            val myWebBody = webViewActivity.dialogsState.webBody
            val areaToolbar = webViewActivity.dialogsState.areaToolbar
            myWebBody!!.addView(
                areaToolbar,
                areaToolbar!!.getViewWidth(),
                webViewActivity.dialogsState.areaToolbar!!.getViewHeight()
            )
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaPlaceAreaToolbar(webViewActivity))
        } catch (unused: Exception) {
            webViewActivity.videoState.areaToolbarBusy = false
        }
    }
}

class WvaHandModeLayout(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        if (TtsSettings.handMode.value) {
            val webViewActivity = wa
            if (webViewActivity.videoFrame != null || webViewActivity.dialogsState.webBody == null) {
                return
            }
            val portraitSingle = webViewActivity.isPortraitSingleWindow()
            webViewActivity.applyTopInset(portraitSingle)
            val relativeLayout = webViewActivity.dialogsState.handModeBar
            if (relativeLayout != null) {
                relativeLayout.setVisibility(if (portraitSingle) View.VISIBLE else View.GONE)
            }
        }
    }
}

class WvaTtsProgressListener(@JvmField val wa: WebViewActivity) : UtteranceProgressListener() {
    override fun onDone(utteranceId: String?) {
        com.mycompany.app.main.SpeechGuard.ended(utteranceId)
        wa.updateTtsPanel(false, false)
    }

    override fun onStop(utteranceId: String?, interrupted: Boolean) {
        com.mycompany.app.main.SpeechGuard.ended(utteranceId)
    }

    @Deprecated("Deprecated in Java")
    override fun onError(utteranceId: String?) {
        com.mycompany.app.main.SpeechGuard.ended(utteranceId)
        wa.mainHandler?.post { wa.ttsFailed() }
    }

    override fun onError(utteranceId: String?, errorCode: Int) {
        @Suppress("DEPRECATION") onError(utteranceId)
    }

    override fun onStart(utteranceId: String?) {
        com.mycompany.app.main.SpeechGuard.started(utteranceId)
        val webViewActivity = wa
        if (webViewActivity.dialogsState.ttsSpeaking && !webViewActivity.isPaused && webViewActivity.videoFrame == null) {
            webViewActivity.updateTtsPanel(true, false)
        } else {
            webViewActivity.stopTts()
        }
    }
}

class WvaLongPressFollowUp(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val imageUrl: String?
        val linkFlag: Boolean
        val webViewActivity = wa
        if (webViewActivity.videoFrame == null) {
            val longPressLinkUrl = webViewActivity.videoState.longPressLinkUrl
            if (TextUtils.isEmpty(webViewActivity.videoState.longPressSrcUrl)) {
                imageUrl = webViewActivity.quickState.longPressImage
                linkFlag = webViewActivity.quickState.longPressInspectBusy
                if (linkFlag) {
                    webViewActivity.quickState.longPressVideo = null
                }
            } else {
                imageUrl = webViewActivity.videoState.longPressSrcUrl
                linkFlag = false
            }
            if (TextUtils.isEmpty(longPressLinkUrl) && TextUtils.isEmpty(imageUrl)) {
                val longPressTitle = webViewActivity.videoState.longPressTitle
                if (TextUtils.isEmpty(longPressTitle)) {
                    val handler: Handler? = if (!BrowserSettings.areaBlock.value || webViewActivity.videoState.longPressPending) null else webViewActivity.mainHandler
                    if (!BrowserSettings.areaBlock.value || webViewActivity.videoState.longPressPending || handler == null) {
                        return
                    }
                    handler.postDelayed(WvaOfferAreaMenu(webViewActivity), 400L)
                    return
                }
                if (webViewActivity.videoState.longPressPending) {
                    return
                }
                val handler = webViewActivity.mainHandler
                if (handler != null) {
                    handler.postDelayed(WvaOfferLinkTitle(webViewActivity, linkTitleText = longPressTitle), 400L)
                    return
                }
                return
            }
            val longPressTitle = webViewActivity.videoState.longPressTitle
            webViewActivity.videoState.longPressLinkUrl = null
            webViewActivity.videoState.longPressSrcUrl = null
            webViewActivity.videoState.longPressTitle = null
            webViewActivity.videoState.webViewActivityBusy = false
            webViewActivity.quickState.longPressImage = null
            webViewActivity.videoState.longPressInspectBusy = false
            webViewActivity.quickState.longPressInspectBusy = false
            if (webViewActivity.dialogsState.longPressBusy) {
                return
            }
            webViewActivity.dialogsState.longPressBusy = true
            if (webViewActivity.videoState.longPressFollowUpBusy) {
                return
            }
            webViewActivity.videoState.longPressFollowUpBusy = true
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(WvaLongPressFollowUpTask(webViewActivity, linkFlag = linkFlag, fromFollowUp = true, linkUrl = longPressLinkUrl, imageUrl = imageUrl, title = longPressTitle))
        }
    }
}

class WvaConfigChangeRelayout(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class RelayoutStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.videoFrame == null && isDarkMode()) {
                if (TtsSettings.webLand.value == 0 && TtsSettings.webLand2.value == 0) {
                    return
                }
                val themeColor = webViewActivity.pageThemeColor()
                webViewActivity.updateWindowBgColor(themeColor, webViewActivity.pageThemeLight())
                webViewActivity.applyBodyColors(themeColor, webViewActivity.navBarColorNow())
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.videoFrame == null) {
            webViewActivity.applySystemBarsBehavior(BrowserSettings.showStatus.value)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(RelayoutStep())
        }
    }
}

class WvaApplyPageScriptsTask(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ApplyPageScriptsStep : Runnable {
        override fun run() {
            val retrying: Boolean
            val webViewActivity = wa
            if (!webViewActivity.dialogsState.isVideoPage || webViewActivity.dialogsState.videoRateOff) {
                retrying = false
            } else {
                youTubeAutoplayJs(webViewActivity.currentPage)
                retrying = webViewActivity.retryVideoRate()
            }
            webViewActivity.videoState.rateRetryPending = retrying
            if (retrying) {
                return
            }
            webViewActivity.resumeWebDestroy()
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage != null) {
            webViewActivity.applyPageStyle()
            val webNestView: WebNestView? = if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value) webViewActivity.currentPage else null
            if (!webViewActivity.onStartPage && BrowserSettings.adsBlock.value && webNestView != null) {
                webNestView.startAdsClean(webViewActivity.dialogsState.pageUrl, webViewActivity.dialogsState.pageHost, true)
            }
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ApplyPageScriptsStep())
        }
    }
}

class WvaEventReceiver(@JvmField val wa: WebViewActivity) : BroadcastReceiver() {

    inner class HandleEventTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val intent = webViewActivity.videoState.eventReceiverItem
            var downPath: String? = null
            webViewActivity.videoState.eventReceiverItem = null
            if (intent == null || webViewActivity.webCoord == null) {
                return
            }
            val intExtra = intent.getIntExtra("EXTRA_STATUS", 3)
            if (ZoneSettings.notiDown.value || intExtra != 3) {
                val longExtra = intent.getLongExtra("EXTRA_ID", -1L)
                val stringExtra = intent.getStringExtra("EXTRA_TYPE")
                intent.getBooleanExtra("secretMode", SyncSettings.secretMode.value)
                if (intExtra != 2) {
                    if (intExtra == 3) {
                        downPath = DbBookDown.pathById(webViewActivity.dialogsState.appContext, longExtra)
                    } else {
                        downPath = DbBookDown.urlById(webViewActivity.dialogsState.appContext, longExtra)
                        if (intExtra == 4 && MainDownSvc.isLiveUrl(downPath)) {
                            downPath = "live"
                        }
                    }
                }
                webViewActivity.requestOpenTypeDialog(longExtra, intExtra, downPath, stringExtra, 0)
            }
        }
    }

    inner class EventReceiverLaterTask : Runnable {

        internal inner class HandleEventTextTask : Runnable {
            override fun run() {
                val webViewActivity = wa
                val eventReceiverText = webViewActivity.videoState.eventReceiverText
                webViewActivity.videoState.eventReceiverText = null
                if (webViewActivity.webCoord == null) {
                    return
                }
                MainUtil.noOp(eventReceiverText)
            }
        }

        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.webCoord == null) {
                return
            }
            webViewActivity.runInBackground(HandleEventTextTask())
        }
    }

    inner class PipSeekForwardTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webVideoFrame = webViewActivity.videoFrame
            if (webVideoFrame != null) {
                webVideoFrame.pipSeekForward()
                return
            }
            if (webViewActivity.videoState.backPlayOn) {
                val webNestView = webViewActivity.currentPage
                val backPlaySrc = webViewActivity.dialogsState.backPlaySrc
                if (webNestView != null) {
                    val sbW = findMediaJs(backPlaySrc)
                    sbW.append("if(ele){var pos=ele.currentTime;if(pos>10){ele.currentTime=pos-10;}else{ele.currentTime=0;}}")
                    sbW.insert(0, "(function(){")
                    sbW.append("})();")
                    evalJs(webNestView, sbW.toString(), false)
                }
            }
        }
    }

    inner class PipTogglePlayTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webVideoFrame = webViewActivity.videoFrame
            if (webVideoFrame != null) {
                webVideoFrame.pipTogglePlay()
                return
            }
            if (webViewActivity.videoState.backPlayOn) {
                val webNestView = webViewActivity.currentPage
                val backPlaySrc = webViewActivity.dialogsState.backPlaySrc
                if (webNestView != null) {
                    val sbW = findMediaJs(backPlaySrc)
                    sbW.append("if(ele){if(ele.paused){ele.play();}else{ele.pause();}}")
                    sbW.insert(0, "(function(){")
                    sbW.append("})();")
                    evalJs(webNestView, sbW.toString(), false)
                }
            }
        }
    }

    inner class FindMediaJsTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webVideoFrame = webViewActivity.videoFrame
            if (webVideoFrame != null) {
                webVideoFrame.pipSeekBack()
                return
            }
            if (webViewActivity.videoState.backPlayOn) {
                val webNestView = webViewActivity.currentPage
                val backPlaySrc = webViewActivity.dialogsState.backPlaySrc
                if (webNestView != null) {
                    val sbW = findMediaJs(backPlaySrc)
                    sbW.append("if(ele){var pos=ele.currentTime;if(pos+10<ele.duration){ele.currentTime=pos+10;}else{ele.currentTime=ele.duration;}}")
                    sbW.insert(0, "(function(){")
                    sbW.append("})();")
                    evalJs(webNestView, sbW.toString(), false)
                }
            }
        }
    }

    inner class StopBackgroundPlayTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.stopBackgroundPlay(true, !webViewActivity.dialogsState.backPlayOn)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        var handler: Handler? = null
        if (intent != null) {
            val action = intent.getAction()
            action!!.javaClass
            var actionKind: Byte = -1
            when (action.hashCode()) {
                -2124716863 -> if (action == "ch.opumlabs.opumbrowser.ACTION_BACK_STOP") {
                    actionKind = 0
                }

                -964917260 -> if (action == "ch.opumlabs.opumbrowser.ACTION_SECRET_EXIT") {
                    actionKind = 1
                }

                -797871473 -> if (action == "ch.opumlabs.opumbrowser.ACTION_DOWN_DELETE") {
                    actionKind = 2
                }

                625504541 -> if (action == "ch.opumlabs.opumbrowser.ACTION_DOWN_COMPLETE") {
                    actionKind = 3
                }

                924672014 -> if (action == "ch.opumlabs.opumbrowser.ACTION_PIP_FFWD") {
                    actionKind = 4
                }

                924683546 -> if (action == "ch.opumlabs.opumbrowser.ACTION_PIP_FRWD") {
                    actionKind = 5
                }

                924975029 -> if (action == "ch.opumlabs.opumbrowser.ACTION_PIP_PLAY") {
                    actionKind = 6
                }
            }
            val webViewActivity = wa
            run sw15@{
when (actionKind.toInt()) {
                0 -> if (webViewActivity.webCoord != null && webViewActivity.videoState.backPlayOn && (webViewActivity.mainHandler.also {
                        handler = it
                    }) != null) {
                    handler!!.post(StopBackgroundPlayTask())
                    return@sw15
                }

                1 -> if (webViewActivity.webCoord != null && SyncSettings.secretMode.value) {
                    webViewActivity.switchSecretMode(false)
                    return@sw15
                }

                2 -> if (webViewActivity.webCoord != null) {
                    val stringExtra = intent.getStringExtra("EXTRA_PATH")
                    if (!TextUtils.isEmpty(stringExtra)) {
                        webViewActivity.videoState.eventReceiverText = stringExtra
                        webViewActivity.webCoord!!.postDelayed(EventReceiverLaterTask(), 2000L)
                    }
                }

                3 -> if (webViewActivity.webCoord != null) {
                    webViewActivity.videoState.eventReceiverItem = intent
                    webViewActivity.runInBackground(HandleEventTask())
                }

                4 -> if (webViewActivity.webCoord != null) {
                    val exitHandler: Handler? = if ((webViewActivity.videoFrame != null || webViewActivity.videoState.backPlayOn)) webViewActivity.mainHandler else null
                    if ((webViewActivity.videoFrame != null || webViewActivity.videoState.backPlayOn) && exitHandler != null) {
                        exitHandler.post(FindMediaJsTask())
                    }
                }

                5 -> if (webViewActivity.webCoord != null) {
                    val pauseHandler: Handler? = if ((webViewActivity.videoFrame != null || webViewActivity.videoState.backPlayOn)) webViewActivity.mainHandler else null
                    if ((webViewActivity.videoFrame != null || webViewActivity.videoState.backPlayOn) && pauseHandler != null) {
                        pauseHandler.post(PipSeekForwardTask())
                    }
                }

                6 -> if (webViewActivity.webCoord != null) {
                    val playHandler: Handler? = if ((webViewActivity.videoFrame != null || webViewActivity.videoState.backPlayOn)) webViewActivity.mainHandler else null
                    if ((webViewActivity.videoFrame != null || webViewActivity.videoState.backPlayOn) && playHandler != null) {
                        playHandler.post(PipTogglePlayTask())
                    }
                }
            }
}
        }
    }
}
