package com.mycompany.app.quick

import com.mycompany.app.web.TabGridListener
import kotlinx.coroutines.launch
import com.mycompany.app.web.runMenuItem
import com.mycompany.app.web.updateRssButton
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings

import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.data.DataNews
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.db.book.DbBookQuick.Companion.colorIndex; import com.mycompany.app.db.book.DbBookQuick.Companion.iconFromCursor
import com.mycompany.app.db.book.DbBookQuick.Companion.countInFolder
import com.mycompany.app.db.book.DbBookQuick.Companion.itemsInFolder
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.loadItems
import com.mycompany.app.db.book.DbBookQuick.Companion.subItems
import com.mycompany.app.db.book.DbBookQuick.Companion.deletePath
import com.mycompany.app.db.book.DbBookQuick.Companion.insertItem
import com.mycompany.app.db.book.DbBookQuick.Companion.moveToFolder
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.quick.QuickAdapter.QuickHolder
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.quick.QuickAdapter.QuickListener
import com.mycompany.app.quick.QuickDragListener
import com.mycompany.app.ui.browser.IconButton
import com.mycompany.app.ui.components.LoadCover
import com.mycompany.app.view.MyProgressBar
import com.mycompany.app.view.MyWebBody

import java.util.Locale
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
class QuickView(@JvmField var ctx: Context?) : RelativeLayout(ctx) {
    @JvmField
    var firstLoad: Boolean = true
    @JvmField
    var updatePending: Boolean = false
    @JvmField
    var controlPending: Boolean = false
    @JvmField
    var secretShown: Boolean = false
    @JvmField
    var loadCover: LoadCover? = null
    @JvmField
    var scrollPos: Int = 0
    @JvmField
    var scrollOffset: Int = 0
    @JvmField
    var scrolledOnce: Boolean = false
    @JvmField
    var editing: Boolean = false
    @JvmField
    var executorCache: ExecutorService? = null
    @JvmField
    var pullEnabled: Boolean = false
    @JvmField
    var gridScrolled: Boolean = false
    @JvmField
    var scrollState: Int = 0
    @JvmField
    var newsUseState: Boolean = false
    @JvmField
    var newsUrlState: String? = null
    @JvmField
    var newsLangState: Int = 0
    @JvmField
    var newsPickOn: Boolean = false
    @JvmField
    var newsPickState: String? = null
    @JvmField
    var newsTime: Long = 0
    @JvmField
    var newsUseShown: Boolean = false
    @JvmField
    var editControl: QuickControl? = null
    @JvmField
    var editIndex: Int = 0
    @JvmField
    var backPressTime: Long = 0
    @JvmField
    var scrollPending: Boolean = false
    @JvmField
    var uiHandler: Handler? = null
    @JvmField
    var layoutPending: Boolean = false
    @JvmField
    var onStartPage: Boolean = false
    @JvmField
    var newsPending: Boolean = false
    @JvmField
    var paused: Boolean = false
    @JvmField
    var editPending: Boolean = false
    @JvmField
    var quickListener: QuickViewListener? = null
    @JvmField
    var itemPending: Boolean = false
    @JvmField
    var settingsButton: IconButton? = null
    @JvmField
    var loadPending: Boolean = false
    @JvmField
    var gridView: QuickGridView? = null
    @JvmField
    var itemAdapter: QuickAdapter? = null
    @JvmField
    var gridManager: QuickGridView? = null
    @JvmField
    var loading: Boolean = false
    @JvmField
    var controlPanel: QuickControl? = null
    @JvmField
    var quickUseState: Boolean = false
    @JvmField
    var portColumns: Int = 0
    @JvmField
    var landColumns: Int = 0
    @JvmField
    var quickSmallState: Boolean = false
    @JvmField
    var quickPlusState: Boolean = false
    @JvmField
    var quickDown: Boolean = false
    @JvmField
    var topOffset: Int = 0
    @JvmField
    var scrollTopPending: Boolean = false
    @JvmField
    var dragIndex: Int = 0
    @JvmField
    var refreshPending: Boolean = false
    @JvmField
    var refreshed: Boolean = false
    @JvmField
    var newsChanged: Boolean = false

    internal inner class NoOpClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class ScrollToPositionTask : Runnable {

        internal inner class ScrollToPositionStep(
                                             @JvmField val position: Int
        ) : Runnable {
            override fun run() {
                val myManagerGrid = this@QuickView.gridManager
                if (myManagerGrid != null) {
                    myManagerGrid.scrollToPosition(this.position)
                }
            }
        }

        override fun run() {
            val quickView = this@QuickView
            if (quickView.gridView != null) {
                quickView.scrollToNews()
                val dragIndex = quickView.dragIndex
                quickView.dragIndex = -1
                if (dragIndex == -1) {
                    return
                }
                quickView.gridView!!.post(ScrollToPositionStep(dragIndex))
            }
        }
    }

    internal inner class ScrollGridStep(@JvmField val outer: QuickView,
                                          @JvmField val scrollFirst: Boolean,
                                          @JvmField val toTop: Boolean
    ) : Runnable {
        override fun run() {
            val quickView = this.outer
            val myManagerGrid = quickView.gridManager
            val myRecyclerView: QuickGridView? = if (myManagerGrid == null) null else quickView.gridView
            if (myManagerGrid == null || myRecyclerView == null) {
                return
            }
            val scrollFirst = this.scrollFirst
            val toTop = this.toTop
            if (scrollFirst) {
                quickView.gridManager!!.scrollToPosition(if (toTop) 0 else myManagerGrid.itemCount - 1)
                return
            }
            var height = myRecyclerView.getHeight() - MainApp.dp48
            if (toTop) {
                height = -height
            }
            val scrollAnim = ZtwoSettings.scrollAnim.value
            val gridView = quickView.gridView
            if (scrollAnim) {
                gridView!!.smoothScrollBy(0, height)
            } else {
                gridView!!.scrollBy(0, height)
            }
        }
    }

    internal inner class DragScrollTask : Runnable {
        override fun run() {
            val quickView = this@QuickView
            val dragIndex = quickView.dragIndex
            quickView.dragIndex = -1
            val myManagerGrid: QuickGridView? = if (dragIndex == -1) null else quickView.gridManager
            if (dragIndex == -1 || myManagerGrid == null) {
                return
            }
            myManagerGrid.scrollToPosition(dragIndex)
        }
    }

    internal inner class InvalidateTask : Runnable {
        override fun run() {
            val quickView = this@QuickView
            if (quickView.uiHandler == null) {
                return
            }
            quickView.invalidate()
        }
    }

    internal inner class NotifyShownTask : Runnable {
        override fun run() {
            val quickView = this@QuickView
            quickView.notifyShown(quickView.onStartPage)
        }
    }

