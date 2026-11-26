package com.mycompany.app.dialog

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainSelectAdapter
import com.mycompany.app.main.MainSelectAdapter.MainSelectItem
import com.mycompany.app.main.MainSelectAdapter.MainSelectListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference
import java.util.Collections
import com.mycompany.app.ui.components.Sym

/**
 * Picks a toolbar / menu button ([currentIdParam] = the current one, marked; [iArr] / [iArr2] = buttons already used, greyed out
 * and refused with "Already added"): a search field (name contains, plus initial-consonant search where the locale
 * uses it, `MainUtil.isKoreanLocale`) over the list of all buttons with their icons, sorted by name, scrolled to the current one.
 * A pick goes to [MainSelectListener] and closes the sheet (the old sheet stayed open until Close). The list is built
 * and filtered by the kept [DialogTask] in the background. Compose now (on [ComposeBottomDialog]); same constructor,
 * shows itself.
 */
@Suppress("deprecation")
class DialogSetItem(
    activity: Activity?,
    currentIdParam: Int,
    iArr: IntArray?,
    iArr2: IntArray?,
    mainSelectListener: MainSelectListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var selectListener: MainSelectListener? = mainSelectListener
    @JvmField
    var usedIds: IntArray? = iArr
    @JvmField
    var extraIds: IntArray? = iArr2
    @JvmField
    var currentId: Int = currentIdParam
    @JvmField
    var koreanLocale: Boolean = MainUtil.isKoreanLocale()
    @JvmField
    var loadTask: DialogTask? = null
    @JvmField
    var sortedItems: ArrayList<MainSelectAdapter.MainSelectItem?>? = null

    private val shown = mutableStateListOf<MainSelectItem>()
    private var query by mutableStateOf("")
    private var scrollTo by mutableIntStateOf(-1)

    class DialogTask(dialogSetItem: DialogSetItem?, query: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: ArrayList<MainSelectAdapter.MainSelectItem?>? = null
        @JvmField
        var selectedPos: Int = 0
        @JvmField
        var query: String? = null

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogSetItem)
            this.dialogRef = weakReference
            if ((weakReference.get() as DialogSetItem?) == null) {
                return@init
            }
            this.selectedPos = -1
            this.query = query
        
        }
    }

        public override fun doInBackground() {
            var arrayList: ArrayList<Any?>?
            val dialogSetItem = this.dialogRef!!.get() as DialogSetItem?
            if (dialogSetItem == null || this.cancelled || dialogSetItem.appContext == null) {
                return
            }
            val sortedItems = dialogSetItem.sortedItems
            var index = 0
            if (sortedItems == null || sortedItems.isEmpty()) {
                val usedIds = dialogSetItem.usedIds
                if (usedIds == null || usedIds.size == 0) {
                    arrayList = null
                } else {
                    arrayList = ArrayList<Any?>()
                    for (index3 in dialogSetItem.usedIds!!) {
                        arrayList.add(index3)
                    }
                }
                val extraIds = dialogSetItem.extraIds
                if (extraIds != null && extraIds.size != 0) {
                    if (arrayList == null) {
                        arrayList = ArrayList<Any?>()
                    }
                    for (index4 in dialogSetItem.extraIds!!) {
                        arrayList.add(index4)
                    }
                }
                val tint = MainUtil.barTint(0, false)
                val newSortedItems: ArrayList<MainSelectAdapter.MainSelectItem?> = ArrayList()
                for (id in 3..73) {
                    if (id != 68) {
                        val mainSelectItem = MainSelectItem()
                        mainSelectItem.id = id
                        mainSelectItem.iconRes = MainUtil.menuIconDarkAt(id, tint)
                        val matchText = dialogSetItem.appContext!!.getString(MainUtil.menuIconAt(id))
                        mainSelectItem.matchText = matchText
                        mainSelectItem.lowerName = matchText.lowercase()
                        if (mainSelectItem.id == dialogSetItem.currentId) {
                            mainSelectItem.isChecked = true
                        } else if (arrayList != null && arrayList.contains(id)) {
                            mainSelectItem.isDisabled = true
                        }
                        newSortedItems.add(mainSelectItem)
                    }
                }
                if (!newSortedItems.isEmpty()) {
                    try {
                        Collections.sort<MainSelectItem?>(newSortedItems, SortItem())
                    } catch (unused: Exception) {
                    }
                    val size = newSortedItems.size
                    var selectedPos = 0
                    var itemIndex = 0
                    while (itemIndex < size) {
                        val entry: Any? = newSortedItems.get(itemIndex)
                        itemIndex++
                        val selectItem = entry as MainSelectItem?
                        if (selectItem != null) {
                            if (selectItem.isChecked) {
                                this.selectedPos = selectedPos
                                break
                            }
                            selectedPos++
                        }
                    }
                }
                dialogSetItem.sortedItems = newSortedItems
                if (newSortedItems.isEmpty()) {
                    return
                }
            }
            val items = dialogSetItem.sortedItems!!
            if (items.isEmpty()) {
                return
            }
            if (TextUtils.isEmpty(this.query)) {
                this.items = items
                return
            }
            this.query = this.query!!.lowercase()
            val koreanLocale = dialogSetItem.koreanLocale
            this.items = ArrayList()
            val size = items.size
            while (index < size) {
                val otherEntry: Any? = items.get(index)
                index++
                val otherSelectItem = otherEntry as MainSelectItem?
                if (otherSelectItem != null) {
                    val query = this.query
                    if (koreanLocale) {
                        val initialHit = initialMatch(otherSelectItem.matchText, otherSelectItem.lowerName, query)
                        if (!TextUtils.isEmpty(initialHit)) {
                            this.items!!.add(otherSelectItem)
                        }
                    }
                    if (otherSelectItem.lowerName!!.contains(query!!)) {
                        this.items!!.add(otherSelectItem)
                    }
                }
            }
        }

        public override fun onCancelled() {
            val dialogSetItem = this.dialogRef!!.get() as DialogSetItem?
            if (dialogSetItem == null) {
                return
            }
            dialogSetItem.loadTask = null
        }

        public override fun onPostExecute() {
            val dialogSetItem = this.dialogRef!!.get() as DialogSetItem?
            if (dialogSetItem != null) {
                dialogSetItem.loadTask = null
                dialogSetItem.showItems(this.items, this.selectedPos)
            }
        }
    }

    private class SortItem : Comparator<MainSelectItem?> {
        override fun compare(
            mainSelectItem: MainSelectItem?,
            other: MainSelectItem?
        ): Int {
            val left = mainSelectItem
            val right = other
            if (left == null) {
                return if (right == null) 0 else 1
            }
            if (right == null) {
                return -1
            }
            return MainUtil.compareStringsIgnoreCase(left.lowerName, right.lowerName, false)
        }
    }

    init {
        setSheet {
            val listState = rememberLazyListState()
            OutlinedTextField(
                value = query,
                onValueChange = { onQuery(it) },
                singleLine = true,
                placeholder = { Text(stringResource(AppR.string.search_url)) },
                leadingIcon = { Icon(Sym.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQuery("") }) { Icon(Sym.Close, contentDescription = null) }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { }),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            )
            LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 520.dp), state = listState) {
                itemsIndexed(shown, key = { _, item -> item.id }) { _, item ->
                    val current = item.id == currentId
                    ListItem(
                        leadingContent = {
                            if (item.iconRes != 0) Icon(painterResource(item.iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
                        },
                        headlineContent = {
                            Text(
                                item.matchText ?: "",
                                color = when {
                                    current -> MaterialTheme.colorScheme.primary
                                    item.isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        trailingContent = { if (current) Icon(Sym.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable { pick(item) },
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { dismiss() }) { Text(stringResource(AppR.string.close)) }
            }
            LaunchedEffect(scrollTo) {
                if (scrollTo > 0) {
                    listState.scrollToItem(scrollTo)
                    scrollTo = -1
                }
            }
        }
        show()
        startTask(null)
    }

    private fun onQuery(newQuery: String) {
        query = newQuery
        val trimmed = newQuery.trim { it <= ' ' }
        startTask(if (TextUtils.isEmpty(trimmed)) null else trimmed)
    }

    private fun startTask(query: String?) {
        loadTask?.cancelled = true
        loadTask = null
        val task = DialogTask(this, query)
        loadTask = task
        task.start(appContext)
    }

    /** Result of [DialogTask]: the (filtered) list and, on the first build, the index of the current item. */
    internal fun showItems(list: ArrayList<MainSelectAdapter.MainSelectItem?>?, index: Int) {
        shown.clear()
        list?.forEach { it?.let { item -> shown.add(item) } }
        if (index >= 1) scrollTo = index
    }

    private fun pick(item: MainSelectItem) {
        if (item.isDisabled) {
            showToast(context, AppR.string.already_added)
            return
        }
        selectListener?.onPick(item.id)
        dismiss()
    }

    override fun dismiss() {
        loadTask?.cancelled = true
        loadTask = null
        selectListener = null
        super.dismiss()
    }

    companion object {
    }
}
