package com.mycompany.app.down

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.main.MainUtil
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Collections
import java.util.zip.GZIPInputStream

@Suppress("deprecation")
class DownParseList {
    @JvmField
    var parsing: Boolean = false
    @JvmField
    var hasStreams: Boolean = false
    @JvmField
    var fakeM3u8: Boolean = false
    @JvmField
    var cancelled: Boolean = false
    @JvmField
    var masterFound: Boolean = false

    class SortM3u8 : Comparator<M3u8Item?> {
        override fun compare(first: M3u8Item?, second: M3u8Item?): Int {
            val m3u8Item = first
            val other = second
            if (m3u8Item == null) {
                return if (other == null) 0 else 1
            }
            val index = if (other == null) 0 else m3u8Item.size
            if (other == null || index < other.size) {
                return -1
            }
            return if (index > other.size) 1 else 0
        }
    }

    fun parse(context: Context?, charset: String?, source: String?): ArrayList<M3u8Item?>? {
        var arrayListB: ArrayList<String?>?
        this.cancelled = false
        try {
            arrayListB = readLines(context, charset, source)
        } catch (unused: OutOfMemoryError) {
            arrayListB = null
        }
        if (arrayListB == null || arrayListB.isEmpty() || !this.parsing) {
            return null
        }
        // Video variants (#EXT-X-STREAM-INF followed by a URI line).
        var arrayList: ArrayList<M3u8Item?>? = null
        try {
            var line: String? = null
            var usedNames: ArrayList<String?>? = null
            val size = arrayListB.size
            var index = 0
            while (index < size) {
                var rawLine = arrayListB.get(index)
                index++
                if (!this.parsing) {
                    break
                }
                if (TextUtils.isEmpty(rawLine)) {
                    continue
                }
                if (rawLine!!.startsWith("#")) {
                    line = rawLine
                    continue
                }
                if (TextUtils.isEmpty(line) || !line!!.startsWith("#EXT-X-STREAM-INF")) {
                    continue
                }
                var resolution: String? = attrValue(line, "RESOLUTION=")
                if (TextUtils.isEmpty(resolution)) {
                    resolution = attrValue(line, "QUALITY=")
                    if (TextUtils.isEmpty(resolution)) {
                        resolution = attrValue(line, "BANDWIDTH=")
                        if (TextUtils.isEmpty(resolution)) {
                            continue
                        }
                    }
                }
                if (rawLine.startsWith("/")) {
                    rawLine = rawLine.substring(1)
                }
                if (TextUtils.isEmpty(rawLine)) {
                    continue
                }
                if (Companion.containsUri(arrayList, rawLine, false)) {
                    continue
                }
                var uniqueResolution = MainUtil.uniqueName(usedNames, resolution)
                if (TextUtils.isEmpty(uniqueResolution)) {
                    uniqueResolution = resolution
                }
                if (usedNames == null) {
                    usedNames = ArrayList()
                }
                usedNames.add(uniqueResolution)
                val m3u8Item = M3u8Item()
                m3u8Item.src = rawLine
                m3u8Item.label = uniqueResolution
                m3u8Item.size = Companion.resolutionOf(resolution, uniqueResolution!!)
                m3u8Item.base = attrValue(line, "AUDIO=")
                if (arrayList == null) {
                    arrayList = ArrayList()
                }
                arrayList.add(m3u8Item)
            }
        } catch (unused: Exception) {
        }
        if (arrayList == null || arrayList.isEmpty()) {
            return null
        }
        try {
            Collections.sort<M3u8Item?>(arrayList, SortM3u8())
        } catch (unused: Exception) {
        }
        if (!this.parsing) {
            return null
        }
        // Alternate audio renditions (#EXT-X-MEDIA:TYPE=AUDIO,...,URI="...").
        var seenUris: ArrayList<M3u8Item?>? = null
        try {
            val size = arrayListB.size
            var index = 0
            while (index < size) {
                val mediaLine = arrayListB.get(index)
                index++
                if (!this.parsing) {
                    break
                }
                if (TextUtils.isEmpty(mediaLine) || !mediaLine!!.startsWith("#EXT-X-MEDIA") || !mediaLine.contains(
                        "TYPE=AUDIO"
                    )
                ) {
                    continue
                }
                var mediaUri: String? = attrValue(mediaLine, "URI=")
                if (TextUtils.isEmpty(mediaUri)) {
                    continue
                }
                if (mediaUri!!.startsWith("/")) {
                    mediaUri = mediaUri.substring(1)
                }
                if (TextUtils.isEmpty(mediaUri) || Companion.containsUri(seenUris, mediaUri, true)) {
                    continue
                }
                val m3u8Item = M3u8Item()
                m3u8Item.audioUrl = mediaUri
                m3u8Item.base = attrValue(mediaLine, "GROUP-ID=")
                if (seenUris == null) {
                    seenUris = ArrayList()
                }
                seenUris.add(m3u8Item)
            }
        } catch (unused: Exception) {
        }
        if (seenUris != null && !seenUris.isEmpty()) {
            val first = seenUris.get(0)
            val audioUrl = if (first != null) first.audioUrl else null
            val sizeNow = arrayList.size
            for (index3 in 0..<sizeNow) {
                val video = arrayList.get(index3)
                if (video == null) {
                    continue
                }
                video.audioUrl = audioUrl
                if (TextUtils.isEmpty(video.base)) {
                    continue
                }
                val size = seenUris.size
                for (index in 0..<size) {
                    val audio = seenUris.get(index)
                    if (audio != null && video.base == audio.base) {
                        video.audioUrl = audio.audioUrl
                        break
                    }
                }
            }
        }
        return arrayList
    }

