package com.mycompany.app.pref

import android.content.Context

@Suppress("deprecation")
object PrefSet {
    @JvmStatic
    fun prefFor(context: Context?, prefIndex: Int): PrefCore? {
        if (prefIndex == 0) {
            return PrefAlbum.Companion.get(context, false)
        }
        if (prefIndex == 1) {
            return PrefEditor.Companion.get(context)
        }
        if (prefIndex == 2) {
            return PrefFloat.Companion.get(context)
        }
        if (prefIndex == 3) {
            return PrefImage.Companion.get(context, false)
        }
        if (prefIndex == 4) {
            return PrefList.Companion.get(context, false)
        }
        if (prefIndex == 5) {
            return PrefMain.Companion.get(context, false)
        }
        if (prefIndex == 6) {
            return PrefPath.Companion.get(context, false)
        }
        if (prefIndex == 7) {
            return PrefPdf.Companion.get(context, false)
        }
        if (prefIndex == 8) {
            return PrefRead.Companion.get(context, false)
        }
        if (prefIndex == 9) {
            return PrefSecret.Companion.get(context, false)
        }
        if (prefIndex == 10) {
            return PrefSub.Companion.get(context)
        }
        if (prefIndex == 11) {
            return PrefSync.Companion.get(context, false)
        }
        if (prefIndex == 12) {
            return PrefTts.Companion.get(context, false)
        }
        if (prefIndex == 13) {
            return PrefVideo.Companion.get(context)
        }
        if (prefIndex == 14) {
            return PrefWeb.Companion.get(context, false)
        }
        if (prefIndex == 15) {
            return PrefZone.Companion.get(context, false)
        }
        if (prefIndex == 16) {
            return PrefZtwo.Companion.get(context, false)
        }
        if (prefIndex == 17) {
            return PrefZtri.Companion.get(context)
        }
        return null
    }

    @JvmStatic
    fun putLong(prefIndex: Int, now: Long, context: Context?, key: String?) {
        val prefCoreA = prefFor(context, prefIndex)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.putLong(now, key)
        prefCoreA.saveAsync()
    }

    @JvmStatic
    fun putBool(prefIndex: Int, context: Context?, key: String?, boolValue: Boolean) {
        val prefCoreA = prefFor(context, prefIndex)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.putBool(key, boolValue)
        prefCoreA.saveAsync()
    }

    @JvmStatic
    fun putSpeedRate(context: Context?, playbackRate: Float) {
        val prefCoreA = prefFor(context, 16)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.putFloat("mYouSpdRate", playbackRate)
        prefCoreA.saveAsync()
    }

    @JvmStatic
    fun putInt(context: Context?, prefIndex: Int, value: Int, key: String?) {
        val prefCoreA = prefFor(context, prefIndex)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.putInt(value, key)
        prefCoreA.saveAsync()
    }

    @JvmStatic
    fun putString(context: Context?, prefIndex: Int, key: String?, path: String?) {
        val prefCoreA = prefFor(context, prefIndex)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.putString(key, path)
        prefCoreA.saveAsync()
    }

    @JvmStatic
    fun putBoolNow(prefIndex: Int, context: Context?, key: String?, boolValue: Boolean) {
        val prefCoreA = prefFor(context, prefIndex)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.putBool(key, boolValue)
        prefCoreA.save()
    }

    @JvmStatic
    fun putDownUri(context: Context?, dir: String?) {
        val prefCoreA = prefFor(context, 6)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.putString("mUriDown", dir)
        prefCoreA.save()
    }

    @JvmStatic
    fun remove(context: Context?, prefIndex: Int, key: String?) {
        val prefCoreA = prefFor(context, prefIndex)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.removeKey(key)
        prefCoreA.saveAsync()
    }

    @JvmStatic
    fun removeNow(context: Context?, prefIndex: Int, key: String?) {
        val prefCoreA = prefFor(context, prefIndex)
        if (prefCoreA == null) {
            return
        }
        prefCoreA.removeKey(key)
        prefCoreA.save()
    }
}
