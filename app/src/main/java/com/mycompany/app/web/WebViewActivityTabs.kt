@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import com.mycompany.app.web.WebNestFrame.PageSwipeUpdate
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.web.WebNestFrame.TabYAnimListener
import com.mycompany.app.web.WebTabBarSubView.RemoveTabBarSubViewStep
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.parseJson
import com.mycompany.app.main.MainUtil.snsJs2
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.snsUrl
import com.mycompany.app.main.MainUtil.snsJs1
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.searchUrlFor
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.imageUrlFor
import com.mycompany.app.main.MainUtil.urlInParens
import com.mycompany.app.main.MainUtil.fileNumberSuffix
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.setUserAgent
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.mediaNameFor
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isOfflineMht
import com.mycompany.app.main.MainUtil.offlineUrlFor
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.isSecretLocked
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.restartActivity
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.applyVideoRotation
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.extractRedirect
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.loadDrawable
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
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.phTitle
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUtil.freeMemory
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.main.MainUtil.newMediaUri
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
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.schemeKind
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.addShortcut
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.main.MainUtil.injectVideoObserver
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.navBarInset
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.applyScreenOrientation
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUtil.mimeForPath
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.appLocaleTag
import android.text.TextUtils
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.view.ViewGroup
import com.mycompany.app.web.WebNestView
import android.webkit.WebViewClient
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import android.animation.ValueAnimator
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.quick.QuickView
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefMain
import android.os.Build
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.PopMenuItem
import com.mycompany.app.ui.browser.ComposeTabOverview
import com.mycompany.app.dialog.DialogDeleteItem
import com.mycompany.app.dialog.DialogSetMsg
import com.mycompany.app.web.WebViewActivity.TabViewListener
import com.mycompany.app.web.WebViewActivity.PrevPageListener
import com.mycompany.app.dialog.DialogSetUrl
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.view.MyBarView
import android.webkit.JsResult
import android.os.Handler
import android.view.View
import ch.opumlabs.opumbrowser.R
import java.lang.Exception
import java.util.ArrayList
import kotlin.math.abs

