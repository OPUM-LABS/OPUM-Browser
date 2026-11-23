package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.PrefSet
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
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
import com.mycompany.app.db.book.DbBookLocale
import com.mycompany.app.db.book.DbRecentLang
import com.mycompany.app.db.book.DbRecentLang.Companion.addAsync
import com.mycompany.app.dialog.DialogSetMsg.DialogMsgListener
import com.mycompany.app.dialog.DialogTransLang.TransNotiListener
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainLangAdapter
import com.mycompany.app.main.MainLangAdapter.MainLangItem
import com.mycompany.app.main.MainLangAdapter.MainLangListener
import com.mycompany.app.main.MainSelectAdapter.MainSelectListener
import com.mycompany.app.main.MainTransLocale
import com.mycompany.app.main.MainTransLocale.TransLocaleListener
import com.mycompany.app.main.MainUri.isFile
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.parseTransResult
import com.mycompany.app.main.MainUtil.httpsUrl
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefAlbum.Companion.get
import com.mycompany.app.pref.PrefFloat.Companion.get
import com.mycompany.app.pref.PrefImage.Companion.get
import com.mycompany.app.pref.PrefList.Companion.get
import com.mycompany.app.pref.PrefMain.Companion.get
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefRead.Companion.get
import com.mycompany.app.pref.PrefSecret.Companion.get
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefTts.Companion.get
import com.mycompany.app.pref.PrefVideo.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.pref.PrefZone.Companion.get
import com.mycompany.app.pref.PrefZtri.Companion.get
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZtwo.Companion.get
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.DialogFrame
import com.mycompany.app.view.MyWebSafe
import java.lang.ref.WeakReference
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
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
 * News region / language picker ([strArr] the region names, default `news_lang`): search field (also by Korean
 * initials) with a clear button, recent regions (each removable; a delete button clears them, after asking) above all
 * regions, the current one marked; picking one records it as recent and calls [MainSelectListener.onPick]. The translate
 * button (one-time dot `mLocNoti`) toggles translated names under each region (`mLocTrans`, names from [DbBookLocale]
 * or fetched by [MainTransLocale], with the Google Translate badge). When the language names are not cached yet
 * ([DataTrans]) they are fetched once through a hidden WebView with the old JS bridge. The list model
 * ([MainLangAdapter] as data), the search task and the fetches are the old code. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogNewsLocale(
    mainActivity: android.app.Activity?,
    strArr: Array<String?>?,
    mainSelectListener: MainSelectListener?
) : ComposeBottomDialog(mainActivity) {
    @JvmField var webSafe: MyWebSafe? = null
    @JvmField var pageLoading: Boolean = false
    @JvmField var jsReady: Boolean = false
    @JvmField var urlLoading: Boolean = false
    @JvmField var pendingJs: String? = null
    @JvmField var localeTag: String? = null
    @JvmField var baseUrl: String? = null
    @JvmField var initialLocale: Boolean = false
    @JvmField var msgDialog: DialogSetMsg? = null
    @JvmField var msgShown: Boolean = false
    @JvmField var finishHandled: Boolean = false
    @JvmField var lastFinishedUrl: String? = null
    @JvmField var pendingHtml: String? = null
    @JvmField var jsResult: String? = null
    @JvmField var hostActivity: android.app.Activity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var selectListener: MainSelectListener? = mainSelectListener
    @JvmField var transListener: TransNotiListener? = null
    @JvmField var langCodes: Array<String?>? = strArr
    @JvmField var selectedLang: Int = ZtwoSettings.newsLang.value
    @JvmField var pendingScroll: Int = -1
    /** Hidden holder of the name-fetch WebView and the [MainTransLocale] page (inside the sheet, invisible). */
    @JvmField var dialogFrame: DialogFrame? = null
    @JvmField var queryTask: DialogTask? = null
    @JvmField var shownItems: MutableList<MainLangAdapter.MainLangItem?>? = null
    @JvmField var allItems: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
    @JvmField var langAdapter: MainLangAdapter? = null
    @JvmField var clearMode: Boolean = false
    @JvmField var queryActive: Boolean = false
    @JvmField var transLocale: MainTransLocale? = null
    @JvmField var dbItems: ArrayList<String?>? = null
    @JvmField var reloadPending: Boolean = false

    @JvmField val mainHandler: Handler? = Handler(android.os.Looper.getMainLooper())

    private var query by mutableStateOf("")
    private var clearShown by mutableStateOf(false)
    private var deleteShown by mutableStateOf(false)
    private var transShown by mutableStateOf(false)
    private var transBusy by mutableStateOf(false)
    private var transOn by mutableStateOf(ZtwoSettings.locTrans.value)
    private var logoShown by mutableStateOf(false)
    internal var transNoti by mutableStateOf(ZtwoSettings.locNoti.value)
    internal var loading by mutableStateOf(true)
    private var version by mutableIntStateOf(0)
    internal var scrollTo by mutableIntStateOf(-1)

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
            // the selected region's name (old row binding set it while drawing)
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
                            IconButton(onClick = { clearQuery() }) {
                                Icon(painterResource(AppR.drawable.outline_cancel_black_18), contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                if (deleteShown) {
                    IconButton(onClick = { ClearModeClick().onClick(null) }) {
                        Icon(painterResource(AppR.drawable.outline_delete_black_20), contentDescription = stringResource(AppR.string.delete))
                    }
                }
                if (transShown) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        if (transBusy) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { RefreshTransClick().onClick(null) }) {
                                if (transOn) {
                                    Image(painterResource(AppR.drawable.outline_g_translate_color_18), contentDescription = null)
                                } else {
                                    Icon(painterResource(AppR.drawable.outline_g_translate_black_18), contentDescription = null)
                                }
                            }
                            if (transNoti) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp).size(8.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
            Box(Modifier.weight(1f, fill = false).heightIn(min = 72.dp)) {
                AndroidView(
                    factory = { ctx -> DialogFrame(ctx).also { dialogFrame = it } },
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
                if (logoShown) {
                    Image(
                        painterResource(if (AppState.darkUi) AppR.drawable.trans_logo_short_back_dark else AppR.drawable.trans_logo_short_back_color),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(width = 130.dp, height = 24.dp),
                    )
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
        val match = item.match
        // old row: the current region's text is coloured (recent rows too); the check mark only in the full list
        val current = item.code == adapter.sel || item.code == adapter.sel - 1000
        val selected = item.rowType.toInt() == 0 && current
        val highlight = MaterialTheme.colorScheme.error
        fun marked(name: String) = if (adapter.filtered && !match.isNullOrEmpty()) {
            buildAnnotatedString {
                val at = name.lowercase().indexOf(match)
                if (at < 0) append(name) else {
                    append(name.substring(0, at))
                    withStyle(SpanStyle(color = highlight)) { append(name.substring(at, at + match.length)) }
                    append(name.substring(at + match.length))
                }
            }
        } else buildAnnotatedString { append(name) }
        val color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        val translated = item.translated
        ListItem(
            headlineContent = { Text(marked(item.lang ?: ""), color = color) },
            supportingContent = if (adapter.showTranslated && !translated.isNullOrEmpty()) {
                { Text(marked(translated), color = if (current) color else MaterialTheme.colorScheme.onSurfaceVariant) }
            } else null,
            trailingContent = {
                if (item.rowType.toInt() == 1 && !adapter.filtered) {
                    IconButton(onClick = { adapter.removeRecent(item) }) {
                        Icon(painterResource(AppR.drawable.outline_close_black_18), contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                } else if (selected && !adapter.filtered) {
                    Icon(painterResource(AppR.drawable.outline_check_black_24), contentDescription = null, tint = color)
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = if (selected && !adapter.filtered) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier.clickable {
                var code = item.code
                if (code >= 1000) code -= 1000
                adapter.listener?.onPick(code, item.lang)
            },
        )
    }

    /** Search text changed. */
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
        if (!empty || queryActive) {
            queryTask?.cancelled = true
            queryTask = null
            val task = DialogTask(this, queryText)
            queryTask = task
            task.start(appContext)
        }
        queryActive = !empty
    }

    /** Clear button. */
    private fun clearQuery() {
        applyClearMode(false)
        query = ""
        queryActive = false
        queryTask?.cancelled = true
        queryTask = null
        val task = DialogTask(this, null)
        queryTask = task
        task.start(appContext)
    }

    /** the list task, then (once) the hidden name fetch. */
    private fun start() {
        if (appContext == null) return
        if (dialogFrame == null) {
            mainHandler?.postDelayed({ start() }, 100)
            return
        }
        refreshTrans()
        mainHandler?.post(StartTask())
    }

    /** Search mode on / off: shows the clear button or the delete / translate buttons. */
    fun applyClearMode(clear: Boolean) {
        clearMode = clear
        clearShown = clear
        if (clear) {
            deleteShown = false
            transShown = false
            transBusy = false
        } else {
            val adapter = langAdapter
            var list: MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
            if (adapter != null && DbRecentLang.limitOf(adapter.recentType.toInt()) != 0) list = adapter.recent
            deleteShown = !(list == null || list.isEmpty())
            transShown = true
            transBusy = transLocale != null
        }
        version++
    }

    /** Scrolls to the selected region once the list is there. */
    fun scrollPending() {
        val pendingScrollNow = pendingScroll
        pendingScroll = -1
        if (pendingScrollNow < 1 || clearMode) return
        mainHandler?.post(RefreshListStep(this, pendingScrollNow))
    }

    /** Translate button state and the Google Translate badge. */
    fun refreshTrans() {
        transOn = ZtwoSettings.locTrans.value
        applyClearMode(clearMode)
        logoShown = ZtwoSettings.locTrans.value && transLocale == null
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        transLocale?.release()
        transLocale = null
        msgDialog?.dismiss()
        msgDialog = null
        queryTask?.cancelled = true
        queryTask = null
        val mainLangAdapter = langAdapter
        if (mainLangAdapter != null) {
            mainLangAdapter.lastCount = mainLangAdapter.getItemCount()
            mainLangAdapter.ctx = null
            mainLangAdapter.recent = null
            mainLangAdapter.items = null
            mainLangAdapter.selectedCode = null
            mainLangAdapter.listener = null
            clearHandler(mainLangAdapter.mainHandler)
            mainLangAdapter.mainHandler = null
            langAdapter = null
        }
        val myWebSafe = webSafe
        if (myWebSafe != null) {
            if (pageLoading) {
                pageLoading = false
                myWebSafe.stopLoading()
            }
            destroyWebViewNow(webSafe)
            webSafe = null
        }
        dialogFrame = null
        hostActivity = null
        appContext = null
        selectListener = null
        transListener = null
        langCodes = null
        shownItems = null
        allItems = null
        dbItems = null
        pendingJs = null
        localeTag = null
        baseUrl = null
        super.dismiss()
    }

    internal inner class StartTask : Runnable {
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            val dialogTask = dialogNewsLocale.queryTask
            if (dialogTask != null) {
                dialogTask.cancelled = true
            }
            dialogNewsLocale.queryTask = null
            val queryTask = DialogNewsLocale.DialogTask(dialogNewsLocale, null)
            dialogNewsLocale.queryTask = queryTask
            queryTask.start(dialogNewsLocale.appContext)
            if (DataTrans.instance(dialogNewsLocale.appContext)
                    .hasLanguages() || dialogNewsLocale.webSafe != null || dialogNewsLocale.dialogFrame == null
            ) {
                return
            }
            val myWebSafe = MyWebSafe(dialogNewsLocale.hostActivity!!)
            dialogNewsLocale.webSafe = myWebSafe
            MainApp.resumeWebTimers(dialogNewsLocale.appContext, myWebSafe)
            dialogNewsLocale.webSafe!!.setVisibility(View.INVISIBLE)
            dialogNewsLocale.dialogFrame!!.addView(dialogNewsLocale.webSafe, 0, ViewGroup.LayoutParams(-1, -1))
            val handler = dialogNewsLocale.mainHandler
            if (handler != null) {
                handler.post(dialogNewsLocale.LoadLangListTask())
            }
        }
    }
    internal inner class LangPicked : MainLangListener {
        override fun onPick(position: Int, lang: String?) {
            val list: MutableList<MainLangAdapter.MainLangItem?>?
            val dialogNewsLocale = this@DialogNewsLocale
            if (dialogNewsLocale.selectListener == null || TextUtils.isEmpty(lang)) {
                return
            }
            val mainLangAdapter = dialogNewsLocale.langAdapter
            var code: String? = null
            if (mainLangAdapter != null && (((mainLangAdapter.recent.also {
                    list = it
                }) == null || list!!.isEmpty()) && !MainUtil.sameObject(mainLangAdapter.selectedCode, lang))) {
                code = mainLangAdapter.selectedCode
            }
            DbRecentLang.addAsync(dialogNewsLocale.appContext, 0, code, lang)
            dialogNewsLocale.selectListener!!.onPick(position)
        }
        override fun onRemoved() {
            val dialogNewsLocale = this@DialogNewsLocale
            dialogNewsLocale.applyClearMode(dialogNewsLocale.clearMode)
        }
    }
    internal inner class RefreshListStep(@JvmField val outer: DialogNewsLocale,
                                          @JvmField val position: Int
    ) : Runnable {
        override fun run() {
            val dialogNewsLocale = this.outer
            if (dialogNewsLocale.langAdapter == null || dialogNewsLocale.clearMode) return
            dialogNewsLocale.scrollTo = this.position
        }
    }
    internal inner class RefreshTransListTask : Runnable {
        internal inner class RefreshTransStep : Runnable {
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                if (dialogNewsLocale.appContext == null) {
                    return
                }
                dialogNewsLocale.refreshTrans()
                val mainLangAdapter = dialogNewsLocale.langAdapter
                if (mainLangAdapter != null) {
                    val showTranslated = ZtwoSettings.locTrans.value
                    val clearMode = dialogNewsLocale.clearMode
                    mainLangAdapter.showTranslated = showTranslated
                    if (!clearMode) {
                        this@DialogNewsLocale.refreshList()
                    }
                }
                dialogNewsLocale.scrollPending()
                dialogNewsLocale.reloadPending = false
            }
        }
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            dialogNewsLocale.ensureLocale()
            val arrayList = dialogNewsLocale.dbItems
            if (arrayList == null || arrayList.isEmpty()) {
                dialogNewsLocale.dbItems = DbBookLocale.tagsOf(dialogNewsLocale.appContext, dialogNewsLocale.localeTag)
            }
            val dbItems = dialogNewsLocale.dbItems
            if (dbItems == null || dbItems.isEmpty()) {
                dialogNewsLocale.startTranslate()
                dialogNewsLocale.reloadPending = false
                return
            }
            dialogNewsLocale.applyTranslations()
            val handler = dialogNewsLocale.mainHandler
            if (handler == null) {
                return
            }
            handler.post(RefreshTransStep())
        }
    }
    internal inner class ReleaseStep : TransLocaleListener {
        override fun onNames(arrayList: ArrayList<String?>?) {
            val dialogNewsLocale = this@DialogNewsLocale
            val mainTransLocale = dialogNewsLocale.transLocale
            if (mainTransLocale == null) {
                return
            }
            mainTransLocale.release()
            dialogNewsLocale.transLocale = null
            dialogNewsLocale.dbItems = arrayList
            dialogNewsLocale.refreshTrans()
            val mainLangAdapter = dialogNewsLocale.langAdapter
            if (mainLangAdapter != null) {
                val showTranslated = ZtwoSettings.locTrans.value
                val clearMode = dialogNewsLocale.clearMode
                mainLangAdapter.showTranslated = showTranslated
                if (!clearMode) {
                    this@DialogNewsLocale.refreshList()
                }
            }
            dialogNewsLocale.scrollPending()
        }
    }
    internal inner class RefreshTransTask : Runnable {
        override fun run() {
            this@DialogNewsLocale.refreshTrans()
        }
    }
    internal inner class LoadLangListTask : Runnable {
        internal inner class ShowLangListStep : Runnable {
            internal inner class PrepareWebStep : Runnable {
                internal inner class RunTransScriptStep : Runnable {
                    override fun run() {
                        val dialogNewsLocale = this@DialogNewsLocale
                        if (dialogNewsLocale.webSafe == null) {
                            return
                        }
                        dialogNewsLocale.runAsync(dialogNewsLocale.TransScriptTask())
                    }
                }
                override fun run() {
                    val loadLangListTask = this@LoadLangListTask
                    val dialogNewsLocale = this@DialogNewsLocale
                    val myWebSafe = dialogNewsLocale.webSafe
                    if (myWebSafe != null) {
                        dialogNewsLocale.urlLoading = true
                        myWebSafe.addJavascriptInterface(
                            dialogNewsLocale.WebAppInterface(),
                            "android"
                        )
                        val handler = this@DialogNewsLocale.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(RunTransScriptStep())
                    }
                }
            }
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                val myWebSafe = dialogNewsLocale.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(dialogNewsLocale.LocalWebViewClient())
                    val handler = dialogNewsLocale.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(PrepareWebStep())
                }
            }
        }
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            val myWebSafe = dialogNewsLocale.webSafe
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
                val handler = dialogNewsLocale.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ShowLangListStep())
            }
        }
    }
    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            MainUtil.clearWebCache(dialogNewsLocale.webSafe)
            dialogNewsLocale.finishHandled = false
        }
    }
    internal inner class DismissTask : Runnable {
        override fun run() {
            this@DialogNewsLocale.dismiss()
        }
    }
    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            val myWebSafe = dialogNewsLocale.webSafe
            if (myWebSafe == null) {
                return
            }
            dialogNewsLocale.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }


    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            val myWebSafe = dialogNewsLocale.webSafe
            if (myWebSafe == null) {
                return
            }
            dialogNewsLocale.urlLoading = true
            myWebSafe.addJavascriptInterface(dialogNewsLocale.WebAppInterface(), "android")
        }
    }
    internal inner class TransScriptTask : Runnable {
        internal inner class EvalTransScriptStep : Runnable {
            internal inner class EvalJsNowTask : Runnable {
                override fun run() {
                    val pendingJs: String?
                    val transScriptTask = this@TransScriptTask
                    val dialogNewsLocale = this@DialogNewsLocale
                    if (dialogNewsLocale.webSafe != null) {
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
                        dialogNewsLocale.pendingJs = pendingJs
                        val owner = this@DialogNewsLocale
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
                val dialogNewsLocale = this@DialogNewsLocale
                val pendingHtml = dialogNewsLocale.pendingHtml
                dialogNewsLocale.pendingHtml = null
                val myWebSafe: MyWebSafe? = if (TextUtils.isEmpty(pendingHtml)) null else dialogNewsLocale.webSafe
                if (TextUtils.isEmpty(pendingHtml) || myWebSafe == null) {
                    return
                }
                MainUtil.loadHtml(myWebSafe, dialogNewsLocale.baseUrl, pendingHtml)
                dialogNewsLocale.runAsync(EvalJsNowTask())
            }
        }
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            dialogNewsLocale.ensureLocale()
            dialogNewsLocale.pendingHtml =
                "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes'/></head><body><p>test</p></body></html>"
            dialogNewsLocale.baseUrl = httpsUrl("soul_lang_", dialogNewsLocale.localeTag)
            val myWebSafe = dialogNewsLocale.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(EvalTransScriptStep())
        }
    }
    internal inner class DestroyWebViewTask : Runnable {
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            val myWebSafe: MyWebSafe? = if (dialogNewsLocale.dialogFrame == null) null else dialogNewsLocale.webSafe
            if (dialogNewsLocale.dialogFrame == null || myWebSafe == null) {
                return
            }
            if (dialogNewsLocale.pageLoading) {
                dialogNewsLocale.pageLoading = false
                myWebSafe.stopLoading()
            }
            destroyWebViewNow(dialogNewsLocale.webSafe)
            dialogNewsLocale.webSafe = null
        }
    }
    internal inner class JsResultTask : Runnable {
        internal inner class JsResultStep : Runnable {
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                val myWebSafe: MyWebSafe? = if (dialogNewsLocale.dialogFrame == null) null else dialogNewsLocale.webSafe
                if (dialogNewsLocale.dialogFrame == null || myWebSafe == null) {
                    return
                }
                if (dialogNewsLocale.pageLoading) {
                    dialogNewsLocale.pageLoading = false
                    myWebSafe.stopLoading()
                }
                destroyWebViewNow(dialogNewsLocale.webSafe)
                dialogNewsLocale.webSafe = null
            }
        }
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            val jsResult = dialogNewsLocale.jsResult
            dialogNewsLocale.jsResult = null
            if (dialogNewsLocale.webSafe != null) {
                parseTransResult(dialogNewsLocale.appContext, jsResult)
                val handler = dialogNewsLocale.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(JsResultStep())
            }
        }
    }
    internal inner class ClearModeMsgListener : DialogMsgListener {
        internal inner class ClearModeChosenTask : Runnable {
            internal inner class ApplyClearModeTask : Runnable {
                override fun run() {
                    val clearModeMsgListener = this@ClearModeMsgListener
                    val dialogNewsLocale = this@DialogNewsLocale
                    if (dialogNewsLocale.langAdapter != null) {
                        dialogNewsLocale.applyClearMode(dialogNewsLocale.clearMode)
                        this@DialogNewsLocale.refreshList()
                        val owner = this@DialogNewsLocale
                        val dialogSetMsg = owner.msgDialog
                        if (dialogSetMsg != null) {
                            dialogSetMsg.dismiss()
                            owner.msgDialog = null
                        }
                    }
                }
            }
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                val mainLangAdapter = dialogNewsLocale.langAdapter
                if (mainLangAdapter == null) {
                    return
                }
                mainLangAdapter.clearRecent()
                dialogNewsLocale.mainHandler!!.post(ApplyClearModeTask())
            }
        }
        override fun onConfirm() {
            val dialogNewsLocale = this@DialogNewsLocale
            val dialogSetMsg = dialogNewsLocale.msgDialog
            if (dialogSetMsg == null) {
                return
            }
            dialogNewsLocale.msgShown = false
            dialogSetMsg.startBusy()
            dialogNewsLocale.runAsync(ClearModeChosenTask())
        }
        override fun onHelp() {
            val dialogNewsLocale = this@DialogNewsLocale
            val transNotiListener = dialogNewsLocale.transListener
            if (transNotiListener != null) {
                transNotiListener.onDone()
                return
            }
            if (dialogNewsLocale.hostActivity == null) {
                return
            }
            dialogNewsLocale.msgShown = true
            val intent = Intent(dialogNewsLocale.appContext, com.mycompany.app.ui.settings.NewsSettingsActivity::class.java as Class<*>)
            intent.putExtra("EXTRA_NOTI", true)
            intent.putExtra("EXTRA_INDEX", 6)
            dialogNewsLocale.hostActivity!!.startActivity(intent)
        }
    }
    internal inner class MsgDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogNewsLocale = this@DialogNewsLocale
            val dialogSetMsg = dialogNewsLocale.msgDialog
            if (dialogSetMsg != null) {
                dialogSetMsg.dismiss()
                dialogNewsLocale.msgDialog = null
            }
            if (dialogNewsLocale.msgShown) {
                dialogNewsLocale.msgShown = false
                if (dialogNewsLocale.langAdapter == null) {
                    return
                }
                dialogNewsLocale.runAsync(dialogNewsLocale.ApplyClearModeLaterTask())
            }
        }
    }
    internal inner class ApplyClearModeLaterTask : Runnable {
        internal inner class ApplyClearModeStep : Runnable {
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                if (dialogNewsLocale.langAdapter == null) {
                    return
                }
                dialogNewsLocale.applyClearMode(dialogNewsLocale.clearMode)
                this@DialogNewsLocale.refreshList()
            }
        }
        override fun run() {
            val dialogNewsLocale = this@DialogNewsLocale
            val mainLangAdapter = dialogNewsLocale.langAdapter
            if (mainLangAdapter == null || !mainLangAdapter.trimRecent()) {
                return
            }
            dialogNewsLocale.mainHandler!!.post(ApplyClearModeStep())
        }
    }


    internal inner class ClearModeClick : View.OnClickListener {
        internal inner class ShowClearModeMsgStep : Runnable {
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                val dialogSetMsg: DialogSetMsg? = if (dialogNewsLocale.hostActivity == null) null else dialogNewsLocale.msgDialog
                if (dialogNewsLocale.hostActivity == null || dialogSetMsg != null) {
                    return
                }
                dialogNewsLocale.msgShown = false
                val msgDialog = DialogSetMsg(
                    dialogNewsLocale.hostActivity,
                    true,
                    AppR.string.lang_delete,
                    AppR.string.delete,
                    0,
                    dialogNewsLocale.ClearModeMsgListener()
                )
                dialogNewsLocale.msgDialog = msgDialog
                msgDialog.setOnDismissListener(dialogNewsLocale.MsgDialogDismiss())
            }
        }
        override fun onClick(view: View?) {
            this@DialogNewsLocale.mainHandler?.post(ShowClearModeMsgStep())
        }
    }
    internal inner class RefreshTransClick : View.OnClickListener {
        internal inner class RefreshTransClickStep : Runnable {
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                if (ZtwoSettings.locNoti.value) {
                    ZtwoSettings.edit(dialogNewsLocale.appContext) { set(ZtwoSettings.locNoti, false) }
                    dialogNewsLocale.transNoti = false
                }
                val locTrans = !ZtwoSettings.locTrans.value
                ZtwoSettings.edit(dialogNewsLocale.appContext) { set(ZtwoSettings.locTrans, locTrans) }
                if (ZtwoSettings.locTrans.value) {
                    dialogNewsLocale.runAsync(dialogNewsLocale.RefreshTransListTask())
                    return
                }
                dialogNewsLocale.refreshTrans()
                val mainLangAdapter = dialogNewsLocale.langAdapter
                if (mainLangAdapter != null) {
                    val showTranslated = ZtwoSettings.locTrans.value
                    val clearMode = dialogNewsLocale.clearMode
                    mainLangAdapter.showTranslated = showTranslated
                    if (!clearMode) {
                        this@DialogNewsLocale.refreshList()
                    }
                }
                dialogNewsLocale.reloadPending = false
            }
        }
        override fun onClick(view: View?) {
            val dialogNewsLocale = this@DialogNewsLocale
            val arrayList = dialogNewsLocale.allItems
            if (arrayList == null || arrayList.isEmpty() || dialogNewsLocale.appContext == null) {
                return
            }
            val mainTransLocale = dialogNewsLocale.transLocale
            val context = dialogNewsLocale.appContext
            if (mainTransLocale != null) {
                showToast(context, AppR.string.wait_retry)
                return
            }
            if (!DataTrans.instance(context).hasLanguages()) {
                showToast(dialogNewsLocale.appContext, AppR.string.wait_retry)
            } else {
                if (dialogNewsLocale.reloadPending) {
                    return
                }
                dialogNewsLocale.reloadPending = true
                dialogNewsLocale.mainHandler?.post(RefreshTransClickStep())
            }
        }
    }


    class DialogTask(dialogNewsLocale: DialogNewsLocale?, query: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var recent: MutableList<MainLangAdapter.MainLangItem?>? = null
        @JvmField
        var allItems: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
        @JvmField
        var withFiles: Boolean = false
        @JvmField
        var query: String? = null

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogNewsLocale)
            this.dialogRef = weakReference
            if ((weakReference.get() as DialogNewsLocale?) == null) {
                return@init
            }
            this.withFiles = false
            this.query = query
        
        }
    }

        public override fun doInBackground() {
            val d = this.dialogRef!!.get() as DialogNewsLocale?
            if (d == null || this.cancelled || d.appContext == null) {
                return
            }
            val ctx = d.appContext
            val adapter = d.langAdapter
            val all: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>?
            if (adapter != null) {
                this.recent = if (DbRecentLang.limitOf(adapter.recentType.toInt()) != 0) adapter.recent else null
                this.allItems = d.allItems
                all = this.allItems
            } else {
                var langs = d.langCodes
                if (langs == null) {
                    langs = ctx!!.getResources().getStringArray(AppR.array.news_lang)
                }
                if (langs.size == 0) {
                    return
                }
                var recent: MutableList<MainLangAdapter.MainLangItem?>? = null
                var cancelled = false
                if (DbRecentLang.limitOf(0) != 0) {
                    var cursor: Cursor? = null
                    try {
                        cursor = DbUtil.query(
                            DbRecentLang.get(ctx).getWritableDatabase(),
                            "DbRecentLang_table",
                            arrayOf<String?>("_id", "_lang"),
                            "_type=?",
                            arrayOf<String?>("0"),
                            "_time DESC"
                        )
                        if (cursor != null && cursor.moveToFirst()) {
                            val idCol = cursor.getColumnIndex("_id")
                            val langCol = cursor.getColumnIndex("_lang")
                            var limit = DbRecentLang.limitOf(0)
                            if (limit == -1) {
                                limit = Int.MAX_VALUE
                            }
                            var count = 0
                            while (true) {
                                if (d.queryTask == null || d.queryTask!!.cancelled) {
                                    cancelled = true
                                    break
                                }
                                val lang = cursor.getString(langCol)
                                if (!TextUtils.isEmpty(lang)) {
                                    var idx = -1
                                    for (idx2 in langs.indices) {
                                        if (lang == langs[idx2]) {
                                            idx = idx2
                                            break
                                        }
                                    }
                                    if (idx != -1) {
                                        val item = MainLangItem()
                                        item.rowType = 1
                                        item.id = cursor.getLong(idCol)
                                        item.code = idx + 1000
                                        item.lang = lang
                                        item.tag = lang!!.lowercase()
                                        if (d.selectedLang == idx) {
                                            d.selectedLang = item.code
                                            d.pendingScroll = count
                                        }
                                        if (recent == null) {
                                            recent = ArrayList()
                                        }
                                        recent.add(item)
                                        count++
                                    }
                                }
                                if (count >= limit || !cursor.moveToNext()) {
                                    break
                                }
                            }
                        } else {
                            recent = null
                        }
                    } catch (unused: Exception) {
                    }
                    if (cursor != null) {
                        try {
                            cursor.close()
                        } catch (unused: Exception) {
                        }
                    }
                }
                if (!cancelled && recent != null && !recent.isEmpty() && d.queryTask != null && !d.queryTask!!.cancelled) {
                    val sep = MainLangItem()
                    sep.rowType = 2
                    sep.code = -1
                    recent.add(sep)
                } else {
                    recent = null
                }
                this.recent = recent
                var list: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = ArrayList()
                for (idx2 in langs.indices) {
                    if (d.queryTask == null || d.queryTask!!.cancelled) {
                        list = null
                        break
                    }
                    val item = MainLangItem()
                    item.code = idx2
                    item.lang = langs[idx2]
                    item.tag = langs[idx2]!!.lowercase()
                    if (d.selectedLang == item.code) {
                        d.pendingScroll = (if (recent != null) recent.size else 0) + idx2
                    }
                    list!!.add(item)
                }
                this.allItems = list
                d.allItems = list
                all = list
            }
            if (all == null || all.isEmpty()) {
                return
            }
            d.shownItems = this.recent
            d.ensureLocale()
            if (ZtwoSettings.locTrans.value) {
                if (d.dbItems == null || d.dbItems!!.isEmpty()) {
                    d.dbItems = DbBookLocale.tagsOf(d.appContext, d.localeTag)
                }
                if (d.dbItems == null || d.dbItems!!.isEmpty()) {
                    d.startTranslate()
                } else {
                    d.applyTranslations()
                }
            }
            if (TextUtils.isEmpty(this.query)) {
                return
            }
            this.withFiles = true
            this.query = this.query!!.lowercase()
            val initial = d.initialLocale
            val out: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?> = ArrayList()
            val src = this.allItems
            val size = src!!.size
            for (idx2 in 0..<size) {
                val item = src.get(idx2)
                if (this.cancelled) {
                    return
                }
                if (item == null || TextUtils.isEmpty(item.lang) || TextUtils.isEmpty(item.tag)) {
                    continue
                }
                val query = this.query
                if (Companion.matchInto(item, item.lang, item.tag!!, query, initial)) {
                    out.add(item)
                } else if (ZtwoSettings.locTrans.value && !TextUtils.isEmpty(item.lowerTrans) && Companion.matchInto(
                        item,
                        item.translated,
                        item.lowerTrans!!,
                        query,
                        initial
                    )
                ) {
                    out.add(item)
                }
            }
            this.allItems = out
        }
        public override fun onCancelled() {
            val dialogNewsLocale = this.dialogRef!!.get() as DialogNewsLocale?
            if (dialogNewsLocale == null) {
                return
            }
            dialogNewsLocale.queryTask = null
        }
        public override fun onPostExecute() {
            val dialogNewsLocale = this.dialogRef!!.get() as DialogNewsLocale?
            if (dialogNewsLocale != null) {
                dialogNewsLocale.queryTask = null
                val list = this.recent
                val arrayList = this.allItems
                val withFiles = this.withFiles
                if (dialogNewsLocale.appContext == null) {
                    return
                }
                val mainLangAdapter = dialogNewsLocale.langAdapter
                if (mainLangAdapter != null) {
                    mainLangAdapter.items = arrayList
                    mainLangAdapter.filtered = withFiles
                    dialogNewsLocale.refreshList()
                    dialogNewsLocale.loading = false
                    dialogNewsLocale.refreshList()
                    return
                }
                val langAdapter = MainLangAdapter(
                    dialogNewsLocale.appContext,
                    0,
                    list,
                    arrayList,
                    dialogNewsLocale.selectedLang,
                    dialogNewsLocale.LangPicked()
                )
                dialogNewsLocale.langAdapter = langAdapter
                langAdapter.showTranslated = ZtwoSettings.locTrans.value
                dialogNewsLocale.applyClearMode(dialogNewsLocale.clearMode)
                dialogNewsLocale.scrollPending()
                dialogNewsLocale.loading = false
                dialogNewsLocale.refreshList()
            }
        }

        companion object {
            private fun matchInto(
                item: MainLangItem?,
                name: String?,
                lower: String?,
                match: String?,
                initial: Boolean
            ): Boolean {
                if (initial) {
                    val hit = initialMatch(name, lower, match)
                    if (!TextUtils.isEmpty(hit)) {
                        item!!.match = hit!!.lowercase()
                        return true
                    }
                }
                if (lower!!.contains(match!!)) {
                    item!!.match = match
                    return true
                }
                return false
            }
        }
    }

    private inner class WebAppInterface {
        inner class CheckLanguagesStep : Runnable {
            override fun run() {
                val dialogNewsLocale = this@DialogNewsLocale
                val context = dialogNewsLocale.appContext
                if (context == null || DataTrans.instance(context).hasLanguages()) {
                    return
                }
                injectTransJs(dialogNewsLocale.webSafe)
            }
        }

        @JavascriptInterface
        fun onJsResult(tag: String?, jsResult: String?) {
            if (TextUtils.isEmpty(tag) || tag != "onTransList") {
                return
            }
            val dialogNewsLocale = this@DialogNewsLocale
            if (dialogNewsLocale.webSafe != null) {
                if (!TextUtils.isEmpty(jsResult)) {
                    dialogNewsLocale.jsResult = jsResult
                    dialogNewsLocale.runAsync(dialogNewsLocale.JsResultTask())
                } else {
                    val handler = dialogNewsLocale.mainHandler
                    if (handler != null) {
                        handler.post(dialogNewsLocale.DestroyWebViewTask())
                    }
                }
            }
        }

        @JavascriptInterface
        fun onObserDet(innerText: String?, found: Int) {
            val handler: Handler? = if (found == 0) null else this@DialogNewsLocale.mainHandler
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

    fun startTranslate() {
        if (this.dialogFrame == null || this.transLocale != null) {
            return
        }
        ensureLocale()
        this.transLocale = MainTransLocale(
            this.hostActivity,
            this.appContext,
            this.dialogFrame,
            this.shownItems,
            this.allItems,
            this.localeTag,
            ReleaseStep()
        )
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(RefreshTransTask())
    }

    fun applyTranslations() {
        val arrayList = this.allItems
        if (arrayList == null || arrayList.isEmpty()) {
            return
        }
        val size = arrayList.size
        var itemIndex = 0
        while (true) {
            var translated: String? = null
            if (itemIndex >= size) {
                try {
                    break
                } catch (unused: Exception) {
                    return
                }
            }
            val entry: Any? = arrayList.get(itemIndex)
            itemIndex++
            val mainLangItem = entry as MainLangItem?
            if (mainLangItem != null) {
                val code = mainLangItem.code
                val dbItems = this.dbItems
                if (dbItems != null && code >= 0 && code < dbItems.size) {
                    translated = dbItems.get(code)
                }
                mainLangItem.translated = translated
                if (!TextUtils.isEmpty(translated)) {
                    mainLangItem.lowerTrans = mainLangItem.translated!!.lowercase()
                }
                mainLangItem.corner = 2.toByte()
            }
        }
        val list: MutableList<MainLangItem?>? = this.shownItems
        if (list == null || list.isEmpty()) {
            return
        }
        for (item in list) {
            if (item != null && item.rowType.toInt() == 1) {
                val index = item.code - 1000
                val dbItems = this.dbItems
                val translated =
                    if (dbItems == null || index < 0 || index >= dbItems.size) null else dbItems.get(
                        index
                    )
                item.translated = translated
                if (!TextUtils.isEmpty(translated)) {
                    item.lowerTrans = item.translated!!.lowercase()
                }
                item.corner = 2.toByte()
            }
        }
    }

    fun ensureLocale() {
        if (TextUtils.isEmpty(this.localeTag)) {
            val appTag = MainUtil.appLocaleTag()
            this.localeTag = appTag
            var isKorean = false
            if (!TextUtils.isEmpty(appTag)) {
                val lowerCase = appTag.lowercase()
                if (!TextUtils.isEmpty(lowerCase)) {
                    isKorean = lowerCase.startsWith("ko")
                }
            }
            this.initialLocale = isKorean
        }
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogNewsLocale = this@DialogNewsLocale
            if (dialogNewsLocale.webSafe != null) {
                dialogNewsLocale.pageLoading = false
                dialogNewsLocale.loadUrl(url)
                if (!dialogNewsLocale.finishHandled && !TextUtils.isEmpty(url) && (url != dialogNewsLocale.lastFinishedUrl)) {
                    dialogNewsLocale.finishHandled = true
                    dialogNewsLocale.lastFinishedUrl = url
                    val handler = dialogNewsLocale.mainHandler
                    if (handler != null) {
                        handler.post(dialogNewsLocale.ClearCacheTask())
                    }
                }
                dialogNewsLocale.jsReady = true
                if (TextUtils.isEmpty(dialogNewsLocale.pendingJs)) {
                    return
                }
                dialogNewsLocale.jsReady = false
                evalJsNow(dialogNewsLocale.webSafe, dialogNewsLocale.pendingJs, true)
                dialogNewsLocale.pendingJs = null
            }
        }
        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogNewsLocale = this@DialogNewsLocale
            if (dialogNewsLocale.webSafe == null) {
                return
            }
            dialogNewsLocale.pageLoading = true
            dialogNewsLocale.loadUrl(url)
        }
        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val dialogNewsLocale = this@DialogNewsLocale
            dialogNewsLocale.webSafe = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogNewsLocale.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(dialogNewsLocale.DismissTask())
            return true
        }
        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val dialogNewsLocale = this@DialogNewsLocale
            if (dialogNewsLocale.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                dialogNewsLocale.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }
        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val dialogNewsLocale = this@DialogNewsLocale
            if (dialogNewsLocale.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    dialogNewsLocale.loadUrl(url)
                }
            }
            return false
        }
    }



    companion object {
    }
}
