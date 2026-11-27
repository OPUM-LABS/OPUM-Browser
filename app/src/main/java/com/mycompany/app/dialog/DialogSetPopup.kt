package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZoneSettings
import android.content.DialogInterface
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainDragAdapter.MainDragItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebViewActivity
import kotlin.math.roundToInt
import com.mycompany.app.ui.components.Sym

/**
 * Edits the long-press menu for links ([imageMode] = false) or images ([imageMode] = true): the
 * twelve items in their order, each with a switch (mask bits in [popupValues] / [popupLabels]); long-press-drag reorders them (order
 * string in [itemOrder], ids joined by "/"). Apply stores both and calls [applyListener]; Reset (after asking) goes back to all items in
 * the default order. The caller sets [hostActivity], [appContext], [applyListener], [imageMode], [usedMask], [itemOrder], [dragItems] = [buildItems] (false), then calls [start]. Compose now
 * (on [ComposeBottomDialog]).
 */
@Suppress("deprecation")
class DialogSetPopup(context: Context?) : ComposeBottomDialog(context) {
    @JvmField
    var hostActivity: WebViewActivity? = null
    @JvmField
    var appContext: Context? = null
    @JvmField
    var applyListener: DialogApplyListener? = null
    @JvmField
    var imageMode: Boolean = false
    @JvmField
    var msgDialog: DialogSetMsg? = null
    @JvmField
    var usedMask: Int = 0
    @JvmField
    var itemOrder: String? = null
    @JvmField
    var dragItems: ArrayList<Any?>? = null

    private val items = mutableStateListOf<MainDragItem>()
    private var applyEnabled by mutableStateOf(true)
    private var started = false

