@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.ui.state.AppState
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.MainSettings
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.hasZtwoFlag
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.notifyBookUpdate
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaMultiJs
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.main.MainUtil.hasClipboard
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookRecent.Companion.isEnabled
import com.mycompany.app.db.book.DbBookRecent.Companion.loadItems
import com.mycompany.app.db.book.DbBookQuick.Companion.iconRes
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
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
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.mimeForUrl
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
import com.mycompany.app.main.MainUtil.readTextFile
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.screenBrightnessPercent
import android.text.TextUtils
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebNestView
import org.jsoup.Jsoup
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import android.content.Intent
import com.mycompany.app.quick.QuickView
import com.mycompany.app.web.WebNestView.WebQuickListener
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.quick.QuickAdapter
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.quick.QuickControl
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.web.WebReadTask
import com.mycompany.app.pref.PrefAlbum
import android.content.DialogInterface
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.PopMenuItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.dialog.DialogQuickEdit
import com.mycompany.app.quick.QuickSubView
import com.mycompany.app.quick.QuickNews
import com.mycompany.app.main.MainTransNews
import com.mycompany.app.dialog.DialogViewRead
import com.mycompany.app.view.MyEditAuto
import com.mycompany.app.quick.QuickSearch
import com.mycompany.app.web.WebSearchAdapter
import com.mycompany.app.view.MyEditAuto.KeyBackListener
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsAnimationCompat.BoundsCompat
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.quick.QuickNewsCustom
import com.mycompany.app.quick.QuickNewsGoogle
import com.mycompany.app.web.WebSearchAdapter.WebSearchListener
import com.mycompany.app.web.WebSearchAdapter.SearchItem
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.quick.QuickNews.NewsListListener
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.book.DataBookRecent
import com.mycompany.app.dialog.DialogDeleteItem.DelItemListener
import com.mycompany.app.dialog.DialogQuickEdit.QuickEditListener
import com.mycompany.app.widget.WidgetSearchProviderLight
import com.mycompany.app.widget.WidgetSearchProviderDark
import android.view.View
import android.webkit.URLUtil
import ch.opumlabs.opumbrowser.R
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.InputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLDecoder
import java.util.ArrayList
import com.mycompany.app.web.WebViewActivity.Companion.checkedPageUrl

