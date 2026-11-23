package com.mycompany.app.down

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.LoopCancelListener
import java.util.Locale
import kotlin.math.max

@Suppress("deprecation")
object DownParseTsfile {
    /**
     * Rebuilds the segment list of a stream from one sample segment url (e.g. ".../seg-12-v1.ts")
     * by locating the counter in the file name and probing which neighbouring numbers exist.
     *
     * @param anyExt the sample may have any extension (not just ".ts")
     */
    @JvmStatic
    fun findSegments(
        context: Context?, url: String?, referer: String?, item: DownItem?, anyExt: Boolean,
        cancel: LoopCancelListener?
    ): ArrayList<String?>? {
        if (item == null && cancel == null) {
            return null
        }
        val sample = MainUtil.urlFromText(url)
        if (TextUtils.isEmpty(sample)) {
            return null
        }
        val ext = if (anyExt) sample!!.lastIndexOf('.') else sample!!.lastIndexOf(".ts")
        if (ext <= 0 || ext >= sample.length) {
            return null
        }
        val slash = sample.lastIndexOf('/', ext - 1)
        if (slash <= 0) {
            return null
        }
        var found: ArrayList<String?>? = null
        var firstStart = -1
        var firstEnd = -1
        val marker = sample.lastIndexOf("-v1-a1", ext - 1)
        var skipTail = (marker > slash && marker < ext)
        if (!skipTail) {
            val host = MainUtil.hostOf(sample, false)
            skipTail = !TextUtils.isEmpty(host) && host!!.endsWith("cdn.qooqlevideo.com")
        }
        if (!skipTail) {
            // Last digit run before the extension.
            var start = -1
            var end = -1
            for (end2 in ext - 1 downTo slash + 1) {
                val c = sample.get(end2)
                if (c >= '0' && c <= '9') {
                    if (end == -1) {
                        end = end2 + 1
                    }
                    start = end2
                } else if (end != -1) {
                    break
                }
            }
            if (start != -1 && end != -1) {
                firstStart = DownParseTsfile.numberStart(sample, start, end, anyExt)
                firstEnd = end
                found = scanRange(context, sample, referer, firstStart, firstEnd, item, cancel)
                if (found != null && found.size > 1) {
                    return found
                }
            }
        }
        // First digit run of the file name.
        var start = -1
        var end = -1
        for (end2 in slash + 1..<ext) {
            val c = sample.get(end2)
            if (c >= '0' && c <= '9') {
                if (start == -1) {
                    start = end2
                }
                end = end2 + 1
            } else if (start != -1) {
                break
            }
        }
        if (start != -1 && end != -1) {
            val counter = DownParseTsfile.numberStart(sample, start, end, anyExt)
            if (counter != firstStart && end != firstEnd) {
                return scanRange(context, sample, referer, counter, end, item, cancel)
            }
        }
        return found
    }

    @JvmStatic
    fun numberStart(sample: String, start: Int, end: Int, anyExt: Boolean): Int {
        if (anyExt) {
            val tripleZero = sample.indexOf("000", start)
            if (tripleZero >= start && tripleZero < end - 3) {
                return tripleZero
            }
            val doubleZero = sample.indexOf("00", start)
            if (doubleZero >= start && doubleZero < end - 2) {
                return doubleZero
            }
            val singleZero = sample.indexOf(48.toChar(), start)
            if (singleZero >= start && singleZero < end - 1) {
                return singleZero
            }
        }
        return max(start, end - 5)
    }

    @JvmStatic
    fun probeUp(
        context: Context?,
        last: Int,
        step: Int,
        prefix: String?,
        format: String,
        suffix: String?,
        referer: String?,
        downItem: DownItem?,
        loopCancelListener: LoopCancelListener?
    ): Int {
        var context = context
        var last = last
        var prefix = prefix
        var format = format
        var suffix = suffix
        var referer = referer
        var index = last + step
        while (index < 100000) {
            if (!isCancelled(downItem, loopCancelListener)) {
                val context2 = context
                val prefix2 = prefix
                val format2 = format
                val suffix2 = suffix
                val referer2 = referer
                if (!probeDown(context2, index, prefix2, format2, suffix2, referer2)) {
                    break
                }
                last = index
                prefix = prefix2
                format = format2
                suffix = suffix2
                referer = referer2
                index += step
                context = context2
            } else {
                return -1
            }
        }
        return last
    }