    /** Shows the sheet with [dragItems] (old: built the views on the handler). */
    fun start() {
        if (started || appContext == null) return
        started = true
        items.clear()
        dragItems?.forEach { (it as? MainDragItem)?.let { item -> items.add(item) } }
        updateApply()
        setSheet {
            Text(
                stringResource(AppR.string.edit), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
            )
            val density = LocalDensity.current
            val rowPx = with(density) { 52.dp.toPx() }
            var dragIndex by remember { mutableIntStateOf(-1) }
            var dragOffset by remember { mutableFloatStateOf(0f) }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                items.forEachIndexed { index, item ->
                    key(item.id) {
                        val dragging = dragIndex == index
                        Row(
                            Modifier.fillMaxWidth().height(52.dp)
                                .zIndex(if (dragging) 1f else 0f)
                                .graphicsLayer { translationY = if (dragging) dragOffset else 0f; shadowElevation = if (dragging) 8f else 0f }
                                .pointerInput(item.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { dragIndex = items.indexOf(item); dragOffset = 0f },
                                        onDrag = { change, dragDelta ->
                                            change.consume()
                                            dragOffset += dragDelta.y
                                            val cur = dragIndex
                                            if (cur >= 0) {
                                                val target = (cur + (dragOffset / rowPx).roundToInt()).coerceIn(0, items.lastIndex)
                                                if (target != cur) {
                                                    items.add(target, items.removeAt(cur))
                                                    dragOffset -= (target - cur) * rowPx
                                                    dragIndex = target
                                                }
                                            }
                                        },
                                        onDragEnd = { dragIndex = -1; dragOffset = 0f; storeOrder() },
                                        onDragCancel = { dragIndex = -1; dragOffset = 0f; storeOrder() },
                                    )
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(com.mycompany.app.ui.components.Sym.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (item.iconRes != 0) {
                                Icon(painterResource(item.iconRes), contentDescription = null, modifier = Modifier.padding(start = 16.dp).size(24.dp))
                            }
                            Text(stringResource(item.titleRes), modifier = Modifier.weight(1f).padding(start = 16.dp))
                            Switch(checked = item.isOn, onCheckedChange = { toggle(item, it) })
                        }
                    }
                }
            }
            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { askReset() }) { Text(stringResource(AppR.string.reset)) }
                TextButton(onClick = { save(true) }, enabled = applyEnabled) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    private fun toggle(item: MainDragItem, on: Boolean) {
        val index = items.indexOf(item)
        if (index < 0) return
        items[index] = MainDragItem(item.id, item.iconRes, item.titleRes, on)
        setUsed(item.id, on)
    }

    /** Order string from the list (old drop listener). */
    private fun storeOrder() {
        if (items.isEmpty()) return
        itemOrder = items.joinToString("/") { it.id.toString() }
    }

    private fun askReset() {
        if (hostActivity == null || msgDialog != null) return
        val msg = DialogSetMsg(hostActivity, AppR.string.reset_setting, AppR.string.reset, object : DialogApplyListener {
            override fun onApply() {
                msgDialog?.dismiss()
                msgDialog = null
                if (usedMask != 8190 || !MainUtil.sameObject(itemOrder, "")) {
                    usedMask = 8190
                    itemOrder = ""
                    items.clear()
                    buildItems(true).forEach { (it as? MainDragItem)?.let { item -> items.add(item) } }
                    updateApply()
                }
                save(false)
            }
        })
        msgDialog = msg
        msg.setOnDismissListener(DialogInterface.OnDismissListener {
            msgDialog?.dismiss()
            msgDialog = null
        })
    }

    fun buildItems(articleA: Boolean): ArrayList<Any?> {
        val imageMode = this.imageMode
        val zoneIcons = MainUtil.zoneIconList(if (imageMode) 1 else 0, articleA)
        val arrayList: ArrayList<Any?> = ArrayList<Any?>()
        if (imageMode) {
            for (index in 0..11) {
                val labelIndex = zoneIcons[index]
                val usedMask = this.usedMask
                val label: Int = popupLabels[labelIndex]
                arrayList.add(
                    MainDragItem(
                        labelIndex,
                        if (AppState.darkUi) MainConst.imageMenuIconsDark[labelIndex] else MainConst.imageMenuIconsBlack[labelIndex],
                        MainConst.imageMenuNames[labelIndex],
                        (usedMask and label) == label
                    )
                )
            }
        } else {
            for (index2 in 0..11) {
                val index = zoneIcons[index2]
                val usedMask = this.usedMask
                val intValue: Int = popupValues[index]
                val used = (usedMask and intValue) == intValue
                if (SyncSettings.secretMode.value && index == 6) {
                    arrayList.add(
                        MainDragItem(
                            index,
                            if (AppState.darkUi) AppR.drawable.outline_mood_dark_24 else AppR.drawable.outline_mood_black_24,
                            AppR.string.normal_tab,
                            used
                        )
                    )
                } else {
                    arrayList.add(
                        MainDragItem(
                            index,
                            if (AppState.darkUi) MainConst.linkMenuIconsDark[index] else MainConst.linkMenuIconsBlack[index],
                            MainConst.linkMenuNames[index],
                            used
                        )
                    )
                }
            }
        }
        return arrayList
    }

    fun save(apply: Boolean) {
        if (this.imageMode) {
            if (ZoneSettings.useImg.value != this.usedMask || !MainUtil.sameObject(ZoneSettings.imgOrder.value, this.itemOrder)) {
                ZoneSettings.editLocal { set(ZoneSettings.useImg, this@DialogSetPopup.usedMask) }
                val itemOrder = this.itemOrder
                ZoneSettings.editLocal { set(ZoneSettings.imgOrder, itemOrder) }
                if (itemOrder == null) {
                    ZoneSettings.editLocal { set(ZoneSettings.imgOrder, "") }
                }
                val prefZoneR = PrefZone.get(this.appContext, false)
                if (apply) {
                    prefZoneR!!.putInt(ZoneSettings.useImg.value, "mUseImg5")
                    prefZoneR.putString("mImgOrder4", ZoneSettings.imgOrder.value)
                } else {
                    prefZoneR!!.removeKey("mUseImg5")
                    prefZoneR.removeKey("mImgOrder4")
                }
                prefZoneR.saveAsync()
                val dialogApplyListener = this.applyListener
                if (dialogApplyListener != null) {
                    dialogApplyListener.onApply()
                }
            }
        } else if (ZoneSettings.useLink.value != this.usedMask || !MainUtil.sameObject(ZoneSettings.linkOrder.value, this.itemOrder)) {
            ZoneSettings.editLocal { set(ZoneSettings.useLink, this@DialogSetPopup.usedMask) }
            val itemOrder = this.itemOrder
            ZoneSettings.editLocal { set(ZoneSettings.linkOrder, itemOrder) }
            if (itemOrder == null) {
                ZoneSettings.editLocal { set(ZoneSettings.linkOrder, "") }
            }
            val prefZoneR = PrefZone.get(this.appContext, false)
            if (apply) {
                prefZoneR!!.putInt(ZoneSettings.useLink.value, "mUseLink7")
                prefZoneR.putString("mLinkOrder6", ZoneSettings.linkOrder.value)
            } else {
                prefZoneR!!.removeKey("mUseLink7")
                prefZoneR.removeKey("mLinkOrder6")
            }
            prefZoneR.saveAsync()
            val dialogApplyListener = this.applyListener
            if (dialogApplyListener != null) {
                (dialogApplyListener as DialogUrlLink.ImageModeApply).onApply()
            }
        }
        if (apply) {
            dismiss()
        }
    }

    /** Apply is possible only with at least one item on. */
    fun updateApply() {
        applyEnabled = usedMask != 0
    }

    fun setUsed(id: Int, on: Boolean) {
        val imageMode = this.imageMode
        val usedMask = this.usedMask
        if (imageMode) {
            val labels: IntArray = popupLabels
            if (on) {
                this.usedMask = usedMask or labels[id]
            } else {
                this.usedMask = usedMask and (labels[id].inv())
            }
        } else {
            val values: IntArray = popupValues
            if (on) {
                this.usedMask = usedMask or values[id]
            } else {
                this.usedMask = usedMask and (values[id].inv())
            }
        }
        updateApply()
    }

    override fun dismiss() {
        msgDialog?.dismiss()
        msgDialog = null
        hostActivity = null
        appContext = null
        applyListener = null
        super.dismiss()
    }

    companion object {
        @JvmField
        val popupValues: IntArray = intArrayOf(
            0,
            2,
            4,
            8,
            16,
            32,
            64,
            128,
            256,
            512,
            1024,
            2048,
            8192
        )
        @JvmField
        val popupLabels: IntArray = intArrayOf(
            0,
            2,
            4,
            8,
            16,
            32,
            64,
            128,
            256,
            512,
            1024,
            2048,
            8192
        )
    }
}
