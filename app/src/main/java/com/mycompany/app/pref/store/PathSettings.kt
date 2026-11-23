package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefPath

/**
 * Settings of the file `PrefPath`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object PathSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefPath.get(context, false)

    val albumPath = string("mAlbumPath") { "" }
    val cmpPath = string("mCmpPath") { "" }
    val pdfPath = string("mPdfPath") { "" }
    val sdkVer = int("mSdkVer2") { 0 }
    val checkPath = bool("mCheckPath") { true }
    val pathDown = string("mPathDown") { "" }
    val pathAlbum = string("mPathAlbum") { "" }
    val docUri = string("mDocUri") { "" }
    val scanAlbum = string("mScanAlbum") { "" }
    val scanPdf = string("mScanPdf") { "" }
    val scanZip = string("mScanZip") { "" }
    val scanFont = string("mScanFont") { "" }

    override fun afterLoad() {
        // PrefPath.r reloads the file while this marker is empty
        PrefPath.loadedTag = "PrefPath"
    }
}