/*
 * The browser activity's tabs functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.finishPageAnim(direction: Int, pageSwipeForward: kotlin.Boolean) {
    if (this.tabsState.currentFrame == null || this.currentPage == null) {
        return
    }
    val webNestView = this.animPage
    if (webNestView != null) {
        val valueAnimator = webNestView.pageAnimator
        if (valueAnimator != null) {
            valueAnimator.cancel()
            webNestView.pageAnimator = null
        }
        this.animPage = null
    }
    val webNestFrame = this.animFrame
    if (webNestFrame != null) {
        webNestFrame.cancelAnims()
        this.animFrame = null
    }
    this.tabsState.pageSwipeState = 0.toByte()
    this.pageSwipeForward = false
    this.dialogsState.swipePage = null
    this.tabsState.swipeFrame = null
    this.currentPage!!.resetSwipeShade()
    this.tabsState.currentFrame!!.resetFakeX()
    val currentFrame = this.tabsState.currentFrame
    if (direction != 4) {
        currentFrame!!.trimBefore()
        return
    }
    val pageIndex =
        if ((pageSwipeForward == true)) currentFrame!!.pageIndex - 1 else currentFrame!!.pageIndex + 1
    val pageItemX = this.tabsState.currentFrame!!.pageItemAt(pageIndex)
    if (pageItemX == null) {
        this.tabsState.currentFrame!!.trimBefore()
        this.tabsState.goBackAnimBusy = false
        val currentPage = this.currentPage
        if ((pageSwipeForward == true)) {
            currentPage!!.goBack()
            return
        } else {
            currentPage!!.goForward()
            return
        }
    }
    finishProgress()
    exitFind(true)
    stopWebClean()
    hidePageOverlays(true)
    val nextPage = pageItemX.webNestView
    if (nextPage != null && nextPage.getVisibility() != View.VISIBLE) {
        if (!TextUtils.isEmpty(nextPage.blankPageUrl)) {
            nextPage.setBackgroundColor(0)
        }
        nextPage.setVisibility(View.VISIBLE)
    }
    this.currentPage!!.setWebViewClient(MainUtil.quietWebClient)
    this.currentPage!!.setWebChromeClient(null)
    this.currentPage!!.setDownloadListener(null)
    this.currentPage!!.setVisibility(View.GONE)
    this.barsState.pageItem = pageItemX
    this.barsState.animPageIndex = pageIndex
}

fun WebViewActivity.revealPrevTab(tabY: kotlin.Float, animate: kotlin.Boolean) {
    if ((animate == true)) {
        val webNestFrame = this.swipeTabFrame
        if (webNestFrame != null) {
            if (webNestFrame.animateTabY(2)) {
                this.tabsState.tabSwipeState = 2.toByte()
                this.tabsState.swipePrevFrame = this.swipeTabFrame
                this.tabsState.tabSwipeForward = false
            }
            this.swipeTabFrame = null
            return
        }
        return
    }
    var size = this.currentTabIndex - 1
    val tabItems = this.tabsState.tabItems
    if (tabItems != null) {
        if (size < 0) {
            size = tabItems.size - 1
        }
        val arrayList: ArrayList<WebTabItem?>? = if (this.tabsState.currentFrame == null) null else this.tabsState.tabItems
        if (this.tabsState.currentFrame == null || arrayList == null || size < 0 || size >= arrayList.size) {
            return
        }
        try {
            if (size != this.currentTabIndex) {
                val webTabItem = this.tabsState.tabItems!!.get(size)
                if (webTabItem == null) {
                    return
                }
                val webFrame = webTabItem.webFrame
                if (webFrame != null) {
                    val pageValid = webFrame.pageValid
                    if (pageValid != null && pageValid.blankPending) {
                        pageValid.setBarColors(pageThemeColor(), pageThemeLight())
                    }
                    if (webTabItem.webFrame!!.getVisibility() != View.VISIBLE) {
                        val targetIndex = this.pageLayout!!.indexOfChild(webTabItem.webFrame)
                        val currentIndex = this.pageLayout!!.indexOfChild(this.tabsState.currentFrame)
                        if (targetIndex == -1 || targetIndex > currentIndex) {
                            this.pageLayout!!.moveFrameTo(webTabItem.webFrame, currentIndex)
                        }
                        webTabItem.webFrame!!.setVisibility(View.VISIBLE)
                    }
                }
            }
            val currentFrame = this.tabsState.currentFrame
            this.swipeTabFrame = currentFrame
            currentFrame!!.tabY = tabY
        } catch (unused: Exception) {
        }
    }
}

fun WebViewActivity.closeJsMessageDialog() {
    val jsResult = this.tabsState.jsResult
    if (jsResult != null) {
        jsResult.cancel()
        this.tabsState.jsResult = null
    }
    val dialogSetMsg = this.jsMessageDialog
    if (dialogSetMsg != null) {
        dialogSetMsg.dismiss()
        this.jsMessageDialog = null
    }
}

fun WebViewActivity.dragNextPage(pageX: kotlin.Float) {
    var pageX = pageX
    val webNestFrame = this.tabsState.currentFrame
    if (webNestFrame == null || this.currentPage == null) {
        return
    }
    var started = true
    try {
        val pageItemX = this.tabsState.currentFrame!!.pageItemAt(webNestFrame.pageIndex + 1)
        if (pageItemX != null) {
            val webNestView = pageItemX.webNestView
            if (webNestView == null) {
                if (this.tabsState.swipeFrame == null && this.tabsState.layoutRtl) {
                    pageX = 0.0f
                }
                val currentFrame = this.tabsState.currentFrame
                this.tabsState.swipeFrame = currentFrame
                currentFrame!!.setSwipeOffset(pageX, true)
                return
            }
            var attached = false
            if (webNestView.getVisibility() != View.VISIBLE) {
                if (this.tabsState.layoutRtl) {
                    webNestView.pageX = 0.0f
                } else {
                    started = false
                }
                if (!TextUtils.isEmpty(webNestView.blankPageUrl)) {
                    webNestView.setBackgroundColor(0)
                }
                webNestView.setVisibility(View.VISIBLE)
                attachAnimListener(webNestView)
                attached = started
            }
            this.dialogsState.swipePage = webNestView
            if (attached) {
                return
            }
            webNestView.pageX = pageX
        }
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.finishTabAnim(direction: Int, tabSwipeForward: kotlin.Boolean) {
    val webNestFrame = this.tabsState.swipePrevFrame
    if (webNestFrame != null) {
        webNestFrame.cancelAnims()
        this.tabsState.swipePrevFrame = null
    }
    val swipeNextFrame = this.tabsState.swipeNextFrame
    if (swipeNextFrame != null) {
        swipeNextFrame.cancelAnims()
        this.tabsState.swipeNextFrame = null
    }
    val myWebCoord = this.webCoord
    if (myWebCoord != null) {
        myWebCoord.clearCloseHint()
    }
    var size = 0
    this.tabsState.tabSwipeState = 0.toByte()
    this.tabsState.tabSwipeForward = false
    this.swipeTabFrame = null
    this.swipeNextTabFrame = null
    this.barsState.addressEditAfterAdd = false
    if (direction == 2) {
        if (this.currentPage == null || this.tabsState.finishTabAnimBusy) {
            return
        }
        this.tabsState.finishTabAnimBusy = true
        val currentTab = tabItemAt(this.currentTabIndex)
        if (currentTab == null) {
            this.tabsState.finishTabAnimBusy = false
            return
        }
        finishProgress()
        exitFind(true)
        stopWebClean()
        hidePageOverlays(true)
        val webCoord = this.webCoord
        if (webCoord != null) {
            webCoord.post(WvaFinishTabAnimTask(this, finishTabAnimTabItem = currentTab))
            return
        }
        return
    }
    if (direction == 3) {
        selectTab(this.currentTabIndex, false)
        return
    }
    if (direction == 4) {
        val currentTabIndex = this.currentTabIndex
        val neighbor = if ((tabSwipeForward == true)) currentTabIndex - 1 else currentTabIndex + 1
        if (!ZtwoSettings.tabLoop.value) {
            size = neighbor
        } else if (!tabSwipeForward) {
            val arrayList = this.tabsState.tabItems
            if (arrayList == null) {
                return
            }
            if (neighbor < arrayList.size) {
                size = neighbor
            } else if (this.currentTabIndex == 0) {
                return
            }
        } else if (neighbor < 0) {
            val tabItems = this.tabsState.tabItems
            val size = if (tabItems == null) 0 else (tabItems.size - 1)
            if (tabItems == null || size == this.currentTabIndex) {
                return
            }
        } else {
            size = neighbor
        }
        selectTab(size, true)
    }
}

fun WebViewActivity.dragNextTab(tabX: kotlin.Float, animate: kotlin.Boolean) {
    var tabX = tabX
    val found: kotlin.Boolean
    var attached = false
    if ((animate == true)) {
        animateTabSwipe(4, false)
        return
    }
    var index = this.currentTabIndex + 1
    if (ZtwoSettings.tabLoop.value) {
        val tabItems = this.tabsState.tabItems
        if (tabItems == null) {
            return
        }
        if (index >= tabItems.size) {
            if (this.currentTabIndex == 0) {
                return
            } else {
                index = 0
            }
        }
    }
    val arrayList: ArrayList<WebTabItem?>? = if (this.tabsState.currentFrame == null) null else this.tabsState.tabItems
    if (this.tabsState.currentFrame == null || arrayList == null || index < 0 || index >= arrayList.size) {
        return
    }
    try {
        val webTabItem = this.tabsState.tabItems!!.get(index)
        if (webTabItem != null) {
            val webNestFrame = webTabItem.webFrame
            if (webNestFrame == null) {
                if (this.swipeNextTabFrame == null && this.tabsState.layoutRtl) {
                    tabX = 0.0f
                }
                val currentFrame = this.tabsState.currentFrame
                this.swipeNextTabFrame = currentFrame
                currentFrame!!.setSwipeOffset(tabX, false)
                return
            }
            val pageValid = webNestFrame.pageValid
            if (pageValid != null && pageValid.blankPending) {
                pageValid.setBarColors(pageThemeColor(), pageThemeLight())
            }
            if (webTabItem.webFrame!!.getVisibility() != View.VISIBLE) {
                if (this.tabsState.layoutRtl) {
                    webTabItem.webFrame!!.tabX = 0.0f
                    found = true
                } else {
                    found = false
                }
                val targetIndex = this.pageLayout!!.indexOfChild(webTabItem.webFrame)
                val currentIndex = this.pageLayout!!.indexOfChild(this.tabsState.currentFrame)
                if (targetIndex == -1) {
                    this.pageLayout!!.moveFrameTo(webTabItem.webFrame, currentIndex + 1)
                } else if (targetIndex < currentIndex) {
                    this.pageLayout!!.moveFrameTo(webTabItem.webFrame, currentIndex)
                }
                webTabItem.webFrame!!.setVisibility(View.VISIBLE)
                attached = found
            }
            val webFrame = webTabItem.webFrame
            this.swipeTabFrame = webFrame
            if (attached) {
                return
            }
            webFrame!!.tabX = tabX
        }
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.dragPrevTab(tabX: kotlin.Float, direction: Int) {
    var size = this.currentTabIndex - 1
    if (ZtwoSettings.tabLoop.value) {
        val tabItems = this.tabsState.tabItems
        if (tabItems == null) {
            return
        }
        val size = if (size < 0) (tabItems.size - 1) else 0
        if (size < 0 && size == this.currentTabIndex) {
            return
        }
    }
    val arrayList: ArrayList<WebTabItem?>? = if (this.tabsState.currentFrame == null) null else this.tabsState.tabItems
    if (this.tabsState.currentFrame == null || arrayList == null || size < 0 || size >= arrayList.size) {
        return
    }
    try {
        val webTabItem = this.tabsState.tabItems!!.get(size)
        if (webTabItem != null) {
            val webNestFrame = webTabItem.webFrame
            if (webNestFrame != null) {
                val pageValid = webNestFrame.pageValid
                if (pageValid != null && pageValid.blankPending) {
                    pageValid.setBarColors(pageThemeColor(), pageThemeLight())
                }
                if (webTabItem.webFrame!!.getVisibility() != View.VISIBLE) {
                    val targetIndex = this.pageLayout!!.indexOfChild(webTabItem.webFrame)
                    val currentIndex = this.pageLayout!!.indexOfChild(this.tabsState.currentFrame)
                    if (targetIndex == -1 || targetIndex > currentIndex) {
                        this.pageLayout!!.moveFrameTo(webTabItem.webFrame, currentIndex)
                    }
                    webTabItem.webFrame!!.setVisibility(View.VISIBLE)
                }
            }
            val currentFrame = this.tabsState.currentFrame
            this.swipeTabFrame = currentFrame
            currentFrame!!.tabX = tabX
            if (this.tabsState.tabSwipeActive) {
                this.webCoord!!.updateCloseHint(tabX, this.tabsState.layoutRtl)
            }
        }
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.finishPageSwipe(direction: Int) {
    if (direction != 0) {
        if (this.dialogsState.swipePage == null && this.tabsState.swipeFrame == null) {
            return
        }
        var pageSwipeForward = direction == 1
        if (this.tabsState.layoutRtl) {
            if (pageSwipeForward) {
                if (pageSwipeX() > pageAreaWidth() / 2) {
                    animatePageSwipe(4, false)
                    return
                }
            } else if (pageSwipeX() < pageAreaWidth() / 2) {
                animatePageSwipe(4, true)
                return
            }
            pageSwipeForward = !pageSwipeForward
        } else if (pageSwipeForward) {
            if (pageSwipeX() > pageAreaWidth() / 2) {
                animatePageSwipe(4, true)
                return
            }
        } else if (pageSwipeX() < pageAreaWidth() / 2) {
            animatePageSwipe(4, false)
            return
        }
        animatePageSwipe(3, pageSwipeForward)
    }
}

fun WebViewActivity.settleTabSwipe(direction: Int) {
    val webNestFrame: WebNestFrame? = if (direction == 0) null else this.swipeTabFrame
    if (direction == 0 || webNestFrame == null) {
        return
    }
    if (direction == 3) {
        val tabY = webNestFrame.tabY
        if (tabY > (this.dialogsState.webBody!!.rectBottom / 2) - MainApp.dp48) {
            revealPrevTab(tabY, true)
            return
        }
        if (this.swipeTabFrame!!.animateTabY(3)) {
            this.tabsState.tabSwipeState = 3.toByte()
            this.tabsState.swipePrevFrame = this.swipeTabFrame
            this.tabsState.tabSwipeForward = false
        }
        this.swipeTabFrame = null
        return
    }
    var forward = direction == 1
    if (this.tabsState.layoutRtl) {
        if (forward) {
            if (tabSwipeX() > pageAreaWidth() / 2) {
                animateTabSwipe(4, false)
                return
            }
        } else if (tabSwipeX() < pageAreaWidth() / 2) {
            if (this.tabsState.tabSwipeActive) {
                animateTabSwipe(2, true)
                return
            } else {
                animateTabSwipe(4, true)
                return
            }
        }
        forward = !forward
    } else if (forward) {
        if (tabSwipeX() > pageAreaWidth() / 2) {
            if (this.tabsState.tabSwipeActive) {
                animateTabSwipe(2, true)
                return
            } else {
                animateTabSwipe(4, true)
                return
            }
        }
    } else if (tabSwipeX() < pageAreaWidth() / 2) {
        animateTabSwipe(4, false)
        return
    }
    animateTabSwipe(3, forward)
}

fun WebViewActivity.showSetUrlDialog(isLink: kotlin.Boolean) {
    val cookieLabel: String?
    var found: kotlin.Boolean
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeSetUrlDialog()
    val webNestView: WebNestView? = if ((isLink == true)) null else this.currentPage
    if ((isLink == true) || webNestView == null) {
        cookieLabel = null
        found = false
    } else {
        val url = webNestView.getUrl()
        if (TextUtils.isEmpty(url) || "file:///android_asset/shortcut.html" == url || "about:blank" == url) {
            cookieLabel = null
        } else {
            cookieLabel = getString(AppR.string.delete_cookie) + " (" + stripWww(this.dialogsState.pageHost) + ")"
            if (url!!.startsWith("https://")) {
                found = true
            }
        }
        found = false
    }
    val menuLabel = cookieLabel
    val dialogTheme = if (isLandscapeScreen()) 0 else AppR.style.DialogExpandTheme
    val dialogSetUrl = DialogSetUrl(this, dialogTheme, isLink, menuLabel, found, WvaSetUrlMenuListener(this, isLink))
    this.tabsState.setUrlDialog = dialogSetUrl
    dialogSetUrl.setOnDismissListener(SetUrlDialogDismissed(this))
    if (MainConst.isApi30Insets || dialogTheme != 0) {
        this.tabsState.setUrlDialog!!.applyColors(this.onStartPage, useDarkWindow(), pageThemeColor(), navBarColorNow(), true)
    }
}

fun WebViewActivity.bindTabBar(themeColor: Int, themeLight: Int) {
    if (this.barsState.touchFrame == null || this.tabBarView == null) {
        return
    }
    this.tabBar = WebTabBar(this.dialogsState.appContext, this.tabsState.tabItems, this.currentTabIndex, this.onStartPage, themeColor, themeLight, WvaTabBarListener(this))
    val webTabBarAdapter = this.tabBar
    val tabBar = this.tabBarView
    webTabBarAdapter!!.composeBar = tabBar
    tabBar!!.adapter = webTabBarAdapter
    tabBar.dragListener = WvaTabDragListener(this)
    tintTabBar(themeColor, themeLight, false)
    if (this.currentTabIndex > 2) {
        this.tabBarView!!.postDelayed(WvaTabBarScrollToCurrent(this), 100L)
    }
}

fun WebViewActivity.showTabGroupDeleteDialog(index: Int) {
    if (this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeTabGroupDeleteDialog()
    val tab = tabItemAt(index)
    if (tab == null || tab.groupId == 0L) {
        return
    }
    closeSnackbar(false)
    val dialogDeleteItem = DialogDeleteItem(this, WvaTabGroupDeleteListener(this, showTabGroupDeleteDialogTabItem = tab, index = index, groupId = tab.groupId))
    this.tabGroupDeleteDialog = dialogDeleteItem
    dialogDeleteItem.setOnDismissListener(WvaTabListDialogDismiss(this))
}

fun WebViewActivity.refreshTabBar(scrollPos: Int, force: kotlin.Boolean) {
    clampTabIndex()
    if (this.currentPage == null || this.tabBarView == null || this.tabBar == null || this.tabsState.tabItems == null) {
        return
    }
    val currentTabIndex = this.currentTabIndex
    val pageColor = pageThemeColor()
    val pageLight = pageThemeLight()
    this.tabBar!!.applyTint(this.tabsState.tabItems, currentTabIndex, this.onStartPage, pageColor, pageLight, force)
    tintSideButton(pageColor, pageLight)
    var size = 0
    val arrayList: ArrayList<WebTabItem?>? = if (scrollPos != 0) this.tabsState.tabItems else null
    if (scrollPos != 0 && arrayList != null) {
        size = arrayList.size
    }
    if (size > 2) {
        this.tabBarView!!.postDelayed(WvaTabBarScrollTo(this, currentTabIndex, scrollPos), 100L)
    }
}

fun WebViewActivity.animatePageSwipe(direction: Int, pageSwipeForward: kotlin.Boolean) {
    var width: Int = 0
    val parent: Any?
    val webNestView = this.dialogsState.swipePage
    if (webNestView == null) {
        val webNestFrame = this.tabsState.swipeFrame
        if (webNestFrame != null) {
            if (webNestFrame.animateTabX(direction, pageSwipeForward)) {
                this.tabsState.pageSwipeState = direction.toByte()
                this.animFrame = this.tabsState.swipeFrame
                this.pageSwipeForward = pageSwipeForward
            }
            this.tabsState.swipeFrame = null
            return
        }
        return
    }
    attachAnimListener(webNestView)
    val swipePage = this.dialogsState.swipePage
    if (direction == 0) {
        swipePage!!.javaClass
    } else if (swipePage!!.pageAnimator == null && ((swipePage.getWidth()
            .also { width = it }) != 0 || ((swipePage.getParent()
            .also { parent = it }) != null && ((parent as View).getWidth()
            .also { width = it }) != 0))
    ) {
        swipePage.ensureShadePaints()
        val targetX =
            if (if (!swipePage.layoutRtl) !(if (!pageSwipeForward) direction == 3 else direction != 3) else !(if (!pageSwipeForward) direction != 3 else direction == 3)) 0 else width
        val float = targetX.toFloat()
        if ((swipePage.swipeOffset).compareTo(float) == 0) {
            swipePage.notifyAnimEnd(direction, pageSwipeForward)
        } else {
            swipePage.animTargetX = targetX
            swipePage.animIndex = direction.toByte()
            swipePage.animForward = (pageSwipeForward) == true
            val swipeOffset = swipePage.swipeOffset
            swipePage.animValue = swipeOffset
            swipePage.animRunning = false
            val valueAnimatorOfFloat = ValueAnimator.ofFloat(swipeOffset, float)
            swipePage.pageAnimator = valueAnimatorOfFloat
            valueAnimatorOfFloat.setDuration(((abs(float - swipePage.swipeOffset) / width) * 300.0f).toLong())
            swipePage.pageAnimator!!.addUpdateListener(swipePage.PageAnimUpdate())
            swipePage.pageAnimator!!.addListener(swipePage.NotifyAnimEndAnimListener())
            swipePage.pageAnimator!!.start()
            this.tabsState.pageSwipeState = direction.toByte()
            this.animPage = this.dialogsState.swipePage
            this.pageSwipeForward = (pageSwipeForward) == true
        }
    }
    this.dialogsState.swipePage = null
}

fun WebViewActivity.showTabOverview() {
    if (this.tabsState.gestureDetector == null || this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeTabOverview()
    var fastList = false
    closeSnackbar(false)
    val miniMode = ZoneSettings.tabMiniMode.value
    this.tabOverviewAnim = miniMode
    if (miniMode && ZoneSettings.tabListType.value == 0) {
        fastList = true
    }
    this.tabOverviewFast = fastList
    // the picture of the page as it is now (the overview's zoom and card show it); else the queued capture as before
    val picture = captureTabPreviewNow(this.currentTabIndex)
    if (picture != null) {
        if (!this.isExiting) this.tabsState.currentFrame?.let { ReloadCover.take(it) }
    } else {
        captureTabPreview(this.currentTabIndex)
    }
    // a switch to another space from the overview shows this space's screen when it is opened again
    SpaceCover.takeOnOverview(this)
    if (!this.tabOverviewAnim) {
        pausePage()
    }
    val overview = ComposeTabOverview(this, this.tabsState.tabItems, TabOverviewListener(this), currentPicture = picture)
    this.tabsState.composeOverview = overview
    overview.setOnDismissListener { dialogInterface ->
        this.tabsState.composeOverview = null
        TabOverviewDismissed(this).onDismiss(dialogInterface)
    }
}

fun WebViewActivity.closeLoadImgDialog() {
    val dialogLoadImg = this.loadImgDialog
    if (dialogLoadImg != null) {
        dialogLoadImg.dismiss()
        this.loadImgDialog = null
    }
}

fun WebViewActivity.showMiniTabOverview() {
    if (this.tabsState.gestureDetector == null || this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    closeMiniTabOverview()
    closeSnackbar(false)
    val miniMode = ZoneSettings.tabMiniMode.value
    this.tabOverviewAnim = miniMode
    this.tabOverviewFast = miniMode && ZoneSettings.tabListType.value == 0
    captureTabPreview(this.currentTabIndex)
    SpaceCover.takeOnOverview(this)
    val landscape = isLandscapeScreen()
    val dialogTheme = if (landscape) 0 else AppR.style.DialogExpandTheme
    if (!this.tabOverviewAnim) {
        pausePage()
    }
    val overview = ComposeTabOverview(this, this.tabsState.tabItems, MiniTabOverviewListener(this), mini = true)
    this.tabsState.composeOverview = overview
    overview.setOnDismissListener { dialogInterface ->
        this.tabsState.composeOverview = null
        MiniTabOverviewDismissed(this).onDismiss(dialogInterface)
    }
}

fun WebViewActivity.closePopupMenuDialog() {
    val dialogPopupMenu = this.popupMenuDialog
    if (dialogPopupMenu != null) {
        dialogPopupMenu.dismiss()
        this.popupMenuDialog = null
    }
}

fun WebViewActivity.closePreImageDialog() {
    val dialogPreImage = this.preImageDialog
    if (dialogPreImage != null) {
        dialogPreImage.dismiss()
        this.preImageDialog = null
    }
    this.dialogsState.linkActionUrl = null
}

fun WebViewActivity.onBarItemClick(myBarView: MyBarView?, view: View?, itemId: Int) {
    val webNestFrame: WebNestFrame?
    val pageValid: WebNestView?
    val list: MutableList<WebNestFrame.PageItem?>?
    if (this.tabsState.gestureDirection.toInt() != 0 || myBarView == null) {
        return
    }
    if (itemId == 1001) {
        showTransControl(view)
        return
    }
    if (itemId == 1002) {
        showSiteSecurity()
        return
    }
    if (itemId == 35) {
        if (myBarView.iconLoading) {
            if (this.currentPage == null) {
                return
            }
            if ((this.nextPageAnim && ((((this.tabsState.currentFrame.also { webNestFrame = it })!!.pageValid.also {
                    pageValid = it
                }) != null && pageValid!!.canGoBack()) || ((webNestFrame!!.pageItems.also {
                    list = it
                }) != null && list!!.size >= 2 && webNestFrame.currentPageIndex > 0))) || this.addTabAnim) {
                goBackFromBackKey(WvaPrevBackListener(this))
                return
            } else {
                updatePageProgress(this.currentPage!!.getProgress())
                this.currentPage!!.stopLoading()
                return
            }
        }
        myBarView.setIconLoad(true)
    } else if (itemId == 51) {
        this.barItemView = view
    }
    runMenuItem(view, itemId)
}

fun WebViewActivity.closeQuickEditDialog() {
    val dialogQuickEdit = this.dialogsState.quickEditDialog
    if (dialogQuickEdit != null) {
        dialogQuickEdit.dismiss()
        this.dialogsState.quickEditDialog = null
    }
}

fun WebViewActivity.pageSwipeX(): kotlin.Float {
    val webNestView = this.dialogsState.swipePage
    if (webNestView != null) {
        return webNestView.pageX
    }
    val webNestFrame = this.tabsState.swipeFrame
    if (webNestFrame != null) {
        return webNestFrame.fakeX
    }
    return 0.0f
}

fun WebViewActivity.isFrameIdle(): kotlin.Boolean {
    val webNestFrame = this.tabsState.currentFrame
    if (webNestFrame != null) {
        return if (!webNestFrame.inserting && webNestFrame.addAnim == null && webNestFrame.backAnim == null && this.tabsState.tabSwipeState.toInt() == 0 && this.tabsState.pageSwipeState.toInt() == 0) false else true
    }
    return false
}

fun WebViewActivity.closeSetUrlDialog() {
    val dialogSetUrl = this.tabsState.setUrlDialog
    if (dialogSetUrl != null) {
        dialogSetUrl.dismiss()
        this.tabsState.setUrlDialog = null
    }
}

fun WebViewActivity.closeDeleteItemDialog() {
    val dialogDeleteItem = this.deleteItemDialog
    if (dialogDeleteItem != null) {
        dialogDeleteItem.dismiss()
        this.deleteItemDialog = null
    }
}

fun WebViewActivity.closeTabOverview() {
    resumePausedPage()
    val overview = this.tabsState.composeOverview
    if (overview != null) {
        this.tabsState.composeOverview = null
        overview.dismiss()
    }
}

fun WebViewActivity.refreshAfterPageChange() {
    var pageHandler: Handler? = null
    val currentPage = this.currentPage
    if (currentPage != null && isDarkMode() && ((currentPage.drawBackdrop || currentPage.drawBackdropOnce) && currentPage.getHeight() - MainSettings.statusHeight.value <= currentPage.getWidth() && (currentPage.uiHandler.also {
            pageHandler = it
        }) != null)) {
        pageHandler!!.post(currentPage.ClearBackdropTask())
    }
    val webNestFrame = this.tabsState.currentFrame
    val handler: Handler? = if (webNestFrame != null && isDarkMode() && webNestFrame.keepVisible && webNestFrame.getHeight() - MainSettings.statusHeight.value <= webNestFrame.getWidth()) webNestFrame.uiHandler else null
    if (webNestFrame != null && isDarkMode() && webNestFrame.keepVisible && webNestFrame.getHeight() - MainSettings.statusHeight.value <= webNestFrame.getWidth() && handler != null) {
        handler.post(webNestFrame.ClearBackdropTask())
    }
    val webNestView: WebNestView? = if (!this.onStartPage) null else this.currentPage
    val quickView: QuickView? = if (!this.onStartPage || webNestView == null) null else webNestView.quickPanel
    if (!this.onStartPage || webNestView == null || quickView == null) {
        return
    }
    quickView.refreshPending = false
    if (quickView.refreshed) {
        quickView.refreshed = false
        quickView.notifyShown(false)
    }
}

fun WebViewActivity.tabBarItemAtTouch(): Int {
    val touchTabIndex = this.tabsState.touchTabIndex
    if (touchTabIndex != -1) {
        return touchTabIndex
    }
    val webTabBarAdapter = this.tabBar
    if (webTabBarAdapter != null) {
        val int = this.tabsState.touchDownX.toInt()
        val touchY = this.findState.touchDownY.toInt()
        val tabBar = this.tabBarView
        val position = if (tabBar == null) -1 else tabBar.tabAt(int, touchY)
        val webTabItemB = if (position == -1) null else webTabBarAdapter.itemAt(position)
        if (webTabItemB != null) {
            return if (webTabItemB.groupTabs != null) WebTabBar.groupColor(webTabItemB) + 10000 else webTabItemB.position
        }
    }
    return -1
}

fun WebViewActivity.closeMiniTabOverview() {
    resumePausedPage()
    val overview = this.tabsState.composeOverview
    if (overview != null) {
        this.tabsState.composeOverview = null
        overview.dismiss()
    }
}

fun WebViewActivity.closeTabPage(webTabItem: WebTabItem?, prevPageListener: PrevPageListener?) {
    if (webTabItem == null || this.currentPage == null) {
        if (prevPageListener != null) {
            prevPageListener.onDone()
        }
        this.tabsState.goBackPageBusy = false
    } else {
        val myWebCoord = this.webCoord
        if (myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaCloseTabPageTask(this, closeTabPageTabItem = webTabItem, closeTabPageBackListener = prevPageListener))
    }
}

fun WebViewActivity.isSwipeMoving(): kotlin.Boolean {
    val webNestView = this.dialogsState.swipePage
    if (webNestView == null) {
        val webNestFrame = this.tabsState.swipeFrame
        if (webNestFrame != null && webNestFrame.swipeOffset > 0.0f) {
            return true
        }
    } else if (webNestView.swipeOffset > 0.0f) {
        return true
    }
    return false
}

fun WebViewActivity.restartWithTab() {
    closeMenuDialogs()
    if (this.tabsState.restartBusy) {
        return
    }
    this.tabsState.restartBusy = true
    this.miscState.restartAppBusy = false
    val webNestView: WebNestView? = if (this.isExiting) null else this.currentPage
    if (this.isExiting || webNestView == null || webNestView.blankPending) {
        restartApp()
        return
    }
    val currentTab = tabItemAt(this.currentTabIndex)
    if (currentTab == null) {
        restartApp()
        return
    }
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    handler.postDelayed(WvaRestartAppTask(this), 1000L)
    this.currentPage!!.saveTabState(this.tabsState.currentFrame, currentTab.uid, currentTab.tabUrl, WvaRestartAppBundleListener(this))
}

fun WebViewActivity.tabSwipeX(): kotlin.Float {
    val webNestFrame = this.swipeTabFrame
    if (webNestFrame != null) {
        return webNestFrame.tabX
    }
    val swipeNextTabFrame = this.swipeNextTabFrame
    if (swipeNextTabFrame != null) {
        return swipeNextTabFrame.fakeX
    }
    return 0.0f
}

fun WebViewActivity.removeTabBarSubView() {
    val webTabBarSubView = this.tabBarSubView
    if (webTabBarSubView != null) {
        webTabBarSubView.release()
        val myWebBody = this.dialogsState.webBody
        if (myWebBody != null) {
            this.floatingState.overlays!!.removeView(this.tabBarSubView)
        }
        this.tabBarSubView = null
    }
}

fun WebViewActivity.showTabMenu(view: View?, groupStart: Int, position: Int, isGroup: kotlin.Boolean) {
    val webTabBarAdapter: WebTabBar?
    if (this.floatingState.popupMenu == null) {
        closeFloatingPopupMenu()
        if (view != null) {
            val arrayList: ArrayList<Any?> = ArrayList<Any?>()
            val tabItems = this.tabsState.tabItems
            if (tabItems != null && tabItems.size > 1) {
                if (groupStart != -1) {
                    arrayList.add(PopMenuItem(0, AppR.string.group_other_tab))
                }
                if (!isGroup || ((this.tabBar.also {
                        webTabBarAdapter = it
                    }) != null && webTabBarAdapter!!.count() > 1)) {
                    arrayList.add(PopMenuItem(1, AppR.string.delete_other_tab))
                }
                arrayList.add(PopMenuItem(2, AppR.string.delete_all_tab))
            }
            arrayList.add(PopMenuItem(3, AppR.string.new_url))
            arrayList.add(PopMenuItem(4, AppR.string.group_url))
            arrayList.add(PopMenuItem(5, AppR.string.back_url))
            if (!isGroup) {
                arrayList.add(PopMenuItem(6, AppR.string.copy_url))
                arrayList.add(PopMenuItem(7, AppR.string.share_url))
                arrayList.add(PopMenuItem(8, AppR.string.open_with))
            }
            val myPopupMenu = MyPopupMenu(
                this,
                this.dialogsState.webBody,
                view,
                arrayList,
                MainUtil.isDarkPage(this.onStartPage),
                WvaTabPopupListener(this, groupStart, position, isGroup)
            )
            this.floatingState.popupMenu = myPopupMenu
            this.popupMenu = myPopupMenu
            val myWebCoord = this.webCoord
            if (myWebCoord == null) {
                return
            }
            myWebCoord.post(WvaPopupMenuClosed(this))
        }
    }
}

fun WebViewActivity.offerTabUndo(webNestFrame: WebNestFrame?) {
    if (!this.tabsState.tabUndoAllowed && webNestFrame != null && this.webCoord != null) {
        webNestFrame.setVisibility(View.GONE)
        this.webCoord!!.post(WvaTabUndoSnackbar(this, undoFrame = webNestFrame))
    } else {
        this.tabsState.tabUndoAllowed = false
        if (webNestFrame != null) {
            webNestFrame.destroyFrame(null)
        }
    }
}

/**
 * URL of the current tab (falling back to the last requested URL), normalized by the web view.
 * Returns null when there is none; with `notify` a toast says why.
 */