/*
 * The browser activity's quick callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaQuickSearchImeAnimation(@JvmField val wa: WebViewActivity) : WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE) {

    internal inner class ImeAnimationDoneStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.quickState.quickSearchImeAnimationBusy = false
            val quickSearch = webViewActivity.quickSearch
            if (quickSearch != null) {
                quickSearch.setBusyMode(false)
            }
        }
    }

    override fun onEnd(windowInsetsAnimationCompat: WindowInsetsAnimationCompat) {
        val handler = wa.mainHandler
        if (handler == null) {
            return
        }
        handler.post(ImeAnimationDoneStep())
    }

    override fun onPrepare(windowInsetsAnimationCompat: WindowInsetsAnimationCompat) {
        val webViewActivity = wa
        webViewActivity.quickState.quickSearchImeAnimationBusy = true
        val quickSearch = webViewActivity.quickSearch
        if (quickSearch != null) {
            quickSearch.setBusyMode(true)
        }
    }

    override fun onProgress(windowInsetsCompat: WindowInsetsCompat, list: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
        return windowInsetsCompat
    }

    override fun onStart(windowInsetsAnimationCompat: WindowInsetsAnimationCompat, boundsCompat: BoundsCompat): BoundsCompat {
        return boundsCompat
    }
}

class WvaWebQuickListener(@JvmField val wa: WebViewActivity) : WebQuickListener {

    internal inner class CheckPageUrlStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val checkedUrl: String? = WebViewActivity.checkedPageUrl(webViewActivity.currentPageUrl(false))
            webViewActivity.quickState.quickActionBusy = false
        }
    }

    internal inner class OpenNewsSettingsTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            webViewActivity.startActivity(
                Intent(
                    webViewActivity.dialogsState.appContext,
                    com.mycompany.app.ui.settings.NewsSettingsActivity::class.java as Class<*>
                )
            )
            webViewActivity.quickState.quickActionBusy = false
        }
    }

    override fun isBarUpdatePending(): Boolean {
        return wa.isBarUpdatePending()
    }

    override fun isLandscape(): Boolean {
        return wa.isLandscapeScreen()
    }

    override fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.isBusy()) {
            return
        }
        var path = quickItem!!.path
        if (TextUtils.isEmpty(path)) {
            path = quickItem.link
        }
        webViewActivity.dialogsState.quickItemIsLink = !isFolder
        webViewActivity.showUrlLinkDialog(path, quickItem.imageUrl, false, quickItem.label, false)
        val dialogUrlLink = webViewActivity.urlLinkDialog
        if (dialogUrlLink != null) {
            dialogUrlLink.iconUrl = quickItem.thumbUrl
        }
        webViewActivity.dialogsState.quickItemIsLink = false
    }

    override fun hasTransNews(): Boolean {
        return wa.quickState.transNews != null
    }

    override fun openNewsItem(quickItem: QuickItem?) {
        val webViewActivity = wa
        if (webViewActivity.isBusy()) {
            return
        }
        val newsOpen = ZtwoSettings.newsOpen.value
        if (newsOpen == 0) {
            webViewActivity.showQuickAddDialog(null, quickItem, false)
            return
        }
        var linkAction = 1
        if (newsOpen != 1) {
            linkAction = 2
            if (newsOpen != 2) {
                var nextAction = 3
                if (newsOpen != 3) {
                    nextAction = 4
                    if (newsOpen != 4) {
                        nextAction = 5
                        if (newsOpen == 5) {
                            linkAction = nextAction
                        } else if (!SyncSettings.secretMode.value) {
                            linkAction = 6
                        }
                    } else {
                        linkAction = nextAction
                    }
                } else {
                    linkAction = nextAction
                }
            }
        }
        var path = quickItem!!.path
        if (TextUtils.isEmpty(path)) {
            path = quickItem.link
        }
        webViewActivity.onUrlLinkAction(linkAction, path, quickItem.label)
    }

    override fun webBody(): MyWebBody? {
        return wa.dialogsState.webBody
    }

    override fun newsListType(): Int {
        return wa.quickState.newsListType
    }

    override fun setQuickMode(enabled: Boolean) {
        val webViewActivity = wa
        if (webViewActivity.webCoord != null) {
            if (!enabled) {
                webViewActivity.setBarsDisabled(1, false)
                webViewActivity.currentPage!!.setQuickControl(null)
                val quickControl = webViewActivity.quickControl
                if (quickControl != null) {
                    quickControl.release()
                    webViewActivity.floatingState.overlays?.removeView(webViewActivity.quickControl)
                    webViewActivity.quickControl = null
                }
            } else if (webViewActivity.quickControl != null) {
                webViewActivity.setBarsDisabled(1, true)
                webViewActivity.quickControl!!.setQuickMode(webViewActivity.onStartPage)
                webViewActivity.currentPage!!.setQuickControl(webViewActivity.quickControl)
            } else if (!webViewActivity.quickState.webQuickListenerBusy) {
                webViewActivity.quickState.webQuickListenerBusy = true
                val handler = webViewActivity.mainHandler
                if (handler != null) {
                    handler.post(WvaEnterQuickMode(webViewActivity))
                }
            }
        }
        if (enabled) {
            webViewActivity.updateRssButton(false)
        }
    }

    override fun closeDeleteDialog() {
        wa.closeQuickDeleteDialog()
    }

    override fun runQuickAction2() {
        val webViewActivity = wa
        if (webViewActivity.isBusy() || webViewActivity.quickState.quickActionBusy) {
            return
        }
        webViewActivity.quickState.quickActionBusy = true
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(OpenNewsSettingsTask())
    }

    override fun runQuickAction1() {
        val webViewActivity = wa
        if (webViewActivity.isBusy() || webViewActivity.quickState.quickActionBusy) {
            return
        }
        webViewActivity.quickState.quickActionBusy = true
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(CheckPageUrlStep())
    }

    override fun showEditDialog(quickItem: QuickItem?) {
        val webViewActivity = wa
        if (webViewActivity.isBusy()) {
            return
        }
        webViewActivity.showQuickEditDialog(quickItem)
    }

    override fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?) {
        val webViewActivity = wa
        if (webViewActivity.quickState.transNews != null) {
            return
        }
        webViewActivity.quickState.transNews = MainTransNews(
            webViewActivity,
            webViewActivity.dialogsState.appContext,
            webViewActivity.webCoord,
            list,
            WvaNewsTranslateListener(webViewActivity)
        )
    }

    override fun showDeleteDialog(quickItem: QuickItem?, position: Int) {
        val webViewActivity = wa
        if (webViewActivity.isBusy()) {
            return
        }
        webViewActivity.showQuickDeleteDialog(quickItem, position)
    }

    override fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int) {
        val webViewActivity = wa
        if (webViewActivity.isBusy() || webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.openQuickFolder(type, left, top, path, color)
    }

    override fun isAddingTab(): Boolean {
        val webViewActivity = wa
        if (webViewActivity.videoState.addTabImplBusy && AlbumSettings.tabAnim.value) {
            return true
        }
        return webViewActivity.isFrameIdle()
    }

    override fun addRssButton() {
        if (ZtwoSettings.newsUse.value && ZtriSettings.newsIcon.value && !QuickView.isCustomNewsUrl()) {
            wa.addRssButton()
        }
    }

    override fun showSettingsMenu(view: View?) {
        val webViewActivity = wa
        if (webViewActivity.isBusy() || webViewActivity.dialogsState.quickSettingsMenu != null) {
            return
        }
        webViewActivity.closeQuickSettingsMenu()
        if (view != null) {
            val arrayList: ArrayList<Any?> = ArrayList<Any?>()
            // with icons (owner's request, 2026-11-19); "Shortcuts" is now called "Favourites"
            val darkMenu = MainUtil.isDarkPage(webViewActivity.onStartPage)
            fun icon(item: PopMenuItem, black: Int, dark: Int) = item.also { it.iconRes = if (darkMenu) dark else black }
            arrayList.add(icon(PopMenuItem(0, AppR.string.quick_access), AppR.drawable.outline_favorite_black_24, AppR.drawable.outline_favorite_dark_24))
            val isApi = MainConst.isApi28
            // "News" (its settings) left out: the news page has its own settings button (2026-11-19)
            arrayList.add(icon(PopMenuItem(2, AppR.string.background), AppR.drawable.outline_wallpaper_black_24, AppR.drawable.outline_wallpaper_dark_24))
            if (ZtriSettings.quickUse.value) {
                arrayList.add(icon(PopMenuItem(3, AppR.string.add), AppR.drawable.outline_add_black_24, AppR.drawable.outline_add_dark_24))
            }
            arrayList.add(icon(PopMenuItem(4, AppR.string.at_bottom, 0, ZtwoSettings.quickDown.value), AppR.drawable.outline_vertical_align_bottom_black_24, AppR.drawable.outline_vertical_align_bottom_dark_24))
            val myPopupMenu = MyPopupMenu(
                webViewActivity,
                webViewActivity.dialogsState.webBody,
                view,
                arrayList,
                MainUtil.isDarkPage(webViewActivity.onStartPage),
                WvaQuickMenuListener(webViewActivity)
            )
            webViewActivity.dialogsState.quickSettingsMenu = myPopupMenu
            webViewActivity.popupMenu = myPopupMenu
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaQuickSettingsMenuClosed(webViewActivity))
        }
    }

    override fun openSearchSource(type: Int, url: String?) {
        val webViewActivity = wa
        if (webViewActivity.isBusy() || webViewActivity.currentPage == null) {
            return
        }
        webViewActivity.pageState.addressEditBusy = false
        webViewActivity.setAddressEditing(false)
        if (type != 0) {
            if (type == 1) {
                webViewActivity.launchExternalForResult(
                    Intent(webViewActivity.dialogsState.appContext, com.mycompany.app.ui.lists.QuickAddActivity::class.java as Class<*>),
                    RequestCode.QUICK_ADD
                )
            }
        } else {
            if (TextUtils.isEmpty(url)) {
                return
            }
            if (hasZtwoFlag(8)) {
                webViewActivity.addTabPlain(null, url, true, null)
            } else {
                webViewActivity.openInCurrentPage(url, null)
            }
        }
    }

    override fun refreshUpButton() {
        val webViewActivity = wa
        if (webViewActivity.onStartPage) {
            webViewActivity.updateUpButton(true)
        }
    }

    override fun loadSearchItems() {
        val quickSearch = wa.quickSearch
        if (quickSearch != null) {
            quickSearch.loadItems()
        }
    }

    override fun scrollPage() {
        val webViewActivity = wa
        if (webViewActivity.isBusy()) {
            return
        }
        webViewActivity.pageScroll(true, true)
    }

    override fun hasNews(): Boolean {
        return wa.quickState.quickNews != null
    }

    override fun loadNews() {
        val newsUrl: String?
        val quickNewsCustom: QuickNews?
        val webViewActivity = wa
        val webFltView = webViewActivity.floatingState.rssButton
        if (webFltView != null) {
            webFltView.setLoad(true)
            webViewActivity.floatingState.rssButton!!.setHideBlocked(true)
        }
        if (webViewActivity.quickState.quickNews == null) {
            val mainTransNews = webViewActivity.quickState.transNews
            var builtUrl: String? = null
            if (mainTransNews != null) {
                mainTransNews.release()
                webViewActivity.quickState.transNews = null
            }
            webViewActivity.quickState.newsListType = 0
            if (QuickView.isCustomNewsUrl()) {
                newsUrl = ZtwoSettings.newsUrl.value
                quickNewsCustom = QuickNewsCustom(true)
                webViewActivity.quickState.quickNews = quickNewsCustom
            } else {
                val context = webViewActivity.dialogsState.appContext
                if (context != null) {
                    val sb = StringBuilder("https://news.google.com")
                    if (ZtwoSettings.newsTopic.value == 9) {
                        sb.append("/search?q=")
                        sb.append(ZtwoSettings.newsSearch.value)
                    } else {
                        sb.append("/headlines/section/topic/")
                        sb.append(QuickView.topics[ZtwoSettings.newsTopic.value])
                    }
                    if (ZtwoSettings.newsTopic.value == 9) {
                        sb.append("&hl=")
                    } else {
                        sb.append("?hl=")
                    }
                    sb.append(
                        QuickView.newsLocale(
                            context,
                            context.getResources().getStringArray(AppR.array.news_code)
                        )
                    )
                    builtUrl = sb.toString()
                }
                val myWebCoord = webViewActivity.webCoord
                val quickNewsGoogle = QuickNewsGoogle(false)
                quickNewsGoogle.hostActivity = webViewActivity
                quickNewsGoogle.parentView = myWebCoord
                webViewActivity.quickState.quickNews = quickNewsGoogle
                newsUrl = builtUrl
                quickNewsCustom = quickNewsGoogle
            }
            quickNewsCustom.start(
                webViewActivity.dialogsState.appContext,
                webViewActivity.mainHandler,
                newsUrl,
                WvaNewsListListener(webViewActivity)
            )
            webViewActivity.videoState.webQuickListenerBusy = true
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.postDelayed(WvaWebQuickListenerTask(webViewActivity), 200L)
        }
    }
}

class WvaQuickSearchListener(@JvmField val wa: WebViewActivity) : WebSearchListener {
    override fun onItemsShown(count: Int, isEmpty: Boolean) {
        val webViewActivity = wa
        val quickSearch: QuickSearch? = if (!webViewActivity.quickState.editingAddress) null else webViewActivity.quickSearch
        if (!webViewActivity.quickState.editingAddress || quickSearch == null) {
            return
        }
        quickSearch.showAuto(count, isEmpty)
    }

    override fun headerCount(): Int {
        val quickSearch = wa.quickSearch
        if (quickSearch == null) {
            return 0
        }
        return quickSearch.outPadHeight
    }

    override fun loadRecent() {
        val quickSearch = wa.quickSearch
        if (quickSearch != null) {
            quickSearch.loadRecent()
        }
    }

    override fun showClip(): Boolean {
        return "file:///android_asset/shortcut.html" != wa.dialogsState.pageUrl
    }

    override fun setClipText(clipText: String?) {
        val quickSearch = wa.quickSearch
        if (quickSearch != null) {
            quickSearch.setLoadClip(clipText)
        }
    }

    override fun setQuery(cursor: Int, query: String?) {
        val myEditAuto = wa.editAuto
        if (myEditAuto == null) {
            return
        }
        myEditAuto.setTextAt(cursor, query)
    }

    override fun onItemClick(searchItem: SearchItem?) {
        wa.onSearchItemClick(searchItem)
    }
}

class WvaFakeTsCheck(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val page: String? = null) : Runnable {
    /**
     * Checks whether the stream qi (page wa.ri) is a fake ".ts" file (wa.an image/FFmpeg container): reads its first
     * 48 bytes; if they mention "FFmpeg" it is reopened as "tsfake:" + url, otherwise wa.O8 counts the miss.
     */
    override fun run() {
        val activity = wa
        val url = this@WvaFakeTsCheck.url
        val page = this@WvaFakeTsCheck.page
        if (TextUtils.isEmpty(url)) {
            return
        }
        var fake = false
        val conn = openHttpConnection(0, 0, activity.dialogsState.appContext, url, page, false)
        if (conn != null) {
            var `in`: InputStream? = null
            try {
                conn.setDoInput(true)
                conn.connect()
                `in` = conn.getInputStream()
                val head = ByteArray(48)
                `in`.read(head, 0, 48)
                val headText = String(head)
                fake = !TextUtils.isEmpty(headText) && headText.contains("FFmpeg")
            } catch (exception: Exception) {
                fake = false
            }
            try {
                if (`in` != null) {
                    `in`.close()
                }
            } catch (ignored: Exception) {
            }
            conn.disconnect()
        }
        if (!fake) {
            activity.quickState.fakeTsMisses++
            return
        }
        activity.quickState.fakeTsMisses = 0
        activity.findPageVideos("tsfake:" + url, page, null, null)
    }
}

