@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import android.view.View

import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.nativeJsN
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.sameSite
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.hmgPageNumber
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.stripHitomiTitle
import com.mycompany.app.main.MainUtil.parseFontList
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.applyDesktopMode
import com.mycompany.app.main.MainUtil.injectPhVideoJs
import com.mycompany.app.main.MainUtil.resetTextSizePref
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.setActivityOrientation
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.hasZtwoFlag
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.main.MainUtil.pageTitleForFile
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.isGoogleUserContent
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.newMediaUri
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.main.MainUtil.backupDirPath2
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
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.db.book.DbBookScript.Companion.loadScripts
import com.mycompany.app.script.Script.Companion.buildScript
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.isFakeStream
import com.mycompany.app.main.MainUtil.getAdsHamJs
import com.mycompany.app.main.MainUtil.injectVideoObserver
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.hasBottomTitle
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUtil.setWindowStatusColor
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.db.book.DbBookWeb.Companion.hasPath
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.compress.Compress.Companion.isSubtitleExt
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import com.mycompany.app.web.WebNestView
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import com.mycompany.app.web.WebViewActivity
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebViewActivity.FaceItem
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import android.view.ViewGroup.MarginLayoutParams
import com.mycompany.app.script.ScriptCache
import com.mycompany.app.ui.browser.BarScroll
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.script.ScriptCriteria
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.web.WebSearchAdapter.SearchItem
import com.mycompany.app.main.MainNative
import com.mycompany.app.script.ScriptId
import com.google.gson.JsonElement
import com.mycompany.app.web.WebViewActivity.SortFace
import android.webkit.URLUtil
import com.google.gson.JsonParser
import com.mycompany.app.script.Script
import ch.opumlabs.opumbrowser.R
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.Collections
import com.mycompany.app.web.WebViewActivity.Companion.pauseWebView
import com.mycompany.app.web.WebViewActivity.Companion.scriptMatches

/*
 * The browser activity's page functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

/**
 * Injects the enabled userscripts matching the current url: true for "document-end" (or unset) scripts,
 * false for "document-start". The matching script list is cached per url in the ScriptCache (Mb).
 */
fun WebViewActivity.injectUserScripts(atEnd: Boolean) {
    if (!PdfSettings.userScript.value) {
        return
    }
    val url = this.dialogsState.pageUrl
    val cache = this.miscState.scriptCache
    var scripts: Array<Script?>? = null
    if (cache != null) {
        synchronized(cache) {
            scripts = if (TextUtils.isEmpty(url)) null else cache.scriptsByUrl.get(url)
        }
        if (scripts == null) {
            var ids: Array<ScriptId?>? = null
            try {
                val all: Array<ScriptCriteria>? = cache.criteriaList as Array<ScriptCriteria>?
                if (!TextUtils.isEmpty(url) && all != null && all.size != 0) {
                    var matched: ArrayList<ScriptId?>? = null
                    for (criteria in all) {
                        if (scriptMatches(criteria, url)) {
                            if (matched == null) {
                                matched = ArrayList<ScriptId?>()
                            }
                            matched.add(criteria)
                        }
                    }
                    if (matched != null && !matched.isEmpty()) {
                        ids = matched.toTypedArray<ScriptId?>()
                    }
                }
            } catch (ignored: Exception) {
            }
            val loaded = if (ids == null) null else DbBookScript.loadScripts(this, ids as Array<ScriptId>, true)
            scripts = if (loaded == null) arrayOfNulls<Script>(0) else loaded
            synchronized(cache) {
                if (!TextUtils.isEmpty(url)) {
                    cache.scriptsByUrl.put(url, scripts)
                }
            }
        }
    }
    if (scripts == null || scripts.size == 0) {
        return
    }
    for (script in scripts) {
        if (script == null) {
            continue
        }
        val runAt = script.runAt
        val inject =
            if (atEnd) (TextUtils.isEmpty(runAt) || "document-end" == runAt) else ("document-start" == runAt)
        if (inject) {
            evalJs(this.currentPage, buildScript(script, this.miscState.scriptCacheUrl), true)
        }
    }
}