fun WebViewActivity.currentPageUrl(notify: kotlin.Boolean): String? {
    if (this.currentPage == null) {
        return null
    }
    var url = this.currentPage!!.getUrl()
    if (TextUtils.isEmpty(url)) {
        url = this.dialogsState.pageUrl
        if (TextUtils.isEmpty(url)) {
            if ((notify == true)) {
                showToast(this, AppR.string.empty)
            }
            return null
        }
    }
    val normalized = this.currentPage!!.displayUrl(url)
    if (!TextUtils.isEmpty(normalized)) {
        return normalized
    }
    if ((notify == true)) {
        showToast(this, AppR.string.invalid_url)
    }
    return null
}

fun WebViewActivity.closeEngineMenu() {
    if (this.engineMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.engineMenu!!.dismissNow()
        this.engineMenu = null
    }
}

fun WebViewActivity.closePopupMenus() {
    closeAddressMenu()
    closeEngineMenu()
    closeAddressPrefixMenu()
    closeFloatingPopupMenu()
    closeTabBarItemMenu()
    closeQuickSettingsMenu()
    closeSubMenu()
    closeTransCtrlMenu()
    closeLockMenu()
}

fun WebViewActivity.closeLockMenu() {
    if (this.dialogsState.lockMenu != null) {
        this.popupMenu = null
        holdQuickMenuReopen()
        this.dialogsState.lockMenu!!.dismissNow()
        this.dialogsState.lockMenu = null
    }
}

