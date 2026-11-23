package com.mycompany.app.main

import android.text.TextUtils
import com.mycompany.app.subtitle.Subtitle

@Suppress("deprecation")
object MainUrl {
    @JvmField
    val mediaExts: Array<String> = arrayOf(".mp4", ".m3u8", ".webm", ".aac", ".mp3")
    @JvmField
    val imageExts: Array<String?> = arrayOf<String?>(".jpg", ".png")
    @JvmField
    val streamKeys: Array<String?> = arrayOf<String?>("mp4&", "m3u8&", "hls&")

    @JvmStatic
    fun innerHttpUrl(source: String?): String? {
        val decoded = MainUtil.urlDecode(source)
        if (TextUtils.isEmpty(decoded)) {
            return source
        }
        val httpIndex = decoded!!.lastIndexOf("http")
        if (httpIndex > 0) {
            return decoded.substring(httpIndex)
        }
        val spaceIndex = decoded.indexOf(32.toChar(), 4)
        return if (spaceIndex > 0) decoded.substring(0, spaceIndex) else decoded
    }

    @JvmStatic
    fun findHttpUrl(end: Int, index: Int, text: String?): String? {
        var index = index
        while (index < end) {
            val httpIndex = text!!.indexOf("http", index)
            if (httpIndex < index) {
                return null
            }
            val start = httpIndex + 4
            var ampIndex = text.indexOf(38.toChar(), start)
            if (ampIndex < start) {
                ampIndex = end
            }
            for (index2 in 0..1) {
                if (MainUtil.indexOfWord(text, start, ampIndex, imageExts[index2]) != -1) {
                    return innerHttpUrl(text.substring(httpIndex, ampIndex))
                }
            }
            index = ampIndex + 1
        }
        return null
    }

    @JvmStatic
    fun pathEnd(scriptInstallUrl: String?): Int {
        var length = if (scriptInstallUrl == null) 0 else scriptInstallUrl.length
        if (scriptInstallUrl == null || length == 0) {
            return -1
        }
        try {
            val queryIndex = scriptInstallUrl.indexOf(63.toChar())
            if (queryIndex >= 0) {
                length = queryIndex
            }
            var lastSlash = scriptInstallUrl.lastIndexOf(47.toChar(), length)
            if (lastSlash < 0) {
                lastSlash = 0
            }
            val hashIndex = MainUtil.lastIndexOfCharIn(scriptInstallUrl, '#', lastSlash, length)
            if (hashIndex > lastSlash && hashIndex < length) {
                length = hashIndex
            }
            val dotIndex = MainUtil.lastIndexOfCharIn(scriptInstallUrl, '.', lastSlash, length)
            if (dotIndex < lastSlash) {
                return -1
            }
            return dotIndex
        } catch (unused: Exception) {
            return -1
        }
    }

    @JvmStatic
    fun streamKeyIndex(start: Int, url: String?): Int {
        for (index in 0..2) {
            val keyIndex = url!!.indexOf(streamKeys[index]!!, start)
            if (keyIndex != -1 && url.get(keyIndex - 1) != '.') {
                return keyIndex
            }
        }
        return -1
    }

    @JvmStatic
    fun isMediaUrl(url: String?, strict: Boolean): Boolean {
        val end = pathEnd(url)
        if (end >= 0) {
            for (index in 0..4) {
                val ext = mediaExts[index]
                if (url!!.startsWith(ext, end)) {
                    val lastSlash = if (!strict || (ext != ".m3u8")) 0 else url.lastIndexOf(47.toChar(), end)
                    if (!strict || (ext != ".m3u8") || lastSlash < 0) {
                        return true
                    }
                    if (MainUtil.indexOfWord(url, lastSlash, end, "rendition") != -1 || MainUtil.indexOfWord(
                            url,
                            lastSlash,
                            end,
                            "chunk"
                        ) != -1
                    ) {
                        break
                    }
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun isSubtitleUrl(url: String?): Boolean {
        val end = pathEnd(url)
        if (end >= 0) {
            for (index in 0..9) {
                if (url!!.startsWith(Subtitle.DOT_EXTENSIONS[index], end)) {
                    val lastSlash = url.lastIndexOf(47.toChar(), end)
                    if (lastSlash < 0) {
                        return true
                    }
                    if (MainUtil.indexOfWord(url, lastSlash, end, "segment") != -1 || MainUtil.indexOfWord(
                            url,
                            lastSlash,
                            end,
                            "seg-"
                        ) != -1 || MainUtil.indexOfWord(url, lastSlash, end, "thumb") != -1
                    ) {
                        break
                    }
                    return true
                }
            }
        }
        return false
    }

    class MedItem(var first: String?, var second: String?)
}