fun WebViewActivity.dragPrevPage(pageX: kotlin.Float) {
    val webNestFrame = this.tabsState.currentFrame
    if (webNestFrame == null || this.currentPage == null) {
        return
    }
    try {
        val pageItemX = this.tabsState.currentFrame!!.pageItemAt(webNestFrame.pageIndex - 1)
        if (pageItemX != null) {
            val webNestView = pageItemX.webNestView
            val currentPage = this.currentPage
            this.dialogsState.swipePage = currentPage
            currentPage!!.pageX = pageX
            if (webNestView == null || webNestView.getVisibility() == View.VISIBLE) {
                return
            }
            if (!TextUtils.isEmpty(webNestView.blankPageUrl)) {
                webNestView.setBackgroundColor(0)
            }
            webNestView.setVisibility(View.VISIBLE)
            attachAnimListener(webNestView)
        }
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.resumePausedPage() {
    if (this.pagePaused) {
        this.pagePaused = false
        val webNestView = this.currentPage
        if (webNestView != null) {
            webNestView.onResume()
        }
    }
}

fun WebViewActivity.nextTabInsertIndex(grouped: kotlin.Boolean): Int {
    val currentTabIndex = this.currentTabIndex
    var index = currentTabIndex + 1
    val arrayList: ArrayList<WebTabItem?>? = if (TtsSettings.tabLast.value) this.tabsState.tabItems else null
    if (TtsSettings.tabLast.value && arrayList != null) {
        if (!grouped) {
            return arrayList.size
        }
        val currentTab = tabItemAt(currentTabIndex)
        val tabs: ArrayList<WebTabItem?>? = if (currentTab != null && currentTab.groupId != 0L) this.tabsState.tabItems else null
        if (currentTab != null && currentTab.groupId != 0L && tabs != null && index >= 0 && index < tabs.size) {
            val groupId = currentTab.groupId
            val size = this.tabsState.tabItems!!.size
            var lastInGroup = index
            while (index < size) {
                val webTabItem = this.tabsState.tabItems!!.get(index)
                if (webTabItem != null) {
                    if (webTabItem.groupId != groupId) {
                        return index
                    }
                    lastInGroup = index
                }
                index++
            }
            return lastInGroup
        }
    }
    return index
}

fun WebViewActivity.injectVideoDetect(inject: kotlin.Boolean, withAudio: kotlin.Boolean) {
    val sb = StringBuilder()
    if ((inject == true)) {
        val nativeLoaded = AppState.nativeLoaded
        if (nativeLoaded) {
            var arrayList: ArrayList<Any?>? = null
            if (nativeLoaded) {
                if (!partsReady(5, MainNative.videoDetectParts)) {
                    val parts: ArrayList<Any?> = ArrayList<Any?>()
                    var partIndex = 0
                    while (true) {
                        if (partIndex >= 5) {
                            MainNative.videoDetectParts = parts
                            arrayList = parts
                            break
                        } else {
                            val adsHamJs = MainUtil.getAdsHamJs(partIndex)
                            if (TextUtils.isEmpty(adsHamJs)) {
                                break
                            }
                            parts.add(adsHamJs)
                            partIndex++
                        }
                    }
                } else {
                    arrayList = MainNative.videoDetectParts
                }
            }
            if (arrayList != null && arrayList.size == 5) {
                for (index in 0..4) {
                    val part = arrayList.get(index) as String?
                    if (TextUtils.isEmpty(part)) {
                        break
                    }
                    sb.append(part)
                }
            }
        }
        sb.append("function myHamUr2(html){if(!html)return false;var len=html.length;if(len==0)return false;if(!window.sbhtag){window.sbhtag=myHamTag(html,len);if(window.sbhtag){android.onHamTag(window.sbhtag);}}return window.sbhtag?true:false;}")
    }
    sb.append("function mySrc(src){return src&&(src.indexOf('http')==0)&&!src.includes('youtube.com');}function myVi2(ele,win){if(((ele.tagName=='VIDEO')&&ele.src)||((ele.tagName=='SOURCE')&&ele.src&&ele.getAttribute('type').includes('video'))){if(mySrc(ele.src)){android.onVidDet(0);}if(win.sb_vi2){win.sb_vi2.disconnect();win.sb_vi2=null;}return true;}return false;}function myVd(doc,win){if(!doc||!win)return false;")
    if ((inject == true)) {
        sb.append("var elh=doc.querySelector(\"script[id='initials-script']\");if(!elh||!myHamUr2(elh.innerHTML))return false;")
    }
    sb.append("var ele=doc.querySelector(\"video\");if(!ele)return false;if(ele.src){if(mySrc(ele.src)){android.onVidDet(0);}return true;}var el2=ele.querySelector(\"source[type*='video']\");if(el2&&el2.src){if(mySrc(el2.src)){android.onVidDet(0);}return true;}if(!win.sb_vi2){win.sb_vi2=new MutationObserver(e=>{for(var i=0;i<e.length;i++){if(myVi2(e[i].target,win))break;}}),win.sb_vi2.observe(doc,{childList:!0,subtree:!0,attributes:!0});}return true;}")
    injectVideoObserver(this.currentPage, sb, withAudio)
}

fun WebViewActivity.addressPathText(): String? {
    var shownUrl: String?
    var length: Int = 0
    var pathStart: Int = 0
    var path: String? = null
    val onStart: Boolean = if (PdfSettings.addrType.value != 1) false else this.onStartPage
    if (PdfSettings.addrType.value != 1 || (onStart == true)) {
        return null
    }
    if ((onStart == true)) {
        shownUrl = null
    } else {
        shownUrl = this.dialogsState.pageUrl
        val webNestView = this.currentPage
        if (webNestView != null) {
            shownUrl = webNestView.displayUrl(shownUrl)
        }
    }
    if (sameObject(shownUrl, this.pageState.addressPathSource)) {
        return this.pageState.addressPathCached
    }
    this.pageState.addressPathSource = shownUrl
    val schemeEnd = if (!TextUtils.isEmpty(shownUrl)) shownUrl!!.indexOf("://") else 0
    var index = if (!TextUtils.isEmpty(shownUrl) && schemeEnd != -1) (schemeEnd + 3) else 0
    if (!TextUtils.isEmpty(shownUrl) && schemeEnd != -1 && index < (shownUrl!!.length.also { length = it }) && (shownUrl.indexOf(47.toChar(), index)
            .also { pathStart = it }) != -1 && ((pathStart != index || (((schemeEnd + 4).also {
            index = it
        }) < length && (shownUrl.indexOf(47.toChar(), index)
            .also { pathStart = it }) != -1)) && pathStart > index && pathStart + 1 < length)) {
        path = shownUrl.substring(pathStart)
    }
    this.pageState.addressPathCached = path
    return path
}

fun WebViewActivity.setBackgroundPlay(checked: kotlin.Boolean) {
    val webNestView = this.currentPage
    if (webNestView == null) {
        return
    }
    if (!checked) {
        webNestView.pauseOnce()
        MainApp.pauseWebTimers(this.dialogsState.appContext, this.currentPage)
    } else {
        this.videoState.backPlayOn = true
        webNestView.setBackPlay(true)
        runInBackground(WvaVideoPlayListenerScript(this))
    }
}

fun WebViewActivity.resolvePageLink(url: String?): String? {
    if (isGoogleUserContent(url)) {
        return url
    }
    val webNestView = this.currentPage
    if (webNestView == null) {
        return this.dialogsState.pageUrl
    }
    val videoPage = webNestView.videoPageUrl(webNestView.videoIndex(url))
    return if (!TextUtils.isEmpty(videoPage)) videoPage else this.dialogsState.pageUrl
}

fun WebViewActivity.stopBackgroundPlay(enabled: kotlin.Boolean, pause: kotlin.Boolean) {
    if (this.videoState.backPlayOn) {
        clearBackPlay(enabled)
        val webNestView: WebNestView? = if (!this.isPaused) null else this.currentPage
        if (!this.isPaused || webNestView == null) {
            return
        }
        if ((pause == true)) {
            webNestView.reload()
        }
        this.currentPage!!.setBackPlay(false)
        this.currentPage!!.pauseOnce()
        MainApp.pauseWebTimers(this.dialogsState.appContext, this.currentPage)
    }
}

fun WebViewActivity.pageScroll(up: kotlin.Boolean, smooth: kotlin.Boolean): kotlin.Boolean {
    var barThreshold: Int = 0
    var scrollThreshold: Int = 0
    var barsToExpand: BarScroll? = null
    val webNestView = this.currentPage
    if (webNestView != null && this.videoFrame == null) {
        if (webNestView.coversPage()) {
            if (ZtwoSettings.newsUse.value && this.onStartPage) {
                val quickView = this.currentPage!!.quickPanel
                if (quickView != null) {
                    quickView.refreshGrid(up, smooth)
                }
                if (!up) {
                    val barScroll = this.barScroll
                    if (barScroll != null) {
                        barScroll.setExpanded(false)
                        return true
                    }
                } else if ((smooth == true) && (this.barScroll.also { barsToExpand = it }) != null) {
                    barsToExpand!!.setExpanded(true)
                    return true
                }
            }
        } else if (ZtwoSettings.scrollAnim.value) {
            val currentPage = this.currentPage
            if ((up == true)) {
                currentPage!!.pageUp(smooth)
                val bars: BarScroll? = if (smooth) this.barScroll else null
                if (smooth && bars != null) {
                    bars.setExpanded(true)
                    return true
                }
            } else {
                currentPage!!.pageDown(smooth)
                val bars = this.barScroll
                if (bars != null) {
                    bars.setExpanded(false)
                    return true
                }
            }
        } else {
            val page = this.currentPage
            if ((smooth == true)) {
                if ((up == true)) {
                    if (page!!.getScrollY() > 0) {
                        this.currentPage!!.scrollTo(0, 0)
                    }
                    val bars = this.barScroll
                    if (bars != null) {
                        bars.setExpanded(true)
                        return true
                    }
                } else {
                    val maxScrollY =
                        page!!.computeVerticalScrollRange() - pageVisibleHeight()
                    if (maxScrollY > this.currentPage!!.getScrollY()) {
                        this.currentPage!!.scrollTo(0, maxScrollY)
                    }
                    val bars = this.barScroll
                    if (bars != null) {
                        bars.setExpanded(false)
                        return true
                    }
                }
            } else if ((up == true)) {
                val scrollY = page!!.getScrollY()
                var step = pageVisibleHeight()
                val scrollThreshold = if (this.pageState.barOffset == 0) (MainApp.dp8 * 15) else 0
                if (this.pageState.barOffset == 0 && step > scrollThreshold) {
                    step -= scrollThreshold
                }
                var index = scrollY - step
                if (index < 0) {
                    index = 0
                }
                if (index < scrollY) {
                    this.currentPage!!.scrollTo(0, index)
                }
                val bars: BarScroll? = if ((smooth == true)) this.barScroll else null
                if ((smooth == true) && bars != null) {
                    bars.setExpanded(true)
                    return true
                }
            } else {
                val currentY = page!!.getScrollY()
                var step = pageVisibleHeight()
                val barThreshold = if (this.pageState.barOffset == 0) (MainApp.dp8 * 15) else 0
                if (this.pageState.barOffset == 0 && step > barThreshold) {
                    step -= barThreshold
                }
                var targetY = currentY + step
                val maxScrollY = this.currentPage!!.computeVerticalScrollRange() - step
                if (targetY > maxScrollY) {
                    targetY = maxScrollY
                }
                if (targetY > currentY) {
                    this.currentPage!!.scrollTo(0, targetY)
                }
                val bars = this.barScroll
                if (bars != null) {
                    bars.setExpanded(false)
                }
            }
        }
        return true
    }
    return false
}

fun WebViewActivity.pageCanGoForward(): kotlin.Boolean {
    var canGoForwardCache = this.pageState.canGoForwardCache
    if (canGoForwardCache.toInt() == 0) {
        val webNestView = this.currentPage
        if (webNestView == null) {
            return false
        }
        if (webNestView.canGoForward()) {
            this.pageState.canGoForwardCache = 1.toByte()
            canGoForwardCache = 1
        } else {
            canGoForwardCache = 2
            this.pageState.canGoForwardCache = 2.toByte()
        }
    }
    return canGoForwardCache.toInt() == 1
}

fun WebViewActivity.pageCanGoBack(): kotlin.Boolean {
    var canGoBackCache = this.pageState.canGoBackCache
    if (canGoBackCache.toInt() == 0) {
        val webNestView = this.currentPage
        if (webNestView == null) {
            return false
        }
        if (webNestView.canGoBack()) {
            this.pageState.canGoBackCache = 1.toByte()
            canGoBackCache = 1
        } else {
            canGoBackCache = 2
            this.pageState.canGoBackCache = 2.toByte()
        }
    }
    return canGoBackCache.toInt() == 1
}

fun WebViewActivity.bindFrameToTab() {
    if (this.pullView != null || this.webCoord == null || this.currentPage == null) {
        return
    }
    val currentTab = tabItemAt(this.currentTabIndex)
    if (currentTab != null) {
        val webNestFrame = this.tabsState.currentFrame
        currentTab.webFrame = webNestFrame
        webNestFrame!!.setTabUid(currentTab.uid)
    }
    layoutTabFrames()
}

fun WebViewActivity.createPageViews() {
    if (this.pullView != null || this.webCoord == null) {
        return
    }
    pauseWebDestroy()
    val webNestView = WebNestView(this)
    this.currentPage = webNestView
    attachWebDestroy(webNestView)
    val currentPage = this.currentPage
    currentPage!!.startPage = this.onStartPage
    currentPage.setDeskMode(this.deskModeStart)
    val webNestFrame = WebNestFrame(this)
    this.tabsState.currentFrame = webNestFrame
    webNestFrame.setDarkBars(this.onStartPage)
    val currentFrame = this.tabsState.currentFrame
    val page = this.currentPage
    if (currentFrame != null) {
        currentFrame.addPage(page, 0, 0)
    }
}

fun WebViewActivity.applyPageLayout() {
    var followsTopBar: kotlin.Boolean
    if (this.pageLayout != null) {
        val topBarHeight = MainUtil.topBarHeight()
        var topMargin = 0
        val pageTopMargin = if (BrowserSettings.fixTop.value) topBarHeight else 0
        val pageBottomMargin = if (BrowserSettings.fixBot.value) MainUtil.bottomInsetTotal() else 0
        if (this.pageLayout != null) {
            if (BrowserSettings.fixTop.value) {
                followsTopBar = false
            } else {
                followsTopBar = true
                val tabBar = if (!MainUtil.hasBottomTitle()) BrowserSettings.tabBar.value else 0
                if (!MainUtil.hasBottomTitle() && tabBar != 1 && tabBar != 2) {
                    followsTopBar = false
                }
            }
            if (this.pageState.pageTopMargin != pageTopMargin || this.pageState.pageBottomMargin != pageBottomMargin || this.pageState.pageFollowsSet != followsTopBar) {
                this.pageState.pageTopMargin = pageTopMargin
                this.pageState.pageBottomMargin = pageBottomMargin
                this.pageState.pageFollowsSet = followsTopBar
                val params = this.pageLayout!!.getLayoutParams() as android.widget.FrameLayout.LayoutParams?
                if (params != null) {
                    val webNestLayout = this.pageLayout
                    // The page follows the top bar's bottom edge (was the MyBehaviorWebTop coordinator behaviour).
                    this.pageFollowsTopBar = followsTopBar
                    if (followsTopBar) {
                        this.pageState.pushBars?.run()
                    } else {
                        webNestLayout!!.setTranslationY(0.0f)
                    }
                    (params as MarginLayoutParams).topMargin = pageTopMargin
                    (params as MarginLayoutParams).bottomMargin = pageBottomMargin
                    this.pageLayout!!.requestLayout()
                }
            }
        }
        val webNestView = this.currentPage
        if (webNestView != null) {
            val webSslView = webNestView.sslView
            if (webSslView != null) {
                webSslView.applyPadding()
            }
            val webCrashView = webNestView.crashView
            if (webCrashView != null) {
                webCrashView.applyPadding()
            }
        }
        val myScrollBar = this.scrollBar
        if (myScrollBar != null) {
            // the scroll bar stays between the top bar and the bottom bar, also when the bars hide while scrolling
            // (it used the page's bottom margin, which is 0 then, and ran behind the bottom bar) (2026-11-19)
            myScrollBar.topInset = topBarHeight
            myScrollBar.bottomInset = MainUtil.bottomInsetTotal()
        }
        val myProgressBar = this.dialogsState.progressBar
        val layoutParams = this.floatingState.overlays?.params(myProgressBar)
        if (myProgressBar == null || layoutParams == null) {
            return
        }
        if (!BrowserSettings.showStatus.value && MainUtil.topBarHeight() == 0) {
            topMargin = MainApp.dp4 / 2
        }
        if (layoutParams.topMargin == topMargin) {
            return
        }
        layoutParams.topMargin = topMargin
        this.floatingState.overlays?.relayout(myProgressBar)
    }
}

fun WebViewActivity.postTransIconUpdate() {
    val myWebBody: MyWebBody? = if (this.currentPage == null) null else this.dialogsState.webBody
    if (this.currentPage == null || myWebBody == null) {
        return
    }
    myWebBody.post(WvaPostTransIconUpdateTask(this))
}

fun WebViewActivity.updateFileUrl(fileSourceUrl: String?) {
    var shownUrl: String?
    val addressChanged: kotlin.Boolean
    val webNestView = this.currentPage
    if (webNestView != null) {
        val fileUrl = webNestView.fileUrl
        val currentPage = this.currentPage
        if (currentPage == null) {
            shownUrl = null
        } else {
            val context = this.dialogsState.appContext
            if (TextUtils.isEmpty(fileSourceUrl) || fileSourceUrl!!.startsWith("http://")) {
                currentPage.barColor = 0
                currentPage.barColor2 = 0
                currentPage.fileShownUrl = null
            } else {
                if (fileSourceUrl.startsWith("file:///")) {
                    currentPage.barColor = 0
                    currentPage.barColor2 = 0
                    if ("file:///android_asset/shortcut.html" == fileSourceUrl) {
                        currentPage.fileShownUrl = "file:///android_asset/shortcut.html"
                    } else if (TextUtils.isEmpty(currentPage.fileShownUrl) || fileSourceUrl != currentPage.fileSourceUrl) {
                        currentPage.fileShownUrl = pageTitleForFile(context, fileSourceUrl)
                    }
                    shownUrl = currentPage.fileShownUrl
                } else {
                    currentPage.fileShownUrl = null
                }
                currentPage.fileSourceUrl = fileSourceUrl
            }
            shownUrl = fileSourceUrl
            currentPage.fileSourceUrl = fileSourceUrl
        }
        if (TextUtils.isEmpty(fileSourceUrl) || !fileSourceUrl!!.startsWith("file:///") || TextUtils.isEmpty(shownUrl) || shownUrl!!.startsWith(
                "file:///"
            )
        ) {
            val page = this.currentPage
            if (sameObject(fileUrl, if (page != null) page.fileUrl else null)) {
                addressChanged = false
            } else {
                addressChanged = true
            }
        } else {
            addressChanged = true
        }
        val tinted = hasPath(this.dialogsState.appContext, shownUrl)
        val tintChanged = this.dialogsState.barsTinted != tinted
        this.dialogsState.barsTinted = tinted
        if (addressChanged || tintChanged) {
            val myWebCoord = this.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaUpdateFileUrlTask(this, addressChanged = addressChanged, tintChanged = tintChanged))
        }
    }
}

fun WebViewActivity.destroyPendingPage() {
    val webNestView = this.pageState.pendingPage
    if (webNestView != null) {
        this.pageState.pendingPage = null
        finishProgress()
        pauseWebDestroy()
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaDestroyOldPage(this, webNestView))
    }
}