fun WebViewActivity.animateTabSwipe(direction: Int, tabSwipeForward: kotlin.Boolean) {
    val shadeX: kotlin.Float
    val parent: Any?
    val webNestFrame = this.swipeTabFrame
    if (webNestFrame == null) {
        val swipeNextTabFrame = this.swipeNextTabFrame
        if (swipeNextTabFrame != null) {
            if (swipeNextTabFrame.animateTabX(direction, tabSwipeForward)) {
                this.tabsState.tabSwipeState = direction.toByte()
                this.tabsState.swipeNextFrame = this.swipeNextTabFrame
                this.tabsState.tabSwipeForward = tabSwipeForward
            }
            this.swipeNextTabFrame = null
            val myWebCoord = this.webCoord
            if (myWebCoord != null) {
                myWebCoord.clearCloseHint()
                return
            }
            return
        }
        return
    }
    if (direction != 0 && webNestFrame.tabYAnim == null) {
        webNestFrame.cancelAddAnim()
        var width = webNestFrame.getWidth()
        if (width != 0 || ((webNestFrame.getParent()
                .also { parent = it }) != null && ((parent as View).getWidth()
                .also { width = it }) != 0)
        ) {
            webNestFrame.ensureShadePaints()
            if (direction == 1) {
                if (webNestFrame.swipeReverse) {
                    shadeX = 0.0f
                    webNestFrame.pageX = 0.0f
                } else {
                    shadeX = width.toFloat()
                    webNestFrame.pageX = shadeX
                }
                webNestFrame.drawShade(shadeX, 1)
            }
            val startX =
                (if (if (!webNestFrame.swipeReverse) !(if (!tabSwipeForward) direction == 3 else direction != 3) else !(if (!tabSwipeForward) direction != 3 else direction == 3)) 0 else width).toFloat()
            if ((webNestFrame.pageX).compareTo(startX) == 0) {
                webNestFrame.animState = 0.toByte()
                webNestFrame.notifySwipeEnd(direction, tabSwipeForward)
            } else {
                webNestFrame.animState = direction.toByte()
                webNestFrame.animStartX = startX
                webNestFrame.swipeState = direction.toByte()
                webNestFrame.swipeForward = (tabSwipeForward) == true
                val pageX = webNestFrame.pageX
                webNestFrame.tabXAnimValue = pageX
                webNestFrame.tabXAnimRunning = false
                val valueAnimatorOfFloat = ValueAnimator.ofFloat(pageX, startX)
                webNestFrame.tabYAnim = valueAnimatorOfFloat
                valueAnimatorOfFloat.setDuration(((abs(startX - webNestFrame.pageX) / width) * 300.0f).toLong())
                webNestFrame.tabYAnim!!.addUpdateListener(webNestFrame.PageSwipeUpdate())
                webNestFrame.tabYAnim!!.addListener(webNestFrame.TabYAnimListener())
                webNestFrame.tabYAnim!!.start()
                this.tabsState.tabSwipeState = direction.toByte()
                this.tabsState.swipePrevFrame = this.swipeTabFrame
                this.tabsState.tabSwipeForward = (tabSwipeForward) == true
            }
        }
    }
    this.swipeTabFrame = null
    val webCoord = this.webCoord
    if (webCoord != null) {
        webCoord.clearCloseHint()
    }
}

