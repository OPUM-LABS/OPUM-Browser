@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.nativeJsN
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.stripHitomiTitle
import com.mycompany.app.main.MainUtil.extractUrl
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.appendAmpJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.main.MainUtil.pipAspectRatio
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.pref.PrefEditor.Companion.withAlpha
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.pref.PrefZtwo.Companion.saveNews
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.freeMemory
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.db.book.DbBookTmem.Companion.get
import com.mycompany.app.db.book.DbBookTrans.Companion.get
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.isUserScriptUrl
import com.mycompany.app.main.MainUtil.downMenuCount
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import android.text.TextUtils
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.view.MyScrollBar
import com.mycompany.app.pref.PrefAlbum
import android.content.DialogInterface
import com.mycompany.app.dialog.DialogSetImage.ChangedListener
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.dialog.DialogSeekAudio
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.pull.MyPullView
import com.mycompany.app.wview.WebUpView
import com.mycompany.app.view.MyGesNoti
import com.mycompany.app.wview.WebUpView.UpViewListener
import com.mycompany.app.wview.WebFltView.FltViewListener
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.main.MainSelectAdapter.MainSelectListener
import com.mycompany.app.dialog.DialogEditText.EditTextListener
import com.mycompany.app.main.MainSelectAdapter
import android.graphics.Paint
import android.os.Handler
import android.view.View
import ch.opumlabs.opumbrowser.R
import java.lang.Exception
import java.util.ArrayList