fun WebViewActivity.syncJsBridge(webView: WebView?, url: String?) {
    if (webView != null) {
        if (webView !is WebNestView) {
            webView.post(WvaSyncJsBridgeTask(this, view = webView, url = url))
            return
        }
        val webNestView = webView
        val isHttp = isHttpUrl(url)
        val jsAdded = webNestView.jsAdded
        if (isHttp) {
            if (jsAdded) {
                webNestView.setJsAdded(false)
                this.dialogsState.langCheckBusy = false
                this.pageState.jsBridgePage = webNestView
                webNestView.post(JsBridgeRemoveTask(this))
                return
            }
            return
        }
        if (jsAdded) {
            return
        }
        webNestView.setJsAdded(true)
        this.dialogsState.langCheckBusy = false
        this.pageState.jsBridgePage = webNestView
        webNestView.post(JsBridgeAddTask(this))
    }
}

fun WebViewActivity.destroyPopupPage() {
    val webNestView = this.pageState.popupPage
    if (webNestView != null) {
        this.pageState.popupPage = null
        finishProgress()
        pauseWebDestroy()
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaDestroyOldPage(this, webNestView))
    }
}

fun WebViewActivity.onPopupPageResult(kind: Int, url: String?, referer: String?) {
    this.pageState.popupUrl = null
    this.pageState.popupHost = null
    if (!this.pageState.popupPending) {
        if (kind == 1) {
            this.pageState.popupPending = true
            this.pageState.popupLoadState = 0.toByte()
            this.miscState.popupResultUrl = url
            val myWebCoord = this.webCoord
            if (myWebCoord != null) {
                myWebCoord.post(WvaOnPopupPageResultTask(this))
                return
            }
            return
        }
        if (kind == 2) {
            this.pageState.popupPending = true
            this.pageState.popupLoadState = 0.toByte()
            this.miscState.popupResultUrl = url
            val webCoord = this.webCoord
            if (webCoord != null) {
                webCoord.post(WvaPopupInGroupTask(this))
                return
            }
            return
        }
    }
    val webNestView = this.pageState.popupPage
    if (webNestView != null) {
        this.pageState.pendingPage = webNestView
        this.pageState.popupPage = null
        val popupLoaded = this.pageState.popupLoadState.toInt() != 0
        this.pageState.popupLoadState = 0.toByte()
        val coord = this.webCoord
        if (coord == null) {
            return
        }
        coord.post(WvaPopupOpenTask(this, kind = kind, popupLoaded = popupLoaded, url = url, referer = referer))
    }
}