fun WebViewActivity.tintTabBar(barColor: Int, themeLight: Int, apply: kotlin.Boolean) {
    var barColor = barColor
    var themeLight = themeLight
    if (this.barsState.touchFrame == null || this.tabBar == null) {
        return
    }
    tintSideButton(barColor, themeLight)
    if ((apply == true)) {
        val webTabBarAdapter = this.tabBar
        val currentTabIndex = this.currentTabIndex
        val onStartPage = this.onStartPage
        webTabBarAdapter!!.javaClass
        if (isDarkBarIcons(barColor, onStartPage)) {
            barColor = 0
            themeLight = 0
        }
        val list = webTabBarAdapter.shownItems
        if (list == null || currentTabIndex < 0 || currentTabIndex >= list.size) {
            return
        }
        val darkPage = MainUtil.isDarkPage(onStartPage)
        if (webTabBarAdapter.shownIndex == currentTabIndex && webTabBarAdapter.darkPage == darkPage && webTabBarAdapter.barColor == barColor) {
            return
        }
        webTabBarAdapter.selectedPos = webTabBarAdapter.barPosOf(currentTabIndex)
        webTabBarAdapter.shownIndex = currentTabIndex
        webTabBarAdapter.darkPage = darkPage
        webTabBarAdapter.barColor = barColor
        webTabBarAdapter.tintColor = if (barColor != 0) themeLight else 0
        webTabBarAdapter.tintMode = MainUtil.barTint(barColor, onStartPage)
        webTabBarAdapter.redrawAll()
        webTabBarAdapter.notifyAllTabs()
    }
}

