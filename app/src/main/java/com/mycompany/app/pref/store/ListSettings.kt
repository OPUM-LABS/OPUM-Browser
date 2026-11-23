package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefList

/**
 * Settings of the file `PrefList`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object ListSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefList.get(context, false)

    val viewType = int("mViewType") { 2 }
    val albumFold = int("mAlbumFold2") { 0 }
    val albumItem = int("mAlbumItem") { 0 }
    val albumRvse = bool("mAlbumRvse") { false }
    val albumDetail = bool("mAlbumDetail") { true }
    val albumSingle = bool("mAlbumSingle") { true }
    val albumRecent = bool("mAlbumRecent") { true }
    val albumContinue = bool("mAlbumContinue") { true }
    val castItem = int("mCastItem") { 0 }
    val castRvse = bool("mCastRvse") { false }
    val castDetail = bool("mCastDetail") { true }
    val castSingle = bool("mCastSingle") { true }
    val fileItem = int("mFileItem") { 0 }
    val fileRvse = bool("mFileRvse") { false }
    val fileDetail = bool("mFileDetail") { true }
    val fileSingle = bool("mFileSingle") { true }
    val bookAlbumFold = int("mBookAlbumFold2") { 0 }
    val bookAlbumItem = int("mBookAlbumItem") { 3 }
    val bookAlbumRvse = bool("mBookAlbumRvse") { true }
    val bookAlbumDetail = bool("mBookAlbumDetail") { true }
    val bookAlbumSingle = bool("mBookAlbumSingle") { true }
    val bookWebUser = bool("mBookWebUser") { true }
    val bookWebFtop = bool("mBookWebFtop") { true }
    val bookWebItem = int("mBookWebItem") { 3 }
    val bookWebRvse = bool("mBookWebRvse") { true }
    val bookWebDetail = bool("mBookWebDetail") { true }
    val bookWebSingle = bool("mBookWebSingle") { true }
    val bookHistFold = int("mBookHistFold2") { 0 }
    val bookHistItem = int("mBookHistItem") { 3 }
    val bookHistRvse = bool("mBookHistRvse") { true }
    val bookHistDetail = bool("mBookHistDetail") { true }
    val bookHistSingle = bool("mBookHistSingle") { true }
    val bookAdsFold = int("mBookAdsFold") { 0 }
    val bookAdsItem = int("mBookAdsItem") { 3 }
    val bookAdsRvse = bool("mBookAdsRvse") { true }
    val bookAdsDetail = bool("mBookAdsDetail") { true }
    val bookAdsSingle = bool("mBookAdsSingle") { true }
    val bookOverFold = int("mBookOverFold") { 0 }
    val bookOverItem = int("mBookOverItem") { 3 }
    val bookOverRvse = bool("mBookOverRvse") { true }
    val bookOverDetail = bool("mBookOverDetail") { true }
    val bookOverSingle = bool("mBookOverSingle") { true }
    val bookPopFold = int("mBookPopFold") { 0 }
    val bookPopItem = int("mBookPopItem") { 3 }
    val bookPopRvse = bool("mBookPopRvse") { true }
    val bookPopDetail = bool("mBookPopDetail") { true }
    val bookPopSingle = bool("mBookPopSingle") { true }
    val bookLinkFold = int("mBookLinkFold") { 0 }
    val bookLinkItem = int("mBookLinkItem") { 3 }
    val bookLinkRvse = bool("mBookLinkRvse") { true }
    val bookLinkDetail = bool("mBookLinkDetail") { true }
    val bookLinkSingle = bool("mBookLinkSingle") { true }
    val bookBlockFold = int("mBookBlockFold") { 0 }
    val bookBlockItem = int("mBookBlockItem") { 3 }
    val bookBlockRvse = bool("mBookBlockRvse") { true }
    val bookBlockDetail = bool("mBookBlockDetail") { true }
    val bookBlockSingle = bool("mBookBlockSingle") { true }
    val bookFilterItem = int("mBookFilterItem") { 1 }
    val bookFilterRvse = bool("mBookFilterRvse") { false }
    val bookFilterDetail = bool("mBookFilterDetail") { true }
    val bookFilterSingle = bool("mBookFilterSingle") { true }
    val bookJavaFold = int("mBookJavaFold") { 0 }
    val bookJavaItem = int("mBookJavaItem") { 3 }
    val bookJavaRvse = bool("mBookJavaRvse") { true }
    val bookJavaDetail = bool("mBookJavaDetail") { true }
    val bookJavaSingle = bool("mBookJavaSingle") { true }
    val bookScrItem = int("mBookScrItem") { 3 }
    val bookScrRvse = bool("mBookScrRvse") { true }
    val bookScrDetail = bool("mBookScrDetail") { true }
    val bookScrSingle = bool("mBookScrSingle") { true }
    val bookTmemFold = int("mBookTmemFold") { 0 }
    val bookTmemItem = int("mBookTmemItem") { 3 }
    val bookTmemRvse = bool("mBookTmemRvse") { true }
    val bookTmemDetail = bool("mBookTmemDetail") { true }
    val bookTmemSingle = bool("mBookTmemSingle") { true }
    val bookTransFold = int("mBookTransFold") { 0 }
    val bookTransItem = int("mBookTransItem") { 3 }
    val bookTransRvse = bool("mBookTransRvse") { true }
    val bookTransDetail = bool("mBookTransDetail") { true }
    val bookTransSingle = bool("mBookTransSingle") { true }
    val bookUserFold = int("mBookUserFold") { 6 }
    val bookUserItem = int("mBookUserItem") { 3 }
    val bookUserRvse = bool("mBookUserRvse") { true }
    val bookDownType = int("mBookDownType") { 0 }
    val bookDownFold = int("mBookDownFold2") { 0 }
    val bookDownItem = int("mBookDownItem") { 3 }
    val bookDownRvse = bool("mBookDownRvse") { true }
    val bookDownDetail = bool("mBookDownDetail") { true }
    val bookDownSingle = bool("mBookDownSingle") { true }
    val bookMemoDetail = bool("mBookMemoDetail") { true }
    val bookMemoSingle = bool("mBookMemoSingle") { true }
    val gdriveItem = int("mGdriveItem") { 0 }
    val gdriveRvse = bool("mGdriveRvse") { false }
    val gdriveDetail = bool("mGdriveDetail") { true }
    val gdriveSingle = bool("mGdriveSingle") { true }

    override fun afterLoad() {
        // PrefList.r reloads the file while this marker is empty
        PrefList.loadedTag = "PrefList"
    }
}