fun WebViewActivity.isShowingStartPage(): kotlin.Boolean {
    val webNestView = this.currentPage
    if (webNestView == null) {
        return false
    }
    if (this.onStartPage) {
        return true
    }
    return "file:///android_asset/shortcut.html" == webNestView.getUrl()
}

fun WebViewActivity.parseMediaDefinitions(scriptText: String?): kotlin.Boolean {
    var root: JsonObject? = null
    var definitions: JsonArray? = null
    var size = 0
    var posterUrl: String? = null
    var urlElement: JsonElement? = null
    var qualityElement: JsonElement? = null
    var height = 0
    var index = 0
    if (!TextUtils.isEmpty(scriptText) && (scriptText!!.endsWith("}") || scriptText.endsWith("};"))) {
        try {
            val jsonStart = scriptText.indexOf(123.toChar())
            if (jsonStart != -1) {
                root = JsonParser.parseString(
                    if (scriptText.endsWith("};")) scriptText.substring(
                        jsonStart,
                        scriptText.length - 1
                    ) else scriptText.substring(jsonStart)
                ).getAsJsonObject()
                try {
                    definitions = root.get("mediaDefinitions").getAsJsonArray()
                    try {
                        size = definitions.size()
                    } catch (unused: Exception) {
                        size = 0
                    }
                } catch (unused2: Exception) {
                    definitions = null
                }
                if (size != 0) {
                    var qualities: ArrayList<WebViewActivity.FaceItem?>? = null
                    for (index2 in 0..<size) {
                        try {
                            val jsonObject = definitions!!.get(index2) as JsonObject?
                            if (jsonObject != null && (jsonObject.get("videoUrl")
                                    .also { urlElement = it }) != null
                            ) {
                                val streamUrl = urlElement!!.getAsString()
                                if (URLUtil.isNetworkUrl(streamUrl) && (jsonObject.get("quality")
                                        .also {
                                            qualityElement = it
                                        }) != null && (qualityElement!!.getAsInt().also { height = it }) > 0
                                ) {
                                    if (qualities == null) {
                                        qualities = ArrayList()
                                    }
                                    qualities.add(FaceItem(streamUrl, height, "" + height, false))
                                    this.dialogsState.snsFixUrl = null
                                }
                            }
                        } catch (unused3: Exception) {
                        }
                    }
                    if (qualities != null && !qualities.isEmpty()) {
                        this.dialogsState.videoQualitiesFound = true
                        try {
                            Collections.sort<FaceItem?>(qualities, SortFace())
                        } catch (unused4: Exception) {
                        }
                        val urls: ArrayList<Any?> = ArrayList<Any?>()
                        val count = qualities.size
                        while (index < count) {
                            val any: Any = qualities.get(index)!!
                            index++
                            urls.add((any as FaceItem).url)
                        }
                        try {
                            val imageElement = root.get("image_url")
                            if (imageElement != null) {
                                posterUrl = imageElement.getAsString()
                                if (!URLUtil.isNetworkUrl(posterUrl)) {
                                    posterUrl = null
                                }
                            } else {
                                posterUrl = null
                            }
                        } catch (unused5: Exception) {
                        }
                        val webNestView = this.currentPage
                        if (webNestView != null) {
                            webNestView.resetMedia(qualities)
                        }
                        findPageVideos(null, null, posterUrl, urls)
                        return true
                    }
                }
            }
        } catch (unused6: Exception) {
            root = null
            definitions = null
        }
    }
    return false
}

