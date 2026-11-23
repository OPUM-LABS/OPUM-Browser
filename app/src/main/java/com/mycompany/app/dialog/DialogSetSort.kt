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
import com.mycompany.app.pref.PrefUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons
import com.mycompany.app.ui.browser.SheetChoiceRow

/**
 * Sort options of a list screen ([sortTypeParam] = the list type: files, albums, cast, bookmark lists, filters, scripts, downloads,
 * Google Drive, ...): sort by (the type's choices from [sortLabels]), ascending / descending, and for most types group by
 * ([groupLabels]). Apply stores them for that type and calls [DialogApplyListener.onApply].
 * Compose now (on [ComposeBottomDialog]); same constructor, shows itself. The companion tables stay (also used by
 * [DialogSetSort2]).
 */
@Suppress("deprecation")
class DialogSetSort(mainActivity: android.app.Activity?, sortTypeParam: Int, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField var appContext: Context? = context
    @JvmField var sortType: Int = sortTypeParam
    @JvmField var applyListener: DialogApplyListener? = dialogApplyListener

    private var groupMode by mutableIntStateOf(PrefUtil.foldMode(sortTypeParam) % 8)
    private var sortMode by mutableIntStateOf(PrefUtil.itemMode(sortTypeParam) % 5)
    private var isReversed by mutableStateOf(PrefUtil.reversed(sortTypeParam))

    init {
        setSheet {
            val t = sortType
            val sorts = when (t) {
                0 -> sorts0
                1, 2 -> sorts12
                3, 13 -> sorts3
                24 -> sorts24
                else -> sortsOther
            }
            SheetChoiceRow(
                stringResource(AppR.string.sort_by), sorts.map { sortLabels[it] }, sorts.indexOf(sortMode),
                { sortMode = sorts[it % sorts.size] }, value = stringResource(sortLabels[sortMode]),
            )
            SheetChoiceRow(
                stringResource(AppR.string.order_by), listOf(AppR.string.order_ascend, AppR.string.order_descend),
                if (isReversed) 1 else 0, { isReversed = it != 0 },
            )
            if (t != 0 && t != 13 && t != 24 && t != 26 && t != 42) {
                val groups = when (t) {
                    1, 2 -> groups12
                    3 -> groups3
                    14, 15, 16 -> groups14
                    31 -> groups31
                    19, 20, 21, 22, 23, 25, 27, 28, 29 -> groupsList
                    else -> groupsOther
                }
                SheetChoiceRow(
                    stringResource(AppR.string.group_by), groups.map { groupLabels[it] }, groups.indexOf(groupMode),
                    { groupMode = groups[it % groups.size] }, value = stringResource(groupLabels[groupMode]),
                )
            }
            SheetButtons({ apply() })
        }
        show()
    }

    /** Apply. */
private fun apply() {
        val dialogSetSort = this
        val context = dialogSetSort.appContext
        val sortType = dialogSetSort.sortType
        val groupMode = dialogSetSort.groupMode
        val sortMode = dialogSetSort.sortMode
        val isReversed = dialogSetSort.isReversed
        if (sortType == 0) {
            ListSettings.editLocal { set(ListSettings.fileItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.fileRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.fileItem.value, "mFileItem")
            prefListR.putBool("mFileRvse", ListSettings.fileRvse.value)
            prefListR.saveAsync()
        } else if (sortType == 1) {
            ListSettings.editLocal { set(ListSettings.albumFold, groupMode) }
            ListSettings.editLocal { set(ListSettings.albumItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.albumRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.albumFold.value, "mAlbumFold2")
            prefListR.putInt(ListSettings.albumItem.value, "mAlbumItem")
            prefListR.putBool("mAlbumRvse", ListSettings.albumRvse.value)
            prefListR.saveAsync()
        } else if (sortType == 2) {
            ListSettings.editLocal { set(ListSettings.albumFold, groupMode) }
            ListSettings.editLocal { set(ListSettings.albumItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.albumRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.albumFold.value, "mAlbumFold2")
            prefListR.putInt(ListSettings.albumItem.value, "mAlbumItem")
            prefListR.putBool("mAlbumRvse", ListSettings.albumRvse.value)
            prefListR.saveAsync()
        } else if (sortType == 3) {
            ListSettings.editLocal { set(ListSettings.albumFold, groupMode) }
            ListSettings.editLocal { set(ListSettings.albumItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.albumRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.albumFold.value, "mAlbumFold2")
            prefListR.putInt(ListSettings.albumItem.value, "mAlbumItem")
            prefListR.putBool("mAlbumRvse", ListSettings.albumRvse.value)
            prefListR.saveAsync()
        } else if (sortType == 13) {
            ListSettings.editLocal { set(ListSettings.castItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.castRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.castItem.value, "mCastItem")
            prefListR.putBool("mCastRvse", ListSettings.castRvse.value)
            prefListR.saveAsync()
        } else if (sortType == 14) {
            ListSettings.editLocal { set(ListSettings.bookAlbumFold, groupMode) }
            ListSettings.editLocal { set(ListSettings.bookAlbumItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.bookAlbumRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.bookAlbumFold.value, "mBookAlbumFold2")
            prefListR.putInt(ListSettings.bookAlbumItem.value, "mBookAlbumItem")
            prefListR.putBool("mBookAlbumRvse", ListSettings.bookAlbumRvse.value)
            prefListR.saveAsync()
        } else if (sortType == 15) {
            ListSettings.editLocal { set(ListSettings.bookAlbumFold, groupMode) }
            ListSettings.editLocal { set(ListSettings.bookAlbumItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.bookAlbumRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.bookAlbumFold.value, "mBookAlbumFold2")
            prefListR.putInt(ListSettings.bookAlbumItem.value, "mBookAlbumItem")
            prefListR.putBool("mBookAlbumRvse", ListSettings.bookAlbumRvse.value)
            prefListR.saveAsync()
        } else if (sortType == 16) {
            ListSettings.editLocal { set(ListSettings.bookAlbumFold, groupMode) }
            ListSettings.editLocal { set(ListSettings.bookAlbumItem, sortMode) }
            ListSettings.editLocal { set(ListSettings.bookAlbumRvse, isReversed) }
            val prefListR = PrefList.get(context, false)
            prefListR!!.putInt(ListSettings.bookAlbumFold.value, "mBookAlbumFold2")
            prefListR.putInt(ListSettings.bookAlbumItem.value, "mBookAlbumItem")
            prefListR.putBool("mBookAlbumRvse", ListSettings.bookAlbumRvse.value)
            prefListR.saveAsync()
        } else if (sortType != 17) {
            if (sortType == 18) {
                ListSettings.editLocal { set(ListSettings.bookHistFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookHistItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookHistRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookHistFold.value, "mBookHistFold2")
                prefListR.putInt(ListSettings.bookHistItem.value, "mBookHistItem")
                prefListR.putBool("mBookHistRvse", ListSettings.bookHistRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 19) {
                ListSettings.editLocal { set(ListSettings.bookAdsFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookAdsItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookAdsRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookAdsFold.value, "mBookAdsFold")
                prefListR.putInt(ListSettings.bookAdsItem.value, "mBookAdsItem")
                prefListR.putBool("mBookAdsRvse", ListSettings.bookAdsRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 20) {
                ListSettings.editLocal { set(ListSettings.bookOverFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookOverItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookOverRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookOverFold.value, "mBookOverFold")
                prefListR.putInt(ListSettings.bookOverItem.value, "mBookOverItem")
                prefListR.putBool("mBookOverRvse", ListSettings.bookOverRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 21) {
                ListSettings.editLocal { set(ListSettings.bookPopFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookPopItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookPopRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookPopFold.value, "mBookPopFold")
                prefListR.putInt(ListSettings.bookPopItem.value, "mBookPopItem")
                prefListR.putBool("mBookPopRvse", ListSettings.bookPopRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 22) {
                ListSettings.editLocal { set(ListSettings.bookLinkFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookLinkItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookLinkRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookLinkFold.value, "mBookLinkFold")
                prefListR.putInt(ListSettings.bookLinkItem.value, "mBookLinkItem")
                prefListR.putBool("mBookLinkRvse", ListSettings.bookLinkRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 23) {
                ListSettings.editLocal { set(ListSettings.bookBlockFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookBlockItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookBlockRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookBlockFold.value, "mBookBlockFold")
                prefListR.putInt(ListSettings.bookBlockItem.value, "mBookBlockItem")
                prefListR.putBool("mBookBlockRvse", ListSettings.bookBlockRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 24) {
                ListSettings.editLocal { set(ListSettings.bookFilterItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookFilterRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookFilterItem.value, "mBookFilterItem")
                prefListR.putBool("mBookFilterRvse", ListSettings.bookFilterRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 25) {
                ListSettings.editLocal { set(ListSettings.bookUserFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookUserItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookUserRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookUserFold.value, "mBookUserFold")
                prefListR.putInt(ListSettings.bookUserItem.value, "mBookUserItem")
                prefListR.putBool("mBookUserRvse", ListSettings.bookUserRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 26) {
                ListSettings.editLocal { set(ListSettings.bookScrItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookScrRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookScrItem.value, "mBookScrItem")
                prefListR.putBool("mBookScrRvse", ListSettings.bookScrRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 27) {
                ListSettings.editLocal { set(ListSettings.bookJavaFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookJavaItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookJavaRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookJavaFold.value, "mBookJavaFold")
                prefListR.putInt(ListSettings.bookJavaItem.value, "mBookJavaItem")
                prefListR.putBool("mBookJavaRvse", ListSettings.bookJavaRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 28) {
                ListSettings.editLocal { set(ListSettings.bookTmemFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookTmemItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookTmemRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookTmemFold.value, "mBookTmemFold")
                prefListR.putInt(ListSettings.bookTmemItem.value, "mBookTmemItem")
                prefListR.putBool("mBookTmemRvse", ListSettings.bookTmemRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 29) {
                ListSettings.editLocal { set(ListSettings.bookTransFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookTransItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookTransRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookTransFold.value, "mBookTransFold")
                prefListR.putInt(ListSettings.bookTransItem.value, "mBookTransItem")
                prefListR.putBool("mBookTransRvse", ListSettings.bookTransRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 31) {
                ListSettings.editLocal { set(ListSettings.bookDownFold, groupMode) }
                ListSettings.editLocal { set(ListSettings.bookDownItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.bookDownRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.bookDownFold.value, "mBookDownFold2")
                prefListR.putInt(ListSettings.bookDownItem.value, "mBookDownItem")
                prefListR.putBool("mBookDownRvse", ListSettings.bookDownRvse.value)
                prefListR.saveAsync()
            } else if (sortType == 42) {
                ListSettings.editLocal { set(ListSettings.gdriveItem, sortMode) }
                ListSettings.editLocal { set(ListSettings.gdriveRvse, isReversed) }
                val prefListR = PrefList.get(context, false)
                prefListR!!.putInt(ListSettings.gdriveItem.value, "mGdriveItem")
                prefListR.putBool("mGdriveRvse", ListSettings.gdriveRvse.value)
                prefListR.saveAsync()
            }
        }
        val dialogApplyListener = dialogSetSort.applyListener
        if (dialogApplyListener != null) {
            dialogApplyListener.onApply()
        }
        dialogSetSort.dismiss()
    }

    override fun dismiss() {
        appContext = null
        applyListener = null
        super.dismiss()
    }

    companion object {
        @JvmField
        val groupLabels: IntArray
        @JvmField
        val groups12: IntArray
        @JvmField
        val groups3: IntArray
        @JvmField
        val groups14: IntArray
        @JvmField
        val groups31: IntArray
        @JvmField
        val groupsList: IntArray
        @JvmField
        val groupsOther: IntArray
        @JvmField
        val sortLabels: IntArray
        @JvmField
        val sorts0: IntArray
        @JvmField
        val sorts12: IntArray
        @JvmField
        val sorts3: IntArray
        @JvmField
        val sorts24: IntArray
        @JvmField
        val sortsOther: IntArray

        init {
            val i = AppR.string.not_used
            val i2 = AppR.string.folder_dir
            val i3 = AppR.string.file
            val i4 = AppR.string.sort_data
            val i5 = AppR.string.sort_ext
            val i6 = AppR.string.sort_time
            groupLabels = intArrayOf(i, i2, i3, i4, i5, i6, AppR.string.domain_name)
            groups12 = intArrayOf(1, 0)
            groups3 = intArrayOf(1, 4, 0)
            groups14 = intArrayOf(2, 0)
            groups31 = intArrayOf(1, 3, 0)
            groupsList = intArrayOf(6, 0)
            groupsOther = intArrayOf(5, 6, 0)
            sortLabels = intArrayOf(AppR.string.sort_name, i4, i5, i6, AppR.string.sort_size)
            sorts0 = intArrayOf(0, 1, 2, 3, 4)
            sorts12 = intArrayOf(0, 3, 4)
            sorts3 = intArrayOf(0, 2, 3, 4)
            sorts24 = intArrayOf(0, 1, 3)
            sortsOther = intArrayOf(0, 3)
        }
    }
}
