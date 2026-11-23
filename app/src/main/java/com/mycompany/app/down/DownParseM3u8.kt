package com.mycompany.app.down

import android.content.Context
import android.text.TextUtils
import android.webkit.URLUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainDownSvc.EncItem
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.main.MainUtil
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.zip.GZIPInputStream

@Suppress("deprecation")
class DownParseM3u8(@JvmField var context: Context?) {
    @JvmField
    var isMaster: Boolean = false
    @JvmField
    var fakeM3u8: Boolean = false
    @JvmField
    var hasKey: Boolean = false
    @JvmField
    var masterFound: Boolean = false
    @JvmField
    var parsed: Boolean = false
    @JvmField
    var encKeys: MutableList<EncItem?>? = null
    @JvmField
    /** The IV items, one per segment, of the last parsed playlist. */
    var segmentList: ArrayList<EncItem?>? = null
    @JvmField
    var keyAbsolute: Boolean = false
    @JvmField
    var depth: Int = 0
    @JvmField
    var parseExecutor: ExecutorService?

    internal inner class ParsedTask(@JvmField val outer: DownParseM3u8,
                                         @JvmField val lang: String?,
                                         @JvmField val referer: String?,
                                         @JvmField val query: String?,
                                         @JvmField val downParseListener: DownParseListener
    ) : Runnable {

        internal inner class KeyListener(@JvmField val outer: ParsedTask,
                                    @JvmField val downParseListener: DownParseListener,
                                    @JvmField val referer: String?,
                                    @JvmField val url: String?,
                                    @JvmField val query: String?
        ) : DownParseListener {

            internal inner class ParseWithKeyStep(@JvmField val outer: KeyListener,
                                                @JvmField val input: String?,
                                                @JvmField val query: String?,
                                                @JvmField val listener: DownParseListener
            ) : Runnable {

                internal inner class KeyResultListener(@JvmField val outer: ParseWithKeyStep,
                                            @JvmField val downParseListener: DownParseListener
                ) : DownParseListener {
                    override fun onParsed(list: MutableList<String?>?) {
                        val downParseM3u = this.outer.outer.outer.outer
                        val hasKey = downParseM3u.hasKey
                        val downParseListener = this.downParseListener
                        if (hasKey) {
                            downParseListener.onParsed(null)
                        } else {
                            downParseM3u.masterFound = downParseM3u.isMaster
                            downParseListener.onParsed(list)
                        }
                    }
                }

                override fun run() {
                    val downParseM3u = this.outer.outer.outer
                    val isApi = MainConst.isApi28
                    downParseM3u.parse(this.input, null, this.query, KeyResultListener(this, this.listener))
                }
            }

            override fun onParsed(list: MutableList<String?>?) {
                val downParseM3u = this.outer.outer
                val downParseListener = this.downParseListener
                try {
                    try {
                        if (downParseM3u.hasKey) {
                            downParseListener.onParsed(null)
                            return
                        }
                        if (downParseM3u.isMaster || TextUtils.isEmpty(this.referer)) {
                            downParseListener.onParsed(list)
                            return
                        }
                        if (downParseM3u.depth > 50) {
                            downParseListener.onParsed(null)
                            return
                        }
                        val parseWithKeyStep = ParseWithKeyStep(this, this.url, this.query, downParseListener)
                        val executorService = downParseM3u.parseExecutor
                        if (executorService != null) {
                            try {
                                executorService.execute(parseWithKeyStep)
                            } catch (unused: Exception) {
                            }
                        }
                    } catch (unused2: Exception) {
                        downParseListener.onParsed(null)
                    }
                } catch (unused3: OutOfMemoryError) {
                    downParseListener.onParsed(null)
                } catch (unused4: StackOverflowError) {
                    downParseListener.onParsed(null)
                }
            }
        }

        override fun run() {
            val downParseListener = this.downParseListener
            val charset = this.referer
            val lang = this.lang
            val listener = this.query
            this.outer.parse(lang, charset, listener, KeyListener(this, downParseListener, charset, lang, listener))
        }
    }