class WvaQuickSearchRefresh(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val quickSearch = wa.quickSearch
        if (quickSearch != null) {
            quickSearch.setupViews()
        }
    }
}

class WvaNewsListListener(@JvmField val wa: WebViewActivity) : NewsListListener {

    internal inner class NewsRefreshTask : Runnable {
        override fun run() {
            val webViewActivity = wa
            val webNestView: WebNestView? = if (webViewActivity.isPaused) null else webViewActivity.currentPage
            val quickView: QuickView? = if (webViewActivity.isPaused || webNestView == null) null else webNestView.quickPanel
            val quickAdapter: QuickAdapter? = if (webViewActivity.isPaused || webNestView == null || quickView == null || !ZtwoSettings.newsUse.value || !quickView.onStartPage || quickView.newsTime == 0L || quickView.loading) null else quickView.itemAdapter
            if (webViewActivity.isPaused || webNestView == null || quickView == null || !ZtwoSettings.newsUse.value || !quickView.onStartPage || quickView.newsTime == 0L || quickView.loading || quickAdapter == null || quickView.quickListener == null || quickAdapter.editMode || quickView.getVisibility() != View.VISIBLE || System.currentTimeMillis() <= quickView.newsTime + 1800000) {
                return
            }
            quickView.loadNews(true)
        }
    }