/**
 * End of a tab hand-over: hide every other page frame that is still VISIBLE in the container (INVISIBLE ones are
 * left as they are). The loop in [finishTabFrameAnim] only covers the
 * tabs within 4 of the current index ([currentTabIndex]); when a second add starts before the first hand-over is done, the frame
 * that was on screen can sit outside that range (or no longer be in the tab list at that index) and stayed visible on top
 * of the new tab, taking its touches. Same handling as the loop: pause its pages, hide it, let it save its state.
 */
fun WebViewActivity.hideStrayFrames(current: WebNestFrame) {
    // Only once the new frame shows, so the screen never goes blank in between.
    if (current.visibility != View.VISIBLE) return
    val parent = current.parent as? android.view.ViewGroup ?: return
    var hidden = 0
    for (index in 0 until parent.childCount) {
        val child = parent.getChildAt(index) as? WebNestFrame ?: continue
        if (child === current || child.visibility != View.VISIBLE) continue
        try {
            child.trimAfter(true)
            child.visibility = View.GONE
            child.refreshPage()
            hidden++
        } catch (unused: Exception) {
        }
    }
    if (hidden != 0) TabFrameLog.step(this, "finishTabFrameAnim hid $hidden stray frame(s)", current)
}

fun WebViewActivity.finishTabFrameAnim() {
    val currentFrame = this.tabsState.currentFrame
    if (currentFrame != null) {
        if (currentFrame.touchBlocked || currentFrame.addAnim != null) {
            TabFrameLog.step(this, "finishTabFrameAnim still animating", currentFrame)
            currentFrame.startAddAnim()
            return
        }
        TabFrameLog.step(this, "finishTabFrameAnim", currentFrame, "addTabAnim=${this.addTabAnim} currentTabIndex=${this.currentTabIndex}")
        val nextPageAnim = this.nextPageAnim
        val addTabAnim = this.addTabAnim
        val animFromStartPage = this.animFromStartPage
        this.nextPageAnim = false
        this.addTabAnim = false
        this.animFromStartPage = false
        if (ZtwoSettings.addPage.value) {
            currentFrame.trimBefore()
        }
        val arrayList = this.tabsState.tabItems
        var size = if (arrayList != null) arrayList.size else 0
        if (arrayList != null && size != 0) {
            val currentTabIndex = this.currentTabIndex
            var index = currentTabIndex - 4
            val windowEnd = currentTabIndex + 4
            if (index < 0) {
                index = 0
            }
            if (windowEnd <= size) {
                size = windowEnd
            }
            val prevIndex = currentTabIndex - 1
            val nextIndex = currentTabIndex + 1
            while (index < size) {
                try {
                    val webTabItem: WebTabItem? = if (index != this.currentTabIndex) (arrayList.get(index)) else null
                    val webNestFrame: WebNestFrame? = if (index != this.currentTabIndex && webTabItem != null) webTabItem.webFrame else null
                    if (index != this.currentTabIndex && webTabItem != null && webNestFrame != null) {
                        val visibility = webNestFrame.getVisibility() != View.GONE
                        webTabItem.webFrame!!.trimAfter(visibility || index == prevIndex || index == nextIndex)
                        if (visibility) {
                            webTabItem.webFrame!!.setVisibility(View.GONE)
                        }
                        webTabItem.webFrame!!.refreshPage()
                    }
                } catch (unused: Exception) {
                }
                // Outside the try: a failing tab used to skip the increment and loop forever.
                index++
            }
        }
        hideStrayFrames(currentFrame)
        this.tabsState.currentFrame!!.setDarkMode(this.onStartPage)
        if (nextPageAnim) {
            this.tabsState.currentFrame!!.postBlankTime()
        }
        if (addTabAnim) {
            trimOldTabs()
        }
        val myWebCoord: MyWebCoord? = if (!animFromStartPage) null else this.webCoord
        if (!animFromStartPage || myWebCoord == null) {
            return
        }
        myWebCoord.post(WvaApplyPageColors(this))
    }
}

