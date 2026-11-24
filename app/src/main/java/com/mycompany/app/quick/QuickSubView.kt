package com.mycompany.app.quick

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.TtsSettings
import ch.opumlabs.opumbrowser.R
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.ItemTouchHelper
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookQuick.Companion.countInFolder
import com.mycompany.app.db.book.DbBookQuick.Companion.itemsInFolder
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.folderCount
import com.mycompany.app.db.book.DbBookQuick.Companion.orderOf
import com.mycompany.app.db.book.DbBookQuick.Companion.deletePath
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.quick.QuickAdapter.QuickHolder
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.quick.QuickAdapter.QuickListener
import com.mycompany.app.quick.QuickDragListener
import com.mycompany.app.ui.components.LoadCover
import com.mycompany.app.view.MyFadeListener
import kotlin.math.min

@Suppress("deprecation")
class QuickSubView(context: Context?) : FrameLayout(context!!) {
    @JvmField
    var columnCount: Int = 0
    @JvmField
    var cover: LoadCover? = null
    @JvmField
    var closing: Boolean = false
    @JvmField
    var ghostShown: Boolean = false
    @JvmField
    var dragStarted: Boolean = false
    @JvmField
    var dragView: View? = null
    @JvmField
    var dragIndex: Int = 0
    @JvmField
    var dragRect: Rect? = null
    @JvmField
    var ghostImage: BitmapDrawable? = null
    @JvmField
    var ghostVisible: Boolean = false
    @JvmField
    var touchFree: Boolean = false
    @JvmField
    var dropPos: Int = 0
    @JvmField
    var boundLeft: Int = 0
    @JvmField
    var boundTop: Int = 0
    @JvmField
    var boundRight: Int = 0
    @JvmField
    var boundBottom: Int = 0
    @JvmField
    var dragWidth: Int = 0
    @JvmField
    var dragHeight: Int = 0
    @JvmField
    var downX: Int = 0
    @JvmField
    var downY: Int = 0
    @JvmField
    var offsetX: Int = 0
    @JvmField
    var offsetY: Int = 0
    @JvmField
    var loadedItems: ArrayList<QuickAdapter.QuickItem?>? = null
    @JvmField
    var appContext: Context? = null
    @JvmField
    var subListener: QuickSubListener? = null
    @JvmField
    var originX: Int = 0
    @JvmField
    var originY: Int = 0
    @JvmField
    var cellSize: Int = 0
    @JvmField
    var rowCount: Int = 0
    @JvmField
    var folderPath: String? = null
    @JvmField
    var darkPage: Boolean = false
    @JvmField
    var headerBar: RelativeLayout? = null
    @JvmField
    var countText: AppCompatTextView? = null
    @JvmField
    var checkAll: com.mycompany.app.ui.components.CheckIcon? = null
    @JvmField
    var edgeView: View? = null
    @JvmField
    var cornerTop1: View? = null
    @JvmField
    var cornerTop2: View? = null
    @JvmField
    var cornerBottomLeft: View? = null
    @JvmField
    var cornerBottomRight: View? = null
    @JvmField
    var actionBar: LinearLayout? = null
    @JvmField
    var deleteButton: AppCompatTextView? = null
    @JvmField
    var editButton: AppCompatTextView? = null
    @JvmField
    var subGrid: QuickGridView? = null
    /** The dragged tile's picture (from the grid; drawn as the ghost that can leave the folder). */
    private var ghostBitmap: android.graphics.Bitmap? = null
    @JvmField
    var subAdapter: QuickAdapter? = null
    @JvmField
    var clickBusy: Boolean = false
    @JvmField
    var itemCount: Int = 0

    internal inner class CloseClick : OnClickListener {
        override fun onClick(view: View?) {
            this@QuickSubView.closeView()
        }
    }

