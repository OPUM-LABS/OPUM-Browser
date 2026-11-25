@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.web.WebNestView.RemoveCrashViewTask
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.openHeadConnection
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.openWithCookies
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.db.book.DbBookTab.Companion.insertTab
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookTab.Companion.loadTabs
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.main.MainUtil.scriptNameFor
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.pref.PrefSync.Companion.saveIndexes
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openKeyboard
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.db.book.DbBookTab.Companion.newUid
import com.mycompany.app.db.book.DbBookTab.Companion.prevUid
import com.mycompany.app.main.MainUtil.nextPageUrl
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
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
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.db.DbUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import android.widget.ImageView.ScaleType
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.dialog.DialogSetMsg
import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.ui.browser.BarScroll
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.view.MyBarView
import com.mycompany.app.view.MyEditAuto
import com.mycompany.app.view.MySnackbar.SnackItem
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.view.MyBarView.BarListener
import com.mycompany.app.view.MyEditAuto.EditAutoListener
import android.widget.TextView.OnEditorActionListener
import android.widget.TextView
import android.view.KeyEvent
import android.view.View
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import com.mycompany.app.web.WebViewActivity.Companion.tabTitleFor

/*
 * The browser activity's bars callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaExpandBarsDelayed(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ExpandBarsStep : Runnable {

        internal inner class ApplyBarsBehaviorStep : Runnable {

            internal inner class ExpandAppBarStep : Runnable {
                override fun run() {
                    val expandBarsStep = this@ExpandBarsStep
                    val webViewActivity = wa
                    val appBarLayout = webViewActivity.barScroll
                    if (appBarLayout != null) {
                        appBarLayout.setExpanded(true)
                    }
                    val myWebCoord = webViewActivity.webCoord
                    if (myWebCoord != null) {
                        myWebCoord.postDelayed(WvaApplySystemBarsAfterExpand(webViewActivity), 300L)
                    }
                    wa.dialogsState.setSystemBarsHiddenBusy = false
                }
            }

            override fun run() {
                val wvaExpandBarsDelayed = this@WvaExpandBarsDelayed
                val webViewActivity = wa
                webViewActivity.applySystemBarsBehavior(webViewActivity.dialogsState.showStatusBar)
                val myWebCoord = wa.webCoord
                if (myWebCoord == null) {
                    return
                }
                myWebCoord.post(ExpandAppBarStep())
            }
        }

        override fun run() {
            val webViewActivity = wa
            webViewActivity.updateSystemBarPadding(webViewActivity.dialogsState.showStatusBar, webViewActivity.videoState.showNavBar)
            val myWebCoord = webViewActivity.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(ApplyBarsBehaviorStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        val keyHelper = webViewActivity.keyHelper
        if (keyHelper != null) {
            keyHelper.applyMode(webViewActivity.dialogsState.showStatusBar, webViewActivity.videoState.showNavBar)
        }
        webViewActivity.applyPageLayout()
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ExpandBarsStep())
    }
}

class WvaShowTopDownButton(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class TopDownClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val webViewActivity = wa
            if (webViewActivity.topDownIcon == null) {
                return
            }
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.setDownNoti(false)
            }
            webViewActivity.topDownIcon!!.showDot(false, true)
            webViewActivity.onDownIconClick(view)
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.barsState.topBarView == null || webViewActivity.mainHandler == null) {
            return
        }
        if (webViewActivity.topDownIcon != null) {
            webViewActivity.tintDownIcon()
            return
        }
        webViewActivity.downIconColor = 1234
        webViewActivity.updateDownBadge()
        try {
            val myIconView = com.mycompany.app.ui.browser.BarIndicator(webViewActivity)
            webViewActivity.topDownIcon = myIconView
            myIconView.setDownIcon(true)
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.setDownNoti(true)
            }
            webViewActivity.topDownIcon!!.showDot(true, false)
            webViewActivity.topDownIcon!!.setScaleType(ScaleType.CENTER_INSIDE)
            webViewActivity.topDownIcon!!.setOnClickListener(TopDownClick())
            webViewActivity.applyDownIconColor(webViewActivity.pageThemeColor(), webViewActivity.pageThemeLight())
            webViewActivity.updateImageButton(true)
            webViewActivity.barsState.topBarView!!.addView(webViewActivity.topDownIcon, MainApp.dp48, -1)
        } catch (unused: Exception) {
        }
    }
}

class WvaShowBottomDownButton(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class BottomDownClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val webViewActivity = wa
            if (webViewActivity.barsState.bottomDownIcon == null) {
                return
            }
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.setDownNoti(false)
            }
            webViewActivity.barsState.bottomDownIcon!!.showDot(false, true)
            webViewActivity.onDownIconClick(view)
        }
    }

    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.barsState.bottomBarView == null || webViewActivity.mainHandler == null) {
            return
        }
        if (webViewActivity.barsState.bottomDownIcon != null) {
            webViewActivity.tintDownIcon()
            return
        }
        webViewActivity.downIconColor = 1234
        webViewActivity.updateDownBadge()
        try {
            val myIconView = com.mycompany.app.ui.browser.BarIndicator(webViewActivity)
            webViewActivity.barsState.bottomDownIcon = myIconView
            myIconView.setDownIcon(true)
            val webNestView = webViewActivity.currentPage
            if (webNestView != null) {
                webNestView.setDownNoti(true)
            }
            webViewActivity.barsState.bottomDownIcon!!.showDot(true, false)
            webViewActivity.barsState.bottomDownIcon!!.setScaleType(ScaleType.CENTER_INSIDE)
            webViewActivity.barsState.bottomDownIcon!!.setOnClickListener(BottomDownClick())
            webViewActivity.applyDownIconColor(webViewActivity.pageThemeColor(), webViewActivity.pageThemeLight())
            webViewActivity.updateImageButton(true)
            webViewActivity.barsState.bottomBarView!!.addView(webViewActivity.barsState.bottomDownIcon, MainApp.dp48, -1)
        } catch (unused: Exception) {
        }
    }
}

class WvaVideoDownFound(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage != null) {
            webViewActivity.videoDownFound = true
            val webVideoFrame = webViewActivity.videoFrame
            if (webVideoFrame != null) {
                webVideoFrame.setVideoDown(true)
            }
            if (webViewActivity.isInputBusy() || webViewActivity.dialogsState.onYouTube) {
                return
            }
            var found = false
            if (ZoneSettings.downPos.value == 1 && webViewActivity.topDownIcon != null) {
                webViewActivity.currentPage!!.setDownNoti(true)
                webViewActivity.topDownIcon!!.showDot(true, true)
                webViewActivity.topDownIcon!!.setVisibility(View.VISIBLE)
            }
            if (ZoneSettings.downPos.value == 2 && webViewActivity.barsState.bottomDownIcon != null) {
                webViewActivity.currentPage!!.setDownNoti(true)
                webViewActivity.barsState.bottomDownIcon!!.showDot(true, true)
                webViewActivity.barsState.bottomDownIcon!!.setVisibility(View.VISIBLE)
            }
            if (ZoneSettings.downPos.value == 3 && webViewActivity.imageButton != null) {
                val videoDownCount = webViewActivity.videoDownCount
                if (videoDownCount < 2) {
                    webViewActivity.videoDownCount = videoDownCount + 1
                    found = true
                }
                val webNestView = webViewActivity.currentPage
                if (webNestView!!.downNotiHidden) {
                    webNestView.setDownNoti(true)
                    webViewActivity.imageButton!!.setNoti(true)
                    found = true
                }
                if (found) {
                    val updatePending = webViewActivity.isBarUpdatePending()
                    val webDownView = webViewActivity.imageButton
                    if (updatePending) {
                        webDownView!!.showNow()
                    } else {
                        webDownView!!.pulse()
                    }
                }
            }
            webViewActivity.setDownIconAlert(true)
        }
    }
}

class WvaShowIntentSnackbar(@JvmField val wa: WebViewActivity, @JvmField val pendingIntent: android.content.Intent? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val intent = this@WvaShowIntentSnackbar.pendingIntent
        if (webViewActivity.isAnyDialogOpen() || intent == null) {
            return
        }
        val snackItem = SnackItem()
        snackItem.intent = intent
        webViewActivity.updateUpButton(false)
        webViewActivity.closeAllSnackbars()
        val mySnackbar = MySnackbar(webViewActivity)
        webViewActivity.dialogsState.appBlockSnackbar = mySnackbar
        mySnackbar.setAppBarView(webViewActivity.barScroll)
        webViewActivity.dialogsState.appBlockSnackbar!!.snackItem = snackItem
        webViewActivity.dialogsState.appBlockSnackbar!!.setActivity(webViewActivity)
        webViewActivity.dialogsState.appBlockSnackbar!!.showWithButton(
            webViewActivity.webCoord,
            AppR.string.app_block_noti,
            1,
            WvaShowIntentSnackbarListener(webViewActivity)
        )
    }
}

class WvaScrollToPendingText(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        if (webViewActivity.currentPage != null && !TextUtils.isEmpty(webViewActivity.dialogsState.pageUrl)) {
            val appBarLayout = webViewActivity.barScroll
            if (appBarLayout != null) {
                appBarLayout.setExpanded(true)
            }
            val webNestView = webViewActivity.currentPage
            val pageUrl = webViewActivity.dialogsState.pageUrl
            webNestView!!.removeSslView()
            webNestView.crashShown = true
            val webCrashView = webNestView.crashView
            if (webCrashView == null) {
                webNestView.crashUrlIn = pageUrl
                val handler = webNestView.uiHandler
                if (handler != null) {
                    handler.post(webNestView.RemoveCrashViewTask())
                }
            } else {
                webCrashView.crashUrl = pageUrl
                webNestView.crashView!!.applyPadding()
                webNestView.crashView!!.setVisibility(View.VISIBLE)
            }
            webViewActivity.updateBarBehavior()
        }
        webViewActivity.videoState.pageFrameBusy = false
    }
}

class WvaShowSetMsgDialog(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class ShowScriptInstallStep : Runnable {
        override fun run() {
            var lockType: Int = 0
            val webViewActivity = wa
            val dialogSetMsg: DialogSetMsg? = if (webViewActivity.isClosing) null else webViewActivity.dialogsState.scriptInstallDialog
            if (webViewActivity.isClosing || dialogSetMsg != null) {
                return
            }
            val scriptInstallUrl = webViewActivity.dialogsState.scriptInstallUrl
            val scriptAlreadyInstalled = webViewActivity.barsState.scriptAlreadyInstalled
            val scriptName = scriptNameFor(webViewActivity.dialogsState.appContext, scriptInstallUrl)
            val sb = StringBuilder("< ")
            sb.append(webViewActivity.getString(AppR.string.user_script_title))
            sb.append(" >\n")
            if (!TextUtils.isEmpty(scriptName)) {
                sb.append(scriptName)
                sb.append("\n")
            }
            sb.append(webViewActivity.getString(AppR.string.install_confirm))
            if (scriptAlreadyInstalled) {
                sb.append("\n")
                sb.append(webViewActivity.getString(AppR.string.already_install))
                lockType = AppR.string.update
            } else {
                lockType = AppR.string.install
            }
            val scriptInstallDialog = DialogSetMsg(
                webViewActivity,
                sb.toString(),
                lockType,
                WvaShowUpdateScriptDialog(webViewActivity)
            )
            webViewActivity.dialogsState.scriptInstallDialog = scriptInstallDialog
            scriptInstallDialog.setOnDismissListener(ScriptInstallDialogDismissed(webViewActivity))
        }
    }

    override fun run() {
        val webViewActivity = wa
        val context = webViewActivity.dialogsState.appContext
        val scriptInstallUrl = webViewActivity.dialogsState.scriptInstallUrl
        val dbBookScript = DbBookScript.cached
        var scriptOn = false
        if (!TextUtils.isEmpty(scriptInstallUrl)) {
            if (DbUtil.existsWhere(
                    DbBookScript.get(context).getWritableDatabase(),
                    "DbBookScript_table",
                    null,
                    "_downurl=?",
                    arrayOf<String?>(scriptInstallUrl!!)
                ) == 1
            ) {
                scriptOn = true
            }
        }
        webViewActivity.barsState.scriptAlreadyInstalled = scriptOn
        val myWebCoord = webViewActivity.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(ShowScriptInstallStep())
    }
}

class WvaEditorFocusSequence(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class FocusEditorStep : Runnable {

        internal inner class ShowKeyboardStep : Runnable {

            internal inner class ClearEditBusyStep : Runnable {
                override fun run() {
                    wa.pageState.addressEditBusy = false
                }
            }

            override fun run() {
                val wvaEditorFocusSequence = this@WvaEditorFocusSequence
                val webViewActivity = wa
                val myEditAuto = webViewActivity.editAuto
                if (myEditAuto == null) {
                    return
                }
                openKeyboard(webViewActivity.dialogsState.appContext, myEditAuto)
                wa.editAuto!!.postDelayed(ClearEditBusyStep(), 600L)
            }
        }

        override fun run() {
            val webViewActivity = wa
            val myEditAuto = webViewActivity.editAuto
            if (myEditAuto == null) {
                return
            }
            myEditAuto.selectAll()
            webViewActivity.editAuto!!.post(ShowKeyboardStep())
        }
    }

    override fun run() {
        val webViewActivity = wa
        val myEditAuto = webViewActivity.editAuto
        if (myEditAuto == null) {
            return
        }
        myEditAuto.requestFocus()
        webViewActivity.editAuto!!.post(FocusEditorStep())
    }
}

class WvaEditorPrefixPopup(@JvmField val wa: WebViewActivity) : MyPopupListener {
    override fun onClose() {
        wa.closeAddressPrefixMenu()
    }

    override fun onItem(view: View?, id: Int): Boolean {
        val myEditAuto = wa.editAuto
        if (myEditAuto == null) {
            return true
        }
        if (id == 0) {
            myEditAuto.setTextAt(8, "https://")
            return true
        }
        myEditAuto.setTextAt(4, "www.")
        return true
    }
}

class WvaRestoreTabFromUrl(@JvmField val wa: WebViewActivity, @JvmField val url: String? = null) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val url = this@WvaRestoreTabFromUrl.url
        val secretMode = !SyncSettings.secretMode.value
        val arrayListC: ArrayList<Any?>? = loadTabs(webViewActivity.dialogsState.appContext, secretMode) as ArrayList<Any?>?
        val size = if (arrayListC != null) arrayListC.size else 0
        val webTabItem = WebTabItem()
        webTabItem.uid = DbBookTab.newUid((arrayListC as ArrayList<WebTabItem?>?))
        webTabItem.parentId = DbBookTab.prevUid(size, (arrayListC as ArrayList<WebTabItem?>?))
        webTabItem.position = size
        webTabItem.tabUrl = url
        webTabItem.tabTitle = WebViewActivity.tabTitleFor(webViewActivity.dialogsState.appContext, webTabItem)
        webTabItem.deskMode = BrowserSettings.deskLock.value
        insertTab(webViewActivity.dialogsState.appContext, webTabItem, secretMode)
        if (SyncSettings.secretMode.value) {
            SyncSettings.editLocal { set(SyncSettings.normalIndex, size) }
        } else {
            PrefSync.currentSecretIndex = size
        }
        webViewActivity.switchSecretMode(secretMode)
    }
}

class WvaBarOffsetListener(@JvmField val wa: WebViewActivity) : BarScroll.OffsetListener {
    // was AppBarLayout.BaseOnOffsetChangedListener
    override fun onOffsetChanged(offset: Int) {
        val webViewActivity = wa
        webViewActivity.pageState.barOffset = offset
        val appBarLayout = webViewActivity.barScroll
        if (appBarLayout != null && appBarLayout.getHeight() > 0) {
            // float division
            val height = (offset.toFloat() / appBarLayout.getHeight()) + 1.0f
            val webUpView = webViewActivity.upButton
            if (webUpView != null) {
                webUpView.setAlpha(height)
            }
            val view = webViewActivity.transFloatButton
            if (view != null) {
                view.setAlpha(height)
            }
        }
    }
}

class WvaTopBarListener(@JvmField val wa: WebViewActivity) : BarListener {
    override fun onItemClick(id: Int, view: View?, isLong: Boolean) {
        val webViewActivity = wa
        webViewActivity.onBarItemClick(webViewActivity.barsState.topBarView, view, id)
    }
}

class WvaBottomBarListener(@JvmField val wa: WebViewActivity) : BarListener {
    override fun onItemClick(id: Int, view: View?, isLong: Boolean) {
        val webViewActivity = wa
        webViewActivity.onBarItemClick(webViewActivity.barsState.bottomBarView, view, id)
    }
}

class WvaEditorClearClick(@JvmField val wa: WebViewActivity) : View.OnClickListener {
    override fun onClick(view: View?) {
        val webViewActivity = wa
        if (webViewActivity.editAuto == null || webViewActivity.isAnyDialogOpen()) {
            return
        }
        webViewActivity.editAuto!!.setTextAt(-1, null)
        postRequestLayout(webViewActivity.editAuto)
    }
}

class WvaEditorAutoListener(@JvmField val wa: WebViewActivity) : EditAutoListener {
    override fun onText(input: String?): Boolean {
        val webViewActivity = wa
        val myEditAuto = webViewActivity.editAuto
        if (myEditAuto == null || !webViewActivity.quickState.editingAddress) {
            return false
        }
        val completed = if (ZtwoSettings.autoComp.value) myEditAuto.updateComplete(input) else false
        // fetch what Enter would open once the typing pauses (with the inline completion, as Enter takes it)
        Preload.onTyping(myEditAuto, { webViewActivity.quickState.editingAddress && webViewActivity.editAuto === myEditAuto }) { myEditAuto.autoText }
        webViewActivity.setAddressEditMode(TextUtils.isEmpty(input))
        webViewActivity.closeQuickSubView()
        return completed
    }
}

class WvaEditorAction(@JvmField val wa: WebViewActivity) : OnEditorActionListener {

    internal inner class EditorActionStep : Runnable {
        override fun run() {
            val webViewActivity = wa
            val myEditAuto = webViewActivity.editAuto
            if (myEditAuto == null) {
                return
            }
            webViewActivity.finishAddressEdit(myEditAuto.autoText)
        }
    }

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        val myEditAuto = wa.editAuto
        if (myEditAuto == null) {
            return true
        }
        myEditAuto.post(EditorActionStep())
        return true
    }
}

class WvaExpandBars(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val appBarLayout = wa.barScroll
        if (appBarLayout != null) {
            appBarLayout.setExpanded(true)
        }
    }
}
