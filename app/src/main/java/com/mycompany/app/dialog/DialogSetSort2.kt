package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.pref.PrefList
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons
import com.mycompany.app.ui.browser.SheetChoiceRow
import com.mycompany.app.ui.browser.SheetSwitchRow

/**
 * Sort options of the web bookmark list (`MainListView2`): user order (when on, the other options are off), folders
 * first, sort by (name / date, [DialogSetSort.sortsOther]) and ascending / descending; Apply stores them (`mBookWebUser`,
 * `mBookWebFtop`, `mBookWebItem`, `mBookWebRvse`) and calls [DialogApplyListener.onApply] when something changed. Compose
 * now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetSort2(mainActivity: MainActivity?, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField var appContext: Context? = context
    @JvmField var applyListener: DialogApplyListener? = dialogApplyListener

    private var userOrder by mutableStateOf(ListSettings.bookWebUser.value)
    private var folderTop by mutableStateOf(ListSettings.bookWebFtop.value)
    private var sortMode by mutableIntStateOf(ListSettings.bookWebItem.value % 5)
    private var isReversed by mutableStateOf(ListSettings.bookWebRvse.value)

    init {
        setSheet {
            SheetSwitchRow(stringResource(AppR.string.sort_user), userOrder, { userOrder = it })
            SheetSwitchRow(stringResource(AppR.string.folder_top), folderTop, { folderTop = it }, enabled = !userOrder)
            val keys = DialogSetSort.sortsOther
            SheetChoiceRow(
                stringResource(AppR.string.sort_by),
                listOf(DialogSetSort.sortLabels[keys[0]], DialogSetSort.sortLabels[keys[1]]),
                if (keys[0] == sortMode) 0 else if (keys[1] == sortMode) 1 else -1,
                { sortMode = keys[it % 2] },
                enabled = !userOrder,
                value = stringResource(DialogSetSort.sortLabels[sortMode]),
            )
            SheetChoiceRow(
                stringResource(AppR.string.order_by),
                listOf(AppR.string.order_ascend, AppR.string.order_descend),
                if (isReversed) 1 else 0,
                { isReversed = it != 0 },
                enabled = !userOrder,
            )
            SheetButtons({ apply() })
        }
        show()
    }

    /** Apply. */
    private fun apply() {
        if (ListSettings.bookWebUser.value != userOrder || ListSettings.bookWebFtop.value != folderTop || ListSettings.bookWebItem.value != sortMode || ListSettings.bookWebRvse.value != isReversed) {
            ListSettings.editLocal { set(ListSettings.bookWebUser, userOrder) }
            ListSettings.editLocal { set(ListSettings.bookWebFtop, folderTop) }
            ListSettings.editLocal { set(ListSettings.bookWebItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.bookWebRvse, isReversed) }
            val prefListR = PrefList.get(appContext, false)
            prefListR!!.putBool("mBookWebUser", ListSettings.bookWebUser.value)
            prefListR.putBool("mBookWebFtop", ListSettings.bookWebFtop.value)
            prefListR.putInt(ListSettings.bookWebItem.value, "mBookWebItem")
            prefListR.putBool("mBookWebRvse", ListSettings.bookWebRvse.value)
            prefListR.saveAsync()
            applyListener?.onApply()
        }
        dismiss()
    }

    override fun dismiss() {
        appContext = null
        applyListener = null
        super.dismiss()
    }
}
