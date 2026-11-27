package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefSecret

/**
 * Settings of the file `PrefSecret`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object SecretSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefSecret.get(context, false)

    val initQuick = bool("mInitQuick") { true }
    val checkTab = bool("mCheckTab") { true }
    val secretHist = long("mSecretHist") { 0L }
    val secretDown = long("mSecretDown") { 1L }
    val keepTab = bool("mKeepTab") { true }
    val keepLogin = bool("mKeepLogin", legacy = true) { false } // factory default changed 2026-11-23 (old one kept by existing installs)
    val secretNoti = bool("mSecretNoti") { true }
    val shotType = int("mShotType", legacy = 0) { 1 } // factory default changed 2026-11-23 (old one kept by existing installs)
    val shotSecret = bool("mShotSecret", legacy = false) { true } // factory default changed 2026-11-23 (old one kept by existing installs)
    val lockType = int("mLockType2") { 0 }
    val lockCode = string("mLockCode2") { "" }
    /** The app lock asks only for locked workspaces (was: only for incognito, until 2026-11-25); false: the whole app. */
    val lockSecret = bool("mLockSecret2") { false }
    val lockReset = int("mLockReset3") { 112 }
    val lockSkip = bool("mLockSkip") { false }
    val savePass = bool("mSavePass2", legacy = false) { true } // factory default changed 2026-11-23 (old one kept by existing installs)
    val loginLock = bool("mLoginLock") { false }
    val passLockType = int("mPassLockType2") { 0 }
    val passLockCode = string("mPassLockCode2") { "" }
    val linkLockType = int("mLinkLockType") { 0 }
    val linkLockCode = string("mLinkLockCode") { "" }
    val touchLockType = int("mTouchLockType") { 0 }
    val touchLockCode = string("mTouchLockCode") { "" }
    /**
     * Which lock guards the passwords (1), the blocked links (2) and the video touch lock (3) (2026-11-27): 0 the app
     * lock, 1 a lock of its own ([passLockType] ...). -1 = not chosen yet: its own lock if one was set up before,
     * else the app lock.
     */
    val passLockMode = int("mPassLockMode") { -1 }
    val linkLockMode = int("mLinkLockMode") { -1 }
    val touchLockMode = int("mTouchLockMode") { -1 }

    /** The own lock type of [pass] (0 = the app lock's). */
    fun ownType(pass: Int): Int = when (pass) {
        1 -> passLockType.value
        2 -> linkLockType.value
        3 -> touchLockType.value
        else -> lockType.value
    }

    fun modeSetting(pass: Int) = when (pass) {
        1 -> passLockMode
        2 -> linkLockMode
        else -> touchLockMode
    }

    /** [pass] (1, 2, 3) uses a lock of its own instead of the app lock. */
    fun ownLock(pass: Int): Boolean {
        if (pass == 0) return true
        val mode = modeSetting(pass).value
        return if (mode < 0) ownType(pass) != 0 else mode == 1
    }

    /** The type of the lock that guards [pass] (0 = none): its own one or the app lock. */
    fun guardType(pass: Int): Int = if (ownLock(pass)) ownType(pass) else lockType.value

    /** The lock screen's `EXTRA_PASS` that checks [pass]: itself for its own lock, 0 for the app lock. */
    fun guardPass(pass: Int): Int = if (ownLock(pass)) pass else 0

    val secureDnt = int("mSecureDnt") { 0 }
    val secureGpc = int("mSecureGpc") { 0 }
    val secureKey = int("mSecureKey") { 2 }

    override fun afterLoad() {
        // PrefSecret.r reloads the file while this marker is empty
        PrefSecret.loadedTag = "PrefSecret"
    }
}
