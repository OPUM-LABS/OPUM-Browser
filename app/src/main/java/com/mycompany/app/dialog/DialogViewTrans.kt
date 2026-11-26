package com.mycompany.app.dialog

import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.mycompany.app.data.DataTrans
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.dialog.DialogTransLang.TransLangListener
import com.mycompany.app.dialog.DialogViewRead.TtsItem
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainTransText
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.evalConfirmJs
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.transLangFromCookie
import com.mycompany.app.main.MainUtil.checkTransClassJs
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.nativeJs14
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.parseTransResult
import com.mycompany.app.main.MainUtil.httpsUrl
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.hideDivsByClass
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefAlbum.Companion.saveTrans
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebReadTask
import com.mycompany.app.web.WebReadTask.ReadItem
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.web.WebTransControl.TransCtrlListener
import java.util.Locale
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.Byte
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.also
import kotlin.math.min

@Suppress("deprecation")
class DialogViewTrans(mainActivity: MainActivity?, transPick: String?, pageLang: String?, transPickLast: String?) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var speakBarState: Any? = null
    @JvmField
    var barClosed: Boolean = false
    @JvmField
    var rewindBtn: TransBtn? = null
    @JvmField
    var pauseBtn: TransBtn? = null
    @JvmField
    var stopBtn: TransBtn? = null
    @JvmField
    var currentBtn: TransBtn? = null
    @JvmField
    var searchBtn: TransBtn? = null
    @JvmField
    var forwardBtn: TransBtn? = null
    @JvmField
    var clickLocked: Boolean = false
    @JvmField
    var loadPending: Boolean = false
    @JvmField
    var urlPending: Boolean = false
    @JvmField
    var pendingUrl: String? = null
    @JvmField
    var transState: Byte = 0
    @JvmField
    var autoTrans: Boolean = false
    @JvmField
    var transLang: String? = null
    @JvmField
    var transText: String? = null
    @JvmField
    var detectedLang: String? = null
    @JvmField
    var baseUrl: String? = null
    @JvmField
    var transLangDialog: DialogTransLang? = null
    @JvmField
    var transDone: Boolean = false
    @JvmField
    var transTextView: MainTransText? = null
    @JvmField
    var hostActivity: MainActivity? = null
    @JvmField
    var transPickLast: String? = null
    @JvmField
    var appCtx: Context? = null
    @JvmField
    var transCodeLast: String? = null
    @JvmField
    var contentView: View? = null
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var sheetFrame: FrameLayout? = null
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var page: WebNestView? = null
    @JvmField
    var jsResultA: String? = null
    @JvmField
    var speakButton: TransBtn? = null
    @JvmField
    var jsResultB: String? = null
    @JvmField
    var logo: TransLogo? = null
    @JvmField
    var jsResultC: String? = null
    @JvmField
    var transFrame: FrameLayout? = null
    @JvmField
    var transControlIn: WebTransControl? = null
    @JvmField
    var transControl: WebTransControl? = null
    @JvmField
    var paragraphQueue: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?>? = null
    @JvmField
    var transPick: String? = null
    @JvmField
    var ttsMoveRunnable: Runnable? = null
    @JvmField
    var pageLang: String? = null
    @JvmField
    var transRunning: Boolean = false
    @JvmField
    var ttsEngine: TextToSpeech? = null
    @JvmField
    var ttsRateSet: Float = 0f
    @JvmField
    var ttsPitchSet: Float = 0f
    @JvmField
    var ttsLang: String? = null
    @JvmField
    var ttsLangSet: String? = null
    @JvmField
    var ttsParagraphs: ArrayList<Any?>? = null
    @JvmField
    var ttsIndex: Int = 0
    @JvmField
    var ttsState: Byte = 0
    @JvmField
    var speakMode: Boolean = false
    @JvmField
    var ttsStartPos: Int = 0
    @JvmField
    var ttsEndPos: Int = 0
    @JvmField
    var ttsRestart: Boolean = false
    @JvmField
    var ttsMoveIndex: Int = 0
    @JvmField
    var rangeStart: Int = 0
    @JvmField
    var rangeEnd: Int = 0
    @JvmField
    var ttsJump: Boolean = false
    @JvmField
    var pageJs: String? = null
    @JvmField
    var transItems: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?>? = null

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            val webNestView = dialogViewTrans.page
            if (webNestView == null) {
                return
            }
            dialogViewTrans.urlPending = false
            webNestView.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            val webNestView = dialogViewTrans.page
            if (webNestView == null) {
                return
            }
            dialogViewTrans.urlPending = true
            webNestView.addJavascriptInterface(dialogViewTrans.WebAppInterface(), "android")
        }
    }

    internal inner class TransScriptTask : Runnable {

        internal inner class EvalTransScriptStep : Runnable {

            internal inner class EvalJsNowTask : Runnable {

                override fun run() {
                    val pendingUrl: String?
                    val transScriptTask = this@TransScriptTask
                    val dialogViewTrans = this@DialogViewTrans
                    if (dialogViewTrans.page != null) {
                        val sbK = transScriptJs()
                        if (sbK != null) {
                            val classJs = classSelectorJs(dialogViewTrans.detectedLang)
                            if (TextUtils.isEmpty(classJs)) {
                                pendingUrl = null
                            } else {
                                sbK.insert(0, classJs)
                                pendingUrl = sbK.toString()
                            }
                        } else {
                            pendingUrl = null
                        }
                        dialogViewTrans.pendingUrl = pendingUrl
                        val owner = this@DialogViewTrans
                        if (!owner.loadPending || TextUtils.isEmpty(owner.pendingUrl)) {
                            return
                        }
                        val pendingUrlNow = owner.pendingUrl
                        owner.loadPending = false
                        owner.pendingUrl = null
                        evalJsNow(owner.page, pendingUrlNow, true)
                    }
                }
            }

            override fun run() {
                val dialogViewTrans = this@DialogViewTrans
                val jsResultA = dialogViewTrans.jsResultA
                dialogViewTrans.jsResultA = null
                val webNestView: WebNestView? = if (TextUtils.isEmpty(jsResultA)) null else dialogViewTrans.page
                if (TextUtils.isEmpty(jsResultA) || webNestView == null) {
                    return
                }
                MainUtil.loadHtml(webNestView, dialogViewTrans.baseUrl, jsResultA)
                dialogViewTrans.page!!.setVisibility(View.VISIBLE)
                dialogViewTrans.runInBackground(EvalJsNowTask())
                val ttsReady = dialogViewTrans.initTts()
                if (dialogViewTrans.ttsEngine != null) {
                    var ttsRateSet = TtsSettings.ttsRate.value
                    var ttsPitchSet = 0.5f
                    if (ttsRateSet < 0.5f) {
                        TtsSettings.editLocal { set(TtsSettings.ttsRate, 0.5f) }
                        ttsRateSet = 0.5f
                    } else if (ttsRateSet > 3.0f) {
                        TtsSettings.editLocal { set(TtsSettings.ttsRate, 3.0f) }
                        ttsRateSet = 3.0f
                    }
                    val ttsPitch = TtsSettings.ttsPitch.value
                    if (ttsPitch < 0.5f) {
                        TtsSettings.editLocal { set(TtsSettings.ttsPitch, 0.5f) }
                    } else {
                        ttsPitchSet = 2.0f
                        if (ttsPitch > 2.0f) {
                            TtsSettings.editLocal { set(TtsSettings.ttsPitch, 2.0f) }
                        } else {
                            ttsPitchSet = ttsPitch
                        }
                    }
                    try {
                        if (ttsReady) {
                            dialogViewTrans.ttsRateSet = ttsRateSet
                            dialogViewTrans.ttsPitchSet = ttsPitchSet
                            if ((ttsRateSet).compareTo(1.0f) != 0) {
                                dialogViewTrans.ttsEngine!!.setSpeechRate(TtsSettings.ttsRate.value)
                            }
                            if ((TtsSettings.ttsPitch.value).compareTo(1.0f) != 0) {
                                dialogViewTrans.ttsEngine!!.setPitch(TtsSettings.ttsPitch.value)
                                return
                            }
                            return
                        }
                        if ((ttsRateSet).compareTo(dialogViewTrans.ttsRateSet) != 0) {
                            val ttsRate = TtsSettings.ttsRate.value
                            dialogViewTrans.ttsRateSet = ttsRate
                            dialogViewTrans.ttsEngine!!.setSpeechRate(ttsRate)
                        }
                        if ((TtsSettings.ttsPitch.value).compareTo(dialogViewTrans.ttsPitchSet) != 0) {
                            val newTtsPitchSet = TtsSettings.ttsPitch.value
                            dialogViewTrans.ttsPitchSet = newTtsPitchSet
                            dialogViewTrans.ttsEngine!!.setPitch(newTtsPitchSet)
                        }
                    } catch (unused: Exception) {
                    }
                }
            }
        }

        override fun run() {
            var picked: String?
            val dialogViewTrans = this@DialogViewTrans
            val transPick = dialogViewTrans.transPick
            if (TextUtils.isEmpty(transPick)) {
                picked = null
            } else {
                val plainPick = transPick!!.replace(" ", " ")
                if (TextUtils.isEmpty(plainPick)) {
                    picked = null
                } else {
                    picked = plainPick.trim { it <= ' ' }
                }
            }
            if (TextUtils.isEmpty(picked)) {
                picked = ""
            }
            val sbI = WebReadTask.htmlHead(true, false)
            val arrayList: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?> = ArrayList()
            val lines: Array<String?> =
                picked!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (lines.size <= 1) {
                val readItem = ReadItem()
                readItem.index = 0
                readItem.articleTitle = picked
                arrayList.add(readItem)
                sbI.append("<p id='")
                sbI.append(readItem.index)
                sbI.append("'>")
                sbI.append(readItem.articleTitle)
                sbI.append("</p>")
            } else {
                var index = 0
                for (rawLine in lines) {
                    if (!TextUtils.isEmpty(rawLine)) {
                        val lineText = rawLine!!.trim { it <= ' ' }
                        if (!TextUtils.isEmpty(lineText)) {
                            val readItem = ReadItem()
                            readItem.index = index
                            readItem.articleTitle = lineText
                            arrayList.add(readItem)
                            sbI.append("<p id='")
                            sbI.append(readItem.index)
                            sbI.append("'>")
                            sbI.append(readItem.articleTitle)
                            sbI.append("</p>")
                            index++
                        }
                    }
                }
            }
            dialogViewTrans.transItems = arrayList
            sbI.append("</body></html>")
            dialogViewTrans.jsResultA = sbI.toString()
            dialogViewTrans.baseUrl = httpsUrl("soul_trans_", null)
            val webNestView = dialogViewTrans.page
            if (webNestView == null) {
                return
            }
            webNestView.post(EvalTransScriptStep())
        }
    }

    internal inner class TransLangFromCookieCallback : ValueCallback<String> {
        override fun onReceiveValue(cookies: String?) {
            val cookieLang = transLangFromCookie(cookies)
            if (TextUtils.isEmpty(cookieLang) || cookieLang == AlbumSettings.transCode.value) {
                return
            }
            AlbumSettings.edit(this@DialogViewTrans.appCtx) { set(AlbumSettings.transCode, cookieLang) }
        }
    }

    internal inner class ParseTransResultTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            val jsResultB = dialogViewTrans.jsResultB
            dialogViewTrans.jsResultB = null
            if (dialogViewTrans.page == null) {
                return
            }
            parseTransResult(dialogViewTrans.appCtx, jsResultB)
        }
    }

    internal inner class LangPrefixTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            val jsResultC = dialogViewTrans.jsResultC
            dialogViewTrans.jsResultC = null
            if (dialogViewTrans.page == null || !TextUtils.isEmpty(dialogViewTrans.detectedLang)) {
                return
            }
            val prefix = langPrefix(jsResultC)
            dialogViewTrans.detectedLang = prefix
            if (prefix.length > 2) {
                hideDivsByClass(dialogViewTrans.page, prefix)
            }
        }
    }

    internal inner class EvalLongPressJsTask(@JvmField val outer: DialogViewTrans,
                                          @JvmField val script: String?
    ) : Runnable {
        override fun run() {
            evalLongPressJs(this.outer.page, this.script)
        }
    }

    internal inner class ShowTransControlTask : Runnable {

        internal inner class ShowTransControlStep : Runnable {
            override fun run() {
                val dialogViewTrans = this@DialogViewTrans
                val webTransControl = dialogViewTrans.transControlIn
                dialogViewTrans.transControlIn = null
                val transControl = dialogViewTrans.transControl
                if (transControl == null && dialogViewTrans.transFrame != null && webTransControl != null) {
                    dialogViewTrans.transControl = webTransControl
                    try {
                        val transState = dialogViewTrans.transState
                        webTransControl.showState(dialogViewTrans.transLang, dialogViewTrans.autoTrans, transState.toInt())
                        dialogViewTrans.transControl!!.setListener(dialogViewTrans.TransControlActions())
                        dialogViewTrans.transFrame!!.addView(dialogViewTrans.transControl, -1, -2)
                    } catch (unused: Exception) {
                    }
                }
                if (dialogViewTrans.page != null) {
                    dialogViewTrans.runInBackground(dialogViewTrans.TransScriptTask())
                }
            }
        }

        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            val webTransControl = dialogViewTrans.transControlIn
            if (webTransControl != null) {
                webTransControl.applyMode(2)
            }
            val handler = dialogViewTrans.mainHandler
            handler.post(ShowTransControlStep())
        }
    }

    internal inner class TransLangPicked : TransLangListener {
        override fun onLanguage(lang: String?) {
            val dialogViewTrans = this@DialogViewTrans
            val dialogTransLang = dialogViewTrans.transLangDialog
            if (dialogTransLang != null) {
                dialogTransLang.dismiss()
                dialogViewTrans.transLangDialog = null
            }
            if (dialogViewTrans.transState.toInt() != 3 || TextUtils.isEmpty(lang)) {
                return
            }
            dialogViewTrans.runInBackground(EvalLongPressJsTask(dialogViewTrans, lang))
        }
    }

    internal inner class TransLangDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewTrans = this@DialogViewTrans
            val dialogTransLang = dialogViewTrans.transLangDialog
            if (dialogTransLang != null) {
                dialogTransLang.dismiss()
                dialogViewTrans.transLangDialog = null
            }
        }
    }

    internal inner class ResetTtsStep : OnInitListener {
        override fun onInit(status: Int) {
            val dialogViewTrans = this@DialogViewTrans
            if (status == -1) {
                dialogViewTrans.resetTts()
            } else if (status == 0) {
                dialogViewTrans.transRunning = false
            }
        }
    }

    internal inner class StopTtsStep : UtteranceProgressListener() {

        internal inner class StopTtsLaterStep : Runnable {
            override fun run() {
                val dialogViewTrans = this@DialogViewTrans
                if (dialogViewTrans.speakBarState != null) {
                    return
                }
                dialogViewTrans.stopTts()
            }
        }

        override fun onDone(utteranceId: String?) {
            com.mycompany.app.main.SpeechGuard.ended(utteranceId)
            val dialogViewTrans = this@DialogViewTrans
            val arrayList = dialogViewTrans.ttsParagraphs
            if (arrayList == null || dialogViewTrans.ttsIndex >= arrayList.size - 1) {
                dialogViewTrans.setTtsState(0, false)
            } else {
                dialogViewTrans.speak(false)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            com.mycompany.app.main.SpeechGuard.ended(utteranceId)
            android.os.Handler(android.os.Looper.getMainLooper()).post { this@DialogViewTrans.ttsFailed() }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            @Suppress("DEPRECATION") onError(utteranceId)
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            com.mycompany.app.main.SpeechGuard.ended(utteranceId)
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            val dialogViewTrans = this@DialogViewTrans
            val ttsStartPos = dialogViewTrans.ttsStartPos
            val rangeStart = ttsStartPos + start
            dialogViewTrans.rangeStart = rangeStart
            val rangeEnd = ttsStartPos + end
            dialogViewTrans.rangeEnd = rangeEnd
            dialogViewTrans.ttsEndPos = start
            if (dialogViewTrans.ttsJump) {
                return
            }
            dialogViewTrans.highlightRange(rangeStart, rangeEnd, dialogViewTrans.ttsIndex, false)
        }

        override fun onStart(utteranceId: String?) {
            com.mycompany.app.main.SpeechGuard.started(utteranceId)
            val dialogViewTrans = this@DialogViewTrans
            val ttsState = dialogViewTrans.ttsState
            if (ttsState.toInt() == 1) {
                dialogViewTrans.setTtsState(1, false)
                return
            }
            if (ttsState.toInt() == 2) {
                dialogViewTrans.pauseTts(true)
            } else {
                val handler: Handler? = if (dialogViewTrans.speakBarState != null) null else dialogViewTrans.mainHandler
                if (dialogViewTrans.speakBarState != null || handler == null) {
                    return
                }
                handler.postDelayed(StopTtsLaterStep(), 100L)
            }
        }
    }

    internal inner class InitTtsTask : Runnable {

        /** Read aloud in the translation view.*/
        override fun run() {
            val view = this@DialogViewTrans
            val paragraphs = view.paragraphQueue
            view.paragraphQueue = null
            view.initTts()
            if (view.ttsEngine == null) {
                return
            }
            view.ttsState = 1
            view.speakMode = true
            try {
                if (paragraphs != null) {
                    view.ttsParagraphs = DialogViewRead.Companion.ttsChunks(paragraphs, view.autoTrans)
                    view.ttsIndex = 0
                } else {
                    view.ttsIndex++
                }
                val current = if (view.ttsParagraphs != null && view.ttsIndex >= 0 && view.ttsIndex < view.ttsParagraphs!!.size)
                    view.ttsParagraphs!!.get(view.ttsIndex) as TtsItem?
                else
                    null
                val speech = if (current != null) current.speech else null
                if (TextUtils.isEmpty(speech)) {
                    view.ttsState = 0
                    view.speakMode = false
                    view.ttsParagraphs = null
                    view.ttsIndex = 0
                    view.setTtsState(0, false)
                    return
                }
                if (view.ttsEngine!!.isSpeaking()) {
                    view.ttsEngine!!.stop()
                }
                // a dead engine is made again once; when it still does not speak, the view says so (2026-11-26)
                if (view.ttsState.toInt() == 1 && com.mycompany.app.main.SpeechGuard.speak(
                        view.ttsEngine, speech!!, "0",
                        prepare = { view.applyTtsLang() },
                        recreate = { done -> view.recreateTts(done) },
                        onFailed = { view.ttsFailed() },
                    )
                ) {
                    view.setTtsState(1, true)
                }
            } catch (unused: Exception) {
                view.ttsState = 0
                view.speakMode = false
            }
        }
    }

    internal inner class ClearBarTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            try {
                if (!dialogViewTrans.speakMode) {
                    dialogViewTrans.clearBar()
                }
                val myButtonImage = dialogViewTrans.speakButton
                if (myButtonImage != null) {
                    if (!dialogViewTrans.speakMode && dialogViewTrans.autoTrans) {
                        myButtonImage.setVisibility(0)
                    }
                    val currentBtn = dialogViewTrans.currentBtn
                    if (currentBtn != null) {
                        currentBtn.setLoad(false)
                        dialogViewTrans.currentBtn = null
                    }
                }
            } catch (unused: Exception) {
            }
        }
    }

    internal inner class SetDeskResult : SetDeskListener {
        override fun onChanged(deskMode: Boolean) {
            val dialogViewTrans = this@DialogViewTrans
            val mainTransText = dialogViewTrans.transTextView
            if (mainTransText != null) {
                mainTransText.release()
                dialogViewTrans.transTextView = null
            }
            if (dialogViewTrans.page != null) {
                dialogViewTrans.transDone = true
                dialogViewTrans.speak(true)
                val myButtonImage = dialogViewTrans.currentBtn
                if (myButtonImage != null) {
                    myButtonImage.setLoad(false)
                    dialogViewTrans.currentBtn = null
                }
            }
        }
    }

    internal inner class InjectPageJsTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.page != null) {
                val pageScript = nativeJs14()
                dialogViewTrans.pageJs = pageScript
                if (TextUtils.isEmpty(pageScript)) {
                    return
                }
                evalJsNow(dialogViewTrans.page, dialogViewTrans.pageJs, true)
            }
        }
    }

    internal inner class ParseFloatOrTask(@JvmField val outer: DialogViewTrans,
                                          @JvmField val range: String?
    ) : Runnable {
        override fun run() {
            val range = this.range
            val rangeParts: Array<String?>? = if (TextUtils.isEmpty(range)) null else range!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (TextUtils.isEmpty(range) || rangeParts == null || rangeParts.size != 2) {
                return
            }
            val dialogViewTrans = this.outer
            val partTop =
                Math.round(MainUtil.dpToPx(dialogViewTrans.appCtx, MainUtil.parseFloatOr(rangeParts[0], -1.0f)))
            val partBottom =
                Math.round(MainUtil.dpToPx(dialogViewTrans.appCtx, MainUtil.parseFloatOr(rangeParts[1], -1.0f)))
            val webNestView: WebNestView? = if (partTop == -1 || partBottom == -1) null else dialogViewTrans.page
            if (partTop == -1 || partBottom == -1 || webNestView == null) {
                return
            }
            val scrollY = webNestView.getScrollY() - MainApp.dp16
            val dp4Px = MainApp.dp4
            val index = (scrollY - dp4Px) + partTop
            val bottom = dp4Px + scrollY + partBottom
            if (index < scrollY) {
                dialogViewTrans.page!!.scrollTo(0, index)
                return
            }
            val scrollDownTo = min(index, bottom - (dialogViewTrans.sheetFrame!!.getHeight() - (MainApp.dp8 * 10)))
            if (scrollDownTo > scrollY) {
                dialogViewTrans.page!!.scrollTo(0, scrollDownTo)
            }
        }
    }

    internal inner class ShowBarTask : Runnable {
        override fun run() {
            this@DialogViewTrans.showBar()
        }
    }

    internal inner class InitTtsClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.speakMode) {
                return
            }
            if (dialogViewTrans.isBusy(false)) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
                return
            }
            val ttsState = dialogViewTrans.ttsState
            if (ttsState.toInt() == 2) {
                dialogViewTrans.initTts()
                if (dialogViewTrans.ttsEngine != null) {
                    dialogViewTrans.ttsState = 1.toByte()
                    dialogViewTrans.speakMode = true
                    try {
                        val ttsIndex = dialogViewTrans.ttsIndex
                        val arrayList = dialogViewTrans.ttsParagraphs
                        val ttsItem =
                            if (arrayList == null || ttsIndex < 0 || ttsIndex >= arrayList.size) null else dialogViewTrans.ttsParagraphs!!.get(
                                ttsIndex
                            ) as TtsItem?
                        val speech = if (ttsItem == null) null else ttsItem.speech
                        if (TextUtils.isEmpty(speech)) {
                            dialogViewTrans.ttsState = 0.toByte()
                            dialogViewTrans.speakMode = false
                            dialogViewTrans.ttsParagraphs = null
                            dialogViewTrans.ttsIndex = 0
                            dialogViewTrans.setTtsState(0, false)
                        } else {
                            val ttsStartPos = dialogViewTrans.ttsStartPos + dialogViewTrans.ttsEndPos
                            dialogViewTrans.ttsStartPos = ttsStartPos
                            val remaining = speech!!.substring(ttsStartPos)
                            if (TextUtils.isEmpty(remaining)) {
                                dialogViewTrans.ttsState = 0.toByte()
                                dialogViewTrans.speakMode = false
                                dialogViewTrans.ttsParagraphs = null
                                dialogViewTrans.ttsIndex = 0
                                dialogViewTrans.setTtsState(0, false)
                            } else {
                                if (dialogViewTrans.ttsEngine!!.isSpeaking()) {
                                    dialogViewTrans.ttsEngine!!.stop()
                                }
                                if (com.mycompany.app.main.SpeechGuard.speak(
                                        dialogViewTrans.ttsEngine, remaining, "0",
                                        prepare = { dialogViewTrans.applyTtsLang() },
                                        recreate = { done -> dialogViewTrans.recreateTts(done) },
                                        onFailed = { dialogViewTrans.ttsFailed() },
                                    )
                                ) {
                                    dialogViewTrans.setTtsState(1, true)
                                }
                            }
                        }
                    } catch (unused: Exception) {
                        dialogViewTrans.ttsState = 0.toByte()
                        dialogViewTrans.speakMode = false
                    }
                }
            } else if (ttsState.toInt() == 0) {
                if (!dialogViewTrans.autoTrans || dialogViewTrans.transDone) {
                    dialogViewTrans.speak(true)
                } else if (dialogViewTrans.sheetFrame != null) {
                    if (dialogViewTrans.transTextView != null) {
                        showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
                    } else {
                        dialogViewTrans.speakMode = true
                        val myButtonImage = dialogViewTrans.currentBtn
                        if (myButtonImage != null) {
                            myButtonImage.setLoading(if (AppState.darkUi) -328966 else -16777216, true)
                        }
                        dialogViewTrans.transTextView = MainTransText(
                            dialogViewTrans.hostActivity,
                            dialogViewTrans.appCtx,
                            dialogViewTrans.sheetFrame,
                            dialogViewTrans.transItems,
                            dpToPx(dialogViewTrans.appCtx, 240.0f).toInt(),
                            dialogViewTrans.baseUrl,
                            dialogViewTrans.SetDeskResult()
                        )
                    }
                }
            }
            dialogViewTrans.ttsRestart = true
            if (dialogViewTrans.hostActivity == null || dialogViewTrans.contentView == null || dialogViewTrans.transLangDialog != null || dialogViewTrans.barClosed) {
                return
            }
            dialogViewTrans.barClosed = true
            dialogViewTrans.clearBar()
            val myDialogLinear = dialogViewTrans.contentView
            if (myDialogLinear != null) {
                myDialogLinear.post(dialogViewTrans.ShowBarTask())
            }
        }
    }

    internal inner class LockClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.lockClick()) {
                return
            }
            if (dialogViewTrans.isBusy(true)) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
            } else {
                dialogViewTrans.ttsMove(1)
            }
        }
    }

    internal inner class TtsBackClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.lockClick()) {
                return
            }
            if (dialogViewTrans.isBusy(true)) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
            } else {
                dialogViewTrans.ttsMove(0)
            }
        }
    }

    internal inner class TtsForwardClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.lockClick()) {
                return
            }
            if (dialogViewTrans.isBusy(true)) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
            } else {
                dialogViewTrans.ttsMove(2)
            }
        }
    }

    internal inner class PauseBtnClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.lockClick()) {
                return
            }
            if (dialogViewTrans.isBusy(true)) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
                return
            }
            dialogViewTrans.speakMode = false
            dialogViewTrans.ttsMoveIndex = -1234
            dialogViewTrans.ttsJump = false
            if (dialogViewTrans.ttsState.toInt() == 0) {
                dialogViewTrans.stopTts()
            } else {
                dialogViewTrans.pauseTts(true)
            }
            dialogViewTrans.clearBar()
        }
    }

    internal inner class StopBtnClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.lockClick()) {
                return
            }
            if (dialogViewTrans.isBusy(true)) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
                return
            }
            dialogViewTrans.speakMode = false
            dialogViewTrans.ttsMoveIndex = -1234
            dialogViewTrans.ttsJump = false
            dialogViewTrans.stopTts()
            dialogViewTrans.clearBar()
        }
    }

    internal inner class UnlockClickTask : Runnable {
        override fun run() {
            this@DialogViewTrans.clickLocked = false
        }
    }

    internal inner class HighlightRangeTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            val ttsIndex = if (!dialogViewTrans.speakMode) 0 else dialogViewTrans.ttsMoveIndex
            if (!dialogViewTrans.speakMode || ttsIndex == -1234) {
                return
            }
            dialogViewTrans.ttsMoveIndex = -1234
            dialogViewTrans.ttsJump = false
            if (dialogViewTrans.ttsState.toInt() != 2) {
                dialogViewTrans.highlightRange(dialogViewTrans.rangeStart, dialogViewTrans.rangeEnd, dialogViewTrans.ttsIndex, false)
            } else {
                dialogViewTrans.ttsIndex = ttsIndex - 1
                dialogViewTrans.speak(false)
            }
        }
    }

    internal inner class SetNoAutofillTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.sheetFrame != null) {
                val webNestView = WebNestView(dialogViewTrans.hostActivity)
                dialogViewTrans.page = webNestView
                webNestView.setNoAutofill(true)
                val darkUi = AppState.darkUi
                val page = dialogViewTrans.page
                if (darkUi) {
                    page!!.setBackgroundColor(-14606047)
                } else {
                    page!!.setBackgroundColor(-1)
                }
                dialogViewTrans.page!!.setVisibility(View.GONE)
                dialogViewTrans.page!!.setOnLongClickListener(dialogViewTrans.PageLongClick())
                dialogViewTrans.sheetFrame!!.addView(dialogViewTrans.page, 0, ViewGroup.LayoutParams(-1, -2))
                val handler = dialogViewTrans.mainHandler
                handler.post(dialogViewTrans.SetupTransControlTask())
            }
        }
    }

    internal inner class PageLongClick : OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            return this@DialogViewTrans.speakMode
        }
    }

    internal inner class SetupTransControlTask : Runnable {

        internal inner class SetupTransControlStep : Runnable {

            internal inner class PreparePageStep : Runnable {

                internal inner class AddTransControlStep : Runnable {
                    override fun run() {
                        val dialogViewTrans = this@DialogViewTrans
                        if (dialogViewTrans.page == null || dialogViewTrans.transControl != null || dialogViewTrans.transFrame == null || dialogViewTrans.transControlIn != null) {
                            return
                        }
                        dialogViewTrans.transControlIn = WebTransControl(dialogViewTrans.hostActivity!!)
                        val handler = dialogViewTrans.mainHandler
                        handler.post(dialogViewTrans.ShowTransControlTask())
                    }
                }

                override fun run() {
                    val setupTransControlTask = this@SetupTransControlTask
                    val dialogViewTrans = this@DialogViewTrans
                    val webNestView = dialogViewTrans.page
                    if (webNestView != null) {
                        dialogViewTrans.urlPending = true
                        webNestView.addJavascriptInterface(
                            dialogViewTrans.WebAppInterface(),
                            "android"
                        )
                        val handler = this@DialogViewTrans.mainHandler
                        handler.post(AddTransControlStep())
                    }
                }
            }

            override fun run() {
                val dialogViewTrans = this@DialogViewTrans
                val webNestView = dialogViewTrans.page
                if (webNestView != null) {
                    webNestView.setWebViewClient(dialogViewTrans.LocalWebViewClient())
                    val handler = dialogViewTrans.mainHandler
                    handler.post(PreparePageStep())
                }
            }
        }

        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            val webNestView = dialogViewTrans.page
            if (webNestView != null) {
                val settings = webNestView.getSettings()
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(false)
                settings.setJavaScriptEnabled(true)
                webNestView.setOverScrollMode(2)
                webNestView.setVerticalScrollBarEnabled(false)
                webNestView.setHorizontalScrollBarEnabled(false)
                val handler = dialogViewTrans.mainHandler
                handler.post(SetupTransControlStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val dialogViewTrans = this@DialogViewTrans
            MainUtil.clearWebCache(dialogViewTrans.page)
            dialogViewTrans.finishHandled = false
        }
    }

    internal inner class DismissTask : Runnable {
        override fun run() {
            this@DialogViewTrans.dismiss()
        }
    }

    private inner class WebAppInterface {

        inner class TransStateStep : Runnable {
            override fun run() {
                val dialogViewTrans = this@DialogViewTrans
                val webTransControl = dialogViewTrans.transControl
                if (webTransControl != null) {
                    webTransControl.showState(
                        dialogViewTrans.transLang,
                        dialogViewTrans.autoTrans,
                        dialogViewTrans.transState.toInt()
                    )
                }
                dialogViewTrans.stopTts()
                dialogViewTrans.clearBar()
                if (dialogViewTrans.transState.toInt() != 1) {
                    val transText = dialogViewTrans.transText
                    dialogViewTrans.transText = null
                    if (TextUtils.isEmpty(transText)) {
                        val webNestView: WebNestView? = if (!TextUtils.isEmpty(AlbumSettings.transPick.value)) dialogViewTrans.page else null
                        if (!TextUtils.isEmpty(AlbumSettings.transPick.value) && webNestView != null) {
                            webNestView.evaluateJavascript(
                                "document.cookie",
                                dialogViewTrans.TransLangFromCookieCallback()
                            )
                        }
                    } else if (!TextUtils.isEmpty(transText)) {
                        dialogViewTrans.runInBackground(EvalLongPressJsTask(dialogViewTrans, transText))
                    }
                    val context = dialogViewTrans.appCtx
                    if (context != null && !DataTrans.instance(context).hasLanguages()) {
                        injectTransJs(dialogViewTrans.page)
                    }
                    if (TextUtils.isEmpty(dialogViewTrans.detectedLang)) {
                        checkTransClassJs(dialogViewTrans.page)
                    }
                }
            }
        }

        @JavascriptInterface
        fun onJsResult(callback: String?, jsResultB: String?) {
            if (TextUtils.isEmpty(callback)) {
                return
            }
            val isTransList = callback == "onTransList"
            val dialogViewTrans = this@DialogViewTrans
            if (isTransList) {
                if (dialogViewTrans.page == null || TextUtils.isEmpty(jsResultB)) {
                    return
                }
                dialogViewTrans.jsResultB = jsResultB
                dialogViewTrans.runInBackground(dialogViewTrans.ParseTransResultTask())
                return
            }
            if (callback != "onTransClass") {
                if (callback == "onTtsMark") {
                    val handler = dialogViewTrans.mainHandler
                    handler.post(ParseFloatOrTask(dialogViewTrans, jsResultB))
                    return
                    return
                }
                return
            }
            if (dialogViewTrans.page != null) {
                if ("0" == jsResultB) {
                    dialogViewTrans.detectedLang = "-"
                    return
                }
                if (TextUtils.isEmpty(jsResultB)) {
                    dialogViewTrans.detectedLang = "-"
                } else if (TextUtils.isEmpty(dialogViewTrans.detectedLang)) {
                    dialogViewTrans.jsResultC = jsResultB
                    dialogViewTrans.runInBackground(dialogViewTrans.LangPrefixTask())
                }
            }
        }

        @JavascriptInterface
        fun onObserDet(transLang: String?, autoTrans: Int) {
            val dialogViewTrans = this@DialogViewTrans
            if (autoTrans == 0) {
                dialogViewTrans.transState = 1.toByte()
            } else {
                dialogViewTrans.transState = 3.toByte()
                dialogViewTrans.autoTrans = autoTrans == 2
                dialogViewTrans.transLang = transLang
                if (TextUtils.isEmpty(AlbumSettings.transPick.value)) {
                    AlbumSettings.editLocal { set(AlbumSettings.transPick, transLang) }
                    AlbumSettings.editLocal { set(AlbumSettings.transCode, "") }
                    PrefAlbum.saveTrans(dialogViewTrans.appCtx)
                }
                if (MainUtil.sameObject(dialogViewTrans.transText, transLang)) {
                    dialogViewTrans.transText = null
                }
            }
            dialogViewTrans.transDone = false
            val myLineFrame = dialogViewTrans.transFrame
            if (myLineFrame == null) {
                return
            }
            myLineFrame.post(TransStateStep())
        }
    }

    init {
        this.ttsMoveRunnable = HighlightRangeTask()
        this.hostActivity = mainActivity
        this.appCtx = context
        this.transPick = transPick
        this.pageLang = pageLang
        this.transRunning = true
        this.transPickLast = transPickLast
        this.transCodeLast = AlbumSettings.transCode.value
        speakButton = TransBtn()
        logo = TransLogo().also { it.res = 1 }
        setSheet { Sheet() }
        show()
    }

    // ------------------------------------------------------------------ Compose chrome

    /** A round button whose look the engine sets through the old `MyButtonImage` calls. */
    class TransBtn {
        var visible by mutableStateOf(true)
        var res by mutableIntStateOf(0)
        var loading by mutableStateOf(false)
        var click: View.OnClickListener? = null
        fun setVisibility(newVisible: Int) { visible = newVisible == 0 }
        fun setImageResource(newRes: Int) { res = newRes }
        fun setBgNorColor(color: Int) {}
        fun setBgPreColor(press: Int) {}
        fun setOnClickListener(newClick: View.OnClickListener?) { click = newClick }
        fun setLoading(color: Int, loadingFlag: Boolean) { loading = true }
        fun setLoad(newLoading: Boolean) { loading = newLoading }
    }

    /** The "Google Translate" logo at the bottom end of the text area. */
    class TransLogo {
        var res by mutableIntStateOf(0)
        fun setBackgroundResource(newRes: Int) { res = newRes }
    }

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runInBackground(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    @JvmField
    val mainHandler: Handler = Handler(android.os.Looper.getMainLooper())

    private var barState by mutableStateOf(false)
    private var barVersion by mutableIntStateOf(0)
    private var composedOnce = false

    @androidx.compose.runtime.Composable
    private fun Sheet() {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val dark = AppState.darkUi
        val screen = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
        Box(Modifier.fillMaxWidth().heightIn(min = 240.dp, max = screen * 0.7f)) {
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).also { sheetFrame2 ->
                        sheetFrame = sheetFrame2
                        contentView = sheetFrame2
                        if (!composedOnce) {
                            composedOnce = true
                            transState = 1.toByte()
                            transText = AlbumSettings.transPick.value
                            speakButton?.setOnClickListener(InitTtsClick())
                            mainHandler.post(SetNoAutofillTask())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            val play = speakButton
            if (play != null && play.visible) {
                Box(
                    Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp).size(48.dp)
                        .background(Color(if (dark) -11513776 else -460552), CircleShape).clip(CircleShape)
                        .clickable { play.click?.onClick(null) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (play.loading) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
                    else Icon(
                        painterResource(if (dark) AppR.drawable.baseline_play_arrow_dark_24 else AppR.drawable.baseline_play_arrow_black_24),
                        contentDescription = null,
                    )
                }
            }
            logo?.let { logo ->
                if (logo.res != 0) Image(
                    painterResource(if (dark) AppR.drawable.trans_logo_short_back_dark else AppR.drawable.trans_logo_short_back_color),
                    contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).width(130.dp).height(24.dp),
                )
            }
        }
        // the line that holds the translate bar
        AndroidView(
            factory = { ctx -> FrameLayout(ctx).also { transFrame = it } },
            modifier = Modifier.fillMaxWidth().height(with(density) { MainApp.dp48.toDp() }),
        )
        if (barState) {
            barVersion
            Row(
                Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BarButton(rewindBtn, AppR.drawable.baseline_fast_rewind_black_24)
                BarButton(pauseBtn, AppR.drawable.baseline_pause_black_24)
                BarButton(stopBtn, AppR.drawable.baseline_stop_black_24)
                BarButton(searchBtn, AppR.drawable.outline_search_black_24)
                BarButton(forwardBtn, AppR.drawable.baseline_fast_forward_black_24)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun BarButton(button: TransBtn?, icon: Int) {
        if (button == null) return
        Box(
            Modifier.size(48.dp).clip(CircleShape).clickable { button.click?.onClick(null) },
            contentAlignment = Alignment.Center,
        ) {
            if (button.loading) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            else Icon(painterResource(icon), contentDescription = null)
        }
    }

    /** Shows the playback bar (old `ShowBarTask` / `29` / `35`: a popup window with five round buttons). */
    fun showBar() {
        if (speakBarState != null || transFrame == null) {
            barClosed = false
            return
        }
        transFrame?.setAlpha(0.4f)
        rewindBtn = TransBtn(); pauseBtn = TransBtn(); stopBtn = TransBtn(); searchBtn = TransBtn(); forwardBtn = TransBtn()
        ttsMoveIndex = -1234
        ttsJump = false
        currentBtn = stopBtn
        if (isBusy(true)) {
            currentBtn!!.setLoading(if (AppState.darkUi) -328966 else -16777216, true)
        }
        rewindBtn!!.setOnClickListener(LockClick())
        searchBtn!!.setOnClickListener(TtsBackClick())
        forwardBtn!!.setOnClickListener(TtsForwardClick())
        pauseBtn!!.setOnClickListener(PauseBtnClick())
        stopBtn!!.setOnClickListener(StopBtnClick())
        speakBarState = Any()
        barState = true
        barVersion++
        barClosed = false
    }

    fun loadPage(pageUrl: String?) {
        if (this.page != null) {
            val isHttp = isHttpUrl(pageUrl)
            val urlPending = this.urlPending
            if (isHttp) {
                if (urlPending) {
                    this.urlPending = false
                    val webNestView = this.page
                    if (webNestView != null) {
                        webNestView.post(RemoveJsBridgeTask())
                        return
                    }
                    return
                }
                return
            }
            if (urlPending) {
                return
            }
            this.urlPending = true
            val webNestView = this.page
            if (webNestView == null) {
                return
            }
            webNestView.post(AddJsBridgeTask())
        }
    }

    fun lockClick(): Boolean {
        if (this.clickLocked) {
            return true
        }
        this.clickLocked = true
        val handler = this.mainHandler
        handler.post(UnlockClickTask())
        return false
    }

    /** Hides the playback bar (old: dismissed its popup window). */
    fun clearBar() {
        speakBarState = null
        barState = false
        rewindBtn = null
        pauseBtn = null
        stopBtn = null
        currentBtn = null
        searchBtn = null
        forwardBtn = null
        barVersion++
        transFrame?.setAlpha(1.0f)
    }

    /** Lets the (dead) engine go and makes a new one; [onReady] gets it once it can speak, or null. */
    fun recreateTts(onReady: (TextToSpeech?) -> Unit) {
        com.mycompany.app.main.SpeechGuard.release(this.ttsEngine)
        this.ttsEngine = null
        val context = this.appCtx ?: return onReady(null)
        this.ttsEngine = com.mycompany.app.main.SpeechGuard.create(context, StopTtsStep(), onReady)
    }

    /** Reading aloud did not work: it stops, and a message offers Android's speech settings. */
    fun ttsFailed() {
        com.mycompany.app.main.SpeechGuard.cancelAll()
        if (this.ttsState.toInt() == 0 && !this.speakMode) return
        this.ttsState = 0.toByte()
        this.speakMode = false
        this.ttsParagraphs = null
        this.ttsIndex = 0
        setTtsState(0, false)
        com.mycompany.app.main.SpeechGuard.showDown(this.hostActivity)
    }

    fun initTts(): Boolean {
        if (this.appCtx == null || this.ttsEngine != null) {
            return false
        }
        this.ttsLang = TtsSettings.ttsLang.value
        try {
            val textToSpeech = TextToSpeech(this.appCtx, ResetTtsStep())
            this.ttsEngine = textToSpeech
            textToSpeech.setOnUtteranceProgressListener(StopTtsStep())
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    fun isBusy(checkTrans: Boolean): Boolean {
        return this.transRunning || (checkTrans && this.autoTrans && !this.transDone) || this.transState.toInt() == 1
    }

    fun pauseTts(pause: Boolean) {
        if (pause) {
            clearSelection()
        }
        this.ttsState = 2.toByte()
        try {
            val textToSpeech = this.ttsEngine
            if (textToSpeech == null || !textToSpeech.isSpeaking()) {
                return
            }
            this.ttsEngine!!.stop()
        } catch (unused: Exception) {
        }
    }

    fun resetTts() {
        clearSelection()
        this.ttsState = 0.toByte()
        this.speakMode = false
        this.ttsLang = null
        this.ttsLangSet = null
        this.ttsParagraphs = null
        this.ttsIndex = 0
        this.ttsStartPos = 0
        this.ttsEndPos = 0
        val textToSpeech = this.ttsEngine
        if (textToSpeech != null) {
            try {
                if (textToSpeech.isSpeaking()) {
                    this.ttsEngine!!.stop()
                }
                this.ttsEngine!!.shutdown()
            } catch (unused: Exception) {
            }
            this.ttsEngine = null
        }
    }

    fun clearSelection() {
        val webNestView = this.page
        if (webNestView == null) {
            return
        }
        evalJsNow(webNestView, "window.getSelection().removeAllRanges();", true)
    }

    /** Read-aloud language: TTS setting, translation target, page language or device locale.  */
    fun applyTtsLang() {
        var locale: Locale? = null
        val language: String?
        if (!TextUtils.isEmpty(TtsSettings.ttsLang.value)) {
            language = TtsSettings.ttsLang.value
        } else if (this.autoTrans && !TextUtils.isEmpty(AlbumSettings.transCode.value)) {
            language = AlbumSettings.transCode.value
        } else if (!TextUtils.isEmpty(this.pageLang)) {
            language = this.pageLang
        } else if (!TextUtils.isEmpty(this.ttsLang)) {
            language = this.ttsLang
        } else {
            locale = MainApp.currentLocale()
            language = locale.toString()
        }
        if (MainUtil.sameObject(this.ttsLangSet, language)) {
            return
        }
        this.ttsLangSet = language
        if (locale == null) {
            locale = parseLocale(language)
        }
        if (locale == null) {
            locale = MainApp.currentLocale()
        } else {
            var available: Int
            try {
                available = this.ttsEngine!!.isLanguageAvailable(locale)
            } catch (unused: Exception) {
                available = -2
            }
            if (available != TextToSpeech.LANG_AVAILABLE && available != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                locale = MainApp.currentLocale()
            }
        }
        try {
            this.ttsEngine!!.setLanguage(locale)
        } catch (unused: Exception) {
        }
    }

    fun highlightRange(rangeStart: Int, rangeEnd: Int, ttsIndex: Int, force: Boolean) {
        var force = force
        if (this.ttsRestart) {
            this.ttsRestart = false
            force = true
        }
        if (this.page != null) {
            val arrayList = this.ttsParagraphs
            val ttsItem =
                if (arrayList == null || ttsIndex < 0 || ttsIndex >= arrayList.size) null else this.ttsParagraphs!!.get(ttsIndex) as TtsItem?
            if (ttsItem == null) {
                return
            }
            val end = ttsItem.end
            val selStart = rangeStart + end
            val selEnd = end + rangeEnd
            val sb = StringBuilder("(function(){var ele=document.getElementById('")
            sb.append(ttsItem.start).append("');setSelRange(ele,").append(selStart).append(",")
            sb.append(selEnd)
            sb.append(");")
            if (force) {
                sb.append("var tag='onTtsMark';var val=0;var bcr=null;if(range){bcr=range.getBoundingClientRect();}else{bcr=ele.getBoundingClientRect();}if(bcr){val=bcr.top+','+bcr.bottom;}android.onJsResult(tag,val);")
            }
            sb.append("})();")
            evalJsNow(this.page, sb.toString(), true)
        }
    }

    fun ttsMove(state: Int) {
        val arrayList: ArrayList<Any?>? = if (this.page == null) null else this.ttsParagraphs
        if (this.page == null || arrayList == null) {
            return
        }
        var size = this.ttsMoveIndex
        if (size == -1234) {
            size = this.ttsIndex
            this.ttsMoveIndex = size
        }
        if (state == 1) {
            size--
        } else if (state == 2) {
            size++
        }
        if (size >= arrayList.size) {
            size = this.ttsParagraphs!!.size - 1
        }
        if (size < 0) {
            size = 0
        }
        val ttsParagraphs = this.ttsParagraphs
        val ttsItem =
            if (ttsParagraphs == null || size < 0 || size >= ttsParagraphs.size) null else this.ttsParagraphs!!.get(
                size
            ) as TtsItem?
        val speech = if (ttsItem != null) ttsItem.speech else null
        if (TextUtils.isEmpty(speech)) {
            return
        }
        this.speakMode = true
        val webNestView = this.page
        val runnable = this.ttsMoveRunnable
        webNestView!!.removeCallbacks(runnable)
        if (state != 0) {
            pauseTts(false)
        }
        highlightRange(0, speech!!.length, size, true)
        this.ttsMoveIndex = size
        this.ttsJump = true
        this.page!!.postDelayed(runnable, 200L)
    }

    fun setTtsState(state: Int, translating: Boolean) {
        if (this.speakButton == null || this.contentView == null) {
            return
        }
        this.ttsState = (state).toByte()
        if (state == 1) {
            this.speakMode = true
        } else {
            this.speakMode = false
            clearSelection()
        }
        if (this.ttsState.toInt() == 1 && translating) {
            return
        }
        this.mainHandler.post(ClearBarTask())
    }

    fun speak(fromStart: Boolean) {
        if (this.page == null) {
            return
        }
        clearSelection()
        this.speakMode = true
        this.ttsStartPos = 0
        this.ttsEndPos = 0
        var arrayList: ArrayList<WebReadTask.ReadItem?>? = null
        if (fromStart) {
            val transItems = this.transItems
            if (transItems == null || transItems.isEmpty()) {
                this.ttsState = 0.toByte()
                this.speakMode = false
                this.ttsParagraphs = null
                this.ttsIndex = 0
                setTtsState(0, false)
                return
            }
            arrayList = transItems
        }
        this.paragraphQueue = arrayList
        runInBackground(InitTtsTask())
    }

    fun stopTts() {
        com.mycompany.app.main.SpeechGuard.cancelAll()
        clearSelection()
        this.ttsState = 0.toByte()
        this.speakMode = false
        this.ttsParagraphs = null
        this.ttsIndex = 0
        this.ttsStartPos = 0
        this.ttsEndPos = 0
        try {
            val textToSpeech = this.ttsEngine
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                this.ttsEngine!!.stop()
            }
            val myButtonImage = this.speakButton
            if (myButtonImage != null) {
                if (!this.speakMode && this.autoTrans) {
                    myButtonImage.setVisibility(0)
                }
                val currentBtn = this.currentBtn
                if (currentBtn != null) {
                    currentBtn.setLoad(false)
                    this.currentBtn = null
                }
            }
        } catch (unused: Exception) {
        }
    }

    public override fun dismiss() {
        if (this.appCtx == null) {
            return
        }
        if (!TextUtils.isEmpty(this.transPickLast) && this.transPickLast != AlbumSettings.transPick.value) {
            AlbumSettings.editLocal { set(AlbumSettings.transPick, this@DialogViewTrans.transPickLast) }
            AlbumSettings.editLocal { set(AlbumSettings.transCode, this@DialogViewTrans.transCodeLast) }
            PrefAlbum.saveTrans(this.appCtx)
        }
        this.transPickLast = null
        this.transCodeLast = null
        val mainTransText = this.transTextView
        if (mainTransText != null) {
            mainTransText.release()
            this.transTextView = null
        }
        resetTts()
        val dialogTransLang = this.transLangDialog
        if (dialogTransLang != null) {
            dialogTransLang.dismiss()
            this.transLangDialog = null
        }
        this.contentView = null
        val webNestView = this.page
        if (webNestView != null) {
            destroyWebView(webNestView, true)
            this.page = null
        }
        this.speakButton = null
        this.transFrame = null
        val webTransControl = this.transControl
        if (webTransControl != null) {
            webTransControl.release()
            this.transControl = null
        }
        this.hostActivity = null
        this.appCtx = null
        this.sheetFrame = null
        this.logo = null
        this.transPick = null
        this.pageLang = null
        this.pageJs = null
        this.transItems = null
        this.pendingUrl = null
        this.transLang = null
        this.transText = null
        this.detectedLang = null
        this.baseUrl = null
        super.dismiss()
    }

    /** Back: closes the playback bar first (stopping the reading), then the sheet. */
    fun closeBar() {
        if (speakBarState == null) {
            dismiss()
        } else {
            stopTts()
            clearBar()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        closeBar()
    }

    internal inner class TransControlActions : TransCtrlListener {
        override fun onOriginal() {
            val dialogViewTrans = this@DialogViewTrans
            val transState = dialogViewTrans.transState
            if (transState.toInt() == 1) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
            } else if (transState.toInt() == 3) {
                evalConfirmJs(dialogViewTrans.page, "restore")
            }
        }

        override fun onMenu(view: View?) {
            val dialogViewTrans = this@DialogViewTrans
            val dialogTransLang: DialogTransLang? = if (dialogViewTrans.hostActivity != null) dialogViewTrans.transLangDialog else null
            if (dialogViewTrans.hostActivity != null && dialogTransLang == null) {
                if (!DataTrans.instance(dialogViewTrans.appCtx).hasLanguages() && dialogViewTrans.transState.toInt() == 1) {
                    showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
                    return
                }
                val transLangDialog =
                    DialogTransLang(dialogViewTrans.hostActivity, false, dialogViewTrans.TransLangPicked())
                dialogViewTrans.transLangDialog = transLangDialog
                transLangDialog.setOnDismissListener(dialogViewTrans.TransLangDialogDismiss())
            }
        }

        override fun onTranslate() {
            val dialogViewTrans = this@DialogViewTrans
            val transState = dialogViewTrans.transState
            if (transState.toInt() == 1) {
                showToast(dialogViewTrans.appCtx, AppR.string.wait_retry)
            } else if (transState.toInt() == 3) {
                evalConfirmJs(dialogViewTrans.page, "confirm")
            }
        }

        override fun onClose() {
        }
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogViewTrans = this@DialogViewTrans
            val webNestView = dialogViewTrans.page
            if (webNestView == null) {
                return
            }
            webNestView.setWebLoading(false)
            dialogViewTrans.loadPage(url)
            if (!dialogViewTrans.finishHandled && !TextUtils.isEmpty(url) && (url != dialogViewTrans.lastFinishedUrl)) {
                dialogViewTrans.finishHandled = true
                dialogViewTrans.lastFinishedUrl = url
                val handler = dialogViewTrans.mainHandler
                handler.post(dialogViewTrans.ClearCacheTask())
            }
            dialogViewTrans.loadPending = true
            if (!TextUtils.isEmpty(dialogViewTrans.pendingUrl)) {
                val pendingUrl = dialogViewTrans.pendingUrl
                dialogViewTrans.loadPending = false
                dialogViewTrans.pendingUrl = null
                evalJsNow(dialogViewTrans.page, pendingUrl, true)
            }
            if (TextUtils.isEmpty(dialogViewTrans.pageJs)) {
                dialogViewTrans.runInBackground(dialogViewTrans.InjectPageJsTask())
            } else {
                evalJsNow(dialogViewTrans.page, dialogViewTrans.pageJs, true)
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogViewTrans = this@DialogViewTrans
            val webNestView = dialogViewTrans.page
            if (webNestView == null) {
                return
            }
            webNestView.setWebLoading(true)
            dialogViewTrans.loadPage(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val dialogViewTrans = this@DialogViewTrans
            dialogViewTrans.page = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogViewTrans.mainHandler
            handler.post(dialogViewTrans.DismissTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.page != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                dialogViewTrans.loadPage(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val dialogViewTrans = this@DialogViewTrans
            if (dialogViewTrans.page != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    dialogViewTrans.loadPage(url)
                }
            }
            return false
        }
    }

    companion object {
    }
}