/*
 * The browser activity's floating callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaUpButtonListener(@JvmField val wa: WebViewActivity) : UpViewListener {
    override fun onTap() {
        wa.pageScroll(true, true)
    }

    override fun onHold() {
        var iconColor: Int = 0
        var arrowIcon: Int = 0
        val fillColor: Int
        val shiftBase: Int
        val webViewActivity = wa
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        val webUpView = webViewActivity.upButton
        val onStartPage = webViewActivity.onStartPage
        var upButtonHidden = false
        if (webUpView != null) {
            val darkPage = MainUtil.isDarkPage(onStartPage)
            val alpha = EditorSettings.upAlpha.value
            if (darkPage) {
                fillColor = withAlpha(-16777216, alpha)
                arrowIcon = AppR.drawable.outline_arrow_upward_dark_24
                iconColor = -1066044043
            } else {
                fillColor = withAlpha(-1, alpha)
                arrowIcon = AppR.drawable.outline_arrow_upward_black_24
                iconColor = -2139785867
            }
            val drawableR = loadDrawable(myWebCoord.getContext(), arrowIcon)
            myWebCoord.iconDrawable = drawableR
            if (drawableR != null) {
                val paint = Paint()
                myWebCoord.iconFill = paint
                paint.setAntiAlias(true)
                myWebCoord.iconFill!!.setStyle(Paint.Style.FILL)
                myWebCoord.iconFill!!.setColor(fillColor)
                val iconStroke = Paint()
                myWebCoord.iconStroke = iconStroke
                iconStroke.setAntiAlias(true)
                myWebCoord.iconStroke!!.setStyle(Paint.Style.STROKE)
                myWebCoord.iconStroke!!.setStrokeWidth(MainApp.dp1.toFloat())
                myWebCoord.iconStroke!!.setColor(iconColor)
                myWebCoord.areaWidth = myWebCoord.getWidth()
                myWebCoord.areaHeight = myWebCoord.getHeight()
                val iconWidth = MainApp.dp48
                myWebCoord.iconWidth = iconWidth
                myWebCoord.iconHeight = iconWidth
                myWebCoord.iconHalf = iconWidth / 2
                myWebCoord.iconRadius = MainApp.dp16
                val upPos = ZoneSettings.showUpPos.value
                if (upPos == 1) {
                    shiftBase = MainApp.dp4
                } else {
                    val freeWidth = myWebCoord.areaWidth - iconWidth
                    shiftBase = if (upPos == 3) freeWidth - MainApp.dp4 else Math.round(freeWidth / 2.0f)
                }
                val dragOffsetY = (myWebCoord.areaHeight - myWebCoord.iconHeight) - MainApp.dp4
                myWebCoord.iconShiftX = (webUpView.getX().toInt()) - shiftBase
                myWebCoord.iconShiftY = (webUpView.getY().toInt()) - dragOffsetY
                myWebCoord.dragOffsetX = shiftBase - myWebCoord.touchX
                myWebCoord.dragOffsetY = dragOffsetY - myWebCoord.touchY
                myWebCoord.moveIcon(shiftBase, dragOffsetY)
                upButtonHidden = true
            }
        }
        webViewActivity.floatingState.upButtonHidden = upButtonHidden
    }
}

class WvaUpButtonPosition(@JvmField val wa: WebViewActivity) : DialogSeekListener {
    override fun onValue(newValue: Int) {
        val webViewActivity = wa
        webViewActivity.floatingState.upButtonHidden = false
        if (webViewActivity.upButton == null) {
            return
        }
        if (ZoneSettings.showUpPos.value != newValue) {
            ZoneSettings.edit(webViewActivity.dialogsState.appContext) { set(ZoneSettings.showUpPos, newValue) }
            webViewActivity.placeUpButton()
            webViewActivity.applyUpButtonPos()
        }
        webViewActivity.showUpButton()
    }
}

class WvaNewsFloatRefresh(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.onStartPage && ZtwoSettings.newsUse.value && webViewActivity.floatingState.rssButton != null) {
            webViewActivity.stopNews()
        }
    }
}

class WvaScrollStopped(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage != null) {
            val myScrollBar: MyScrollBar? = if (ZoneSettings.scrollPos.value != 0) webViewActivity.scrollBar else null
            if (ZoneSettings.scrollPos.value != 0 && myScrollBar != null) {
                myScrollBar.updatePos(0, 0)
            }
            webViewActivity.updateTransFloatButton(true)
            webViewActivity.updateUpButton(true)
        }
        webViewActivity.pageState.pageViewListenerBusy = false
    }
}

class WvaNewsLocalePicked(@JvmField val wa: WebViewActivity) : MainSelectListener {
    override fun onPick(id: Int) {
        val webViewActivity = wa
        val dialogNewsLocale = webViewActivity.newsLocaleDialog
        if (dialogNewsLocale != null) {
            dialogNewsLocale.dismiss()
            webViewActivity.newsLocaleDialog = null
        }
        if (webViewActivity.floatingState.rssButton == null || ZtwoSettings.newsLang.value == id) {
            return
        }
        ZtwoSettings.edit(webViewActivity.dialogsState.appContext) { set(ZtwoSettings.newsLang, id) }
        val quickView = webViewActivity.currentPage!!.quickPanel
        if (quickView != null) {
            quickView.applyNewsChange(true)
        }
    }
}

class WvaNewsSearchEntered(@JvmField val wa: WebViewActivity) : EditTextListener {
    override fun onText(input: String?) {
        val webViewActivity = wa
        val dialogNewsSearch = webViewActivity.newsSearchDialog
        if (dialogNewsSearch != null) {
            dialogNewsSearch.dismiss()
            webViewActivity.newsSearchDialog = null
        }
        if (webViewActivity.floatingState.rssButton == null) {
            return
        }
        if (ZtwoSettings.newsTopic.value != 9 || !sameObject(ZtwoSettings.newsSearch.value, input)) {
            ZtwoSettings.editLocal { set(ZtwoSettings.newsTopic, 9) }
            ZtwoSettings.editLocal { set(ZtwoSettings.newsSearch, input) }
            PrefZtwo.saveNews(webViewActivity.dialogsState.appContext)
        }
        webViewActivity.stopNews()
    }

    override fun onCleared() {
    }
}

class WvaSetTransDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeSetTransDialog()
        if (AlbumSettings.transMode.value == 0) {
            webViewActivity.autoTransPending = false
        }
        webViewActivity.updateTransIcon(false)
        val webFltView = webViewActivity.floatingState.translateButton
        if (webFltView != null) {
            webFltView.refreshColors()
        }
        webViewActivity.applyTransLang()
    }
}

class WvaUpdateTransFloat(@JvmField val wa: WebViewActivity, @JvmField val urlChanged: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val urlChanged = this@WvaUpdateTransFloat.urlChanged
        val translateActive = if (!webViewActivity.isTranslateActive()) false else AlbumSettings.transPage.value
        val webFltView = webViewActivity.floatingState.translateButton
        if (translateActive) {
            if (webFltView == null && webViewActivity.dialogsState.webBody != null) {
                try {
                    val translateButton = WebFltView(webViewActivity, 3)
                    webViewActivity.floatingState.translateButton = translateButton
                    translateButton.refreshColors()
                    webViewActivity.floatingState.translateButton!!.setVisibility(View.GONE)
                    webViewActivity.floatingState.translateButton!!.setFltListener(WvaTransFloatListener(webViewActivity))
                    val myWebBody = webViewActivity.dialogsState.webBody
                    val translateButtonNow = webViewActivity.floatingState.translateButton
                    val width = MainApp.dp48
                    wa.floatingState.overlays!!.addView(translateButtonNow, width, width)
                } catch (unused: Exception) {
                }
            }
            webViewActivity.updateTranslateButton(true)
        } else if (urlChanged) {
            val myWebBody: MyWebBody? = if (webFltView != null) webViewActivity.dialogsState.webBody else null
            if (webFltView != null && myWebBody != null) {
                if (!AlbumSettings.transPage.value || AlbumSettings.transMode.value == 0) {
                    myWebBody.post(WvaRemoveTransFloat(webViewActivity))
                } else {
                    webFltView.hide(false)
                }
            }
        } else if (webFltView != null) {
            webFltView.hide(false)
        }
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord != null) {
            myWebCoord.post(WvaAddTransFloat(webViewActivity, updateTransFloatFlag = urlChanged))
        }
    }
}

class WvaRemoveTransFloat(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webFltView = webViewActivity.floatingState.translateButton
        if (webFltView != null) {
            webFltView.release()
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody != null) {
                wa.floatingState.overlays!!.removeView(webViewActivity.floatingState.translateButton)
            }
            webViewActivity.floatingState.translateButton = null
        }
    }
}

class WvaRemoveListFloat(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webFltView = webViewActivity.floatingState.lockButton
        if (webFltView != null) {
            webFltView.release()
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody != null) {
                wa.floatingState.overlays!!.removeView(webViewActivity.floatingState.lockButton)
            }
            webViewActivity.floatingState.lockButton = null
        }
    }
}

class WvaListFloatPopup(@JvmField val wa: WebViewActivity) : MyPopupListener {
    override fun onClose() {
        wa.closeLockMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        val webViewActivity = wa
        val arrayList: ArrayList<Any?>? = if (webViewActivity.floatingState.lockButton != null) webViewActivity.dialogsState.siteLockList else null
        val index = if (webViewActivity.floatingState.lockButton != null && arrayList != null && !arrayList.isEmpty()) (id / 2) else 0
        val childItem: ChildItem? = if (webViewActivity.floatingState.lockButton != null && arrayList != null && !arrayList.isEmpty() && index < arrayList.size) (arrayList.get(index) as ChildItem?) else null
        if (webViewActivity.floatingState.lockButton != null && arrayList != null && !arrayList.isEmpty() && index < arrayList.size && childItem != null) {
            if (id % 2 == 0) {
                copyTextToast(webViewActivity, AppR.string.copied_clipboard, "Copied username", childItem.url)
                return true
            }
            copyTextToast(webViewActivity, AppR.string.copied_clipboard, "Copied password", childItem.subText)
        }
        return true
    }
}

class WvaZoomButtonChanged(@JvmField val wa: WebViewActivity) : ChangedListener {
    override fun onChanged() {
        val webViewActivity = wa
        if (webViewActivity.currentPage != null) {
            if (ZtriSettings.zoomIcon.value) {
                webViewActivity.addZoomButton()
                val webFltView = webViewActivity.floatingState.zoomButton
                if (webFltView != null) {
                    webFltView.refreshColors()
                }
                webViewActivity.updateZoomButton(true)
                return
            }
            val webFltView = webViewActivity.floatingState.zoomButton
            if (webFltView != null) {
                webFltView.release()
                val myWebBody = webViewActivity.dialogsState.webBody
                if (myWebBody != null) {
                    wa.floatingState.overlays!!.removeView(webViewActivity.floatingState.zoomButton)
                }
                webViewActivity.floatingState.zoomButton = null
            }
        }
    }
}

class WvaFloatGroupListener(@JvmField val wa: WebViewActivity) : FltViewListener {
    override fun onTap(view: View?, button: Int) {
        val webViewActivity = wa
        val webFltView = webViewActivity.floatingState.userButton1
        if (webFltView != null) {
            webFltView.show(false)
        }
        val userButton2 = webViewActivity.floatingState.userButton2
        if (userButton2 != null) {
            userButton2.show(false)
        }
        val userButton3 = webViewActivity.floatingState.userButton3
        if (userButton3 != null) {
            userButton3.show(false)
        }
        webViewActivity.runMenuItem(view, button)
    }

    override fun onGone() {
        wa.hideMoveHint()
    }

    override fun onLongPress(view: View?) {
        wa.showMoveHint(view)
    }
}

class WvaAddFullscreenButton(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class FullscreenButtonListener : FltViewListener {

        internal inner class OpenLoadDialogStep : Runnable {
            override fun run() {
                val webViewActivity = wa
                webViewActivity.openLoadDialog(webViewActivity.dialogsState.fullscreenButtonMode.toInt())
            }
        }

        override fun onTap(view: View?, button: Int) {
            val myWebBody = wa.dialogsState.webBody
            if (myWebBody == null) {
                return
            }
            myWebBody.post(OpenLoadDialogStep())
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
        if (webViewActivity.floatingState.fullscreenButton != null || webViewActivity.dialogsState.webBody == null) {
            return
        }
        try {
            val webFltView = WebFltView(webViewActivity, 5)
            webViewActivity.floatingState.fullscreenButton = webFltView
            webFltView.refreshColors()
            webViewActivity.floatingState.fullscreenButton!!.setHideBlocked(true)
            webViewActivity.floatingState.fullscreenButton!!.setFltListener(FullscreenButtonListener())
            val myWebBody = webViewActivity.dialogsState.webBody
            val fullscreenButton = webViewActivity.floatingState.fullscreenButton
            val width = MainApp.dp48
            wa.floatingState.overlays!!.addView(fullscreenButton, width, width)
        } catch (unused: Exception) {
        }
    }
}

class WvaRemoveFullscreenButton(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webFltView = webViewActivity.floatingState.fullscreenButton
        if (webFltView != null) {
            webFltView.release()
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody != null) {
                wa.floatingState.overlays!!.removeView(webViewActivity.floatingState.fullscreenButton)
            }
            webViewActivity.floatingState.fullscreenButton = null
        }
    }
}

class WvaRemoveNewsFloat(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webFltView = webViewActivity.floatingState.rssButton
        if (webFltView != null) {
            webFltView.release()
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody != null) {
                wa.floatingState.overlays!!.removeView(webViewActivity.floatingState.rssButton)
            }
            webViewActivity.floatingState.rssButton = null
        }
    }
}

class WvaShowFloatingButtons(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val myScrollBar = webViewActivity.scrollBar
        if (myScrollBar == null || !myScrollBar.dragging) {
            webViewActivity.updateImageButton(true)
            webViewActivity.updateVideoButton(true)
            webViewActivity.showUserButtons(true)
            webViewActivity.updatePullButton(true)
            webViewActivity.updateRssButton(true)
            webViewActivity.updateTranslateButton(true)
            webViewActivity.updateZoomButton(true)
        } else {
            webViewActivity.hideAllButtons()
        }
        webViewActivity.floatingState.floatingShown = false
    }
}

class WvaRefreshFloatingButtons(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val myScrollBar = webViewActivity.scrollBar
        if (myScrollBar == null || !myScrollBar.dragging) {
            webViewActivity.hideAllButtons()
        } else {
            webViewActivity.updateImageButton(true)
            webViewActivity.updateVideoButton(true)
            webViewActivity.showUserButtons(true)
            webViewActivity.updatePullButton(true)
            webViewActivity.updateRssButton(true)
            webViewActivity.updateTranslateButton(true)
            webViewActivity.updateZoomButton(true)
        }
        webViewActivity.floatingState.floatingShown = false
    }
}

class WvaGestureEnd(@JvmField val wa: WebViewActivity) : Runnable {

    override fun run() {
        val webViewActivity = wa
        webViewActivity.dialogsState.barUpdateBusy = false
        if (webViewActivity.currentPage != null) {
            val gestureDirection = webViewActivity.tabsState.gestureDirection
            var handled = true
            if (gestureDirection.toInt() != 0 || webViewActivity.floatingState.edgeHint.toInt() == 0) {
                if (gestureDirection.toInt() == 3) {
                    webViewActivity.settleTabSwipe(gestureDirection.toInt())
                } else {
                    val gestureMode = webViewActivity.floatingState.gestureMode
                    if (gestureMode.toInt() == 0) {
                        if (gestureDirection.toInt() == 1) {
                            if (webViewActivity.swipeBackOpensTab()) {
                                webViewActivity.finishPageSwipe(webViewActivity.tabsState.gestureDirection.toInt())
                            } else {
                                val myScrollNavi = webViewActivity.leftScrollNavi
                                if (!(if (myScrollNavi == null) false else myScrollNavi.isOut())) {
                                    webViewActivity.resetScrollNavis(false, false)
                                } else if (!webViewActivity.floatingState.edgeTouchActive && webViewActivity.tabsState.layoutRtl) {
                                    webViewActivity.startGoForward()
                                } else {
                                    webViewActivity.goBackFromBackKey(null)
                                }
                            }
                        } else if (gestureDirection.toInt() == 2) {
                            if (webViewActivity.swipeForwardOpensTab()) {
                                webViewActivity.finishPageSwipe(webViewActivity.tabsState.gestureDirection.toInt())
                            } else {
                                val myScrollNavi = webViewActivity.floatingState.rightScrollNavi
                                if (!(if (myScrollNavi == null) false else myScrollNavi.isOut())) {
                                    webViewActivity.resetScrollNavis(false, false)
                                } else if (webViewActivity.floatingState.edgeTouchActive || webViewActivity.tabsState.layoutRtl) {
                                    webViewActivity.goBackFromBackKey(null)
                                } else {
                                    webViewActivity.startGoForward()
                                }
                            }
                        }
                    } else if (gestureMode.toInt() != 1) {
                        webViewActivity.finishTabSwipe(gestureDirection.toInt())
                    } else if (gestureDirection.toInt() == 1) {
                        if (webViewActivity.floatingState.edgeTouchActive && webViewActivity.tabsState.tabSwipeActive) {
                            val myScrollNavi = webViewActivity.leftScrollNavi
                            if (if (myScrollNavi == null) false else myScrollNavi.isOut()) {
                                webViewActivity.goBackFromBackKey(null)
                            } else {
                                webViewActivity.finishTabSwipe(webViewActivity.tabsState.gestureDirection.toInt())
                            }
                        } else {
                            webViewActivity.finishTabSwipe(webViewActivity.tabsState.gestureDirection.toInt())
                        }
                    } else if (gestureDirection.toInt() != 2) {
                        webViewActivity.finishTabSwipe(gestureDirection.toInt())
                    } else if (webViewActivity.floatingState.edgeTouchActive && webViewActivity.tabsState.tabSwipeActive) {
                        val myScrollNavi = webViewActivity.floatingState.rightScrollNavi
                        if (if (myScrollNavi == null) false else myScrollNavi.isOut()) {
                            webViewActivity.goBackFromBackKey(null)
                        } else {
                            webViewActivity.finishTabSwipe(webViewActivity.tabsState.gestureDirection.toInt())
                        }
                    } else {
                        webViewActivity.finishTabSwipe(webViewActivity.tabsState.gestureDirection.toInt())
                    }
                }
                handled = false
            } else {
                val myGesNoti = webViewActivity.floatingState.gestureHint
                if (myGesNoti != null) {
                    myGesNoti.hideLater()
                }
                val edgeHint = webViewActivity.floatingState.edgeHint
                if (edgeHint.toInt() == 1) {
                    webViewActivity.runMenuItem(null, ZoneSettings.gesUpLeft.value)
                } else if (edgeHint.toInt() == 2) {
                    webViewActivity.runMenuItem(null, ZoneSettings.gesUpRight.value)
                } else if (edgeHint.toInt() == 3) {
                    webViewActivity.runMenuItem(null, ZoneSettings.gesDownLeft.value)
                } else if (edgeHint.toInt() == 4) {
                    webViewActivity.runMenuItem(null, ZoneSettings.gesDownRight.value)
                }
            }
            if (webViewActivity.isZoneGestureOff()) {
                val leftScrollNavi = webViewActivity.leftScrollNavi
                if (leftScrollNavi != null) {
                    leftScrollNavi.hide()
                }
                val myScrollNavi = webViewActivity.floatingState.rightScrollNavi
                if (myScrollNavi != null) {
                    myScrollNavi.hide()
                }
            }
            val myGesNoti: MyGesNoti? = if (!handled) webViewActivity.floatingState.gestureHint else null
            if (!handled && myGesNoti != null) {
                myGesNoti.fadeOutNow()
            }
            val myScrollBar: MyScrollBar? = if (ZoneSettings.scrollPos.value != 0) webViewActivity.scrollBar else null
            if (ZoneSettings.scrollPos.value != 0 && myScrollBar != null) {
                myScrollBar.restartTimer()
            }
            val myPullView: MyPullView? = if (ZoneSettings.pullRefresh.value) webViewActivity.pullView else null
            if (ZoneSettings.pullRefresh.value && myPullView != null) {
                myPullView.cancelTouch()
            }
            if (webViewActivity.imageButton != null && webViewActivity.dialogsState.snsFixBusy && ZoneSettings.downPos.value == 3) {
                val pageUrl = webViewActivity.dialogsState.pageUrl
                val handler: Handler? = if ((if (TextUtils.isEmpty(pageUrl)) false else pageUrl!!.startsWith("https://m.facebook.com/reel"))) webViewActivity.mainHandler else null
                if ((if (TextUtils.isEmpty(pageUrl)) false else pageUrl!!.startsWith("https://m.facebook.com/reel")) && handler != null) {
                    handler.post(WvaDownNotiPanelRefresh(webViewActivity))
                }
            }
        }
        webViewActivity.floatingState.gestureMode = 2.toByte()
        webViewActivity.floatingState.gestureZone = 0.toByte()
        webViewActivity.tabsState.gestureDirection = 0.toByte()
        webViewActivity.barsState.edgeDirection = 0.toByte()
        webViewActivity.floatingState.edgeHint = 0.toByte()
        webViewActivity.tabsState.tabSwipeActive = false
    }
}

class WvaApplyBarColors(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val themeColor = webViewActivity.dialogsState.themeColor
        val themeLight = webViewActivity.miscState.themeLight
        val myFindView = webViewActivity.findView
        if (myFindView != null) {
            myFindView.applyColors(themeColor, themeLight, webViewActivity.onStartPage)
        }
        webViewActivity.tintProgressBar(themeColor)
        val webUpView = webViewActivity.upButton
        if (webUpView != null) {
            webUpView.setBgColors(webViewActivity.onStartPage)
        }
        val view = webViewActivity.transFloatButton
        if (view != null) {
            if (AppState.darkUi) {
                view.setBackgroundResource(AppR.drawable.trans_logo_short_back_white)
            } else {
                view.setBackgroundResource(AppR.drawable.trans_logo_short_back_color)
            }
        }
        val myScrollBar = webViewActivity.scrollBar
        if (myScrollBar != null) {
            myScrollBar.setPreColor(if (AppState.darkUi) -12632257 else -2434342)
        }
        val myPullView = webViewActivity.pullView
        if (myPullView != null) {
            myPullView.applyColors(webViewActivity.onStartPage)
        }
    }
}
