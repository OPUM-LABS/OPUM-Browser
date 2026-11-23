package com.mycompany.app.pref

import android.content.Context
import com.mycompany.app.pref.store.ListSettings

@Suppress("deprecation")
object PrefUtil {
    @JvmStatic
    fun showDetail(listType: Int): Boolean {
        if (listType == 0) {
            return ListSettings.fileDetail.value
        }
        if (listType == 1) {
            return ListSettings.albumDetail.value
        }
        if (listType == 2) {
            return ListSettings.albumDetail.value
        }
        if (listType == 3) {
            return ListSettings.albumDetail.value
        }
        if (listType == 13) {
            return ListSettings.castDetail.value
        }
        if (listType == 14) {
            return ListSettings.bookAlbumDetail.value
        }
        if (listType == 15) {
            return ListSettings.bookAlbumDetail.value
        }
        if (listType == 16) {
            return ListSettings.bookAlbumDetail.value
        }
        if (listType == 17) {
            return ListSettings.bookWebDetail.value
        }
        if (listType == 18) {
            return ListSettings.bookHistDetail.value
        }
        if (listType == 19) {
            return ListSettings.bookAdsDetail.value
        }
        if (listType == 20) {
            return ListSettings.bookOverDetail.value
        }
        if (listType == 21) {
            return ListSettings.bookPopDetail.value
        }
        if (listType == 22) {
            return ListSettings.bookLinkDetail.value
        }
        if (listType == 23) {
            return ListSettings.bookBlockDetail.value
        }
        if (listType == 24) {
            return ListSettings.bookFilterDetail.value
        }
        if (listType == 25) {
            return false
        }
        if (listType == 26) {
            return ListSettings.bookScrDetail.value
        }
        if (listType == 27) {
            return ListSettings.bookJavaDetail.value
        }
        if (listType == 28) {
            return ListSettings.bookTmemDetail.value
        }
        if (listType == 29) {
            return ListSettings.bookTransDetail.value
        }
        if (listType == 31) {
            return ListSettings.bookDownDetail.value
        }
        if (listType == 34 || listType == 35) {
            return false
        }
        if (listType == 36) {
            return ListSettings.bookMemoDetail.value
        }
        if (listType == 42) {
            return ListSettings.gdriveDetail.value
        }
        return true
    }

    @JvmStatic
    fun showRecent(listType: Int): Boolean {
        if (listType == 1) {
            return ListSettings.albumRecent.value
        }
        if (listType == 2) {
            return ListSettings.albumRecent.value
        }
        if (listType == 3) {
            return ListSettings.albumRecent.value
        }
        return listType == 31 || listType == 34 || listType == 35 || listType == 41
    }

    @JvmStatic
    fun singleLine(listType: Int): Boolean {
        if (listType == 0) {
            return ListSettings.fileSingle.value
        }
        if (listType == 1) {
            return ListSettings.albumSingle.value
        }
        if (listType == 2) {
            return ListSettings.albumSingle.value
        }
        if (listType == 3) {
            return ListSettings.albumSingle.value
        }
        if (listType == 13) {
            return ListSettings.castSingle.value
        }
        if (listType == 14) {
            return ListSettings.bookAlbumSingle.value
        }
        if (listType == 15) {
            return ListSettings.bookAlbumSingle.value
        }
        if (listType == 16) {
            return ListSettings.bookAlbumSingle.value
        }
        if (listType == 17) {
            return ListSettings.bookWebSingle.value
        }
        if (listType == 18) {
            return ListSettings.bookHistSingle.value
        }
        if (listType == 19) {
            return ListSettings.bookAdsSingle.value
        }
        if (listType == 20) {
            return ListSettings.bookOverSingle.value
        }
        if (listType == 21) {
            return ListSettings.bookPopSingle.value
        }
        if (listType == 22) {
            return ListSettings.bookLinkSingle.value
        }
        if (listType == 23) {
            return ListSettings.bookBlockSingle.value
        }
        if (listType == 24) {
            return ListSettings.bookFilterSingle.value
        }
        if (listType == 25) {
            return true
        }
        if (listType == 26) {
            return ListSettings.bookScrSingle.value
        }
        if (listType == 27) {
            return ListSettings.bookJavaSingle.value
        }
        if (listType == 28) {
            return ListSettings.bookTmemSingle.value
        }
        if (listType == 29) {
            return ListSettings.bookTransSingle.value
        }
        if (listType == 31) {
            return ListSettings.bookDownSingle.value
        }
        if (listType == 34 || listType == 35) {
            return true
        }
        if (listType == 36) {
            return ListSettings.bookMemoSingle.value
        }
        if (listType == 42) {
            return ListSettings.gdriveSingle.value
        }
        return false
    }