fun WebViewActivity.onSearchItemClick(searchItem: SearchItem?) {
    if (this.currentPage == null || this.searchAdapter == null) {
        return
    }
    val itemKind = searchItem!!.itemKind
    if (itemKind.toInt() == 3) {
        setAddressEditing(false)
        return
    }
    if (itemKind.toInt() == 1) {
        return
    }
    if (itemKind.toInt() == 2) {
        this.pageState.addressEditBusy = false
        setAddressEditing(false)
        openFindBar(searchItem.title)
        return
    }
    var targetUrl = searchItem.path
    if (TextUtils.isEmpty(targetUrl)) {
        targetUrl = null
    } else if (targetUrl == "about:blank") {
        targetUrl = "about:blank"
    } else if (!URLUtil.isValidUrl(targetUrl)) {
        targetUrl = "http://" + targetUrl
    }
    if (TextUtils.isEmpty(targetUrl)) {
        finishAddressEdit(searchItem.title)
        return
    }
    if (hasZtwoFlag(4)) {
        addTabPlain(null, targetUrl, true, null)
    } else {
        openInCurrentPage(targetUrl, null)
    }
    this.pageState.addressEditBusy = false
    setAddressEditing(false)
}

fun WebViewActivity.pageUrlForTab(): String? {
    val webNestView = this.currentPage
    if (webNestView == null) {
        return null
    }
    var url = webNestView.getUrl()
    if (TextUtils.isEmpty(url)) {
        url = this.currentPage!!.validPageUrl
        val currentTab: WebTabItem? = if (TextUtils.isEmpty(url)) tabItemAt(this.currentTabIndex) else null
        if (TextUtils.isEmpty(url) && currentTab != null) {
            return currentTab.tabUrl
        }
    }
    return url
}

