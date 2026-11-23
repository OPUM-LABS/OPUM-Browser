@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import android.view.View

import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.AlbumSettings
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.parseFloatOr
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
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
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.userFontResponse
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefSync
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.pref.PrefAlbum
import android.content.DialogInterface
import com.mycompany.app.dialog.DialogTransLang
import com.mycompany.app.main.MainTransNews
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.dialog.DialogTransLang.TransLangListener
import com.mycompany.app.main.MainTransNews.TransNewsListener
import com.bumptech.glide.Glide
import android.widget.ImageView
import java.lang.Exception

/*
 * The browser activity's translate callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaTranslateRestoreBack(@JvmField val wa: WebViewActivity, @JvmField val transBackListener: com.mycompany.app.web.WebViewActivity.PrevBackListener? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val prevBackListener = this@WvaTranslateRestoreBack.transBackListener
        webViewActivity.startBackImpl(true, prevBackListener, null)
    }
}

class WvaTransLangPicked(@JvmField val wa: WebViewActivity) : TransLangListener {
    override fun onLanguage(lang: String?) {
        val webViewActivity = wa
        val dialogTransLang = webViewActivity.transLangDialog
        if (dialogTransLang != null) {
            dialogTransLang.dismiss()
            webViewActivity.transLangDialog = null
        }
        if (webViewActivity.dialogsState.transState.toInt() != 3 || TextUtils.isEmpty(lang)) {
            return
        }
        webViewActivity.runInBackground(WvaApplyTransLangTask(webViewActivity, lang))
    }
}

class WvaTransLangDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogTransLang = webViewActivity.transLangDialog
        if (dialogTransLang != null) {
            dialogTransLang.dismiss()
            webViewActivity.transLangDialog = null
        }
    }
}

class WvaViewTransDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogViewTrans = webViewActivity.viewTransDialog
        if (dialogViewTrans != null) {
            dialogViewTrans.dismiss()
            webViewActivity.viewTransDialog = null
        }
    }
}

class WvaTransControlRefresh(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTransControl = webViewActivity.transControl
        if (webTransControl == null) {
            return
        }
        val transState = webViewActivity.dialogsState.transState
        webTransControl.showState(webViewActivity.transLangCode, webViewActivity.dialogsState.transReady, transState.toInt())
    }
}

class WvaShowTransControl(@JvmField val wa: WebViewActivity, @JvmField val view: android.view.View? = null) : Runnable {

    internal inner class PlaceTransControlTask : Runnable {
        override fun run() {
            var leftMargin: Int
            var widthValue: Int = 0
            var topMargin: Int
            val pivotYPx: Int
            val webViewActivity = wa
            val webTransControl = webViewActivity.translateState.pendingTransControl
            val view = this@WvaShowTransControl.view
            webViewActivity.translateState.pendingTransControl = null
            if (webTransControl == null || webViewActivity.transControl != null) {
                return
            }
            webViewActivity.transControl = webTransControl
            var newPivotYPx = 0
            if (AlbumSettings.transPage.value) {
                val webFltView = webViewActivity.floatingState.translateButton
                if (webFltView != null) {
                    webFltView.setHideBlocked(true)
                }
            } else if (AlbumSettings.transNoti.value) {
                AlbumSettings.edit(webViewActivity.dialogsState.appContext) { set(AlbumSettings.transNoti, false) }
                val myBarView = webViewActivity.barsState.topBarView
                if (myBarView != null) {
                    myBarView.setAddrNoti(false)
                }
                val bottomBarView = webViewActivity.barsState.bottomBarView
                if (bottomBarView != null) {
                    bottomBarView.setAddrNoti(false)
                }
            }
            try {
                val isSingleBarMode = AlbumSettings.transPage.value || !webViewActivity.isSingleBarMode()
                val bodyPos = IntArray(2)
                webViewActivity.dialogsState.webBody!!.getLocationOnScreen(bodyPos)
                val paddingLeft = bodyPos[0] + webViewActivity.dialogsState.webBody!!.getPaddingLeft()
                val paddingTop = bodyPos[1] + webViewActivity.dialogsState.webBody!!.getPaddingTop()
                val rectWidth = webViewActivity.dialogsState.webBody!!.rectWidth
                var rectHeight = webViewActivity.dialogsState.webBody!!.rectHeight
                view!!.getLocationOnScreen(bodyPos)
                val pivotX = bodyPos[0] - paddingLeft
                val bodyTop = bodyPos[1] - paddingTop
                val width = view.getWidth()
                val height = view.getHeight()
                val pivotXPx = pivotX + width
                val dp48Px = MainApp.dp48
                var arrowX = pivotXPx - (dp48Px / 2)
                if (arrowX > rectWidth) {
                    arrowX = rectWidth
                }
                if (arrowX < 0) {
                    arrowX = 0
                }
                if (rectWidth <= rectHeight || rectWidth <= ((dp48Px * 7).also { widthValue = it })) {
                    leftMargin = 0
                    widthValue = -1
                } else {
                    leftMargin = arrowX - (widthValue / 2)
                    val maxLeft = rectWidth - widthValue
                    if (leftMargin > maxLeft) {
                        leftMargin = maxLeft
                    }
                    if (leftMargin < 0) {
                        leftMargin = 0
                    }
                    arrowX -= leftMargin
                    val maxPivotX = rectWidth - leftMargin
                    if (arrowX > maxPivotX) {
                        arrowX = maxPivotX
                    }
                    if (arrowX < 0) {
                        arrowX = 0
                    }
                }
                val popupHeight = MainApp.dp8 * 9
                if (isSingleBarMode) {
                    topMargin = bodyTop - popupHeight
                    pivotYPx = popupHeight
                } else {
                    topMargin = MainApp.dp48 + bodyTop
                    pivotYPx = 0
                }
                val maxTop = rectHeight - popupHeight
                if (topMargin > maxTop) {
                    topMargin = maxTop
                }
                if (topMargin < 0) {
                    var belowTop = bodyTop + (height / 2)
                    if (belowTop > popupHeight) {
                        belowTop = popupHeight
                    }
                    if (belowTop <= rectHeight) {
                        rectHeight = belowTop
                    }
                    topMargin = 0
                    if (rectHeight >= 0) {
                        newPivotYPx = rectHeight
                    }
                } else {
                    newPivotYPx = pivotYPx
                }
                val transControl = webViewActivity.transControl
                transControl!!.pivotXPx = arrowX
                transControl.pivotYPx = newPivotYPx
                transControl.setVisibility(View.INVISIBLE)
                webViewActivity.transControl!!.setListener(WvaTransCtrlListener(webViewActivity))
                val layoutParams = FrameLayout.LayoutParams(widthValue, popupHeight)
                if (widthValue != -1) {
                    if (webViewActivity.tabsState.layoutRtl) {
                        layoutParams.rightMargin = rectWidth - (leftMargin + widthValue)
                    } else {
                        layoutParams.leftMargin = leftMargin
                    }
                }
                layoutParams.topMargin = topMargin
                val frameLayout = FrameLayout(webViewActivity)
                webViewActivity.transControlFrame = frameLayout
                frameLayout.addView(webViewActivity.transControl, layoutParams)
                webViewActivity.transControlFrame!!.setOnClickListener(WvaHideTransControlClick(webViewActivity))
                wa.floatingState.overlays!!.addView(webViewActivity.transControlFrame, -1, -1)
                webViewActivity.dialogsState.webBody!!.post(WvaTransControlRefreshShow(webViewActivity))
            } catch (unused: Exception) {
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        val webTransControl = webViewActivity.translateState.pendingTransControl
        if (webTransControl != null) {
            webTransControl.applyMode(0)
        }
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(PlaceTransControlTask())
    }
}

class WvaTransControlRefreshShow(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTransControl = webViewActivity.transControl
        if (webTransControl == null) {
            return
        }
        val transState = webViewActivity.dialogsState.transState
        webTransControl.showState(webViewActivity.transLangCode, webViewActivity.dialogsState.transReady, transState.toInt())
        webViewActivity.transControl!!.animateShow(true)
    }
}

class WvaNewsTranslateListener(@JvmField val wa: WebViewActivity) : TransNewsListener {
    override fun onDone(isOk: Boolean) {
        val webViewActivity = wa
        val mainTransNews = webViewActivity.quickState.transNews
        if (mainTransNews != null) {
            if (!isOk) {
                mainTransNews.release()
                webViewActivity.quickState.transNews = null
            }
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.setTransLoaded(isOk)
            }
        }
    }

    override fun targetCount(): Int {
        val webNestView = wa.currentPage
        if (webNestView == null) {
            return 0
        }
        return webNestView.transPos
    }
}