    @JvmStatic
    fun foldMode(listType: Int): Int {
        if (listType == 1) {
            return ListSettings.albumFold.value
        }
        if (listType == 2) {
            return ListSettings.albumFold.value
        }
        if (listType == 3) {
            return ListSettings.albumFold.value
        }
        if (listType == 13) {
            return 0
        }
        if (listType == 14) {
            return ListSettings.bookAlbumFold.value
        }
        if (listType == 15) {
            return ListSettings.bookAlbumFold.value
        }
        if (listType == 16) {
            return ListSettings.bookAlbumFold.value
        }
        if (listType == 17) {
            return 0
        }
        if (listType == 18) {
            return ListSettings.bookHistFold.value
        }
        if (listType == 19) {
            return ListSettings.bookAdsFold.value
        }
        if (listType == 20) {
            return ListSettings.bookOverFold.value
        }
        if (listType == 21) {
            return ListSettings.bookPopFold.value
        }
        if (listType == 22) {
            return ListSettings.bookLinkFold.value
        }
        if (listType == 23) {
            return ListSettings.bookBlockFold.value
        }
        if (listType == 24) {
            return 0
        }
        if (listType == 25) {
            return ListSettings.bookUserFold.value
        }
        if (listType == 26) {
            return 0
        }
        if (listType == 27) {
            return ListSettings.bookJavaFold.value
        }
        if (listType == 28) {
            return ListSettings.bookTmemFold.value
        }
        if (listType == 29) {
            return ListSettings.bookTransFold.value
        }
        if (listType == 31) {
            return ListSettings.bookDownFold.value
        }
        return 0
    }

    @JvmStatic
    fun itemMode(listType: Int): Int {
        if (listType == 0) {
            return ListSettings.fileItem.value
        }
        if (listType == 1) {
            return ListSettings.albumItem.value
        }
        if (listType == 2) {
            return ListSettings.albumItem.value
        }
        if (listType == 3) {
            return ListSettings.albumItem.value
        }
        if (listType == 13) {
            return ListSettings.castItem.value
        }
        if (listType == 14) {
            return ListSettings.bookAlbumItem.value
        }
        if (listType == 15) {
            return ListSettings.bookAlbumItem.value
        }
        if (listType == 16) {
            return ListSettings.bookAlbumItem.value
        }
        if (listType == 17) {
            return 3
        }
        if (listType == 18) {
            return ListSettings.bookHistItem.value
        }
        if (listType == 19) {
            return ListSettings.bookAdsItem.value
        }
        if (listType == 20) {
            return ListSettings.bookOverItem.value
        }
        if (listType == 21) {
            return ListSettings.bookPopItem.value
        }
        if (listType == 22) {
            return ListSettings.bookLinkItem.value
        }
        if (listType == 23) {
            return ListSettings.bookBlockItem.value
        }
        if (listType == 24) {
            return ListSettings.bookFilterItem.value
        }
        if (listType == 25) {
            return ListSettings.bookUserItem.value
        }
        if (listType == 26) {
            return ListSettings.bookScrItem.value
        }
        if (listType == 27) {
            return ListSettings.bookJavaItem.value
        }
        if (listType == 28) {
            return ListSettings.bookTmemItem.value
        }
        if (listType == 29) {
            return ListSettings.bookTransItem.value
        }
        if (listType == 31) {
            return ListSettings.bookDownItem.value
        }
        if (listType == 42) {
            return ListSettings.gdriveItem.value
        }
        return 0
    }

