package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefZtri

/**
 * Settings of the file `PrefZtri`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object ZtriSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefZtri.get(context)

    val readAccent = bool("mReadAccent") { true }
    val zoomIcon = bool("mZoomIcon") { false }
    val zoomLtX = float("mZoomLtX") { -1.0f }
    val zoomRtX = float("mZoomRtX") { -1.0f }
    val zoomUpY = float("mZoomUpY") { -1.0f }
    val zoomDnY = float("mZoomDnY") { -1.0f }
    val zoomSize = int("mZoomSize") { 200 }
    val comicLtX = float("mComicLtX") { -1.0f }
    val comicRtX = float("mComicRtX") { -1.0f }
    val comicUpY = float("mComicUpY") { -1.0f }
    val comicDnY = float("mComicDnY") { -1.0f }
    val ypipLtX = float("mYpipLtX") { -1.0f }
    val ypipRtX = float("mYpipRtX") { -1.0f }
    val ypipUpY = float("mYpipUpY") { -1.0f }
    val ypipDnY = float("mYpipDnY") { -1.0f }
    val downLtX = float("mDownLtX") { -1.0f }
    val downRtX = float("mDownRtX") { -1.0f }
    val downUpY = float("mDownUpY") { -1.0f }
    val downDnY = float("mDownDnY") { -1.0f }
    val newsIcon = bool("mNewsIcon") { true }
    val newsLtX = float("mNewsLtX") { -1.0f }
    val newsRtX = float("mNewsRtX") { -1.0f }
    val newsUpY = float("mNewsUpY") { -1.0f }
    val newsDnY = float("mNewsDnY") { -1.0f }
    val handIcon = bool("mHandIcon") { false }
    val handLtX = float("mHandLtX") { -1.0f }
    val handRtX = float("mHandRtX") { -1.0f }
    val handUpY = float("mHandUpY") { -1.0f }
    val handDnY = float("mHandDnY") { -1.0f }
    val passIcon = bool("mPassIcon") { true }
    val passLtX = float("mPassLtX") { -1.0f }
    val passRtX = float("mPassRtX") { -1.0f }
    val passUpY = float("mPassUpY") { -1.0f }
    val passDnY = float("mPassDnY") { -1.0f }
    val trnsLtX = float("mTrnsLtX") { -1.0f }
    val trnsRtX = float("mTrnsRtX") { -1.0f }
    val trnsUpY = float("mTrnsUpY") { -1.0f }
    val trnsDnY = float("mTrnsDnY") { -1.0f }
    val readLtX = float("mReadLtX") { -1.0f }
    val readRtX = float("mReadRtX") { -1.0f }
    val readUpY = float("mReadUpY") { -1.0f }
    val readDnY = float("mReadDnY") { -1.0f }
    val notiQmenu = bool("mNotiQmenu2") { true }
    val quickUse = bool("mQuickUse") { true }
    val qckSchType = int("mQckSchType") { 1 }
    val quickPlus = bool("mQuickPlus") { true }
    val quickPort = int("mQuickPort") { 5 }
    val quickLand = int("mQuickLand") { 5 }
    val lastSwipe = bool("mLastSwipe") { true }
    val notiZoom = bool("mNotiZoom") { true }
    val guideDark = bool("mGuideDark") { true }
    val guideRest = bool("mGuideRest") { true }
    val moveLong = bool("mMoveLong") { false }

    override fun afterLoad() {
        // PrefZtri.r reloads the file while this marker is empty
        PrefZtri.loadedTag = "PrefZtri"
    }
}
