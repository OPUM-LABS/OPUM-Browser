package com.mycompany.app.down

import android.content.Context
import com.mycompany.app.pref.store.ZoneSettings
import android.os.Environment
import android.text.TextUtils
import android.webkit.URLUtil
import com.mycompany.app.db.book.DbBookDown.Companion.addFileItem
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc.DownImageListener
import com.mycompany.app.main.MainDownSvc.ImageItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZone
import java.io.EOFException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Suppress("deprecation")
object DownSaveImage {

    /**
     * Downloads image `index` of the list into a temp file under `str`, moves it to its target folder and
     * records it in the download list. Failures go to [.b] which schedules a retry.
     */
    @JvmStatic
    fun saveImageAt(
        context: Context?,
        arrayList: ArrayList<*>,
        tmpDir: String?,
        index: Int,
        isSecret: Boolean,
        sessionId: Long,
        retryCount: Int,
        triesLeft: Int,
        downImageListener: DownImageListener
    ) {
        if (index < 0 || index >= arrayList.size) {
            return
        }
        val item = arrayList.get(index) as ImageItem?
        if (item == null) {
            return
        }
        if (TextUtils.isEmpty(item.url) || TextUtils.isEmpty(item.fileName)) {
            if (downImageListener.isRunning) {
                item.state = 4
                downImageListener.onImagesSaved(arrayList, isSecret)
            }
            return
        }
        item.state = 1
        val conn = MainUtil.openHttpConnection(0, 0, context, item.url, item.referer, false)
        if (conn == null) {
            if (downImageListener.isRunning) {
                fetchImage(context, arrayList, tmpDir, index, isSecret, sessionId, 0, triesLeft, item, null, false, downImageListener)
            }
            return
        }
        var tmpPath: String? = null
        var retry = retryCount
        var tries = triesLeft
        var redirect: String? = null
        var wrote = false
        var ok = false
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            tmpPath = tmpDir + "/" + item.fileName + index
            conn.setDoInput(true)
            conn.connect()
            val state = MainUtil.retryKind(conn.getResponseCode())
            retry = 0
            if (state == 1) {
                // server busy: retry later
                retry = retryCount + 1
            } else if (state == 2) {
                val location = conn.getHeaderField("Location")
                if (URLUtil.isNetworkUrl(location) && location != item.url) {
                    redirect = location
                }
            } else {
                `in` = conn.getInputStream()
                out = MainUtil.openFileOutput(tmpPath, false)
                val buffer = ByteArray(1024)
                try {
                    while (downImageListener.isRunning) {
                        val n = `in`.read(buffer, 0, 1024)
                        if (n == -1) {
                            break
                        }
                        out!!.write(buffer, 0, n)
                        tries = 0
                        wrote = true
                    }
                } catch (unused: EOFException) {
                }
                ok = downImageListener.isRunning
            }
        } catch (unused2: Exception) {
            ok = false
        }
        if (out != null) {
            try {
                out.close()
            } catch (unused3: Exception) {
            }
        }
        if (`in` != null) {
            try {
                `in`.close()
            } catch (unused4: Exception) {
            }
        }
        conn.disconnect()
        if (ok) {
            ok = DownSaveImage.saveFile(context, item, tmpPath!!, isSecret, sessionId)
        }
        if (!downImageListener.isRunning) {
            return
        }
        if (ok) {
            item.state = 3
            downImageListener.onImagesSaved(arrayList, isSecret)
            return
        }
        fetchImage(context, arrayList, tmpDir, index, isSecret, sessionId, retry, tries, item, redirect, wrote, downImageListener)
    }

    /** Moves the downloaded temp file into place (renaming animated images to .gif) and records it.  */
    private fun saveFile(
        context: Context?,
        item: ImageItem,
        tmpPath: String,
        isSecret: Boolean,
        sessionId: Long
    ): Boolean {
        val length = File(tmpPath).length()
        if (length <= 0) {
            return false
        }
        val uri = MainUri.createUri(context, item.dirPath, item.dirName, item.fileName)
        if (uri == null || !MainUtil.moveFileOrUri(context, tmpPath, uri.uriString)) {
            return false
        }
        var gifUri: UriItem? = null
        if (item.isGif) {
            var name = uri.name
            if (MainUtil.isAnimatedImage(context, uri.uriString) && !TextUtils.isEmpty(name)) {
                val dot = name!!.lastIndexOf(46.toChar())
                if (dot > 0 && dot < name.length) {
                    name = name.substring(0, dot)
                }
                gifUri = MainUri.createUri(context, item.dirPath, item.dirName, name + ".gif")
                if (gifUri != null && !MainUri.moveUri(context, uri.uriString, gifUri.uriString, true, true)) {
                    gifUri = null
                }
            }
        }
        val dirPath: String?
        val dirName: String?
        if (MainUri.isContentUri(item.dirPath)) {
            dirPath = item.dirPath
            dirName = item.dirName
        } else {
            val sb = StringBuilder().append(item.dirPath).append("/")
            sb.append(Environment.DIRECTORY_DOWNLOADS)
            if (TextUtils.isEmpty(item.dirName)) {
                dirPath = sb.toString()
                dirName = Environment.DIRECTORY_DOWNLOADS
            } else {
                sb.append("/")
                sb.append(item.dirName)
                dirPath = sb.toString()
                dirName = item.dirName
            }
        }
        val target = if (gifUri != null) gifUri else uri
        target.dirPath = dirPath
        target.dirName = dirName
        target.size = length
        addFileItem(context, item.url, item.referer, target, isSecret, sessionId)
        return true
    }

    /**
     * Schedules the next attempt for an image that did not download: wait 10 s while the server is busy
     * (`retry` counts those, up to 360), follow a redirect, retry once without the referer, or retry
     * after 2 s up to ZoneSettings.retryDown.value times. Otherwise the image is marked failed.
     */
    @JvmStatic
    fun fetchImage(
        context: Context?,
        arrayList: ArrayList<*>,
        tmpPath: String?,
        index: Int,
        isSecret: Boolean,
        sessionId: Long,
        retry: Int,
        triesLeft: Int,
        imageItem: ImageItem,
        url: String?,
        wrote: Boolean,
        downImageListener: DownImageListener
    ) {
        var tries = triesLeft
        val delay: Long
        if (retry > 0) {
            if (retry >= 360) {
                imageItem.state = 4
                downImageListener.onImagesSaved(arrayList, isSecret)
                return
            }
            delay = 10000
        } else if (!TextUtils.isEmpty(url)) {
            imageItem.url = url
            delay = 0
        } else if (!wrote && !TextUtils.isEmpty(imageItem.referer)) {
            imageItem.referer = null
            delay = 0
        } else if (tries < ZoneSettings.retryDown.value) {
            tries++
            delay = 2000
        } else {
            imageItem.state = 4
            downImageListener.onImagesSaved(arrayList, isSecret)
            return
        }
        try {
            if (delay == 0L) {
                MainApp.runInBackground(
                    context,
                    SaveImagesStep(
                        index,
                        retry,
                        tries,
                        sessionId,
                        context,
                        downImageListener,
                        imageItem,
                        tmpPath,
                        arrayList,
                        isSecret
                    )
                )
                return
            }
            val handler = downImageListener.uiHandler()
            if (handler == null) {
                imageItem.state = 4
                downImageListener.onImagesSaved(arrayList, isSecret)
                return
            }
            handler.postDelayed(
                ImagesSavedTask(
                    index,
                    retry,
                    tries,
                    sessionId,
                    context,
                    downImageListener,
                    imageItem,
                    tmpPath,
                    arrayList,
                    isSecret
                ), delay
            )
        } catch (unused: OutOfMemoryError) {
            imageItem.state = 4
            downImageListener.onImagesSaved(arrayList, isSecret)
        }
    }

    internal class ImagesSavedTask(@JvmField val index: Int,
                                   @JvmField val retry: Int,
                                   @JvmField val tries: Int,
                                   @JvmField val sessionId: Long,
                                   @JvmField val context: Context?,
                                   @JvmField val downImageListener: DownImageListener,
                                   @JvmField val imageItem: ImageItem,
                                   @JvmField val path: String?,
                                   @JvmField val items: ArrayList<*>,
                                   @JvmField val isSecret: Boolean
    ) : Runnable {
        override fun run() {
            val context = this.context
            val arrayList = this.items
            val path = this.path
            val index = this.index
            val isSecret = this.isSecret
            val sessionId = this.sessionId
            val retry = this.retry
            val tries = this.tries
            val imageItem = this.imageItem
            val downImageListener = this.downImageListener
            try {
                MainApp.runInBackground(
                    context,
                    SaveImagesStep(
                        index,
                        retry,
                        tries,
                        sessionId,
                        context,
                        downImageListener,
                        imageItem,
                        path,
                        arrayList,
                        isSecret
                    )
                )
            } catch (unused: OutOfMemoryError) {
                imageItem.state = 4.toByte()
                downImageListener.onImagesSaved(arrayList, isSecret)
            }
        }
    }

    internal class SaveImagesStep(@JvmField val index: Int,
                                   @JvmField val retry: Int,
                                   @JvmField val tries: Int,
                                   @JvmField val sessionId: Long,
                                   @JvmField val context: Context?,
                                   @JvmField val downImageListener: DownImageListener,
                                   @JvmField val imageItem: ImageItem,
                                   @JvmField val tmpPath: String?,
                                   @JvmField val items: ArrayList<*>,
                                   @JvmField val isOk: Boolean
    ) : Runnable {
        override fun run() {
            val arrayList = this.items
            val downImageListener = this.downImageListener
            if (downImageListener.isRunning) {
                try {
                    saveImageAt(
                        this.context,
                        arrayList,
                        this.tmpPath,
                        this.index,
                        this.isOk,
                        this.sessionId,
                        this.retry,
                        this.tries,
                        downImageListener
                    )
                } catch (unused: OutOfMemoryError) {
                    this.imageItem.state = 4.toByte()
                    downImageListener.onImagesSaved(arrayList, this.isOk)
                }
            }
        }
    }
}