fun WebViewActivity.restartBrowser() {
    if (this.pageState.restartBrowserBusy) {
        return
    }
    this.pageState.restartBrowserBusy = true
    if (this.webCoord == null) {
        return
    }
    if (this.videoState.backPlayOn) {
        clearBackPlay(true)
        val webNestView: WebNestView? = if (this.isPaused) this.currentPage else null
        if (this.isPaused && webNestView != null) {
            webNestView.setBackPlay(false)
            pauseWebView(this.currentPage)
        }
    }
    if (SyncSettings.secretMode.value) {
        runInBackground(WvaRestartWithUrl(this))
        return
    }
    MainUtil.clearWebData(this.dialogsState.appContext)
    if (SecretSettings.keepLogin.value) {
        runInBackground(WvaRestartWithUrl(this))
    } else {
        clearBrowsingData(this.mainHandler, WvaRestartClearAuth(this))
    }
}

fun WebViewActivity.removeLoadView() {
    val webLoadView = this.loadView
    if (webLoadView != null) {
        webLoadView.release()
        this.loadView = null
    }
}

fun WebViewActivity.pageThemeColor(): Int {
    val webNestView = this.currentPage
    val themeHead = BrowserSettings.headMode()
    if (themeHead == 0) {
        return 0
    }
    if (themeHead == 2) {
        val headColor = PrefWeb.headColor
        if (headColor != 0) {
            return headColor
        }
        MainUtil.resetTextSizePref()
        return PrefWeb.headColor
    }
    if (this.onStartPage || webNestView == null) {
        return 0
    }
    if (!webNestView.blankPending) {
        return webNestView.themeColor
    }
    webNestView.setBarColors(this.dialogsState.themeColor, this.miscState.themeLight)
    return this.dialogsState.themeColor
}

