package com.mycompany.app.web

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.quick.TabDragListener
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.ui.browser.ComposeTabBar
import com.mycompany.app.view.MyFadeListener
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WvaTabBarSubListener
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The popup under a tab group in the tab bar: the group's tabs in a row, in Compose. It covers the page (a tap outside
 * closes it) and holds a [ComposeTabBar] bound to its own [WebTabBar] model of the group. Tap a tab: switch to it and
 * close the popup; the close button or a vertical flick closes the tab (kept for undo); a long press drags the tab:
 * moving it in the row reorders the group, and dragging it out of the popup (the popup fades away and a floating copy
 * follows the finger) and letting go there takes it out of the group, next to it in the tab bar.
 * The fields the activity reads keep their old names; [popupFrame] is the popup frame (open / fade state).
 */
@Suppress("deprecation")
class WebTabBarSubView(context: Context?) : ComposeOverlay(context!!) {
    @JvmField var dragMoved: Boolean = false
    @JvmField var dragFrom: Int = 0
    @JvmField var dragTo: Int = 0
    @JvmField var swipeArmed: Boolean = false
    @JvmField var dropPos: Int = 0
    @JvmField var hostContext: Context? = null
    @JvmField var subListener: TabBarSubListener? = null
    @JvmField var anchorPos: Int = 0
    @JvmField var frameSize: Int = 0
    @JvmField var onStartPage: Boolean = false
    @JvmField var tintColor: Int = 0
    @JvmField var tintColor2: Int = 0
    @JvmField var allTabs: ArrayList<WebTabItem?>? = null
    @JvmField var selectedTab: Int = 0
    @JvmField var groupStart: Int = 0
    @JvmField var groupOffset: Int = 0
    /** The popup frame: [PopupFrame.isClosed] is true while it is hidden. */
    @JvmField var popupFrame: PopupFrame? = null
    @JvmField var tabBar: ComposeTabBar? = null
    @JvmField var tabAdapter: WebTabBar? = null
    @JvmField var closeBusy: Boolean = false
    @JvmField var groupTabs: MutableList<WebTabItem?>? = null
    @JvmField var groupSelected: Int = 0
    @JvmField var closedPos: Int = 0
    @JvmField var closedItem: WebTabItem? = null
    @JvmField var dropLeft: Int = 0
    @JvmField var dropTop: Int = 0
    @JvmField var dropRight: Int = 0
    @JvmField var dropBottom: Int = 0

    private var shown by mutableStateOf(false)
    private var outside by mutableStateOf(false)
    private var plusAtEnd = false
    private var showPlus = false
    private var frameY = 0
    private var frameHeight = 0
    private var background = 0
    private var border = 0
    private var barOffsetStart = 0
    private var barOffsetEnd = 0
    private var chipTitle by mutableStateOf<String?>(null)
    private var chipX by mutableFloatStateOf(0f)
    private var chipY by mutableFloatStateOf(0f)
    private var dropOutside = false
    private val handler = Handler(Looper.getMainLooper())

    /** Open / fade state of the popup, as the old `MyTabFrame` had it: [j] shows or hides it, then calls the listener. */
    inner class PopupFrame {
        @JvmField var isClosed: Boolean = true

        fun setOpen(anchorPos: Int, frameSize: Int, newShown: Boolean, myFadeListener: MyFadeListener?) {
            isClosed = !newShown
            shown = newShown
            handler.postDelayed({ myFadeListener?.onFadeEnd(newShown) }, 160)
        }

        fun refresh() {}
    }

    internal inner class SetOpenTask : Runnable {
        override fun run() {
            val frame = this@WebTabBarSubView.popupFrame
            if (frame != null) {
                frame.setOpen(this@WebTabBarSubView.anchorPos, this@WebTabBarSubView.frameSize, true, null)
                val groupSelected = this@WebTabBarSubView.groupSelected
                if (groupSelected > 1) {
                    this@WebTabBarSubView.tabBar?.scrollTo(groupSelected)
                }
            }
        }
    }

    internal inner class RemoveTabBarSubViewStep : MyFadeListener {
        override fun onFadeEnd(shown: Boolean) {
            if (!shown && this@WebTabBarSubView.subListener != null) {
                this@WebTabBarSubView.subListener!!.activity().removeTabBarSubView()
            }
        }

        override fun onFadeStart(shown: Boolean, animate: Boolean) {}
    }

