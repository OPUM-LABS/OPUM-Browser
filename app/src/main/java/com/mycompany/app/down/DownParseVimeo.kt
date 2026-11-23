package com.mycompany.app.down

import android.content.Context
import android.text.TextUtils
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.main.MainUtil
import java.net.HttpURLConnection

@Suppress("deprecation")
object DownParseVimeo {
    @JvmStatic
    fun trimPath(depth: Int, base: String?): String? {
        var slashIndex: Int = 0
        if (depth == 0) {
            return base
        }
        val length = if (base != null) base.length else 0
        val lastSlash = if (base != null && length != 0) ((base.lastIndexOf(47.toChar())
                .also { slashIndex = it }) - 1) else 0
        if (base != null && length != 0 && lastSlash > 0 && lastSlash < length) {
            for (index3 in 0..<depth) {
                slashIndex = base.lastIndexOf(47.toChar(), slashIndex - 1)
                val index = slashIndex - 1
                if (index <= 0 || index >= length) {
                    return null
                }
            }
            val index = slashIndex + 1
            if (index > 0 && index < length) {
                return base.substring(0, index)
            }
        }
        return null
    }

    @JvmStatic
    fun vimeoItem(context: Context?, url: String?, referer: String?): M3u8Item? {
        val itemText = MainUtil.urlFromText(url)
        var suffix: String? = null
        val parts: Array<String?>? = if (!TextUtils.isEmpty(itemText)) itemText!!.split("<,>".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray() else null
        val downUrl: String? = if (!TextUtils.isEmpty(itemText) && parts != null && parts.size == 3) parts[0] else null
        val pathPart: String? = if (!TextUtils.isEmpty(itemText) && parts != null && parts.size == 3 && downUrl != null) parts[1] else null
        val src: String? = if (!TextUtils.isEmpty(itemText) && parts != null && parts.size == 3 && downUrl != null && pathPart != null) parts[2] else null
        val jsonObjectC: JsonObject? = if (!TextUtils.isEmpty(itemText) && parts != null && parts.size == 3 && downUrl != null && pathPart != null && src != null) loadConfig(
                context,
                downUrl,
                referer
            ) else null
        val levels = if (!TextUtils.isEmpty(itemText) && parts != null && parts.size == 3 && downUrl != null && pathPart != null && src != null && jsonObjectC != null) heightOf(pathPart) else 0
        if (!TextUtils.isEmpty(itemText) && parts != null && parts.size == 3 && downUrl != null && pathPart != null && src != null && jsonObjectC != null && levels != 0) {
            var baseUrl = trimPath(levels, downUrl)
            if (!TextUtils.isEmpty(baseUrl)) {
                if (levels == 0) {
                    suffix = pathPart
                } else {
                    val length = pathPart.length
                    val index = if (length != 0) (levels * 3) else 0
                    if (length != 0 && index < length) {
                        suffix = pathPart.substring(index)
                    }
                }
                if (!TextUtils.isEmpty(suffix)) {
                    baseUrl = "$baseUrl$suffix"
                }
                val m3u8Item = M3u8Item()
                m3u8Item.vimeoJson = jsonObjectC
                m3u8Item.base = baseUrl
                m3u8Item.src = src
                return m3u8Item
            }
        }
        return null
    }

    @JvmStatic
    fun loadConfig(context: Context?, downUrl: String?, referer: String?): JsonObject? {
        var httpURLConnectionP: HttpURLConnection?
        var configText: String?
        val configUrl = MainUtil.urlFromText(downUrl)
        if (!TextUtils.isEmpty(configUrl)) {
            try {
                httpURLConnectionP = MainUtil.openHttpConnection(0, 0, context, configUrl, referer, true)
                if (httpURLConnectionP != null) {
                    try {
                        configText = MainUtil.readTextFile(
                            context,
                            httpURLConnectionP.getInputStream(),
                            "gzip" == httpURLConnectionP.getContentEncoding()
                        )
                    } catch (unused: Exception) {
                        configText = null
                    } catch (unused2: OutOfMemoryError) {
                        MainUtil.restartLater(context)
                        configText = null
                    }
                    httpURLConnectionP.disconnect()
                    if (!TextUtils.isEmpty(configText)) {
                        try {
                            return JsonParser.parseString(configText).getAsJsonObject()
                        } catch (unused3: Exception) {
                        } catch (unused4: OutOfMemoryError) {
                            MainUtil.restartLater(context)
                        }
                    }
                }
            } catch (unused5: Exception) {
                httpURLConnectionP = null
            } catch (unused6: OutOfMemoryError) {
                httpURLConnectionP = null
            }
        }
        return null
    }

    @JvmStatic
    fun vimeoSegments(jsonObject: JsonObject?, base: String?, src: String?, audio: Boolean): ArrayList<String?>? {
        var baseUrl: String?
        if (jsonObject != null && !TextUtils.isEmpty(base) && (audio || !TextUtils.isEmpty(src))) {
            try {
                val clipId = jsonObject.get("clip_id")
                if (clipId != null && !TextUtils.isEmpty(clipId.getAsString())) {
                    val jsonArray = jsonObject.get(if (audio) "audio" else "video") as JsonArray?
                    val size = if (jsonArray != null) jsonArray.size() else 0
                    if (jsonArray != null && size != 0) {
                        val arrayList: ArrayList<String?> = ArrayList()
                        for (index in 0..<size) {
                            try {
                                val stream = jsonArray.get(index) as JsonObject?
                                val jsonElementD: JsonElement? = if (stream != null) stream.get("id") else null
                                if (stream != null && jsonElementD != null) {
                                    val entrySrc = jsonElementD.getAsString()
                                    if (!TextUtils.isEmpty(entrySrc) && (audio || entrySrc == src)) {
                                        val streamBase = stream.get("base_url")
                                        if (streamBase == null) {
                                            if (!audio) {
                                                break
                                            }
                                        } else {
                                            var basePath = streamBase.getAsString()
                                            if (TextUtils.isEmpty(basePath)) {
                                                if (!audio) {
                                                    break
                                                }
                                            } else {
                                                val height = heightOf(basePath)
                                                if (height != 0) {
                                                    baseUrl = trimPath(height, base)
                                                    if (TextUtils.isEmpty(baseUrl)) {
                                                        if (!audio) {
                                                            break
                                                        }
                                                    } else {
                                                        if (height != 0) {
                                                            basePath = if (basePath == null || basePath.length == 0 || height * 3 >= basePath.length) null else basePath.substring(height * 3)
                                                        }
                                                        if (TextUtils.isEmpty(basePath)) {
                                                            if (!audio) {
                                                                break
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    baseUrl = base
                                                }
                                                val initSegmentJson = stream.get("init_segment")
                                                if (initSegmentJson == null) {
                                                    if (!audio) {
                                                        break
                                                    }
                                                } else {
                                                    val initSegment = initSegmentJson.getAsString()
                                                    if (TextUtils.isEmpty(initSegment)) {
                                                        if (!audio) {
                                                            break
                                                        }
                                                    } else {
                                                        val segments =
                                                            stream.get("segments") as JsonArray?
                                                        if (segments == null) {
                                                            if (!audio) {
                                                                break
                                                            }
                                                        } else {
                                                            val segmentCount = segments.size()
                                                            if (segmentCount != 0) {
                                                                arrayList.add(initSegment)
                                                                val indexSegment =
                                                                    stream.get("index_segment")
                                                                if (indexSegment != null) {
                                                                    val segmentPath = indexSegment.getAsString()
                                                                    if (!TextUtils.isEmpty(segmentPath)) {
                                                                        arrayList.add(baseUrl + basePath + segmentPath)
                                                                    }
                                                                }
                                                                for (index2 in 0..<segmentCount) {
                                                                    try {
                                                                        val segment =
                                                                            segments.get(index2) as JsonObject?
                                                                        val segmentUrl: JsonElement? = if (segment != null) segment.get(
                                                                                "url"
                                                                            ) else null
                                                                        if (segment != null && segmentUrl != null) {
                                                                            val segmentPath =
                                                                                segmentUrl.getAsString()
                                                                            if (!TextUtils.isEmpty(
                                                                                    segmentPath
                                                                                )
                                                                            ) {
                                                                                arrayList.add(baseUrl + basePath + segmentPath)
                                                                            }
                                                                        }
                                                                    } catch (unused: Exception) {
                                                                    }
                                                                }
                                                                break
                                                            }
                                                            if (!audio) {
                                                                break
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (unused2: Exception) {
                            }
                        }
                        return arrayList
                    }
                }
            } catch (unused3: Exception) {
            }
        }
        return null
    }

    @JvmStatic
    fun heightOf(path: String?): Int {
        var index = 0
        val length = if (path == null) 0 else path.length
        if (path == null || length == 0) {
            return 0
        }
        var pos = 0
        while (path.startsWith("../", index)) {
            index += 3
            pos++
            if (index >= length) {
                break
            }
        }
        return pos
    }
}