fun WebViewActivity.hideMoveHint() {
    val webMoveText = this.moveHint
    if (webMoveText != null) {
        webMoveText.hideSoon()
    }
}

fun WebViewActivity.pageAreaWidth(): Int {
    val webNestLayout = this.pageLayout
    if (webNestLayout != null) {
        return webNestLayout.getWidth()
    }
    val myWebBody = this.dialogsState.webBody
    if (myWebBody != null) {
        return myWebBody.rectWidth
    }
    return 0
}

fun WebViewActivity.applyPageScripts(force: kotlin.Boolean) {
    val onStartPage = this.onStartPage
    val webNestView: WebNestView? = if (onStartPage) null else this.currentPage
    if (onStartPage || webNestView == null) {
        return
    }
    if (!force) {
        if (BrowserSettings.adsBlock.value || TtsSettings.checkOver.value != 0) {
            runInBackground(WvaPageAdScripts(this))
            return
        }
        return
    }
    val myProgressBar = this.dialogsState.progressBar
    if (if (myProgressBar == null) false else !myProgressBar.drawSkipped) {
        return
    }
    if (!onStartPage && BrowserSettings.adsBlock.value) {
        webNestView.setAdsRunning(true)
    }
    runInBackground(WvaApplyPageScriptsTask(this))
}

fun WebViewActivity.pausePage() {
    this.pagePaused = true
    finishActionMode()
    val webNestView = this.currentPage
    if (webNestView != null) {
        webNestView.pauseOnce()
    }
}

fun WebViewActivity.rebuildTabs(keepCurrent: kotlin.Boolean): kotlin.Boolean {
    var found = false
    var index = 0
    found = false
    found = false
    if (this.webCoord == null || this.pageState.rebuildTabsBusy) {
        return false
    }
    this.pageState.rebuildTabsBusy = true
    val arrayList: ArrayList<WebTabItem?>? = if (this.currentPage != null) this.tabsState.tabItems else null
    if (this.currentPage != null && arrayList != null && arrayList.size != 0) {
        finishProgress()
        exitFind(true)
        synchronized(this.tabLock) {
            val tabItems = this.tabsState.tabItems
            val size = tabItems!!.size
            while (index < size) {
                val entry: Any? = tabItems.get(index)
                index++
                val webTabItem = entry as WebTabItem?
                val webNestFrame: WebNestFrame? = if (webTabItem != null) webTabItem.webFrame else null
                if (webTabItem != null && webNestFrame != null) {
                    if (webNestFrame.destroyFrame(this.currentPage)) {
                        this.currentPage = null
                    }
                    webTabItem.webFrame = null
                }
            }
        }
        runInBackground(WvaSaveOpenTabsTask(this, keepCurrent))
        found = true
    }
    this.webCoord!!.post(WvaRebuildTabsTask(this))
    return found
}

fun WebViewActivity.detachFrame(webNestFrame: WebNestFrame?) {
    if (this.pageLayout != null) {
        try {
            val visibility = webNestFrame!!.getVisibility() != View.GONE
            webNestFrame.trimAfter(visibility)
            if (visibility) {
                webNestFrame.setVisibility(View.GONE)
            }
            webNestFrame.refreshPage()
            this.pageLayout!!.removeViewInLayout(webNestFrame)
        } catch (unused: Exception) {
        }
    }
}