    internal inner class TabListChange : WebTabBar.ChangeListener {
        override fun onChanged() {
            val bar = this@WebTabBarSubView.tabBar ?: return
            bar.postDelayed({ this@WebTabBarSubView.closeBusy = false }, 300L)
        }
    }

    internal inner class ScrollToGroupTask : Runnable {
        override fun run() {
            this@WebTabBarSubView.tabBar?.smoothScrollTo(this@WebTabBarSubView.groupSelected)
        }
    }

    /** Implemented by WvaTabBarSubListener; gives access to the owning activity.  */
    interface TabBarSubListener {
        fun activity(): WebViewActivity
    }

    /** The group's tabs, as copies indexed from 0; also finds the selected one ([groupSelected]). */
    private var subRealList: MutableList<WebTabItem?>?
        get() {
            val arrayList = this.allTabs
            var arrayList2: ArrayList<WebTabItem?>? = null
            val i = if (arrayList != null) this.groupStart else 0
            val webTabItem: WebTabItem? = if (arrayList != null && i >= 0 && i < arrayList.size) (this.allTabs!!.get(this.groupStart)) else null
            if (arrayList != null && i >= 0 && i < arrayList.size && webTabItem != null) {
                val j = webTabItem.groupId
                if (j != 0L) {
                    val size = this.allTabs!!.size
                    for (i2 in this.groupStart..<size) {
                        val tab = this.allTabs!!.get(i2)
                        if (tab != null) {
                            if (tab.groupId != j) {
                                break
                            }
                            val copy = WebTabItem()
                            copy.uid = tab.uid
                            val i3 = i2 - this.groupStart
                            copy.position = i3
                            copy.tabUrl = tab.tabUrl
                            copy.tabTitle = tab.tabTitle
                            if (this.selectedTab == tab.position) {
                                this.groupSelected = i3
                            }
                            if (arrayList2 == null) {
                                arrayList2 = ArrayList()
                            }
                            arrayList2.add(copy)
                        }
                    }
                }
            }
            return arrayList2 as MutableList<WebTabItem?>?
        }
        set(list) {
            if (list == null || list.isEmpty()) {
                return
            }
            val arrayList: ArrayList<WebTabItem?> = ArrayList()
            var i = 0
            var i2 = -1
            for (webTabItem in list) {
                if (webTabItem != null) {
                    val i3 = webTabItem.position
                    val groupTabs = this.groupTabs
                    val webTabItem2 =
                        if (groupTabs == null || i3 < 0 || i3 >= groupTabs.size) null else this.groupTabs!!.get(i3)
                    if (webTabItem2 != null) {
                        if (this.groupSelected == webTabItem2.position) {
                            i2 = i
                        }
                        webTabItem2.position = i
                        arrayList.add(webTabItem2)
                        i++
                    }
                }
            }
            this.groupTabs = arrayList
            this.groupSelected = i2
            this.tabAdapter!!.applyTint(arrayList, i2, this.onStartPage, this.tintColor, this.tintColor2, true)
        }

    /** Writes the group's new child order (offset by this popup's position) back into the tab bar.  */
    fun setDrawList(list: MutableList<WebTabItem?>?) {
        if (list == null || list.isEmpty()) {
            return
        }
        var copy: ArrayList<WebTabItem?>? = null
        for (item in list) {
            if (item == null) {
                continue
            }
            val moved = WebTabItem()
            moved.position = item.position + this.groupStart
            moved.groupPos = item.groupPos + this.groupOffset
            if (copy == null) {
                copy = ArrayList()
            }
            copy.add(moved)
        }
        if (this.subListener == null) {
            return
        }
        val adapter = this.subListener!!.activity().tabBar
        if (adapter != null) {
            val group: WebTabItem? = if (copy != null) adapter.itemAt(this.groupOffset) else null
            if (copy != null && group != null && group.groupTabs != null) {
                group.groupTabs = copy
            }
            adapter.rebuild(true)
        }
    }

