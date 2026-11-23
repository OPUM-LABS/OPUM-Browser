package com.mycompany.app.subtitle

import com.mycompany.app.subtitle.Subtitle.nextTrimmed
import java.io.InputStream

/** SubRip SRT: numbered blocks of `start --> end` and text lines. */
internal object FormatSRT {
    @JvmStatic
    fun parse(input: InputStream, charset: String, listener: Subtitle.SubtitleListener): CueLists? {
        val reader = Subtitle.open(input, charset) ?: return null
        val result = Subtitle.newResult()
        val starts = HashSet<Int>()
        try {
            var expected = 1
            var line = reader.readLine()?.replace("﻿", "")
            while (line != null && !listener.isCancelled()) {
                var lineText = line.trim()
                if (lineText.isNotEmpty()) {
                    var caption: Caption? = null
                    if (lineText.toIntOrNull() == expected) {
                        expected++
                        caption = try {
                            lineText = reader.nextTrimmed()
                            Caption(Time.millis(lineText.substring(0, 12), ','), Time.millis(lineText.substring(lineText.length - 12), ','))
                        } catch (exception: Exception) {
                            null
                        }
                    }
                    if (caption != null) {
                        lineText = reader.nextTrimmed()
                        val body = StringBuilder()
                        while (lineText.isNotEmpty()) {
                            if (body.isNotEmpty()) body.append("<br />")
                            body.append(lineText)
                            lineText = reader.nextTrimmed()
                        }
                        caption.text = body.toString()
                        Subtitle.uniqueStart(caption, starts)
                        Subtitle.addCue(caption, result)
                    }
                    while (lineText.isNotEmpty()) lineText = reader.nextTrimmed()
                }
                line = reader.readLine()
            }
        } catch (exception: Exception) {
        }
        Subtitle.close(reader)
        return result
    }
}
