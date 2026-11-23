package com.mycompany.app.web

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeTabBar
import java.util.ArrayList

/**
 * The model of the tab bar: the bar items [tabItems] (single tabs, and groups whose members are in
 * [WebTabItem.groupTabs]), built from the flat tab list [shownItems] by [applyList]; the selected tab ([selectedPos] flat
 * index, [shownIndex] bar position); and the look prefs. It replaces the RecyclerView adapter the bar had
 * (`WebTabBarAdapter` stays for the tab-list popup and the guide); the Compose [ComposeTabBar] draws it and watches
 * [version], which every change ([notifyAllTabs], [notifyChanged], [notifyInserted], [notifyMoved], [redrawAll]) bumps.
 * The tab bar's callbacks to the activity are [Listener]. Favicons are loaded with the list loader into [icons] (keyed by
 * tab id).
 */
@Suppress("deprecation")
class WebTabBar(
    context: Context?,
    list: MutableList<WebTabItem?>?,
    shownIndex: Int,
    onStartPage: Boolean,
    barColorParam: Int,
    tintColorParam: Int,
    tabBarListener: Listener?
) {
    @JvmField var frozenCount: Int = 0
    @JvmField var appContext: Context?
    /** The view that draws this model (for `post`, scrolling). */
    @JvmField var composeBar: ComposeTabBar? = null
    @JvmField var shownItems: MutableList<WebTabItem?>? = null
    @JvmField var shownIndex: Int = 0
    @JvmField var tabItems: ArrayList<WebTabItem?>? = null
    @JvmField var selectedPos: Int = 0
    @JvmField var darkPage: Boolean
    @JvmField var barListener: Listener?
    @JvmField var barColor: Int
    @JvmField var tintColor: Int
    @JvmField var tintMode: Int
    @JvmField var customStyle: Boolean = false
    @JvmField var customOutline: Boolean = false
    @JvmField var customColor: Int = 0
    @JvmField var customClose: Boolean = false
    @JvmField var customWidth: Int = 0
    @JvmField var listLoader: MainListLoader?
    @JvmField var padStart: Int
    @JvmField var padEnd: Int
    @JvmField var padSmall: Int
    @JvmField var changeListener: ChangeListener? = null

    /** Bumped on every change; the bar redraws from it. */
    var version by mutableIntStateOf(0)
    /** Favicons by tab id. */
    val icons: SnapshotStateMap<Long, Bitmap> = mutableStateMapOf()

    private fun bump() { version++ }
    fun notifyAllTabs() = bump()
    fun notifyChanged(position: Int) = bump()
    fun notifyInserted(position: Int) = bump()
    fun notifyMoved(from: Int, to: Int) = bump()
    internal inner class NotifyAllTabsTask : Runnable {
        override fun run() { if (this@WebTabBar.appContext != null) this@WebTabBar.notifyAllTabs() }
    }

    interface ChangeListener {
        fun onChanged()
    }

    interface Listener {
        fun onListChanged()

        /** Close (or group menu) button of the tab with flat index [position]. */
        fun onCloseTab(position: Int, view: View?, isGroup: Boolean)

        /** Long press on a tab: [position] flat index, [i2] bar position. The bar starts the drag itself. */
        fun onTabLongPress(view: View?, position: Int, dragFrom: Int, isGroup: Boolean)

        /** Tap on a tab: [x] centre x, [i2] flat index, [i3] bar position. */
        fun onTabClick(x: Int, position: Int, width: Int, isGroup: Boolean)

        fun onTabsMerged(position: Int, arrayList: ArrayList<WebTabItem?>?)
    }

    init {
        var i2 = barColorParam
        var i3 = tintColorParam
        if (MainUtil.isDarkBarIcons(i2, onStartPage)) {
            i2 = 0
            i3 = 0
        }
        this.appContext = context
        this.darkPage = MainUtil.isDarkPage(onStartPage)
        this.barColor = i2
        this.tintColor = if (i2 == 0) 0 else i3
        this.tintMode = MainUtil.barTint(i2, onStartPage)
        this.barListener = tabBarListener
        this.padStart = Math.round(MainUtil.dpToPx(this.appContext, 44.0f))
        this.padEnd = Math.round(MainUtil.dpToPx(this.appContext, 32.0f))
        this.padSmall = Math.round(MainUtil.dpToPx(this.appContext, 8.0f))
        try {
            applyList(shownIndex, list)
        } catch (unused: Exception) {
            val tabBarListener = this.barListener
            if (tabBarListener != null) {
                tabBarListener.onListChanged()
            }
        }
        this.listLoader = MainListLoader(this.appContext!!, false, object : ListLoadListener {
            override fun onFailed(childItem: ChildItem?, view: View?) {}

            override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                if (childItem == null || !MainUtil.isBitmapOk(bitmap)) return
                icons[childItem.id] = bitmap!!
            }
        })
    }

    fun itemAt(pos: Int): WebTabItem? {
        try {
            val arrayList = this.tabItems
            if (arrayList == null || pos < 0 || pos >= arrayList.size) {
                return null
            }
            return this.tabItems!!.get(pos)
        } catch (unused: Exception) {
            return null
        }
    }

    fun barPosOf(currentTabIndex: Int): Int {
        val arrayList = this.tabItems
        val size = if (arrayList == null) 0 else arrayList.size
        val list: MutableList<WebTabItem?>? = if (arrayList == null || size == 0) null else this.shownItems
        if (arrayList == null || size == 0 || list == null || list.size == 0) {
            return -1
        }
        val shownIndex = this.shownIndex
        var selectedPos = this.selectedPos
        if (shownIndex == currentTabIndex) {
            return selectedPos
        }
        if (selectedPos < 0) {
            selectedPos = 0
        } else if (selectedPos >= size) {
            selectedPos = size - 1
        }
        if (shownIndex < currentTabIndex) {
            val forwardIndex: Int = findForward(currentTabIndex, arrayList, size, selectedPos)
            return if (forwardIndex != -1) forwardIndex else findBackward(arrayList, currentTabIndex, selectedPos - 1)
        }
        val backwardIndex: Int = findBackward(arrayList, currentTabIndex, selectedPos)
        return if (backwardIndex != -1) backwardIndex else findForward(currentTabIndex, arrayList, size, selectedPos + 1)
    }

    fun shownAt(position: Int): WebTabItem? {
        try {
            val list = this.shownItems
            if (list != null && position >= 0 && position < list.size) {
                return this.shownItems!!.get(position)
            }
        } catch (unused: Exception) {
        }
        return null
    }

    /** Releases the loader and the references. */
    fun release() {
        this.frozenCount = count()
        val mainListLoader = this.listLoader
        if (mainListLoader != null) {
            mainListLoader.release()
            this.listLoader = null
        }
        this.appContext = null
        this.composeBar = null
        this.barListener = null
        this.shownItems = null
        this.tabItems = null
    }

    /** Moves bar item [from] to [i2]. */
    fun moveItem(from: Int, to: Int): Boolean {
        var webTabItem: WebTabItem? = null
        val arrayList = this.tabItems
        val size = if (arrayList == null || from < 0 || to < 0) 0 else arrayList.size
        if (arrayList == null || from < 0 || to < 0 || from >= size || to >= size || ((this.tabItems!!.removeAt(from)).also {
                webTabItem = it
            }) == null) {
            return false
        }
        this.tabItems!!.add(to, webTabItem)
        notifyMoved(from, to)
        return true
    }

    /** Builds the bar items [notifyChanged] from the flat tab list: single tabs and groups (tabs sharing a group id in a row). */
    fun applyList(shownIndex: Int, list: MutableList<WebTabItem?>?) {
        val webTabItem: WebTabItem
        var shownTab: WebTabItem
        this.shownItems = list
        this.shownIndex = shownIndex
        var selectedPos = -1
        if (list == null || list.isEmpty()) {
            this.tabItems = null
            this.selectedPos = -1
            return
        }
        val arrayList: ArrayList<WebTabItem?> = ArrayList()
        val it: MutableIterator<WebTabItem?> = list.iterator()
        var groupTabs: ArrayList<WebTabItem?>? = null
        var groupPos = 0
        var groupId: Long = 0
        while (it.hasNext()) {
            val tab = it.next()
            if (tab != null) {
                val copy = WebTabItem()
                copy.parentId = tab.parentId
                copy.position = tab.position
                if (groupTabs != null) {
                    val groupIdNow = tab.groupId
                    if (groupIdNow == 0L || groupIdNow != groupId) {
                        if (groupTabs.size == 1) {
                            shownTab = groupTabs.get(0) as WebTabItem
                            shownTab.groupTabs = null
                        } else {
                            shownTab = WebTabItem()
                            shownTab.groupTabs = groupTabs
                        }
                        shownTab.groupPos = groupPos
                        arrayList.add(shownTab)
                        groupPos++
                        groupTabs = null
                    }
                }
                if (shownIndex == tab.position) {
                    selectedPos = groupPos
                }
                if (tab.groupId != 0L) {
                    if (groupTabs == null) {
                        groupTabs = ArrayList()
                    }
                    groupTabs.add(copy)
                } else {
                    copy.groupPos = groupPos
                    arrayList.add(copy)
                    groupPos++
                }
                groupId = tab.groupId
            }
        }
        if (groupTabs != null) {
            if (groupTabs.size == 1) {
                webTabItem = groupTabs.get(0) as WebTabItem
                webTabItem.groupTabs = null
            } else {
                webTabItem = WebTabItem()
                webTabItem.groupTabs = groupTabs
            }
            arrayList.add(webTabItem)
        }
        this.tabItems = arrayList
        this.selectedPos = selectedPos
    }

    /** A new favicon bitmap for the tab with flat index [index] (the page reported its icon). */
    fun setIcon(webTabItem: WebTabItem?, index: Int, bitmap: Bitmap?) {
        if (webTabItem == null || !MainUtil.isBitmapOk(bitmap)) return
        icons[webTabItem.uid] = bitmap!!
    }

    /** Asks the loader for the favicon of [webTabItem] (a web page) if it is not known yet. */
    fun loadIcon(webTabItem: WebTabItem, view: View?, pos: Int) {
        if (icons.containsKey(webTabItem.uid) || listLoader == null || view == null) return
        val url = webTabItem.tabUrl
        if (url.isNullOrEmpty() || url.startsWith("file:///") || url.startsWith("/data/")) return
        val child = ChildItem()
        child.listType = 39
        child.itemType = 11.toByte()
        child.id = webTabItem.uid
        child.path = url
        child.position = pos
        val cached = MainListLoader.cachedIconFor(child)
        if (MainUtil.isBitmapOk(cached)) { icons[webTabItem.uid] = cached!!; return }
        listLoader!!.enqueue(child, view)
    }

    /** Re-reads the list after the tabs, the selection or the look changed. */
    fun applyTint(list: MutableList<WebTabItem?>?, shownIndex: Int, onStartPage: Boolean, barColor: Int, tintColor: Int, reset: Boolean) {
        var barColor = barColor
        var tintColor = tintColor
        if (MainUtil.isDarkBarIcons(barColor, onStartPage)) {
            barColor = 0
            tintColor = 0
        }
        val mainListLoader = this.listLoader
        if (mainListLoader != null) {
            mainListLoader.queue = null
        }
        val shown: MutableList<WebTabItem?>? = if (reset) null else this.shownItems
        if (reset || shown == null || shown.isEmpty()) {
            try {
                applyList(shownIndex, list)
            } catch (unused: Exception) {
                val tabBarListener = this.barListener
                if (tabBarListener != null) {
                    tabBarListener.onListChanged()
                }
            }
        } else {
            this.selectedPos = barPosOf(shownIndex)
            this.shownIndex = shownIndex
        }
        this.darkPage = MainUtil.isDarkPage(onStartPage)
        this.barColor = barColor
        this.tintColor = if (barColor != 0) tintColor else 0
        this.tintMode = MainUtil.barTint(barColor, onStartPage)
        notifyAllTabs()
    }

    /**
     * Rebuilds the flat tab order [.f] from the bar items [.h] (single tabs and groups
     * with children in [WebTabItem.groupTabs]): renumbers every tab, re-links it to the previous tab ([WebTabItem.parentId]),
     * saves tabs whose link changed, tracks the current tab, and with `notify` refreshes the
     * range of bar positions that changed.
     */
    fun rebuild(notify: Boolean) {
        val bar = this.tabItems
        if (bar == null || bar.isEmpty()) {
            this.shownItems = null
            this.shownIndex = 0
            this.selectedPos = -1
            if (this.barListener != null) {
                this.barListener!!.onTabsMerged(0, null)
            }
            return
        }
        val flat: ArrayList<WebTabItem?> = ArrayList()
        var changedTabs: ArrayList<WebTabItem?>? = null
        var prevUid: Long = -1
        var index = 0 // position in the flat tab list
        var barPos = 0 // position in the tab bar
        var current = 0
        var currentBar = -1
        var anyChanged = false
        for (entry2 in bar) {
            val entry = entry2
            if (entry == null) {
                continue
            }
            val members: MutableList<WebTabItem?>?
            if (entry.groupTabs == null) {
                members = mutableListOf(entry)
            } else {
                members = entry.groupTabs
            }
            var changed = false
            var any = false
            for (member2 in members!!) {
                val member = member2
                val real = if (member != null) shownAt(member.position) else null
                if (real == null) {
                    continue
                }
                any = true
                if (this.shownIndex == real.position) {
                    current = index
                    currentBar = barPos
                }
                real.parentId = prevUid
                real.position = index
                member!!.position = index
                if (member.inGroup || member.parentId != prevUid) {
                    member.parentId = prevUid
                    member.inGroup = false
                    if (changedTabs == null) {
                        changedTabs = ArrayList()
                    }
                    val save = WebTabItem()
                    save.uid = real.uid
                    save.parentId = real.parentId
                    save.groupId = real.groupId
                    save.groupName = real.groupName
                    save.groupColor = real.groupColor
                    save.position = real.position
                    changedTabs.add(save)
                    changed = notify
                }
                flat.add(real)
                prevUid = real.uid
                index++
            }
            if (entry.groupTabs == null && !any) {
                continue
            }
            if (changed || entry.groupPos != barPos) {
                entry.groupPos = barPos
                if (notify) anyChanged = true
            }
            barPos++
        }
        if (anyChanged) bump()
        if (changedTabs != null && !changedTabs.isEmpty()) {
            DbBookTab.updateTabs(this.appContext, (changedTabs))
        }
        this.shownItems = flat
        this.shownIndex = current
        this.selectedPos = currentBar
        if (this.barListener != null) {
            this.barListener!!.onTabsMerged(current, flat)
        }
    }

    fun count(): Int {
        val frozenCount = this.frozenCount
        if (frozenCount != 0) {
            return frozenCount
        }
        val arrayList = this.tabItems
        if (arrayList == null) {
            return 0
        }
        return arrayList.size
    }

    /** The tab list changed (tab added / removed): rebuild, redraw, then tell [tabBarChangeListener]. */
    fun updateList(
        list: MutableList<WebTabItem?>?,
        groupSelected: Int,
        currentTabIndex: Int,
        animate: Boolean,
        tabBarChangeListener: ChangeListener?
    ) {
        if (this.changeListener == null) {
            this.changeListener = tabBarChangeListener
            val selectedPos = this.selectedPos
            val barPos = barPosOf(currentTabIndex)
            val arrayList = this.tabItems
            val size = if (arrayList != null) arrayList.size else 0
            try {
                applyList(groupSelected, list)
            } catch (unused: Exception) {
                val tabBarListener = this.barListener
                if (tabBarListener != null) {
                    tabBarListener.onListChanged()
                }
            }
            val tabItems = this.tabItems
            val size2 = if (tabItems != null) tabItems.size else 0
            val bar = this.composeBar
            if (bar == null) {
                return
            }
            notifyAllTabs()
            bar.post {
                val bus = this@WebTabBar
                val listener = bus.changeListener
                if (bus.appContext != null && listener != null) {
                    listener.onChanged()
                    bus.changeListener = null
                }
            }
        }
    }

    /** Redraws every tab. */
    fun redrawAll() = bump()

    /** Redraws the tab at bar position [position]. */
    fun redrawAt(position: Int) = bump()

    companion object {
        @JvmStatic
        fun findForward(currentTabIndex: Int, arrayList: ArrayList<WebTabItem?>?, end: Int, index: Int): Int {
            var index = index
            var indexNow = index
            while (index < end) {
                val webTabItem = arrayList!!.get(index)
                if (webTabItem != null) {
                    val groupTabs = webTabItem.groupTabs
                    if (groupTabs != null) {
                        val size = groupTabs.size
                        var tabIndex = 0
                        while (tabIndex < size) {
                            val entry: Any? = groupTabs.get(tabIndex)
                            tabIndex++
                            val tab = entry as WebTabItem?
                            if (tab != null && tab.position == currentTabIndex) {
                                return indexNow
                            }
                        }
                        indexNow++
                    } else {
                        if (webTabItem.position == currentTabIndex) {
                            return indexNow
                        }
                        indexNow++
                    }
                }
                index++
            }
            return -1
        }

        /** Flat index of the first tab of bar item [webTabItem]. */
        @JvmStatic
        fun groupColor(webTabItem: WebTabItem?): Int {
            val arrayList = webTabItem!!.groupTabs
            if (arrayList == null) {
                return webTabItem.position
            }
            return if (arrayList.size == 0) webTabItem.position else (webTabItem.groupTabs!!.get(0) as WebTabItem).position
        }

        @JvmStatic
        fun colorIndex(color: Int, darkUi: Boolean): Int {
            var color = color
            if (color == 0) {
                color = -65536
            }
            val length = MainConst.iconColors.size
            for (index in 0..<length) {
                if (color == MainConst.iconColors[index]) {
                    return if (color == -16777216 && darkUi) R.drawable.baseline_folder_2_black_24 else MainConst.folderIcons[index]
                }
            }
            return MainConst.folderIcons[0]
        }

        @JvmStatic
        fun findBackward(arrayList: ArrayList<WebTabItem?>?, currentTabIndex: Int, selectedPos: Int): Int {
            var selectedPos = selectedPos
            var indexValue = selectedPos
            while (selectedPos > -1) {
                val webTabItem = arrayList!!.get(selectedPos)
                if (webTabItem != null) {
                    val groupTabs = webTabItem.groupTabs
                    if (groupTabs != null) {
                        val size = groupTabs.size
                        var index = 0
                        while (index < size) {
                            val entry: Any? = groupTabs.get(index)
                            index++
                            val tab = entry as WebTabItem?
                            if (tab != null && tab.position == currentTabIndex) {
                                return indexValue
                            }
                        }
                        indexValue--
                    } else {
                        if (webTabItem.position == currentTabIndex) {
                            return indexValue
                        }
                        indexValue--
                    }
                }
                selectedPos--
            }
            return -1
        }
    }
}