    /** Closes the tab at [i] in the group (kept in [closedItem] for undo when tab restore is on). */
    fun closeTab(closedPos: Int) {
        val list: MutableList<WebTabItem?>? = if (this.closeBusy || this.subListener == null || this.tabAdapter == null) null else this.groupTabs
        if (this.closeBusy || this.subListener == null || this.tabAdapter == null || list == null) {
            return
        }
        this.closeBusy = true
        try {
            if (BrowserSettings.tabRestore.value) {
                this.closedPos = closedPos
                this.closedItem = list.get(closedPos)
            }
            this.groupTabs!!.removeAt(closedPos)
            val groupTabs = this.groupTabs
            val last = groupTabs == null || groupTabs.size < 2
            if (last) {
                (this.subListener as WvaTabBarSubListener).onTabChecked(this.groupStart + closedPos, last)
                return
            }
            val groupSelected = this.groupSelected
            if (groupSelected >= 0 && closedPos <= groupSelected) {
                this.groupSelected = groupSelected - 1
            }
            renumberTabs()
            (this.subListener as WvaTabBarSubListener).onTabChecked(this.groupStart + closedPos, last)
            this.tabAdapter!!.updateList(this.groupTabs, this.groupSelected, closedPos, false, TabListChange())
        } catch (unused: Exception) {
            this.closeBusy = false
        }
    }

    /** Frame position, background and border for the tab bar position preference. */
    private fun layoutFrame(body: MyWebBody) {
        val frame = PdfSettings.tabHeight.value + MainApp.dp16
        var top = 0
        var color = 0
        var stroke = 0
        when (BrowserSettings.tabBar.value) {
            1 -> {
                top = PdfSettings.tabHeight.value + MainApp.dp4
                val r0 = MainUtil.barTint(this.tintColor, this.onStartPage)
                color = if (r0 == 0) -1 else if (r0 == 1) -16777216 else r0
                stroke = if (r0 == 0) -16777216 else -5197648
                this.frameSize = 0
            }
            2 -> {
                top = PdfSettings.tabHeight.value + MainApp.dp4 + PdfSettings.topHeight.value
                val r0 = MainUtil.barTint(this.tintColor, this.onStartPage)
                color = if (r0 == 0) -1 else if (r0 == 1) -16777216 else r0
                stroke = if (r0 == 0) -16777216 else -5197648
                this.frameSize = 0
            }
            3, 4 -> {
                val extra = if (BrowserSettings.tabBar.value == 3) PdfSettings.tabHeight.value + PdfSettings.botHeight.value else PdfSettings.tabHeight.value
                top = (((body.rectBottom - body.rectTop) - frame) - max(MainApp.dp48, extra)) - MainApp.dp4
                val r0 = MainUtil.barTint(0, this.onStartPage)
                color = if (r0 == 0) PrefEditor.withAlpha(-1, 0) else if (r0 == 1) PrefEditor.withAlpha(-16777216, 0) else PrefEditor.withAlpha(-16777216, 30)
                stroke = if (r0 == 0) -16777216 else -5197648
                this.frameSize = frame
            }
        }
        frameY = top
        frameHeight = frame
        background = color
        border = stroke
        val side = PdfSettings.tabAdd.value
        var left = 0
        var right = 0
        if (side == 3) right = MainApp.dp48 else if (side != 0) left = MainApp.dp48
        barOffsetStart = left
        barOffsetEnd = right
        val rectTop = top + MainApp.dp8
        this.dropLeft = left
        this.dropTop = rectTop
        this.dropRight = left + (body.rectEnd - right)
        this.dropBottom = rectTop + PdfSettings.tabHeight.value
    }