    /** Downloads a master playlist; returns its non-empty lines, or null if it turns out to be a media playlist.  */
    fun readLines(context: Context?, charset: String?, source: String?): ArrayList<String?>? {
        val conn = MainUtil.openHttpConnection(0, 0, context, charset, source, true)
        if (conn == null) {
            return null
        }
        var lines: ArrayList<String?>? = null
        var inputStream: InputStream? = null
        var gzip: GZIPInputStream? = null
        var reader: InputStreamReader? = null
        var bufferedReader: BufferedReader? = null
        try {
            conn.setDoInput(true)
            conn.connect()
            if (this.fakeM3u8 && MainUtil.isMediaResponse(conn)) {
                this.cancelled = true
            } else {
                inputStream = conn.getInputStream()
                this.hasStreams = true
                if ("gzip" == conn.getContentEncoding()) {
                    gzip = GZIPInputStream(inputStream)
                    reader = InputStreamReader(gzip)
                } else {
                    reader = InputStreamReader(inputStream)
                }
                bufferedReader = BufferedReader(reader)
                try {
                    while (true) {
                        val line = bufferedReader.readLine()
                        if (line == null) {
                            break
                        }
                        if (!this.parsing) {
                            lines = null
                            break
                        }
                        val trimmed = line.trim { it <= ' ' }
                        if (TextUtils.isEmpty(trimmed)) {
                            continue
                        }
                        if (trimmed.startsWith("#EXTINF")) {
                            lines = null
                            break
                        }
                        if (lines == null) {
                            lines = ArrayList()
                        }
                        lines.add(trimmed)
                    }
                } catch (unused: Exception) {
                }
            }
        } catch (unused2: Exception) {
            lines = null
        } catch (unused3: OutOfMemoryError) {
            MainUtil.restartLater(context)
            lines = null
        }
        val closeables = arrayOf<Closeable?>(bufferedReader, reader, gzip, inputStream)
        for (c in closeables) {
            if (c != null) {
                try {
                    c.close()
                } catch (unused4: Exception) {
                }
            }
        }
        conn.disconnect()
        return lines
    }

    fun parseUrl(context: Context?, downUrl: String?, source: String?): ArrayList<M3u8Item?>? {
        if (if (TextUtils.isEmpty(downUrl)) false else downUrl!!.startsWith("m3fake:")) {
            this.fakeM3u8 = true
        }
        val listUrl = MainUtil.urlFromText(downUrl)
        if (!TextUtils.isEmpty(listUrl)) {
            this.parsing = true
            this.hasStreams = false
            this.masterFound = false
            val arrayListA = parse(context, listUrl, source)
            if (!this.cancelled) {
                if (this.hasStreams || TextUtils.isEmpty(source)) {
                    return arrayListA
                }
                if (this.parsing) {
                    val isApi = MainConst.isApi28
                    val items = parse(context, listUrl, null)
                    if (!this.cancelled) {
                        this.masterFound = this.hasStreams
                        return items
                    }
                }
            }
        }
        return null
    }

    companion object {
        @JvmStatic
        fun resolutionOf(line: String?, key: String): Int {
            if (TextUtils.isEmpty(line)) {
                return 0
            }
            val pixels = MainUtil.parseIntSafe(line!!.replace("x", ""))
            if (key.length > line.length) {
                val rest = key.substring(line.length)
                if (!TextUtils.isEmpty(rest)) {
                    return MainUtil.parseIntSafe(
                        rest.replace(" (", "").replace(")", "").trim { it <= ' ' }) + pixels
                }
            }
            return pixels
        }

        @JvmStatic
        fun attrValue(line: String?, key: String): String? {
            val keyIndex = if (TextUtils.isEmpty(line)) 0 else line!!.indexOf(key)
            val length = if (TextUtils.isEmpty(line) || keyIndex == -1) 0 else (key.length + keyIndex)
            if (TextUtils.isEmpty(line) || keyIndex == -1 || length >= line!!.length) {
                return null
            }
            val commaIndex = line.indexOf(44.toChar(), length)
            return MainUtil.stripQuotes(
                if (commaIndex <= length || commaIndex > line.length) line.substring(
                    length
                ) else line.substring(length, commaIndex)
            )
        }

        private fun containsUri(list: ArrayList<M3u8Item?>?, uri: String, audio: Boolean): Boolean {
            if (list == null || list.isEmpty()) {
                return false
            }
            val size = list.size
            for (index in 0..<size) {
                val item = list.get(index)
                if (item != null && uri == if (audio) item.audioUrl else item.src) {
                    return true
                }
            }
            return false
        }
    }
}
