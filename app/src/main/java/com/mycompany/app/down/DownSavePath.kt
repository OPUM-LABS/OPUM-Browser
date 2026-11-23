package com.mycompany.app.down

import android.content.Context
import com.mycompany.app.pref.store.ZoneSettings
import android.text.TextUtils
import android.webkit.URLUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainDownSvc.DownSaveListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZone
import java.io.EOFException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Suppress("deprecation")
object DownSavePath {
    /**
     * Downloads one range part of a plain HTTP download into `<temp>/<part>v`, renaming it to
     * "...vf" when complete. Progress goes to `item.p`; on errors [.b] schedules a retry.
     *
     * @param part    part index, `digits` its zero-padded width
     * @param wait    number of "server busy" retries so far
     * @param retries number of other failed attempts so far
     * @param resumed true for retries (the bytes already on disk were counted on the first attempt)
     */
    @JvmStatic
    fun savePathPart(
        context: Context?,
        item: DownItem?,
        url: String?,
        part: Int,
        digits: Int,
        wait: Int,
        retries: Int,
        resumed: Boolean,
        listener: DownSaveListener
    ) {
        if (item == null) {
            return
        }
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(item.path)) {
            if (item.status == 1) {
                listener.onStarted(item)
            }
            return
        }
        val start: Long
        val length: Long
        if (item.partCount == 1) {
            start = 0
            length = item.videoLength
        } else {
            start = part * item.partSize
            length = if (part == item.partCount - 1) item.videoLength - start else item.partSize
        }
        val path = MainDownSvc.segmentName(part, digits, item.segmentDir)
        val attempt =
            transfer(context, item, url, path, start, length, item.partCount == 1, wait, resumed, listener)
        if (attempt == null) {
            return
        }
        if (attempt.noConnection) {
            if (item.status == 1) {
                fetchPathPart(context, item, url, part, digits, 0, retries, null, false, listener)
            }
            return
        }
        if (item.status != 1) {
            return
        }
        if (attempt.permanent) {
            // 403 / 404 / 410 …, or a server that ignores ranges for a split download: retrying cannot help
            listener.onStarted(item)
            return
        }
        if (attempt.retry) {
            fetchPathPart(
                context,
                item,
                url,
                part,
                digits,
                attempt.wait,
                if (attempt.progressed) 0 else retries,
                attempt.location,
                attempt.progressed,
                listener,
                attempt.retryAfterMs,
            )
        } else {
            listener.onFailed(item)
        }
    }

    /**
     * Shared by video parts and the separate audio stream. Returns null when the part was already
     * complete (the listener has been told), otherwise what the caller should do next.
     *
     * @param single whether this is the only stream (a range header is then sent only to resume)
     */
    @JvmStatic
    fun transfer(
        context: Context?, item: DownItem, url: String?, path: String, start: Long, length: Long,
        single: Boolean, wait: Int, resumed: Boolean, listener: DownSaveListener
    ): Attempt? {
        val donePath = path + "f"
        val done = File(donePath).length()
        if (done > 0) {
            countExisting(item, done, resumed)
            if (item.status == 1) {
                listener.onFailed(item)
            }
            return null
        }
        val have = MainUtil.fileSizeOf(context, path)
        countExisting(item, have, resumed)
        if (length > 0 && have >= length) {
            File(path).renameTo(File(donePath))
            if (item.status == 1) {
                listener.onFailed(item)
            }
            return null
        }
        val result = Attempt()
        // timeouts (they were none at all: a stalled server held the download, and its worker thread, forever)
        val conn = MainUtil.openHttpConnection(CONNECT_TIMEOUT, READ_TIMEOUT, context, url, item.referer, false)
        if (conn == null) {
            result.noConnection = true
            return result
        }
        result.retry = true
        var `in`: InputStream? = null
        var out: OutputStream? = null
        var finished = false
        var have2 = have
        try {
            val rangeSent = !single || have > 0
            if (rangeSent) {
                val range = StringBuilder("bytes=").append(start + have).append("-")
                if (length > 0) {
                    range.append(start + length - 1)
                }
                conn.setRequestProperty("Accept-Ranges", "bytes")
                conn.setRequestProperty("Range", range.toString())
            }
            conn.setDoInput(true)
            conn.connect()
            val status = conn.getResponseCode()
            val code = MainUtil.retryKind(status)
            if (rangeSent && status == 200) {
                // The server ignored the range and sends the whole file (2026-11-20: this used to be written into
                // every part, giving a broken file ~10× too large). A resumed single download starts over; a split
                // download cannot continue.
                if (single) {
                    countExisting(item, -have2, false)
                    have2 = 0
                } else {
                    item.rangeIgnored = true
                    result.permanent = true
                    result.retry = false
                }
            }
            if (result.permanent) {
                // handled below (nothing is read)
            } else if (status in PERMANENT) {
                item.httpStatus = status
                result.permanent = true
                result.retry = false
            } else if (code == 1) {
                // Server busy / temporarily unavailable: retry later with a longer delay (or when it says).
                result.wait = wait + 1
                result.retryAfterMs = retryAfterMs(conn.getHeaderField("Retry-After"))
                // 429: the list says the site is limiting downloads while it waits
                if (status == 429) {
                    item.httpStatus = 429
                    listener.onFinished(item)
                }
            } else if (code == 2) {
                val location = conn.getHeaderField("Location")
                if (URLUtil.isNetworkUrl(location) && location != url) {
                    result.location = location
                }
            } else {
                `in` = conn.getInputStream()
                if (item.httpStatus == 429) item.httpStatus = 0 // data flows again
                out = MainUtil.openOutput(context, path, have2 > 0)
                // 64 KB reads (was 1 KB: many more system calls at high speed)
                val buf = ByteArray(BUFFER_SIZE)
                var read: Int = 0
                try {
                    while (item.status == 1 && (`in`.read(buf, 0, buf.size).also { read = it }) != -1) {
                        addBytes(item, read)
                        out!!.write(buf, 0, read)
                        result.progressed = true
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
                finished = item.status == 1
                result.retry = false
            }
        } catch (unused: Exception) {
            result.retry = true
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
        return result
    }

    /** Counts bytes already on disk once (on the first attempt) and keeps totals consistent.  */
    private fun countExisting(item: DownItem, bytes: Long, resumed: Boolean) {
        if (!resumed) {
            item.totalSize += bytes
        }
        if (item.readSize != 0L && item.readSize < item.totalSize) {
            item.readSize = item.totalSize
        }
        if (item.totalSize > item.lastSize) {
            item.lastSize = 0
        }
    }

    private fun addBytes(item: DownItem, bytes: Int) {
        item.totalSize += bytes.toLong()
        if (item.readSize != 0L && item.readSize < item.totalSize) {
            item.readSize = item.totalSize
        }
        // the shown progress is smoothed while catching up after a resume.
        if (item.lastSize > 0) {
            item.lastSize += (bytes / 10).toLong()
            if (item.totalSize > item.lastSize) {
                item.lastSize = 0
            } else if (item.lastSize > item.readSize) {
                item.lastSize = item.readSize
            }
        }
    }

    /**
     * Schedules the next attempt of a part:
     *
     *  * server busy (`wait > 0`): after its Retry-After or 10 / 20 / 40 / 60 s, pausing after [MAX_BUSY_TRIES] tries;
     *  * redirected (`location`): immediately at the new url;
     *  * first failure without progress while a referer is set: immediately without it;
     *  * otherwise after 2, 4, 8 … 60 s ([retryDelay]), failing after [ZoneSettings.retryDown.value] retries.
     *  Permanent answers (403, 404, …) fail at once (see [Attempt.permanent]).
     *
     */
    @JvmStatic
    fun fetchPathPart(
        context: Context?,
        item: DownItem?,
        url: String?,
        part: Int,
        digits: Int,
        wait: Int,
        retries: Int,
        location: String?,
        progressed: Boolean,
        listener: DownSaveListener,
        retryAfterMs: Long = 0,
    ) {
        var retries = retries
        if (item == null) {
            return
        }
        val delay: Long
        var next = url
        if (wait > 0) {
            if (wait >= MAX_BUSY_TRIES) {
                listener.onPaused(item)
                return
            }
            delay = busyDelay(wait, retryAfterMs)
        } else if (!TextUtils.isEmpty(location)) {
            delay = 0
            next = location
        } else if (!progressed && !TextUtils.isEmpty(item.referer)) {
            item.referer = null
            delay = 0
        } else if (retries >= ZoneSettings.retryDown.value) {
            listener.onStarted(item)
            return
        } else {
            retries++
            delay = retryDelay(retries)
        }
        try {
            if (delay == 0L) {
                DownPool.execute(SaveStep(part, digits, wait, retries, context, item, listener, next))
                return
            }
            val handler = listener.uiHandler()
            if (handler == null) {
                listener.onStarted(item)
                return
            }
            handler.postDelayed(
                StartedTask(
                    part,
                    digits,
                    wait,
                    retries,
                    context,
                    item,
                    listener,
                    next
                ), delay
            )
        } catch (unused: OutOfMemoryError) {
            item.outOfMemory = true
            listener.onStarted(item)
        }
    }

    internal class StartedTask(@JvmField val part: Int,
                                   @JvmField val digits: Int,
                                   @JvmField val wait: Int,
                                   @JvmField val retries: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downItem: DownItem?,
                                   @JvmField val downSaveListener: DownSaveListener,
                                   @JvmField val sub: String?
    ) : Runnable {
        override fun run() {
            val context = this.context
            val sub = this.sub
            val part = this.part
            val digits = this.digits
            val wait = this.wait
            val retries = this.retries
            val downSaveListener = this.downSaveListener
            val downItem = this.downItem
            if (downItem != null) {
                try {
                    DownPool.execute(SaveStep(part, digits, wait, retries, context, downItem, downSaveListener, sub))
                } catch (unused: OutOfMemoryError) {
                    downItem.outOfMemory = true
                    downSaveListener.onStarted(downItem)
                }
            }
        }
    }

    internal class SaveStep(@JvmField val part: Int,
                                   @JvmField val digits: Int,
                                   @JvmField val wait: Int,
                                   @JvmField val retries: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downItem: DownItem,
                                   @JvmField val downSaveListener: DownSaveListener,
                                   @JvmField val sub: String?
    ) : Runnable {
        override fun run() {
            val downSaveListener = this.downSaveListener
            val downItem = this.downItem
            if (downItem.status != 1) {
                return
            }
            try {
                savePathPart(this.context, downItem, this.sub, this.part, this.digits, this.wait, this.retries, true, downSaveListener)
            } catch (unused: OutOfMemoryError) {
                downItem.outOfMemory = true
                downSaveListener.onStarted(downItem)
            }
        }
    }

    /** Outcome of one [.transfer] attempt.  */
    class Attempt {
        @JvmField
        var noConnection: Boolean = false
        @JvmField
        var retry: Boolean = false
        @JvmField
        var wait: Int = 0
        @JvmField
        var location: String? = null
        @JvmField
        var progressed: Boolean = false
        /** Retrying cannot help (403, 404, 410, …, or a split download whose server ignores ranges). */
        @JvmField
        var permanent: Boolean = false
        /** The server's Retry-After, in ms (0 = none). */
        @JvmField
        var retryAfterMs: Long = 0
    }

    // ---- 2026-11-20: timeouts, buffer, retry policy -------------------------------------------------------------

    const val CONNECT_TIMEOUT = 15_000
    const val READ_TIMEOUT = 60_000
    const val BUFFER_SIZE = 64 * 1024

    /** Answers that will not change on a retry. */
    @JvmField
    val PERMANENT = setOf(400, 401, 403, 404, 405, 410, 411, 412, 414, 416, 451)

    /** "Server busy" attempts before the download is paused (with the backoff below, about an hour). */
    const val MAX_BUSY_TRIES = 60

    /** Delay before retry number [retries] (1, 2, …) after a failure: 2, 4, 8 … s up to 60 s, plus up to 1 s jitter. */
    @JvmStatic
    fun retryDelay(retries: Int): Long {
        val base = 2000L shl (retries - 1).coerceIn(0, 5)
        return minOf(base, 60_000L) + (Math.random() * 1000).toLong()
    }

    /** Delay after "server busy" number [wait]: the server's Retry-After (≤ 5 min), else 10, 20, 40, 60, 60 … s. */
    @JvmStatic
    fun busyDelay(wait: Int, retryAfterMs: Long): Long {
        if (retryAfterMs > 0) return minOf(retryAfterMs, 300_000L)
        return minOf(10_000L shl (wait - 1).coerceIn(0, 3), 60_000L) + (Math.random() * 1000).toLong()
    }

    /** Retry-After as seconds or an HTTP date, in ms (0 when missing or unreadable). */
    @JvmStatic
    fun retryAfterMs(value: String?): Long {
        val v = value?.trim() ?: return 0
        v.toLongOrNull()?.let { return (it * 1000).coerceAtLeast(0) }
        return try {
            val date = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US).parse(v)
            ((date?.time ?: 0L) - System.currentTimeMillis()).coerceAtLeast(0)
        } catch (_: Exception) { 0 }
    }
}
