package com.mycompany.app.down

import android.content.Context
import com.mycompany.app.pref.store.ZoneSettings
import android.text.TextUtils
import android.util.Base64
import android.webkit.URLUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainDownSvc.DownSaveListener
import com.mycompany.app.main.MainDownSvc.EncItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZone
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Suppress("deprecation")
object DownSaveSplit {
    /**
     * Downloads one stream segment into `<temp>/<index>v` (or `...a` for audio segments),
     * decrypting AES-128 segments on the fly, and renames it to "...f" when complete. Segment byte
     * counts feed the size estimate [.f]. On errors [.g] schedules a retry.
     *
     * @param index   segment index (audio segments follow the video ones)
     * @param audio   segment belongs to the separate audio playlist
     * @param inline  `url` is base64 data (Vimeo init segments), not a url
     * @param wait    "server busy" retries so far, `retries` other failures so far
     * @param resumed true for retries (bytes already on disk were counted on the first attempt)
     */
    @JvmStatic
    fun saveSegment(
        context: Context?, item: DownItem, url: String?, index: Int, digits: Int, audio: Boolean,
        inline: Boolean, wait: Int, retries: Int, resumed: Boolean, listener: DownSaveListener
    ) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(item.path)) {
            if (item.status == 1) {
                listener.onStarted(item)
            }
            return
        }
        val path = if (audio)
            StringBuilder().append(item.segmentDir).append("/").append(MainUtil.zeroPadded(index, digits)).append("a").toString()
        else
            MainDownSvc.segmentName(index, digits, item.segmentDir)
        val donePath = path + "f"
        val done = File(donePath).length()
        if (done > 0) {
            if (!resumed) {
                item.totalSize += done
            }
            countSegment(item, audio, done)
            return
        }
        val have = MainUtil.fileSizeOf(context, path)
        if (!resumed) {
            item.totalSize += have
        }
        if (item.readSize != 0L && item.readSize < item.totalSize) {
            item.readSize = item.totalSize
        }
        // a segment cut out of a larger file (#EXT-X-BYTERANGE): its own URL and byte range (2026-11-20)
        val (plainUrl, rangeStart, rangeLength) = DownHls.split(url)
        var remote: Long = 0
        if (!inline && rangeLength > 0) {
            remote = rangeLength
            if (have >= remote) {
                File(path).renameTo(File(donePath))
                countSegment(item, audio, have)
                return
            }
        } else if (!inline && have > 0) {
            remote = MainUtil.contentLengthOf(context, url, item.referer)
            if (remote > 0 && have >= remote) {
                File(path).renameTo(File(donePath))
                countSegment(item, audio, have)
                return
            }
        }
        if (inline) {
            var ok = false
            var out: OutputStream? = null
            try {
                out = MainUtil.openFileOutput(path, false)
                val data = Base64.decode(url, Base64.NO_WRAP)
                item.totalSize += data.size.toLong()
                if (item.readSize != 0L && item.readSize < item.totalSize) {
                    item.readSize = item.totalSize
                }
                out!!.write(data, 0, data.size)
                ok = true
            } catch (unused: Exception) {
            }
            if (out != null) {
                try {
                    out.close()
                } catch (unused: Exception) {
                }
            }
            if (ok) {
                File(path).renameTo(File(donePath))
            } else if (item.status == 1) {
                saveSegmentRetry(
                    context,
                    item,
                    url,
                    index,
                    digits,
                    audio,
                    inline,
                    wait,
                    retries,
                    null,
                    false,
                    listener
                )
            }
            return
        }
        val cipher = if (audio)
            cipherFor(context, item, index - item.audioStart, item.audioKeys, item.audioSegments)
        else
            cipherFor(context, item, index, item.videoKeys, item.videoSegments)
        val conn = MainUtil.openHttpConnection(DownSavePath.CONNECT_TIMEOUT, DownSavePath.READ_TIMEOUT, context, plainUrl, item.referer, false)
        if (conn == null) {
            if (item.status == 1) {
                saveSegmentRetry(
                    context,
                    item,
                    url,
                    index,
                    digits,
                    audio,
                    inline,
                    0,
                    retries,
                    null,
                    false,
                    listener
                )
            }
            return
        }
        var retry = true
        var finished = false
        var progressed = false
        var nextWait = 0
        var location: String? = null
        var `in`: InputStream? = null
        var out: OutputStream? = null
        var permanent = false
        var retryAfter = 0L
        try {
            val ranged = rangeLength > 0
            if (ranged) {
                conn.setRequestProperty("Range", "bytes=" + (rangeStart + have) + "-" + (rangeStart + rangeLength - 1))
            } else if (have > 0) {
                val range = StringBuilder("bytes=").append(have).append("-")
                if (remote > 0) {
                    range.append(remote - 1)
                }
                conn.setRequestProperty("Accept-Ranges", "bytes")
                conn.setRequestProperty("Range", range.toString())
            }
            conn.setDoInput(true)
            conn.connect()
            val status = conn.getResponseCode()
            val code = MainUtil.retryKind(status)
            if (status in DownSavePath.PERMANENT) item.httpStatus = status
            if (status in DownSavePath.PERMANENT || (ranged && status == 200)) {
                // retrying cannot help (403 / 404 …, or a byte-range segment from a server without ranges)
                permanent = true
                retry = false
            } else if (code == 1) {
                nextWait = wait + 1
                retryAfter = DownSavePath.retryAfterMs(conn.getHeaderField("Retry-After"))
                if (status == 429) {
                    item.httpStatus = 429
                    listener.onFinished(item)
                }
            } else if (code == 2) {
                val target = conn.getHeaderField("Location")
                if (URLUtil.isNetworkUrl(target) && target != url) {
                    location = target
                }
            } else {
                `in` = conn.getInputStream()
                if (item.httpStatus == 429) item.httpStatus = 0 // data flows again
                out = MainUtil.openOutput(context, path, have > 0)
                val buf = ByteArray(DownSavePath.BUFFER_SIZE) // 64 KB (was 1 KB)
                var segmentBytes = have
                var read: Int = 0
                try {
                    while (item.status == 1 && (`in`.read(buf, 0, buf.size).also { read = it }) != -1) {
                        segmentBytes += read.toLong()
                        item.totalSize += read.toLong()
                        if (item.readSize != 0L && item.readSize < item.totalSize) {
                            item.readSize = item.totalSize
                        }
                        if (cipher != null) {
                            var plain: ByteArray?
                            try {
                                plain = cipher.update(buf, 0, read)
                            } catch (exception: Exception) {
                                plain = null
                            }
                            if (plain != null) {
                                out!!.write(plain, 0, plain.size)
                            } else {
                                out!!.write(buf, 0, read)
                            }
                        } else {
                            out!!.write(buf, 0, read)
                        }
                        progressed = true
                        val now = System.currentTimeMillis()
                        if (item.lastTime == 0L || now - item.lastTime > 800) {
                            item.lastTime = now
                            if (item.status == 1) {
                                listener.onFinished(item)
                            }
                        }
                    }
                } catch (unused: EOFException) {
                }
                if (cipher != null) {
                    try {
                        val tail = cipher.doFinal()
                        out!!.write(tail, 0, tail.size)
                    } catch (unused: Exception) {
                    }
                }
                retry = false
                if (item.status == 1) {
                    countSegment(item, audio, segmentBytes)
                    finished = true
                }
            }
        } catch (unused: Exception) {
            retry = true
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
        if (finished) {
            File(path).renameTo(File(donePath))
        }
        if (permanent && item.status == 1) {
            listener.onStarted(item)
            return
        }
        if (retry && item.status == 1) {
            saveSegmentRetry(
                context,
                item,
                url,
                index,
                digits,
                audio,
                inline,
                nextWait,
                if (progressed) 0 else retries,
                location,
                progressed,
                listener,
                retryAfter,
            )
        }
    }

    /** Adds a finished segment to the video (C/y) or audio (D/z) totals and re-estimates the size.  */
    private fun countSegment(item: DownItem, audio: Boolean, bytes: Long) {
        if (audio) {
            item.audioRead += bytes
            item.audioDone++
        } else {
            item.videoRead += bytes
            item.partsDone++
        }
        item.readSize = totalSize(item)
    }

    /** AES cipher for segment `index`, fetching (and caching) its key; null if unencrypted.  */
    private fun cipherFor(
        context: Context?,
        item: DownItem,
        index: Int,
        keys: MutableList<*>?,
        ivs: ArrayList<*>?
    ): Cipher? {
        if (keys == null || keys.isEmpty()) {
            return null
        }
        val key = encItemAt(index, keys)
        var keyBytes: ByteArray? = null
        if (key != null) {
            if (key.fetched == null) {
                val fetched = fetchBytes(context, key.keyUri, item.referer)
                if (fetched != null) {
                    key.fetched = fetched
                }
            }
            keyBytes = key.fetched
        }
        return makeCipher(keyBytes, ivFor(index, ivs))
    }

    @JvmStatic
    fun makeCipher(key: ByteArray?, iv: ByteArray?): Cipher? {
        var iv = iv
        if (key == null) {
            return null
        }
        if (iv == null) {
            iv = ByteArray(16)
        }
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(2, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            return cipher
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun encItemAt(index: Int, list: MutableList<*>?): EncItem? {
        val encItem: EncItem? = if (list == null || index < 0 || index >= list.size) null else (list.get(index) as EncItem?)
        if (list == null || index < 0 || index >= list.size || encItem == null) {
            return null
        }
        val lastKeyIndex = encItem.lastKeyIndex
        if (!TextUtils.isEmpty(encItem.keyUri) || index == lastKeyIndex) {
            return encItem
        }
        if (lastKeyIndex >= 0 && lastKeyIndex < list.size) {
            return list.get(lastKeyIndex) as EncItem?
        }
        return null
    }

    /** Downloads an AES key from `url`, zero-padded to a multiple of 16 bytes; null on failure.  */
    @JvmStatic
    fun fetchBytes(context: Context?, url: String?, referer: String?): ByteArray? {
        val conn = MainUtil.openHttpConnection(DownSavePath.CONNECT_TIMEOUT, DownSavePath.READ_TIMEOUT, context, url, referer, false)
        if (conn == null) {
            return null
        }
        var data: ByteArray? = null
        var `in`: InputStream? = null
        var out: ByteArrayOutputStream? = null
        try {
            conn.setDoInput(true)
            conn.connect()
            `in` = conn.getInputStream()
            out = ByteArrayOutputStream()
            val buf = ByteArray(DownSavePath.BUFFER_SIZE) // 64 KB (was 1 KB)
            var read: Int = 0
            try {
                while ((`in`.read(buf, 0, buf.size).also { read = it }) != -1) {
                    out.write(buf, 0, read)
                }
            } catch (unused: EOFException) {
            }
            data = out.toByteArray()
        } catch (unused: Exception) {
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
        return pad16(data)
    }

    private fun pad16(data: ByteArray?): ByteArray? {
        if (data == null || data.size == 0) {
            return null
        }
        val rest = data.size % 16
        if (rest == 0) {
            return data
        }
        val padded = ByteArray(data.size + 16 - rest)
        System.arraycopy(data, 0, padded, 0, data.size)
        return padded
    }

    /** IV bytes of segment `index` parsed from its hex IV attribute (cached in the item).  */
    @JvmStatic
    fun ivFor(index: Int, ivs: ArrayList<*>?): ByteArray? {
        val item = encItemAt(index, ivs)
        if (item == null) {
            return null
        }
        if (item.fetched != null) {
            return item.fetched
        }
        var hex = item.keyUri
        if (!TextUtils.isEmpty(hex)) {
            if (hex!!.startsWith("0x") || hex.startsWith("0X")) {
                hex = hex.substring(2)
            }
            if (!TextUtils.isEmpty(hex)) {
                try {
                    var bytes = BigInteger(hex, 16).toByteArray()
                    if (bytes.size >= 2 && bytes[0].toInt() == 0) {
                        // Drop BigInteger's sign byte.
                        val trimmed = ByteArray(bytes.size - 1)
                        System.arraycopy(bytes, 1, trimmed, 0, trimmed.size)
                        bytes = trimmed
                    }
                    val padded = pad16(bytes)
                    if (padded != null) {
                        item.fetched = padded
                    }
                } catch (unused: Exception) {
                }
            }
        }
        return item.fetched
    }

    @JvmStatic
    fun totalSize(downItem: DownItem): Long {
        val audioSize: Long
        val audioPart: Long
        var partsDone = downItem.partsDone
        var audioStart = 1
        if (partsDone < 1 || partsDone > (downItem.audioStart.also { audioStart = it })) {
            partsDone = audioStart
        }
        val videoSize = (downItem.videoRead / partsDone) * (downItem.audioStart.toLong())
        val segmentCount = downItem.segmentCount
        if (segmentCount > 0) {
            var audioDone = downItem.audioDone
            if (audioDone > 0) {
                if (audioDone > segmentCount) {
                    audioDone = segmentCount
                }
                audioPart = (downItem.audioRead / audioDone)
            } else {
                audioPart = downItem.audioOffset
            }
            audioSize = audioPart * (segmentCount.toLong())
        } else {
            audioSize = 0
        }
        val readSize = downItem.readSize
        return if (readSize == 0L) videoSize + audioSize else (((readSize + videoSize) + audioSize) / 2.0f).toLong()
    }

    /** Schedules the next attempt of a segment; same rules as [DownSavePath.retryDelay].  */
    @JvmStatic
    fun saveSegmentRetry(
        context: Context?, item: DownItem, url: String?, index: Int, digits: Int, audio: Boolean,
        inline: Boolean, wait: Int, retries: Int, location: String?, progressed: Boolean,
        listener: DownSaveListener, retryAfterMs: Long = 0,
    ) {
        var retries = retries
        val delay: Long
        var next = url
        // the same rules as plain downloads (DownSavePath): Retry-After / growing waits, backoff 2 … 60 s
        if (wait > 0) {
            if (wait >= DownSavePath.MAX_BUSY_TRIES) {
                listener.onPaused(item)
                return
            }
            delay = DownSavePath.busyDelay(wait, retryAfterMs)
        } else if (!TextUtils.isEmpty(location)) {
            delay = 0
            // keep a segment's byte range on the redirected address
            val r = DownHls.split(url)
            next = if (r.third > 0) DownHls.withRange(location!!, r.second, r.third) else location
        } else if (!progressed && !TextUtils.isEmpty(item.referer)) {
            item.referer = null
            delay = 0
        } else if (retries >= ZoneSettings.retryDown.value) {
            listener.onStarted(item)
            return
        } else {
            retries++
            delay = DownSavePath.retryDelay(retries)
        }
        try {
            if (delay == 0L) {
                DownPool.execute(FailedTask(
                        index,
                        digits,
                        wait,
                        retries,
                        context,
                        item,
                        listener,
                        next,
                        audio,
                        inline
                    )
                )
                return
            }
            val handler = listener.uiHandler()
            if (handler == null) {
                listener.onStarted(item)
                return
            }
            handler.postDelayed(
                StartedTask(
                    index,
                    digits,
                    wait,
                    retries,
                    context,
                    item,
                    listener,
                    next,
                    audio,
                    inline
                ), delay
            )
        } catch (unused: OutOfMemoryError) {
            item.outOfMemory = true
            listener.onStarted(item)
        }
    }

    internal class StartedTask(@JvmField val index: Int,
                                   @JvmField val digits: Int,
                                   @JvmField val wait: Int,
                                   @JvmField val number2x: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downItem: DownItem,
                                   @JvmField val downSaveListener: DownSaveListener,
                                   @JvmField val path: String?,
                                   @JvmField val isSecret: Boolean,
                                   @JvmField val resumed: Boolean
    ) : Runnable {
        override fun run() {
            val context = this.context
            val downItem = this.downItem
            val path = this.path
            val index = this.index
            val digits = this.digits
            val isSecret = this.isSecret
            val resumed = this.resumed
            val wait = this.wait
            val number2x = this.number2x
            val downSaveListener = this.downSaveListener
            try {
                DownPool.execute(FailedTask(index, digits, wait, number2x, context, downItem, downSaveListener, path, isSecret, resumed)
                )
            } catch (unused: OutOfMemoryError) {
                downItem.outOfMemory = true
                downSaveListener.onStarted(downItem)
            }
        }
    }

    internal class FailedTask(@JvmField val index: Int,
                                   @JvmField val digits: Int,
                                   @JvmField val wait: Int,
                                   @JvmField val retries: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downItem: DownItem,
                                   @JvmField val downSaveListener: DownSaveListener,
                                   @JvmField val url: String?,
                                   @JvmField val audio: Boolean,
                                   @JvmField val inline: Boolean
    ) : Runnable {
        override fun run() {
            val downSaveListener = this.downSaveListener
            val downItem = this.downItem
            if (downItem.status == 1) {
                try {
                    saveSegment(
                        this.context,
                        downItem,
                        this.url,
                        this.index,
                        this.digits,
                        this.audio,
                        this.inline,
                        this.wait,
                        this.retries,
                        true,
                        downSaveListener
                    )
                    if (downItem.status == 1) {
                        downSaveListener.onFailed(downItem)
                    }
                } catch (unused: OutOfMemoryError) {
                    downItem.outOfMemory = true
                    downSaveListener.onStarted(downItem)
                }
            }
        }
    }
}
