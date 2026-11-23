package com.mycompany.app.down

import android.text.TextUtils
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
object DownParseReddit {
    @JvmStatic
    fun parseRedditItem(url: String?): M3u8Item? {
        val urlText = MainUtil.urlFromText(url)
        val parts: Array<String?>? = if (TextUtils.isEmpty(urlText)) null else urlText!!.split("<,>".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        val src: String? = if (TextUtils.isEmpty(urlText) || parts == null || parts.size != 2) null else parts[0]
        if (TextUtils.isEmpty(urlText) || parts == null || parts.size != 2 || src == null) {
            return null
        }
        val audioPart = parts[1]
        val audioUrl = if ("isNull" == audioPart) null else audioPart
        val m3u8Item = M3u8Item()
        m3u8Item.src = src
        m3u8Item.audioUrl = audioUrl
        return m3u8Item
    }

    @JvmStatic
    fun redditSegments(src: String?): ArrayList<String?>? {
        if (TextUtils.isEmpty(src)) {
            return null
        }
        val arrayList: ArrayList<String?> = ArrayList()
        arrayList.add(src)
        return arrayList
    }
}
