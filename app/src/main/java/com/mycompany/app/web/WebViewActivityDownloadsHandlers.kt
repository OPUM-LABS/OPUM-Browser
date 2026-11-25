@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.openHeadConnection
import com.mycompany.app.main.MainUtil.githubRawToRepo
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.siteNameOf
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.isFullScreenOff
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.translateUrlFor
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.findMediaJs
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
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.web.WebReadTask.WebReadListener
import com.mycompany.app.web.WebReadTask
import android.content.DialogInterface
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.wview.WebDownView
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.wview.WebFltView.FltViewListener
import android.view.View
import java.lang.Exception
import java.util.ArrayList

/*
 * The browser activity's downloads callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaShowDownNotiPanel(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ImageButtonListener : FltViewListener {
        override fun onTap(view: View?, button: Int) {
            val webViewActivity = wa
            if (webViewActivity.imageButton == null) {
                return
            }
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.setDownNoti(false)
            }
            webViewActivity.imageButton!!.setNoti(false)
            webViewActivity.onDownIconClick(view)
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
        if (webViewActivity.dialogsState.webBody == null || webViewActivity.mainHandler == null) {
            return
        }
        val webDownView = webViewActivity.imageButton
        if (webDownView != null) {
            webDownView.refreshIcon()
            return
        }
        webViewActivity.downIconColor = 1234
        webViewActivity.updateDownBadge()
        try {
            val imageButton = WebDownView(webViewActivity)
            webViewActivity.imageButton = imageButton
            imageButton.refreshColors()
            webViewActivity.imageButton!!.setVisibility(View.GONE)
            webViewActivity.imageButton!!.setListener(ImageButtonListener())
            val myWebBody = webViewActivity.dialogsState.webBody
            val imageButtonNow = webViewActivity.imageButton
            val width = MainApp.dp48
            wa.floatingState.overlays!!.addView(imageButtonNow, width, width)
        } catch (unused: Exception) {
        }
    }
}

class WvaDownNotiPanelRefresh(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webDownView = wa.imageButton
        if (webDownView != null) {
            webDownView.showNow()
        }
    }
}

class WvaDownPageDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogDownPage = webViewActivity.downPageDialog
        if (dialogDownPage != null) {
            dialogDownPage.dismiss()
            webViewActivity.downPageDialog = null
        }
    }
}

class WvaApplyPendingDocFind(@JvmField val wa: WebViewActivity, @JvmField val findText: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val findText = this@WvaApplyPendingDocFind.findText
        if (TextUtils.isEmpty(findText) || webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.resetTranslate(false)
        loadHtml(webViewActivity.currentPage, webViewActivity.dialogsState.pageUrl, findText)
    }
}

class WvaDownUrlDialogApplyPending(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val dialogDownUrl = webViewActivity.videoState.downUrlDialog
        if (dialogDownUrl != null) {
            dialogDownUrl.appCode = 0
            val downListItem = dialogDownUrl.downItemIn
            if (downListItem != null) {
                dialogDownUrl.downItemIn = null
                val url = downListItem.url
                dialogDownUrl.m3u8Url = url
                dialogDownUrl.downUrl = url
                dialogDownUrl.posterUrl = downListItem.audioUrl
                dialogDownUrl.fileSize = downListItem.fileSize
                dialogDownUrl.setExtension(downListItem.title)
                dialogDownUrl.updateList(false)
            } else {
                val m3u8Item = dialogDownUrl.m3u8In
                if (m3u8Item != null) {
                    dialogDownUrl.m3u8In = null
                    dialogDownUrl.pickM3u8(m3u8Item)
                    dialogDownUrl.updateList(false)
                } else {
                    val faceItem = dialogDownUrl.faceIn
                    if (faceItem != null) {
                        dialogDownUrl.faceIn = null
                        val label = faceItem.label
                        dialogDownUrl.m3u8Url = DialogDownUrl.joinUrl(dialogDownUrl.downUrl, label)
                        dialogDownUrl.downUrl = faceItem.url
                        dialogDownUrl.posterUrl = faceItem.posterUrl
                        dialogDownUrl.fileSize = faceItem.fileSize
                        dialogDownUrl.setExtension(label)
                        dialogDownUrl.updateList(false)
                    }
                }
            }
        }
        webViewActivity.miscState.previewDialogListenerBusy = false
    }
}

class WvaReadTaskListener(@JvmField val wa: WebViewActivity) : WebReadListener {
    override fun onFailed() {
        val webViewActivity = wa
        val webReadTask = webViewActivity.dialogsState.readTask
        if (webReadTask != null) {
            webReadTask.release()
            webViewActivity.dialogsState.readTask = null
        }
    }

    override fun onArticle(pageUrl: String?, pageTitle: String?, html: String?, arrayList: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?>?) {
        val webViewActivity = wa
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            if (ZtwoSettings.addPage.value) {
                webViewActivity.openInNextPage(pageUrl, html, true)
            } else {
                loadHtml(webNestView, pageUrl, html)
            }
        }
        val webReadTask = webViewActivity.dialogsState.readTask
        if (webReadTask != null) {
            webReadTask.release()
            webViewActivity.dialogsState.readTask = null
        }
    }

    override fun onEmpty() {
    }

    override fun onImageUrl(imageUrl: String?) {
    }
}