    override fun onTranslated(list: MutableList<QuickAdapter.QuickItem?>?) {
        val webViewActivity = wa
        val webNestView: WebNestView? = if (webViewActivity.quickState.quickNews == null) null else webViewActivity.currentPage
        if (webViewActivity.quickState.quickNews == null || webNestView == null) {
            return
        }
        webNestView.setNewsPrepared(list)
    }

    override fun onList(list: MutableList<QuickAdapter.QuickItem?>?, isOk: Boolean) {
        val webViewActivity = wa
        val quickNews = webViewActivity.quickState.quickNews
        if (quickNews != null) {
            quickNews.release()
            webViewActivity.quickState.quickNews = null
            webViewActivity.quickState.newsListType = 0
            webViewActivity.setRssLoading(false)
            val webNestView = webViewActivity.currentPage
            val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
            if (webNestView != null && quickView != null) {
                quickView.newsTime = DataNews.instance(quickView.ctx).loadedAt
                val quickAdapter = quickView.itemAdapter
                if (quickAdapter != null) {
                    if (isOk) {
                        quickAdapter.refreshNewsRange()
                    } else {
                        quickAdapter.applyNewsList(list, true)
                    }
                    quickAdapter.newsLoading = false
                }
                quickView.newsChanged = true
                quickView.scheduleScroll()
            }
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody == null) {
                return
            }
            val runnable = webViewActivity.quickState.newsRefreshTask
            if (runnable == null) {
                webViewActivity.quickState.newsRefreshTask = NewsRefreshTask()
            } else {
                myWebBody.removeCallbacks(runnable)
            }
            webViewActivity.dialogsState.webBody!!.postDelayed(webViewActivity.quickState.newsRefreshTask, 3600000L)
        }
    }

    override fun onProgress(progress: Int) {
        val webViewActivity = wa
        webViewActivity.quickState.newsListType = progress
        val webNestView = webViewActivity.currentPage
        if (webNestView != null) {
            webNestView.setNewsProgress(progress)
        }
    }
}

