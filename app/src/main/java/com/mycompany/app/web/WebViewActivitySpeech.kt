@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUtil.isDarkTheme
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.appLocaleTag
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefTts
import android.speech.tts.TextToSpeech
import android.os.Handler
import java.lang.Exception
import java.util.Locale

/*
 * The browser activity's speech functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.applyTtsSettings() {
    val ttsReady = initTts()
    if (this.speechState.tts == null) {
        return
    }
    var ttsRate = TtsSettings.ttsRate.value
    var ttsPitch = 0.5f
    if (ttsRate < 0.5f) {
        TtsSettings.editLocal { set(TtsSettings.ttsRate, 0.5f) }
        ttsRate = 0.5f
    } else if (ttsRate > 3.0f) {
        TtsSettings.editLocal { set(TtsSettings.ttsRate, 3.0f) }
        ttsRate = 3.0f
    }
    val pitch = TtsSettings.ttsPitch.value
    if (pitch < 0.5f) {
        TtsSettings.editLocal { set(TtsSettings.ttsPitch, 0.5f) }
    } else {
        ttsPitch = 2.0f
        if (pitch > 2.0f) {
            TtsSettings.editLocal { set(TtsSettings.ttsPitch, 2.0f) }
        } else {
            ttsPitch = pitch
        }
    }
    try {
        if (ttsReady) {
            this.speechState.ttsRate = ttsRate
            this.speechState.ttsPitch = ttsPitch
            if ((ttsRate).compareTo(1.0f) != 0) {
                this.speechState.tts!!.setSpeechRate(TtsSettings.ttsRate.value)
            }
            if ((TtsSettings.ttsPitch.value).compareTo(1.0f) != 0) {
                this.speechState.tts!!.setPitch(TtsSettings.ttsPitch.value)
            }
        } else {
            if ((ttsRate).compareTo(this.speechState.ttsRate) != 0) {
                val newTtsRate = TtsSettings.ttsRate.value
                this.speechState.ttsRate = newTtsRate
                this.speechState.tts!!.setSpeechRate(newTtsRate)
            }
            if ((TtsSettings.ttsPitch.value).compareTo(this.speechState.ttsPitch) != 0) {
                val newTtsPitch = TtsSettings.ttsPitch.value
                this.speechState.ttsPitch = newTtsPitch
                this.speechState.tts!!.setPitch(newTtsPitch)
            }
        }
    } catch (unused: Exception) {
    }
    this.speechState.ttsLang = null
    checkPageLanguage(true)
}

fun WebViewActivity.applyTtsLanguage() {
    var localeI: Locale? = null
    var ttsLang: String? = null
    var langStatus = 0
    var ttsLocale: String? = null
    if (TextUtils.isEmpty(TtsSettings.ttsLang.value)) {
        if (TextUtils.isEmpty(this.speechState.ttsLang)) {
            localeI = MainApp.currentLocale()
            ttsLocale = localeI.toString()
        } else {
            ttsLang = this.speechState.ttsLang
        }
        if (sameObject(this.speechState.ttsLocale, ttsLocale)) {
        }
        this.speechState.ttsLocale = ttsLocale
        if (localeI == null) {
            localeI = parseLocale(ttsLocale)
        }
        if (localeI == null) {
            localeI = MainApp.currentLocale()
        } else {
            try {
                langStatus = this.speechState.tts!!.isLanguageAvailable(localeI)
            } catch (unused: Exception) {
                langStatus = -2
            }
            if (langStatus != 0 && langStatus != 1) {
                localeI = MainApp.currentLocale()
            }
        }
        try {
            this.speechState.tts!!.setLanguage(localeI)
        } catch (unused2: Exception) {
            return
        }
    }
    ttsLang = TtsSettings.ttsLang.value
    ttsLocale = ttsLang
    localeI = null
    if (sameObject(this.speechState.ttsLocale, ttsLocale)) {
        this.speechState.ttsLocale = ttsLocale
        localeI = parseLocale(ttsLocale)
        if (localeI == null) {
            localeI = MainApp.currentLocale()
        } else {
            langStatus = this.speechState.tts!!.isLanguageAvailable(localeI)
            if (langStatus != 0) {
                localeI = MainApp.currentLocale()
            }
        }
        this.speechState.tts!!.setLanguage(localeI)
    }
}

fun WebViewActivity.initTts(): kotlin.Boolean {
    if (TtsSettings.ttsMode.value && this.dialogsState.appContext != null) {
        if (this.speechState.tts == null) {
            try {
                val textToSpeech = TextToSpeech(this.dialogsState.appContext, WvaTtsInitListener(this))
                this.speechState.tts = textToSpeech
                textToSpeech.setOnUtteranceProgressListener(WvaTtsProgressListener(this))
                if (!TtsSettings.ttsIcon.value) {
                    removeTtsPanel()
                    return true
                }
                val handler: Handler? = if (this.speechState.ttsButton != null) null else this.mainHandler
                if (this.speechState.ttsButton != null || handler == null) {
                    return true
                }
                handler.post(WvaShowTtsPanel(this))
                return true
            } catch (unused: Exception) {
            }
        } else {
            if (!TtsSettings.ttsIcon.value) {
                removeTtsPanel()
                return false
            }
            val handler: Handler? = if (this.speechState.ttsButton == null) this.mainHandler else null
            if (this.speechState.ttsButton == null && handler != null) {
                handler.post(WvaShowTtsPanel(this))
                return false
            }
        }
    }
    return false
}

/** Lets the (dead) engine go and makes a new one; [onReady] gets it once it can speak, or null. */
fun WebViewActivity.recreateTts(onReady: (TextToSpeech?) -> Unit) {
    com.mycompany.app.main.SpeechGuard.release(this.speechState.tts)
    this.speechState.tts = null
    this.speechState.ttsLang = null
    this.speechState.ttsLocale = null
    val context = this.dialogsState.appContext ?: return onReady(null)
    this.speechState.tts = com.mycompany.app.main.SpeechGuard.create(context, WvaTtsProgressListener(this), onReady)
}

