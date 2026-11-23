package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefSecret(context: Context?) : PrefCore(context, "PrefSecret") {
    companion object {
        // the stored values live in pref/store/SecretSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefSecret? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefSecret(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefSecret = mainAppO.prefSecret
            if (prefSecret == null) {
                synchronized(PrefSecret::class.java) {
                    if (mainAppO.prefSecret == null) {
                        mainAppO.prefSecret = PrefSecret(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefSecret.filePath)) {
                synchronized(PrefSecret::class.java) {
                    mainAppO.prefSecret!!.init(context, "PrefSecret")
                }
                reload = false
            }
            if (reload || mainAppO.prefSecret!!.notLoaded()) {
                mainAppO.prefSecret!!.load()
            }
            return mainAppO.prefSecret
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.SecretSettings.load(context)
        }

        @JvmStatic
        fun saveLinkLock(context: Context?) {
            val prefSecretR: PrefSecret? = if (context == null) null else get(context, false)
            if (context == null || prefSecretR == null) {
                return
            }
            prefSecretR.putInt(com.mycompany.app.pref.store.SecretSettings.linkLockType.value, "mLinkLockType")
            prefSecretR.putString("mLinkLockCode", com.mycompany.app.pref.store.SecretSettings.linkLockCode.value)
            prefSecretR.saveAsync()
        }

        @JvmStatic
        fun saveLock(context: Context?) {
            val prefSecretR: PrefSecret? = if (context == null) null else get(context, false)
            if (context == null || prefSecretR == null) {
                return
            }
            prefSecretR.putInt(com.mycompany.app.pref.store.SecretSettings.lockType.value, "mLockType2")
            prefSecretR.putString("mLockCode2", com.mycompany.app.pref.store.SecretSettings.lockCode.value)
            prefSecretR.saveAsync()
        }

        @JvmStatic
        fun savePassLock(context: Context?) {
            val prefSecretR: PrefSecret? = if (context == null) null else get(context, false)
            if (context == null || prefSecretR == null) {
                return
            }
            prefSecretR.putBool("mSavePass2", com.mycompany.app.pref.store.SecretSettings.savePass.value)
            prefSecretR.putBool("mLoginLock", com.mycompany.app.pref.store.SecretSettings.loginLock.value)
            prefSecretR.putInt(com.mycompany.app.pref.store.SecretSettings.passLockType.value, "mPassLockType2")
            prefSecretR.putString("mPassLockCode2", com.mycompany.app.pref.store.SecretSettings.passLockCode.value)
            prefSecretR.saveAsync()
        }

        @JvmStatic
        fun saveTouchLock(context: Context?) {
            val prefSecretR: PrefSecret? = if (context == null) null else get(context, false)
            if (context == null || prefSecretR == null) {
                return
            }
            prefSecretR.putInt(com.mycompany.app.pref.store.SecretSettings.touchLockType.value, "mTouchLockType")
            prefSecretR.putString("mTouchLockCode", com.mycompany.app.pref.store.SecretSettings.touchLockCode.value)
            prefSecretR.saveAsync()
        }
    }
}