class WvaQuickRecentRefresh(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.quickState.editingAddress) {
            val quickSearch = webViewActivity.quickSearch
            if (quickSearch != null) {
                quickSearch.clipShown = hasClipboard(quickSearch.dataActivity)
            }
            val arrayList = DataBookRecent.instance(webViewActivity.dialogsState.appContext).items
            if (if (arrayList == null || arrayList.size <= 0) !(!DbBookRecent.isEnabled() || (ZtriSettings.qckSchType.value != 2 && (BrowserSettings.sugType.value and 2) != 2)) else !(ZtriSettings.qckSchType.value != 2 || !DataBookRecent.instance(
                    webViewActivity.dialogsState.appContext
                ).changed)
            ) {
                val arrayListC: ArrayList<com.mycompany.app.web.WebSearchAdapter.SearchItem?>? = DbBookRecent.loadItems(webViewActivity.dialogsState.appContext)
                val dataBookRecentA = DataBookRecent.instance(webViewActivity.dialogsState.appContext)
                dataBookRecentA.items = arrayListC
                dataBookRecentA.changed = false
                val quickSearchNow = webViewActivity.quickSearch
                if (quickSearchNow != null) {
                    quickSearchNow.loadRecent()
                }
            }
            val myWebBody: MyWebBody? = if (!webViewActivity.quickState.editingAddress) null else webViewActivity.dialogsState.webBody
            if (!webViewActivity.quickState.editingAddress || myWebBody == null) {
                return
            }
            myWebBody.post(WvaQuickSearchStart(webViewActivity))
        }
    }
}

class WvaQuickSearchStart(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class StartQuickSearchStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            if (webViewActivity.quickState.editingAddress) {
                if (BrowserSettings.sugEngine.value == 0 && BrowserSettings.sugType.value == 0) {
                    return
                }
                webViewActivity.cancelSearchTask()
                if (!webViewActivity.quickState.editingAddress || webViewActivity.currentPage == null) {
                    return
                }
                val searchTask = WebViewActivity.SearchTask(webViewActivity)
                webViewActivity.searchTask = searchTask
                searchTask.execute(webViewActivity.dialogsState.appContext)
            }
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.quickState.editingAddress) {
            webViewActivity.applyEditColor(true)
            val quickSearch = webViewActivity.quickSearch
            if (quickSearch != null) {
                quickSearch.applyState(true, webViewActivity.onStartPage, !webViewActivity.isSingleBarMode())
            }
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(StartQuickSearchStep())
        }
    }
}

class WvaEnterQuickMode(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage == null) {
            webViewActivity.quickState.webQuickListenerBusy = false
            return
        }
        webViewActivity.setBarsDisabled(1, true)
        val quickControl = webViewActivity.quickControl
        if (quickControl != null) {
            quickControl.setQuickMode(webViewActivity.onStartPage)
            webViewActivity.currentPage!!.setQuickControl(webViewActivity.quickControl)
            webViewActivity.quickState.webQuickListenerBusy = false
            return
        }
        val newQuickControl = QuickControl(webViewActivity)
        webViewActivity.quickControl = newQuickControl
        newQuickControl.applyMode(webViewActivity.onStartPage)
        val handler = webViewActivity.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaShowQuickControl(webViewActivity))
    }
}

class WvaShowQuickControl(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val myWebCoord = webViewActivity.webCoord
        val quickControl: QuickControl? = if (myWebCoord != null) webViewActivity.quickControl else null
        if (myWebCoord != null && quickControl != null && webViewActivity.currentPage != null) {
            wa.floatingState.overlays!!.addCoordView(quickControl, com.mycompany.app.ui.browser.OverlayParams(-1, -1))
            webViewActivity.currentPage!!.setQuickControl(webViewActivity.quickControl)
        }
        webViewActivity.quickState.webQuickListenerBusy = false
    }
}

class WvaQuickDeleteListener(@JvmField val wa: WebViewActivity, @JvmField val showQuickDeleteDialogQuickItem: com.mycompany.app.quick.QuickAdapter.QuickItem? = null, @JvmField val position: Int = 0) : DelItemListener {

    override fun onDelete() {
        val reloaded: Boolean
        val webViewActivity = wa
        if (webViewActivity.quickDeleteDialog != null) {
            val quickSubView = webViewActivity.quickState.quickSubView
            if (quickSubView != null) {
                reloaded = quickSubView.reloadItems()
            } else {
                val quickSchEdit = webViewActivity.videoState.quickSchEdit
                if (quickSchEdit != null) {
                    val quickView = quickSchEdit.shownView
                    if (quickView != null) {
                        reloaded = quickView.loadItems()
                    } else {
                        reloaded = false
                    }
                } else {
                    val webNestView = webViewActivity.currentPage
                    val quickView: QuickView? = if (webNestView == null) null else webNestView.quickPanel
                    if (webNestView == null || quickView == null) {
                        reloaded = false
                    } else {
                        reloaded = quickView.loadItems()
                    }
                }
            }
            if (reloaded) {
                return
            }
            webViewActivity.closeQuickDeleteDialog()
        }
    }

