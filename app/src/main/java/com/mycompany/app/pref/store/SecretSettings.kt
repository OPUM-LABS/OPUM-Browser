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
    val secureDnt = int("mSecureDnt") { 0 }
    val secureGpc = int("mSecureGpc") { 0 }
    val secureKey = int("mSecureKey") { 2 }

    override fun afterLoad() {
        // PrefSecret.r reloads the file while this marker is empty
        PrefSecret.loadedTag = "PrefSecret"
    }
}