    /** Builds the popup for the group at bar position [groupOffset]: [i] centre x of the tapped tab, [arrayList] all tabs, [i2] the current one, [i3] the first tab of the group. */
    fun open(
        myWebBody: MyWebBody?,
        anchorPos: Int,
        arrayList: ArrayList<WebTabItem?>?,
        selectedTab: Int,
        groupStart: Int,
        groupOffset: Int,
        tintFlag: Boolean,
        tintColor: Int,
        newTintColor: Int,
        tabBarSubListener: TabBarSubListener?
    ) {
        if (myWebBody == null) {
            return
        }
        this.subListener = tabBarSubListener
        this.allTabs = arrayList
        this.selectedTab = selectedTab
        this.groupStart = groupStart
        this.groupOffset = groupOffset
        this.onStartPage = tintFlag
        this.tintColor = tintColor
        this.tintColor2 = newTintColor
        this.groupSelected = -1
        this.groupTabs = this.subRealList
        this.anchorPos = anchorPos
        layoutFrame(myWebBody)
        this.tabAdapter = WebTabBar(this.hostContext, this.groupTabs, this.groupSelected, tintFlag, tintColor, newTintColor, ApplyListStep())
        val bar = ComposeTabBar(this.hostContext)
        this.tabBar = bar
        bar.allowMerge = false
        bar.swipeAny = true
        bar.adapter = this.tabAdapter
        this.tabAdapter!!.composeBar = bar
        bar.dragListener = RedrawAllStep()
        bar.dragReport = { phase, x, y, pos -> onDragReport(phase, x, y, pos) }
        bar.dragDrop = { pos -> onDragDrop(pos) }
        showPlus = PdfSettings.tabAdd.value != 0
        plusAtEnd = PdfSettings.tabAdd.value == 3
        this.popupFrame = PopupFrame()
        setPage({ AppState.darkUi }) { Popup() }
    }