    override fun bindHead(myRoundImage: MyRoundImage?, appCompatTextView: AppCompatTextView?) {
        val webViewActivity = wa
        val quickItem = this@WvaQuickDeleteListener.showQuickDeleteDialogQuickItem
        val position = this@WvaQuickDeleteListener.position
        if (webViewActivity.quickDeleteDialog == null) {
            return
        }
        if (quickItem == null) {
            val sb = StringBuilder()
            sb.append(position)
            sb.append(webViewActivity.getString(if (position > 1) AppR.string.items else AppR.string.item))
            myRoundImage!!.showIcon(-460552, AppR.drawable.outline_public_black_24)
            appCompatTextView!!.setText(sb.toString())
            return
        }
        val arrayList = if (quickItem.isFolder) quickItem.subItems else null
        if (arrayList == null || arrayList.isEmpty()) {
            val color = quickItem.color
            if (color == 0 || color == -460552) {
                val bitmapC = MainListLoader.cachedListIcon(quickItem.path, SyncSettings.secretMode.value)
                if (isBitmapOk(bitmapC)) {
                    myRoundImage!!.setIconSmall(true)
                    myRoundImage.setImageBitmap(bitmapC)
                } else {
                    myRoundImage!!.showLetterIcon(
                        -460552,
                        AppR.drawable.outline_public_black_24,
                        quickItem.title,
                        null
                    )
                }
            } else if (quickItem.isFolder) {
                myRoundImage!!.showIcon(0, iconRes(color))
            } else {
                myRoundImage!!.setCircleRadius((MainApp.dp4 * 3).toFloat())
                myRoundImage.showLetterNeutral(quickItem.color, quickItem.title, null)
            }
        } else {
            myRoundImage!!.setFolder(1, arrayList, SyncSettings.secretMode.value, if (AppState.darkUi) -11513776 else -460552)
        }
        if (!TextUtils.isEmpty(quickItem.label)) {
            appCompatTextView!!.setText(quickItem.label)
            return
        }
        appCompatTextView!!.setText(position.toString() + webViewActivity.getString(AppR.string.item))
    }
}

class WvaQuickDeleteDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        webViewActivity.closeQuickDeleteDialog()
    }
}

class WvaQuickAdded(@JvmField val wa: WebViewActivity) : QuickEditListener {
    override fun onSaved(path: String?, color: Int, order: Int, title: String?) {
        val webViewActivity = wa
        webViewActivity.closeQuickEditDialog()
        showToast(webViewActivity, AppR.string.added)
        if (webViewActivity.quickState.quickSubView == null) {
            val webNestView = webViewActivity.currentPage
            val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
            if (webNestView != null && quickView != null) {
                quickView.addItem(path, color, order, title)
            }
            val quickSearch = webViewActivity.quickSearch
            val quickAdapter: QuickAdapter? = if (quickSearch == null) null else quickSearch.gridAdapter
            if (quickSearch == null || quickAdapter == null) {
                return
            }
            quickAdapter.insertItem(path, color, order, title)
        }
    }

    override fun onSavedCustom(
        oldPath: String?,
        path: String?,
        title: String?,
        color: Int,
        arrayList: ArrayList<com.mycompany.app.quick.QuickAdapter.QuickSubItem?>?
    ) {
        var arrayList2: ArrayList<com.mycompany.app.quick.QuickAdapter.QuickSubItem?>? = null
        val webViewActivity = wa
        webViewActivity.closeQuickEditDialog()
        showToast(webViewActivity, AppR.string.changed)
        val quickSubView = webViewActivity.quickState.quickSubView
        if (quickSubView != null) {
            val quickAdapter = quickSubView.subAdapter
            if (quickAdapter != null) {
                quickAdapter.updateItem(oldPath, path, title, color, null)
                quickSubView.closing = true
                quickSubView.leaveEditMode()
                return
            }
            return
        }
        val quickSchEdit = webViewActivity.videoState.quickSchEdit
        val quickView2: QuickView? = if (quickSchEdit == null) null else quickSchEdit.shownView
        val quickAdapter3: QuickAdapter? = if (quickSchEdit == null || quickView2 == null) null else quickView2.itemAdapter
        if (quickSchEdit == null || quickView2 == null || quickAdapter3 == null) {
            arrayList2 = arrayList
        } else {
            arrayList2 = arrayList
            quickAdapter3.updateItem(oldPath, path, title, color, arrayList2)
            quickView2.exitEditMode()
        }
        val webNestView = webViewActivity.currentPage
        val quickView: QuickView? = if (webNestView != null) webNestView.quickPanel else null
        val quickAdapter2: QuickAdapter? = if (webNestView != null && quickView != null) quickView.itemAdapter else null
        if (webNestView != null && quickView != null && quickAdapter2 != null) {
            quickAdapter2.updateItem(oldPath, path, title, color, arrayList2)
            quickView.exitEditMode()
        }
        val quickSearch = webViewActivity.quickSearch
        val quickAdapter: QuickAdapter? = if (quickSearch == null) null else quickSearch.gridAdapter
        if (quickSearch == null || quickAdapter == null) {
            return
        }
        quickAdapter.updateItem(oldPath, path, title, color, arrayList2)
    }
}