fun WebViewActivity.closeTabBarSubView() {
    val webTabBarSubView = this.tabBarSubView
    if (webTabBarSubView != null) {
        val myTabFrame = webTabBarSubView.popupFrame
        if (myTabFrame == null) {
            webTabBarSubView.release()
        } else {
            myTabFrame.setOpen(
                webTabBarSubView.anchorPos,
                webTabBarSubView.frameSize,
                false,
                webTabBarSubView.RemoveTabBarSubViewStep()
            )
        }
    }
}

fun WebViewActivity.showJsAlert(message: String?, jsResult: JsResult?) {
    if (this.isClosing) {
        return
    }
    closeJsMessageDialog()
    this.tabsState.jsResult = jsResult
    val dialogSetMsg = DialogSetMsg(this, message, AppR.string.ok, JsMessageDialogApply(this))
    this.jsMessageDialog = dialogSetMsg
    dialogSetMsg.setOnCancelListener(JsMessageDialogCancelled(this))
    this.jsMessageDialog!!.setOnDismissListener(JsMessageDialogDismissed(this))
}

fun WebViewActivity.createBackTab(backUrl: String?) {
    if (this.webCoord == null || this.tabsState.createBackTabBusy) {
        return
    }
    this.tabsState.createBackTabBusy = true
    settleSwipes()
    val isStartPage = "file:///android_asset/shortcut.html" == backUrl
    pauseWebDestroy()
    val webNestView = WebNestView(this)
    attachWebDestroy(webNestView)
    webNestView.startPage = isStartPage
    webNestView.setBackTab(true)
    webNestView.setDeskMode(false)
    val webNestFrame = WebNestFrame(this)
    webNestFrame.setDarkBars(isStartPage)
    webNestFrame.addPage(webNestView, 0, 0)
    webNestView.setScrollPos(ZoneSettings.scrollPos.value)
    webNestView.setWebViewClient(WvaBackTabClient())
    webNestView.setBackListener(WvaWebBackListener(this))
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaCreateBackTabTask(this, backTabPage = webNestView, backTabFrame = webNestFrame, backUrl = backUrl))
}

fun WebViewActivity.previewPrevTab() {
    val arrayList: ArrayList<WebTabItem?>? = if (this.currentPage == null || this.swipeTabFrame != null || this.swipeNextTabFrame != null) null else this.tabsState.tabItems
    val myWebCoord: MyWebCoord? = if (this.currentPage == null || this.swipeTabFrame != null || this.swipeNextTabFrame != null || arrayList == null || arrayList.size == 0) null else this.webCoord
    if (this.currentPage == null || this.swipeTabFrame != null || this.swipeNextTabFrame != null || arrayList == null || arrayList.size == 0 || myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaPreviewPrevTabTask(this))
}

fun WebViewActivity.onTabBarItemClick(touchTabIndex: Int, targetIndex: Int) {
    if (touchTabIndex == -1 || this.tabsState.tabBarBusy) {
        return
    }
    if (touchTabIndex == this.currentTabIndex) {
        runMenuItem(null, 38)
        return
    }
    if (touchTabIndex < 10000) {
        switchToTab(touchTabIndex)
        return
    }
    showTabGroupDeleteDialog(touchTabIndex - 10000)
    if (this.tabBar != null) {
        val myWebCoord = this.webCoord
        if (myWebCoord != null) {
            myWebCoord.post(WvaTabBarItemRefresh(this, targetIndex = targetIndex))
        }
    }
}

fun WebViewActivity.switchToTab(index: Int) {
    var frame: WebNestFrame?
    val webViewActivity: WebViewActivity?
    val tab = tabItemAt(index)
    if (tab == null) {
        this.tabsState.tabBarBusy = false
        return
    }
    if (this.tabsState.switchToTabBusy) {
        return
    }
    this.tabsState.switchToTabBusy = true
    finishProgress()
    exitFind(true)
    synchronized(this.tabLock) {
        val webNestFrame = tab.webFrame
        frame = null
        if (webNestFrame != null) {
            detachFromParent(webNestFrame)
            val tabRestore = BrowserSettings.tabRestore.value
            val webFrame = tab.webFrame
            if (tabRestore) {
                webFrame!!.backItem = tab
                frame = webFrame
            } else {
                webFrame!!.destroyFrame(null)
                tab.webFrame = null
            }
        }
        if (BrowserSettings.tabRestore.value && frame == null) {
            webViewActivity = this
            frame =
                webViewActivity.createTabFrame(tab.uid, tab.tabUrl, tab.deskMode, false)
            frame.backItem = tab
        } else {
            webViewActivity = this
        }
    }
    webViewActivity!!.tabsState.switchIndex = index
    webViewActivity.tabsState.switchFrame = frame
    val myWebCoord = webViewActivity.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaSwitchToTabTask(webViewActivity))
}