    @JvmStatic
    fun reversed(listType: Int): Boolean {
        if (listType == 0) {
            return ListSettings.fileRvse.value
        }
        if (listType == 1) {
            return ListSettings.albumRvse.value
        }
        if (listType == 2) {
            return ListSettings.albumRvse.value
        }
        if (listType == 3) {
            return ListSettings.albumRvse.value
        }
        if (listType == 13) {
            return ListSettings.castRvse.value
        }
        if (listType == 14) {
            return ListSettings.bookAlbumRvse.value
        }
        if (listType == 15) {
            return ListSettings.bookAlbumRvse.value
        }
        if (listType == 16) {
            return ListSettings.bookAlbumRvse.value
        }
        if (listType == 17) {
            return false
        }
        if (listType == 18) {
            return ListSettings.bookHistRvse.value
        }
        if (listType == 19) {
            return ListSettings.bookAdsRvse.value
        }
        if (listType == 20) {
            return ListSettings.bookOverRvse.value
        }
        if (listType == 21) {
            return ListSettings.bookPopRvse.value
        }
        if (listType == 22) {
            return ListSettings.bookLinkRvse.value
        }
        if (listType == 23) {
            return ListSettings.bookBlockRvse.value
        }
        if (listType == 24) {
            return ListSettings.bookFilterRvse.value
        }
        if (listType == 25) {
            return ListSettings.bookUserRvse.value
        }
        if (listType == 26) {
            return ListSettings.bookScrRvse.value
        }
        if (listType == 27) {
            return ListSettings.bookJavaRvse.value
        }
        if (listType == 28) {
            return ListSettings.bookTmemRvse.value
        }
        if (listType == 29) {
            return ListSettings.bookTransRvse.value
        }
        if (listType == 31) {
            return ListSettings.bookDownRvse.value
        }
        if (listType == 42) {
            return ListSettings.gdriveRvse.value
        }
        return false
    }

    @JvmStatic
    fun setDetail(context: Context?, listType: Int, showDetail: Boolean) {
        if (listType == 0) {
            ListSettings.edit(context) { set(ListSettings.fileDetail, showDetail) }
            return
        }
        if (listType == 1) {
            ListSettings.edit(context) { set(ListSettings.albumDetail, showDetail) }
            return
        }
        if (listType == 2) {
            ListSettings.edit(context) { set(ListSettings.albumDetail, showDetail) }
            return
        }
        if (listType == 3) {
            ListSettings.edit(context) { set(ListSettings.albumDetail, showDetail) }
            return
        }
        if (listType == 13) {
            ListSettings.edit(context) { set(ListSettings.castDetail, showDetail) }
            return
        }
        if (listType == 14) {
            ListSettings.edit(context) { set(ListSettings.bookAlbumDetail, showDetail) }
            return
        }
        if (listType == 15) {
            ListSettings.edit(context) { set(ListSettings.bookAlbumDetail, showDetail) }
            return
        }
        if (listType == 16) {
            ListSettings.edit(context) { set(ListSettings.bookAlbumDetail, showDetail) }
            return
        }
        if (listType == 17) {
            ListSettings.edit(context) { set(ListSettings.bookWebDetail, showDetail) }
            return
        }
        if (listType == 18) {
            ListSettings.edit(context) { set(ListSettings.bookHistDetail, showDetail) }
            return
        }
        if (listType == 19) {
            ListSettings.edit(context) { set(ListSettings.bookAdsDetail, showDetail) }
            return
        }
        if (listType == 20) {
            ListSettings.edit(context) { set(ListSettings.bookOverDetail, showDetail) }
            return
        }
        if (listType == 21) {
            ListSettings.edit(context) { set(ListSettings.bookPopDetail, showDetail) }
            return
        }
        if (listType == 22) {
            ListSettings.edit(context) { set(ListSettings.bookLinkDetail, showDetail) }
            return
        }
        if (listType == 23) {
            ListSettings.edit(context) { set(ListSettings.bookBlockDetail, showDetail) }
            return
        }
        if (listType == 24) {
            ListSettings.edit(context) { set(ListSettings.bookFilterDetail, showDetail) }
            return
        }
        if (listType == 26) {
            ListSettings.edit(context) { set(ListSettings.bookScrDetail, showDetail) }
            return
        }
        if (listType == 27) {
            ListSettings.edit(context) { set(ListSettings.bookJavaDetail, showDetail) }
            return
        }
        if (listType == 28) {
            ListSettings.edit(context) { set(ListSettings.bookTmemDetail, showDetail) }
            return
        }
        if (listType == 29) {
            ListSettings.edit(context) { set(ListSettings.bookTransDetail, showDetail) }
            return
        }
        if (listType == 31) {
            ListSettings.edit(context) { set(ListSettings.bookDownDetail, showDetail) }
        } else if (listType == 36) {
            ListSettings.edit(context) { set(ListSettings.bookMemoDetail, showDetail) }
        } else if (listType == 42) {
            ListSettings.edit(context) { set(ListSettings.gdriveDetail, showDetail) }
        }
    }