class WvaCreateQuickSubView(@JvmField val wa: WebViewActivity, @JvmField val folderPath: String? = null, @JvmField val originX: Int = 0, @JvmField val originY: Int = 0, @JvmField val cellSize: Int = 0, @JvmField val rowCount: Int = 0) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val originX = this@WvaCreateQuickSubView.originX
        val originY = this@WvaCreateQuickSubView.originY
        val cellSize = this@WvaCreateQuickSubView.cellSize
        val rowCount = this@WvaCreateQuickSubView.rowCount
        val folderPath = this@WvaCreateQuickSubView.folderPath
        webViewActivity.dialogsState.openQuickFolderBusy = false
        if (webViewActivity.quickState.quickSubView == null) {
            val quickSubView = QuickSubView(webViewActivity)
            quickSubView.ghostShown = true
            quickSubView.appContext = webViewActivity
            webViewActivity.quickState.quickSubView = quickSubView
            val onStartPage = webViewActivity.onStartPage
            quickSubView.subListener = WvaQuickSubListener(webViewActivity)
            quickSubView.originX = originX
            quickSubView.originY = originY
            quickSubView.cellSize = cellSize
            quickSubView.rowCount = rowCount
            quickSubView.folderPath = folderPath
            quickSubView.darkPage = onStartPage
            quickSubView.buildViews()
            val handler = webViewActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(WvaShowQuickSubView(webViewActivity))
        }
    }
}

class WvaShowQuickSubView(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val quickSubView = webViewActivity.quickState.quickSubView
        if (quickSubView == null) {
            return
        }
        if (webViewActivity.quickState.editingAddress) {
            quickSubView.setPadding(0, 0, 0, webViewActivity.quickSearch!!.outPadBottom)
        }
        wa.floatingState.overlays!!.addView(webViewActivity.quickState.quickSubView, -1, -1)
        webViewActivity.quickState.quickSubView!!.postRefresh()
    }
}

class WvaResumeInit(@JvmField val wa: WebViewActivity) : Runnable {
    /**
     * Startup work: loads the native library, after wa.an update (AppState.startupPending) clears stale downloads, refreshes the
     * search widgets and the wallpaper/theme state (wa.setTmemDialog), then runs wa.checkPendingDownloads() when needed.
     */
    override fun run() {
        if (!AppState.nativeLoaded) {
            try {
                System.loadLibrary("myutil")
                AppState.nativeLoaded = true
            } catch (ignored: Throwable) {
            }
        }
        val updated = AppState.startupPending
        val activity = wa
        if (updated) {
            AppState.startupPending = false
            try {
                DbBookDown.clearDone(activity.dialogsState.appContext)
                if (!TextUtils.isEmpty(MainSettings.widgetBook.value)) {
                    notifyBookUpdate(activity.dialogsState.appContext, true)
                }
                val context = activity.dialogsState.appContext
                if (context != null) {
                    val light = Intent(context, WidgetSearchProviderLight::class.java)
                    light.setAction("ch.opumlabs.opumbrowser.ACTION_SRCHGET_UPDATE")
                    context.sendBroadcast(light)
                    val dark = Intent(context, WidgetSearchProviderDark::class.java)
                    dark.setAction("ch.opumlabs.opumbrowser.ACTION_SRCHGET_UPDATE")
                    context.sendBroadcast(dark)
                }
            } catch (ignored: Exception) {
            }
        }
        if (BrowserSettings.checkDown.value || updated) {
            activity.checkPendingDownloads()
        }
    }
}

class WvaShowViewReadDialog(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null, @JvmField val title: String? = null, @JvmField val showQuickAddDialogQuickItem: com.mycompany.app.quick.QuickAdapter.QuickItem? = null, @JvmField val fromHtml: Boolean = false, @JvmField val transOn: Boolean = false) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaShowViewReadDialog.url
        val title = this@WvaShowViewReadDialog.title
        val quickItem = this@WvaShowViewReadDialog.showQuickAddDialogQuickItem
        val fromHtml = this@WvaShowViewReadDialog.fromHtml
        val transOn = this@WvaShowViewReadDialog.transOn
        webViewActivity.quickState.quickAddBusy = false
        if (webViewActivity.dialogsState.viewReadDialog != null || webViewActivity.webCoord == null) {
            return
        }
        val dialogViewRead = DialogViewRead(webViewActivity)
        webViewActivity.dialogsState.viewReadDialog = dialogViewRead
        dialogViewRead.openReader(
            webViewActivity,
            webViewActivity.dialogsState.webBody,
            url,
            title,
            quickItem,
            fromHtml,
            webViewActivity.dialogsState.topBarColor,
            webViewActivity.dialogsState.bottomBarColor,
            webViewActivity.dialogsState.windowBgColor,
            transOn,
            WvaReadDialogListener(webViewActivity)
        )
    }
}