    internal inner class RoundOutline : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (view == null || outline == null) {
                return
            }
            val width = view.getWidth()
            val height = view.getHeight()
            val dp8Px = MainApp.dp8
            outline.setRoundRect(0, 0, width, height + dp8Px, dp8Px.toFloat())
        }
    }

    internal inner class ShowLoadedItemsTask : Runnable {

        internal inner class ShowLoadedItemsStep : Runnable {
            override fun run() {
                val quickSubView = this@QuickSubView
                val arrayList = quickSubView.loadedItems
                quickSubView.loadedItems = null
                val quickAdapter: QuickAdapter? = if (quickSubView.subGrid == null) null else quickSubView.subAdapter
                if (quickSubView.subGrid == null || quickAdapter == null) {
                    return
                }
                quickAdapter.applyList(arrayList, false)
                quickSubView.subGrid!!.startFade(quickSubView.originX, quickSubView.originY, true, null)
            }
        }

        override fun run() {
            val quickSubView = this@QuickSubView
            quickSubView.loadedItems = itemsInFolder(quickSubView.appContext, quickSubView.folderPath)
            if (quickSubView.subGrid == null || quickSubView.subAdapter == null) {
                return
            }
            quickSubView.post(ShowLoadedItemsStep())
        }
    }

    internal inner class SubGridFade : MyFadeListener {
        override fun onFadeEnd(shown: Boolean) {
            val quickSubListener: QuickSubListener? = if (shown) null else this@QuickSubView.subListener
            if (shown || quickSubListener == null) {
                return
            }
            quickSubListener.close()
        }

        override fun onFadeStart(shown: Boolean, animate: Boolean) {
        }
    }

    internal inner class CoverClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class HideHeaderTask : Runnable {
        override fun run() {
            val quickSubView = this@QuickSubView
            val relativeLayout = quickSubView.headerBar
            if (relativeLayout == null) {
                return
            }
            relativeLayout.setVisibility(View.GONE)
            quickSubView.actionBar!!.setVisibility(View.GONE)
            quickSubView.edgeView!!.setVisibility(View.GONE)
            quickSubView.cornerTop1!!.setVisibility(View.GONE)
            quickSubView.cornerTop2!!.setVisibility(View.GONE)
            quickSubView.cornerBottomLeft!!.setVisibility(View.GONE)
            quickSubView.cornerBottomRight!!.setVisibility(View.GONE)
        }
    }

    internal inner class CloseDeleteDialogTask : Runnable {

        internal inner class CloseDeleteDialogStep(val outer: CloseDeleteDialogTask?,
                                             val deleted: Boolean
        ) : Runnable {
            override fun run() {
                val quickSubView = this@QuickSubView
                val subListener = quickSubView.subListener
                if (subListener != null) {
                    subListener.closeDeleteDialog()
                }
                if (quickSubView.subAdapter != null) {
                    if (this.deleted) {
                        quickSubView.closing = true
                        showToast(quickSubView.appContext, AppR.string.deleted)
                    }
                    val quickAdapter: QuickAdapter? = if (!quickSubView.leaveEditMode()) quickSubView.subAdapter else null
                    if (!quickSubView.leaveEditMode() && quickAdapter != null) {
                        quickAdapter.notifyDataSetChanged()
                    }
                    val subAdapter = quickSubView.subAdapter
                    val quickSubListener: QuickSubListener? = if (subAdapter == null || subAdapter.bookCount() != 0) null else quickSubView.subListener
                    if (subAdapter == null || subAdapter.bookCount() != 0 || quickSubListener == null) {
                        return
                    }
                    quickSubView.closing = true
                    quickSubListener.close()
                }
            }
        }

        override fun run() {
            val quickSubView = this@QuickSubView
            val quickAdapter = quickSubView.subAdapter
            if (quickAdapter != null) {
                val deleted = quickAdapter.deleteChecked(quickSubView.folderPath)
                if (quickSubView.subGrid == null) {
                    return
                }
                quickSubView.post(CloseDeleteDialogStep(this, deleted))
            }
        }
    }

    internal inner class BookCountTask : Runnable {

        internal inner class BookCountStep : Runnable {
            override fun run() {
                val quickSubView = this@QuickSubView
                val quickSubListener = quickSubView.subListener
                if (quickSubListener != null) {
                    quickSubView.closing = true
                    quickSubListener.close()
                }
            }
        }

        override fun run() {
            val quickSubView = this@QuickSubView
            val quickAdapter = quickSubView.subAdapter
            if (quickAdapter == null) {
                return
            }
            val folderPath = quickSubView.folderPath
            val quickItemD = quickAdapter.itemAt(quickSubView.dropPos)
            if (quickItemD != null && !TextUtils.isEmpty(quickItemD.path)) {
                val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), quickItemD.path)
                val folders = folderCount(quickAdapter.appContext)
                val contentValuesG = android.content.ContentValues().also { cv -> cv.put("_rsv1", "") }
                contentValuesG.put("_order", folders)
                val writableDatabase = get(quickAdapter.appContext).getWritableDatabase()
                updateWhere(
                    writableDatabase,
                    "DbBookQuick_table",
                    contentValuesG,
                    "_secret=? AND _path=?",
                    args
                )
                if (quickAdapter.bookCount() <= 2) {
                    quickAdapter.items!!.remove(quickItemD)
                    val itemAt = quickAdapter.itemAt(0)
                    if (itemAt == null || TextUtils.isEmpty(itemAt.path)) {
                        deletePath(quickAdapter.appContext, folderPath, false)
                    } else {
                        val order = orderOf(quickAdapter.appContext, folderPath)
                        if (order != -1) {
                            val secretArgs: Array<String?>? =
                                arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), itemAt.path)
                            val values = android.content.ContentValues().also { cv -> cv.put("_rsv1", "") }
                            values.put("_order", order)
                            updateWhere(
                                writableDatabase,
                                "DbBookQuick_table",
                                values,
                                "_secret=? AND _path=?",
                                secretArgs
                            )
                            deletePath(quickAdapter.appContext, folderPath, false)
                        }
                    }
                }
            }
            quickSubView.post(BookCountStep())
        }
    }

    internal inner class HeaderBarClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class SubGridClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class AllCheckedClick : OnClickListener {
        override fun onClick(view: View?) {
            val quickSubView = this@QuickSubView
            val quickAdapter = quickSubView.subAdapter
            if (quickAdapter == null) {
                return
            }
            quickAdapter.setAllChecked(!quickAdapter.allChecked(), true)
            quickSubView.updateCount(quickSubView.subAdapter!!.checkedCount(), quickSubView.subAdapter!!.bookCount(), true)
        }
    }

    internal inner class CheckedCountClick : OnClickListener {
        override fun onClick(view: View?) {
            val quickSubView = this@QuickSubView
            val quickAdapter = quickSubView.subAdapter
            val checkedCount = if (quickAdapter == null || quickSubView.subListener == null) 0 else quickAdapter.checkedCount()
            if (quickAdapter == null || quickSubView.subListener == null || checkedCount == 0) {
                return
            }
            val quickSubListener = quickSubView.subListener
            if (checkedCount == 1) {
                quickSubListener!!.showDeleteDialog(quickSubView.subAdapter!!.firstChecked(), checkedCount)
            } else {
                quickSubListener!!.showDeleteDialog(null, checkedCount)
            }
        }
    }

    internal inner class ShowEditDialogClick : OnClickListener {
        override fun onClick(view: View?) {
            val quickSubView = this@QuickSubView
            val quickAdapter = quickSubView.subAdapter
            val quickSubListener: QuickSubListener? = if (quickAdapter == null) null else quickSubView.subListener
            if (quickAdapter == null || quickSubListener == null) {
                return
            }
            quickSubListener.showEditDialog(quickAdapter.firstChecked())
        }
    }

    internal inner class NormalizeUrlStep : QuickListener {
        override fun isLandscape(): Boolean {
            val quickSubView = this@QuickSubView
            val quickSubListener = quickSubView.subListener
            return if (quickSubListener == null) isLandscape(quickSubView.appContext) else quickSubListener.isLandscape()
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
            val quickSubView = this@QuickSubView
            val quickAdapter: QuickAdapter? = if (quickSubView.clickBusy) null else quickSubView.subAdapter
            if (quickSubView.clickBusy || quickAdapter == null || quickSubView.subListener == null) {
                return
            }
            if (!quickAdapter.editMode) {
                val quickItemD = quickAdapter.itemAt(position)
                if (quickItemD != null) {
                    quickSubView.subListener!!.openUrl(MainUtil.normalizeUrl(null, quickItemD.path))
                    return
                }
                return
            }
            val quickItemD = quickAdapter.itemAt(position)
            if (quickItemD != null && quickItemD.itemKind.toInt() == 0) {
                quickItemD.isChecked = !quickItemD.isChecked
                quickAdapter.applyEditAlpha(position, true)
            }
            quickSubView.updateCount(quickSubView.subAdapter!!.checkedCount(), quickSubView.subAdapter!!.bookCount(), true)
        }

        override fun onEditMode(enabled: Boolean) {
        }

        override fun onItemLongClick(quickHolder: QuickHolder?, position: Int) {
            val quickSubView = this@QuickSubView
            val quickAdapter: QuickAdapter? = if (quickSubView.clickBusy) null else quickSubView.subAdapter
            if (quickSubView.clickBusy || quickAdapter == null || quickSubView.subListener == null) {
                return
            }
            if (!quickAdapter.editMode) {
                quickAdapter.applyEditMode(position, true)
                quickSubView.setEditView(true)
            }
            val grid: QuickGridView? = if (quickSubView.subAdapter!!.itemAt(position) == null) null else quickSubView.subGrid
            if (quickSubView.subAdapter!!.itemAt(position) == null || grid == null) {
                return
            }
            grid.startDrag(quickHolder)
        }
    }

    internal inner class SaveOrderStep : QuickDragListener {

        internal inner class SaveOrderUiStep : Runnable {
            override fun run() {
                val quickSubView = this@QuickSubView
                val quickAdapter = quickSubView.subAdapter
                if (quickAdapter != null && quickSubView.cover == null && quickAdapter.saveOrder(quickSubView.subGrid)) {
                    quickSubView.closing = true
                }
            }
        }

        override fun onDragState(state: Int) {
            val quickSubView = this@QuickSubView
            quickSubView.clickBusy = false
            if (state == 2) {
                quickSubView.clickBusy = true
            } else if (state == 0 && quickSubView.subAdapter != null && quickSubView.cover == null) {
                quickSubView.post(SaveOrderUiStep())
            }
        }

        override fun onMove(from: Int, to: Int): Boolean {
            val quickSubView = this@QuickSubView
            val quickAdapter = quickSubView.subAdapter
            if (quickAdapter == null || quickSubView.cover != null) {
                return false
            }
            return quickAdapter.moveItem(from, to)
        }

        override fun onMerge(from: Int, to: Int) {
        }
    }

    internal inner class PlaceSubGridTask : Runnable {
        override fun run() {
            val fits: Boolean
            val originX: Int
            var newOriginX: Int
            var boundTop: Int
            val quickSubView = this@QuickSubView
            if (quickSubView.subListener == null || quickSubView.subGrid == null) {
                return
            }
            val itemCount = quickSubView.itemCount
            val columnCount = quickSubView.columnCount
            var rows = itemCount / columnCount
            if (itemCount % columnCount != 0) {
                rows++
            }
            if (rows > 4) {
                rows = 4
            }
            var gridWidth = (min(quickSubView.cellSize, MainApp.dp16 * 5) * quickSubView.columnCount) + MainApp.dp16
            var originY = (quickSubView.rowCount * rows) + MainApp.dp8
            val width = quickSubView.getWidth()
            val height = quickSubView.getHeight() - quickSubView.getPaddingBottom()
            val dp48Px = MainApp.dp48
            var index = (height - dp48Px) - dp48Px
            if (index < quickSubView.rowCount + dp48Px) {
                fits = true
                index = height
            } else {
                fits = false
            }
            if (gridWidth > width) {
                gridWidth = width
            }
            if (originY > index) {
                originY = index
            }
            val rtl = isRtl(quickSubView.appContext)
            var originXNow = quickSubView.originX
            if (rtl) {
                if (TtsSettings.webLand2.value != 0) {
                    val quickSubListener = quickSubView.subListener
                    if (if (quickSubListener == null) isLandscape(quickSubView.appContext) else quickSubListener.isLandscape()) {
                        originX = TtsSettings.webLand2.value
                        originXNow += originX
                    }
                }
            } else if (TtsSettings.webLand.value != 0) {
                val quickSubListener = quickSubView.subListener
                if (if (quickSubListener == null) isLandscape(quickSubView.appContext) else quickSubListener.isLandscape()) {
                    originX = TtsSettings.webLand.value
                    originXNow += originX
                }
            }
            if (originXNow < 0) {
                quickSubView.originX = 0
                originXNow = 0
                newOriginX = 0
            } else {
                val maxOriginX = width - gridWidth
                if (originXNow > maxOriginX) {
                    val shiftedOriginX = quickSubView.originX - maxOriginX
                    quickSubView.originX = shiftedOriginX
                    newOriginX = shiftedOriginX
                    originXNow = maxOriginX
                } else {
                    quickSubView.originX = 0
                    newOriginX = 0
                }
            }
            if (rtl) {
                newOriginX = gridWidth - newOriginX
                quickSubView.originX = newOriginX
            }
            if (newOriginX < 0) {
                quickSubView.originX = 0
            } else if (newOriginX > gridWidth) {
                quickSubView.originX = gridWidth
            }
            if (fits) {
                boundTop = 0
            } else {
                boundTop = quickSubView.originY - originY
                val minTop = MainApp.dp48
                if (boundTop < minTop) {
                    boundTop = minTop
                } else {
                    val maxTop = (height - minTop) - originY
                    if (boundTop > maxTop) {
                        boundTop = maxTop
                    }
                }
            }
            val newOriginY = quickSubView.originY - boundTop
            quickSubView.originY = newOriginY
            if (newOriginY < 0) {
                quickSubView.originY = 0
            } else if (newOriginY > originY) {
                quickSubView.originY = originY
            }
            if (rtl) {
                val boundLeft = (width - originXNow) - gridWidth
                quickSubView.boundLeft = boundLeft
                quickSubView.boundTop = boundTop
                quickSubView.boundRight = boundLeft + gridWidth
                quickSubView.boundBottom = boundTop + originY
            } else {
                quickSubView.boundLeft = originXNow
                quickSubView.boundTop = boundTop
                quickSubView.boundRight = originXNow + gridWidth
                quickSubView.boundBottom = boundTop + originY
            }
            val gridParams = quickSubView.subGrid!!.getLayoutParams() as LayoutParams?
            if (gridParams != null) {
                if (rtl) {
                    gridParams.rightMargin = originXNow
                } else {
                    gridParams.leftMargin = originXNow
                }
                gridParams.topMargin = boundTop
                gridParams.width = gridWidth
                gridParams.height = originY
            }
            val relativeLayout = quickSubView.headerBar
            val layoutParams: LayoutParams? = if (relativeLayout != null) (relativeLayout.getLayoutParams() as LayoutParams?) else null
            if (relativeLayout != null && layoutParams != null) {
                if (rtl) {
                    layoutParams.rightMargin = originXNow
                } else {
                    layoutParams.leftMargin = originXNow
                }
                layoutParams.topMargin = boundTop - layoutParams.height
                layoutParams.width = gridWidth
                val edgeParams = quickSubView.edgeView!!.getLayoutParams() as LayoutParams?
                if (edgeParams != null) {
                    if (rtl) {
                        edgeParams.rightMargin = originXNow
                    } else {
                        edgeParams.leftMargin = originXNow
                    }
                    edgeParams.topMargin = boundTop
                    edgeParams.width = gridWidth
                    val cornerTop1Params = quickSubView.cornerTop1!!.getLayoutParams() as LayoutParams?
                    if (cornerTop1Params != null) {
                        if (rtl) {
                            cornerTop1Params.rightMargin = originXNow
                        } else {
                            cornerTop1Params.leftMargin = originXNow
                        }
                        cornerTop1Params.topMargin = boundTop
                        val cornerTop2Params = quickSubView.cornerTop2!!.getLayoutParams() as LayoutParams?
                        if (cornerTop2Params != null) {
                            val rightMargin = originXNow + gridWidth
                            if (rtl) {
                                cornerTop2Params.rightMargin = rightMargin - MainApp.dp16
                            } else {
                                cornerTop2Params.leftMargin = rightMargin - MainApp.dp16
                            }
                            cornerTop2Params.topMargin = boundTop
                        }
                    }
                }
            }
            if (quickSubView.subGrid == null || quickSubView.subAdapter == null) {
                return
            }
            MainApp.runInBackground(quickSubView.appContext, quickSubView.ShowLoadedItemsTask())
        }
    }

    interface QuickSubListener {
        fun isLandscape(): Boolean

        fun setBarsDisabled(disabled: Boolean)

        fun closeDeleteDialog()

        fun showEditDialog(quickItem: QuickItem?)

        fun showDeleteDialog(quickItem: QuickItem?, position: Int)

        fun openUrl(url: String?)

        fun close()
    }

    fun setEditView(disabled: Boolean) {
        val quickAdapter = this.subAdapter
        if (quickAdapter != null) {
            if (disabled) {
                updateCount(quickAdapter.checkedCount(), this.subAdapter!!.bookCount(), false)
                this.headerBar!!.setVisibility(View.VISIBLE)
                this.actionBar!!.setVisibility(View.VISIBLE)
                this.edgeView!!.setVisibility(View.VISIBLE)
                this.cornerTop1!!.setVisibility(View.VISIBLE)
                this.cornerTop2!!.setVisibility(View.VISIBLE)
                this.cornerBottomLeft!!.setVisibility(View.VISIBLE)
                this.cornerBottomRight!!.setVisibility(View.VISIBLE)
            } else {
                post(HideHeaderTask())
            }
            val quickSubListener = this.subListener
            if (quickSubListener != null) {
                quickSubListener.setBarsDisabled(disabled)
            }
        }
    }

    fun reloadItems(): Boolean {
        if (this.subAdapter == null) {
            return false
        }
        MainApp.runInBackground(this.appContext, CloseDeleteDialogTask())
        return true
    }

    fun closeView() {
        val quickAdapter: QuickAdapter? = if (this.subGrid != null) this.subAdapter else null
        if (this.subGrid != null && quickAdapter != null) {
            if (quickAdapter.editMode) {
                setEditView(false)
            }
            this.subGrid!!.startFade(this.originX, this.originY, false, SubGridFade())
        } else {
            val quickSubListener = this.subListener
            if (quickSubListener != null) {
                quickSubListener.close()
            }
        }
    }

    fun buildViews() {
        val spanCount: Int
        val context = this.appContext
        if (context == null) {
            return
        }
        val myRecyclerView = QuickGridView(context)
        val top = MainApp.dp4
        myRecyclerView.setPadding(0, top, 0, top)
        myRecyclerView.screenLayout = false
        myRecyclerView.mergeEnabled = false
        myRecyclerView.dragHooks = object : QuickGridView.DragHooks {
            override fun started(holder: QuickHolder, pos: Int) {
                // the old helper's start of a drag: remember the tile, the drag-out code takes it from here
                endDrag()
                ghostBitmap = null
                dragView = holder.itemView
                dragIndex = pos
                dragStarted = true
                MainUtil.hapticTap(this@QuickSubView)
            }

            override fun ghost(bitmap: android.graphics.Bitmap) {
                ghostBitmap = bitmap
            }

            override fun moved(pos: Int) = setDragPos(pos)

            override fun ended() {
                ghostVisible = false
                endDrag()
                invalidate()
            }
        }
        myRecyclerView.applyRound(true, true)
        myRecyclerView.setVisibility(View.INVISIBLE)
        addView(myRecyclerView, -2, -2)
        val relativeLayout = RelativeLayout(context)
        relativeLayout.setVisibility(View.GONE)
        addView(relativeLayout, -1, MainApp.dp48)
        val appCompatTextView = AppCompatTextView(context, null)
        val left = MainApp.dp8
        appCompatTextView.setPadding(left, 0, left, 0)
        appCompatTextView.setGravity(16)
        appCompatTextView.setTextDirection(3)
        appCompatTextView.setTextSize(1, 16.0f)
        val layoutParams = RelativeLayout.LayoutParams(-2, MainApp.dp48)
        layoutParams.addRule(21)
        layoutParams.setMarginEnd(dpToPx(context, 52.0f).toInt())
        relativeLayout.addView(appCompatTextView, layoutParams)
        val myButtonCheck = com.mycompany.app.ui.components.CheckIcon(context)
        val checkParams = RelativeLayout.LayoutParams(MainApp.dp48, MainApp.dp48)
        checkParams.addRule(21)
        relativeLayout.addView(myButtonCheck, checkParams)
        val view = View(context)
        view.setBackgroundResource(AppR.drawable.shadow_list_up)
        view.setVisibility(View.GONE)
        val edgeParams = LayoutParams(-1, MainApp.dp16)
        edgeParams.topMargin = MainApp.dp48
        addView(view, edgeParams)
        val cornerTop1 = View(context)
        cornerTop1.setVisibility(View.GONE)
        val dp16Px = MainApp.dp16
        val cornerTop1Params = LayoutParams(dp16Px, dp16Px)
        cornerTop1Params.topMargin = MainApp.dp48
        addView(cornerTop1, cornerTop1Params)
        val cornerTop2 = View(context)
        cornerTop2.setVisibility(View.GONE)
        val cornerSize = MainApp.dp16
        val cornerTop2Params = LayoutParams(cornerSize, cornerSize)
        cornerTop2Params.topMargin = MainApp.dp48
        addView(cornerTop2, cornerTop2Params)
        val cornerBottomLeft = View(context)
        cornerBottomLeft.setVisibility(View.GONE)
        val bottomCornerSize = MainApp.dp16
        val bottomLeftParams = LayoutParams(bottomCornerSize, bottomCornerSize)
        bottomLeftParams.bottomMargin = MainApp.dp48
        bottomLeftParams.gravity = 8388691
        addView(cornerBottomLeft, bottomLeftParams)
        val cornerBottomRight = View(context)
        cornerBottomRight.setVisibility(View.GONE)
        val rightCornerSize = MainApp.dp16
        val bottomRightParams = LayoutParams(rightCornerSize, rightCornerSize)
        bottomRightParams.bottomMargin = MainApp.dp48
        bottomRightParams.gravity = 8388693
        addView(cornerBottomRight, bottomRightParams)
        val linearLayout = LinearLayout(context)
        linearLayout.setBaselineAligned(false)
        linearLayout.setOrientation(LinearLayout.HORIZONTAL)
        linearLayout.setVisibility(View.GONE)
        val bottomBarParams = LayoutParams(-1, MainApp.dp48)
        bottomBarParams.gravity = 8388691
        addView(linearLayout, bottomBarParams)
        val myLineText = AppCompatTextView(context, null)
        myLineText.setGravity(17)
        myLineText.setTextSize(1, 16.0f)
        val layoutParamsF = com.mycompany.app.outline.LegacyViews.setTextLinearParams(myLineText, AppR.string.delete, 0, -1)
        layoutParamsF.weight = 1.0f
        linearLayout.addView(myLineText, layoutParamsF)
        // the thin divider the old MyLineText drew at its edge
        val divider = View(context)
        divider.setBackgroundColor(-9079435)
        linearLayout.addView(divider, LinearLayout.LayoutParams(1, -1))
        val editButton = AppCompatTextView(context, null)
        editButton.setGravity(17)
        editButton.setTextSize(1, 16.0f)
        val layoutParamsE = com.mycompany.app.outline.LegacyViews.setTextLinearParams(editButton, AppR.string.edit, 0, -1)
        layoutParamsE.weight = 1.0f
        linearLayout.addView(editButton, layoutParamsE)
        this.headerBar = relativeLayout
        this.countText = appCompatTextView
        this.checkAll = myButtonCheck
        this.edgeView = view
        this.cornerTop1 = cornerTop1
        this.cornerTop2 = cornerTop2
        this.cornerBottomLeft = cornerBottomLeft
        this.cornerBottomRight = cornerBottomRight
        this.actionBar = linearLayout
        this.deleteButton = myLineText
        this.editButton = editButton
        this.subGrid = myRecyclerView
        setColor(false)
        val headerBar = this.headerBar
        if (headerBar != null) {
            headerBar.setOutlineProvider(RoundOutline())
            this.headerBar!!.setClipToOutline(true)
        }
        this.headerBar!!.setElevation(MainApp.dp16.toFloat())
        this.edgeView!!.setElevation(MainApp.dp16.toFloat())
        this.cornerTop1!!.setElevation(MainApp.dp16.toFloat())
        this.cornerTop2!!.setElevation(MainApp.dp16.toFloat())
        this.subGrid!!.setRoundSize(MainApp.dp8)
        this.subGrid!!.setElevation(MainApp.dp16.toFloat())
        setOnClickListener(CloseClick())
        this.headerBar!!.setOnClickListener(HeaderBarClick())
        this.subGrid!!.setOnClickListener(SubGridClick())
        this.checkAll!!.setOnClickListener(AllCheckedClick())
        this.deleteButton!!.setOnClickListener(CheckedCountClick())
        this.editButton!!.setOnClickListener(ShowEditDialogClick())
        val itemCount = countInFolder(this.appContext, this.folderPath)
        this.itemCount = itemCount
        if (itemCount == 0) {
            this.columnCount = 1
            spanCount = 1
        } else if (itemCount < 4) {
            this.columnCount = itemCount
            spanCount = itemCount
        } else {
            this.columnCount = 4
            spanCount = 4
        }
        val grid = this.subGrid!!
        grid.spanCount = spanCount
        this.subAdapter = QuickAdapter(this.appContext, 1, this.darkPage, NormalizeUrlStep())
        val dragListener = SaveOrderStep()
        grid.setDragListener(dragListener)
        grid.setAdapter(this.subAdapter)
    }

    public override fun dispatchDraw(canvas: Canvas) {
        if (this.ghostShown) {
            super.dispatchDraw(canvas)
            val bitmapDrawable = this.ghostImage
            if (bitmapDrawable != null) {
                bitmapDrawable.draw(canvas)
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val myRecyclerView: QuickGridView?
        if (!this.ghostShown) {
            return false
        }
        this.downX = event!!.getX().toInt()
        this.downY = event.getY().toInt()
        val actionMasked = event.getActionMasked()
        if (actionMasked == 1) {
            if (this.touchFree) {
                this.touchFree = false
                this.dropPos = this.dragIndex
                myRecyclerView = this.subGrid
                if (myRecyclerView != null && this.subAdapter != null) {
                    myRecyclerView.cancelFade()
                    if (this.cover == null) {
                        val myCoverView = LoadCover(
                            this.appContext,
                            -328966,
                            Math.round(MainApp.dp16 / 8.0f),
                            MainApp.dp16 shl 1
                        )
                        this.cover = myCoverView
                        myCoverView.setVisibility(View.GONE)
                        this.cover!!.setBackColor(-1593835520)
                        this.cover!!.setOnClickListener(CoverClick())
                        addView(this.cover, -1, -1)
                        this.cover!!.showCover(true, 1.0f, 200L)
                    }
                    MainApp.runInBackground(this.appContext, BookCountTask())
                }
            }
        } else if (actionMasked == 2) {
            val dragStarted = this.dragStarted
            val view: View? = if (dragStarted) this.dragView else null
            // the ghost starts once the grid handed over the tile's picture (the grid hides the tile itself)
            val ghost = ghostBitmap
            if (dragStarted && view != null && ghost != null) {
                this.dragStarted = false
                this.dragWidth = view.getWidth()
                val height = view.getHeight()
                this.dragHeight = height
                this.ghostImage = BitmapDrawable(getResources(), ghost)
                val left = view.getLeft() + this.boundLeft
                val top = view.getTop() + this.boundTop
                this.offsetX = left - this.downX
                this.offsetY = top - this.downY
                val rect = Rect(left, top, this.dragWidth + left, this.dragHeight + top)
                this.dragRect = rect
                this.ghostImage!!.setBounds(rect)
                invalidate()
                this.ghostVisible = true
            }
            val rect: Rect? = if (this.ghostVisible) this.dragRect else null
            if (this.ghostVisible && rect != null) {
                val leftValue = this.downX + this.offsetX
                val topValue = this.downY + this.offsetY
                val index = this.dragWidth + leftValue
                val bottomValue = this.dragHeight + topValue
                rect.set(leftValue, topValue, index, bottomValue)
                this.ghostImage!!.setBounds(this.dragRect!!)
                invalidate()
                if (!this.touchFree && (index < this.boundLeft || leftValue > this.boundRight || bottomValue < this.boundTop || topValue > this.boundBottom)) {
                    this.touchFree = true
                    if (this.subGrid != null) {
                        setEditView(false)
                        this.subGrid!!.startFade(this.originX, this.originY, false, null)
                    }
                }
            }
        } else if (actionMasked == 3) {
            if (this.touchFree) {
                this.touchFree = false
                this.dropPos = this.dragIndex
                myRecyclerView = this.subGrid
                if (myRecyclerView != null) {
                    myRecyclerView.cancelFade()
                    if (this.cover == null) {
                        val myCoverView = LoadCover(
                            this.appContext,
                            -328966,
                            Math.round(MainApp.dp16 / 8.0f),
                            MainApp.dp16 shl 1
                        )
                        this.cover = myCoverView
                        myCoverView.setVisibility(View.GONE)
                        this.cover!!.setBackColor(-1593835520)
                        this.cover!!.setOnClickListener(CoverClick())
                        addView(this.cover, -1, -1)
                        this.cover!!.showCover(true, 1.0f, 200L)
                    }
                    MainApp.runInBackground(this.appContext, BookCountTask())
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun endDrag() {
        val view = this.dragView
        if (view != null) {
            view.setVisibility(View.VISIBLE)
            this.dragView = null
        }
        this.dragStarted = false
        this.dragRect = null
        this.ghostImage = null
    }

    fun updateCount(done: Int, on: Int, animated: Boolean) {
        val textColor: Int
        val deleteColor: Int
        if (this.countText == null) {
            return
        }
        val dark = MainUtil.isDarkPage(this.darkPage)
        val myButtonCheck = this.checkAll!!
        if (dark) {
            myButtonCheck.applyIcons(
                AppR.drawable.baseline_check_circle_dark_24,
                AppR.drawable.outline_radio_button_unchecked_dark_24
            )
            textColor = -328966
            deleteColor = -8355712
        } else {
            myButtonCheck.applyIcons(
                AppR.drawable.baseline_check_circle_black_24,
                AppR.drawable.outline_radio_button_unchecked_black_24
            )
            textColor = -14784824
            deleteColor = -2434342
        }
        this.countText!!.setText(MainUtil.countLabel(done, on))
        this.checkAll!!.applyChecked(done >= on, animated)
        val myLineText = this.deleteButton!!
        if (done > 0) {
            myLineText.setEnabled(true)
            this.deleteButton!!.setTextColor(textColor)
        } else {
            myLineText.setEnabled(false)
            this.deleteButton!!.setTextColor(deleteColor)
        }
        val appCompatTextView = this.editButton!!
        if (done == 1) {
            appCompatTextView.setEnabled(true)
            this.editButton!!.setTextColor(textColor)
        } else {
            appCompatTextView.setEnabled(false)
            this.editButton!!.setTextColor(deleteColor)
        }
    }

    fun postRefresh() {
        if (this.subGrid == null) {
            return
        }
        post(PlaceSubGridTask())
    }

    fun leaveEditMode(): Boolean {
        val quickAdapter = this.subAdapter
        if (quickAdapter == null || !quickAdapter.editMode) {
            return false
        }
        quickAdapter.applyEditMode(-1, false)
        setEditView(false)
        return true
    }

    fun setColor(night: Boolean) {
        if (this.subGrid != null) {
            val dark = MainUtil.isDarkPage(this.darkPage)
            val relativeLayout = this.headerBar!!
            if (dark) {
                relativeLayout.setBackgroundColor(-16777216)
                this.actionBar!!.setBackgroundColor(-16777216)
                this.subGrid!!.setBackgroundColor(-14606047)
                this.countText!!.setTextColor(-328966)
                this.cornerTop1!!.setBackgroundResource(AppR.drawable.round_top_left_b)
                this.cornerTop2!!.setBackgroundResource(AppR.drawable.round_top_right_b)
                this.cornerBottomLeft!!.setBackgroundResource(AppR.drawable.round_bot_left_b)
                this.cornerBottomRight!!.setBackgroundResource(AppR.drawable.round_bot_right_b)
                this.deleteButton!!.setBackgroundResource(AppR.drawable.selector_normal_dark)
                this.deleteButton!!.setTextColor(-328966)
                this.editButton!!.setBackgroundResource(AppR.drawable.selector_normal_dark)
                this.editButton!!.setTextColor(-328966)
            } else {
                relativeLayout.setBackgroundColor(-460552)
                this.actionBar!!.setBackgroundColor(-460552)
                this.subGrid!!.setBackgroundColor(-1)
                this.countText!!.setTextColor(-16777216)
                this.cornerTop1!!.setBackgroundResource(AppR.drawable.round_top_left_g)
                this.cornerTop2!!.setBackgroundResource(AppR.drawable.round_top_right_g)
                this.cornerBottomLeft!!.setBackgroundResource(AppR.drawable.round_bot_left_g)
                this.cornerBottomRight!!.setBackgroundResource(AppR.drawable.round_bot_right_g)
                this.deleteButton!!.setBackgroundResource(AppR.drawable.selector_normal)
                this.deleteButton!!.setTextColor(-14784824)
                this.editButton!!.setBackgroundResource(AppR.drawable.selector_normal)
                this.editButton!!.setTextColor(-14784824)
            }
            val quickAdapter: QuickAdapter? = if (!night) null else this.subAdapter
            if (!night || quickAdapter == null) {
                return
            }
            quickAdapter.notifyDataSetChanged()
        }
    }

    fun setDragPos(dragIndex: Int) {
        this.dragIndex = dragIndex
    }

    companion object {
    }
}
