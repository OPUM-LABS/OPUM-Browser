package com.mycompany.app.main

import java.util.ArrayList
import com.mycompany.app.ui.state.AppState
import android.text.TextUtils

@Suppress("deprecation")
object MainNative {
    @JvmField
    var snsFixParts6: ArrayList<Any?>? = null
    @JvmField
    var faceUrl: String? = null
    @JvmField
    var twitUrl: String? = null
    @JvmField
    var siteLockParts1: ArrayList<Any?>? = null
    @JvmField
    var siteLockParts2: ArrayList<Any?>? = null
    @JvmField
    var requestFixParts: ArrayList<Any?>? = null
    @JvmField
    var viewReadParts: ArrayList<Any?>? = null
    @JvmField
    var viewReadTransParts: ArrayList<Any?>? = null
    @JvmField
    var nativeParts1: ArrayList<Any?>? = null
    @JvmField
    var blobParts: ArrayList<Any?>? = null
    @JvmField
    var adsGoogleParts: ArrayList<Any?>? = null
    @JvmField
    var videoDetectParts: ArrayList<Any?>? = null
    @JvmField
    var backupKeyCache: String? = null
    @JvmField
    var ampParts: ArrayList<Any?>? = null
    @JvmField
    var overlayParts: ArrayList<Any?>? = null
    @JvmField
    var nativeParts0: ArrayList<Any?>? = null
    @JvmField
    var transContain: String? = null
    @JvmField
    var transElement: String? = null
    @JvmField
    var transPromptSection: String? = null
    @JvmField
    var transPromptLang: String? = null
    @JvmField
    var transFinishSection: String? = null
    @JvmField
    var transFinishLang: String? = null
    @JvmField
    var transTrId: String? = null
    @JvmField
    var transSpId: String? = null
    @JvmField
    var transLang: String? = null
    @JvmField
    var transCsp: String? = null
    @JvmField
    var nativeParts2: ArrayList<Any?>? = null
    @JvmField
    var nativeParts3: ArrayList<Any?>? = null
    @JvmField
    var snsFixParts1: ArrayList<Any?>? = null
    @JvmField
    var snsFixParts2: ArrayList<Any?>? = null
    @JvmField
    var instaScriptParts: ArrayList<Any?>? = null
    @JvmField
    var instaAid: String? = null
    @JvmField
    var instaMid1: String? = null
    @JvmField
    var instaMid2: String? = null
    @JvmField
    var snsFixParts3: ArrayList<Any?>? = null
    @JvmField
    var storyKeys: ArrayList<Any?>? = null
    @JvmField
    var snsFixParts4: ArrayList<Any?>? = null
    @JvmField
    var snsFixParts5: ArrayList<Any?>? = null

    @JvmStatic
    fun partsReady(count: Int, list: MutableList<Any?>?): Boolean {
        if (list == null || list.size != count) {
            return false
        }
        val it: MutableIterator<Any?> = list.iterator()
        while (it.hasNext()) {
            if (TextUtils.isEmpty(it.next() as String?)) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun clearCache() {
        backupKeyCache = null
        ampParts = null
        overlayParts = null
        nativeParts0 = null
        transContain = null
        transElement = null
        transPromptSection = null
        transPromptLang = null
        transFinishSection = null
        transFinishLang = null
        transTrId = null
        transSpId = null
        transLang = null
        transCsp = null
        nativeParts2 = null
        nativeParts3 = null
        snsFixParts1 = null
        snsFixParts2 = null
        instaScriptParts = null
        instaAid = null
        instaMid1 = null
        instaMid2 = null
        snsFixParts3 = null
        storyKeys = null
        snsFixParts4 = null
        snsFixParts5 = null
        snsFixParts6 = null
        faceUrl = null
        twitUrl = null
        siteLockParts1 = null
        siteLockParts2 = null
        requestFixParts = null
        viewReadParts = null
        viewReadTransParts = null
        nativeParts1 = null
        blobParts = null
        adsGoogleParts = null
        videoDetectParts = null
    }

    @JvmStatic
    fun transContainer(): String? {
        if (TextUtils.isEmpty(transContain)) {
            transContain = MainUtil.transContain
        }
        return transContain
    }

    @JvmStatic
    fun transElementSel(): String? {
        if (TextUtils.isEmpty(transElement)) {
            transElement = MainUtil.transElement
        }
        return transElement
    }
}
