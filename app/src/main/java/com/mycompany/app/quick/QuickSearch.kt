package com.mycompany.app.quick

import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import ch.opumlabs.opumbrowser.R
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.graphics.Outline
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.mycompany.app.data.book.DataBookRecent
import com.mycompany.app.db.book.DbBookQuick.Companion.loadItems
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookRecent.Companion.loadItems
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.hasClipboard
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.quick.QuickAdapter.QuickHolder
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.quick.QuickAdapter.QuickListener
import com.mycompany.app.quick.QuickAdapter.QuickRcntListener
import com.mycompany.app.ui.browser.IconButton
import com.mycompany.app.ui.browser.ClipRow
import com.mycompany.app.ui.browser.ClipRow.MyClipListener
import com.mycompany.app.view.MyManagerLinear
import com.mycompany.app.view.PopMenuItem
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.view.MyRecyclerView
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebSearchAdapter
import com.mycompany.app.web.WebSearchAdapter.SearchItem
import com.mycompany.app.web.WebViewActivity
import kotlin.math.abs

@Suppress("deprecation")
class QuickSearch(context: Context?) : FrameLayout(context!!) {
    @JvmField
    var padKeyboard: Int = 0
    @JvmField
    var outlineTop: Boolean = false
    @JvmField
    var outlineBottom: Boolean = false
    @JvmField
    var autoList: MyRecyclerView? = null
    @JvmField
    var autoAdapter: WebSearchAdapter? = null
    @JvmField
    var autoManager: MyManagerLinear? = null
    @JvmField
    var padLeft: Int = 0
    @JvmField
    var padRight: Int = 0
    @JvmField
    var padBottom: Int = 0
    @JvmField
    var padTop: Int = 0
    @JvmField
    var outPadBottom: Int = 0
    @JvmField
    var recentAll: Boolean = false
    @JvmField
    var loading: Boolean = false
    @JvmField
    var recentLoaded: Boolean = false
    @JvmField
    var busy: Boolean = false
    @JvmField
    var layoutPending: Boolean = false
    @JvmField
    var busyShown: Boolean = false
    @JvmField
    var busyFade: Boolean = false
    @JvmField
    var refreshPending: Boolean = false
    @JvmField
    var searchMenu: MyPopupMenu? = null
    @JvmField
    var touchMoved: Boolean = false
    @JvmField
    var touchX: Float = 0f
    @JvmField
    var touchY: Float = 0f
    @JvmField
    var shortcutList: ArrayList<QuickAdapter.QuickItem?>? = null
    @JvmField
    var ownerActivity: WebViewActivity? = null
    @JvmField
    var recentList: MutableList<SearchItem?>? = null
    @JvmField
    var dataActivity: WebViewActivity? = null
    @JvmField
    var clipShown: Boolean = false
    @JvmField
    var gridShown: Boolean = false
    @JvmField
    var darkState: Boolean = false
    @JvmField
    var darkModeState: Boolean = false
    @JvmField
    var darkHomeState: Boolean = false
    @JvmField
    var darkUiState: Boolean = false
    @JvmField
    var reversed: Boolean = false
    @JvmField
    var columns: Int = 0
    @JvmField
    var quickSmallState: Boolean = false
    @JvmField
    var quickPlusState: Boolean = false
    @JvmField
    var clipShownState: Boolean = false
    @JvmField
    var landscapeState: Boolean = false
    @JvmField
    var searchListener: QuickSearchListener? = null
    @JvmField
    var scrollView: NestedScrollView? = null
    @JvmField
    var body: LinearLayout? = null
    @JvmField
    var editButton: IconButton? = null
    @JvmField
    var clipHolder: LinearLayout? = null
    @JvmField
    var clipView: ClipRow? = null
    @JvmField
    var shortcutGrid: QuickGridView? = null
    @JvmField
    var gridAdapter: QuickAdapter? = null
    @JvmField
    var recentGrid: QuickGridView? = null
    @JvmField
    var scrollTop: Boolean = false
    @JvmField
    var scrollBottom: Boolean = false

    internal inner class CloseSearchClick : OnClickListener {
        override fun onClick(view: View?) {
            val quickSearchListener = this@QuickSearch.searchListener
            if (quickSearchListener != null) {
                quickSearchListener.onClose()
            }
        }
    }

