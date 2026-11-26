package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefZtwo

/**
 * Search, user agent, gesture sensitivity, tab memory, news, YouTube and recent-list settings (file `PrefZtwo`).
 */
object ZtwoSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefZtwo.get(context, false)

    /** Search engine. */
    val searchType = int("mSearchType2") { 4 }
    /** Custom search address. */
    val searchUser = string("mSearchUser") { "" }
    /** Custom search address has a %s. */
    val searchForm = bool("mSearchForm2") { false }
    val searchColor = int("mSearchColor") { 0 }
    val soulAgent = string("mSoulAgent") { "" }
    val soulAgentName = string("mSoulAgName") { "" }
    /** User agent type. */
    val agentType = int("mAgentType") { 0 }
    /** Custom user agent. */
    val agentUser = string("mAgentUser") { "" }
    val tabIndi = bool("mTabIndi") { false }
    val tabLoop = bool("mTabLoop") { true }
    /** Gesture sensitivity, top. */
    val senseTop = int("mSenseTop2") { 100 }
    val senseBot = int("mSenseBot2") { 100 }
    val senseLeft = int("mSenseLeft2") { 100 }
    val senseRight = int("mSenseRight2") { 100 }
    val senseCenter = int("mSenseCenter2") { 100 }
    val quickDown = bool("mQuickDown2") { false }
    val tabDown = bool("mTabDown2") { false }
    /**
     * Where the tab overview puts the current tab when it opens: 0 like the address bar (bottom when the address bar is
     * at the bottom), 1 at the top, 2 at the bottom. Added 2026-11-19; until it is set, the old [tabDown] "At the bottom"
     * switch decides (on = 2).
     */
    val tabCurrentPos = int("mTabCurPos") { if (tabDown.value) 2 else 0 }
    val webScale = int("mWebScale") { 1 }
    /** Address auto complete. */
    val autoComp = bool("mAutoComp") { true }
    val tabOpen = int("mTabOpen2") { 0 }
    val scrollAnim = bool("mScrollAnim") { false }
    val addPage = bool("mAddPage") { true }
    val tabLimit = int("mTabLimit") { 0 }
    /** Archive tabs not used for this many days (0 = off), see `TabArchive` (2026-11-21). */
    val tabArchive = int("mTabArchive", legacy = 0) { 30 } // factory default changed 2026-11-23 (old one kept by existing installs)
    /** When archiving was first turned on (ms): tabs with no recorded last use count as used then. */
    val tabArchiveSince = long("mTabArchiveSince") { 0L }
    /** Delete archived tabs after this many days in the archive (0 = keep them). */
    val tabArchiveDelete = int("mTabArchiveDel") { 0 }
    val tabMemory = int("mTabMemory") { 5 }
    val pageMemory = int("mPageMemory") { 5 }
    val dayMemory = int("mDayMemory") { 2 }
    /**
     * How the memory limits above are chosen (2026-11-21, see [MemoryPlan]): 0 automatic (from the phone's memory),
     * 1 save memory, 2 faster, 3 custom (the stored values); -1 = not chosen yet.
     */
    val memoryMode = int("mMemMode") { -1 }
    val newsUse = bool("mNewsUse") { false }
    val newsAllow = bool("mNewsAllow") { true }
    val newsUrl = string("mNewsUrl") { "" }
    val newsOpen = int("mNewsOpen") { 0 }
    val newsLang = int("mNewsLang2") { -1 }
    val newsTitle = bool("mNewsTitle") { true }
    val newsPick = string("mNewsPick") { "" }
    val newsTopic = int("mNewsTopic") { 0 }
    val newsSearch = string("mNewsSearch") { "" }
    /** YouTube autoplay. */
    val autoPlay = bool("mAutoPlay") { true }
    val youUnmute = bool("mYouUnmute") { true }
    val youSpeedSave = bool("mYouSpdSave") { false }
    val youSpeedRate = float("mYouSpdRate") { 1.0f }
    val locNoti = bool("mLocNoti") { true }
    val locTrans = bool("mLocTrans") { false }
    val recentLimit = int("mRecentLimit") { 20 }
    val recentSecret = bool("mRecentSecret") { false }
    val recentLocal = int("mRecentLocal") { 5 }
    val recentTitle = int("mRecentTitle") { 5 }
    val recentTrans = int("mRecentTrans") { 5 }
    val notiMenu = bool("mNotiMenu") { true }

    /** RSS reader feeds (2026-11-19): one feed address per line. */
    val rssFeeds = string("mRssFeeds") { "" }
    /** The News button on the start page (2026-11-20; News and RSS are one page). */
    val newsButton = bool("mNewsButton") { true }
    /** The News page's last tab: "top" (Google News), "all" (my feeds) or a feed address. */
    val newsTab = string("mNewsTab") { "" }
    /**
     * Sites whose scroll lock is lifted on every visit (one host per line): the user chose "Unlock scrolling" when the
     * web filter had hidden a dialog the page was waiting for (2026-11-23, see `web/filter/ScrollUnlock`).
     */
    val scrollUnlock = string("mScrollUnlock") { "" }
    val ibexBest = int("mIbexBest") { 0 } // best score of the ibex game (not a setting shown anywhere)

    override fun afterLoad() {
        // PrefZtwo.r reloads the file while this marker is empty
        PrefZtwo.loadedTag = "PrefZtwo"
        MemoryPlan.applyLimits()
    }
}