fun WebViewActivity.grantSitePermission(requestCode: Int) {
    if (requestCode != 24) {
        val permissionRequest = this.permissionRequest2
        if (permissionRequest != null) {
            permissionRequest.grant(permissionRequest.getResources())
        }
    } else if (this.geoCallback != null && !TextUtils.isEmpty(this.geoOrigin)) {
        this.geoCallback!!.invoke(this.geoOrigin, true, false)
    }
    val pageHost = this.dialogsState.pageHost
    val pendingPermissionAllow = this.pageState.pendingPermissionAllow
    val permissionKind = this.permissionKind
    this.pageState.permissionHost = pageHost
    this.pageState.sitePermissionAllow = pendingPermissionAllow
    this.pageState.permissionKind = permissionKind
    runInBackground(WvaSavePermissionTask(this))
}

fun WebViewActivity.openMediaUrl(url: String?, mime: String?) {
    if (this.currentPage == null || this.isClosing) {
        return
    }
    val fileExt = fileExtOf(url, false)
    val mimeType = if (TextUtils.isEmpty(mime)) mimeForExt(fileExt) else mime
    if (!TextUtils.isEmpty(mimeType)) {
        if (mimeType!!.startsWith("video")) {
            openDownloadDialog(url, null, mimeType, 0L, 5, url)
            return
        } else if (mimeType.startsWith("audio")) {
            openDownloadDialog(url, null, mimeType, 0L, 6, url)
            return
        } else if (mimeType.startsWith("image")) {
            openDownloadDialog(url, null, mimeType, 0L, 4, url)
            return
        }
    }
    if (isSubtitleExt(fileExt)) {
        openDownloadDialog(url, null, mimeType, 0L, 7, url)
    } else {
        openDownloadDialog(url, null, "video/*", 0L, 5, url)
    }
}

fun WebViewActivity.openDownloadDialog(url: String?, disposition: String?, mime: String?, length: Long, downType: Int, referer: String?) {
    val type: Byte
    val popupTitle = this.pageState.popupTitle
    val jsBridgeBusy = this.pageState.jsBridgeBusy
    val videoDownListenerText = this.pageState.videoDownListenerText
    val externalDownload = this.pageState.externalDownload
    val arrayList = this.pageState.videoJsonResultList
    val videoJsonIndex = this.dialogsState.videoJsonIndex
    val downloadItems = this.dialogsState.downloadItems
    val downloadFromBridge = this.dialogsState.downloadFromBridge
    this.pageState.popupTitle = null
    this.pageState.jsBridgeBusy = false
    this.pageState.videoDownListenerText = null
    this.pageState.externalDownload = false
    this.pageState.videoJsonResultList = null
    this.dialogsState.downloadItems = null
    this.dialogsState.phpDownload = this.pageState.phpDownload
    this.pageState.phpDownload = false
    if (this.currentPage == null || this.isClosing) {
        return
    }
    if (!jsBridgeBusy && !TextUtils.isEmpty(url) && url!!.startsWith("blob:")) {
        runInBackground(WvaBlobDownloadScript(this, url = url, mime = disposition, disposition = mime, length = length))
        return
    }
    var dialogMime = mime
    if (isFakeStream(url)) {
        dialogMime = "video/*"
        type = 5
    } else {
        type = downType.toByte()
    }
    this.pageState.fromMenu = false
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaOpenDownloadDialogTask(this, url = url, disposition = disposition, mime = dialogMime, referer = referer, title = popupTitle, fileName = videoDownListenerText, faceItems = arrayList, listItems = downloadItems, pageUrl = this.currentPage!!.getUrl(), pageTitle = this.currentPage!!.getTitle(), length = length, type = type, external = externalDownload, index = videoJsonIndex, fromBridge = downloadFromBridge))
}

fun WebViewActivity.finishCoverView() {
    val webNestView: WebNestView? = if (this.coverView == null) null else this.currentPage
    if (this.coverView == null || webNestView == null) {
        removeCoverView()
        return
    }
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return
    }
    myWebBody.postDelayed(WvaCoverViewCheck(this, scrollRange = webNestView.computeVerticalScrollRange()), 200L)
}

fun WebViewActivity.hidePageOverlays(hide: kotlin.Boolean) {
    hideMoveHint()
    val webAreaText = this.linkTitlePill
    if (webAreaText != null) {
        webAreaText.hideSoon()
    }
    val webAreaMenu = this.videoState.areaMenu
    if (webAreaMenu != null) {
        webAreaMenu.hideSoon()
    }
    if (this.dialogsState.areaToolbar != null) {
        if ((hide == true)) {
            evalJs(
                this.currentPage,
                "(function(){window.sblast=null;window.sbchkc=false;myClr();document.removeEventListener('touchstart',myTc1);document.removeEventListener('touchmove',myTc2);document.removeEventListener('touchend',myTc3);})();",
                true
            )
        }
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaRemoveAreaToolbar(this))
    }
}

fun WebViewActivity.applyTextZoom() {
    val webNestView = this.currentPage
    if (webNestView != null) {
        val textZoom = webNestView.getSettings().getTextZoom()
        if (textZoom != ZtriSettings.zoomSize.value) {
            this.pageState.textZoomPending = false
            this.pageState.textZoomNudged = false
            this.currentPage!!.getSettings().setTextZoom(ZtriSettings.zoomSize.value)
        } else if (textZoom != ZoneSettings.textSize.value) {
            this.pageState.textZoomPending = false
            this.pageState.textZoomNudged = false
            this.currentPage!!.getSettings().setTextZoom(ZoneSettings.textSize.value)
        }
    }
}
