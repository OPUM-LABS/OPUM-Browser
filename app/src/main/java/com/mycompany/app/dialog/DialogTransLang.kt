package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.DataTrans
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbRecentLang
import com.mycompany.app.db.book.DbRecentLang.Companion.addAsync
import com.mycompany.app.dialog.DialogSetMsg.DialogMsgListener
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainLangAdapter
import com.mycompany.app.main.MainLangAdapter.MainLangItem
import com.mycompany.app.main.MainLangAdapter.MainLangListener
import com.mycompany.app.view.MyWebSafe
import com.mycompany.app.main.MainUri.isFile
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.parseTransResult
import com.mycompany.app.main.MainUtil.httpsUrl
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.clipRound3
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefAlbum.Companion.get
import com.mycompany.app.pref.PrefAlbum.Companion.saveTrans
import com.mycompany.app.pref.PrefFloat.Companion.get
import com.mycompany.app.pref.PrefImage.Companion.get
import com.mycompany.app.pref.PrefList.Companion.get
import com.mycompany.app.pref.PrefMain.Companion.get
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefRead.Companion.get
import com.mycompany.app.pref.PrefSecret.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefTts.Companion.get
import com.mycompany.app.pref.PrefVideo.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.pref.PrefZone.Companion.get
import com.mycompany.app.pref.PrefZtri.Companion.get
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZtwo.Companion.get
import java.lang.ref.WeakReference
import android.os.Looper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Translation target language picker (news language when [newsMode]): search field (also by Korean initials) with a
 * clear button, recent languages (each removable; a delete button clears them, after asking) above all languages,
 * the current one marked; picking one stores it (`AlbumSettings.transPick.value` / `ZtwoSettings.newsPick.value`), records it as recent and calls
 * [TransLangListener]. When the language names are not cached yet ([DataTrans]) they are fetched once through a hidden
 * WebView with the old JS bridge. The list model ([MainLangAdapter] as data), the search task and the fetch are the
 * old code. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogTransLang(
    mainActivity: android.app.Activity?,
    newsMode: Boolean,
    transLangListener: TransLangListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var msgShown: Boolean = false
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var pendingHtml: String? = null
    @JvmField
    var jsResult: String? = null
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var langListener: TransLangListener? = transLangListener
    @JvmField
    var transListener: TransNotiListener? = null
    @JvmField
    var newsMode: Boolean = newsMode
    @JvmField
    var recentType: Byte = if (newsMode) 1.toByte() else 2.toByte()
    @JvmField
    var pickedLang: String? = if (newsMode) ZtwoSettings.newsPick.value else AlbumSettings.transPick.value
    @JvmField
    var selectedLang: Int = -1
    @JvmField
    var scrollPos: Int = -1
    @JvmField
    var koreanLocale: Boolean = MainUtil.isKoreanLocale()
    @JvmField
    var webSafe: MyWebSafe? = null
    @JvmField
    var pageLoading: Boolean = false
    @JvmField
    var jsReady: Boolean = false
    @JvmField
    var urlLoading: Boolean = false
    @JvmField
    var pendingJs: String? = null
    @JvmField
    var baseUrl: String? = null
    @JvmField
    var queryTask: DialogTask? = null
    @JvmField
    var allItems: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
    @JvmField
    var langAdapter: MainLangAdapter? = null
    /** Search text present (clear button shown). */
    @JvmField
    var clearMode: Boolean = false
    @JvmField
    var queryActive: Boolean = false
    @JvmField
    var msgDialog: DialogSetMsg? = null

    @JvmField
    val mainHandler: Handler? = Handler(Looper.getMainLooper())

    private var query by mutableStateOf("")
    private var clearShown by mutableStateOf(false)
    private var deleteShown by mutableStateOf(false)
    internal var loading by mutableStateOf(true)
    private var version by mutableIntStateOf(0)
    internal var scrollTo by mutableIntStateOf(-1)
    private var webHolder: android.widget.FrameLayout? = null

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    internal fun refreshList() {
        val adapter = langAdapter
        if (adapter != null) {
            // the selected language's name (old row binding set it while drawing)
            val sel = adapter.sel
            for (k in 0 until adapter.getItemCount()) {
                val item = adapter.itemAt(k) ?: continue
                if (item.rowType.toInt() == 0 && (item.code == sel || item.code == sel - 1000)) adapter.selectedCode = item.lang
            }
        }
        version++
    }

    init {
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { onQuery(it) },
                    placeholder = { Text(stringResource(AppR.string.search_url)) },
                    singleLine = true,
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {}),
                    trailingIcon = {
                        if (clearShown) {
                            IconButton(onClick = { onQuery(""); applyClearMode(false); startQuery(null) }) {
                                Icon(painterResource(AppR.drawable.outline_cancel_black_18), contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                if (deleteShown) {
                    IconButton(onClick = { askDeleteRecent() }) {
                        Icon(painterResource(AppR.drawable.outline_delete_black_20), contentDescription = stringResource(AppR.string.delete))
                    }
                }
            }
            Box(Modifier.weight(1f, fill = false).heightIn(min = 72.dp)) {
                AndroidView(
                    factory = { ctx -> android.widget.FrameLayout(ctx).also { webHolder = it } },
                    modifier = Modifier.matchParentSize().alpha(0f),
                )
                val listState = rememberLazyListState()
                val target = scrollTo
                LaunchedEffect(target) {
                    if (target >= 0) {
                        listState.scrollToItem(target)
                        scrollTo = -1
                    }
                }
                val adapter = langAdapter
                @Suppress("UNUSED_VARIABLE") val v = version
                if (adapter != null) {
                    val count = adapter.getItemCount()
                    LazyColumn(state = listState) {
                        items(count) { k ->
                            val item = adapter.itemAt(k)
                            if (item != null) LangRow(adapter, item)
                        }
                    }
                }
                if (loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).align(Alignment.Center))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { dismiss() }) { Text(stringResource(AppR.string.close)) }
            }
        }
        show()
        mainHandler?.post { start() }
    }

    @androidx.compose.runtime.Composable
    private fun LangRow(adapter: MainLangAdapter, item: MainLangItem) {
        if (item.rowType.toInt() == 2) {
            HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
            return
        }
        val name = item.lang ?: ""
        val match = item.match
        val selected = item.rowType.toInt() == 0 && !adapter.filtered && (item.code == adapter.sel || item.code == adapter.sel - 1000)
        val label = if (adapter.filtered && !match.isNullOrEmpty()) {
            buildAnnotatedString {
                val at = name.lowercase().indexOf(match)
                if (at < 0) append(name) else {
                    append(name.substring(0, at))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) { append(name.substring(at, at + match.length)) }
                    append(name.substring(at + match.length))
                }
            }
        } else buildAnnotatedString { append(name) }
        ListItem(
            headlineContent = { Text(label, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
            trailingContent = {
                if (item.rowType.toInt() == 1) {
                    IconButton(onClick = { adapter.removeRecent(item) }) {
                        Icon(painterResource(AppR.drawable.outline_close_black_18), contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier.clickable {
                var code = item.code
                if (code >= 1000) code -= 1000
                adapter.listener?.onPick(code, item.lang)
            },
        )
    }

    private fun onQuery(newQuery: String) {
        query = newQuery
        var queryText: String? = if (TextUtils.isEmpty(newQuery)) null else newQuery
        if (TextUtils.isEmpty(queryText)) {
            applyClearMode(false)
        } else {
            applyClearMode(true)
            queryText = queryText!!.trim { it <= ' ' }
        }
        val empty = TextUtils.isEmpty(queryText)
        if (!empty || queryActive) startQuery(queryText)
        queryActive = !empty
    }

    private fun askDeleteRecent() {
        if (hostActivity == null || msgDialog != null) return
        msgShown = false
        val msg = DialogSetMsg(hostActivity, true, AppR.string.lang_delete, AppR.string.delete, 0, ApplyClearModeStep())
        msgDialog = msg
        msg.setOnDismissListener(MsgDialogDismiss())
    }

    /** Shows the list, or first fetches the language names through the hidden WebView. */
    private fun start() {
        if (DataTrans.instance(appContext).hasLanguages()) {
            startQuery(null)
            return
        }
        val act = hostActivity ?: return
        val holder = webHolder
        if (webSafe != null || holder == null) {
            mainHandler?.postDelayed({ start() }, 100)
            return
        }
        val web = MyWebSafe(act)
        webSafe = web
        MainApp.resumeWebTimers(appContext, web)
        web.setVisibility(View.INVISIBLE)
        holder.addView(web, 0, ViewGroup.LayoutParams(-1, -1))
        mainHandler?.post(SetWebViewClientTask())
    }

    internal inner class SetWebViewClientTask : Runnable {

        internal inner class LoadPageStep : Runnable {

            internal inner class PrepareWebStep : Runnable {

                internal inner class RunTransScriptStep : Runnable {
                    override fun run() {
                        val dialogTransLang = this@DialogTransLang
                        if (dialogTransLang.webSafe == null) {
                            return
                        }
                        dialogTransLang.runAsync(dialogTransLang.TransScriptTask())
                    }
                }

                override fun run() {
                    val setWebViewClientTask = this@SetWebViewClientTask
                    val dialogTransLang = this@DialogTransLang
                    val myWebSafe = dialogTransLang.webSafe
                    if (myWebSafe != null) {
                        dialogTransLang.urlLoading = true
                        myWebSafe.addJavascriptInterface(
                            dialogTransLang.WebAppInterface(),
                            "android"
                        )
                        val handler = this@DialogTransLang.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(RunTransScriptStep())
                    }
                }
            }

            override fun run() {
                val dialogTransLang = this@DialogTransLang
                val myWebSafe = dialogTransLang.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(dialogTransLang.LocalWebViewClient())
                    val handler = dialogTransLang.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(PrepareWebStep())
                }
            }
        }

        override fun run() {
            val dialogTransLang = this@DialogTransLang
            val myWebSafe = dialogTransLang.webSafe
            if (myWebSafe != null) {
                val settings = myWebSafe.getSettings()
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(false)
                settings.setJavaScriptEnabled(true)
                myWebSafe.setOverScrollMode(2)
                val handler = dialogTransLang.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadPageStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val dialogTransLang = this@DialogTransLang
            MainUtil.clearWebCache(dialogTransLang.webSafe)
            dialogTransLang.finishHandled = false
        }
    }

    internal inner class DismissTask : Runnable {
        override fun run() {
            this@DialogTransLang.dismiss()
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val dialogTransLang = this@DialogTransLang
            val myWebSafe = dialogTransLang.webSafe
            if (myWebSafe == null) {
                return
            }
            dialogTransLang.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val dialogTransLang = this@DialogTransLang
            val myWebSafe = dialogTransLang.webSafe
            if (myWebSafe == null) {
                return
            }
            dialogTransLang.urlLoading = true
            myWebSafe.addJavascriptInterface(dialogTransLang.WebAppInterface(), "android")
        }
    }

    internal inner class TransScriptTask : Runnable {

        internal inner class EvalTransScriptStep : Runnable {

            internal inner class EvalJsNowTask : Runnable {

                override fun run() {
                    val pendingJs: String?
                    val transScriptTask = this@TransScriptTask
                    val dialogTransLang = this@DialogTransLang
                    if (dialogTransLang.webSafe != null) {
                        val sbK = transScriptJs()
                        if (sbK != null) {
                            val classJs = classSelectorJs(null)
                            if (TextUtils.isEmpty(classJs)) {
                                pendingJs = null
                            } else {
                                sbK.insert(0, classJs)
                                pendingJs = sbK.toString()
                            }
                        } else {
                            pendingJs = null
                        }
                        dialogTransLang.pendingJs = pendingJs
                        val owner = this@DialogTransLang
                        if (!owner.jsReady || TextUtils.isEmpty(owner.pendingJs)) {
                            return
                        }
                        owner.jsReady = false
                        evalJsNow(owner.webSafe, owner.pendingJs, true)
                        owner.pendingJs = null
                    }
                }
            }

            override fun run() {
                val dialogTransLang = this@DialogTransLang
                val pendingHtml = dialogTransLang.pendingHtml
                dialogTransLang.pendingHtml = null
                val myWebSafe: MyWebSafe? = if (TextUtils.isEmpty(pendingHtml)) null else dialogTransLang.webSafe
                if (TextUtils.isEmpty(pendingHtml) || myWebSafe == null) {
                    return
                }
                MainUtil.loadHtml(myWebSafe, dialogTransLang.baseUrl, pendingHtml)
                dialogTransLang.runAsync(EvalJsNowTask())
            }
        }

        override fun run() {
            val dialogTransLang = this@DialogTransLang
            dialogTransLang.pendingHtml =
                "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes'/></head><body><p>test</p></body></html>"
            dialogTransLang.baseUrl = httpsUrl("soul_lang_", null)
            val myWebSafe = dialogTransLang.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(EvalTransScriptStep())
        }
    }

    internal inner class ReleaseWebTask : Runnable {
        override fun run() {
            this@DialogTransLang.releaseWeb()
        }
    }

    internal inner class JsResultTask : Runnable {

        internal inner class ReleaseWebStep : Runnable {
            override fun run() {
                val dialogTransLang = this@DialogTransLang
                dialogTransLang.releaseWeb()
            }
        }

        override fun run() {
            val dialogTransLang = this@DialogTransLang
            val jsResult = dialogTransLang.jsResult
            dialogTransLang.jsResult = null
            if (dialogTransLang.webSafe != null) {
                parseTransResult(dialogTransLang.appContext, jsResult)
                val handler = dialogTransLang.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ReleaseWebStep())
            }
        }
    }

    internal inner class LangPicked : MainLangListener {
        override fun onPick(position: Int, lang: String?) {
            val list: MutableList<MainLangAdapter.MainLangItem?>?
            val dialogTransLang = this@DialogTransLang
            if (dialogTransLang.langListener == null || TextUtils.isEmpty(lang)) {
                return
            }
            if (dialogTransLang.newsMode) {
                if (!ZtwoSettings.newsTitle.value || lang != ZtwoSettings.newsPick.value) {
                    ZtwoSettings.editLocal { set(ZtwoSettings.newsTitle, true) }
                    ZtwoSettings.editLocal { set(ZtwoSettings.newsPick, lang) }
                    val context = dialogTransLang.appContext
                    val prefZtwoR: PrefZtwo? = if (context != null) PrefZtwo.get(context, false) else null
                    if (context != null && prefZtwoR != null) {
                        prefZtwoR.putBool("mNewsTitle", ZtwoSettings.newsTitle.value)
                        prefZtwoR.putString("mNewsPick", ZtwoSettings.newsPick.value)
                        prefZtwoR.saveAsync()
                    }
                }
            } else if (lang != AlbumSettings.transPick.value) {
                AlbumSettings.editLocal { set(AlbumSettings.transPick, lang) }
                AlbumSettings.editLocal { set(AlbumSettings.transCode, "") }
                PrefAlbum.saveTrans(dialogTransLang.appContext)
            }
            val mainLangAdapter = dialogTransLang.langAdapter
            var code: String? = null
            if (mainLangAdapter != null && (((mainLangAdapter.recent.also {
                    list = it
                }) == null || list!!.isEmpty()) && !MainUtil.sameObject(mainLangAdapter.selectedCode, lang))) {
                code = mainLangAdapter.selectedCode
            }
            DbRecentLang.addAsync(dialogTransLang.appContext, dialogTransLang.recentType.toInt(), code, lang)
            dialogTransLang.langListener!!.onLanguage(lang)
        }

        override fun onRemoved() {
            val dialogTransLang = this@DialogTransLang
            dialogTransLang.applyClearMode(dialogTransLang.clearMode)
            dialogTransLang.refreshList()
        }
    }

    internal inner class ApplyClearModeStep : DialogMsgListener {

        internal inner class ClearModeChosenTask : Runnable {

            internal inner class ApplyClearModeTask : Runnable {
                override fun run() {
                    val applyClearModeStep = this@ApplyClearModeStep
                    val dialogTransLang = this@DialogTransLang
                    if (dialogTransLang.langAdapter != null) {
                        dialogTransLang.applyClearMode(dialogTransLang.clearMode)
                        this@DialogTransLang.refreshList()
                        val owner = this@DialogTransLang
                        val dialogSetMsg = owner.msgDialog
                        if (dialogSetMsg != null) {
                            dialogSetMsg.dismiss()
                            owner.msgDialog = null
                        }
                    }
                }
            }

            override fun run() {
                val dialogTransLang = this@DialogTransLang
                val mainLangAdapter = dialogTransLang.langAdapter
                if (mainLangAdapter == null) {
                    return
                }
                mainLangAdapter.clearRecent()
                dialogTransLang.mainHandler!!.post(ApplyClearModeTask())
            }
        }

        override fun onConfirm() {
            val dialogTransLang = this@DialogTransLang
            val dialogSetMsg = dialogTransLang.msgDialog
            if (dialogSetMsg == null) {
                return
            }
            dialogTransLang.msgShown = false
            dialogSetMsg.startBusy()
            dialogTransLang.runAsync(ClearModeChosenTask())
        }

        override fun onHelp() {
            val dialogTransLang = this@DialogTransLang
            val transNotiListener = dialogTransLang.transListener
            if (transNotiListener != null) {
                transNotiListener.onDone()
                return
            }
            if (dialogTransLang.hostActivity == null) {
                return
            }
            dialogTransLang.msgShown = true
            val intent = Intent(dialogTransLang.appContext, com.mycompany.app.ui.settings.TransSettingsActivity::class.java as Class<*>)
            intent.putExtra("EXTRA_NOTI", true)
            intent.putExtra("EXTRA_INDEX", 3)
            dialogTransLang.hostActivity!!.startActivity(intent)
        }
    }

    internal inner class MsgDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogTransLang = this@DialogTransLang
            val dialogSetMsg = dialogTransLang.msgDialog
            if (dialogSetMsg != null) {
                dialogSetMsg.dismiss()
                dialogTransLang.msgDialog = null
            }
            if (dialogTransLang.msgShown) {
                dialogTransLang.msgShown = false
                if (dialogTransLang.langAdapter == null) {
                    return
                }
                dialogTransLang.runAsync(dialogTransLang.ApplyClearModeLaterTask())
            }
        }
    }

    internal inner class ApplyClearModeLaterTask : Runnable {

        internal inner class ApplyClearModeNowStep : Runnable {
            override fun run() {
                val dialogTransLang = this@DialogTransLang
                if (dialogTransLang.langAdapter == null) {
                    return
                }
                dialogTransLang.applyClearMode(dialogTransLang.clearMode)
                this@DialogTransLang.refreshList()
            }
        }

        override fun run() {
            val dialogTransLang = this@DialogTransLang
            val mainLangAdapter = dialogTransLang.langAdapter
            if (mainLangAdapter == null || !mainLangAdapter.trimRecent()) {
                return
            }
            dialogTransLang.mainHandler!!.post(ApplyClearModeNowStep())
        }
    }

    class DialogTask(dialogTransLang: DialogTransLang?, query: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var recent: MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
        @JvmField
        var allItems: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
        @JvmField
        var withFiles: Boolean = false
        @JvmField
        var query: String? = null

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogTransLang)
            this.dialogRef = weakReference
            if ((weakReference.get() as DialogTransLang?) == null) {
                return@init
            }
            this.withFiles = false
            this.query = query

        }
    }

        public override fun doInBackground() {
            val dialog = this.dialogRef!!.get() as DialogTransLang?
            if (dialog == null || this.cancelled || dialog.appContext == null) {
                return
            }
            val all: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>?
            val adapter = dialog.langAdapter
            if (adapter != null) {
                // Adapter already built: reuse its recent list and the full language list.
                this.recent = if (DbRecentLang.limitOf(adapter.recentType.toInt()) != 0) adapter.recent else null
                this.allItems = dialog.allItems
                all = dialog.allItems
            } else {
                val langs = DataTrans.instance(dialog.appContext).languages
                if (langs == null || langs.isEmpty()) {
                    return
                }
                val recent: MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = loadRecent(dialog, langs)
                this.recent = recent
                var list: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = ArrayList()
                val size = langs.size
                for (code in 0..<size) {
                    if (dialog.queryTask == null || dialog.queryTask!!.cancelled) {
                        list = null
                        break
                    }
                    val item = MainLangItem()
                    item.code = code
                    item.lang = langs.get(code)
                    item.tag = item.lang!!.lowercase()
                    if (!TextUtils.isEmpty(dialog.pickedLang) && dialog.pickedLang == item.lang) {
                        dialog.pickedLang = null
                        dialog.selectedLang = item.code
                        dialog.scrollPos = (if (recent != null) recent.size else 0) + code
                    }
                    list!!.add(item)
                }
                this.allItems = list
                dialog.allItems = list
                all = list
            }
            if (all == null || all.isEmpty() || TextUtils.isEmpty(this.query)) {
                return
            }
            // Filter by the search query.
            this.withFiles = true
            this.query = this.query!!.lowercase()
            val initial = dialog.koreanLocale
            val result: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?> = ArrayList()
            for (item2 in this.allItems!!) {
                if (this.cancelled) {
                    return
                }
                val item = item2
                if (item == null || TextUtils.isEmpty(item.lang) || TextUtils.isEmpty(item.tag)) {
                    continue
                }
                val query = this.query
                if (initial) {
                    val match = initialMatch(item.lang, item.tag, query)
                    if (!TextUtils.isEmpty(match)) {
                        item.match = match!!.lowercase()
                        result.add(item)
                        continue
                    }
                }
                if (item.tag!!.contains(query!!)) {
                    item.match = query
                    result.add(item)
                }
            }
            this.allItems = result
        }

        public override fun onCancelled() {
            val dialogTransLang = this.dialogRef!!.get() as DialogTransLang?
            if (dialogTransLang == null) {
                return
            }
            dialogTransLang.queryTask = null
        }

        public override fun onPostExecute() {
            val dialogTransLang = this.dialogRef!!.get() as DialogTransLang?
            if (dialogTransLang != null) {
                dialogTransLang.queryTask = null
                val list = this.recent
                val arrayList = this.allItems
                val withFiles = this.withFiles
                val mainLangAdapter = dialogTransLang.langAdapter
                if (mainLangAdapter != null) {
                    mainLangAdapter.items = arrayList
                    mainLangAdapter.filtered = withFiles
                    dialogTransLang.refreshList()
                    return
                }
                val langAdapter = MainLangAdapter(
                    dialogTransLang.appContext,
                    dialogTransLang.recentType.toInt(),
                    list,
                    arrayList,
                    dialogTransLang.selectedLang,
                    dialogTransLang.LangPicked()
                )
                dialogTransLang.langAdapter = langAdapter
                dialogTransLang.applyClearMode(dialogTransLang.clearMode)
                dialogTransLang.refreshList()
                dialogTransLang.loading = false
                if (dialogTransLang.scrollPos >= 1) dialogTransLang.scrollTo = dialogTransLang.scrollPos
            }
        }

        companion object {
            /**
             * Recently used languages (newest first, limited per type) followed by a divider item,
             * or null when there are none or the task was cancelled.
             */
            private fun loadRecent(
                dialog: DialogTransLang?,
                langs: List<String?>?
            ): MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? {
                val type = dialog!!.recentType.toInt()
                if (DbRecentLang.limitOf(type) == 0) {
                    return null
                }
                var recent: MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        DbRecentLang.get(dialog.appContext).getWritableDatabase(),
                        "DbRecentLang_table",
                        arrayOf<String?>("_id", "_lang"),
                        "_type=?",
                        arrayOf<String?>(type.toString()),
                        "_time DESC"
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val idCol = cursor.getColumnIndex("_id")
                        val langCol = cursor.getColumnIndex("_lang")
                        var limit = DbRecentLang.limitOf(type)
                        if (limit == -1) {
                            limit = Int.MAX_VALUE
                        }
                        var count = 0
                        do {
                            if (dialog.queryTask == null || dialog.queryTask!!.cancelled) {
                                cursor.close()
                                return null
                            }
                            val lang = cursor.getString(langCol)
                            val index = if (!TextUtils.isEmpty(lang)) langs!!.indexOf(lang) else 0
                            if (!TextUtils.isEmpty(lang) && index != -1) {
                                val item = MainLangItem()
                                item.rowType = 1
                                item.id = cursor.getLong(idCol)
                                item.code = index + 1000
                                item.lang = lang
                                item.tag = lang!!.lowercase()
                                if (!TextUtils.isEmpty(dialog.pickedLang) && dialog.pickedLang == item.lang) {
                                    dialog.pickedLang = null
                                    dialog.selectedLang = item.code
                                    dialog.scrollPos = count
                                }
                                if (recent == null) {
                                    recent = ArrayList()
                                }
                                recent.add(item)
                                count++
                            }
                        } while (count < limit && cursor.moveToNext())
                    }
                } catch (unused: Exception) {
                    // keep whatever was read before the failure
                }
                if (cursor != null) {
                    cursor.close()
                }
                if (recent == null || recent.isEmpty() || dialog.queryTask == null || dialog.queryTask!!.cancelled) {
                    return null
                }
                val divider = MainLangItem()
                divider.rowType = 2
                divider.code = -1
                recent.add(divider)
                return recent
            }
        }
    }

    interface TransLangListener {
        fun onLanguage(lang: String?)
    }

    interface TransNotiListener {
        fun onDone()
    }

    private inner class WebAppInterface {

        inner class CheckLanguagesStep : Runnable {
            override fun run() {
                val dialogTransLang = this@DialogTransLang
                val context = dialogTransLang.appContext
                if (context == null || DataTrans.instance(context).hasLanguages()) {
                    return
                }
                injectTransJs(dialogTransLang.webSafe)
            }
        }

        @JavascriptInterface
        fun onJsResult(tag: String?, jsResult: String?) {
            if (TextUtils.isEmpty(tag) || tag != "onTransList") {
                return
            }
            val dialogTransLang = this@DialogTransLang
            if (dialogTransLang.webSafe != null) {
                if (!TextUtils.isEmpty(jsResult)) {
                    dialogTransLang.jsResult = jsResult
                    dialogTransLang.runAsync(dialogTransLang.JsResultTask())
                } else {
                    val handler = dialogTransLang.mainHandler
                    if (handler != null) {
                        handler.post(dialogTransLang.ReleaseWebTask())
                    }
                }
            }
        }

        @JavascriptInterface
        fun onObserDet(innerText: String?, found: Int) {
            val handler: Handler? = if (found == 0) null else this@DialogTransLang.mainHandler
            if (found == 0 || handler == null) {
                return
            }
            handler.post(CheckLanguagesStep())
        }
    }

    fun loadUrl(pageUrl: String?) {
        if (this.webSafe != null) {
            val isHttp = isHttpUrl(pageUrl)
            val urlLoading = this.urlLoading
            if (isHttp) {
                if (urlLoading) {
                    this.urlLoading = false
                    val myWebSafe = this.webSafe
                    if (myWebSafe != null) {
                        myWebSafe.post(RemoveJsBridgeTask())
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
            val myWebSafe = this.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(AddJsBridgeTask())
        }
    }

    fun startQuery(query: String?) {
        val dialogTask = this.queryTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.queryTask = null
        val queryTask = DialogTask(this, query)
        this.queryTask = queryTask
        queryTask.start(this.appContext)
    }

    /** Clear button (search text) or recent-delete button. */
    fun applyClearMode(clear: Boolean) {
        clearMode = clear
        clearShown = clear
        if (clear) {
            deleteShown = false
            return
        }
        val adapter = langAdapter
        var list: MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
        if (adapter != null && DbRecentLang.limitOf(adapter.recentType.toInt()) != 0) list = adapter.recent
        deleteShown = !(list == null || list.isEmpty())
    }

    /** The language names arrived (or failed): drop the WebView and show the list. */
    fun releaseWeb() {
        val web = webSafe
        if (web != null) {
            if (pageLoading) {
                pageLoading = false
                web.stopLoading()
            }
            webHolder?.removeView(web)
            destroyWebViewNow(webSafe)
            webSafe = null
        }
        if (DataTrans.instance(appContext).hasLanguages()) {
            startQuery(null)
        } else {
            loading = false
            showToast(appContext, AppR.string.fail)
        }
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        msgDialog?.dismiss()
        msgDialog = null
        queryTask?.cancelled = true
        queryTask = null
        val web = webSafe
        if (web != null) {
            if (pageLoading) {
                pageLoading = false
                web.stopLoading()
            }
            webHolder?.removeView(web)
            destroyWebViewNow(webSafe)
            webSafe = null
        }
        val adapter = langAdapter
        if (adapter != null) {
            adapter.lastCount = adapter.getItemCount()
            adapter.ctx = null
            adapter.recent = null
            adapter.items = null
            adapter.selectedCode = null
            adapter.listener = null
            clearHandler(adapter.mainHandler)
            adapter.mainHandler = null
            langAdapter = null
        }
        hostActivity = null
        appContext = null
        langListener = null
        transListener = null
        pickedLang = null
        pendingJs = null
        baseUrl = null
        allItems = null
        super.dismiss()
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogTransLang = this@DialogTransLang
            if (dialogTransLang.webSafe != null) {
                dialogTransLang.pageLoading = false
                dialogTransLang.loadUrl(url)
                if (!dialogTransLang.finishHandled && !TextUtils.isEmpty(url) && (url != dialogTransLang.lastFinishedUrl)) {
                    dialogTransLang.finishHandled = true
                    dialogTransLang.lastFinishedUrl = url
                    val handler = dialogTransLang.mainHandler
                    if (handler != null) {
                        handler.post(dialogTransLang.ClearCacheTask())
                    }
                }
                dialogTransLang.jsReady = true
                if (TextUtils.isEmpty(dialogTransLang.pendingJs)) {
                    return
                }
                dialogTransLang.jsReady = false
                evalJsNow(dialogTransLang.webSafe, dialogTransLang.pendingJs, true)
                dialogTransLang.pendingJs = null
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogTransLang = this@DialogTransLang
            if (dialogTransLang.webSafe == null) {
                return
            }
            dialogTransLang.pageLoading = true
            dialogTransLang.loadUrl(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val dialogTransLang = this@DialogTransLang
            dialogTransLang.webSafe = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogTransLang.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(dialogTransLang.DismissTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val dialogTransLang = this@DialogTransLang
            if (dialogTransLang.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                dialogTransLang.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val dialogTransLang = this@DialogTransLang
            if (dialogTransLang.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    dialogTransLang.loadUrl(url)
                }
            }
            return false
        }
    }

    companion object {
    }
}
