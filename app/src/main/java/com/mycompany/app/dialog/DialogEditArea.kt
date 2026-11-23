package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.ReadSettings
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.data.DataUtil.saveFile
import com.mycompany.app.db.DbUtil.existsById
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.db.book.DbBookSearch.Companion.get
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.updateById
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainAreaAdapter
import com.mycompany.app.main.MainAreaAdapter.AreaItem
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.isMediaUri
import com.mycompany.app.main.MainUriDoc.renameNumbered
import com.mycompany.app.main.MainUriVol.createUriItem
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.canUseBiometric
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefUtil.itemMode
import com.mycompany.app.pref.PrefUtil.setDetail
import com.mycompany.app.web.WebClean
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Block area editor ([pageUrl] page URL, [hostUrl] host, [savedText] the picked element selector, "!@!"-separated with the
 * attribute hints; [confirmFirst] = preview in the reader): the selector built from the checked parts, the parts as a list (each
 * level a group: the TAG row and its [attr] rows, a check to use / skip each, tap the text to edit it in
 * [DialogEditCmd]), Help (the area guide), Reset (back to the picked selector, after a confirm), Preview (the page in
 * [DialogWebView] with the rule applied) and Remove (saves the rule as a user filter, then [EditAreaListener]).
 * Parsing, the selector string and the save are the old code. Compose now (on [ComposeBottomDialog]); same constructor,
 * shows itself.
 */