class WvaLongPressInspect(@JvmField val wa: WebViewActivity, @JvmField val html: String? = null) : Runnable {
    /**
     * Finds the page's preview media from its html (Hn) when none is known yet (In): on Facebook the last
     * attribute holding wa.an http .jpg url; elsewhere the reader-mode cover image, else the first real <video> or
     * <source type=video/ *></source> url (blob: and YouTube skipped, relative urls resolved). wa.Nn marks a video; a video
     * url is also kept in wa.On and shown via WvaLongPressInspect2. Always ends with wa.Jn = true and wa.handleLinkLongPress().
    </video> */
    override fun run() {
        val activity = wa
        val html = this@WvaLongPressInspect.html
        val page = activity.dialogsState.pageUrl
        val host = activity.dialogsState.pageHost
        if (TextUtils.isEmpty(activity.quickState.longPressImage) && !TextUtils.isEmpty(host) && !TextUtils.isEmpty(
                html
            )
        ) {
            var doc: Document?
            try {
                doc = Jsoup.parse(html!!, page!!)
            } catch (exception: Exception) {
                doc = null
            }
            if (doc != null) {
                if (host!!.endsWith("facebook.com")) {
                    val image = facebookImage(doc)
                    if (!TextUtils.isEmpty(image)) {
                        activity.quickState.longPressImage = image
                        activity.quickState.longPressInspectBusy = false
                    }
                } else {
                    var image: String? = WebReadTask.Companion.firstImage(doc)
                    if (TextUtils.isEmpty(image)) {
                        image = WebReadTask.Companion.figureImage(doc)
                    }
                    if (TextUtils.isEmpty(image)) {
                        image = WebReadTask.Companion.pictureImage(doc)
                    }
                    if (TextUtils.isEmpty(image)) {
                        image = WebReadTask.Companion.ampImage(doc)
                    }
                    val base = MainUtil.hostOf(page, true)
                    var video: String? = null
                    try {
                        video = firstVideo(doc.select("video"), base, false)
                    } catch (ignored: Exception) {
                    }
                    if (video == null) {
                        try {
                            video = firstVideo(doc.select("source"), base, true)
                        } catch (ignored: Exception) {
                        }
                    }
                    if (!TextUtils.isEmpty(image)) {
                        activity.quickState.longPressImage = image
                        activity.quickState.longPressInspectBusy = false
                    } else if (!TextUtils.isEmpty(video)) {
                        activity.quickState.longPressImage = video
                        activity.quickState.longPressInspectBusy = true
                    }
                    activity.quickState.longPressVideo = video
                    if (!TextUtils.isEmpty(video) && activity.webCoord != null) {
                        activity.webCoord!!.postDelayed(WvaLongPressInspect2(activity), 300L)
                    }
                }
            }
        }
        activity.videoState.longPressInspectBusy = true
        activity.handleLinkLongPress()
    }

    private fun firstVideo(elements: Elements?, base: String?, typed: Boolean): String? {
        if (elements == null) {
            return null
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
            return src
        }
        return null
    }

    /** The last attribute value with wa.an http .jpg url, unquoted, unescaped and decoded.  */
    private fun facebookImage(doc: Document?): String? {
        var value: String? = null
        try {
            val all = doc!!.getAllElements()
            for (element in all) {
                for (attribute in element.attributes()) {
                    val attrValue = attribute.value
                    if (!TextUtils.isEmpty(attrValue) && attrValue.contains("http") && attrValue.contains(".jpg")) {
                        value = attrValue
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        if (TextUtils.isEmpty(value)) {
            return null
        }
        if (!value!!.startsWith("http")) {
            val len = value.length
            var quote = '"'
            var at = value.indexOf("\"http")
            if (at == -1) {
                quote = '\''
                at = value.indexOf("'http")
            }
            if (at != -1) {
                if (at + 5 >= len) {
                    return null
                }
                val end = value.indexOf(quote, at + 5)
                val start = at + 1
                if (end <= start || end > len) {
                    return null
                }
                value = value.substring(start, end)
            }
            if (TextUtils.isEmpty(value) || !value.startsWith("http")) {
                return null
            }
        }
        value = value.replace("\\/", "/").replace('\\', '%').replace(" ", "")
        var decoded: String?
        try {
            decoded = URLDecoder.decode(value, "UTF-8")
        } catch (exception: Exception) {
            decoded = value
        }
        return if (TextUtils.isEmpty(decoded)) null else decoded.replace("amp;", "")
    }
}

class WvaEditorCopyUrlClick(@JvmField val wa: WebViewActivity) : View.OnClickListener {

    internal inner class CopyUrlTask(
                                         @JvmField val path: String?
    ) : Runnable {
        override fun run() {
            val webViewActivity = wa
            val copiedClipboard = AppR.string.copied_clipboard
            val path = this.path
            copyText(webViewActivity, copiedClipboard, "Copied URL", path)
            val quickSearch = webViewActivity.quickSearch
            if (quickSearch != null) {
                quickSearch.setLoadClip(path)
            }
        }
    }

    override fun onClick(view: View?) {
        val webViewActivity = wa
        if (webViewActivity.editAuto == null || webViewActivity.isAnyDialogOpen()) {
            return
        }
        val autoText = webViewActivity.editAuto!!.autoText
        if (TextUtils.isEmpty(autoText)) {
            showToast(webViewActivity, AppR.string.empty)
        } else {
            webViewActivity.runInBackground(CopyUrlTask(autoText))
        }
    }
}

class WvaQuickBackListener(@JvmField val wa: WebViewActivity) : KeyBackListener {
    override fun onBackKey(isShown: Boolean, isEditing: Boolean): Boolean {
        val webViewActivity = wa
        if (!webViewActivity.dismissPopups()) {
            val quickSubView = webViewActivity.quickState.quickSubView
            if (quickSubView != null) {
                if ((isEditing || isShown) && !quickSubView.leaveEditMode()) {
                    quickSubView.closeView()
                    return true
                }
            } else {
                if (!webViewActivity.quickState.editingAddress) {
                    if (!isEditing || !webViewActivity.keyboardOpen) {
                        return false
                    }
                    webViewActivity.hideKeyboard()
                    return true
                }
                if (!webViewActivity.pageState.addressEditBusy) {
                    webViewActivity.setAddressEditing(false)
                    return true
                }
                val handler = webViewActivity.mainHandler
                if (handler != null) {
                    handler.postDelayed(WvaAddressEditUnlock(webViewActivity), 200L)
                    return true
                }
            }
        }
        return true
    }
}