    @androidx.compose.runtime.Composable
    private fun Popup() {
        val bar = tabBar ?: return
        val density = LocalDensity.current
        val corner = with(density) { MainApp.dp16.toDp() }
        val plus = with(density) { MainApp.dp48.toDp() }
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { dismissAnimated() } },
        ) {
            // stays composed while faded out: the dragged tab's gesture lives in it
            val alpha by androidx.compose.animation.core.animateFloatAsState(if (shown && !outside) 1f else 0f, label = "popup")
            run {
                Surface(
                    modifier = Modifier
                        .offset { IntOffset(0, frameY) }
                        .graphicsLayer { this.alpha = alpha }
                        .fillMaxWidth().height(with(density) { frameHeight.toDp() })
                        .pointerInput(Unit) { detectTapGestures { } },
                    shape = RoundedCornerShape(corner), color = Color(background), shadowElevation = with(density) { MainApp.dp16.toDp() },
                    border = if (border != 0) BorderStroke(with(density) { MainApp.dp1.toDp() }, Color(border)) else null,
                ) {
                    Box(Modifier.padding(vertical = with(density) { MainApp.dp8.toDp() })) {
                        Box(
                            Modifier.fillMaxSize().padding(
                                start = with(density) { barOffsetStart.toDp() }, end = with(density) { barOffsetEnd.toDp() },
                            ),
                        ) { AndroidView(factory = { bar }, modifier = Modifier.fillMaxSize()) }
                        if (showPlus) {
                            val r0 = MainUtil.barTint(if (BrowserSettings.tabBar.value != 1 || BrowserSettings.tabBar.value == 2) tintColor else 0, onStartPage)
                            Box(
                                Modifier.align(if (plusAtEnd) Alignment.CenterEnd else Alignment.CenterStart).width(plus).fillMaxSize()
                                    .pointerInput(Unit) { detectTapGestures { onPlus() } },
                                contentAlignment = Alignment.Center,
                            ) {
                                androidx.compose.foundation.Image(
                                    painterResource(if (r0 == 0) ch.opumlabs.opumbrowser.R.drawable.outline_add_black_24 else ch.opumlabs.opumbrowser.R.drawable.outline_add_dark_24),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
            val title = chipTitle
            if (title != null) {
                Surface(
                    modifier = Modifier.offset { IntOffset(chipX.roundToInt(), chipY.roundToInt()) }
                        .width(with(density) { PdfSettings.tabWidth.value.toDp() }).height(with(density) { PdfSettings.tabHeight.value.toDp() }),
                    shape = RoundedCornerShape(corner), color = Color(background), shadowElevation = 8.dp,
                    border = BorderStroke(with(density) { MainApp.dp1.toDp() }, Color(if (border != 0) border else -5197648)),
                ) {
                    Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Text(title, maxLines = 1, color = Color(if (MainUtil.barTint(tintColor, onStartPage) == 0) -16777216 else -1))
                    }
                }
            }
        }
    }

    private fun onPlus() {
        val listener = this.subListener ?: return
        val groupSelected = this.groupSelected
        if (groupSelected >= 0) {
            (listener as WvaTabBarSubListener).onTabPicked(this.groupStart + groupSelected, false)
            return
        }
        val list = this.groupTabs
        if (list == null || list.isEmpty()) {
            return
        }
        (listener as WvaTabBarSubListener).onTabPicked((list.size + this.groupStart) - 1, true)
    }

    /** The tab being dragged: while it is out of the popup a floating copy follows the finger and the popup fades away. */
    private fun onDragReport(phase: Int, rawX: Float, rawY: Float, pos: Int) {
        val bar = this.tabBar ?: return
        when (phase) {
            0 -> {
                val real = tabAdapter?.shownAt(tabAdapter?.itemAt(pos)?.position ?: -1)
                chipTitle = null
                dropOutside = false
                this.subListener?.activity()?.closeFloatingPopupMenu()
                if (real != null) chipTitle = WebViewActivity.tabTitleFor(this.hostContext, real) ?: ""
                chipTitle = null
            }
            1 -> {
                val where = IntArray(2)
                val mine = IntArray(2)
                bar.getLocationOnScreen(where)
                this.getLocationOnScreen(mine)
                val px = rawX + where[0] - mine[0]
                val py = rawY + where[1] - mine[1]
                val tabWidth = PdfSettings.tabWidth.value.toFloat()
                val tabHeight = PdfSettings.tabHeight.value.toFloat()
                val left = px - tabWidth / 2f
                val top = py - tabHeight / 2f
                val out = left + tabWidth < this.dropLeft || left > this.dropRight || top + tabHeight < this.dropTop || top > this.dropBottom
                if (out != outside) {
                    outside = out
                    bar.hideDragged = out
                }
                if (out) {
                    if (chipTitle == null) {
                        val real = tabAdapter?.shownAt(tabAdapter?.itemAt(pos)?.position ?: -1)
                        chipTitle = (if (real != null) WebViewActivity.tabTitleFor(this.hostContext, real) else null) ?: ""
                    }
                    chipX = left
                    chipY = top
                } else {
                    chipTitle = null
                }
            }
            2 -> {
                dropOutside = outside
                chipTitle = null
                bar.hideDragged = false
                if (!dropOutside) outside = false
            }
        }
    }

    /** A drag ended: outside the popup the tab leaves the group; true = handled. */
    private fun onDragDrop(pos: Int): Boolean {
        if (!dropOutside) return false
        dropOutside = false
        this.dropPos = pos
        pullOut()
        return true
    }

    /** A tab was dragged out of the group: move it next to the group in the tab bar.  */
    private fun pullOut() {
        val listener = this.subListener ?: return
        val activity = listener.activity()
        val groupPos = this.groupOffset
        val childPos = this.dropPos
        val adapter = activity.tabBar
        if (adapter == null) {
            return
        }
        val bar = adapter.tabItems
        val group: WebTabItem? = if (bar != null && groupPos >= 0 && groupPos < bar.size) adapter.itemAt(groupPos) else null
        val members = group?.groupTabs
        val child: WebTabItem? = if (members != null && !members.isEmpty() && childPos >= 0 && childPos < members.size) (members.get(childPos)) else null
        val real: WebTabItem? = if (child != null) adapter.shownAt(child.position) else null
        if (bar != null && group != null && members != null && child != null && real != null) {
            members.removeAt(childPos)
            ungroup(real, child)
            adapter.tabItems!!.add(groupPos + 1, child)
            if (members.isEmpty()) {
                adapter.tabItems!!.remove(group)
            } else if (members.size == 1) {
                // A group of one is dissolved into a plain tab.
                adapter.tabItems!!.remove(group)
                val last = members.get(0)
                val lastReal: WebTabItem? = if (last != null) adapter.shownAt(last.position) else null
                if (last != null && lastReal != null) {
                    members.removeAt(0)
                    ungroup(lastReal, last)
                    adapter.tabItems!!.add(groupPos, last)
                }
            }
            adapter.notifyChanged(groupPos)
            adapter.notifyInserted(groupPos + 1)
            adapter.rebuild(true)
        }
        if (activity.tabBarSubView != null) {
            activity.tabBarSubView!!.post(CloseSubViewTask(activity))
        }
    }

    private class CloseSubViewTask(val activity: WebViewActivity) : Runnable {
        override fun run() {
            activity.closeTabBarSubView()
        }
    }

    private fun ungroup(real: WebTabItem?, barItem: WebTabItem?) {
        real!!.groupId = 0L
        real.groupName = null
        real.groupColor = 0
        barItem!!.inGroup = true
    }

    /** Fades the popup away and then asks the activity to remove it. */
    fun dismissAnimated() {
        val frame = this.popupFrame
        if (frame == null) {
            release()
        } else {
            frame.setOpen(this.anchorPos, this.frameSize, false, RemoveTabBarSubViewStep())
        }
    }

    /** Whether the popup is open (not hidden or fading). */
    fun isOpen(): Boolean = this.popupFrame?.isClosed == false

    /** Releases everything; the activity removes the view. */
    fun release() {
        handler.removeCallbacksAndMessages(null)
        val adapter = this.tabAdapter
        if (adapter != null) {
            adapter.release()
            this.tabAdapter = null
        }
        this.tabBar = null
        this.popupFrame = null
        this.hostContext = null
        this.subListener = null
        this.groupTabs = null
        this.closedItem = null
    }

    /** Renumbers the group's tabs from 0. */
    fun renumberTabs() {
        val list: MutableList<WebTabItem?>? = (this.groupTabs)
        if (list == null || list.isEmpty()) {
            return
        }
        var position = 0
        for (webTabItem in list) {
            if (webTabItem != null) {
                webTabItem.position = position
                position++
            }
        }
    }

    internal inner class ApplyListStep : WebTabBar.Listener {
        override fun onListChanged() {
            val adapter = this@WebTabBarSubView.tabAdapter
            if (adapter != null) {
                try {
                    adapter.applyList(this@WebTabBarSubView.groupSelected, this@WebTabBarSubView.groupTabs)
                } catch (unused: Exception) {
                }
            }
        }

        override fun onCloseTab(position: Int, view: View?, isGroup: Boolean) {
            this@WebTabBarSubView.closeTab(position)
        }

        override fun onTabLongPress(view: View?, position: Int, dragFrom: Int, isGroup: Boolean) {
            val sub = this@WebTabBarSubView
            if (sub.subListener == null) {
                return
            }
            // Long press: show the tab's preview and start dragging it.
            sub.subListener!!.activity().showTabMenu(view, sub.groupStart, position + sub.groupStart, false)
            sub.dragFrom = dragFrom
            sub.dragTo = dragFrom
        }

        override fun onTabClick(x: Int, position: Int, width: Int, isGroup: Boolean) {
            val sub = this@WebTabBarSubView
            if (sub.subListener == null) {
                return
            }
            // Tab tapped: close the group popup and switch to it.
            val activity = sub.subListener!!.activity()
            activity.closeTabBarSubView()
            activity.selectTab(sub.groupStart + position, true)
        }

        override fun onTabsMerged(position: Int, arrayList: ArrayList<WebTabItem?>?) {}
    }

    internal inner class RedrawAllStep : TabDragListener {
        internal inner class RedrawAllTask : Runnable {
            override fun run() {
                val sub = this@WebTabBarSubView
                if (sub.dragMoved) {
                    sub.dragMoved = false
                    val adapter = sub.tabAdapter
                    if (adapter == null) {
                        return
                    }
                    if (sub.dragFrom == sub.dragTo) {
                        adapter.redrawAll()
                        return
                    }
                    val list = adapter.tabItems
                    sub.subRealList = list as MutableList<WebTabItem?>?
                    sub.setDrawList(list)
                }
            }
        }

        override fun onDragState(state: Int) {
            val sub = this@WebTabBarSubView
            if (state == 1) {
                sub.dragMoved = false
                sub.swipeArmed = true
            } else if (state == 2) {
                sub.dragMoved = true
                sub.swipeArmed = false
            } else if (state == 0 && sub.dragMoved) {
                sub.post(RedrawAllTask())
            }
        }

        override fun onMove(from: Int, to: Int): Boolean {
            val sub = this@WebTabBarSubView
            val adapter = sub.tabAdapter
            if (adapter == null) {
                return false
            }
            sub.dragTo = to
            return adapter.moveItem(from, to)
        }

        override fun onMerge(from: Int, to: Int) {}

        override fun onSwiped(position: Int) {
            val sub = this@WebTabBarSubView
            if (sub.swipeArmed) {
                sub.swipeArmed = false
                sub.closeTab(position)
            }
        }

        override fun onSwipePos(state: Int) {
        }
    }
}
