package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.MainSettings
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.View
import com.mycompany.app.main.MainApp
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.AddrIconAdapter
import com.mycompany.app.main.AddrIconAdapter.AddrHolder
import com.mycompany.app.main.AddrIconAdapter.AddrListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.themeColorAt
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MenuIconAdapter.MainMenuItem
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.quick.MenuDragHelper
import com.mycompany.app.quick.MenuDragHelper.MenuDragListener
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.MyManagerLinear
import com.mycompany.app.view.MyRecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import kotlin.math.max

/**
 * Address-bar buttons (Settings > Composition > Address bar icons): the bar preview with its icons, which can be dragged
 * into a new order or tapped (x) to take them off; the icons not in use below (tap to put one back), or the short guide
 * when all are in use; Reset (asks first, then back to the default order) and Apply.
 * The preview (a rounded pill, was `MyAddrView`) and the draggable icon row are `AddrIconAdapter` / `MenuDragHelper` in an
 * `AndroidView`; the rest is Compose (on [ComposeBottomDialog]). Same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetAddr(activity: Activity?) : ComposeBottomDialog(activity) {
    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var hostActivity: Activity? = activity
    @JvmField var appContext: Context? = context
    @JvmField var listView: MyRecyclerView? = null
    @JvmField var iconAdapter: AddrIconAdapter? = null
    @JvmField var layoutManager: MyManagerLinear? = null
    @JvmField var dragHelper: MenuDragHelper? = null
    @JvmField var touchHelper: ItemTouchHelper? = null
    @JvmField var applying: Boolean = false
    @JvmField var originalIds: IntArray? = parseIntList(MainSettings.addrItems.value)
    @JvmField var msgDialog: DialogSetMsg? = null
    @JvmField var mainBusy: Boolean = false

    private var preview: FrameLayout? = null
    /** Ids of the icons not on the bar. */
    private var unused by mutableStateOf<List<Pair<Int, Int>>>(emptyList())

    init {
        buildViews()
        setSheet {
            val frame = preview
            if (frame != null) {
                AndroidView(factory = { frame }, modifier = Modifier.fillMaxWidth().height(80.dp))
            }
            val list = unused
            if (list.isNotEmpty()) {
                Text(
                    stringResource(AppR.string.not_used), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                )
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
                ) {
                    for ((index, id) in list) {
                        FilledTonalIconButton(onClick = { addBack(index) }, modifier = Modifier.size(56.dp)) {
                            Icon(painterResource(themeColorAt(id, MainUtil.barTint(0, false))), null)
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(stringResource(AppR.string.quick_guide_1), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(AppR.string.addr_icon_info), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onReset() }) { Text(stringResource(AppR.string.reset)) }
                TextButton(onClick = { onApply() }) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    /** The bar preview and the draggable icon row on top of it. */
    private fun buildViews() {
        val context = this.appContext ?: return
        val frame = FrameLayout(context)
        val height = dpToPx(context, 80.0f).toInt()
        // the address pill behind the icons (was MyAddrView: a rounded rectangle, inset from the sides, centred,
        // in the bar's setting colour)
        val bar = View(context)
        bar.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(com.mycompany.app.ui.browser.BarFace.bgColorFor(MainUtil.barTint(0, false), 0, true))
            cornerRadius = MainApp.dp8.toFloat()
        }
        val pill = FrameLayout.LayoutParams(-1, MainApp.dp48 - 2 * MainApp.dp8)
        pill.gravity = android.view.Gravity.CENTER_VERTICAL
        pill.leftMargin = MainApp.dp4
        pill.rightMargin = MainApp.dp4
        frame.addView(bar, pill)
        val list = com.mycompany.app.outline.LegacyViews.recyclerView(context, false, false)
        frame.addView(list, ViewGroup.LayoutParams(-1, height))
        this.listView = list
        this.preview = frame
        this.layoutManager = MyManagerLinear(0)
        val adapter = AddrIconAdapter()
        adapter.hasBack = true
        adapter.listView = list
        adapter.itemListener = UpdateUnusedStep()
        adapter.buildItems(false, false)
        adapter.notiMode = ZoneSettings.notiAddr.value
        this.iconAdapter = adapter
        val drag = MenuDragHelper(IconDragListener())
        this.dragHelper = drag
        val touch = ItemTouchHelper(drag)
        this.touchHelper = touch
        touch.attachToRecyclerView(list)
        list.setLayoutManager(this.layoutManager)
        list.setAdapter(adapter)
        list.setSizeListener(IconListSizeListener())
        updateUnused()
    }

    /** One of the unused icons tapped: back onto the bar. */
    private fun addBack(index: Int) {
        val adapter = this.iconAdapter ?: return
        val spare = if (adapter.menuItems != null) adapter.spareItems else null
        if (adapter.menuItems != null && spare != null && index >= 0 && index < spare.size) {
            val item = spare.removeAt(index) as MainMenuItem?
            if (item != null) {
                adapter.menuItems!!.add(item)
                adapter.notifyDataSetChanged()
            }
        }
        updateUnused()
    }

    /** Apply. */
    private fun onApply() {
        if (iconAdapter == null || mainBusy) return
        mainBusy = true
        mainHandler?.post {
            applyItems(true)
            mainBusy = false
        }
    }

    /** Reset: asks first. */
    private fun onReset() {
        if (hostActivity == null || msgDialog != null) return
        val msg = DialogSetMsg(hostActivity, AppR.string.reset_setting, AppR.string.reset, BuildItemsStep())
        msgDialog = msg
        msg.setOnDismissListener(MsgDialogDismiss())
    }

    internal inner class BuildItemsStep : DialogApplyListener {
        override fun onApply() {
            msgDialog?.dismiss()
            msgDialog = null
            val adapter = iconAdapter ?: return
            adapter.buildItems(true, true)
            updateUnused()
            if (mainBusy) return
            mainBusy = true
            mainHandler?.post {
                applyItems(false)
                mainBusy = false
            }
        }
    }

    internal inner class MsgDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            msgDialog?.dismiss()
            msgDialog = null
        }
    }

    internal inner class UpdateUnusedStep : AddrListener {
        /** Removes the address-bar icon at `position` (not the fixed ones) into the "unused" list.  */
        override fun onItemClick(position: Int) {
            val dialog = this@DialogSetAddr
            if (dialog.applying || dialog.iconAdapter == null) {
                return
            }
            val adapter = dialog.iconAdapter
            if (adapter!!.menuItems != null && position >= (if (adapter.hasBack) 1 else 0) && position < adapter.menuItems!!.size) {
                val removed: Any? = adapter.menuItems!!.removeAt(position)
                if (removed != null) {
                    if (adapter.spareItems == null) {
                        adapter.spareItems = ArrayList<Any?>()
                    }
                    adapter.spareItems!!.add(removed)
                    adapter.notifyDataSetChanged()
                }
            }
            dialog.updateUnused()
        }

        override fun onStartDrag(addrHolder: AddrHolder?) {
            val touch = if (applying || iconAdapter == null) null else touchHelper
            touch?.startDrag(addrHolder!!)
        }
    }

    internal inner class IconDragListener : MenuDragListener {
        override fun onDragState(state: Int) {
            applying = state == 2
        }

        override fun onMove(from: Int, to: Int) {
            val adapter = iconAdapter ?: return
            val items = adapter.menuItems ?: return
            val first = if (adapter.hasBack) 1 else 0
            if (from < first || to < first || from >= items.size || to >= items.size) return
            val item = items.removeAt(from) as MainMenuItem? ?: return
            items.add(to, item)
            adapter.notifyItemMoved(from, to)
        }
    }

    internal inner class IconListSizeListener : ImageSizeListener {
        override fun onSize(view: View?, width: Int, height: Int) {
            iconAdapter?.notifyDataSetChanged()
        }
    }

    /** Saves the address-bar icon order if it changed (`refresh`: save it explicitly and close; otherwise reset to default).  */
    fun applyItems(refresh: Boolean) {
        val adapter = this.iconAdapter
        if (adapter == null) {
            return
        }
        val offset = if (adapter.hasBack) 1 else 0
        val original = this.originalIds
        val originalCount = if (original != null) original.size else 0
        val count = if (adapter.menuItems != null) max(adapter.menuItems!!.size - offset, 0) else 0
        var changed = originalCount != count
        run {
            var k = 0
            while (!changed && k < count) {
                val item = adapter.menuItems!!.get(k + offset) as MainMenuItem?
                if (item != null && original!![k] != item.id) {
                    changed = true
                }
                k++
            }
        }
        if (changed) {
            var icons: IntArray? = null
            if (count != 0) {
                icons = IntArray(count)
                for (k in 0..<count) {
                    val item = adapter.menuItems!!.get(k + offset) as MainMenuItem?
                    if (item != null && item.id >= 0 && item.id < 3) {
                        icons[k] = item.id
                    }
                }
            }
            MainSettings.editLocal { set(MainSettings.addrItems, MainUtil.joinIntList(icons)) }
            val pref = PrefMain.get(this.appContext, false)
            if (refresh) {
                pref!!.putString("mAddrItems2", MainSettings.addrItems.value)
            } else {
                pref!!.removeKey("mAddrItems2")
            }
            pref.saveAsync()
        }
        if (refresh) {
            dismiss()
        }
    }

    /** Refreshes the "not used" icons. */
    fun updateUnused() {
        val adapter = this.iconAdapter ?: return
        val spare = adapter.spareItems
        if (spare == null || spare.isEmpty()) {
            unused = emptyList()
            return
        }
        val ids = ArrayList<Pair<Int, Int>>()
        for (k in 0..<minOf(spare.size, 3)) {
            val item = spare[k] as MainMenuItem?
            val id = item?.id ?: -1
            if (id in 0..2) ids.add(k to id)
        }
        unused = ids
    }

    override fun dismiss() {
        if (this.appContext != null) {
            this.msgDialog?.dismiss()
            this.msgDialog = null
            this.listView?.release()
            this.listView = null
            val adapter = this.iconAdapter
            if (adapter != null) {
                adapter.lastCount = adapter.getItemCount()
                adapter.listView = null
                adapter.itemListener = null
                adapter.menuItems = null
                adapter.spareItems = null
                this.iconAdapter = null
            }
            this.dragHelper?.menuDragListener = null
            this.dragHelper = null
            this.hostActivity = null
            this.appContext = null
            this.layoutManager = null
            this.touchHelper = null
            this.originalIds = null
            this.preview = null
        }
        super.dismiss()
    }
}
