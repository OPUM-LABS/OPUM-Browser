package com.mycompany.app.down

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.LoopCancelListener
import java.util.Locale

@Suppress("deprecation")
object DownParseKakao {
    @JvmStatic
    fun parseItemText(url: String?): M3u8Item? {
        val urlText = MainUtil.urlFromText(url)
        val parts: Array<String?>? = if (TextUtils.isEmpty(urlText)) null else urlText!!.split("<,>".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        val newUrl: String? = if (TextUtils.isEmpty(urlText) || parts == null || parts.size != 5) null else parts[0]
        val template: String? = if (TextUtils.isEmpty(urlText) || parts == null || parts.size != 5 || newUrl == null) null else parts[1]
        val src: String? = if (TextUtils.isEmpty(urlText) || parts == null || parts.size != 5 || newUrl == null || template == null) null else parts[2]
        if (TextUtils.isEmpty(urlText) || parts == null || parts.size != 5 || newUrl == null || template == null || src == null) {
            return null
        }
        var audioTemplate = parts[3]
        val audioPart = parts[4]
        if ("isNull" == audioTemplate) {
            audioTemplate = null
        }
        val audioUrl = if ("isNull" == audioPart) null else audioPart
        val m3u8Item = M3u8Item()
        m3u8Item.url = newUrl
        m3u8Item.template = template
        m3u8Item.src = src
        m3u8Item.audioTemplate = audioTemplate
        m3u8Item.audioUrl = audioUrl
        return m3u8Item
    }

    /**
     * Segment urls of a Kakao DASH stream: the init segment (`init`) followed by the media
     * template (`media`, numbered) from the first to the last existing segment, all relative
     * to the manifest's folder and carrying the manifest's query string.
     */
    @JvmStatic
    fun segmentUrls(
        context: Context?, manifest: String?, referer: String?, init: String?, media: String?,
        item: DownItem?, cancel: LoopCancelListener?
    ): ArrayList<String?>? {
        val mpd = if ((item == null && cancel == null) || TextUtils.isEmpty(manifest) || TextUtils.isEmpty(
                init
            ) || TextUtils.isEmpty(media)) 0 else manifest!!.indexOf(".mpd")
        val slash = if ((item == null && cancel == null) || TextUtils.isEmpty(manifest) || TextUtils.isEmpty(
                init
            ) || TextUtils.isEmpty(media) || mpd <= 0) 0 else manifest!!.lastIndexOf(
                '/',
                mpd - 1
            )
        if ((item == null && cancel == null) || TextUtils.isEmpty(manifest) || TextUtils.isEmpty(
                init
            ) || TextUtils.isEmpty(media) || mpd <= 0 || slash == -1) {
            return null
        }
        val base = manifest!!.substring(0, slash + 1)
        var query: String? = null
        val afterMpd = mpd + 4
        val q = if (afterMpd < manifest.length) manifest.lastIndexOf('?', afterMpd) else 0
        if (afterMpd < manifest.length && q != -1) {
            query = manifest.substring(q)
        }
        var first = -1
        run {
            var first2 = 0
            while (first2 < 10 && !DownParseKakao.isCancelled(item, cancel)) {
                if (DownParseKakao.segmentExists(context, first2, base, media!!, query, referer)) {
                    first = first2
                    break
                }
                first2++
            }
        }
        if (first == -1) {
            return null
        }
        var last = DownParseKakao.knownLast(context, base, media!!, query, referer, item, cancel)
        if (last == -1 && !isCancelled(item, cancel)) {
            var guess: Int = 0
            if (DownParseKakao.segmentExists(context, 100, base, media, query, referer)) {
                guess =
                    DownParseKakao.probeSegments(context, 100, 50, base, media, query, referer, item, cancel)
            } else {
                guess = -1
                var first2 = 50
                while (first2 > -1 && !isCancelled(item, cancel)) {
                    if (DownParseKakao.segmentExists(context, first2, base, media, query, referer)) {
                        guess = first2
                        break
                    }
                    first2 -= 50
                }
            }
            last = if (guess == -1) first else guess
            for (step in intArrayOf(20, 10, 5, 2, 1)) {
                last = DownParseKakao.probeSegments(
                    context,
                    last,
                    step,
                    base,
                    media,
                    query,
                    referer,
                    item,
                    cancel
                )
            }
        }
        val list: ArrayList<String?> = ArrayList()
        list.add(base + init + (if (TextUtils.isEmpty(query)) "" else query))
        for (first2 in first..last) {
            if (!isCancelled(item, cancel)) {
                list.add(DownParseKakao.segmentUrl(first2, base, media, query))
            }
        }
        return list
    }

    /** item.w - 1 when the playlist's segment count is confirmed (that segment exists, the next doesn't).  */
    private fun knownLast(
        context: Context?, base: String, media: String, query: String?, referer: String?,
        item: DownItem?, cancel: LoopCancelListener?
    ): Int {
        if (item == null || item.audioStart <= 1 || isCancelled(item, cancel)) {
            return -1
        }
        val last = item.audioStart - 1
        if (segmentExists(context, last, base, media, query, referer) && !isCancelled(item, cancel) && !segmentExists(
                context,
                item.audioStart,
                base,
                media,
                query,
                referer
            )
        ) {
            return last
        }
        return -1
    }

    @JvmStatic
    fun probeSegments(
        context: Context?,
        last: Int,
        step: Int,
        base: String,
        media: String,
        query: String?,
        referer: String?,
        downItem: DownItem?,
        loopCancelListener: LoopCancelListener?
    ): Int {
        var context = context
        var last = last
        var base = base
        var media = media
        var query = query
        var referer = referer
        var index = last + step
        while (index < 100000) {
            if (!isCancelled(downItem, loopCancelListener)) {
                val context2 = context
                val base2 = base
                val media2 = media
                val query2 = query
                val referer2 = referer
                if (!segmentExists(context2, index, base2, media2, query2, referer2)) {
                    break
                }
                last = index
                base = base2
                media = media2
                query = query2
                referer = referer2
                index += step
                context = context2
            } else {
                return -1
            }
        }
        return last
    }

    @JvmStatic
    fun segmentUrl(last: Int, prefix: String, format: String, suffix: String?): String {
        val sb = StringBuilder(prefix)
        sb.append(String.format(Locale.US, format, last))
        if (!TextUtils.isEmpty(suffix)) {
            sb.append(suffix)
        }
        return sb.toString()
    }

    @JvmStatic
    fun isCancelled(downItem: DownItem?, loopCancelListener: LoopCancelListener?): Boolean {
        if (downItem != null) {
            return downItem.status == 6
        }
        return loopCancelListener != null && loopCancelListener.isCancelled
    }

    @JvmStatic
    fun segmentExists(
        context: Context?,
        last: Int,
        base: String,
        media: String,
        query: String?,
        referer: String?
    ): Boolean {
        return MainUtil.isImageUrl(context, segmentUrl(last, base, media, query), referer)
    }
}