fun WebViewActivity.finishTabSwipe(direction: Int) {
    settleTabSwipe(direction)
    if (isSwipeMoving() && direction == 0) {
        val webNestFrame = this.tabsState.currentFrame
        if (webNestFrame != null) {
            webNestFrame.trimBefore()
            this.tabsState.currentFrame!!.resetFakeX()
        }
        val webNestView = this.currentPage
        if (webNestView != null) {
            webNestView.resetSwipeShade()
        }
    }
}

/**
 * The current page's picture as it is right now, for the tab overview about to open (2026-11-27): drawn at once, the
 * same way as [captureTabPreview], instead of after three hops through the message queue, so the zoom and the card
 * show what is on screen (a slideshow, a video) and not the picture from when the page loaded. Stored like the queued
 * one. Null when the page cannot be drawn now; the caller then falls back to [captureTabPreview].
 */
fun WebViewActivity.captureTabPreviewNow(currentTabIndex: Int): android.graphics.Bitmap? {
    val page = this.currentPage
    if (ZoneSettings.tabListType.value == 2 || this.tabsState.restartBusy || this.isExiting || this.tabsState.currentFrame == null ||
        page == null || page.blankPending || page.isNotLive()) return null
    val tab = tabItemAt(currentTabIndex) ?: return null
    return try {
        val width = page.getWidth()
        val height = page.getHeight()
        if (width == 0 || height == 0) return null
        val size = TabSnapshot.size(width, height) ?: return null
        val bitmap = androidx.core.graphics.createBitmap(size.first, size.second, android.graphics.Bitmap.Config.RGB_565)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(if (com.mycompany.app.ui.state.AppState.darkWeb) -16777216 else -1)
        canvas.scale(TabSnapshot.SCALE, TabSnapshot.SCALE)
        val scrollX = page.getScrollX()
        val scrollY = page.getScrollY()
        if (scrollX != 0 || scrollY != 0) canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
        page.drawBackdropOnce = com.mycompany.app.main.MainUtil.isDarkBars(page.startPage)
        page.swipeOffset = 0.0f
        page.shadeAlpha = 0
        page.draw(canvas)
        // a page that has not drawn yet gives a blank picture
        if (TabSnapshot.isBlank(bitmap)) return null
        val key = com.mycompany.app.glide.ImageCache.key(2, tab.tabUrl + tab.deskMode + com.mycompany.app.pref.store.SyncSettings.secretMode.value)
        this.webCoord?.post(WvaStoreTabThumb(this, tabId = tab.uid, cacheKey = key, bitmap = bitmap))
        bitmap
    } catch (_: Exception) {
        null
    } catch (_: OutOfMemoryError) {
        null
    }
}

fun WebViewActivity.captureTabPreview(currentTabIndex: Int) {
    // the same moments suit the reload picture: page loaded, overview opening, browser leaving the screen
    if (!this.isExiting) this.tabsState.currentFrame?.let { ReloadCover.take(it) }
    val webNestView: WebNestView? = if (ZoneSettings.tabListType.value == 2 || this.tabsState.restartBusy || this.isExiting || this.tabsState.currentFrame == null) null else this.currentPage
    val currentTab: WebTabItem? = if (ZoneSettings.tabListType.value == 2 || this.tabsState.restartBusy || this.isExiting || this.tabsState.currentFrame == null || webNestView == null || webNestView.blankPending) null else tabItemAt(currentTabIndex)
    if (ZoneSettings.tabListType.value == 2 || this.tabsState.restartBusy || this.isExiting || this.tabsState.currentFrame == null || webNestView == null || webNestView.blankPending || currentTab == null) {
        return
    }
    val myWebCoord = this.webCoord
    if (myWebCoord != null) {
        myWebCoord.post(WvaCaptureTabPreviewTask(this, captureTabPreviewPage = this.currentPage, tabItem = currentTab))
    }
}

fun WebViewActivity.refreshTabBarItems(scrollIndex: Int) {
    val webTabBarAdapter: WebTabBar? = if (this.tabBarView == null) null else this.tabBar
    val arrayList: ArrayList<WebTabItem?>? = if (this.tabBarView == null || webTabBarAdapter == null) null else this.tabsState.tabItems
    if (this.tabBarView == null || webTabBarAdapter == null || arrayList == null) {
        this.tabsState.tabBarBusy = false
    } else {
        this.tabsState.tabBarBusy = true
        webTabBarAdapter.updateList(arrayList, this.currentTabIndex, scrollIndex, false, WvaTabBarChangeListener(this))
    }
}

fun WebViewActivity.reloadTabViews(tabViewListener: TabViewListener?) {
    if (this.tabsState.reloadTabViewsBusy) {
        return
    }
    this.tabsState.reloadTabViewsBusy = true
    var relinked: ArrayList<Any?>? = null
    val tabs: ArrayList<WebTabItem?>? = if (this.currentPage != null) this.tabsState.tabItems else null
    val current = if (this.currentPage != null && tabs != null) this.currentTabIndex else 0
    if (this.currentPage != null && tabs != null && current >= 0 && current < tabs.size) {
        closeSnackbar(true)
        val myWebCoord = this.webCoord
        if (myWebCoord != null) {
            myWebCoord.clearCloseHint()
        }
        this.tabsState.tabSwipeState = 0.toByte()
        this.tabsState.swipePrevFrame = null
        this.tabsState.swipeNextFrame = null
        this.tabsState.tabSwipeForward = false
        this.swipeTabFrame = null
        this.swipeNextTabFrame = null
        exitFind(true)
        try {
            val allTabs = this.tabsState.tabItems
            val size = allTabs!!.size
            var previousUid: Long = -1
            var position = 0
            var index = 0
            while (index < size) {
                val entry: Any? = allTabs.get(index)
                index++
                val webTabItem = entry as WebTabItem?
                if (webTabItem != null) {
                    val otherParent = webTabItem.parentId != previousUid
                    webTabItem.parentId = previousUid
                    webTabItem.position = position
                    previousUid = webTabItem.uid
                    position++
                    if (otherParent) {
                        if (relinked == null) {
                            relinked = ArrayList<Any?>()
                        }
                        val copy = WebTabItem()
                        copy.uid = webTabItem.uid
                        copy.parentId = webTabItem.parentId
                        relinked.add(copy)
                    }
                    val webNestFrame: WebNestFrame? = if (webTabItem.position != this.currentTabIndex) webTabItem.webFrame else null
                    if (webTabItem.position != this.currentTabIndex && webNestFrame != null) {
                        webNestFrame.refreshPage()
                    }
                }
            }
        } catch (unused: Exception) {
        }
    }
    this.tabsState.tabViewListener = tabViewListener
    if (relinked == null || relinked.isEmpty()) {
        val webCoord = this.webCoord
        if (webCoord != null) {
            webCoord.post(WvaReloadAttachTask(this))
            return
        }
        return
    }
    val coord = this.webCoord
    if (coord != null) {
        coord.post(WvaReloadTabViewsTask(this, tabItems = relinked))
    }
}

fun WebViewActivity.loadTabItemAsync(webTabItem: WebTabItem?) {
    runInBackground(WvaSaveTabItem(this, loadTabItemAsyncTabItem = webTabItem))
}
