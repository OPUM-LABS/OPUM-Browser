package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefTts

/**
 * Settings of the file `PrefTts`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object TtsSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefTts.get(context, false)

    val ttsMode = bool("mTtsMode") { false }
    val ttsLang = string("mTtsLang") { "" }
    val ttsRate = float("mTtsRate") { 1.0f }
    val ttsPitch = float("mTtsPitch") { 1.0f }
    val ttsIcon = bool("mTtsIcon") { true }
    val ttsLtX = float("mTtsLtX") { -1.0f }
    val ttsRtX = float("mTtsRtX") { -1.0f }
    val ttsUpY = float("mTtsUpY") { -1.0f }
    val ttsDnY = float("mTtsDnY") { -1.0f }
    val checkBack = bool("mCheckBack") { false }
    val checkEle = bool("mCheckEle3") { false }
    val checkOver = int("mCheckOver2") { 0 }
    val autoGroup = bool("mAutoGroup") { false }
    val appKeypad = bool("mAppKeypad") { false }
    val skipKeypad = bool("mSkipKeypad") { false }
    val vpnMode = bool("mVpnMode") { false }
    val tabLast = bool("mTabLast") { false }
    val lastExit = bool("mLastExit") { false }
    val webLand = int("mWebLand1") { 0 }
    val webLand2 = int("mWebLand2") { 0 }
    val handMode = bool("mHandMode") { false }
    val handNoti = bool("mHandNoti") { true }
    val handTop = int("mHandTop2") { 60 }

    override fun afterLoad() {
        // PrefTts.r reloads the file while this marker is empty
        PrefTts.loadedTag = "PrefTts"
    }
}
