package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.state.AppState
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mycompany.app.data.book.DataBookScript
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.db.book.DbBookScript.Companion.saveScript
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.httpsUrl
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.showToastText
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.script.Script
import com.mycompany.app.script.Script.Companion.parse
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.web.WebSrcView
import org.apache.commons.text.StringEscapeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeFullDialog

/**
 * Full-screen user-script editor ([childItem] = the script to edit, null = a new one from the template): the source
 * sits in an html textarea inside a [WebSrcView] (read back through the "android" JS interface); the check button
 * saves (parse, name / namespace clash = "exists", else stored in DbBookScript and reported to [EditUrlListener.onSaved]
 * on dismiss), back asks to save when there are changes ([DialogSaveConfirm]). Compose now (on [ComposeFullDialog]:
 * top bar + the web editor + a loading cover); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogEditScript(
    mainActivity: android.app.Activity?,
    childItem: ChildItem?,
    editUrlListener: EditUrlListener?
) : ComposeFullDialog(mainActivity) {
    /** Compose stand-in for the old header buttons and cover view (visibility / enabled). */
    class UiPart(visible: Boolean) {
        var shown by mutableStateOf(visible)
        var on by mutableStateOf(true)
        fun setVisibility(visibility: Int) { shown = visibility == 0 }
        fun setEnabled(newOn: Boolean) { on = newOn }
    }

    private val ui = android.os.Handler(android.os.Looper.getMainLooper())
    @JvmField val mainHandler: Handler? = ui
    private var executor: java.util.concurrent.ExecutorService? = null

    @JvmField var hostActivity: android.app.Activity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var editListener: EditUrlListener? = editUrlListener
    @JvmField var scriptItem: ChildItem? = childItem
    @JvmField var forceLoad: Boolean = false
    @JvmField var backButton: UiPart? = UiPart(true)
    @JvmField var saveButton: UiPart? = UiPart(false)
    @JvmField var srcView: WebSrcView? = null
    @JvmField var pageLoading: Boolean = false
    @JvmField var loadCover: UiPart? = UiPart(false)
    @JvmField var jsReady: Boolean = false
    @JvmField var pageReady: Boolean = false
    @JvmField var scriptName: String? = null
    @JvmField var scriptTitle: String? = null
    @JvmField var scriptMatch: String? = null
    @JvmField var originalText: String? = null
    @JvmField var currentText: String? = null
    @JvmField var saveConfirm: DialogSaveConfirm? = null
    @JvmField var urlLoading: Boolean = false
    @JvmField var baseUrl: String? = null
    @JvmField var saving: Boolean = false
    @JvmField var savePending: Boolean = false
    @JvmField var localeName: String? = null
    @JvmField var pageHtml: String? = null
    @JvmField var finishHandled: Boolean = false
    @JvmField var lastFinishedUrl: String? = null
    @JvmField var pendingJs: String? = null
    @JvmField var closeAfterSave: Boolean = false
    @JvmField var edited: Boolean = false
    @JvmField var pendingText: String? = null
    @JvmField var needsReload: Boolean = false
    @JvmField var itemId: Long = 0

    private var web by mutableStateOf<WebSrcView?>(null)
    private var dark by mutableStateOf(AppState.darkUi)

    /** Theme switched while open (called by the list screen). */
    fun applyTheme() {
        dark = AppState.darkUi
        srcView?.setBackgroundColor(if (AppState.darkUi) -14606047 else -1)
    }

    /** Old popup-menu close; there is no popup now. */
    fun refreshButtons() {}

    init {
        val back = backButton!!
        val save = saveButton!!
        val cover = loadCover!!
        setPage {
            val dark = this.dark
            Surface(
                Modifier.fillMaxSize(), color = if (dark) Color.Black else Color(0xFFF8F8F8.toInt()),
                contentColor = if (dark) Color(0xFFFAFAFA.toInt()) else Color.Black,
            ) {
                Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                    Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { requestText(true) }, enabled = back.on) {
                            Icon(painterResource(AppR.drawable.outline_chevron_left_black_24), null)
                        }
                        Text(
                            stringResource(AppR.string.user_script_title), style = MaterialTheme.typography.titleLarge,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 12.dp),
                        )
                        if (save.shown) {
                            IconButton(onClick = { requestText(false) }, enabled = save.on) {
                                Icon(painterResource(AppR.drawable.outline_check_black_24), stringResource(AppR.string.save))
                            }
                        }
                    }
                    Box(
                        Modifier.weight(1f).fillMaxWidth()
                            .background(if (dark) Color(0xFF212121.toInt()) else Color.White)
                    ) {
                        web?.let { view ->
                            AndroidView(
                                // MATCH_PARENT: with AndroidView's default WRAP_CONTENT the WebView gets a 0-height viewport (100vh = 0).
                                factory = { view.apply { layoutParams = android.view.ViewGroup.LayoutParams(-1, -1) } }, modifier = Modifier.fillMaxSize().padding(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp))
                        }
                        if (cover.shown) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        }
                    }
                }
            }
        }
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setCanceledOnTouchOutside(false)
        show()
        ui.post { start() }
        runAsync(QueryTask())
    }

    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    /** the web editor, then its setup ([SetWebViewClientTask]). */
    private fun start() {
        if (appContext == null || hostActivity == null) return
        val webSrcView = WebSrcView(hostActivity)
        webSrcView.setBackgroundColor(if (AppState.darkUi) -14606047 else -1)
        srcView = webSrcView
        web = webSrcView
        loadCover!!.setVisibility(0)
        ui.post(SetWebViewClientTask())
    }

    /** Old `SrcViewListener` (only drew a scroll shadow). */
    internal inner class SrcViewListener : com.mycompany.app.web.WebNestView.WebViewListener {
        override fun onCrashAction(action: Int) {}
        override fun openAdvanced() {}
        override fun onProgress(progress: Int) {}
        override fun canPull(): Boolean = false
        override fun onScrollStop() {}
        override fun onBlockCount(count: Int) {}
        override fun onPull(x: Float, y: Float, action: Int): Boolean = false
        override fun onPageCommit(pageUrl: String?) {}
    }

    internal inner class SetWebViewClientTask : Runnable {
        internal inner class LoadPageStep : Runnable {
            internal inner class StartLoadStep : Runnable {
                override fun run() {
                    val setWebViewClientTask = this@SetWebViewClientTask
                    val dialogEditScript = this@DialogEditScript
                    val webSrcView = dialogEditScript.srcView
                    if (webSrcView != null) {
                        dialogEditScript.urlLoading = true
                        webSrcView.addJavascriptInterface(
                            dialogEditScript.WebAppInterface(),
                            "android"
                        )
                        val owner = this@DialogEditScript
                        owner.jsReady = true
                        val handler: Handler? = if (TextUtils.isEmpty(owner.pendingJs)) null else owner.mainHandler
                        if (TextUtils.isEmpty(owner.pendingJs) || handler == null) {
                            return
                        }
                        handler.post(owner.RunPendingJsTask())
                    }
                }
            }
            override fun run() {
                val dialogEditScript = this@DialogEditScript
                val webSrcView = dialogEditScript.srcView
                if (webSrcView != null) {
                    webSrcView.setWebViewClient(dialogEditScript.LocalWebViewClient())
                    webSrcView.setWebChromeClient(dialogEditScript.LocalChromeClient())
                    webSrcView.setListener(dialogEditScript.SrcViewListener())
                    val handler = dialogEditScript.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(StartLoadStep())
                }
            }
        }
        override fun run() {
            val dialogEditScript = this@DialogEditScript
            val webSrcView = dialogEditScript.srcView
            if (webSrcView != null) {
                val settings = webSrcView.getSettings()
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(false)
                settings.setJavaScriptEnabled(true)
                webSrcView.setOverScrollMode(2)
                webSrcView.setVerticalScrollBarEnabled(false)
                webSrcView.setHorizontalScrollBarEnabled(false)
                val handler = dialogEditScript.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadPageStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val dialogEditScript = this@DialogEditScript
            MainUtil.clearWebCache(dialogEditScript.srcView)
            dialogEditScript.finishHandled = false
        }
    }
    internal inner class DismissTask : Runnable {
        override fun run() {
            this@DialogEditScript.dismiss()
        }
    }
    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val dialogEditScript = this@DialogEditScript
            val webSrcView = dialogEditScript.srcView
            if (webSrcView == null) {
                return
            }
            dialogEditScript.urlLoading = false
            webSrcView.removeJavascriptInterface("android")
        }
    }
    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val dialogEditScript = this@DialogEditScript
            val webSrcView = dialogEditScript.srcView
            if (webSrcView == null) {
                return
            }
            dialogEditScript.urlLoading = true
            webSrcView.addJavascriptInterface(dialogEditScript.WebAppInterface(), "android")
        }
    }
    internal inner class RunPendingJsTask : Runnable {
        override fun run() {
            val dialogEditScript = this@DialogEditScript
            if (!dialogEditScript.jsReady || TextUtils.isEmpty(dialogEditScript.pendingJs)) {
                return
            }
            val pendingJs = dialogEditScript.pendingJs
            dialogEditScript.pendingJs = null
            if (dialogEditScript.srcView != null) {
                dialogEditScript.loadCover!!.setVisibility(8)
                MainUtil.loadHtml(dialogEditScript.srcView, dialogEditScript.baseUrl, pendingJs)
            }
        }
    }
    internal inner class SaveScriptTask : Runnable {
        override fun run() {
            val dialogEditScript = this@DialogEditScript
            val closeAfterSave = dialogEditScript.closeAfterSave
            val edited = dialogEditScript.edited
            dialogEditScript.closeAfterSave = false
            dialogEditScript.saving = false
            if (!edited) {
                if (closeAfterSave || dialogEditScript.forceLoad) {
                    dialogEditScript.dismiss()
                    return
                } else {
                    dialogEditScript.saveScript()
                    return
                }
            }
            if (!closeAfterSave) {
                dialogEditScript.saveScript()
                return
            }
            val dialogSaveConfirm: DialogSaveConfirm? = if (dialogEditScript.hostActivity == null) null else dialogEditScript.saveConfirm
            if (dialogEditScript.hostActivity == null || dialogSaveConfirm != null) {
                return
            }
            val saveConfirm =
                DialogSaveConfirm(dialogEditScript.hostActivity, dialogEditScript.SaveConfirmListener())
            dialogEditScript.saveConfirm = saveConfirm
            saveConfirm.setOnDismissListener(dialogEditScript.SaveConfirmDialogDismiss())
        }
    }
    internal inner class SaveConfirmListener : DialogSeekListener {
        override fun onValue(newValue: Int) {
            val unusedZero: Int = syntheticZero
            val dialogEditScript = this@DialogEditScript
            val dialogSaveConfirm = dialogEditScript.saveConfirm
            if (dialogSaveConfirm != null) {
                dialogSaveConfirm.dismiss()
                dialogEditScript.saveConfirm = null
            }
            if (newValue == 0) {
                dialogEditScript.saveScript()
            } else {
                dialogEditScript.dismiss()
            }
        }
    }
    internal inner class SaveConfirmDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val unusedZero: Int = syntheticZero
            val dialogEditScript = this@DialogEditScript
            val dialogSaveConfirm = dialogEditScript.saveConfirm
            if (dialogSaveConfirm != null) {
                dialogSaveConfirm.dismiss()
                dialogEditScript.saveConfirm = null
            }
        }
    }
    internal inner class QueryTask : Runnable {
        /**
         * Loads the edited script's source (or a new-script template), wraps it in an html textarea
         * page (colored for the theme) and opens it via RunPendingJsTask once the dialog is ready.
         */
        override fun run() {
            val dialog = this@DialogEditScript
            val item = dialog.scriptItem
            var content: String? = null
            if (item != null) {
                dialog.scriptTitle = item.name
                dialog.scriptMatch = item.namespace
                if (!TextUtils.isEmpty(item.name) && !TextUtils.isEmpty(item.namespace)) {
                    var cursor: Cursor? = null
                    try {
                        cursor = DbUtil.query(
                            DbBookScript.get(dialog.appContext).getWritableDatabase(),
                            "DbBookScript_table",
                            arrayOf<String?>("_content"),
                            "_name=? AND _namespace=?",
                            arrayOf<String?>(item.name, item.namespace),
                            null
                        )
                        if (cursor != null && cursor.moveToFirst()) {
                            content = cursor.getString(cursor.getColumnIndex("_content"))
                        }
                    } catch (ignored: Exception) {
                        content = null
                    }
                    if (cursor != null) {
                        cursor.close()
                    }
                }
            }
            if (item != null && !TextUtils.isEmpty(content)) {
                dialog.forceLoad = true
            } else {
                val title = dialog.defaultName()
                content =
                    ("// ==UserScript==\n// @name             " + title + "\n// @namespace        "
                            + (if (TextUtils.isEmpty(title)) null else "userscript://" + title!!.lowercase())
                            + "\n// @version          1.0\n// @description      User\n// @run-at           document-end\n"
                            + "// @match            https://*\n// ==/UserScript==\n\n(function() {\n\n    // your code here\n\n})();\n")
            }
            dialog.originalText = content
            val color = if (AppState.darkUi) "#fafafa;" else "black;"
            val background = if (AppState.darkUi) "#171717;'>" else "white;'>"
            val page = StringBuilder(
                ("<!DOCTYPE html><html dir='auto'><head><meta charset=\"utf-8\"/>"
                        + "<meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'/>"
                        + "<style>body{margin:0;padding:0;}</style></head><body><textarea style='display:block;margin:0;padding:0;"
                        + "width:100vw;height:100vh;border:none;outline:none;font-size:12px;color:")
            )
            page.append(color)
            page.append("background-color:")
            page.append(background)
            if (!TextUtils.isEmpty(content)) {
                var escaped: String?
                try {
                    escaped = StringEscapeUtils.escapeHtml4(content)
                } catch (exception: Exception) {
                    escaped = null
                }
                page.append(escaped)
            }
            page.append("</textarea></body></html>")
            dialog.pendingJs = page.toString()
            dialog.baseUrl = httpsUrl("soul_script_", null)
            if (dialog.jsReady && !TextUtils.isEmpty(dialog.pendingJs) && dialog.mainHandler != null) {
                dialog.mainHandler.post(dialog.RunPendingJsTask())
            }
        }
    }
    internal inner class DefaultNameTask : Runnable {
        internal inner class ApplyDefaultNameStep : Runnable {
            override fun run() {
                val dialogEditScript = this@DialogEditScript
                val unusedZero: Int = syntheticZero
                var text = dialogEditScript.pendingText
                dialogEditScript.pendingText = null
                val myButtonImage = dialogEditScript.backButton
                if (myButtonImage != null) {
                    myButtonImage.setEnabled(true)
                    dialogEditScript.saveButton!!.setEnabled(true)
                    dialogEditScript.loadCover!!.setVisibility(8)
                    if (dialogEditScript.needsReload) {
                        showToast(dialogEditScript.appContext, AppR.string.exist_name)
                    } else if (dialogEditScript.itemId > 0) {
                        dialogEditScript.dismiss()
                    } else {
                        if (TextUtils.isEmpty(text)) {
                            val sb = StringBuilder("Error\n")
                            text = sb.append(dialogEditScript.appContext!!.getString(AppR.string.check_format)).toString()
                        }
                        MainUtil.showToastText(dialogEditScript.appContext, text)
                    }
                }
                dialogEditScript.savePending = false
            }
        }
        override fun run() {
            try {
                var charset: String?
                var scriptName: String?
                val childItemM: ChildItem?
                val dataBookScriptK: DataBookScript?
                val list: MutableList<Long>?
                val unusedZero: Int = syntheticZero
                val dialogEditScript = this@DialogEditScript
                val context = dialogEditScript.appContext
                if (context != null) {
                    dialogEditScript.pendingText = null
                    dialogEditScript.needsReload = false
                    dialogEditScript.itemId = 0L
                    val childItem = dialogEditScript.scriptItem
                    val currentText = dialogEditScript.currentText
                    if (childItem != null) {
                        charset = childItem.path
                        scriptName = childItem.name
                    } else {
                        charset = null
                        scriptName = null
                    }
                    if (TextUtils.isEmpty(charset)) {
                        scriptName = dialogEditScript.defaultName()
                        if (TextUtils.isEmpty(scriptName)) {
                            charset = null
                        } else {
                            charset = "userscript://" + scriptName!!.lowercase()
                        }
                    }
                    val localeI: Locale? = if (TextUtils.isEmpty(dialogEditScript.localeName)) MainApp.currentLocale() else null
                    if (TextUtils.isEmpty(dialogEditScript.localeName) && localeI != null) {
                        val language = localeI.getLanguage()
                        if (!TextUtils.isEmpty(language)) {
                            val locale = Locale.US
                            val lowerCase = language.lowercase(locale)
                            val country = localeI.getCountry()
                            val localeTag = if (!TextUtils.isEmpty(country)) "$lowerCase-${country.lowercase(locale)}" else null
                            dialogEditScript.localeName = lowerCase
                            dialogEditScript.pageHtml = localeTag
                        }
                    }
                    val scriptC =
                        parse(context, charset, scriptName, currentText, dialogEditScript.localeName, dialogEditScript.pageHtml, true)
                    dialogEditScript.pendingText = Script.pendingText
                    Script.pendingText = null
                    if (scriptC != null) {
                        val name = scriptC.name
                        val namespace = scriptC.namespace
                        if (MainUtil.sameObject(name, dialogEditScript.scriptTitle) && MainUtil.sameObject(namespace, dialogEditScript.scriptMatch)) {
                            childItemM = DbBookScript.saveScript(
                                context,
                                scriptC,
                                childItem,
                                dialogEditScript.localeName,
                                dialogEditScript.pageHtml
                            )
                            if (childItemM != null) {
                                dialogEditScript.scriptTitle = name
                                dialogEditScript.scriptMatch = namespace
                                dataBookScriptK = DataBookScript.instance(context)
                                list = dataBookScriptK.itemIds
                                if (list != null) {
                                    dataBookScriptK.putItem(childItemM)
                                }
                                dialogEditScript.itemId = childItemM.id
                            }
                        } else {
                            val appContext = dialogEditScript.appContext
                            val dbBookScript = DbBookScript.cached
                            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(namespace)) {
                                childItemM = DbBookScript.saveScript(
                                    context,
                                    scriptC,
                                    childItem,
                                    dialogEditScript.localeName,
                                    dialogEditScript.pageHtml
                                )
                                if (childItemM != null) {
                                    dialogEditScript.scriptTitle = name
                                    dialogEditScript.scriptMatch = namespace
                                    dataBookScriptK = DataBookScript.instance(context)
                                    list = dataBookScriptK.itemIds
                                    if (list != null && list.size > 0) {
                                        dataBookScriptK.putItem(childItemM)
                                    }
                                    dialogEditScript.itemId = childItemM.id
                                }
                            } else if (DbUtil.existsWhere(
                                    DbBookScript.get(appContext).getWritableDatabase(),
                                    "DbBookScript_table",
                                    null,
                                    "_name=? AND _namespace=?",
                                    arrayOf<String?>(name!!, namespace!!)
                                ) == 1
                            ) {
                                dialogEditScript.needsReload = true
                            } else {
                                childItemM = DbBookScript.saveScript(
                                    context,
                                    scriptC,
                                    childItem,
                                    dialogEditScript.localeName,
                                    dialogEditScript.pageHtml
                                )
                                if (childItemM != null) {
                                    dialogEditScript.scriptTitle = name
                                    dialogEditScript.scriptMatch = namespace
                                    dataBookScriptK = DataBookScript.instance(context)
                                    list = dataBookScriptK.itemIds
                                    if (list != null) {
                                        dataBookScriptK.putItem(childItemM)
                                    }
                                    dialogEditScript.itemId = childItemM.id
                                }
                            }
                        }
                    }
                }
                val handler = dialogEditScript.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ApplyDefaultNameStep())
            } catch (_ex: Throwable) {
                throw RuntimeException(_ex)
            }
        }
    }

    private inner class LocalChromeClient : WebChromeClient() {
        override fun onProgressChanged(webView: WebView?, newProgress: Int) {
            if (newProgress == 100) {
                val unusedZero: Int = syntheticZero
                val dialogEditScript = this@DialogEditScript
                if (dialogEditScript.pageReady) {
                    return
                }
                dialogEditScript.pageReady = true
                val myButtonImage = dialogEditScript.saveButton
                if (myButtonImage != null) {
                    myButtonImage.setVisibility(0)
                }
            }
        }
    }

    private inner class WebAppInterface {
        @JavascriptInterface
        fun onEditText(currentText: String?) {
            var currentText = currentText
            val unusedZero: Int = syntheticZero
            if (TextUtils.isEmpty(currentText)) {
                currentText = null
            }
            val dialogEditScript = this@DialogEditScript
            dialogEditScript.currentText = currentText
            dialogEditScript.edited = !MainUtil.sameObject(currentText, dialogEditScript.originalText)
            val handler = dialogEditScript.mainHandler
            if (handler == null) {
                return
            }
            handler.post(dialogEditScript.SaveScriptTask())
        }
    }

    fun loadUrl(pageUrl: String?) {
        if (this.srcView != null) {
            val isHttp = isHttpUrl(pageUrl)
            val urlLoading = this.urlLoading
            if (isHttp) {
                if (urlLoading) {
                    this.urlLoading = false
                    val webSrcView = this.srcView
                    if (webSrcView != null) {
                        webSrcView.post(RemoveJsBridgeTask())
                        return
                    }
                    return
                }
                return
            }
            if (urlLoading) {
                return
            }
            this.urlLoading = true
            val webSrcView = this.srcView
            if (webSrcView == null) {
                return
            }
            webSrcView.post(AddJsBridgeTask())
        }
    }

    fun requestText(closeAfterSave: Boolean): Boolean {
        val webSrcView: WebSrcView? = if (!this.pageReady) null else this.srcView
        if (!this.pageReady || webSrcView == null) {
            return false
        }
        if (!this.saving && !this.savePending) {
            this.saving = true
            this.closeAfterSave = closeAfterSave
            evalJs(webSrcView, "android.onEditText(document.querySelector('textarea').value);", true)
        }
        return true
    }

    fun defaultName(): String? {
        var name: String?
        if (!TextUtils.isEmpty(this.scriptName)) {
            return this.scriptName
        }
        val now = System.currentTimeMillis()
        try {
            name =
                SimpleDateFormat("yyyyMMddhhmmssSSS", MainApp.currentLocale()).format(Date(now))
        } catch (unused: Exception) {
            name = now.toString()
        }
        val userName = if (TextUtils.isEmpty(name)) null else "User.$name"
        this.scriptName = userName
        return userName
    }

    fun saveScript() {
        if (this.srcView == null || this.editListener == null) {
            return
        }
        if (TextUtils.isEmpty(this.currentText)) {
            showToast(this.appContext, AppR.string.empty)
            return
        }
        this.savePending = true
        this.backButton!!.setEnabled(false)
        this.saveButton!!.setEnabled(false)
        this.loadCover!!.setVisibility(0)
        runAsync(DefaultNameTask())
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogEditScript = this@DialogEditScript
            if (dialogEditScript.srcView != null) {
                dialogEditScript.pageLoading = false
                dialogEditScript.loadUrl(url)
                if (!dialogEditScript.finishHandled && !TextUtils.isEmpty(url) && (url != dialogEditScript.lastFinishedUrl)) {
                    dialogEditScript.finishHandled = true
                    dialogEditScript.lastFinishedUrl = url
                    val handler = dialogEditScript.mainHandler
                    if (handler != null) {
                        handler.post(dialogEditScript.ClearCacheTask())
                    }
                }
                if (dialogEditScript.pageReady) {
                    return
                }
                dialogEditScript.pageReady = true
                val myButtonImage = dialogEditScript.saveButton
                if (myButtonImage == null) {
                    return
                }
                myButtonImage.setVisibility(0)
            }
        }
        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogEditScript = this@DialogEditScript
            if (dialogEditScript.srcView == null) {
                return
            }
            dialogEditScript.pageLoading = true
            dialogEditScript.loadUrl(url)
        }
        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val unusedZero: Int = syntheticZero
            val dialogEditScript = this@DialogEditScript
            dialogEditScript.srcView = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogEditScript.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(dialogEditScript.DismissTask())
            return true
        }
        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val dialogEditScript = this@DialogEditScript
            if (dialogEditScript.srcView != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                dialogEditScript.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }
        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val dialogEditScript = this@DialogEditScript
            if (dialogEditScript.srcView != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    dialogEditScript.loadUrl(url)
                }
            }
            return true
        }
    }

    fun tryClose() {
        if (requestText(true)) return
        dismiss()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        tryClose()
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        saveConfirm?.dismiss()
        saveConfirm = null
        val editUrlListener = editListener
        if (editUrlListener != null) {
            // Cleared first: the list's listener dismisses this dialog again from inside the callback.
            editListener = null
            val itemIdNow = itemId
            if (itemIdNow > 0) editUrlListener.onSaved(itemIdNow, null, null)
        }
        val webSrcView = srcView
        if (webSrcView != null) {
            if (pageLoading) {
                pageLoading = false
                webSrcView.stopLoading()
            }
            destroyWebViewNow(srcView)
            srcView = null
        }
        hostActivity = null
        appContext = null
        scriptItem = null
        scriptName = null
        scriptTitle = null
        scriptMatch = null
        originalText = null
        currentText = null
        baseUrl = null
        localeName = null
        pageHtml = null
        super.dismiss()
    }

    companion object {
        const val syntheticZero: Int = 0
    }
}