@Suppress("deprecation")
class DialogEditArea(
    mainActivity: android.app.Activity?,
    pageUrl: String?,
    hostUrl: String?,
    savedText: String?,
    confirmFirst: Boolean,
    editAreaListener: EditAreaListener?,
) : ComposeBottomDialog(mainActivity) {
    private val ui = android.os.Handler(android.os.Looper.getMainLooper())
    @JvmField val mainHandler: android.os.Handler? = ui

    @JvmField var hostActivity: android.app.Activity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var areaListener: EditAreaListener? = editAreaListener
    @JvmField var guideDialog: DialogGuideArea? = null
    @JvmField var previewDialog: DialogWebView? = null
    @JvmField var confirmFirst: Boolean = confirmFirst
    @JvmField var msgDialog: DialogSetMsg? = null
    @JvmField var cmdDialog: DialogEditCmd? = null
    @JvmField var pageUrl: String? = pageUrl
    @JvmField var hostUrl: String? = hostUrl
    @JvmField var hostText: String? = null
    @JvmField var firstPart: String? = null
    @JvmField var cssText: String? = null
    @JvmField var cssJs: String? = null
    @JvmField var areaSelText: String? = null
    @JvmField var restParts: MutableList<Any?>? = null
    @JvmField var sectionList: ArrayList<ArrayList<Any?>?>? = null
    @JvmField var resultItems: ArrayList<Any?>? = null
    @JvmField var hintItems: ArrayList<Any?>? = null
    /** Help hint dot (one-time `mNotiArea`). */
    @JvmField var notiShown: Boolean = false
    @JvmField var building: Boolean = false
    @JvmField var savedText: String? = savedText

    private var selText by mutableStateOf<String?>(null)
    private var version by mutableIntStateOf(0)
    private var busy by mutableStateOf(false)
    /** Item index that carries the one-time "tap to edit" dot (`mNotiAre3`), -1 = none. */
    private var hintIndex by mutableIntStateOf(-1)

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    init {
        runAsync(RestoreTextTask())
    }

    private fun hintOf(list: ArrayList<Any?>?): Int {
        if (!ReadSettings.notiAre3.value || list == null) return -1
        for (o in list) { val it = o as AreaItem; if (it.level.toInt() == 1) return it.cmdId }
        for (o in list) { val it = o as AreaItem; if (it.level.toInt() == 0) return it.cmdId }
        return -1
    }

    private fun build() {
        if (appContext == null) return
        selText = areaSelText
        if (ReadSettings.notiArea.value) notiShown = true
        hintIndex = hintOf(hintItems)
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    IconButton(onClick = { help() }) {
                        Icon(painterResource(AppR.drawable.outline_help_black_24), null)
                    }
                    if (notiShown && version >= 0) {
                        Box(
                            Modifier.padding(start = 30.dp, top = 10.dp).size(8.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { reset() }) {
                    Icon(painterResource(AppR.drawable.outline_replay_black_20), stringResource(AppR.string.reset))
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Text(
                    selText ?: "", style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(horizontal = 24.dp, vertical = 16.dp),
                )
                val versionNow = version
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        hintItems?.forEach { any ->
                            val item = any as AreaItem
                            if (item.level.toInt() == 2) {
                                Spacer(Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceContainerLow))
                            } else {
                                AreaRow(item, versionNow)
                            }
                        }
                    }
                }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { preview() }, enabled = !busy) { Text(stringResource(AppR.string.preview)) }
                TextButton(onClick = { remove() }, enabled = !busy) { Text(stringResource(AppR.string.remove_title)) }
            }
        }
        show()
    }

    @androidx.compose.runtime.Composable
    private fun AreaRow(item: AreaItem, checkedIndex: Int) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f).heightIn(min = 48.dp).clickable { edit(item) }
                    .padding(start = if (item.level.toInt() == 0) 12.dp else 24.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hintIndex == item.cmdId) {
                    Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                    Spacer(Modifier.size(6.dp))
                }
                if (item.level.toInt() == 0) {
                    Text(
                        "TAG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                }
                Text(
                    item.sel ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Checkbox(checked = checkedIndex >= 0 && item.isChecked, onCheckedChange = { toggle(item) })
        }
    }

    private fun toggle(item: AreaItem) {
        if (building) return
        item.isChecked = !item.isChecked
        version++
        rebuild()
    }

    /** Tap a part's text: edit it . */
    private fun edit(item: AreaItem) {
        if (building) return
        if (ReadSettings.notiAre3.value && hintIndex == item.cmdId) {
            ReadSettings.edit(appContext) { set(ReadSettings.notiAre3, false) }
            hintIndex = -1
        }
        if (hostActivity == null || anyDialogOpen()) return
        cmdDialog?.dismiss()
        val dialogEditCmd = DialogEditCmd(hostActivity, item.cmdId.toLong(), item.sel, true, object : EditUrlListener {
            override fun onSaved(id: Long, path: String?, extra: String?) {
                val it = hintItems?.getOrNull(id.toInt()) as AreaItem?
                if (it != null) {
                    it.sel = path
                    version++
                }
                rebuild()
            }
        })
        cmdDialog = dialogEditCmd
        dialogEditCmd.setOnDismissListener {
            cmdDialog?.dismiss()
            cmdDialog = null
            if (notiShown && !ReadSettings.notiArea.value) {
                notiShown = false
                version++
            }
        }
    }

    private fun help() {
        if (ReadSettings.notiArea.value) {
            ReadSettings.edit(appContext) { set(ReadSettings.notiArea, false) }
        }
        if (notiShown) {
            notiShown = false
            version++
        }
        if (hostActivity == null || anyDialogOpen()) return
        guideDialog?.dismiss()
        val dialogGuideArea = DialogGuideArea(hostActivity, 2, null)
        guideDialog = dialogGuideArea
        dialogGuideArea.setOnDismissListener { guideDialog?.dismiss(); guideDialog = null }
    }

    private fun reset() {
        if (building || hostActivity == null || anyDialogOpen()) return
        msgDialog?.dismiss()
        val dialogSetMsg = DialogSetMsg(hostActivity, AppR.string.reset_confirm, AppR.string.reset, object : DialogApplyListener {
            override fun onApply() {
                msgDialog?.dismiss()
                msgDialog = null
                val list = resultItems
                if (list == null || list.isEmpty()) return
                hintItems = filterItems(list)
                if (ReadSettings.notiAre3.value) hintIndex = hintOf(hintItems)
                version++
                rebuild()
            }
        })
        msgDialog = dialogSetMsg
        dialogSetMsg.setOnDismissListener { msgDialog?.dismiss(); msgDialog = null }
    }

    private fun remove() {
        if (building || busy) return
        if (TextUtils.isEmpty(areaSelText)) {
            showToast(appContext, AppR.string.backup_target)
            return
        }
        busy = true
        runAsync(ApplyTask())
    }

    private fun preview() {
        if (building || hostActivity == null || anyDialogOpen()) return
        val url = pageUrl
        val areaText = hostText
        previewDialog?.dismiss()
        val dialogWebView = DialogWebView(hostActivity, url, url, false, 4, null)
        previewDialog = dialogWebView
        dialogWebView.setOnDismissListener { previewDialog?.dismiss(); previewDialog = null }
        if (confirmFirst) dialogWebView.confirmPending = true
        if (!TextUtils.isEmpty(areaText)) {
            dialogWebView.areaText = areaText
            dialogWebView.areaPreviewJs = buildCssJs(cssText, true)
        }
    }

    internal inner class RestoreTextTask : Runnable {
        override fun run() {
            val d = this@DialogEditArea
            if (d.appContext == null) {
                return
            }
            val saved = d.savedText
            d.savedText = null
            var parts: MutableList<Any?>? = null
            if (!TextUtils.isEmpty(saved)) {
                val split: Array<String?> =
                    saved!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (s in split) {
                    if (!TextUtils.isEmpty(s)) {
                        if (parts == null) {
                            parts = ArrayList<Any?>()
                        }
                        parts.add(s)
                    }
                }
            }
            if (parts != null && !parts.isEmpty()) {
                val count = parts.size
                d.hostText = MainUtil.stripHostPrefix(d.hostUrl)
                d.firstPart = parts.get(0) as String?
                if (count > 1) {
                    d.restParts = parts.subList(1, count)
                }
                d.cssText = d.firstPart
                d.areaSelText = d.hostText + "##" + d.cssText
            }
            if (d.appContext != null && !TextUtils.isEmpty(d.firstPart)) {
                val secs: Array<String?> =
                    d.firstPart!!.split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (secs.size != 0) {
                    run {
                        var secIndex = 0
                        while (secIndex < secs.size && d.appContext != null) {
                            val sec: ArrayList<Any?>? = parseItems(secIndex, secs[secIndex])
                            if (sec == null || sec.isEmpty()) {
                                break
                            }
                            if (d.sectionList == null) {
                                d.sectionList = ArrayList()
                            }
                            d.sectionList!!.add(sec)
                            secIndex++
                        }
                    }
                } else {
                    val sec: ArrayList<Any?>? = parseItems(0, d.firstPart)
                    if (sec != null && !sec.isEmpty()) {
                        d.sectionList = ArrayList()
                        d.sectionList!!.add(sec)
                    }
                }
            }
            if (d.appContext != null && d.sectionList != null && !d.sectionList!!.isEmpty()) {
                val sections = d.sectionList
                val count = sections!!.size
                var result: ArrayList<Any?>? = null
                var aborted = false
                for (secIndex in 0..<count) {
                    if (d.appContext == null) {
                        aborted = true
                        break
                    }
                    val list = sections.get(secIndex)
                    var tag: String? = null
                    if (d.restParts != null && secIndex < d.restParts!!.size) {
                        tag = d.restParts!!.get(secIndex) as String?
                        if (TextUtils.isEmpty(tag) || "null" == tag) {
                            tag = null
                        }
                    }
                    if (list == null || list.isEmpty()) {
                        break
                    }
                    addTagged(list, tag, secIndex)
                    val first = list.get(0) as AreaItem?
                    if (first != null) {
                        first.topPad = MainApp.dp8
                    }
                    val last = list.get(list.size - 1) as AreaItem?
                    if (last != null) {
                        last.botPad = MainApp.dp8
                    }
                    if (count >= 2) {
                        if (secIndex == 0) {
                            if (last != null) {
                                last.corner = 2
                            }
                        } else {
                            if (secIndex == count - 1) {
                                if (first != null) {
                                    first.corner = 1
                                }
                            } else if (list.size == 1) {
                                if (first != null) {
                                    first.corner = 3
                                }
                            } else {
                                if (first != null) {
                                    first.corner = 1
                                }
                                if (last != null) {
                                    last.corner = 2
                                }
                            }
                            val header = AreaItem()
                            header.level = 2
                            header.section = secIndex
                            list.add(0, header)
                        }
                    }
                    if (result == null) {
                        result = ArrayList<Any?>()
                    }
                    result.addAll(list)
                }
                if (!aborted && result != null && !result.isEmpty()) {
                    val size = result.size
                    for (secIndex in 0..<size) {
                        (result.get(secIndex) as AreaItem).cmdId = secIndex
                    }
                    d.resultItems = result
                    d.hintItems = filterItems(result)
                }
            }
            val handler = d.mainHandler
            if (handler == null) {
                return
            }
            handler.post { build() }
        }
        private fun addTagged(list: MutableList<Any?>?, tag: String?, section: Int) {
            if (TextUtils.isEmpty(tag)) {
                return
            }
            var start = tag!!.indexOf('[')
            while (start != -1) {
                var end = tag.indexOf(']', start + 1)
                if (end == -1) {
                    return
                }
                end++
                val sel = tag.substring(start, end)
                if (TextUtils.isEmpty(sel)) {
                    return
                }
                val key: String? = ruleKey(sel)
                if (!TextUtils.isEmpty(key)) {
                    var exists = false
                    for (o in list!!) {
                        if (key == (o as AreaItem).key) {
                            exists = true
                            break
                        }
                    }
                    if (!exists) {
                        val item = AreaItem()
                        item.level = 1
                        item.section = section
                        item.key = key
                        item.sel = sel
                        list.add(item)
                    }
                }
                start = tag.indexOf('[', end)
            }
        }
    }

    internal inner class ApplyTask : Runnable {
        internal inner class DismissStep : Runnable {
            override fun run() {
                this@DialogEditArea.dismiss()
            }
        }
        internal inner class NotifyApplyStep : Runnable {
            override fun run() {
                val dialogEditArea = this@DialogEditArea
                val editAreaListener = dialogEditArea.areaListener
                if (editAreaListener == null) {
                    return
                }
                editAreaListener.onApply(dialogEditArea.hostText, dialogEditArea.cssText, dialogEditArea.cssJs)
                dialogEditArea.dismiss()
            }
        }

        /**
         * Saves the user filter text for the picked selector as a new DbBookUser row; when that worked, (re)adds the user-filter
         * entry to the filter list. Then continues on the UI thread: DismissStep for a normal filter, or
         * NotifyApplyStep after preparing the CSS when the selector is a special case `WebClean.isValidSelector` detects.
         */
        override fun run() {
            val dialog = this@DialogEditArea
            val context = dialog.appContext
            if (context == null) {
                return
            }
            val hostText = dialog.hostText
            val path = dialog.areaSelText
            var id: Long = 0
            if (!TextUtils.isEmpty(path)) {
                val db = DbBookUser.get(context).getWritableDatabase()
                val state = existsById(db, "DbBookUser_table", 0L)
                var row: Long = 0
                if (state != 0) {
                    val values = android.content.ContentValues().also { cv -> cv.put("_rsv1", hostText); cv.put("_path", path) }
                    if (state == 1) {
                        updateById(db, "DbBookUser_table", values, 0L)
                    } else {
                        values.put("_time", System.currentTimeMillis())
                        values.put("_use", 1)
                        row = insert(db, "DbBookUser_table", values)
                    }
                }
                if (row != 0L) {
                    var cursor: Cursor? = null
                    try {
                        cursor = queryById(db, "DbBookUser_table", null, row)
                        if (cursor != null && cursor.moveToFirst()) {
                            id = cursor.getLong(cursor.getColumnIndex("_id"))
                        }
                    } catch (exception: Exception) {
                        id = 0
                    }
                    if (cursor != null) {
                        cursor.close()
                    }
                }
            }
            if (id > 0) {
                DbBookFilter.addPathItem(context, "sb_user_filter_path", "sb_user_filter_path")
            }
            if (!WebClean.isValidSelector(dialog.areaSelText)) {
                if (dialog.mainHandler != null) {
                    dialog.mainHandler.post(DismissStep())
                }
                return
            }
            dialog.cssJs = buildCssJs(dialog.cssText, false)
            if (dialog.mainHandler != null) {
                dialog.mainHandler.post(NotifyApplyStep())
            }
        }
    }

    internal inner class LoadSelectionTask : Runnable {
        internal inner class ApplySelectionStep : Runnable {
            override fun run() {
                val dialogEditArea = this@DialogEditArea
                if (dialogEditArea.appContext == null) {
                    return
                }
                dialogEditArea.selText = dialogEditArea.areaSelText
                dialogEditArea.building = false
            }
        }
        override fun run() {
            val dialogEditArea = this@DialogEditArea
            if (dialogEditArea.appContext != null) {
                var sb: StringBuilder? = null
                dialogEditArea.cssText = null
                dialogEditArea.areaSelText = null
                val arrayList = dialogEditArea.hintItems
                if (arrayList != null && !arrayList.isEmpty()) {
                    val size = arrayList.size
                    var index = 0
                    var lastSection = -1
                    while (index < size) {
                        val rawAreaItem: Any? = arrayList.get(index)
                        index++
                        val areaItem = rawAreaItem as AreaItem
                        if (areaItem.level.toInt() != 2 && areaItem.isChecked && !TextUtils.isEmpty(areaItem.sel)) {
                            val section = areaItem.section
                            if (lastSection == -1) {
                                lastSection = section
                            } else if (lastSection != section) {
                                if (sb != null) {
                                    sb.append(">")
                                }
                                lastSection = section
                            }
                            if (sb == null) {
                                sb = StringBuilder()
                            }
                            sb.append(areaItem.sel)
                        }
                    }
                    if (sb != null) {
                        dialogEditArea.cssText = sb.toString()
                        dialogEditArea.areaSelText = dialogEditArea.hostText + "##" + dialogEditArea.cssText
                    }
                }
            }
            val handler = dialogEditArea.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ApplySelectionStep())
        }
    }

    interface EditAreaListener {
        fun onApply(host: String?, cmd: String?, script: String?)
    }

    fun anyDialogOpen(): Boolean = !(guideDialog == null && previewDialog == null && msgDialog == null && cmdDialog == null)

    /** Rebuild the selector from the checked parts (background), then show it. */
    fun rebuild() {
        if (appContext == null || building) return
        building = true
        runAsync(LoadSelectionTask())
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        guideDialog?.dismiss(); guideDialog = null
        previewDialog?.dismiss(); previewDialog = null
        msgDialog?.dismiss(); msgDialog = null
        cmdDialog?.dismiss(); cmdDialog = null
        hostActivity = null
        appContext = null
        areaListener = null
        restParts = null
        sectionList = null
        resultItems = null
        hintItems = null
        super.dismiss()
    }

    companion object {
        @JvmStatic
        fun filterItems(arrayList: ArrayList<Any?>?): ArrayList<Any?>? {
            if (arrayList == null || arrayList.isEmpty()) {
                return null
            }
            val filtered: ArrayList<Any?> = ArrayList<Any?>()
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val rawAreaItem: Any? = arrayList.get(index)
                index++
                val areaItem = rawAreaItem as AreaItem
                val copy = AreaItem()
                copy.level = areaItem.level
                copy.section = areaItem.section
                copy.corner = areaItem.corner
                copy.topPad = areaItem.topPad
                copy.botPad = areaItem.botPad
                copy.cmdId = areaItem.cmdId
                copy.key = areaItem.key
                copy.sel = areaItem.sel
                copy.isChecked = areaItem.isChecked
                filtered.add(copy)
            }
            return filtered
        }

        @JvmStatic
        fun buildCssJs(cssText: String?, withCss: Boolean): String? {
            if (TextUtils.isEmpty(cssText)) {
                return null
            }
            val sb = StringBuilder("(function(){")
            if (!withCss) {
                sb.append("myClr();")
            }
            sb.append("if(document.head){var ele=document.createElement('style');ele.innerText='").append(cssText).append("{display:none !important;}';document.head.appendChild(ele);}{var eles=document.querySelectorAll('").append(cssText)
            sb.append("');if(eles&&(eles.length!=0)){for(var i=0;i<eles.length;i++){var ele=eles[i];var par=ele.parentNode;if(par){par.removeChild(ele);}else{ele.style.display='none';}}}}})();")
            return sb.toString()
        }

        @JvmStatic
        fun ruleKey(sel: String?): String? {
            var equalsIndex = if (TextUtils.isEmpty(sel)) 0 else sel!!.indexOf(61.toChar())
            if (TextUtils.isEmpty(sel) || equalsIndex == -1) {
                return null
            }
            val bracketEnd = equalsIndex - 1
            if (sel!!.startsWith("^", bracketEnd) || sel.startsWith("$", bracketEnd) || sel.startsWith("*", bracketEnd)) {
                equalsIndex--
            }
            return sel.substring(1, equalsIndex)
        }

        @JvmStatic
        fun parseItems(section: Int, text: String?): ArrayList<Any?>? {
            var areaItem: AreaItem?
            if (TextUtils.isEmpty(text)) {
                return null
            }
            val arrayList: ArrayList<Any?> = ArrayList<Any?>()
            var openIndex = text!!.indexOf(91.toChar())
            if (openIndex == -1) {
                val rootItem = AreaItem()
                rootItem.level = 0.toByte()
                rootItem.section = section
                rootItem.key = "tag"
                rootItem.sel = text
                rootItem.isChecked = true
                arrayList.add(rootItem)
                return arrayList
            }
            if (openIndex > 0) {
                val secondRoot = AreaItem()
                secondRoot.level = 0.toByte()
                secondRoot.section = section
                secondRoot.key = "tag"
                secondRoot.sel = text.substring(0, openIndex)
                secondRoot.isChecked = true
                arrayList.add(secondRoot)
            }
            while (openIndex != -1) {
                val closeIndex = text.indexOf(93.toChar(), openIndex + 1)
                if (closeIndex == -1) {
                    break
                }
                val itemStart = closeIndex + 1
                val item = text.substring(openIndex, itemStart)
                if (TextUtils.isEmpty(item)) {
                    break
                }
                val key: String? = ruleKey(item)
                if (TextUtils.isEmpty(key)) {
                    areaItem = null
                } else {
                    areaItem = AreaItem()
                    areaItem.level = 1.toByte()
                    areaItem.section = section
                    areaItem.key = key
                    areaItem.sel = item
                    areaItem.isChecked = true
                }
                if (areaItem != null) {
                    arrayList.add(areaItem)
                }
                openIndex = text.indexOf(91.toChar(), itemStart)
            }
            return arrayList
        }
    }
}