    internal inner class ScheduleScrollTask : Runnable {

        internal inner class ApplyListStep(
                                             @JvmField val items: ArrayList<QuickAdapter.QuickItem?>?
        ) : Runnable {
            override fun run() {
                val quickView = this@QuickView
                if (quickView.itemAdapter == null) {
                    return
                }
                quickView.scheduleScroll()
                quickView.itemAdapter!!.applyList(this.items, true)
                quickView.newsChanged = true
                quickView.setButtonView(false)
                if (quickView.newsUseShown) {
                    quickView.newsUseShown = false
                    if (ZtwoSettings.newsUse.value) {
                        quickView.loadNews(false)
                    }
                }
                quickView.scrollToTopItem()
                quickView.layoutPending = false
            }
        }

        override fun run() {
            val quickView = this@QuickView
            val arrayListI: ArrayList<QuickAdapter.QuickItem?>? = loadItems(quickView.ctx, true, !ZtwoSettings.newsUse.value, quickView.firstLoad)
            quickView.firstLoad = false
            quickView.updatePending = false
            val handler = quickView.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ApplyListStep(arrayListI))
        }
    }

    internal inner class CloseDeleteDialogTask : Runnable {

        internal inner class CloseDeleteDialogStep(
                                             @JvmField val checked: Boolean
        ) : Runnable {
            override fun run() {
                val quickView = this@QuickView
                val quickViewListener = quickView.quickListener
                if (quickViewListener != null) {
                    quickViewListener.closeDeleteDialog()
                }
                if (quickView.itemAdapter == null) {
                    return
                }
                if (this.checked) {
                    showToast(quickView.ctx, AppR.string.deleted)
                    quickView.setButtonView(false)
                    val quickListener = quickView.quickListener
                    if (quickListener != null) {
                        quickListener.loadSearchItems()
                    }
                }
                if (!quickView.exitEditMode() && quickView.itemAdapter != null) {
                    quickView.scheduleScroll()
                    quickView.itemAdapter!!.notifyDataSetChanged()
                }
                quickView.loadPending = false
            }
        }

        override fun run() {
            val quickView = this@QuickView
            val quickAdapter = quickView.itemAdapter
            if (quickAdapter != null) {
                val deleted = quickAdapter.deleteChecked(null)
                val handler = quickView.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(CloseDeleteDialogStep(deleted))
            }
        }
    }

    internal inner class ReloadItemsTask : Runnable {

        internal inner class ApplyEditListStep(
                                             @JvmField val items2: ArrayList<QuickAdapter.QuickItem?>?
        ) : Runnable {
            override fun run() {
                val quickView = this@QuickView
                val editIndex = quickView.editIndex
                if (quickView.itemAdapter == null || quickView.controlPanel == null) {
                    return
                }
                quickView.scheduleScroll()
                quickView.itemAdapter!!.applyList(this.items2, false)
                quickView.newsChanged = true
                quickView.setQuickEditMode(editIndex)
                quickView.controlPanel!!.setCount(quickView.itemAdapter!!.checkedCount(), quickView.itemAdapter!!.bookCount())
                quickView.newsPending = false
            }
        }

        override fun run() {
            val quickView = this@QuickView
            val arrayListI: ArrayList<QuickAdapter.QuickItem?>? = loadItems(quickView.ctx, true, true, quickView.firstLoad)
            quickView.firstLoad = false
            quickView.controlPending = false
            val handler = quickView.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ApplyEditListStep(arrayListI))
        }
    }

    internal inner class ScrollToNewsItemTask : Runnable {

        internal inner class ScrollToNewsStep(
                                             @JvmField val items3: ArrayList<QuickAdapter.QuickItem?>?
        ) : Runnable {

            internal inner class ScrollToNewsTask : Runnable {
                override fun run() {
                    val quickView = this@QuickView
                    val topicList: Array<String?>? = topics
                    quickView.scrollToNews()
                }
            }

            override fun run() {
                val quickView = this@QuickView
                if (quickView.itemAdapter != null) {
                    quickView.scheduleScroll()
                    quickView.itemAdapter!!.applyList(this.items3, true)
                    quickView.newsChanged = true
                    quickView.setButtonView(true)
                    val quickViewListener = quickView.quickListener
                    if (quickViewListener != null) {
                        quickViewListener.loadSearchItems()
                    }
                    quickView.editPending = false
                    if (quickView.quickDown) {
                        quickView.gridView!!.postDelayed(ScrollToNewsTask(), 200L)
                    }
                }
            }
        }

        /** Re-saves the stored shortcuts (regenerating their icons), then loads the shortcut list.  */
        override fun run() {
            val view = this@QuickView
            val context = view.ctx
            var list: ArrayList<QuickAdapter.QuickItem?>? = null
            if (context != null) {
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        get(context).getWritableDatabase(), "DbBookQuick_table", null, "_secret=?",
                        arrayOf<String?>("0"), "_order ASC"
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val colType = cursor.getColumnIndex("_rsv5")
                        val colExtra = cursor.getColumnIndex("_rsv1")
                        val colPath = cursor.getColumnIndex("_path")
                        val colTitle = cursor.getColumnIndex("_title")
                        val colIcon = cursor.getColumnIndex("_icon")
                        val colColor = cursor.getColumnIndex("_rsv4")
                        val colOrder = cursor.getColumnIndex("_order")
                        do {
                            val type = cursor.getInt(colType)
                            val extra = cursor.getString(colExtra)
                            val path = cursor.getString(colPath)
                            val title = cursor.getString(colTitle)
                            val color = cursor.getInt(colColor)
                            val order = cursor.getInt(colOrder)
                            var icon: Bitmap? = null
                            if (color == 0 || color == -460552) {
                                icon = iconFromCursor(cursor, colIcon, path, false)
                                if (MainUtil.isBitmapOk(icon)) {
                                    MainListLoader.cacheListIcon(path, icon, true)
                                }
                            }
                            insertItem(context, type, extra, path, title, icon, color, order)
                        } while (cursor.moveToNext())
                    }
                } catch (unused: Exception) {
                }
                if (cursor != null) {
                    cursor.close()
                }
                list = loadItems(context, true, !ZtwoSettings.newsUse.value, false)
            }
            view.secretShown = false
            if (view.uiHandler != null) {
                view.uiHandler!!.post(ScrollToNewsStep(list))
            }
        }
    }

    internal inner class UpdateHeaderFooterTask : Runnable {
        /**
         * Fills the news section: placeholder rows while nothing is loaded yet, otherwise the cached
         * news (reloaded only when the cache changed since the last call).
         */
        override fun run() {
            val view = this@QuickView
            val adapter = view.itemAdapter
            if (adapter == null) {
                return
            }
            var stamp = view.newsTime
            val cacheStamp = DataNews.instance(view.ctx).loadedAt
            if (adapter.appContext != null) {
                adapter.updateHeaderFooter()
                if (ZtwoSettings.newsUse.value || !ZtwoSettings.newsAllow.value) {
                    adapter.newsLoading = true
                    val shown = adapter.newsItems
                    if (shown == null || shown.size <= 2) {
                        val placeholders: ArrayList<QuickAdapter.QuickItem?> = ArrayList()
                        val types = intArrayOf(5, 6, 8)
                        for (newsIndex in types.indices) {
                            val item = QuickItem()
                            item.itemKind = types[newsIndex].toByte()
                            item.newsIndex = newsIndex
                            placeholders.add(item)
                        }
                        stamp = DataNews.instance(adapter.appContext).setItems(placeholders)
                        adapter.applyNewsList(placeholders, true)
                    } else {
                        if (stamp != cacheStamp) {
                            adapter.applyNewsList(DataNews.instance(adapter.appContext).items, true)
                        } else {
                            adapter.checkNewsHead(adapter.newsItems)
                            if (adapter.newsHeadOnly && adapter.newsDragPos != -1) {
                                val holder: QuickHolder? = adapter.rowAt(adapter.newsDragPos)
                                if (holder != null && holder.bar != null) {
                                    adapter.styleNotice(holder)
                                }
                            }
                        }
                        stamp = cacheStamp
                    }
                } else {
                    val placeholder: ArrayList<QuickAdapter.QuickItem?> = ArrayList()
                    val item = QuickItem()
                    item.itemKind = 4
                    item.newsIndex = 0
                    placeholder.add(item)
                    stamp = DataNews.instance(adapter.appContext).setItems(placeholder)
                    adapter.applyNewsList(placeholder, true)
                }
            }
            view.newsTime = stamp
        }
    }

    internal inner class QuickMenuNotiClick : OnClickListener {
        override fun onClick(view: View?) {
            val quickView = this@QuickView
            if (quickView.quickListener == null) {
                return
            }
            quickView.quickListener!!.showSettingsMenu(view)
        }
    }

    internal inner class ClearScrollPendingTask : Runnable {
        override fun run() {
            this@QuickView.scrollPending = false
        }
    }

    internal inner class ExitEditModeStep : QuickListener {
        override fun isLandscape(): Boolean {
            val topicList: Array<String?>? = topics
            val quickView = this@QuickView
            val quickViewListener = quickView.quickListener
            return if (quickViewListener == null) isLandscape(quickView.ctx) else quickViewListener.isLandscape()
        }

        override fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean) {
            val quickView = this@QuickView
            if (quickView.quickListener == null || quickView.editing) {
                return
            }
            quickView.exitEditMode()
            quickView.quickListener!!.showLinkDialog(quickItem, isFolder)
        }

        override fun hasTransNews(): Boolean {
            val quickViewListener = this@QuickView.quickListener
            if (quickViewListener == null) {
                return false
            }
            return quickViewListener.hasTransNews()
        }

        override fun openNewsItem(quickItem: QuickItem?) {
            val quickView = this@QuickView
            if (quickView.quickListener == null || quickView.editing) {
                return
            }
            quickView.exitEditMode()
            quickView.quickListener!!.openNewsItem(quickItem)
        }

        override fun newsListType(): Int {
            return this@QuickView.newsProgress
        }

        override fun openNewsSettings() {
            val quickView = this@QuickView
            if (quickView.quickListener == null || quickView.editing) {
                return
            }
            quickView.exitEditMode()
            quickView.quickListener!!.runQuickAction2()
        }

        override fun openNewsSources() {
            val quickView = this@QuickView
            if (quickView.quickListener == null || quickView.editing) {
                return
            }
            quickView.exitEditMode()
            quickView.quickListener!!.runQuickAction1()
        }

        override fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?) {
            val quickViewListener = this@QuickView.quickListener
            if (quickViewListener == null) {
                return
            }
            quickViewListener.translateNews(list)
        }

        override fun onItemClick(quickHolder: QuickHolder?, position: Int) {
            var width: Int
            val height: Int
            val widthValue: Int
            val heightValue: Int
            val quickView = this@QuickView
            val quickAdapter: QuickAdapter? = if (quickView.loading) null else quickView.itemAdapter
            if (quickView.loading || quickAdapter == null || quickView.quickListener == null || quickView.editing) {
                return
            }
            if (quickAdapter.editMode) {
                val quickItemD = quickAdapter.itemAt(position)
                if (quickItemD != null && quickItemD.itemKind.toInt() == 0) {
                    quickItemD.isChecked = !quickItemD.isChecked
                    quickAdapter.applyEditAlpha(position, true)
                }
                val quickControl = quickView.controlPanel
                if (quickControl != null) {
                    quickControl.setCount(quickView.itemAdapter!!.checkedCount(), quickView.itemAdapter!!.bookCount())
                    return
                }
                return
            }
            val quickItemD = quickAdapter.itemAt(position)
            if (quickItemD != null) {
                if (!quickItemD.isFolder) {
                    val itemKind = quickItemD.itemKind
                    if (itemKind.toInt() == 0) {
                        quickView.quickListener!!.openSearchSource(itemKind.toInt(), MainUtil.normalizeUrl(null, quickItemD.path))
                        return
                    }
                    if (itemKind.toInt() == 1) {
                        quickView.quickListener!!.openSearchSource(itemKind.toInt(), null)
                        return
                    }
                    if (itemKind.toInt() != 3 || !SyncSettings.secretMode.value || quickView.gridView == null || quickView.secretShown) {
                        return
                    }
                    quickView.secretShown = true
                    quickView.editPending = true
                    quickView.runInBackground(quickView.ScrollToNewsItemTask())
                    return
                }
                val myWebBodyF = quickView.quickListener!!.webBody()
                if (myWebBodyF != null) {
                    val location = IntArray(2)
                    myWebBodyF.getLocationOnScreen(location)
                    val paddingStart = myWebBodyF.getPaddingStart() + location[0]
                    val paddingTop = myWebBodyF.getPaddingTop() + location[1]
                    quickView.getLocationOnScreen(location)
                    val screenX = location[0]
                    val screenY = location[1]
                    val view: View? = if (quickHolder == null) null else quickHolder.itemView
                    if (quickHolder == null || view == null) {
                        val minSize = MainApp.dp16 * 5
                        width = quickView.getWidth() / 2
                        height = quickView.getHeight() / 2
                        widthValue = minSize
                        heightValue = widthValue
                    } else {
                        val viewWidth = view.getWidth()
                        val viewHeight = view.getHeight()
                        view.getLocationOnScreen(location)
                        width = (viewWidth / 2) + (location[0] - screenX)
                        height = ((viewHeight / 2) + (location[1] - screenY)) - MainApp.dp8
                        if (isRtl(quickView.ctx)) {
                            width = quickView.getWidth() - width
                        }
                        widthValue = viewWidth
                        heightValue = viewHeight
                    }
                    quickView.quickListener!!.openFolder(
                        (screenX - paddingStart) + width,
                        (screenY - paddingTop) + height,
                        widthValue,
                        quickItemD.path,
                        heightValue
                    )
                }
            }
        }

        override fun onEditMode(enabled: Boolean) {
            val quickView = this@QuickView
            if (quickView.quickListener == null || !ZtwoSettings.newsAllow.value) {
                return
            }
            ZtwoSettings.edit(quickView.ctx) { set(ZtwoSettings.newsAllow, false) }
            if (ZtwoSettings.newsUse.value != enabled) {
                ZtwoSettings.edit(quickView.ctx) { set(ZtwoSettings.newsUse, enabled) }
            }
            quickView.applyNewsChange(true)
            if (ZtwoSettings.newsUse.value) {
                quickView.quickListener!!.addRssButton()
            }
        }

        override fun onItemLongClick(quickHolder: QuickHolder?, position: Int) {
            val quickView = this@QuickView
            val quickAdapter: QuickAdapter? = if (quickView.loading) null else quickView.itemAdapter
            if (quickView.loading || quickAdapter == null || quickView.quickListener == null || quickView.editing) {
                return
            }
            if (!quickAdapter.editMode) {
                quickAdapter.applyEditMode(position, true)
                quickView.setButtonView(false)
                quickView.quickListener!!.setQuickMode(true)
            }
            val quickItemD = quickView.itemAdapter!!.itemAt(position)
            val grid: QuickGridView? = if (quickItemD == null || quickItemD.itemKind.toInt() != 0) null else quickView.gridView
            if (quickItemD == null || quickItemD.itemKind.toInt() != 0 || grid == null) {
                return
            }
            grid.startDrag(quickHolder)
        }
    }

    internal inner class ScrollPageStep : TabGridListener {
        override fun onChanged() {
            val quickViewListener = this@QuickView.quickListener
            if (quickViewListener != null) {
                quickViewListener.scrollPage()
            }
        }
    }

    internal inner class SaveOrderStep : QuickDragListener {

        internal inner class SaveOrderUiStep : Runnable {
            override fun run() {
                val quickView = this@QuickView
                val quickAdapter = quickView.itemAdapter
                val quickViewListener: QuickViewListener? = if (quickAdapter == null || quickView.loadCover != null || !quickAdapter.saveOrder(quickView.gridView)) null else quickView.quickListener
                if (quickAdapter == null || quickView.loadCover != null || !quickAdapter.saveOrder(quickView.gridView) || quickViewListener == null) {
                    return
                }
                quickViewListener.loadSearchItems()
            }
        }

        internal inner class SaveOrderTask : Runnable {

            internal inner class RescrollStep : Runnable {
                override fun run() {
                    val saveOrderStep = this@SaveOrderStep
                    val quickView = this@QuickView
                    if (quickView.itemAdapter == null) {
                        return
                    }
                    if (quickView.scrolledOnce) {
                        quickView.scheduleScroll()
                        this@QuickView.itemAdapter!!.notifyDataSetChanged()
                        val quickViewListener = this@QuickView.quickListener
                        if (quickViewListener != null) {
                            quickViewListener.loadSearchItems()
                        }
                    }
                    this@QuickView.exitEditMode()
                    val owner = this@QuickView
                    val myCoverView = owner.loadCover
                    if (myCoverView != null) {
                        myCoverView.release()
                        owner.removeView(owner.loadCover)
                        owner.loadCover = null
                    }
                    this@QuickView.itemPending = false
                }
            }

            override fun run() {
                var scrolledOnce: Boolean
                var footerStart: Int = 0
                var size: Int = 0
                var quickItemD: QuickItem? = null
                var target: QuickItem? = null
                val quickView = this@QuickView
                val quickAdapter = quickView.itemAdapter
                if (quickAdapter == null) {
                    return
                }
                val scrollPos = quickView.scrollPos
                val scrollOffset = quickView.scrollOffset
                val arrayList = quickAdapter.items
                if (arrayList == null || scrollPos < ((if (quickAdapter.hasHeader) 1 else 0).also {
                        footerStart = it
                    }) || scrollOffset < footerStart || scrollPos >= ((arrayList.size - quickAdapter.footerCount).also {
                        size = it
                    }) || scrollOffset >= size || (quickAdapter.itemAt(scrollPos)
                        .also { quickItemD = it }) == null || (quickAdapter.itemAt(scrollOffset)
                        .also { target = it }) == null || quickAdapter.orderBusy
                ) {
                    scrolledOnce = false
                } else {
                    quickAdapter.orderBusy = true
                    val isFolder = quickItemD!!.isFolder
                    val isFolderNow = target!!.isFolder
                    val context = quickAdapter.appContext
                    if (!isFolder) {
                        if (isFolderNow) {
                            val folderCount = countInFolder(context, target.path)
                            if (folderCount == 0) {
                                quickAdapter.orderBusy = false
                                scrolledOnce = false
                            } else {
                                moveToFolder(quickAdapter.appContext, folderCount, target.path, quickItemD.path)
                                target.subItems = subItems(quickAdapter.appContext, target.path)
                                quickAdapter.items!!.remove(quickItemD)
                                quickAdapter.orderBusy = false
                            }
                        } else {
                            DbBookQuick.addToFolder(context, quickItemD.path, target, null, 0)
                            target.subItems = subItems(quickAdapter.appContext, target.path)
                            quickAdapter.items!!.remove(quickItemD)
                            quickAdapter.orderBusy = false
                        }
                        scrolledOnce = true
                    } else if (isFolderNow) {
                        var folderCount = countInFolder(context, target.path)
                        val arrayListD: ArrayList<QuickAdapter.QuickItem?>? = if (folderCount == 0) null else itemsInFolder(quickAdapter.appContext, quickItemD.path)
                        if (folderCount == 0 || arrayListD == null || arrayListD.isEmpty()) {
                            quickAdapter.orderBusy = false
                            scrolledOnce = false
                        } else {
                            val arrayListDSize = arrayListD.size
                            var index = 0
                            while (index < arrayListDSize) {
                                val obj: Any = arrayListD.get(index)!!
                                index++
                                moveToFolder(quickAdapter.appContext, folderCount, target.path, (obj as QuickItem).path)
                                folderCount++
                            }
                            deletePath(quickAdapter.appContext, quickItemD.path, false)
                            target.subItems = subItems(quickAdapter.appContext, target.path)
                            quickAdapter.items!!.remove(quickItemD)
                            quickAdapter.orderBusy = false
                            scrolledOnce = true
                        }
                    } else {
                        val arrayListD: ArrayList<QuickAdapter.QuickItem?>? = itemsInFolder(context, quickItemD.path)
                        if (arrayListD == null || arrayListD.isEmpty()) {
                            quickAdapter.orderBusy = false
                            scrolledOnce = false
                        } else {
                            val arrayListDSize = arrayListD.size
                            var folderCount = 0
                            var index = 0
                            var first = true
                            while (index < arrayListDSize) {
                                val rawQuickItem: Any? = arrayListD.get(index)
                                index++
                                val quickItem = rawQuickItem as QuickItem
                                val appContext = quickAdapter.appContext
                                if (first) {
                                    DbBookQuick.addToFolder(
                                        appContext,
                                        quickItem.path,
                                        target,
                                        quickItemD.label,
                                        quickItemD.color
                                    )
                                    first = false
                                } else {
                                    moveToFolder(appContext, folderCount, target.path, quickItem.path)
                                }
                                folderCount++
                            }
                            deletePath(quickAdapter.appContext, quickItemD.path, false)
                            target.subItems = subItems(quickAdapter.appContext, target.path)
                            quickAdapter.items!!.remove(quickItemD)
                            quickAdapter.orderBusy = false
                            scrolledOnce = true
                        }
                    }
                }
                quickView.scrolledOnce = scrolledOnce
                quickView.scrolledOnce = quickView.itemAdapter!!.saveOrder(null) || quickView.scrolledOnce
                quickView.post(RescrollStep())
            }
        }

        override fun onDragState(state: Int) {
            val quickView = this@QuickView
            quickView.loading = false
            if (state == 2) {
                quickView.loading = true
            } else if (state == 0 && quickView.itemAdapter != null && quickView.loadCover == null) {
                quickView.post(SaveOrderUiStep())
            }
        }

        override fun onMove(from: Int, to: Int): Boolean {
            val quickView = this@QuickView
            val quickAdapter = quickView.itemAdapter
            if (quickAdapter == null || quickView.loadCover != null) {
                return false
            }
            return quickAdapter.moveItem(from, to)
        }

        override fun onMerge(from: Int, to: Int) {
            val quickView = this@QuickView
            if (quickView.itemAdapter == null) {
                return
            }
            quickView.itemPending = true
            if (quickView.loadCover == null) {
                val myCoverView = LoadCover(
                    quickView.ctx,
                    -328966,
                    Math.round(MainApp.dp16 / 8.0f),
                    MainApp.dp16 shl 1
                )
                quickView.loadCover = myCoverView
                myCoverView.setVisibility(View.GONE)
                quickView.loadCover!!.setBackColor(-1593835520)
                quickView.loadCover!!.setOnClickListener(ConsumeCoverClick())
                quickView.addView(quickView.loadCover, -1, -1)
                quickView.loadCover!!.showCover(true, 1.0f, 200L)
            }
            quickView.scrollPos = from
            quickView.scrollOffset = to
            quickView.runInBackground(SaveOrderTask())
        }
    }

    internal inner class UpdateHeaderHeightStep : ImageSizeListener {
        override fun onSize(view: View?, width: Int, height: Int) {
            val quickView = this@QuickView
            val quickDown = quickView.quickDown
            val quickAdapter: QuickAdapter? = if (!quickDown) null else quickView.itemAdapter
            if (!quickDown || quickAdapter == null) {
                return
            }
            quickAdapter.updateHeaderHeight(quickView.availableHeight(height), quickDown)
            if (quickView.itemAdapter!!.getItemCount() == 0) {
                return
            }
            quickView.scheduleScroll()
            quickView.itemAdapter!!.notifyDataSetChanged()
            quickView.scrollToTopItem()
        }
    }

    /** The grid scrolled: remember whether it left its top, and tell the listener (news mode). */
    private fun onGridScrolled(scrolled: Boolean) {
        this.scrollState = if (scrolled) 1 else 0
        this.gridScrolled = scrolled
        val quickViewListener: QuickViewListener? = if (ZtwoSettings.newsUse.value && this.onStartPage) this.quickListener else null
        quickViewListener?.refreshUpButton()
    }

    internal inner class ConsumeCoverClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    interface QuickViewListener {
        fun isBarUpdatePending(): Boolean

        fun isLandscape(): Boolean

        fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean)

        fun hasTransNews(): Boolean

        fun openNewsItem(quickItem: QuickItem?)

        fun webBody(): MyWebBody?

        fun onPull(x: Float, y: Float, action: Int): Boolean

        fun newsListType(): Int

        fun setQuickMode(enabled: Boolean)

        fun closeDeleteDialog()

        fun runQuickAction2()

        fun runQuickAction1()

        fun showEditDialog(quickItem: QuickItem?)

        fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?)

        fun showDeleteDialog(quickItem: QuickItem?, position: Int)

        fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int)

        fun isAddingTab(): Boolean

        fun addRssButton()

        fun showSettingsMenu(view: View?)

        fun openSearchSource(type: Int, url: String?)

        fun refreshUpButton()

        fun loadSearchItems()

        fun scrollPage()

        fun hasNews(): Boolean

        fun loadNews()
    }

    var newsProgress: Int

        get() {
            val quickViewListener = this.quickListener
            if (quickViewListener == null) {
                return 0
            }
            return quickViewListener.newsListType()
        }
        set(i) {
            if (this.itemAdapter == null || !isDragging()) {
                return
            }
            val quickAdapter = this.itemAdapter
            if (i == 0) {
                quickAdapter!!.javaClass
                return
            }
            val myProgressBar: MyProgressBar = quickAdapter!!.rowAt(quickAdapter.newsDragPos)?.bar ?: return
            myProgressBar.stepQuick(this, i)
        }

    fun setButtonView(showEmpty: Boolean) {
        val myButtonImage: IconButton? = if (this.itemAdapter == null) null else this.settingsButton
        if (this.itemAdapter == null || myButtonImage == null) {
            return
        }
        myButtonImage.setVisibility(if (this.onStartPage) View.VISIBLE else View.GONE)
        if (this.itemAdapter!!.editMode) {
            this.settingsButton!!.setEnabled(false)
            return
        }
        applyButtonColors()
        this.settingsButton!!.setEnabled(true)
        if (SyncSettings.secretMode.value && showEmpty && this.itemAdapter!!.bookCount() == 0) {
            showToast(this.ctx, AppR.string.import_no_quick)
        }
    }

    fun scheduleScroll() {
        this.scrollPending = true
        postDelayed(ClearScrollPendingTask(), 200L)
    }

    fun applyButtonColors() {
        if (this.settingsButton == null) {
            return
        }
        if (!MainUtil.isDarkBars(this.onStartPage)) {
            val darkUi = AppState.darkUi
            val myButtonImage = this.settingsButton
            if (darkUi) {
                myButtonImage!!.setImageResource(AppR.drawable.outline_more_vert_dark_24)
                this.settingsButton!!.setMaxAlpha(0.7f)
                this.settingsButton!!.applyColors(0, -12632257)
                return
            } else {
                myButtonImage!!.setImageResource(AppR.drawable.outline_more_vert_black_24)
                this.settingsButton!!.setMaxAlpha(1.0f)
                this.settingsButton!!.applyColors(0, -2039584)
                return
            }
        }
        if (AppState.darkUi || BrowserSettings.darkHome.value) {
            this.settingsButton!!.setImageResource(AppR.drawable.outline_more_vert_dark_24)
            this.settingsButton!!.setMaxAlpha(0.7f)
            this.settingsButton!!.applyFillRadius(MainApp.dp8.toFloat(), true)
            this.settingsButton!!.applyColors(-16777216, -1586137739)
            return
        }
        this.settingsButton!!.setImageResource(AppR.drawable.outline_more_vert_black_24)
        this.settingsButton!!.setMaxAlpha(0.7f)
        this.settingsButton!!.applyFillRadius(MainApp.dp8.toFloat(), true)
        this.settingsButton!!.applyColors(-1, -1586137739)
    }

    fun scrollToNews() {
        val quickAdapter: QuickAdapter? = if (!this.quickDown || this.gridManager == null) null else this.itemAdapter
        val headerOffset = if (!this.quickDown || this.gridManager == null || quickAdapter == null) 0 else (quickAdapter.headerSize() - 1)
        if (!this.quickDown || this.gridManager == null || quickAdapter == null || headerOffset <= 0) {
            return
        }
        this.gridManager!!.scrollToPositionWithOffset(0, -headerOffset)
    }

    fun refreshGrid(newsUse: Boolean, newsTitle: Boolean) {
        val myRecyclerView: QuickGridView? = if (this.gridManager == null) null else this.gridView
        if (this.gridManager == null || myRecyclerView == null) {
            return
        }
        myRecyclerView.post(ScrollGridStep(this, newsTitle, newsUse))
    }

    fun applySettings() {
        if (this.gridView == null) {
            return
        }
        clearPending()
        val newsAllow = ZtwoSettings.newsAllow.value
        val context = this.ctx
        if (newsAllow && ZtwoSettings.newsUse.value && this.onStartPage) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.newsAllow, false) }
        }
        if (this.portColumns != ZtriSettings.quickPort.value || this.landColumns != ZtriSettings.quickLand.value || this.quickSmallState != PdfSettings.quickSmall.value || this.quickPlusState != ZtriSettings.quickPlus.value || this.quickDown != isQuickDown() || this.topOffset != availableHeight(
                0
            )
        ) {
            applyLayout()
        }
        if (this.quickUseState != ZtriSettings.quickUse.value) {
            notifyShown(this.onStartPage)
        }
        val newsUse = ZtwoSettings.newsUse.value
        val newsTitle = newsUse && ZtwoSettings.newsTitle.value
        val pageUrl = if (newsTitle) ZtwoSettings.newsPick.value else ""
        if (this.newsUseState && !newsUse && this.onStartPage) {
            com.mycompany.app.glide.ThumbLoader.stopAll()
        }
        if (this.newsUseState != ZtwoSettings.newsUse.value || this.newsLangState != ZtwoSettings.newsLang.value || this.newsPickOn != newsTitle || !MainUtil.sameObject(this.newsPickState, pageUrl) || !MainUtil.sameObject(
                this.newsUrlState,
                ZtwoSettings.newsUrl.value
            ) || this.newsTime != DataNews.instance(context).loadedAt
        ) {
            applyNewsChange(false)
        } else if (ZtwoSettings.newsUse.value && this.onStartPage && this.newsTime != 0L && System.currentTimeMillis() > this.newsTime + 1800000) {
            loadNews(true)
        }
        if (ZtwoSettings.newsUse.value && this.onStartPage && !this.newsUseShown) {
            val quickViewListener = this.quickListener
            if (!(if (quickViewListener == null) false else quickViewListener.hasNews()) && !hasNews(
                    DataNews.instance(
                        context
                    ).items
                )
            ) {
                loadNews(true)
            }
        }
        setVisibility(View.VISIBLE)
        this.editing = false
        onNewsResume()
    }

    /**
     * Back on the News page (e.g. from its settings): the tabs follow the settings and the feed list; a tab that is gone
     * (Top stories switched off, a removed feed) gives way to the first one; a changed feed list is read again.
     * The scroll position stays.
     */
    private fun onNewsResume() {
        val adapter = this.itemAdapter ?: return
        if (!adapter.newsPage) return
        val key = if (!adapter.rssPage) TAB_TOP else (adapter.rssFilter ?: TAB_ALL)
        if (newsTabs().none { it.first == key }) {
            showNewsTab(TAB_ALL)
            return
        }
        refreshNewsTabs()
        if (adapter.rssPage && RssFeeds.stale()) loadRss(false)
    }

    fun exitEditMode(): Boolean {
        val quickAdapter = this.itemAdapter
        if (quickAdapter == null || this.quickListener == null || !quickAdapter.editMode) {
            return false
        }
        quickAdapter.applyEditMode(-1, false)
        setButtonView(false)
        this.quickListener!!.setQuickMode(false)
        return true
    }

    fun addItem(path: String?, status: Int, type: Int, stringExtra: String?) {
        val quickAdapter = this.itemAdapter
        if (quickAdapter == null) {
            return
        }
        quickAdapter.insertItem(path, status, type, stringExtra)
        setButtonView(false)
    }

    fun updateQuickUse() {
        val quickAdapter: QuickAdapter? = if (!isQuickDown()) null else this.itemAdapter
        if (!isQuickDown() || quickAdapter == null) {
            return
        }
        if (this.quickUseState == ZtriSettings.quickUse.value && this.newsUseState == ZtwoSettings.newsUse.value) {
            return
        }
        quickAdapter.updateHeaderHeight(availableHeight(this.gridView!!.getHeight()), true)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val pullEnabled: Boolean
        var myRecyclerView: QuickGridView? = null
        if (isBusy()) {
            if (event != null) {
                val actionMasked = event.getActionMasked()
                if (actionMasked == 0) {
                    if (this.backPressTime == 0L) {
                        this.backPressTime = System.currentTimeMillis()
                        return true
                    }
                } else if ((actionMasked == 1 || actionMasked == 3) && System.currentTimeMillis() - this.backPressTime > 500) {
                    clearPending()
                }
            }
            return true
        }
        this.backPressTime = 0L
        if (!ZoneSettings.pullRefresh.value || !this.onStartPage) {
            return super.dispatchTouchEvent(event)
        }
        val quickAdapter: QuickAdapter? = if (this.loading || this.quickListener == null) null else this.itemAdapter
        if (this.loading || this.quickListener == null || quickAdapter == null || quickAdapter.editMode) {
            return super.dispatchTouchEvent(event)
        }
        val actionMasked = event!!.getActionMasked()
        if (actionMasked == 0) {
            val quickDown = this.quickDown
            val gridScrolled = this.gridScrolled
            if (!quickDown) {
                pullEnabled = true xor gridScrolled
                this.pullEnabled = pullEnabled
            } else if (gridScrolled) {
                this.pullEnabled = false
                pullEnabled = false
            } else {
                pullEnabled = this.scrollState == 0
                this.pullEnabled = pullEnabled
            }
            if (pullEnabled && !BrowserSettings.showStatus.value && event.getY() < MainApp.dp16) {
                this.pullEnabled = false
            }
        } else if (actionMasked == 1) {
            if (canPullRefresh()) {
                this.quickListener!!.onPull(event.getX(), event.getY(), actionMasked)
            }
            this.pullEnabled = false
        } else if (actionMasked != 2) {
            if (actionMasked == 3) {
                if (canPullRefresh()) {
                    this.quickListener!!.onPull(event.getX(), event.getY(), actionMasked)
                }
                this.pullEnabled = false
            } else if (actionMasked == 5) {
                if (canPullRefresh()) {
                    this.quickListener!!.onPull(0.0f, 0.0f, 3)
                }
                this.pullEnabled = false
            }
        } else if (this.gridScrolled) {
            if (canPullRefresh()) {
                this.quickListener!!.onPull(0.0f, 0.0f, 3)
            }
            this.pullEnabled = false
        } else if (canPullRefresh() && this.quickListener!!.onPull(
                event.getX(),
                event.getY(),
                actionMasked
            ) && (this.gridView.also { myRecyclerView = it }) != null
        ) {
            myRecyclerView!!.setPullDown(true)
        }
        return super.dispatchTouchEvent(event)
    }

    fun applyNewsChange(landscape: Boolean) {
        if (this.itemAdapter != null) {
            var newsPickOn = false
            if (ZtwoSettings.newsAllow.value && ZtwoSettings.newsUse.value && this.onStartPage) {
                ZtwoSettings.edit(this.ctx) { set(ZtwoSettings.newsAllow, false) }
            }
            updateQuickUse()
            val newsUseState = ZtwoSettings.newsUse.value
            this.newsUseState = newsUseState
            this.newsUrlState = ZtwoSettings.newsUrl.value
            this.newsLangState = ZtwoSettings.newsLang.value
            if (newsUseState && ZtwoSettings.newsTitle.value) {
                newsPickOn = true
            }
            this.newsPickOn = newsPickOn
            this.newsPickState = if (newsPickOn) ZtwoSettings.newsPick.value else ""
            if (newsUseState && this.onStartPage) {
                if (this.newsUseShown) {
                    return
                }
                loadNews(landscape)
            } else {
                this.newsTime = 0L
                scheduleScroll()
                this.itemAdapter!!.applyNewsList(null, true)
                this.newsChanged = true
            }
        }
    }

    fun applyColumns(landscape: Boolean) {
        val myManagerGrid = this.gridManager
        if (myManagerGrid != null) {
            val spanCount = if (landscape) this.landColumns else this.portColumns
            if (spanCount == 0 || myManagerGrid.spanCount == spanCount) {
                return
            }
            myManagerGrid.spanCount = spanCount
        }
    }

    fun applyLayout() {
        if (this.itemAdapter == null) {
            return
        }
        var portColumns = ZtriSettings.quickPort.value
        var landColumns = 5
        if (portColumns == 0) {
            ZtriSettings.editLocal { set(ZtriSettings.quickPort, 5) }
            portColumns = 5
        }
        val quickLand = ZtriSettings.quickLand.value
        if (quickLand == 0) {
            ZtriSettings.editLocal { set(ZtriSettings.quickLand, 5) }
        } else {
            landColumns = quickLand
        }
        this.portColumns = portColumns
        this.landColumns = landColumns
        this.quickSmallState = PdfSettings.quickSmall.value
        this.quickPlusState = ZtriSettings.quickPlus.value
        this.quickDown = isQuickDown()
        this.topOffset = availableHeight(0)
        val quickViewListener = this.quickListener
        applyColumns(if (quickViewListener == null) isLandscape(this.ctx) else quickViewListener.isLandscape())
        this.itemAdapter!!.updateHeaderHeight(availableHeight(this.gridView!!.getHeight()), this.quickDown)
        this.itemAdapter!!.updatePlusItem()
        scheduleScroll()
        this.itemAdapter!!.notifyDataSetChanged()
    }

    val transPos: Int
        get() {
            val quickAdapter = this.itemAdapter
            val rows = quickAdapter?.host ?: return 0
            return rows.firstVisible()
        }

    fun scrollToTopItem() {
        val quickAdapter: QuickAdapter? = if (!this.scrollTopPending) null else this.itemAdapter
        if (!this.scrollTopPending || quickAdapter == null || quickAdapter.getItemCount() == 0 || this.itemAdapter!!.headerSize() - 1 <= 0) {
            return
        }
        this.scrollTopPending = false
        this.gridView!!.post(ScrollToPositionTask())
    }

    fun loadItems(): Boolean {
        if (this.itemAdapter == null) {
            return false
        }
        this.loadPending = true
        runInBackground(CloseDeleteDialogTask())
        return true
    }

    fun availableHeight(height: Int): Int {
        var height = height
        if (!this.onStartPage) {
            return height
        }
        if (!BrowserSettings.fixTop.value) {
            height -= topBarHeight()
        }
        return if (!BrowserSettings.fixBot.value) height - MainUtil.bottomInsetTotal() else height
    }

    fun clearPending() {
        this.backPressTime = 0L
        this.scrollPending = false
        this.layoutPending = false
        this.newsPending = false
        this.editPending = false
        this.itemPending = false
        this.loadPending = false
    }

    fun setupView() {
        if (this.settingsButton == null || this.gridView == null) {
            return
        }
        this.uiHandler = Handler(Looper.getMainLooper())
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
        this.quickUseState = ZtriSettings.quickUse.value
        this.portColumns = portColumns
        this.landColumns = landColumns
        this.quickSmallState = PdfSettings.quickSmall.value
        this.quickPlusState = ZtriSettings.quickPlus.value
        this.quickDown = isQuickDown()
        this.topOffset = availableHeight(0)
        this.scrollTopPending = this.quickDown
        this.dragIndex = -1
        val newsUseState = ZtwoSettings.newsUse.value
        this.newsUseState = newsUseState
        this.newsUrlState = ZtwoSettings.newsUrl.value
        this.newsLangState = ZtwoSettings.newsLang.value
        val newsPickOn = newsUseState && ZtwoSettings.newsTitle.value
        this.newsPickOn = newsPickOn
        this.newsPickState = if (newsPickOn) ZtwoSettings.newsPick.value else ""
        setColor(false)
        setOnClickListener(NoOpClick())
        this.settingsButton!!.setOnClickListener(QuickMenuNotiClick())
        val quickViewListener = this.quickListener
        val columns =
            if (if (quickViewListener == null) isLandscape(this.ctx) else quickViewListener.isLandscape()) this.landColumns else this.portColumns
        val grid = this.gridView!!
        this.gridManager = grid
        grid.spanCount = if (columns != 0) columns else 5
        scheduleScroll()
        val onStartPage = this.onStartPage
        val quickAdapter =
            QuickAdapter(this.ctx, if (onStartPage) 0 else 3, onStartPage, ExitEditModeStep())
        this.itemAdapter = quickAdapter
        quickAdapter.tabListener = ScrollPageStep()
        quickAdapter.homeListener = homeListener
        val dragListener = SaveOrderStep()
        grid.setDragListener(dragListener)
        this.itemAdapter!!.updateHeaderHeight(availableHeight(grid.getHeight()), this.quickDown)
        grid.setSizeListener(UpdateHeaderHeightStep())
        grid.setAdapter(this.itemAdapter)
        grid.setOnScrolled { scrolled -> onGridScrolled(scrolled) }
    }

    fun refresh() {
        if (this.onStartPage) {
            this.itemAdapter?.refreshHomeBooks()
            this.newsUseShown = ZtwoSettings.newsUse.value
            applySettings()
            notifyShown(this.onStartPage)
            return
        }
        val quickControl = this.editControl
        this.editControl = null
        val myButtonImage: IconButton? = if (quickControl == null || this.itemAdapter == null) null else this.settingsButton
        if (quickControl == null || this.itemAdapter == null || myButtonImage == null) {
            return
        }
        this.controlPanel = quickControl
        myButtonImage.setVisibility(View.GONE)
        if (this.controlPending) {
            return
        }
        this.controlPending = true
        this.newsPending = true
        runInBackground(ReloadItemsTask())
    }

    fun createButton() {
        val context = this.ctx
        if (context == null) {
            return
        }
        val myButtonImage = IconButton(context)
        val quickViewSet = AppR.id.quick_view_set
        myButtonImage.setId(quickViewSet)
        myButtonImage.setScaleType(ScaleType.CENTER_INSIDE)
        // the start page's menu (⋮) in the top right corner (was a ▾ at the top left)
        val buttonParams = LayoutParams(MainApp.dp48, MainApp.dp40)
        buttonParams.addRule(ALIGN_PARENT_END)
        addView(myButtonImage, buttonParams)
        val myRecyclerView = QuickGridView(context)
        val left = MainApp.dp8
        myRecyclerView.setPadding(left, 0, left, 0)
        myRecyclerView.applyRound(true, true)
        addView(myRecyclerView, com.mycompany.app.outline.LegacyViews.relativeParams(-1, -1, 3, quickViewSet))
        this.settingsButton = myButtonImage
        this.gridView = myRecyclerView
    }

    fun isBusy(): Boolean {
        return this.scrollPending || this.layoutPending || this.newsPending || this.editPending || this.itemPending || this.loadPending
    }

    fun isQuickDown(): Boolean {
        return ZtwoSettings.quickDown.value && this.onStartPage
    }

    fun setColor(night: Boolean) {
        if (this.gridView != null) {
            if (this.onStartPage) {
                if (isDarkMode()) {
                    setBackgroundColor(0)
                } else if (AppState.darkUi) {
                    setBackgroundColor(-16777216)
                } else {
                    setBackgroundColor(-1)
                }
            } else if (AppState.darkUi) {
                setBackgroundColor(-14606047)
            } else {
                setBackgroundColor(-1)
            }
            applyButtonColors()
            val quickAdapter: QuickAdapter? = if (!night) null else this.itemAdapter
            if (!night || quickAdapter == null) {
                return
            }
            quickAdapter.darkState = this.onStartPage
            scheduleScroll()
            this.itemAdapter!!.notifyDataSetChanged()
        }
    }

    fun setNewsPrepared(list: MutableList<QuickItem?>?) {
        val quickAdapter = this.itemAdapter
        if (quickAdapter != null) {
            quickAdapter.applyNewsList(list, true)
        }
    }

    fun setQuickControl(quickControl: QuickControl?) {
        this.controlPanel = quickControl
        val quickAdapter = this.itemAdapter
        if (quickAdapter == null || quickControl == null) {
            return
        }
        quickControl.setCount(quickAdapter.checkedCount(), this.itemAdapter!!.bookCount())
    }

    fun setQuickEditMode(dragIndex: Int) {
        val quickAdapter = this.itemAdapter
        if (quickAdapter == null || this.quickListener == null || this.gridView == null) {
            return
        }
        if (!quickAdapter.editMode) {
            quickAdapter.editMode = true
            val quickItemD = quickAdapter.itemAt(dragIndex)
            if (quickItemD != null && quickItemD.itemKind.toInt() == 0) {
                quickItemD.isChecked = true
            }
            quickAdapter.notifyDataSetChanged()
        }
        setButtonView(false)
        this.quickListener!!.setQuickMode(true)
        this.dragIndex = dragIndex
        this.gridView!!.postDelayed(DragScrollTask(), 200L)
    }

    fun setQuickSearch(enabled: Boolean) {
        if (this.uiHandler == null || !enabled) {
            return
        }
        exitEditMode()
    }

    fun setTransLoaded(isOk: Boolean) {
        val quickAdapter = this.itemAdapter
        if (quickAdapter != null) {
            quickAdapter.refreshNewsRange()
            if (isOk || !quickAdapter.newsPending) {
                return
            }
            quickAdapter.newsPending = false
            quickAdapter.checkNewsChange(quickAdapter.newsItems)
        }
    }

    fun loadNews(enabled: Boolean) {
        if (this.itemAdapter == null) {
            return
        }
        // news are fetched only while the news page is open (no request for every start page / new tab)
        if (this.onStartPage && (!this.itemAdapter!!.newsPage || this.itemAdapter!!.rssPage)) {
            val list = DataNews.instance(this.ctx).items
            if (!enabled && hasNews(list)) {
                this.newsTime = DataNews.instance(this.ctx).loadedAt
                this.itemAdapter!!.applyNewsList((list), true)
            }
            return
        }
        if (!enabled) {
            val context = this.ctx
            val list = DataNews.instance(context).items
            if (hasNews(list)) {
                this.newsTime = DataNews.instance(context).loadedAt
                scheduleScroll()
                this.itemAdapter!!.applyNewsList((list), true)
                this.newsChanged = true
                return
            }
        }
        val quickViewListener = this.quickListener
        if (quickViewListener != null) {
            quickViewListener.loadNews()
        }
        post(UpdateHeaderFooterTask())
    }

    fun notifyShown(onStartPage: Boolean) {
        val quickViewListener: QuickViewListener? = if (this.paused || this.itemAdapter == null) null else this.quickListener
        if (this.paused || this.itemAdapter == null || quickViewListener == null) {
            return
        }
        if (onStartPage) {
            if (this.refreshPending) {
                this.refreshPending = false
                this.refreshed = false
                val handler = this.uiHandler
                if (handler != null) {
                    handler.post(InvalidateTask())
                    return
                }
                return
            }
            if (quickViewListener.isAddingTab()) {
                this.refreshPending = false
                this.refreshed = true
                return
            }
        }
        this.refreshPending = false
        this.refreshed = false
        if (this.onStartPage && this.quickListener!!.isBarUpdatePending()) {
            val handler = this.uiHandler
            if (handler != null) {
                handler.postDelayed(NotifyShownTask(), 200L)
                return
            }
            return
        }
        updateQuickUse()
        val quickUseState = ZtriSettings.quickUse.value
        this.quickUseState = quickUseState
        if (quickUseState) {
            if (this.updatePending) {
                return
            }
            this.updatePending = true
            this.layoutPending = true
            runInBackground(ScheduleScrollTask())
            return
        }
        this.updatePending = false
        if (this.itemAdapter != null) {
            scheduleScroll()
            this.itemAdapter!!.applyList(null, true)
            this.newsChanged = true
            setButtonView(false)
            if (this.newsUseShown) {
                this.newsUseShown = false
                if (ZtwoSettings.newsUse.value) {
                    loadNews(false)
                }
            }
        }
    }

    fun isDragging(): Boolean {
        if (this.itemAdapter != null && this.quickListener != null && !isBusy() && !this.quickListener!!.isBarUpdatePending()) {
            val quickAdapter = this.itemAdapter
            if (quickAdapter!!.newsHeadOnly && quickAdapter.newsDragPos != -1) {
                return true
            }
        }
        return false
    }

    fun canPullRefresh(): Boolean {
        return this.pullEnabled && ZoneSettings.pullRefresh.value && this.quickListener != null
    }

    fun releaseView() {
        rssJob?.cancel()
        if (ZtwoSettings.newsUse.value && this.onStartPage) {
            com.mycompany.app.glide.ThumbLoader.stopAll()
        }
        val myButtonImage = this.settingsButton
        if (myButtonImage != null) {
            myButtonImage.release()
            this.settingsButton = null
        }
        val myRecyclerView = this.gridView
        if (myRecyclerView != null) {
            myRecyclerView.release()
            this.gridView = null
        }
        val quickAdapter = this.itemAdapter
        if (quickAdapter != null) {
            quickAdapter.release()
            this.itemAdapter = null
        }
        val myCoverView = this.loadCover
        if (myCoverView != null) {
            myCoverView.release()
            this.loadCover = null
        }
        clearHandler(this.uiHandler)
        this.uiHandler = null
        this.quickListener = null
        this.gridManager = null
        this.controlPanel = null
        this.executorCache = null
        this.newsUrlState = null
    }

    fun runInBackground(runnable: Runnable?) {
        var executorServiceH = this.executorCache
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(this.ctx)
            if (executorServiceH == null) {
                return
            } else {
                this.executorCache = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    /** The strip's taps (news page, a bookmark, the bookmark list). */
    private val homeListener = object : QuickAdapter.HomeListener {
        override fun openNews() = openNewsPage()

        override fun closeNews() = closeNewsPage()

        override fun selectTab(index: Int) {
            val key = newsTabs().getOrNull(index)?.first ?: return
            showNewsTab(key)
        }

        override fun addFeed() {
            if (this@QuickView.itemAdapter?.newsPage != true) return
            RssFeedsDialog(this@QuickView.ctx) {
                // a new feed: show "My feeds" (or stay on the feed's own tab) and read again
                val adapter = this@QuickView.itemAdapter ?: return@RssFeedsDialog
                // (a removed feed's tab falls back to the first tab; the changed list is read again: stale)
                showNewsTab(if (!adapter.rssPage) TAB_ALL else (adapter.rssFilter ?: TAB_ALL))
            }
        }

        override fun openBook(path: String) {
            if (this@QuickView.loading || this@QuickView.editing) return
            this@QuickView.quickListener?.openSearchSource(0, MainUtil.normalizeUrl(null, path))
        }

        override fun openAllBooks() {
            if (this@QuickView.loading || this@QuickView.editing) return
            webActivity()?.runMenuItem(null, 29)
        }
    }

    private fun webActivity(): com.mycompany.app.web.WebViewActivity? {
        var c: Context? = this.ctx
        while (c is android.content.ContextWrapper) {
            if (c is com.mycompany.app.web.WebViewActivity) return c
            c = c.baseContext
        }
        return null
    }

    /** True while the news page is shown instead of the start page. */
    val newsPageOpen: Boolean get() = this.itemAdapter?.newsPage == true

    /** The News page's tabs: (key, label) — Top stories (Google News, when on), My feeds, then each feed. */
    fun newsTabs(): List<Pair<String, String>> {
        val ctx = this.ctx ?: return emptyList()
        val list = ArrayList<Pair<String, String>>()
        if (ZtwoSettings.newsUse.value) list.add(TAB_TOP to ctx.getString(ch.opumlabs.opumbrowser.R.string.news_top))
        list.add(TAB_ALL to ctx.getString(ch.opumlabs.opumbrowser.R.string.news_my_feeds))
        for (feed in RssFeeds.feeds()) list.add(feed to RssFeeds.titleOf(feed))
        return list
    }

    /** Passes the tabs and the selected one to the masthead. */
    private fun refreshNewsTabs() {
        val adapter = this.itemAdapter ?: return
        val tabs = newsTabs()
        val key = when {
            !adapter.rssPage -> TAB_TOP
            adapter.rssFilter == null -> TAB_ALL
            else -> adapter.rssFilter
        }
        adapter.tabLabels = tabs.map { it.second }
        adapter.tabIndex = tabs.indexOfFirst { it.first == key }.coerceAtLeast(0)
        if (adapter.newsPage) adapter.notifyItemChanged(0)
    }

    /**
     * Opens the News page (2026-11-20: news and RSS are one page) on the tab used last: Top stories (Google News),
     * My feeds (all feeds mixed) or one feed. Each tab is fetched only while it is shown.
     */
    fun openNewsPage() {
        val adapter = this.itemAdapter ?: return
        if (adapter.newsPage || adapter.editMode) return
        RssFeeds.migrateCustomNews(this.ctx)
        adapter.newsPage = true
        showNewsTab(ZtwoSettings.newsTab.value ?: "")
    }

    /** Shows tab [key] (falls back to the first tab when it is gone) and remembers it. */
    fun showNewsTab(wantedKey: String) {
        val adapter = this.itemAdapter ?: return
        if (!adapter.newsPage) return
        val tabs = newsTabs()
        val key = tabs.firstOrNull { it.first == wantedKey }?.first ?: tabs.first().first
        if (ZtwoSettings.newsTab.value != key) ZtwoSettings.edit(this.ctx) { set(ZtwoSettings.newsTab, key) }
        val activity = webActivity()
        if (key == TAB_TOP) {
            adapter.rssPage = false
            adapter.rssFilter = null
            refreshNewsTabs()
            adapter.notifyDataSetChanged()
            this.gridView?.scrollWhenLaidOut(0)
            val data = DataNews.instance(this.ctx)
            val stale = !hasNews(data.items) || System.currentTimeMillis() > data.loadedAt + 1800000
            loadNews(stale)
            this.quickListener?.addRssButton()
            activity?.updateRssButton(true)
        } else {
            adapter.rssPage = true
            adapter.rssFilter = if (key == TAB_ALL) null else key
            refreshNewsTabs()
            adapter.applyRss(RssFeeds.items, RssFeeds.stale() && RssFeeds.feeds().isNotEmpty())
            adapter.notifyDataSetChanged()
            this.gridView?.scrollWhenLaidOut(0)
            activity?.updateRssButton(false)
            if (RssFeeds.stale()) loadRss(false)
        }
    }

    private var rssJob: kotlinx.coroutines.Job? = null
    private val rssScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

    /** Reads the feeds in the background and shows the result if the RSS page is still open; [force] after a change. */
    fun loadRss(force: Boolean) {
        val adapter = this.itemAdapter ?: return
        if (!adapter.rssPage) return
        if (rssJob?.isActive == true) {
            if (!force) return
            rssJob?.cancel()
        }
        val context = this.ctx?.applicationContext
        if (RssFeeds.feeds().isEmpty()) {
            adapter.applyRss(emptyList(), false)
            return
        }
        adapter.applyRss(if (force) emptyList() else RssFeeds.items, true)
        rssJob = rssScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { RssFeeds.load(context) }
            val a = this@QuickView.itemAdapter ?: return@launch
            if (a.rssPage) {
                refreshNewsTabs()
                a.applyRss(RssFeeds.items, false)
            }
        }
    }

    /** Back to the start page (shortcuts at the bottom again). */
    fun closeNewsPage() {
        val adapter = this.itemAdapter ?: return
        if (!adapter.newsPage) return
        adapter.newsPage = false
        adapter.rssPage = false
        adapter.notifyDataSetChanged()
        webActivity()?.updateRssButton(false)
        // back to the shortcuts at the bottom (more than one row: the first rows scrolled up as when the page opens)
        val extra = if (this.quickDown) adapter.headerSize() - 1 else 0
        this.gridView?.scrollWhenLaidOut(0, if (extra > 0) extra else 0)
    }

    fun handleBack(): Boolean {
        if (this.gridView == null || getVisibility() != View.VISIBLE) {
            return false
        }
        if (this.itemAdapter?.newsPage == true && !isBusy()) {
            closeNewsPage()
            return true
        }
        if (!isBusy()) {
            this.backPressTime = 0L
            return exitEditMode()
        }
        if (this.backPressTime == 0L) {
            this.backPressTime = System.currentTimeMillis()
            return true
        }
        if (System.currentTimeMillis() - this.backPressTime <= 500) {
            return true
        }
        clearPending()
        return true
    }

    companion object {
        /** News page tab keys (a feed's tab is its address). */
        const val TAB_TOP = "top"
        const val TAB_ALL = "all"

        @JvmField
        val topics: Array<String?> = arrayOf<String?>(
            "HEADLINE",
            "WORLD",
            "NATION",
            "BUSINESS",
            "TECHNOLOGY",
            "ENTERTAINMENT",
            "SPORTS",
            "SCIENCE",
            "HEALTH"
        )

        /**
         * Google News locale parameters ("hl=..&gl=..&ceid=..") from `locales`: the chosen one
         * (ZtwoSettings.newsLang.value), else the device language + country (saved as the choice), else English/US.
         */
        @JvmStatic
        fun newsLocale(context: Context?, locales: Array<String>?): String? {
            if (locales == null) {
                return "en-US&gl=US&ceid=US:en"
            }
            var index = ZtwoSettings.newsLang.value
            if (index < 0 || index >= locales.size) {
                index = -1
                val locale: Locale? = MainApp.currentLocale()
                if (locale != null) {
                    val language = locale.getLanguage()
                    val country = locale.getCountry()
                    if (!TextUtils.isEmpty(language) && !TextUtils.isEmpty(country)) {
                        for (index2 in locales.indices) {
                            val entry = locales[index2]
                            if (entry.startsWith(language) && entry.startsWith(
                                    country,
                                    entry.lastIndexOf('=') + 1
                                )
                            ) {
                                index = index2
                                break
                            }
                        }
                    }
                }
                if (index == -1) {
                    index = 27
                }
                if (ZtwoSettings.newsLang.value != index) {
                    ZtwoSettings.edit(context) { set(ZtwoSettings.newsLang, index) }
                }
            }
            return if (index < locales.size) locales[index] else "en-US&gl=US&ceid=US:en"
        }

        @JvmStatic
        fun hasNews(list: MutableList<*>?): Boolean {
            if (list == null || list.isEmpty()) {
                return false
            }
            val it: MutableIterator<Any?> = list.iterator()
            while (it.hasNext()) {
                val quickItem = it.next() as QuickItem?
                if (quickItem != null && quickItem.itemKind.toInt() == 7) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun isCustomNewsUrl(): Boolean {
            if (TextUtils.isEmpty(ZtwoSettings.newsUrl.value)) {
                return false
            }
            return "https://news.google.com/rss" != ZtwoSettings.newsUrl.value
        }
    }
}