    internal inner class ParsedStep(@JvmField val listener: DownParseListener) :
        DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            this.listener.onParsed(list)
        }
    }

    internal inner class PlaylistParsed(@JvmField val listener: DownParseListener) :
        DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            this.listener.onParsed(list)
        }
    }

    internal inner class SegmentsParsed(@JvmField val listener: DownParseListener) :
        DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            this.listener.onParsed(list)
        }
    }

    internal inner class MasterParsed(@JvmField val listener: DownParseListener) :
        DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            this.listener.onParsed(list)
        }
    }

    internal inner class MediaParsed(@JvmField val listener: DownParseListener) :
        DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            this.listener.onParsed(list)
        }
    }

    interface DownParseListener {
        fun onParsed(list: MutableList<String?>?)
    }

    /**
     * Downloads and parses playlist `url`. A master playlist continues with its first variant
     * stream; a media playlist yields its segment list (plus init segments), with any AES keys and IVs
     * collected into [.g] / [.h]. Relative URIs are resolved by [.m].
     *
     * @param query query string of the playlist url, re-appended to resolved URIs when needed
     */
    /** The last media playlist read ended with #EXT-X-ENDLIST (a finished video, not a live stream). */
    @JvmField var endList: Boolean = false

    /** A media playlist without #EXT-X-ENDLIST: a live stream (it would grow forever). */
    @JvmField var liveList: Boolean = false

    fun parse(url: String?, referer: String?, query: String?, listener: DownParseListener) {
        this.liveList = false
        this.hasKey = false
        this.parsed = false
        this.encKeys = null
        this.segmentList = null
        this.keyAbsolute = false
        if (this.depth > 50) {
            listener.onParsed(null)
            return
        }
        var lines: ArrayList<String?>?
        try {
            lines = readLines(url, referer)
        } catch (unused: OutOfMemoryError) {
            lines = null
        } catch (unused: StackOverflowError) {
            lines = null
        }
        if (lines == null || lines.isEmpty()) {
            listener.onParsed(null)
            return
        }
        if (!this.parsed) {
            // Master playlist (no #EXTINF): follow the first variant stream.
            var variant: String? = null
            try {
                var tag: String? = null
                for (line2 in lines) {
                    val line = line2
                    if (TextUtils.isEmpty(line)) {
                        continue
                    }
                    if (line!!.startsWith("#")) {
                        tag = line
                        continue
                    }
                    if (tag != null && tag.startsWith("#EXT-X-STREAM-INF")
                        && (!TextUtils.isEmpty(
                            DownParseList.Companion.attrValue(
                                tag,
                                "RESOLUTION="
                            )
                        ) || !TextUtils.isEmpty(
                            DownParseList.Companion.attrValue(
                                tag,
                                "QUALITY="
                            )
                        ) || !TextUtils.isEmpty(DownParseList.Companion.attrValue(tag, "BANDWIDTH=")))
                    ) {
                        val uri: String? = stripSlash(line)
                        if (!TextUtils.isEmpty(uri)) {
                            variant = uri
                            break
                        }
                    }
                }
            } catch (unused: Exception) {
            }
            if (TextUtils.isEmpty(variant)) {
                listener.onParsed(null)
            } else {
                parseSegments(null, url, referer, query, variant, PlaylistParsed(listener))
            }
            return
        }
        // a media playlist: live when it has no end marker
        this.liveList = !this.endList
        var segments: ArrayList<String?>? = null
        try {
            var keys: ArrayList<EncItem?>? = null
            var ivs: ArrayList<EncItem?>? = null
            var keyUri: String? = null
            var iv: String? = null
            var lastKey: String? = null
            var lastIv: String? = null
            var lastKeyIndex = 0
            var lastIvIndex = 0
            var firstKey = true
            // 2026-11-20: a set for the duplicate check (was a list scan per segment), and #EXT-X-BYTERANGE support:
            // a ranged segment carries its range on the URL (DownHls.withRange), so pieces of one file stay apart
            val seen = HashSet<String>()
            var pendingRange: Pair<Long, Long>? = null
            val nextOffset = HashMap<String, Long>()
            fun ranged(uri: String, range: Pair<Long, Long>?): String {
                if (range == null) return uri
                val offset = if (range.second >= 0) range.second else nextOffset[uri] ?: 0L
                nextOffset[uri] = offset + range.first
                return DownHls.withRange(uri, offset, range.first)
            }
            for (line2 in lines) {
                val line = line2
                if (TextUtils.isEmpty(line)) {
                    continue
                }
                if (line!!.startsWith("#")) {
                    if (line.startsWith("#EXT-X-BYTERANGE:")) {
                        pendingRange = DownHls.parseByteRange(line.substringAfter(':'))
                        continue
                    }
                    val isKey = line.startsWith("#EXT-X-KEY")
                    if (!isKey && !line.startsWith("#EXT-X-MAP")) {
                        continue
                    }
                    val uri: String? = stripSlash(DownParseList.Companion.attrValue(line, "URI="))
                    if (TextUtils.isEmpty(uri)) {
                        continue
                    }
                    if (isKey) {
                        keyUri = uri
                        iv = DownParseList.Companion.attrValue(line, "IV=")
                    } else {
                        // Init segment of fragmented mp4 streams goes first in the segment list.
                        if (segments == null) {
                            segments = ArrayList()
                        }
                        val init = ranged(uri!!, DownHls.parseByteRange(DownParseList.Companion.attrValue(line, "BYTERANGE=")))
                        if (seen.add(init)) {
                            segments.add(init)
                        }
                    }
                    continue
                }
                val segment0: String? = stripSlash(line)
                if (TextUtils.isEmpty(segment0)) {
                    continue
                }
                val segment = ranged(segment0!!, pendingRange)
                pendingRange = null
                if (segments == null) {
                    segments = ArrayList()
                }
                if (seen.add(segment)) {
                    segments.add(segment)
                }
                if (TextUtils.isEmpty(keyUri)) {
                    continue
                }
                // One EncItem per segment; repeats point (a) at the index of the item holding the value.
                if (keys == null) {
                    keys = ArrayList()
                }
                val key = EncItem()
                if (MainUtil.sameObject(lastKey, keyUri)) {
                    key.lastKeyIndex = lastKeyIndex
                } else {
                    key.lastKeyIndex = -1
                    key.keyUri = keyUri
                    lastKeyIndex = keys.size
                    if (firstKey) {
                        this.keyAbsolute = keyUri!!.startsWith("http")
                        firstKey = false
                    }
                    lastKey = keyUri
                }
                keys.add(key)
                if (ivs == null) {
                    ivs = ArrayList()
                }
                val ivItem = EncItem()
                if (MainUtil.sameObject(lastIv, iv)) {
                    ivItem.lastKeyIndex = lastIvIndex
                } else {
                    ivItem.lastKeyIndex = -1
                    ivItem.keyUri = iv
                    lastIvIndex = ivs.size
                    lastIv = iv
                }
                ivs.add(ivItem)
            }
            this.encKeys = keys
            this.segmentList = ivs
        } catch (unused: Exception) {
            segments = null
        }
        if (segments == null || segments.isEmpty()) {
            listener.onParsed(null)
        } else {
            parseSegments(
                segments,
                url,
                referer,
                query,
                segments.get(0),
                ParsedStep(listener)
            )
        }
    }

    /** A base url that, prefixed to `uri`, gives a reachable url; with [.lastWithQuery].  */
    private fun findBase(
        playlistUrl: String?,
        uri: String?,
        referer: String?,
        query: String?
    ): String? {
        val withQuery: Boolean = needsQuery(uri, query)
        val origin: String? = schemeHost(playlistUrl)
        if (TextUtils.isEmpty(origin)) {
            return null
        }
        val urlLength = playlistUrl!!.length
        if (MainUtil.hasChar('/'.code, uri)) {
            // Path-like uri: try bases from the origin downwards.
            var base = origin!!
            var length = origin.length
            while (length <= urlLength) {
                if (probe(uri, base, referer, query, withQuery)) {
                    return base
                }
                val next: String? = pathPrefix(urlLength, length, playlistUrl)
                if (TextUtils.isEmpty(next) || next == base) {
                    return null
                }
                base = next!!
                length = next.length
            }
        } else {
            // Bare file name: try the playlist's folder and its parents.
            var base: String? = fileNameOf(playlistUrl)
            if (TextUtils.isEmpty(base)) {
                return null
            }
            var length = base!!.length
            while (length > 0) {
                if (probe(uri, base, referer, query, withQuery)) {
                    return base
                }
                val next: String? = commonPrefix(playlistUrl, urlLength, length, origin)
                if (TextUtils.isEmpty(next) || next == base) {
                    return null
                }
                base = next
                length = next!!.length
            }
        }
        return null
    }

    private var lastWithQuery = false

    init {
        this.parseExecutor = MainApp.executor(context)
    }

    private fun probe(
        uri: String?,
        base: String?,
        referer: String?,
        query: String?,
        withQuery: Boolean
    ): Boolean {
        if (MainUtil.isImageUrl(this.context, joinUrl(uri, base, query, withQuery), referer)) {
            this.lastWithQuery = withQuery
            return true
        }
        if (withQuery && MainUtil.isImageUrl(this.context, joinUrl(uri, base, null, false), referer)) {
            this.lastWithQuery = false
            return true
        }
        return false
    }

    /**
     * Key items of the last parsed playlist with relative key URIs made absolute (resolved like
     * segments, see [.m]). Returns [.g].
     */
    fun resolveKeys(playlistUrl: String?, referer: String?, query: String): MutableList<EncItem?>? {
        val keys = this.encKeys
        if (this.keyAbsolute || keys == null || keys.isEmpty()) {
            return keys
        }
        val first = keys.get(0)
        if (first == null || TextUtils.isEmpty(first.keyUri)) {
            return keys
        }
        try {
            val base = findBase(playlistUrl, first.keyUri, referer, query)
            if (base != null) {
                val resolved: MutableList<EncItem?>? = filterItems(keys, this.lastWithQuery, base, query)
                if (resolved != null && !resolved.isEmpty()) {
                    this.encKeys = resolved
                }
            }
        } catch (unused: Exception) {
        }
        return this.encKeys
    }

    fun readLines(url: String?, referer: String?): ArrayList<String?>? {
        this.endList = false
        val conn = MainUtil.openHttpConnection(0, 0, this.context, url, referer, true)
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
                this.hasKey = true
            } else {
                inputStream = conn.getInputStream()
                this.isMaster = true
                if ("gzip" == conn.getContentEncoding()) {
                    gzip = GZIPInputStream(inputStream)
                    reader = InputStreamReader(gzip)
                } else {
                    reader = InputStreamReader(inputStream)
                }
                bufferedReader = BufferedReader(reader)
                while (true) {
                    val line = bufferedReader.readLine()
                    if (line == null) {
                        break
                    }
                    val trimmed = line.trim { it <= ' ' }
                    if (TextUtils.isEmpty(trimmed)) {
                        continue
                    }
                    if (trimmed.startsWith("#EXT-X-ENDLIST")) {
                        this.endList = true
                        break
                    }
                    if (trimmed.startsWith("#EXTINF")) {
                        this.parsed = true
                    }
                    if (lines == null) {
                        lines = ArrayList()
                    }
                    lines.add(trimmed)
                }
            }
        } catch (unused: Exception) {
        } catch (unused2: OutOfMemoryError) {
            MainUtil.restartLater(this.context)
        }
        val closeables = arrayOf<Closeable?>(bufferedReader, reader, gzip, inputStream)
        for (c in closeables) {
            if (c != null) {
                try {
                    c.close()
                } catch (unused3: Exception) {
                }
            }
        }
        conn.disconnect()
        return lines
    }

    fun parseMaster(full: String?, referer: String?, query: String?, downParseListener: DownParseListener) {
        this.isMaster = false
        this.masterFound = false
        if (this.depth > 50) {
            downParseListener.onParsed(null)
            return
        }
        val parsedTask: ParsedTask =
            ParsedTask(this, full, referer, query, downParseListener)
        val executorService = this.parseExecutor
        if (executorService == null) {
            return
        }
        try {
            executorService.execute(parsedTask)
        } catch (unused: Exception) {
        }
    }

    /**
     * Resolves `uri` (a variant playlist, or the first entry of `segments`) against
     * `playlistUrl` by probing candidate base urls. With segments, the listener gets the whole
     * list made absolute; without, the variant playlist is parsed next via [.k].
     */
    fun parseSegments(
        segments: ArrayList<String?>?,
        playlistUrl: String?,
        referer: String?,
        query: String?,
        uri: String?,
        listener: DownParseListener
    ) {
        this.depth++
        if (this.depth > 50 || TextUtils.isEmpty(uri)) {
            listener.onParsed(null)
            return
        }
        try {
            val withQuery: Boolean = needsQuery(uri, query)
            if (URLUtil.isNetworkUrl(uri)) {
                if (withQuery) {
                    val full: String? = joinUrl(uri, null, query, true)
                    if (MainUtil.isImageUrl(this.context, full, referer)) {
                        if (segments != null) {
                            listener.onParsed(absoluteUrls(null, query, segments, true))
                        } else {
                            parseMaster(full, referer, query, SegmentsParsed(listener))
                        }
                        return
                    }
                }
                if (segments != null) {
                    listener.onParsed(segments)
                } else {
                    parseMaster(uri, referer, null, MasterParsed(listener))
                }
                return
            }
            val base = findBase(playlistUrl, uri, referer, query)
            if (base == null) {
                listener.onParsed(null)
                return
            }
            val useQuery = this.lastWithQuery
            if (segments != null) {
                listener.onParsed(absoluteUrls(base, if (useQuery) query else null, segments, useQuery))
            } else {
                parseMaster(
                    joinUrl(uri, base, query, useQuery),
                    referer,
                    if (useQuery) query else null,
                    MediaParsed(listener)
                )
            }
        } catch (unused: Exception) {
            listener.onParsed(null)
        } catch (unused: OutOfMemoryError) {
            listener.onParsed(null)
        } catch (unused: StackOverflowError) {
            listener.onParsed(null)
        }
    }

    fun release() {
        this.context = null
        this.encKeys = null
        this.segmentList = null
        this.parseExecutor = null
    }

    companion object {
        @JvmStatic
        fun filterItems(list: MutableList<EncItem?>?, lastWithQuery: Boolean, base: String, query: String): MutableList<EncItem?>? {
            var base = base
            if (list == null) {
                return null
            }
            val baseEmpty = TextUtils.isEmpty(base)
            val it = list.iterator()
            while (it.hasNext()) {
                val encItem = it.next()
                if (encItem != null) {
                    val keyUri = encItem.keyUri
                    if (!TextUtils.isEmpty(keyUri)) {
                        val sb = StringBuilder()
                        if (!baseEmpty && !keyUri!!.startsWith(base)) {
                            base = trimSlash(keyUri, base)!!
                            sb.append(base)
                        }
                        sb.append(keyUri)
                        if (lastWithQuery && !keyUri!!.endsWith(query)) {
                            sb.append(query)
                        }
                        encItem.keyUri = sb.toString()
                    }
                }
            }
            return list
        }

        @JvmStatic
        fun pathPrefix(urlLength: Int, length: Int, playlistUrl: String?): String? {
            val index = if (playlistUrl != null && length >= 0) (length + 1) else 0
            val slashIndex = if (playlistUrl != null && length >= 0 && index < urlLength) playlistUrl.indexOf(47.toChar(), index) else 0
            if (playlistUrl != null && length >= 0 && index < urlLength && slashIndex > 0 && slashIndex <= urlLength) {
                return if (slashIndex == urlLength) playlistUrl else playlistUrl.substring(0, slashIndex)
            }
            return null
        }

        @JvmStatic
        fun absoluteUrls(base: String?, query: String?, arrayList: ArrayList<String?>, useQuery: Boolean): ArrayList<String?> {
            var base = base
            val baseEmpty = TextUtils.isEmpty(base)
            val lines: ArrayList<String?> = ArrayList()
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val url = arrayList.get(index)
                index++
                if (url != null) {
                    val sb = StringBuilder()
                    if (!baseEmpty) {
                        base = trimSlash(url, base)
                        sb.append(base)
                    }
                    sb.append(url)
                    if (useQuery) {
                        sb.append(query)
                    }
                    lines.add(sb.toString())
                }
            }
            return lines
        }

        @JvmStatic
        fun joinUrl(uri: String?, base: String?, query: String?, withQuery: Boolean): String? {
            val baseEmpty = TextUtils.isEmpty(base)
            val sb = StringBuilder()
            if (!baseEmpty) {
                sb.append(trimSlash(uri, base))
            }
            sb.append(uri)
            if (withQuery) {
                sb.append(query)
            }
            return sb.toString()
        }

        @JvmStatic
        fun fileNameOf(line: String?): String? {
            val length = if (line == null) 0 else line.length
            if (line == null || length == 0) {
                return null
            }
            val queryIndex = line.indexOf(63.toChar())
            val lastSlash =
                if (queryIndex <= 0 || queryIndex >= length) line.lastIndexOf(47.toChar()) else line.lastIndexOf(
                    47.toChar(),
                    queryIndex
                )
            if (lastSlash <= 0 || lastSlash > length) {
                return null
            }
            return if (lastSlash == length) line else line.substring(0, lastSlash)
        }

        @JvmStatic
        fun commonPrefix(playlistUrl: String?, urlLength: Int, length: Int, origin: String?): String? {
            val prefixEnd = if (TextUtils.isEmpty(origin) || length <= origin!!.length || playlistUrl == null || length <= 0) 0 else (length - 1)
            val lastSlash = if (TextUtils.isEmpty(origin) || length <= origin!!.length || playlistUrl == null || length <= 0 || prefixEnd > urlLength) 0 else playlistUrl.lastIndexOf(47.toChar(), prefixEnd)
            if (TextUtils.isEmpty(origin) || length <= origin!!.length || playlistUrl == null || length <= 0 || prefixEnd > urlLength || lastSlash <= 0 || lastSlash > urlLength) {
                return null
            }
            if (lastSlash <= origin.length) {
                return origin
            }
            return if (lastSlash == urlLength) playlistUrl else playlistUrl.substring(0, lastSlash)
        }

        @JvmStatic
        fun schemeHost(playlistUrl: String?): String? {
            val length = if (playlistUrl != null) playlistUrl.length else 0
            val schemeIndex = if (playlistUrl != null && length != 0) playlistUrl.indexOf("://") else 0
            val index = if (playlistUrl != null && length != 0 && schemeIndex > 0) (schemeIndex + 3) else 0
            if (playlistUrl != null && length != 0 && schemeIndex > 0 && index <= length) {
                return if (index == length) playlistUrl else playlistUrl.substring(0, index)
            }
            return null
        }

        @JvmStatic
        fun parseItemText(url: String?): M3u8Item? {
            val itemText = MainUtil.urlFromText(url)
            val parts: Array<String?>? = if (TextUtils.isEmpty(itemText)) null else itemText!!.split("<,>".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            val newUrl: String? = if (TextUtils.isEmpty(itemText) || parts == null || parts.size != 3) null else parts[0]
            val src: String? = if (TextUtils.isEmpty(itemText) || parts == null || parts.size != 3 || newUrl == null) null else parts[1]
            if (TextUtils.isEmpty(itemText) || parts == null || parts.size != 3 || newUrl == null || src == null) {
                return null
            }
            val audioPart = parts[2]
            val audioUrl = if ("isNull" == audioPart) null else audioPart
            val m3u8Item = M3u8Item()
            m3u8Item.url = newUrl
            m3u8Item.src = src
            m3u8Item.audioUrl = audioUrl
            return m3u8Item
        }

        @JvmStatic
        fun queryOf(url: String?): String? {
            val queryIndex = if (!TextUtils.isEmpty(url)) url!!.indexOf(63.toChar()) else 0
            if (!TextUtils.isEmpty(url) && queryIndex > 0 && queryIndex < url!!.length) {
                return url.substring(queryIndex)
            }
            return null
        }

        @JvmStatic
        fun trimSlash(keyUri: String?, base: String?): String? {
            if (TextUtils.isEmpty(base)) {
                return base
            }
            if (base!!.endsWith("/")) {
                if (!TextUtils.isEmpty(keyUri) && keyUri!!.startsWith("/")) {
                    return base.let { s -> s.substring(0, s.length - 1) }
                }
            } else if (TextUtils.isEmpty(keyUri) || !keyUri!!.startsWith("/")) {
                return base + "/"
            }
            return base
        }

        private fun stripSlash(uri: String?): String? {
            if (uri != null && uri.startsWith("/")) {
                return uri.substring(1)
            }
            return uri
        }

        /** Whether the query string should be appended to `uri` (it has none of its own).  */
        private fun needsQuery(uri: String?, query: String?): Boolean {
            return !TextUtils.isEmpty(query) && (TextUtils.isEmpty(uri) || uri!!.indexOf('?') == -1)
        }
    }
}
