@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon", "ClassName")

package com.mycompany.app.web

import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.appendAmpJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.db.book.DbBookTmem.Companion.get
import com.mycompany.app.db.book.DbBookTrans.Companion.get
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebViewActivity
import android.content.DialogInterface
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.wview.WebTtsView
import com.mycompany.app.wview.WebFltView.FltViewListener
import android.view.View
import java.lang.Exception

/*
 * The browser activity's speech callbacks and runnables (its former inner classes),
 * now top-level classes that get the activity as `wa`. Bodies unchanged except that activity members are reached
 * through `wa`.
 */

class WvaUseTtsDialogDismiss(@JvmField val wa: WebViewActivity) : DialogInterface.OnDismissListener {
    override fun onDismiss(dialogInterface: DialogInterface?) {
        val webViewActivity = wa
        val dialogSetUseTts = webViewActivity.dialogsState.setUseTtsDialog
        if (dialogSetUseTts != null) {
            dialogSetUseTts.dismiss()
            webViewActivity.dialogsState.setUseTtsDialog = null
        }
    }
}

class WvaTtsShutdown(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        try {
            if (webViewActivity.speechState.tts!!.isSpeaking()) {
                webViewActivity.speechState.tts!!.stop()
            }
            webViewActivity.speechState.tts!!.shutdown()
        } catch (unused: Exception) {
        }
        webViewActivity.speechState.tts = null
        webViewActivity.speechState.jsBridgeBusy = false
    }
}

class WvaTtsStop(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        try {
            if (webViewActivity.speechState.tts!!.isSpeaking()) {
                webViewActivity.speechState.tts!!.stop()
            }
        } catch (unused: Exception) {
        }
        webViewActivity.speechState.jsBridgeBusy = false
    }
}

class WvaShowTtsPanel(@JvmField val wa: WebViewActivity) : Runnable {

    internal inner class TtsButtonListener : FltViewListener {
        override fun onTap(view: View?, button: Int) {
            val webViewActivity = wa
            webViewActivity.stopTts()
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
        if (webViewActivity.speechState.ttsButton != null || webViewActivity.dialogsState.webBody == null) {
            return
        }
        try {
            val webTtsView = WebTtsView(webViewActivity.dialogsState.appContext ?: throw IllegalStateException("no context"))
            webViewActivity.speechState.ttsButton = webTtsView
            webTtsView.refreshColors()
            webViewActivity.speechState.ttsButton!!.setVisibility(View.GONE)
            webViewActivity.speechState.ttsButton!!.setTtsListener(TtsButtonListener())
            val myWebBody = webViewActivity.dialogsState.webBody
            val ttsButton = webViewActivity.speechState.ttsButton
            val width = MainApp.dp48
            wa.floatingState.overlays!!.addView(ttsButton, width, width)
        } catch (unused: Exception) {
        }
    }
}

class WvaRemoveTtsPanel(@JvmField val wa: WebViewActivity) : Runnable {
    override fun run() {
        val webViewActivity = wa
        val webTtsView = webViewActivity.speechState.ttsButton
        if (webTtsView != null) {
            webTtsView.release()
            val myWebBody = webViewActivity.dialogsState.webBody
            if (myWebBody != null) {
                wa.floatingState.overlays!!.removeView(webViewActivity.speechState.ttsButton)
            }
            webViewActivity.speechState.ttsButton = null
        }
    }
}

class WvaUpdateTtsPanel(@JvmField val webViewActivity: WebViewActivity,
                                       @JvmField val isShown: Boolean,
                                       @JvmField val animate: Boolean
) : Runnable {
    override fun run() {
        val webViewActivity = this.webViewActivity
        val webTtsView = webViewActivity.speechState.ttsButton
        if (webTtsView == null || webViewActivity.dialogsState.webBody == null) {
            return
        }
        if (this.isShown) {
            webTtsView.appear(this.animate)
        } else {
            webTtsView.fadeAway()
        }
    }
}
