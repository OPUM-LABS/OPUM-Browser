package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefCore

/**
 * Settings of the file `PrefAlbum`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object AlbumSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefAlbum.get(context, false)

    val guideDown = bool("mGuideDown") { true }
    val imageType = int("mImageType2") { 126 }
    val notiIcon = bool("mNotiIcon") { true }
    val notiQuick = bool("mNotiQuick") { true }
    val guideTab = bool("mGuideTab") { true }
    val notiClean = bool("mNotiClean") { true }
    val notiClea = bool("mNotiClea2") { true }
    val notiBook = bool("mNotiBook") { true }
    val filterAdd = string("mFilterAdd4") { "" }
    val adguardAdd = string("mAdguardAdd2") { "" }
    /** Lists picked in the uBlock Origin catalog of `DialogSetFilter` (indexes, "/"-separated). */
    val uboAdd = string("mUboAdd") { "" }
    /** Lists picked in the Brave catalog of `DialogSetFilter` (indexes, "/"-separated). */
    val braveAdd = string("mBraveAdd") { "" }
    /** The "add uBlock Origin's recommended lists" card on the Ad filter screen was answered (added or dismissed). */
    val uboOfferDone = bool("mUboOfferDone") { false }
    /** Requests the web filter blocked since the app was installed (shown in the Web Filter dialog; `PageStats`). */
    val uboBlockedTotal = long("mUboBlockedTotal") { 0L }
    val transMode = int("mTransMode2") { 0 }
    val transIcon = bool("mTransIcon") { false }
    val transPage = bool("mTransPage") { false }
    /** No translate button at all, neither in the address bar nor floating (2026-11-26); the menu still translates. */
    val transHide = bool("mTransHide") { false }
    val transPick = string("mTransPick") { "" }
    val transCode = string("mTransCode") { "" }
    val dappName = string("mDappName2") { "" }
    val dappPkg = string("mDappPkg2") { "" }
    val dappCls = string("mDappCls2") { "" }
    val playName = string("mPlayName2") { "" }
    val playPkg = string("mPlayPkg2") { "" }
    val playCls = string("mPlayCls2") { "" }
    val webBookDir = string("mWebBookDir") { "" }
    val webBookSec = string("mWebBookSec") { "" }
    val tabAnim = bool("mTabAnim") { true }
    val linkApp = bool("mLinkApp") { false }

    override fun afterLoad() {
        // PrefAlbum.r reloads the file while this marker is empty
        PrefAlbum.loadedTag = "PrefAlbum"
    }
}