    /**
     * Segment urls `prefix + counter + suffix` where `url[start:end]` is the counter:
     * finds the first existing number (0-9) and the last one by probing in shrinking steps.
     */
    @JvmStatic
    fun scanRange(
        context: Context?, url: String?, referer: String?, start: Int, end: Int, item: DownItem?,
        cancel: LoopCancelListener?
    ): ArrayList<String?>? {
        if ((item == null && cancel == null) || TextUtils.isEmpty(url)) {
            return null
        }
        val prefix = url!!.substring(0, start)
        val suffix = url.substring(end)
        val digits = url.substring(start, end)
        if (TextUtils.isEmpty(prefix) || TextUtils.isEmpty(suffix) || MainUtil.parseColorSafe(digits) == -1) {
            return null
        }
        val format =
            if (digits.length > 1 && digits.get(0) == '0') "%0" + digits.length + "d" else "%d"
        var first = -1
        run {
            var first2 = 0
            while (first2 < 10 && !DownParseTsfile.isCancelled(item, cancel)) {
                if (DownParseTsfile.probeDown(context, first2, prefix, format, suffix, referer)) {
                    first = first2
                    break
                }
                first2++
            }
        }
        if (first == -1 || isCancelled(item, cancel)) {
            return null
        }
        var last: Int = 0
        if (probeDown(context, 800, prefix, format, suffix, referer)) {
            last = probeUp(context, 800, 100, prefix, format, suffix, referer, item, cancel)
        } else {
            last = -1
            var first2 = 700
            while (first2 > -1 && !isCancelled(item, cancel)) {
                if (probeDown(context, first2, prefix, format, suffix, referer)) {
                    last = first2
                    break
                }
                first2 -= 100
            }
        }
        if (last >= 99900) {
            return null
        }
        if (last == -1) {
            last = first
        }
        for (step in intArrayOf(50, 20, 10, 5, 2, 1)) {
            last = probeUp(context, last, step, prefix, format, suffix, referer, item, cancel)
        }
        val list: ArrayList<String?> = ArrayList()
        for (first2 in first..last) {
            if (isCancelled(item, cancel)) {
                return null
            }
            list.add(segmentUrl(first2, prefix, format, suffix))
        }
        return list
    }

    @JvmStatic
    fun segmentUrl(last: Int, prefix: String?, format: String, suffix: String?): String {
        return prefix + String.format(Locale.US, format, last) + suffix
    }

    @JvmStatic
    fun isCancelled(downItem: DownItem?, loopCancelListener: LoopCancelListener?): Boolean {
        if (downItem != null) {
            return downItem.status == 6
        }
        return loopCancelListener != null && loopCancelListener.isCancelled
    }

    @JvmStatic
    fun probeDown(
        context: Context?,
        index: Int,
        prefix: String?,
        format: String,
        suffix: String?,
        referer: String?
    ): Boolean {
        var contentType: String?
        val segUrl = segmentUrl(index, prefix, format, suffix)
        val httpURLConnectionQ = MainUtil.openHeadConnection(context, segUrl, referer, 0, 0, false, MainUtil.userAgentPref(), true)
        var found = false
        if (httpURLConnectionQ == null) {
            return false
        }
        try {
            httpURLConnectionQ.setDoInput(true)
            httpURLConnectionQ.connect()
            if (httpURLConnectionQ.getResponseCode() == 200) {
                try {
                    contentType = httpURLConnectionQ.getContentType()
                } catch (unused: Exception) {
                    contentType = null
                }
                if (TextUtils.isEmpty(contentType) || contentType!!.startsWith("image/")) {
                    if (httpURLConnectionQ.getContentLengthLong() > 0) {
                        found = true
                    }
                } else if (contentType.startsWith("text/html")) {
                    val mime = MainUtil.mimeForPath(segUrl)
                    if (!TextUtils.isEmpty(mime) && mime!!.startsWith("text/html")) {
                        found = true
                    }
                } else {
                    found = true
                }
            }
        } catch (unused2: Exception) {
        }
        httpURLConnectionQ.disconnect()
        return found
    }
}
