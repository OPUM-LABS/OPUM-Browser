package com.mycompany.app.down

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.down.DownParseList.SortM3u8
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.LoopCancelListener
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.util.Collections

@Suppress("deprecation")
object DownParseDzen {

    @JvmStatic
    fun parsePage(context: Context?, downUrl: String?): ArrayList<M3u8Item?>? {
        var document: Document?
        var arrayList: ArrayList<M3u8Item?>?
        var last: Int = 0
        var segment: Int = 0
        val urlText = MainUtil.urlFromText(downUrl)
        if (!TextUtils.isEmpty(urlText)) {
            try {
                document = Jsoup.connect(urlText!!).parser(Parser.xmlParser()).get()
            } catch (unused: Exception) {
                document = null
            } catch (unused: LinkageError) {
                document = null
            } catch (unused2: OutOfMemoryError) {
                MainUtil.restartLater(context)
                document = null
            }
            if (document != null) {
                try {
                    val elementsByTag = document.getElementsByTag("BaseURL")
                    if (elementsByTag.size != 0) {
                        var start = 0
                        val element = elementsByTag.get(0)
                        val baseUrl = element.text()
                        var elementsSelect: Elements? = if (!TextUtils.isEmpty(baseUrl)) document.select("[mimeType*='video']") else null
                        if (!TextUtils.isEmpty(baseUrl) && elementsSelect != null && elementsSelect.size != 0) {
                            val items: ArrayList<M3u8Item?> = ArrayList()
                            var size = elementsSelect.size
                            var index = 0
                            while (index < size) {
                                val t: Any? = elementsSelect!!.get(index)
                                index++
                                val adaptation = t as Element?
                                val representations: Elements? = if (adaptation != null) adaptation.children() else null
                                if (adaptation != null && representations != null && representations.size != 0) {
                                    val repCount = representations.size
                                    var repIndex = start
                                    while (repIndex < repCount) {
                                        val rawElement: Any? = representations.get(repIndex)
                                        repIndex++
                                        val representation = rawElement as Element?
                                        if (representation != null) {
                                            arrayList = null
                                            try {
                                                var elementsChildren = representation.children()
                                                if (elementsChildren.size != 0) {
                                                    val videoId = representation.attr("id")
                                                    if (!TextUtils.isEmpty(videoId)) {
                                                        val elements = elementsSelect
                                                        val widthText = representation.attr("width")
                                                        if (TextUtils.isEmpty(widthText)) {
                                                            elementsSelect = elements
                                                        } else {
                                                            val count = size
                                                            val heightText =
                                                                representation.attr("height")
                                                            if (TextUtils.isEmpty(heightText)) {
                                                                elementsSelect = elements
                                                                size = count
                                                            } else {
                                                                val segmentCount =
                                                                    elementsChildren.size
                                                                var template: String? = null
                                                                var src: String? = null
                                                                val segmentPos = index
                                                                var segmentIndex = 0
                                                                while (segmentIndex < segmentCount) {
                                                                    val rawSegment: Any? =
                                                                        elementsChildren.get(
                                                                            segmentIndex
                                                                        )
                                                                    val next = segmentIndex + 1
                                                                    val segments =
                                                                        elementsChildren
                                                                    val segmentNode =
                                                                        rawSegment as Element?
                                                                    if (segmentNode == null) {
                                                                        segment = next
                                                                    } else {
                                                                        segment = next
                                                                        val initTemplate =
                                                                            segmentNode.attr("initialization")
                                                                        if (!TextUtils.isEmpty(
                                                                                initTemplate
                                                                            )
                                                                        ) {
                                                                            val initUrl =
                                                                                initTemplate.replace(
                                                                                    "\$RepresentationID$",
                                                                                    videoId
                                                                                )
                                                                            if (!TextUtils.isEmpty(
                                                                                    initUrl
                                                                                )
                                                                            ) {
                                                                                val mediaTemplate =
                                                                                    segmentNode.attr(
                                                                                        "media"
                                                                                    )
                                                                                if (!TextUtils.isEmpty(
                                                                                        mediaTemplate
                                                                                    )
                                                                                ) {
                                                                                    val mediaUrl =
                                                                                        mediaTemplate.replace(
                                                                                            "\$RepresentationID$",
                                                                                            videoId
                                                                                        )
                                                                                    if (!TextUtils.isEmpty(
                                                                                            mediaUrl
                                                                                        )
                                                                                    ) {
                                                                                        src =
                                                                                            mediaUrl
                                                                                        template =
                                                                                            initUrl
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    segmentIndex = segment
                                                                    elementsChildren =
                                                                        segments
                                                                }
                                                                if (!TextUtils.isEmpty(template) && !TextUtils.isEmpty(
                                                                        src
                                                                    )
                                                                ) {
                                                                    val videoWidth =
                                                                        MainUtil.parseIntSafe(widthText)
                                                                    val videoHeight =
                                                                        MainUtil.parseIntSafe(heightText)
                                                                    val m3u8Item = M3u8Item()
                                                                    m3u8Item.url = baseUrl
                                                                    m3u8Item.template = template
                                                                    m3u8Item.src = src
                                                                    m3u8Item.label =
                                                                        widthText + "x" + heightText
                                                                    m3u8Item.size = videoWidth * videoHeight
                                                                    items.add(m3u8Item)
                                                                }
                                                                elementsSelect = elements
                                                                size = count
                                                                index = segmentPos
                                                            }
                                                        }
                                                    }
                                                    start = 0
                                                }
                                            } catch (unused3: Exception) {
                                                return arrayList
                                            }
                                        }
                                    }
                                }
                            }
                            arrayList = null
                            if (items.isEmpty()) {
                                return null
                            }
                            try {
                                Collections.sort<M3u8Item?>(items, SortM3u8())
                            } catch (unused4: Exception) {
                            }
                            var audioSets = document.select("[mimeType*='audio']")
                            if (audioSets.size != 0) {
                                var audioSetCount = audioSets.size
                                var audioTemplate: String? = null
                                var audioUrl: String? = null
                                var audioSetIndex = 0
                                while (audioSetIndex < audioSetCount) {
                                    val rawElement: Any? = audioSets.get(audioSetIndex)
                                    audioSetIndex++
                                    val audioSet = rawElement as Element?
                                    val elementsChildren: Elements? = if (audioSet == null) null else audioSet.children()
                                    if (audioSet == null || elementsChildren == null || elementsChildren.size == 0) {
                                        audioSets = audioSets
                                        audioSetCount = audioSetCount
                                    } else {
                                        val elementsChildrenSize = elementsChildren.size
                                        var audioRepIndex = 0
                                        while (audioRepIndex < elementsChildrenSize) {
                                            val rawAudioRep: Any? = elementsChildren.get(audioRepIndex)
                                            audioRepIndex++
                                            val audioRep = rawAudioRep as Element?
                                            val audioSegments: Elements? = if (audioRep != null) audioRep.children() else null
                                            if (audioRep != null && audioSegments != null && audioSegments.size != 0) {
                                                val audioId = audioRep.attr("id")
                                                if (!TextUtils.isEmpty(audioId)) {
                                                    val audioSegmentCount = audioSegments.size
                                                    val elements = audioSets
                                                    var audioSegmentIndex = 0
                                                    while (audioSegmentIndex < audioSegmentCount) {
                                                        val rawAudioSegment: Any? =
                                                            audioSegments.get(audioSegmentIndex)
                                                        val nextIndex = audioSegmentIndex + 1
                                                        val audioSegment = rawAudioSegment as Element?
                                                        if (audioSegment == null) {
                                                            last = audioSetCount
                                                        } else {
                                                            last = audioSetCount
                                                            val audioInitTemplate =
                                                                audioSegment.attr("initialization")
                                                            if (!TextUtils.isEmpty(audioInitTemplate)) {
                                                                val audioInitUrl =
                                                                    audioInitTemplate.replace(
                                                                        "\$RepresentationID$",
                                                                        audioId
                                                                    )
                                                                if (!TextUtils.isEmpty(
                                                                        audioInitUrl
                                                                    )
                                                                ) {
                                                                    val audioMediaTemplate =
                                                                        audioSegment.attr("media")
                                                                    if (!TextUtils.isEmpty(
                                                                            audioMediaTemplate
                                                                        )
                                                                    ) {
                                                                        val audioMediaUrl =
                                                                            audioMediaTemplate.replace(
                                                                                "\$RepresentationID$",
                                                                                audioId
                                                                            )
                                                                        if (!TextUtils.isEmpty(
                                                                                audioMediaUrl
                                                                            )
                                                                        ) {
                                                                            audioUrl = audioMediaUrl
                                                                            audioTemplate = audioInitUrl
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        audioSetCount = last
                                                        audioSegmentIndex = nextIndex
                                                    }
                                                    audioSets = elements
                                                }
                                            }
                                            audioSets = audioSets
                                            audioSetCount = audioSetCount
                                        }
                                    }
                                }
                                if (!TextUtils.isEmpty(audioTemplate) && !TextUtils.isEmpty(audioUrl)) {
                                    val itemCount = items.size
                                    var itemIndex = 0
                                    while (itemIndex < itemCount) {
                                        val rawM3u8Item: Any? = items.get(itemIndex)
                                        itemIndex++
                                        val m3u8Item = rawM3u8Item as M3u8Item?
                                        if (m3u8Item != null) {
                                            m3u8Item.audioTemplate = audioTemplate
                                            m3u8Item.audioUrl = audioUrl
                                        }
                                    }
                                }
                            }
                            return items
                        }
                    }
                } catch (unused5: Exception) {
                }
            }
        }
        return null
    }

    @JvmStatic
    fun parseItemText(url: String?): M3u8Item? {
        val itemText = MainUtil.urlFromText(url)
        val parts: Array<String?>? = if (TextUtils.isEmpty(itemText)) null else itemText!!.split("<,>".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        val newUrl: String? = if (TextUtils.isEmpty(itemText) || parts == null || parts.size != 5) null else parts[0]
        val template: String? = if (TextUtils.isEmpty(itemText) || parts == null || parts.size != 5 || newUrl == null) null else parts[1]
        val src: String? = if (TextUtils.isEmpty(itemText) || parts == null || parts.size != 5 || newUrl == null || template == null) null else parts[2]
        if (TextUtils.isEmpty(itemText) || parts == null || parts.size != 5 || newUrl == null || template == null || src == null) {
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
     * Segment urls of a Dzen DASH stream: `base + init`, then `base + media` with
     * "$Number$" replaced from the first to the last existing segment.
     */
    @JvmStatic
    fun segmentUrls(
        context: Context?, base: String?, referer: String?, init: String?, media: String?,
        item: DownItem?, cancel: LoopCancelListener?
    ): ArrayList<String?>? {
        if ((item == null && cancel == null) || TextUtils.isEmpty(base) || TextUtils.isEmpty(init) || TextUtils.isEmpty(
                media
            )
        ) {
            return null
        }
        var first = -1
        run {
            var first2 = 0
            while (first2 < 10 && !DownParseDzen.isCancelled(item, cancel)) {
                if (DownParseDzen.segmentExists(context, first2, base, media!!, referer)) {
                    first = first2
                    break
                }
                first2++
            }
        }
        if (first == -1) {
            return null
        }
        var last = -1
        if (item != null && item.audioStart > 1 && !isCancelled(item, cancel)) {
            val candidate = item.audioStart - 1
            if (DownParseDzen.segmentExists(context, candidate, base, media!!, referer) && !isCancelled(
                    item,
                    cancel
                ) && !DownParseDzen.segmentExists(context, item.audioStart, base, media, referer)
            ) {
                last = candidate
            }
        }
        if (last == -1) {
            var guess = -1
            if (!isCancelled(item, cancel)) {
                if (DownParseDzen.segmentExists(context, 100, base, media!!, referer)) {
                    guess = DownParseDzen.probeSegments(context, 100, 50, base, media, referer, item, cancel)
                } else {
                    var first2 = 50
                    while (first2 > -1 && !isCancelled(item, cancel)) {
                        if (DownParseDzen.segmentExists(context, first2, base, media, referer)) {
                            guess = first2
                            break
                        }
                        first2 -= 50
                    }
                }
            }
            last = if (guess == -1) first else guess
            for (step in intArrayOf(20, 10, 5, 2, 1)) {
                last = DownParseDzen.probeSegments(context, last, step, base, media!!, referer, item, cancel)
            }
        }
        val list: ArrayList<String?> = ArrayList()
        list.add(base + init)
        for (first2 in first..last) {
            if (isCancelled(item, cancel)) {
                return null
            }
            list.add(base + media!!.replace("\$Number$", first2.toString()))
        }
        return list
    }

    @JvmStatic
    fun probeSegments(
        context: Context?,
        last: Int,
        step: Int,
        base: String?,
        media: String,
        referer: String?,
        downItem: DownItem?,
        loopCancelListener: LoopCancelListener?
    ): Int {
        var last = last
        var segment: Int = 0
        do {
            segment = last
            last += step
            if (last >= 100000) {
                break
            }
            if (isCancelled(downItem, loopCancelListener)) {
                return -1
            }
        } while (segmentExists(context, last, base, media, referer))
        return segment
    }

    @JvmStatic
    fun isCancelled(downItem: DownItem?, loopCancelListener: LoopCancelListener?): Boolean {
        if (downItem != null) {
            return downItem.status == 6
        }
        return loopCancelListener != null && loopCancelListener.isCancelled
    }

    @JvmStatic
    fun segmentExists(context: Context?, segment: Int, base: String?, media: String, referer: String?): Boolean {
        val sbR = StringBuilder().append(base)
        sbR.append(media.replace("\$Number$", segment.toString()))
        return MainUtil.isImageUrl(context, sbR.toString(), referer)
    }
}