    internal inner class PanelOutline : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (view == null || outline == null) {
                return
            }
            val dp16Px = MainApp.dp16
            val quickSearch = this@QuickSearch
            val outlineTop = quickSearch.outlineTop
            if (outlineTop && quickSearch.outlineBottom) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp16Px.toFloat())
            } else if (outlineTop) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + dp16Px, dp16Px.toFloat())
            } else if (quickSearch.outlineBottom) {
                outline.setRoundRect(0, -dp16Px, view.getWidth(), view.getHeight(), dp16Px.toFloat())
            }
        }
    }

    internal inner class UpdatePlusItemTask : Runnable {

        internal inner class UpdatePlusItemStep : Runnable {

            internal inner class GridHeightStep : Runnable {

                internal inner class ColorsStep : Runnable {

                    internal inner class PaddingStep : Runnable {

                        internal inner class ColumnsStep : Runnable {

                            internal inner class PlusItemStep : Runnable {
                                override fun run() {
                                    val paddingStep = this@PaddingStep
                                    val quickSearch = this@QuickSearch
                                    val quickAdapter = quickSearch.gridAdapter
                                    if (quickAdapter != null) {
                                        quickAdapter.darkState = quickSearch.darkState
                                        quickAdapter.reversed = quickSearch.reversed
                                        quickAdapter.updatePlusItem()
                                        this@QuickSearch.gridAdapter!!.notifyDataSetChanged()
                                    }
                                    val search = this@QuickSearch
                                    val myRecyclerView = search.autoList
                                    val webSearchAdapter: WebSearchAdapter? = if (myRecyclerView != null && myRecyclerView.getVisibility() == View.VISIBLE) search.autoAdapter else null
                                    if (myRecyclerView != null && myRecyclerView.getVisibility() == View.VISIBLE && webSearchAdapter != null) {
                                        webSearchAdapter.notifyDataSetChanged()
                                    }
                                    val owner = this@QuickSearch
                                    owner.refreshPending = true
                                    owner.refreshLater()
                                }
                            }

                            override fun run() {
                                val colorsStep = this@ColorsStep
                                val quickSearch = this@QuickSearch
                                val columns: Int = columnsFor(quickSearch.isLandscapeNow())
                                quickSearch.columns = columns
                                val grid: QuickGridView? = if (ZtriSettings.qckSchType.value == 1) quickSearch.recentGrid else null
                                if (grid != null && grid.spanCount != columns) grid.spanCount = columns
                                this@QuickSearch.post(PlusItemStep())
                            }
                        }

                        override fun run() {
                            val gridHeightStep = this@GridHeightStep
                            val quickSearch = this@QuickSearch
                            quickSearch.updateBodyPadding()
                            this@QuickSearch.post(ColumnsStep())
                        }
                    }

                    override fun run() {
                        val updatePlusItemStep = this@UpdatePlusItemStep
                        val quickSearch = this@QuickSearch
                        quickSearch.applyColors()
                        this@QuickSearch.post(PaddingStep())
                    }
                }

                override fun run() {
                    val updatePlusItemTask = this@UpdatePlusItemTask
                    val quickSearch = this@QuickSearch
                    quickSearch.updateGridHeight()
                    this@QuickSearch.post(ColorsStep())
                }
            }

            override fun run() {
                val quickSearch = this@QuickSearch
                quickSearch.placeClip()
                quickSearch.post(GridHeightStep())
            }
        }

        override fun run() {
            val quickSearch = this@QuickSearch
            quickSearch.placeEditButton()
            quickSearch.post(UpdatePlusItemStep())
        }
    }

    internal inner class UpdateBodyPaddingTask : Runnable {
        override fun run() {
            val quickSearch = this@QuickSearch
            if (quickSearch.body == null) {
                return
            }
            quickSearch.updateBodyPadding()
            quickSearch.refreshPending = true
            quickSearch.refreshLater()
        }
    }

    internal inner class ScrollShortcutsTask : Runnable {

        internal inner class ShowShortcutsStep : Runnable {

            internal inner class FocusShortcutsStep : Runnable {
                override fun run() {
                    val scrollShortcutsTask = this@ScrollShortcutsTask
                    val myRecyclerView = this@QuickSearch.shortcutGrid
                    if (myRecyclerView == null) {
                        return
                    }
                    myRecyclerView.setFocusable(true)
                    this@QuickSearch.shortcutGrid!!.requestLayout()
                }
            }

            override fun run() {
                val quickSearch = this@QuickSearch
                val linearLayout = quickSearch.body
                if (linearLayout == null) {
                    return
                }
                linearLayout.setVisibility(View.VISIBLE)
                quickSearch.post(FocusShortcutsStep())
            }
        }

        override fun run() {
            val reload: Boolean
            val quickSearch = this@QuickSearch
            if (quickSearch.gridAdapter != null) {
                if (ZtriSettings.qckSchType.value != 2) {
                    reload = false
                } else {
                    reload = if (!quickSearch.recentLoaded) true else DataBookRecent.instance(quickSearch.dataActivity).reloadNeeded
                }
                if (reload) {
                    DataBookRecent.instance(quickSearch.dataActivity).reloadNeeded = false
                    if (quickSearch.gridAdapter!!.getItemCount() >= 2) {
                        quickSearch.shortcutGrid!!.setFocusable(false)
                        quickSearch.scrollView!!.fullScroll(if (quickSearch.reversed) 130 else 33)
                    }
                }
            }
            quickSearch.post(ShowShortcutsStep())
        }
    }

    internal inner class RelayoutTask : Runnable {
        override fun run() {
            val quickSearch = this@QuickSearch
            quickSearch.itemList
            quickSearch.layoutPending = true
            quickSearch.relayout()
        }
    }

    internal inner class UpdateHeaderFooterTask : Runnable {
        override fun run() {
            val quickSearch = this@QuickSearch
            val arrayList = quickSearch.shortcutList
            val list = quickSearch.recentList
            quickSearch.shortcutList = null
            quickSearch.recentList = null
            if (quickSearch.shortcutGrid == null) {
                return
            }
            val quickAdapter = quickSearch.gridAdapter
            if (quickAdapter != null) {
                quickAdapter.darkState = quickSearch.darkState
                quickAdapter.reversed = quickSearch.reversed
                if (ZtriSettings.qckSchType.value == 2) {
                    quickAdapter.recentMode = true
                    val mainListLoader = quickAdapter.listLoader
                    if (mainListLoader != null) {
                        mainListLoader.queue = null
                    }
                    quickAdapter.updateHeaderFooter()
                    quickAdapter.recentItems = list
                    quickAdapter.items = null
                    quickAdapter.notifyDataSetChanged()
                } else {
                    quickAdapter.applyList(arrayList, false)
                }
            }
            quickSearch.post(quickSearch.UpdateBodyPaddingLaterTask())
        }
    }

    internal inner class CheckClipboardTask : Runnable {
        override fun run() {
            val quickSearch = this@QuickSearch
            val hasClip = hasClipboard(quickSearch.dataActivity)
            if (quickSearch.clipShown == hasClip) {
                return
            }
            quickSearch.clipShown = hasClip
            quickSearch.post(quickSearch.UpdateBodyPaddingLaterTask())
        }
    }

    internal inner class UpdateBodyPaddingLaterTask : Runnable {

        internal inner class UpdateBodyPaddingStep : Runnable {

            internal inner class BodyGridHeightStep : Runnable {

                internal inner class BodyColorsStep : Runnable {

                    internal inner class BodyPaddingStep : Runnable {
                        override fun run() {
                            val bodyGridHeightStep = this@BodyGridHeightStep
                            val quickSearch = this@QuickSearch
                            quickSearch.updateBodyPadding()
                            val search = this@QuickSearch
                            search.post(search.ScrollShortcutsTask())
                            val owner = this@QuickSearch
                            owner.loading = false
                            owner.recentLoaded = true
                        }
                    }

                    override fun run() {
                        val updateBodyPaddingStep = this@UpdateBodyPaddingStep
                        val quickSearch = this@QuickSearch
                        quickSearch.applyColors()
                        this@QuickSearch.post(BodyPaddingStep())
                    }
                }

                override fun run() {
                    val updateBodyPaddingLaterTask = this@UpdateBodyPaddingLaterTask
                    val quickSearch = this@QuickSearch
                    quickSearch.updateGridHeight()
                    this@QuickSearch.post(BodyColorsStep())
                }
            }

            override fun run() {
                val quickSearch = this@QuickSearch
                quickSearch.placeClip()
                quickSearch.post(BodyGridHeightStep())
            }
        }

        override fun run() {
            val quickSearch = this@QuickSearch
            quickSearch.placeEditButton()
            quickSearch.post(UpdateBodyPaddingStep())
        }
    }

    internal inner class GridTypePopup : MyPopupListener {

        internal inner class ApplyGridTypeLaterStep : Runnable {

            internal inner class ApplyGridTypeTask : Runnable {
                override fun run() {
                    val gridTypePopup = this@GridTypePopup
                    val quickSearch = this@QuickSearch
                    quickSearch.applyGridType()
                    this@QuickSearch.loadItems()
                }
            }

            override fun run() {
                val quickSearch = this@QuickSearch
                val quickAdapter = quickSearch.gridAdapter
                if (quickAdapter != null) {
                    val mainListLoader = quickAdapter.listLoader
                    if (mainListLoader != null) {
                        mainListLoader.queue = null
                    }
                    quickAdapter.items = null
                    quickAdapter.newsItems = null
                    quickAdapter.recentItems = null
                    quickAdapter.notifyDataSetChanged()
                    quickSearch.gridAdapter = null
                }
                quickSearch.post(ApplyGridTypeTask())
            }
        }

        override fun onClose() {
            this@QuickSearch.dismissMenu()
        }

        override fun onItem(view: View?, id: Int): Boolean {
            val searchType: Int
            val quickSearch = this@QuickSearch
            if (quickSearch.body != null) {
                if (id == 0) {
                    searchType = 1
                } else {
                    searchType = if (id == 1) 2 else 0
                }
                if (ZtriSettings.qckSchType.value != searchType) {
                    ZtriSettings.edit(quickSearch.dataActivity) { set(ZtriSettings.qckSchType, searchType) }
                    quickSearch.body!!.setVisibility(View.INVISIBLE)
                    quickSearch.postDelayed(ApplyGridTypeLaterStep(), 300L)
                    return true
                }
            }
            return true
        }
    }

    internal inner class ScrollShortcutsLaterTask : Runnable {

        internal inner class ScrollShortcutsLaterStep : Runnable {

            internal inner class RelayoutShortcutsStep : Runnable {
                override fun run() {
                    val myRecyclerView = this@QuickSearch.shortcutGrid
                    if (myRecyclerView == null) {
                        return
                    }
                    myRecyclerView.requestLayout()
                }
            }

            override fun run() {
                val quickSearch = this@QuickSearch
                if (quickSearch.shortcutGrid == null) {
                    return
                }
                val columns: Int = columnsFor(quickSearch.isLandscapeNow())
                quickSearch.columns = columns
                val grid: QuickGridView? = if (ZtriSettings.qckSchType.value == 1) quickSearch.recentGrid else null
                if (grid != null && grid.spanCount != columns) grid.spanCount = columns
                quickSearch.post(RelayoutShortcutsStep())
            }
        }

        override fun run() {
            val quickSearch = this@QuickSearch
            quickSearch.updateBodyPadding()
            if (quickSearch.shortcutGrid == null) {
                return
            }
            quickSearch.post(ScrollShortcutsLaterStep())
        }
    }

    internal inner class ScrollViewTouch : OnTouchListener {
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            this@QuickSearch.setScrollTouch(event)
            return false
        }
    }

    internal inner class DismissMenuClick : OnClickListener {
        override fun onClick(view: View?) {
            val quickSearch = this@QuickSearch
            if (quickSearch.ownerActivity == null || quickSearch.searchMenu != null) {
                return
            }
            quickSearch.dismissMenu()
            if (view != null) {
                val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                arrayList.add(PopMenuItem(0, AppR.string.quick_access, ZtriSettings.qckSchType.value == 1))
                arrayList.add(PopMenuItem(1, AppR.string.recent_search, ZtriSettings.qckSchType.value == 2))
                arrayList.add(PopMenuItem(2, AppR.string.not_used, ZtriSettings.qckSchType.value == 0))
                val myPopupMenu = MyPopupMenu(
                    quickSearch.ownerActivity,
                    quickSearch.clipHolder,
                    view,
                    arrayList,
                    MainUtil.isDarkPage(quickSearch.darkState),
                    quickSearch.GridTypePopup()
                )
                quickSearch.searchMenu = myPopupMenu
                val webViewActivity = quickSearch.ownerActivity
                if (webViewActivity != null) {
                    webViewActivity.popupMenu = myPopupMenu
                }
            }
        }
    }

    internal inner class SetupClipTask : Runnable {

        internal inner class ApplyGridTypeStep : Runnable {

            internal inner class BuildAutoListStep : Runnable {
                override fun run() {
                    val quickSearch = this@QuickSearch
                    if (quickSearch.ownerActivity == null || quickSearch.autoManager != null || quickSearch.autoList == null) {
                        return
                    }
                    val myManagerLinear = MyManagerLinear(1)
                    quickSearch.autoManager = myManagerLinear
                    quickSearch.autoList!!.setLayoutManager(myManagerLinear)
                    quickSearch.autoList!!.setSizeListener(quickSearch.AutoListSizeListener())
                }
            }

            override fun run() {
                val quickSearch = this@QuickSearch
                if (quickSearch.ownerActivity == null) {
                    return
                }
                quickSearch.applyGridType()
                quickSearch.post(BuildAutoListStep())
            }
        }

        override fun run() {
            val quickSearch = this@QuickSearch
            if (quickSearch.ownerActivity == null) {
                return
            }
            val myClipView = quickSearch.clipView
            if (myClipView != null) {
                val webViewActivity = quickSearch.dataActivity
                val darkState = quickSearch.darkState
                val reversed = quickSearch.reversed
                val clipListener = quickSearch.ClipListener()
                myClipView.appContext = webViewActivity
                myClipView.darkColor = darkState
                myClipView.roundOn = true
                myClipView.reversedOrder = reversed
                myClipView.clipListener = clipListener
                quickSearch.clipView!!.build()
            }
            quickSearch.post(ApplyGridTypeStep())
        }
    }

    internal inner class ClipListener : MyClipListener {
        override fun onOpenText(clipText: String?) {
            val quickSearchListener = this@QuickSearch.searchListener
            if (quickSearchListener != null) {
                quickSearchListener.openText(clipText)
            }
        }

        override fun onFillText(clipText: String?) {
            val quickSearchListener = this@QuickSearch.searchListener
            if (quickSearchListener != null) {
                quickSearchListener.fillText(clipText)
            }
        }
    }

    internal inner class NormalizeUrlStep : QuickListener {
        override fun isLandscape(): Boolean {
            return this@QuickSearch.isLandscapeNow()
        }

        override fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean) {
        }

        override fun hasTransNews(): Boolean {
            return false
        }

        override fun openNewsItem(quickItem: QuickItem?) {
        }

        override fun newsListType(): Int {
            return 0
        }

        override fun openNewsSettings() {
        }

        override fun openNewsSources() {
        }

        override fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?) {
        }

        override fun onItemClick(quickHolder: QuickHolder?, position: Int) {
            var width: Int
            val height: Int
            val widthValue: Int
            val heightValue: Int
            val quickSearch = this@QuickSearch
            val quickAdapter = quickSearch.gridAdapter
            val quickItemD: QuickItem? = if (quickAdapter == null || quickSearch.searchListener == null) null else quickAdapter.itemAt(position)
            if (quickAdapter == null || quickSearch.searchListener == null || quickItemD == null) {
                return
            }
            if (!quickItemD.isFolder) {
                val itemKind = quickItemD.itemKind
                if (itemKind.toInt() == 0) {
                    quickSearch.searchListener!!.openUrl(itemKind.toInt(), MainUtil.normalizeUrl(null, quickItemD.path))
                    return
                } else {
                    if (itemKind.toInt() == 1) {
                        quickSearch.searchListener!!.openUrl(itemKind.toInt(), null)
                        return
                    }
                    return
                }
            }
            val myWebBodyF = quickSearch.searchListener!!.webBody()
            if (myWebBodyF != null) {
                val location = IntArray(2)
                myWebBodyF.getLocationOnScreen(location)
                val paddingStart = myWebBodyF.getPaddingStart() + location[0]
                val paddingTop = myWebBodyF.getPaddingTop() + location[1]
                quickSearch.getLocationOnScreen(location)
                val screenX = location[0]
                val screenY = location[1]
                val view: View? = if (quickHolder == null) null else quickHolder.itemView
                if (quickHolder == null || view == null) {
                    val minSize = MainApp.dp16 * 5
                    width = quickSearch.getWidth() / 2
                    height = quickSearch.getHeight() / 2
                    widthValue = minSize
                    heightValue = widthValue
                } else {
                    val viewWidth = view.getWidth()
                    val viewHeight = view.getHeight()
                    view.getLocationOnScreen(location)
                    width = (viewWidth / 2) + (location[0] - screenX)
                    height = ((viewHeight / 2) + (location[1] - screenY)) - MainApp.dp8
                    if (isRtl(quickSearch.dataActivity)) {
                        width = quickSearch.getWidth() - width
                    }
                    widthValue = viewWidth
                    heightValue = viewHeight
                }
                quickSearch.searchListener!!.openFolder(
                    (screenX - paddingStart) + width,
                    (screenY - paddingTop) + height,
                    widthValue,
                    quickItemD.path,
                    heightValue
                )
            }
        }

        override fun onEditMode(enabled: Boolean) {
        }

        override fun onItemLongClick(quickHolder: QuickHolder?, position: Int) {
            val quickSearch = this@QuickSearch
            val quickAdapter = quickSearch.gridAdapter
            val quickItemD: QuickItem? = if (quickAdapter == null || quickSearch.searchListener == null) null else quickAdapter.itemAt(position)
            if (quickAdapter == null || quickSearch.searchListener == null || quickItemD == null || quickItemD.itemKind.toInt() != 0) {
                return
            }
            quickSearch.searchListener!!.openEngine(position + 1)
        }
    }

    internal inner class SetQueryStep : QuickRcntListener {
        override fun owner(): QuickSearch {
            return this@QuickSearch
        }

        override fun onRecentPick(cursor: Int, query: String?) {
            val quickSearchListener = this@QuickSearch.searchListener
            if (quickSearchListener != null) {
                quickSearchListener.setQuery(cursor, query)
            }
        }
    }

    internal inner class AutoListSizeListener : ImageSizeListener {

        internal inner class ResizeAutoListStep : Runnable {
            override fun run() {
                val quickSearch = this@QuickSearch
                val myRecyclerView = quickSearch.autoList
                val webSearchAdapter: WebSearchAdapter? = if (myRecyclerView == null || myRecyclerView.getVisibility() != View.VISIBLE) null else quickSearch.autoAdapter
                if (myRecyclerView == null || myRecyclerView.getVisibility() != View.VISIBLE || webSearchAdapter == null) {
                    return
                }
                webSearchAdapter.notifyDataSetChanged()
            }
        }

        override fun onSize(view: View?, width: Int, height: Int) {
            val quickSearch = this@QuickSearch
            val myRecyclerView = quickSearch.autoList
            if (myRecyclerView == null || myRecyclerView.getVisibility() != View.VISIBLE) {
                return
            }
            quickSearch.post(ResizeAutoListStep())
        }
    }

    internal inner class ScrollEdgesOutline : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (view == null || outline == null) {
                return
            }
            val dp16Px = MainApp.dp16
            val quickSearch = this@QuickSearch
            val scrollTop = quickSearch.scrollTop
            val scrollBottom = quickSearch.scrollBottom
            if (!scrollTop) {
                if (scrollBottom) {
                    outline.setRoundRect(0, -dp16Px, view.getWidth(), view.getHeight(), dp16Px.toFloat())
                }
            } else if (scrollBottom) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp16Px.toFloat())
            } else {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + dp16Px, dp16Px.toFloat())
            }
        }
    }

    interface QuickSearchListener {
        fun webBody(): MyWebBody?

        fun setQuery(cursor: Int, query: String?)

        fun onItemClick(searchItem: SearchItem?)

        fun openText(input: String?)

        fun fillText(input: String?)

        fun onClose()

        fun openUrl(type: Int, url: String?)

        fun openEngine(engine: Int)

        fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int)
    }

    private val bodyPadding: Int
        get() {
            val height: Int
            if (!this.reversed) {
                return 0
            }
            var i2 = MainApp.dp48
            var i = 0
            val i3 = ZtriSettings.qckSchType.value
            val z = this.gridShown
            if (i3 == 1) {
                if (z) {
                    i2 += MainApp.dp90
                }
                if (this.clipShown) {
                    i = MainApp.dp56
                    i2 += i
                }
            } else if (z || this.clipShown) {
                i = MainApp.dp56
                i2 += i
            }
            if (MainConst.isApi30) {
                val parent: Any? = getParent()
                if (parent == null) {
                    return 0
                }
                height = (parent as View).getHeight()
            } else {
                height = getHeight()
            }
            val paddingTop = ((height - getPaddingTop()) - getPaddingBottom()) - i2
            if (paddingTop <= 0) {
                return 0
            }
            return paddingTop
        }

    val itemList: Unit

        get() {
            this.shortcutList = null
            this.recentList = null
            var z = false
            this.gridShown = false
            val i = ZtriSettings.qckSchType.value
            if (i != 0) {
                if (i == 1) {
                    val arrayListI: ArrayList<QuickAdapter.QuickItem?>? =
                        loadItems(this.dataActivity, false, false, this.recentAll)
                    this.recentAll = false
                    if (arrayListI != null && !arrayListI.isEmpty()) {
                        z = true
                    }
                    this.gridShown = z
                    this.shortcutList = arrayListI
                    return
                }
                if (DbBookRecent.isEnabled()) {
                    var arrayListC =
                        if (DataBookRecent.instance(this.dataActivity).changed) null else DataBookRecent.instance(this.dataActivity).items
                    if (arrayListC == null || arrayListC.isEmpty()) {
                        arrayListC = loadItems(this.dataActivity)
                        val dataBookRecentA = DataBookRecent.instance(this.dataActivity)
                        dataBookRecentA.items = arrayListC
                        dataBookRecentA.changed = false
                    }
                    if (arrayListC != null && !arrayListC.isEmpty()) {
                        z = true
                    }
                    this.gridShown = z
                    this.recentList = arrayListC
                }
            }
        }

    fun setScrollTouch(motionEvent: MotionEvent?) {
        if (this.scrollView != null) {
            val actionMasked = motionEvent!!.getActionMasked()
            if (actionMasked == 0) {
                this.touchMoved = true
                this.touchX = motionEvent.getX()
                this.touchY = motionEvent.getY()
                return
            }
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (this.touchMoved) {
                        if (abs(this.touchX - motionEvent.getX()) > MainApp.dp16 || abs(this.touchY - motionEvent.getY()) > MainApp.dp16) {
                            this.touchMoved = false
                            return
                        }
                        return
                    }
                    return
                }
                if (actionMasked != 3) {
                    return
                }
            }
            if (this.touchMoved) {
                this.touchMoved = false
                val quickSearchListener = this.searchListener
                if (quickSearchListener != null) {
                    quickSearchListener.onClose()
                }
            }
        }
    }

    fun dismissMenu() {
        val myPopupMenu = this.searchMenu
        if (myPopupMenu != null) {
            val webViewActivity = this.ownerActivity
            if (webViewActivity != null) {
                webViewActivity.popupMenu = null
            }
            myPopupMenu.dismissNow()
            this.searchMenu = null
        }
    }

    fun applyGridType() {
        if (ZtriSettings.qckSchType.value == 0 || this.shortcutGrid == null) {
            return
        }
        val gridColumns: Int = columnsFor(isLandscapeNow())
        this.columns = gridColumns
        val grid = this.shortcutGrid!!
        // shortcuts as tiles in columns (type 1), or recent searches as full-width rows (type 2)
        this.recentGrid = if (ZtriSettings.qckSchType.value == 1) grid else null
        grid.spanCount = if (ZtriSettings.qckSchType.value == 1) gridColumns else 1
        val quickAdapter = QuickAdapter(this.dataActivity, 2, this.darkState, NormalizeUrlStep())
        this.gridAdapter = quickAdapter
        quickAdapter.recentListener = SetQueryStep()
        quickAdapter.reversed = this.reversed
        grid.setAdapter(this.gridAdapter)
    }

    fun setupViews() {
        val webViewActivity: WebViewActivity? = if (this.scrollView != null) null else this.dataActivity
        if (this.scrollView != null || webViewActivity == null) {
            return
        }
        val nestedScrollView = NestedScrollView(webViewActivity)
        nestedScrollView.setOverScrollMode(2)
        addView(nestedScrollView, -1, -1)
        val linearLayout = LinearLayout(webViewActivity)
        linearLayout.setOrientation(LinearLayout.VERTICAL)
        linearLayout.setVisibility(View.GONE)
        nestedScrollView.addView(linearLayout, -1, -1)
        val clipHolder = LinearLayout(webViewActivity)
        clipHolder.setOrientation(LinearLayout.VERTICAL)
        linearLayout.addView(clipHolder, -1, -2)
        // all rows at once: the list sits in the scroll view above
        val myRecyclerView = QuickGridView(webViewActivity)
        myRecyclerView.wrapContent = true
        myRecyclerView.setRoundSize(MainApp.dp16)
        myRecyclerView.setVisibility(View.GONE)
        clipHolder.addView(myRecyclerView, -1, -2)
        val width = if (this.reversed && ZtriSettings.qckSchType.value == 2) 1 else 0
        val myClipView = ClipRow(this.dataActivity!!)
        myClipView.setVisibility(View.GONE)
        clipHolder.addView(myClipView, width, LinearLayout.LayoutParams(-1, MainApp.dp56))
        val insertAt = if (!this.reversed) 1 else 0
        val myButtonImage = IconButton(webViewActivity)
        myButtonImage.setScaleType(ScaleType.CENTER_INSIDE)
        myButtonImage.applyRadii(MainApp.dp8.toFloat(), MainApp.dp16.toFloat())
        val dp48Px = MainApp.dp48
        val layoutParams = LinearLayout.LayoutParams(dp48Px, dp48Px)
        layoutParams.gravity = 8388613
        linearLayout.addView(myButtonImage, insertAt, layoutParams)
        val autoList = MyRecyclerView(webViewActivity)
        autoList.setRoundSize(MainApp.dp16)
        autoList.setVerticalScrollBarEnabled(false)
        autoList.setHorizontalScrollBarEnabled(false)
        autoList.setVisibility(View.INVISIBLE)
        addView(autoList, -1, -2)
        this.scrollView = nestedScrollView
        this.body = linearLayout
        this.editButton = myButtonImage
        this.clipHolder = clipHolder
        this.clipView = myClipView
        this.shortcutGrid = myRecyclerView
        this.autoList = autoList
        setOnClickListener(CloseSearchClick())
        this.scrollView!!.setOnTouchListener(ScrollViewTouch())
        this.editButton!!.setOnClickListener(DismissMenuClick())
        post(SetupClipTask())
    }

    fun isLandscapeNow(): Boolean {
        val webViewActivity = this.ownerActivity
        return if (webViewActivity == null) isLandscape(this.dataActivity) else webViewActivity.isLandscapeScreen()
    }

    val outPadHeight: Int
        get() = (getHeight() - this.padTop) - this.outPadBottom

    fun updateBodyPadding() {
        var bodyPadding: Int = 0
        val linearLayout = this.body
        if (linearLayout == null || linearLayout.getPaddingTop() == (this.bodyPadding.also {
                bodyPadding = it
            })) {
            return
        }
        linearLayout.setPadding(0, bodyPadding, 0, 0)
    }

    fun placeClip() {
        val myClipView: ClipRow? = if (!this.clipShown) null else this.clipView
        val linearLayout: LinearLayout? = if (!this.clipShown || myClipView == null) null else this.clipHolder
        if (!this.clipShown || myClipView == null || linearLayout == null) {
            return
        }
        val width = if (this.reversed && ZtriSettings.qckSchType.value == 2) 1 else 0
        if (linearLayout.indexOfChild(myClipView) == width) {
            return
        }
        detachFromParent(myClipView)
        linearLayout.addView(myClipView, width, LinearLayout.LayoutParams(-1, MainApp.dp56))
    }

    fun updateGridHeight() {
        val myRecyclerView: QuickGridView? = if (!this.gridShown) null else this.shortcutGrid
        val layoutParams: LinearLayout.LayoutParams? = if (!this.gridShown || myRecyclerView == null) null else (myRecyclerView.getLayoutParams() as LinearLayout.LayoutParams?)
        if (!this.gridShown || myRecyclerView == null || layoutParams == null) {
            return
        }
        var topMargin = 0
        if (ZtriSettings.qckSchType.value == 1 && this.clipShown) {
            topMargin = MainApp.dp8
        }
        if (layoutParams.topMargin == topMargin) {
            return
        }
        layoutParams.topMargin = topMargin
    }

    fun placeEditButton() {
        var width: Int = 0
        val view: View? = this.editButton
        val linearLayout: LinearLayout? = if (view == null) null else this.body
        width = if (view == null || linearLayout == null) 0 else (if (!this.reversed) 1 else 0)
        if (view == null || linearLayout == null || linearLayout.indexOfChild(view) == width) {
            return
        }
        detachFromParent(view)
        val dp48Px = MainApp.dp48
        val layoutParams = LinearLayout.LayoutParams(dp48Px, dp48Px)
        layoutParams.gravity = 8388613
        linearLayout.addView(view, width, layoutParams)
    }

    fun relayout() {
        if (!this.layoutPending || this.busy) {
            return
        }
        this.layoutPending = false
        if (this.shortcutGrid != null) {
            post(UpdateHeaderFooterTask())
        }
    }

    fun loadItems() {
        if (this.shortcutGrid != null) {
            if (ZtriSettings.qckSchType.value != 0) {
                if (this.loading) {
                    return
                }
                this.loading = true
                MainApp.runInBackground(this.dataActivity, RelayoutTask())
                return
            }
            this.shortcutList = null
            this.recentList = null
            this.gridShown = false
            this.layoutPending = true
            relayout()
        }
    }

    fun setScrollEdges(scrollTop: Boolean, scrollBottom: Boolean) {
        val nestedScrollView = this.scrollView
        if (nestedScrollView != null) {
            if (this.scrollTop == scrollTop && this.scrollBottom == scrollBottom) {
                return
            }
            this.scrollTop = scrollTop
            this.scrollBottom = scrollBottom
            if (!scrollTop && !scrollBottom) {
                nestedScrollView.setClipToOutline(false)
            } else {
                nestedScrollView.setOutlineProvider(ScrollEdgesOutline())
                nestedScrollView.setClipToOutline(true)
            }
        }
    }

    fun showAuto(pos: Int, isEmpty: Boolean) {
        val nestedScrollView = this.scrollView
        if (nestedScrollView != null) {
            if (!isEmpty) {
                if (nestedScrollView.getVisibility() == View.INVISIBLE) {
                    this.scrollView!!.setVisibility(View.VISIBLE)
                }
                if (this.autoList!!.getVisibility() == View.VISIBLE) {
                    this.autoList!!.setVisibility(View.INVISIBLE)
                    return
                }
                return
            }
            if (nestedScrollView.getVisibility() == View.VISIBLE) {
                this.scrollView!!.setVisibility(View.INVISIBLE)
            }
            val myClipView = this.clipView
            if (myClipView != null) {
                myClipView.hidePreview()
            }
            if (this.autoList!!.getVisibility() != View.VISIBLE) {
                this.autoList!!.setVisibility(View.VISIBLE)
            }
            val myManagerLinear: MyManagerLinear? = if (pos < 0) null else this.autoManager
            if (pos < 0 || myManagerLinear == null) {
                return
            }
            myManagerLinear.scrollToPosition(pos)
        }
    }

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        post(ScrollShortcutsLaterTask())
    }

    fun refreshLater() {
        if (!this.refreshPending || this.busy) {
            return
        }
        this.refreshPending = false
        post(ScrollShortcutsTask())
    }

    fun applyColors() {
        val left: Int
        val darkState = this.darkState
        if (this.shortcutGrid == null) {
            return
        }
        this.darkState = darkState
        this.darkModeState = isDarkMode()
        this.darkHomeState = BrowserSettings.darkHome.value
        this.darkUiState = AppState.darkUi
        val darkBars = MainUtil.isDarkBars(this.darkState)
        val darkPage = MainUtil.isDarkPage(this.darkState)
        if (AppState.darkUi || this.darkState) {
            setBackgroundColor(-2130706432)
        } else {
            setBackgroundColor(553648128)
        }
        val padLeft = if (ZtriSettings.qckSchType.value == 2 || !darkBars) 0 else MainApp.dp4
        val padBottom = if (this.reversed) 0 else MainApp.dp16
        if (this.padLeft != padLeft || this.padRight != padLeft || this.padBottom != padBottom) {
            this.padLeft = padLeft
            this.padRight = padLeft
            this.padBottom = padBottom
            setPadding(padLeft, this.padTop, padLeft, padBottom + this.outPadBottom)
            updateBodyPadding()
        }
        if (this.clipShown || this.gridShown) {
            if (darkBars) {
                setScrollEdges(true, true)
            } else if (this.reversed) {
                setScrollEdges(true, false)
            } else {
                setScrollEdges(false, true)
            }
            val portColumns = ZtriSettings.qckSchType.value
            val linearLayout = this.clipHolder!!
            if (portColumns == 1) {
                linearLayout.setBackgroundColor(if (darkPage) -16777216 else -460552)
            } else {
                linearLayout.setBackgroundColor(if (darkPage) -14606047 else -1)
            }
            val reversed = this.reversed
            val outlineBottom = !reversed
            val clipHolder = this.clipHolder
            if (clipHolder != null && (this.outlineTop != reversed || this.outlineBottom != outlineBottom)) {
                this.outlineTop = reversed
                this.outlineBottom = outlineBottom
                if (reversed || !reversed) {
                    clipHolder.setOutlineProvider(PanelOutline())
                    clipHolder.setClipToOutline(true)
                } else {
                    clipHolder.setClipToOutline(false)
                }
            }
        } else {
            this.clipHolder!!.setBackground(null)
        }
        this.clipView!!.applyState(this.clipShown, this.darkState, this.reversed)
        val myButtonImage = this.editButton!!
        if (darkPage) {
            myButtonImage.setImageResource(AppR.drawable.outline_arrow_drop_down_dark_24)
            this.editButton!!.applyColors(-16777216, -12632257)
        } else {
            myButtonImage.setImageResource(AppR.drawable.outline_arrow_drop_down_black_24)
            this.editButton!!.applyColors(-1, -2039584)
        }
        if (!this.gridShown) {
            this.shortcutGrid!!.setVisibility(View.GONE)
            return
        }
        if (ZtriSettings.qckSchType.value != 2) {
            left = if (darkBars) MainApp.dp4 else MainApp.dp8
        } else {
            left = 0
        }
        if (this.shortcutGrid!!.getPaddingLeft() != left) {
            this.shortcutGrid!!.setPadding(left, 0, left, 0)
        }
        val landColumns = ZtriSettings.qckSchType.value
        val myRecyclerView = this.shortcutGrid
        if (landColumns == 1) {
            myRecyclerView!!.applyRound(true, true)
            this.shortcutGrid!!.setBackgroundColor(if (darkPage) -14606047 else -1)
        } else {
            myRecyclerView!!.applyRound(false, false)
            this.shortcutGrid!!.setBackground(null)
        }
        this.shortcutGrid!!.setVisibility(View.VISIBLE)
    }

    fun applyState(clipShown: Boolean, darkState: Boolean, reversed: Boolean) {
        val found: Boolean
        val hidden: Boolean
        val linearLayout: LinearLayout?
        val myClipView = this.clipView
        if (myClipView == null) {
            return
        }
        myClipView.clipText = null
        myClipView.subText = null
        if (!clipShown) {
            this.loading = false
            this.recentLoaded = true
            this.layoutPending = false
            this.busyShown = false
            this.busyFade = false
            this.refreshPending = false
            this.landscapeState = isLandscapeNow()
            dismissMenu()
            setVisibility(View.GONE)
            this.clipView!!.hidePreview()
            return
        }
        val columns: Int = columnsFor(isLandscapeNow())
        if (this.darkState == darkState && this.darkModeState == isDarkMode() && this.darkHomeState == BrowserSettings.darkHome.value && this.darkUiState == AppState.darkUi && this.reversed == reversed && this.columns == columns && this.quickSmallState == PdfSettings.quickSmall.value && this.quickPlusState == ZtriSettings.quickPlus.value && this.clipShownState == this.clipShown) {
            found = false
        } else {
            this.darkState = darkState
            this.darkModeState = isDarkMode()
            this.darkHomeState = BrowserSettings.darkHome.value
            this.darkUiState = AppState.darkUi
            this.reversed = reversed
            this.columns = columns
            this.quickSmallState = PdfSettings.quickSmall.value
            this.quickPlusState = ZtriSettings.quickPlus.value
            this.clipShownState = this.clipShown
            found = true
        }
        if (!this.recentLoaded || (ZtriSettings.qckSchType.value == 2 && DataBookRecent.instance(this.dataActivity).changed)) {
            this.body!!.setVisibility(View.INVISIBLE)
            loadItems()
        } else {
            val body = this.body
            if (found) {
                body!!.setVisibility(View.INVISIBLE)
                this.busyShown = true
                if (!this.busy) {
                    this.busyShown = false
                    post(UpdatePlusItemTask())
                }
            } else if (body == null || body.getPaddingTop() == this.bodyPadding) {
                val landscape = isLandscapeNow()
                var hide = true
                if (this.landscapeState != landscape) {
                    this.landscapeState = landscape
                } else {
                    if (ZtriSettings.qckSchType.value != 2) {
                        hidden = false
                    } else {
                        hidden = if (!this.recentLoaded) true else DataBookRecent.instance(this.dataActivity).reloadNeeded
                    }
                    hide = hidden || ((this.body.also {
                        linearLayout = it
                    }) != null && linearLayout!!.getVisibility() != View.VISIBLE)
                }
                if (hide) {
                    this.body!!.setVisibility(View.INVISIBLE)
                    this.refreshPending = true
                    refreshLater()
                }
            } else {
                this.body!!.setVisibility(View.INVISIBLE)
                this.busyFade = true
                if (!this.busy) {
                    this.busyFade = false
                    post(UpdateBodyPaddingTask())
                }
            }
        }
        if (AppState.darkUi || this.darkState) {
            setBackgroundColor(-2130706432)
        } else {
            setBackgroundColor(553648128)
        }
        showAuto(-1, false)
        setVisibility(View.VISIBLE)
    }

    fun loadRecent() {
        if (ZtriSettings.qckSchType.value == 2 && !this.loading && this.recentLoaded) {
            val arrayList = DataBookRecent.instance(this.dataActivity).items
            var gridShown = false
            DataBookRecent.instance(this.dataActivity).changed = false
            if (arrayList != null && !arrayList.isEmpty()) {
                gridShown = true
            }
            this.gridShown = gridShown
            this.recentList = arrayList
            this.layoutPending = true
            relayout()
        }
    }

    fun setAutoAdapter(webSearchAdapter: WebSearchAdapter?) {
        val myRecyclerView = this.autoList
        if (myRecyclerView == null) {
            return
        }
        this.autoAdapter = webSearchAdapter
        if (webSearchAdapter != null) {
            webSearchAdapter.listView = myRecyclerView
        }
        myRecyclerView.setAdapter(webSearchAdapter)
    }

    fun setBusyMode(busy: Boolean) {
        this.busy = busy
        if (busy) {
            return
        }
        if (this.layoutPending) {
            relayout()
            return
        }
        val busyShown = this.busyShown
        if (busyShown) {
            if (!busyShown || busy) {
                return
            }
            this.busyShown = false
            post(UpdatePlusItemTask())
            return
        }
        val busyFade = this.busyFade
        if (!busyFade) {
            if (this.refreshPending) {
                refreshLater()
            }
        } else {
            if (!busyFade || busy) {
                return
            }
            this.busyFade = false
            post(UpdateBodyPaddingTask())
        }
    }

    fun setLoadClip(clipText: String?) {
        val myClipView = this.clipView
        if (myClipView != null) {
            myClipView.fillText(clipText)
        }
        if (this.clipShown) {
            return
        }
        if (TextUtils.isEmpty(clipText)) {
            if (this.loading) {
                return
            }
            MainApp.runInBackground(this.dataActivity, CheckClipboardTask())
        } else {
            this.clipShown = true
            if (this.loading) {
                return
            }
            post(UpdateBodyPaddingLaterTask())
        }
    }

    companion object {
        @JvmStatic
        fun columnsFor(landscape: Boolean): Int {
            val searchType = ZtriSettings.qckSchType.value
            if (searchType == 0) {
                return 0
            }
            if (searchType == 2) {
                return 1
            }
            var portColumns = ZtriSettings.quickPort.value
            if (portColumns == 0) {
                ZtriSettings.editLocal { set(ZtriSettings.quickPort, 5) }
                portColumns = 5
            }
            var landColumns = ZtriSettings.quickLand.value
            if (landColumns == 0) {
                ZtriSettings.editLocal { set(ZtriSettings.quickLand, 5) }
                landColumns = 5
            }
            if (landscape) {
                portColumns = landColumns
            }
            if (portColumns == 0) {
                return 5
            }
            return portColumns
        }
    }
}
