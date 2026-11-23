package com.mycompany.app.down

import android.content.Context
import com.mycompany.app.pref.store.ZoneSettings
import android.text.TextUtils
import android.webkit.URLUtil
import com.mycompany.app.compress.Compress
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc.DownZipListener
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZone
import java.io.EOFException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Suppress("deprecation")
object DownSaveZip {
    /** Downloads one entry of a "save all images" job, with a busy count and a retry count. */
    @JvmStatic
    fun saveNext(
        context: Context?,
        arrayList: ArrayList<*>,
        index: Int,
        busyCount: Int,
        retriesLeft: Int,
        downZipListener: DownZipListener
    ) {
        if (index < 0 || index >= arrayList.size) {
            return
        }
        val item = arrayList.get(index) as ChildItem?
        if (item == null) {
            return
        }
        if (item.status == 3) {
            downZipListener.onSaved(arrayList)
            return
        }
        if (TextUtils.isEmpty(item.url) || TextUtils.isEmpty(item.path)) {
            if (downZipListener.isRunning) {
                item.status = 4
                downZipListener.onSaved(arrayList)
            }
            return
        }
        item.status = 1
        val conn = MainUtil.openHttpConnection(0, 0, context, item.url, item.referer, false)
        if (conn == null) {
            if (downZipListener.isRunning) {
                saveItem(context, arrayList, index, 0, retriesLeft, item, null, false, downZipListener)
            }
            return
        }
        var busy = busyCount
        var retries = retriesLeft
        var redirect: String? = null
        var wrote = false
        var ok = false
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            if (File(item.path!!).length() > 0) {
                item.path = MainUtil.nextFileInFolder(item.path)
            }
            conn.setDoInput(true)
            conn.connect()
            val state = MainUtil.retryKind(conn.getResponseCode())
            if (state == 1) {
                busy = busyCount + 1
            } else if (state == 2) {
                busy = 0
                val location = conn.getHeaderField("Location")
                if (URLUtil.isNetworkUrl(location) && location != item.url) {
                    redirect = location
                }
            } else {
                busy = 0
                `in` = conn.getInputStream()
                out = MainUtil.openFileOutput(item.path, false)
                val buf = ByteArray(DownSavePath.BUFFER_SIZE) // 64 KB (was 1 KB)
                try {
                    while (downZipListener.isRunning) {
                        val n = `in`.read(buf, 0, 1024)
                        if (n == -1) {
                            break
                        }
                        out!!.write(buf, 0, n)
                        wrote = true
                        retries = 0
                    }
                } catch (unused: EOFException) {
                }
                ok = downZipListener.isRunning
            }
        } catch (unused: Exception) {
            ok = false
        }
        if (out != null) {
            try {
                out.close()
            } catch (unused: Exception) {
            }
        }
        if (`in` != null) {
            try {
                `in`.close()
            } catch (unused: Exception) {
            }
        }
        conn.disconnect()
        if (ok) {
            ok = File(item.path!!).length() > 0
        }
        if (!downZipListener.isRunning) {
            return
        }
        if (!ok) {
            saveItem(context, arrayList, index, busy, retries, item, redirect, wrote, downZipListener)
            return
        }
        if (!Compress.isImageName(item.path!!, true, true)) {
            // Saved without a usable extension: rename to .gif / .jpg by content.
            val path = item.path
            var target: String? = null
            if (!TextUtils.isEmpty(path)) {
                val gif = MainUtil.isAnimatedImage(context, path)
                var name = MainUtil.parentPathOf(context, path)
                val dot = name!!.lastIndexOf(46.toChar())
                if (dot > 0 && dot < name.length) {
                    name = name.substring(0, dot)
                }
                target = MainUtil.uriDisplayName(context, path) + "/" + name + (if (gif) ".gif" else ".jpg")
                if (File(target).length() > 0) {
                    target = MainUtil.nextFileInFolder(target)
                }
                MainUtil.moveFileOrUri(context, path, target)
            }
            item.path = target
        }
        item.status = 3
        downZipListener.onSaved(arrayList)
    }

    @JvmStatic
    fun saveAll(
        context: Context?,
        arrayList: ArrayList<*>,
        type: Int,
        width: Int,
        height: Int,
        downZipListener: DownZipListener
    ) {
        var context = context
        var arrayList = arrayList
        var downZipListener = downZipListener
        val size = arrayList.size
        var index = 0
        while (index < type && downZipListener.isRunning) {
            val index2 = (height * index) + width
            if (index2 >= size) {
                return
            }
            val context2 = context
            val arrayList2 = arrayList
            val downZipListener2 = downZipListener
            try {
                saveNext(context2, arrayList2, index2, 0, 0, downZipListener2)
            } catch (unused: OutOfMemoryError) {
                val childItem = arrayList2.get(index2) as ChildItem?
                if (childItem == null) {
                    return
                }
                childItem.status = 4
                downZipListener2.onSaved(arrayList2)
            }
            index++
            context = context2
            arrayList = arrayList2
            downZipListener = downZipListener2
        }
    }

    /**
     * Schedules the next attempt for one entry. The busy count counts "server busy" answers (wait 10 s, give up at 360),
     * the retry count counts failed attempts (wait 2 s, give up at ZoneSettings.retryDown.value); a redirect or dropping the referer retries
     * at once.
     */
    @JvmStatic
    fun saveItem(
        context: Context?,
        arrayList: ArrayList<*>,
        index: Int,
        busy: Int,
        retriesLeft: Int,
        childItem: ChildItem,
        url: String?,
        wrote: Boolean,
        downZipListener: DownZipListener
    ) {
        val delay: Long
        var retries = retriesLeft
        if (busy > 0) {
            if (busy >= 360) {
                childItem.status = 4
                downZipListener.onSaved(arrayList)
                return
            }
            delay = 10000
        } else if (!TextUtils.isEmpty(url)) {
            childItem.url = url
            delay = 0
        } else if (!wrote && !TextUtils.isEmpty(childItem.referer)) {
            childItem.referer = null
            delay = 0
        } else if (retries < ZoneSettings.retryDown.value) {
            retries++
            delay = 2000
        } else {
            childItem.status = 4
            downZipListener.onSaved(arrayList)
            return
        }
        try {
            if (delay == 0L) {
                DownPool.execute(SaveZipStep(index, busy, retries, context, downZipListener, childItem, arrayList)
                )
                return
            }
            val handler = downZipListener.uiHandler()
            if (handler == null) {
                childItem.status = 4
                downZipListener.onSaved(arrayList)
                return
            }
            handler.postDelayed(
                SavedTask(
                    index,
                    busy,
                    retries,
                    context,
                    downZipListener,
                    childItem,
                    arrayList
                ), delay
            )
        } catch (unused: OutOfMemoryError) {
            childItem.status = 4
            downZipListener.onSaved(arrayList)
        }
    }

    internal class SavedTask(@JvmField val index: Int,
                                   @JvmField val sync: Int,
                                   @JvmField val retries: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downZipListener: DownZipListener,
                                   @JvmField val childItem: ChildItem,
                                   @JvmField val items: ArrayList<*>
    ) : Runnable {
        override fun run() {
            val context = this.context
            val arrayList = this.items
            val index = this.index
            val sync = this.sync
            val retries = this.retries
            val childItem = this.childItem
            val downZipListener = this.downZipListener
            try {
                DownPool.execute(SaveZipStep(index, sync, retries, context, downZipListener, childItem, arrayList)
                )
            } catch (unused: OutOfMemoryError) {
                childItem.status = 4
                downZipListener.onSaved(arrayList)
            }
        }
    }

    internal class SaveZipStep(@JvmField val index: Int,
                                   @JvmField val sync: Int,
                                   @JvmField val retries: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downZipListener: DownZipListener,
                                   @JvmField val item: ChildItem,
                                   @JvmField val items: ArrayList<*>
    ) : Runnable {
        override fun run() {
            val arrayList = this.items
            val downZipListener = this.downZipListener
            if (downZipListener.isRunning) {
                try {
                    saveNext(this.context, arrayList, this.index, this.sync, this.retries, downZipListener)
                } catch (unused: OutOfMemoryError) {
                    this.item.status = 4
                    downZipListener.onSaved(arrayList)
                }
            }
        }
    }
}