/** Reading aloud did not work: the stop button goes, and a message offers Android's speech settings. */
fun WebViewActivity.ttsFailed() {
    com.mycompany.app.main.SpeechGuard.cancelAll()
    this.speechState.jsBridgeBusy = false
    if (!this.dialogsState.ttsSpeaking && !this.speechState.ttsPanelShown) return
    this.dialogsState.ttsSpeaking = false
    updateTtsPanel(false, false)
    if (!this.isPaused && !this.isClosing) com.mycompany.app.main.SpeechGuard.showDown(this)
}

fun WebViewActivity.shutdownTts() {
    this.speechState.jsBridgeBusy = true
    this.dialogsState.ttsSpeaking = false
    this.speechState.ttsLang = null
    this.speechState.ttsLocale = null
    removeTtsPanel()
    if (this.speechState.tts == null) {
        this.speechState.jsBridgeBusy = false
    } else {
        runInBackground(WvaTtsShutdown(this))
    }
}

fun WebViewActivity.updateTtsPanel(isShown: kotlin.Boolean, animate: kotlin.Boolean) {
    this.speechState.jsBridgeBusy = (animate) == true
    this.speechState.ttsPanelShown = (isShown) == true
    val handler: Handler? = if (this.speechState.ttsButton != null) this.mainHandler else null
    if (this.speechState.ttsButton != null && handler != null) {
        handler.post(WvaUpdateTtsPanel(this, isShown, animate))
    } else {
        this.speechState.jsBridgeBusy = false
        this.speechState.ttsPanelShown = false
    }
}

fun WebViewActivity.removeTtsPanel() {
    val handler: Handler? = if (this.speechState.ttsButton == null) null else this.mainHandler
    if (this.speechState.ttsButton == null || handler == null) {
        return
    }
    handler.post(WvaRemoveTtsPanel(this))
}

fun WebViewActivity.stopTts() {
    com.mycompany.app.main.SpeechGuard.cancelAll()
    this.speechState.jsBridgeBusy = true
    this.dialogsState.ttsSpeaking = false
    this.speechState.ttsPanelShown = false
    if (this.speechState.tts == null) {
        this.speechState.jsBridgeBusy = false
        return
    }
    val webTtsView = this.speechState.ttsButton
    if (webTtsView != null) {
        webTtsView.fadeAway()
    }
    runInBackground(WvaTtsStop(this))
}