    @JvmStatic
    fun setSingleLine(context: Context?, listType: Int, single: Boolean) {
        if (listType == 0) {
            ListSettings.edit(context) { set(ListSettings.fileSingle, single) }
            return
        }
        if (listType == 1) {
            ListSettings.edit(context) { set(ListSettings.albumSingle, single) }
            return
        }
        if (listType == 2) {
            ListSettings.edit(context) { set(ListSettings.albumSingle, single) }
            return
        }
        if (listType == 3) {
            ListSettings.edit(context) { set(ListSettings.albumSingle, single) }
            return
        }
        if (listType == 13) {
            ListSettings.edit(context) { set(ListSettings.castSingle, single) }
            return
        }
        if (listType == 14) {
            ListSettings.edit(context) { set(ListSettings.bookAlbumSingle, single) }
            return
        }
        if (listType == 15) {
            ListSettings.edit(context) { set(ListSettings.bookAlbumSingle, single) }
            return
        }
        if (listType == 16) {
            ListSettings.edit(context) { set(ListSettings.bookAlbumSingle, single) }
            return
        }
        if (listType == 17) {
            ListSettings.edit(context) { set(ListSettings.bookWebSingle, single) }
            return
        }
        if (listType == 18) {
            ListSettings.edit(context) { set(ListSettings.bookHistSingle, single) }
            return
        }
        if (listType == 19) {
            ListSettings.edit(context) { set(ListSettings.bookAdsSingle, single) }
            return
        }
        if (listType == 20) {
            ListSettings.edit(context) { set(ListSettings.bookOverSingle, single) }
            return
        }
        if (listType == 21) {
            ListSettings.edit(context) { set(ListSettings.bookPopSingle, single) }
            return
        }
        if (listType == 22) {
            ListSettings.edit(context) { set(ListSettings.bookLinkSingle, single) }
            return
        }
        if (listType == 23) {
            ListSettings.edit(context) { set(ListSettings.bookBlockSingle, single) }
            return
        }
        if (listType == 24) {
            ListSettings.edit(context) { set(ListSettings.bookFilterSingle, single) }
            return
        }
        if (listType == 26) {
            ListSettings.edit(context) { set(ListSettings.bookScrSingle, single) }
            return
        }
        if (listType == 27) {
            ListSettings.edit(context) { set(ListSettings.bookJavaSingle, single) }
            return
        }
        if (listType == 28) {
            ListSettings.edit(context) { set(ListSettings.bookTmemSingle, single) }
            return
        }
        if (listType == 29) {
            ListSettings.edit(context) { set(ListSettings.bookTransSingle, single) }
            return
        }
        if (listType == 31) {
            ListSettings.edit(context) { set(ListSettings.bookDownSingle, single) }
        } else if (listType == 36) {
            ListSettings.edit(context) { set(ListSettings.bookMemoSingle, single) }
        } else if (listType == 42) {
            ListSettings.edit(context) { set(ListSettings.gdriveSingle, single) }
        }
    }
}
